package Parsing;

import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.awt.List;

import javax.swing.JFileChooser;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import Visualizer.CoordinatesConversor;
import Visualizer.Viewer;
import GraphComponents.AdyacencyInfo;
import GraphComponents.DirectedEdge;
import GraphComponents.GraphNode;
import GraphComponents.Pair;
import GraphComponents.RoadGraph;
import MapOptimizer.MapQuadrantsGenerator;
import Polygons.PolygonsOperator;


public class OsmParserAndCustomizer {
	private LinkedList<GraphNode> nodes= new LinkedList<GraphNode>();
	private LinkedList<DirectedEdge> edges= new LinkedList<DirectedEdge>();
	public Area[] cityQuadrants= new Area[4];
	private RoadGraph g = new RoadGraph();
	private Area boundaryArea;
	private double max_latit;
	private double max_longit;
	private double min_latit;
	private double min_longit;
	
 
	public void ParseOSM (String pathToArchive, String nameArchives) throws FileNotFoundException, IOException, XmlPullParserException{
		
		System.out.println("Run started at"+ LocalDateTime.now() );
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		XmlPullParser xpp = factory.newPullParser();
		xpp.setInput ( new FileReader (pathToArchive));

		//Parseo y armado del road graph
		g.osmGraphParser(xpp, nameArchives);

		//Armado del polígono de la ciudad
		generateBoundaryAreaAndSetCityLimits();
		
		//Divide City in four quadrants 
		//generateQuadrants();
		MapQuadrantsGenerator mg= new MapQuadrantsGenerator(this);
		mg.generateQuadrants();
		
		//Filtrado del mapa. Se quitan los nodos fuera de la zona adminstrativa del mapa
		filterGraph();
		
		//Se crean los nodos a visualizar
		//generateNodes();
		
		//Se crean los ejes
		generateEdges();
		
		System.out.println("Parsing ended at"+ LocalDateTime.now() );
		System.out.println("Edges = "+edges.size());
		System.out.println("Nodes = "+g.getNodes().size());
		System.out.println("AdyList = "+g.getAdyLst().size());
		
		System.out.println("refBound = "+g.getRefBoundary().size());
		
	}

	private void generateNodes() {
		for(Iterator<Entry<Long,GraphNode>> it= g.getNodes().entrySet().iterator(); it.hasNext();){
			
			Map.Entry<Long,GraphNode> entry= it.next();
			nodes.add(g.getNodes().get(entry.getKey()));
		}
			
	}

	//Método para generar ejes del grafo
	private void generateEdges() {
		GraphNode actual, adyNode;
		HashSet visitedNodes= new HashSet();
		AdyacencyInfo adyItem;
		DirectedEdge tempEdge;
		
		
		for(Iterator<Entry<Long,LinkedList<AdyacencyInfo>>> it= g.getAdyLst().entrySet().iterator(); it.hasNext();)
		{
			Map.Entry<Long, LinkedList<AdyacencyInfo>> entry= it.next();
			
			//Nodo actual
			actual= g.getNodes().get(entry.getKey());
			
			if(nodeIsIncludedInCity(actual)){
				//se agrega como visitado
				visitedNodes.add(entry.getKey());
				
				//Lista de adyacentes
				LinkedList<AdyacencyInfo> listValues= entry.getValue();
				
				for(int i=0;i < listValues.size();i++){
					//nodo adyacente "actual"
					adyItem= listValues.get(i);
					adyNode= g.getNodes().get(adyItem.getAdyId());
					
					if(!visitedNodes.contains(adyItem.getAdyId()) && adyNode != null && nodeIsIncludedInCity(adyNode)){
						
						//Se obtiene el cuadrante donde se encuentra el nuevo eje (cuadrante del primer extremo)
						int edgeQuads= getEdgeQuadrant(actual,adyNode);
						
						//Se crea el eje y se lo agrega a la colección de ejes de la ciudad
						tempEdge = new DirectedEdge(actual, adyNode,
								adyItem.getLenght(),adyItem.getOneWay(), adyItem.getType(),
								adyItem.getName(),edgeQuads);
						
						edges.add(tempEdge);
					}
				}
			}
		}	
		
	}
	
	
	private int getEdgeQuadrant(GraphNode fromNode, GraphNode toNode) {
	
	/*Analiza el cuadrante al que pertenece el primer extremo del nuevo eje y lo setea como cuadrante del mismo */
		int fromNodeQuadrant;
		
		fromNodeQuadrant= getNodeQuadrant(fromNode);
		return fromNodeQuadrant;
	}

	public int getNodeQuadrant(GraphNode fromNode) {
		// obtiene el cuadrante al que pertenece el nodo
		Area quadrant;
		int id_quad= -1;
		
		for(int i=0;(id_quad == -1) && i < cityQuadrants.length;i++){
			quadrant= cityQuadrants[i];
			
			if(nodeIsIncludedInQuadrant(fromNode,quadrant))
				id_quad= i; //el íd del cuadrante al que pertenece
		}
		
		return id_quad;
	}

	private boolean nodeIsIncludedInQuadrant(GraphNode fromNode, Area quadrant) {
		return areaContainsNode(fromNode,quadrant);
	}

