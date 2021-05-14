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

public class CommandPurge extends CoreCommand {
    public CommandPurge(CorePlugin plugin) {
        super(plugin, "pvpstats.purge", Language.MSG.COMMAND_ARGUMENT_COUNT_INVALID);
    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG.NO_PERMISSION_PURGE.parse());
            return;
        }

        if (!argCountValid(sender, args, new Integer[]{2, 3})) {
            return;
        }

        int days = 30;

        if (args.length > 2) {
            try {
                days = Integer.parseInt(args[args.length - 1]);
            } catch (Exception e) {

            }
        }

        if (args.length > 1) {
            if (args[1].equalsIgnoreCase("specific")) {
                final int count = DatabaseAPI.purgeKillStats(days);
                PVPStats.getInstance().sendPrefixed(sender, Language.MSG.COMMAND_PURGE_SUCCESS.parse(String.valueOf(count)));
            } else if (args[1].equalsIgnoreCase("standard")) {
                final int count = DatabaseAPI.purgeStats(days);
                PVPStats.getInstance().sendPrefixed(sender, Language.MSG.COMMAND_PURGE_SUCCESS.parse(String.valueOf(count)));
            } else if (args[1].equalsIgnoreCase("both")) {
                final int count = DatabaseAPI.purgeKillStats(days) + DatabaseAPI.purgeStats(days);
                PVPStats.getInstance().sendPrefixed(sender, Language.MSG.COMMAND_PURGE_SUCCESS.parse(String.valueOf(count)));
            } else {
                PVPStats.getInstance().sendPrefixed(sender, "/pvpstats purge [specific | standard | both] [days]");
            }

            DatabaseAPI.refresh();
        } else {
            PVPStats.getInstance().sendPrefixed(sender, "/pvpstats purge [specific | standard | both] [days]");
        }
    }

    public List<String> completeTab(String[] args) {
        List<String> results = new ArrayList<>();

        if (args.length < 2 || args[1].equals("")) {
            // list first argument possibilities
            results.add("specific");
            results.add("standard");
            results.add("both");
            return results;
        }

        if (args.length > 2) {
            return results; // don't go too far!
        }

        // we started typing!
        addIfMatches(results, "specific", args[1].toLowerCase());
        addIfMatches(results, "standard", args[1].toLowerCase());
        addIfMatches(results, "both", args[1].toLowerCase());

        return results;
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("purge");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!p");
    }

    @Override
    public String getShortInfo() {
        return "/pvpstats purge [specific/standard/both] {days} - remove entries older than {days} (defaults to 30)";
    }
}
