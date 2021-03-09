package net.slipcor.pvpstats.runnables;

import net.slipcor.pvpstats.PVPStats;
import net.slipcor.pvpstats.api.PlayerStatisticsBuffer;
import net.slipcor.pvpstats.classes.Debugger;
import org.bukkit.Bukkit;

import java.util.UUID;

public class CheckAndDo implements Runnable {

    private final String playerName;
    private final UUID uuid;
    private final boolean kill;
    private final boolean addMaxStreak;
    private final int elo;
    private final String world;

    private static final Debugger DEBUGGER = new Debugger(20);
    private final PVPStats plugin = PVPStats.getInstance();

    public CheckAndDo(final String playerName, final UUID uuid, final boolean kill, final boolean addMaxStreak, int elo, String world) {
        this.playerName = playerName;
        this.uuid = uuid;
        this.kill = kill;
        this.addMaxStreak = addMaxStreak;
        this.elo = elo;
        this.world = world;
    }

    @Override
    public void run() {

        DEBUGGER.i("checkAndDo running in thread: " + Thread.currentThread().getName());
        DEBUGGER.i("checkAndDo isMainThread: " + (Bukkit.isPrimaryThread() ? "YES" : "NO"));

        if (!plugin.getSQLHandler().hasEntry(uuid)) {

            DEBUGGER.i("player has no entry yet, adding!");

            final int kills = kill ? 1 : 0;
            final int deaths = kill ? 0 : 1;

            plugin.getSQLHandler().addFirstStat(playerName, uuid, kills, deaths, elo);

            PlayerStatisticsBuffer.setKills(uuid, kills);
            PlayerStatisticsBuffer.setDeaths(uuid, deaths);
            return;
        }

        if (addMaxStreak && kill) {
            DEBUGGER.i("increasing kills and max streak");
//            Bukkit.getScheduler().runTask(PVPStats.getInstance(),
//                    new DatabaseIncreaseKillsStreak(playerName, uuid, elo));
            (new DatabaseIncreaseKillsStreak(playerName, uuid, elo)).run();
        } else if (kill) {
            DEBUGGER.i("increasing kills and current streak");
//            Bukkit.getScheduler().runTask(PVPStats.getInstance(),
//                    new DatabaseIncreaseKills(playerName, uuid, elo));
            (new DatabaseIncreaseKills(playerName, uuid, elo)).run();
        } else {
            DEBUGGER.i("increasing deaths");
//            Bukkit.getScheduler().runTask(PVPStats.getInstance(),
//                    new DatabaseIncreaseDeaths(playerName, uuid, elo));
            (new DatabaseIncreaseDeaths(playerName, uuid, elo)).run();
        }

        if (kill) {
            PlayerStatisticsBuffer.addKill(uuid);
        } else {
            PlayerStatisticsBuffer.addDeath(uuid);
        }
    }
}
