package net.slipcor.pvpstats.impl;

import net.slipcor.pvpstats.PVPStats;
import net.slipcor.pvpstats.api.DatabaseConnection;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class FlatFileConnection implements DatabaseConnection {

    // File names
    private final String dbTable, dbKillTable;
    private boolean collectPrecise = false;

    public FlatFileConnection(String dbTable, String dbKillTable) {
        this.dbTable = dbTable;
        this.dbKillTable = dbKillTable;
    }

    // The connection object
    private FileConfiguration statConfig, killStatConfig;

    /**
     * @param printError should we print errors that we encounter?
     * @return true if the connection was made successfully, false otherwise.
     */
    @Override
    public boolean connect(boolean printError) {
        try {
            File file = new File(PVPStats.getInstance().getDataFolder(), dbTable + ".yml");
            if (!file.exists()) {
                file.createNewFile();
            }
            statConfig = new YamlConfiguration();
            statConfig.load(file);

            if (dbKillTable != null && !"".equals(dbKillTable)) {

                File killFile = new File(PVPStats.getInstance().getDataFolder(), dbKillTable + ".yml");
                if (!killFile.exists()) {
                    killFile.createNewFile();
                }
                killStatConfig = new YamlConfiguration();
                killStatConfig.load(killFile);

                collectPrecise = true;
            }
            return statConfig != null;
        } catch (InvalidConfigurationException | IOException e) {
            if (printError) e.printStackTrace();
            return false;
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
        File file = new File(PVPStats.getInstance().getDataFolder(), table + ".yml");
        return file.exists();
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

        String root = uuid+".";

        int count = 0;

        try {
            count = statConfig.getConfigurationSection(statConfig.getCurrentPath()).getValues(false).keySet().size();
        } catch (Exception e) {
            e.printStackTrace();
        }

        count++;

        statConfig.set(root + "oid", count);
        statConfig.set(root + "name", playerName);
        statConfig.set(root + "kills", kills);
        statConfig.set(root + "deaths", deaths);
        statConfig.set(root + "streak", kills);
        statConfig.set(root + "currentstreak", kills);
        statConfig.set(root + "elo", elo);
        statConfig.set(root + "time", time);

        save(statConfig, dbTable);
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

        StringBuilder root = new StringBuilder(uuid.toString());
        root.append('.');
        root.append(kill?"kills.":"deaths.");
        root.append(time);

        killStatConfig.set(root.toString(), playerName);

        save(killStatConfig, dbKillTable);
    }

    /**
     * create the kill stat table
     * @param printError should we print errors that we encounter?
     */
    @Override
    public void createKillStatsTable(boolean printError) {
        /*
         * uuid:
         *   kills:
         *     time: playerName
         *     ...
         *   deaths:
         *     time: playerName
         */
        // needs no setup
    }

    /**
     * create the statistics table
     * @param printError should we print errors that we encounter?
     */
    @Override
    public void createStatsTable(boolean printError) {
        /*
         * uuid:
         *   oid: order ID
         *   name: playerName
         *   kills: kill amount
         *   deaths: death amount
         *   streak: max streak
         *   currentstreak: current streak
         *   elo: elo score
         *   time: last time seen
         */
        // needs no setup
    }

    /**
     * Run a custom query. THIS WILL BE REMOVED
     * @param query the query to run
     */
    @Override
    public void customQuery(String query) {

    }

    /**
     * Delete ALL kill stats
     */
    @Override
    public void deleteKills() {
        if (!collectPrecise) {
            return;
        }
        killStatConfig = new YamlConfiguration();
        save(killStatConfig, dbKillTable);
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
        ConfigurationSection root = killStatConfig.getConfigurationSection(killStatConfig.getCurrentPath());

        Map<String, Object> uuids = root.getValues(false);

        List<String> removeables = new ArrayList<>();

        uuids: for (String uuid : uuids.keySet()) {
            ConfigurationSection player = root.getConfigurationSection(uuid);

            Map<String, Object> kills = player.getConfigurationSection("kills").getValues(false);
            Map<String, Object> deaths = player.getConfigurationSection("deaths").getValues(false);

            for (String key : kills.keySet()) {
                if (player.getConfigurationSection("kills").getString(key, "").equals(playerName)) {
                    removeables.add(uuid);
                    continue uuids;
                }
            }

            for (String key : deaths.keySet()) {
                if (player.getConfigurationSection("deaths").getString(key, "").equals(playerName)) {
                    removeables.add(uuid);
                    continue uuids;
                }
            }
        }

        for (String key : removeables) {
            killStatConfig.set(key, null);
        }

        save(killStatConfig, dbKillTable);
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
        ConfigurationSection root = killStatConfig.getConfigurationSection(killStatConfig.getCurrentPath());

        Map<String, Object> uuids = root.getValues(false);

        List<String> removeables = new ArrayList<>();

        for (String uuid : uuids.keySet()) {
            ConfigurationSection player = root.getConfigurationSection(uuid);

            Map<String, Object> kills = new HashMap<>();
            try {
                kills = player.getConfigurationSection("kills").getValues(false);
            } catch (NullPointerException e) {
            }
            Map<String, Object> deaths = new HashMap<>();
            try {
                deaths = player.getConfigurationSection("deaths").getValues(false);
            } catch (NullPointerException e) {
            }

            for (String key : kills.keySet()) {
                long time = Long.parseLong(key);
                if (time < timestamp) {
                    removeables.add(uuid+".kills."+key);
                }
            }

            for (String key : deaths.keySet()) {
                long time = Long.parseLong(key);
                if (time < timestamp) {
                    removeables.add(uuid+".deaths."+key);
                }
            }
        }

        for (String key : removeables) {
            killStatConfig.set(key, null);
        }

        save(killStatConfig, dbKillTable);
        return removeables.size();
    }

    /**
     * Delete all statistics
     */
    @Override
    public void deleteStats() {
        statConfig = new YamlConfiguration();
        save(statConfig, dbTable);
    }

    /**
     * Delete all statistics by ID
     * @param list the list of IDs to delete
     * @throws SQLException
     */
    @Override
    public void deleteStatsByIDs(List<Integer> list) throws SQLException {
        ConfigurationSection root = statConfig.getConfigurationSection(statConfig.getCurrentPath());

        Map<String, Object> uuids = root.getValues(false);

        List<String> removeables = new ArrayList<>();

        for (String uuid : uuids.keySet()) {
            ConfigurationSection player = root.getConfigurationSection(uuid);

            if (list.contains(player.getInt("oid", 0))) {
                removeables.add(uuid);
            }
        }

        for (String key : removeables) {
            statConfig.set(key, null);
        }

        save(statConfig, dbTable);
    }

    /**
     * Delete statistics by player name
     * @param playerName the player's name
     */
    @Override
    public void deleteStatsByName(String playerName) {
        ConfigurationSection root = statConfig.getConfigurationSection(statConfig.getCurrentPath());

        Map<String, Object> uuids = root.getValues(false);

        List<String> removeables = new ArrayList<>();

        for (String uuid : uuids.keySet()) {
            ConfigurationSection player = root.getConfigurationSection(uuid);

            if (playerName.equals(player.getString("name", ""))) {
                removeables.add(uuid);
            }
        }

        for (String key : removeables) {
            statConfig.set(key, null);
        }

        save(statConfig, dbTable);
    }

    /**
     * Delete statistics older than a timestamp
     * @param timestamp the timestamp to compare to
     * @throws SQLException
     */
    @Override
    public int deleteStatsOlderThan(long timestamp) throws SQLException {
        ConfigurationSection root = statConfig.getConfigurationSection(statConfig.getCurrentPath());

        Map<String, Object> uuids = root.getValues(false);

        List<String> removeables = new ArrayList<>();

        for (String uuid : uuids.keySet()) {
            ConfigurationSection player = root.getConfigurationSection(uuid);

            if (timestamp > player.getLong("time", 0)) {
                removeables.add(uuid);
            }
        }

        for (String key : removeables) {
            statConfig.set(key, null);
        }

        save(statConfig, dbTable);
        return removeables.size();
    }

    /**
     * Get a statistic value by exact player name
     * @param stat the statistic value
     * @param playerName the exact player's name to look for
     * @return a set of all matching entries
     * @throws SQLException
     */
    @Override
    public int getStatExact(String stat, String playerName) throws SQLException {
        ConfigurationSection root = statConfig.getConfigurationSection(statConfig.getCurrentPath());

        Map<String, Object> uuids = root.getValues(false);

        for (String uuid : uuids.keySet()) {
            ConfigurationSection player = root.getConfigurationSection(uuid);

            if (playerName.equals(player.getString("name", ""))) {
                return player.getInt(stat, 0);
            }
        }
        return 0;
    }

    /**
     * Get a statistic value by matching partial player name
     * @param stat the statistic value
     * @param playerName the partial player's name to look for
     * @return a set of all matching entries
     * @throws SQLException
     */
    @Override
    public int getStatLike(String stat, String playerName) throws SQLException {
        ConfigurationSection root = statConfig.getConfigurationSection(statConfig.getCurrentPath());

        Map<String, Object> uuids = root.getValues(false);

        for (String uuid : uuids.keySet()) {
            ConfigurationSection player = root.getConfigurationSection(uuid);

            if (player.getString("name", "").contains(playerName)) {
                return player.getInt(stat, 0);
            }
        }
        return 0;
    }

    /**
     * Get statistics by exact player name
     * @param playerName the exact player's name to look for
     * @return a set of all matching entries
     * @throws SQLException
     */
    @Override
    public PlayerStatistic getStatsExact(String playerName) throws SQLException {
        ConfigurationSection root = statConfig.getConfigurationSection(statConfig.getCurrentPath());

        Map<String, Object> uuids = root.getValues(false);

        for (String uuid : uuids.keySet()) {
            ConfigurationSection player = root.getConfigurationSection(uuid);

            if (playerName.equals(player.getString("name", ""))) {
                return new PlayerStatistic(player.getString("name", ""),
                        player.getInt("kills", 0),
                        player.getInt("deaths", 0),
                        player.getInt("streak", 0),
                        player.getInt("currentstreak", 0),
                        player.getInt("elo", 0));
            }
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
        /*
         * Instead of just a sorted row, this implementation will return only entries that actually have duplicates
         * as this is the only purpose of this method so far. Find duplicate name entries and delete all that are of
         * lower kill value.
         * So if there is only one entry - which should be the case until someone renames to a name that formerly existed -
         * we do not need to remove any entries!
         */
        Map<Integer, String> result = new LinkedHashMap<>();


        Map<String, Integer> maxKills = new HashMap<>();
        Map<String, Integer> maxOID = new HashMap<>();
        Map<String, List<Integer>> oids = new HashMap<>();

        ConfigurationSection root = statConfig.getConfigurationSection(statConfig.getCurrentPath());

        Map<String, Object> uuids = root.getValues(false);

        for (String uuid : uuids.keySet()) {
            ConfigurationSection player = root.getConfigurationSection(uuid);
            String playerName = player.getString("name", "");
            if (oids.containsKey(playerName)) {
                // we already found an entry. compare kills
                List<Integer> list = oids.get(playerName);
                int thisKills = player.getInt("kills", 0);
                int maxKillsBefore = maxKills.get(playerName);
                int thisOID = player.getInt("oid", 0);

                if (thisKills > maxKillsBefore) {
                    // this entry is of more value, let us use this!
                    list.add(thisOID);
                    oids.put(playerName, list);
                    maxKills.put(playerName, thisKills);
                    maxOID.put(playerName, thisOID);

                    break;
                }
            } else {
                // this is the first entry. if this stays this way we don't need to delete anything!
                List<Integer> list = new ArrayList<>();
                int thisOID = player.getInt("oid", 0);
                list.add(thisOID);
                oids.put(playerName, list);
                maxKills.put(playerName, player.getInt("kills", 0));
                maxOID.put(playerName, thisOID);
            }
        }

        // this is the map of entries we need to remove later
        Map<String, List<Integer>> updates = new HashMap<>();

        for (String playerName : oids.keySet()) {
            // only populate if there is more than one OID attached
            if (oids.get(playerName).size() > 1) {
                List<Integer> list = oids.get(playerName);
                list.remove(maxKills.get(playerName)); // remove the highest kill OID
                updates.put(playerName, list); // let us remove the rest later
            }
        }

        for (String playerName : updates.keySet()) {
            // first make an entry that will be the first read, the max entry
            result.put(maxOID.get(playerName), playerName);

            List<Integer> lower = updates.get(playerName);
            for (Integer i : lower) {
                // then add entries that will be removed from the "database" because they are duplicates
                result.put(i, playerName);
            }

        }

        return result;
    }

    /**
     * Get statistics by matching partial player name
     * @param playerName the partial player's name to look for
     * @return a set of all matching entries
     * @throws SQLException
     */
    @Override
    public PlayerStatistic getStatsLike(String playerName) throws SQLException {

        ConfigurationSection root = statConfig.getConfigurationSection(statConfig.getCurrentPath());

        Map<String, Object> uuids = root.getValues(false);

        for (String uuid : uuids.keySet()) {
            ConfigurationSection player = root.getConfigurationSection(uuid);
            if (player.getString("name", "").contains(playerName)) {

                return new PlayerStatistic(player.getString("name", ""),
                        player.getInt("kills", 0),
                        player.getInt("deaths", 0),
                        player.getInt("streak", 0),
                        player.getInt("currentstreak", 0),
                        player.getInt("elo", 0));
            }

        }
        return null;
    }

    /**
     * Get all player names
     * @return all player names
     * @throws SQLException
     */
    @Override
    public List<String> getStatsNames() throws SQLException {
        List<String> names = new ArrayList<>();

        ConfigurationSection root = statConfig.getConfigurationSection(statConfig.getCurrentPath());

        Map<String, Object> uuids = root.getValues(false);

        for (String uuid : uuids.keySet()) {
            ConfigurationSection player = root.getConfigurationSection(uuid);
            String playerName = player.getString("name");

            if (!names.contains(playerName)) {
                names.add(playerName);
            }
        }

        return names;
    }

    /**
     * Get a player's saved UUID entry
     * @param p the player to look for
     * @return their UID
     * @throws SQLException
     */
    @Override
    public String getStatUIDFromPlayer(Player p) throws SQLException {

        ConfigurationSection root = statConfig.getConfigurationSection(statConfig.getCurrentPath());

        Map<String, Object> uuids = root.getValues(false);

        for (String uuid : uuids.keySet()) {
            ConfigurationSection player = root.getConfigurationSection(uuid);

            if (p.getName().equals(player.getString("name", ""))) {
                return uuid;
            }
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
    public List<PlayerStatistic> getTopSorted(final int amount, final String orderBy, final boolean ascending) throws SQLException {
        class CustomComparator implements Comparator<PlayerStatistic> {

            @Override
            public int compare(PlayerStatistic stat1, PlayerStatistic stat2) {
                switch (orderBy) {
                    case "kills":
                        return (stat1.getKills()-stat2.getKills()) * (ascending?1:-1);
                    case "deaths":
                        return (stat1.getDeaths()-stat2.getDeaths()) * (ascending?1:-1);
                    case "streak":
                        return (stat1.getMaxStreak()-stat2.getMaxStreak()) * (ascending?1:-1);
                    case "currentstreak":
                        return (stat1.getCurrentStreak()-stat2.getCurrentStreak()) * (ascending?1:-1);
                    case "elo":
                        return (stat1.getELO()-stat2.getELO()) * (ascending?1:-1);
                }
                return 0;
            }
        }


        ConfigurationSection root = statConfig.getConfigurationSection(statConfig.getCurrentPath());

        Map<String, Object> uuids = root.getValues(false);

        List<PlayerStatistic> result = new ArrayList<>();

        for (String uuid : uuids.keySet()) {
            ConfigurationSection player = root.getConfigurationSection(uuid);

            result.add(new PlayerStatistic(player.getString("name", ""),
                    player.getInt("kills", 0),
                    player.getInt("deaths", 0),
                    player.getInt("streak", 0),
                    player.getInt("currentstreak", 0),
                    player.getInt("elo", 0)));
        }

        Collections.sort(result, new CustomComparator());

        return result.size()>amount?result.subList(0, amount):result;
    }

    /**
     * Check whether an entry matches a player UUID
     * @param uuid the UUID to find
     * @return true if found, false otherwise
     */
    @Override
    public boolean hasEntry(UUID uuid) {
        return !statConfig.getString(uuid+".name", "").equals("");
    }

    /**
     * Increase player death count, update ELO score and reset streak
     * @param uuid the player's UUID
     * @param elo the new ELO rating
     */
    @Override
    public void increaseDeaths(UUID uuid, int elo) {
        long time = System.currentTimeMillis() / 1000;
        String root = uuid+".";

        statConfig.set(root + "deaths", statConfig.getInt(root + "deaths", 0) + 1);
        statConfig.set(root + "elo", elo);
        statConfig.set(root + "currentstreak", 0);
        statConfig.set(root + "time", time);

        save(statConfig, dbTable);
    }

    /**
     * Increase player kill count, update ELO score and the max and current streak
     * @param uuid the player's UUID
     * @param elo the new ELO rating
     */
    @Override
    public void increaseKillsAndMaxStreak(UUID uuid, int elo) {
        long time = System.currentTimeMillis() / 1000;
        String root = uuid+".";

        statConfig.set(root + "kills", statConfig.getInt(root + "kills", 0) + 1);
        statConfig.set(root + "elo", elo);
        statConfig.set(root + "streak", statConfig.getInt(root + "streak", 0) + 1);
        statConfig.set(root + "currentstreak", statConfig.getInt(root + "currentstreak", 0) + 1);
        statConfig.set(root + "time", time);

        save(statConfig, dbTable);
    }

    /**
     * Increase player kill count, update ELO score and the current streak
     * @param uuid the player's UUID
     * @param elo the new ELO rating
     */
    @Override
    public void increaseKillsAndStreak(UUID uuid, int elo) {
        long time = System.currentTimeMillis() / 1000;
        String root = uuid+".";

        statConfig.set(root + "kills", statConfig.getInt(root + "kills", 0) + 1);
        statConfig.set(root + "elo", elo);
        statConfig.set(root + "currentstreak", statConfig.getInt(root + "currentstreak", 0) + 1);
        statConfig.set(root + "time", time);

        save(statConfig, dbTable);
    }

    private void save(FileConfiguration config, String filename) {
        File file = new File(PVPStats.getInstance().getDataFolder(), filename + ".yml");
        try {
            config.save(file);
        } catch (IOException e) {
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
        // can not happen in this implementation
    }
}
