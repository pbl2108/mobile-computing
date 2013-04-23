import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class printFiles {
	
	static String apkList = "ds_apks";

	public static void main(String[] args) throws IOException {
			int idx = 0;
			int fileCount = 0;
			
			//File topDir = new File("/proj/ds/encos/apk");
			File topDir = new File("/home/Dfosak/Desktop/apks");
			File[] files = topDir.listFiles();
			File outfile = new File(apkList + idx);
			FileWriter fw = new FileWriter(outfile.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			
			while (fileCount < files.length){
				bw.write(files[fileCount] + "\n");
				
				fileCount++;
				
				
				if(fileCount % (files.length/6) == 0){
					idx++;
					bw.close();
					fw.close();
					fw = new FileWriter(outfile.getAbsoluteFile());
					bw = new BufferedWriter(fw);
					outfile = new File(apkList + idx);
				}
				
				
			}
			
			bw.close();
			fw.close();
			
		}
	}