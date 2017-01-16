package Main;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
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

import Parsing.OsmParserAndCustomizer;
import Polygons.MapPolygon;
import Polygons.PolygonsGenerator;
import Polygons.PolygonsGeneratorFromFilteredEntries;
import Polygons.PolygonsOperator;
import Solver.SystemSolver;
import jscip.*;

public class OSMtoGraph extends JFrame {
    private JButton boton;
    
    public OSMtoGraph() {
        super("ParseOSMtoGraph");
        boton = new JButton("Abrir");
        add(boton, BorderLayout.NORTH);
        boton.setForeground(Color.WHITE);
        boton.setBackground(Color.GRAY);
       
        boton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evento) {
                    boolean cont;
            		JFileChooser elegir = new JFileChooser();
                    
                    //Filter only osm files
                    FileNameExtensionFilter filter = new FileNameExtensionFilter(
                            "OSM maps", "osm");
                    
                    elegir.setFileFilter(filter);
                    
                    int opcion = elegir.showOpenDialog(boton);
               
                    if (opcion == JFileChooser.APPROVE_OPTION) {
                        String pathArchivo = elegir.getSelectedFile().getPath(); //Obtiene path del archivo
                        String nombre = elegir.getSelectedFile().getName(); //obtiene nombre del archivo
                        
                        OsmParserAndCustomizer p = new OsmParserAndCustomizer();
                	    try {
                	    	//Parse osm to graph
                            p.ParseOSM(pathArchivo,nombre);
							
							//Polygons generator algorithm
							PolygonsGenerator gen= new PolygonsGeneratorFromFilteredEntries(p);
							gen.generatePolygons();
							
							SystemSolver solv= new SystemSolver();
 							solv.solve(gen.getPolygons(),gen.getPolygonsCount(), p,true,3);
							
 							System.out.println("Problem solved at "+ LocalDateTime.now() );
 							
							//Preparar lista de poligonos en solucion
							LinkedList<MapPolygon> polygonsInSolution= new LinkedList<MapPolygon>();
							extractPolygonsInSolutionToList(gen, solv,polygonsInSolution);
							
							//Visualize solution
							PolygonsOperator polOp= new PolygonsOperator();
							polOp.operateWithPolygons(p, polygonsInSolution);
						    
                	    } catch (IOException | XmlPullParserException e) {
							e.printStackTrace();
						}
                    }
                }

			private void extractPolygonsInSolutionToList(PolygonsGenerator gen,	SystemSolver solv, LinkedList<MapPolygon> polygonsInSolution) {

				int id_Pol;
				
				//Iterate over polygons in solution
				for(int s=0;s < solv.getPolygonsInSolution().size();s++){
					id_Pol= solv.getPolygonsInSolution().get(s);
					MapPolygon pol= gen.getPolygonWithId(id_Pol);
					greedyAddingMapPolygon(pol, polygonsInSolution);
					//polygonsInSolution.add(gen.getPolygonWithId(id_Pol));
				}
			}
			
			//greedy algorithm (Solo se agregan los poligonos que no se solapan)
			private void greedyAddingMapPolygon(MapPolygon polygon,LinkedList<MapPolygon> polygonsInSolution) {
				//chequeo si no se interseca con ningun polígono en el conjunto solución
				if (!overlapsWithOtherPolsInSolution(polygon,polygonsInSolution))
					polygonsInSolution.add(polygon);
			}

			private boolean overlapsWithOtherPolsInSolution(MapPolygon pol,LinkedList<MapPolygon> polygonsInSolution) {
			
				boolean overlaps= false;
				
				//Iterate over solution
				int p=0;
				
				while(p < polygonsInSolution.size() && !overlaps){
					MapPolygon temp_pol= polygonsInSolution.get(p);
					overlaps= intersect(pol, temp_pol);
					p++;
				}
					
				return overlaps;
			}

			private boolean intersect(MapPolygon pol, MapPolygon temp_pol) {
				//check intersection between map polygons
				Area polArea= new Area(pol.getPolArea());
				Area otherArea= new Area(temp_pol.getPolArea());
				
				polArea.intersect(otherArea);
				
				return !polArea.isEmpty();
			}
          });  
    }

  }