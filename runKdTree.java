

public class runKdTree {
	public static void main(String[] args) {
		
		BitSetBank bsb = new BitSetBank();
		kdtreeCompare kd = new kdtreeCompare();
		long startTime = System.currentTimeMillis();
		
		//bsb.readFromSerial("/Users/xuyiming/Desktop/bitSetMap1000.ser");
		bsb.readAllFromSerial("/Users/xuyiming/Desktop/mobile-computing/results");
		
		//System.out.println(bsb.bitSetsHashMap.size());
		bsb.compareBitSetBank_KDtree(null, null, kd);
		//System.out.println(kd.getAppListSize());
		//System.out.println("-----------------------");
		//bsb.readFromSerial("/Users/xuyiming/Desktop/bitSetMap1000.ser");
		
		//System.out.println(bsb.bitSetsHashMap.size());
		kd.runKdtreeCompare(0.1, bsb);
		
		long endTime = System.currentTimeMillis();
		
		System.out.println("-----------------------");
		System.out.println("the total time: " + (endTime - startTime));
	}
}
