package praxis.slipcor.pvpstats;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * MySQL access class
 * 
 * @author slipcor
 * 
 */

public final class PSMySQL {
	
	private PSMySQL() {
		
	}

	private static PVPStats plugin = null;

	private static void mysqlQuery(final String query) {
		if (plugin.mySQL) {
			try {
				plugin.sqlHandler.executeQuery(query, true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private static boolean mysqlExists(final String query) {
		ResultSet result = null;
		if (plugin.mySQL) {
			try {
				result = plugin.sqlHandler.executeQuery(query, false);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				while (result != null && result.next()) {
					return true;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * @param player
	 */
	public static void incKill(final Player player) {
		if (player.hasPermission("pvpstats.count")) {
			boolean incStreak = false;
			if (PVPData.hasStreak(player.getName())) {
				incStreak = PVPData.addStreak(player.getName());
			} else {
				PVPData.setStreak(player.getName(), 1);
				PVPData.setMaxStreak(player.getName(), 1);
				incStreak = true;
			}
			checkAndDo(player.getName(), true, incStreak);
		}
	}

	public static void incDeath(final Player player) {
		if (player.hasPermission("pvpstats.count")) {
			PVPData.setStreak(player.getName(), 0);
			checkAndDo(player.getName(), false, false);
		}
	}

	private static void checkAndDo(final String sPlayer, final boolean kill, final boolean addStreak) {
		if (!mysqlExists("SELECT * FROM `"+plugin.dbTable+"` WHERE `name` = '" + sPlayer
				+ "';")) {
			final int kills = kill?1:0;
			final int deaths = kill?0:1;
			mysqlQuery("INSERT INTO `"+plugin.dbTable+"` (`name`,`kills`,`deaths`) VALUES ('"
					+ sPlayer + "', "+kills+", "+deaths+")");
			PVPData.setKills(sPlayer, kills);
			PVPData.setDeaths(sPlayer, deaths);
			return;
		} else {
			final String var = kill ? "kills" : "deaths";
			mysqlQuery("UPDATE `"+plugin.dbTable+"` SET `" + var + "` = `" + var
					+ "`+1 WHERE `name` = '" + sPlayer + "'");
		}
		if (addStreak && kill) {
			mysqlQuery("UPDATE `"+plugin.dbTable+"` SET `streak` = `streak`+1 WHERE `name` = '" + sPlayer + "'");
		}
		mysqlQuery("INSERT INTO "+plugin.dbKillTable+" (`name`,`kill`,`time`) VALUES(" +
				"'"+sPlayer+"', '"+(kill?1:0)+"', '"+(int) System.currentTimeMillis()/1000+"')");
	}

	/**
	 * @param count the amount to fetch
	 * @param sort sorting string
	 * @return a sorted array
	 */
	public static String[] top(final int count, String sort) {
		if (!plugin.mySQL) {
			plugin.getLogger().severe("MySQL is not set!");
			return null;
		}
		
		sort = sort.toUpperCase();
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
					.executeQuery("SELECT `name`,`kills`,`deaths`,`streak` FROM `"+plugin.dbTable+"` WHERE 1 ORDER BY `"+order+"` DESC LIMIT "+count+";", false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			while (result != null && result.next()) {
				if (sort.equals("KILLS")) {
					sortedValues.add(ChatColor.RED + result.getString("name") + ":" + ChatColor.GRAY + " " + result.getInt(order));
				} else if (sort.equals("DEATHS")) {
					sortedValues.add(ChatColor.RED + result.getString("name") + ":" + ChatColor.GRAY + " " + result.getInt(order));
				} else if (sort.equals("STREAK")) {
					sortedValues.add(ChatColor.RED + result.getString("name") + ":" + ChatColor.GRAY + " " + result.getInt(order));
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
			result[pos] = ChatColor.RED + key + ":" + ChatColor.GRAY + " " + sort[pos];
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

	/**
	 * @param string the player name to get
	 * @return the player info
	 */
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
				String name = result.getString("name");
				Integer streak = PVPData.getStreak(name);
				if (streak == null) {
					streak = 0;
				}
				output = new String[6];
				
				output[0] = Language.INFO_FORMAT.toString(
						Language.INFO_NAME.toString(),
						name);
				output[1] = Language.INFO_FORMAT.toString(
						Language.INFO_KILLS.toString(),
						String.valueOf(result.getInt("kills")));
				output[2] = Language.INFO_FORMAT.toString(
						Language.INFO_DEATHS.toString(),
						String.valueOf(result.getInt("deaths")));
				output[3] = Language.INFO_FORMAT.toString(
						Language.INFO_RATIO.toString(),
						String.valueOf(calcResult(result.getInt("kills"), result.getInt("deaths"))));
				output[4] = Language.INFO_FORMAT.toString(
						Language.INFO_STREAK.toString(),
						String.valueOf(streak));
				output[5] = Language.INFO_FORMAT.toString(
						Language.INFO_MAXSTREAK.toString(),
						String.valueOf(result.getInt("streak")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (output != null) {
			return output;
		}
		
		output = new String[1];
		output[0] = Language.INFO_PLAYERNOTFOUND.toString(string);
		return output;
	}
	
	public static Integer getEntry(String player, String entry) {
		if (entry == null) {
			throw new IllegalArgumentException("entry can not be null!");
		}
		
		if (!entry.equals("kills") &&
				!entry.equals("deaths") &&
				!entry.equals("streak")) {
			throw new IllegalArgumentException("entry can not be '"+entry+"'. Valid values: kills, deaths, streak");
		}
		
		if (!plugin.mySQL) {
			plugin.getLogger().severe("MySQL is not set!");
			return null;
		}
		ResultSet result = null;
		try {
			result = plugin.sqlHandler
					.executeQuery("SELECT `"+entry+"` FROM `"+plugin.dbTable+"` WHERE `name` LIKE '%"+player+"%' LIMIT 1;", false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		try {
			while (result != null && result.next()) {
				return result.getInt(entry);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return 0;
	}

	public static void initiate(final PVPStats pvpStats) {
		plugin = pvpStats;
	}

	public static void wipe(final String name) {
		if (name == null) {
			mysqlQuery("DELETE FROM `"+plugin.dbTable+"` WHERE 1;");
			mysqlQuery("DELETE FROM `"+plugin.dbKillTable+"` WHERE 1;");
		} else {
			PVPData.setDeaths(name, 0);
			PVPData.setKills(name, 0);
			PVPData.setMaxStreak(name, 0);
			PVPData.setStreak(name, 0);

			mysqlQuery("DELETE FROM `"+plugin.dbTable+"` WHERE `name` = '" + name
					+ "';");
			mysqlQuery("DELETE FROM `"+plugin.dbKillTable+"` WHERE `name` = '" + name
					+ "';");
		}
	}
}
