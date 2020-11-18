package net.slipcor.pvpstats.runnables;

import net.slipcor.pvpstats.PVPStats;
import net.slipcor.pvpstats.core.Config;
import org.bukkit.entity.Player;

public class DatabaseFirstEntry implements Runnable {
    private final Player player;
    public DatabaseFirstEntry(Player player) {
        this.player = player;
    }
    @Override
    public void run() {
        PVPStats.getInstance().getSQLHandler().addFirstStat(
                player.getName(), player.getUniqueId(), 0, 0,
                PVPStats.getInstance().config().getInt(Config.Entry.ELO_DEFAULT));
    }
}
