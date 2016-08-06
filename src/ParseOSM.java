import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.*;
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

public class ParseOSM {
	LinkedList<GraphNode> nodes= new LinkedList<GraphNode>();
	LinkedList edges;
	RoadGraph g = new RoadGraph();
	Area boundaryArea;
 
	public void ParseOSM (String pathToArchive, String nameArchives) throws FileNotFoundException, IOException, XmlPullParserException{
		
		System.out.println("Run started at"+ LocalDateTime.now() );
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		XmlPullParser xpp = factory.newPullParser();
		xpp.setInput ( new FileReader (pathToArchive));

		//Parseo y armado del road graph
		g.osmGraphParser(xpp, nameArchives);

		//Armado del polígono de la ciudad
		generateBoundaryArea();
		
		//Filtrado del mapa. Se quitan los nodos fuera de la zona adminstrativa del mapa
		filterGraph();
		
		long k= 2644320638L;
		edges = g.edges;
		
		System.out.println("Parsing ended at"+ LocalDateTime.now() );
		System.out.println("Edges = "+edges.size());
		System.out.println("Nodes = "+g.nodes.size());
		System.out.println("Nodes = "+g.adylst.size());
		
		System.out.println("refBound = "+g.getRefBoundary().size());
		
	}

	private void filterGraph() {
		filterOnlyNodesInCityPolygon();
		filterOnlyEdgesBetweenCityNodes();
	}

	private void filterOnlyEdgesBetweenCityNodes() {
		AdyacencyInfo ady;
		//FILTRO LOS EJES. DEJO AQUELLOS QUE CONECTAN NODOS PERTENECIENTES A LA CIUDAD 
		for(Iterator<Entry<Long,LinkedList<AdyacencyInfo>>> it= g.adylst.entrySet().iterator(); it.hasNext();)
		{
			Map.Entry<Long, LinkedList<AdyacencyInfo>> entry= it.next();
			
			//verifica si es un nodo de la ciudad
			if(g.nodes.containsKey(entry.getKey())){
				LinkedList<AdyacencyInfo> listValues= entry.getValue();
				for(int i=0;i < listValues.size();i++){
					ady= listValues.get(i);
					//Si el adyacente no es nodo de la ciudad lo quito de la lista de adyacentes
					if(!g.nodes.containsKey(ady.getAdyId()))
						listValues.remove(ady);
				}
			}else{
				it.remove();
			}
			
		}
		
	}

	private void filterOnlyNodesInCityPolygon() {
		//FILTRO NODOS QUE ESTEN DENTRO DE LA ZONA ADMINISTRATIVA DE LA CIUDAD
		for(Iterator<Map.Entry<Long,GraphNode>> it= g.nodes.entrySet().iterator(); it.hasNext();)
		{
			Map.Entry<Long, GraphNode> entry= it.next();
			GraphNode nodeValue= entry.getValue();
			
			if(!nodeIsIncludedInCity(nodeValue))
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

	public LinkedList getNodes() {	
		return nodes;
	}
	public void setNodes(LinkedList nodes) {
		this.nodes = nodes;
	}
	public LinkedList getEdges() {
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
	public void generateBoundaryArea() {
		//Inicializo variables
		LinkedList<GraphNode> nodesB= g.getNodesBoundary();
		int size= nodesB.size();
		double latit, longit;
		double[] xPoints= new double[size];
		double[] yPoints= new double[size];
		
		//Conversión (latitud,longitud) a puntos en el plano R2 (x,y)
		for(int i=0;i < size; i++){
			latit= nodesB.get(i).getLat();
			longit= nodesB.get(i).getLon();
			xPoints[i]= CoordinatesConversor.getTileNumberLat(latit);
			yPoints[i]= CoordinatesConversor.getTileNumberLong(longit);
		}
		
		//ARMADO DEL PERÍMETRO DE LA CIUDAD
		Path2D path= new Path2D.Double();
		
		path.moveTo(xPoints[0], yPoints[0]);
		for(int i=1;i < size;i++){
			path.lineTo(xPoints[i], yPoints[i]);
		}
		
		path.closePath();
		final Area area= new Area(path);
		boundaryArea= area;

	}

	public Area getBoundaryArea() {
		return boundaryArea;
	}
			
	
}
