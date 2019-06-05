package net.slipcor.pvpstats.commands;


import net.slipcor.pvpstats.Language;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractCommand {
    final String[] perms;

    AbstractCommand(final String[] permissions) {
        perms = permissions.clone();
    }

    static boolean argCountValid(final CommandSender sender, final String[] args,
                                 final Integer[] validCounts) {

        for (final int i : validCounts) {
            if (i == args.length) {
                return true;
            }
        }

        sender.sendMessage(Language.ERROR_INVALID_ARGUMENT_COUNT.toString(String.valueOf(args.length), joinArray(validCounts, "|")));
        return false;
    }

    public abstract void commit(CommandSender sender, String[] args);

    public abstract List<String> getMain();

    public abstract String getName();

    public abstract List<String> getShort();

    public abstract String getShortInfo();

    public boolean hasPerms(final CommandSender sender) {
        for (final String perm : perms) {
            if (sender.hasPermission(perm)) {
                return true;
            }
        }
        return false;
    }

    public void load(final List<AbstractCommand> list, final Map<String, AbstractCommand> map) {
        for (String sShort : getShort()) {
            map.put(sShort, this);
        }
        for (String sMain : getMain()) {
            map.put(sMain, this);
        }
        list.add(this);
    }

    public List<String> completeTab(String[] args) {
        return new ArrayList<>(); // we have no arguments
    }

    private static String joinArray(final Object[] array, final String glue) {
        final StringBuilder result = new StringBuilder();
        for (final Object o : array) {
            result.append(glue);
            result.append(o);
        }
        if (result.length() <= glue.length()) {
            return result.toString();
        }
        return result.substring(glue.length());
    }
}
