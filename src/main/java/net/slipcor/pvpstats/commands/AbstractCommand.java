package net.slipcor.pvpstats.commands;


import net.slipcor.pvpstats.core.Language;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;

/**
 * A base class for commands to fully implement
 */
public abstract class AbstractCommand {
    final String[] perms;

    AbstractCommand(final String[] permissions) {
        perms = permissions.clone();
    }

    protected void addIfMatches(List<String> list, String word, String check) {
        if (check.equals("") || word.toLowerCase().startsWith(check.toLowerCase())) {
            list.add(word);
        }
    }

    /**
     * Are the given arguments of valid count?
     *
     * @param sender      the sender issuing the command
     * @param args        the arguments given
     * @param validCounts valid argument counts
     * @return whether the amount of arguments is valid
     */
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

    /**
     * Do what the command is supposed to do
     *
     * @param sender the sender issuing the command
     * @param args   the command arguments
     */
    public abstract void commit(CommandSender sender, String[] args);

    /**
     * @return a list of command names
     */
    public abstract List<String> getMain();

    /**
     * @return the command class name
     */
    public abstract String getName();

    /**
     * @return a list of command shorthand names
     */
    public abstract List<String> getShort();

    /**
     * @return an info text explaining the command
     */
    public abstract String getShortInfo();

    /**
     * Check whether a sender has the permission to use this command
     *
     * @param sender the sender trying to issue the command
     * @return whether they have the permission
     */
    public boolean hasPerms(final CommandSender sender) {
        for (final String perm : perms) {
            if (sender.hasPermission(perm)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Load the command into the plugin's command list and map
     *
     * @param list the list to add to
     * @param map  the map to add to
     */
    public void load(final List<AbstractCommand> list, final Map<String, AbstractCommand> map) {
        for (String sShort : getShort()) {
            map.put(sShort, this);
        }
        for (String sMain : getMain()) {
            map.put(sMain, this);
        }
        list.add(this);
    }

    /**
     * Return tab complete matches
     *
     * @param args the current command progress
     * @return a list of matches to complete with
     */
    public abstract List<String> completeTab(String[] args);

    /**
     * Helper function to join an array of strings together
     *
     * @param array the array to join
     * @param glue  the joining string, can be empty
     * @return a joined string
     */
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
