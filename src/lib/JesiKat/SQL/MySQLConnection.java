package lib.JesiKat.SQL;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Jesika(Kaitlyn) Tremaine aka JesiKat
 *
 *	MySQLConnection.
 *	This class is a simple database connector with the basic useful methods such as getting the number of rows in a table,
 *  the names of columns in the table, etc.
 */
public class MySQLConnection {
	/*The host for the database, the username for the database, and the password*/
	private final String dbUrl,	dbUsername, dbPassword;

	/*The connection object*/
	private Connection databaseConnection;

	/**
	 * 
	 * @param host The host of the database server
	 * @param port The port that the server is on
	 * @param database The name of the database to connect to. If left as null, it will connect to the server without
	 * using a database
	 * @param username The username for the database.
	 * @param password The password for the database
	 * @return The resulting MySQLConnection. Returns null if there was an error.
	 */
	public static MySQLConnection newJDBCConnection(String table, String host, int port, String database, String username, String password) {
		try {
			return new MySQLConnection(table, host, port, database, username, password);
		} catch (Exception e) {
			return null;
		}
	}
	/**
	 * 
	 * @param host The host of the database server
	 * @param port The port that the server is on
	 * @param username The username for the database
	 * @param password The password for the database
	 * @return The resulting MySQLConnection. Returns null if there was an error
	 */
	public static MySQLConnection newJDBCConnection(String table, String host, int port, String username, String password) {
		return newJDBCConnection(table, host, port, "", username, password);
	}
	/**
	 * 
	 * @param host The host of the database server
	 * @param username The username for the database.
	 * @param password The password for the database
	 * @return The resulting MySQLConnection. Returns null if there was an error.
	 */
	public static MySQLConnection newJDBCConnection(String table, String host, String username, String password, int port) {
		return newJDBCConnection(table, host, port, "", username, password);
	}

