package net.slipcor.pvpstats.commands;

import net.slipcor.pvpstats.Language;
import net.slipcor.pvpstats.PSMySQL;
import net.slipcor.pvpstats.PVPStats;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CommandWipe extends AbstractCommand {
    public CommandWipe() {
        super(new String[]{"pvpstats.wipe"});
    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            sender.sendMessage(Language.MSG_NOPERMWIPE.toString());
            return;
        }

        if (args.length < 2) {
            PSMySQL.wipe(null);
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG_WIPED.toString());
        } else {
            PSMySQL.wipe(args[1]);
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG_WIPED.toString(args[1]));
        }
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("wipe");
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!w");
    }

    @Override
    public String getShortInfo() {
        return "/pvpstats wipe {player} - wipe all/player statistics";
    }
}
