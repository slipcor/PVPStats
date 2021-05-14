package net.slipcor.pvpstats.commands;

import net.slipcor.core.CoreCommand;
import net.slipcor.core.CoreDebugger;
import net.slipcor.core.CorePlugin;
import net.slipcor.pvpstats.PVPStats;
import net.slipcor.pvpstats.yml.Language;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandCleanup extends CoreCommand {
    public CommandCleanup(CorePlugin plugin) {
        super(plugin, "pvpstats.cleanup", Language.MSG.COMMAND_ARGUMENT_COUNT_INVALID);
    }

    public static CoreDebugger debugger;

    @Override
    public void commit(CommandSender sender, String[] args) {
        debugger.i("cleaning up");
        if (!hasPerms(sender)) {
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG.NO_PERMISSION_CLEANUP.parse());
            return;
        }

        int count = PVPStats.getInstance().getSQLHandler().cleanup(sender);

        if (count > 0) {
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG.COMMAND_CLEANUP_SUCCESS.parse(String.valueOf(count)));
        } else if (count == 0) {
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG.COMMAND_CLEANUP_SKIPPED.parse());
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
    public List<String> getShort() {
        return Collections.singletonList("!clean");
    }

    @Override
    public String getShortInfo() {
        return "/pvpstats cleanup";
    }
}
