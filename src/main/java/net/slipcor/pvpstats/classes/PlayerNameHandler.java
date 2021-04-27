package net.slipcor.pvpstats.classes;

import net.slipcor.pvpstats.PVPStats;
import net.slipcor.pvpstats.yml.Config;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

public class PlayerNameHandler {

    public static OfflinePlayer findPlayer(String value) {
        OfflinePlayer result = null;
        for (OfflinePlayer off : Bukkit.getOfflinePlayers()) {
            if (off.getName().equalsIgnoreCase(value)) {
                return off;
            }
            if (off.getPlayer() != null && off.getPlayer().getDisplayName().toLowerCase().contains(value.toLowerCase())) {
                return off;
            }
            if (result == null && off.getName().toLowerCase().contains(value.toLowerCase())) {
                result = off;
            }
        }
        // only return match if no exact result was found
        return result;
    }

    public static String getPlayerName(OfflinePlayer offlinePlayer) {
        if (PVPStats.getInstance().config().getBoolean(Config.Entry.OTHER_DISPLAYNAMES)) {
            if (offlinePlayer.getPlayer() != null) {
                return offlinePlayer.getPlayer().getDisplayName();
            }
        }
        return offlinePlayer.getName();
    }

    public static String getRawPlayerName(OfflinePlayer offlinePlayer) {
        if (PVPStats.getInstance().config().getBoolean(Config.Entry.OTHER_DISPLAYNAMES)) {
            if (offlinePlayer.getPlayer() != null) {
                return ChatColor.stripColor(offlinePlayer.getPlayer().getDisplayName());
            }
        }
        return offlinePlayer.getName();
    }
}
