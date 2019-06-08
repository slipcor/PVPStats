package net.slipcor.pvpstats.listeners;

import net.slipcor.pvpstats.PVPStats;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;

/**
 * Plugin Event Listener class
 */

public class PluginListener implements Listener {
    final PVPStats plugin;

    public PluginListener(final PVPStats plugin) {
        this.plugin = plugin;
    }

    /**
     * Hook into Plugins enabling, to maybe hook into them
     *
     * @param event the PluginEnableEvent
     */
    @EventHandler
    public void onPluginEnable(final PluginEnableEvent event) {
        if (plugin.getPAHandler() != null) {
            return;
        }
        if (event.getPlugin().getName().equals("pvparena")) {
            plugin.getLogger().info("<3 PVP Arena");
            plugin.setPAHandler(event.getPlugin());
            plugin.getServer().getPluginManager().registerEvents(plugin.getPAListener(), plugin);
        }
    }
}