package net.slipcor.pvpstats.commands;

import net.slipcor.pvpstats.PVPStats;
import net.slipcor.pvpstats.api.DatabaseAPI;
import net.slipcor.pvpstats.core.Language;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CommandMigrate extends AbstractCommand {
    public CommandMigrate() {
        super(new String[]{"pvpstats.migrate"});
    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            sender.sendMessage(Language.MSG_NOPERMMIGRATE.toString());
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
            sender.sendMessage(Language.ERROR_COMMAND_ARGUMENT.toString(args[2], "'mysql' or 'sqlite' or 'yml'"));
            return;
        }

        if (args[1].toLowerCase().equals("from")) {
            int result = DatabaseAPI.migrateFrom(method, sender);
            if (result >= 0) {
                if (result > 0) {
                    PVPStats.getInstance().sendPrefixed(sender, Language.MSG_MIGRATED.toString(String.valueOf(result)));
                } else {
                    PVPStats.getInstance().sendPrefixed(sender, Language.MSG_MIGRATE_EMPTY.toString());
                }
            }
            return;
        } else if (args[1].toLowerCase().equals("to")) {
            int result = DatabaseAPI.migrateTo(method, sender);
            if (result >= 0) {
                if (result > 0) {
                    PVPStats.getInstance().sendPrefixed(sender, Language.MSG_MIGRATED.toString(String.valueOf(result)));
                } else {
                    PVPStats.getInstance().sendPrefixed(sender, Language.MSG_MIGRATE_EMPTY.toString());
                }
            }
            return;
        }

        sender.sendMessage(Language.ERROR_COMMAND_ARGUMENT.toString(args[1], "'from' or 'to'"));

        final int count = DatabaseAPI.clean();
        PVPStats.getInstance().sendPrefixed(sender, Language.MSG_CLEANED.toString(String.valueOf(count)));
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("migrate");
    }

    @Override
    public String getName() {
        return getClass().getName();
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
