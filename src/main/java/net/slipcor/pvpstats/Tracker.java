package net.slipcor.pvpstats;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.net.URL;
import java.net.URLEncoder;

/**
 * tracker class
 * <p/>
 * -
 * <p/>
 * tracks plugin version
 *
 * @author slipcor
 */

public class Tracker implements Runnable {
    private Plugin plugin;
    private static BukkitTask timer = null;

    public Tracker(Plugin p) {
        plugin = p;
    }

    public void start() {
        timer = Bukkit.getScheduler().runTaskTimer(plugin, this,
                0L, 72000L);
    }

    public static void stop() {
        timer.cancel();
    }

    private void callHome() {
        if (!plugin.getConfig().getBoolean("tracker", true)) {
            return;
        }

        try {
            String url = String
                    .format("http://www.slipcor.net/stats/call.php?port=%s&name=%s&version=%s",
                            plugin.getServer().getPort(),
                            URLEncoder.encode(
                                    plugin.getDescription().getName(), "UTF-8"),
                            URLEncoder.encode(plugin.getDescription()
                                    .getVersion(), "UTF-8"));
            new URL(url).openConnection().getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        callHome();
    }
}
