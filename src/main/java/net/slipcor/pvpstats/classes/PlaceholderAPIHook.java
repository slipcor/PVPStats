package net.slipcor.pvpstats.classes;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.slipcor.core.LanguageEntry;
import net.slipcor.pvpstats.PVPStats;
import net.slipcor.pvpstats.api.LeaderboardBuffer;
import net.slipcor.pvpstats.api.PlayerStatisticsBuffer;
import net.slipcor.pvpstats.yml.Language;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import java.util.HashMap;
import java.util.Map;

/**
 * Hook class to hook into the Placeholder API
 *
 * Created by Yaël on 27/02/2016.
 * Updated with code by extendedclip on 15/05/2019
 */
public class PlaceholderAPIHook extends PlaceholderExpansion {
    long lastError = 0;

    static Map<String, LanguageEntry> stringToEntry = new HashMap<>();

    static {
        stringToEntry.put("LINE", Language.MSG.STATISTIC_SEPARATOR);
        stringToEntry.put("KILLS", Language.MSG.STATISTIC_HEADLINE_KILLS);
        stringToEntry.put("DEATHS", Language.MSG.STATISTIC_HEADLINE_DEATHS);
        stringToEntry.put("STREAK", Language.MSG.STATISTIC_HEADLINE_STREAK);
        stringToEntry.put("RATIO", Language.MSG.STATISTIC_HEADLINE_RATIO);
        stringToEntry.put("ELO", Language.MSG.STATISTIC_HEADLINE_ELO);
    }

    @Override
    public String getIdentifier() {
        return "slipcorpvpstats";
    }

    @Override
    public String getAuthor() {
        return "SLiPCoR";
    }

    @Override
    public String getVersion() {
        return "0.0.4";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String s) {
        if (s.equals("kills")) {
            return String.valueOf(PlayerStatisticsBuffer.getKills(player.getUniqueId()));
        }

        if (s.equals("deaths")) {
            return String.valueOf(PlayerStatisticsBuffer.getDeaths(player.getUniqueId()));
        }

        if (s.equals("streak")) {
            return String.valueOf(PlayerStatisticsBuffer.getStreak(player.getUniqueId()));
        }

        if (s.equals("maxstreak")) {
            return String.valueOf(PlayerStatisticsBuffer.getMaxStreak(player.getUniqueId()));
        }

        if (s.equals("elo")) {
            return String.valueOf(PlayerStatisticsBuffer.getEloScore(player.getUniqueId()));
        }

        if (s.equals("ratio")) {
            return String.format("%.2f", PlayerStatisticsBuffer.getRatio(player.getUniqueId()));
        }

        if (s.startsWith("top_")) {
            try {

                String[] split = s.split("_");
                int pos = Integer.parseInt(s.split("_")[2]);
                String name = split[1].toUpperCase();

                if (split.length > 3 && !(s.endsWith("_name") || s.endsWith("_value"))) {
                    return Language.MSG.STATISTIC_HEADLINE_TOP.parse(
                            String.valueOf(pos),
                            stringToEntry.get(name).parse());
                }

                String[] top = LeaderboardBuffer.top(pos, name, 0);

                if (top.length < pos) {
                    return ""; // we do not have enough entries, return empty
                }
                if (s.endsWith("_name")) {
                    return ChatColor.stripColor(top[pos-1].split(":")[0]);
                } else if (s.endsWith("_value")) {
                    return ChatColor.stripColor(top[pos-1].split(":")[1].substring(1));
                }
                return Language.MSG.STATISTIC_FORMAT_NUMBER.parse(String.valueOf(pos), top[pos-1]);
            } catch (Exception e) {
                // let's ignore this for now
                long now = System.currentTimeMillis();
                if (now > lastError+10000) {
                    PVPStats.getInstance().getLogger().warning("Placeholder not working, here is more info:");
                    e.printStackTrace();
                }
                return "";
            }
        } else if (s.startsWith("flop_")) {
            try {

                String[] split = s.split("_");
                int pos = Integer.parseInt(s.split("_")[2]);
                String name = split[1].toUpperCase();

                if (split.length > 3 && !(s.endsWith("_name") || s.endsWith("_value"))) {
                    return Language.MSG.STATISTIC_HEADLINE_FLOP.parse(
                            String.valueOf(pos),
                            stringToEntry.get(name).parse());
                }

                String[] top = LeaderboardBuffer.flop(pos, name);

                if (top.length < pos) {
                    return ""; // we do not have enough entries, return empty
                }
                if (s.endsWith("_name")) {
                    return ChatColor.stripColor(top[pos-1].split(":")[0]);
                } else if (s.endsWith("_value")) {
                    return ChatColor.stripColor(top[pos-1].split(":")[1].substring(1));
                }

                return Language.MSG.STATISTIC_FORMAT_NUMBER.parse(String.valueOf(pos), top[pos-1]);
            } catch (Exception e) {
                // let's ignore this for now
                long now = System.currentTimeMillis();
                if (now > lastError+10000) {
                    PVPStats.getInstance().getLogger().warning("Placeholder not working, here is more info:");
                    e.printStackTrace();
                }
                return "";
            }
        } else if (s.startsWith("topplus_")) {
            try {

                String[] split = s.split("_");
                int pos = Integer.parseInt(split[2]);
                int days = Integer.parseInt(split[3]);

                String name = split[1].toUpperCase();

                if (split.length > 4) {
                    return Language.MSG.STATISTIC_HEADLINE_TOP.parse(
                            String.valueOf(pos),
                            stringToEntry.get(name).parse());
                }

                String[] top = LeaderboardBuffer.topPlus(pos, name, days);

                if (top.length < pos) {
                    return ""; // we do not have enough entries, return empty
                }

                return Language.MSG.STATISTIC_FORMAT_NUMBER.parse(String.valueOf(pos), top[pos-1]);
            } catch (Exception e) {
                // let's ignore this for now
                long now = System.currentTimeMillis();
                if (now > lastError+10000) {
                    PVPStats.getInstance().getLogger().warning("Placeholder not working, here is more info:");
                    e.printStackTrace();
                }
                return "";
            }
        } else if (s.startsWith("topworld_")) {
            try {

                String[] split = s.split("_");
                int pos = Integer.parseInt(split[2]);
                String world = split[3];
                int days = Integer.parseInt(split[4]);

                String name = split[1].toUpperCase();

                if (split.length > 5) {
                    return Language.MSG.STATISTIC_HEADLINE_TOP.parse(
                            String.valueOf(pos),
                            stringToEntry.get(name).parse());
                }

                String[] top = LeaderboardBuffer.topWorld(pos, name, world, days);

                if (top.length < pos) {
                    return ""; // we do not have enough entries, return empty
                }

                return Language.MSG.STATISTIC_FORMAT_NUMBER.parse(String.valueOf(pos), top[pos-1]);
            } catch (Exception e) {
                // let's ignore this for now
                long now = System.currentTimeMillis();
                if (now > lastError+10000) {
                    PVPStats.getInstance().getLogger().warning("Placeholder not working, here is more info:");
                    e.printStackTrace();
                }
                return "";
            }
        }
            return null;
    }
}
