package net.slipcor.pvpstats.classes;

import net.slipcor.pvpstats.PVPStats;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Formatter;
import java.util.logging.*;


/**
 * Debugger class, provides methods for logging when in debug mode
 *
 * @author slipcor
 */

public class Debugger {
    private static boolean override;
    private static boolean server_log;

    private static final String prefix = "[PS-debug] ";
    private static final Set<Integer> check = new HashSet<>();
    private static final Set<String> strings = new HashSet<>();

    private final int debugID;

    private static Logger logger;

    private static final List<Logger> loggers = new ArrayList<>();
    private static final List<Debugger> DEBUGGERS = new ArrayList<>();
    private boolean active;

    public Debugger(final int iID) {
        debugID = iID;
    }

    private static Logger getGlobalLogger() {
        if (logger == null) {
            logger = Logger.getAnonymousLogger();
            logger.setLevel(Level.ALL);
            logger.setUseParentHandlers(false);

            for (final Handler handler : logger.getHandlers()) {
                logger.removeHandler(handler);
            }

            try {
                final SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd-HH-mm");

                final File debugFolder = new File(PVPStats.getInstance().getDataFolder(), "debug");


                debugFolder.mkdirs();
                final File logFile = new File(debugFolder, dateformat.format(new Date()) + "general.log");
                logFile.createNewFile();

                final FileHandler handler = new FileHandler(logFile.getAbsolutePath());

                handler.setFormatter(LogFileFormatter.newInstance());

                logger.addHandler(handler);

                loggers.add(logger);
            } catch (final IOException | SecurityException ex) {
                PVPStats.getInstance().getLogger().log(Level.SEVERE, null, ex);
            }
        }

        return logger;
    }

    /**
     * does this class debug?
     *
     * @return true if DEBUGGERS, false otherwise
     */
    private boolean debugs() {
        return override || active || check.contains(debugID) || check.contains(666);
    }

    private boolean debugs(final String term) {
        return override || active || strings.contains(term) || check.contains(666);
    }

    /**
     * log a message as prefixed INFO
     *
     * @param string the message
     */
    public void i(final String string) {
        if (!debugs()) {
            return;
        }
        getGlobalLogger().info(prefix + System.currentTimeMillis() % 1000 + ' ' + string);

        if (server_log) {
            System.out.print(prefix + System.currentTimeMillis() % 1000 + ' ' + string);
        }
    }

    public void i(final String string, final CommandSender sender) {
        if (sender == null) {
            i(string, "null");
            return;
        }
        if (!debugs(sender.getName())) {
            return;
        }
        getGlobalLogger().info(prefix + "[p:" + sender.getName() + ']' + System.currentTimeMillis() % 1000 + ' ' + string);
        if (server_log) {
            System.out.print(prefix + " " + "[p:" + sender.getName() + ']' + System.currentTimeMillis() % 1000 + ' ' + string);
        }
    }

    public void i(final String string, final String filter) {
        if (!debugs(filter)) {
            return;
        }

        getGlobalLogger().info(prefix + System.currentTimeMillis() % 1000 + ' ' + string);
        if (server_log) {
            System.out.print(prefix + System.currentTimeMillis() % 1000 + ' ' + string);
        }
    }

    public static void load(final PVPStats instance, final CommandSender sender) {
        check.clear();
        strings.clear();
        override = false;
        boolean isPlayer = sender instanceof Player;

        final String debugs = instance.getConfig().getString("debug", "off");

        loggers.clear();

        if ("off".equalsIgnoreCase(debugs) || "none".equalsIgnoreCase(debugs) || "false".equalsIgnoreCase(debugs)) {
            if (isPlayer) {
                sender.sendMessage("debugging: off");
            } else {
                PVPStats.getInstance().getLogger().info("debugging: off");
            }
        } else {
            if ("on".equalsIgnoreCase(debugs) || "all".equalsIgnoreCase(debugs) || "true".equalsIgnoreCase(debugs)) {
                Debugger.check.add(666);
                override = true;
                if (isPlayer) {
                    sender.sendMessage("debugging on!");
                } else {
                    PVPStats.getInstance().getLogger().info("debugging on!");
                }
            } else {
                final String[] sIds = debugs.split(",");
                if (isPlayer) {
                    sender.sendMessage("debugging: " + debugs);
                } else {
                    PVPStats.getInstance().getLogger().info("debugging: " + debugs);
                }
                for (final String s : sIds) {
                    try {
                        Debugger.check.add(Integer.valueOf(s));
                    } catch (final Exception e) {
                        strings.add(s);
                    }
                }
            }
        }
    }

    private void activate() {
        active = true;
    }

    public static void destroy() {

        for (final Logger log : Debugger.loggers) {
            final Handler[] handlers = log.getHandlers().clone();
            for (final Handler hand : handlers) {
                log.removeHandler(hand);
                hand.close();
            }
        }
        Debugger.loggers.clear();
    }


    static class LogFileFormatter extends Formatter {

        private final SimpleDateFormat date;

        static LogFileFormatter newInstance() {
            return new LogFileFormatter();
        }

        private LogFileFormatter() {
            super();
            date = new SimpleDateFormat("yy.MM.dd HH:mm:ss");
        }

        @Override
        public String format(final LogRecord record) {
            final StringBuilder builder = new StringBuilder();
            final Throwable exception = record.getThrown();

            builder.append(date.format(record.getMillis()));
            builder.append(" [");
            builder.append(record.getLevel().getLocalizedName().toUpperCase());
            builder.append("] ");
            builder.append(record.getMessage());
            builder.append('\n');

            if (exception != null) {
                final StringWriter writer = new StringWriter();
                exception.printStackTrace(new PrintWriter(writer));
                builder.append(writer);
            }

            return builder.toString();
        }
    }
}