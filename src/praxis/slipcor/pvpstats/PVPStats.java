package praxis.slipcor.pvpstats;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import praxis.slipcor.pvpstats.Updater.UpdateType;

/**
 * main class
 * 
 * @author slipcor
 * 
 */

public class PVPStats extends JavaPlugin {
	protected Plugin paHandler = null;
	protected lib.JesiKat.SQL.MySQLConnection sqlHandler; // MySQL handler

	// Settings Variables
	protected Boolean mySQL = false;
	protected String dbHost = null;
	protected String dbUser = null;
	protected String dbPass = null;
	protected String dbDatabase = null;
	protected String dbTable = null;
	protected String dbKillTable = null;
	protected int dbPort = 3306;

	private final PSListener entityListener = new PSListener(this);
	protected final PSPAListener paListener = new PSPAListener();
	private PSPAPluginListener paPluginListener;
	
	private Updater updater = null;
	
	public void onEnable() {
		final PluginDescriptionFile pdfFile = getDescription();
		
		getServer().getPluginManager().registerEvents(entityListener, this);
		
		loadConfig();
		loadHooks();
		
		if (getConfig().getBoolean("PVPArena")) {
			if (getServer().getPluginManager().isPluginEnabled("pvparena")) {
				getServer().getPluginManager().registerEvents(paListener, this);
			} else {
				paPluginListener = new PSPAPluginListener(this);
				getServer().getPluginManager().registerEvents(paPluginListener, this);
			}
		}
		
		if (getConfig().getBoolean("updatecheck", true)) {
			
			if (getConfig().getBoolean("autodownload", true)) {
				updater = new Updater(this, 32908, this.getFile(), UpdateType.NO_DOWNLOAD, false);
			} else {
				updater = new Updater(this, 32908, this.getFile(), UpdateType.DEFAULT, false);
			}
		}
		
		loadLanguage();
		
		getLogger().info("enabled. (version " + pdfFile.getVersion() + ")");
	}
	
	private void loadLanguage() {
		final File langFile = new File(this.getDataFolder(), "lang.yml");
		if (!langFile.exists()) {
			try {
				langFile.createNewFile();
			} catch (IOException e) {
				this.getLogger().warning("Language file could not be created. Using defaults!");
				e.printStackTrace();
			}
		}
		final YamlConfiguration cfg = YamlConfiguration.loadConfiguration(langFile);
		if (Language.load(cfg)) {
			try {
				cfg.save(langFile);
			} catch (IOException e) {
				this.getLogger().warning("Language file could not be written. Using defaults!");
				e.printStackTrace();
			}
		}
	}

	private void loadHooks() {
		final Plugin paPlugin = getServer().getPluginManager().getPlugin("pvparena");
		if (paPlugin != null && paPlugin.isEnabled()) {
			getLogger().info("<3 PVP Arena");
			this.paHandler = paPlugin;
		}
	}
	
	public Updater getUpdater() {
		return updater;
	}
	
	public void sendPrefixed(final CommandSender sender, final String message) {
		sender.sendMessage(Language.MSG_PREFIX + message);
	}

	public boolean onCommand(final CommandSender sender, final Command cmd, final String commandLabel, final String[] args) {
		
		if (args == null || args.length < 1 || !(args[0].equalsIgnoreCase("reload")||args[0].equalsIgnoreCase("wipe"))) {
			return parsecommand(sender, args);
		}
		
		if (args[0].equalsIgnoreCase("wipe")) {
			if (!sender.hasPermission("pvpstats.wipe")) {
				sendPrefixed(sender, Language.MSG_NOPERMWIPE.toString());
				return true;
			}
			
			if (args.length < 2) {
				PSMySQL.wipe(null);
				sendPrefixed(sender, Language.MSG_WIPED.toString());
			} else {
				PSMySQL.wipe(args[1]);
				sendPrefixed(sender, Language.MSG_WIPED.toString(args[1]));
			}
			
			return true;
		}
		
		if (!sender.hasPermission("pvpstats.reload")) {
			sendPrefixed(sender, Language.MSG_NOPERMRELOAD.toString());
			return true;
		}

		loadConfig();
		loadLanguage();
		sendPrefixed(sender, Language.MSG_RELOADED.toString());
		
		return true;
	}
	
