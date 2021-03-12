package net.slipcor.pvpstats.api;

import net.slipcor.pvpstats.PVPStats;
import net.slipcor.pvpstats.classes.PlayerStatistic;
import net.slipcor.pvpstats.core.Config;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.*;

/**
 * Class for fast temporary access to player statistics
 * <p>
 * Should never be publicly used to SET variables, only for quick access to existing values
 *
 * @author slipcor
 */
public final class PlayerStatisticsBuffer {

    private static final Map<UUID, Integer> kills = new HashMap<>();
    private static final Map<UUID, Integer> deaths = new HashMap<>();
    private static final Map<UUID, Integer> streaks = new HashMap<>();
    private static final Map<UUID, Integer> maxStreaks = new HashMap<>();
    private static final Map<UUID, Integer> eloScore = new HashMap<>();

    private PlayerStatisticsBuffer() {
    }

    /**
     * Increase a player's death count
     *
     * @param uuid the player's UUID
     */
    public static void addDeath(UUID uuid) {
        int value = deaths.containsKey(uuid) ? deaths.get(uuid) : 0;

        deaths.put(uuid, ++value);
    }

    /**
     * Increase a player's kill count
     *
     * @param uuid the player's UUID
     */
    public static void addKill(UUID uuid) {
        int value = kills.containsKey(uuid) ? kills.get(uuid) : 0;

        kills.put(uuid, ++value);
    }

