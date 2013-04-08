import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.lucene.util.OpenBitSet;

public class SmaliParser {

	private static final String manifestName = "AndroidManifest.xml";
	private static final String packageIndicator = "package=\"";
	private static final String separatorSign = "/";

	private static final String featuresMapPath = "smali-methods.txt";
	private static final String outputFeaturePath = "testRun.txt";
	private static final String whiteListLibraries = "whitelist_libraries.txt";
	
//	private static final String folderRoot = "/home/Dfosak/";
//	private static final String featuresMapPath = "/home/Dfosak/Desktop/mobile-computing/smali-methods.txt";
//	private static final String outputFeaturePath = "/home/Dfosak/Desktop/mobile-computing/testRun.txt";
//	private static final String whiteListLibraries = "/home/Dfosak/Desktop/mobile-computing/whitelist_libraries.txt";
	
//	public String folderRoot = "C:\\Users\\Dfosak\\workspace\\MobileComputing\\apks-decompile";
//	public String hashMapFile = "C:\\Users\\Dfosak\\Documents\\GitHub\\mobile-computing\\smali-methods.txt";
//	public String outputFeatureFile = "C:\\Users\\Dfosak\\Documents\\GitHub\\mobile-computing\\testRun.txt";
//	public String outputBitVectorFile = "C:\\Users\\Dfosak\\Documents\\GitHub\\mobile-computing\\bitSets.csv";
//	public String outputSimFile = "C:\\Users\\Dfosak\\Documents\\GitHub\\mobile-computing\\similarities.txt";
//	public String whitelistLibraries = "C:\\Users\\Dfosak\\Documents\\GitHub\\mobile-computing\\whitelist_libraries.txt";

	//PETER
	// public String folderRoot = "/home/peter/columbia/mob/sample-decompile";
	// public String hashMapFile =
	// "/home/peter/columbia/mob/helper_txts/smali-methods.txt";
	// public String whitelistLibraries =
	// "/home/peter/columbia/mob/helper_txts/whitelist_libraries.txt";
	// public String outputFeatureFile =
	// "/home/peter/columbia/mob/helper_txts/output/testRun.txt";
	// public String outputBitVectorFile =
	// "/home/peter/columbia/mob/helper_txts/output/bitSets.csv";
	// public String outputSimFile =
	// "/home/peter/columbia/mob/helper_txts/output/similarities.txt";

	public File folder;
	public HashMap<String, Integer> featuresHashMap;
	public HashMap<String, OpenBitSet> bitSetsHashMap;
	public HashMap<String, Long> recognizedHashMap;
	public HashMap<String, Long> unRecognizedHashMap;
	public HashMap<String, Integer> whiteListHashMap;
	public int bitSetsCount;
	public long recCount;
	public long unRecCount;
	public long totalCount;
	public long indivdualMethodCount;
	public double jaccardThreshold = .70;
	public long dirCount;
	public int featuresCount;


