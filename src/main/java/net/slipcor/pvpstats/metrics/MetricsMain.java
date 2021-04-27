package net.slipcor.pvpstats.metrics;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.slipcor.core.CoreMetrics;
import net.slipcor.pvpstats.PVPStats;
import net.slipcor.pvpstats.yml.Config;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.Callable;

/**
 * bStats collects some data for plugin authors.
 *
 * Check out https://bStats.org/ to learn more about bStats!
 */
public class MetricsMain extends CoreMetrics {

    /**
     * Class constructor.
     *
     * @param plugin The plugin which stats should be submitted.
     */
    public MetricsMain(Plugin plugin) {
        super(plugin, 9747);

        this.addConnectionChart(Config.Entry.MYSQL_ACTIVE, Config.Entry.SQLITE_ACTIVE);
        this.addChart(Config.Entry.STATISTICS_KD_SIMPLE, "simple_kd_ratio");

        this.addChart(Config.Entry.STATISTICS_CLEAR_ON_START, "clear_on_start");
        this.addChart(Config.Entry.STATISTICS_CREATE_ON_JOIN, "create_on_join");
        this.addChart(Config.Entry.STATISTICS_COLLECT_PRECISE, "collect_precise");
        this.addChart(Config.Entry.STATISTICS_COUNT_REGULAR_DEATHS, "count_regular_deaths");

        this.addChart(Config.Entry.STATISTICS_CHECK_ABUSE, "check_abuse");
        this.addChart(Config.Entry.STATISTICS_CHECK_NEWBIES, "check_newbies");

        this.addChart(Config.Entry.STATISTICS_DEATHS_DESCENDING, "deaths_descending");
        this.addChart(Config.Entry.STATISTICS_RESET_KILLSTREAK_ON_QUIT, "reset_streak");
        this.addChart(Config.Entry.STATISTICS_STREAK_ANNOUNCEMENTS, "streak_announcements");
        this.addChart(Config.Entry.STATISTICS_STREAK_COMMANDS, "streak_commands");

        this.addChart(Config.Entry.ELO_ACTIVE, "use_elo");
        this.addChart(Config.Entry.OTHER_PVPARENA, "use_pvparena");

        PVPStats.getInstance().getLogger().info("sending full Metrics! You can deactivate this in the config.yml");
    }

    private void addConnectionChart(Config.Entry mysql, Config.Entry sqlite) {
        this.addCustomChart(new SimplePie("connection_type", new Callable<String>() {
            @Override
            public String call() throws Exception {
                if (PVPStats.getInstance().config().getBoolean(mysql)) {
                    return "mySQL";
                }
                if (PVPStats.getInstance().config().getBoolean(sqlite)) {
                    return "SQLite";
                }
                return "YML";
            }
        }));
    }

    private void addChart(Config.Entry cfg, String id) {
        this.addCustomChart(new SimplePie(id, new Callable<String>() {
            @Override
            public String call() throws Exception {
                return String.valueOf(PVPStats.getInstance().config().getBoolean(cfg));
            }
        }));
    }

    @Override
    protected JsonArray calculateCharts() {
        JsonArray customCharts = new JsonArray();
        for (CustomChart customChart : charts) {
            // Add the data of the custom charts
            JsonObject chart = customChart.getRequestJsonObject();
            if (chart == null) { // If the chart is null, we skip it
                continue;
            }
            customCharts.add(chart);
        }
        return customCharts;
    }
}
