package net.slipcor.pvpstats.impl;

import net.slipcor.pvpstats.PVPStats;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLiteConnection extends AbstractSQLConnection {
    // SQL connection details
    private final String dbDatabase;

    public SQLiteConnection(String dbDatabase, String dbTable, String dbKillTable) {
        super(dbTable, dbKillTable);
        this.dbDatabase = dbDatabase;
    }

    public boolean allowsAsync() {
        return false;
    }

    /**
     * Remove duplicate UUIDs from the database
     *
     * @param sender the sender trying to run the cleanup
     *
     * @return how many entries have been removed
     */
    @Override
    public int cleanup(CommandSender sender) {
        return super.cleanup(sender, "rowid");
    }

    /**
     * Try to connect to the database
     *
     * @param printError should we print errors that we encounter?
     * @return true if the connection was made successfully, false otherwise.
     */
    @Override
    public boolean connect(boolean printError) {
        try {
            File file = new File(PVPStats.getInstance().getDataFolder(), dbDatabase + ".db");
            if (file.exists()) {
                file.createNewFile();
            }
            this.databaseConnection = DriverManager.getConnection("jdbc:sqlite:" + file);

            collectPrecise = dbKillTable != null && !"".equals(dbKillTable);

            return this.databaseConnection != null;
        } catch (SQLException | IOException e) {
            if (printError) e.printStackTrace();
            return false;
        }
    }

    /**
     * Check whether a table exists
     *
     * @param database The database to check for the table in.
     * @param table    The table to check for existence.
     * @return true if the table exists, false if there was an error or the database doesn't exist.
     */
    public boolean tableExists(String database, String table) {
        String query = "SELECT name FROM sqlite_master WHERE type='table' AND name='" + table + "'";
        try {
            ResultSet set = executeQuery(query, false);
            return set != null && set.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
     * ----------------------
     *  TABLE ENTRY CREATION
     * ----------------------
     */

    /**
     * Create the kill stat table
     *
     * @param printError should we print errors that we encounter?
     */
    @Override
    public void createKillStatsTable(boolean printError) {
        String world = Bukkit.getServer().getWorlds().get(0).getName();
        final String query2 = "CREATE TABLE `" + dbKillTable + "` ( " +
                "`name` varchar(42) NOT NULL, " +
                "`uid` varchar(42), " +
                "`victim` varchar(42), " +
                "`victimuid` varchar(42), " +
                "`kill` int(1) NOT NULL default 0," +
                "`time` int(16) not null default 0, " +
                "`world` varchar(42) NOT NULL DEFAULT '" + world + "');";
        try {
            executeQuery(query2, true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create the statistics table
     *
     * @param printError should we print errors that we encounter?
     */
    @Override
    public void createStatsTable(boolean printError) {
        final String query = "CREATE TABLE `" + dbTable + "` ( " +
                "`name` varchar(42) NOT NULL, " +
                "`uid` varchar(42), " +
                "`kills` int(8) not null default 0, " +
                "`deaths` int(8) not null default 0, " +
                "`streak` int(8) not null default 0, " +
                "`currentstreak` int(8) not null default 0, " +
                "`elo` int(8) not null default 0, " +
                "`time` int(16) not null default 0);";
        try {
            executeQuery(query, true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
