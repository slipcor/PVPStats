package praxis.slipcor.pvpstats;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.bukkit.entity.Player;

/**
 * stats access class
 * 
 * @version v0.1.0
 * 
 * @author slipcor
 * 
 */

public class PSMySQL {

	public static PVPStats plugin = null;

	public static void mysqlQuery(String query) {
		if (plugin.MySQL) {
			try {
				plugin.sqlHandler.executeQuery(query, true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public static boolean mysqlExists(String query) {
		ResultSet result = null;
		if (plugin.MySQL) {
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

	public static void incKill(Player player) {
		if (player.hasPermission("pvpstats.count"))
			checkAndDo(player.getName(), true);
	}

	public static void incDeath(Player player) {
		if (player.hasPermission("pvpstats.count"))
			checkAndDo(player.getName(), false);
	}

	private static void checkAndDo(String sPlayer, boolean kill) {
		if (!mysqlExists("SELECT * FROM `pvpstats` WHERE `name` = '" + sPlayer
				+ "';")) {
			mysqlQuery("INSERT INTO `pvpstats` (`name`,`kills`,`deaths`) VALUES ('"
					+ sPlayer + "', 0, 0)");
		}
		String var = kill ? "kills" : "deaths";
		mysqlQuery("UPDATE `pvpstats` SET `" + var + "` = `" + var
				+ "`+1 WHERE `name` = '" + sPlayer + "'");
	}

	public static String[] top(int count) {
		if (!plugin.MySQL) {
			plugin.getLogger().severe("MySQL is not set!");
			return null;
		}
		ResultSet result = null;
		HashMap<String, Integer> results = new HashMap<String, Integer>();
		try {
			result = plugin.sqlHandler
					.executeQuery("SELECT `name`,`kills`,`deaths` FROM `pvpstats` WHERE 1 ORDER BY `kills` DESC;", false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			while (result != null && result.next()) {
				results.put(
						result.getString("name"),
						calcResult(result.getInt("kills"),
								result.getInt("deaths")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		String[] output = sortParse(results, count);

		return output;
	}

	private static String[] sortParse(HashMap<String, Integer> results,
			int count) {
		String[] result = new String[results.size()];
		Integer[] sort = new Integer[results.size()];

		int a = 0;

		for (String key : results.keySet()) {
			sort[a] = results.get(key);
			result[a] = key + ": " + sort[a];
			a++;
		}

		int n = results.size();
		boolean doMore = true;
		while (doMore) {
			n--;
			doMore = false; // assume this is our last pass over the array
			for (int i = 0; i < n; i++) {
				if (sort[i] < sort[i + 1]) {
					// exchange elements

					int tempI = sort[i];
					sort[i] = sort[i + 1];
					sort[i + 1] = tempI;

					String tempR = result[i];
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

	private static Integer calcResult(int a, int b) {
		return a - b;
	}

	public static String[] info(String string) {
		if (!plugin.MySQL) {
			plugin.getLogger().severe("MySQL is not set!");
			return null;
		}
		ResultSet result = null;
		try {
			result = plugin.sqlHandler
					.executeQuery("SELECT `name`,`kills`,`deaths` FROM `pvpstats` WHERE `name` LIKE '%"+string+"%' LIMIT 1;", false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		String[] output = null;
		try {
			while (result != null && result.next()) {
				output = new String[7];
				output[0] = "---------------";
				output[1] = "PVP Stats for §a"+string;
				output[2] = "---------------";
				output[3] = "Name: "+result.getString("name");
				output[4] = "Kills: "+result.getInt("kills");
				output[5] = "Deaths: "+result.getInt("deaths");
				output[6] = "Ratio: "+calcResult(result.getInt("kills"),
						result.getInt("deaths"));
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
}
