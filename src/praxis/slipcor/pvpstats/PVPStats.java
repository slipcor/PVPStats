package praxis.slipcor.pvpstats;

import java.sql.SQLException;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * main class
 * 
 * @author slipcor
 * 
 * @version: v0.1.1
 * 
 */

public class PVPStats extends JavaPlugin {
	static final Logger log = Logger.getLogger("Minecraft");
	Plugin paHandler = null;
	//mySQL access
	protected lib.JesiKat.SQL.MySQLConnection sqlHandler; // MySQL handler

	// Settings Variables
	Boolean MySQL = false;
	String dbHost = null;
	String dbUser = null;
	String dbPass = null;
	String dbDatabase = null;

	private final PSListener entityListener = new PSListener(this);
	final PSPAListener paListener = new PSPAListener(this);
	
	public void onEnable() {
		PluginDescriptionFile pdfFile = getDescription();
		
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(entityListener, this);
		
		load_config();
		load_hooks();
		
		if (paHandler != null) {
			log.info("[PVP Stats] registering PVP Arena events");
			pm.registerEvents(paListener, this);
		}
		
		UpdateManager.updateCheck(this);
		
		log.info("[PVP Stats] enabled. (version " + pdfFile.getVersion() + ")");
	}
	
	private void load_hooks() {
		Plugin pa = getServer().getPluginManager().getPlugin("pvparena");
		if (pa != null && pa.isEnabled()) {
			log.info("[PVP Stats] <3 PVP Arena");
			this.paHandler = pa;
		}
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (!commandLabel.equals("pvpstats"))
			return true;
		
		if (args == null || args.length < 1 || !args[0].equalsIgnoreCase("reload")) {
			return parsecommand(sender, args);
		}
		
		try {
			Player p = (Player) sender;
			if (!p.isOp() && !p.hasPermission("pvpstats.reload")) {
				p.sendMessage("[PVP Stats] No permission to reload!");
				return true;
			}
		} catch (Exception e) {
			// nothing
		}
		
		if (args[0].equalsIgnoreCase("reload")) {
			load_config();
		} else {
			return false; // show command
		}
		
		sender.sendMessage("[PVP Stats] config reloaded!");
		
		return true;
	}
	
	private boolean parsecommand(CommandSender sender, String[] args) {
		if (args == null || args.length < 1) {
			String[] info = PSMySQL.info(sender.getName());
			int i = 1;
			for (String stat : info) {
				sender.sendMessage(String.valueOf(i++) + ": "+stat);
			}
			return true;
		}
		try {
			int count = Integer.parseInt(args[0]);
			String[] top = PSMySQL.top(count);
			sender.sendMessage("---------------");
			sender.sendMessage("PVP Stats Top "+args[0]);
			sender.sendMessage("---------------");
			int i = 1;
			for (String stat : top) {
				sender.sendMessage(String.valueOf(i++) + ": "+stat);
			}
			return true;
		} catch (Exception e) {
			String[] info = PSMySQL.info(args[0]);
			int i = 1;
			for (String stat : info) {
				sender.sendMessage(String.valueOf(i++) + ": "+stat);
			}
			return true;
		}
	}

	private void load_config() {

		getConfig().options().copyDefaults(true);
		saveConfig();

		// get variables from settings handler
 		if (getConfig().getBoolean("MySQL", false)) {
 			this.MySQL = getConfig().getBoolean("MySQL", false);
 			this.dbHost = getConfig().getString("MySQLhost", "");
 			this.dbUser = getConfig().getString("MySQLuser", "");
 			this.dbPass = getConfig().getString("MySQLpass", "");
 			this.dbDatabase = getConfig().getString("MySQLdb", "");
 		}
 		
 		// Check Settings
 		if (this.MySQL) {
 			if (this.dbHost.equals("")) { this.MySQL = false;  }
 			else if (this.dbUser.equals("")) { this.MySQL = false; }
 			else if (this.dbPass.equals("")) { this.MySQL = false; }
 			else if (this.dbDatabase.equals("")) { this.MySQL = false; }
 		}
 		
 		// Enabled SQL/MySQL
 		if (this.MySQL) {
 			// Declare MySQL Handler
			try {
				sqlHandler = new lib.JesiKat.SQL.MySQLConnection(dbHost, 3306, dbDatabase, dbUser,
						dbPass);
			} catch (InstantiationException e1) {
				e1.printStackTrace();
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}
 			
			log.info("[PVP Stats] MySQL Initializing");
 			// Initialize MySQL Handler
 			
 				if (sqlHandler.connect(true)) {
 					log.info("[PVP Stats] MySQL connection successful");
 	 				// Check if the tables exist, if not, create them
 					if (!sqlHandler.tableExists(dbDatabase,"pvpstats")) {
 						log.info("[PVP Stats] Creating table pvpstats");
 						String query = "CREATE TABLE `pvpstats` ( `id` int(5) NOT NULL AUTO_INCREMENT, `name` varchar(42) NOT NULL, `kills` int(8), `deaths` int(8), PRIMARY KEY (`id`) ) AUTO_INCREMENT=1 ;";
 						try {
 							sqlHandler.executeQuery(query, true);
 						} catch (SQLException e) {
 							e.printStackTrace();
 						}
 					}
 				} else {
 					log.severe("[PVP Stats] MySQL connection failed");
 					this.MySQL = false;
 				}
 			PSMySQL.plugin = this;
 		} else {
 			Bukkit.getServer().getPluginManager().disablePlugin(this);
		}
	}

	public void onDisable() {
		PluginDescriptionFile pdfFile = getDescription();
		log.info("[PVP Stats] disabled. (version " + pdfFile.getVersion() + ")");
	}

}