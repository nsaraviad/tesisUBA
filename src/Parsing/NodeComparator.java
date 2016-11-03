package Parsing;

import java.util.Comparator;

import GraphComponents.GraphNode;


public class NodeComparator implements Comparator<GraphNode> {

	@Override
	public int compare(GraphNode n1, GraphNode n2) {
		//Orden ascendente por número de id 
		if(n1.getId() < n2.getId()){
			return -1;
		}else if(n1.getId() > n2.getId()){
			return 1;
		}else{
			return 0;
		}
		
	}

}
