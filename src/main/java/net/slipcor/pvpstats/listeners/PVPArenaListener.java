package net.slipcor.pvpstats.listeners;

import net.slipcor.pvparena.events.PADeathEvent;
import net.slipcor.pvparena.events.PAKillEvent;
import net.slipcor.pvpstats.PVPStats;
import net.slipcor.pvpstats.api.DatabaseAPI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * PVP Arena Event Listener class
 */

public class PVPArenaListener implements Listener {

    private final PVPStats plugin;

    public PVPArenaListener(final PVPStats plugin) {
        this.plugin = plugin;
    }

    /**
     * Hook into a Player killing someone
     *
     * @param event the PAKillEvent
     */
    @EventHandler
    public void onArenaKill(final PAKillEvent event) {
        if (plugin.ignoresWorld(event.getPlayer().getWorld().getName())) {
            return;
        }
        DatabaseAPI.AkilledB(event.getPlayer(), null);
    }

    /**
     * Hook into a Player being killed
     *
     * @param event the PADeathEvent
     */
    @EventHandler
    public void onArenaDeath(final PADeathEvent event) {
        if (plugin.ignoresWorld(event.getPlayer().getWorld().getName()) || !event.isPVP()) {
            return;
        }
        DatabaseAPI.AkilledB(null, event.getPlayer());
    }
}