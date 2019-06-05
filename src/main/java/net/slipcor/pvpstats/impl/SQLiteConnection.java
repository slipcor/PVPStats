package net.slipcor.pvpstats.impl;

import net.slipcor.pvpstats.PVPStats;
import net.slipcor.pvpstats.api.DatabaseConnection;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class SQLiteConnection implements DatabaseConnection {
    // SQL connection details
    private final String dbDatabase;

    // Database tables
    private final String dbTable, dbKillTable;

    private boolean collectPrecise = false;

    public SQLiteConnection(String dbDatabase, String dbTable, String dbKillTable) {
        this.dbDatabase = dbDatabase;
        this.dbTable = dbTable;
        this.dbKillTable = dbKillTable;
    }

    // The connection object
    private Connection databaseConnection;

    /**
     * @param printError should we print errors that we encounter?
     * @return true if the connection was made successfully, false otherwise.
     */
    @Override
    public boolean connect(boolean printError) {
        try {
            File file = new File(PVPStats.getInstance().getDataFolder(), dbDatabase + ".db");
            if (file.exists()) {
                file.createNewFile();
            }
            this.databaseConnection = DriverManager.getConnection("jdbc:sqlite:" + file);

            collectPrecise = dbKillTable != null && !"".equals(dbKillTable);

            return this.databaseConnection != null;
        } catch (SQLException | IOException e) {
            if (printError) e.printStackTrace();
            return false;
        }
    }

    /**
     * @param query    The Query to send to the SQL server.
     * @param modifies If the Query modifies the database, set this to true. If not, set this to false
     * @return If {@code modifies} is true, returns a valid ResultSet obtained from the query. If {@code modifies} is false, returns null.
     * @throws SQLException if the query had an error or there was not a valid connection.
     */
    private ResultSet executeQuery(final String query, final boolean modifies) throws SQLException {
        Statement statement = this.databaseConnection.createStatement();
        if (modifies) {
            statement.execute(query);
            return null;
        } else {
            return statement.executeQuery(query);
        }
    }

    /**
     * @param database The database to check for the table in.
     * @param table    The table to check for existence.
     * @return true if the table exists, false if there was an error or the database doesn't exist.
     * <p/>
     * This method looks through the information schema that comes with a MySQL installation and checks
     * if a certain table exists within a database.
     */
    public boolean tableExists(String database, String table) {
        String query = "SELECT name FROM sqlite_master WHERE type='table' AND name='"+table+"'";
        try {
            ResultSet set = executeQuery(query, false);
            return set != null && set.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
     * ----------------------
     *  TABLE ENTRY CREATION
     * ----------------------
     */

    /**
     * Create the first statistic entry for a player
     * @param playerName the player's name
     * @param uuid the player's UUID
     * @param kills the kill amount
     * @param deaths the death amount
     * @param elo the ELO rating
     */
    @Override
    public void addFirstStat(String playerName, UUID uuid, int kills, int deaths, int elo) {
        long time = System.currentTimeMillis() / 1000;
        try {
            executeQuery("INSERT INTO `" + dbTable +
                    "` (`name`, `uid`, `kills`,`deaths`,`streak`,`currentstreak`,`elo`,`time`) VALUES ('"
                    + playerName + "', '" + uuid + "', " + kills + ", " + deaths + ", " +
                    kills + ", " + kills + ", " + elo + ", " + time + ")", true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add a kill to the player's count
     * @param playerName the player's name
     * @param uuid the player's uuid
     * @param kill true if they did kill, false if they were killed
     */
    @Override
    public void addKill(String playerName, UUID uuid, boolean kill) {
        if (!collectPrecise) {
            return;
        }
        long time = System.currentTimeMillis() / 1000;
        try {
            executeQuery("INSERT INTO " + dbKillTable + " (`name`,`uid`,`kill`,`time`) VALUES(" +
                    "'" + playerName + "', '" + uuid + "', '" + (kill ? 1 : 0) + "', " + time + ")", true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * create the kill stat table
     * @param printError should we print errors that we encounter?
     */
    @Override
    public void createKillStatsTable(boolean printError) {
        final String query2 = "CREATE TABLE `" + dbKillTable + "` ( " +
                "`name` varchar(42) NOT NULL, " +
                "`uid` varchar(42), " +
                "`kill` int(1) not null default 0, " +
                "`time` int(16) not null default 0);";
        try {
            executeQuery(query2, true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * create the statistics table
     * @param printError should we print errors that we encounter?
     */
    @Override
    public void createStatsTable(boolean printError) {
        final String query = "CREATE TABLE `" + dbTable + "` ( " +
                "`name` varchar(42) NOT NULL, " +
                "`uid` varchar(42), " +
                "`kills` int(8) not null default 0, " +
                "`deaths` int(8) not null default 0, " +
                "`streak` int(8) not null default 0, " +
                "`currentstreak` int(8) not null default 0, " +
                "`elo` int(8) not null default 0, " +
                "`time` int(16) not null default 0);";
        try {
            executeQuery(query, true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Run a custom query. THIS WILL BE REMOVED
     * @param query the query to run
     */
    @Override
    public void customQuery(String query) {
        try {
            executeQuery(query, true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete ALL kill stats
     */
    @Override
    public void deleteKills() {
        if (!collectPrecise) {
            return;
        }
        try {
            executeQuery("DELETE FROM `" + dbKillTable + "` WHERE 1;", true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete kill stats of a player
     * @param playerName the player's name
     */
    @Override
    public void deleteKillsByName(String playerName) {
        if (!collectPrecise) {
            return;
        }
        try {
            executeQuery("DELETE FROM `" + dbKillTable + "` WHERE `name` = '" + playerName
                    + "';", true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete kill stats older than a timestamp
     * @param timestamp to compare to
     * @throws SQLException
     */
    @Override
    public int deleteKillsOlderThan(long timestamp) throws SQLException {
        if (!collectPrecise) {
            return 0;
        }
        int count = 0;

        ResultSet result = executeQuery("SELECT `time` FROM `" + dbKillTable + "` WHERE `time` < "+timestamp+";", false);
        while (result.next()) {
            count++;
        }
        executeQuery("DELETE FROM `" + dbKillTable + "` WHERE `time` < "+timestamp+";", true);
        return count;
    }

    /**
     * Delete all statistics
     */
    @Override
    public void deleteStats() {
        try {
            executeQuery("DELETE FROM `" + dbTable + "` WHERE 1;", true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete all statistics by ID
     * @param list the list of IDs to delete
     * @throws SQLException
     */
    @Override
    public void deleteStatsByIDs(List<Integer> list) throws SQLException {

        StringBuilder buff = new StringBuilder("DELETE FROM `");
        buff.append(dbTable);
        buff.append("` WHERE `ROWID` IN (");

        boolean first = true;

        for (Integer i : list) {
            if (!first) {
                buff.append(',');
            }
            first = false;
            buff.append(i);
        }

        buff.append(");");

        executeQuery(buff.toString(), true);
    }

    /**
     * Delete statistics by player name
     * @param playerName the player's name
     */
    @Override
    public void deleteStatsByName(String playerName) {
        try {
            executeQuery("DELETE FROM `" + dbTable + "` WHERE `name` = '" + playerName
                    + "';", true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete statistics older than a timestamp
     * @param timestamp the timestamp to compare to
     * @throws SQLException
     */
    @Override
    public int deleteStatsOlderThan(long timestamp) throws SQLException {
        executeQuery("DELETE FROM `" + dbTable + "` WHERE `time` < "+timestamp+";", true);
        return 0;
    }

    /**
     * Get a statistic value by exact player name
     * @param stat the statistic value
     * @param playerName the exact player's name to look for
     * @return a matching statistical value otherwise 0
     * @throws SQLException
     */
    @Override
    public int getStatExact(String stat, String playerName) throws SQLException {
        ResultSet result = executeQuery("SELECT `" + stat + "` FROM `" + dbTable + "` WHERE `name` = '" + playerName + "' LIMIT 1;", false);
        return (result != null && result.next()) ? result.getInt(stat) : -1;
    }

    /**
     * Get a statistic value by matching partial player name
     * @param stat the statistic value
     * @param playerName the partial player's name to look for
     * @return a matching statistical value otherwise 0
     * @throws SQLException
     */
    @Override
    public int getStatLike(String stat, String playerName) throws SQLException {
        ResultSet result = executeQuery("SELECT `" + stat + "` FROM `" + dbTable + "` WHERE `name` LIKE '%" + playerName + "%' LIMIT 1;", false);
        return (result != null && result.next()) ? result.getInt(stat) : -1;
    }

    /**
     * Get statistics by exact player name
     * @param playerName the exact player's name to look for
     * @return a set of all matching entries
     * @throws SQLException
     */
    @Override
    public PlayerStatistic getStatsExact(String playerName) throws SQLException {
        ResultSet result = executeQuery("SELECT `name`,`kills`,`deaths`,`streak`,`currentstreak`, `elo` FROM `" + dbTable + "` WHERE `name` = '" + playerName + "' LIMIT 1;", false);
        if (result.next()) {
            return new PlayerStatistic(result.getString("name"),
                    result.getInt("kills"),
                    result.getInt("deaths"),
                    result.getInt("streak"),
                    result.getInt("currentstreak"),
                    result.getInt("elo"));
        }
        return null;
    }

    /**
     * Get ALL statistics player names and entry IDs
     * @return a set of all entry IDs and player names
     * @throws SQLException
     */
    @Override
    public Map<Integer, String> getStatsIDsAndNames() throws SQLException {
        Map<Integer, String> map = new LinkedHashMap<>();
        ResultSet result = executeQuery("SELECT `oid`, `name` FROM `" + dbTable + "` WHERE 1 ORDER BY `kills` DESC;", false);
        while (result.next()) {
            map.put(result.getInt("oid"), result.getString("name"));
        }
        return map;
    }

    /**
     * Get statistics by matching partial player name
     * @param playerName the partial player's name to look for
     * @return a set of all matching entries
     * @throws SQLException
     */
    @Override
    public PlayerStatistic getStatsLike(String playerName) throws SQLException {
        ResultSet result = executeQuery("SELECT `name`,`kills`,`deaths`,`streak`,`currentstreak`, `elo` FROM `" + dbTable + "` WHERE `name` LIKE '%" + playerName + "%' LIMIT 1;", false);
        if (result.next()) {
            return new PlayerStatistic(result.getString("name"),
                    result.getInt("kills"),
                    result.getInt("deaths"),
                    result.getInt("streak"),
                    result.getInt("currentstreak"),
                    result.getInt("elo"));
        }
        return null;
    }

    /**
     * Get all player names
     * @return list of all player names
     * @throws SQLException
     */
    @Override
    public List<String> getStatsNames() throws SQLException {
        List<String> list = new ArrayList<>();
        ResultSet result = executeQuery("SELECT `name` FROM `" + dbTable + "` GROUP BY `name`;", false);
        while (result.next()) {
            list.add(result.getString("name"));
        }
        return list;
    }

    /**
     * Get a player's saved UUID entry
     * @param player the player to look for
     * @return their UID
     * @throws SQLException
     */
    @Override
    public String getStatUIDFromPlayer(Player player) throws SQLException {
        ResultSet result = executeQuery("SELECT `uid` FROM `" + dbTable + "` WHERE `name` = '" + player.getName() + "';", false);
        while (result.next()) {
            return result.getString("uid");
        }
        return "";
    }

    /**
     * Get the top players sorted by a given column
     * @param amount the amount to return
     * @param orderBy the column to sort by
     * @param ascending true if ascending order, false otherwise
     * @return a set of all stats from the top players
     * @throws SQLException
     */
    @Override
    public List<PlayerStatistic> getTopSorted(int amount, String orderBy, boolean ascending) throws SQLException {
        String query = "SELECT `name`,`kills`,`deaths`,`streak`,`currentstreak`,`elo` FROM `" +
                dbTable + "` WHERE 1 ORDER BY `" + orderBy + "` " + (ascending?"ASC":"DESC") + " LIMIT " + amount + ";";


        List<PlayerStatistic> list = new ArrayList<>();

        ResultSet result = executeQuery(query, false);

        if (result == null) {
            return null;
        }

        while (result.next()) {
            list.add(new PlayerStatistic(result.getString("name"),
                    result.getInt("kills"),
                    result.getInt("deaths"),
                    result.getInt("streak"),
                    result.getInt("currentstreak"),
                    result.getInt("elo")));
        }
        return list;
    }

    /**
     * Check whether an entry matches a player UUID
     * @param uuid the UUID to find
     * @return true if found, false otherwise
     */
    @Override
    public boolean hasEntry(UUID uuid) {
        try {
            ResultSet result = executeQuery("SELECT * FROM `" + dbTable + "` WHERE `uid` = '" + uuid + "';", false);
            return result != null && result.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Increase player death count, update ELO score and reset streak
     * @param uuid the player's UUID
     * @param elo the new ELO rating
     */
    @Override
    public void increaseDeaths(UUID uuid, int elo) {
        long time = System.currentTimeMillis() / 1000;
        try {
            executeQuery("UPDATE `" + dbTable + "` SET `deaths` = `deaths`+1, `elo` = " + elo +
                    ", `currentstreak` = 0, `time` = " + time + " WHERE `uid` = '" + uuid + "'", true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Increase player kill count, update ELO score and the max and current streak
     * @param uuid the player's UUID
     * @param elo the new ELO rating
     */
    @Override
    public void increaseKillsAndMaxStreak(UUID uuid, int elo) {
        long time = System.currentTimeMillis() / 1000;
        try {
            executeQuery("UPDATE `" + dbTable + "` SET `kills` = `kills`+1, `elo` = '" + elo +
                    "', `streak` = `streak`+1, `currentstreak` = `currentstreak`+1, `time` = " + time +
                    " WHERE `uid` = '" + uuid + "'", true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Increase player kill count, update ELO score and the current streak
     * @param uuid the player's UUID
     * @param elo the new ELO rating
     */
    @Override
    public void increaseKillsAndStreak(UUID uuid, int elo) {
        long time = System.currentTimeMillis() / 1000;
        try {
            executeQuery("UPDATE `" + dbTable + "` SET `kills` = `kills`+1, `elo` = '" + elo +
                    "', `currentstreak` = `currentstreak`+1, `time` = " + time +
                    " WHERE `uid` = '" + uuid + "'", true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set the UUID of a certain player entry
     * @param player the player to find and update
     * @throws SQLException
     */
    @Override
    public void setStatUIDByPlayer(Player player) throws SQLException {
        executeQuery("UPDATE `" + dbTable + "` SET `uid` = '" + player.getUniqueId() + "' WHERE `name` = '" + player.getName() + "';", true);
    }
}
