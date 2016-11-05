package Visualizer;
import GraphComponents.DirectedEdge;
import GraphComponents.GraphNode;
import Parsing.*;

import java.util.LinkedList;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import javax.swing.JFrame;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.*;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;


public class GraphVisualizer {
 
	public void Visualize(ParseOSM graph, String nameArchive){
		
		LinkedList<GraphNode> nodes;
		LinkedList<DirectedEdge> edges;
		nodes = graph.getNodes();
		edges = graph.getEdges();
		
		Graph<GraphNode, DirectedEdge> a = new SparseGraph<GraphNode,DirectedEdge>();
		
		//ARMADO DEL GRAFO A VISUALIZAR
		addVertexToVisualize(a, nodes);
	    addEdgesToVisualize(a, edges);
	
	    
	    //TRANSFORMERS DE VISUALIZACIÓN
		
		//Mapeo geocoordenadas (Latitud,Longitud) a puntos del plano (x,y).
		Transformer<GraphNode,Point2D> locationTransformer = new Transformer<GraphNode,Point2D>(){
	
				public Point2D transform(GraphNode vertex){
			
						double valueLat = vertex.getLat();
						double valueLong = vertex.getLon();
						
						double xValue = CoordinatesConversor.getTileNumberLong(valueLong);
						double yValue = CoordinatesConversor.getTileNumberLat(valueLat);
						
						return new Point2D.Double(xValue , yValue);
				}
		}; 
				
		//Coloreo de vértices
		Transformer<GraphNode, Paint> vertexPaint = new Transformer<GraphNode, Paint>() {

		       public Paint transform(GraphNode v) {   
		            return Color.LIGHT_GRAY;
		       }
		};

		//Coloreo de Ejes
		Transformer<DirectedEdge, Paint> edgePaint = new Transformer<DirectedEdge, Paint>() {

			  public Paint transform(DirectedEdge e) {
				    
				  	if(e.getType().equals("primary")){
				    	return Color.ORANGE;
				    }else if(e.getType().equals("secondary")){
				    	return Color.RED;
				    }else{
				    	return Color.WHITE;
				    }
			  }
		};
		
		//Visualización de nodos
		Transformer<GraphNode,Shape> vertexSize = new Transformer<GraphNode,Shape>(){
				
			public Shape transform(GraphNode i){
				Ellipse2D circle = new Ellipse2D.Double(-0.5,-0.5,1,1);
				return circle;
			}
		};
				
		Transformer<DirectedEdge,String> showRoadName = new Transformer<DirectedEdge,String>(){
	    	
			public String transform(DirectedEdge e){
	    		return e.getName();
	    	}
	    };
	    
		//VISUALIZACIÓN
	    Layout<GraphNode,DirectedEdge> map = new StaticLayout<GraphNode,DirectedEdge>(a,locationTransformer);
		 
		map.setSize(new Dimension(800,800));
		 
		VisualizationViewer<GraphNode,DirectedEdge> vv = new VisualizationViewer<GraphNode,DirectedEdge>(map);
		
		vv.setPreferredSize(new Dimension(850,850));
		
		vv.getRenderingHints().remove(RenderingHints.KEY_ANTIALIASING); 
	    	
		/* SE APLICAN LAS TRANSFORMACIONES */		    
		
		//Coloreo nodos	   
	    vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
		
		//Visualización de los vértices   
	    vv.getRenderContext().setVertexShapeTransformer(vertexSize);
	    
	    //Coloreo ejes
	    vv.getRenderContext().setEdgeDrawPaintTransformer(edgePaint);
	    
	    //Coloreo de flechas de ejes(sentido calles)
	    vv.getRenderContext().setArrowFillPaintTransformer(new ConstantTransformer(Color.WHITE));
	    	    
	    //Visualizaciòn de Ejes
	    //vv.getRenderContext().setEdgeLabelTransformer(showRoadName);
	    
	    vv.setEdgeToolTipTransformer(showRoadName);
	    	    
	    //Ejes rectos
	    vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line<GraphNode, DirectedEdge>());
	    
	    vv.setBackground(Color.BLACK);
	    
	    //Detección eventos mouse en el panel
        DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
        gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
        vv.setGraphMouse(gm); 
    
        JFrame frame = new JFrame("Grafo Vista de " + nameArchive);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(vv);
        frame.setLocationRelativeTo(null);
        frame.pack();
        frame.setVisible(true);
	
	}
	
	
	//Método que agrega ejes al grafo a visualizar
	private static void addEdgesToVisualize(Graph<GraphNode, DirectedEdge> a, LinkedList<DirectedEdge> edges) {
	
		for(DirectedEdge e : edges){
			//solo se agregan los ejes correspondientes a caminos transitables "highway"
			if(e.getType() != null){
				
				DirectedEdge ei = new DirectedEdge(e.to(),e.from(),e.getLength(),
						e.isOneway(),e.getType(),e.getName());//,e.getWayId());
				
				//Solo se agregan ejes con extremos no nulos
				if(ei.from() != null && ei.to() != null){
					
					if(e.isOneway()){
						a.addEdge(ei, ei.from(),ei.to(),EdgeType.DIRECTED);	
					}
					else
					{
						a.addEdge(ei, ei.from(),ei.to(),EdgeType.UNDIRECTED);					
					}
				}
			}
		}
	}

	//Método que agrega nodos al grafo
	private static void addVertexToVisualize(Graph<GraphNode, DirectedEdge> a, LinkedList<GraphNode> nodes) {
	
  		for(GraphNode n : nodes){
  				a.addVertex(n);
  		}
	}
		
}
	
	
