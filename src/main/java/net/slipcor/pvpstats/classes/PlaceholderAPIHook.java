package net.slipcor.pvpstats.classes;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.slipcor.pvpstats.api.PlayerStatisticsBuffer;
import org.bukkit.entity.Player;

/**
 * Hook class to hook into the Placeholder API
 * <p>
 * Created by Yaël on 27/02/2016.
 * Updated with code by extendedclip on 15/05/2019
 */
public class PlaceholderAPIHook extends PlaceholderExpansion {
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
        return "0.0.1";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer p, String s)

        if (s.equals("kills")) {
            return String.valueOf(PlayerStatisticsBuffer.getKills(p.getName()));
        }

        if (s.equals("deaths")) {
            return String.valueOf(PlayerStatisticsBuffer.getDeaths(p.getName()));
        }

        if (s.equals("streak")) {
            return String.valueOf(PlayerStatisticsBuffer.getStreak(p.getName()));
        }

        if (s.equals("maxstreak")) {
            return String.valueOf(PlayerStatisticsBuffer.getMaxStreak(p.getName()));
        }

        if (s.equals("elo")) {
            return String.valueOf(PlayerStatisticsBuffer.getEloScore(p.getName()));
        }

        return null;
    }
}
