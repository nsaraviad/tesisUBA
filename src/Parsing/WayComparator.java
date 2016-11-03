package Parsing;

import java.util.Comparator;

import GraphComponents.GraphWay;


public class WayComparator implements Comparator<GraphWay> {

	@Override
	public int compare(GraphWay w1, GraphWay w2) {
	
		//Orden ascendente de caminos por id.
		if(w1.getId() < w2.getId()){
			return -1;
		}else if(w1.getId() > w2.getId()){
			return 1;
		}else{
			return 0;
		}
	}

}
