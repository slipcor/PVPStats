package net.slipcor.pvpstats;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
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

    public Tracker(Plugin p) {
        plugin = p;
    }

    @Override
    public void run() {
        callHome();
    }

    private void callHome() {
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

}
