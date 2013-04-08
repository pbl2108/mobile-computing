import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
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
		connect = DriverManager.getConnection("jdbc:mysql://localhost/appsdb?"
				+ "user=apps_user&password=apps_userpw");

		// Statements allow to issue SQL queries to the database
		statement = connect.createStatement();
		// Result set get the result of the SQL query
		resultSet = statement.executeQuery("select * from appsdb.apps");
		printResultSetToConsole(resultSet);
	}

	private void printResultSetToConsole(ResultSet resultSet) throws SQLException {
		// ResultSet is initially before the first data set
		while (resultSet.next()) {
			// It is possible to get the columns via name
			// also possible to get the columns via the column number
			// which starts at 1
			// e.g. resultSet.getSTring(2);
			String name = resultSet.getString("name");
			String md5 = resultSet.getString("md5");
			Double x = resultSet.getDouble("x");
			Double y = resultSet.getDouble("y");
			Double z = resultSet.getDouble("z");
			OpenBitSet feature_vector = new OpenBitSet();

			ByteArrayInputStream fv_blob;

			ObjectInputStream ins;
			try {

				fv_blob = new ByteArrayInputStream(
						resultSet.getBytes("feature_vector"));

				ins = new ObjectInputStream(fv_blob);

				feature_vector = (OpenBitSet) ins.readObject();

				// System.out.println("Object in value ::"+mc.getSName());
				ins.close();

			} catch (Exception e) {

				e.printStackTrace();
			}

			System.out.println("name: " + name);
			System.out.println("md5: " + md5);
			System.out.println("X: " + x);
			System.out.println("Y: " + y);
			System.out.println("Z: " + z);
			System.out.println("GET 100th bit is: " + feature_vector.get(100));
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

}