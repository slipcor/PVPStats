package net.slipcor.pvpstats.runnables;

import net.slipcor.pvpstats.PVPStats;
import net.slipcor.pvpstats.classes.PlayerNameHandler;
import net.slipcor.pvpstats.yml.Config;
import org.bukkit.OfflinePlayer;

public class DatabaseFirstEntry implements Runnable {
    private final OfflinePlayer player;
    public DatabaseFirstEntry(OfflinePlayer player) {
        this.player = player;
    }
    @Override
    public void run() {
        PVPStats.getInstance().getSQLHandler().addFirstStat(
                PlayerNameHandler.getPlayerName(player), player.getUniqueId(), 0, 0,
                PVPStats.getInstance().config().getInt(Config.Entry.ELO_DEFAULT));
    }
}