	public SmaliParser() {
		this.featuresHashMap = new HashMap<String, Integer>();
		this.recognizedHashMap = new HashMap<String, Long>();
		this.unRecognizedHashMap = new HashMap<String, Long>();
		this.bitSetsHashMap = new HashMap<String, OpenBitSet>();
		this.whiteListHashMap = new HashMap<String, Integer>();
		this.recCount = 0;
		this.unRecCount = 0;
		this.totalCount = 0;
		this.indivdualMethodCount = 0;
		this.bitSetsCount = 0;
		this.dirCount = 0;
		this.featuresCount  = 0;	//keeps track of the total number of feature vectors. 
		this.loadFeaturesHashMap();
		this.loadWhitelistLibs();

								
	}

//	public static void main(String[] args) {
//		long startTime = System.currentTimeMillis();
//		SmaliParser smaliParser = new SmaliParser();
//		//smaliParser.topLevelTraversal(smaliParser.folder);
//		long endSmaliParseTime = System.currentTimeMillis();
//
//		double recPercent = 100 * (double) smaliParser.recognizedHashMap.size()
//				/ (double) smaliParser.indivdualMethodCount;
//		double unRecPercent = 100
//				* (double) smaliParser.unRecognizedHashMap.size()
//				/ (double) smaliParser.indivdualMethodCount;
//		double recCoverage = 100
//				* (double) smaliParser.recognizedHashMap.size()
//				/ (double) smaliParser.featuresHashMap.size();
//		double unRecCoverage = 100
//				* (double) smaliParser.unRecognizedHashMap.size()
//				/ (double) smaliParser.featuresHashMap.size();
//
//		System.out.println("Recognized: "
//				+ smaliParser.recognizedHashMap.size() + " or " + recPercent
//				+ "%");
//		System.out.println("Unrecognized: "
//				+ smaliParser.unRecognizedHashMap.size() + " or "
//				+ unRecPercent + "%");
//		System.out.println("Recognized Coverage: " + recCoverage + "%");
//		System.out.println("Unrecognized Coverage: " + unRecCoverage + "%");
//		System.out.println("Total Features in Hash: "
//				+ smaliParser.featuresHashMap.size());
//		System.out.println("Total Distinct Methods Found: "
//				+ smaliParser.indivdualMethodCount);
//		System.out.println("Total Methods Parsed: " + smaliParser.totalCount
//				+ "\n");
//		
//		
//		long bitSetHashSize = smaliParser.bitSetsHashMap.size();
//		long cmpStartTime = System.currentTimeMillis();
//		//smaliParser.compareBitVectors();
//		long cmpEndTime = System.currentTimeMillis();
//
//		System.out.println("\nTotal time: " + (cmpEndTime - startTime)
//				+ " ms for " + bitSetHashSize + " bitSets");
//		System.out.println("Parse time: " + (endSmaliParseTime - startTime)
//				+ " ms or " + (double) (endSmaliParseTime - startTime)
//				/ (double) bitSetHashSize + " ms/bitSet");
//		System.out.println("Comparison time: " + (cmpEndTime - cmpStartTime)
//				+ " ms or " + (double) (cmpEndTime - cmpStartTime)
//				/ (double) bitSetHashSize + " ms/bitSet");
//
//	}

	private void parseDelimitedFile(String filePath, OpenBitSet bitSet)
			throws Exception {

		FileReader fr = new FileReader(filePath);
		BufferedReader br = new BufferedReader(fr);
		String currentRecord;
		String delimiter = "\\s+";
		String tokens[];
		String token;
		
		//System.out.println(filePath);

		while ((currentRecord = br.readLine()) != null) {
			if (currentRecord.contains("invoke")) {
				tokens = currentRecord.split(delimiter);
				token = tokens[tokens.length - 1];

				if (featuresHashMap.containsKey(token)) 
					bitSet.fastSet(featuresHashMap.get(token));
			}
		}
		br.close();
		fr.close();

	}
	
	public void parseDelimitedFile(String filePath, OpenBitSet bitSet, boolean debug)
			throws Exception {

		FileReader fr = new FileReader(filePath);
		BufferedReader br = new BufferedReader(fr);
		String currentRecord;
		String delimiter = "\\s+";
		String tokens[];
		String token;

		File file = new File(outputFeaturePath);

		// if file doesnt exists, then create it
		if (!file.exists()) {
			file.createNewFile();
		}

		FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
		BufferedWriter bw = new BufferedWriter(fw);

		while ((currentRecord = br.readLine()) != null) {
			if (currentRecord.contains("invoke")) {
				tokens = currentRecord.split(delimiter);
				token = tokens[tokens.length - 1];
				this.totalCount++;

				// if (token.startsWith("Landroid")){
				if (featuresHashMap.containsKey(token)) {
					bitSet.fastSet(featuresHashMap.get(token));
					if (!recognizedHashMap.containsKey(token)) {
						bw.write("R: " + token + "\n");
						indivdualMethodCount++;
						recognizedHashMap.put(token, indivdualMethodCount);
					}

				} else {
					if (!unRecognizedHashMap.containsKey(token)) {
						bw.write("U: " + token + "\n");
						indivdualMethodCount++;
						unRecognizedHashMap.put(token, indivdualMethodCount);
					}
				}
				// }

			}
		}

		bw.close();
		br.close();
		fw.close();
		fr.close();
	}

