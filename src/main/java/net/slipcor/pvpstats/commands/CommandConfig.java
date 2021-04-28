package net.slipcor.pvpstats.commands;

import net.slipcor.core.ConfigEntry;
import net.slipcor.core.CoreCommand;
import net.slipcor.core.CorePlugin;
import net.slipcor.pvpstats.PVPStats;
import net.slipcor.pvpstats.yml.Config;
import net.slipcor.pvpstats.yml.Language;
import org.apache.commons.lang.ObjectUtils;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandConfig extends CoreCommand {
    final List<Config.Entry> accessibleLists = new ArrayList<>();

    public CommandConfig(CorePlugin plugin) {
        super(plugin, "pvpstats.config", Language.MSG.ERROR_INVALID_ARGUMENT_COUNT);

        accessibleLists.add(Config.Entry.IGNORE_WORLDS);
    }

    @Override
    public void commit(final CommandSender sender, final String[] args) {
        if (!hasPerms(sender)) {
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG.MSG_NOPERMCONFIGSET.parse());
            return;
        }

        if (args.length >= 2 && args[1].toLowerCase().equals("set")) {
            if (!argCountValid(sender, args, new Integer[]{4})) {
                return;
            }

            //           0         1      2      3
            // /pvpstats config    set    [node] [value]
            set(sender, args[2], args[3]);
            return;
        }

        if (args.length >= 2 && args[1].toLowerCase().equals("get")) {
            if (!argCountValid(sender, args, new Integer[]{3})) {
                return;
            }

            //           0         1      2
            // /pvpstats config    get    [node]
            get(sender, args[2]);
            return;
        }

        if (args.length >= 2 && args[1].toLowerCase().equals("add")) {
            if (!argCountValid(sender, args, new Integer[]{4})) {
                return;
            }

            //           0         1      2      3
            // /pvpstats config    add    [node] [value]
            add(sender, args[2], args[3]);
            return;
        }

        if (args.length >= 2 && args[1].toLowerCase().equals("remove")) {
            if (!argCountValid(sender, args, new Integer[]{4})) {
                return;
            }

            //           0         1      2      3
            // /pvpstats config    add    [node] [value]
            remove(sender, args[2], args[3]);
            return;
        }

        PVPStats.getInstance().sendPrefixed(sender, getShortInfo());
    }

    private Config.Entry getFullNode(String part) {

        boolean foundEntry = false;
        Config.Entry completedEntry = null;

        for (Config.Entry entry : Config.Entry.values()) {
            if (entry.getNode().toLowerCase().contains(part.toLowerCase()) && entry.getNode().length() != part.length()) {
                if (foundEntry) {
                    // found a second match, let us not autocomplete this!
                    foundEntry = false;
                    completedEntry = null;
                    break;
                }
                foundEntry = true;
                completedEntry = entry;
            }
        }

        return completedEntry;
    }

    private void add(final CommandSender sender, final String node, final String value) {

        Config.Entry completedEntry = getFullNode(node);

        if (completedEntry != null) {
            // get the actual full proper node
            add(sender, completedEntry.getNode(), value);
            return;
        }

        final Config.Entry entry = Config.Entry.getByNode(node);

        if (entry == null) {
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG.ERROR_CONFIG_UNKNOWN.parse(node));
            return;
        }
        final ConfigEntry.Type entryType = entry.getType();

        Config config = PVPStats.getInstance().config();

        if (entryType == ConfigEntry.Type.COMMENT) {
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG.ERROR_CONFIG_SET_GROUP.parse(node));
            return;
        } else if (entryType == ConfigEntry.Type.LIST) {
            List<String> newList = new ArrayList<>(config.getStringList(entry, new ArrayList<String>()));
            if (newList.contains(value)) {
                PVPStats.getInstance().sendPrefixed(sender, Language.MSG.ERROR_CONFIG_ADD.parse(node, value));
                return;
            }
            newList.add(value);
            config.setValue(entry, newList);
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG.MSG_CONFIGADDED.parse(node, value));
        } else {
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG.ERROR_CONFIG_NO_LIST_NODE.parse(node));
            return;
        }
        config.save();
    }

    private void get(final CommandSender sender, final String node) {

        Config.Entry completedEntry = getFullNode(node);

        if (completedEntry != null) {
            // get the actual full proper node
            get(sender, completedEntry.getNode());
            return;
        }

        final Config.Entry entry = Config.Entry.getByNode(node);

        if (entry == null) {
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG.ERROR_CONFIG_UNKNOWN.parse(node));
            return;
        }
        if (entry.secret) {
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG.ERROR_CONFIG_SECRET.parse(node));
            return;
        }
        final ConfigEntry.Type entryType = entry.getType();

        Config config = PVPStats.getInstance().config();

        if (entryType == ConfigEntry.Type.COMMENT) {
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG.ERROR_CONFIG_GET_GROUP.parse(node));
        } else if (entryType == ConfigEntry.Type.LIST) {
            StringBuffer value = new StringBuffer();
            List<String> list = config.getStringList(entry, new ArrayList<String>());
            for (String item : list) {
                value.append("\n");
                value.append(item);
            }
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG.MSG_CONFIGGET.parse(node, value.toString()));
        } else if (entryType == ConfigEntry.Type.BOOLEAN) {
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG.MSG_CONFIGGET.parse(node, String.valueOf(config.getBoolean(entry))));
        } else if (entryType == ConfigEntry.Type.STRING) {
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG.MSG_CONFIGGET.parse(node, config.getString(entry)));
        } else if (entryType == ConfigEntry.Type.INT) {
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG.MSG_CONFIGGET.parse(node, String.valueOf(config.getInt(entry))));
        } else if (entryType == ConfigEntry.Type.DOUBLE) {
            PVPStats.getInstance().sendPrefixed(sender,
                    Language.MSG.MSG_CONFIGGET.parse(node, String.format("%.2f", config.getDouble(entry))));
        } else {
            PVPStats.getInstance().sendPrefixed(sender,
                    Language.MSG.ERROR_CONFIG_TYPE_UNKNOWN.parse(entryType.name()));
        }
    }

    private void remove(final CommandSender sender, final String node, final String value) {

        Config.Entry completedEntry = getFullNode(node);

        if (completedEntry != null) {
            // get the actual full proper node
            remove(sender, completedEntry.getNode(), value);
            return;
        }

        final Config.Entry entry = Config.Entry.getByNode(node);

        if (entry == null) {
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG.ERROR_CONFIG_UNKNOWN.parse(node));
            return;
        }
        final ConfigEntry.Type entryType = entry.getType();

        Config config = PVPStats.getInstance().config();

        if (entryType == ConfigEntry.Type.COMMENT) {
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG.ERROR_CONFIG_SET_GROUP.parse(node));
            return;
        } else if (entryType == ConfigEntry.Type.LIST) {
            List<String> newList = new ArrayList<>(config.getStringList(entry, new ArrayList<String>()));
            if (!newList.contains(value)) {
                PVPStats.getInstance().sendPrefixed(sender, Language.MSG.ERROR_CONFIG_REMOVE.parse(node, value));
                return;
            }
            newList.remove(value);
            config.setValue(entry, newList);
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG.MSG_CONFIGREMOVED.parse(node, value));
        } else {
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG.ERROR_CONFIG_NO_LIST_NODE.parse(node));
            return;
        }
        config.save();
    }

    private void set(final CommandSender sender, final String node, final String value) {

        Config.Entry completedEntry = getFullNode(node);

        if (completedEntry != null) {
            // get the actual full proper node
            set(sender, completedEntry.getNode(), value);
            return;
        }

        final Config.Entry entry = Config.Entry.getByNode(node);

        if (entry == null) {
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG.ERROR_CONFIG_UNKNOWN.parse(node));
            return;
        }
        final ConfigEntry.Type entryType = entry.getType();

        Config config = PVPStats.getInstance().config();

        if (entryType == ConfigEntry.Type.COMMENT) {
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG.ERROR_CONFIG_SET_GROUP.parse(node));
            return;
        } else if (entryType == ConfigEntry.Type.LIST) {
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG.ERROR_CONFIG_SET_LIST.parse(node));
            return;
        } else if (entryType == ConfigEntry.Type.BOOLEAN) {
            if ("true".equalsIgnoreCase(value)) {
                config.setValue(entry, Boolean.TRUE);
                PVPStats.getInstance().sendPrefixed(sender, Language.MSG.MSG_CONFIGSET.parse(node, "true"));
            } else if ("false".equalsIgnoreCase(value)) {
                config.setValue(entry, Boolean.FALSE);
                PVPStats.getInstance().sendPrefixed(sender, Language.MSG.MSG_CONFIGSET.parse(node, "false"));
            } else {
                PVPStats.getInstance().sendPrefixed(sender,
                        Language.MSG.ERROR_COMMAND_ARGUMENT.parse(value, "boolean (true|false)"));
                return;
            }
        } else if (entryType == ConfigEntry.Type.STRING) {
            config.setValue(entry, String.valueOf(value));
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG.MSG_CONFIGSET.parse(node, value));
        } else if (entryType == ConfigEntry.Type.INT) {
            final int iValue;

            try {
                iValue = Integer.parseInt(value);
            } catch (final Exception e) {
                PVPStats.getInstance().sendPrefixed(sender, Language.MSG.ERROR_INVALID_NUMBER.parse(value));
                return;
            }
            config.setValue(entry, iValue);
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG.MSG_CONFIGSET.parse(node, String.valueOf(iValue)));
        } else if (entryType == ConfigEntry.Type.DOUBLE) {
            final double dValue;

            try {
                dValue = Double.parseDouble(value);
            } catch (final Exception e) {
                PVPStats.getInstance().sendPrefixed(sender,
                        Language.MSG.ERROR_COMMAND_ARGUMENT.parse(value, "double (e.g. 12.00)"));
                return;
            }
            config.setValue(entry, dValue);
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG.MSG_CONFIGSET.parse(node,
                            String.valueOf(dValue)));
        } else {
            PVPStats.getInstance().sendPrefixed(sender,
                    Language.MSG.ERROR_CONFIG_TYPE_UNKNOWN.parse(entryType.name()));
            return;
        }
        config.save();
    }

    public List<String> completeTab(String[] args) {
        List<String> results = new ArrayList<>();
        //           0         1      2      3
        // /pvpstats config    add    [node] [value]

        if (args.length < 2 || args[1].equals("")) {
            // list first argument possibilities
            results.add("get");
            results.add("set");
            results.add("add");
            results.add("remove");
        } else if (args.length == 2) {
            // second argument!
            addIfMatches(results, "get", args[1]);
            addIfMatches(results, "set", args[1]);
            addIfMatches(results, "add", args[1]);
            addIfMatches(results, "remove", args[1]);
        } else {
            // args is >= 3

            if (
                    !args[1].equalsIgnoreCase("get") &&
                            !args[1].equalsIgnoreCase("set") &&
                            !args[1].equalsIgnoreCase("add") &&
                            !args[1].equalsIgnoreCase("remove")
            ) {
                return results;
            }

            if (args[2].equals("")) {
                // list actual argument possibilities
                for (Config.Entry entry : Config.Entry.values()) {

                    if (args[1].equalsIgnoreCase("get")) {
                        if (entry.getType().equals(ObjectUtils.Null.class)) {
                            continue;
                        }
                    } else if (args[1].equalsIgnoreCase("set")) {
                        if (entry.getType().equals(ObjectUtils.Null.class) || entry.getType().equals(List.class)) {
                            continue;
                        }
                    } else {
                        if (entry.getType().equals(ObjectUtils.Null.class) || !accessibleLists.contains(entry)) {
                            continue;
                        }
                    }
                    results.add(entry.getNode());
                }
                return results;
            }

            if (args.length > 3) {
                return results; // don't go too far!
            }

            for (Config.Entry entry : Config.Entry.values()) {
                if (args[1].equalsIgnoreCase("get")) {
                    if (entry.getType().equals(ObjectUtils.Null.class)) {
                        continue;
                    }
                } else if (args[1].equalsIgnoreCase("set")) {
                    if (entry.getType().equals(ObjectUtils.Null.class) || entry.getType().equals(List.class)) {
                        continue;
                    }
                } else {
                    if (entry.getType().equals(ObjectUtils.Null.class) || !accessibleLists.contains(entry)) {
                        continue;
                    }
                }

                addIfMatches(results, entry.getNode(), args[2]);
            }
        }

        return results;
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("config");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!c");
    }

    @Override
    public String getShortInfo() {
        return "/pvpstats config get [node] - get a config value\n" +
                "/pvpstats config set [node] [value] - set a config value\n" +
                "/pvpstats config add [node] [value] - add a value to a config list\n" +
                "/pvpstats config remove [node] [value] - remove a value from a config list";
    }
}
