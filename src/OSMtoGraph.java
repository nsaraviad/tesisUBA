	


 
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.LinkedList;

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
                        
                        //Parseo del osm al grafo
                        ParseOSM p = new ParseOSM();
                	    try {
							p.ParseOSM(pathArchivo,nombre);
							
							/* PRUEBA DE INCLUSION */
							//double p1,p2;
							//p1= CoordinatesConversor.getTileNumberLat(-31.6792205);
							//p2= CoordinatesConversor.getTileNumberLong(-63.8703698);
							
							//Point2D point= new Point2D.Double(p1,p2);
							
							//if(p.getBoundaryArea().contains(point))
							//	cont= true;
							
							/* VISUALIZACIÓN */
							
							//JUNG
							GraphVisualizer gv = new GraphVisualizer();
		                	gv.Visualize(p,nombre);
						
							//JMAP VIEWER
							//Visualize(p);
						
                	    } catch (IOException | XmlPullParserException e) {
						
							e.printStackTrace();
						}
                		  
                    }
                }

			private void Visualize(ParseOSM p) {
				LinkedList<GraphNode> nodesB = p.getRoadGraph().getNodesBoundary();
				LinkedList<Coordinate> lista= new LinkedList<Coordinate>();
				double latit,longit;
				//double max_lat,max_lon,min_lat,min_lon;
				
				/*
				//Obtener la maxima,mínima latitud/longitud de nodos frontera
				if(nodesB.size() > 0){
					//Inicializar las variables con el primer nodo de la lista
					max_lat= nodesB.get(0).getLat();
					max_lon= nodesB.get(0).getLon();
					min_lat= nodesB.get(0).getLat();
					min_lon= nodesB.get(0).getLon();
				
					for(int j=1; j < nodesB.size(); j++){
						latit= nodesB.get(j).getLat();
						longit= nodesB.get(j).getLon();
						
						if(latit > max_lat)
							max_lat= latit;
						if(longit > max_lon)
							max_lon= longit;
						
						if(latit < min_lat)
							min_lat= latit;
						
						if(longit < min_lon)
							min_lon= longit;
						
					}
				
				//Puntos que delimitan la zona 
				lista.add(new Coordinate(max_lat,min_lon));
				lista.add(new Coordinate(max_lat,max_lon));
				lista.add(new Coordinate(min_lat,max_lon));
				lista.add(new Coordinate(min_lat,min_lon));
				
				}
				*/
				
				for(int i=0; i < nodesB.size(); i++){
					latit= nodesB.get(i).getLat();
					longit= nodesB.get(i).getLon();
					lista.add(new Coordinate(latit,longit));
				}
				
				Viewer viewer = new Viewer(lista);
				
				if(lista.size()>0){
					viewer.mostrar();
				}else{
					System.out.println("No hay boundary");
				}
				
			}
        });  
    }

  }