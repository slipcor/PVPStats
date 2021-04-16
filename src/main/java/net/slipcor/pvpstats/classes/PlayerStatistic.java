package net.slipcor.pvpstats.classes;

import net.slipcor.pvpstats.PVPStats;
import net.slipcor.pvpstats.api.InformationType;
import net.slipcor.pvpstats.core.Config;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A container class that holds all player stats, used when handling database results
 */
public class PlayerStatistic {
    private final String name;
    private final int kills;
    private final int deaths;
    private final int streak;
    private final int currentstreak;
    private final int elo;
    private final long time;
    private final UUID uid;

    public static int ELO_MINIMUM = 16;
    public static int ELO_DEFAULT = 1500;

    public PlayerStatistic(String name, int kills, int deaths, int streak, int currentstreak, int elo, long time, UUID uid) {
        this.name = name;
        this.kills = kills;
        this.deaths = deaths;
        this.streak = streak;
        this.currentstreak = currentstreak;
        this.elo = elo > ELO_MINIMUM ? elo : ELO_DEFAULT;
        this.time = time;
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public int getKills() {
        return kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public int getMaxStreak() {
        return streak;
    }

    public int getCurrentStreak() {
        return currentstreak;
    }

    public int getELO() {
        return elo;
    }

    public long getTime() {
        return time;
    }

    public UUID getUid() { return uid; }

    public Map<InformationType, String> toStringMap() {
        Map<InformationType, String> result = new HashMap<>();

        result.put(InformationType.NAME, String.valueOf(name));
        result.put(InformationType.DEATHS, String.valueOf(deaths));
        result.put(InformationType.KILLS, String.valueOf(kills));
        result.put(InformationType.ELO, String.valueOf(elo));
        result.put(InformationType.CURRENTSTREAK, String.valueOf(currentstreak));
        result.put(InformationType.STREAK, String.valueOf(streak));

        return result;
    }
}
