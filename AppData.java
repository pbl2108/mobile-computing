
public class AppData {
	private String appName;
	private Coordinate co;
	
	public AppData(String name, double x, double y) {
		appName = name;
		co = new Coordinate(x,y);
	}
	
	public AppData(String name, Coordinate c) {
		appName = name;
		co = c;
	}
	
	public Coordinate getCoordinate() {
		return co;
	}
	
	public String getName() {
		return appName;
	}
	
	public String getAppInfo() {
		return appName + " " + co.toString();
	}
}
