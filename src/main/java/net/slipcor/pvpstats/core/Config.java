package net.slipcor.pvpstats.core;

import org.apache.commons.lang.ObjectUtils.Null;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Config access class
 *
 * @author slipcor
 */
public class Config {
    private final JavaPlugin plugin;

    /**
     * The Config constructor;
     * <p>
     * Create the config and append comments
     *
     * @param plugin the JavaPlugin which contains the config
     */
    public Config(final JavaPlugin plugin) {
        super();
        this.plugin = plugin;
        this.saveDefaultConfig();
        this.appendComments();
    }


    /**
     * The Config entry class
     * <p>
     * Each entry has an explicit class type, a node, optional comments
     * and a default value
     */
    public enum Entry {
        MYSQL(Null.class, "MySQL", null, new String[]{
                "# === [ MySQL Settings ] ==="}),
        MYSQL_ACTIVE(Boolean.class, "MySQL.active", false, new String[]{
                "# activate MySQL"}),
        MYSQL_HOST(String.class, "MySQL.hostname", "host", new String[]{
                "# hostname to use to connect to the database, in most cases 'localhost'"}),
        MYSQL_USERNAME(String.class, "MySQL.username", "user", new String[]{
                "# username to use to connect to the database"}),
        MYSQL_PASSWORD(String.class, "MySQL.password", "pw", new String[]{
                "# password to use to connect to the database"}),
        MYSQL_DATABASE(String.class, "MySQL.database", "db", new String[]{
                "# database name to connect to"}),
        MYSQL_PORT(Integer.class, "MySQL.port", 3306, new String[]{
                "# database port to connect to"}),
        MYSQL_TABLE(String.class, "MySQL.table", "pvpstats", new String[]{
                "# general statistic table name"}),
        MYSQL_KILLTABLE(String.class, "MySQL.killtable", "pvpkillstats", new String[]{
                "# kill statistic table name"}),
        MYSQL_OPTIONS(String.class, "MySQL.options", "?autoReconnect=true", new String[]{
                "# connection options"}),

        SQLITE(Null.class, "SQLite", null, new String[]{
                "# === [ SQLite Settings ] ==="}),
        SQLITE_ACTIVE(Boolean.class, "SQLite.active", false, new String[]{
                "# activate SQLite"}),
        SQLITE_FILENAME(String.class, "SQLite.filename", "database", new String[]{
                "# database file name, plugin will append extension '.db'"}),
        SQLITE_TABLE(String.class, "SQLite.table", "pvpstats", new String[]{
                "# general statistic table name"}),
        SQLITE_KILLTABLE(String.class, "SQLite.killtable", "pvpkillstats", new String[]{
                "# kill statistic table name"}),

        YML(Null.class, "YML", null, new String[]{
                "# === [ YML Database Emulation Settings ] ==="}),
        YML_TABLE(String.class, "YML.table", "pvpstats", new String[]{
                "# general statistic file name, plugin will append extension '.yml'"}),
        YML_KILLTABLE(String.class, "YML.killtable", "pvpkillstats", new String[]{
                "# kill statistic file name, plugin will append extension '.yml'"}),

        STATISTICS(Null.class, "statistics", null, new String[]{
                "# === [ Statistic Settings ] ==="}),
        STATISTICS_CLEAR_ON_START(Boolean.class, "statistics.clearOnStart", true, new String[]{
                "# clear (duplicated) statistics on every start"}),
        STATISTICS_CREATE_ON_JOIN(Boolean.class, "statistics.createOnJoin", true, new String[]{
                "# create empty player entry when they join"}),
        STATISTICS_COLLECT_PRECISE(Boolean.class, "statistics.collectPrecise", true, new String[]{
                "# save every kill - is never read internally, so only for web stats or alike"}),
        STATISTICS_COUNT_REGULAR_DEATHS(Boolean.class, "statistics.countRegularDeaths", false, new String[]{
                "# count dying from other sources than players towards death count and resetting of streaks"}),
        STATISTICS_CHECK_ABUSE(Boolean.class, "statistics.checkAbuse", true, new String[]{
                "# prevent players from getting kills from the same victim"}),
        STATISTICS_ABUSE_SECONDS(Integer.class, "statistics.abuseSeconds", -1, new String[]{
                "# seconds to wait before allowing to kill the same player again to count (-1 will never reset)"}),
        STATISTICS_KD_CALCULATION(String.class, "statistics.killDeathCalculation", "pvpkillstats", new String[]{
                "# mathematical formula to calculate kill/death ratio"}),
        STATISTICS_RESET_KILLSTREAK_ON_QUIT(Boolean.class, "statistics.resetKillstreakOnQuit", false, new String[]{
                "# always reset a streak when a player disconnects"}),


