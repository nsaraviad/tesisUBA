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

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import Visualizer.CoordinatesConversor;
import GraphComponents.AdyacencyInfo;
import GraphComponents.DirectedEdge;
import GraphComponents.GraphNode;
import GraphComponents.Pair;
import GraphComponents.RoadGraph;

public class ParseOSM {
	LinkedList<GraphNode> nodes= new LinkedList<GraphNode>();
	LinkedList<DirectedEdge> edges= new LinkedList<DirectedEdge>();
	Area[] cityQuadrants= new Area[4];
	RoadGraph g = new RoadGraph();
	Area boundaryArea;
	double max_latit;
	double max_longit;
	double min_latit;
	double min_longit;
 
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
		
		//Divide City in quadrants (4)
		generateQuadrants();
		
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

	private void generateQuadrants() {
		//Metodo encargado de subdividir el cuadrante principal que encierra a la ciudad entera en 4 subcuadrantes y los guarda en un array
		
		//Tengo 4 puntos extremos (max_x,min_y),(max_x,max_y),(min_x,max_y),(min_x,min_y)
		LinkedList quadrantsPoints= new LinkedList<LinkedList<Pair>>();
		
		//Calculo puntos intermedios
		double mid_latit, mid_longit, latit, longit;
		double[] xPoints= new double[4];
		double[] yPoints= new double[4];
		
		mid_latit= (max_latit + min_latit)/2;
		mid_longit= (max_longit + min_longit)/2;
		
		//Armado de Puntos para formar cuadrantes
		prepareQuadrants(quadrantsPoints, mid_latit, mid_longit);
		
		//Conversión y calculo area de cada cuadrante
		for(int i=0;i < 4; i++){
			//Cuadrante actual
			LinkedList<Pair> temp_quad = (LinkedList<Pair>) quadrantsPoints.get(i);
			
			for(int j=0;j<temp_quad.size();j++){
				latit= (double) temp_quad.get(j).getFirst();
				longit= (double) temp_quad.get(j).getSecond();
				xPoints[j]= CoordinatesConversor.getTileNumberLat(latit);
				yPoints[j]= CoordinatesConversor.getTileNumberLong(longit);
			}
			
			
			//ARMADO DEL PERÍMETRO DEL CUADRANTE 
			Path2D path= new Path2D.Double();
				
			path.moveTo(xPoints[0], yPoints[0]);
			for(int k=1;k < 4;k++)
				path.lineTo(xPoints[k], yPoints[k]);
						
			//CALCULO AREA CUADRANTE
			path.closePath();
			final Area area= new Area(path);
			cityQuadrants[i]= area;
			
		}
		
	}

	private void prepareQuadrants(LinkedList quadrantsPoints,double mid_latit, double mid_longit) {
		
		LinkedList temp_quad= new LinkedList<Pair>();
		
		//Quad_1
		temp_quad.add(new Pair(min_latit,max_longit));
		temp_quad.add(new Pair(mid_latit,max_longit));
		temp_quad.add(new Pair(mid_latit,mid_longit));
		temp_quad.add(new Pair(min_latit,mid_longit));
		
		quadrantsPoints.add(temp_quad);
		temp_quad.clear();
		
		//Quad_2
		temp_quad.add(new Pair(mid_latit,max_longit));
		temp_quad.add(new Pair(max_latit,max_longit));
		temp_quad.add(new Pair(max_latit,mid_longit));
		temp_quad.add(new Pair(mid_latit,mid_longit));
				
		quadrantsPoints.add(temp_quad);
		temp_quad.clear();
		
		//Quad_3
		temp_quad.add(new Pair(min_latit,mid_longit));
		temp_quad.add(new Pair(mid_latit,mid_longit));
		temp_quad.add(new Pair(mid_latit,min_longit));
		temp_quad.add(new Pair(min_latit,min_longit));
				
		quadrantsPoints.add(temp_quad);
		temp_quad.clear();
		
		//Quad_4
		temp_quad.add(new Pair(mid_latit,mid_longit));
		temp_quad.add(new Pair(max_latit,mid_longit));
		temp_quad.add(new Pair(max_latit,min_longit));
		temp_quad.add(new Pair(mid_latit,min_longit));
				
		quadrantsPoints.add(temp_quad);
		
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
			//se agrega como visitado
			visitedNodes.add(entry.getKey());
			
			//Lista de adyacentes
			LinkedList<AdyacencyInfo> listValues= entry.getValue();
			
			
			for(int i=0;i < listValues.size();i++){
				adyItem= listValues.get(i);
				adyNode= g.getNodes().get(adyItem.getAdyId());
				
				if(!visitedNodes.contains(adyItem.getAdyId())){
					//Creo eje
					tempEdge = new DirectedEdge(actual, adyNode,
							adyItem.getLenght(),adyItem.getOneWay(), adyItem.getType(),
							adyItem.getName());
					
					edges.add(tempEdge);
					
					visitedNodes.add(adyItem.getAdyId());
				}
			}
	
		}		
		
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
		
	private boolean nodeIsIncludedInCity(GraphNode value) {
		double p1,p2;
		p1= CoordinatesConversor.getTileNumberLat(value.getLat());
		p2= CoordinatesConversor.getTileNumberLong(value.getLon());
		
		Point2D point= new Point2D.Double(p1,p2);
		
		return (this.getBoundaryArea().contains(point));
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
		latit_max= Double.MIN_VALUE;
		latit_min= Double.MAX_VALUE;
		longit_max= Double.MIN_VALUE;
		longit_min= Double.MAX_VALUE;
		
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
			
	
}
