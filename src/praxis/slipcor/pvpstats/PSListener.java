package praxis.slipcor.pvpstats;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.PluginEnableEvent;

/**
 * Listener class
 * 
 * @author slipcor
 * 
 * @version v0.1.2
 *
 */

public class PSListener implements Listener {
	private final PVPStats plugin;
	
	private final Map<String, String> lastKill = new HashMap<String, String>();

	public PSListener(final PVPStats instance) {
		this.plugin = instance;
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onPlayerJoin(final PlayerJoinEvent event) {
		if (event.getPlayer().isOp()) {
			UpdateManager.message(event.getPlayer());
		}
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onPlayerDeath(final PlayerDeathEvent event) {

		if (event.getEntity() == null || event.getEntity().getKiller() == null) {
			return;
		}

		final Player attacker = event.getEntity().getKiller();
		final Player player = event.getEntity();
		
		if (plugin.getConfig().getBoolean("checkabuse")) {
			
			if (lastKill.containsKey(attacker.getName()) && lastKill.get(attacker.getName()).equals(player.getName())) {
				return; // no logging!
			}
			
			lastKill.put(attacker.getName(), player.getName());
		}
		// here we go, PVP!
		PSMySQL.incDeath(player);
		PSMySQL.incKill(attacker);
	}
	
	@EventHandler
	public void onPluginEnable(final PluginEnableEvent event) {
		if (plugin.paHandler != null || !plugin.getConfig().getBoolean("PVPArena")) {
			return;
		}
		if (!event.getPlugin().getName().equals("pvparena")) {
			plugin.getLogger().info("<3 PVP Arena");
			plugin.paHandler = event.getPlugin();
			plugin.getServer().getPluginManager().registerEvents(plugin.paListener, plugin);
		}
	}
}