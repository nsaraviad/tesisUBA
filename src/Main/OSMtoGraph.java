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
import Polygons.PolygonAreaComparator;
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
 							solv.solve(gen.getPolygons(),gen.getPolygonsCount(), p,true,4);
							
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
				
				/* Lista intermedia, en la cual los polígonos de la solución se insertan ordenadamente 
				 * de acuerdo al tamaño de su área(orden ascendente)*/
				LinkedList<MapPolygon> orderedListByAreaSize= new LinkedList<MapPolygon>();
				
				//como paso previo se puede insertar ordenadamente los polígonos comparando tamaños de area (menor a mayor)
				int id_Pol;
								
				//Ordered polygons list by area size
				orderListByPolygonAreaSize(gen, solv, orderedListByAreaSize);
					
				//Una vez ordenada la lista, aplico el algoritmo greedy
				greedyAddingMapPolygon(orderedListByAreaSize, polygonsInSolution);
				
			}

			private void orderListByPolygonAreaSize(PolygonsGenerator gen,
					SystemSolver solv,
					LinkedList<MapPolygon> orderedListByAreaSize) {
				int id_Pol;
				for(int s=0;s < solv.getPolygonsInSolution().size();s++){
					id_Pol= solv.getPolygonsInSolution().get(s);
					MapPolygon pol= gen.getPolygonWithId(id_Pol);
				
					orderedInsertByAreaSize(pol,orderedListByAreaSize);
				}
			}
			
			private void orderedInsertByAreaSize(MapPolygon pol,LinkedList<MapPolygon> orderedListByAreaSize) {
				// Inserta ordenadamente de menor a mayor por tamaño de area
				PolygonAreaComparator comp = new PolygonAreaComparator();
				
				if(orderedListByAreaSize.isEmpty()){
					orderedListByAreaSize.add(pol);
				}else if(comp.compare(pol.getPolArea(),orderedListByAreaSize.getFirst().getPolArea()) == -1){
					//area de pol es menor al área del primero de la lista ordenada
					//agrego al comienzo
					orderedListByAreaSize.add(0, pol);
				}else if(comp.compare(pol.getPolArea(), orderedListByAreaSize.getLast().getPolArea()) == 1){
					//area de pol es mayor al area del ultimo elemento de la lista ordenada
					//agrego al final
					orderedListByAreaSize.add(orderedListByAreaSize.size(), pol);
				}else{
					int i= 0;
					//mientras al área de pol sea mayor al area del i-esimo poligono de la lista, itero
					while(comp.compare(pol.getPolArea(), orderedListByAreaSize.get(i).getPolArea()) == 1){
						i++;
					}
					orderedListByAreaSize.add(i, pol);
				}
				
			}

			//greedy algorithm (Solo se agregan los poligonos que no se solapan)
			private void greedyAddingMapPolygon(LinkedList<MapPolygon> orderedPolygonsList,LinkedList<MapPolygon> polygonsInSolution) {
				
				MapPolygon p_polygon;
				
				for(int p=0;p < orderedPolygonsList.size();p++){
					p_polygon= orderedPolygonsList.get(p);
					
					//chequeo si no se interseca con ningun polígono en el conjunto solución
					if (!overlapsWithOtherPolsInSolution(p_polygon,polygonsInSolution))
						polygonsInSolution.add(p_polygon);
				}
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