package net.slipcor.pvpstats.runnables;

import net.slipcor.pvpstats.api.DatabaseAPI;
import net.slipcor.pvpstats.core.Language;
import org.bukkit.command.CommandSender;

public class SendPlayerTop implements Runnable {
    final CommandSender sender;
    final String name;
    final int amount;
    final String displayAmount;

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
        String[] top = DatabaseAPI.top(amount, name);
        sender.sendMessage(Language.HEAD_LINE.toString());
        sender.sendMessage(Language.HEAD_HEADLINE.toString(
                displayAmount,
                Language.valueOf("HEAD_" + name).toString()));
        sender.sendMessage(Language.HEAD_LINE.toString());


        int pos = 1;
        for (String stat : top) {
            sender.sendMessage(pos++ + ": " + stat);
        }
    }
}
