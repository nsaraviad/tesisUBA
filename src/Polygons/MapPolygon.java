package Polygons;

import java.awt.geom.Area;
import java.util.LinkedList;

import GraphComponents.Pair;

public class MapPolygon {
	private int id_polygon;
	private double[] xPoints;
	private double[] yPoints;
	private Area polygon_area;
	
	public MapPolygon(int id, Pair polygonPoints, Area area){
		id_polygon= id;
		xPoints= (double[]) polygonPoints.getFirst();
		yPoints= (double[]) polygonPoints.getSecond();
		polygon_area= area;
	}
	
	public void setId(int id){
		id_polygon= id;
	}
	
	public int getPolygonId(){
		return id_polygon;
	}
	
	public double[] getPolygonxPoints(){
		return xPoints;
	}
	
	public double[] getPolygonyPoints(){
		return yPoints;
	}
	
	public void setxPoints(double[] points){
		xPoints= points;
	}
	
	public void setyPoints(double[] points){
		yPoints= points;
	}
	
	public Area getPolArea(){
		return polygon_area;
	}
}
