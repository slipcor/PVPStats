package net.slipcor.pvpstats.impl;

public class PlayerStatistic {
    private String name;
    private int kills;
    private int deaths;
    private int streak;
    private int currentstreak;
    private int elo;

    public PlayerStatistic(String name, int kills, int deaths, int streak, int currentstreak, int elo) {
        this.name = name;
        this.kills = kills;
        this.deaths = deaths;
        this.streak = streak;
        this.currentstreak = currentstreak;
        this.elo = elo;
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
}
