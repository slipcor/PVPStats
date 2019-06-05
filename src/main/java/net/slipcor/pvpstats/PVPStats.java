package net.slipcor.pvpstats;

import net.slipcor.pvpstats.api.DatabaseConnection;
import net.slipcor.pvpstats.commands.*;
import net.slipcor.pvpstats.impl.FlatFileConnection;
import net.slipcor.pvpstats.impl.MySQLConnection;
import net.slipcor.pvpstats.impl.SQLiteConnection;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * main class
 *
 * @author slipcor
 */

public class PVPStats extends JavaPlugin {
    Plugin paHandler = null;
    final Map<String, AbstractCommand> commandMap = new HashMap<>();
    final List<AbstractCommand> commandList = new ArrayList<>();
    DatabaseConnection sqlHandler; // MySQL handler

    // Settings Variables
    boolean connection = false;

    private boolean mySQL = false;
    private boolean SQLite = false;

    private String dbHost = null;
    private String dbUser = null;
    private String dbPass = null;
    private String dbDatabase = null;
    private String dbTable = null;
    private String dbKillTable = null;
    private String dbOptions = "?autoReconnect=true";
    private int dbPort = 3306;

    private final PSListener entityListener = new PSListener(this);
    final PSPAListener paListener = new PSPAListener(this);

    private Updater updater = null;
    private static PVPStats instance;

    private final Debug DEBUG = new Debug(8);

    public void onEnable() {
        instance = this;
        try {

            OfflinePlayer.class.getDeclaredMethod("getUniqueId");
        } catch (Exception e) {
            getLogger().info("Your server is still not ready for UUIDs? Use PVP Stats older than v0.8.25.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        final PluginDescriptionFile pdfFile = getDescription();

        getServer().getPluginManager().registerEvents(entityListener, this);

        loadConfig();
        loadCommands();

        if(!this.connection) {
            getLogger().severe("Database not connected, plugin DISABLED!");
            getServer().getPluginManager().disablePlugin(this);
            return; //to ensure the rest of the plugins code is not executed as this can lead to problems.
        }
        loadHooks();

        if (getConfig().getBoolean("PVPArena")) {
            if (getServer().getPluginManager().isPluginEnabled("pvparena")) {
                getServer().getPluginManager().registerEvents(paListener, this);
            } else {
                PSPAPluginListener paPluginListener = new PSPAPluginListener(this);
                getServer().getPluginManager().registerEvents(paPluginListener, this);
            }
        }

        updater = new Updater(this, getFile());

        loadLanguage();

        if(getConfig().getBoolean("tracker", true)) { //only call the task if true
            getServer().getScheduler().runTaskTimer(this, new Tracker(this), 0L, 72000L);
        }

        if (getConfig().getBoolean("clearonstart", true)) {

            Bukkit.getScheduler().runTaskLater(this, new Runnable() { //run the task within its own runnable no need for an imbedded class
                @Override
                public void run() {
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "pvpstats cleanup");
                }
            }, 5000L);
        }

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            getLogger().info("PVPStats - PlaceholderAPI found.");
            new PlaceholderAPIHook().register();
        }

        Debug.load(this, Bukkit.getConsoleSender());

        getLogger().info("enabled. (version " + pdfFile.getVersion() + ")");
    }

    public void loadLanguage() {
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
        if (!"".equals(message)) {
            sender.sendMessage(Language.MSG_PREFIX + message);
        }
    }

    public boolean onCommand(final CommandSender sender, final Command cmd, final String commandLabel, final String[] args) {

        DEBUG.i("onCommand!", sender);

        final AbstractCommand acc = (args.length > 0) ? commandMap.get(args[0].toLowerCase()) : null;
        if (acc != null) {
            acc.commit(sender, args);
            return true;
        }

        if (args.length < 1) {
            commandMap.get("show").commit(sender, new String[0]);
            return true;
        }
        int legacy = 0;
        try {
            legacy = Integer.parseInt(args[0]);
        } catch (Exception e) {
        }

        if (legacy > 0) {
            commandMap.get("top").commit(sender, args);
            return true;
        }

        boolean found = false;
        for (AbstractCommand command : commandList) {
            if (command.hasPerms(sender)) {
                sender.sendMessage(ChatColor.YELLOW + command.getShortInfo());
                found = true;
            }
        }
        return found;
    }

