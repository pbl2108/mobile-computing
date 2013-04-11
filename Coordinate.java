
public class Coordinate {
	public double x;
	public double y;
	
	public Coordinate(double newX, double newY) {
		x = newX;
		y = newY;
	}
	
	public double getCoordinateWithDimension(DimensionType d) {
		return (d == DimensionType.X_COORDINIATE) ? x : y;
	}
	
	/* actually greater or equal to */
	public boolean greater(Coordinate obj, DimensionType d) {
		return this.getCoordinateWithDimension(d) >= obj.getCoordinateWithDimension(d);
	}
	
	public boolean smaller(Coordinate obj, DimensionType d) {
		return this.getCoordinateWithDimension(d) < obj.getCoordinateWithDimension(d);
	}
	
	@Override
	public String toString() {
		return "(" + x + "," + y + ")";
	}
}
