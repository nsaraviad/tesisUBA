	


 
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.xmlpull.v1.XmlPullParserException;
 
public class OSMtoGraph extends JFrame {
    private JButton boton;
    
    public OSMtoGraph() {
        super("ParseOSMtoGraph	");
        boton = new JButton("Abrir");
        add(boton, BorderLayout.NORTH);
        boton.setForeground(Color.WHITE);
        boton.setBackground(Color.GRAY);
       
        boton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evento) {
                    boolean cont;
            		JFileChooser elegir = new JFileChooser();
                    
                    //Se filtran solamente archivos con extensión "osm"
                    FileNameExtensionFilter filter = new FileNameExtensionFilter(
                            "OSM maps", "osm");
                    
                    elegir.setFileFilter(filter);
                    
                    int opcion = elegir.showOpenDialog(boton);
               
                    //Si presionamos el boton ABRIR en pathArchivo obtenemos el path del archivo
                    if (opcion == JFileChooser.APPROVE_OPTION) {
                        String pathArchivo = elegir.getSelectedFile().getPath(); //Obtiene path del archivo
                        String nombre = elegir.getSelectedFile().getName(); //obtiene nombre del archivo
                        
                        ParseOSM p = new ParseOSM();
                	    try {
                	    	//Parseo del osm al grafo
                            p.ParseOSM(pathArchivo,nombre);
							
							//ALGORITMO GENERADOR DE POLÍGONOS
							PolygonsGenerator gen= new PolygonsGenerator(p);
							gen.generatePolygons();
							
							/* PRUEBA DE INCLUSION */
							//Point2D point= new Point2D.Double(p1,p2);
							
							//if(p.getBoundaryArea().contains(point))
							//	cont= true;
							
							/* VISUALIZACIÓN */
							
							//JUNG
							//GraphVisualizer gv = new GraphVisualizer();
		                	//gv.Visualize(p,nombre);
						
							//JMAP VIEWER
							operateWithPolygons(p,gen.getPolygons());
						
                	    } catch (IOException | XmlPullParserException e) {
							e.printStackTrace();
						}
                    }
                }

			private void operateWithPolygons(ParseOSM p, LinkedList<LinkedList<Long>> polygons) {
				LinkedList<Long> poly= new LinkedList<Long>();
				double polygon_lenght; //longitud en km a recorrer dado un polígono
			    
				//Calculo distancias recorridas y visualizacion de cada polígono
				//for(int i=0;i<polygons.size();i++){
				for(int i=0;i<1;i++){	
				//i-esimo polígono
					poly= polygons.get(i);
					visualizePolygon(poly,p);
					//polygon_lenght= calculatePolygonEdgesAndLenght(poly,p);	
				}
					
			}

			private void visualizePolygon(LinkedList<Long> poly,ParseOSM p) {
				// Visualización de polígonos usando JMap Viewer
				LinkedList<Coordinate> lista= new LinkedList<Coordinate>();
				
				//Iterate over the polygons collection
				setCoordinates(poly,p,lista);
				show(lista);
			}

			private Pair calculatePolygonEdgesAndLenght(LinkedList<Long> poly, ParseOSM p) {
				
				//Cálculo del conjunto de ejes incluídos en el polígono y longitud de recorrido 
				AdyacencyInfo ady;
				RoadGraph graph;
				GraphNode temp_node,ady_node;
				
				graph= p.getRoadGraph();
				
				//ARMADO DEL AREA DEL POLÍGONO poly
				Area polygon_area= calculatePolygonArea(poly, graph);
				
				//FILTRO LOS EJES. DEJO AQUELLOS QUE CONECTAN NODOS PERTENECIENTES AL POLIGONO 
				for(Iterator<Entry<Long,LinkedList<AdyacencyInfo>>> it= graph.getAdyLst().entrySet().iterator(); it.hasNext();)
				{
					Map.Entry<Long, LinkedList<AdyacencyInfo>> entry= it.next();
					
					//Nodo actual
					temp_node= graph.getNodes().get(entry.getKey());
					
					//verifica si es un nodo del polígono
					if(nodeIsContainedInPolygon(temp_node,polygon_area)){
					
						//obtengo adyacentes
						LinkedList<AdyacencyInfo> adyacents= entry.getValue();
						for(int i=0;i < adyacents.size();i++){
							ady= adyacents.get(i);
							//Si el adyacente  es nodo del poligono entonces cuento la distancia (el eje pertenece al poligono)
							//ya que une dos nodos del mismo.
							ady_node= graph.getNodes().get(ady.getAdyId());  
							
						    if(nodeIsContainedInPolygon(ady_node,polygon_area)){
						    	//lenght= lenght + (eje(entry,ady).distance);
						    	//Agregar el eje que los une al conjunto de ejes del polígono
						    }
						    		
						}
					}
				}
				
				return null;
			}

			private boolean nodeIsContainedInPolygon(GraphNode temp_node,Area polygon_area) {
				// Se chequea si dada la latitud y longitud del nodo, está contenida en el área del 
				//polígono
				double latit2D,longit2D,latit,longit;
				
				latit= temp_node.getLat();
				longit= temp_node.getLon();
				
				latit2D= CoordinatesConversor.getTileNumberLat(latit);
				longit2D= CoordinatesConversor.getTileNumberLat(longit);
				
				Point2D nodePoint= new Point2D.Double(latit2D,longit2D);
				
				
				return polygon_area.contains(nodePoint);
			}

			private Area calculatePolygonArea(LinkedList<Long> poly,RoadGraph graph) {
				int size= poly.size();
				
				double latit, longit;
				GraphNode temp_node;
				double[] xPoints= new double[size];
				double[] yPoints= new double[size];
				
				//Conversión (latitud,longitud) a puntos en el plano R2 (x,y)
				for(int i=0;i < size; i++){
					temp_node= graph.getNodes().get(i);
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
				final Area polygon_area= new Area(path);
				
				return polygon_area;
			}

			private void show(LinkedList<Coordinate> lista) {
				Viewer viewer = new Viewer(lista);
				viewer.mostrar();
			}

			private void setCoordinates(LinkedList<Long> poly, ParseOSM p,LinkedList<Coordinate> lista) {
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
        });  
    }

  }