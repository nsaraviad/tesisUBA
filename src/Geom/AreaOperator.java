package Geom;

import java.util.LinkedList;

import Polygons.MapPolygon;

public class AreaOperator {

	//Calculate area size average 
    public int calculateAreaSizeAverageFor(LinkedList<MapPolygon> polList) {
		int areaSum= 0;
		
		for(int p=0;p<polList.size();p++){
			areaSum= areaSum + getAreaSize(polList.get(p));
		}
		
		return areaSum/(polList.size());
	}

    //Calculate the area size from a MapPolygon
  	private int getAreaSize(MapPolygon mapPolygon){
  		
  		LinkedList<Double> xs,ys;
  		int sum= 0;
  		
  		if(mapPolygon.getSubpathsX() == null){
  			xs= mapPolygon.getPolygonxPoints();
  			ys= mapPolygon.getPolygonyPoints();
  			
  			sum= sum + calculateSum(xs, ys); 
  		}
  		else{//tiene mas de un subpaths
  			for(int s=0; s < mapPolygon.getSubpathsX().size();s++)
  				sum= sum + calculateSum(mapPolygon.getSubpathsX().get(s),mapPolygon.getSubpathsY().get(s));
  		}
  		
  		return Math.abs(sum/2);
  	}
  	
    //calculate the sum for 
  	private int calculateSum(LinkedList<Double> xs, LinkedList<Double> ys) {
  		int res= 0;
  		for(int i=0;i < xs.size();i++){
  			res=  res + (int) (xs.get(i)*ys.get((i+1)% (xs.size())) - ys.get(i)*xs.get((i+1) % (xs.size())));
  		}
  		
  		return res;
  	}
	
	
	
}
