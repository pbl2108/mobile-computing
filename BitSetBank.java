import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.apache.lucene.util.OpenBitSet;


public class BitSetBank {

	public static final String serialBitSetBankMap ="bitSetMap.ser";
	public static final String outputSimPath = "similarities.txt";
	public static final String authorsMapPath = "apkSignatures.csv";
	
	public HashMap<String, OpenBitSet> bitSetsHashMap;
	public HashMap<String, String> authorsMap;
	public double jaccardThreshold = .70;
	
	public BitSetBank() {
		this.bitSetsHashMap = new HashMap<String, OpenBitSet>();
		this.authorsMap = new HashMap<String, String>();

	}
	
	public void writeToSerial() {
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(serialBitSetBankMap);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(this.bitSetsHashMap);
			oos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void add(String fileName, OpenBitSet bitSet) {
		if (!bitSet.isEmpty()) 
			bitSetsHashMap.put(fileName, bitSet);
		else
			System.out.println("No bits set for: " + fileName + ", Excluding package..." );
	}
	
	public void readFromSerial() {
		try {
			FileInputStream fis = new FileInputStream(serialBitSetBankMap);
	        ObjectInputStream ois = new ObjectInputStream(fis);
	        bitSetsHashMap = (HashMap<String, OpenBitSet>) ois.readObject();
	        ois.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String bitSetToString(OpenBitSet bitSet) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(bitSet);
			oos.close();
			byte[] bytes = baos.toByteArray();
			return (Hex.encodeHexString(bytes));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public void loadAuthorsMap() {
		try {

			BufferedReader in = new BufferedReader(new FileReader(authorsMapPath));
			String delimiter = "\\s+";
			String currentLine;
			String tokens[];

			while ((currentLine = in.readLine()) != null) {
					tokens = currentLine.split(delimiter);
					authorsMap.put(tokens[0], tokens[1]);
			}
			// Close buffered reader
			in.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void compareBitSetBank() {

		OpenBitSet bitSet1;
		OpenBitSet bitSet2;
		double jSim;

		try {

			File file = new File(outputSimPath);

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			for (Iterator<Map.Entry<String, OpenBitSet>> iter1 = bitSetsHashMap
					.entrySet().iterator(); iter1.hasNext();) {
				Map.Entry<String, OpenBitSet> entry1 = iter1.next();
				bitSet1 = entry1.getValue();
				iter1.remove();
				bw.write("Jaccard Similarity for " + entry1.getKey()
						+ " against:\n");

				for (Iterator<Map.Entry<String, OpenBitSet>> iter2 = bitSetsHashMap
						.entrySet().iterator(); iter2.hasNext();) {
					Map.Entry<String, OpenBitSet> entry2 = iter2.next();
					bitSet2 = entry2.getValue();
					jSim = this.JaccardSim(bitSet1, bitSet2);
					bw.write("\t" + entry2.getKey() + " = " + jSim + "\n");

					if (jSim > jaccardThreshold && isDifferentAuthors(entry1.getKey(), entry2.getKey()))
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
	
	//Compares MD5 hash for each APK. Returns true if both are different 
	private boolean isDifferentAuthors(String key1, String key2) {
		String hash1 = authorsMap.get(key1);
		String hash2 = authorsMap.get(key2);
		
		if (hash1 == null || hash2 == null){
			System.out.print("NO MD5:");
			return true;
		}
		
//		if (!hash1.equals(hash2)){
//			System.out.println(hash1 + " " + hash2);
//			return true;
//		}
//		return false;
		
//		if (hash1.equals(hash2)){
//		System.out.println(hash1 + " " + hash2);
//		System.out.println(key1 + " " + key2);
//		return false;
//		}
//		return true;
		
		return (!hash1.equals(hash2));
	}

	public double JaccardSim(OpenBitSet bitSet1, OpenBitSet bitSet2) {

		OpenBitSet bitSetIntersect = (OpenBitSet) bitSet1.clone();
		OpenBitSet bitSetUnion = (OpenBitSet) bitSet1.clone();

		bitSetIntersect.intersect(bitSet2);
		bitSetUnion.union(bitSet2);

		double jaccardSim = (double) bitSetIntersect.cardinality()
				/ (double) bitSetUnion.cardinality();
		// System.out.println(bitSetIntersect.cardinality() + " " +
		// bitSetUnion.cardinality() + " " + jaccardSim);

		return jaccardSim;
	}
	
	
}
