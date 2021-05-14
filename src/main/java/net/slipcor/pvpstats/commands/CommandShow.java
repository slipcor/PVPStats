package net.slipcor.pvpstats.commands;

import net.slipcor.core.CoreCommand;
import net.slipcor.core.CorePlugin;
import net.slipcor.pvpstats.PVPStats;
import net.slipcor.pvpstats.classes.PlayerHandler;
import net.slipcor.pvpstats.runnables.SendPlayerStats;
import net.slipcor.pvpstats.yml.Language;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandShow extends CoreCommand {
    public CommandShow(CorePlugin plugin) {
        super(plugin, "pvpstats.count", Language.MSG.COMMAND_ARGUMENT_COUNT_INVALID);
    }

    @Override
    public void commit(final CommandSender sender, final String[] args) {
        if (!hasPerms(sender)) {
            return;
        }

        if (args == null || args.length < 1 || (args.length == 1 && args[0].equals("show"))) {
            // /pvpstats - show your pvp stats
            if (sender instanceof Player) {
                Bukkit.getScheduler().runTaskAsynchronously(PVPStats.getInstance(),
                        new SendPlayerStats(sender, (Player) sender));
            } else {
                PVPStats.getInstance().sendPrefixed(sender, Language.MSG.PLAYER_NO_STATS.parse());
            }
            return;
        }
        if (sender.hasPermission("pvpstats.show")) {

            // /pvpstats [player] - show player's pvp stats

            final OfflinePlayer player = PlayerHandler.findPlayer(args[1]);

            if (player == null) {
                PVPStats.getInstance().sendPrefixed(sender, Language.MSG.COMMAND_PLAYER_NOT_FOUND.parse(args[1]));
                return;
            }
            Bukkit.getScheduler().runTaskAsynchronously(
                    PVPStats.getInstance(), new SendPlayerStats(sender, player));
        } else {
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG.NO_PERMISSION_SHOW.parse());
        }
    }

    private boolean isVanished(Player p) {
        for (MetadataValue meta : p.getMetadata("vanished")) {
            if (meta.asBoolean()) {
                return true;
            }
        }
        return false;
    }

    public List<String> completeTab(String[] args) {
        List<String> results = new ArrayList<>();

        if (args.length < 2 || args[1].equals("")) {
            // list first argument possibilities
            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                if (!isVanished(p)) {
                    results.add(p.getName());
                }
            }
            return results;
        }

        if (args.length > 2) {
            return results; // don't go too far!
        }

        // we started typing!
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (!isVanished(p)) {
                addIfMatches(results, p.getName(), args[1]);
            }
        }
        return results;
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("show");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!sh");
    }

    @Override
    public String getShortInfo() {
        return "/pvpstats - show your pvp stats";
    }
}
