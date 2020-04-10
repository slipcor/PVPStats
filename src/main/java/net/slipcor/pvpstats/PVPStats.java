package net.slipcor.pvpstats;

import net.slipcor.pvpstats.api.DatabaseAPI;
import net.slipcor.pvpstats.api.DatabaseConnection;
import net.slipcor.pvpstats.classes.Debugger;
import net.slipcor.pvpstats.classes.PlaceholderAPIHook;
import net.slipcor.pvpstats.commands.*;
import net.slipcor.pvpstats.core.*;
import net.slipcor.pvpstats.impl.FlatFileConnection;
import net.slipcor.pvpstats.impl.MySQLConnection;
import net.slipcor.pvpstats.impl.SQLiteConnection;
import net.slipcor.pvpstats.listeners.PVPArenaListener;
import net.slipcor.pvpstats.listeners.PlayerListener;
import net.slipcor.pvpstats.listeners.PluginListener;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main Plugin class
 *
 * @author slipcor
 */

public class PVPStats extends JavaPlugin {
    // Plugin instance to use staticly all over the place
    private static PVPStats instance;
    private final static int CFGVERSION = 1;

    // database type setting
    private boolean mySQL = false;
    private boolean SQLite = false;

    // database handler
    private DatabaseConnection dbHandler;

    // managers
    private final Debugger debugger = new Debugger(8);
    private Plugin paHandler = null;
    private Updater updater = null;
    private Config configHandler = null;

    // listeners
    private final PlayerListener playerListener = new PlayerListener(this);
    private final PVPArenaListener pluginListener = new PVPArenaListener(this);

    // commands
    private final Map<String, AbstractCommand> commandMap = new HashMap<>();
    private final List<AbstractCommand> commandList = new ArrayList<>();

    public static PVPStats getInstance() {
        return instance;
    }

    /**
     * @return the Config instance, create if not yet instantiated
     */
    public Config config() {
        if (configHandler == null) {
            if (getConfig().getInt("ver", 0) < CFGVERSION) {
                getConfig().options().copyDefaults(true);
                getConfig().set("ver", CFGVERSION);
                saveConfig();
            }
            this.reloadConfig();
            configHandler = new Config(this);
            getLogger().info("Loaded config file!");
        }
        return configHandler;
    }

    /**
     * @return the PVPArena plugin
     */
    public Plugin getPAHandler() {
        return paHandler;
    }

    /**
     * @return the PVPArena Listener
     */
    public PVPArenaListener getPAListener() {
        return pluginListener;
    }

    /**
     * @return the DatabaseConnection instance
     */
    public DatabaseConnection getSQLHandler() {
        return dbHandler;
    }

    /**
     * @return the Updater instance
     */
    public Updater getUpdater() {
        return updater;
    }

    /**
     * Check whether a world is disabled for this plugin
     *
     * @param name the world name
     * @return true if it is disabled, false otherwise
     */
    public boolean ignoresWorld(final String name) {
        if (!getConfig().contains(Config.Entry.IGNORE_WORLDS.getNode())) {
            return false;
        }
        return config().getList(Config.Entry.IGNORE_WORLDS).contains(name);
    }

    /**
     * Instantiate command
     */
    private void loadCommands() {
        new CommandCleanup().load(commandList, commandMap);
        new CommandDebug().load(commandList, commandMap);
        new CommandDebugKill().load(commandList, commandMap);
        new CommandMigrate().load(commandList, commandMap);
        new CommandPurge().load(commandList, commandMap);
        new CommandShow().load(commandList, commandMap);
        new CommandSet().load(commandList, commandMap);
        new CommandTop().load(commandList, commandMap);
        new CommandReload().load(commandList, commandMap);
        new CommandWipe().load(commandList, commandMap);
    }

