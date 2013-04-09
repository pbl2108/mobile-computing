import java.util.ArrayList;


/**
 * API for K-d Tree (2D)
 * 
 *	Support insert node and range search for a circle
 */

public class KdTree {
	
	private KdTreeNode root;

	class KdTreeNode {
		private KdTreeNode left;
		private KdTreeNode right;
		private AppData obj;
		
		public KdTreeNode(AppData app) {
			left = null;
			right = null;
			obj = app;
		}
		
		public void insertNode(AppData newObj, DimensionType current) {
			if (obj.getCoordinate().greater(newObj.getCoordinate(), current)) {
				if (left != null) {
					left.insertNode(newObj, current.nextCoordinate());
				} else {
					left = new KdTreeNode(newObj);
					//System.out.println("add node to left branch: " + newObj.getAppInfo());
				}
			} else {
				if (right != null) {
					right.insertNode(newObj, current.nextCoordinate());
				} else {
					right = new KdTreeNode(newObj);
					//System.out.println("add node to right branch: " + newObj.getAppInfo());
				}
			}
		}
		
		public void nodesInRange(CircleRange cr, DimensionType current, ArrayList<AppData> result) {
			
			boolean lowerThanUpperBound = false;
			boolean greaterThanLowerBound = false;
			
			if (obj.getCoordinate().greater(cr.getLowerBound(), current)) {
				greaterThanLowerBound = true;
				if (left != null)
					left.nodesInRange(cr, current.nextCoordinate(), result);
			}
			
			if (obj.getCoordinate().smaller(cr.getUpperBound(), current)) {
				lowerThanUpperBound = true;
				if (right != null)
					right.nodesInRange(cr, current.nextCoordinate(), result);
			}
			
			if (obj.isChecked())
				return;
			
			/* if the coordinate of this dimension is between upper bound and lower bound
			 * go and check the other dimension to see whether this node is inside this range */
			if (greaterThanLowerBound && lowerThanUpperBound) {
				if (obj.getCoordinate().greater(cr.getLowerBound(), current.nextCoordinate())
						&& obj.getCoordinate().smaller(cr.getUpperBound(), current.nextCoordinate())) {
					
					// in the range, add this app into the list
					//if (!cr.isCenterApp(obj.getName()))
						result.add(obj);
					//System.out.println("added " + obj.getAppInfo() + " to the list");
				}
			}
		}
	}
	
	/* describe the circle we want to search */
	class CircleRange {
		private Coordinate lowerBound;
		private Coordinate upperBound;
		private String centerApp;
		
		public CircleRange(AppData center, double r) {
			double x = center.getCoordinate().getCoordinateWithDimension(DimensionType.X_COORDINIATE);
			double y = center.getCoordinate().getCoordinateWithDimension(DimensionType.Y_COORDINATE);
			
			lowerBound = new Coordinate(x - r, y - r);
			upperBound = new Coordinate(x + r, y + r);
			
			centerApp = center.getName();
			
			center.markAsChecked();			// do not compare this node any more
			
			//System.out.println("the center: " + x + " " + y);
		}
		
		public Coordinate getUpperBound() {
			return upperBound;
		}
		
		public Coordinate getLowerBound() {
			return lowerBound;
		}
		
		/* public boolean isCenterApp(String name) {
			return centerApp.equals(name);
		} */
	}
	
	
	/** functions **/
	
	private void initRoot(AppData app) {
		root = new KdTreeNode(app);
	}
	
	private void initRoot(String appName, double x, double y) {
		initRoot(new AppData(appName, x, y));
	}
	
	public KdTree() {}
	
	public KdTree(AppData app) {
		initRoot(app);
	}
	
	public KdTree(String appName, double x, double y) {
		initRoot(appName, x, y);
	}
	
	public void insertNode(AppData newObj) {
		if (root != null)
			root.insertNode(newObj, DimensionType.X_COORDINIATE);
		else
			initRoot(newObj);
	}
	
	public void insertNode(String appName, double x, double y) {
		if (root != null)
			root.insertNode(new AppData(appName, x, y), DimensionType.X_COORDINIATE);
		else
			initRoot(appName, x, y);
	}
	
	public ArrayList<AppData> searchRange(AppData center, double radius) {
		ArrayList<AppData> ret = new ArrayList<AppData>();
		root.nodesInRange(new CircleRange(center, radius), DimensionType.X_COORDINIATE, ret);
		return ret;
	}
}
