package net.slipcor.pvpstats;

import java.util.HashMap;
import java.util.Map;

/**
 * class for full access to player statistics
 */
public final class PVPData {

    private static Map<String, Integer> kills = new HashMap<String, Integer>();
    private static Map<String, Integer> deaths = new HashMap<String, Integer>();
    private static Map<String, Integer> streaks = new HashMap<String, Integer>();
    private static Map<String, Integer> maxStreaks = new HashMap<String, Integer>();
    private static Map<String, Integer> eloScore = new HashMap<String, Integer>();

    private PVPData() {
    }

    /**
     * increase a player killstreak - eventually increases the maximum killstreak
     *
     * @param name the player name to handle
     * @return true if the maximum streak should be increased database wise
     */
    public static boolean addStreak(String name) {
        final int streak = streaks.get(name) + 1;
        streaks.put(name, streak);
        if (hasMaxStreak(name)) {
            if (PVPData.maxStreaks.get(name) < streak) {
                PVPData.maxStreaks.put(name, Math.max(PVPData.maxStreaks.get(name), streak));
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
     * clear a player's temporary variables
     *
     * @param name the player to clear
     */
    public static void clear(String name) {
        clearDeaths(name);
        clearKills(name);
        clearMaxStreak(name);
        clearStreak(name);
        clearEloScore(name);
    }

    /**
     * clear a player's death count
     *
     * @param name the player to clear
     */
    public static void clearDeaths(String name) {
        deaths.remove(name);
    }

    /**
     * clear a player's kill count
     *
     * @param name the player to clear
     */
    public static void clearKills(String name) {
        kills.remove(name);
    }

    /**
     * clear a player's maximum kill streak
     *
     * @param name the player to clear
     */
    public static void clearMaxStreak(String name) {
        maxStreaks.remove(name);
    }

    /**
     * clear a player's current kill streak
     *
     * @param name the player to clear
     */
    public static void clearStreak(String name) {
        streaks.remove(name);
    }

    /**
     * clear a player's current elo score
     *
     * @param name the player to read
     */
    public static void clearEloScore(String name) {
        eloScore.remove(name);
    }

    /**
     * get a player's death count
     *
     * @param name the player to read
     * @return the player's death count
     */
    public static Integer getDeaths(String name) {
        if (deaths.containsKey(name)) {
            return deaths.get(name);
        }

        final int value = PSMySQL.getEntry(name, "deaths");
        deaths.put(name, value);
        return value;
    }

    /**
     * get a player's kill count
     *
     * @param name the player to read
     * @return the player's kill count
     */
    public static Integer getKills(String name) {
        if (kills.containsKey(name)) {
            return kills.get(name);
        }

        final int value = PSMySQL.getEntry(name, "kills");
        kills.put(name, value);
        return value;
    }

    /**
     * get a player's maximum kill streak
     *
     * @param name the player to read
     * @return the player's maximum kill streak
     */
    public static Integer getMaxStreak(String name) {
        if (hasMaxStreak(name)) {
            return maxStreaks.get(name);
        }

        final int value = PSMySQL.getEntry(name, "streak");
        maxStreaks.put(name, value);
        return value;
    }

    /**
     * get a player's current kill streak
     *
     * @param name the player to read
     * @return the player's current kill streak
     */
    public static Integer getStreak(String name) {
        if (hasStreak(name)) {
            return streaks.get(name);
        }

        final int value = PSMySQL.getEntry(name, "currentstreak");
        streaks.put(name, value);
        return value;
    }

    /**
     * get a player's current elo score
     *
     * @param name the player to read
     * @return the player's current elo score
     */
    public static Integer getEloScore(String name) {
        if (hasEloScore(name)) {
            return eloScore.get(name);
        }

        final int value = PSMySQL.getEntry(name, "elo");

        if (value > 0) {
            eloScore.put(name, value);
            return value;
        }

        Integer idefault = PVPStats.getInstance().getConfig().getInt("eloscore.default");
        eloScore.put(name, idefault);
        return idefault;
    }

    /**
     * does a player already have a maximum kill streak
     *
     * @param name the player to check
     * @return true if the player has a maximum kill streak
     */
    public static boolean hasMaxStreak(String name) {
        return maxStreaks.containsKey(name);
    }

    /**
     * does a player already have a kill streak
     *
     * @param name the player to check
     * @return true if the player has a kill streak
     */
    public static boolean hasStreak(String name) {
        return streaks.containsKey(name);
    }

    /**
     * does a player already have a elo score
     *
     * @param name the player to check
     * @return true if the player has a elo score
     */
    public static boolean hasEloScore(String name) {
        return eloScore.containsKey(name);
    }

    /**
     * force set a player's death count - this does NOT update the database!
     *
     * @param name  the player to update
     * @param value the value to set
     */
    public static void setDeaths(String name, int value) {
        deaths.put(name, value);
    }

    /**
     * force set a player's kill count - this does NOT update the database!
     *
     * @param name  the player to update
     * @param value the value to set
     */
    public static void setKills(String name, int value) {
        kills.put(name, value);
    }

    /**
     * force set a player's max killstreak count - this does NOT update the database!
     *
     * @param name  the player to update
     * @param value the value to set
     */
    public static void setMaxStreak(String name, int value) {
        maxStreaks.put(name, value);
    }

    /**
     * force set a player's killstreak count - this does NOT update the database!
     *
     * @param name  the player to update
     * @param value the value to set
     */
    public static void setStreak(String name, int value) {
        streaks.put(name, value);
    }

    /**
     * force set a player's elo score - this does NOT update the database!
     *
     * @param name  the player to update
     * @param value the value to set
     */
    public static void setEloScore(String name, int value) {
        eloScore.put(name, value);
    }
}
