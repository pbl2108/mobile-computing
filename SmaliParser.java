import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.apache.lucene.util.OpenBitSet;

public class SmaliParser {

	private static final String manifestName = "AndroidManifest.xml";
	private static final String packageIndicator = "package=\"";
	private static final String separatorSign = "/";

	public String filePath = "C:\\Users\\Dfosak\\workspace\\MobileComputing\\kidapp\\MngPage.smali";
	public String folderRoot = "C:\\Users\\Dfosak\\workspace\\MobileComputing\\apks-decompile";
	public String hashMapFile = "C:\\Users\\Dfosak\\Documents\\GitHub\\mobile-computing\\smali-methods.txt";
	public String outputFeatureFile = "C:\\Users\\Dfosak\\Documents\\GitHub\\mobile-computing\\testRun.txt";
	public String outputBitVectorFile = "C:\\Users\\Dfosak\\Documents\\GitHub\\mobile-computing\\bitVectors.csv";
	public String outputSimFile = "C:\\Users\\Dfosak\\Documents\\GitHub\\mobile-computing\\similarities.txt";
	public String whitelistLibraries = "C:\\Users\\Dfosak\\Documents\\GitHub\\mobile-computing\\whitelist_libraries.txt";

	// public String folderRoot = "/home/peter/columbia/mob/sample-decompile";
	// public String hashMapFile =
	// "/home/peter/columbia/mob/helper_txts/smali-methods.txt";
	// public String whitelistLibraries =
	// "/home/peter/columbia/mob/helper_txts/whitelist_libraries.txt";
	// public String outputFeatureFile =
	// "/home/peter/columbia/mob/helper_txts/output/testRun.txt";
	// public String outputBitVectorFile =
	// "/home/peter/columbia/mob/helper_txts/output/bitVectors.csv";
	// public String outputSimFile =
	// "/home/peter/columbia/mob/helper_txts/output/similarities.txt";

	public File folder;
	public HashMap<String, Integer> featuresHashMap;
	public HashMap<String, OpenBitSet> bitVectorsHashMap;
	public HashMap<String, Long> recognizedHashMap;
	public HashMap<String, Long> unRecognizedHashMap;
	public ArrayList<String> whitelistLibsHashMap;
	public int bitVectorsCount;
	public long recCount;
	public long unRecCount;
	public long totalCount;
	public long indivdualMethodCount;
	public double jaccardThreshold = .70;
	// public long startTime;
	// public long endSmaliParseTime;
	// public long cmpStartTime;
	// public long cmpEndTime;
	public long dirCount;

	public SmaliParser() {
		this.folder = new File(this.folderRoot);
		this.featuresHashMap = new HashMap<String, Integer>();
		this.recognizedHashMap = new HashMap<String, Long>();
		this.unRecognizedHashMap = new HashMap<String, Long>();
		this.bitVectorsHashMap = new HashMap<String, OpenBitSet>();
		this.whitelistLibsHashMap = new ArrayList<String>();
		this.recCount = 0;
		this.unRecCount = 0;
		this.totalCount = 0;
		this.indivdualMethodCount = 0;
		this.bitVectorsCount = 0;
		this.dirCount = 0;
	}

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		SmaliParser smaliParser = new SmaliParser();
		smaliParser.createHashMap(smaliParser.hashMapFile);
		smaliParser.loadWhitelistLibs(smaliParser.whitelistLibraries);
		smaliParser.topLevelTraversal(smaliParser.folder);
		long endSmaliParseTime = System.currentTimeMillis();

		double recPercent = 100 * (double) smaliParser.recognizedHashMap.size()
				/ (double) smaliParser.indivdualMethodCount;
		double unRecPercent = 100
				* (double) smaliParser.unRecognizedHashMap.size()
				/ (double) smaliParser.indivdualMethodCount;
		double recCoverage = 100
				* (double) smaliParser.recognizedHashMap.size()
				/ (double) smaliParser.featuresHashMap.size();
		double unRecCoverage = 100
				* (double) smaliParser.unRecognizedHashMap.size()
				/ (double) smaliParser.featuresHashMap.size();

		System.out.println("Recognized: "
				+ smaliParser.recognizedHashMap.size() + " or " + recPercent
				+ "%");
		System.out.println("Unrecognized: "
				+ smaliParser.unRecognizedHashMap.size() + " or "
				+ unRecPercent + "%");
		System.out.println("Recognized Coverage: " + recCoverage + "%");
		System.out.println("Unrecognized Coverage: " + unRecCoverage + "%");
		System.out.println("Total Features in Hash: "
				+ smaliParser.featuresHashMap.size());
		System.out.println("Total Distinct Methods Found: "
				+ smaliParser.indivdualMethodCount);
		System.out.println("Total Methods Parsed: " + smaliParser.totalCount
				+ "\n");

		long bitVectorHashSize = smaliParser.bitVectorsHashMap.size();
		long cmpStartTime = System.currentTimeMillis();
		smaliParser.compareBitVectors();
		long cmpEndTime = System.currentTimeMillis();

