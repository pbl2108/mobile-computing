import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.lucene.util.OpenBitSet;

public class MySQLAccess {
	public static final double sizeInterval = 0.45;
	private Connection connect = null;
	// private static final String connectionString =
	// "jdbc:mysql://localhost/test?user=apps_user&password=apps_userpw";
	private static final String connectionString = "jdbc:mysql://localhost/test_new";
	private Statement statement = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;

	public MySQLAccess() {
		try {
			Class.forName("com.mysql.jdbc.Driver");

			// Setup the connection with the DB
			connect = DriverManager.getConnection(connectionString);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void insert(String name, AppVector featureVector, String md5,
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
		// Statements allow to issue SQL queries to the database
		statement = connect.createStatement();
		// Result set get the result of the SQL query
		resultSet = statement.executeQuery("select * from test_new.appstable1");
		// printResultSetToConsole(resultSet);
	}

	public void read(String app1, String app2) throws Exception {
		// Statements allow to issue SQL queries to the database
		statement = connect.createStatement();
		// Result set get the result of the SQL query
		String query = "select * from test_new.appstable1 where eid in (\"" + app1
				+ "\",\"" + app2 + "\")";
		resultSet = statement.executeQuery(query);
		printResultSetToConsole(resultSet);
	}

	public ResultSet readResultSet(String app1, String app2) throws Exception {
		// Statements allow to issue SQL queries to the database
		PreparedStatement smt = connect
				.prepareStatement("select * from test_new.appstable1 where eid in(?,?)");
		smt.setString(1, app1);
		smt.setString(2, app2);
		// // Result set get the result of the SQL query
		// String query = "select * from test_new.appstable1 where eid in (\"" +
		// app1
		// + "\",\"" + app2 + "\")";
		return smt.executeQuery();
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
			String hash = resultSet.getString("author_md5");

			System.out.println("EID: " + eid);
			System.out.println("Creator: " + creator);
			System.out.println("HASH: " + hash);
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

	//isAuthorDifferent isSize
	public boolean[] isAuthorAndSize(String app1, String app2) {
		String[] a = new String[2];
		String[] b = new String[2];
		long[] asset_size = new long[2];
		ResultSet rs = null;
		PreparedStatement smt = null;
		try {

			smt = connect
					.prepareStatement("select * from test.appstable1 where eid in(?,?)");
			smt.setString(1, app1);
			smt.setString(2, app2);
			rs = smt.executeQuery();
			int i = 0;
			while (rs.next()) {
				a[i] = rs.getString("creator");
				b[i] = rs.getString("author_md5");
				asset_size[i] = rs.getLong("asset_size");
				i++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (smt != null) {
					smt.close();
					rs.close();
				}
			} catch (Exception e1) {
				System.out.print("Can't close Resultset");
			}
		}

		boolean isAuthor;
		if ((a[0] == null || a[1] == null) && (b[0] == null || b[1] == null)) {
			isAuthor = true;
		}
		isAuthor = !(a[0].compareToIgnoreCase(a[1]) == 0)
				&& !(b[0].compareToIgnoreCase(b[1]) == 0);

		boolean isSize;
		if (asset_size[0] == 0 || asset_size[1] == 0) {
			isSize = false;
		}
		double ratio = (asset_size[0] * 1.0) / asset_size[1];
		isSize = ratio <= 1 + sizeInterval && ratio >= 1 - sizeInterval;

		return new boolean[] { isAuthor, isSize };
	}
	
	public boolean isAuthorDifferent(String app1, String app2) {
		String[] a = new String[2];
		String[] b = new String[2];
		ResultSet rs = null;
		PreparedStatement smt = null;
		
		boolean creator = true;
		boolean md5 = true;
		
		try {

			smt = connect
					.prepareStatement("select * from test_new.appstable1 where eid in(?,?)");
			smt.setString(1, app1);
			smt.setString(2, app2);
			rs = smt.executeQuery();
			int i = 0;
			while (rs.next()) {
				a[i] = rs.getString("creator");
				//b[i] = rs.getString("contact_phone");
				 b[i] = rs.getString("author_md5");
				i++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (smt != null) {
					smt.close();
					rs.close();
				}
			} catch (Exception e1) {
				System.out.print("Can't close Resultset");
			}
		}

//		if ((a[0] == null || a[1] == null) && (b[0] == null || b[1] == null)) {
//			return true;
//		}
		
		if (a[0] == null || a[1] == null)
			creator = true;
		else
			creator = !(a[0].compareToIgnoreCase(a[1]) == 0);
		
		if (b[0] == null || b[1] == null)
			md5 = true;
		else
			md5 = !(b[0].compareToIgnoreCase(b[1]) == 0);
		// System.out.println("Creator: " + a[0] + " +++++++++++++++++++++ " +
		// a[1]);

		return creator && md5;
	}

	HashMap<String, Integer> tmpHashMap = new HashMap<String, Integer>();

	public void getAllLibsAndPermissions(String field) {
		String[] a;
		String val;
		int i;
		try {
			read();
			while (resultSet.next()) {
				val = resultSet.getString(field);
				if (val.isEmpty() || val == null || val.length() == 2)
					continue;
				a = val.split("\", \"");
				a[0] = a[0].substring(2);
				a[a.length - 1] = a[a.length - 1].replaceAll("\"]", "");
				for (i = 0; i < a.length; i++) {
					if (tmpHashMap.containsKey(a[i])) {
						tmpHashMap.put(a[i], tmpHashMap.get(a[i]) + 1);
						continue;
					}
					tmpHashMap.put(a[i], 0);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void getAllLibsAndPermissionsME(String field) {
		StringBuilder sb = new StringBuilder();
		String[] a;
		try {
			read();
			while (resultSet.next()) {
				sb.append(resultSet.getString(field));
				// String val = resultSet.getString(field);
				if (sb.length() == 2)
					continue;
				sb.delete(0, 2);
				sb.delete(sb.length() - 2, sb.length());
				a = sb.toString().split("\", \"");
				// a[0] = a[0].substring(2);
				// a[a.length - 1] = a[a.length - 1].replaceAll("\"]", "");
				for (String t : a) {
					if (tmpHashMap.containsKey(t)) {
						tmpHashMap.put(t, tmpHashMap.get(t) + 1);
						continue;
					}
					tmpHashMap.put(t, 0);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void temp() {
		try {
			statement = connect.createStatement();
			// Result set get the result of the SQL query
			resultSet = statement
					.executeQuery("select * from test_new.appstable1 where eid = 'CommunityClinical.PocketRx-1'");

			while (resultSet.next()) {
				// It is possible to get the columns via name
				// also possible to get the columns via the column number
				// which starts at 1
				// e.g. resultSet.getSTring(2);
				InputStream bos = resultSet.getBinaryStream("app_vector");
				ObjectInputStream out = new ObjectInputStream(bos);

				AppVector ap = (AppVector) out.readObject();
				OpenBitSet x = ap.LogicVector;
				OpenBitSet y = ap.ContentVector;
				System.out.println("Y: " + x.get(1234));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {

		long start = System.currentTimeMillis();
		MySQLAccess access = new MySQLAccess();

		// Get the size
		boolean size = access.isSizeSimilar("a.b.mart-1", "a.co.action-23");
		System.out.println("Size: " + size);

		// access.temp();
		// access.loadAuthorsMapIntoDB_Batch("/media/peter/GufretBot/000Linux/Dropbox/mobile-computing/Results423/apkSignatures_ds67.txt");
		// AppVector app = new AppVector();
		// OpenBitSet x = new OpenBitSet(27000);
		// OpenBitSet y = new OpenBitSet(5000);
		// x.set(27000);
		// y.set(5000);

		try {
			// access.loadVectorsIntoDB_Batch(new File(
			// "/media/peter/GufretBot/000Linux/Dropbox/mobile-computing/120000/"));
			// access.insert("name", new AppVector(x, y), "MD5", 12.2, 12.3,
			// 12.4);
			// access.temp();
			// access.read("com.skyd.bestpuzzle.n809-1",
			// "air.gnpgmfrh-1000001");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// System.out.println("START:" + System.currentTimeMillis());
		//
		// MySQLAccess access = new MySQLAccess();
		//
		// access.getAllLibsAndPermissions("lib_names");
		// System.out.println("Write to File");
		// access.writeHashMapToTXT(null, "lib_names", true, true);
		// //access.writeHashMapToTXT(null, "lib_names", true, false);
		System.out.println("TIME:" + (System.currentTimeMillis() - start));
	}

	public void writeHashMapToTXT(HashMap<String, Integer> map,
			String fileName, boolean usePathSeparator, boolean writeCount) {
		if (map == null)
			map = tmpHashMap;
		String t = null;
		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			// Creates outputLogs Directory if it Does not Exist
			File directory = new File("outputLogs/");
			directory.mkdirs();
			File file = new File("outputLogs/" + fileName + ".txt");
			file.createNewFile();

			fw = new FileWriter(file.getAbsoluteFile(), true);
			bw = new BufferedWriter(fw);

			for (Iterator<Map.Entry<String, Integer>> iter1 = map.entrySet()
					.iterator(); iter1.hasNext();) {
				Map.Entry<String, Integer> entry1 = iter1.next();
				t = entry1.getKey();
				if (usePathSeparator)
					t = "/" + t.replace('.', '/');
				if (writeCount)
					bw.write(t + " " + entry1.getValue() + "\n");
				else
					bw.write(t + "\n");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				bw.close();
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void loadAuthorsMapIntoDB(String filePath) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(filePath));
			String delimiter = "\\s+";
			String currentLine;
			String tokens[];

			while ((currentLine = in.readLine()) != null) {
				tokens = currentLine.split(delimiter);
				this.updateAuthor(tokens[0], tokens[1]);
			}
			// Close buffered reader
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadAuthorsMapIntoDB_Batch(String filePath) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(filePath));
			String delimiter = "\\s+";
			String currentLine;
			String tokens[];
			String[][] authors = new String[128000][2];

			connect.setAutoCommit(false);

			PreparedStatement stm = connect
					.prepareStatement("UPDATE test_new.appstable1 SET author_md5 = ? WHERE eid = ?");

			int i = 0;
			while ((currentLine = in.readLine()) != null) {
				tokens = currentLine.split(delimiter);
				stm.setString(1, tokens[1]);
				stm.setString(2, tokens[0]);
				stm.addBatch();
				authors[i][0] = tokens[0];
				authors[i][1] = tokens[1];
				i++;
			}
			int[] results = stm.executeBatch();
			stm.close();
			// Close buffered reader
			in.close();

			PreparedStatement stm1 = connect
					.prepareStatement("INSERT INTO test_new.appstable1 (eid, author_md5) VALUES (?, ?) ON DUPLICATE KEY UPDATE app_type='D'");
			int length = results.length;
			for (int k = 0; k < length; k++) {
				if (results[k] <= 0) {
					String t = authors[k][0];
					if (t.length() > 100)
						t = t.substring(0, 99);
					stm1.setString(1, t);
					stm1.setString(2, authors[k][1]);
					stm1.addBatch();
					System.out
							.println(authors[k][0] + " ---- " + authors[k][1]);
				}
			}
			stm1.executeBatch();
			stm1.close();

			connect.commit();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void loadVectorsIntoDB_Batch(File file) throws Exception {

		/* read all serial files in the folder */
		for (File entry : file.listFiles()) {

			if (entry.isDirectory()) {
				loadVectorsIntoDB_Batch(entry);
				continue;
			}

			if (entry.isFile() && !entry.getName().endsWith(".ser"))
				continue;

			FileInputStream fis = new FileInputStream(entry.getAbsoluteFile());
			ObjectInputStream ois = new ObjectInputStream(fis);
			HashMap<String, AppVector> buff = (HashMap<String, AppVector>) ois
					.readObject();
			ois.close();

			connect.setAutoCommit(false);
			PreparedStatement stm = connect
					.prepareStatement("UPDATE test_new.appstable1 SET app_vector = ? WHERE eid = ?");
			for (Iterator<Map.Entry<String, AppVector>> iter1 = buff.entrySet()
					.iterator(); iter1.hasNext();) {
				Map.Entry<String, AppVector> x = iter1.next();

				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(bos);
				oos.writeObject(x.getValue());
				oos.flush();
				oos.close();
				bos.close();

				byte[] fv = bos.toByteArray();

				stm.setObject(1, fv);
				stm.setString(2, x.getKey());
				stm.addBatch();
			}
			stm.executeBatch();
			connect.commit();
			stm.close();
			System.out.println("500 inserted");
			// System.out.println(bitSetsHashMap.size());
		}
	}

	private void updateAuthor(String appName, String authorHash) {
		// Statements allow to issue SQL queries to the database
		try {
			statement = connect.createStatement();

			String query = "UPDATE test_new.appstable1 SET contact_phone = \""
					+ authorHash + "\" WHERE eid = \"" + appName + "\"";
			statement.executeUpdate(query);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Result set get the result of the SQL query
	}

	public boolean isSizeSimilar(String app1, String app2) {
		PreparedStatement smt;
		long[] result = { 0, 0 };
		int i = 0;
		try {
			smt = connect
					.prepareStatement("select * from test_new.appstable1 where eid in(?, ?)");
			smt.setString(1, app1);
			smt.setString(2, app2);
			ResultSet rs = smt.executeQuery();
			while (rs.next()) {
				result[i++] = rs.getLong("asset_size");
			}
			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (result[0] == 0 || result[1] == 0) {
			return false;
		}
		double ratio = (result[0] * 1.0 )/ result[1];
		return ratio <= 1 + sizeInterval && ratio >= 1 - sizeInterval;
	}

	public long getSize(String name) {
		PreparedStatement smt;
		long result = 0;
		try {
			smt = connect
					.prepareStatement("select * from test_new.appstable1 where eid in(?)");
			smt.setString(1, name);
			ResultSet rs = smt.executeQuery();
			while (rs.next()) {
				result = rs.getLong("asset_size");
			}
			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
}