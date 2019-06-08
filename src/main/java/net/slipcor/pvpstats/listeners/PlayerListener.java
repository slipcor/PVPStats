package net.slipcor.pvpstats.listeners;

import net.slipcor.pvpstats.PVPStats;
import net.slipcor.pvpstats.api.DatabaseAPI;
import net.slipcor.pvpstats.api.PlayerStatisticsBuffer;
import net.slipcor.pvpstats.classes.Debugger;
import net.slipcor.pvpstats.core.Config;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

/**
 * Player Event Listener class
 *
 * @author slipcor
 */

public class PlayerListener implements Listener {
    private final PVPStats plugin;

    private final Debugger Debugger = new Debugger(3);

    private final Map<String, String> lastKill = new HashMap<>();
    private final Map<String, BukkitTask> killTask = new HashMap<>();

    public PlayerListener(final PVPStats instance) {
        this.plugin = instance;
    }

    /**
     * Hook into a player joining the server
     * <p>
     * - tell OPs that there was an update (if enabled=
     * - initiate players for stats
     *
     * @param event the PlayerJoinEvent
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        if (event.getPlayer().isOp() && plugin.getUpdater() != null) {
            plugin.getUpdater().message(event.getPlayer());

        }
        DatabaseAPI.initiatePlayer(event.getPlayer());
    }

    /**
     * Hook into a player quitting the server
     * <p>
     * - if set, reset the killstreaks of a player
     *
     * @param event the PlayerQuitEvent
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        if (plugin.config().getBoolean(Config.Entry.STATISTICS_RESET_KILLSTREAK_ON_QUIT)) {
            PlayerStatisticsBuffer.setStreak(event.getPlayer().getName(), 0);
        }
    }

    /**
     * Hook into a player death, count deaths and kills, where applicable
     *
     * @param event the PlayerDeathEvent
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDeath(final PlayerDeathEvent event) {
        if (plugin.ignoresWorld(event.getEntity().getWorld().getName())) {
            return;
        }

        Debugger.i("Player killed!", event.getEntity());

        if (event.getEntity().getKiller() == null) {
            Debugger.i("Killer is null", event.getEntity());
            if (plugin.config().getBoolean(Config.Entry.STATISTICS_COUNT_REGULAR_DEATHS)) {
                Debugger.i("Kill will be counted", event.getEntity());
                DatabaseAPI.AkilledB(null, event.getEntity());
            }
            return;
        }

        final Player attacker = event.getEntity().getKiller();
        final Player player = event.getEntity();

        if (plugin.config().getBoolean(Config.Entry.STATISTICS_CHECK_ABUSE)) {
            Debugger.i("- checking abuse", event.getEntity());
            if (lastKill.containsKey(attacker.getName()) && lastKill.get(attacker.getName()).equals(player.getName())) {
                Debugger.i("> OUT!", event.getEntity());
                return; // no logging!
            }

            lastKill.put(attacker.getName(), player.getName());
            int abusesec = plugin.config().getInt(Config.Entry.STATISTICS_ABUSE_SECONDS);
            if (abusesec > 0) {
                class RemoveLater implements Runnable {

                    @Override
                    public void run() {
                        lastKill.remove(attacker.getName());
                        killTask.remove(attacker.getName());
                    }

                }
                BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, new RemoveLater(), abusesec * 20L);

                if (killTask.containsKey(attacker.getName())) {
                    killTask.get(attacker.getName()).cancel();
                }

                killTask.put(attacker.getName(), task);
            }
        }
        // here we go, PVP!
        Debugger.i("Counting kill by " + attacker.getName(), event.getEntity());
        DatabaseAPI.AkilledB(attacker, player);
    }
}