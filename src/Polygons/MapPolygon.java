package Polygons;

import java.awt.geom.Area;
import java.util.LinkedList;

public class MapPolygon {
	private int id_polygon;
	private LinkedList<Long> points;
	private Area polygon_area;
	
	public MapPolygon(int id, LinkedList<Long> polygonPoints, Area area){
		id_polygon= id;
		points= polygonPoints;
		polygon_area= area;
	}
	
	public void setId(int id){
		id_polygon= id;
	}
	
	public int getPolygonId(){
		return id_polygon;
	}
	
	public LinkedList<Long> getPolygonPoints(){
		return points;
	}
	
	public Area getPolArea(){
		return polygon_area;
	}
}
