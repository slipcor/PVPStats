package net.slipcor.pvpstats.commands;

import net.slipcor.pvpstats.PVPStats;
import net.slipcor.pvpstats.api.DatabaseAPI;
import net.slipcor.pvpstats.api.PlayerStatisticsBuffer;
import net.slipcor.pvpstats.core.Config;
import net.slipcor.pvpstats.core.Language;
import net.slipcor.pvpstats.display.SignDisplay;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandDebugKill extends AbstractCommand {
    public CommandDebugKill() {
        super(new String[]{"pvpstats.debugkill"});
    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            sender.sendMessage(Language.MSG_NOPERMDEBUG.toString());
            return;
        }
        if (!argCountValid(sender, args, new Integer[]{3})) {
            return;
        }
        if (!(sender instanceof Player)) {
            PVPStats.getInstance().sendPrefixed(sender, Language.ERROR_NOT_A_PLAYER.toString());
            return;
        }

        String attacker = args[1];
        String victim = args[2];

        OfflinePlayer offlineAttacker = Bukkit.getServer().getOfflinePlayer(attacker);
        OfflinePlayer offlineVictim = Bukkit.getServer().getOfflinePlayer(victim);

        if (attacker.equalsIgnoreCase("null") && victim.equalsIgnoreCase("null")) {
            PVPStats.getInstance().sendPrefixed(sender, Language.ERROR_NULL_KILLS.toString());
            return;
        }

        if (victim.equalsIgnoreCase("null")) {
            DatabaseAPI.forceIncKill(offlineAttacker, PlayerStatisticsBuffer.getEloScore(offlineAttacker.getUniqueId()));

            PVPStats.getInstance().getSQLHandler().addKill(
                    offlineAttacker.getName(), offlineAttacker.getUniqueId().toString(),
                    "", "",
                    ((Player)sender).getWorld().getName());

            PVPStats.getInstance().sendPrefixed(sender, Language.INFO_AKILLEDB.toString(attacker, "null"));

            SignDisplay.updateAll();
            return;
        }
        if (attacker.equalsIgnoreCase("null")) {
            DatabaseAPI.forceIncDeath(offlineVictim, PlayerStatisticsBuffer.getEloScore(offlineVictim.getUniqueId()));

            PVPStats.getInstance().getSQLHandler().addKill(
                    "", "",
                    offlineVictim.getName(), offlineVictim.getUniqueId().toString(),
                    ((Player)sender).getWorld().getName());

            PVPStats.getInstance().sendPrefixed(sender, Language.INFO_AKILLEDB.toString("null", victim));

            SignDisplay.updateAll();
            return;
        }

        Config config = PVPStats.getInstance().config();

        if (!config.getBoolean(Config.Entry.ELO_ACTIVE)) {
            DatabaseAPI.forceIncKill(offlineAttacker, PlayerStatisticsBuffer.getEloScore(offlineAttacker.getUniqueId()));
            DatabaseAPI.forceIncDeath(offlineVictim, PlayerStatisticsBuffer.getEloScore(offlineVictim.getUniqueId()));

            PVPStats.getInstance().getSQLHandler().addKill(
                    offlineAttacker.getName(), offlineAttacker.getUniqueId().toString(),
                    offlineVictim.getName(), offlineVictim.getUniqueId().toString(),
                    ((Player)sender).getWorld().getName());

            PVPStats.getInstance().sendPrefixed(sender, Language.INFO_AKILLEDB.toString(attacker, victim));

            SignDisplay.updateAll();
            return;
        }

        final int min = config.getInt(Config.Entry.ELO_MINIMUM);
        final int max = config.getInt(Config.Entry.ELO_MAXIMUM);
        final int kBelow = config.getInt(Config.Entry.ELO_K_BELOW);
        final int kAbove = config.getInt(Config.Entry.ELO_K_ABOVE);
        final int kThreshold = config.getInt(Config.Entry.ELO_K_THRESHOLD);

        final int oldA = PlayerStatisticsBuffer.getEloScore(offlineAttacker.getUniqueId());
        final int oldP = PlayerStatisticsBuffer.getEloScore(offlineVictim.getUniqueId());

        final int kA = oldA >= kThreshold ? kAbove : kBelow;
        final int kP = oldP >= kThreshold ? kAbove : kBelow;

        final int newA = DatabaseAPI.calcElo(oldA, oldP, kA, true, min, max);
        final int newP = DatabaseAPI.calcElo(oldP, oldA, kP, false, min, max);

        if (DatabaseAPI.forceIncKill(offlineAttacker, newA)) {
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG_ELO_ADDED.toString(String.valueOf(newA - oldA), String.valueOf(newA)));
            PlayerStatisticsBuffer.setEloScore(offlineAttacker.getUniqueId(), newA);
        }
        if (DatabaseAPI.forceIncDeath(offlineVictim, newP)) {
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG_ELO_SUBBED.toString(String.valueOf(oldP - newP), String.valueOf(newP)));
            PlayerStatisticsBuffer.setEloScore(offlineVictim.getUniqueId(), newP);
        }

        PVPStats.getInstance().getSQLHandler().addKill(
                offlineAttacker.getName(), offlineAttacker.getUniqueId().toString(),
                offlineVictim.getName(), offlineVictim.getUniqueId().toString(),
                ((Player)sender).getWorld().getName());

        PVPStats.getInstance().sendPrefixed(sender, Language.INFO_AKILLEDB.toString(attacker, victim));

        SignDisplay.updateAll();
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

        // we started typing!
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            addIfMatches(results, p.getName(), p.getName());
        }
        return results;
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("debugkill");
    }

    @Override
    public String getName() {
        return getClass().getName();
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
