package net.slipcor.pvpstats.commands;

import net.slipcor.pvpstats.PVPStats;
import net.slipcor.pvpstats.core.Language;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CommandReload extends AbstractCommand {
    public CommandReload() {
        super(new String[]{"pvpstats.reload"});
    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            sender.sendMessage(Language.MSG_NOPERMRELOAD.toString());
            return;
        }

        PVPStats.getInstance().reloadConfig();
        PVPStats.getInstance().loadConfig();
        PVPStats.getInstance().loadLanguage();
        PVPStats.getInstance().sendPrefixed(sender, Language.MSG_RELOADED.toString());

    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("reload");
    }

    @Override
    public String getName() {
        return getClass().getName();
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
