package net.slipcor.pvpstats.classes;

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
