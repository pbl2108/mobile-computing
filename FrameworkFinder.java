import java.io.BufferedReader;
import java.io.FileReader;

public class FrameworkFinder {
	public boolean isFramework(String fName) {

		return false;
	}

	//air:338416
	public boolean isAdobeAir(String line) {
		return line.contains("air.");
	}
	
	//appinventor: 154486
	public boolean isAppInventor(String line) {
		return line.contains("appinventor.");
	}
	
	//phonegap: 117692
	public boolean isPhoneGap(String line) {
		return line.contains("phonegap.");
	}
	
	//wallpaper: 718292
	public boolean isWallpaper(String line) {
		return line.contains("wallpaper");
	}

	public void processSimilarityOutput(String inputFile) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(inputFile));
			String currentLine;

			int count = 0;
			int t = 0;
			boolean found = false;
			while ((currentLine = in.readLine()) != null) {
				if (found && currentLine.startsWith("-t")) {
					t = Integer.parseInt(currentLine.substring(currentLine.indexOf(':') + 2));
					count += t;
					found = false;
				}
				if (this.isWallpaper(currentLine)) {
					//System.out.println(currentLine);
					found = true;
					//count++;
				}
			}
			System.out.println(count);
			// Close buffered reader
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		FrameworkFinder finder = new FrameworkFinder();
		finder.processSimilarityOutput("/home/peter/columbia/mob/output.txt");
	}
}
