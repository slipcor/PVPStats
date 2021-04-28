package net.slipcor.pvpstats.commands;

import net.slipcor.core.CoreCommand;
import net.slipcor.core.CorePlugin;
import net.slipcor.pvpstats.PVPStats;
import net.slipcor.pvpstats.api.DatabaseAPI;
import net.slipcor.pvpstats.yml.Language;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandMigrate extends CoreCommand {
    public CommandMigrate(CorePlugin plugin) {
        super(plugin, "pvpstats.migrate", Language.MSG.ERROR_INVALID_ARGUMENT_COUNT);
    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG.MSG_NOPERMMIGRATE.parse());
            return;
        }
        if (!argCountValid(sender, args, new Integer[]{3})) {
            return;
        }

        String method = "";

        if (args[2].toLowerCase().equals("mysql") ||
                args[2].toLowerCase().equals("sqlite") ||
                args[2].toLowerCase().equals("yml")) {
            method = args[2].toLowerCase();
        } else {
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG.ERROR_COMMAND_ARGUMENT.parse(args[2], "'mysql' or 'sqlite' or 'yml'"));
            return;
        }

        if (args[1].toLowerCase().equals("from")) {
            int result = DatabaseAPI.migrateFrom(method, sender);
            if (result >= 0) {
                if (result > 0) {
                    PVPStats.getInstance().sendPrefixed(sender, Language.MSG.MSG_MIGRATED.parse(String.valueOf(result)));
                } else {
                    PVPStats.getInstance().sendPrefixed(sender, Language.MSG.MSG_MIGRATE_EMPTY.parse());
                }
            }

            DatabaseAPI.refresh();
            return;
        } else if (args[1].toLowerCase().equals("to")) {
            int result = DatabaseAPI.migrateTo(method, sender);
            if (result >= 0) {
                if (result > 0) {
                    PVPStats.getInstance().sendPrefixed(sender, Language.MSG.MSG_MIGRATED.parse(String.valueOf(result)));
                } else {
                    PVPStats.getInstance().sendPrefixed(sender, Language.MSG.MSG_MIGRATE_EMPTY.parse());
                }
            }
            return;
        }

        PVPStats.getInstance().sendPrefixed(sender, Language.MSG.ERROR_COMMAND_ARGUMENT.parse(args[1], "'from' or 'to'"));
    }

    public List<String> completeTab(String[] args) {
        List<String> results = new ArrayList<>();

        if (args.length < 2 || args[1].equals("")) {
            // list first argument possibilities
            results.add("to");
            results.add("from");
            return results;
        }

        if (args.length > 3) {
            return results; // don't go too far!
        }

        if (args.length < 3) {
            // first argument!
            addIfMatches(results, "to", args[1].toLowerCase());
            addIfMatches(results, "from", args[1].toLowerCase());
        } else {
            // second argument!
            addIfMatches(results, "mysql", args[2].toLowerCase());
            addIfMatches(results, "sqlite", args[2].toLowerCase());
            addIfMatches(results, "yml", args[2].toLowerCase());
        }

        return results;
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("migrate");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!m");
    }

    @Override
    public String getShortInfo() {
        return "/pvpstats migrate [from|to] [mysql|sqlite|yml] - read database from / save database to other database logic";
    }
}
