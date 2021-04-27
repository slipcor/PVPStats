package net.slipcor.pvpstats.runnables;

import net.slipcor.core.CoreDebugger;
import net.slipcor.pvpstats.PVPStats;

public class DatabaseKillAddition implements Runnable {
    private final String attackerName;
    private final String attackerUUID;
    private final String victimName;
    private final String victimUUID;
    private final String world;

    public static CoreDebugger debugger;
    public DatabaseKillAddition(String attackerName, String attackerUUID, String victimName, String victimUUID, String world) {
        this.attackerName = attackerName;
        this.attackerUUID = attackerUUID;
        this.victimName = victimName;
        this.victimUUID = victimUUID;
        this.world = world;
    }

    @Override
    public void run() {
        PVPStats.getInstance().getSQLHandler().addKill(attackerName, attackerUUID, victimName, victimUUID, world);
        debugger.i("kill addition KA sent!");
    }
}
