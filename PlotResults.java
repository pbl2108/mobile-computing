import java.util.Random;

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
		ScatterPlot(null);
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