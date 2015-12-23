package net.slipcor.pvpstats;

import net.slipcor.pvpstats.Updater.UpdateType;
import net.slipcor.pvpstats.uuid.UUIDUpdater;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * main class
 *
 * @author slipcor
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
    public static boolean useUUIDs = false;

    private final PSListener entityListener = new PSListener(this);
    protected final PSPAListener paListener = new PSPAListener(this);
    private PSPAPluginListener paPluginListener;

    private Updater updater = null;
    private static PVPStats instance;

    public void onEnable() {
        instance = this;
        try {

            OfflinePlayer.class.getDeclaredMethod("getUniqueId");
            useUUIDs = true;
        } catch (Exception e) {
            getLogger().info("Your server is not yet ready for UUIDs, just FYI");
        }

        final PluginDescriptionFile pdfFile = getDescription();

        getServer().getPluginManager().registerEvents(entityListener, this);

        loadConfig();
        if(!this.mySQL) {
            getLogger().severe("MySQL disabled, plugin DISABLED!");
            getServer().getPluginManager().disablePlugin(this);
            return; //to ensure the rest of the plugins code is not executed as this can lead to problems.
        }
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

        if (args == null || args.length < 1 || !(args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("wipe") || args[0].equalsIgnoreCase("cleanup") || args[0].equalsIgnoreCase("purge"))) {
            if (!parsecommand(sender, args)) {
                sender.sendMessage("/pvpstats - show your pvp stats");
                sender.sendMessage("/pvpstats [player] - show player's pvp stats");
                sender.sendMessage("/pvpstats [amount] - show the top [amount] players (K-D)");
                if (sender.hasPermission("pvpstats.top")) {
                    sender.sendMessage("/pvpstats top [amount] - show the top [amount] players (K-D)");
                    sender.sendMessage("/pvpstats top [type] - show the top 10 players of the type");
                    sender.sendMessage("/pvpstats top [type] [amount] - show the top [amount] players of the type");
                }
                if (sender.hasPermission("pvpstats.reload")) {
                    sender.sendMessage("/pvpstats reload - reload the configs");
                }
                if (sender.hasPermission("pvpstats.cleanup")) {
                    sender.sendMessage("/pvpstats cleanup - removes multi entries");
                }
                if (sender.hasPermission("pvpstats.purge")) {
                    sender.sendMessage("/pvpstats purge {type} [amount] - remove kill entries older than [amount] days");
                }
            }
            return true;
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
        } else if (args[0].equalsIgnoreCase("cleanup")) {
            if (!sender.hasPermission("pvpstats.cleanup")) {
                sendPrefixed(sender, Language.MSG_NOPERMCLEANUP.toString());
                return true;
            }

            final int count = PSMySQL.clean();
            sendPrefixed(sender, Language.MSG_CLEANED.toString(String.valueOf(count)));

            return true;
        } else if (args[0].equalsIgnoreCase("purge")) {
            if (!sender.hasPermission("pvpstats.purge")) {
                sendPrefixed(sender, Language.MSG_NOPERMPURGE.toString());
                return true;
            }

            int days = 30;

            if (args.length > 2) {
                try {
                    days = Integer.parseInt(args[args.length - 1]);
                } catch (Exception e) {

                }
            }

            if (args.length > 1) {
                if (args[1].equalsIgnoreCase("specific")) {
                    final int count = PSMySQL.purgeKillStats(days);
                    sendPrefixed(sender, Language.MSG_CLEANED.toString(String.valueOf(count)));
                } else if (args[1].equalsIgnoreCase("standard")) {
                    final int count = PSMySQL.purgeStats(days);
                    sendPrefixed(sender, Language.MSG_CLEANED.toString(String.valueOf(count)));
                } else {
                    final int count = PSMySQL.purgeKillStats(days) + PSMySQL.purgeStats(days);
                    sendPrefixed(sender, Language.MSG_CLEANED.toString(String.valueOf(count)));
                }
            } else {
                final int count = PSMySQL.purgeKillStats(days) + PSMySQL.purgeStats(days);
                sendPrefixed(sender, Language.MSG_CLEANED.toString(String.valueOf(count)));
            }




            return true;
        }

        if (!sender.hasPermission("pvpstats.reload")) {
            sendPrefixed(sender, Language.MSG_NOPERMRELOAD.toString());
            return true;
        }

        this.reloadConfig();
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
                                    Language.valueOf("HEAD_" + name).toString()));
                            sender.sendMessage(Language.HEAD_LINE.toString());


                            int pos = 1;
                            for (String stat : top) {
                                sender.sendMessage(pos++ + ": " + stat);
                            }
                        }

                    }

                    if (args[1].equals("kills")) {
                        Bukkit.getScheduler().runTaskAsynchronously(this, new RunLater("KILLS", amount));
                    } else if (args[1].equals("deaths")) {
                        Bukkit.getScheduler().runTaskAsynchronously(this, new RunLater("DEATHS", amount));
                    } else if (args[1].equals("streak")) {
                        Bukkit.getScheduler().runTaskAsynchronously(this, new RunLater("STREAK", amount));
                    } else if (args[1].equalsIgnoreCase("elo")) {
                        Bukkit.getScheduler().runTaskAsynchronously(this, new RunLater("ELO", amount));
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
                int count = legacyTop == 0 ? 10 : Integer.parseInt(args[0]);
                if (count > 20) {
                    count = 20;
                }
                if (legacyTop == 0) {
                    args[0] = String.valueOf(count);
                }
                class RunLater implements Runnable {
                    int count;

                    RunLater(int i) {
                        count = i;
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
                            sender.sendMessage(String.valueOf(pos++) + ": " + stat);
                        }
                    }

                }
                Bukkit.getScheduler().runTaskAsynchronously(this, new RunLater(count));

                return true;
            } catch (Exception e) {
                e.printStackTrace();
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
        PSMySQL.initiate(this);

        // get variables from settings handler
        if (getConfig().getBoolean("MySQL", false)) {
            this.mySQL = getConfig().getBoolean("MySQL", false);
            this.dbHost = getConfig().getString("MySQLhost", "");
            this.dbUser = getConfig().getString("MySQLuser", "");
            this.dbPass = getConfig().getString("MySQLpass", "");
            this.dbDatabase = getConfig().getString("MySQLdb", "");
            this.dbTable = getConfig().getString("MySQLtable", "pvpstats");

            if (getConfig().getBoolean("collectprecise")) {
                this.dbKillTable = getConfig().getString("MySQLkilltable", "pvpkillstats");
            }

            this.dbPort = getConfig().getInt("MySQLport", 3306);
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
                if (!sqlHandler.tableExists(dbDatabase, dbTable)) {
                    // normal table doesnt exist, create both

                    getLogger().info("Creating table " + dbTable);
                    final String query = "CREATE TABLE `" + dbTable + "` ( " +
                            "`id` int(5) NOT NULL AUTO_INCREMENT, " +
                            "`name` varchar(42) NOT NULL, " +
                            "`uid` varchar(42), " +
                            "`kills` int(8) not null default 0, " +
                            "`deaths` int(8) not null default 0, " +
                            "`streak` int(8) not null default 0, " +
                            "`elo` int(8) not null default 0, " +
                            "`time` int(16) not null default 0, " +
                            "PRIMARY KEY (`id`) ) AUTO_INCREMENT=1 ;";
                    try {
                        sqlHandler.executeQuery(query, true);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    if (dbKillTable != null) {

                        getLogger().info("Creating table " + dbKillTable);
                        final String query2 = "CREATE TABLE `" + dbKillTable + "` ( " +
                                "`id` int(16) NOT NULL AUTO_INCREMENT, " +
                                "`name` varchar(42) NOT NULL, " +
                                "`uid` varchar(42), " +
                                "`kill` int(1) not null default 0, " +
                                "`time` int(16) not null default CURRENT_TIMESTAMP, " +
                                "PRIMARY KEY (`id`) ) AUTO_INCREMENT=1 ;";
                        try {
                            sqlHandler.executeQuery(query2, true);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    // normal exists, do we need to update?
                    try {
                        List<String> columns = Arrays.asList(sqlHandler.getColumns(dbDatabase, dbTable));

                        if (!columns.contains("streak")) {
                            final String queryA = "ALTER TABLE `" + dbTable + "` ADD `streak` int(8) not null default 0; ";
                            final String queryB = "ALTER TABLE `" + dbTable + "` CHANGE `deaths` `deaths` INT( 8 ) NOT NULL DEFAULT 0;";
                            final String queryC = "ALTER TABLE `" + dbTable + "` CHANGE `kills` `kills` INT( 8 ) NOT NULL DEFAULT 0;";
                            final String queryD = "ALTER TABLE `" + dbTable + "` ADD `uid` varchar(42); ";
                            final String queryE = "ALTER TABLE `" + dbTable + "` ADD `elo` int(8) not null default 0; ";
                            final String queryF = "ALTER TABLE `" + dbTable + "` ADD `time` int(16) not null default CURRENT_TIMESTAMP; ";
                            try {
                                sqlHandler.executeQuery(queryA, true);
                                getLogger().info("Added 'streak' column to MySQL!");
                                sqlHandler.executeQuery(queryB, true);
                                getLogger().info("Updated MySQL field 'deaths'");
                                sqlHandler.executeQuery(queryC, true);
                                getLogger().info("Updated MySQL field 'kills'");
                                sqlHandler.executeQuery(queryD, true);
                                getLogger().info("Added 'uid' column to MySQL!");
                                sqlHandler.executeQuery(queryE, true);
                                getLogger().info("Added 'elo' column to MySQL!");
                                sqlHandler.executeQuery(queryF, true);
                                getLogger().info("Added 'time' column to MySQL!");
                                new UUIDUpdater(this, dbTable);
                            } catch (SQLException e2) {
                                e2.printStackTrace();
                            }
                        } else if (!columns.contains("uid")) {
                            final String queryD = "ALTER TABLE `" + dbTable + "` ADD `uid` varchar(42); ";
                            final String queryE = "ALTER TABLE `" + dbTable + "` ADD `elo` int(8) not null default 0; ";
                            final String queryF = "ALTER TABLE `" + dbTable + "` ADD `time` int(16) not null default CURRENT_TIMESTAMP; ";
                            try {
                                sqlHandler.executeQuery(queryD, true);
                                getLogger().info("Added 'uid' column to MySQL!");
                                sqlHandler.executeQuery(queryE, true);
                                getLogger().info("Added 'elo' column to MySQL!");
                                sqlHandler.executeQuery(queryF, true);
                                getLogger().info("Added 'time' column to MySQL!");
                            } catch (SQLException e2) {
                                e2.printStackTrace();
                            }
                            new UUIDUpdater(this, dbTable);
                        } else if (!columns.contains("elo")) {
                            final String queryE = "ALTER TABLE `" + dbTable + "` ADD `elo` int(8) not null default 0; ";
                            final String queryF = "ALTER TABLE `" + dbTable + "` ADD `time` int(16) not null default CURRENT_TIMESTAMP; ";
                            try {
                                sqlHandler.executeQuery(queryE, true);
                                getLogger().info("Added 'elo' column to MySQL!");
                                sqlHandler.executeQuery(queryF, true);
                                getLogger().info("Added 'time' column to MySQL!");
                            } catch (SQLException e2) {
                                e2.printStackTrace();
                            }
                            new UUIDUpdater(this, dbTable); // double check if we still don't need this
                        } else if (!columns.contains("time")) {
                            final String queryF = "ALTER TABLE `" + dbTable + "` ADD `time` int(16) not null default CURRENT_TIMESTAMP; ";
                            try {
                                sqlHandler.executeQuery(queryF, true);
                                getLogger().info("Added 'time' column to MySQL!");
                            } catch (SQLException e2) {
                                e2.printStackTrace();
                            }
                            new UUIDUpdater(this, dbTable); // double check if we still don't need this
                        }
                    } catch (SQLException e1) {
                        e1.printStackTrace();
                    }

                    if (dbKillTable != null && !sqlHandler.tableExists(dbDatabase, dbKillTable)) {
                        // second table doesnt exist, create that

                        getLogger().info("Creating table " + dbKillTable);
                        final String query = "CREATE TABLE `" + dbKillTable + "` ( " +
                                "`id` int(16) NOT NULL AUTO_INCREMENT, " +
                                "`name` varchar(42) NOT NULL, " +
                                "`uid` varchar(42), " +
                                "`kill` int(1) not null default 0, " +
                                "`time` int(16) not null default 0, " +
                                "PRIMARY KEY (`id`) ) AUTO_INCREMENT=1 ;";
                        try {
                            sqlHandler.executeQuery(query, true);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    } else if (dbKillTable != null) {
                        // did we really add the "tine" ??!!

                        List<String> columns = new ArrayList<String>();
                        try {
                            columns = Arrays.asList(sqlHandler.getColumns(dbDatabase, dbKillTable));
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                        if (columns.contains("tine")) {
                            final String query = "ALTER TABLE `" + dbKillTable + "` CHANGE `tine` `time` INT( 16 ) NOT NULL DEFAULT 0;";
                            final String queryD = "ALTER TABLE `" + dbKillTable + "` ADD `uid` varchar(42); ";

                            try {
                                sqlHandler.executeQuery(query, true);
                                getLogger().info("Fixed MySQL field 'time'");
                                sqlHandler.executeQuery(queryD, true);
                                getLogger().info("Added field 'uid'");
                            } catch (SQLException e2) {
                                e2.printStackTrace();
                            }
                            new UUIDUpdater(this, dbKillTable);
                        } else if (!columns.contains("uid")) {
                            final String queryD = "ALTER TABLE `" + dbKillTable + "` ADD `uid` varchar(42); ";

                            try {
                                sqlHandler.executeQuery(queryD, true);
                                getLogger().info("Added field 'uid'");
                            } catch (SQLException e2) {
                                e2.printStackTrace();
                            }
                            new UUIDUpdater(this, dbKillTable);
                        }
                    }
                }
            } else {
                getLogger().severe("MySQL connection failed");
                this.mySQL = false;
            }
        }
    }

    public void onDisable() {
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
