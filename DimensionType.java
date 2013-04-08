
public enum DimensionType {
	X_COORDINIATE,
	Y_COORDINATE;
	
	public DimensionType nextCoordinate() {
		if (this == X_COORDINIATE)
			return Y_COORDINATE;
		else
			return X_COORDINIATE;
	}
}
