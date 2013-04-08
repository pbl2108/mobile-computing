import java.util.ArrayList;


public class runKdTree {
	public static void main(String[] args) {
		
		BitSetBank bsb = new BitSetBank();
		kdtreeCompare kd = new kdtreeCompare();
		
		bsb.readFromSerial("/Users/xuyiming/Desktop/bitSetMap300.ser");
		
		//System.out.println(bsb.bitSetsHashMap.size());
		bsb.compareBitSetBank_KDtree(null, null, kd);
		System.out.println("-----------------------");
		bsb.readFromSerial("/Users/xuyiming/Desktop/bitSetMap300.ser");
		//System.out.println(bsb.bitSetsHashMap.size());
		kd.runKdtreeCompare(0.1, bsb);
	}
}