	private boolean parsecommand(final CommandSender sender, final String[] args) {
		if (args == null || args.length < 1) {
			
			// /pvpstats - show your pvp stats
			
			class TellLater implements Runnable {

				@Override
				public void run() {
					final String[] info = PSMySQL.info(sender.getName());
					sender.sendMessage(info);
				}
				
			}
			Bukkit.getScheduler().runTaskAsynchronously(this, new TellLater());
			return true;
		}
		
		if (args[0].equals("?") || args[0].equals("help")) {
			return false;
		}
		
		int legacyTop = 0;
		
		try {
			legacyTop = Integer.parseInt(args[0]);
		} catch (Exception e) {
			
		}
		
		if (sender.hasPermission("pvpstats.top") && (args[0].equals("top") || legacyTop > 0)) {
		
			if (args.length > 1) {
				int amount = -1;
				
				try {
					amount = Integer.parseInt(args[1]);
				} catch (Exception e) {
					

					if (args.length > 2) {
				        // /pvpstats top [type] [amount] - show the top [amount] players of the type
						try {
							amount = Integer.parseInt(args[2]);
						} catch (Exception e2) {
							amount = 10;
						}
					}
					
			        //   /pvpstats top [type] - show the top 10 players of the type
					if (amount == -1) {
						amount = 10;
					}
					
					class RunLater implements Runnable {
						final String name;
						final int amount;
						RunLater(String name, int amount) {
							this.name = name;
							this.amount = amount;
						}
						@Override
						public void run() {
							String[] top = PSMySQL.top(amount, name);
							sender.sendMessage(Language.HEAD_LINE.toString());
							sender.sendMessage(Language.HEAD_HEADLINE.toString(
									String.valueOf(amount),
									Language.valueOf("HEAD_"+name).toString()));
							sender.sendMessage(Language.HEAD_LINE.toString());
							

							int pos = 1;
							for (String stat : top) {
								sender.sendMessage(pos++ + ": "+stat);
							}
						}
						
					}
					
					if (args[1].equals("kills")) {
						Bukkit.getScheduler().runTaskAsynchronously(this, new RunLater("KILLS", amount));
					} else if (args[1].equals("deaths")) {
						Bukkit.getScheduler().runTaskAsynchronously(this, new RunLater("DEATHS", amount));
					} else if (args[1].equals("streak")) {
						Bukkit.getScheduler().runTaskAsynchronously(this, new RunLater("STREAK", amount));
					} else {
						return false;
					}
					
					return true;
				}
		        //   /pvpstats top [amount] - show the top [amount] players (K-D)
				args[0] = args[1];
				legacyTop = 1;
			}
			
			// /pvpstats [amount] - show the top [amount] players (K-D)
			try {
				int count = legacyTop==0?10:Integer.parseInt(args[0]);
				if (count > 20) {
					count = 20;
				}
				if (legacyTop == 0) {
					args[0] = String.valueOf(count);
				}
				class RunLater implements Runnable {
					int count;
					RunLater(int i) {
						count= i;
					}
					@Override
					public void run() {
						final String[] top = PSMySQL.top(count, "K-D");
						sender.sendMessage(Language.HEAD_LINE.toString());
						sender.sendMessage(Language.HEAD_HEADLINE.toString(
								args[0],
								Language.HEAD_RATIO.toString()));
						sender.sendMessage(Language.HEAD_LINE.toString());
						int pos = 1;
						for (String stat : top) {
							sender.sendMessage(String.valueOf(pos++) + ": "+stat);
						}
					}
					
				}
				Bukkit.getScheduler().runTaskAsynchronously(this, new RunLater(count));
				
				return true;
			} catch (Exception e) {
				return false;
			}
		
		}
		// /pvpstats [player] - show player's pvp stats
		
		class TellLater implements Runnable {

			@Override
			public void run() {
				final String[] info = PSMySQL.info(args[0]);
				sender.sendMessage(info);
			}
			
		}
		Bukkit.getScheduler().runTaskAsynchronously(this, new TellLater());
		return true;
	}

