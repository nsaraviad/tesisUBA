import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.*;
import java.util.LinkedList;

import javax.swing.JFileChooser;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class ParseOSM {
	LinkedList nodes;
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
		
		nodes = g.nodes;
		edges = g.edges;
		System.out.println("Parsing ended at"+ LocalDateTime.now() );
		System.out.println("Edges = "+edges.size());
		System.out.println("Nodes = "+nodes.size());
		System.out.println("refBound = "+g.getRefBoundary().size());
		
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
