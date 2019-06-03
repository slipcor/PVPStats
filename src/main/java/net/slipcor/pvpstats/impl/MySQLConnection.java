package net.slipcor.pvpstats.impl;


import net.slipcor.pvpstats.api.DatabaseConnection;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.List;
import java.util.UUID;

/**
 * Based on MySQLConnection by Jesika(Kaitlyn) Tremaine aka JesiKat
 */
public class MySQLConnection implements DatabaseConnection {
    // SQL connection details
    private final String dbUrl, dbUsername, dbPassword, dbOptions;

    // Database tables
    private final String dbTable, dbKillTable;

    // The connection object
    private Connection databaseConnection;

    public MySQLConnection(String host, int port,
                           String database, String username, String password, String dbOptions,
                           String dbTable, String dbKillTable)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        this.dbUrl = host + ":" + port + "/" + database;
        this.dbUsername = username;
        this.dbPassword = password;
        this.dbOptions = dbOptions;

        this.dbTable = dbTable;
        this.dbKillTable = dbKillTable;
        Class.forName("com.mysql.jdbc.Driver").newInstance();
    }

    /**
     * @param printError should we print errors that we encounter?
     * @return true if the connection was made successfully, false otherwise.
     */
    @Override
    public boolean connect(boolean printError) {
        try {
            this.databaseConnection = DriverManager.getConnection("jdbc:mysql://" + this.dbUrl + this.dbOptions, this.dbUsername, this.dbPassword);
            return this.databaseConnection != null;
        } catch (SQLException e) {
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
        String format = "SELECT * FROM `information_schema`.`TABLES` WHERE TABLE_SCHEMA = '$DB' && TABLE_NAME = '$TABLE';";
        try {
            return this.databaseConnection.createStatement().executeQuery(format.replace("$DB", database).replace("$TABLE", table)).first();
        } catch (SQLException e) {
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
        long time = System.currentTimeMillis() / 1000;
        try {
            executeQuery("INSERT INTO " + dbKillTable + " (`name`,`uid`,`kill`,`time`) VALUES(" +
                    "'" + playerName + "', '" + uuid + "', '" + (kill ? 1 : 0) + "', " + time + ")", true);
        } catch (SQLException e) {
        }
    }

    /**
     * create the kill stat table
     * @param printError should we print errors that we encounter?
     */
    @Override
    public void createKillStatsTable(boolean printError) {
        final String query2 = "CREATE TABLE `" + dbKillTable + "` ( " +
                "`id` int(16) NOT NULL AUTO_INCREMENT, " +
                "`name` varchar(42) NOT NULL, " +
                "`uid` varchar(42), " +
                "`kill` int(1) not null default 0, " +
                "`time` int(16) not null default 0, " +
                "PRIMARY KEY (`id`) ) AUTO_INCREMENT=1 ;";
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
                "`id` int(5) NOT NULL AUTO_INCREMENT, " +
                "`name` varchar(42) NOT NULL, " +
                "`uid` varchar(42), " +
                "`kills` int(8) not null default 0, " +
                "`deaths` int(8) not null default 0, " +
                "`streak` int(8) not null default 0, " +
                "`currentstreak` int(8) not null default 0, " +
                "`elo` int(8) not null default 0, " +
                "`time` int(16) not null default 0, " +
                "PRIMARY KEY (`id`) ) AUTO_INCREMENT=1 ;";
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

        }
    }

    /**
     * Delete ALL kill stats
     */
    @Override
    public void deleteKills() {
        try {
            executeQuery("DELETE FROM `" + dbKillTable + "` WHERE 1;", true);
        } catch (SQLException e) {
        }
    }

    /**
     * Delete kill stats of a player
     * @param playerName the player's name
     */
    @Override
    public void deleteKillsByName(String playerName) {
        try {
            executeQuery("DELETE FROM `" + dbKillTable + "` WHERE `name` = '" + playerName
                    + "';", true);
        } catch (SQLException e) {
        }
    }

    /**
     * Delete kill stats older than a timestamp
     * @param timestamp to compare to
     * @throws SQLException
     */
    @Override
    public void deleteKillsOlderThan(long timestamp) throws SQLException {
        executeQuery("DELETE FROM `" + dbKillTable + "` WHERE `time` < "+timestamp+";", true);
    }

    /**
     * Delete all statistics
     */
    @Override
    public void deleteStats() {
        try {
            executeQuery("DELETE FROM `" + dbTable + "` WHERE 1;", true);
        } catch (SQLException e) {
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
        buff.append("` WHERE `id` IN (");

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

        }
    }

    /**
     * Delete statistics older than a timestamp
     * @param timestamp the timestamp to compare to
     * @throws SQLException
     */
    @Override
    public void deleteStatsOlderThan(long timestamp) throws SQLException {
        executeQuery("DELETE FROM `" + dbTable + "` WHERE `time` < "+timestamp+";", true);
    }

    /**
     * Get a statistic value by exact player name
     * @param stat the statistic value
     * @param playerName the exact player's name to look for
     * @return a set of all matching entries
     * @throws SQLException
     */
    @Override
    public ResultSet getStatExact(String stat, String playerName) throws SQLException {
        return executeQuery("SELECT `" + stat + "` FROM `" + dbTable + "` WHERE `name` = '" + playerName + "' LIMIT 1;", false);
    }

    /**
     * Get a statistic value by matching partial player name
     * @param stat the statistic value
     * @param playerName the partial player's name to look for
     * @return a set of all matching entries
     * @throws SQLException
     */
    @Override
    public ResultSet getStatLike(String stat, String playerName) throws SQLException {
        return executeQuery("SELECT `" + stat + "` FROM `" + dbTable + "` WHERE `name` LIKE '%" + playerName + "%' LIMIT 1;", false);
    }

    /**
     * Get statistics by exact player name
     * @param playerName the exact player's name to look for
     * @return a set of all matching entries
     * @throws SQLException
     */
    @Override
    public ResultSet getStatsExact(String playerName) throws SQLException {
        return executeQuery("SELECT `name`,`kills`,`deaths`,`streak`,`currentstreak`, `elo` FROM `" + dbTable + "` WHERE `name` = '" + playerName + "' LIMIT 1;", false);
    }

    /**
     * Get ALL statistics player names and entry IDs
     * @return a set of all entry IDs and player names
     * @throws SQLException
     */
    @Override
    public ResultSet getStatsIDsAndNames() throws SQLException {
        return executeQuery("SELECT `id`, `name` FROM `" + dbTable + "` WHERE 1 ORDER BY `kills` DESC;", false);
    }

    /**
     * Get statistics by matching partial player name
     * @param playerName the partial player's name to look for
     * @return a set of all matching entries
     * @throws SQLException
     */
    @Override
    public ResultSet getStatsLike(String playerName) throws SQLException {
        return executeQuery("SELECT `name`,`kills`,`deaths`,`streak`,`currentstreak`, `elo` FROM `" + dbTable + "` WHERE `name` LIKE '%" + playerName + "%' LIMIT 1;", false);
    }

    /**
     * Get all player names
     * @return a grouped set of all player names
     * @throws SQLException
     */
    @Override
    public ResultSet getStatsNames() throws SQLException {
        return executeQuery("SELECT `name` FROM `" + dbTable + "` GROUP BY `name`;", false);
    }

    /**
     * Get a player's saved UUID entry
     * @param player the player to look for
     * @return a set of their UID
     * @throws SQLException
     */
    @Override
    public ResultSet getStatUIDFromPlayer(Player player) throws SQLException {
        return executeQuery("SELECT `uid` FROM `" + dbTable + "` WHERE `name` = '" + player.getName() + "';", false);
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
    public ResultSet getTopSorted(int amount, String orderBy, boolean ascending) throws SQLException {
        String query = "SELECT `name`,`kills`,`deaths`,`streak`,`currentstreak`,`elo` FROM `" +
                dbTable + "` WHERE 1 ORDER BY `" + orderBy + "` " + (ascending?"ASC":"DESC") + " LIMIT " + amount + ";";

        return executeQuery(query, false);
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
