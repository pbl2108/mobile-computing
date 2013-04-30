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
			
			File topDir = new File("/proj/ds/encos/apk");
			//File topDir = new File("/home/Dfosak/Desktop/apks");
			File[] files = topDir.listFiles();
			File outfile = new File(apkList + idx);
			FileWriter fw = new FileWriter(outfile.getAbsoluteFile(),true);
			BufferedWriter bw = new BufferedWriter(fw);
			
			System.out.println(files.length);
			
			while (fileCount < files.length){
				bw.write(files[fileCount].getName() + "\n");
				
				fileCount++;
				
				
				if(fileCount % (files.length/8) == 0){
					System.out.println(fileCount);
					idx++;
					bw.close();
					fw.close();
					outfile = new File(apkList + idx);
					fw = new FileWriter(outfile.getAbsoluteFile(), true);
					bw = new BufferedWriter(fw);
					
				}
				
				
			}
			
			System.out.println(fileCount);
			
			bw.close();
			fw.close();
			
		}
	}