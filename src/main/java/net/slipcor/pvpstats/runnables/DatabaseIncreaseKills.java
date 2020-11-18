package net.slipcor.pvpstats.runnables;

import net.slipcor.pvpstats.PVPStats;

import java.util.UUID;

public class DatabaseIncreaseKills implements Runnable {
    private final String name;
    private final UUID uuid;
    private final int elo;
    public DatabaseIncreaseKills(String name, UUID uuid, int elo) {
        this.name = name;
        this.uuid = uuid;
        this.elo = elo;
    }
    @Override
    public void run() {
        PVPStats.getInstance().getSQLHandler().increaseKillsAndStreak(
                name, uuid, elo);
    }
}