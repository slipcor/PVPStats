package net.slipcor.pvpstats.metrics;

import net.slipcor.core.CoreMetrics;
import net.slipcor.pvpstats.PVPStats;
import org.bukkit.plugin.Plugin;

/**
 * bStats collects some data for plugin authors.
 *
 * Check out https://bStats.org/ to learn more about bStats!
 */
public class MetricsLite extends CoreMetrics {

    /**
     * Class constructor.
     *
     * @param plugin The plugin which stats should be submitted.
     */
    public MetricsLite(Plugin plugin) {
        super(plugin, 9747);
        PVPStats.getInstance().getLogger().info("sending minimum Metrics <3");
    }
}
