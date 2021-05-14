package net.slipcor.pvpstats.runnables;

import net.slipcor.core.LanguageEntry;
import net.slipcor.pvpstats.api.LeaderboardBuffer;
import net.slipcor.pvpstats.yml.Language;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class SendPlayerTop implements Runnable {
    final CommandSender sender;
    final String name;
    final int amount;
    final String displayAmount;

    static Map<String, LanguageEntry> stringToEntry = new HashMap<>();

    static {
        stringToEntry.put("LINE", Language.MSG.STATISTIC_SEPARATOR);
        stringToEntry.put("KILLS", Language.MSG.STATISTIC_HEADLINE_KILLS);
        stringToEntry.put("DEATHS", Language.MSG.STATISTIC_HEADLINE_DEATHS);
        stringToEntry.put("STREAK", Language.MSG.STATISTIC_HEADLINE_STREAK);
        stringToEntry.put("RATIO", Language.MSG.STATISTIC_HEADLINE_RATIO);
        stringToEntry.put("K-D", Language.MSG.STATISTIC_HEADLINE_RATIO);
        stringToEntry.put("ELO", Language.MSG.STATISTIC_HEADLINE_ELO);
    }

    public SendPlayerTop(CommandSender sender, String name, int amount) {
        this.sender = sender;
        this.name = name;
        this.amount = amount;
        displayAmount = String.valueOf(amount);
    }

    public SendPlayerTop(CommandSender sender, String name, int amount, String displayAmount) {
        this.sender = sender;
        this.name = name;
        this.amount = amount;
        this.displayAmount = displayAmount;
    }

    @Override
    public void run() {
        String[] top = LeaderboardBuffer.top(amount, name);
        sender.sendMessage(Language.MSG.STATISTIC_SEPARATOR.parse());
        sender.sendMessage(Language.MSG.STATISTIC_HEADLINE_TOP.parse(
                displayAmount,
                stringToEntry.get(name).parse()));
        sender.sendMessage(Language.MSG.STATISTIC_SEPARATOR.parse());

        int pos = 1;
        for (String stat : top) {
            sender.sendMessage(Language.MSG.STATISTIC_FORMAT_NUMBER.parse(String.valueOf(pos++), stat));
        }
    }
}
