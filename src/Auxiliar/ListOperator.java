package Auxiliar;

import java.util.LinkedList;

public class ListOperator {

	//Check if l1 intersects l2
	public boolean containsIntersects(LinkedList<Double> l1,LinkedList<Double> l2) {
		
		boolean intersect= false;
		
		for(int i=0;i<l1.size() && !intersect;i++){
			if(l2.contains(l1.get(i)))
				intersect= true;
		}
		
		return intersect;
	}

	
}
