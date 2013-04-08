import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


public class path {
	
	private static final String manifestName = "AndroidManifest.xml";
	private static final String packageIndicator = "package=\"";
	private static final String separatorSign = "/";
	private static String whitelistLibraries = "C:\\Users\\Dfosak\\Documents\\GitHub\\mobile-computing\\whitelist_libraries2.txt";
	private static ArrayList<String> whitelistLibsArray;
	
//	public static void main(String[] args) {
//		//String target = "/Users/xuyiming/Desktop/tmp2";
//		String target = "C:\\Users\\Dfosak\\workspace\\MobileComputing\\apks-decompile";
//		whitelistLibsArray = new ArrayList<String>();
//		loadWhitelistLibs(whitelistLibraries);
//		File file = new File(target);
//		int count = 0;
//		FileWriter fw = null;
//		BufferedWriter out = null;
//		File tmp;
//		ArrayList<String> fail = new ArrayList<String>();
//		try {
//			//fw = new FileWriter("/Users/xuyiming/Desktop/main_smali2.txt");
//			fw = new FileWriter("C:\\Users\\Dfosak\\Documents\\GitHub\\mobile-computing\\mainComponent.txt");
//			out = new BufferedWriter(fw);
//		
//		 for (File fileEntry : file.listFiles()) {
//			 if (fileEntry.isDirectory()) {
//				 tmp = toMainActivityFolder(fileEntry);
//					if (tmp == null) {
//						if (!fileEntry.getAbsolutePath().endsWith("-byte"))
//							continue;
//						else{
//							//fileEntry remains the top level of the smali directory
//						}
//					} else {
//						fileEntry = tmp;
//					}
//				out.write("--------------------\n");
//				out.write(fileEntry.getAbsolutePath() + "\n");
//				listFilesForFolder(fileEntry, out);
//				count ++;
//			 }
//		 }
//		 out.write("\n\nThe total number: " + count + "\n\nThe apps failing to get correct folder:\n");
//		 
//		 for (int i = 0; i < fail.size(); i++)
//			 out.write(fail.get(i) + "\n");
//		 out.close();
//		 
//		} catch (Exception e) {
//			System.out.println("aaaa");
//		}
//		
//		
//		
//	}
	
	static private void loadWhitelistLibs(String filePath) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(filePath));
			String str;
			while ((str = in.readLine()) != null) {
				str.replace("/", separatorSign);
				whitelistLibsArray.add(str);
			}
			// Close buffered reader
			in.close();

		} catch (Exception e) {
			// If something unexpected happened
			// print exception information and quit
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	static public void listFilesForFolder(File folder, BufferedWriter bw) throws IOException {
		for (File fileEntry : folder.listFiles()) {
			if (isWhitelisted(fileEntry)) {
				System.out.println("WHITELISTED: " + fileEntry.getPath());
				continue;
			}
		
	        if (fileEntry.isDirectory()) {
	            listFilesForFolder(fileEntry, bw);
	        } else {
	        	if (fileEntry.getName().endsWith(".smali")) 
	        		bw.write(fileEntry.getName() + "\n");
	        }
			
		}
	}
	
	static private Boolean isWhitelisted(File fileEntry) {
		for (String lib : whitelistLibsArray) {
			if (fileEntry.getPath().contains(lib))
				return true;
		}
		return false;
	}
	
	/* check Manifest.xml, if has get package name
	 * else return NULL means it is not a smali folder */
	static private String getMainPackageName(File folder) throws IOException {

		String buff;
		BufferedReader in = null;

		try {
			//FileInputStream fs = new FileInputStream(manifestName);
			in = new BufferedReader(new FileReader(folder.getAbsolutePath() + separatorSign + manifestName));

			while ((buff = in.readLine()) != null) {

				buff = extractPackageName(buff, packageIndicator);
				if (buff != null) {
					in.close();
					return buff;
				}
			}

		} catch (Exception e) {
			// do nothing
		}

		in.close();
		return null;
	}

	static private String extractPackageName(String line, String sign) {

		int index = line.indexOf(sign);
		if (index == -1)
			return null;
		else
			index += sign.length();	// move to the start of the name

		int endIndex = line.indexOf("\"", index);
		if (endIndex == -1)
			return null;

		return line.substring(index, endIndex);
	}

	/* go to the main component folder as the name indicated
	 * return the destination folder object */
	static private File toMainFolder(File folder) {

		String packageName = null;
		
		try {
			packageName = getMainPackageName(folder);
		} catch (Exception e) {
			
		}
			
		if (packageName == null)
			return null;

		File tmp = new File(folder.getAbsolutePath() + separatorSign + "smali" + separatorSign + packageName.replace(".", separatorSign));

		if (tmp.exists())
			return tmp;
		else
			return null;
	}
	
	static private String getMainActivityPath(File folder) throws IOException {
		
		String buff;
		String nearestName = null;
		BufferedReader in = null;

		try {
			in = new BufferedReader(new FileReader(folder.getAbsolutePath() + separatorSign + manifestName));

			while ((buff = in.readLine()) != null) {

				/* if the line is activity attributes, get the name of this attribute */
				if (buff.contains("<activity") && buff.contains("android:name=\""))
					nearestName = extractPackageName(buff, "android:name=\"");
				else if (buff.contains("android.intent.action.MAIN")) {
					in.close();
					return nearestName;
				}
			}

		} catch (Exception e) {
			// do nothing
		}

		in.close();
		return null;
	}
	
	/* return the file object of the folder containing main activity 
	 * return null means no such folder or same with main component folder */
	static private File toMainActivityFolder(File folder) {
		
		String activityName = null;
		
		try {
			activityName = getMainActivityPath(folder);
			if (activityName != null)
				System.out.println(activityName);
		} catch (Exception e) {
			
		}
			
		if (activityName == null || activityName.startsWith(".") || !activityName.contains("."))
			return null;

		File tmp = new File(folder.getAbsolutePath() + separatorSign + "smali" + separatorSign + activityName.substring(0, activityName.lastIndexOf(".")).replace(".", separatorSign));

		if (tmp.exists())
			return tmp;
		else
			return null;
	}
}
