package net.slipcor.pvpstats.yml;

import net.slipcor.core.CorePlugin;
import net.slipcor.core.LanguageEntry;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class LanguageMigration {
    public static void commit(CorePlugin plugin) {
        FileConfiguration cfg = new YamlConfiguration();
        try {
            File languageFile = new File(plugin.getDataFolder(), "lang.yml");

            File backup = new File(plugin.getDataFolder(), "lang_backup.yml");

            cfg.load(languageFile);
            if (cfg.getDouble("version", 1.0) > 1.10 || backup.exists()) {
                return;
            } else {
                Files.copy(languageFile.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            for (Moving m : Moving.values()) {
                cfg.set(m.entry.getNode(), cfg.get(m.node));
            }

            cfg.set("head", null);
            cfg.set("error", null);
            cfg.set("info", null);
            cfg.set("msg", null);
            cfg.set("log", null);

            //                 1.10.4
            cfg.set("version", 1.1004);

            cfg.save(languageFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public enum Moving {

        COMMAND_ARGUMENT_INVALID_TYPE("error.command_argument", Language.MSG.COMMAND_ARGUMENT_INVALID_TYPE),
        COMMAND_ARGUMENT_COUNT_INVALID("error.invalid_argument_count", Language.MSG.COMMAND_ARGUMENT_COUNT_INVALID),
        COMMAND_ARGUMENT_INVALID_NUMBER("error.invalid_number", Language.MSG.COMMAND_ARGUMENT_INVALID_NUMBER),

        COMMAND_CLEANUP_SKIPPED("msg.notcleanedup", Language.MSG.COMMAND_CLEANUP_SKIPPED),
        COMMAND_CLEANUP_SUCCESS("msg.cleanedup", Language.MSG.COMMAND_CLEANUP_SUCCESS),
        COMMAND_CONFIG_ADD_SKIPPED("error.config_add", Language.MSG.COMMAND_CONFIG_ADD_SKIPPED),
        COMMAND_CONFIG_ADD_SUCCESS("msg.configadded", Language.MSG.COMMAND_CONFIG_ADD_SUCCESS),
        COMMAND_CONFIG_GET_GROUP_IMPOSSIBLE("error.configget_group", Language.MSG.COMMAND_CONFIG_GET_GROUP_IMPOSSIBLE),
        COMMAND_CONFIG_GET_SECRET_FORBIDDEN("error.config_secret", Language.MSG.COMMAND_CONFIG_GET_SECRET_FORBIDDEN),
        COMMAND_CONFIG_GET_SUCCESS("msg.configget", Language.MSG.COMMAND_CONFIG_GET_SUCCESS),
        COMMAND_CONFIG_NO_LIST("error.config_nolist", Language.MSG.COMMAND_CONFIG_NO_LIST),
        COMMAND_CONFIG_REMOVE_SKIPPED("error.config_remove", Language.MSG.COMMAND_CONFIG_REMOVE_SKIPPED),
        COMMAND_CONFIG_REMOVE_SUCCESS("msg.configremoved", Language.MSG.COMMAND_CONFIG_REMOVE_SUCCESS),
        COMMAND_CONFIG_SET_GROUP_IMPOSSIBLE("error.configset_group", Language.MSG.COMMAND_CONFIG_SET_GROUP_IMPOSSIBLE),
        COMMAND_CONFIG_SET_LIST_IMPOSSIBLE("error.configset_list", Language.MSG.COMMAND_CONFIG_SET_LIST_IMPOSSIBLE),
        COMMAND_CONFIG_SET_SUCCESS("msg.configset", Language.MSG.COMMAND_CONFIG_SET_SUCCESS),
        COMMAND_CONFIG_UNKNOWN_NODE("error.config_unknown", Language.MSG.COMMAND_CONFIG_UNKNOWN_NODE),
        COMMAND_CONFIG_UNKNOWN_TYPE("error.configset_typeunknown", Language.MSG.COMMAND_CONFIG_UNKNOWN_TYPE),
        COMMAND_MIGRATE_DATABASE_METHOD_INVALID("error.database_method", Language.MSG.COMMAND_MIGRATE_DATABASE_METHOD_INVALID),
        COMMAND_MIGRATE_SKIPPED("msg.migrate_empty", Language.MSG.COMMAND_MIGRATE_SKIPPED),
        COMMAND_MIGRATE_SUCCESS("msg.migrated", Language.MSG.COMMAND_MIGRATE_SUCCESS),
        COMMAND_PLAYER_NOT_FOUND("info.playernotfound", Language.MSG.COMMAND_PLAYER_NOT_FOUND),
        COMMAND_PLAYER_NOT_FOUND_EXPLANATION("info.playernotfound2", Language.MSG.COMMAND_PLAYER_NOT_FOUND_EXPLANATION),
        COMMAND_PURGE_SUCCESS("msg.cleaned", Language.MSG.COMMAND_PURGE_SUCCESS),
        COMMAND_RELOAD_SUCCESS("msg.reloaded", Language.MSG.COMMAND_RELOAD_SUCCESS),
        COMMAND_SET_SUCCESS("msg.set", Language.MSG.COMMAND_SET_SUCCESS),
        COMMAND_WIPE_GLOBAL_SUCCESS("msg.wiped", Language.MSG.COMMAND_WIPE_GLOBAL_SUCCESS),
        COMMAND_WIPE_PLAYER_SUCCESS("msg.wipedfor", Language.MSG.COMMAND_WIPE_PLAYER_SUCCESS),

        DISPLAY_SETUP_INVALID("error.display_invalid", Language.MSG.DISPLAY_SETUP_INVALID),
        DISPLAY_SORTED_BY("msg.display_column", Language.MSG.DISPLAY_SORTED_BY),
        DISPLAY_SUCCESSFUL("msg.display_created", Language.MSG.DISPLAY_SUCCESSFUL),

        LOG_UPDATE_DISABLED("log.updatedisabled", Language.MSG.LOG_UPDATE_DISABLED),
        LOG_UPDATE_ENABLED("log.updateenabled", Language.MSG.LOG_UPDATE_ENABLED),

        MESSAGE_PREFIX("msg.prefix", Language.MSG.MESSAGE_PREFIX),

        NO_PERMISSION_CLEANUP("msg.nopermcleanup", Language.MSG.NO_PERMISSION_CLEANUP),
        NO_PERMISSION_CONFIG_SET("msg.nopermconfigset", Language.MSG.NO_PERMISSION_CONFIG_SET),
        NO_PERMISSION_DEBUG("msg.nopermdebug", Language.MSG.NO_PERMISSION_DEBUG),
        NO_PERMISSION_MIGRATE("msg.nopermmigrate", Language.MSG.NO_PERMISSION_MIGRATE),
        NO_PERMISSION_PURGE("msg.nopermpurge", Language.MSG.NO_PERMISSION_PURGE),
        NO_PERMISSION_RELOAD("msg.nopermreload", Language.MSG.NO_PERMISSION_RELOAD),
        NO_PERMISSION_SET("msg.nopermset", Language.MSG.NO_PERMISSION_SET),
        NO_PERMISSION_SHOW("msg.nopermshow", Language.MSG.NO_PERMISSION_SHOW),
        NO_PERMISSION_TOP("msg.nopermtop", Language.MSG.NO_PERMISSION_TOP),
        NO_PERMISSION_WIPE("msg.nopermwipe", Language.MSG.NO_PERMISSION_WIPE),

        PLAYER_ELO_ADDED("msg.elo.added", Language.MSG.PLAYER_ELO_ADDED),
        PLAYER_ELO_REMOVED("msg.elo.subbed", Language.MSG.PLAYER_ELO_REMOVED),
        PLAYER_NO_STATS("msg.nostats", Language.MSG.PLAYER_NO_STATS),

        SIGN_SORTED_BY("msg.display_sortedby", Language.MSG.SIGN_SORTED_BY),
        SIGN_SORTED_COLUMN("msg.display_sortedcolumn", Language.MSG.SIGN_SORTED_COLUMN),

        STATISTIC_FORMAT_NUMBER("info.numbers", Language.MSG.STATISTIC_FORMAT_NUMBER),
        STATISTIC_FORMAT_VALUE("info.format", Language.MSG.STATISTIC_FORMAT_VALUE),
        STATISTIC_HEADLINE_DEATHS("head.deaths", Language.MSG.STATISTIC_HEADLINE_DEATHS),
        STATISTIC_HEADLINE_ELO("head.elo", Language.MSG.STATISTIC_HEADLINE_ELO),
        STATISTIC_HEADLINE_KILLS("head.kills", Language.MSG.STATISTIC_HEADLINE_KILLS),
        STATISTIC_HEADLINE_RATIO("head.ratio", Language.MSG.STATISTIC_HEADLINE_RATIO),
        STATISTIC_HEADLINE_STREAK("head.streaks", Language.MSG.STATISTIC_HEADLINE_STREAK),
        STATISTIC_HEADLINE_TOP("head.headline", Language.MSG.STATISTIC_HEADLINE_TOP),

        STATISTIC_SEPARATOR("head.line", Language.MSG.STATISTIC_SEPARATOR),

        STATISTIC_VALUE_DEATHS("info.deaths", Language.MSG.STATISTIC_VALUE_DEATHS),
        STATISTIC_VALUE_ELO("info.helo", Language.MSG.STATISTIC_VALUE_ELO),
        STATISTIC_VALUE_KILLS("info.kills", Language.MSG.STATISTIC_VALUE_KILLS),
        STATISTIC_VALUE_MAX_STREAK("info.maxstreak", Language.MSG.STATISTIC_VALUE_MAX_STREAK),
        STATISTIC_VALUE_NAME("info.name", Language.MSG.STATISTIC_VALUE_NAME),
        STATISTIC_VALUE_RATIO("info.ratio", Language.MSG.STATISTIC_VALUE_RATIO),
        STATISTIC_VALUE_STREAK("info.streak", Language.MSG.STATISTIC_VALUE_STREAK);

        private final String node;
        final LanguageEntry entry;

        Moving(final String node, final LanguageEntry entry) {
            this.node = node;
            this.entry = entry;
        }
    }
}
