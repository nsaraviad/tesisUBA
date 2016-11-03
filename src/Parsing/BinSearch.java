package Parsing;

import GraphComponents.*;
import java.util.LinkedList;


public class BinSearch {

	public int binSearchOverNodes(LinkedList<GraphNode> nodes, Long ref) {
		
			//Binary Search over nodes list searching ref paramether.
			int first, last, middle,retIndex;
			
			retIndex= -1;
			first= 0;
			last= nodes.size() -1;
			middle= (first + last)/2;
			
			while(first <= last){
				if(nodes.get(middle).getId() < ref){
					first= middle +1;
				}else if(nodes.get(middle).getId() == ref){
					retIndex= middle;
					break;
				}else{
					last= middle - 1;
				}
				middle= (first + last)/2;
			}
			return retIndex;
	}

	public int binSearchOverWays(LinkedList<GraphWay> ways, Long ref) {
		int first, last, middle,retIndex;
		
		retIndex= -1;
		first= 0;
		last= ways.size() -1;
		middle= (first + last)/2;
		
		while(first <= last){
			if(ways.get(middle).getId() < ref){
				first= middle +1;
			}else if(ways.get(middle).getId() == ref){
				retIndex= middle;
				break;
			}else{
				last= middle - 1;
			}
			middle= (first + last)/2;
		}
		
		return retIndex;
	}

}
