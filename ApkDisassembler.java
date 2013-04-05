import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


import org.apache.commons.io.FileUtils;
import org.apache.lucene.util.OpenBitSet;


public class ApkDisassembler {
	
		private static final String apkSuffix = ".apk";
		private static final String separator = "/";
//		private static final String keyPath = "META-INF/CERT.RSA";
//		private static final String keyFolderName = "META-INF";
		private static final int fileLimit = 15;
		private static final String apktoolPath = "/home/Dfosak/Desktop/apktool1.5.2/apktool";
		private static final String apkPath = "/home/Dfosak/Desktop/apks";
		private static final String destPath = "/home/Dfosak/Desktop/tmp";
		private static final String fileListPath = "/home/Dfosak/Desktop/mobile-computing/apkList.txt";

		/* default path */
//		private String apktoolPath = "/Users/xuyiming/Desktop/apktool/apktool";
//		private String apkPath = "/Users/xuyiming/Desktop/apks2";
//		private String destPath = "/Users/xuyiming/Desktop/tmp";
		
		public File topDir;
		public File[] fileArray;
		
		public int currentFile;

		
		public ApkDisassembler() {
			this.topDir = new File(apkPath);		// directory of apk folder
			this.currentFile = 0;
		}
		
		/* disassemble all apks under the path indicated */
		public void disassembleAll() {
			this.fileArray = topDir.listFiles();
			
//			File topDir = new File(apkPath);		// directory of apk folder
//			String apkName = null;					// name buffer
//			
//			for (File fileEntry : topDir.listFiles()) {
//				apkName = getPureName(apkName);
//
//				
//				/* extract key and disassemble apk to get smali and manifest */
//				try {
//					//System.out.println("Start dissembling...");
//					
//					disassembleApk(fileEntry, apkName);
//					//FileUtils.deleteDirectory(new File(destPath + separator + apkName));
//					//Thread.sleep(500);
//					//System.out.println(getAuthKey(fileEntry, apkName));
//				} catch (Exception e) {
//					System.out.println("Error: " + apkName);
//				} 
//				
//			}
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
				disassembleApk(fileEntry, apkName);
				
			} catch (Exception e) {
				System.out.println("Error: " + apkName);
			} 
			
			currentFile++;
			return new File(destPath + separator + apkName);
				
		}
		
		
		public void getRandomFiles() {
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
				File file = new File(fileListPath);
				
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
		
		public static void main(String[] args) {
			long startTime = System.currentTimeMillis();

			ApkDisassembler ad = new ApkDisassembler();
			SmaliParser sp = new SmaliParser();
			BitSetBank bsb = new BitSetBank();
			
			File currentDir;
			
			//ad.getRandomFiles();
			ad.disassembleAll();
			
			while((currentDir = ad.disassembleNextFile()) != null){
				OpenBitSet bitSet = new OpenBitSet(sp.featuresCount);
				sp.apkDirectoryTraversal(currentDir, bitSet);
				bsb.add(currentDir.getName(), bitSet);
				//FileUtils.deleteDirectory(new File(destPath + separator + apkName));
			}
			
			//bsb.writeToSerial();
			ad.printDisassembleList();
			
			bsb.loadAuthorsMap();
			bsb.compareBitSetBank();
			
			long endTime = System.currentTimeMillis();
			
			
			System.out.println("\nTotal time: " + (endTime - startTime));
		}


}
