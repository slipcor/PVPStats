package net.slipcor.pvpstats.classes;

import net.slipcor.pvpstats.PVPStats;
import net.slipcor.pvpstats.yml.Config;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PlayerHandler {

    public static OfflinePlayer findPlayer(String value) {
        OfflinePlayer result = Bukkit.getServer().getPlayer(value);

        if (result != null) {
            return result;
        }

        if (value.length() < 3 || value.length() > 16 || value.contains(" ")) {
            // player name invalid
            return null;
        }

        if (Bukkit.getOfflinePlayers().length > 10000) {
            return PVPStats.getInstance().getSQLHandler().findPlayer(value);
        }

        for (OfflinePlayer off : Bukkit.getOfflinePlayers()) {
            if (off == null) {
                continue;
            }
            String name = off.getName();

            if (name.equalsIgnoreCase(value)) {
                return off;
            }
            if (off.getPlayer() != null && off.getPlayer().getDisplayName().toLowerCase().contains(value.toLowerCase())) {
                return off;
            }
            if (result == null && name.toLowerCase().contains(value.toLowerCase())) {
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

    public static String getPlayerWorld(OfflinePlayer offlinePlayer) {
        if (offlinePlayer.getPlayer() != null) {
            Player player = offlinePlayer.getPlayer();
            return player.getWorld().getName();
        }

        if (Bukkit.getServer().getWorlds().size() < 1) {
            return "unknown";
        }

        return Bukkit.getServer().getWorlds().get(0).getName();
    }
}
