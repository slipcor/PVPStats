package net.slipcor.pvpstats.yml;

import net.slipcor.core.CoreLanguage;
import net.slipcor.core.CorePlugin;
import net.slipcor.core.LanguageEntry;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class Language extends CoreLanguage {
    public Language(CorePlugin plugin) {
        super(plugin);
    }

    @Override
    protected LanguageEntry[] getAllNodes() {
        return MSG.values();
    }

    @Override
    public String load(String fileName) {
        String error = super.load(fileName);

        if (error != null) {
            return error;
        }

        final File configFile = new File(plugin.getDataFolder(), fileName + ".yml");

        final YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(configFile);
        } catch (final Exception e) {
            e.printStackTrace();
            return "Error when loading language file:\n" + e.getMessage();
        }
        try {
            config.set("version", 1.1103);

            config.save(configFile);
        } catch (final Exception e) {
            e.printStackTrace();
            return "Error when saving language file:\n" + e.getMessage();
        }
        return null;
    }

    /**
     * A YML based language system implementation allowing for placeholders
     *
     * @author slipcor
     */
    public enum MSG implements LanguageEntry {

        COMMAND_ARGUMENT_COUNT_INVALID("&cInvalid number of arguments&r (%0% instead of %1%)!"),
        COMMAND_ARGUMENT_INVALID_NUMBER("&cNot a valid number: &r%0%"),
        COMMAND_ARGUMENT_INVALID_TYPE("&cInvalid argument '%0%', expected %1%"),

        COMMAND_CLEANUP_SKIPPED("Player list had no duplicates!"),
        COMMAND_CLEANUP_SUCCESS("Player list is clear! %0% entries removed!"),

        COMMAND_CONFIG_NO_LIST("Not a list node: &e%0%&r!"),
        COMMAND_CONFIG_UNKNOWN_NODE("Unknown node: &e%0%&r!"),
        COMMAND_CONFIG_UNKNOWN_TYPE("Unknown node type: &e%0%&r!"),

        COMMAND_CONFIG_ADD_SKIPPED("List &e%0%&r already contains &e%1%&r!"),
        COMMAND_CONFIG_ADD_SUCCESS("Added &e%1%&r to &a%0%&r!"),
        COMMAND_CONFIG_GET_GROUP_IMPOSSIBLE("Cannot get value of group node: &e%0%&r!"),
        COMMAND_CONFIG_GET_SECRET_FORBIDDEN( "Cannot show node: &e%0%&r!"),
        COMMAND_CONFIG_GET_SUCCESS("Value of node &a%0%&r is: &e%1%&r"),
        COMMAND_CONFIG_REMOVE_SKIPPED("List &e%0%&r does not contain &e%1%&r!"),
        COMMAND_CONFIG_REMOVE_SUCCESS("Removed &e%1%&r from &a%0%&r!"),
        COMMAND_CONFIG_SET_GROUP_IMPOSSIBLE("Cannot set value to group node: &e%0%&r!"),
        COMMAND_CONFIG_SET_LIST_IMPOSSIBLE("Cannot set value to list node: &e%0%&r! Use add / remove!"),
        COMMAND_CONFIG_SET_SUCCESS("&a%0%&r set to &e%1%&r!"),

        COMMAND_MIGRATE_DATABASE_METHOD_INVALID("&cInvalid database method, must be different from current!"),
        COMMAND_MIGRATE_SKIPPED("No entries stored! Are you sure you selected the right migrate direction?"),
        COMMAND_MIGRATE_SUCCESS("Statistics successfully migrated! %0% entries stored!"),

        COMMAND_PLAYER_NOT_FOUND("Player not found: %0%"),
        COMMAND_PLAYER_NOT_FOUND_EXPLANATION("They probably did not kill or die yet!"),

        COMMAND_PURGE_SUCCESS("Statistics successfully cleaned up! %0% entries removed!"),
        COMMAND_RELOAD_SUCCESS("Configuration reloaded!"),
        COMMAND_SET_SUCCESS("Set %0% of player %1% to %2%!"),
        COMMAND_WIPE_GLOBAL_SUCCESS("Statistics wiped!"),
        COMMAND_WIPE_PLAYER_SUCCESS("Statistics wiped for %0%!"),

        DISPLAY_SETUP_INVALID("&cInvalid display setup - here is how to do it: https://slipcor.net/?go=mc13"),
        DISPLAY_SORTED_BY("Leaderboard is now sorted by %0%!"),
        DISPLAY_SUCCESSFUL("PVP Leaderboard created at %0%!"),

        LOG_UPDATE_DISABLED("Updates deactivated. Please check spigotmc.org for updates"),
        LOG_UPDATE_ENABLED("Checking for updates..."),

        MESSAGE_PREFIX("[PVP Stats] "),

        NO_PERMISSION_CLEANUP("No permission to clean up!"),
        NO_PERMISSION_CONFIG_SET("No permission to set config setting!"),
        NO_PERMISSION_DEBUG("No permission to debug!"),
        NO_PERMISSION_MIGRATE("No permission to migrate!"),
        NO_PERMISSION_PURGE("No permission to purge!"),
        NO_PERMISSION_RELOAD("No permission to reload!"),
        NO_PERMISSION_SET("No permission to set!"),
        NO_PERMISSION_SHOW("No permission to see stats!"),
        NO_PERMISSION_TOP("No permission to view top players!"),
        NO_PERMISSION_WIPE("No permission to wipe!"),

        PLAYER_ELO_ADDED("You got %0% ELO points! Your score is now: %1%"),
        PLAYER_ELO_REMOVED("You lost %0% ELO points! Your score is now: %1%"),

        PLAYER_NO_STATS("You do not have stats!"),

        STATISTIC_FORMAT_NUMBER("%0%: %1%"),
        STATISTIC_FORMAT_VALUE("&c%0%: &7%1%"),

        STATISTIC_HEADLINE_TOP("&cPVP Stats Top &7%0% &c%1%"),
        STATISTIC_HEADLINE_FLOP("&cPVP Stats Flop &7%0% &c%1%"),
        STATISTIC_HEADLINE_ELO("ELO"),
        STATISTIC_HEADLINE_DEATHS("Deaths"),
        STATISTIC_HEADLINE_KILLS("Kills"),
        STATISTIC_HEADLINE_RATIO(""),
        STATISTIC_HEADLINE_STREAK("Streaks"),

        STATISTIC_SEPARATOR("&7---------------"),

        STATISTIC_VALUE_DEATHS("Deaths"),
        STATISTIC_VALUE_ELO("Elo"),
        STATISTIC_VALUE_KILLS("Kills"),
        STATISTIC_VALUE_MAX_STREAK("Max Streak"),
        STATISTIC_VALUE_NAME("Name"),
        STATISTIC_VALUE_RATIO("Ratio"),
        STATISTIC_VALUE_STREAK("Streak"),

        SIGN_SORTED_BY("sorted by"),
        SIGN_SORTED_COLUMN("&a%0%");

        private final String node;
        private String value;

        MSG(final String value) {
            this.node = this.name().toLowerCase().replace("_", "-");
            this.value = value;
        }

        /**
         * Return a colorized string with replaced placeholders
         *
         * @param args the placeholders to replace
         * @return the replaced colorized string
         */
        @Override
        public String parse(String... args) {
            String result = toString();
            for (int pos = 0; pos < args.length; pos++) {
                result = result.replace("%" + pos + "%", args[pos]);
            }
            return ChatColor.translateAlternateColorCodes('&', result);
        }

        public String parse() {
            return ChatColor.translateAlternateColorCodes('&', toString());
        }

        @Override
        public String getNode() {
            return node;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public void setValue(final String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
