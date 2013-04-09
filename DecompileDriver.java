import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.util.OpenBitSet;


public class DecompileDriver {
	
	public static File inputFolder;
	public static File outputFolder;
	public static int divisor = 0;
	public static int sectionNumber = 0;
	
	  private static Options createOptions() {
		    Options options = new Options();
		    options.addOption("h", "help", false, "print this message and exit");
		    options.addOption("i", "input", true, "input folder containing all apks");
		    options.addOption("o", "output", true, "temporary folder to hold apks while processing");
		    options.addOption("r", "rand", true, "decompile a random assortment of apks");
		    options.addOption("a", "all", false, "decompile all apks");
		    options.addOption("d", "divisor", true, "number of sections the total apks should be divided into");
		    options.addOption("s", "section", true, "respective fraction of the apks to be processed");
		    options.addOption("w", "whitelist", false, "generate whitelist for files; output class file is whitelist-classes.txt");
		    
		    return options;
	  }
	  
	  private static void showHelp(Options options) {
		  	System.out.println("Current working directory : " + System.getProperty("user.dir"));
		    HelpFormatter h = new HelpFormatter();
		    h.printHelp("help", options);
		    System.exit(-1);
		  }
	
	/*
	 * Use this function to plot against two apps.
	 */
	public static void main(String[] args) {
		//SmaliParser sp = new SmaliParser();
		OpenBitSet x, y;
		
		BitSetBank bsb = new BitSetBank();
		bsb.readFromSerial("/home/peter/github/mobile-computing/results/bitSetMap1000.ser");
		String xKey = bsb.findVectorWithMaxVariance();
		System.out.println("X:" + xKey);
		x = bsb.bitSetsHashMap.get(xKey);
		
		String yKey = bsb.findMostDistant(x);
		System.out.println("Y:" + yKey);
		y = bsb.bitSetsHashMap.get(yKey);
		
		//Object[] featureVectors = bsb.bitSetsHashMap.values().toArray();
		bsb.plotAndCompareBitSetBank(x, y, "20  and 100 -- 1000");
	}

	public static void main_(String[] args) {
		
		long startTime = System.currentTimeMillis();
		ApkDisassembler ad = null;
		Options options = createOptions();
		try {
			// create the command line parser
			CommandLineParser parser = new PosixParser();
			CommandLine cmd = parser.parse(options, args);
			String inputPath = cmd.getOptionValue("i"); 
			
			
			//Get the input and output paths. Default to output /tmp if no path present 
			if (inputPath != null)
				inputFolder = new File(inputPath);
			else{
				System.out.println("Please specify an input apk folder");
				showHelp(options);
			}
			
			 if(!inputFolder.exists() || !inputFolder.isDirectory()){
				System.out.println("Invalid input folder");
				showHelp(options);
			 }
			 
			 String outputPath = cmd.getOptionValue("o"); 
				
				
			if (outputPath != null)
				outputFolder = new File(outputPath);
			else{
				outputFolder = new File("tmp");
			}
			
			ad = new ApkDisassembler(inputFolder.getAbsolutePath(), outputFolder.getAbsolutePath());
			
			
			//Check which apks to decompile
			if( cmd.getOptionValue( "r" ) != null ) {
				int randSize = Integer.parseInt(cmd.getOptionValue( "r" ));
				ad.getRandomFiles(randSize);
			}else if( cmd.getOptionValue( "d" ) != null && cmd.getOptionValue( "s" ) != null ) {
				divisor = Integer.parseInt(cmd.getOptionValue( "d" ));
				sectionNumber = Integer.parseInt(cmd.getOptionValue( "s" ));
				ad.getFileSection(divisor, sectionNumber);
			}else if ((cmd.getOptionValue( "d" ) != null && cmd.getOptionValue( "s" ) == null ) 
					  	|| (cmd.getOptionValue( "d" ) == null && cmd.getOptionValue( "s" ) != null )){
				System.out.println("Please specify both divisor and section number");
				showHelp(options);
			}else{
				ad.disassembleAll();
			}
			
			//Check if user wants to generate whitelist file
			if( cmd.hasOption( "w" ) ) {
				System.out.println("Generating Whitelist");
				WhiteListGenerator wl = new WhiteListGenerator();
				wl.generateWhiteList(ad);
				System.exit(0);
			}
			
			
			
				 
		} catch (ParseException e1) {
			e1.printStackTrace();
			showHelp(options);
		}
		
	

		
		SmaliParser sp = new SmaliParser();
		BitSetBank bsb = new BitSetBank();
		
		File currentDir;
		
		ad.createApkListLog(divisor, sectionNumber);
		//bsb.createSerialWriteStream(divisor, sectionNumber);
		
		while((currentDir = ad.disassembleNextFile()) != null){				
			OpenBitSet bitSet = new OpenBitSet(sp.featuresCount);
			sp.apkDirectoryTraversal(currentDir, bitSet);
			bsb.add(currentDir.getName(), bitSet);
			try {
				FileUtils.deleteDirectory(new File(currentDir.getAbsolutePath()));
				ad.bw.write(currentDir.getAbsolutePath() + "\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		bsb.writeToSerial();
		ad.closeApkListLog();
		
//		bsb.loadAuthorsMap();
//		bsb.compareBitSetBank();
		
		long endTime = System.currentTimeMillis();
		
		
		System.out.println("\nTotal time: " + (endTime - startTime) + " ms");
		System.out.println("\nAverage time for 1 out of " + bsb.bitSetsHashMap.size() +  " app: " + (endTime - startTime)/bsb.bitSetsHashMap.size() + " ms/app");
		System.out.println("Failed APK's: " + sp.failedApk);
	}
}