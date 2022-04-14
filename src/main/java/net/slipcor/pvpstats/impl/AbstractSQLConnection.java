package net.slipcor.pvpstats.impl;

import net.slipcor.pvpstats.PVPStats;
import net.slipcor.pvpstats.api.DatabaseConnection;
import net.slipcor.pvpstats.classes.PlayerHandler;
import net.slipcor.pvpstats.classes.PlayerStatistic;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;

/**
 * A partial implementation of methods that are handled the same by all SQL implementations
 */
public abstract class AbstractSQLConnection implements DatabaseConnection {

    // Database tables
    final String dbTable;
    final String dbKillTable;

    // The connection object
    Connection databaseConnection;

    boolean collectPrecise = false;

    AbstractSQLConnection(String dbTable, String dbKillTable) {
        this.dbTable = dbTable;
        this.dbKillTable = dbKillTable;
    }

    /**
     * Actually execute an SQL query
     *
     * @param query    the query to send to the SQL server.
     * @param modifies tf the Query modifies the database, set this to true, otherwise set this to false
     * @return If modifies is true, returns a valid ResultSet obtained from the query, otherwise returns null.
     * @throws SQLException if the query had an error or there was not a valid connection.
     */
    protected ResultSet executeQuery(final String query, final boolean modifies) throws SQLException {
        //System.out.println(query);
        Statement statement = this.databaseConnection.createStatement();
        if (modifies) {
            statement.execute(query);
            return null;
        } else {
            return statement.executeQuery(query);
        }
    }

    /**
     * Check whether the database has a column
     *
     * @param tableName the table to check
     * @param column the column to find
     * @return whether the database has this column
     */
    @Override
    public boolean hasColumn(String tableName, String column) {
        try {
            ResultSet result = executeQuery(
                    "SELECT `" + column + "` FROM `" + tableName + "` WHERE 1 LIMIT 1", false);
            result.next(); // we just need to verify that this runs without error
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    /*
     * ----------------------
     *  TABLE UPDATES
     * ----------------------
     */

    /**
     * Add the world column to the database structure
     */
    @Override
    public void addWorldColumn() {
        try {
            String world = Bukkit.getServer().getWorlds().size() > 0 ? Bukkit.getServer().getWorlds().get(0).getName() : "unknown";
            executeQuery("ALTER TABLE `" + dbKillTable + "` ADD `world` varchar(42) NOT NULL DEFAULT '" + world + "';", true);
        } catch (SQLException e) {
        }
    }

    /**
     * Add the victim column to the database structure
     */
    @Override
    public void addKillVictim() {
        try {
            executeQuery("ALTER TABLE `" + dbKillTable + "` ADD `victim` varchar(42);", true);
            executeQuery("ALTER TABLE `" + dbKillTable + "` ADD `victimuid` varchar(42);", true);

            if (this instanceof SQLiteConnection) {
                executeQuery("ALTER TABLE `" + dbKillTable + "` ADD `time` int(16) not null default 0;", true);
            }
        } catch (SQLException e) {
        }
    }

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
     *
     * @param playerName the killer's name
     * @param uuid       the killer's uuid
     * @param victimName the victim's name
     * @param victimUUID the victim's uuid
     * @param world      the world in which the kill happened
     */
    @Override
    public void addKill(String playerName, String uuid, String victimName, String victimUUID, String world) {
        if (!collectPrecise) {
            return;
        }
        long time = System.currentTimeMillis() / 1000;
        try {
            executeQuery("INSERT INTO " + dbKillTable + " (`name`,`uid`,`victim`,`victimuid`,`time`,`world`) VALUES(" +
                    "'" + playerName + "', '" + uuid + "', '" + victimName + "', '" + victimUUID + "', " + time + ", '" + world +"')", true);
        } catch (SQLException e) {
        }
    }

    /**
     * Remove duplicate UUIDs from the database
     *
     * @param sender the sender trying to run the cleanup
     * @param rowid  the row identifier
     *
     * @return how many entries have been removed
     */
    protected int cleanup(CommandSender sender, String rowid) {
        String query = "SELECT `" + rowid + "`, `uid` FROM " + dbTable + " WHERE 1 ORDER BY `kills` DESC;";
        List<Integer> ids = new ArrayList<>();
        Set<String> uids = new HashSet<>();
        try {
            ResultSet result = executeQuery(query, false);
            while (result != null && result.next()) {
                String uid = result.getString("uid");
                if (uids.contains(uid)) {
                    ids.add(result.getInt(rowid));
                } else {
                    uids.add(uid);
                }
            }

            if (ids.size() > 0) {
                StringBuilder buffer = new StringBuilder("DELETE FROM `");
                buffer.append(dbTable);
                buffer.append("` WHERE `");
                buffer.append(rowid);
                buffer.append("` IN (-1");

                for (Integer i : ids) {
                    buffer.append(',');
                    buffer.append(i);
                }

                buffer.append(");");
                executeQuery(buffer.toString(), true);
            }
            return ids.size();
        } catch (SQLException e) {
            System.out.println("Error while cleaning up. Please send this to slipcor:");
            e.printStackTrace();
        }
        if (sender instanceof Player) {
            PVPStats.getInstance().sendPrefixed(sender, ChatColor.RED + "There was an error trying to clean up. Please see the logfile!");
        }
        return -1;
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
        }
    }

    /**
     * Delete kill stats of a player
     *
     * @param uuid the player's UUID
     */
    @Override
    public void deleteKillsByUUID(UUID uuid) {
        if (!collectPrecise) {
            return;
        }
        try {
            executeQuery("DELETE FROM `" + dbKillTable + "` WHERE `uid` = '" + uuid
                    + "';", true);
        } catch (SQLException e) {
        }
    }

    /**
     * Delete kill stats older than a timestamp
     *
     * @param timestamp the timestamp to compare to
     * @throws SQLException
     */
    @Override
    public int deleteKillsOlderThan(long timestamp) throws SQLException {
        if (!collectPrecise) {
            return 0;
        }

        int count = 0;

        ResultSet result = executeQuery("SELECT `time` FROM `" + dbKillTable + "` WHERE `time` < " + timestamp + ";", false);
        while (result.next()) {
            count++;
        }
        executeQuery("DELETE FROM `" + dbKillTable + "` WHERE `time` < " + timestamp + ";", true);
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
        }
    }

