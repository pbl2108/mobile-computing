import org.apache.lucene.util.OpenBitSet;

/*
 * An object that represents an Android Application.
 */
public class AndroidApp {
	
	public String Name;
	public OpenBitSet FeatureVector;
	public String MD5;
	public Double X;
	public Double Y;
	public Double Z;

	public AndroidApp(String name, String md5, OpenBitSet fv, Double x, Double y, Double z) {
		this.Name = name;
		this.MD5 = md5;
		this.FeatureVector = fv;
		this.X = x;
		this.Y = y;
		this.Z = z;
	}
	
	public AndroidApp(String name) {
		this(name, null, null, 0d, 0d, 0d);
	}
}
