package praxis.slipcor.pvpstats;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitTask;

import praxis.slipcor.pvpstats.Updater.UpdateResult;

/**
 * Listener class
 * 
 * @author slipcor
 *
 */

public class PSListener implements Listener {
	private final PVPStats plugin;
	
	private final Map<String, String> lastKill = new HashMap<String, String>();
	private final Map<String, BukkitTask> killTask = new HashMap<String, BukkitTask>();

	public PSListener(final PVPStats instance) {
		this.plugin = instance;
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onPlayerJoin(final PlayerJoinEvent event) {
		if (event.getPlayer().isOp() && plugin.getUpdater() != null) {
			
			final UpdateResult test = plugin.getUpdater().getResult();
			
			switch (test) {
			case SUCCESS:
				event.getPlayer().sendMessage(Language.MSG_UPDATED.toString());
				break;
			case UPDATE_AVAILABLE:
				event.getPlayer().sendMessage(Language.MSG_UPDATE.toString());
			default:
				break;
			}
		
		}
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onPlayerDeath(final PlayerDeathEvent event) {
		
		if (event.getEntity() == null || event.getEntity().getKiller() == null ||
				plugin.ignoresWorld(event.getEntity().getWorld().getName())) {
			return;
		}

		final Player attacker = event.getEntity().getKiller();
		final Player player = event.getEntity();
		
		if (plugin.getConfig().getBoolean("checkabuse")) {
			
			if (lastKill.containsKey(attacker.getName()) && lastKill.get(attacker.getName()).equals(player.getName())) {
				return; // no logging!
			}
			
			lastKill.put(attacker.getName(), player.getName());
			int abusesec = plugin.getConfig().getInt("abuseseconds");
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
		
		PSMySQL.AkilledB(attacker, player);
	}
}