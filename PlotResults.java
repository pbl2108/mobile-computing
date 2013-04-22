import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.lucene.util.OpenBitSet;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class PlotResults {

	public void ScatterPlot(XYDataset dataSet) {
		ScatterPlot(dataSet, "Jaccard Distance");
	}

	public void ScatterPlot(XYDataset dataSet, String title) {
		ScatterPlot(dataSet, title, new HashMap<String, String>());
	}
	public void ScatterPlot(XYDataset dataSet, String title, HashMap<String, String> revMap) {
		JFreeChart chart = ChartFactory.createScatterPlot(title, // title
				"X", // X - labels
				"Y", // Y - label
				dataSet, // data set to plot
				PlotOrientation.VERTICAL, // chart orientation
				true, // include legend
				true, // tooltips
				false // urls
				);

		
		// create and display a frame...
		ChartFrame frame = new ChartFrame(title, chart);
		
		OurMouseListener listener = new OurMouseListener(revMap);
		frame.getChartPanel().addChartMouseListener(listener);
		frame.pack();
		frame.setVisible(true);
	}
	public class OurMouseListener implements ChartMouseListener {

		 HashMap<String, String> revMapofApps;
		public OurMouseListener() {
			super();
			revMapofApps = new HashMap<String, String>();
		}
		public OurMouseListener(HashMap<String, String> revMap){
			this();
			revMapofApps = revMap;
		}
		
		@Override
		public void chartMouseClicked(ChartMouseEvent arg0) {
			// TODO Auto-generated method stub
			ChartEntity entity = arg0.getEntity();

			if (entity != null && entity instanceof XYItemEntity) {
				XYItemEntity ent = (XYItemEntity) entity;
							
				
				int sindex = ent.getSeriesIndex();
				int iindex = ent.getItem();
				
				XYDataset dataSet = ent.getDataset();
				
				System.out.println(revMapofApps.get(dataSet.getXValue(sindex, iindex) + " " + dataSet.getYValue(sindex, iindex)));
				System.out.println("x = " + dataSet.getXValue(sindex, iindex) + " y = " + dataSet.getYValue(sindex, iindex));
			}
		}

		@Override
		public void chartMouseMoved(ChartMouseEvent arg0) {
			// TODO Auto-generated method stub
			ChartEntity entity = arg0.getEntity();
			if (entity != null && entity instanceof XYItemEntity) {
				XYItemEntity ent = (XYItemEntity) entity;
				
				int sindex = ent.getSeriesIndex();
				int iindex = ent.getItem();
				
				XYDataset dataSet = ent.getDataset();
							
				ent.setToolTipText(revMapofApps.get(dataSet.getXValue(sindex, iindex) + " " + dataSet.getYValue(sindex, iindex)) + " x = " + dataSet.getXValue(sindex, iindex) + " y = " + dataSet.getYValue(sindex, iindex));

			}
		}
		
	}

	public void SampleScatterPlot(XYDataset dataSet) {
		ScatterPlot(createDataset());
	}

	public static void main(String[] args) {
		try {
			
			/* Example
			 * -i /home/peter/Dropbox/mobile-computing/bitSetMap_d_15_s_8 -x com.mcc.probeapp-2 -y com.mcp.android.dq4u-28 */
			Options options = createOptions();
			// create the command line parser
			CommandLineParser parser = new PosixParser();
			CommandLine cmd;
			OpenBitSet x, y;
			BitSetBank bsb = new BitSetBank();


			cmd = parser.parse(options, args);
			
			String inputCSV = cmd.getOptionValue("c");
			String inputPath = cmd.getOptionValue("i");
			if (inputCSV != null) {
				
				XYSeries series = new XYSeries("Android Apps");
				HashMap<String, String> revMap = new HashMap<String,String>();
				
				readFromCSV(inputCSV, series, revMap);
				
				XYSeriesCollection seriesCollection = new XYSeriesCollection();
				seriesCollection.addSeries(series);
				
				PlotResults plotter = new PlotResults();
				plotter.ScatterPlot(seriesCollection, inputCSV, revMap);
			}else if (inputPath != null) {
				bsb.readAllFromSerial(inputPath);
				
				/* Max variance Logic and Max variance Content */
				String xKey = cmd.getOptionValue("x");// "com.mcc.probeapp-2"; 
				if (xKey == null)
					xKey = bsb.findVectorWithMaxVariance(true);
				System.out.println("X:" + xKey);
				x = bsb.bitSetsHashMap.get(xKey).LogicVector;

				String yKey = cmd.getOptionValue("y"); //"com.mcp.android.dq4u-28";
				if (yKey == null)
					 bsb.findVectorWithMaxVariance(false);
				System.out.println("Y:" + yKey);
				y = bsb.bitSetsHashMap.get(yKey).ContentVector;

				bsb.plotAndCompareBitSetBank(x, y, " X: " + xKey + " \r\n Y:"
						+ yKey);
			}
			else{
				System.out.println("Please specify a directory of .csv or .ser files.");
				showHelp(options);
			}


			
			
//			
//			long k = bsb.bitSetsHashMap.get("com.mcc.probeapp-2").LogicVector.capacity();
//			OpenBitSet logic1 = new OpenBitSet (k);
//			OpenBitSet logic2 = new OpenBitSet (k);
//			for (int i = 0; i< k; i++){
//				if (i % 2 == 0)
//					logic1.fastSet(i);
//				else
//					logic2.fastSet(i);
//			}
			

			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void readFromCSV(String filePath, XYSeries series, HashMap<String, String> revMap) {
		try {
			if (filePath == null || filePath.isEmpty())
				return;
			
			if (filePath.endsWith(".csv"))
				readandPlotCSV(filePath, series, revMap);
			
			File dir = new File(filePath);
			if (dir.isFile())
				return;
			
			readAllFromCSV(dir, series, revMap);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void readAllFromCSV(File dir, XYSeries series, HashMap<String, String> revMap) {
		try {
			for (File entry : dir.listFiles()) {
					
					if (entry.isDirectory()) {
						readAllFromCSV(entry, series, revMap);
						continue;
					}
					
					if (entry.isFile() && !entry.getName().endsWith(".csv"))
						continue;
					else
						readandPlotCSV(entry.getAbsolutePath(), series, revMap);
	
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private static void readandPlotCSV(String filePath, XYSeries series, HashMap<String, String> revMap) throws IOException {
			FileReader fr = new FileReader(filePath);
			BufferedReader br = new BufferedReader(fr);
			String currentRow;
			String delimiter = "\\s*,\\s*";
			String tokens[];
			String token;

						
			while ((currentRow = br.readLine()) != null) {
				tokens = currentRow.split(delimiter);
				double jSimX = Double.parseDouble(tokens[1]);
				double jSimY = Double.parseDouble(tokens[2]);
				series.add(jSimX, jSimY);
				revMap.put(jSimX + " " + jSimY,tokens[0]);

			}
			
			br.close();
			fr.close();



	}
		

	private static Options createOptions() {
		Options options = new Options();
		options.addOption("h", "help", false, "print this message and exit");
		options.addOption("i", "input", true, "input folder containing .ser files for loading the hashmap of vectors");
		options.addOption("c", "csv", true, "input is csv file");
		options.addOption("x", "X-app", true, "the name of the app used as X: base vector for plotting. The app has to be present in the .ser file(s).");
		options.addOption("y", "Y-app", true, "the name of the app used as Y: base vector for plotting. The app has to be present in the .ser file(s).");

		return options;
	}
	private static void showHelp(Options options) {
		System.out.println("Current working directory : "
				+ System.getProperty("user.dir"));
		HelpFormatter h = new HelpFormatter();
		h.printHelp("help", options);
		System.exit(-1);
	}

	private static final Random r = new Random();

	private static XYDataset createDataset() {
		XYSeriesCollection result = new XYSeriesCollection();
		XYSeries series = new XYSeries("Random");
		for (int i = 0; i <= 100; i++) {
			double x = r.nextDouble();
			double y = r.nextDouble();
			series.add(x, y);
		}
		result.addSeries(series);
		return result;
	}
}