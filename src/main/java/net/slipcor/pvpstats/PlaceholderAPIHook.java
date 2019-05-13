package net.slipcor.pvpstats;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

/**
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
    public String getPlugin() {
        return null;
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
    public String onPlaceholderRequest(Player player, String s) {

        if (s.equals("kills")) {
            return String.valueOf(PVPData.getKills(player.getName()));
        }

        if (s.equals("deaths")) {
            return String.valueOf(PVPData.getDeaths(player.getName()));
        }

        if (s.equals("streak")) {
            return String.valueOf(PVPData.getStreak(player.getName()));
        }

        if (s.equals("maxstreak")) {
            return String.valueOf(PVPData.getMaxStreak(player.getName()));
        }

        if (s.equals("elo")) {
            return String.valueOf(PVPData.getEloScore(player.getName()));
        }

        return null;
    }
}