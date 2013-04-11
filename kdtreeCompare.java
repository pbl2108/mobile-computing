import java.util.ArrayList;


public class kdtreeCompare {

	private KdTree kdtree;
	private ArrayList<AppData> appList;
	private static final double defaultCompareRadius = 0.01;
	
	public kdtreeCompare() {
		kdtree = new KdTree();
		appList = new ArrayList<AppData>();
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
		//ArrayList<String> highSimApps = new ArrayList<String>();
		int ret = 0;
		int totalSim = 0;
		int comp = 0;
		
		bsb.loadAuthorsMap();
		
		for (int i = 0; i < appList.size(); i++) {
			nearApps = kdtree.searchRange(appList.get(i), radius);
			ret = doPairwiseComparisonWithNeighbors(appList.get(i), nearApps, bsb);
			//ret = doPairwiseComparison(appList.get(i), appList, bsb);
			comp += nearApps.size();
			
			if (ret != 0) {
				System.out.println("total number: " + ret);
				System.out.println("-----------------------");
				totalSim += ret;
				
				/* if (ret >= 100)
					highSimApps.add(appList.get(i).getName() + ".apk");*/
			}
		}
		
		//System.out.println("-----------------------");
		System.out.println("total similar apps detected: " + totalSim);
		//System.out.println("-----------------------");
		System.out.println("average comparison per app: " + (double)comp/bsb.bitSetsHashMap.size());
		
		/*for (int i = 0; i < highSimApps.size(); i++)
			System.out.println(highSimApps.get(i));*/
	}
	
	private int doPairwiseComparisonWithNeighbors(AppData base, ArrayList<AppData> list, BitSetBank bsb) {
		double similarity;
		int count = 0;
		
		for (int i = 0; i < list.size(); i++) {
			similarity = bsb.JaccardSim(base.getName(), list.get(i).getName());
			if (similarity > 0.7 && bsb.isDifferentAuthors(base.getName(), list.get(i).getName())) {
				System.out.println(base.getName() + ", " + list.get(i).getName() + " : similarity " + similarity);
				count++;
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
			similarity = bsb.JaccardSim(base.getName(), list.get(i).getName());
			//System.out.println(similarity);
			if (similarity > 0.7) {
				System.out.println(base.getName() + ", " + list.get(i).getName());
				count++;
			}
			}
		}
		
		return count;
	}
}
