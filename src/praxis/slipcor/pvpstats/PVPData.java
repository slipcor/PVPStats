package praxis.slipcor.pvpstats;

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
	
	private PVPData() {
	}

	/**
	 * increase a player killstreak - eventually increases the maximum killstreak
	 * @param name the player name to handle
	 * @return true if the maximum streak should be increased database wise
	 */
	public static boolean addStreak(String name) {
		final int streak = streaks.get(name)+1;
		streaks.put(name, streak);
		if (hasMaxStreak(name)) {
			if (PVPData.maxStreaks.get(name)<streak) {
				PVPData.maxStreaks.put(name, Math.max(PVPData.maxStreaks.get(name), streak));
				return true;
			}
		} else {
			maxStreaks.put(name, streak);
			return true;
		}
		return false;
	}

	/**
	 * get a player's death count
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
	 * @param name the player to read
	 * @return the player's current kill streak
	 */
	public static Integer getStreak(String name) {
		if (hasStreak(name)) {
			return streaks.get(name);
		}
		return 0;
	}

	/**
	 * does a player already have a maximum kill streak
	 * @param name the player to check
	 * @return true if the player has a maximum kill streak
	 */
	public static boolean hasMaxStreak(String name) {
		return maxStreaks.containsKey(name);
	}

	/**
	 * does a player already have a kill streak
	 * @param name the player to check
	 * @return true if the player has a kill streak
	 */
	public static boolean hasStreak(String name) {
		return streaks.containsKey(name);
	}

	/**
	 * force set a player's death count - this does NOT update the database!
	 * @param name the player to update
	 * @param value the value to set
	 */
	public static void setDeaths(String name, int value) {
		deaths.put(name, value);
	}

	/**
	 * force set a player's kill count - this does NOT update the database!
	 * @param name the player to update
	 * @param value the value to set
	 */
	public static void setKills(String sPlayer, int value) {
		kills.put(sPlayer, value);
	}

	/**
	 * force set a player's max killstreak count - this does NOT update the database!
	 * @param name the player to update
	 * @param value the value to set
	 */
	public static void setMaxStreak(String name, int value) {
		maxStreaks.put(name, value);
	}

	/**
	 * force set a player's killstreak count - this does NOT update the database!
	 * @param name the player to update
	 * @param value the value to set
	 */
	public static void setStreak(String name, int value) {
		streaks.put(name, value);
	}
}
