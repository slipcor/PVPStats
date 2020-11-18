package net.slipcor.pvpstats.runnables;

import net.slipcor.pvpstats.api.DatabaseAPI;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class SendPlayerStats implements Runnable {
    private final OfflinePlayer infoPlayer;
    private final CommandSender inquirer;

    public SendPlayerStats(CommandSender inquirer, OfflinePlayer infoPlayer) {
        this.infoPlayer = infoPlayer;
        this.inquirer = inquirer;
    }

    @Override
    public void run() {
        final String[] info = DatabaseAPI.info(infoPlayer);
        inquirer.sendMessage(info);
    }
}
