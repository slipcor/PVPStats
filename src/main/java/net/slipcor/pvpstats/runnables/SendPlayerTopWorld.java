package net.slipcor.pvpstats.runnables;

import net.slipcor.core.LanguageEntry;
import net.slipcor.pvpstats.api.LeaderboardBuffer;
import net.slipcor.pvpstats.yml.Language;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class SendPlayerTopWorld implements Runnable {
    final CommandSender sender;
    final String type;
    final int amount;
    final int days;
    final String displayAmount;
    final World world;

    static Map<String, LanguageEntry> stringToEntry = new HashMap<>();

    static {
        stringToEntry.put("LINE", Language.MSG.STATISTIC_SEPARATOR);
        stringToEntry.put("KILLS", Language.MSG.STATISTIC_HEADLINE_KILLS);
        stringToEntry.put("DEATHS", Language.MSG.STATISTIC_HEADLINE_DEATHS);
        stringToEntry.put("RATIO", Language.MSG.STATISTIC_HEADLINE_RATIO);
        stringToEntry.put("K-D", Language.MSG.STATISTIC_HEADLINE_RATIO);
    }

    public SendPlayerTopWorld(CommandSender sender, String type, World world, int amount, int days) {
        this (sender, type, world, amount, days, String.valueOf(amount));
    }

    public SendPlayerTopWorld(CommandSender sender, String type, World world, int amount, int days, String displayAmount) {
        this.sender = sender;
        this.type = type;
        this.world = world;
        this.amount = amount;
        this.days = days;
        this.displayAmount = displayAmount;
    }

    @Override
    public void run() {
        String[] top = LeaderboardBuffer.topWorld(amount, type, world.getName(), days);
        sender.sendMessage(Language.MSG.STATISTIC_SEPARATOR.parse());
        sender.sendMessage(Language.MSG.STATISTIC_HEADLINE_TOPWORLD.parse(
                displayAmount,
                stringToEntry.get(type).parse(),
                world.getName()));
        sender.sendMessage(Language.MSG.STATISTIC_SEPARATOR.parse());

        int pos = 1;
        for (String stat : top) {
            sender.sendMessage(Language.MSG.STATISTIC_FORMAT_NUMBER.parse(String.valueOf(pos++), stat));
        }
    }
}
