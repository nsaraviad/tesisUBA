package Geom;

import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.util.LinkedList;

import Polygons.MapPolygon;

public class AreaOperator {

	//Calculate area size average 
    public double calculateAreaSizeAverageFor(LinkedList<MapPolygon> polList) {
		double areaSum= 0;
		
		for(int p=0;p<polList.size();p++){
			areaSum= areaSum + getAreaSize(polList.get(p));
		}
		
		return areaSum/(polList.size());
	}

    //Calculate the area size from a MapPolygon
  	public double getAreaSize(MapPolygon mapPolygon){
  		
  		LinkedList<Double> xs,ys;
  		double sum= 0;
  		
  		xs= mapPolygon.getPolygonxPoints();
  		ys= mapPolygon.getPolygonyPoints();
  			
  		for(int i=0;i < xs.size()-1;i++){
  			if(i==0){
  				sum=+ xs.get(i)*(ys.get(i+1) - ys.get(xs.size()-1) );
  			}
  			else{
  				sum=+ xs.get(i)*(ys.get(i+1) - ys.get(i-1));
  			}
  		}
  		
  		double area= 0.5*Math.abs(sum);
  		
  		return area;
  	}
  	
    //calculate the sum for 
  	private int calculateSum(LinkedList<Double> xs, LinkedList<Double> ys) {
  		int res= 0;
  		for(int i=0;i < xs.size();i++){
  			res=  res + (int) (xs.get(i)*ys.get((i+1)% (xs.size())) - ys.get(i)*xs.get((i+1) % (xs.size())));
  		}
  		
  		return res;
  	}

	public Area calculateArea(LinkedList<Double> xPoints,LinkedList<Double> yPoints) {
		// Calculate polygon area
		Path2D path= new Path2D.Double();
		
		path.moveTo(xPoints.get(0), yPoints.get(0));
		
		for(int i=1;i < xPoints.size();i++)
			path.lineTo(xPoints.get(i), yPoints.get(i));
		
		path.closePath();
		
		return new Area(path);
	}
	
}
