package net.slipcor.pvpstats.commands;

import net.slipcor.pvpstats.PVPStats;
import net.slipcor.pvpstats.api.DatabaseAPI;
import net.slipcor.pvpstats.core.Language;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandCleanup extends AbstractCommand {
    public CommandCleanup() {
        super(new String[]{"pvpstats.cleanup"});
    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            sender.sendMessage(Language.MSG_NOPERMCLEANUP.toString());
            return;
        }

        final int count = DatabaseAPI.clean();
        PVPStats.getInstance().sendPrefixed(sender, Language.MSG_CLEANED.toString(String.valueOf(count)));

        DatabaseAPI.refresh();
    }

    public List<String> completeTab(String[] args) {
        return new ArrayList<>(); // we have no arguments
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
        return Collections.singletonList("!c");
    }

    @Override
    public String getShortInfo() {
        return "/pvpstats cleanup - clear statistics of multiple players with the same name";
    }
}
