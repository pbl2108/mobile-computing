import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.codec.binary.Hex;
import org.apache.lucene.util.OpenBitSet;




public class SmaliParser {

	private static final String manifestName = "AndroidManifest.xml";
	private static final String packageIndicator = "package=\"";
	private static final String separatorSign = "/";

	public ArrayList<String> features; 
	//public String filePath = "C:\\Users\\Dfosak\\workspace\\MobileComputing\\kidapp\\MngPage.smali";
	public String folderRoot = "C:\\Users\\Dfosak\\workspace\\MobileComputing\\apks-decompile";
	public String hashMapFile = "C:\\Users\\Dfosak\\Documents\\GitHub\\mobile-computing\\smali-methods.txt";
	public String outputFeatureFile = "C:\\Users\\Dfosak\\Documents\\GitHub\\mobile-computing\\testRun.txt";
	public String outputBitVectorFile = "C:\\Users\\Dfosak\\Documents\\GitHub\\mobile-computing\\bitVectors.csv";
	public File folder;
	public HashMap<String, Integer> featuresHashMap;
	public long recCount;
	public long unRecCount;
	public long count;
	
	
	public SmaliParser(){
		this.features = new ArrayList<String>();
		this.folder = new File(this.folderRoot);
		this.featuresHashMap = new HashMap<String, Integer>(); 
		this.recCount = 0;
		this.unRecCount = 0;
		this.count=0;
	}
	
	 public static void main(String[] args) {
		SmaliParser smaliParser = new SmaliParser();
		smaliParser.createHashMap(smaliParser.hashMapFile);

		
		smaliParser.topLevelTraversal(smaliParser.folder);
		
		System.out.println("Recognized: " + smaliParser.recCount + " Unrecognized: " + smaliParser.unRecCount + " Total: " + smaliParser.count );

		
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
					
			//System.out.println(token);
			if (featuresHashMap.containsKey(token)) {
				bitVector.fastSet(featuresHashMap.get(token));
				//System.out.println("Recognized method: " + token);
				bw.write("R: " + token + "\n");
				this.recCount++;
				this.count++;
			}
			else{
				//System.out.println("Unrecognized method: " + token);
				bw.write("U: " + token + "\n");
				this.unRecCount++;
				this.count++;
			}
		}
	}

	  

	  bw.close();
	  br.close();
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

				fileEntry = toMainFolder(fileEntry);
				if (fileEntry == null)
					continue;

		            listFilesForFolder(fileEntry, bitVector);
		            
		            if (!bitVector.isEmpty()){
		            	//System.out.println(fileEntry.getName() + ", " + bitVector);
		            	ByteArrayOutputStream baos = new ByteArrayOutputStream();  
		    		    ObjectOutputStream oos = new ObjectOutputStream(baos); 
		    		    oos.writeObject(bitVector);  
		    		    oos.close();  
		    		    byte[] bytes = baos.toByteArray();  
		    		    bw.write(Hex.encodeHexString(bytes) + ", " + fileEntry.getName() + "\n");
		            
		            }
		            
		        } else {
		        	//Do nothing
		        }
		    } 
		    
		    bw.close();
		    
		    
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

	/* check Manifest.xml, if has get package name
	 * else return NULL means it is not a smali folder */
	private String getMainPackageName(File folder) throws IOException {

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

	private String extractPackageName(String line) {

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
	private File toMainFolder(File folder) {

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
