import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;


public class apkDissembler {
	
		private static final String apkSuffix = ".apk";
		private static final String separator = "/";
//		private static final String keyPath = "META-INF/CERT.RSA";
//		private static final String keyFolderName = "META-INF";
		private static final int fileLimit = 15;

		/* default path */
//		private String apktoolPath = "/Users/xuyiming/Desktop/apktool/apktool";
//		private String apkPath = "/Users/xuyiming/Desktop/apks2";
//		private String destPath = "/Users/xuyiming/Desktop/tmp";
		private String apktoolPath = "/home/Dfosak/Desktop/apktool1.5.2/apktool";
		private String apkPath = "/home/Dfosak/Desktop/apks";
		private String destPath = "/home/Dfosak/Desktop/tmp";
		private String fileListPath = "/home/Dfosak/Desktop/mobile-computing/apkList.txt";;
		
		public File topDir;
		public ArrayList<File> apkList;
		
		public apkDissembler() {
			this.topDir = new File(apkPath);		// directory of apk folder
			this.apkList = new ArrayList<File>();
		}
		
		/* dissemble all apks under the path indicated */
		public void dissembleAll() {
			
			File topDir = new File(apkPath);		// directory of apk folder
			String apkName = null;					// name buffer
			
			for (File fileEntry : topDir.listFiles()) {
				apkName = getPureName(apkName);

				
				/* extract key and dissemble apk to get smali and manifest */
				try {
					//System.out.println("Start dissembling...");
					dissembleApk(fileEntry, apkName);
					//FileUtils.deleteDirectory(new File(destPath + separator + apkName));
					
					//Thread.sleep(500);
					//System.out.println(getAuthKey(fileEntry, apkName));
				} catch (Exception e) {
					System.out.println("Error: " + apkName);
				} 
				
				System.out.println();
			}
		}
		
		/* dissemble the selected list of apks under the path indicated */
		public void dissembleList() {
			
			String apkName = null;				// name buffer
			
			for (File fileEntry : apkList) {
				
				/* extract key and dissemble apk to get smali and manifest */
				try {
					apkName = getPureName(apkName);
					
					//System.out.println("Start dissembling...");
					dissembleApk(fileEntry, fileEntry.getName());
					//FileUtils.deleteDirectory(new File(destPath + separator + apkName));
				} catch (Exception e) {
					System.out.println("Error: " + apkName);
				} 
				
			}
		}
		
		
		public void getRandomFiles() {
			int fileCount = 0;
			int idx;
			
			File fileEntry;
			File[] fileArray = topDir.listFiles();
					
			while (fileCount < fileLimit){
			
				idx = (int)(Math.random()*fileArray.length);
				
				fileEntry = fileArray[idx];
				
				if (!isApkFile(fileEntry.getName())) {
					continue;
					// skip non-apk file
				}else{
					apkList.add(fileEntry);
				}
				
				fileCount++;
			}
		}
		
//		/* unzip the key and copy it to destination folder */
//		private String getAuthKey(File apk, String pureName) throws Exception {
//			//String cmd = getCopyCmd(pureName);
//			//System.out.println(cmd);
//			Process p = Runtime.getRuntime().exec(getUnzipCmd(apk, pureName));
//			
//			p.waitFor();
//			
//			MessageDigest md = MessageDigest.getInstance("MD5");
//			//System.out.println(getDestFolderName(pureName) + separator + keyPath);
//	        FileInputStream fis = new FileInputStream(getDestFolderName(pureName) + separator + keyPath);
//	 
//	        byte[] dataBytes = new byte[1024];
//	 
//	        int nread = 0; 
//	        fis.read(dataBytes, 0, 56);
//	        System.out.printf("%x\n", dataBytes[54]);
//	        System.out.printf("%x\n", dataBytes[55]);
//	        
//	        while ((nread = fis.read(dataBytes)) != -1) {
//	          md.update(dataBytes, 0, nread);
//	        };
//			
//			byte[] digest = md.digest();
//			StringBuffer sb = new StringBuffer();
//	        for (int i = 0; i < digest.length; i++) {
//	          sb.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
//	        }
//	        
//	        return sb.toString();
//		}
		
		private void dissembleApk(File apk, String pureName) throws Exception {
			Process p = Runtime.getRuntime().exec(getDissembleCmd(apk, pureName));
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
		
		private String getDissembleCmd(File apk, String name) {
			return apktoolPath + " d " + apk.getAbsolutePath() + " " + getDestFolderName(name);
		}
		
		/* eliminate the suffix for the apk
		 * must guarantee that the name is ended with ".apk" */
		private String getPureName(String name) {
			return name.substring(0, name.lastIndexOf(apkSuffix));
		}
		
		private void printDissembleList() {
			try {
				File file = new File(this.fileListPath);
				
				if (!file.exists()) {
					file.createNewFile();
				}

				FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
				BufferedWriter bw = new BufferedWriter(fw);
				
				for (File fileEntry : apkList) 
					bw.write(fileEntry.getAbsolutePath() + "\n");
				
				bw.close();
				fw.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		public static void main(String[] args) {
			long startTime = System.currentTimeMillis();

			apkDissembler ad = new apkDissembler();
			ad.getRandomFiles();
			ad.dissembleList();
			ad.printDissembleList();
			//ad.dissembleAll();

			long endTime = System.currentTimeMillis();
			
			System.out.println("\nTotal time: " + (endTime - startTime));
		}


}
