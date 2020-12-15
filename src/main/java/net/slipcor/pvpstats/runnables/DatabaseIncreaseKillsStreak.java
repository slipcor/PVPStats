package net.slipcor.pvpstats.runnables;

import net.slipcor.pvpstats.PVPStats;
import net.slipcor.pvpstats.classes.Debugger;

import java.util.UUID;

public class DatabaseIncreaseKillsStreak implements Runnable {
    private final String name;
    private final UUID uuid;
    private final int elo;

    static Debugger debugger = new Debugger(15);
    public DatabaseIncreaseKillsStreak(String name, UUID uuid, int elo) {
        this.name = name;
        this.uuid = uuid;
        this.elo = elo;
    }
    @Override
    public void run() {
        PVPStats.getInstance().getSQLHandler().increaseKillsAndMaxStreak(
                name, uuid, elo);
        debugger.i("kill addition sent!");
    }
}
