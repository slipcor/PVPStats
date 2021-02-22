package net.slipcor.pvpstats.impl;

import net.slipcor.pvpstats.PVPStats;
import net.slipcor.pvpstats.api.DatabaseConnection;
import net.slipcor.pvpstats.classes.PlayerNameHandler;
import net.slipcor.pvpstats.classes.PlayerStatistic;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
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

    public boolean allowsAsync() {
        return false;
    }

    @Override
    public int cleanup(CommandSender sender) {
        return 0; // based on how YML is saved this can not be a problem
    }

    /**
     * Try to connect to the database
     *
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
     * Check whether the database has a column
     *
     * @param tableName the table to check
     * @param column the column to find
     * @return whether the database has this column
     */
    @Override
    public boolean hasColumn(String tableName, String column) {
        return true;
    }

    /**
     * Check whether a table exists
     *
     * @param database The database to check for the table in.
     * @param table    The table to check for existence.
     * @return true if the table exists, false if there was an error or the database doesn't exist.
     */
    public boolean tableExists(String database, String table) {
        File file = new File(PVPStats.getInstance().getDataFolder(), table + ".yml");
        return file.exists();
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
    }

    @Override
    public void addKillVictim() {
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

        String root = uuid.toString() + ".";

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

        StringBuilder root = new StringBuilder();
        root.append(uuid);
        root.append('.');
        root.append("kills.");
        root.append(time);

        if (uuid != null && !uuid.isEmpty()) {
            killStatConfig.set(root.toString() + ".killerName", playerName);
            killStatConfig.set(root.toString() + ".victimName", victimName);
            killStatConfig.set(root.toString() + ".victimUUID", victimUUID);
        }

        root = new StringBuilder();
        root.append(victimUUID);
        root.append('.');
        root.append("deaths.");
        root.append(time);

        if (victimUUID != null && !victimUUID.isEmpty()) {
            killStatConfig.set(root.toString() + ".killerName", playerName);
            killStatConfig.set(root.toString() + ".killerUUID", uuid);
            killStatConfig.set(root.toString() + ".victimName", victimName);
        }

        save(killStatConfig, dbKillTable);
    }

    /**
     * Create the kill stat table
     *
     * @param printError should we print errors that we encounter?
     */
    @Override
    public void createKillStatsTable(boolean printError) {
        // needs no setup
        this.collectPrecise = true;
    }

    /**
     * Create the statistics table
     *
     * @param printError should we print errors that we encounter?
     */
    @Override
    public void createStatsTable(boolean printError) {
        // needs no setup
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
     *
     * @param uuid the player's name
     */
    @Override
    public void deleteKillsByUUID(UUID uuid) {
        if (!collectPrecise) {
            return;
        }
        killStatConfig.set(uuid.toString(), null);

        save(killStatConfig, dbKillTable);
    }

    /**
     * Delete kill stats older than a timestamp
     *
     * @param timestamp the timestamp to compare to
     */
    @Override
    public int deleteKillsOlderThan(long timestamp) {
        if (!collectPrecise) {
            return 0;
        }

        Map<String, Object> uuids = killStatConfig.getValues(false);

        List<String> removeables = new ArrayList<>();

        for (String uuid : uuids.keySet()) {
            ConfigurationSection player = killStatConfig.getConfigurationSection(uuid);

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
                    removeables.add(uuid + ".kills." + key);
                }
            }

            for (String key : deaths.keySet()) {
                long time = Long.parseLong(key);
                if (time < timestamp) {
                    removeables.add(uuid + ".deaths." + key);
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
     * Delete statistics by player name
     *
     * @param uuid the player's name
     */
    @Override
    public void deleteStatsByUUID(UUID uuid) {
        statConfig.set(uuid.toString(), null);

        save(statConfig, dbTable);
    }

    /**
     * Delete statistics older than a timestamp
     *
     * @param timestamp the timestamp to compare to
     */
    @Override
    public int deleteStatsOlderThan(long timestamp) {
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
     * Get all statistics
     *
     * @return a list of all stats
     */
    @Override
    public List<PlayerStatistic> getAll() {
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
                    player.getInt("elo", 0),
                    player.getLong("time", 0),
                    UUID.fromString(uuid)));
        }

        return result;
    }

    /**
     * Get all player names
     *
     * @return all player names
     */
    @Override
    public List<String> getNamesWithoutUUIDs() {
        return new ArrayList<>();
    }

    /**
     * Get a statistic value by exact player name
     *
     * @param stat       the statistic value
     * @param uuid the exact player's name to look for
     * @return a set of all matching entries
     */
    @Override
    public int getStats(String stat, UUID uuid) {
        ConfigurationSection root = statConfig.getConfigurationSection(statConfig.getCurrentPath());
        ConfigurationSection player = root.getConfigurationSection(uuid.toString());
        if (player == null) {
            return 0;
        }
        return player.getInt(stat, 0);
    }

    /**
     * Get statistics by exact player name
     *
     * @param offlinePlayer the exact player's name to look for
     * @return the first matching player stat entry
     */
    @Override
    public PlayerStatistic getStats(OfflinePlayer offlinePlayer) {
        ConfigurationSection player = statConfig.getConfigurationSection(offlinePlayer.getUniqueId().toString());

        if (player == null) {
            return new PlayerStatistic(PlayerNameHandler.getPlayerName(offlinePlayer),
                    0, 0, 0, 0, 0, 0, offlinePlayer.getUniqueId());
        }

        return new PlayerStatistic(player.getString("name", ""),
                player.getInt("kills", 0),
                player.getInt("deaths", 0),
                player.getInt("streak", 0),
                player.getInt("currentstreak", 0),
                player.getInt("elo", 0),
                player.getLong("time", 0),
                offlinePlayer.getUniqueId());
    }

    @Override
    public List<UUID> getStatsUUIDs() {
        List<UUID> ids = new ArrayList<>();

        ConfigurationSection root = statConfig.getConfigurationSection(statConfig.getCurrentPath());

        Map<String, Object> uuids = root.getValues(false);

        for (String uuid : uuids.keySet()) {
            ids.add(UUID.fromString(uuid));
        }

        return ids;
    }

    /**
     * Get the top players sorted by a given column
     *
     * @param amount    the amount to return
     * @param orderBy   the column to sort by
     * @param ascending true if ascending order, false otherwise
     * @return a list of all stats from the top players
     */
    @Override
    public List<PlayerStatistic> getTopSorted(final int amount, final String orderBy, final boolean ascending) {
        class CustomComparator implements Comparator<PlayerStatistic> {

            @Override
            public int compare(PlayerStatistic stat1, PlayerStatistic stat2) {
                switch (orderBy) {
                    case "kills":
                        return (stat1.getKills() - stat2.getKills()) * (ascending ? 1 : -1);
                    case "deaths":
                        return (stat1.getDeaths() - stat2.getDeaths()) * (ascending ? 1 : -1);
                    case "streak":
                        return (stat1.getMaxStreak() - stat2.getMaxStreak()) * (ascending ? 1 : -1);
                    case "currentstreak":
                        return (stat1.getCurrentStreak() - stat2.getCurrentStreak()) * (ascending ? 1 : -1);
                    case "elo":
                        return (stat1.getELO() - stat2.getELO()) * (ascending ? 1 : -1);
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
                    player.getInt("elo", 0),
                    player.getLong("time", 0),
                    UUID.fromString(uuid)));
        }

        Collections.sort(result, new CustomComparator());

        return result.size() > amount ? result.subList(0, amount) : result;
    }

    /**
     * Check whether an entry matches a player UUID
     *
     * @param uuid the UUID to find
     * @return true if found, false otherwise
     */
    @Override
    public boolean hasEntry(UUID uuid) {
        return !statConfig.getString(uuid.toString() + ".name", "").equals("");
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
        String root = uuid.toString() + ".";

        statConfig.set(root + "deaths", statConfig.getInt(root + "deaths", 0) + 1);
        statConfig.set(root + "elo", elo);
        statConfig.set(root + "currentstreak", 0);
        statConfig.set(root + "time", time);
        statConfig.set(root + "name", name);

        save(statConfig, dbTable);
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
        String root = uuid.toString() + ".";

        statConfig.set(root + "kills", statConfig.getInt(root + "kills", 0) + 1);
        statConfig.set(root + "elo", elo);
        statConfig.set(root + "streak", statConfig.getInt(root + "streak", 0) + 1);
        statConfig.set(root + "currentstreak", statConfig.getInt(root + "currentstreak", 0) + 1);
        statConfig.set(root + "time", time);
        statConfig.set(root + "name", name);

        save(statConfig, dbTable);
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
        String root = uuid.toString() + ".";

        statConfig.set(root + "kills", statConfig.getInt(root + "kills", 0) + 1);
        statConfig.set(root + "elo", elo);
        statConfig.set(root + "currentstreak", statConfig.getInt(root + "currentstreak", 0) + 1);
        statConfig.set(root + "time", time);
        statConfig.set(root + "name", name);

        save(statConfig, dbTable);
    }

    @Override
    public void insert(PlayerStatistic stat) {
        String root = stat.getUid() + ".";

        int count = 0;

        try {
            count = statConfig.getConfigurationSection(statConfig.getCurrentPath()).getValues(false).keySet().size();
        } catch (Exception e) {
            e.printStackTrace();
        }

        count++;

        statConfig.set(root + "oid", count);
        statConfig.set(root + "name", stat.getName());
        statConfig.set(root + "kills", stat.getKills());
        statConfig.set(root + "deaths", stat.getDeaths());
        statConfig.set(root + "streak", stat.getMaxStreak());
        statConfig.set(root + "currentstreak", stat.getCurrentStreak());
        statConfig.set(root + "elo", stat.getELO());
        statConfig.set(root + "time", stat.getTime());

        save(statConfig, dbTable);
    }

    /**
     * @return whether the connection was established properly
     */
    @Override
    public boolean isConnected() {
        return statConfig != null;
    }

    /**
     * Save a configuration to a file
     *
     * @param config   the configuration to save
     * @param filename the file name (excluding extension .yml) to save to
     */
    private void save(FileConfiguration config, String filename) {
        File file = new File(PVPStats.getInstance().getDataFolder(), filename + ".yml");
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set specific statistical value of a player
     *
     * @param uuid the player to find
     * @param entry      the entry to set
     * @param value      the value to set
     */
    @Override
    public void setSpecificStat(UUID uuid, String entry, int value) {
        statConfig.set(uuid.toString() + "." + entry, value);
        save(statConfig, dbTable);
    }

    /**
     * Set the UUID of a certain player entry
     *
     * @param player the player to find and update
     */
    @Override
    public void setStatUIDByPlayer(OfflinePlayer player) {
        // can not happen in this implementation
    }
}
