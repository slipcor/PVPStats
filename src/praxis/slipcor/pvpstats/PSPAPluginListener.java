package praxis.slipcor.pvpstats;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;

/**
 * PVP Arena Plugin listener class
 * 
 * Waits for the PVP Arena Plugin to enable
 *
 */

public class PSPAPluginListener implements Listener {
	final PVPStats plugin;
	public PSPAPluginListener(final PVPStats plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPluginEnable(final PluginEnableEvent event) {
		if (plugin.paHandler != null) {
			return;
		}
		if (event.getPlugin().getName().equals("pvparena")) {
			plugin.getLogger().info("<3 PVP Arena");
			plugin.paHandler = event.getPlugin();
			plugin.getServer().getPluginManager().registerEvents(plugin.paListener, plugin);
		}
	}
}