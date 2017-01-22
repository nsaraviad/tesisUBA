package Polygons;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.LinkedList;

import org.openstreetmap.gui.jmapviewer.Coordinate;

import GraphComponents.DirectedEdge;
import GraphComponents.GraphNode;
import GraphComponents.Pair;
import GraphComponents.RoadGraph;
import Parsing.OsmParserAndCustomizer;
import Visualizer.CoordinatesConversor;
import Visualizer.Viewer;


//OPERATIONS IN POLYGONS CLASS

public class PolygonsOperator {

	public double checkIfEdgeIsInPolygon(DirectedEdge e, Area polygonArea,OsmParserAndCustomizer p){
		GraphNode extrNode_1,extrNode_2;
		double includedInPolygon= 0;
		
		//Extremos del eje e
		extrNode_1= e.from();
		extrNode_2= e.to();

		//Se verifica si los dos extremos de la arista se encuentran incluídos en el polígono
		//Si lo están entonces el eje está incluído en dicho polígono
		if(nodeIsContainedInPolygon(extrNode_1,polygonArea) && nodeIsContainedInPolygon(extrNode_2,polygonArea))
			includedInPolygon= 1;
		
		//1 si está incluído, 0 si no
		return includedInPolygon;
	}
	
	
	public boolean nodeIsContainedInPolygon(GraphNode temp_node,Area polygon_area) {
		// Se chequea si dada la latitud y longitud del nodo, está contenida en el área del 
		//polígono
		double latit2D,longit2D,latit,longit;
		double latit_right,latit_left,longit_up,longit_down;
		double latit_right_2D,latit_left_2D,longit_up_2D,longit_down_2D;
		double move= 0.0001; //factor de desplazamiento en cada dirección
		
		
		//Punto real
		latit= temp_node.getLat();
		longit= temp_node.getLon();
		
		//Verifico si me expando "un poco" en cada dirección el punto se encuentra contenido
		//Para detectar nodos del borde del polígono (detección de casos bordes)
		latit_right= latit + move;
		latit_left= latit - move;
		longit_up= longit + move;
		longit_down= longit - move;
		
		//Punto real
		latit2D= CoordinatesConversor.convertLatitudeToPoint(latit);
		longit2D= CoordinatesConversor.convertLongitudeToPoint(longit);
		
		//Direcciones desplazadas
		latit_right_2D= CoordinatesConversor.convertLatitudeToPoint(latit_right);
		latit_left_2D= CoordinatesConversor.convertLatitudeToPoint(latit_left);
		longit_up_2D= CoordinatesConversor.convertLongitudeToPoint(longit_up);
		longit_down_2D= CoordinatesConversor.convertLongitudeToPoint(longit_down);
		
		
		//Analizo las cuatro direcciones
		Point2D dir_right_up= new Point2D.Double(latit_right_2D,longit_up_2D);
		Point2D dir_right_down= new Point2D.Double(latit_right_2D,longit_down_2D);
		Point2D dir_left_up= new Point2D.Double(latit_left_2D,longit_up_2D);
		Point2D dir_left_down= new Point2D.Double(latit_left_2D,longit_down_2D);
		
		//El punto real
		Point2D nodePoint= new Point2D.Double(latit2D,longit2D);
		
		return (polygon_area.contains(nodePoint) || 
				polygon_area.contains(dir_right_up) || 
				polygon_area.contains(dir_right_down) || 
				polygon_area.contains(dir_left_up) || 
				polygon_area.contains(dir_left_down));
		
	}

	public Area calculatePolygonArea(LinkedList<Long> poly,RoadGraph graph) {
		
		int size= poly.size();
		double latit, longit;
		GraphNode temp_node;
		double[] xPoints= new double[size];
		double[] yPoints= new double[size];
		
		//Conversión (latitud,longitud) a puntos en el plano R2 (x,y)
		for(int i=0;i < size; i++){
			temp_node= graph.getNodes().get(poly.get(i));
			latit= temp_node.getLat();
			longit= temp_node.getLon();
			
			xPoints[i]= CoordinatesConversor.convertLatitudeToPoint(latit);
			yPoints[i]= CoordinatesConversor.convertLongitudeToPoint(longit);
		}
		
		//ARMADO DEL PERÍMETRO DEL POLÍGONO
		Path2D path= new Path2D.Double();
		
		path.moveTo(xPoints[0], yPoints[0]);
		
		for(int i=1;i < size;i++)
			path.lineTo(xPoints[i], yPoints[i]);
		
		path.closePath();
		Area polygon_area= new Area(path);
		
		return polygon_area;
	}

	
	public void operateWithPolygons(OsmParserAndCustomizer p, LinkedList<MapPolygon> polygons) {
		LinkedList<Long> poly= new LinkedList<Long>();
		LinkedList<LinkedList<Coordinate>> mapPols= new LinkedList<LinkedList<Coordinate>>();
		
		//Calculo distancias recorridas y visualizacion de cada polígono
		for(int i=0;i<polygons.size();i++){	
		//for(int i=0;i<5;i++){
			//i-esimo polígono
			poly= polygons.get(i).getPolygonPoints();
			//calculatePolygonEdgesAndLenght(poly,p);
			addMapPolygonToViewer(poly,p, mapPols);
			//showPolygonsInMap(mapPols);
		}
		
		showPolygonsInMap(mapPols);
	}

	private void addMapPolygonToViewer(LinkedList<Long> poly,OsmParserAndCustomizer p,
											LinkedList<LinkedList<Coordinate>> listPols) {
		//Visualización de polígonos usando JMap Viewer
		LinkedList<Coordinate> lista= new LinkedList<Coordinate>();
		
		//Iterate over the polygons collection
		setCoordinatesToList(poly,p,lista);
		listPols.add(lista);
	}
	
	private void setCoordinatesToList(LinkedList<Long> poly, OsmParserAndCustomizer p,LinkedList<Coordinate> lista) {
		//Método encargado de setear las coordenadas del polygono a la lista  
		HashMap<Long,GraphNode> nodes;
		long keyPoint;
		double latit,longit;
		
		nodes= p.getRoadGraph().getNodes();
		
		for(int j=0;j<poly.size();j++){
			keyPoint= poly.get(j);
			latit= nodes.get(keyPoint).getLat();
			longit= nodes.get(keyPoint).getLon();
			lista.add(new Coordinate(latit,longit));
		}
	}	
	
	//SHOW POLYGON IN MAP
	private void showPolygonsInMap(LinkedList<LinkedList<Coordinate>> lista) {
		Viewer viewer = new Viewer(lista);
		viewer.mostrar();
	}
	
}
