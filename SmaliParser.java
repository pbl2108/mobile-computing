import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;



public class SmaliParser {
	
	public ArrayList<String> features; 
	String filePath = "C:\\Users\\Dfosak\\workspace\\MobileComputing\\kidapp\\MngPage.smali";
	
	public SmaliParser(){
		this.features = new ArrayList<String>();
	}
	
	 public static void main(String[] args) {
		SmaliParser smaliParser = new SmaliParser();
		try {
			smaliParser.parseDelimitedFile();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	 }
	 
	private void parseDelimitedFile() throws Exception
	{
	  
	  FileReader fr = new FileReader(this.filePath);
	  BufferedReader br = new BufferedReader(fr);
	  String currentRecord;
	  String delimiter = "\\s+";
	  String tokens[];
	  
	  
	  while((currentRecord = br.readLine()) != null){
		
		
		if (currentRecord.contains("invoke")){
			tokens =  currentRecord.split(delimiter);
			for (String token : tokens) {
				if( token.indexOf("Landroid") == 0){
					this.features.add(token);
					System.out.println(token);
				}
			}
		}

	    
	  }
	  br.close();
	}
	
	
}
