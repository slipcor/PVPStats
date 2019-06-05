package net.slipcor.pvpstats.commands;

import net.slipcor.pvpstats.PSMySQL;
import net.slipcor.pvpstats.PVPStats;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CommandShow extends AbstractCommand {
    public CommandShow() {
        super(new String[]{"pvpstats.count"});
    }

    @Override
    public void commit(final CommandSender sender, final String[] args) {
        if (!hasPerms(sender)) {
            return;
        }

        if (args == null || args.length < 1 || (args.length == 1 && args[0].equals("show"))) {
            // /pvpstats - show your pvp stats

            class TellLater implements Runnable {

                @Override
                public void run() {
                    final String[] info = PSMySQL.info(sender.getName());
                    sender.sendMessage(info);
                }

            }
            Bukkit.getScheduler().runTaskAsynchronously(PVPStats.getInstance(), new TellLater());
            return;
        }
        if (sender.hasPermission("pvpstats.top")) {

            // /pvpstats [player] - show player's pvp stats

            class TellLater implements Runnable {

                @Override
                public void run() {
                    final String[] info = PSMySQL.info(args[1]);
                    sender.sendMessage(info);
                }

            }
            Bukkit.getScheduler().runTaskAsynchronously(PVPStats.getInstance(), new TellLater());
        }
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("show");
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!s");
    }

    @Override
    public String getShortInfo() {
        return "/pvpstats - show your pvp stats";
    }
}
