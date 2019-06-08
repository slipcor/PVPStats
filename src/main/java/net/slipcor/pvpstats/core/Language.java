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
    ERROR_INVALID_ARGUMENT_COUNT("error.invalid_argument_count", "&cInvalid number of arguments&r (%0% instead of %1%)!"),
    ERROR_INVALID_NUMBER("error.invalid_number", "&cNot a valid number: &r%0%"),
    INFO_FORMAT("info.format", "&c%0%: &7%1%"),
    INFO_NAME("info.name", "Name"),
    INFO_KILLS("info.kills", "Kills"),
    INFO_DEATHS("info.deaths", "Deaths"),
    INFO_MAXSTREAK("info.maxstreak", "Max Streak"),
    INFO_STREAK("info.streak", "Streak"),
    INFO_RATIO("info.ratio", "Ratio"),
    INFO_ELO("info.helo", "Elo"),
    INFO_PLAYERNOTFOUND("info.playernotfound", "Player not found: %0%"),
    LOG_UPDATE_DISABLED("log.updatedisabled", "Updates deactivated. Please check spigotmc.org for updates"),
    LOG_UPDATE_ENABLED("log.updateenabled", "Checking for updates..."),
    MSG_CLEANED("msg.cleaned", "Statistics successfully cleaned up! %0% entries removed!"),
    MSG_SET("msg.set", "Set %0% of player %1% to %2%!"),
    MSG_ELO_ADDED("msg.elo.added", "You got %0% ELO points! Your score is now: %1%"),
    MSG_ELO_SUBBED("msg.elo.subbed", "You lost %0% ELO points! Your score is now: %1%"),
    MSG_NOPERMRELOAD("msg.nopermreload", "No permission to reload!"),
    MSG_NOPERMCLEANUP("msg.nopermcleanup", "No permission to cleanup!"),
    MSG_NOPERMDEBUG("msg.nopermdebug", "No permission to debug!"),
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
