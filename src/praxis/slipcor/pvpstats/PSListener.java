package praxis.slipcor.pvpstats;

import java.util.HashMap;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
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
	public PVPStats plugin;
	
	private HashMap<String, String> lastKill = new HashMap<String, String>();

	public PSListener(PVPStats instance) {
		this.plugin = instance;
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		if (!player.isOp()) {
			return; // no OP => OUT
		}
		UpdateManager.message(player);
	}
	
	@EventHandler
	public void onEntityDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		
		if (!(player.getLastDamageCause() instanceof EntityDamageByEntityEvent)) {
			return; // no PVP
		}
		EntityDamageByEntityEvent lastEvent = (EntityDamageByEntityEvent) player.getLastDamageCause();
		
		Entity attacker = lastEvent.getDamager();
		if (lastEvent.getCause() == DamageCause.PROJECTILE) {
			attacker = ((Projectile)attacker).getShooter();
		}

		if (!(attacker instanceof Player)) {
			return; // no PVP
		}
		Player playera = (Player) attacker;
		
		if (plugin.getConfig().getBoolean("checkabuse")) {
			
			if (lastKill.containsKey(playera.getName()) && lastKill.get(playera.getName()).equals(player.getName())) {
				return; // no logging!
			}
			
			lastKill.put(playera.getName(), player.getName());
		}
		
		// here we go, PVP!
		PSMySQL.incKill(playera);
		PSMySQL.incDeath(player);
	}
	
	@EventHandler
	public void onPluginEnable(PluginEnableEvent event) {
		if (plugin.paHandler != null || !plugin.getConfig().getBoolean("PVPArena")) {
			return;
		}
		if (!event.getPlugin().getName().equals("pvparena")) {
			PVPStats.log.info("[PVP Stats] <3 PVP Arena");
			plugin.paHandler = event.getPlugin();
			plugin.getServer().getPluginManager().registerEvents(plugin.paListener, plugin);
		}
	}
}