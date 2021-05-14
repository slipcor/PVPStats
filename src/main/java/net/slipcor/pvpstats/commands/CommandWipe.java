package net.slipcor.pvpstats.commands;

import net.slipcor.core.CoreCommand;
import net.slipcor.core.CorePlugin;
import net.slipcor.pvpstats.PVPStats;
import net.slipcor.pvpstats.api.DatabaseAPI;
import net.slipcor.pvpstats.classes.PlayerHandler;
import net.slipcor.pvpstats.yml.Language;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandWipe extends CoreCommand {
    public CommandWipe(CorePlugin plugin) {
        super(plugin, "pvpstats.wipe", Language.MSG.COMMAND_ARGUMENT_COUNT_INVALID);
    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG.NO_PERMISSION_WIPE.parse());
            return;
        }

        if (args.length < 2) {
            DatabaseAPI.wipe(null);
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG.COMMAND_WIPE_GLOBAL_SUCCESS.parse());
        } else {
            OfflinePlayer player =  PlayerHandler.findPlayer(args[1]);

            if (player == null) {
                PVPStats.getInstance().sendPrefixed(sender, Language.MSG.COMMAND_PLAYER_NOT_FOUND.parse(args[1]));
                return;
            }

            DatabaseAPI.wipe(player.getUniqueId());
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG.COMMAND_WIPE_PLAYER_SUCCESS.parse(args[1]));
        }

        DatabaseAPI.refresh();
    }

    public List<String> completeTab(String[] args) {
        List<String> results = new ArrayList<>();

        if (args.length < 2 || args[1].equals("")) {
            // list first argument possibilities
            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                results.add(p.getName());
            }
            return results;
        }

        if (args.length > 2) {
            return results; // don't go too far!
        }

        // we started typing!
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            addIfMatches(results, p.getName(), args[1]);
        }
        return results;
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("wipe");
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
