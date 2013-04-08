import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class ApkDisassembler {
	
		private static final String apkSuffix = ".apk";
		private static final String separator = "/";
		
		private static final String apktoolPath = "apktool1.5.2/apktool";
		private static final String fileListPath = "apkList_";

		public String apkPath;
		public String destPath;
		
		public File topDir;
		public File[] fileArray;
		
		public int currentFile;

		
		public ApkDisassembler(String apkPath, String destPath) {
			this.topDir = new File(apkPath);		// directory of apk folder
			this.currentFile = 0;
			this.apkPath = apkPath;
			this.destPath = destPath;
		}
		
		/* disassemble all apks under the path indicated */
		public void disassembleAll() {
			this.fileArray = topDir.listFiles();
		}
		
		/* disassemble the next apks in FileArray and returns a file pointer to the newly created directory */
		public File disassembleNextFile() {
			
			if (currentFile >= fileArray.length)
				return null;
			
			String apkName = null;				// name buffer
			
			File fileEntry = fileArray[currentFile];
				
			/* extract key and disassemble apk to get smali and manifest */
			try {
				apkName = getPureName(fileEntry.getName());
				
				//System.out.println("Start dissembling...");
				//disassembleApk(fileEntry, apkName);
				
			} catch (Exception e) {
				System.out.println("Error: " + apkName);
			} 
			
			currentFile++;
			return new File(destPath + separator + apkName);
				
		}
		
		
		public void getRandomFiles(int fileLimit) {
			int idx;
			int fileCount = 0;
			File fileEntry;
			File[] files = topDir.listFiles();
			this.fileArray = new File[fileLimit];
					
			while (fileCount < fileLimit){
			
				idx = (int)(Math.random()*files.length);
				
				fileEntry = files[idx];
				
				if (!isApkFile(fileEntry.getName())) {
					continue;
					// skip non-apk file
				}else{
					fileArray[fileCount] = fileEntry;
				}
				
				fileCount++;
			}
		}
		
		public void getFileSection(int divisor, int sectionNumber) {
			int idx;
			int fileCount = 0;
			int arrayLength;
			
			File[] files = topDir.listFiles();
			
			arrayLength = files.length/divisor;
			idx = arrayLength * (sectionNumber-1);
						
			if (divisor == sectionNumber)
				arrayLength = files.length - idx;
		
			this.fileArray = new File[arrayLength];
								
			while (fileCount < arrayLength){
				fileArray[fileCount] = files[idx];
								
				idx++;
				fileCount++;
			}
			
			System.out.println("Total files in folder:  " + files.length);
			System.out.println("Processing " + arrayLength + " files [" + (idx - arrayLength) + "," + idx + ")" );
			
		}
		
		
		private void disassembleApk(File apk, String pureName) throws Exception {
			Process p = Runtime.getRuntime().exec(getDisassembleCmd(apk, pureName));
			p.waitFor();
			return;
		}
		
		private String getDestFolderName(String name) {
			return destPath + separator + name;
		}
		
		private boolean isApkFile(String name) {
			return name.endsWith(apkSuffix);
		}
		
//		private String getUnzipCmd(File apk, String name) {
//			return "unzip " + apk.getAbsolutePath() + " " + keyPath + " -d " + getDestFolderName(name);// + separator + "key";
//		}
		
		/*private String getCopyCmd(String name) {
			return "mv " + apkPath + separator + keyFolderName + " " + getDestFolderName(name);
		}*/
		
		private String getDisassembleCmd(File apk, String name) {
			return apktoolPath + " d " + apk.getAbsolutePath() + " " + getDestFolderName(name);
		}
		
		/* eliminate the suffix for the apk
		 * must guarantee that the name is ended with ".apk" */
		private String getPureName(String name) {
			return name.substring(0, name.lastIndexOf(apkSuffix));
		}
		
		public void printDisassembleList() {
			try {
				String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
				File file = new File(fileListPath + timeStamp + ".txt");
				
				if (!file.exists()) {
					file.createNewFile();
				}

				FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
				BufferedWriter bw = new BufferedWriter(fw);
				
				for (File fileEntry : fileArray) 
					bw.write(fileEntry.getAbsolutePath() + "\n");
				
				bw.close();
				fw.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		
		
		


}
