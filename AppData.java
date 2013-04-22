
public class AppData {
	private String appName;
	private Coordinate co;
	private boolean checked;
	
	private boolean writenToCluster;
	private boolean writenToSimilar;
	
	public AppData(String name, double x, double y) {
		appName = name;
		co = new Coordinate(x,y);
		checked = false;
		
		writenToCluster = false;
		writenToSimilar = false;
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
	
	public String getAppInfoCSV() {
		return appName + "," + co.toStringCSV();
	}
	
	public boolean isChecked() {
		return checked;
	}
	
	public void markAsChecked() {
		checked = true;
	}
	
	public boolean isWritenToCluster() {
		return writenToCluster;
	}
	
	public void markAsWritenToCluster() {
		writenToCluster = true;
	}
	
	public boolean isWritenToSimilar() {
		return writenToSimilar;
	}
	
	public void markAsWritenToSimilar() {
		writenToSimilar = true;
	}
}
