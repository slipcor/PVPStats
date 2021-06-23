package net.slipcor.pvpstats;

import net.slipcor.core.*;
import net.slipcor.pvpstats.api.DatabaseAPI;
import net.slipcor.pvpstats.api.DatabaseConnection;
import net.slipcor.pvpstats.classes.PlaceholderAPIHook;
import net.slipcor.pvpstats.classes.PlayerHandler;
import net.slipcor.pvpstats.classes.PlayerStatistic;
import net.slipcor.pvpstats.commands.*;
import net.slipcor.pvpstats.display.SignDisplay;
import net.slipcor.pvpstats.impl.FlatFileConnection;
import net.slipcor.pvpstats.impl.MySQLConnection;
import net.slipcor.pvpstats.impl.SQLiteConnection;
import net.slipcor.pvpstats.listeners.PVPArenaListener;
import net.slipcor.pvpstats.listeners.PlayerListener;
import net.slipcor.pvpstats.listeners.PluginListener;
import net.slipcor.pvpstats.metrics.MetricsLite;
import net.slipcor.pvpstats.metrics.MetricsMain;
import net.slipcor.pvpstats.runnables.*;
import net.slipcor.pvpstats.text.TextComponent;
import net.slipcor.pvpstats.text.TextFormatter;
import net.slipcor.pvpstats.yml.Config;
import net.slipcor.pvpstats.yml.Language;
import net.slipcor.pvpstats.yml.LanguageMigration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Main Plugin class
 *
 * @author slipcor
 */

public class PVPStats extends CorePlugin {
    // Plugin instance to use staticly all over the place
    private static PVPStats instance;
    private final static int CFGVERSION = 1;

    // database type setting
    private boolean mySQL = false;
    private boolean SQLite = false;

    // database handler
    private DatabaseConnection dbHandler;

    // managers
    private CoreDebugger debugger;
    private Plugin paHandler = null;
    private CoreUpdater updater = null;
    private CoreLanguage language = null;
    private Config configHandler = null;
    private CoreTabCompleter completer;

    private FileConfiguration announcements = null; // configurable announcements per streak level
    private FileConfiguration commands = null;      // configurable commands per streak level

    // listeners
    private PlayerListener playerListener;
    private final PVPArenaListener pluginListener = new PVPArenaListener(this);

    // commands
    private final Map<String, CoreCommand> commandMap = new HashMap<>();
    private final List<CoreCommand> commandList = new ArrayList<>();

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

    @Override
    protected String getMessagePrefix() {
        return Language.MSG.MESSAGE_PREFIX.parse();
    }

