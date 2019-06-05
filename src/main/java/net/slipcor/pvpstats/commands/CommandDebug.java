package net.slipcor.pvpstats.commands;

import net.slipcor.pvpstats.Debug;
import net.slipcor.pvpstats.Language;
import net.slipcor.pvpstats.PSMySQL;
import net.slipcor.pvpstats.PVPStats;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CommandDebug extends AbstractCommand {
    public CommandDebug() {
        super(new String[]{"pvpstats.debug"});
    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            sender.sendMessage(Language.MSG_NOPERMDEBUG.toString());
            return;
        }
        if (!argCountValid(sender, args, new Integer[]{2})) {
            return;
        }
        Debug.destroy();
        if (args.length > 1) {
            PVPStats.getInstance().getConfig().set("debug", args[1]);
        }

        Debug.load(PVPStats.getInstance(), sender);
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
