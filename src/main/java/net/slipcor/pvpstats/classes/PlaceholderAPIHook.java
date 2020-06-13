package net.slipcor.pvpstats.classes;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.slipcor.pvpstats.PVPStats;
import net.slipcor.pvpstats.api.DatabaseAPI;
import net.slipcor.pvpstats.api.PlayerStatisticsBuffer;
import net.slipcor.pvpstats.core.Language;
import org.bukkit.OfflinePlayer;

/**
 * Hook class to hook into the Placeholder API
 * <p>
 * Created by Yaël on 27/02/2016.
 * Updated with code by extendedclip on 15/05/2019
 */
public class PlaceholderAPIHook extends PlaceholderExpansion {
    long lastError = 0;

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
        return "0.0.3";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String s) {
        if (s.equals("kills")) {
            return String.valueOf(PlayerStatisticsBuffer.getKills(player.getName()));
        }

        if (s.equals("deaths")) {
            return String.valueOf(PlayerStatisticsBuffer.getDeaths(player.getName()));
        }

        if (s.equals("streak")) {
            return String.valueOf(PlayerStatisticsBuffer.getStreak(player.getName()));
        }

        if (s.equals("maxstreak")) {
            return String.valueOf(PlayerStatisticsBuffer.getMaxStreak(player.getName()));
        }

        if (s.equals("elo")) {
            return String.valueOf(PlayerStatisticsBuffer.getEloScore(player.getName()));
        }

        if (s.equals("ratio")) {
            return String.format("%.2f", PlayerStatisticsBuffer.getRatio(player.getName()));
        }

        if (s.startsWith("top_")) {
            try {

                String[] split = s.split("_");
                int pos = Integer.parseInt(s.split("_")[2]);
                String name = split[1].toUpperCase();

                if (split.length > 3) {
                    return Language.HEAD_HEADLINE.toString(
                            String.valueOf(pos),
                            Language.valueOf("HEAD_" + name).toString());
                }

                String[] top = DatabaseAPI.top(pos, name);

                if (top == null || top.length < pos) {
                    return ""; // we do not have enough entries, return empty
                }

                return (pos + ": " + top[pos-1]);
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

        // slipcorpvpstats_top_kills_10_head

        // slipcorpvpstats_top_kills_1
        // slipcorpvpstats_top_deaths_1
        // slipcorpvpstats_top_streak_1
        // slipcorpvpstats_top_elo_1
        // slipcorpvpstats_top_k-d_1

        return null;
    }
}
