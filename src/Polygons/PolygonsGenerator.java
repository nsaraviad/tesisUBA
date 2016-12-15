package Polygons;

import java.util.LinkedList;

public abstract class PolygonsGenerator {
	
	//interface
	abstract public void generatePolygons();
	abstract public int getPolygonsCount();
	abstract public LinkedList[] getPolygons();
	abstract public MapPolygon getPolygonWithId(int idPol);
}
