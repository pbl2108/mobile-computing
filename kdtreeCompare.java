import java.util.ArrayList;

import org.apache.lucene.util.OpenBitSet;


public class kdtreeCompare {
	
	class statistics {
		private int[] logic = {0, 0, 0};
		private int[] content = {0, 0, 0};
		private int[] both = {0, 0, 0};
		
		public statistics() {}
		
		private int _record(int[] a, double s) {
			if (s <= 0.8) {
				a[0] += 1;
				return 0;
			} else if (s <= 0.9) {
				a[1] += 1;
				return 1;
			} else {
				a[2] += 1;
				return 2;
			}
		}
		
		public void recordStatistics(double ls, double cs) {
			int l_index = _record(logic, ls);
			int c_index = _record(content, cs);
			
			if (l_index == c_index)
				both[l_index] += 1;
		}
		
		public void printStatistics() {
			System.out.println("logic vector: (0.7, 0.8]: " + logic[0] + "; (0.8, 0.9]: " + logic[1] + "; (0.9, 1.0]: " + logic[2]);
			System.out.println("content vector: (0.7, 0.8]: " + content[0] + "; (0.8, 0.9]: " + content[1] + "; (0.9, 1.0]: " + content[2]);
			System.out.println("match both: (0.7, 0.8]: " + both[0] + "; (0.8, 0.9]: " + both[1] + "; (0.9, 1.0]: " + both[2]);
		}
	}

	private KdTree kdtree;
	private ArrayList<AppData> appList;
	private static final double defaultCompareRadius = 0.01;
	private statistics stat;
	public final double threshold = 0.9;
	
	MySQLAccess sqlAccess;
	public kdtreeCompare() {
		kdtree = new KdTree();
		appList = new ArrayList<AppData>();
		stat = new statistics();
		this.sqlAccess = new MySQLAccess();
	}
	
	public void insertKdtree(String name, double x, double y) {
		AppData app = new AppData(name, x, y);
		appList.add(app);
		kdtree.insertNode(app);
	}
	
	
	public int getAppListSize() {
		return appList.size();
	}
	
	public void runKdtreeCompare(BitSetBank bsb) {
		//System.out.println(bsb.bitSetsHashMap.size());
		runKdtreeCompare(defaultCompareRadius, bsb);
	}
	
	public void runKdtreeCompare(double radius, BitSetBank bsb) {
		
		ArrayList<AppData> nearApps;
		ArrayList<String> highSimApps = new ArrayList<String>();
		int ret = 0;
		int totalSim = 0;
		int comp = 0;
		
		//bsb.loadAuthorsMap();
		
		for (int i = 0; i < appList.size(); i++) {
			nearApps = kdtree.searchRange(appList.get(i), radius);
			ret = doPairwiseComparisonWithNeighbors(appList.get(i), nearApps, bsb);
			//ret = doPairwiseComparison(appList.get(i), appList, bsb);
			comp += nearApps.size();
			
			if (ret != 0) {
				System.out.println("total number: " + ret);
				System.out.println("-----------------------");
				totalSim += ret;
				
				if (ret >= 25)
					highSimApps.add(appList.get(i).getName() + ".apk");
			}
		}
		
		//System.out.println("-----------------------");
		System.out.println("total apps scanned: " + appList.size());
		System.out.println("average comparison per app: " + (double)comp/bsb.bitSetsHashMap.size());
		System.out.println("total similar apps detected: " + totalSim);
		stat.printStatistics();
		
		System.out.println("-----------------------");
		for (int i = 0; i < highSimApps.size(); i++)
			System.out.println(highSimApps.get(i));
	}
	
	private int doPairwiseComparisonWithNeighbors(AppData base, ArrayList<AppData> list, BitSetBank bsb) {
		
		double simLogic, simContent;
		int count = 0;
		
		for (int i = 0; i < list.size(); i++) {
			simLogic = bsb.JaccardSimLogic(base.getName(), list.get(i).getName());
			simContent = bsb.JaccardSimContent(base.getName(), list.get(i).getName());
			if (simLogic > threshold && simContent > threshold && sqlAccess.IsAuthorEqual(base.getName(), list.get(i).getName())) {
				System.out.println(base.getName() + ", " + list.get(i).getName() + " : similarity (" + simLogic + ", " + simContent + ")");
				count++;
				stat.recordStatistics(simLogic, simContent);
			}
		}
		
		return count;
	}
	
	/* pure pairwise comparison */
	private int doPairwiseComparison(AppData base, ArrayList<AppData> list, BitSetBank bsb) {
		double similarity;
		int count = 0;
		
		for (int i = 0; i < list.size(); i++) {
			if (base != list.get(i)) {
			similarity = bsb.JaccardSimLogic(base.getName(), list.get(i).getName());
			//System.out.println(similarity);
			if (similarity > threshold) {
				System.out.println(base.getName() + ", " + list.get(i).getName());
				count++;
			}
			}
		}
		
		return count;
	}
}
