import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;


public class apkDissembler {
	
		private static final String apkSuffix = ".apk";
		private static final String separator = "/";
		private static final String keyPath = "META-INF/CERT.RSA";
		private static final String keyFolderName = "META-INF";

		/* default path */
		private String apktoolPath = "/Users/xuyiming/Desktop/apktool/apktool";
		private String apkPath = "/Users/xuyiming/Desktop/apks2";
		private String destPath = "/Users/xuyiming/Desktop/tmp";
		
		public apkDissembler() {}
		
		/* dissemble all apks under the path indicated */
		public void dissembleAll() {
			
			File topDir = new File(apkPath);		// directory of apk folder
			String apkName = null;					// name buffer
			
			for (File fileEntry : topDir.listFiles()) {
				apkName = fileEntry.getName();
				
				System.out.println("Processing: " + apkName);
				
				if (!isApkFile(apkName)) {
					System.out.println();
					continue;						// skip non-apk file
				} else {
					apkName = getPureName(apkName);
				}
				
				/* extract key and dissemble apk to get smali and manifest */
				try {
					//System.out.println("Start dissembling...");
					//dissembleApk(fileEntry, apkName);
					//Thread.sleep(500);
					System.out.println(getAuthKey(fileEntry, apkName));
				} catch (Exception e) {
					System.out.println("Error: " + apkName);
				} //catch (InterruptedException e) {
				//}
				
				System.out.println();
			}
		}
		
		/* unzip the key and copy it to destination folder */
		private String getAuthKey(File apk, String pureName) throws Exception {
			//String cmd = getCopyCmd(pureName);
			//System.out.println(cmd);
			Process p = Runtime.getRuntime().exec(getUnzipCmd(apk, pureName));
			
			p.waitFor();
			
			MessageDigest md = MessageDigest.getInstance("MD5");
			//System.out.println(getDestFolderName(pureName) + separator + keyPath);
	        FileInputStream fis = new FileInputStream(getDestFolderName(pureName) + separator + keyPath);
	 
	        byte[] dataBytes = new byte[1024];
	 
	        int nread = 0; 
	        fis.read(dataBytes, 0, 56);
	        System.out.printf("%x\n", dataBytes[54]);
	        System.out.printf("%x\n", dataBytes[55]);
	        
	        while ((nread = fis.read(dataBytes)) != -1) {
	          md.update(dataBytes, 0, nread);
	        };
			
			byte[] digest = md.digest();
			StringBuffer sb = new StringBuffer();
	        for (int i = 0; i < digest.length; i++) {
	          sb.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
	        }
	        
	        return sb.toString();
		}
		
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
		
		private String getUnzipCmd(File apk, String name) {
			return "unzip " + apk.getAbsolutePath() + " " + keyPath + " -d " + getDestFolderName(name);// + separator + "key";
		}
		
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
		
		public static void main(String[] args) {
			apkDissembler ad = new apkDissembler();
			ad.dissembleAll();
		}
}
