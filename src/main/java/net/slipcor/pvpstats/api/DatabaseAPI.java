package net.slipcor.pvpstats.api;

import net.slipcor.pvpstats.PVPStats;
import net.slipcor.pvpstats.classes.Debugger;
import net.slipcor.pvpstats.classes.PlayerNameHandler;
import net.slipcor.pvpstats.classes.PlayerStatistic;
import net.slipcor.pvpstats.core.Config;
import net.slipcor.pvpstats.core.Language;
import net.slipcor.pvpstats.display.SignDisplay;
import net.slipcor.pvpstats.display.SortColumn;
import net.slipcor.pvpstats.impl.FlatFileConnection;
import net.slipcor.pvpstats.impl.MySQLConnection;
import net.slipcor.pvpstats.impl.SQLiteConnection;
import net.slipcor.pvpstats.runnables.*;
import net.slipcor.pvpstats.text.TextComponent;
import net.slipcor.pvpstats.text.TextFormatter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Database access class to handle player statistics, possibly from other plugins
 *
 * @author slipcor
 */

public final class DatabaseAPI {

    private DatabaseAPI() {
    }

    private static PVPStats plugin = null;

    private static final Debugger DEBUGGER = new Debugger(4);

    private static final Map<String, String> lastKill = new HashMap<>();
    private static final Map<String, BukkitTask> killTask = new HashMap<>();

    private static final TextComponent DATABASE_CONNECTED = new TextComponent("Warning: Database is not connected! Kills will not be recorded.");

    /**
     * Player A killed player B - use this to generally emulate a player kill.
     * <p>
     * There will be checks for newbie status, whether both players are valid Player objects
     *
     * @param attacker the killing player
     * @param victim   the killed player
     */
    public static void AkilledB(OfflinePlayer attacker, OfflinePlayer victim) {

        PVPStatsPVPEvent event = new PVPStatsPVPEvent(attacker, victim);

        Bukkit.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            DEBUGGER.i("Another plugin prevented PVP!");
            return;
        }

        if ((attacker == null || attacker.getPlayer() == null) &&
                (victim == null || victim.getPlayer() == null)) {
            DEBUGGER.i("attacker and victim are null");
            return;
        }

