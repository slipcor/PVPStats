package praxis.slipcor.pvpstats;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

/**
 * stats access class
 * 
 * @version v0.1.0
 * 
 * @author slipcor
 * 
 */

public final class PSMySQL {
	
	private PSMySQL() {
		
	}

	private static PVPStats plugin = null;

	public static void mysqlQuery(final String query) {
		if (plugin.mySQL) {
			try {
				plugin.sqlHandler.executeQuery(query, true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public static boolean mysqlExists(final String query) {
		ResultSet result = null;
		if (plugin.mySQL) {
			try {
				result = plugin.sqlHandler.executeQuery(query, false);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		try {
			while (result != null && result.next()) {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	private static Map<String, Integer> streaks = new HashMap<String, Integer>();
	private static Map<String, Integer> maxStreaks = new HashMap<String, Integer>();
	
	public static void incKill(final Player player) {
		if (player.hasPermission("pvpstats.count")) {
			boolean incStreak = false;
			if (streaks.containsKey(player.getName())) {
				final int streak = streaks.get(player.getName())+1;
				streaks.put(player.getName(), streak);
				if (maxStreaks.get(player.getName())<streak) {
					maxStreaks.put(player.getName(), Math.max(maxStreaks.get(player.getName()), streak));
					incStreak = true;
				}
			} else {
				streaks.put(player.getName(), 1);
				maxStreaks.put(player.getName(), 1);
			}
			checkAndDo(player.getName(), true, incStreak);
		}
	}

	public static void incDeath(final Player player) {
		if (player.hasPermission("pvpstats.count")) {
			streaks.put(player.getName(), 0);
			checkAndDo(player.getName(), false, false);
		}
	}

	private static void checkAndDo(final String sPlayer, final boolean kill, final boolean addStreak) {
		if (!mysqlExists("SELECT * FROM `"+plugin.dbTable+"` WHERE `name` = '" + sPlayer
				+ "';")) {
			mysqlQuery("INSERT INTO `"+plugin.dbTable+"` (`name`,`kills`,`deaths`) VALUES ('"
					+ sPlayer + "', 0, 0)");
		}
		final String var = kill ? "kills" : "deaths";
		mysqlQuery("UPDATE `"+plugin.dbTable+"` SET `" + var + "` = `" + var
				+ "`+1 WHERE `name` = '" + sPlayer + "'");
		if (addStreak && kill) {
			mysqlQuery("UPDATE `"+plugin.dbTable+"` SET `streak` = `streak`+1 WHERE `name` = '" + sPlayer + "'");
		}
	}

	public static String[] top(final int count, String sort) {
		if (!plugin.mySQL) {
			plugin.getLogger().severe("MySQL is not set!");
			return null;
		}
		ResultSet result = null;
		final Map<String, Integer> results = new HashMap<String, Integer>();
		
		final List<String> sortedValues = new ArrayList<String>();

		String order = null;
		try {
			
			if (sort.equals("KILLS")) {
				order = "kills";
			} else if (sort.equals("DEATHS")) {
				order = "deaths";
			} else if (sort.equals("STREAK")) {
				order = "streak";
			} else {
				order = "kills";
			}
			
			result = plugin.sqlHandler
					.executeQuery("SELECT `name`,`kills`,`deaths`,`streak` FROM `"+plugin.dbTable+"` WHERE 1 ORDER BY `"+order+"` DESC;", false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			while (result != null && result.next()) {
				if (sort.equals("KILLS")) {
					sortedValues.add(result.getString("name") + ": " + result.getInt(order));
				} else if (sort.equals("DEATHS")) {
					sortedValues.add(result.getString("name") + ": " + result.getInt(order));
				} else if (sort.equals("STREAK")) {
					sortedValues.add(result.getString("name") + ": " + result.getInt(order));
				} else {
					results.put(
							result.getString("name"),
							calcResult(result.getInt("kills"),
									result.getInt("deaths")));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (sort.equals("KILLS")||sort.equals("DEATHS")||sort.equals("STREAK")) {
			String[] output = new String[sortedValues.size()];
			
			int pos = 0;
			
			for (String s : sortedValues) {
				output[pos++] = s;
			}
			return output;
		}

		final String[] output = sortParse(results, count);
		return output;
	}

	private static String[] sortParse(final Map<String, Integer> results,
			final int count) {
		String[] result = new String[results.size()];
		Integer[] sort = new Integer[results.size()];

		int pos = 0;

		for (String key : results.keySet()) {
			sort[pos] = results.get(key);
			result[pos] = key + ": " + sort[pos];
			pos++;
		}

		int pos2 = results.size();
		boolean doMore = true;
		while (doMore) {
			pos2--;
			doMore = false; // assume this is our last pass over the array
			for (int i = 0; i < pos2; i++) {
				if (sort[i] < sort[i + 1]) {
					// exchange elements

					final int tempI = sort[i];
					sort[i] = sort[i + 1];
					sort[i + 1] = tempI;

					final String tempR = result[i];
					result[i] = result[i + 1];
					result[i + 1] = tempR;

					doMore = true; // after an exchange, must look again
				}
			}
		}
		if (result.length < count) {
			return result;
		}
		String[] output = new String[count];
		for (int i = 0; i < output.length; i++) {
			output[i] = result[i];
		}

		return output;
	}

	private static Integer calcResult(final int a, final int b) {
		return a - b;
	}

	public static String[] info(final String string) {
		if (!plugin.mySQL) {
			plugin.getLogger().severe("MySQL is not set!");
			return null;
		}
		ResultSet result = null;
		try {
			result = plugin.sqlHandler
					.executeQuery("SELECT `name`,`kills`,`deaths`,`streak` FROM `"+plugin.dbTable+"` WHERE `name` LIKE '%"+string+"%' LIMIT 1;", false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		String[] output = null;
		try {
			while (result != null && result.next()) {
				output = new String[8];
				output[0] = "---------------";
				output[1] = "PVP Stats for §a"+string;
				output[2] = "---------------";
				output[3] = "Name: "+result.getString("name");
				output[4] = "Kills: "+result.getInt("kills");
				output[5] = "Deaths: "+result.getInt("deaths");
				output[6] = "Ratio: "+calcResult(result.getInt("kills"),
						result.getInt("deaths"));
				output[7] = "Max Streak: "+result.getInt("streak");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (output != null) {
			return output;
		}
		
		output = new String[1];
		output[0] = "Player not found: "+ string;
		return output;
	}

	public static void initiate(final PVPStats pvpStats) {
		plugin = pvpStats;
	}
}
