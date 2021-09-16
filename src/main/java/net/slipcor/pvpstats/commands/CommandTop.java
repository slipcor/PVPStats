package net.slipcor.pvpstats.commands;

import net.slipcor.core.CoreCommand;
import net.slipcor.core.CorePlugin;
import net.slipcor.pvpstats.PVPStats;
import net.slipcor.pvpstats.runnables.SendPlayerTop;
import net.slipcor.pvpstats.yml.Language;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandTop extends CoreCommand {
    public CommandTop(CorePlugin plugin) {
        super(plugin, "pvpstats.top", Language.MSG.COMMAND_ARGUMENT_COUNT_INVALID);
    }

    @Override
    public void commit(final CommandSender sender, final String[] args) {
        if (!hasPerms(sender)) {
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG.NO_PERMISSION_TOP.parse());
            return;
        }

        int legacyTop = 0;

        try {
            legacyTop = Integer.parseInt(args[0]);
        } catch (Exception e) {

        }

        if ((args[0].equals("top") || legacyTop > 0)) {

            if (args.length > 1) {
                int amount = -1;

                try {
                    amount = Integer.parseInt(args[1]);
                } catch (Exception e) {


                    if (args.length > 2) {
                        // /pvpstats top [type] [amount] - show the top [amount] players of the type
                        try {
                            amount = Integer.parseInt(args[2]);
                        } catch (Exception e2) {
                            amount = 10;
                        }
                    }

                    //   /pvpstats top [type] - show the top 10 players of the type
                    if (amount == -1) {
                        amount = 10;
                    }

                    int offset = 0;

                    if (args.length > 3) {
                        // /pvpstats top [type] [amount] [page] - show the top [amount] players of [type], page number [page]
                        try {
                            offset = 10 * (Integer.parseInt(args[3])-1);
                        } catch (Exception ignored) {
                        }
                    }
                    offset = Math.max(0, offset);

                    if (args[1].equalsIgnoreCase("kills")) {
                        Bukkit.getScheduler().runTaskAsynchronously(PVPStats.getInstance(), new SendPlayerTop(sender, "KILLS", amount, offset));
                    } else if (args[1].equalsIgnoreCase("deaths")) {
                        Bukkit.getScheduler().runTaskAsynchronously(PVPStats.getInstance(), new SendPlayerTop(sender, "DEATHS", amount, offset));
                    } else if (args[1].equalsIgnoreCase("streak")) {
                        Bukkit.getScheduler().runTaskAsynchronously(PVPStats.getInstance(), new SendPlayerTop(sender, "STREAK", amount, offset));
                    } else if (args[1].equalsIgnoreCase("elo")) {
                        Bukkit.getScheduler().runTaskAsynchronously(PVPStats.getInstance(), new SendPlayerTop(sender, "ELO", amount, offset));
                    } else if (args[1].equalsIgnoreCase("ratio")) {
                        Bukkit.getScheduler().runTaskAsynchronously(PVPStats.getInstance(), new SendPlayerTop(sender, "K-D", amount, offset));
                    } else {
                        return;
                    }

                    return;
                }
                //   /pvpstats top [amount] - show the top [amount] players (K-D)
                args[0] = args[1];
                legacyTop = 1;
            }

            // /pvpstats [amount] - show the top [amount] players (K-D)
            try {
                // legacytop == 0 -->
                int count = legacyTop == 0 ? 10 : Integer.parseInt(args[0]);
                if (legacyTop == 0) {
                    args[0] = String.valueOf(count);
                }
                int offset = 0;
                if (args.length > 2) {
                    // we did offset the arguments for backwards compatibility
                    // /pvpstats [amount] [amount] [page]
                    offset = 10 * (Integer.parseInt(args[2]) - 1);
                }
                Bukkit.getScheduler().runTaskAsynchronously(PVPStats.getInstance(), new SendPlayerTop(sender, "K-D", count, args[0], offset));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public List<String> completeTab(String[] args) {
        List<String> results = new ArrayList<>();

        if (args.length < 2 || args[1].equals("")) {
            // list first argument possibilities
            results.add("kills");
            results.add("deaths");
            results.add("streak");
            results.add("elo");
            results.add("ratio");
            return results;
        }

        if (args.length > 2) {
            return results; // don't go too far!
        }

        // we started typing!
        addIfMatches(results, "kills", args[1].toLowerCase());
        addIfMatches(results, "deaths", args[1].toLowerCase());
        addIfMatches(results, "streak", args[1].toLowerCase());
        addIfMatches(results, "elo", args[1].toLowerCase());
        addIfMatches(results, "ratio", args[1].toLowerCase());

        return results;
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("top");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!t");
    }

    @Override
    public String getShortInfo() {
        return "/pvpstats top [amount] - show the top [amount] players (K-D)\n" +
                "/pvpstats top [type] - show the top 10 players of the type\n" +
                "/pvpstats top [type] [amount] - show the top [amount] players of the type";
    }
}
