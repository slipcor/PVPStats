package net.slipcor.pvpstats.listeners;

import net.slipcor.pvpstats.PVPStats;
import net.slipcor.pvpstats.api.DatabaseAPI;
import net.slipcor.pvpstats.api.PlayerStatisticsBuffer;
import net.slipcor.pvpstats.classes.Debugger;
import net.slipcor.pvpstats.classes.PlayerDamageHistory;
import net.slipcor.pvpstats.core.Config;
import net.slipcor.pvpstats.core.Language;
import net.slipcor.pvpstats.display.SignDisplay;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    private boolean lock = false;

    private int assistSeconds = 60;

    private final Map<UUID, PlayerDamageHistory> lastDamage = new HashMap<>();

    public PlayerListener(final PVPStats instance) {
        this.plugin = instance;
        assistSeconds = this.plugin.config().getInt(Config.Entry.STATISTICS_ASSIST_SECONDS);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.getEntityType() != EntityType.PLAYER) {
            return;
        }

        Player attacked = (Player) event.getEntity();
        Player attacker = null;

        if (event.getDamager() instanceof Player && !event.getDamager().equals(attacked)) {
            attacker = (Player) event.getDamager();
        } else if (event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();
            if (projectile.getShooter() instanceof Player && !projectile.getShooter().equals(attacked)) {
                attacker = (Player) projectile.getShooter();
            }
        }

        if (attacker == null) {
            return;
        }

        if (lastDamage.containsKey(attacked.getUniqueId())) {
            PlayerDamageHistory history = lastDamage.get(attacked.getUniqueId());
            history.commitPlayerDamage(attacker);
        } else {
            PlayerDamageHistory history = new PlayerDamageHistory();
            history.commitPlayerDamage(attacker);
            lastDamage.put(attacked.getUniqueId(), history);
        }
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
            PlayerStatisticsBuffer.setStreak(event.getPlayer().getUniqueId(), 0);
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

        final Player player = event.getEntity();
        Debugger.i("Player killed!", player);
        Player attacker = event.getEntity().getKiller();

        if (attacker == null) {
            Debugger.i("Killer is null", player);

            if (lastDamage.containsKey(player.getUniqueId())) {
                PlayerDamageHistory history = lastDamage.get(player.getUniqueId());
                List<UUID> damagers = history.getLastDamage(assistSeconds);
                if (damagers.size() > 0) {
                    attacker = Bukkit.getPlayer(damagers.get(0));
                }
                lastDamage.remove(player.getUniqueId()); // clear map for next kill
            }

            if (attacker == null) {
                Debugger.i("Killer is still null", player);
                if (plugin.config().getBoolean(Config.Entry.STATISTICS_COUNT_REGULAR_DEATHS)) {
                    Debugger.i("Kill will be counted", event.getEntity());
                    DatabaseAPI.AkilledB(null, event.getEntity());
                }
                return;
            }
        }


        if (plugin.config().getBoolean(Config.Entry.STATISTICS_CHECK_ABUSE)) {
            Debugger.i("- checking abuse", event.getEntity());
            if (lastKill.containsKey(attacker.getName()) && lastKill.get(attacker.getName()).equals(player.getName())) {
                Debugger.i("> OUT!", event.getEntity());
                return; // no logging!
            }

            lastKill.put(attacker.getName(), player.getName());
            int abusesec = plugin.config().getInt(Config.Entry.STATISTICS_ABUSE_SECONDS);
            if (abusesec > 0) {
                Player finalAttacker = attacker;
                class RemoveLater implements Runnable {

                    @Override
                    public void run() {
                        lastKill.remove(finalAttacker.getName());
                        killTask.remove(finalAttacker.getName());
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

    /**
     * Hook into a player interacting
     *
     * @param event the PlayerInteractEvent
     */
    @EventHandler(ignoreCancelled = true)
    public void playerInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block != null) {
            if (block.getState() instanceof Sign) {
                SignDisplay display = SignDisplay.byLocation(block.getLocation());

                if (display == null) {
                    // it does not exist yet, try creating it
                    display = SignDisplay.init(block.getLocation());
                    if (display != null) {
                        if (!display.isValid()) {
                            // we could not create it!
                            PVPStats.getInstance().sendPrefixed(event.getPlayer(), Language.ERROR_DISPLAY_INVALID.toString());
                        } else {
                            PVPStats.getInstance().sendPrefixed(event.getPlayer(),
                                    Language.MSG_DISPLAY_CREATED.toString(event.getClickedBlock().getLocation().toString()));

                            SignDisplay.saveAllDisplays();
                        }
                    }
                    return;
                }
                event.setCancelled(!event.getPlayer().isOp());
                if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
                    return;
                }

                display.cycleSortColumn();
                PVPStats.getInstance().sendPrefixed(
                        event.getPlayer(),
                        Language.MSG_DISPLAY_COLUMN.toString(display.getSortColumn().name()));
            } else if (SignDisplay.needsProtection(event.getPlayer().getLocation())) {
                event.setCancelled(!event.getPlayer().isOp());
            }
        }
    }
}