import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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


public class BitVector {

	public static final String serialBitVectorMap ="/home/Dfosak/Desktop/mobile-computing/bitVectorMap.ser";
	public static final String outputSimPath = "/home/Dfosak/Desktop/mobile-computing/similarities.txt";
	public static final String authorsMapPath = "/home/Dfosak/Desktop/mobile-computing/apkSignatures.csv";
	
	public HashMap<String, OpenBitSet> bitVectorsHashMap;
	public HashMap<String, String> authorsMap;
	public double jaccardThreshold = .70;
	
	public BitVector() {
		this.bitVectorsHashMap = new HashMap<String, OpenBitSet>();
		this.authorsMap = new HashMap<String, String>();

	}
	
	public static void main(String[] args) {
		BitVector bitVector = new BitVector();
		bitVector.readSerialBitVector();
		bitVector.loadAuthorsMap();
		
		long bitVectorHashSize = bitVector.bitVectorsHashMap.size();
		long cmpStartTime = System.currentTimeMillis();
		
		bitVector.compareBitVectors();
		
		long cmpEndTime = System.currentTimeMillis();
	

		System.out.println("Comparison time: " + (cmpEndTime - cmpStartTime)
				+ " ms or " + (double) (cmpEndTime - cmpStartTime)
				/ (double) bitVectorHashSize + " ms/bitVector");
	}
	
	private void readSerialBitVector() {
		try {
			FileInputStream fis = new FileInputStream(serialBitVectorMap);
	        ObjectInputStream ois = new ObjectInputStream(fis);
	        bitVectorsHashMap = (HashMap<String, OpenBitSet>) ois.readObject();
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
	
	public void loadAuthorsMap() {
		try {

			BufferedReader in = new BufferedReader(new FileReader(authorsMapPath));
			String delimiter = "\\s+";
			String currentLine;
			String tokens[];
			String token;
			int bitIndex = 0;

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
	
	public void compareBitVectors() {

		OpenBitSet bitVector1;
		OpenBitSet bitVector2;
		double jSim;

		try {

			File file = new File(outputSimPath);

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
	
	
}