	private void loadConfig() {

		getConfig().options().copyDefaults(true);
		saveConfig();

		// get variables from settings handler
 		if (getConfig().getBoolean("MySQL", false)) {
 			this.mySQL = getConfig().getBoolean("MySQL", false);
 			this.dbHost = getConfig().getString("MySQLhost", "");
 			this.dbUser = getConfig().getString("MySQLuser", "");
 			this.dbPass = getConfig().getString("MySQLpass", "");
 			this.dbDatabase = getConfig().getString("MySQLdb", "");
 			this.dbTable = getConfig().getString("MySQLtable", "pvpstats");
 			this.dbKillTable = getConfig().getString("MySQLkilltable", "pvpkillstats");
 			this.dbPort = getConfig().getInt("MySQLport", 3306);
 		}
 		
 		// Check Settings
 		if (this.mySQL) {
 			if (this.dbHost.equals("")) { this.mySQL = false;  }
 			else if (this.dbUser.equals("")) { this.mySQL = false; }
 			else if (this.dbPass.equals("")) { this.mySQL = false; }
 			else if (this.dbDatabase.equals("")) { this.mySQL = false; }
 		}
 		
 		// Enabled SQL/MySQL
 		if (this.mySQL) {
 			// Declare MySQL Handler
			try {
				sqlHandler = new lib.JesiKat.SQL.MySQLConnection(dbTable, dbHost, dbPort, dbDatabase, dbUser,
						dbPass);
			} catch (InstantiationException e1) {
				e1.printStackTrace();
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}
 			
			getLogger().info("MySQL Initializing");
 			// Initialize MySQL Handler
 			
 				if (sqlHandler.connect(true)) {
 					getLogger().info("MySQL connection successful");
 	 				// Check if the tables exist, if not, create them
 					if (!sqlHandler.tableExists(dbDatabase,dbTable)) {
 						// normal table doesnt exist, create both
 						
 						getLogger().info("Creating table "+dbTable);
 						final String query = "CREATE TABLE `"+dbTable+"` ( " +
 								"`id` int(5) NOT NULL AUTO_INCREMENT, " +
 								"`name` varchar(42) NOT NULL, " +
 								"`kills` int(8) not null default 0, " +
 								"`deaths` int(8) not null default 0, " +
 								"`streak` int(8) not null default 0, " +
 								"PRIMARY KEY (`id`) ) AUTO_INCREMENT=1 ;";
 						try {
 							sqlHandler.executeQuery(query, true);
 						} catch (SQLException e) {
 							e.printStackTrace();
 						}
 						
 						getLogger().info("Creating table "+dbKillTable);
 						final String query2 = "CREATE TABLE `"+dbKillTable+"` ( " +
 								"`id` int(16) NOT NULL AUTO_INCREMENT, " +
 								"`name` varchar(42) NOT NULL, " +
 								"`kill` int(1) not null default 0, " +
 								"`time` int(16) not null default 0, " +
 								"PRIMARY KEY (`id`) ) AUTO_INCREMENT=1 ;";
 						try {
 							sqlHandler.executeQuery(query2, true);
 						} catch (SQLException e) {
 							e.printStackTrace();
 						}
 					} else {
 						// normal exists, do we need to update?
 						try {
							List<String> columns = Arrays.asList(sqlHandler.getColumns(dbDatabase, dbTable));
							
							if (!columns.contains("streak")) {
								final String queryA = "ALTER TABLE `"+dbTable+"` ADD `streak` int(8) not null default 0; ";
								final String queryB = "ALTER TABLE `"+dbTable+"` CHANGE `deaths` `deaths` INT( 8 ) NOT NULL DEFAULT 0;";
								final String queryC = "ALTER TABLE `"+dbTable+"` CHANGE `kills` `kills` INT( 8 ) NOT NULL DEFAULT 0;";
	 	 						try {
	 	 							sqlHandler.executeQuery(queryA, true);
	 	 		 					getLogger().info("Added 'streak' column to MySQL!");
	 	 							sqlHandler.executeQuery(queryB, true);
	 	 		 					getLogger().info("Updated MySQL field 'deaths'");
	 	 							sqlHandler.executeQuery(queryC, true);
	 	 		 					getLogger().info("Updated MySQL field 'kills'");
	 	 						} catch (SQLException e2) {
	 	 							e2.printStackTrace();
	 	 						}
							}
						} catch (SQLException e1) {
							e1.printStackTrace();
						}
 						
 						if (!sqlHandler.tableExists(dbDatabase,dbKillTable)) {
 	 						// second table doesnt exist, create that
 	 						
 	 						getLogger().info("Creating table "+dbKillTable);
 	 						final String query = "CREATE TABLE `"+dbKillTable+"` ( " +
 	 								"`id` int(16) NOT NULL AUTO_INCREMENT, " +
 	 								"`name` varchar(42) NOT NULL, " +
 	 								"`kill` int(1) not null default 0, " +
 	 								"`time` int(16) not null default 0, " +
 	 								"PRIMARY KEY (`id`) ) AUTO_INCREMENT=1 ;";
 	 						try {
 	 							sqlHandler.executeQuery(query, true);
 	 						} catch (SQLException e) {
 	 							e.printStackTrace();
 	 						}
 	 					} else {
 	 						// did we really add the "tine" ??!!
 	 						
 							List<String> columns = new ArrayList<String>();
							try {
								columns = Arrays.asList(sqlHandler.getColumns(dbDatabase, dbKillTable));
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							if (columns.contains("tine")) {
								final String query = "ALTER TABLE `"+dbTable+"` CHANGE `tine` `time` INT( 16 ) NOT NULL DEFAULT 0;";
								
	 	 						try {
	 	 							sqlHandler.executeQuery(query, true);
	 	 		 					getLogger().info("Fixed MySQL field 'time'");
	 	 						} catch (SQLException e2) {
	 	 							e2.printStackTrace();
	 	 						}
							}
 	 					}
 					}
 				} else {
 					getLogger().severe("MySQL connection failed");
 					this.mySQL = false;
 				}
 			PSMySQL.initiate(this);
 		} else {
 			Bukkit.getServer().getPluginManager().disablePlugin(this);
		}
	}

	public void onDisable() {
		getLogger().info("disabled. (version " + getDescription().getVersion() + ")");
	}

}
