package net.slipcor.pvpstats.commands;

import net.slipcor.pvpstats.api.DatabaseAPI;
import net.slipcor.pvpstats.api.PlayerStatisticsBuffer;
import net.slipcor.pvpstats.core.Language;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;
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

            if (DatabaseAPI.hasEntry(args[1])) {
                try {
                    if (args[2].toLowerCase().equals("kills")) {
                        PlayerStatisticsBuffer.setKills(args[2], amount);
                        DatabaseAPI.setSpecificStat(args[2], "kills", amount);
                    } else if (args[2].toLowerCase().equals("deaths")) {
                        PlayerStatisticsBuffer.setDeaths(args[2], amount);
                        DatabaseAPI.setSpecificStat(args[2], "deaths", amount);
                    } else if (args[2].toLowerCase().equals("streak")) {
                        PlayerStatisticsBuffer.setStreak(args[2], amount);
                        DatabaseAPI.setSpecificStat(args[2], "streak", amount);
                    } else if (args[2].toLowerCase().equals("currentstreak")) {
                        PlayerStatisticsBuffer.setMaxStreak(args[2], amount);
                        DatabaseAPI.setSpecificStat(args[2], "currentstreak", amount);
                    } else if (args[2].toLowerCase().equalsIgnoreCase("elo")) {
                        PlayerStatisticsBuffer.setEloScore(args[2], amount);
                        DatabaseAPI.setSpecificStat(args[2], "elo", amount);
                    } else {
                        sender.sendMessage(this.getShortInfo());
                        return;
                    }

                    sender.sendMessage(Language.MSG_SET.toString(args[2], args[1], String.valueOf(amount)));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                sender.sendMessage(Language.INFO_PLAYERNOTFOUND.toString(args[1]));
            }
        } catch (Exception e) {
            sender.sendMessage(Language.ERROR_INVALID_NUMBER.toString(args[3]));
        }
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