        if (attacker != null && victim != null && plugin.config().getBoolean(Config.Entry.STATISTICS_CHECK_ABUSE)) {
            DEBUGGER.i("- checking abuse");
            if (lastKill.containsKey(attacker.getName()) && lastKill.get(attacker.getName()).equals(victim.getName())) {
                TextFormatter.explainAbusePrevention(attacker, victim);
                DEBUGGER.i("> OUT: " + victim.getName());
                return; // no logging!
            }

            lastKill.put(attacker.getName(), victim.getName());
            int abusesec = plugin.config().getInt(Config.Entry.STATISTICS_ABUSE_SECONDS);
            if (abusesec > 0) {
                final String finalAttacker = attacker.getPlayer().getName();
                class RemoveLater implements Runnable {

                    @Override
                    public void run() {
                        lastKill.remove(finalAttacker);
                        killTask.remove(finalAttacker);
                    }

                }
                BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, new RemoveLater(), abusesec * 20L);

                if (killTask.containsKey(attacker.getName())) {
                    killTask.get(attacker.getName()).cancel();
                }

                killTask.put(attacker.getName(), task);
            }
        }

        if (victim == null || victim.getPlayer() == null) {
            DEBUGGER.i("victim is null", attacker.getName());
            incKill(attacker.getPlayer(), PlayerStatisticsBuffer.getEloScore(attacker.getUniqueId()));

            if (plugin.getSQLHandler().allowsAsync()) {
                Bukkit.getScheduler().runTaskAsynchronously(PVPStats.getInstance(), new DatabaseKillAddition(
                        PlayerNameHandler.getPlayerName(attacker), attacker.getUniqueId().toString(),
                        "", "",
                        attacker.getPlayer().getWorld().getName()));
            } else {
                Bukkit.getScheduler().runTask(PVPStats.getInstance(), new DatabaseKillAddition(
                        PlayerNameHandler.getPlayerName(attacker), attacker.getUniqueId().toString(),
                        "", "",
                        attacker.getPlayer().getWorld().getName()));
            }

            SignDisplay.updateAll();
            return;
        }

        if (attacker == null || attacker.getPlayer() == null) {
            DEBUGGER.i("attacker is null", victim.getName());
            incDeath(victim.getPlayer(), PlayerStatisticsBuffer.getEloScore(victim.getUniqueId()));

            if (plugin.getSQLHandler().allowsAsync()) {
                Bukkit.getScheduler().runTaskAsynchronously(PVPStats.getInstance(), new DatabaseKillAddition(
                        "", "",
                        PlayerNameHandler.getPlayerName(victim), victim.getUniqueId().toString(),
                        victim.getPlayer().getWorld().getName()));
            } else {
                Bukkit.getScheduler().runTask(PVPStats.getInstance(), new DatabaseKillAddition(
                        "", "",
                        PlayerNameHandler.getPlayerName(victim), victim.getUniqueId().toString(),
                        victim.getPlayer().getWorld().getName()));
            }

            SignDisplay.updateAll();
            return;
        }

        if (plugin.config().getBoolean(Config.Entry.STATISTICS_CHECK_NEWBIES) &&
                (isNewbie(attacker) || isNewbie(victim))) {

            DEBUGGER.i("either one has newbie status", victim.getName());
            TextFormatter.explainNewbieStatus(attacker, victim);
            return;
        }
        // here we go, PVP!
        DEBUGGER.i("Counting kill by " + attacker.getName(), victim.getPlayer());

        if (!plugin.config().getBoolean(Config.Entry.ELO_ACTIVE)) {
            DEBUGGER.i("no elo", victim.getName());
            incKill(attacker.getPlayer(), PlayerStatisticsBuffer.getEloScore(attacker.getUniqueId()));
            incDeath(victim.getPlayer(), PlayerStatisticsBuffer.getEloScore(victim.getUniqueId()));

            SignDisplay.updateAll();
            return;
        }

        final int min = plugin.config().getInt(Config.Entry.ELO_MINIMUM);
        final int max = plugin.config().getInt(Config.Entry.ELO_MAXIMUM);
        final int kBelow = plugin.config().getInt(Config.Entry.ELO_K_BELOW);
        final int kAbove = plugin.config().getInt(Config.Entry.ELO_K_ABOVE);
        final int kThreshold = plugin.config().getInt(Config.Entry.ELO_K_THRESHOLD);

        final int oldA = PlayerStatisticsBuffer.getEloScore(attacker.getUniqueId());
        final int oldP = PlayerStatisticsBuffer.getEloScore(victim.getUniqueId());

        final int kA = oldA >= kThreshold ? kAbove : kBelow;
        final int kP = oldP >= kThreshold ? kAbove : kBelow;

        final int newA = calcElo(oldA, oldP, kA, true, min, max);
        final int newP = calcElo(oldP, oldA, kP, false, min, max);

        if (incKill(attacker.getPlayer(), newA)) {
            DEBUGGER.i("increasing kill", attacker.getPlayer());

            Bukkit.getScheduler().runTaskLaterAsynchronously(
                    PVPStats.getInstance(), new Runnable() {
                        @Override
                        public void run() {
                            plugin.sendPrefixed(attacker.getPlayer(),
                                    Language.MSG_ELO_ADDED.toString(String.valueOf(newA - oldA), String.valueOf(newA)));
                        }
                    }, 1L
            );

            PlayerStatisticsBuffer.setEloScore(attacker.getUniqueId(), newA);
        }
        if (incDeath(victim.getPlayer(), newP)) {
            DEBUGGER.i("increasing death", victim.getPlayer());

            Bukkit.getScheduler().runTaskLaterAsynchronously(
                    PVPStats.getInstance(), new Runnable() {
                        @Override
                        public void run() {
                            plugin.sendPrefixed(victim.getPlayer(),
                                    Language.MSG_ELO_SUBBED.toString(String.valueOf(oldP - newP), String.valueOf(newP)));
                        }
                    }, 1L
            );

            PlayerStatisticsBuffer.setEloScore(victim.getUniqueId(), newP);
        }
        if (plugin.getSQLHandler().allowsAsync()) {
            Bukkit.getScheduler().runTaskAsynchronously(PVPStats.getInstance(), new DatabaseKillAddition(
                    PlayerNameHandler.getPlayerName(attacker), attacker.getUniqueId().toString(),
                    PlayerNameHandler.getPlayerName(victim), victim.getUniqueId().toString(),
                    attacker.getPlayer().getWorld().getName()));
        } else {
            Bukkit.getScheduler().runTask(PVPStats.getInstance(), new DatabaseKillAddition(
                    PlayerNameHandler.getPlayerName(attacker), attacker.getUniqueId().toString(),
                    PlayerNameHandler.getPlayerName(victim), victim.getUniqueId().toString(),
                    attacker.getPlayer().getWorld().getName()));
        }

        SignDisplay.updateAll();
    }

    private static List<UUID> allUUIDs;

    /**
     * @return a list of all UUIDs of players that have statistic entries
     */
    public static List<UUID> getAllUUIDs() {
        if (!plugin.getSQLHandler().isConnected()) {
            plugin.getLogger().severe("Database is not connected!");
            plugin.sendPrefixedOP(new ArrayList<>(), DATABASE_CONNECTED);
            return null;
        }

        if (allUUIDs == null) {
            List<UUID> output = new ArrayList<>();
            try {
                List<UUID> result = plugin.getSQLHandler().getStatsUUIDs();
                output.addAll(result);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            allUUIDs = output;
        }

        return allUUIDs;
    }

    private static List<String> allPlayerNames;

    /**
     * Return a player's statistic
     * @param player the player to find
     * @return the player's statistic
     */
    public static PlayerStatistic getAllStats(OfflinePlayer player) {
        if (!plugin.getSQLHandler().isConnected()) {
            plugin.getLogger().severe("Database is not connected!");
            plugin.sendPrefixedOP(new ArrayList<>(), DATABASE_CONNECTED);
            return new PlayerStatistic(PlayerNameHandler.getPlayerName(player),
                    0, 0, 0, 0, 0, 0, player.getUniqueId());
        }

        try {
            return plugin.getSQLHandler().getStats(player);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new PlayerStatistic(PlayerNameHandler.getPlayerName(player),
                0, 0, 0, 0, 0, 0, player.getUniqueId());
    }

    /**
     * @return a list of all players that have statistic entries
     */
    public static List<String> getAllPlayers() {
        if (!plugin.getSQLHandler().isConnected()) {
            plugin.getLogger().severe("Database is not connected!");
            plugin.sendPrefixedOP(new ArrayList<>(), DATABASE_CONNECTED);
            return null;
        }
        if (allPlayerNames == null) {
            List<String> output = new ArrayList<>();

            try {
                List<String> result = plugin.getSQLHandler().getNamesWithoutUUIDs();
                output.addAll(result);
                allPlayerNames = output;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return allPlayerNames;
    }

    /**
     * Get a player's statistic entry
     *
     * @param uuid the player id to find
     * @param entry      the entry to find
     * @return the entry value, 0 if not found, throwing an Exception if there was a bigger problem
     */
    public static Integer getEntry(UUID uuid, String entry) {
        if (entry == null) {
            throw new IllegalArgumentException("entry can not be null!");
        }

        if (!entry.equals("elo") &&
                !entry.equals("kills") &&
                !entry.equals("deaths") &&
                !entry.equals("streak") &&
                !entry.equals("currentstreak")) {
            throw new IllegalArgumentException("entry can not be '" + entry + "'. Valid values: elo, kills, deaths, streak, currentstreak");
        }

        if (!plugin.getSQLHandler().isConnected()) {
            plugin.getLogger().severe("Database is not connected!");
            plugin.sendPrefixedOP(new ArrayList<>(), DATABASE_CONNECTED);
            return null;
        }
        int result = -1;
        try {
            result = plugin.getSQLHandler().getStats(entry, uuid);
            if (result < 0) {
                return 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result < 0 ? 0 : result;
    }

    /**
     * Check whether a player has a statistic entry
     * <p>
     * YML will return true always as this is only about deciding between INSERT and UPDATE query
     *
     * @param uuid the player id to find
     * @return true if an entry was found
     */
    public static boolean hasEntry(UUID uuid) {
        int result = -1;
        try {
            result = plugin.getSQLHandler().getStats("kills", uuid);
        } catch (SQLException e) {
        }
        return result > -1;
    }

    /**
     * Get a player's stats in the form of a string array
     *
     * @param player the player to find
     * @return the player info in lines as overridable in the config
     */
    public static String[] info(final OfflinePlayer player) {
        if (!plugin.getSQLHandler().isConnected()) {
            plugin.getLogger().severe("Database is not connected!");
            plugin.sendPrefixedOP(new ArrayList<>(), DATABASE_CONNECTED);
            return null;
        }

        DEBUGGER.i("getting info for " + player.getName());
        PlayerStatistic result = null;
        try {
            result = plugin.getSQLHandler().getStats(player);
            if (result == null) {
                String[] output = new String[1];
                output[0] = Language.INFO_PLAYERNOTFOUND.toString(PlayerNameHandler.getPlayerName(player));
                return output;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (result == null) {
            String[] output = new String[1];
            output[0] = Language.INFO_PLAYERNOTFOUND.toString(PlayerNameHandler.getPlayerName(player));
            output[1] = Language.INFO_PLAYERNOTFOUND2.toString();
            return output;
        }
        String[] output;

        String name = result.getName();

        int elo = result.getELO();
        int kills = result.getKills();
        int deaths = result.getDeaths();
        int streak = result.getCurrentStreak();
        int maxStreak = result.getMaxStreak();
        Double ratio = calculateRatio(kills, deaths, maxStreak, streak);
        DecimalFormat df = new DecimalFormat("#.##");

        if (plugin.config().getBoolean(Config.Entry.MESSAGES_OVERRIDES)) {
            List<String> lines = plugin.config().getList(Config.Entry.MESSAGES_OVERRIDE_LIST);
            output = new String[lines.size()];

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);

                line = line.replace("%d", String.valueOf(deaths));
                line = line.replace("%k", String.valueOf(kills));
                line = line.replace("%m", String.valueOf(maxStreak));
                line = line.replace("%n", name);
                line = line.replace("%r", df.format(ratio));
                line = line.replace("%s", String.valueOf(streak));
                line = line.replace("%e", String.valueOf(elo));

                output[i] = ChatColor.translateAlternateColorCodes('&', line);
            }

            return output;
        }


        output = new String[7];

        output[0] = Language.INFO_FORMAT.toString(
                Language.INFO_NAME.toString(),
                name);
        output[1] = Language.INFO_FORMAT.toString(
                Language.INFO_KILLS.toString(),
                String.valueOf(kills));
        output[2] = Language.INFO_FORMAT.toString(
                Language.INFO_DEATHS.toString(),
                String.valueOf(deaths));
        output[3] = Language.INFO_FORMAT.toString(
                Language.INFO_RATIO.toString(),
                df.format(ratio));
        output[4] = Language.INFO_FORMAT.toString(
                Language.INFO_STREAK.toString(),
                String.valueOf(streak));
        output[5] = Language.INFO_FORMAT.toString(
                Language.INFO_MAXSTREAK.toString(),
                String.valueOf(maxStreak));
        output[6] = Language.INFO_FORMAT.toString(
                Language.INFO_ELO.toString(),
                String.valueOf(elo));
        return output;
    }

    /**
     * Initiate the access class
     *
     * @param plugin the plugin instance to use
     */
    public static void initiate(final PVPStats plugin) {
        DatabaseAPI.plugin = plugin;
    }

    /**
     * Initiate a player by actually reading the database
     *
     * @param player the player to initiate
     */
    public static void initiatePlayer(OfflinePlayer player) {
        if (getAllUUIDs().contains(player.getUniqueId())) {
            // an entry exists!
        } else if (getAllPlayers().contains(PlayerNameHandler.getPlayerName(player))) {
            // an entry without UUID exists!
            try {
                 plugin.getSQLHandler().setStatUIDByPlayer(player);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            allUUIDs.add(player.getUniqueId());
            allPlayerNames.remove(PlayerNameHandler.getPlayerName(player));
        } else if (plugin.config().getBoolean(Config.Entry.STATISTICS_CREATE_ON_JOIN)) {
            if (plugin.getSQLHandler().allowsAsync()) {
                Bukkit.getScheduler().runTaskAsynchronously(PVPStats.getInstance(), new DatabaseFirstEntry(player));
            } else {
                Bukkit.getScheduler().runTask(PVPStats.getInstance(), new DatabaseFirstEntry(player));
            }
            allUUIDs.add(player.getUniqueId());
        } else {
            allUUIDs.add(player.getUniqueId());
        }

        // read all the data from database
        PlayerStatisticsBuffer.loadPlayer(player);
    }

    private static DatabaseConnection connectToOther(String method, CommandSender sender) {

        DatabaseConnection dbHandler = null;

        String dbHost = null;
        String dbUser = null;
        String dbPass = null;
        String dbDatabase = null;
        String dbTable = null;
        String dbOptions = null;
        String dbKillTable = null;
        int dbPort = 0;
        
        Config config = PVPStats.getInstance().config();

        if (method.equals("yml")) {
            if (PVPStats.getInstance().getSQLHandler() instanceof FlatFileConnection) {
                PVPStats.getInstance().sendPrefixed(sender, Language.ERROR_DATABASE_METHOD.toString());
                return null;
            }

            dbTable = config.get(Config.Entry.YML_TABLE);
            if (config.getBoolean(Config.Entry.STATISTICS_COLLECT_PRECISE)) {
                dbKillTable = config.get(Config.Entry.MYSQL_KILLTABLE);
            }

            dbHandler = new FlatFileConnection(dbTable, dbKillTable);
        } else if (method.equals("sqlite")) {
            if (PVPStats.getInstance().getSQLHandler() instanceof SQLiteConnection) {
                PVPStats.getInstance().sendPrefixed(sender, Language.ERROR_DATABASE_METHOD.toString());
                return null;
            }

            dbDatabase = config.get(Config.Entry.SQLITE_FILENAME);

            dbTable = config.get(Config.Entry.SQLITE_TABLE);
            if (config.getBoolean(Config.Entry.STATISTICS_COLLECT_PRECISE)) {
                dbKillTable = config.get(Config.Entry.SQLITE_KILLTABLE);
            }

            dbHandler = new SQLiteConnection(dbDatabase, dbTable, dbKillTable);
        } else if (method.equals("mysql")) {
            if (PVPStats.getInstance().getSQLHandler() instanceof MySQLConnection) {
                PVPStats.getInstance().sendPrefixed(sender, Language.ERROR_DATABASE_METHOD.toString());
                return null;
            }

            dbHost = config.get(Config.Entry.MYSQL_HOST);
            dbUser = config.get(Config.Entry.MYSQL_USERNAME);
            dbPass = config.get(Config.Entry.MYSQL_PASSWORD);
            dbDatabase = config.get(Config.Entry.MYSQL_DATABASE);
            dbTable = config.get(Config.Entry.MYSQL_TABLE);
            dbOptions = config.get(Config.Entry.MYSQL_OPTIONS);

            if (config.getBoolean(Config.Entry.STATISTICS_COLLECT_PRECISE)) {
                dbKillTable = config.get(Config.Entry.MYSQL_KILLTABLE);
            }

            dbPort = config.getInt(Config.Entry.MYSQL_PORT);

            try {
                dbHandler = new MySQLConnection(dbHost, dbPort, dbDatabase, dbUser,
                        dbPass, dbOptions, dbTable, dbKillTable);
            } catch (InstantiationException | ClassNotFoundException | IllegalAccessException e1) {
                e1.printStackTrace();
            }
        } else {
            return null;
        }


        if (dbHandler != null && dbHandler.connect(true)) {
            sender.sendMessage("Database connection successful");
            // Check if the tables exist, if not, create them
            if (!dbHandler.tableExists(dbDatabase, dbTable)) {
                // normal table doesnt exist, create both

                sender.sendMessage("Creating table " + dbTable);
                dbHandler.createStatsTable(true);

                if (dbKillTable != null) {
                    sender.sendMessage("Creating table " + dbKillTable);
                    dbHandler.createKillStatsTable(true);
                }
            } else if (!dbHandler.hasColumn(dbKillTable, "world")) {
                dbHandler.addWorldColumn();
            }
        } else {
            sender.sendMessage("Database connection failed");
        }

        return dbHandler;
    }

    /**
     * Read the current statistics from another database implementation - this does NOT clear an existing database!
     *
     * @param method the other database method
     * @return the entries migrated
     */
    public static int migrateFrom(String method, CommandSender sender) {
        // database handler
        DatabaseConnection dbHandler = connectToOther(method, sender);
        if (dbHandler == null) {
            return -1;
        }

        try {
            List<PlayerStatistic> players = dbHandler.getAll();
            for (PlayerStatistic stat : players) {
                PVPStats.getInstance().getSQLHandler().insert(stat);
            }
            return players.size();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Save the current statistics to another database implementation - this does NOT clear an existing database!
     * 
     * @param method the other database method
     * @return the entries migrated
     */
    public static int migrateTo(String method, CommandSender sender) {
        // database handler
        DatabaseConnection dbHandler = connectToOther(method, sender);
        if (dbHandler == null) {
            return -1;
        }

        try {
            List<PlayerStatistic> players = PVPStats.getInstance().getSQLHandler().getAll();
            for(PlayerStatistic stat : players) {
                dbHandler.insert(stat);
            }
            return players.size();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private static boolean isNewbie(OfflinePlayer player) {
        // get the player object, or null if they are offline
        Player p = player.getPlayer();

        // if the player is offline, we assume they are not newbies (and log)
        if (p == null) {
            DEBUGGER.i("Player is offline, we assume they are not newbie...");
            return false;
        }

        // otherwise continue with our permission checks:
        // backwards compatibility
        boolean newbie = p.hasPermission("pvpstats.newbie");

        if (p.hasPermission("pvpstats.null")) {
            DEBUGGER.i("Player has ALL permissions, we assume they are not newbie...");
            /*
             * If a player does have the previous permission, we can assume that the permission
             * plugin either does always reply with TRUE or has ALL PERMS set to true, which means
             * they probably want to consider getting all access.
             *
             * This is a solution until a warning system is in place to ask admins to set it up
             * properly.
             */
            return false;
        }


        if (newbie) {
            DEBUGGER.i("Player has 'newbie'...");
            // backwards compatibility until we have a warning system in place to ask admins to change to
            // proper permission logic
            return true;
        }

        return !p.hasPermission("pvpstats.nonewbie");
    }

    /**
     * Purge the kill statistics older than a certain amount of days
     *
     * @param days the amount
     * @return the amount of removed entries
     */
    public static int purgeKillStats(int days) {
        if (!plugin.getSQLHandler().isConnected()) {
            plugin.getLogger().severe("Database is not connected!");
            plugin.sendPrefixedOP(new ArrayList<>(), DATABASE_CONNECTED);
            return 0;
        }

        int count = 0;

        long timestamp = System.currentTimeMillis() / 1000 - ((long) days * 24L * 60L * 60L);

        try {
            count = plugin.getSQLHandler().deleteKillsOlderThan(timestamp);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return count;
    }

    /**
     * Purge the general statistics older than a certain amount of days
     *
     * @param days the amount
     * @return the amount of removed entries
     */
    public static int purgeStats(int days) {
        if (!plugin.getSQLHandler().isConnected()) {
            plugin.getLogger().severe("Database is not connected!");
            plugin.sendPrefixedOP(new ArrayList<>(), DATABASE_CONNECTED);
            return 0;
        }
        int count = 0;

        long timestamp = System.currentTimeMillis() / 1000 - ((long) days * 24L * 60L * 60L);

        try {
            count = plugin.getSQLHandler().deleteStatsOlderThan(timestamp);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return count;
    }

    /**
     * Set a player's statistic value
     *
     * @param player the player to update
     * @param entry      the entry to update
     * @param value      the value to set
     * @throws SQLException
     */
    public static void setSpecificStat(OfflinePlayer player, String entry, int value) {
        if (!entry.equals("elo") &&
                !entry.equals("kills") &&
                !entry.equals("deaths") &&
                !entry.equals("streak") &&
                !entry.equals("currentstreak")) {
            throw new IllegalArgumentException("entry can not be '" + entry + "'. Valid values: elo, kills, deaths, streak, currentstreak");
        }
        if (PVPStats.getInstance().getSQLHandler().allowsAsync()) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new DatabaseSetSpecific(player.getUniqueId(), entry, value));
        } else {
            Bukkit.getScheduler().runTask(plugin, new DatabaseSetSpecific(player.getUniqueId(), entry, value));
        }

    }

    /**
     * Get the top statistics sorted by type
     *
     * @param count the amount to fetch
     * @param sort  the type to sort by
     * @return a sorted array
     */
    public static String[] top(final int count, String sort) {
        if (!plugin.getSQLHandler().isConnected()) {
            plugin.getLogger().severe("Database is not connected!");
            plugin.sendPrefixedOP(new ArrayList<>(), DATABASE_CONNECTED);
            return null;
        }

        sort = sort.toUpperCase();
        List<PlayerStatistic> result = null;
        final Map<String, Double> results = new HashMap<>();

        final List<String> sortedValues = new ArrayList<>();

        String order;
        try {

            switch (sort) {
                case "DEATHS":
                    order = "deaths";
                    break;
                case "STREAK":
                    order = "streak";
                    break;
                case "CURRENTSTREAK":
                    order = "currentstreak";
                    break;
                case "ELO":
                    order = "elo";
                    break;
                case "KILLS":
                case "K-D":
                default:
                    order = "kills";
                    break;
            }

            int limit = sort.equals("K-D") ? Math.min(count, 50) : count;

            result = plugin.getSQLHandler().getTopSorted(limit, order,
                    sort.equals("DEATHS") &&
                            !PVPStats.getInstance().config().getBoolean(Config.Entry.STATISTICS_DEATHS_DESCENDING));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (result != null) {
            for (PlayerStatistic entry : result) {
                switch (sort) {

                    case "KILLS":
                        sortedValues.add(Language.INFO_FORMAT.toString(
                                entry.getName(), String.valueOf(entry.getKills())));
                        break;
                    case "DEATHS":
                        sortedValues.add(Language.INFO_FORMAT.toString(
                                entry.getName(),String.valueOf(entry.getDeaths())));
                        break;
                    case "ELO":
                        sortedValues.add(Language.INFO_FORMAT.toString(
                                entry.getName(),String.valueOf(entry.getELO())));
                        break;
                    case "STREAK":
                        sortedValues.add(Language.INFO_FORMAT.toString(
                                entry.getName(),String.valueOf(entry.getMaxStreak())));
                        break;
                    case "CURRENTSTREAK":
                        sortedValues.add(Language.INFO_FORMAT.toString(
                                entry.getName(),String.valueOf(entry.getCurrentStreak())));
                        break;
                    default:
                        results.put(
                                entry.getName(),
                                calculateRatio(entry.getKills(),
                                        entry.getDeaths(),
                                        entry.getMaxStreak(), PlayerStatisticsBuffer.getStreak(entry.getUid())));
                        break;
                }
            }
        }
        if (sort.equals("KILLS") || sort.equals("DEATHS") || sort.equals("ELO") || sort.equals("STREAK") || sort.equals("CURRENTSTREAK")) {
            String[] output = new String[sortedValues.size()];

            int pos = 0;

            for (String s : sortedValues) {
                output[pos++] = s;
            }
            return output;
        }

        return sortParse(results, count);
    }

    /**
     * Get the top statistics sorted by type
     *
     * @param count the amount to fetch
     * @param sort  the type to sort by
     * @return a sorted array
     */
    public static String[] flop(final int count, String sort) {
        if (!plugin.getSQLHandler().isConnected()) {
            plugin.getLogger().severe("Database is not connected!");
            plugin.sendPrefixedOP(new ArrayList<>(), DATABASE_CONNECTED);
            return null;
        }

        sort = sort.toUpperCase();
        List<PlayerStatistic> result = null;
        final Map<String, Double> results = new HashMap<>();

        final List<String> sortedValues = new ArrayList<>();

        String order;
        try {

            switch (sort) {
                case "DEATHS":
                    order = "deaths";
                    break;
                case "STREAK":
                    order = "streak";
                    break;
                case "CURRENTSTREAK":
                    order = "currentstreak";
                    break;
                case "ELO":
                    order = "elo";
                    break;
                case "KILLS":
                case "K-D":
                default:
                    order = "kills";
                    break;
            }

            int limit = sort.equals("K-D") ? Math.min(count, 50) : count;

            result = plugin.getSQLHandler().getTopSorted(limit, order, !sort.equals("DEATHS") ||
                    !PVPStats.getInstance().config().getBoolean(Config.Entry.STATISTICS_DEATHS_DESCENDING));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (result != null) {
            for (PlayerStatistic entry : result) {
                switch (sort) {

                    case "KILLS":
                        sortedValues.add(Language.INFO_FORMAT.toString(
                                entry.getName(), String.valueOf(entry.getKills())));
                        break;
                    case "DEATHS":
                        sortedValues.add(Language.INFO_FORMAT.toString(
                                entry.getName(),String.valueOf(entry.getDeaths())));
                        break;
                    case "ELO":
                        sortedValues.add(Language.INFO_FORMAT.toString(
                                entry.getName(),String.valueOf(entry.getELO())));
                        break;
                    case "STREAK":
                        sortedValues.add(Language.INFO_FORMAT.toString(
                                entry.getName(),String.valueOf(entry.getMaxStreak())));
                        break;
                    case "CURRENTSTREAK":
                        sortedValues.add(Language.INFO_FORMAT.toString(
                                entry.getName(),String.valueOf(entry.getCurrentStreak())));
                        break;
                    default:
                        results.put(
                                entry.getName(),
                                calculateRatio(entry.getKills(),
                                        entry.getDeaths(),
                                        entry.getMaxStreak(), PlayerStatisticsBuffer.getStreak(entry.getUid())));
                        break;
                }
            }
        }
        if (sort.equals("KILLS") || sort.equals("DEATHS") || sort.equals("ELO") || sort.equals("STREAK") || sort.equals("CURRENTSTREAK")) {
            String[] output = new String[sortedValues.size()];

            int pos = 0;

            for (String s : sortedValues) {
                output[pos++] = s;
            }
            return output;
        }

        return sortParse(results, count);
    }

    public static List<Map<SortColumn, String>> detailedTop(int max, SortColumn column) {
        List<Map<SortColumn, String>> result = new ArrayList<>();

        try {
            String sort = "";
            // `name`,`kills`,`deaths`,`streak`,`currentstreak`,`elo`,`time`,`uid`
            switch (column) {
                case NAME:
                case DEATHS:
                case KILLS:
                case ELO:
                case CURRENTSTREAK:
                case STREAK:
                    sort = column.name().toLowerCase();
                    break;
            }
            List<PlayerStatistic> stats = plugin.getSQLHandler().getTopSorted(max, sort, column == SortColumn.DEATHS);

            for (PlayerStatistic stat : stats) {
                result.add(stat.toStringMap());
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return result;
    }

    /**
     * Wipe all stats
     *
     * @param uuid player name to wipe or null to wipe all stats
     */
    public static void wipe(final UUID uuid) {
        if (uuid == null) {
            plugin.getSQLHandler().deleteStats();
            plugin.getSQLHandler().deleteKills();
        } else {
            plugin.getSQLHandler().deleteStatsByUUID(uuid);
            plugin.getSQLHandler().deleteKillsByUUID(uuid);
        }
        PlayerStatisticsBuffer.clear(uuid);
    }

    private static ScriptEngine scriptEngine;

    /**
     * Calculate the kill / death ratio as defined in the config
     *
     * @param kills     to take into account
     * @param deaths    to take into account
     * @param streak    to take into account
     * @param maxstreak to take into account
     * @return the calculated value
     */
    public static Double calculateRatio(final int kills, final int deaths, final int streak,
                                        final int maxstreak) {
        if (plugin.config().getBoolean(Config.Entry.STATISTICS_KD_SIMPLE)) {
            if (deaths < 1) {
                return 0d;
            }
            return ((double) kills) / deaths;
        }

        String string = plugin.config().get(Config.Entry.STATISTICS_KD_CALCULATION);

        string = string.replaceAll("&k", "(" + kills + ")");
        string = string.replaceAll("&d", "(" + deaths + ")");
        string = string.replaceAll("&s", "(" + streak + ")");
        string = string.replaceAll("&m", "(" + maxstreak + ")");

        if (scriptEngine == null) {
            ScriptEngineManager mgr = new ScriptEngineManager();
            scriptEngine = mgr.getEngineByName("JavaScript");

            // Java 8 compatibility
            if (scriptEngine == null) {
                mgr = new ScriptEngineManager(null);
                scriptEngine = mgr.getEngineByName("nashorn");
            }
        }

        StringBuilder saneString = new StringBuilder();

        for (char c : string.toCharArray()) {
            switch (c) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case '-':
                case '+':
                case '*':
                case '/':
                case '(':
                case ')':
                case '<':
                case '>':
                case '?':
                case ':':
                case '=':
                    saneString.append(c);
                    break;
                default:
            }
        }

        try {
            Object value = scriptEngine.eval(saneString.toString());

            if (value instanceof Double) {
                return (Double) value;
            } else if (value instanceof Integer) {
                int i = (Integer) value;
                return (double) i;
            }
            plugin.getLogger().severe("SaneString: " + value.toString());

            return 0d;
        } catch (ScriptException e) {
            plugin.getLogger().severe("SaneString: " + saneString.toString());
            e.printStackTrace();
            return 0d;
        }
    }

    /**
     * Calculate a new ELO score
     *
     * @param myOld    the player's former ELO score
     * @param otherOld the other player's former ELO score
     * @param k        the k-factor
     * @param win      whether the player did a kill
     * @param min      min ELO score
     * @param max      max ELO score
     * @return the new ELO score
     */
    public static int calcElo(int myOld, int otherOld, int k, boolean win, int min, int max) {
        double expected = 1.0f / (1.0f + Math.pow(10.0f, ((float) (otherOld - myOld)) / 400.0f));

        int newVal;
        if (win) {
            newVal = (int) Math.round(myOld + k * (1.0f - expected));
        } else {
            newVal = (int) Math.round(myOld + k * (0.0f - expected));
        }

        if (min > -1 && newVal < min) {
            return min;
        }

        if (max > -1 && newVal > max) {
            return max;
        }

        return newVal;
    }

    /**
     * Do all the things we need to do when a kill happened. Kills, deaths, streaks, max streaks
     *
     * @param playerName   the player to handle
     * @param uuid         the player's UUID
     * @param kill         true if the player did a kill, false if they were killed
     * @param addMaxStreak should we increase the max streak?
     * @param elo          the new ELO score to set
     * @param world        the world the kill happened in
     */
    private static void checkAndDo(final String playerName, final UUID uuid, final boolean kill, final boolean addMaxStreak, int elo, String world) {
        if (plugin.getSQLHandler().allowsAsync()) {
            Bukkit.getScheduler().runTaskAsynchronously(PVPStats.getInstance(), new CheckAndDo(
                    playerName, uuid, kill, addMaxStreak, elo, world
            ));
        } else {
            Bukkit.getScheduler().runTask(PVPStats.getInstance(), new CheckAndDo(
                    playerName, uuid, kill, addMaxStreak, elo, world
            ));
        }
    }

    private static boolean incDeath(final Player player, int elo) {
        if (player.hasPermission("pvpstats.count")) {
            PlayerStatisticsBuffer.setStreak(player.getUniqueId(), 0);
            checkAndDo(PlayerNameHandler.getPlayerName(player), player.getUniqueId(),
                    false, false, elo, player.getWorld().getName());
            return true;
        }
        return false;
    }

    /**
     * Force increase a death count
     *
     * @param player the player to increase
     * @param elo        the ELO score to set
     * @return whether the setting succeeded
     */
    public static boolean forceIncDeath(final OfflinePlayer player, int elo) {
        PlayerStatisticsBuffer.setStreak(player.getUniqueId(), 0);
        checkAndDo(PlayerNameHandler.getPlayerName(player), player.getUniqueId(),
                false, false, elo, "world");
        return true;
    }

    private static boolean incKill(final Player player, int elo) {
        if (player.hasPermission("pvpstats.count")) {
            boolean incMaxStreak;
            if (PlayerStatisticsBuffer.hasStreak(player.getUniqueId())) {
                incMaxStreak = PlayerStatisticsBuffer.addStreak(player.getUniqueId());
                PlayerStatisticsBuffer.getStreak(player.getUniqueId());
            } else {

                int streakCheck = PlayerStatisticsBuffer.getStreak(player.getUniqueId());
                if (streakCheck < 1) {
                    PlayerStatisticsBuffer.setStreak(player.getUniqueId(), 1);
                    PlayerStatisticsBuffer.setMaxStreak(player.getUniqueId(), 1);
                    incMaxStreak = true;
                } else {
                    incMaxStreak = PlayerStatisticsBuffer.addStreak(player.getUniqueId());
                }

            }
            checkAndDo(PlayerNameHandler.getPlayerName(player), player.getUniqueId(),
                    true, incMaxStreak, elo, player.getWorld().getName());
            return true;
        }
        return false;
    }

    /**
     * Force increase a kill count
     *
     * @param player the player to increase
     * @param elo        the ELO score to set
     * @return whether the setting succeeded
     */
    public static boolean forceIncKill(final OfflinePlayer player, int elo) {
        boolean incMaxStreak;
        if (PlayerStatisticsBuffer.hasStreak(player.getUniqueId())) {
            incMaxStreak = PlayerStatisticsBuffer.addStreak(player.getUniqueId());
            PlayerStatisticsBuffer.getStreak(player.getUniqueId());
        } else {

            int streakCheck = PlayerStatisticsBuffer.getStreak(player.getUniqueId());
            if (streakCheck < 1) {
                PlayerStatisticsBuffer.setStreak(player.getUniqueId(), 1);
                PlayerStatisticsBuffer.setMaxStreak(player.getUniqueId(), 1);
                incMaxStreak = true;
            } else {
                incMaxStreak = PlayerStatisticsBuffer.addStreak(player.getUniqueId());
            }

        }
        checkAndDo(PlayerNameHandler.getPlayerName(player), player.getUniqueId(),
                true, incMaxStreak, elo, "world");
        return true;
    }

    private static String[] sortParse(final Map<String, Double> results,
                                      final int count) {
        String[] result = new String[results.size()];
        Double[] sort = new Double[results.size()];

        int pos = 0;

        DecimalFormat df = new DecimalFormat("#.##");

        for (String key : results.keySet()) {
            sort[pos] = results.get(key);
            result[pos] = Language.INFO_FORMAT.toString(key, df.format(sort[pos]));
            pos++;
        }

        int pos2 = results.size();
        boolean doMore = true;
        while (doMore) {
            pos2--;
            doMore = false; // assume this is our last pass over the array
            for (int i = 0; i < pos2; i++) {
                if (sort[i] < sort[i + 1]) {
                    // exchange elements

                    final double tempI = sort[i];
                    sort[i] = sort[i + 1];
                    sort[i + 1] = tempI;

                    final String tempR = result[i];
                    result[i] = result[i + 1];
                    result[i + 1] = tempR;

                    doMore = true; // after an exchange, must look again
                }
            }
        }
        if (result.length < count) {
            return result;
        }
        String[] output = new String[count];
        System.arraycopy(result, 0, output, 0, output.length);

        return output;
    }

    /**
     * Refresh the RAM values after making changes with a command
     */
    public static void refresh() {
        PlayerStatisticsBuffer.refresh();
    }
}