	public MySQLConnection(String table, String host, int port, String database, String username, String password) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		this.dbUrl = host + ":" + port + "/" + database;
		this.dbUsername = username;
		this.dbPassword = password;
		Class.forName("com.mysql.jdbc.Driver").newInstance();
	}

	/**
	 * @return True if the connection was made successfully, false if otherwise.
	 */
	public boolean connect() { return connect(false); }
	/**
	 * @param printerror If this is true, this method will print an error if there is one and return false
	 * @return True if the connection was made successfully, false if otherwise.
	 */
	public boolean connect(boolean printerror) {
		try {
			this.databaseConnection = DriverManager.getConnection("jdbc:mysql://" + this.dbUrl + "?autoReconnect=true", this.dbUsername, this.dbPassword);
			if (this.databaseConnection==null) return false;
			return true;
		} catch (SQLException e) {
			if (printerror) e.printStackTrace();
			return false;
		}
	}

	/**
	 * @return True if the disconnect was successful, false if otherwise.
	 */
	public boolean disconnect() { return disconnect(false); }
	/**
	 * @param printerror If this is true, this method will print an error if there is one and return false.
	 * @return True if the disconnect was successful, false if otherwise.
	 */
	public boolean disconnect(boolean printerror) {
		try {
			this.databaseConnection.close();
			return true;
		} catch (SQLException e) {
			if (printerror) e.printStackTrace();
			return false;
		}
	}

	/**
	 * @param query The Query to send to the SQL server.
	 * @param modifies If the Query modifies the database, set this to true. If not, set this to false
	 * @return If {@value modifies} is true, returns a valid ResultSet obtained from the Query. If {@value modifies} is false, returns null.
	 * @throws SQLException if the Query had an error or there was not a valid connection.
	 */
	public ResultSet executeQuery(final String query, final boolean modifies) throws SQLException {
		
		
		Statement statement = this.databaseConnection.createStatement();
		if (modifies) {
			statement.execute(query);
			return null;
		} else {
			return statement.executeQuery(query);
		}
	}

	/**
	 * @param database The database to check for existence.
	 * @return true if the database exists, false if there was an error or the database doesn't exist.
	 * 
	 * This method looks through the information schema that comes with a MySQL installation and
	 * checks to see if a certain database exists.
	 */
	public boolean databaseExists(String database) {
		String format = "SELECT * FROM `information_schema`.`schemata` WHERE `SCHEMA_NAME` = '$DB' ;";
		try {
			return this.executeQuery(format.replace("$DB", database), false).first();
		} catch (SQLException e) {
			return false;
		}
	}

	/**
	 * @param database The database to check for the table in.
	 * @param table The table to check for existence.
	 * @return true if the table exists, false if there was an error or the database doesn't exist.
	 * 
	 * This method looks through the information schema that comes with a MySQL installation and checks
	 * if a certain table exists within a database.
	 */
	public boolean tableExists(String database, String table) {
		String format = "SELECT * FROM `information_schema`.`TABLES` WHERE TABLE_SCHEMA = '$DB' && TABLE_NAME = '$TABLE';";
		try {
			return this.databaseConnection.createStatement().executeQuery(format.replace("$DB", database).replace("$TABLE", table)).first();
		} catch (SQLException e) {
			return false;
		}
	}

	/**
	 * @param database The database the table is in.
	 * @param table The table to get the row count from.
	 * @return the number of rows in the table.
	 * 
	 * This method loops through all rows and returns the row count.
	 */
	public int getRowCount(String database, String table) {
		String format = "SELECT * FROM `$DB`.`$TABLE`;";
		int rows = 0;
		try {
			ResultSet set = executeQuery(format.replace("$DB", database).replace("$TABLE", table), false);
			while (set.next()) { rows++; }
		} catch (SQLException e) {
			return 0;
		}
		return rows;
	}

	/**
	 * @param database The database the table is in.
	 * @param table The table to get the columns from.
	 * @return A String array containing the names of all the columns from the table in the order they are in in the table.
	 * @throws SQLException
	 * 
	 * This method loops through all columns of a table within a database and adds their name to an array, then returns the
	 * array.
	 */
	public String[] getColumns(String database, String table) throws SQLException {
		String format = "SELECT * FROM `$DB`.`$TABLE`;";
		ResultSet set = executeQuery(format.replace("$DB", database).replace("$TABLE", table), false);
		int count = set.getMetaData().getColumnCount();
		ArrayList<String> columns = new ArrayList<String>();
		for (int i = 1; i<=count; i++) {
			columns.add(set.getMetaData().getColumnName(i));
		}
		return columns.toArray(new String[columns.size()]);
	}

	/**
	 * @param database The database to get the tables from.
	 * @return A String array containing all of the tables in the database in alphabetical order.
	 * @throws SQLException
	 */
	public String[] getTables(String database) throws SQLException {
		String format = "SELECT `TABLE_NAME` FROM `information_schema`.`TABLES` WHERE TABLE_SCHEMA='$DB';";
		ResultSet set = executeQuery(format.replace("$DB", database), false);
		ArrayList<String> tables = new ArrayList<String>();
		while (set.next()) {
			tables.add(set.getString(1));
		}
		List<String> sorted = Arrays.asList(tables.toArray(new String[tables.size()]));
		Collections.sort(sorted, String.CASE_INSENSITIVE_ORDER);
		return sorted.toArray(new String[tables.size()]);
	}

	/**
	 * @return A String array of all databases excluding those that are included with the MySQL installation.
	 * @throws SQLException
	 */
	public String[] getDatabases() throws SQLException { return getDatabases(true); }

	/**
	 * @param onlyAdded determines whether or not the method will get all databases or if the method should
	 * exclude the databases that are included with the MySQL installation.
	 * @return A String array of all databases. If {@value onlyAdded} is true, returns all databases. If {@value onlyAdded} is false,
	 * returns all databases excluding those that are included with the MySQL installation.
	 * @throws SQLException
	 */
	public String[] getDatabases(boolean onlyAdded) throws SQLException {
		ResultSet set = executeQuery("SHOW DATABASES;", false);
		ArrayList<String> databases = new ArrayList<String>();
		while (set.next()) {
			String database = set.getString(1);
			if (onlyAdded) {
				if (!(database.equalsIgnoreCase("information_schema") || database.equalsIgnoreCase("mysql") || database.equalsIgnoreCase("performance_schema"))) {
					databases.add(database);
				}
			} else 
				databases.add(database);
		}
		List<String> sorted = Arrays.asList(databases.toArray(new String[databases.size()]));
		Collections.sort(sorted, String.CASE_INSENSITIVE_ORDER);
		return sorted.toArray(new String[databases.size()]);
	}

	/**
	 * @return The raw connection that this class uses for database interactions.
	 */
	public Connection getRawConnection() { return this.databaseConnection; }
}