    /**
     * Delete statistics by player UUID
     *
     * @param uuid the player's UUID
     */
    @Override
    public void deleteStatsByUUID(UUID uuid) {
        try {
            executeQuery("DELETE FROM `" + dbTable + "` WHERE `uid` = '" + uuid
                    + "';", true);
        } catch (SQLException e) {

        }
    }

    /**
     * Delete statistics older than a timestamp
     *
     * @param timestamp the timestamp to compare to
     * @throws SQLException
     */
    @Override
    public int deleteStatsOlderThan(long timestamp) throws SQLException {

        int count = 0;

        ResultSet result = executeQuery("SELECT `time` FROM `" + dbTable + "` WHERE `time` < " + timestamp + ";", false);
        while (result.next()) {
            count++;
        }
        executeQuery("DELETE FROM `" + dbTable + "` WHERE `time` < " + timestamp + ";", true);
        return count;
    }

    /**
     * Find a player by their name
     *
     * @param name the player's last known name
     * @return an OfflinePlayer, can be null!
     */
    @Override
    public OfflinePlayer findPlayer(String name) {
        try {
            PreparedStatement statement = databaseConnection.prepareStatement("SELECT `name`, `uid` FROM " + dbTable + " WHERE `name` LIKE ?;");
            statement.setString(1, "%" + name + "%");
            ResultSet result = statement.executeQuery();

            String match = null;
            UUID matchUUID = null;

            while (result.next()) {

                String rowName = result.getString("name");
                String rowUUID = result.getString("uid");

                if (rowUUID != null && !rowUUID.isEmpty()) {
                    matchUUID = UUID.fromString(rowUUID);
                }
                match = rowName;

                if (match.equalsIgnoreCase(name)) {
                    break;
                }
            }

            if (matchUUID != null) {
                return Bukkit.getOfflinePlayer(matchUUID);
            }
            if (match != null) {
                return Bukkit.getOfflinePlayer(match);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get all statistics
     *
     * @return a list of all stats
     * @throws SQLException
     */
    @Override
    public List<PlayerStatistic> getAll() throws SQLException {
        String query = "SELECT `name`,`kills`,`deaths`,`streak`,`currentstreak`,`elo`,`time`,`uid` FROM `" +
                dbTable + "` WHERE 1;";

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
                    result.getInt("elo"),
                    result.getLong("time"),
                    UUID.fromString(result.getString("uid"))));
        }
        return list;
    }

