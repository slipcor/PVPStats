package net.slipcor.pvpstats.api;

import net.slipcor.pvpstats.PVPStats;
import net.slipcor.pvpstats.classes.Debugger;
import net.slipcor.pvpstats.classes.PlayerStatistic;
import net.slipcor.pvpstats.core.Config;
import net.slipcor.pvpstats.core.Language;
import net.slipcor.pvpstats.impl.FlatFileConnection;
import net.slipcor.pvpstats.impl.MySQLConnection;
import net.slipcor.pvpstats.impl.SQLiteConnection;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

    /**
     * Player A killed player B - use this to generally emulate a player kill.
     * <p>
     * There will be checks for newbie status, whether both players are valid Player objects
     *
     * @param attacker the killing player
     * @param victim   the killed player
     */
    public static void AkilledB(Player attacker, Player victim) {
        DEBUGGER.i("AkilledB, A is " + attacker, victim);
        if (attacker == null && victim == null) {
            DEBUGGER.i("attacker and victim are null");
            return;
        }

        if (victim == null) {
            DEBUGGER.i("victim is null", attacker);
            incKill(attacker, PlayerStatisticsBuffer.getEloScore(attacker.getName()));
            return;
        }
        if (attacker == null) {
            DEBUGGER.i("attacker is null", victim);
            incDeath(victim, PlayerStatisticsBuffer.getEloScore(victim.getName()));
            return;
        }

        if (attacker.hasPermission("pvpstats.newbie") || victim.hasPermission("pvpstats.newbie")) {
            DEBUGGER.i("either one has newbie status", victim);
            return;
        }

        if (!plugin.config().getBoolean(Config.Entry.ELO_ACTIVE)) {
            DEBUGGER.i("no elo", victim);
            incKill(attacker, PlayerStatisticsBuffer.getEloScore(attacker.getName()));
            incDeath(victim, PlayerStatisticsBuffer.getEloScore(victim.getName()));
            return;
        }

        final int min = plugin.config().getInt(Config.Entry.ELO_MINIMUM);
        final int max = plugin.config().getInt(Config.Entry.ELO_MAXIMUM);
        final int kBelow = plugin.config().getInt(Config.Entry.ELO_K_BELOW);
        final int kAbove = plugin.config().getInt(Config.Entry.ELO_K_ABOVE);
        final int kThreshold = plugin.config().getInt(Config.Entry.ELO_K_THRESHOLD);

        final int oldA = PlayerStatisticsBuffer.getEloScore(attacker.getName());
        final int oldP = PlayerStatisticsBuffer.getEloScore(victim.getName());

        final int kA = oldA >= kThreshold ? kAbove : kBelow;
        final int kP = oldP >= kThreshold ? kAbove : kBelow;

        final int newA = calcElo(oldA, oldP, kA, true, min, max);
        final int newP = calcElo(oldP, oldA, kP, false, min, max);

        if (incKill(attacker, newA)) {
            DEBUGGER.i("increasing kill", attacker);
            plugin.sendPrefixed(attacker, Language.MSG_ELO_ADDED.toString(String.valueOf(newA - oldA), String.valueOf(newA)));
            PlayerStatisticsBuffer.setEloScore(attacker.getName(), newA);
        }
        if (incDeath(victim, newP)) {
            DEBUGGER.i("increasing death", victim);
            plugin.sendPrefixed(victim, Language.MSG_ELO_SUBBED.toString(String.valueOf(oldP - newP), String.valueOf(newP)));
            PlayerStatisticsBuffer.setEloScore(victim.getName(), newP);
        }
    }

    /**
     * Clean up database, removing stat entries with the same player name
     *
     * @return the amount of entries removed
     */
    public static int clean() {
        if (!plugin.getSQLHandler().isConnected()) {
            plugin.getLogger().severe("Database is not connected!");
            return 0;
        }
        Map<Integer, String> result;

        List<Integer> ints = new ArrayList<>();
        Map<String, Integer> players = new HashMap<>();

        try {

            result = plugin.getSQLHandler().getStatsIDsAndNames();

            for (Integer key : result.keySet()) {
                String playerName = result.get(key);

                if (players.containsKey(playerName)) {
                    ints.add(key);
                    players.put(playerName, players.get(playerName) + 1);
                } else {
                    players.put(playerName, 1);
                }
            }

            if (ints.size() > 0) {
                plugin.getSQLHandler().deleteStatsByIDs(ints);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ints.size();
    }

    /**
     * @return a list of all UUIDs of players that have statistic entries
     */
    public static List<UUID> getAllUUIDs() {
        if (!plugin.getSQLHandler().isConnected()) {
            plugin.getLogger().severe("Database is not connected!");
            return null;
        }
        List<UUID> output = new ArrayList<>();

        try {
            List<UUID> result = plugin.getSQLHandler().getStatsUUIDs();
            output.addAll(result);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return output;
    }

    /**
     * @return a list of all players that have statistic entries
     */
    public static List<String> getAllPlayers() {
        if (!plugin.getSQLHandler().isConnected()) {
            plugin.getLogger().severe("Database is not connected!");
            return null;
        }
        List<String> output = new ArrayList<>();

        try {
            List<String> result = plugin.getSQLHandler().getStatsNames();
            output.addAll(result);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return output;
    }

    /**
     * Get a player's statistic entry
     *
     * @param playerName the player name to find
     * @param entry      the entry to find
     * @return the entry value, 0 if not found, throwing an Exception if there was a bigger problem
     */
    public static Integer getEntry(String playerName, String entry) {
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
            return null;
        }
        int result = -1;
        try {
            result = plugin.getSQLHandler().getStatExact(entry, playerName);
            if (result < 0) {
                result = plugin.getSQLHandler().getStatLike(entry, playerName);
                if (result < 0) {
                    return 0;
                }
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
     * @param playerName the player name to find
     * @return true if an entry was found
     */
    public static boolean hasEntry(String playerName) {
        int result = -1;
        try {
            result = plugin.getSQLHandler().getStatExact("kills", playerName);
        } catch (SQLException e) {
        }
        return result > -1;
    }

    /**
     * Get a player's stats in the form of a string array
     *
     * @param playerName the player name to find
     * @return the player info in lines as overridable in the config
     */
    public static String[] info(final String playerName) {
        if (!plugin.getSQLHandler().isConnected()) {
            plugin.getLogger().severe("Database is not connected!");
            return null;
        }
        DEBUGGER.i("getting info for " + playerName);
        PlayerStatistic result = null;
        try {
            result = plugin.getSQLHandler().getStatsExact(playerName);
            if (result == null) {
                result = plugin.getSQLHandler().getStatsLike(playerName);
                if (result == null) {
                    String[] output = new String[1];
                    output[0] = Language.INFO_PLAYERNOTFOUND.toString(playerName);
                    return output;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (result == null) {
            String[] output = new String[1];
            output[0] = Language.INFO_PLAYERNOTFOUND.toString(playerName);
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

        if (plugin.config().getBoolean(Config.Entry.MESSAGES_OVERRIDES)) {
            List<String> lines = plugin.config().getList(Config.Entry.MESSAGES_OVERRIDE_LIST);
            output = new String[lines.size()];

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);

                line = line.replace("%d", String.valueOf(deaths));
                line = line.replace("%k", String.valueOf(kills));
                line = line.replace("%m", String.valueOf(maxStreak));
                line = line.replace("%n", name);
                line = line.replace("%r", String.valueOf(ratio));
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
                String.valueOf(ratio));
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
    public static void initiatePlayer(Player player) {
        String result = null;

        if (getAllPlayers().contains(player.getName())) {

            try {
                result = plugin.getSQLHandler().getStatUIDFromPlayer(player);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (result == null || result.equals("")) {
                    plugin.getSQLHandler().setStatUIDByPlayer(player);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (getAllUUIDs().contains(player.getUniqueId())) {
            // they were here but with a different name!
            PVPStats.getInstance().getLogger().info("Trying to rename player with UUID '" + player.getUniqueId() + "' to '" + player.getName() + "'");
            DatabaseAPI.renamePlayer(player.getUniqueId(), player.getName());
        } else if (plugin.config().getBoolean(Config.Entry.STATISTICS_CREATE_ON_JOIN)) {
            plugin.getSQLHandler().addFirstStat(
                    player.getName(), player.getUniqueId(), 0, 0,
                    PVPStats.getInstance().config().getInt(Config.Entry.ELO_DEFAULT));
        }

        // read all the data from database
        PlayerStatisticsBuffer.getStreak(player.getName());
        PlayerStatisticsBuffer.getDeaths(player.getName());
        PlayerStatisticsBuffer.getEloScore(player.getName());
        PlayerStatisticsBuffer.getKills(player.getName());
        PlayerStatisticsBuffer.getMaxStreak(player.getName());
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

    /**
     * Purge the kill statistics older than a certain amount of days
     *
     * @param days the amount
     * @return the amount of removed entries
     */
    public static int purgeKillStats(int days) {
        if (!plugin.getSQLHandler().isConnected()) {
            plugin.getLogger().severe("Database is not connected!");
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
     * @param playerName the player to update
     * @param entry      the entry to update
     * @param value      the value to set
     * @throws SQLException
     */
    public static void setSpecificStat(String playerName, String entry, int value) throws SQLException {
        if (!entry.equals("elo") &&
                !entry.equals("kills") &&
                !entry.equals("deaths") &&
                !entry.equals("streak") &&
                !entry.equals("currentstreak")) {
            throw new IllegalArgumentException("entry can not be '" + entry + "'. Valid values: elo, kills, deaths, streak, currentstreak");
        }
        plugin.getSQLHandler().setSpecificStat(playerName, entry, value);
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

            result = plugin.getSQLHandler().getTopSorted(limit, order, sort.equals("DEATHS"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (result != null) {
            for (PlayerStatistic entry : result) {
                switch (sort) {
                    case "KILLS":
                        sortedValues.add(ChatColor.RED + entry.getName() + ":" + ChatColor.GRAY + " " + entry.getKills());
                        break;
                    case "DEATHS":
                        sortedValues.add(ChatColor.RED + entry.getName() + ":" + ChatColor.GRAY + " " + entry.getDeaths());
                        break;
                    case "ELO":
                        sortedValues.add(ChatColor.RED + entry.getName() + ":" + ChatColor.GRAY + " " + entry.getELO());
                        break;
                    case "STREAK":
                        sortedValues.add(ChatColor.RED + entry.getName() + ":" + ChatColor.GRAY + " " + entry.getMaxStreak());
                        break;
                    case "CURRENTSTREAK":
                        sortedValues.add(ChatColor.RED + entry.getName() + ":" + ChatColor.GRAY + " " + entry.getCurrentStreak());
                        break;
                    default:
                        results.put(
                                entry.getName(),
                                calculateRatio(entry.getKills(),
                                        entry.getDeaths(),
                                        entry.getMaxStreak(), PlayerStatisticsBuffer.getStreak(entry.getName())));
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
     * Wipe all stats
     *
     * @param name player name to wipe or null to wipe all stats
     */
    public static void wipe(final String name) {
        if (name == null) {
            plugin.getSQLHandler().deleteStats();
            plugin.getSQLHandler().deleteKills();
        } else {
            plugin.getSQLHandler().deleteStatsByName(name);
            plugin.getSQLHandler().deleteKillsByName(name);
        }
        PlayerStatisticsBuffer.clear(name);
    }


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

        String string = plugin.config().get(Config.Entry.STATISTICS_KD_CALCULATION);

        string = string.replaceAll("&k", "(" + kills + ")");
        string = string.replaceAll("&d", "(" + deaths + ")");
        string = string.replaceAll("&s", "(" + streak + ")");
        string = string.replaceAll("&m", "(" + maxstreak + ")");

        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("JavaScript");
        StringBuilder saneString = new StringBuilder();

        // Java 8 compatibility
        if (engine == null) {
            mgr = new ScriptEngineManager(null);
            engine = mgr.getEngineByName("nashorn");
        }

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
            Object value = engine.eval(saneString.toString());

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

        if (!plugin.getSQLHandler().hasEntry(uuid)) {

            DEBUGGER.i("player has no entry yet, adding!");

            final int kills = kill ? 1 : 0;
            final int deaths = kill ? 0 : 1;

            plugin.getSQLHandler().addFirstStat(playerName, uuid, kills, deaths, elo);

            PlayerStatisticsBuffer.setKills(playerName, kills);
            PlayerStatisticsBuffer.setDeaths(playerName, deaths);

            plugin.getSQLHandler().addKill(playerName, uuid, kill, world);

            return;
        }

        if (addMaxStreak && kill) {
            DEBUGGER.i("increasing kills and max streak");
            plugin.getSQLHandler().increaseKillsAndMaxStreak(uuid, elo);
        } else if (kill) {
            DEBUGGER.i("increasing kills and current streak");
            plugin.getSQLHandler().increaseKillsAndStreak(uuid, elo);
        } else {
            DEBUGGER.i("increasing deaths");
            plugin.getSQLHandler().increaseDeaths(uuid, elo);
        }

        DEBUGGER.i("adding Kill: " + kill);
        plugin.getSQLHandler().addKill(playerName, uuid, kill, world);
        if (kill) {
            PlayerStatisticsBuffer.addKill(playerName);
        } else {
            PlayerStatisticsBuffer.addDeath(playerName);
        }
    }

    private static boolean incDeath(final Player player, int elo) {
        if (player.hasPermission("pvpstats.count")) {
            PlayerStatisticsBuffer.setStreak(player.getName(), 0);
            checkAndDo(player.getName(), player.getUniqueId(), false, false, elo, player.getWorld().getName());
            return true;
        }
        return false;
    }

    /**
     * Force increase a death count
     *
     * @param playerName the player to increase
     * @param elo        the ELO score to set
     * @param admin      the player issuing the command
     * @return whether the setting succeeded
     */
    public static boolean forceIncDeath(final String playerName, int elo, final OfflinePlayer admin) {
        PlayerStatisticsBuffer.setStreak(playerName, 0);
        checkAndDo(playerName, admin.getUniqueId(), false, false, elo, "world");
        return true;
    }

    private static boolean incKill(final Player player, int elo) {
        if (player.hasPermission("pvpstats.count")) {
            boolean incMaxStreak;
            if (PlayerStatisticsBuffer.hasStreak(player.getName())) {
                incMaxStreak = PlayerStatisticsBuffer.addStreak(player.getName());
                PlayerStatisticsBuffer.getStreak(player.getName());
            } else {

                int streakCheck = PlayerStatisticsBuffer.getStreak(player.getName());
                if (streakCheck < 1) {
                    PlayerStatisticsBuffer.setStreak(player.getName(), 1);
                    PlayerStatisticsBuffer.setMaxStreak(player.getName(), 1);
                    incMaxStreak = true;
                } else {
                    incMaxStreak = PlayerStatisticsBuffer.addStreak(player.getName());
                }

            }
            checkAndDo(player.getName(), player.getUniqueId(), true, incMaxStreak, elo, player.getWorld().getName());
            return true;
        }
        return false;
    }

    /**
     * Force increase a kill count
     *
     * @param playerName the player to increase
     * @param elo        the ELO score to set
     * @param admin      the player issuing the command
     * @return whether the setting succeeded
     */
    public static boolean forceIncKill(final String playerName, int elo, final OfflinePlayer admin) {
        boolean incMaxStreak;
        if (PlayerStatisticsBuffer.hasStreak(playerName)) {
            incMaxStreak = PlayerStatisticsBuffer.addStreak(playerName);
            PlayerStatisticsBuffer.getStreak(playerName);
        } else {

            int streakCheck = PlayerStatisticsBuffer.getStreak(playerName);
            if (streakCheck < 1) {
                PlayerStatisticsBuffer.setStreak(playerName, 1);
                PlayerStatisticsBuffer.setMaxStreak(playerName, 1);
                incMaxStreak = true;
            } else {
                incMaxStreak = PlayerStatisticsBuffer.addStreak(playerName);
            }

        }
        checkAndDo(playerName, admin.getUniqueId(), true, incMaxStreak, elo, "world");
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
            result[pos] = ChatColor.RED + key + ":" + ChatColor.GRAY + " " + df.format(sort[pos]);
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
     * Refresh the RAM values after making changes
     */
    public static void refresh() {
        PlayerStatisticsBuffer.refresh();
    }

    /**
     * Update the database with the new name of a player
     *
     * @param uuid    the UUID to look for
     * @param newName the new name to set
     */
    public static void renamePlayer(UUID uuid, String newName) {
        plugin.getSQLHandler().renamePlayer(uuid, newName);
    }
}
