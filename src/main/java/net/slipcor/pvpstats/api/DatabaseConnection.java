package net.slipcor.pvpstats.api;

import net.slipcor.pvpstats.classes.PlayerStatistic;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Database connection interface, defines all necessary methods to handle the database
 *
 * @author slipcor
 */
public interface DatabaseConnection {

    /**
     * Try to connect to the database
     *
     * @param printError should we print errors that we encounter?
     * @return true if the connection was made successfully, false otherwise.
     */
    boolean connect(boolean printError);

    /**
     * Check whether a table exists
     *
     * @param database The database to check for the table in.
     * @param table    The table to check for existence.
     * @return true if the table exists, false if there was an error or the database doesn't exist.
     * <p/>
     * This method looks through the information schema that comes with a MySQL installation and checks
     * if a certain table exists within a database.
     */
    boolean tableExists(String database, String table);

    /*
     * ----------------------
     *  TABLE UPDATES
     * ----------------------
     */

    /**
     * Add the world column to the database structure
     */
    void addWorldColumn();

    /*
     * ----------------------
     *  TABLE ENTRY CREATION
     * ----------------------
     */

    /**
     * Create the first statistic entry for a player
     *
     * @param playerName the player's name
     * @param uuid       the player's UUID
     * @param kills      the kill amount
     * @param deaths     the death amount
     * @param elo        the ELO rating
     */
    void addFirstStat(String playerName, UUID uuid, int kills, int deaths, int elo);

    /**
     * Add a kill to the player's count
     *
     * @param playerName the player's name
     * @param uuid       the player's uuid
     * @param kill       true if they did kill, false if they were killed
     * @param world      the world name where the kill happened
     */
    void addKill(String playerName, UUID uuid, boolean kill, String world);

    /**
     * Create the kill stat table
     *
     * @param printError should we print errors that we encounter?
     */
    void createStatsTable(boolean printError);

    /**
     * Create the statistics table
     *
     * @param printError should we print errors that we encounter?
     */
    void createKillStatsTable(boolean printError);

    /**
     * Delete ALL kill stats
     */
    void deleteKills();

    /**
     * Delete kill stats of a player
     *
     * @param playerName the player's name
     */
    void deleteKillsByName(String playerName);

    /**
     * Delete kill stats older than a timestamp
     *
     * @param timestamp the timestamp to compare to
     * @throws SQLException
     */
    int deleteKillsOlderThan(long timestamp) throws SQLException;

    /**
     * Delete all statistics
     */
    void deleteStats();

    /**
     * Delete all statistics by ID
     *
     * @param list the list of IDs to delete
     * @throws SQLException
     */
    void deleteStatsByIDs(List<Integer> list) throws SQLException;

    /**
     * Delete statistics by player name
     *
     * @param playerName the player's name
     */
    void deleteStatsByName(String playerName);

    /**
     * Delete statistics older than a timestamp
     *
     * @param timestamp the timestamp to compare to
     * @throws SQLException
     */
    int deleteStatsOlderThan(long timestamp) throws SQLException;

    /**
     * Get all statistics
     *
     * @return a list of all stats
     * @throws SQLException
     */
    List<PlayerStatistic> getAll() throws SQLException;

    /**
     * Get a statistic value by exact player name
     *
     * @param stat       the statistic value
     * @param playerName the exact player's name to look for
     * @return a set of all matching entries
     * @throws SQLException
     */
    int getStatExact(String stat, String playerName) throws SQLException;

    /**
     * Get a statistic value by matching partial player name
     *
     * @param stat       the statistic value
     * @param playerName the partial player's name to look for
     * @return a set of all matching entries
     * @throws SQLException
     */
    int getStatLike(String stat, String playerName) throws SQLException;

    /**
     * Get statistics by exact player name
     *
     * @param playerName the exact player's name to look for
     * @return the first matching player stat entry
     * @throws SQLException
     */
    PlayerStatistic getStatsExact(String playerName) throws SQLException;

    /**
     * Get ALL statistics player names and entry IDs
     *
     * @return a map of all entry IDs and player names
     * @throws SQLException
     */
    Map<Integer, String> getStatsIDsAndNames() throws SQLException;

    /**
     * Get statistics by matching partial player name
     *
     * @param playerName the partial player's name to look for
     * @return the first matching player stat entry
     * @throws SQLException
     */
    PlayerStatistic getStatsLike(String playerName) throws SQLException;

    /**
     * Get all player names
     *
     * @return all player names
     * @throws SQLException
     */
    List<String> getStatsNames() throws SQLException;

    /**
     * Get a player's saved UUID entry
     *
     * @param player the player to look for
     * @return their UID
     * @throws SQLException
     */
    String getStatUIDFromPlayer(Player player) throws SQLException;

    /**
     * Get the top players sorted by a given column
     *
     * @param amount    the amount to return
     * @param orderBy   the column to sort by
     * @param ascending true if ascending order, false otherwise
     * @return a list of all stats from the top players
     * @throws SQLException
     */
    List<PlayerStatistic> getTopSorted(int amount, String orderBy, boolean ascending) throws SQLException;

    /**
     * Check whether the database has a column
     *
     * @param column the column to find
     * @return whether the database has this column
     */
    boolean hasColumn(String tableName, String column);

    /**
     * Check whether an entry matches a player UUID
     *
     * @param uuid the UUID to find
     * @return true if found, false otherwise
     */
    boolean hasEntry(UUID uuid);

    /**
     * Increase player death count, update ELO score and reset streak
     *
     * @param uuid the player's UUID
     * @param elo  the new ELO rating
     */
    void increaseDeaths(UUID uuid, int elo);

    /**
     * Increase player kill count, update ELO score and the max and current streak
     *
     * @param uuid the player's UUID
     * @param elo  the new ELO rating
     */
    void increaseKillsAndMaxStreak(UUID uuid, int elo);

    /**
     * Increase player kill count, update ELO score and the current streak
     *
     * @param uuid the player's UUID
     * @param elo  the new ELO rating
     */
    void increaseKillsAndStreak(UUID uuid, int elo);

    /**
     * Add player statistic to the database
     *
     * @param stat the player's stats
     */
    void insert(PlayerStatistic stat) throws SQLException;

    /**
     * @return whether the connection was established properly
     */
    boolean isConnected();

    /**
     * Set specific statistical value of a player
     *
     * @param playerName the player to find
     * @param entry      the entry to set
     * @param value      the value to set
     */
    void setSpecificStat(String playerName, String entry, int value) throws SQLException;

    /**
     * Set the UUID of a certain player entry
     *
     * @param player the player to find and update
     * @throws SQLException
     */
    void setStatUIDByPlayer(Player player) throws SQLException;
}