    /**
     * Get a statistic value by player UUID
     *
     * @param stat       the statistic value
     * @param uuid the player's UUID to look for
     * @return a set of all matching entries
     * @throws SQLException
     */
    @Override
    public int getStats(String stat, UUID uuid) throws SQLException {
        ResultSet result = executeQuery("SELECT `" + stat + "` FROM `" + dbTable + "` WHERE `uid` = '" + uuid + "' LIMIT 1;", false);
        return (result != null && result.next()) ? result.getInt(stat) : -1;
    }

    /**
     * Get statistics by player UUID
     *
     * @param offlinePlayer the player to look for
     * @return the first matching player stat entry
     * @throws SQLException
     */
    @Override
    public PlayerStatistic getStats(OfflinePlayer offlinePlayer) throws SQLException {
        ResultSet result = executeQuery("SELECT `name`,`kills`,`deaths`,`streak`,`currentstreak`, `elo`,`time`,`uid` FROM `" + dbTable + "` WHERE `uid` = '" + offlinePlayer.getUniqueId() + "' LIMIT 1;", false);
        if (result.next()) {
            return new PlayerStatistic(result.getString("name"),
                    result.getInt("kills"),
                    result.getInt("deaths"),
                    result.getInt("streak"),
                    result.getInt("currentstreak"),
                    result.getInt("elo"),
                    result.getInt("time"),
                    UUID.fromString(result.getString("uid")));
        }
        return new PlayerStatistic(
                PlayerHandler.getPlayerName(offlinePlayer),
                0, 0, 0, 0, 0, 0,
                offlinePlayer.getUniqueId());
    }

    /**
     * Get all player names that have no UUID
     *
     * @return all player names that have no UUID attached
     * @throws SQLException
     */
    @Override
    public List<String> getNamesWithoutUUIDs() throws SQLException {
        List<String> list = new ArrayList<>();
        ResultSet result = executeQuery("SELECT `name` FROM `" + dbTable + "` WHERE `uid` = '';", false);
        while (result.next()) {
            list.add(result.getString("name"));
        }
        return list;
    }

    /**
     * Get all player UUIDs
     *
     * @return all player UUIDs
     * @throws SQLException
     */
    @Override
    public List<UUID> getStatsUUIDs() throws SQLException {
        List<UUID> ids = new ArrayList<>();
        ResultSet result = executeQuery("SELECT `uid` FROM `" + dbTable + "` WHERE 1;", false);
        while (result.next()) {
            ids.add(UUID.fromString(result.getString("uid")));
        }
        return ids;
    }

    /**
     * Get the top players sorted by a given column
     *
     * @param amount    the amount to return
     * @param orderBy   the column to sort by
     * @param world     the world to filter by
     * @param days      the amount of days to query
     * @return a list of all stats from the top players
     * @throws SQLException
     */
    @Override
    public List<PlayerStatistic> getTopWorldSorted(int amount, String orderBy, String world, int days) throws SQLException {

        List<PlayerStatistic> list = new ArrayList<>();

        long queryTime = (System.currentTimeMillis() / 1000) - (days * (60 * 60 * 24));

        ResultSet result = executeQuery("SELECT * FROM " + dbKillTable + " WHERE `time` > " + queryTime + " ORDER BY `time` DESC", false);

        if (result == null) {
            return null;
        }
        Map<UUID, PlayerStatistic> players = new HashMap<>();

        /**
         * executeQuery("INSERT INTO " + dbKillTable + " (`name`,`uid`,`victim`,`victimuid`,`time`,`world`) VALUES(" +
         *                     "'" + playerName + "', '" + uuid + "', '" + victimName + "', '" + victimUUID + "', " + time + ", '" + world +"')", true);
         */

        while (result.next()) {
            if (!result.getString("world").equals(world)) {
                continue;
            }
            UUID killerUID = UUID.fromString(result.getString("uid"));
            UUID victimUID = UUID.fromString(result.getString("victimuid"));

            if (killerUID.equals(victimUID)) {
                continue;
            }

            if (players.containsKey(killerUID)) {
                // we already are tracked, let us just add one to the kills!
                players.get(killerUID).addKill();
            } else {
                players.put(killerUID,
                        new PlayerStatistic(
                                result.getString("name"),
                                1, 0, 0, 0, 0, 0,
                                killerUID
                        )
                );
            }
            if (players.containsKey(victimUID)) {
                // we already are tracked, let us just add one to the deaths!
                players.get(victimUID).addDeath();
            } else {
                players.put(victimUID,
                        new PlayerStatistic(
                                result.getString("victim"),
                                0, 1, 0, 0, 0, 0,
                                victimUID
                        )
                );
            }
        }
        list.addAll(players.values());

        if (orderBy.equals("KILLS")) {
            Collections.sort(list, Comparator.comparingInt(PlayerStatistic::getKills).reversed());
        } else if (orderBy.equals("DEATHS")) {
            Collections.sort(list, Comparator.comparingInt(PlayerStatistic::getDeaths).reversed());
        } else {
            Collections.sort(list, Comparator.comparingDouble(PlayerStatistic::getRatio).reversed());
        }

        while (list.size() > amount) {
            list.remove(amount);
        }

        return list;
    }

