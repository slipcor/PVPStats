package net.slipcor.pvpstats.commands;

import net.slipcor.pvpstats.PVPStats;
import net.slipcor.pvpstats.classes.Debugger;
import net.slipcor.pvpstats.core.Language;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandDebug extends AbstractCommand {
    public CommandDebug() {
        super(new String[]{"pvpstats.debug"});
    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG_NOPERMDEBUG.toString());
            return;
        }
        if (!argCountValid(sender, args, new Integer[]{2})) {
            return;
        }
        Debugger.destroy();
        if (args.length > 1) {
            PVPStats.getInstance().getConfig().set("debug", args[1]);
        }

        Debugger.load(PVPStats.getInstance(), sender);
    }

    public List<String> completeTab(String[] args) {
        List<String> results = new ArrayList<>();

        if (args.length < 2 || args[1].equals("")) {
            // list first argument possibilities
            results.add("on");
            results.add("off");
            results.add("all");
            results.add("none");
            results.add("true");
            results.add("false");
            return results;
        }

        if (args.length > 2) {
            return results; // don't go too far!
        }

        // we started typing!
        addIfMatches(results, "on", args[1].toLowerCase());
        addIfMatches(results, "off", args[1].toLowerCase());
        addIfMatches(results, "all", args[1].toLowerCase());
        addIfMatches(results, "none", args[1].toLowerCase());
        addIfMatches(results, "true", args[1].toLowerCase());
        addIfMatches(results, "false", args[1].toLowerCase());

        return results;
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("debug");
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!d");
    }

    @Override
    public String getShortInfo() {
        return "/pvpstats debug [on/off] - activate or deactivate debugging";
    }
}
