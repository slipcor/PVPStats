package net.slipcor.pvpstats;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;

/**
 * MySQL access class
 *
 * @author slipcor
 */

public final class PSMySQL {

    private PSMySQL() {

    }

    private static PVPStats plugin = null;

    private static final Debug DEBUG = new Debug(4);

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
                if (result != null && result.next()) {
                    return true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private static boolean incKill(final Player player, int elo) {
        if (player.hasPermission("pvpstats.count")) {
            boolean incMaxStreak;
            int streak;
            if (PVPData.hasStreak(player.getName())) {
                incMaxStreak = PVPData.addStreak(player.getName());
                streak = PVPData.getStreak(player.getName());
            } else {

                int streakCheck = PVPData.getStreak(player.getName());
                if (streakCheck < 1) {
                    PVPData.setStreak(player.getName(), 1);
                    PVPData.setMaxStreak(player.getName(), 1);
                    streak = 1;
                    incMaxStreak = true;
                } else {
                    incMaxStreak = PVPData.addStreak(player.getName());
                    streak = PVPData.getStreak(player.getName());
                }

            }
            checkAndDo(player.getName(), player.getUniqueId(), true, incMaxStreak, streak, elo);
            return true;
        }
        return false;
    }

    private static boolean incDeath(final Player player, int elo) {
        if (player.hasPermission("pvpstats.count")) {
            PVPData.setStreak(player.getName(), 0);
            checkAndDo(player.getName(), player.getUniqueId(), false, false, 0, elo);
            return true;
        }
        return false;
    }

    private static void checkAndDo(final String sPlayer, final UUID pid, final boolean kill, final boolean addMaxStreak, final int currentStreak, int elo) {

        if (!mysqlExists("SELECT * FROM `" + plugin.dbTable + "` WHERE `uid` = '" + pid
                + "';")) {
            final int kills = kill ? 1 : 0;
            final int deaths = kill ? 0 : 1;
            mysqlQuery("INSERT INTO `" + plugin.dbTable + "` (`name`, `uid`, `kills`,`deaths`,`streak`,`currentstreak`,`elo`,`time`) VALUES ('"
                    + sPlayer + "', '" + pid + "', " + kills + ", " + deaths + ", " + kills + ", " + kills + ", " + elo + ", " + System.currentTimeMillis() / 1000 + ")");
            PVPData.setKills(sPlayer, kills);
            PVPData.setDeaths(sPlayer, deaths);

            if (plugin.dbKillTable != null) {
                mysqlQuery("INSERT INTO " + plugin.dbKillTable + " (`name`,`uid`,`kill`,`time`) VALUES(" +
                        "'" + sPlayer + "', '" + pid + "', '" + (kill ? 1 : 0) + "', " + System.currentTimeMillis() / 1000 + ")");
            }
            return;
        }
        final String var = kill ? "kills" : "deaths";
        mysqlQuery("UPDATE `" + plugin.dbTable + "` SET `" + var + "` = `" + var
                + "`+1, `elo` = '" + elo + "', `time` = " + System.currentTimeMillis() / 1000 + " WHERE `uid` = '" + pid + "'");

        if (addMaxStreak && kill) {
            mysqlQuery("UPDATE `" + plugin.dbTable + "` SET `streak` = `streak`+1, `currentstreak` = `currentstreak`+1, `time` = " +
                    System.currentTimeMillis() / 1000 + " WHERE `uid` = '" + pid + "'");
        } else if (kill) {
            mysqlQuery("UPDATE `" + plugin.dbTable + "` SET `currentstreak` = `currentstreak`+1, `time` = " +
                    System.currentTimeMillis() / 1000 + " WHERE `uid` = '" + pid + "'");
        } else {
            mysqlQuery("UPDATE `" + plugin.dbTable + "` SET `currentstreak` = 0, `time` = " +
                    System.currentTimeMillis() / 1000 + " WHERE `uid` = '" + pid + "'");
        }

        if (plugin.dbKillTable != null) {
            mysqlQuery("INSERT INTO " + plugin.dbKillTable + " (`name`,`uid`,`kill`,`time`) VALUES(" +
                    "'" + sPlayer + "', '" + pid + "', '" + (kill ? 1 : 0) + "', " + System.currentTimeMillis() / 1000 + ")");
        }

    }

    /**
     * @param count the amount to fetch
     * @param sort  sorting string
     * @return a sorted array
     */
    public static String[] top(final int count, String sort) {
        if (!plugin.mySQL) {
            plugin.getLogger().severe("MySQL is not set!");
            return null;
        }

        sort = sort.toUpperCase();
        ResultSet result = null;
        final Map<String, Double> results = new HashMap<>();

        final List<String> sortedValues = new ArrayList<>();

        String order = null;
        try {

            switch (sort) {
                case "DEATHS":
                    order = "deaths";
                    break;
                case "STREAK":
                    order = "streak";
                    break;
                case "CURRENTSTREAK":
                    order = "currentstreak";
                    break;
                case "ELO":
                    order = "elo";
                    break;
                case "KILLS":
                case "K-D":
                default:
                    order = "kills";
                    break;
            }

            int limit = sort.equals("K-D") ? 50 : count;

            String sorting = sort.equals("DEATHS") ? "ASC" : "DESC";

            String query = "SELECT `name`,`kills`,`deaths`,`streak`,`currentstreak`,`elo` FROM `" +
                    plugin.dbTable + "` WHERE 1 ORDER BY `" + order + "` " + sorting + " LIMIT " + limit + ";";

            result = plugin.sqlHandler
                    .executeQuery(query, false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            while (result != null && result.next()) {
                switch (sort) {
                    case "KILLS":
                    case "DEATHS":
                    case "ELO":
                    case "STREAK":
                    case "CURRENTSTREAK":
                        sortedValues.add(ChatColor.RED + result.getString("name") + ":" + ChatColor.GRAY + " " + result.getInt(order));
                        break;
                    default:
                        results.put(
                                result.getString("name"),
                                calcResult(result.getInt("kills"),
                                        result.getInt("deaths"),
                                        result.getInt("streak"), PVPData.getStreak(result.getString("name"))));
                        break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (sort.equals("KILLS") || sort.equals("DEATHS") || sort.equals("ELO") || sort.equals("STREAK") || sort.equals("CURRENTSTREAK")) {
            String[] output = new String[sortedValues.size()];

            int pos = 0;

            for (String s : sortedValues) {
                output[pos++] = s;
            }
            return output;
        }

        return sortParse(results, count);
    }

    private static String[] sortParse(final Map<String, Double> results,
                                      final int count) {
        String[] result = new String[results.size()];
        Double[] sort = new Double[results.size()];

        int pos = 0;

        DecimalFormat df = new DecimalFormat("#.##");

        for (String key : results.keySet()) {
            sort[pos] = results.get(key);
            result[pos] = ChatColor.RED + key + ":" + ChatColor.GRAY + " " + df.format(sort[pos]);
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

                    final double tempI = sort[i];
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
        System.arraycopy(result, 0, output, 0, output.length);

        return output;
    }

    private static Double calcResult(final int kills, final int deaths, final int streak,
                                     final int maxstreak) {

        String string = plugin.getConfig().getString("kdcalculation");

        string = string.replaceAll("&k", "(" + kills + ")");
        string = string.replaceAll("&d", "(" + deaths + ")");
        string = string.replaceAll("&s", "(" + streak + ")");
        string = string.replaceAll("&m", "(" + maxstreak + ")");

        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("JavaScript");
        StringBuilder saneString = new StringBuilder();

        // Java 8 compatibility
        if (engine == null) {
            mgr = new ScriptEngineManager(null);
            engine = mgr.getEngineByName("nashorn");
        }

        for (char c : string.toCharArray()) {
            switch (c) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case '-':
                case '+':
                case '*':
                case '/':
                case '(':
                case ')':
                case '<':
                case '>':
                case '?':
                case ':':
                case '=':
                    saneString.append(c);
                    break;
                default:
            }
        }

        try {
            Object value = engine.eval(saneString.toString());

            if (value instanceof Double) {
                return (Double) value;
            } else if (value instanceof Integer) {
                int i = (Integer) value;
                return (double) i;
            }
            plugin.getLogger().severe("SaneString: " + value.toString());

            return 0d;
        } catch (ScriptException e) {
            plugin.getLogger().severe("SaneString: " + saneString.toString());
            e.printStackTrace();
            return 0d;
        }
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
        DEBUG.i("getting info for " + string);
        ResultSet result = null;
        try {
            result = plugin.sqlHandler
                    .executeQuery("SELECT `name`,`kills`,`deaths`,`streak`,`currentstreak`, `elo` FROM `" + plugin.dbTable + "` WHERE `name` = '" + string + "' LIMIT 1;", false);
            if (result == null || !result.next()) {
                result = plugin.sqlHandler
                        .executeQuery("SELECT `name`,`kills`,`deaths`,`streak`,`currentstreak`, `elo` FROM `" + plugin.dbTable + "` WHERE `name` LIKE '%" + string + "%' LIMIT 1;", false);
                if (result == null || !result.next()) {
                    String[] output = new String[1];
                    output[0] = Language.INFO_PLAYERNOTFOUND.toString(string);
                    return output;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String[] output = null;
        try {
            String name = result.getString("name");

            int elo = result.getInt("elo");
            int kills = result.getInt("kills");
            int deaths = result.getInt("deaths");
            int streak = result.getInt("currentstreak");
            int maxStreak = result.getInt("streak");
            Double ratio = calcResult(kills, deaths, maxStreak, streak);

            if (plugin.getConfig().getBoolean("msgoverrides")) {
                List<String> lines = plugin.getConfig().getStringList("msg.main");
                output = new String[lines.size()];

                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i);

                    line = line.replace("%d", String.valueOf(deaths));
                    line = line.replace("%k", String.valueOf(kills));
                    line = line.replace("%m", String.valueOf(maxStreak));
                    line = line.replace("%n", name);
                    line = line.replace("%r", String.valueOf(ratio));
                    line = line.replace("%s", String.valueOf(streak));
                    line = line.replace("%e", String.valueOf(elo));

                    output[i] = ChatColor.translateAlternateColorCodes('&', line);
                }

                return output;
            }


            output = new String[7];

            output[0] = Language.INFO_FORMAT.toString(
                    Language.INFO_NAME.toString(),
                    name);
            output[1] = Language.INFO_FORMAT.toString(
                    Language.INFO_KILLS.toString(),
                    String.valueOf(kills));
            output[2] = Language.INFO_FORMAT.toString(
                    Language.INFO_DEATHS.toString(),
                    String.valueOf(deaths));
            output[3] = Language.INFO_FORMAT.toString(
                    Language.INFO_RATIO.toString(),
                    String.valueOf(ratio));
            output[4] = Language.INFO_FORMAT.toString(
                    Language.INFO_STREAK.toString(),
                    String.valueOf(streak));
            output[5] = Language.INFO_FORMAT.toString(
                    Language.INFO_MAXSTREAK.toString(),
                    String.valueOf(maxStreak));
            output[6] = Language.INFO_FORMAT.toString(
                    Language.INFO_ELO.toString(),
                    String.valueOf(elo));
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

        if (!entry.equals("elo") &&
                !entry.equals("kills") &&
                !entry.equals("deaths") &&
                !entry.equals("streak") &&
                !entry.equals("currentstreak")) {
            throw new IllegalArgumentException("entry can not be '" + entry + "'. Valid values: elo, kills, deaths, streak, currentstreak");
        }

        if (!plugin.mySQL) {
            plugin.getLogger().severe("MySQL is not set!");
            return null;
        }
        ResultSet result = null;
        try {
            result = plugin.sqlHandler
                    .executeQuery("SELECT `" + entry + "` FROM `" + plugin.dbTable + "` WHERE `name` = '" + player + "' LIMIT 1;", false);
            if (result == null || !result.next()) {
                result = plugin.sqlHandler
                        .executeQuery("SELECT `" + entry + "` FROM `" + plugin.dbTable + "` WHERE `name` LIKE '%" + player + "%' LIMIT 1;", false);
                if (result == null || !result.next()) {
                    return 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            return result.getInt(entry);
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
            mysqlQuery("DELETE FROM `" + plugin.dbTable + "` WHERE 1;");
            if (plugin.dbKillTable != null) {
                mysqlQuery("DELETE FROM `" + plugin.dbKillTable + "` WHERE 1;");
            }
        } else {
            PVPData.setDeaths(name, 0);
            PVPData.setKills(name, 0);
            PVPData.setMaxStreak(name, 0);
            PVPData.setStreak(name, 0);

            mysqlQuery("DELETE FROM `" + plugin.dbTable + "` WHERE `name` = '" + name
                    + "';");
            if (plugin.dbKillTable != null) {
                mysqlQuery("DELETE FROM `" + plugin.dbKillTable + "` WHERE `name` = '" + name
                        + "';");
            }
        }
    }

    public static int purgeStats(int days) {
        if (!plugin.mySQL) {
            plugin.getLogger().severe("MySQL is not set!");
            return 0;
        }
        ResultSet result;

        int count = 0;

        long timestamp = System.currentTimeMillis()/1000 - ((long) days * 24L * 60L * 60L);

        try {

            result = plugin.sqlHandler
                    .executeQuery("SELECT `time` FROM `" + plugin.dbTable + "` WHERE `time` < "+timestamp+";", false);

            while (result != null && result.next()) {
                count++;
            }

            if (count > 0) {
                StringBuilder buff = new StringBuilder("DELETE FROM `");
                buff.append(plugin.dbTable);
                buff.append("` WHERE `time` < "+timestamp+";");

                mysqlQuery(buff.toString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return count;
    }

    public static int purgeKillStats(int days) {
        if (!plugin.mySQL) {
            plugin.getLogger().severe("MySQL is not set!");
            return 0;
        }
        ResultSet result;

        int count = 0;

        long timestamp = System.currentTimeMillis()/1000 - ((long) days * 24L * 60L * 60L);

        try {

            result = plugin.sqlHandler
                    .executeQuery("SELECT `time` FROM `" + plugin.dbKillTable + "` WHERE `time` < "+timestamp+";", false);

            while (result != null && result.next()) {
                count++;
            }

            if (count > 0) {
                StringBuilder buff = new StringBuilder("DELETE FROM `");
                buff.append(plugin.dbKillTable);
                buff.append("` WHERE `time` < "+timestamp+";");

                mysqlQuery(buff.toString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return count;
    }

    public static int clean() {
        if (!plugin.mySQL) {
            plugin.getLogger().severe("MySQL is not set!");
            return 0;
        }
        ResultSet result;

        List<Integer> ints = new ArrayList<>();
        Map<String, Integer> players = new HashMap<>();

        try {

            result = plugin.sqlHandler
                    .executeQuery("SELECT `id`, `name` FROM `" + plugin.dbTable + "` WHERE 1 ORDER BY `kills` DESC;", false);

            while (result != null && result.next()) {
                String playerName = result.getString("name");

                if (players.containsKey(playerName)) {
                    ints.add(result.getInt("id"));
                    players.put(playerName, players.get(playerName) + 1);
                } else {
                    players.put(playerName, 1);
                }
            }

            if (ints.size() > 0) {
                StringBuilder buff = new StringBuilder("DELETE FROM `");
                buff.append(plugin.dbTable);
                buff.append("` WHERE `id` IN (");

                boolean first = true;

                for (Integer i : ints) {
                    if (!first) {
                        buff.append(',');
                    }
                    first = false;
                    buff.append(i);
                }

                buff.append(");");

                mysqlQuery(buff.toString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        int i = 10;

        return ints.size();
    }

    public static List<String> getAllPlayers(String dbTable) {
        if (!plugin.mySQL) {
            plugin.getLogger().severe("MySQL is not set!");
            return null;
        }
        List<String> output = new ArrayList<>();

        ResultSet result = null;

        try {
            result = plugin.sqlHandler
                    .executeQuery("SELECT `name` FROM `" + dbTable + "` GROUP BY `name`;", false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            while (result != null && result.next()) {
                output.add(result.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return output;
    }

    public static void commit(String dbTable, Map<String, UUID> map) {

        for (Entry<String, UUID> set : map.entrySet()) {
            mysqlQuery("UPDATE `" + dbTable + "` SET `uid` = '" + set.getValue() + "' WHERE `name` = '" + set.getKey() + "';");
        }
    }

    public static void AkilledB(Player attacker, Player player) {
        DEBUG.i("AkilledB, A is " + attacker, player);
        if (attacker == null && player == null) {
            DEBUG.i("attacker and player are null", player);
            return;
        }

        if (player == null) {
            DEBUG.i("player is null", player);
            incKill(attacker, PVPData.getEloScore(attacker.getName()));
            return;
        }
        if (attacker == null) {
            DEBUG.i("attacker is null", player);
            incDeath(player, PVPData.getEloScore(player.getName()));
            return;
        }

        if (attacker.hasPermission("pvpstats.newbie") || player.hasPermission("pvpstats.newbie")) {
            DEBUG.i("either one has newbie status", player);
            return;
        }

        ConfigurationSection sec = PVPStats.getInstance().getConfig().getConfigurationSection("eloscore");

        if (!sec.getBoolean("active")) {
            DEBUG.i("no elo", player);
            incKill(attacker, PVPData.getEloScore(attacker.getName()));
            incDeath(player, PVPData.getEloScore(player.getName()));
            return;
        }

        final int min = sec.getInt("minimum", 18);
        final int max = sec.getInt("maximum", 3000);
        final int kBelow = sec.getInt("k-factor.below", 32);
        final int kAbove = sec.getInt("k-factor.above", 16);
        final int kThreshold = sec.getInt("k-factor.threshold", 2000);

        final int oldA = PVPData.getEloScore(attacker.getName());
        final int oldP = PVPData.getEloScore(player.getName());

        final int kA = oldA >= kThreshold ? kAbove : kBelow;
        final int kP = oldP >= kThreshold ? kAbove : kBelow;

        final int newA = calcElo(oldA, oldP, kA, true, min, max);
        final int newP = calcElo(oldP, oldA, kP, false, min, max);

        if (incKill(attacker, newA)) {
            DEBUG.i("increasing kill", attacker);
            plugin.sendPrefixed(attacker, Language.MSG_ELO_ADDED.toString(String.valueOf(newA - oldA), String.valueOf(newA)));
            PVPData.setEloScore(attacker.getName(), newA);
        }
        if (incDeath(player, newP)) {
            DEBUG.i("increasing death", player);
            plugin.sendPrefixed(player, Language.MSG_ELO_SUBBED.toString(String.valueOf(oldP - newP), String.valueOf(newP)));
            PVPData.setEloScore(player.getName(), newP);
        }
    }

    private static int calcElo(int myOld, int otherOld, int k, boolean win, int min, int max) {
        double expected = 1.0f / (1.0f + Math.pow(10.0f, ((float) (otherOld - myOld)) / 400.0f));

        int newVal;
        if (win) {
            newVal = (int) Math.round(myOld + k * (1.0f - expected));
        } else {
            newVal = (int) Math.round(myOld + k * (0.0f - expected));
        }

        if (min > -1 && newVal < min) {
            return min;
        }

        if (max > -1 && newVal > max) {
            return max;
        }

        return newVal;
    }

    public static void initiatePlayer(Player player, String dbTable) {
        ResultSet result = null;

        if (getAllPlayers(dbTable).contains(player.getName())) {

            try {
                result = plugin.sqlHandler
                        .executeQuery("SELECT `uid` FROM `" + dbTable + "` WHERE `name` = '" + player.getName() + "';", false);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                while (result != null && result.next()) {
                    String value = result.getString("uid");
                    if (value == null || value.equals("")) {
                        plugin.sqlHandler.executeQuery("UPDATE `" + dbTable + "` SET `uid` = '" + player.getUniqueId() + "' WHERE `name` = '" + player.getName() + "';", true);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // read all the data from database
        PVPData.getStreak(player.getName());
        PVPData.getDeaths(player.getName());
        PVPData.getEloScore(player.getName());
        PVPData.getKills(player.getName());
        PVPData.getMaxStreak(player.getName());
    }
}