    /**
     * Get the top players sorted by a given column
     *
     * @param amount    the amount to return
     * @param orderBy   the column to sort by
     * @param days      the amount of days to query
     * @return a list of all stats from the top players
     * @throws SQLException
     */
    @Override
    public List<PlayerStatistic> getTopPlusSorted(int amount, String orderBy, int days) throws SQLException {

        List<PlayerStatistic> list = new ArrayList<>();

        long queryTime = (System.currentTimeMillis() / 1000) - (days * (60 * 60 * 24));

        ResultSet result = executeQuery("SELECT * FROM " + dbKillTable + " WHERE `time` > " + queryTime + " ORDER BY `time` DESC", false);

        if (result == null) {
            return null;
        }
        Map<UUID, PlayerStatistic> players = new HashMap<>();

        /**
         * executeQuery("INSERT INTO " + dbKillTable + " (`name`,`uid`,`victim`,`victimuid`,`time`,`world`) VALUES(" +
         *                     "'" + playerName + "', '" + uuid + "', '" + victimName + "', '" + victimUUID + "', " + time + ", '" + world +"')", true);
         */

        while (result.next()) {
            UUID killerUID = UUID.fromString(result.getString("uid"));
            UUID victimUID = UUID.fromString(result.getString("victimuid"));

            if (killerUID.equals(victimUID)) {
                continue;
            }

            if (players.containsKey(killerUID)) {
                // we already are tracked, let us just add one to the kills!
                players.get(killerUID).addKill();
            } else {
                players.put(killerUID,
                        new PlayerStatistic(
                                result.getString("name"),
                                1, 0, 0, 0, 0, 0,
                                killerUID
                        )
                );
            }
            if (players.containsKey(victimUID)) {
                // we already are tracked, let us just add one to the deaths!
                players.get(victimUID).addDeath();
            } else {
                players.put(victimUID,
                        new PlayerStatistic(
                                result.getString("victim"),
                                0, 1, 0, 0, 0, 0,
                                victimUID
                        )
                );
            }
        }
        list.addAll(players.values());

        if (orderBy.equals("KILLS")) {
            Collections.sort(list, Comparator.comparingInt(PlayerStatistic::getKills).reversed());
        } else if (orderBy.equals("DEATHS")) {
            Collections.sort(list, Comparator.comparingInt(PlayerStatistic::getDeaths).reversed());
        } else {
            Collections.sort(list, Comparator.comparingDouble(PlayerStatistic::getRatio).reversed());
        }

        while (list.size() > amount) {
            list.remove(amount);
        }

        return list;
    }

