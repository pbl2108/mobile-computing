
public class AppData {
	private String appName;
	private Coordinate co;
	private boolean checked;
	
	public AppData(String name, double x, double y) {
		appName = name;
		co = new Coordinate(x,y);
		checked = false;
	}
	
	public AppData(String name, Coordinate c) {
		appName = name;
		co = c;
		checked = false;
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
	
	public boolean isChecked() {
		return checked;
	}
	
	public void markAsChecked() {
		checked = true;
	}
}
