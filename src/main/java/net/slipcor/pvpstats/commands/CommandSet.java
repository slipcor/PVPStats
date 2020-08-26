package net.slipcor.pvpstats.commands;

import net.slipcor.pvpstats.api.DatabaseAPI;
import net.slipcor.pvpstats.api.PlayerStatisticsBuffer;
import net.slipcor.pvpstats.classes.PlayerNameHandler;
import net.slipcor.pvpstats.core.Language;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandSet extends AbstractCommand {
    public CommandSet() {
        super(new String[]{"pvpstats.set"});
    }

    @Override
    public void commit(final CommandSender sender, final String[] args) {
        if (!hasPerms(sender)) {
            sender.sendMessage(Language.MSG_NOPERMSET.toString());
            return;
        }


        if (!argCountValid(sender, args, new Integer[]{4})) {
            return;
        }

        // /pvpstats set [player] [type] amount

        try {
            int amount = Integer.parseInt(args[3]);

            OfflinePlayer player =  PlayerNameHandler.findPlayer(args[1]);

            if (player != null && DatabaseAPI.hasEntry(player.getUniqueId())) {
                if (args[2].toLowerCase().equals("kills")) {
                    PlayerStatisticsBuffer.setKills(player.getUniqueId(), amount);
                    DatabaseAPI.setSpecificStat(player, "kills", amount);
                } else if (args[2].toLowerCase().equals("deaths")) {
                    PlayerStatisticsBuffer.setDeaths(player.getUniqueId(), amount);
                    DatabaseAPI.setSpecificStat(player, "deaths", amount);
                } else if (args[2].toLowerCase().equals("streak")) {
                    PlayerStatisticsBuffer.setStreak(player.getUniqueId(), amount);
                    DatabaseAPI.setSpecificStat(player, "streak", amount);
                } else if (args[2].toLowerCase().equals("currentstreak")) {
                    PlayerStatisticsBuffer.setMaxStreak(player.getUniqueId(), amount);
                    DatabaseAPI.setSpecificStat(player, "currentstreak", amount);
                } else if (args[2].toLowerCase().equalsIgnoreCase("elo")) {
                    PlayerStatisticsBuffer.setEloScore(player.getUniqueId(), amount);
                    DatabaseAPI.setSpecificStat(player, "elo", amount);
                } else {
                    sender.sendMessage(this.getShortInfo());
                    return;
                }

                sender.sendMessage(Language.MSG_SET.toString(args[2], args[1], String.valueOf(amount)));

                DatabaseAPI.refresh();
            } else {
                sender.sendMessage(Language.INFO_PLAYERNOTFOUND.toString(args[1]));
            }
        } catch (Exception e) {
            sender.sendMessage(Language.ERROR_INVALID_NUMBER.toString(args[3]));
        }
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

        if (args.length > 3) {
            return results; // don't go too far!
        }

        if (args.length < 3) {

            // first argument!
            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                results.add(p.getName());
            }
        } else {

            // second argument!
            addIfMatches(results, "kills", args[2].toLowerCase());
            addIfMatches(results, "deaths", args[2].toLowerCase());
            addIfMatches(results, "streak", args[2].toLowerCase());
            addIfMatches(results, "currentstreak", args[2].toLowerCase());
            addIfMatches(results, "elo", args[2].toLowerCase());
        }

        return results;
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("set");
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!st");
    }

    @Override
    public String getShortInfo() {
        return "/pvpstats set [player] [type] [amount] - set a player's [type] statistic - valid types:\nkills, deaths, streak, currentstrak, elo";
    }
}
