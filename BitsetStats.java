import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.lucene.util.OpenBitSet;

public class BitsetStats {

	private static final String contentMapPath = "content_extensions.txt";
	private static final String logicMapPath = "smali-methods.txt";
	private static final String whiteListLibraries = "whitelist_librariesDB.txt";
	private static final String permissionsMapPath = "permissions.txt";

	HashMap<String, Long> logicCountMap;
	HashMap<String, Long> contentCountMap;
	long contentFeaturesCount;
	long logicFeaturesCount;
	int contentSize;
	int logicSize;
	int contentBitSize = 32;
	long[] logicArray = new long[logicSize];
	long[] contentArray = new long[contentSize];

	public static void main(String[] args) throws IOException {
		OpenBitSet logicBitSet;
		OpenBitSet contentBitSet;

		BitSetBank bsb = new BitSetBank();
		bsb.readAllFromSerial("/home/peter/github/mobile-computing/db/ee");
		BitsetStats bitStats = new BitsetStats();
		bitStats.logicCountMap = new HashMap<String, Long>();
		bitStats.contentCountMap = new HashMap<String, Long>();

		System.out.println("bitSetsHashMapSize = " + bsb.bitSetsHashMap.size());

		Iterator<Map.Entry<String, AppVector>> iter0 = bsb.bitSetsHashMap
				.entrySet().iterator();
		Map.Entry<String, AppVector> entry0 = iter0.next();
		logicBitSet = entry0.getValue().LogicVector;
		contentBitSet = entry0.getValue().ContentVector;

		
		bitStats.contentSize = (int) contentBitSet.capacity();
		bitStats.logicSize = (int) logicBitSet.capacity();
		bitStats.logicArray = new long[bitStats.logicSize];
		bitStats.contentArray = new long[bitStats.contentSize];

		System.out.println("contentSize = " + bitStats.contentSize
				+ " logicSize = " + bitStats.logicSize);
		System.out.println("contentArray = " + bitStats.contentArray.length
				+ " logicArray = " + bitStats.logicArray.length);

		for (Iterator<Map.Entry<String, AppVector>> iter1 = bsb.bitSetsHashMap
				.entrySet().iterator(); iter1.hasNext();) {
			Map.Entry<String, AppVector> entry1 = iter1.next();
			logicBitSet = entry1.getValue().LogicVector;
			contentBitSet = entry1.getValue().ContentVector;
			bitStats.addtoStatistics(logicBitSet, contentBitSet);
		}

		bitStats.loadLogicHashMap();
		bitStats.loadContentHashMap();
		bitStats.loadWhitelistLibs();
		bitStats.loadPermissionsHashMap(permissionsMapPath);

		System.out.println("contentMapSize = "
				+ bitStats.contentCountMap.size() + " logicMapSize = "
				+ bitStats.logicCountMap.size());
		System.out.println("contentFeatureCount = "
				+ bitStats.contentFeaturesCount + " logicFeaturesCount = "
				+ bitStats.logicFeaturesCount);

		bitStats.printHashMap(bitStats.logicCountMap, "LogicStats");
		bitStats.printHashMap(bitStats.contentCountMap, "contentStats");
	}

	public void addtoStatistics(OpenBitSet logicBitVector,
			OpenBitSet contentBitVector) {
		for (int i = 0; i < this.logicSize; i++) {
			if (logicBitVector.fastGet(i))
				this.logicArray[i]++;

			if (i < contentSize && contentBitVector.fastGet(i))
				this.contentArray[i]++;
		}
	}

	private void loadLogicHashMap() {
		try {

			BufferedReader in = new BufferedReader(new FileReader(logicMapPath));
			String str;

			while ((str = in.readLine()) != null) {
				this.logicCountMap.put(str,
						this.logicArray[(int) logicFeaturesCount]);
				this.logicFeaturesCount++;
			}
			// Close buffered reader
			in.close();

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private void loadContentHashMap() {
		try {

			BufferedReader in = new BufferedReader(new FileReader(
					contentMapPath));
			String str;

			while ((str = in.readLine()) != null) {
				for (int i = 0; i < contentBitSize; i++) {
					Long count = this.contentCountMap.get(str);
					if (count == null) {
						this.contentCountMap.put(str + "_____" + contentFeaturesCount,
								contentArray[(int) contentFeaturesCount]);
						this.contentFeaturesCount++;
					}
				}
			}
			System.out.println(contentFeaturesCount);
			// Close buffered reader
			in.close();

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private void loadWhitelistLibs() {
		try {

			BufferedReader in = new BufferedReader(new FileReader(
					whiteListLibraries));
			String str;

			while ((str = in.readLine()) != null) {
				Long count = this.contentCountMap.get(str);
				if (count == null) {
					this.contentCountMap.put(str,
							contentArray[(int) contentFeaturesCount]);
					this.contentFeaturesCount++;
				}
			}
			System.out.println(contentFeaturesCount);
			// Close buffered reader
			in.close();

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void loadPermissionsHashMap(String filePath) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(filePath));
			String str;

			while ((str = in.readLine()) != null) {
				this.contentCountMap.put(str,
						contentArray[(int) contentFeaturesCount]);
				this.contentFeaturesCount++;
			}
			System.out.println(contentFeaturesCount);
			// Close buffered reader
			in.close();

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void printHashMap(HashMap<String, Long> map, String type) {

		try {

			ValueComparatorLong bvc = new ValueComparatorLong(map);
			TreeMap<String, Long> sorted_map = new TreeMap<String, Long>(bvc);
			sorted_map.putAll(map);

			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
					.format(Calendar.getInstance().getTime());

			// Creates outputLogs Directory if it Does not Exist
			File directory = new File("outputLogs/");
			directory.mkdirs();

			File file = new File("outputLogs/" + type + "_" + timeStamp
					+ ".txt");

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			for (Iterator<Map.Entry<String, Long>> iter1 = sorted_map
					.entrySet().iterator(); iter1.hasNext();) {
				Map.Entry<String, Long> entry1 = iter1.next();
				bw.write(entry1.getKey() + " " + entry1.getValue() + "\n");

			}

			bw.close();
			fw.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

class ValueComparatorLong implements Comparator<String> {

	Map<String, Long> base;

	public ValueComparatorLong(Map<String, Long> base) {
		this.base = base;
	}

	// Note: this comparator imposes orderings that are inconsistent with
	// equals.
	@Override
	public int compare(String a, String b) {
		if (base.get(a) >= base.get(b)) {
			return -1;
		} else {
			return 1;
		} // returning 0 would merge keys
	}
}
