package net.slipcor.pvpstats.impl;

import net.slipcor.pvpstats.PVPStats;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class SQLiteConnection extends AbstractSQLConnection {
    // SQL connection details
    private final String dbDatabase;

    public SQLiteConnection(String dbDatabase, String dbTable, String dbKillTable) {
        super(dbTable, dbKillTable);
        this.dbDatabase = dbDatabase;
    }

    /**
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
     * @param database The database to check for the table in.
     * @param table    The table to check for existence.
     * @return true if the table exists, false if there was an error or the database doesn't exist.
     * <p/>
     * This method looks through the information schema that comes with a MySQL installation and checks
     * if a certain table exists within a database.
     */
    public boolean tableExists(String database, String table) {
        String query = "SELECT name FROM sqlite_master WHERE type='table' AND name='"+table+"'";
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
     * create the kill stat table
     * @param printError should we print errors that we encounter?
     */
    @Override
    public void createKillStatsTable(boolean printError) {
        final String query2 = "CREATE TABLE `" + dbKillTable + "` ( " +
                "`name` varchar(42) NOT NULL, " +
                "`uid` varchar(42), " +
                "`kill` int(1) not null default 0, " +
                "`time` int(16) not null default 0);";
        try {
            executeQuery(query2, true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * create the statistics table
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

    /**
     * Delete all statistics by ID
     * @param list the list of IDs to delete
     * @throws SQLException
     */
    @Override
    public void deleteStatsByIDs(List<Integer> list) throws SQLException {

        StringBuilder buff = new StringBuilder("DELETE FROM `");
        buff.append(dbTable);
        buff.append("` WHERE `ROWID` IN (");

        boolean first = true;

        for (Integer i : list) {
            if (!first) {
                buff.append(',');
            }
            first = false;
            buff.append(i);
        }

        buff.append(");");

        executeQuery(buff.toString(), true);
    }

    /**
     * Get ALL statistics player names and entry IDs
     * @return a set of all entry IDs and player names
     * @throws SQLException
     */
    @Override
    public Map<Integer, String> getStatsIDsAndNames() throws SQLException {
        Map<Integer, String> map = new LinkedHashMap<>();
        ResultSet result = executeQuery("SELECT `oid`, `name` FROM `" + dbTable + "` WHERE 1 ORDER BY `kills` DESC;", false);
        while (result.next()) {
            map.put(result.getInt("oid"), result.getString("name"));
        }
        return map;
    }
}
