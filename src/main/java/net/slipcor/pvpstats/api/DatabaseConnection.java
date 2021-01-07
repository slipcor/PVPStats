package net.slipcor.pvpstats.api;

import net.slipcor.pvpstats.classes.PlayerStatistic;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Database connection interface, defines all necessary methods to handle the database
 *
 * @author slipcor
 */
public interface DatabaseConnection {

    boolean allowsAsync();

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

    /**
     * Add the victim column to the database structure
     */
    void addKillVictim();

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
     * @param playerName the killer's name
     * @param uuid       the killer's uuid
     * @param victimName the victim's name
     * @param victimUUID the victim's uuid
     * @param world      the world name where the kill happened
     */
    void addKill(String playerName, String uuid, String victimName, String victimUUID, String world);

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
     * @param uuid the player's name
     */
    void deleteKillsByUUID(UUID uuid);

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
     * Delete statistics by player name
     *
     * @param uuid the player's name
     */
    void deleteStatsByUUID(UUID uuid);

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
     * Get all player names
     *
     * @return all player names
     * @throws SQLException
     */
    List<String> getNamesWithoutUUIDs() throws SQLException;

    /**
     * Get a statistic value by exact player name
     *
     * @param stat       the statistic value
     * @param uuid the exact player's name to look for
     * @return a set of all matching entries
     * @throws SQLException
     */
    int getStats(String stat, UUID uuid) throws SQLException;

    /**
     * Get statistics by exact player name
     *
     * @param offlinePlayer the exact player's name to look for
     * @return the first matching player stat entry
     * @throws SQLException
     */
    PlayerStatistic getStats(OfflinePlayer offlinePlayer) throws SQLException;

    /**
     * Get all player UUIDs
     *
     * @return all player UUIDs
     * @throws SQLException
     */
    List<UUID> getStatsUUIDs() throws SQLException;

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
     * @param name the player's name
     * @param uuid the player's UUID
     * @param elo  the new ELO rating
     */
    void increaseDeaths(String name, UUID uuid, int elo);

    /**
     * Increase player kill count, update ELO score and the max and current streak
     *
     * @param name the player's name
     * @param uuid the player's UUID
     * @param elo  the new ELO rating
     */
    void increaseKillsAndMaxStreak(String name, UUID uuid, int elo);

    /**
     * Increase player kill count, update ELO score and the current streak
     *
     * @param name the player's name
     * @param uuid the player's UUID
     * @param elo  the new ELO rating
     */
    void increaseKillsAndStreak(String name, UUID uuid, int elo);

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
     * @param uuid the player id to find
     * @param entry      the entry to set
     * @param value      the value to set
     */
    void setSpecificStat(UUID uuid, String entry, int value) throws SQLException;

    /**
     * Set the UUID of a certain player entry
     *
     * @param player the player to find and update
     * @throws SQLException
     */
    void setStatUIDByPlayer(OfflinePlayer player) throws SQLException;

    /**
     * Remove duplicate entries
     *
     * @param sender the CommandSender issuing the cleanup
     *
     * @return the amount of entries that have been removed
     */
    int cleanup(CommandSender sender);
}
