package net.slipcor.pvpstats.yml;

import net.slipcor.pvpstats.PVPStats;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A temporary solution to migrate config from prior to v1.3
 *
 * @deprecated as it was implemented in 2019 as a one-time measure, it will be removed in the next major version
 */
@Deprecated
public class ConfigMigration {
    public static void commit() {
        FileConfiguration cfg = PVPStats.getInstance().getConfig();

        if (cfg.getInt("MySQLport", 0) == 0) {
            // migration done!
            return;
        }

        PVPStats.getInstance().getLogger().info("Migrating Config to new system!");

        Map<String, Boolean> booleans = new LinkedHashMap<>();
        Map<String, Integer> integers = new LinkedHashMap<>();
        Map<String, String> strings = new LinkedHashMap<>();

        // first setup the nodes to read with their defaults

        booleans.put("MySQL", false);
        strings.put("MySQLhost", "host");
        integers.put("MySQLport", 3306);
        strings.put("MySQLuser", "user");
        strings.put("MySQLpass", "pw");
        strings.put("MySQLdb", "db");
        strings.put("MySQLtable", "pvpstats");
        strings.put("MySQLkilltable", "pvpkillstats");
        strings.put("MySQLoptions", "?autoReconnect=true");

        booleans.put("SQLite", false);
        strings.put("SQLitefile", "database");
        strings.put("SQLitetable", "pvpstats");
        strings.put("SQLitekilltable", "pvpkillstats");

        strings.put("FlatFiletable", "pvpstats");
        strings.put("FlatFilekilltable", "pvpkillstats");

        booleans.put("PVPArena", false);

        booleans.put("checkabuse", true);
        booleans.put("updatecheck", true);
        booleans.put("autodownload", true);
        booleans.put("tracker", true);
        booleans.put("collectprecise", true);
        booleans.put("clearonstart", true);
        booleans.put("resetkillstreakonquit", false);
        booleans.put("countregulardeaths", false);
        booleans.put("msgoverrides", false);

        integers.put("abuseseconds", -1);
        strings.put("kdcalculation", "&k/(&d+1)");

        // then update the values and unset all old nodes

        for (String key : booleans.keySet()) {
            Boolean value = booleans.get(key);
            Boolean newValue = cfg.getBoolean(key, value);
            if (newValue != value) {
                booleans.put(key, newValue);
            }
            cfg.set(key, null);
        }

        for (String key : integers.keySet()) {
            Integer value = integers.get(key);
            Integer newValue = cfg.getInt(key, value);
            if (!Objects.equals(newValue, value)) {
                integers.put(key, newValue);
            }
            cfg.set(key, null);
        }

        for (String key : strings.keySet()) {
            String value = strings.get(key);
            String newValue = cfg.getString(key, value);
            if (!newValue.equals(value)) {
                strings.put(key, newValue);
            }
            cfg.set(key, null);
        }

        // finally set the new nodes

        cfg.set(Config.Entry.MYSQL_ACTIVE.getNode(), booleans.get("MySQL"));
        cfg.set(Config.Entry.MYSQL_HOST.getNode(), strings.get("MySQLhost"));
        cfg.set(Config.Entry.MYSQL_PORT.getNode(), integers.get("MySQLport"));
        cfg.set(Config.Entry.MYSQL_USERNAME.getNode(), strings.get("MySQLuser"));
        cfg.set(Config.Entry.MYSQL_PASSWORD.getNode(), strings.get("MySQLpass"));
        cfg.set(Config.Entry.MYSQL_DATABASE.getNode(), strings.get("MySQLdb"));
        cfg.set(Config.Entry.MYSQL_TABLE.getNode(), strings.get("MySQLtable"));
        cfg.set(Config.Entry.MYSQL_KILLTABLE.getNode(), strings.get("MySQLkilltable"));
        cfg.set(Config.Entry.MYSQL_OPTIONS.getNode(), strings.get("MySQLoptions"));

        cfg.set(Config.Entry.SQLITE_ACTIVE.getNode(), booleans.get("SQLite"));
        cfg.set(Config.Entry.SQLITE_FILENAME.getNode(), strings.get("SQLitefile"));
        cfg.set(Config.Entry.SQLITE_TABLE.getNode(), strings.get("SQLitetable"));
        cfg.set(Config.Entry.SQLITE_KILLTABLE.getNode(), strings.get("SQLitekilltable"));

        cfg.set(Config.Entry.YML_TABLE.getNode(), strings.get("FlatFiletable"));
        cfg.set(Config.Entry.YML_KILLTABLE.getNode(), strings.get("FlatFilekilltable"));

        cfg.set(Config.Entry.OTHER_PVPARENA.getNode(), booleans.get("PVPArena"));

        cfg.set(Config.Entry.STATISTICS_CHECK_ABUSE.getNode(), booleans.get("checkabuse"));
        cfg.set(Config.Entry.STATISTICS_COLLECT_PRECISE.getNode(), booleans.get("collectprecise"));
        cfg.set(Config.Entry.STATISTICS_CLEAR_ON_START.getNode(), booleans.get("clearonstart"));
        cfg.set(Config.Entry.STATISTICS_RESET_KILLSTREAK_ON_QUIT.getNode(), booleans.get("resetkillstreakonquit"));
        cfg.set(Config.Entry.STATISTICS_COUNT_REGULAR_DEATHS.getNode(), booleans.get("countregulardeaths"));
        cfg.set(Config.Entry.MESSAGES_OVERRIDES.getNode(), booleans.get("msgoverrides"));

        cfg.set(Config.Entry.STATISTICS_ABUSE_SECONDS.getNode(), integers.get("abuseseconds"));
        cfg.set(Config.Entry.STATISTICS_KD_CALCULATION.getNode(), strings.get("kdcalculation"));

        File file = new File(PVPStats.getInstance().getDataFolder(), "config.yml");
        try {
            cfg.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