    /**
     * Increase a player killstreak - eventually increases the maximum killstreak
     *
     * @param uuid the player UUID to handle
     * @return true if the maximum streak should be increased wise
     */
    public static boolean addStreak(UUID uuid) {
        final int streak = streaks.get(uuid) + 1;

        Bukkit.getScheduler().runTaskLaterAsynchronously(
                PVPStats.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        // issue streak commands AFTER the death message has gone through
                        PVPStats.getInstance().handleStreak(uuid, streak);
                    }
                }, 1L
        );

        streaks.put(uuid, streak);
        if (hasMaxStreak(uuid)) {
            if (PlayerStatisticsBuffer.maxStreaks.get(uuid) < streak) {
                PlayerStatisticsBuffer.maxStreaks.put(uuid, Math.max(PlayerStatisticsBuffer.maxStreaks.get(uuid), streak));
                return true;
            }
        } else {
            int max = getMaxStreak(uuid); // load the streaks
            if (max > streak) {
                return false;
            }
            maxStreaks.put(uuid, streak);
            return true;
        }
        return false;
    }

    /**
     * Clear a player's temporary variables
     *
     * @param uuid the player UUID to clear, null to clear everything
     */
    public static void clear(UUID uuid) {
        if (uuid == null) {
            deaths.clear();
            kills.clear();
            maxStreaks.clear();
            streaks.clear();
            eloScore.clear();
        } else {
            clearDeaths(uuid);
            clearKills(uuid);
            clearMaxStreak(uuid);
            clearStreak(uuid);
            clearEloScore(uuid);
        }
    }

    /**
     * Clear a player's death count
     *
     * @param uuid the player UUID to clear
     */
    public static void clearDeaths(UUID uuid) {
        deaths.remove(uuid);
    }

    /**
     * Clear a player's kill count
     *
     * @param uuid the player UUID to clear
     */
    public static void clearKills(UUID uuid) {
        kills.remove(uuid);
    }

    /**
     * Clear a player's maximum kill streak
     *
     * @param uuid the player UUID to clear
     */
    public static void clearMaxStreak(UUID uuid) {
        maxStreaks.remove(uuid);
    }

    /**
     * Clear a player's current kill streak
     *
     * @param uuid the player UUID to clear
     */
    public static void clearStreak(UUID uuid) {
        streaks.remove(uuid);
    }

    /**
     * Clear a player's current elo score
     *
     * @param uuid the player UUID to read
     */
    public static void clearEloScore(UUID uuid) {
        eloScore.remove(uuid);
    }

    /**
     * Get a player's death count
     *
     * @param uuid the player UUID to read
     * @return the player's death count
     */
    public static Integer getDeaths(UUID uuid) {
        if (deaths.containsKey(uuid)) {
            return deaths.get(uuid);
        }

        final int value = DatabaseAPI.getEntry(uuid, "deaths");
        deaths.put(uuid, value);
        return value;
    }

    /**
     * Get a player's kill count
     *
     * @param uuid the player to read
     * @return the player's kill count
     */
    public static Integer getKills(UUID uuid) {
        if (kills.containsKey(uuid)) {
            return kills.get(uuid);
        }

        final int value = DatabaseAPI.getEntry(uuid, "kills");
        kills.put(uuid, value);
        return value;
    }

    /**
     * Get a player's maximum kill streak
     *
     * @param uuid the player to read
     * @return the player's maximum kill streak
     */
    public static Integer getMaxStreak(UUID uuid) {
        if (hasMaxStreak(uuid)) {
            return maxStreaks.get(uuid);
        }

        final int value = DatabaseAPI.getEntry(uuid, "streak");
        maxStreaks.put(uuid, value);
        return value;
    }

    /**
     * Get a player's current kill streak
     *
     * @param uuid the player to read
     * @return the player's current kill streak
     */
    public static Integer getStreak(UUID uuid) {
        if (hasStreak(uuid)) {
            return streaks.get(uuid);
        }

        final int value = DatabaseAPI.getEntry(uuid, "currentstreak");
        streaks.put(uuid, value);
        return value;
    }

    /**
     * Get a player's current elo score
     *
     * @param uuid the player to read
     * @return the player's current elo score
     */
    public static Integer getEloScore(UUID uuid) {
        if (hasEloScore(uuid)) {
            return eloScore.get(uuid);
        }

        final int value = DatabaseAPI.getEntry(uuid, "elo");

        if (value > 0) {
            eloScore.put(uuid, value);
            return value;
        }

        Integer idefault = PVPStats.getInstance().config().getInt(Config.Entry.ELO_DEFAULT);
        eloScore.put(uuid, idefault);
        return idefault;
    }

    /**
     * Get a player's current configurable kill/death ratio
     *
     * @param uuid the player UUID to read
     * @return the player's current k/d ratio
     */
    public static Double getRatio(UUID uuid) {
        return DatabaseAPI.calculateRatio(getKills(uuid), getDeaths(uuid), getStreak(uuid), getMaxStreak(uuid));
    }

    /**
     * Does a player already have a maximum kill streak
     *
     * @param uuid the player UUID to check
     * @return true if the player has a maximum kill streak
     */
    public static boolean hasMaxStreak(UUID uuid) {
        return maxStreaks.containsKey(uuid);
    }

    /**
     * Does a player already have a kill streak
     *
     * @param uuid the player UUID to check
     * @return true if the player has a kill streak
     */
    public static boolean hasStreak(UUID uuid) {
        return streaks.containsKey(uuid);
    }

    /**
     * Does a player already have a elo score
     *
     * @param uuid the player UUID to check
     * @return true if the player has a elo score
     */
    public static boolean hasEloScore(UUID uuid) {
        return eloScore.containsKey(uuid);
    }

    /**
     * Make sure all statistics are loaded, query the database if necessary
     * @param player the player whose stats to load
     */
    public static void loadPlayer(OfflinePlayer player) {
        UUID uuid = player.getUniqueId();
        if (deaths.containsKey(uuid) && kills.containsKey(uuid)) {
            return; // we already did it
        }

        PlayerStatistic statistic = DatabaseAPI.getAllStats(player);

        deaths.put(uuid, statistic.getDeaths());
        kills.put(uuid, statistic.getKills());
        eloScore.put(uuid, statistic.getELO());
        streaks.put(uuid, statistic.getCurrentStreak());
        maxStreaks.put(uuid, statistic.getMaxStreak());
    }

    /**
     * Force set a player's death count - this does NOT update the database!
     *
     * @param uuid  the player UUID to update
     * @param value the value to set
     */
    public static void setDeaths(UUID uuid, int value) {
        deaths.put(uuid, value);
    }

    /**
     * Force set a player's kill count - this does NOT update the database!
     *
     * @param uuid  the player UUID to update
     * @param value the value to set
     */
    public static void setKills(UUID uuid, int value) {
        kills.put(uuid, value);
    }

    /**
     * Force set a player's max killstreak count - this does NOT update the database!
     *
     * @param uuid  the player UUID to update
     * @param value the value to set
     */
    public static void setMaxStreak(UUID uuid, int value) {
        maxStreaks.put(uuid, value);
    }

    /**
     * Force set a player's killstreak count - this does NOT update the database!
     *
     * @param uuid  the player UUID to update
     * @param value the value to set
     */
    public static void setStreak(UUID uuid, int value) {
        streaks.put(uuid, value);
    }

    /**
     * Force set a player's elo score - this does NOT update the database!
     *
     * @param uuid  the player UUID to update
     * @param value the value to set
     */
    public static void setEloScore(UUID uuid, int value) {
        eloScore.put(uuid, value);
    }

    /**
     * Refresh the maps after making changes by command
     */
    static void refresh() {
        List<UUID> uuids = new ArrayList<>(DatabaseAPI.getAllUUIDs());

        clear(null); // clear all entries

        for (UUID uuid : uuids) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            DatabaseAPI.info(player); // pre-load previously loaded players
        }
    }
}
