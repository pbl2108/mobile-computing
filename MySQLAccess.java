import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.lucene.util.OpenBitSet;

public class MySQLAccess {
	private Connection connect = null;
	private static final String connectionString = "jdbc:mysql://localhost/test?user=apps_user&password=apps_userpw";
	private Statement statement = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;

	public void insert(String name, OpenBitSet featureVector, String md5,
			Double x, Double y, Double z) throws Exception {

		try {
			// This will load the MySQL driver, each DB has its own driver
			Class.forName("com.mysql.jdbc.Driver");
			// Setup the connection with the DB
			connect = DriverManager
					.getConnection("jdbc:mysql://localhost/appsdb?"
							+ "user=apps_user&password=apps_userpw");

			preparedStatement = connect
					.prepareStatement("INSERT INTO appsdb.apps (name, feature_vector, md5, x, y, z) VALUES (?, ?, ?, ?, ?, ?)");

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(featureVector);
			oos.flush();
			oos.close();
			bos.close();
			byte[] fv = bos.toByteArray();

			preparedStatement.setString(1, name);
			preparedStatement.setObject(2, fv);
			preparedStatement.setString(3, md5);
			preparedStatement.setDouble(4, x);
			preparedStatement.setDouble(5, y);
			preparedStatement.setDouble(6, z);
			preparedStatement.executeUpdate();

		} catch (Exception e) {
			throw e;
		} finally {
			close();
		}
	}

	public void read() throws Exception {
		Class.forName("com.mysql.jdbc.Driver");
		// Setup the connection with the DB
		connect = DriverManager.getConnection(connectionString);

		// Statements allow to issue SQL queries to the database
		statement = connect.createStatement();
		// Result set get the result of the SQL query
		resultSet = statement
				.executeQuery("select * from test.appstable1 limit 10");
		printResultSetToConsole(resultSet);
	}

	public void read(String app1, String app2) throws Exception {
		Class.forName("com.mysql.jdbc.Driver");
		// Setup the connection with the DB
		connect = DriverManager.getConnection(connectionString);

		// Statements allow to issue SQL queries to the database
		statement = connect.createStatement();
		// Result set get the result of the SQL query
		String query = "select * from test.appstable1 where eid in (\"" + app1
				+ "\",\"" + app2 + "\")";
		System.out.println(query);
		resultSet = statement.executeQuery(query);
		printResultSetToConsole(resultSet);
	}

	public ResultSet readResultSet(String app1, String app2) throws Exception {
		Class.forName("com.mysql.jdbc.Driver");
		// Setup the connection with the DB
		connect = DriverManager.getConnection(connectionString);

		// Statements allow to issue SQL queries to the database
		statement = connect.createStatement();
		// Result set get the result of the SQL query
		String query = "select * from test.appstable1 where eid in (\"" + app1
				+ "\",\"" + app2 + "\")";
		System.out.println(query);
		return statement.executeQuery(query);
	}

	private void printResultSetToConsole(ResultSet resultSet)
			throws SQLException {
		// ResultSet is initially before the first data set
		while (resultSet.next()) {
			// It is possible to get the columns via name
			// also possible to get the columns via the column number
			// which starts at 1
			// e.g. resultSet.getSTring(2);
			String eid = resultSet.getString("eid");
			String creator = resultSet.getString("creator");

			System.out.println("EID: " + eid);
			System.out.println("Creator: " + creator);
		}
	}

	@SuppressWarnings("unused")
	private void writeMetaData(ResultSet resultSet) throws SQLException {
		System.out.println("The columns in the table are: ");

		System.out.println("Table: " + resultSet.getMetaData().getTableName(1));
		for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
			System.out.println("Column " + i + " "
					+ resultSet.getMetaData().getColumnName(i));
		}
	}

	// You need to close the resultSet
	private void close() {
		try {
			if (resultSet != null) {
				resultSet.close();
			}

			if (statement != null) {
				statement.close();
			}

			if (connect != null) {
				connect.close();
			}
		} catch (Exception e) {

		}
	}

	public boolean IsAuthorEqual(String app1, String app2) {
		String[] a = new String[2];
		try {
			ResultSet resultSet = this.readResultSet(app1, app2);
			int i = 0;
			while (resultSet.next()) {
				a[i] = resultSet.getString("creator");
				System.out.println("Creator: " + a[i]);
				i++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return a[0].compareToIgnoreCase(a[1]) == 0;
	}

	public static void main(String[] args) {
		MySQLAccess access = new MySQLAccess();
		try {
			boolean res = access
					.IsAuthorEqual("a.a.lens-3", "a.accelerodraw-3");
			System.out.println(res);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}