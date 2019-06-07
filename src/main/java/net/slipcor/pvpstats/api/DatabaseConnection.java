package net.slipcor.pvpstats.api;

import net.slipcor.pvpstats.impl.PlayerStatistic;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface DatabaseConnection {

    /**
     * @param printError should we print errors that we encounter?
     * @return true if the connection was made successfully, false otherwise.
     */
    boolean connect(boolean printError);

    /**
     * @param database The database to check for the table in.
     * @param table    The table to check for existence.
     * @return true if the table exists, false if there was an error or the database doesn't exist.
     */
    boolean tableExists(String database, String table);

    void createStatsTable(boolean printError);

    void createKillStatsTable(boolean printError);

    List<PlayerStatistic> getTopSorted(int amount, String orderBy, boolean sorting) throws SQLException;

    PlayerStatistic getStatsExact(String playerName) throws SQLException;

    PlayerStatistic getStatsLike(String playerName) throws SQLException;

    int getStatExact(String stat, String playerName) throws SQLException;

    int getStatLike(String stat, String playerName) throws SQLException;

    int deleteKillsOlderThan(long timestamp) throws SQLException;

    int deleteStatsOlderThan(long timestamp) throws SQLException;

    Map<Integer, String> getStatsIDsAndNames() throws SQLException;

    void deleteStatsByIDs(List<Integer> list) throws SQLException;

    List<String> getStatsNames() throws SQLException;

    String getStatUIDFromPlayer(Player player) throws SQLException;

    void setStatUIDByPlayer(Player player) throws SQLException;

    @Deprecated
    void customQuery(String query);

    void deleteKillsByName(String playerName);
    void deleteStatsByName(String playerName);

    void deleteStats();
    void deleteKills();

    void addKill(String sPlayer, UUID uuid, boolean kill);

    void increaseDeaths(UUID pid, int elo);

    void increaseKillsAndStreak(UUID pid, int elo);
    void increaseKillsAndMaxStreak(UUID pid, int elo);

    void addFirstStat(String playerName, UUID uuid, int kills, int deaths, int elo);

    boolean hasEntry(UUID uuid);

    void setSpecificStat(String playerName, String entry, int value) throws SQLException;
}