        ELO(Null.class, "eloscore", null, new String[]{
                "# === [ ELO Score Settings ] ==="}),
        ELO_ACTIVE(Boolean.class, "eloscore.active", false, new String[0]),
        ELO_MINIMUM(Integer.class, "eloscore.minimum", 18, new String[]{
                "# min possible ELO score"}),
        ELO_DEFAULT(Integer.class, "eloscore.default", 1500, new String[]{
                "# starting ELO score"}),
        ELO_MAXIMUM(Integer.class, "eloscore.maximum", 3000, new String[]{
                "# max possible ELO score"}),
        ELO_KFACTOR(Null.class, "eloscore.k-factor", null, new String[]{
                "# K-Factor settings"}),
        ELO_K_BELOW(Integer.class, "eloscore.k-factor.below", 32, new String[]{
                "# K-Factor below threshold"}),
        ELO_K_ABOVE(Integer.class, "eloscore.k-factor.above", 16, new String[]{
                "# K-Factor above threshold"}),
        ELO_K_THRESHOLD(Integer.class, "eloscore.k-factor.threshold", 2000, new String[]{
                "# K-Factor threshold"}),

        MESSAGES(Null.class, "msg", null, new String[]{
                "# === [ Message Settings ] ==="}),
        MESSAGES_OVERRIDES(Boolean.class, "msg.overrides", false, new String[]{"# activate the following overrides"}),
        MESSAGES_OVERRIDE_LIST(List.class, "msg.main",
                Arrays.asList("&cName: &7%n", "&cKills: &7%k", "&cDeaths: &7%d", "&cRatio: &7%r", "&cStreak: &7%s", "&cMax Streak: &7%m"),
                new String[0]),

        UPDATE(Null.class, "update", null, new String[]{
                "# === [ Updater Settings ] ==="}),
        UPDATE_MODE(String.class, "update.mode", "both", new String[]{
                "# what to do? Valid values: off, announce, download, both"}),
        UPDATE_TYPE(String.class, "update.type", "beta", new String[]{
                "# which type of branch to get updates? Valid values: dev, alpha, beta, release"}),
        UPDATE_TRACKER(Boolean.class, "update.tracker", true, new String[]{
                "# phone home to inform slipcor that you use the plugin"}),

        OTHER(Null.class, "other", null, new String[]{
                "# === [ Integration into other Plugins ] ==="}),
        OTHER_PVPARENA(Boolean.class, "other.PVPArena", false, new String[]{"# count PVP Arena deaths"}),

        IGNORE_WORLDS(List.class, "ignoreworlds", Collections.singletonList("doNotTrack"), new String[]{"# world names where not to count statistics"});


        final Class type;
        final String node;
        final Object value;
        final String[] comments;

        Entry(final Class oClass, final String node, final Object def, final String[] comments) {
            type = oClass;
            this.node = node;
            value = def;
            this.comments = comments == null ? null : comments.clone();
        }

        /**
         * Try to get a ConfigEntry based on a node string
         *
         * @param node the node to search for
         * @return the entry or null if not found
         */
        private static Entry getByNode(final String node) {
            for (Entry c : values()) {
                if (c.getNode().equals(node)) {
                    return c;
                }
            }
            return null;
        }

        /**
         * @return the node string
         */
        public String getNode() {
            return node;
        }
    }

    /**
     * Read a config string entry
     *
     * @param entry the entry to read
     * @return the config string value
     */
    public String get(final Entry entry) {
        return plugin.getConfig().getString(entry.getNode());
    }

    /**
     * Read a config boolean entry
     *
     * @param entry the entry to read
     * @return the config boolean value
     */
    public boolean getBoolean(final Entry entry) {
        return plugin.getConfig().getBoolean(entry.getNode());
    }

    /**
     * Read a config integer entry
     *
     * @param entry the entry to read
     * @return the config int value
     */
    public int getInt(final Entry entry) {
        return plugin.getConfig().getInt(entry.getNode());
    }

    /**
     * Read a config double entry
     *
     * @param entry the entry to read
     * @return the config double value
     */
    public double getDouble(final Entry entry) {
        return plugin.getConfig().getDouble(entry.getNode());
    }

