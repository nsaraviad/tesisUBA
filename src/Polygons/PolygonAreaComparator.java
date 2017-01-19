package Polygons;

import java.awt.geom.Area;
import java.util.Comparator;

import GraphComponents.GraphWay;


public class PolygonAreaComparator implements Comparator<Area> {

	@Override
	public int compare(Area a1, Area a2) {
	
		Area aux1, aux2;
		aux1= new Area(a1);
		aux2= new Area(a2);
		
		//a1 - a2
		aux1.subtract(aux2);
		
		//iguales
		if(a1.equals(a2)){
			return 0;
		}else if(!aux1.isEmpty()){ 
			// a1 > a2
			return 1;
		}else{
			//a2 > a1
			return -1;
		}
	}

}