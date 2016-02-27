package net.slipcor.pvpstats;

import me.clip.placeholderapi.external.EZPlaceholderHook;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Created by Yaël on 27/02/2016.
 */
public class PlaceholderAPIHook extends EZPlaceholderHook {

    public PlaceholderAPIHook(Plugin plugin, String placeholderName) {
        super(plugin, placeholderName);
    }

    @Override
    public String onPlaceholderRequest(Player player, String s) {

        if (s.equals("kills")) {
            return String.valueOf(PVPData.getKills(player.getName()));
        }

        if (s.equals("deaths")) {
            return String.valueOf(PVPData.getDeaths(player.getName()));
        }

        if (PVPData.hasStreak(player.getName()) && s.equals("streak")) {
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