    /**
     * Get the top players sorted by a given column
     *
     * @param amount    the amount to return
     * @param orderBy   the column to sort by
     * @param ascending true if ascending order, false otherwise
     * @return a list of all stats from the top players
     * @throws SQLException
     */
    @Override
    public List<PlayerStatistic> getTopSorted(int amount, String orderBy, boolean ascending) throws SQLException {
        String query = "SELECT `name`,`kills`,`deaths`,`streak`,`currentstreak`,`elo`,`time`,`uid` FROM `" +
                dbTable + "` WHERE 1 ORDER BY `" + orderBy + "` " + (ascending ? "ASC" : "DESC") + " LIMIT " + amount + ";";

        if (orderBy.contains("`")) {
            // special delivery, just throw all of this into the mix without escaping
            query = "SELECT `name`,`kills`,`deaths`,`streak`,`currentstreak`,`elo`,`time`,`uid` FROM `" +
                    dbTable + "` WHERE 1 ORDER BY " + orderBy + " " + (ascending ? "ASC" : "DESC") + " LIMIT " + amount + ";";
        }

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
                    result.getInt("elo"),
                    result.getInt("time"),
                    UUID.fromString(result.getString("uid"))));
        }
        return list;
    }

    /**
     * Check whether an entry matches a player UUID
     *
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
     *
     * @param name the player's name
     * @param uuid the player's UUID
     * @param elo  the new ELO rating
     */
    @Override
    public void increaseDeaths(String name, UUID uuid, int elo) {
        long time = System.currentTimeMillis() / 1000;
        try {
            executeQuery("UPDATE `" + dbTable + "` SET `name` = '" + name + "', `deaths` = `deaths`+1, `elo` = " + elo +
                    ", `currentstreak` = 0, `time` = " + time + " WHERE `uid` = '" + uuid + "'", true);
        } catch (SQLException e) {
        }
    }

    /**
     * Increase player kill count, update ELO score and the max and current streak
     *
     * @param name the player's name
     * @param uuid the player's UUID
     * @param elo  the new ELO rating
     */
    @Override
    public void increaseKillsAndMaxStreak(String name, UUID uuid, int elo) {
        long time = System.currentTimeMillis() / 1000;
        try {
            executeQuery("UPDATE `" + dbTable + "` SET `name` = '" + name + "', `kills` = `kills`+1, `elo` = '" + elo +
                    "', `streak` = `streak`+1, `currentstreak` = `currentstreak`+1, `time` = " + time +
                    " WHERE `uid` = '" + uuid + "'", true);
        } catch (SQLException e) {
        }
    }

    /**
     * Increase player kill count, update ELO score and the current streak
     *
     * @param name the player's name
     * @param uuid the player's UUID
     * @param elo  the new ELO rating
     */
    @Override
    public void increaseKillsAndStreak(String name, UUID uuid, int elo) {
        long time = System.currentTimeMillis() / 1000;
        try {
            executeQuery("UPDATE `" + dbTable + "` SET `name` = '" + name + "', `kills` = `kills`+1, `elo` = '" + elo +
                    "', `currentstreak` = `currentstreak`+1, `time` = " + time +
                    " WHERE `uid` = '" + uuid + "'", true);
        } catch (SQLException e) {
        }
    }

    @Override
    public void insert(PlayerStatistic stat) throws SQLException {
        executeQuery("INSERT INTO `" + dbTable +
                "` (`name`, `uid`, `kills`,`deaths`,`streak`,`currentstreak`,`elo`,`time`) VALUES ('"
                + stat.getName() + "', '" + stat.getUid() + "', " + stat.getKills() + ", " + stat.getDeaths() + ", " +
                stat.getMaxStreak() + ", " + stat.getCurrentStreak() + ", " + stat.getELO() + ", " + stat.getTime() + ")", true);
    }

    /**
     * @return whether the connection was established properly
     */
    @Override
    public boolean isConnected() {
        return this.databaseConnection != null;
    }


    /**
     * Set specific statistical value of a player
     *
     * @param uuid the player to find
     * @param entry      the entry to set
     * @param value      the value to set
     */
    @Override
    public void setSpecificStat(UUID uuid, String entry, int value) throws SQLException {
        executeQuery("UPDATE `" + dbTable + "` SET `" + entry + "` = " + value + " WHERE `uid` = '" + uuid.toString() + "';", true);
    }

    /**
     * Set the UUID of a certain player entry
     *
     * @param player the player to find and update
     * @throws SQLException
     */
    @Override
    public void setStatUIDByPlayer(OfflinePlayer player) throws SQLException {
        executeQuery("UPDATE `" + dbTable + "` SET `uid` = '" + player.getUniqueId() + "' WHERE `uid` = '" + player.getUniqueId() + "';", true);
    }
}
