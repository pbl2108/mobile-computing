import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;



public class WhiteListGenerator {

	public long dirCount;
	public long indivdualMethodCount;
	public long totalCount;
	public HashMap<String, Integer> whiteListHashMap;
	public File folder;
	public File folder2;
	private static final String separatorSign = "/";
	public String folderRoot = "C:\\Users\\Dfosak\\workspace\\MobileComputing\\apks-decompile";
	public String folderRoot2 = "C:\\Users\\Dfosak\\workspace\\MobileComputing\\apks-decompile2";
	public String whiteListClassFile = "C:\\Users\\Dfosak\\Documents\\GitHub\\mobile-computing\\whitelist_classes.txt";
	public String whitelistLibraries = "C:\\Users\\Dfosak\\Documents\\GitHub\\mobile-computing\\whitelist_libraries2.txt";
	public ArrayList<String> whitelistLibsArray;
	
	
	public WhiteListGenerator() {
		this.folder = new File(this.folderRoot);
		this.folder2 = new File(this.folderRoot2);
		this.whiteListHashMap = new HashMap<String, Integer>();
		this.whitelistLibsArray = new ArrayList<String>();
		dirCount = 0;

	}
	
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		WhiteListGenerator whiteListGen = new WhiteListGenerator();
		whiteListGen.loadWhitelistLibs(whiteListGen.whitelistLibraries);
		whiteListGen.topLevelTraversal(whiteListGen.folder);
		whiteListGen.topLevelTraversal(whiteListGen.folder2);
		long endTime = System.currentTimeMillis();
		
		System.out.println("Total classes in Hash: "
				+ whiteListGen.whiteListHashMap.size());


		System.out.println("Total time: " + (endTime - startTime)
				+ " ms for " + whiteListGen.totalCount + " files");
		System.out.println("Time Per File " + (endTime - startTime)
				/ (double) whiteListGen.whiteListHashMap.size() + " ms/file");
		
		whiteListGen.printHashMap();
		
		long endTime2 = System.currentTimeMillis();
		
		System.out.println("Time to Print File " + (endTime2 - endTime) + " ms");

	}

	public void topLevelTraversal(File folder) {

			for (File fileEntry : folder.listFiles()) {
				dirCount++;

				if (fileEntry.isDirectory()) {
					listFilesForFolder(fileEntry);
				}else {
					// Do nothing
				}
			}
		

	}
	
	public void listFilesForFolder(File folder) {
		for (File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				listFilesForFolder(fileEntry);
			} else {
				if (fileEntry.getName().endsWith(".smali")) {
					try {
						this.parseDelimitedFile(fileEntry.getAbsolutePath());

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	private void parseDelimitedFile(String filePath)
			throws Exception {

		FileReader fr = new FileReader(filePath);
		BufferedReader br = new BufferedReader(fr);
		String currentRecord;
		String delimiter = "\\s+";
		String tokens[];
		String token;

		while ((currentRecord = br.readLine()) != null) {
				tokens = currentRecord.split(delimiter);
				token = tokens[tokens.length - 1];
				this.totalCount++;
				
				if (this.isWhitelisted(token)) {
					//System.out.println("WHITELISTED: " + token);
					break;
				}
				
				Integer count = whiteListHashMap.get(token);
				if (count == null) {
					whiteListHashMap.put(token, 1);
				}
				else {
					whiteListHashMap.put(token, count + 1);
				}
				
				break;
		}
		
		br.close();
		fr.close();

	}
	
	public void printHashMap() {

		try {

			ValueComparator bvc =  new ValueComparator(whiteListHashMap);
	        TreeMap<String,Integer> sorted_map = new TreeMap<String,Integer>(bvc);
	        sorted_map.putAll(whiteListHashMap);
			
			File file = new File(this.whiteListClassFile);

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			for (Iterator<Map.Entry<String, Integer>> iter1 = sorted_map.entrySet().iterator(); iter1.hasNext();) {
				Map.Entry<String, Integer> entry1 = iter1.next();
				bw.write(entry1.getKey() + " " + entry1.getValue() + "\n");

			}

			bw.close();
			fw.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Boolean isWhitelisted(String className) {
		for (String lib : this.whitelistLibsArray) {
			if (className.contains(lib))
				return true;
		}
		return false;
	}
	
	private void loadWhitelistLibs(String filePath) {
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

}

class ValueComparator implements Comparator<String> {

    Map<String, Integer> base;
    public ValueComparator(Map<String, Integer> base) {
        this.base = base;
    }

    // Note: this comparator imposes orderings that are inconsistent with equals.    
    public int compare(String a, String b) {
        if (base.get(a) >= base.get(b)) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys
    }
}


