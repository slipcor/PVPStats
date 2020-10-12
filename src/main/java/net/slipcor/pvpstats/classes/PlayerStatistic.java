package net.slipcor.pvpstats.classes;

import net.slipcor.pvpstats.display.SortColumn;

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

    public PlayerStatistic(String name, int kills, int deaths, int streak, int currentstreak, int elo, long time, UUID uid) {
        this.name = name;
        this.kills = kills;
        this.deaths = deaths;
        this.streak = streak;
        this.currentstreak = currentstreak;
        this.elo = elo;
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

    public Map<SortColumn, String> toStringMap() {
        Map<SortColumn, String> result = new HashMap<>();

        result.put(SortColumn.NAME, String.valueOf(name));
        result.put(SortColumn.DEATHS, String.valueOf(deaths));
        result.put(SortColumn.KILLS, String.valueOf(kills));
        result.put(SortColumn.ELO, String.valueOf(elo));
        result.put(SortColumn.CURRENTSTREAK, String.valueOf(currentstreak));
        result.put(SortColumn.STREAK, String.valueOf(streak));

        return result;
    }
}