    /**
     * Read the config and try to connect to the database
     */
    public void loadConfig() {
        DatabaseAPI.initiate(this);

        config().reload();

        String dbHost = null;
        String dbUser = null;
        String dbPass = null;
        String dbDatabase = null;
        String dbTable = null;
        String dbOptions = null;
        String dbKillTable = null;
        int dbPort = 0;

        if (config().getBoolean(Config.Entry.MYSQL_ACTIVE)) {
            this.mySQL = true;

            dbHost = config().get(Config.Entry.MYSQL_HOST);
            dbUser = config().get(Config.Entry.MYSQL_USERNAME);
            dbPass = config().get(Config.Entry.MYSQL_PASSWORD);
            dbDatabase = config().get(Config.Entry.MYSQL_DATABASE);
            dbTable = config().get(Config.Entry.MYSQL_TABLE);
            dbOptions = config().get(Config.Entry.MYSQL_OPTIONS);

            if (config().getBoolean(Config.Entry.STATISTICS_COLLECT_PRECISE)) {
                dbKillTable = config().get(Config.Entry.MYSQL_KILLTABLE);
            }

            dbPort = config().getInt(Config.Entry.MYSQL_PORT);

        } else if (config().getBoolean(Config.Entry.SQLITE_ACTIVE)) {
            this.SQLite = true;
            dbDatabase = config().get(Config.Entry.SQLITE_FILENAME);

            dbTable = config().get(Config.Entry.SQLITE_TABLE);
            if (config().getBoolean(Config.Entry.STATISTICS_COLLECT_PRECISE)) {
                dbKillTable = config().get(Config.Entry.SQLITE_KILLTABLE);
                getLogger().warning("Specific stats can be turned off as they are never used, they are intended for SQL and web frontend usage!");
                getLogger().warning("We recommend you set '" + Config.Entry.STATISTICS_COLLECT_PRECISE.getNode() + "' to false");
            }
        } else {
            dbTable = config().get(Config.Entry.YML_TABLE);
            if (config().getBoolean(Config.Entry.STATISTICS_COLLECT_PRECISE)) {
                dbKillTable = config().get(Config.Entry.MYSQL_KILLTABLE);
                getLogger().warning("Specific stats can be turned off as they are never used, they are intended for SQL and web frontend usage!");
                getLogger().warning("We recommend you set '" + Config.Entry.STATISTICS_COLLECT_PRECISE.getNode() + "' to false");
            }
        }

        // verify settings
        if (this.mySQL) {
            if (dbHost.equals("") ||
                    dbUser.equals("") ||
                    dbPass.equals("") ||
                    dbDatabase.equals("") ||
                    dbPort == 0) {
                this.mySQL = false;
            }
        } else if (this.SQLite) {
            if (dbDatabase.equals("")) {
                this.SQLite = false;
            }
        }

        // Enabled SQL/MySQL
        if (this.mySQL) {
            // Declare MySQL Handler
            getLogger().info("Database: mySQL");
            try {
                dbHandler = new MySQLConnection(dbHost, dbPort, dbDatabase, dbUser,
                        dbPass, dbOptions, dbTable, dbKillTable);
            } catch (InstantiationException | ClassNotFoundException | IllegalAccessException e1) {
                e1.printStackTrace();
            }
        } else if (this.SQLite) {
            getLogger().info("Database: SQLite");
            dbHandler = new SQLiteConnection(dbDatabase, dbTable, dbKillTable);
        } else {
            // default to flatfile
            getLogger().warning("Database: YML");
            dbHandler = new FlatFileConnection(dbTable, dbKillTable);
        }


        getLogger().info("Database Initializing");
        // Initialize MySQL Handler

        if (dbHandler != null && dbHandler.connect(true)) {
            getLogger().info("Database connection successful");
            // Check if the tables exist, if not, create them
            if (!dbHandler.tableExists(dbDatabase, dbTable)) {
                // normal table doesnt exist, create both

                getLogger().info("Creating table " + dbTable);
                dbHandler.createStatsTable(true);

                if (dbKillTable != null) {
                    getLogger().info("Creating table " + dbKillTable);
                    dbHandler.createKillStatsTable(true);
                }
            } else if (!dbHandler.hasColumn(dbKillTable, "world")) {
                dbHandler.addWorldColumn();
            }
        } else {
            getLogger().severe("Database connection failed");
            this.mySQL = false;
            this.SQLite = false;
        }
    }

    /**
     * Load the language file
     */
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

    /**
     * Hook into other plugins
     */
    private void loadHooks() {
        final Plugin paPlugin = getServer().getPluginManager().getPlugin("pvparena");
        if (paPlugin != null && paPlugin.isEnabled()) {
            getLogger().info("<3 PVP Arena");
            this.paHandler = paPlugin;
        }
    }

    public boolean onCommand(final CommandSender sender, final Command cmd, final String commandLabel, final String[] args) {

        debugger.i("onCommand!", sender);

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

        if (!found && DatabaseAPI.hasEntry(args[0])) {
            commandMap.get("show").commit(sender, args);
            return true;
        }

        return found;
    }

    public void onDisable() {
        Debugger.destroy();
        getLogger().info("disabled. (version " + getDescription().getVersion() + ")");
    }

    public void onEnable() {
        instance = this;
        try {

            OfflinePlayer.class.getDeclaredMethod("getUniqueId");
        } catch (Exception e) {
            getLogger().info("Your server is still not ready for UUIDs? Use PVP Stats older than v0.8.25.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        ConfigMigration.commit();
        configHandler = config();

        final PluginDescriptionFile pdfFile = getDescription();

        getServer().getPluginManager().registerEvents(playerListener, this);

        loadConfig();
        loadCommands();

        if (dbHandler == null || !dbHandler.isConnected()) {
            getLogger().severe("Database not connected, plugin DISABLED!");
            getServer().getPluginManager().disablePlugin(this);
            return; //to ensure the rest of the plugins code is not executed as this can lead to problems.
        }
        loadHooks();

        if (config().getBoolean(Config.Entry.OTHER_PVPARENA)) {
            if (getServer().getPluginManager().isPluginEnabled("pvparena")) {
                getServer().getPluginManager().registerEvents(pluginListener, this);
            } else {
                PluginListener paPluginListener = new PluginListener(this);
                getServer().getPluginManager().registerEvents(paPluginListener, this);
            }
        }

        updater = new Updater(this, getFile());

        loadLanguage();

        if (config().getBoolean(Config.Entry.UPDATE_TRACKER)) { //only call the task if true
            getServer().getScheduler().runTaskTimer(this, new Tracker(this), 0L, 72000L);
        }

        if (config().getBoolean(Config.Entry.STATISTICS_CLEAR_ON_START)) {

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

        Debugger.load(this, Bukkit.getConsoleSender());

        getLogger().info("enabled. (version " + pdfFile.getVersion() + ")");
    }

    public void sendPrefixed(final CommandSender sender, final String message) {
        if (!"".equals(message)) {
            sender.sendMessage(Language.MSG_PREFIX + message);
        }
    }

    /**
     * Set our PVP Arena handler when the plugin has been enabled
     *
     * @param plugin the PVP Arena plugin
     */
    public void setPAHandler(Plugin plugin) {
        this.paHandler = plugin;
    }
}
