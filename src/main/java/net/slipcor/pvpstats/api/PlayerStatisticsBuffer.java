package net.slipcor.pvpstats.api;

import net.slipcor.pvpstats.PVPStats;
import net.slipcor.pvpstats.core.Config;

import java.util.HashMap;
import java.util.Map;

/**
 * Class for fast temporary access to player statistics
 * <p>
 * Should never used to SET variables, only for quick access to existing values
 *
 * @author slipcor
 */
public final class PlayerStatisticsBuffer {

    private static Map<String, Integer> kills = new HashMap<>();
    private static Map<String, Integer> deaths = new HashMap<>();
    private static Map<String, Integer> streaks = new HashMap<>();
    private static Map<String, Integer> maxStreaks = new HashMap<>();
    private static Map<String, Integer> eloScore = new HashMap<>();

    private PlayerStatisticsBuffer() {
    }

    /**
     * Increase a player's death count
     *
     * @param playerName the player's name
     */
    public static void addDeath(String playerName) {
        int value = deaths.containsKey(playerName) ? deaths.get(playerName) : 0;

        deaths.put(playerName, ++value);
    }

    /**
     * Increase a player's kill count
     *
     * @param playerName the player's name
     */
    public static void addKill(String playerName) {
        int value = kills.containsKey(playerName) ? kills.get(playerName) : 0;

        kills.put(playerName, ++value);
    }

    /**
     * Increase a player killstreak - eventually increases the maximum killstreak
     *
     * @param name the player name to handle
     * @return true if the maximum streak should be increased wise
     */
    public static boolean addStreak(String name) {
        final int streak = streaks.get(name) + 1;
        streaks.put(name, streak);
        if (hasMaxStreak(name)) {
            if (PlayerStatisticsBuffer.maxStreaks.get(name) < streak) {
                PlayerStatisticsBuffer.maxStreaks.put(name, Math.max(PlayerStatisticsBuffer.maxStreaks.get(name), streak));
                return true;
            }
        } else {
            int max = getMaxStreak(name); // load the streaks
            if (max > streak) {
                return false;
            }
            maxStreaks.put(name, streak);
            return true;
        }
        return false;
    }

    /**
     * Clear a player's temporary variables
     *
     * @param name the player to clear, null to clear everything
     */
    public static void clear(String name) {
        if (name == null) {
            deaths.clear();
            kills.clear();
            maxStreaks.clear();
            streaks.clear();
            eloScore.clear();
        } else {
            clearDeaths(name);
            clearKills(name);
            clearMaxStreak(name);
            clearStreak(name);
            clearEloScore(name);
        }
    }

    /**
     * Clear a player's death count
     *
     * @param name the player to clear
     */
    public static void clearDeaths(String name) {
        deaths.remove(name);
    }

    /**
     * Clear a player's kill count
     *
     * @param name the player to clear
     */
    public static void clearKills(String name) {
        kills.remove(name);
    }

    /**
     * Clear a player's maximum kill streak
     *
     * @param name the player to clear
     */
    public static void clearMaxStreak(String name) {
        maxStreaks.remove(name);
    }

    /**
     * Clear a player's current kill streak
     *
     * @param name the player to clear
     */
    public static void clearStreak(String name) {
        streaks.remove(name);
    }

    /**
     * Clear a player's current elo score
     *
     * @param name the player to read
     */
    public static void clearEloScore(String name) {
        eloScore.remove(name);
    }

    /**
     * Get a player's death count
     *
     * @param name the player to read
     * @return the player's death count
     */
    public static Integer getDeaths(String name) {
        if (deaths.containsKey(name)) {
            return deaths.get(name);
        }

        final int value = DatabaseAPI.getEntry(name, "deaths");
        deaths.put(name, value);
        return value;
    }

    /**
     * Get a player's kill count
     *
     * @param name the player to read
     * @return the player's kill count
     */
    public static Integer getKills(String name) {
        if (kills.containsKey(name)) {
            return kills.get(name);
        }

        final int value = DatabaseAPI.getEntry(name, "kills");
        kills.put(name, value);
        return value;
    }

    /**
     * Get a player's maximum kill streak
     *
     * @param name the player to read
     * @return the player's maximum kill streak
     */
    public static Integer getMaxStreak(String name) {
        if (hasMaxStreak(name)) {
            return maxStreaks.get(name);
        }

        final int value = DatabaseAPI.getEntry(name, "streak");
        maxStreaks.put(name, value);
        return value;
    }

    /**
     * Get a player's current kill streak
     *
     * @param name the player to read
     * @return the player's current kill streak
     */
    public static Integer getStreak(String name) {
        if (hasStreak(name)) {
            return streaks.get(name);
        }

        final int value = DatabaseAPI.getEntry(name, "currentstreak");
        streaks.put(name, value);
        return value;
    }

    /**
     * Get a player's current elo score
     *
     * @param name the player to read
     * @return the player's current elo score
     */
    public static Integer getEloScore(String name) {
        if (hasEloScore(name)) {
            return eloScore.get(name);
        }

        final int value = DatabaseAPI.getEntry(name, "elo");

        if (value > 0) {
            eloScore.put(name, value);
            return value;
        }

        Integer idefault = PVPStats.getInstance().config().getInt(Config.Entry.ELO_DEFAULT);
        eloScore.put(name, idefault);
        return idefault;
    }

    /**
     * Get a player's current configurable kill/death ratio
     *
     * @param name the player to read
     * @return the player's current k/d ratio
     */
    public static Double getRatio(String name) {
        return DatabaseAPI.calculateRatio(getKills(name), getDeaths(name), getStreak(name), getMaxStreak(name));
    }

    /**
     * Does a player already have a maximum kill streak
     *
     * @param name the player to check
     * @return true if the player has a maximum kill streak
     */
    public static boolean hasMaxStreak(String name) {
        return maxStreaks.containsKey(name);
    }

    /**
     * Does a player already have a kill streak
     *
     * @param name the player to check
     * @return true if the player has a kill streak
     */
    public static boolean hasStreak(String name) {
        return streaks.containsKey(name);
    }

    /**
     * Does a player already have a elo score
     *
     * @param name the player to check
     * @return true if the player has a elo score
     */
    public static boolean hasEloScore(String name) {
        return eloScore.containsKey(name);
    }

    /**
     * Force set a player's death count - this does NOT update the database!
     *
     * @param name  the player to update
     * @param value the value to set
     */
    public static void setDeaths(String name, int value) {
        deaths.put(name, value);
    }

    /**
     * Force set a player's kill count - this does NOT update the database!
     *
     * @param name  the player to update
     * @param value the value to set
     */
    public static void setKills(String name, int value) {
        kills.put(name, value);
    }

    /**
     * Force set a player's max killstreak count - this does NOT update the database!
     *
     * @param name  the player to update
     * @param value the value to set
     */
    public static void setMaxStreak(String name, int value) {
        maxStreaks.put(name, value);
    }

    /**
     * Force set a player's killstreak count - this does NOT update the database!
     *
     * @param name  the player to update
     * @param value the value to set
     */
    public static void setStreak(String name, int value) {
        streaks.put(name, value);
    }

    /**
     * Force set a player's elo score - this does NOT update the database!
     *
     * @param name  the player to update
     * @param value the value to set
     */
    public static void setEloScore(String name, int value) {
        eloScore.put(name, value);
    }
}
