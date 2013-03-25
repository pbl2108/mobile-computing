import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.apache.lucene.util.OpenBitSet;




public class SmaliParser {
	
	public ArrayList<String> features; 
	//public String filePath = "C:\\Users\\Dfosak\\workspace\\MobileComputing\\kidapp\\MngPage.smali";
	public String folderRoot = "C:\\Users\\Dfosak\\workspace\\MobileComputing\\apks-decompile";
	public String hashMapFile = "C:\\Users\\Dfosak\\Documents\\GitHub\\mobile-computing\\smali-methods.txt";
	public String outputFeatureFile = "C:\\Users\\Dfosak\\Documents\\GitHub\\mobile-computing\\testRun.txt";
	public String outputBitVectorFile = "C:\\Users\\Dfosak\\Documents\\GitHub\\mobile-computing\\bitVectors.csv";
	public String outputSimFile = "C:\\Users\\Dfosak\\Documents\\GitHub\\mobile-computing\\similarities.txt";
	public File folder;
	public HashMap<String, Integer> featuresHashMap;
	public HashMap<String, OpenBitSet> bitVectorsHashMap;
	public HashMap<String, Long> recognizedHashMap;
	public HashMap<String, Long> unRecognizedHashMap;
	public int bitVectorsCount;
	public long recCount;
	public long unRecCount;
	public long count;
	public double jaccardThreshold = .70;
	
	
	public SmaliParser(){
		this.features = new ArrayList<String>();
		this.folder = new File(this.folderRoot);
		this.featuresHashMap = new HashMap<String, Integer>(); 
		this.recognizedHashMap = new HashMap<String, Long>(); 
		this.unRecognizedHashMap = new HashMap<String, Long>(); 
		this.bitVectorsHashMap = new HashMap<String, OpenBitSet>(); 
		this.recCount = 0;
		this.unRecCount = 0;
		this.count = 0;
		this.bitVectorsCount = 0;
	}
	
	 public static void main(String[] args) {
		SmaliParser smaliParser = new SmaliParser();
		smaliParser.createHashMap(smaliParser.hashMapFile);		
		smaliParser.topLevelTraversal(smaliParser.folder);		
		
		double recPercent = (double)smaliParser.recognizedHashMap.size()/(double)smaliParser.featuresHashMap.size();
		double unRecPercent = (double)smaliParser.unRecognizedHashMap.size()/(double)smaliParser.featuresHashMap.size();
		
		System.out.println("Recognized: " + smaliParser.recognizedHashMap.size() + " or " + recPercent + "%");
		System.out.println("Unrecognized: " + smaliParser.unRecognizedHashMap.size() + " or " + unRecPercent + "%");
		System.out.println("Total Features in hash: " + smaliParser.featuresHashMap.size() );
		System.out.println("Total Methods parsed: " + smaliParser.count + "\n");
		
		smaliParser.compareBitVectors();
		
	 }
	 
	private void parseDelimitedFile(String filePath, OpenBitSet bitVector) throws Exception
	{
	  
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
	  
		  while((currentRecord = br.readLine()) != null){		
			if (currentRecord.contains("invoke")){
				tokens =  currentRecord.split(delimiter);
				token = tokens[tokens.length-1];
				this.count++;
				
				//if (token.startsWith("Landroid")){
					//System.out.println(token);
					if (featuresHashMap.containsKey(token)) {
						bitVector.fastSet(featuresHashMap.get(token));
						//System.out.println("Recognized method: " + token);
						if (!recognizedHashMap.containsKey(token)) {
							bw.write("R: " + token + "\n");
							recognizedHashMap.put(token, count);
						}
						
					}
					else{
						if (!unRecognizedHashMap.containsKey(token)) {
							bw.write("U: " + token + "\n");
							unRecognizedHashMap.put(token, count);
						}
					}
				//}
						
				
			}
		}

	  

	  bw.close();
	  br.close();
	  fw.close();
	}
	
	
	private void createHashMap(String filePath)
	{
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
	
	
	public void topLevelTraversal(File folder) {
		
		try{
			
		
			File file = new File(this.outputBitVectorFile);
		  
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
			
			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);
					
			
		    for (File fileEntry : folder.listFiles()) {
		        if (fileEntry.isDirectory()) {
		        	
		        	OpenBitSet bitVector = new OpenBitSet(this.featuresHashMap.size());
		            listFilesForFolder(fileEntry, bitVector);
		            
		            if (!bitVector.isEmpty()){
		            	//System.out.println(fileEntry.getName() + ", " + bitVector);
		            	bw.write(this.bitVectorToString(bitVector) + ", " + fileEntry.getName() + "\n");
		            	bitVectorsHashMap.put(fileEntry.getName(), bitVector);
		            }
		            
		        } else {
		        	//Do nothing
		        }
		    } 
		    
		    bw.close();
		    fw.close();
		    
		    
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	   
	    
	}
	
	public void listFilesForFolder(File folder, OpenBitSet bitVector) {
	    for (File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
	            listFilesForFolder(fileEntry, bitVector);
	        } else {
	        	if (fileEntry.getName().endsWith(".smali"))
	            {
	        		try {
	        			this.parseDelimitedFile(fileEntry.getAbsolutePath(), bitVector);
	        			
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
		
		try{
			
			File file = new File(this.outputSimFile);

		  
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
			

			
			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

		
			 for(Iterator<Map.Entry<String, OpenBitSet>> iter1 = bitVectorsHashMap.entrySet().iterator(); iter1.hasNext(); ) {
					Map.Entry<String, OpenBitSet> entry1 = iter1.next();
					bitVector1 = entry1.getValue();
					iter1.remove();
					bw.write("Jaccard Similarity for " + entry1.getKey() + " against:\n");
					
					for(Iterator<Map.Entry<String, OpenBitSet>> iter2 = bitVectorsHashMap.entrySet().iterator(); iter2.hasNext(); ) {
						Map.Entry<String, OpenBitSet> entry2 = iter2.next();
						bitVector2 = entry2.getValue();
						jSim = this.JaccardSim(bitVector1, bitVector2);
						bw.write("\t" + entry2.getKey() + " = " + jSim + "\n");
						
						
						if (jSim > jaccardThreshold)
							System.out.println(entry1.getKey() + " vs " + entry2.getKey() + " = " + jSim);
						
					}
			  }
			 
			 bw.close();
			 fw.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String bitVectorToString(OpenBitSet bitVector){
		try{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();  
		    ObjectOutputStream oos = new ObjectOutputStream(baos); 
		    oos.writeObject(bitVector);
		    oos.close();  
		    byte[] bytes = baos.toByteArray(); 
		    return(Hex.encodeHexString(bytes));
		    
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public double JaccardSim(OpenBitSet bitVector1, OpenBitSet bitVector2){
		
		OpenBitSet bitVectorIntersect = (OpenBitSet)bitVector1.clone();
		OpenBitSet bitVectorUnion = (OpenBitSet)bitVector1.clone();
		
		bitVectorIntersect.intersect(bitVector2);
		bitVectorUnion.union(bitVector2);
		
		double jaccardSim = (double)bitVectorIntersect.cardinality()/(double)bitVectorUnion.cardinality();
		//System.out.println(bitVectorIntersect.cardinality() + " " + bitVectorUnion.cardinality() + " " + jaccardSim);
		
		return jaccardSim;
	}


	
}