    private void loadCommands() {
        new CommandCleanup().load(commandList, commandMap);
        new CommandDebug().load(commandList, commandMap);
        new CommandPurge().load(commandList, commandMap);
        new CommandShow().load(commandList, commandMap);
        new CommandTop().load(commandList, commandMap);
        new CommandReload().load(commandList, commandMap);
        new CommandWipe().load(commandList, commandMap);
    }

    public void loadConfig() {

        getConfig().options().copyDefaults(true);
        saveConfig();
        PSMySQL.initiate(this);

        // get variables from settings handler
        if (getConfig().getBoolean("MySQL", false)) {
            this.mySQL = true;
            this.dbHost = getConfig().getString("MySQLhost", "");
            this.dbUser = getConfig().getString("MySQLuser", "");
            this.dbPass = getConfig().getString("MySQLpass", "");
            this.dbDatabase = getConfig().getString("MySQLdb", "");
            this.dbTable = getConfig().getString("MySQLtable", "pvpstats");
            this.dbOptions = getConfig().getString("MySQLoptions", "?autoReconnect=true");

            if (getConfig().getBoolean("collectprecise")) {
                this.dbKillTable = getConfig().getString("MySQLkilltable", "pvpkillstats");
            }

            this.dbPort = getConfig().getInt("MySQLport", 3306);
        } else if (getConfig().getBoolean("SQLite", false)) {
            this.SQLite = true;
            this.dbDatabase = getConfig().getString("SQLitefile", "");

            this.dbTable = getConfig().getString("SQLitetable", "pvpstats");
            if (getConfig().getBoolean("collectprecise")) {
                this.dbKillTable = getConfig().getString("SQLitekilltable", "pvpkillstats");
            }
        } else {
            this.dbTable = getConfig().getString("FlatFiletable", "pvpstats");
            if (getConfig().getBoolean("collectprecise")) {
                this.dbKillTable = getConfig().getString("FlatFilekilltable", "pvpkillstats");
            }
        }

        // Check Settings
        if (this.mySQL) {
            if (this.dbHost.equals("")) {
                this.mySQL = false;
            } else if (this.dbUser.equals("")) {
                this.mySQL = false;
            } else if (this.dbPass.equals("")) {
                this.mySQL = false;
            } else if (this.dbDatabase.equals("")) {
                this.mySQL = false;
            }
        } else if (this.SQLite) {
            dbDatabase = getConfig().getString("SQLitefile", "");
            if (this.dbDatabase.equals("")) {
                this.SQLite = false;
            }
        }

        // Enabled SQL/MySQL
        if (this.mySQL) {
            // Declare MySQL Handler
            getLogger().info("Database: mySQL");
            try {
                sqlHandler = new MySQLConnection(dbHost, dbPort, dbDatabase, dbUser,
                        dbPass, dbOptions, dbTable, dbKillTable);
            } catch (InstantiationException | ClassNotFoundException | IllegalAccessException e1) {
                e1.printStackTrace();
            }
        } else if (this.SQLite) {
            getLogger().info("Database: SQLite");
            sqlHandler = new SQLiteConnection(dbDatabase, dbTable, dbKillTable);
        } else {
            // default to flatfile
            getLogger().warning("Database: YML");
            sqlHandler = new FlatFileConnection(dbTable, dbKillTable);
        }



        getLogger().info("Database Initializing");
        // Initialize MySQL Handler

        if (sqlHandler != null && sqlHandler.connect(true)) {
            getLogger().info("Database connection successful");
            connection = true;
            // Check if the tables exist, if not, create them
            if (!sqlHandler.tableExists(dbDatabase, dbTable)) {
                // normal table doesnt exist, create both

                getLogger().info("Creating table " + dbTable);
                sqlHandler.createStatsTable(true);

                if (dbKillTable != null) {
                    getLogger().info("Creating table " + dbKillTable);
                    sqlHandler.createKillStatsTable(true);
                }
            }
        } else {
            getLogger().severe("Database connection failed");
            this.mySQL = false;
            this.SQLite = false;
        }
    }

    public void onDisable() {
        Debug.destroy();
        getLogger().info("disabled. (version " + getDescription().getVersion() + ")");
    }

    public boolean ignoresWorld(final String name) {
        if (!getConfig().contains("ignoreworlds")) {
            return false;
        }
        return getConfig().getStringList("ignoreworlds").contains(name);
    }

    public static PVPStats getInstance() {
        return instance;
    }
}
