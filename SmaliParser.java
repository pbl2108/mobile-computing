import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;




public class SmaliParser {
	
	public ArrayList<String> features; 
	//public String filePath = "C:\\Users\\Dfosak\\workspace\\MobileComputing\\kidapp\\MngPage.smali";
	public String folderRoot = "C:\\Users\\Dfosak\\workspace\\MobileComputing\\apks-decompile";
	public String hashMapFile = "C:\\Users\\Dfosak\\Documents\\GitHub\\mobile-computing\\smali-methods.txt";
	public String outputFile = "C:\\Users\\Dfosak\\Documents\\GitHub\\mobile-computing\\testRun.txt";
	public File folder;
	public HashMap<String, Integer> methodsHashMap;
	public long recCount;
	public long unRecCount;
	public long count;
	
	public SmaliParser(){
		this.features = new ArrayList<String>();
		this.folder = new File(this.folderRoot);
		this.methodsHashMap = new HashMap<String, Integer>(); 
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
	 
	private void parseDelimitedFile(String filePath) throws Exception
	{
	  
	  FileReader fr = new FileReader(filePath);
	  BufferedReader br = new BufferedReader(fr);
	  String currentRecord;
	  String delimiter = "\\s+";
	  String tokens[];
	  String token;
	  
	  
	  
	  File file = new File(this.outputFile);
	  
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
			if (methodsHashMap.containsKey(token)) {
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
                    if (methodsHashMap.containsKey(str)) {
                        System.out.println("Duplicate method: " + str);
                    } else { 
                    	methodsHashMap.put(str, bitIndex);
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
	    for (File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
	            listFilesForFolder(fileEntry);
	        } else {
	        	//Do nothing
	        }
	    }
	}
	
	public void listFilesForFolder(File folder) {
	    for (File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
	            listFilesForFolder(fileEntry);
	        } else {
	        	if (fileEntry.getName().endsWith(".smali"))
	            {
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


	
}