    /**
     * Read a config StringList entry
     *
     * @param entry the entry to read
     * @return the config string list value
     */
    public List<String> getList(final Entry entry) {
        return plugin.getConfig().getStringList(entry.getNode());
    }

    /**
     * On reloading, append the comments
     */
    public void reload() {
        saveDefaultConfig();
        appendComments();
    }

    /**
     * Append the comments.
     * <p>
     * Iterate over the config file and add comments, if we didn't do that
     * already.
     */
    private void appendComments() {

        final File ymlFile = new File(plugin.getDataFolder(), "config.yml");

        try {

            final FileInputStream fis = new FileInputStream(ymlFile);
            final DataInputStream dis = new DataInputStream(fis);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(dis));

            // temporary write all the notes to a dedicated file
            final File tempFile = new File(plugin.getDataFolder(), "config-temp.yml");
            if (!tempFile.exists()) {
                tempFile.createNewFile();
            }

            final FileOutputStream fos = new FileOutputStream(tempFile);
            final DataOutputStream dos = new DataOutputStream(fos);
            final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(dos));

            String stringLine;

            int indent = 0;

            String key = null;

            while ((stringLine = reader.readLine()) != null) {

                if (key == null && writer.toString().length() < 1) {
                    writer.append("# === [ PVP Stats Config ] ===");
                    writer.newLine();
                }

                if (stringLine.startsWith("  #")) {
                    writer.flush();
                    writer.close();
                    reader.close();
                    tempFile.delete();
                    plugin.getLogger().warning("Config already has comments!");
                    return;
                }

                final int firstDigit = (indent * 2);

                if (stringLine.startsWith("#") || stringLine.length() < firstDigit + 1 || stringLine.charAt(firstDigit) == '#') {

                    writer.append(stringLine);
                    writer.newLine();
                    continue;
                }

                if (stringLine.contains(":")) {
                    final String newStringLine = stringLine.split(":")[0] + ":";
                    int pos;
                    final StringBuilder builder = new StringBuilder();
                    int newDigit = -1;

                    for (pos = 0; pos < newStringLine.length(); pos++) {
                        if (newStringLine.charAt(pos) != ' '
                                && newStringLine.charAt(pos) != ':') {
                            if (newDigit == -1) {
                                newDigit = pos;
                            }
                            builder.append(newStringLine.charAt(pos));
                        }
                    }

                    if (key == null) {
                        key = builder.toString();
                    }

                    String[] split = key.split("\\.");

                    if (newDigit > firstDigit) {
                        indent++;

                        final String[] newString = new String[split.length + 1];
                        System.arraycopy(split, 0, newString, 0, split.length);
                        newString[split.length] = builder.toString();
                        split = newString;
                    } else if (newDigit < firstDigit) {

                        indent = (newDigit / 2);

                        final String[] newString = new String[indent + 1];

                        System.arraycopy(split, 0, newString, 0, indent);

                        newString[newString.length - 1] = builder.toString();
                        split = newString;
                    } else {
                        split[split.length - 1] = builder.toString();
                    }

                    final StringBuilder buffer = new StringBuilder();
                    for (String string : split) {
                        buffer.append('.');
                        buffer.append(string);
                    }

                    key = buffer.substring(1);

                    final Entry entry = Entry.getByNode(key);

                    if (entry == null) {
                        writer.append(stringLine);
                        writer.newLine();
                        continue;
                    }

                    final StringBuilder value = new StringBuilder();

                    for (int k = 0; k < indent; k++) {
                        value.append("  ");
                    }
                    if (entry.comments != null) {
                        for (String s : entry.comments) {
                            writer.append(String.valueOf(value)).append(s);
                            writer.newLine();
                        }
                    }
                }
                writer.append(stringLine);
                writer.newLine();
            }

            // close ALL THE THINGS
            writer.flush();
            dos.close();
            fos.close();
            writer.close();

            fis.close();
            dis.close();
            reader.close();

            if (!ymlFile.delete()) {
                plugin.getLogger().warning("Could not replace config file with commented version!");
            }
            if (!tempFile.renameTo(ymlFile)) {
                plugin.getLogger().warning("Could not update config file with comments!");
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Workaround to the config not updating defaults.
     */
    private void saveDefaultConfig() {
        plugin.getConfig().options().copyDefaults(true);
        try {
            plugin.getConfig().save(new File(plugin.getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
