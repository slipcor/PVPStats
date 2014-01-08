package praxis.slipcor.pvpstats;

import java.util.List;

import net.slipcor.pvparena.events.PADeathEvent;
import net.slipcor.pvparena.events.PAKillEvent;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * PVP Arena listener class
 * 
 * Checks the PVP Arena events to possibly ignore kills happening there
 *
 */

public class PSPAListener implements Listener {
	
	private final PVPStats plugin;
	
	public PSPAListener(final PVPStats plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onArenaKill(final PAKillEvent event) {
		if (plugin.ignoresWorld(event.getPlayer().getWorld().getName())) {
			return;
		}
		PSMySQL.incKill(event.getPlayer());
	}
	
	@EventHandler
	public void onArenaDeath(final PADeathEvent event) {
		if (plugin.ignoresWorld(event.getPlayer().getWorld().getName()) ||
				!event.isPVP()) {
			return;
		}
		PSMySQL.incDeath(event.getPlayer());
	}
}