package net.slipcor.pvpstats.core;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * A YML based language system implementation allowing for placeholders
 *
 * @author slipcor
 */
public enum Language {
    HEAD_LINE("head.line", "&7---------------"),
    HEAD_HEADLINE("head.headline", "&cPVP Stats Top &7%0% &c%1%"),
    HEAD_KILLS("head.kills", "Kills"),
    HEAD_DEATHS("head.deaths", "Deaths"),
    HEAD_STREAK("head.streaks", "Streaks"),
    HEAD_RATIO("head.ratio", ""),
    HEAD_ELO("head.elo", "ELO"),

    ERROR_COMMAND_ARGUMENT("error.command_argument", "&cInvalid argument '%0%', expected %1%"),
    ERROR_DATABASE_METHOD("error.database_method", "&cInvalid database method, must be different from current!"),
    ERROR_DISPLAY_INVALID("error.display_invalid", "&cInvalid display setup - here is how to do it: https://slipcor.net/?go=mc13"),
    ERROR_INVALID_ARGUMENT_COUNT("error.invalid_argument_count", "&cInvalid number of arguments&r (%0% instead of %1%)!"),
    ERROR_INVALID_NUMBER("error.invalid_number", "&cNot a valid number: &r%0%"),
    ERROR_NOT_A_PLAYER("error.not_a_player", "&cYou are not a player. Please execute as player!"),
    ERROR_NULL_KILLS("error.null_kills", "&cBoth players are null!"),

    ERROR_CONFIG_NO_LIST_NODE("error.config_nolist", "Not a list node: &e%0%&r!"),
    ERROR_CONFIG_ADD("error.config_add", "List &e%0%&r already contains &e%1%&r!"),
    ERROR_CONFIG_REMOVE("error.config_remove", "List &e%0%&r does not contain &e%1%&r!"),
    ERROR_CONFIG_GET_GROUP("error.configget_group", "Cannot get value of group node: &e%0%&r!"),
    ERROR_CONFIG_SET_GROUP("error.configset_group", "Cannot set value to group node: &e%0%&r!"),
    ERROR_CONFIG_SET_LIST("error.configset_list", "Cannot set value to list node: &e%0%&r! Use add / remove!"),
    ERROR_CONFIG_TYPE_UNKNOWN("error.configset_typeunknown", "Unknown node type: &e%0%&r!"),
    ERROR_CONFIG_UNKNOWN("error.config_unknown", "Unknown node: &e%0%&r!"),

    INFO_FORMAT("info.format", "&c%0%: &7%1%"),
    INFO_NAME("info.name", "Name"),
    INFO_KILLS("info.kills", "Kills"),
    INFO_DEATHS("info.deaths", "Deaths"),
    INFO_MAXSTREAK("info.maxstreak", "Max Streak"),
    INFO_STREAK("info.streak", "Streak"),
    INFO_RATIO("info.ratio", "Ratio"),
    INFO_ELO("info.helo", "Elo"),
    INFO_AKILLEDB("info.a_killed_b", "%0% killed %1%"),
    INFO_PLAYERNOTFOUND("info.playernotfound", "Player not found: %0%"),
    INFO_PLAYERNOTFOUND2("info.playernotfound2", "They probably did not kill or die yet!"),

    LOG_UPDATE_DISABLED("log.updatedisabled", "Updates deactivated. Please check spigotmc.org for updates"),
    LOG_UPDATE_ENABLED("log.updateenabled", "Checking for updates..."),

    MSG_CLEANED("msg.cleaned", "Statistics successfully cleaned up! %0% entries removed!"),
    MSG_CONFIGGET("msg.configget", "Value of node &a%0%&r is: &e%1%&r"),
    MSG_CONFIGSET("msg.configset", "&a%0%&r set to &e%1%&r!"),
    MSG_CONFIGADDED("msg.configadded", "Added &e%1%&r to &a%0%&r!"),
    MSG_CONFIGREMOVED("msg.configremoved", "Removed &e%1%&r from &a%0%&r!"),
    MSG_DISPLAY_CREATED("msg.display_created", "PVP Leaderboard created at %0%!"),
    MSG_DISPLAY_COLUMN("msg.display_column", "Leaderboard is now sorted by %0%!"),
    MSG_DISPLAY_SORTEDBY("msg.display_sortedby", "sorted by"),
    MSG_DISPLAY_SORTEDCOLUMN("msg.display_sortedcolumn", "&a%0%"),
    MSG_MIGRATED("msg.migrated", "Statistics successfully migrated! %0% entries stored!"),
    MSG_MIGRATE_EMPTY("msg.migrate_empty", "No entries stored! Are you sure you selected the right migrate direction?"),
    MSG_SET("msg.set", "Set %0% of player %1% to %2%!"),
    MSG_ELO_ADDED("msg.elo.added", "You got %0% ELO points! Your score is now: %1%"),
    MSG_ELO_SUBBED("msg.elo.subbed", "You lost %0% ELO points! Your score is now: %1%"),
    MSG_NOPERMRELOAD("msg.nopermreload", "No permission to reload!"),
    MSG_NOPERMCONFIGSET("msg.nopermconfigset", "No permission to set config setting!"),
    MSG_NOPERMDEBUG("msg.nopermdebug", "No permission to debug!"),
    MSG_NOPERMMIGRATE("msg.nopermmigrate", "No permission to migrate!"),
    MSG_NOPERMPURGE("msg.nopermpurge", "No permission to purge!"),
    MSG_NOPERMSET("msg.nopermset", "No permission to set!"),
    MSG_NOPERMWIPE("msg.nopermwipe", "No permission to wipe!"),
    MSG_NOPERMTOP("msg.nopermtop", "No permission to view top players!"),
    MSG_PREFIX("msg.prefix", "[PVP Stats] "),
    MSG_RELOADED("msg.reloaded", "Configuration reloaded!"),
    MSG_UPDATE("msg.update", "Update available, check spigotmc.org!"),
    MSG_UPDATED("msg.updated", "Update installed! please restart the server!"),
    MSG_WIPED("msg.wiped", "Statistics wiped!"),
    MSG_WIPEDFOR("msg.wipedfor", "Statistics wiped for %0%!");

    private final String sDefault;
    private final String node;
    private String value = null;

    Language(final String node, final String content) {
        this.node = node;
        sDefault = content;
    }

    private void override(final String value) {
        this.value = value;
    }

    /**
     * @return the node colorized content
     */
    @Override
    public String toString() {
        final String result = (value == null) ? sDefault : value;
        return ChatColor.translateAlternateColorCodes('&', result);
    }

    /**
     * Return a colorized string with replaced placeholders
     *
     * @param args the placeholders to replace
     * @return the replaced colorized string
     */
    public String toString(String... args) {
        String result = value == null ? sDefault : value;
        for (int pos = 0; pos < args.length; pos++) {
            result = result.replace("%" + pos + "%", args[pos]);
        }
        return ChatColor.translateAlternateColorCodes('&', result);
    }

    /**
     * Load a Configuration, possibly add defaults
     *
     * @param cfg the Configuration to access
     * @return true if the file has been updated and should be saved
     */
    public static boolean load(final FileConfiguration cfg) {
        boolean changed = false;

        for (Language lang : Language.values()) {
            if (cfg.get(lang.node) == null) {
                cfg.set(lang.node, lang.sDefault);
                changed = true;
            } else {
                lang.override(cfg.getString(lang.node));
            }
        }

        return changed;
    }
}
