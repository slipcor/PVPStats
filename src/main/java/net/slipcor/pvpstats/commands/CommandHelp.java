package net.slipcor.pvpstats.commands;

import net.slipcor.core.CoreCommand;
import net.slipcor.core.CorePlugin;
import net.slipcor.pvpstats.PVPStats;
import net.slipcor.pvpstats.yml.Language;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandHelp extends CoreCommand {
    public CommandHelp(CorePlugin plugin) {
        super(plugin, "pvpstats.help", Language.MSG.COMMAND_ARGUMENT_COUNT_INVALID);
    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG.NO_PERMISSION_HELP.parse());
            return;
        }

        for (CoreCommand command : PVPStats.getInstance().getCommands()) {
            if (command.hasPerms(sender)) {
                sender.sendMessage(ChatColor.YELLOW + command.getShortInfo());
            }
        }
    }

    public List<String> completeTab(String[] args) {
        return new ArrayList<>();
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("help");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("?");
    }

    @Override
    public String getShortInfo() {
        return "/pvpstats help - get your available commands and information about it";
    }
}
