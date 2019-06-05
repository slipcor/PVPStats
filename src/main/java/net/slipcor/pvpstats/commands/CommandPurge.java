package net.slipcor.pvpstats.commands;

import net.slipcor.pvpstats.Language;
import net.slipcor.pvpstats.PSMySQL;
import net.slipcor.pvpstats.PVPStats;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CommandPurge extends AbstractCommand {
    public CommandPurge() {
        super(new String[]{"pvpstats.purge"});
    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            sender.sendMessage(Language.MSG_NOPERMPURGE.toString());
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
                final int count = PSMySQL.purgeKillStats(days);
                PVPStats.getInstance().sendPrefixed(sender, Language.MSG_CLEANED.toString(String.valueOf(count)));
            } else if (args[1].equalsIgnoreCase("standard")) {
                final int count = PSMySQL.purgeStats(days);
                PVPStats.getInstance().sendPrefixed(sender, Language.MSG_CLEANED.toString(String.valueOf(count)));
            } else if (args[1].equalsIgnoreCase("both")) {
                final int count = PSMySQL.purgeKillStats(days) + PSMySQL.purgeStats(days);
                PVPStats.getInstance().sendPrefixed(sender, Language.MSG_CLEANED.toString(String.valueOf(count)));
            } else {
                PVPStats.getInstance().sendPrefixed(sender, "/pvpstats purge [specific | standard | both] [days]");
            }
        } else {
            PVPStats.getInstance().sendPrefixed(sender, "/pvpstats purge [specific | standard | both] [days]");
        }
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("purge");
    }

    @Override
    public String getName() {
        return getClass().getName();
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
