package Polygons;

import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.LinkedList;

import GraphComponents.Pair;

public class MapPolygon {
	private int id_polygon;
	private LinkedList<Double> xPoints;
	private LinkedList<Double> yPoints;
	private Area polygon_area;
	private LinkedList<LinkedList<Double>> subpathsX;
	private LinkedList<LinkedList<Double>> subpathsY;
	
	public MapPolygon(int id, Pair polygonPoints, Area area){
		id_polygon= id;
		xPoints= (LinkedList<Double>) polygonPoints.getFirst();
		yPoints= (LinkedList<Double>) polygonPoints.getSecond();
		polygon_area= area;
		subpathsX= null;
		subpathsY= null;
	}
	
	public void setId(int id){
		id_polygon= id;
	}
	
	public int getPolygonId(){
		return id_polygon;
	}
	
	public LinkedList<Double> getPolygonxPoints(){
		return xPoints;
	}
	
	public LinkedList<Double> getPolygonyPoints(){
		return yPoints;
	}
	
	
	public void setxPoints(LinkedList<Double> points){
		xPoints= points;
	}
	
	
	public void setyPoints(LinkedList<Double> points){
		yPoints= points;
	}
	
	
	public Area getPolArea(){
		return polygon_area;
	}
	
	public LinkedList<LinkedList<Double>> getSubpathsX(){
		return subpathsX;
	}
	
	public LinkedList<LinkedList<Double>> getSubpathsY(){
		return subpathsY;
	}
	
	public void setSubpathsX(LinkedList<LinkedList<Double>> subpathsList){
		subpathsX= subpathsList;
	}
	
	public void setSubpathsY(LinkedList<LinkedList<Double>> subpathsList){
		subpathsY= subpathsList;
	}
	
	
	
	
	
}