	private void filterGraph() {
		filterOnlyNodesInCityPolygon();
		filterOnlyCityAdyNodes();
	}

	private void filterOnlyCityAdyNodes() {
		AdyacencyInfo ady;
		//FILTRO EN LA LISTA DE ADYACENCIAS DE CADA NODO LOS ADYACENTES PERTENECIENTES A LA CIUDAD 
		for(Iterator<Entry<Long,LinkedList<AdyacencyInfo>>> it= g.getAdyLst().entrySet().iterator(); it.hasNext();)
		{
			Map.Entry<Long, LinkedList<AdyacencyInfo>> entry= it.next();
			
			//verifica si es un nodo de la ciudad
			if(g.getNodes().containsKey(entry.getKey())){
				LinkedList<AdyacencyInfo> listValues= entry.getValue();
				for(int i=0;i < listValues.size();i++){
					ady= listValues.get(i);
					
					//Si el adyacente no es nodo de la ciudad lo quito de la lista de adyacentes
					if(!g.getNodes().containsKey(ady.getAdyId()))
						entry.getValue().remove(ady);
				}
			}else{
				it.remove();
			}
			
		}
		
	}

	private void filterOnlyNodesInCityPolygon() {
		//FILTRO NODOS QUE ESTEN DENTRO DE LA ZONA ADMINISTRATIVA DE LA CIUDAD
		for(Iterator<Map.Entry<Long,GraphNode>> it= g.getNodes().entrySet().iterator(); it.hasNext();)
		{
			Map.Entry<Long, GraphNode> entry= it.next();
			GraphNode nodeValue= entry.getValue();
			
			//Solo quedan los nodos que están dentro del polygono de la ciudad y en la frontera.
			if(!nodeIsIncludedInCity(nodeValue) && !g.getNodesBoundary().contains(nodeValue))
					it.remove();
		}
	}
		
	public boolean nodeIsIncludedInCity(GraphNode value) {
		return areaContainsNode(value,this.getBoundaryArea());
	}

	public LinkedList<GraphNode> getNodes() {	
		return nodes;
	}
	public void setNodes(LinkedList nodes) {
		this.nodes = nodes;
	}
	public LinkedList<DirectedEdge> getf() {
		return edges;
	}
	public void setEdges(LinkedList edges) {
		this.edges = edges;
	}
	public RoadGraph getRoadGraph() {
		return g;
	}
	public void setG(RoadGraph g) {
		this.g = g;
	}

	//Método que genera el área del polígono de la ciudad.
	public void generateBoundaryAreaAndSetCityLimits() {
		//Inicializo variables
		LinkedList<GraphNode> nodesB= g.getNodesBoundary();
		int size= nodesB.size();
		double latit, longit;
		double[] xPoints= new double[size];
		double[] yPoints= new double[size];
		double latit_max,latit_min,longit_max,longit_min; //cuadrante que encierra a toda la ciudad
		double conv_latit,conv_longit;

		//Inicializo
		latit_max= nodesB.get(0).getLat();
		latit_min= nodesB.get(0).getLat();
		longit_max= nodesB.get(0).getLon();
		longit_min= nodesB.get(0).getLon();
		
		//Conversión (latitud,longitud) a puntos en el plano R2 (x,y)
		for(int i=0;i < size; i++){
			latit= nodesB.get(i).getLat();
			longit= nodesB.get(i).getLon();
			conv_latit= CoordinatesConversor.getTileNumberLat(latit);
			conv_longit= CoordinatesConversor.getTileNumberLong(longit);
			
			//ACTUALIZO MAX Y MIN x y
			if(latit < latit_min)
				latit_min= latit;
			if(latit > latit_max)
				latit_max= latit;
			if(longit < longit_min)
				longit_min= longit;
			if(longit > longit_max)
				longit_max= longit;

			xPoints[i]= conv_latit;
			yPoints[i]= conv_longit;
		}
		
		
		//ARMADO DEL PERÍMETRO DE LA CIUDAD
		Path2D path= new Path2D.Double();
		
		path.moveTo(xPoints[0], yPoints[0]);
		for(int i=1;i < size;i++){
			path.lineTo(xPoints[i], yPoints[i]);
		}
		
		//CALCULO AREA
		path.closePath();
		final Area area= new Area(path);
		boundaryArea= area;
		
		//set limits 
		max_latit= latit_max;
		max_longit= longit_max;
		min_latit= latit_min;
		min_longit= longit_min;

	}

	public Area getBoundaryArea() {
		return boundaryArea;
	}

	public LinkedList<DirectedEdge> getEdges() {
		
		return edges;
	}
			
	private boolean areaContainsNode(GraphNode value, Area area) {
		
		PolygonsOperator pg= new PolygonsOperator();
		
		return pg.nodeIsContainedInPolygon(value,area);
	}
	
	public double getMaxLatit(){
		return max_latit;
	}
	
	public double getMinLatit(){
		return min_latit;
	}
	
	public double getMaxLongit(){
		return max_longit;
	}
	
	public double getMinLongit(){
		return min_longit;
	}
}