		System.out.println("\nTotal time: " + (cmpEndTime - startTime)
				+ " ms for " + bitVectorHashSize + " bitVectors");
		System.out.println("Parse time: " + (endSmaliParseTime - startTime)
				+ " ms or " + (double) (endSmaliParseTime - startTime)
				/ (double) bitVectorHashSize + " ms/bitVector");
		System.out.println("Comparison time: " + (cmpEndTime - cmpStartTime)
				+ " ms or " + (double) (cmpEndTime - cmpStartTime)
				/ (double) bitVectorHashSize + " ms/bitVector");

	}

	private void parseDelimitedFile(String filePath, OpenBitSet bitVector)
			throws Exception {

		FileReader fr = new FileReader(filePath);
		BufferedReader br = new BufferedReader(fr);
		String currentRecord;
		String delimiter = "\\s+";
		String tokens[];
		String token;

		File file = new File(this.outputFeatureFile);

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
					bitVector.fastSet(featuresHashMap.get(token));
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
	}

	private void createHashMap(String filePath) {
		try {

			BufferedReader in = new BufferedReader(new FileReader(filePath));
			String str;
			int bitIndex = 0;

			while ((str = in.readLine()) != null) {
				if (featuresHashMap.containsKey(str)) {
					System.out.println("Duplicate method: " + str);
				} else {
					featuresHashMap.put(str, bitIndex);
					bitIndex++;
				}
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

	private void loadWhitelistLibs(String filePath) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(filePath));
			String str;
			while ((str = in.readLine()) != null) {
				str.replace("/", separatorSign);
				whitelistLibsHashMap.add(str);
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

	private Boolean isWhitelisted(File fileEntry) {
		for (String lib : this.whitelistLibsHashMap) {
			if (fileEntry.getPath().contains(lib))
				return true;
		}
		return false;
	}

	public void topLevelTraversal(File folder) {
		try {

			File file = new File(this.outputBitVectorFile);

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			for (File fileEntry : folder.listFiles()) {
				dirCount++;

				if (fileEntry.isDirectory()) {

					OpenBitSet bitVector = new OpenBitSet(
							this.featuresHashMap.size());

					//fileEntry = toMainFolder(fileEntry);
					if (fileEntry == null)
						continue;

					listFilesForFolder(fileEntry, bitVector);

					if (!bitVector.isEmpty()) {
						// System.out.println(fileEntry.getName() + ", " +
						// bitVector);
						bw.write(this.bitVectorToString(bitVector) + ", "
								+ fileEntry.getName() + "\n");
						bitVectorsHashMap.put(fileEntry.getName(), bitVector);
					}

				} else {
					// Do nothing
				}
			}

			bw.close();
			fw.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void listFilesForFolder(File folder, OpenBitSet bitVector) {
		for (File fileEntry : folder.listFiles()) {
			if (this.isWhitelisted(fileEntry)) {
				System.out.println("WHITELISTED: " + fileEntry.getPath());
				continue;
			}

			if (fileEntry.isDirectory()) {
				listFilesForFolder(fileEntry, bitVector);
			} else {
				if (fileEntry.getName().endsWith(".smali")) {
					try {
						this.parseDelimitedFile(fileEntry.getAbsolutePath(),
								bitVector);

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void compareBitVectors() {

		OpenBitSet bitVector1;
		OpenBitSet bitVector2;
		double jSim;

		try {

			File file = new File(this.outputSimFile);

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			for (Iterator<Map.Entry<String, OpenBitSet>> iter1 = bitVectorsHashMap
					.entrySet().iterator(); iter1.hasNext();) {
				Map.Entry<String, OpenBitSet> entry1 = iter1.next();
				bitVector1 = entry1.getValue();
				iter1.remove();
				bw.write("Jaccard Similarity for " + entry1.getKey()
						+ " against:\n");

				for (Iterator<Map.Entry<String, OpenBitSet>> iter2 = bitVectorsHashMap
						.entrySet().iterator(); iter2.hasNext();) {
					Map.Entry<String, OpenBitSet> entry2 = iter2.next();
					bitVector2 = entry2.getValue();
					jSim = this.JaccardSim(bitVector1, bitVector2);
					bw.write("\t" + entry2.getKey() + " = " + jSim + "\n");

					if (jSim > jaccardThreshold)
						System.out.println(entry1.getKey() + " vs "
								+ entry2.getKey() + " = " + jSim);

				}
			}

			bw.close();
			fw.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String bitVectorToString(OpenBitSet bitVector) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(bitVector);
			oos.close();
			byte[] bytes = baos.toByteArray();
			return (Hex.encodeHexString(bytes));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public double JaccardSim(OpenBitSet bitVector1, OpenBitSet bitVector2) {

		OpenBitSet bitVectorIntersect = (OpenBitSet) bitVector1.clone();
		OpenBitSet bitVectorUnion = (OpenBitSet) bitVector1.clone();

		bitVectorIntersect.intersect(bitVector2);
		bitVectorUnion.union(bitVector2);

		double jaccardSim = (double) bitVectorIntersect.cardinality()
				/ (double) bitVectorUnion.cardinality();
		// System.out.println(bitVectorIntersect.cardinality() + " " +
		// bitVectorUnion.cardinality() + " " + jaccardSim);

		return jaccardSim;
	}

	/*
	 * check Manifest.xml, if has get package name else return NULL means it is
	 * not a smali folder
	 */
	private String getMainPackageName(File folder) throws IOException {

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

		in.close();
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
	private File toMainFolder(File folder) {

		String packageName = null;

		try {
			packageName = getMainPackageName(folder);
		} catch (IOException e) {

		}

		if (packageName == null)
			return null;

		File tmp = new File(folder.getAbsolutePath() + separatorSign + "smali"
				+ separatorSign + packageName.replace(".", separatorSign));

		if (tmp.exists())
			return tmp;
		else
			return null;
	}
}
