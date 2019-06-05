package net.slipcor.pvpstats.commands;

import net.slipcor.pvpstats.Language;
import net.slipcor.pvpstats.PSMySQL;
import net.slipcor.pvpstats.PVPStats;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CommandTop extends AbstractCommand {
    public CommandTop() {
        super(new String[]{"pvpstats.top"});
    }

    @Override
    public void commit(final CommandSender sender, final String[] args) {
        if (!hasPerms(sender)) {
            sender.sendMessage(Language.MSG_NOPERMTOP.toString());
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

                    class RunLater implements Runnable {
                        final String name;
                        final int amount;

                        RunLater(String name, int amount) {
                            this.name = name;
                            this.amount = amount;
                        }

                        @Override
                        public void run() {
                            String[] top = PSMySQL.top(amount, name);
                            sender.sendMessage(Language.HEAD_LINE.toString());
                            sender.sendMessage(Language.HEAD_HEADLINE.toString(
                                    String.valueOf(amount),
                                    Language.valueOf("HEAD_" + name).toString()));
                            sender.sendMessage(Language.HEAD_LINE.toString());


                            int pos = 1;
                            for (String stat : top) {
                                sender.sendMessage(pos++ + ": " + stat);
                            }
                        }

                    }

                    if (args[1].equals("kills")) {
                        Bukkit.getScheduler().runTaskAsynchronously(PVPStats.getInstance(), new RunLater("KILLS", amount));
                    } else if (args[1].equals("deaths")) {
                        Bukkit.getScheduler().runTaskAsynchronously(PVPStats.getInstance(), new RunLater("DEATHS", amount));
                    } else if (args[1].equals("streak")) {
                        Bukkit.getScheduler().runTaskAsynchronously(PVPStats.getInstance(), new RunLater("STREAK", amount));
                    } else if (args[1].equalsIgnoreCase("elo")) {
                        Bukkit.getScheduler().runTaskAsynchronously(PVPStats.getInstance(), new RunLater("ELO", amount));
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
                int count = legacyTop == 0 ? 10 : Integer.parseInt(args[0]);
                if (count > 20) {
                    count = 20;
                }
                if (legacyTop == 0) {
                    args[0] = String.valueOf(count);
                }
                class RunLater implements Runnable {
                    int count;

                    RunLater(int i) {
                        count = i;
                    }

                    @Override
                    public void run() {
                        final String[] top = PSMySQL.top(count, "K-D");
                        sender.sendMessage(Language.HEAD_LINE.toString());
                        sender.sendMessage(Language.HEAD_HEADLINE.toString(
                                args[0],
                                Language.HEAD_RATIO.toString()));
                        sender.sendMessage(Language.HEAD_LINE.toString());
                        int pos = 1;
                        for (String stat : top) {
                            sender.sendMessage(String.valueOf(pos++) + ": " + stat);
                        }
                    }

                }
                Bukkit.getScheduler().runTaskAsynchronously(PVPStats.getInstance(), new RunLater(count));

                return;
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("top");
    }

    @Override
    public String getName() {
        return getClass().getName();
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
