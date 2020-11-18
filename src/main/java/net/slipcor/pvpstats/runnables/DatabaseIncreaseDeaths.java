package net.slipcor.pvpstats.runnables;

import net.slipcor.pvpstats.PVPStats;
import net.slipcor.pvpstats.core.Config;
import org.bukkit.entity.Player;

import java.util.UUID;

public class DatabaseIncreaseDeaths implements Runnable {
    private final String name;
    private final UUID uuid;
    private final int elo;
    public DatabaseIncreaseDeaths(String name, UUID uuid, int elo) {
        this.name = name;
        this.uuid = uuid;
        this.elo = elo;
    }
    @Override
    public void run() {
        PVPStats.getInstance().getSQLHandler().increaseDeaths(
                name, uuid, elo);
    }
}
