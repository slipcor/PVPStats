package net.slipcor.pvpstats.commands;

import net.slipcor.core.CoreCommand;
import net.slipcor.core.CoreDebugger;
import net.slipcor.core.CorePlugin;
import net.slipcor.pvpstats.PVPStats;
import net.slipcor.pvpstats.api.DatabaseAPI;
import net.slipcor.pvpstats.classes.PlayerHandler;
import net.slipcor.pvpstats.yml.Language;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandDebugKill extends CoreCommand {
    public CommandDebugKill(CorePlugin plugin) {
        super(plugin, "pvpstats.debugkill", Language.MSG.COMMAND_ARGUMENT_COUNT_INVALID);
    }

    public static CoreDebugger debugger;

    @Override
    public void commit(CommandSender sender, String[] args) {
        debugger.i("debug kill!");
        if (!hasPerms(sender)) {
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG.NO_PERMISSION_DEBUG.parse());
            return;
        }
        if (!argCountValid(sender, args, new Integer[]{3, 5})) {
            return;
        }
        debugger.i("let's go!");

        if (args.length == 5) {
            String attacker = args[1];
            String victim1 = args[2];
            String victim2 = args[3];
            String victim3 = args[4];

            NeinOfflinePlayer offlineAttacker = Bukkit.getServer().getOfflinePlayer(attacker);
            NeinOfflinePlayer offlineVictim1 = Bukkit.getServer().getOfflinePlayer(victim1);
            NeinOfflinePlayer offlineVictim2 = Bukkit.getServer().getOfflinePlayer(victim2);
            NeinOfflinePlayer offlineVictim3 = Bukkit.getServer().getOfflinePlayer(victim3);

            for (int i = 1; i <= 100; i++) {
                DatabaseAPI.AkilledB(offlineAttacker, offlineVictim1);
                DatabaseAPI.AkilledB(offlineAttacker, offlineVictim2);
                DatabaseAPI.AkilledB(offlineAttacker, offlineVictim3);
                System.out.println("kill counted: " + i);
            }

            return;
        }

        String attacker = args[1];
        String victim = args[2];

        OfflinePlayer offlineAttacker = Bukkit.getServer().getOfflinePlayer(attacker);
        OfflinePlayer offlineVictim = Bukkit.getServer().getOfflinePlayer(victim);

        DatabaseAPI.AkilledB(offlineAttacker, offlineVictim);
    }

    public List<String> completeTab(String[] args) {
        List<String> results = new ArrayList<>();

        if (args.length < 2 || args[1].equals("")) {
            // list first argument possibilities
            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                results.add(PlayerHandler.getRawPlayerName(p));
            }
            return results;
        }

        if (args.length > 3) {
            return results; // don't go too far!
        }

        // we started typing!
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            addIfMatches(results, p.getName(), args[1]);
        }
        return results;
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("debugkill");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!dk");
    }

    @Override
    public String getShortInfo() {
        return "/pvpstats debugkill killer killed - add a manual kill";
    }
}
