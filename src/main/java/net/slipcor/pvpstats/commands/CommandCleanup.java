package net.slipcor.pvpstats.commands;

import net.slipcor.pvpstats.PVPStats;
import net.slipcor.pvpstats.classes.Debugger;
import net.slipcor.pvpstats.core.Language;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandCleanup extends AbstractCommand {
    public CommandCleanup() {
        super(new String[]{"pvpstats.cleanup"});
    }

    static Debugger debugger = new Debugger(11);

    @Override
    public void commit(CommandSender sender, String[] args) {
        debugger.i("cleaning up");
        if (!hasPerms(sender)) {
            sender.sendMessage(Language.MSG_NOPERMCLEANUP.toString());
            return;
        }

        int count = PVPStats.getInstance().getSQLHandler().cleanup(sender);

        if (count > 0) {
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG_CLEANED.toString(String.valueOf(count)));
        } else if (count == 0) {
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG_NOTCLEANED.toString());
        }
        // else we sent an error
    }

    public List<String> completeTab(String[] args) {
        return new ArrayList<>();
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("cleanup");
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!clean");
    }

    @Override
    public String getShortInfo() {
        return "/pvpstats cleanup";
    }
}
