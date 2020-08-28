package net.slipcor.pvpstats.classes;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class PlayerNameHandler {

    public static OfflinePlayer findPlayer(String value) {
        OfflinePlayer result = null;
        for (OfflinePlayer off : Bukkit.getOfflinePlayers()) {
            if (off.getName().equalsIgnoreCase(value)) {
                return off;
            }
            if (result == null && off.getName().toLowerCase().contains(value.toLowerCase())) {
                result = off;
            }
        }
        // only return match if no exact result was found
        return result;
    }
}
