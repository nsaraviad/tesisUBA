package Polygons;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.openstreetmap.gui.jmapviewer.Coordinate;

import GraphComponents.AdyacencyInfo;
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
	/*
	public Pair calculatePolygonEdgesAndLenght(LinkedList<Long> polygon, ParseOSM p) {
		//Cálculo del conjunto de ejes incluídos en el polígono y longitud de recorrido 
		AdyacencyInfo ady;
		GraphNode temp_node,ady_node;
		HashSet polygon_edges= new HashSet();
		HashSet visitedNodes= new HashSet();
		double polygonDistance= 0;
		Pair res;
		long node_id;
		boolean test;
		
		RoadGraph graph= p.getRoadGraph();
		
		//ARMADO DEL AREA DEL POLÍGONO poly y de su perimetro
		Area polygon_area= calculatePolygonArea(polygon, graph);
		
		//Itero sobre los nodos del polígono
		for(int j=0;j < polygon.size();j++){
			node_id= polygon.get(j);
			temp_node= graph.getNodes().get(node_id);
				
			//verifica si es un nodo del polígono
			if(nodeIsContainedInPolygon(temp_node,polygon_area)){
				//Lo agrego a los nodos ya analizados
				visitedNodes.add(temp_node);
				//obtengo adyacentes
				//LinkedList<AdyacencyInfo> adyacents= entry.getValue();
				LinkedList<AdyacencyInfo> adyacents= graph.getAdyLst().get(node_id);
				for(int i=0;i < adyacents.size();i++){
					ady= adyacents.get(i);
					//Si el adyacente  es nodo del poligono entonces cuento la distancia (el eje pertenece al poligono)
					//ya que une dos nodos del mismo.
					ady_node= graph.getNodes().get(ady.getAdyId());  
					
				    if((ady_node != null) &&  nodeIsContainedInPolygon(ady_node,polygon_area) && !visitedNodes.contains(ady_node)){
				    	//Incremento en la distancia de recorrido del polígono
				    	polygonDistance= polygonDistance + ady.getLenght();
				    	//Agregar el eje que los une al conjunto de ejes del polígono
				    	addEdgeToPolygonEdges(temp_node,ady_node,ady,polygon_edges);
				    }
				}
			}
			
		}
		
		return new Pair(polygonDistance,polygon_edges);
	}
	 */
	/*public void addEdgeToPolygonEdges(GraphNode temp_node,GraphNode ady_node, AdyacencyInfo ady, HashSet polygon_edges) {
		//Dado dos nodos pertenecientes al polígono, se crea un eje y se lo agrega al conjunto de 
		//ejes contenidos en el mismo
		DirectedEdge newEdge; 
		
		newEdge = new DirectedEdge(temp_node, ady_node,
				ady.getLenght(),ady.getOneWay(), ady.getType(),
				ady.getName());
		
		polygon_edges.add(newEdge);
	}
*/
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
		//Para detectar nodos del borde del polígono
		latit_right= latit + move;
		latit_left= latit - move;
		longit_up= longit + move;
		longit_down= longit - move;
		
		//Punto real
		latit2D= CoordinatesConversor.getTileNumberLat(latit);
		longit2D= CoordinatesConversor.getTileNumberLong(longit);
		
		//Direcciones desplazadas
		latit_right_2D= CoordinatesConversor.getTileNumberLat(latit_right);
		latit_left_2D= CoordinatesConversor.getTileNumberLat(latit_left);
		longit_up_2D= CoordinatesConversor.getTileNumberLong(longit_up);
		longit_down_2D= CoordinatesConversor.getTileNumberLong(longit_down);
		
		
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
			xPoints[i]= CoordinatesConversor.getTileNumberLat(latit);
			yPoints[i]= CoordinatesConversor.getTileNumberLong(longit);
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
			//i-esimo polígono
			poly= polygons.get(i).getPolygonPoints();
			//calculatePolygonEdgesAndLenght(poly,p);
			addMapPolygonToViewer(poly,p, mapPols);
			showPolygonsInMap(mapPols);
		}
		
		//showPolygonsInMap(mapPols);
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
