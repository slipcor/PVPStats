package net.slipcor.pvpstats.commands;

import net.slipcor.core.CoreCommand;
import net.slipcor.core.CorePlugin;
import net.slipcor.pvpstats.PVPStats;
import net.slipcor.pvpstats.api.DatabaseAPI;
import net.slipcor.pvpstats.display.SignDisplay;
import net.slipcor.pvpstats.yml.Language;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandReload extends CoreCommand {
    public CommandReload(CorePlugin plugin) {
        super(plugin, "pvpstats.reload", Language.MSG.COMMAND_ARGUMENT_COUNT_INVALID);
    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG.NO_PERMISSION_RELOAD.parse());
            return;
        }

        PVPStats.getInstance().reloadConfig();
        PVPStats.getInstance().loadConfig();
        PVPStats.getInstance().loadCommands();
        String error = PVPStats.getInstance().loadLanguage();
        if (error != null) {
            PVPStats.getInstance().sendPrefixed(sender, ChatColor.RED + error);
            return;
        }
        PVPStats.getInstance().reloadStreaks();
        PVPStats.getInstance().sendPrefixed(sender, Language.MSG.COMMAND_RELOAD_SUCCESS.parse());

        DatabaseAPI.refresh();

        SignDisplay.loadAllDisplays();
    }

    public List<String> completeTab(String[] args) {
        return new ArrayList<>(); // we have no arguments
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("reload");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!r");
    }

    @Override
    public String getShortInfo() {
        return "/pvpstats reload - reload the configs";
    }
}
