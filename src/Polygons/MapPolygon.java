package Polygons;

import java.util.LinkedList;

public class MapPolygon {
	private int id_polygon;
	private LinkedList<Long> points;
	
	public MapPolygon(int id, LinkedList<Long> polygonPoints){
		id_polygon= id;
		points= polygonPoints;
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
}
