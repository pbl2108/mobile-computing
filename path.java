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
	
	public static void main(String[] args) {
		String target = "/Users/xuyiming/Desktop/tmp";
		File file = new File(target);
		int count = 0;
		FileWriter fw = null;
		BufferedWriter out = null;
		File tmp;
		ArrayList<String> fail = new ArrayList<String>();
		try {
			fw = new FileWriter("/Users/xuyiming/Desktop/main_smali2.txt");
			out = new BufferedWriter(fw);
		
		 for (File fileEntry : file.listFiles()) {
			 if (fileEntry.isDirectory()) {
				 tmp = toMainFolder(fileEntry);
					if (tmp == null) {
						if (fileEntry.getAbsolutePath().endsWith("-byte"))
							fail.add(fileEntry.getAbsolutePath());
						continue;
					} else {
						fileEntry = tmp;
					}
				out.write("--------------------\n");
				out.write(fileEntry.getAbsolutePath() + "\n");
				listFilesForFolder(fileEntry, out);
					count ++;
			 }
		 }
		 out.write("\n\nThe total number: " + count + "\n\nThe apps failing to get correct folder:\n");
		 
		 for (int i = 0; i < fail.size(); i++)
			 out.write(fail.get(i) + "\n");
		 out.close();
		 
		} catch (Exception e) {
			System.out.println("aaaa");
		}
		
		
		
	}
	
	static public void listFilesForFolder(File folder, BufferedWriter bw) throws IOException {
		
		for (File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
	            listFilesForFolder(fileEntry, bw);
	        } else {
	        	bw.write(fileEntry.getName() + "\n");
	        }
		}
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

				buff = extractPackageName(buff);
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

	static private String extractPackageName(String line) {

		int index = line.indexOf(packageIndicator);
		if (index == -1)
			return null;
		else
			index += packageIndicator.length();	// move to the start of the name

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
}
