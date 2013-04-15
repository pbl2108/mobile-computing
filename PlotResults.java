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
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class PlotResults {

	public static void ScatterPlot(XYDataset dataSet) {
		ScatterPlot(dataSet, "Jaccard Distance");
	}

	public static void ScatterPlot(XYDataset dataSet, String title) {
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
		frame.pack();
		frame.setVisible(true);
	}

	public static void SampleScatterPlot(XYDataset dataSet) {
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

			String inputPath = cmd.getOptionValue("i");
			if (inputPath != null) {
				bsb.readAllFromSerial(inputPath);
			}
			else{
				System.out.println("Please specify a directory of .ser files.");
				showHelp(options);
			}
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
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static Options createOptions() {
		Options options = new Options();
		options.addOption("h", "help", false, "print this message and exit");
		options.addOption("i", "input", true, "input folder containing .ser files for loading the hashmap of vectors");
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