	private void loadFeaturesHashMap() {
		try {

			BufferedReader in = new BufferedReader(new FileReader(featuresMapPath));
			String str;
			
			while ((str = in.readLine()) != null) {
				featuresHashMap.put(str, featuresCount);
				featuresCount++;
			}
			// Close buffered reader
			in.close();

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private void loadWhitelistLibs() {
		try {

			BufferedReader in = new BufferedReader(new FileReader(whiteListLibraries));
			String str;
			
			while ((str = in.readLine()) != null) {
				str.replace("/", separatorSign);
				whiteListHashMap.put(str, featuresCount);
				featuresCount++;
			}
			// Close buffered reader
			in.close();

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private Boolean isWhitelisted(String fileEntryName) {
		if (whiteListHashMap.containsKey(fileEntryName)) {
			return true;
		}
		return false;
	}

	//Traverses root folder housing all the decompiled apk folders
//	public void apkRootTraversal(File folder, BitSetBank bsb) {
//		try {
//
//			for (File fileEntry : folder.listFiles()) {
//				dirCount++;
//
//				if (fileEntry.isDirectory()) {
//					
//					String packageName = getMainPackageName(folder);
//
//					if (packageName == null)
//						continue;
//					
//					fileEntry = toMainFolder(packageName);
//					if (fileEntry == null)
//						continue;
//
//					OpenBitSet bitSet = new OpenBitSet(this.featuresCount);
//					listFilesForFolder(fileEntry, bitSet);
//					bsb.add(fileEntry.getName(), bitSet);
//
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	
	//Traverses the decompiled folder that was produced from an APK
	public void apkDirectoryTraversal(File folder, OpenBitSet bitSet) {
		try {
			
			File fileEntry = new File(folder.getAbsoluteFile() + "/smali/");
			int folderNameLength = fileEntry.getAbsolutePath().length();

			listFilesForFolder(fileEntry, bitSet, folderNameLength);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//	public void listFilesForFolder(File folder, OpenBitSet bitSet) {
//		for (File fileEntry : folder.listFiles()) {
//			if (isWhitelisted(fileEntry.getAbsolutePath())) {
//				//System.out.println("WHITELISTED: " + fileEntry.getPath());
//				continue;
//			}
//
//			if (fileEntry.isDirectory()) {
//				listFilesForFolder(fileEntry, bitSet);
//			} else {
//				if (fileEntry.getName().endsWith(".smali")) {
//					try {
//						this.parseDelimitedFile(fileEntry.getAbsolutePath(),
//								bitSet);
//
//					} catch (Exception e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//			}
//		}
//	}
	
	public void listFilesForFolder(File folder, OpenBitSet bitSet, int folderNameLength) {
		for (File fileEntry : folder.listFiles()) {
			if (isWhitelisted(fileEntry.getAbsolutePath().substring(folderNameLength))) {
				bitSet.fastSet(whiteListHashMap.get(fileEntry.getAbsolutePath().substring(folderNameLength)));
			//	System.out.println("WHITELISTED: " + fileEntry.getPath());
				continue;
			}

			if (fileEntry.isDirectory()) {
				listFilesForFolder(fileEntry, bitSet, folderNameLength);
			} else {
				try {
					this.parseDelimitedFile(fileEntry.getAbsolutePath(),
							bitSet);

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}


	/*
	 * check Manifest.xml, if has get package name else return NULL means it is
	 * not a smali folder
	 */
	private String getMainPackageName(File folder) {

		String buff;
		BufferedReader in = null;

		try {
			// FileInputStream fs = new FileInputStream(manifestName);
			in = new BufferedReader(new FileReader(folder.getAbsolutePath()
					+ separatorSign + manifestName));

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

		try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private String extractPackageName(String line) {

		int index = line.indexOf(packageIndicator);
		if (index == -1)
			return null;
		else
			index += packageIndicator.length(); // move to the start of the name

		int endIndex = line.indexOf("\"", index);
		if (endIndex == -1)
			return null;

		return line.substring(index, endIndex);
	}

	/*
	 * go to the main component folder as the name indicated return the
	 * destination folder object
	 */
	private File toMainFolder(String packageName) {
		File tmp = new File(folder.getAbsolutePath() + separatorSign + "smali"
				+ separatorSign + packageName.replace(".", separatorSign));

		if (tmp.exists())
			return tmp;
		else
			return null;
	}
	
	private String getMainActivityPath(File folder) throws IOException {
		
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
	
	/* return the file object of the folder containing main activity 
	 * return null means no such folder or same with main component folder */
	private File toMainActivityFolder(File folder) {
		
		String activityName = null;
		
		try {
			activityName = getMainActivityPath(folder);
			//if (activityName != null)
				//System.out.println(activityName);
		} catch (Exception e) {
			
		}
			
		if (activityName == null || activityName.startsWith(".") || !activityName.contains("."))
			return null;

		File tmp = new File(folder.getAbsolutePath() + separatorSign + "smali" + separatorSign + activityName.substring(0, activityName.lastIndexOf(".")).replace(".", separatorSign));
		
		if (tmp.exists()){
			//System.out.println(tmp.getAbsolutePath());
			return tmp;
		}else
			return null;
	}

}
