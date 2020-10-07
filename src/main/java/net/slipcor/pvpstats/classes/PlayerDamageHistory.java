package net.slipcor.pvpstats.classes;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class PlayerDamageHistory {
    private final Map<UUID, Long> damagers = new HashMap<>();

    public void commitPlayerDamage(Player damager) {
        //System.out.print("committing player damage. damager: " + damager.getName());
        this.damagers.put(damager.getUniqueId(), System.currentTimeMillis());
    }

    public List<UUID> getLastDamage(int seconds) {
        List<UUID> uuids = new ArrayList<>();

        long tenSecondsAgo = System.currentTimeMillis() - (1000 * seconds);

        damagers.entrySet().stream().sorted(
                Map.Entry.<UUID, Long>comparingByValue().reversed()).
                filter(a -> a.getValue() > tenSecondsAgo).
                limit(2).forEach( a -> uuids.add(a.getKey()) );

        int pos = 1 ;
        for (UUID u : uuids) {
            //System.out.println("GetLastDamage: " + pos++ + " - " + Bukkit.getPlayer(u).getName());
        }

        return uuids;
    }
}