    public String getDebugPrefix() {
        return "[PS-debug] ";
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
    public CoreUpdater getUpdater() {
        return updater;
    }

    /**
     * Handle a player gaining a streak level, maybe issuing commands or announcements
     *
     * @param uuid  the player's UUID
     * @param value the new streak value
     */
    public void handleStreak(UUID uuid, int value) {
        String key = String.valueOf(value);
        OfflinePlayer player = Bukkit.getPlayer(uuid);
        try {
            if (config().getBoolean(Config.Entry.STATISTICS_STREAK_ANNOUNCEMENTS)) {
                if (announcements == null) {
                    announcements = new YamlConfiguration();
                    announcements.load(new File(getDataFolder(), "streak_announcements.yml"));
                }

                List<String> msgList = new ArrayList<>();

                // handle either a string or an array of strings in the file
                if (announcements.isString(key)) {
                    String msg = announcements.getString(key, "");
                    if (!msg.equals("")) {
                        msgList.add(msg);
                    }
                } else if (announcements.isList(key)) {
                    msgList = announcements.getStringList(key);
                }

                for (String message : msgList) {
                    if (!message.isEmpty()) {
                        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message)
                                .replace("%player%", PlayerHandler.getPlayerName(player)));
                    }
                }
            }
            if (config().getBoolean(Config.Entry.STATISTICS_STREAK_COMMANDS)) {
                if (commands == null) {
                    commands = new YamlConfiguration();
                    commands.load(new File(getDataFolder(), "streak_commands.yml"));
                }

                List<String> cmdList = new ArrayList<>();

                // handle either a string or an array of strings in the file
                if (commands.isString(key)) {
                    String cmd = commands.getString(key, "");
                    if (!cmd.equals("")) {
                        cmdList.add(commands.getString(key, ""));
                    }
                } else if (commands.isList(key)) {
                    cmdList = commands.getStringList(key);
                }

                for (String command : cmdList) {
                    if (!command.isEmpty()) {
                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
                                command.replace("%player%", PlayerHandler.getPlayerName(player)));
                    }
                }
            }
        } catch (IOException | InvalidConfigurationException exception) {
            exception.printStackTrace();
        }
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
        return config().getStringList(Config.Entry.IGNORE_WORLDS, new ArrayList<String>()).contains(name);
    }

    /**
     * Instantiate command
     */
    public void loadCommands() {
        commandList.clear();
        commandMap.clear();
        this.completer = null;
        new CommandCleanup(this).load(commandList, commandMap);
        new CommandConfig(this).load(commandList, commandMap);
        new CommandDebug(this).load(commandList, commandMap);
        new CommandDebugKill(this).load(commandList, commandMap);
        new CommandMigrate(this).load(commandList, commandMap);
        new CommandPurge(this).load(commandList, commandMap);
        new CommandShow(this).load(commandList, commandMap);
        new CommandSet(this).load(commandList, commandMap);
        new CommandTop(this).load(commandList, commandMap);
        new CommandReload(this).load(commandList, commandMap);
        new CommandWipe(this).load(commandList, commandMap);
    }

    /**
     * Try to connect to the database
     */
    public void loadConfig() {
        DatabaseAPI.initiate(this);

        config().load();

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

            dbHost = config().getString(Config.Entry.MYSQL_HOST);
            dbUser = config().getString(Config.Entry.MYSQL_USERNAME);
            dbPass = config().getString(Config.Entry.MYSQL_PASSWORD);
            dbDatabase = config().getString(Config.Entry.MYSQL_DATABASE);
            dbTable = config().getString(Config.Entry.MYSQL_TABLE);
            dbOptions = config().getString(Config.Entry.MYSQL_OPTIONS);

            if (config().getBoolean(Config.Entry.STATISTICS_COLLECT_PRECISE)) {
                dbKillTable = config().getString(Config.Entry.MYSQL_KILLTABLE);
            }

            dbPort = config().getInt(Config.Entry.MYSQL_PORT);

        } else if (config().getBoolean(Config.Entry.SQLITE_ACTIVE)) {
            this.SQLite = true;
            dbDatabase = config().getString(Config.Entry.SQLITE_FILENAME);

            dbTable = config().getString(Config.Entry.SQLITE_TABLE);
            if (config().getBoolean(Config.Entry.STATISTICS_COLLECT_PRECISE)) {
                dbKillTable = config().getString(Config.Entry.SQLITE_KILLTABLE);
                getLogger().warning("Specific stats can be turned off as they are never used, they are intended for SQL and web frontend usage!");
                getLogger().warning("We recommend you set '" + Config.Entry.STATISTICS_COLLECT_PRECISE.getNode() + "' to false");
            }
        } else {
            dbTable = config().getString(Config.Entry.YML_TABLE);
            if (config().getBoolean(Config.Entry.STATISTICS_COLLECT_PRECISE) &&
                    config().getBoolean(Config.Entry.YML_COLLECT_PRECISE)) {
                dbKillTable = config().getString(Config.Entry.MYSQL_KILLTABLE);
            } else if (config().getBoolean(Config.Entry.STATISTICS_COLLECT_PRECISE)) {
                getLogger().warning("Specific stats can be turned off as they are never used, they are intended for SQL and web frontend usage!");
                getLogger().warning("Please either switch to SQLite or re-enable this by setting '" + Config.Entry.YML_COLLECT_PRECISE.getNode() + "' to true");
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
            } else if (dbKillTable != null) {
                // Normal table exists, and we want to hook to the specific killstats
                if (!dbHandler.tableExists(dbDatabase, dbKillTable)) {

                    getLogger().info("Creating table " + dbKillTable);
                    dbHandler.createKillStatsTable(true);

                } else if (!dbHandler.hasColumn(dbKillTable, "world")) {

                    dbHandler.addWorldColumn();
                    dbHandler.addKillVictim();

                } else if (!dbHandler.hasColumn(dbKillTable, "victim")) {
                    dbHandler.addKillVictim();
                }
            }
        } else {
            getLogger().severe("Database connection failed");
            this.mySQL = false;
            this.SQLite = false;
        }

        PlayerStatistic.ELO_DEFAULT = config().getInt(Config.Entry.ELO_DEFAULT);
        PlayerStatistic.ELO_MINIMUM = config().getInt(Config.Entry.ELO_MINIMUM);
    }

    /**
     * Load the language file
     */
    public String loadLanguage() {
        LanguageMigration.commit(this);
        return language.load("lang");
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

        final CoreCommand acc = (args.length > 0) ? commandMap.get(args[0].toLowerCase()) : null;
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
        for (CoreCommand command : commandList) {
            if (command.hasPerms(sender)) {
                sender.sendMessage(ChatColor.YELLOW + command.getShortInfo());
                found = true;
            }
        }

        final OfflinePlayer player = PlayerHandler.findPlayer(args[0]);

        if (player == null) {
            sendPrefixed(sender, Language.MSG.COMMAND_PLAYER_NOT_FOUND.parse(args[0]));
        }

        if (!found && DatabaseAPI.hasEntry(player.getUniqueId())) {
            commandMap.get("show").commit(sender, args);
            return true;
        }

        return found;
    }

    public void onDisable() {
        destroyDebugger();
        getLogger().info("disabled. (version " + getDescription().getVersion() + ")");
    }

    @Override
    public void onLoad() {
        instance = this;
        language = new Language(this);
        loadConfig();
    }

    public void onEnable() {
        debugger = new CoreDebugger(this,8);
        try {

            OfflinePlayer.class.getDeclaredMethod("getUniqueId");
        } catch (Exception e) {
            getLogger().info("Your server is still not ready for UUIDs? Use PVP Stats older than v0.8.25.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        configHandler = config();

        final PluginDescriptionFile pdfFile = getDescription();

        loadCommands();

        DatabaseAPI.DEBUGGER = new CoreDebugger(this, 4);
        CommandCleanup.debugger = new CoreDebugger(this, 11);
        CommandDebugKill.debugger = new CoreDebugger(this, 13);
        SignDisplay.debugger = new CoreDebugger(this, 16);
        PlayerListener.Debugger = new CoreDebugger(this, 3);
        CheckAndDo.DEBUGGER = new CoreDebugger(this, 20);
        DatabaseIncreaseDeaths.debugger = new CoreDebugger(this, 19);
        DatabaseIncreaseKills.debugger = new CoreDebugger(this, 17);
        DatabaseIncreaseKillsStreak.debugger = new CoreDebugger(this, 15);
        DatabaseKillAddition.debugger = new CoreDebugger(this, 14);

        if (!new File(getDataFolder(), "streak_announcements.yml").exists()) {
            saveResource("streak_announcements.yml", false);
        }
        if (!new File(getDataFolder(), "streak_commands.yml").exists()) {
            saveResource("streak_commands.yml", false);
        }

        playerListener = new PlayerListener(this);
        getServer().getPluginManager().registerEvents(playerListener, this);

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

        updater = new CoreUpdater(this, getFile(),
                "pvpstats", "https://www.spigotmc.org/resources/pvp-stats.59124/",
                Config.Entry.UPDATE_MODE, Config.Entry.UPDATE_TYPE);

        if (loadLanguage() != null) {
            getPluginLoader().disablePlugin(this);
            return;
        }

        if (config().getBoolean(Config.Entry.BSTATS_ENABLED)) {
            if (config().getBoolean(Config.Entry.BSTATS_FULL)) {
                MetricsMain mainMetrics = new MetricsMain(this);
            } else {
                MetricsLite liteMetrics = new MetricsLite(this);
            }
        }

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            getLogger().info("PVPStats - PlaceholderAPI found.");
            new PlaceholderAPIHook().register();
        }

        loadDebugger("debug", Bukkit.getConsoleSender());

        if (config().getBoolean(Config.Entry.STATISTICS_CLEAR_ON_START)) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pvpstats cleanup");
        }

        SignDisplay.loadAllDisplays();

        getLogger().info("enabled. (version " + pdfFile.getVersion() + ")");
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command cmd, final String alias, final String[] args) {
        if (completer == null) {
            completer = new CoreTabCompleter(config().getBoolean(Config.Entry.GENERAL_SHORTHAND_COMMANDS));
        }
        return completer.getMatches(sender, commandList, args);
    }

    public void reloadStreaks() {
        commands = null;
        announcements = null;
    }

    public void sendPrefixedOP(List<CommandSender> senders, final TextComponent... message) {
        // if the admin has a config node set to false, noop
        if (!config().getBoolean(Config.Entry.OTHER_OP_MESSAGES)) {
            debugger.i("Would opmsg but config is false");
            return;
        }

        // if we list no senders then send to all users with permission (who are online)
        if (senders.size() == 0) {
            senders.addAll(Bukkit.getServer().getOnlinePlayers());
        } else {
            // deduplicate
            senders = new ArrayList<>(new HashSet<>(senders));
            senders.remove(null);
        }

        for (CommandSender sender : senders) {
            // if the user does not have permission for debug messages, noop
            if (!sender.hasPermission("pvpstats.opmessages")) {
                debugger.i("Would opmsg but permission is false");
                return;
            }

            // otherwise send the message
            if (TextFormatter.hasContent(message)) {
                TextFormatter.send(sender, TextFormatter.addPrefix(message));
                TextFormatter.explainDisableOPMessages(sender);
            }
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
