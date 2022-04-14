package net.slipcor.pvpstats.commands;

import net.slipcor.core.CoreCommand;
import net.slipcor.core.CorePlugin;
import net.slipcor.pvpstats.PVPStats;
import net.slipcor.pvpstats.runnables.SendPlayerTopWorld;
import net.slipcor.pvpstats.yml.Language;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandTopWorld extends CoreCommand {
    public CommandTopWorld(CorePlugin plugin) {
        super(plugin, "pvpstats.topworld", Language.MSG.COMMAND_ARGUMENT_COUNT_INVALID);
    }

    @Override
    public void commit(final CommandSender sender, final String[] args) {
        if (!hasPerms(sender)) {
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG.NO_PERMISSION_TOPPLUS.parse());
            return;
        }

        // /pvpstats topworld [type] [world] [days]

        if (args.length > 3) {
            int days;

            try {
                days = Integer.parseInt(args[3]);
            } catch (Exception e) {
                PVPStats.getInstance().sendPrefixed(sender, Language.MSG.COMMAND_ARGUMENT_INVALID_NUMBER.parse(args[2]));
                return;
            }

            World world = (sender instanceof Player) ? ((Player) sender).getWorld() : null;
            for (World w : Bukkit.getServer().getWorlds()) {
                if (w.getName().equalsIgnoreCase(args[2])) {
                    world = w;
                    break;
                }
            }

            if (world == null) {
                PVPStats.getInstance().sendPrefixed(sender, Language.MSG.COMMAND_TOPWORLD_INVALID_WORLD.parse(args[2]));
                return;
            }

            int amount = 10;

            if (args.length > 4) {
                // /pvpstats topworld [type] [world] [days] [amount] - show the top [amount] players of the last [days] days in world [world]
                try {
                    amount = Integer.parseInt(args[4]);
                } catch (Exception e2) {
                    PVPStats.getInstance().sendPrefixed(sender, Language.MSG.COMMAND_ARGUMENT_INVALID_NUMBER.parse(args[4]));
                    amount = 10;
                }
            }

            if (args[1].equalsIgnoreCase("kills")) {
                Bukkit.getScheduler().runTaskAsynchronously(PVPStats.getInstance(), new SendPlayerTopWorld(sender, "KILLS", world, amount, days));
            } else if (args[1].equalsIgnoreCase("deaths")) {
                Bukkit.getScheduler().runTaskAsynchronously(PVPStats.getInstance(), new SendPlayerTopWorld(sender, "DEATHS", world, amount, days));
            } else if (args[1].equalsIgnoreCase("ratio")) {
                Bukkit.getScheduler().runTaskAsynchronously(PVPStats.getInstance(), new SendPlayerTopWorld(sender, "K-D", world, amount, days));
            } else {
                return;
            }
        }

    }

    public List<String> completeTab(String[] args) {
        List<String> results = new ArrayList<>();

        if (args.length < 2 || args[1].equals("")) {
            // list first argument possibilities
            results.add("kills");
            results.add("deaths");
            results.add("ratio");
            return results;
        }

        if (args.length > 2) {

            if (args.length > 3) {
                return results; // don't go too far!
            }
            for (World world :  Bukkit.getServer().getWorlds()) {
                addIfMatches(results, world.getName(), args[2].toLowerCase());
            }
            return results;
        }

        // we started typing!
        addIfMatches(results, "kills", args[1]);
        addIfMatches(results, "deaths", args[1]);
        addIfMatches(results, "ratio", args[1]);

        return results;
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("topworld");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!tw");
    }

    @Override
    public String getShortInfo() {
        return "/pvpstats topworld [type] [world] [days] - show the top 10 players of given type, in the last [days] days\n" +
                "/pvpstats topworld [type] [world] [days] [amount] - show the top [amount] players of the given type, in the last [days] days";
    }
}
