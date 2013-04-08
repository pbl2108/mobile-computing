import java.io.File;
import java.io.IOException;

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
	
	  private static Options createOptions() {
		    Options options = new Options();
		    options.addOption("h", "help", false, "print this message and exit");
		    options.addOption("i", "input", true, "input folder containing all apks");
		    options.addOption("o", "output", true, "temporary folder to hold apks while processing");
		    options.addOption("r", "rand", true, "decompile a random assortment of apks");
		    options.addOption("a", "all", false, "decompile all apks");
		    options.addOption("d", "divisor", true, "number of sections the total apks should be divided into");
		    options.addOption("s", "section", true, "respective fraction of the apks to be processed");
		    
		    
		    return options;
	  }
	  
	  private static void showHelp(Options options) {
		    HelpFormatter h = new HelpFormatter();
		    h.printHelp("help", options);
		    System.exit(-1);
		  }
	
	public static void main(String[] args) {
		
		ApkDisassembler ad = null;
		Options options = createOptions();
		try {
			// create the command line parser
			CommandLineParser parser = new PosixParser();
			CommandLine cmd = parser.parse(options, args);
			String inputPath = cmd.getOptionValue("i"); 
			
			
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
			ad.disassembleAll();
			
			
			if( cmd.getOptionValue( "r" ) != null ) {
				int randSize = Integer.parseInt(cmd.getOptionValue( "r" ));
				ad.getRandomFiles(randSize);
			}
			
			if( cmd.getOptionValue( "d" ) != null && cmd.getOptionValue( "s" ) != null ) {
				int divisor = Integer.parseInt(cmd.getOptionValue( "d" ));
				int sectionNumber = Integer.parseInt(cmd.getOptionValue( "s" ));
				ad.getFileSection(divisor, sectionNumber);
			}else if (cmd.getOptionValue( "d" ) == null && cmd.getOptionValue( "s" ) == null ){
				//Do nothing
			}else{
				System.out.println("Please specify both divisor and section number");
				showHelp(options);
			}
			
			
			
				 
		} catch (ParseException e1) {
			e1.printStackTrace();
			showHelp(options);
		}
		
		long startTime = System.currentTimeMillis();

		
		SmaliParser sp = new SmaliParser();
		BitSetBank bsb = new BitSetBank();
		
		File currentDir;
		
		
		while((currentDir = ad.disassembleNextFile()) != null){
			//System.out.println(currentDir.getName());
			OpenBitSet bitSet = new OpenBitSet(sp.featuresCount);
			sp.apkDirectoryTraversal(currentDir, bitSet);
			bsb.add(currentDir.getName(), bitSet);
			try {
				FileUtils.deleteDirectory(new File(currentDir.getAbsolutePath()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		bsb.writeToSerial();
		ad.printDisassembleList();
		
//		bsb.loadAuthorsMap();
//		bsb.compareBitSetBank();
		
		long endTime = System.currentTimeMillis();
		
		
		System.out.println("\nTotal time: " + (endTime - startTime) + " ms");
		System.out.println("\nAverage time for 1 out of " + bsb.bitSetsHashMap.size() +  " app: " + (endTime - startTime)/bsb.bitSetsHashMap.size() + " apps/ms");
	}
}