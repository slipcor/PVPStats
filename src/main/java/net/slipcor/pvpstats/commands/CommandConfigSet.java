package net.slipcor.pvpstats.commands;

import net.slipcor.pvpstats.PVPStats;
import net.slipcor.pvpstats.core.Config;
import net.slipcor.pvpstats.core.Language;
import org.apache.commons.lang.ObjectUtils;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandConfigSet extends AbstractCommand {
    public CommandConfigSet() {
        super(new String[]{"pvpstats.configset"});
    }

    @Override
    public void commit(final CommandSender sender, final String[] args) {
        if (!hasPerms(sender)) {
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG_NOPERMCONFIGSET.toString());
            return;
        }

        if (!argCountValid(sender, args, new Integer[]{3})) {
            return;
        }

        // /pvpstats set [node] [value]

        set(sender, args[1], args[2]);
    }
    private void set(final CommandSender sender, final String node, final String value) {

        for (Config.Entry entry : Config.Entry.values()) {
            if (entry.getNode().toLowerCase().endsWith('.' + node.toLowerCase())) {
                // get the actual full proper node
                set(sender, entry.getNode(), value);
                return;
            }
        }

        final Config.Entry entry = Config.Entry.getByNode(node);

        if (entry == null) {
            PVPStats.getInstance().sendPrefixed(sender, Language.ERROR_CONFIGSET_UNKNOWN.toString(node));
            return;
        }
        final Class type = entry.getType();

        Config config = PVPStats.getInstance().config();

        if (type.equals(ObjectUtils.Null.class)) {
            PVPStats.getInstance().sendPrefixed(sender, Language.ERROR_CONFIGSET_GROUP.toString(node));
            return;
        } else if (List.class.equals(type)) {
            PVPStats.getInstance().sendPrefixed(sender, Language.ERROR_CONFIGSET_LIST.toString(node));
            return;
        } else if (Boolean.class.equals(type)) {
            if ("true".equalsIgnoreCase(value)) {
                config.setValue(entry, Boolean.TRUE);
                PVPStats.getInstance().sendPrefixed(sender, Language.MSG_CONFIGSET.toString(node, "true"));
            } else if ("false".equalsIgnoreCase(value)) {
                config.setValue(entry, Boolean.FALSE);
                PVPStats.getInstance().sendPrefixed(sender, Language.MSG_CONFIGSET.toString(node, "false"));
            } else {
                PVPStats.getInstance().sendPrefixed(sender,
                        Language.ERROR_COMMAND_ARGUMENT.toString(value, "boolean (true|false)"));
                return;
            }
        } else if (String.class.equals(type)) {
            config.setValue(entry, String.valueOf(value));
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG_CONFIGSET.toString(node, value));
        } else if (Integer.class.equals(type)) {
            final int iValue;

            try {
                iValue = Integer.parseInt(value);
            } catch (final Exception e) {
                PVPStats.getInstance().sendPrefixed(sender, Language.ERROR_INVALID_NUMBER.toString(value));
                return;
            }
            config.setValue(entry, iValue);
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG_CONFIGSET.toString(node, String.valueOf(iValue)));
        } else if (Double.class.equals(type)) {
            final double dValue;

            try {
                dValue = Double.parseDouble(value);
            } catch (final Exception e) {
                PVPStats.getInstance().sendPrefixed(sender,
                        Language.ERROR_COMMAND_ARGUMENT.toString(value, "double (e.g. 12.00)"));
                return;
            }
            config.setValue(entry, dValue);
            PVPStats.getInstance().sendPrefixed(sender, Language.MSG_CONFIGSET.toString(node,
                            String.valueOf(dValue)));
        } else {
            PVPStats.getInstance().sendPrefixed(sender,
                    Language.ERROR_CONFIGSET_TYPE_UNKNOWN.toString(String.valueOf(type)));
            return;
        }
        config.save();
    }

    public List<String> completeTab(String[] args) {
        List<String> results = new ArrayList<>();

        if (args.length < 2 || args[1].equals("")) {
            // list first argument possibilities
            for (Config.Entry entry : Config.Entry.values()) {
                if (entry.getType().equals(ObjectUtils.Null.class) || entry.getType().equals(List.class)) {
                    continue;
                }

                results.add(entry.getNode());
            }
            return results;
        }

        if (args.length > 2) {
            return results; // don't go too far!
        }

        // second argument!
        for (Config.Entry entry : Config.Entry.values()) {
            if (entry.getType().equals(ObjectUtils.Null.class) || entry.getType().equals(List.class)) {
                continue;
            }

            addIfMatches(results, entry.getNode(), args[1]);
        }

        return results;
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("configset");
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!cs");
    }

    @Override
    public String getShortInfo() {
        return "/pvpstats configset [node] [value] - set a config value";
    }
}
