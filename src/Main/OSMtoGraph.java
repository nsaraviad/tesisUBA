 
package Main;
 
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;

import javax.swing.*;
import javax.swing.filechooser.*;

import org.xmlpull.v1.XmlPullParserException;

import Parsing.OsmParserAndCustomizer;
import Polygons.MapPolygon;
import Polygons.PolygonAreaComparator;
import Polygons.PolygonsGenerator;
import Polygons.PolygonsGeneratorFromFilteredEntries;
import Polygons.PolygonsOperator;
import Solver.SystemSolver;
 

public class OSMtoGraph extends JPanel
                              implements ActionListener {
    static private String newline = "\n";
    private JTextArea log;
    private JFileChooser fc;
 
    public OSMtoGraph() {
        super(new BorderLayout());
        setPreferredSize(new Dimension(400,300));
        //Create the log first, because the action listener
        //needs to refer to it.
        log = new JTextArea(5,20);
        log.setMargin(new Insets(5,5,5,5));
        log.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(log);
 
        JButton sendButton = new JButton("Attach...");
        sendButton.addActionListener(this);
        
        add(sendButton, BorderLayout.PAGE_START);
        add(logScrollPane, BorderLayout.CENTER);
    }
 
    public void actionPerformed(ActionEvent e) {
        //Set up the file chooser.
        if (fc == null) {
            fc = new JFileChooser();
            
            //Filter only osm files
            FileNameExtensionFilter filter = new FileNameExtensionFilter("OSM maps", "osm");
            fc.setFileFilter(filter);
        }
        
        //Show it.
        int returnVal = fc.showDialog(OSMtoGraph.this,"Attach");	
 
        //Process the results.
        if (returnVal == JFileChooser.APPROVE_OPTION) {
        	 String filePath = fc.getSelectedFile().getPath(); //Obtiene path del archivo
             String fileName = fc.getSelectedFile().getName(); //obtiene nombre del archivo
             
             log.setText("");
             log.append("> " + fileName + " is attached." + newline);
        	 log.update(log.getGraphics());
             
             OsmParserAndCustomizer p = new OsmParserAndCustomizer();
     	     
             try {
            	 	DateTimeFormatter dtf= DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
            	 	//Parse osm to graph
	            	log.append("> Parsing started at " + dtf.format(LocalDateTime.now()) + newline);
	            	log.update(log.getGraphics());
	                p.ParseOSM(filePath,fileName);
	                log.append("> Parsing ended at " + dtf.format(LocalDateTime.now()) + newline);
	            	log.update(log.getGraphics());
	                 
     	    		//Polygons generator algorithm
	            	log.append("> Generating polygons ..." + newline);
	            	log.update(log.getGraphics());
	                PolygonsGenerator gen= new PolygonsGeneratorFromFilteredEntries(p);
					gen.generatePolygons();
										
					//Solving problem
					log.append("> Solving system ..." + newline);
	            	log.update(log.getGraphics());
	                SystemSolver solv= new SystemSolver();
					solv.solve(gen.getPolygons(),gen.getPolygonsCount(), p,true,5);
					log.append("> Problem solved at " + dtf.format(LocalDateTime.now()) + newline);
	            	log.update(log.getGraphics());
	                
	            	//Preparar lista de poligonos en solución
	            	log.append("> Processing solution ..." + newline);
	            	log.update(log.getGraphics());
	                LinkedList<MapPolygon> polygonsInSolution= new LinkedList<MapPolygon>();
					extractPolygonsInSolutionToList(gen, solv,polygonsInSolution);
					log.append("> Polygons in solution " + polygonsInSolution.size() + newline);
					
					//Visualize solution
					log.append("> Visualizing solution ..." + newline);
	            	log.update(log.getGraphics());
	                PolygonsOperator polOp= new PolygonsOperator();
					polOp.operateWithPolygons(p, polygonsInSolution);
					log.append("> Final " + dtf.format(LocalDateTime.now()) + newline);
					
     	    }catch (IOException | XmlPullParserException ex) {
					ex	.printStackTrace();
			}
             
        }else{
            log.append("Attachment cancelled by user." + newline);
            log.update(log.getGraphics());
        }
        
        //Reset the file chooser for the next time it's shown.
        fc.setSelectedFile(null);
    }
 
     
    private void extractPolygonsInSolutionToList(PolygonsGenerator gen,	SystemSolver solv, LinkedList<MapPolygon> polygonsInSolution) {
		
		int id_Pol;
		//Ordered list by area size
		LinkedList<MapPolygon> orderedListByAreaSize= new LinkedList<MapPolygon>();
		
		//Ordered polygons list by area size
		orderListByPolygonAreaSize(gen, solv, orderedListByAreaSize);
		
		//Una vez ordenada la lista, aplico el algoritmo greedy
		greedyAddingMapPolygon(orderedListByAreaSize, polygonsInSolution);
	}

	private void orderListByPolygonAreaSize(PolygonsGenerator gen,SystemSolver solv,LinkedList<MapPolygon> orderedListByAreaSize) {
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
			//mientras al área de pol sea mayor al area del i-esimo poligono de la lista, se itera
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
			
			/*Se compara el poligono actual contra el resto de la solución. Si se interseca con
			 * alguno se "recorta" dicha intersección. Finalmente se agrega a la solucion (si no es vacío). 
			 */
			compareWithSolutionModifyIfNecessaryAndAddToSolutionSet(p_polygon,polygonsInSolution);
		}
	}

	private void compareWithSolutionModifyIfNecessaryAndAddToSolutionSet(MapPolygon p_polygon,LinkedList<MapPolygon> polygonsInSolution) {
		
		Area initPolArea= new Area(p_polygon.getPolArea());
		
		for(int p=0;p < polygonsInSolution.size();p++){
			MapPolygon temp_pol= polygonsInSolution.get(p);
			checkOverlapsAndCutPolygonIfNecessary(p_polygon,temp_pol);
		}
		
		boolean polAreaIsModified= !(p_polygon.getPolArea().equals(initPolArea));

		//Area is modified
		if(polAreaIsModified){
			if(!p_polygon.getPolArea().isEmpty()){
				modifyPolygonsPoints(p_polygon); //el area ya se ha modificado
				polygonsInSolution.add(p_polygon);
			}
		}
		else
			{
				polygonsInSolution.add(p_polygon);
		}
	}

	private void modifyPolygonsPoints(MapPolygon p_polygon) {
		// Dada el area modificada del polígono, se recorre su "contorno" y se actualizan los puntos 
		double[] coords= new double[6];
		LinkedList<LinkedList<Double>> l1,l2;
		l1= new LinkedList<LinkedList<Double>>();
		l2= new LinkedList<LinkedList<Double>>();
		
		Area polygonArea= p_polygon.getPolArea();
		//itero sobre el borde del area del polígono
			
		LinkedList<Double> aux_lstx= new LinkedList<Double>();
		LinkedList<Double> aux_lsty= new LinkedList<Double>();
		
		for(PathIterator pi= polygonArea.getPathIterator(null);!pi.isDone();pi.next()){
			
			switch(pi.currentSegment(coords)){
			
				case PathIterator.SEG_MOVETO:
					//Auxiliar listfor a new subpath
					aux_lstx.clear();
					aux_lsty.clear();
					aux_lstx.add(coords[0]);
					aux_lsty.add(coords[1]);
					break;
													
				case PathIterator.SEG_LINETO:
					//add in same subpath
					aux_lstx.add(coords[0]);
					aux_lsty.add(coords[1]);
					break;
				
				case PathIterator.SEG_CLOSE:
					l1.add(aux_lstx);
					l2.add(aux_lsty);
					break;
			}
			
		}
		
		//ambas longitudes deben ser iguales
		assert (l1.size()==l2.size());
		
		//actualizo los subpaths de los polígonos
	    p_polygon.setSubpathsX(l1);
	    p_polygon.setSubpathsY(l2);
		
		//se actualizan los puntos del polígono
		if(l1.size() == 1 && !l1.getFirst().isEmpty()){
			LinkedList<Double> xp= new LinkedList<Double>();
			LinkedList<Double> yp= new LinkedList<Double>();
			
			xp= p_polygon.getSubpathsX().getFirst();
			yp= p_polygon.getSubpathsY().getFirst();
				
			//update xpoints & yPoints 
			p_polygon.setxPoints(xp);
			p_polygon.setyPoints(yp);
		}
	}

	private void checkOverlapsAndCutPolygonIfNecessary(MapPolygon thisPolygon,MapPolygon otherPolygon) {
		//Método encargado de chequear si hay interseccion entre ambos polígonos. En el caso de haber,
		//se "corta" del polígono el area que se interseca.
		Area thisPolArea, otherPolArea;
		
		if(intersect(thisPolygon,otherPolygon)){
			thisPolygon.getPolArea().subtract(otherPolygon.getPolArea());
		}
	}

	private boolean intersect(MapPolygon thisPolygon, MapPolygon otherPolygon) {
		//check intersection between map polygons
		Area polArea= new Area(thisPolygon.getPolArea());
		Area otherArea= new Area(otherPolygon.getPolArea());
		
		polArea.intersect(otherArea);
		
		return !polArea.isEmpty();
	}
	
	
	private void copyListInArray(LinkedList<Double> l1, double[] xp) {
		for(int i=0;i < l1.size();i++)
			xp[i]= l1.get(i);
	}
	  
     private static void createAndShowGUI() {
	        //Create and set up the window.
	        JFrame frame = new JFrame("ZOosm");
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        
	        //Add content to the window.
	        frame.add(new OSMtoGraph());
	        frame.setLocationRelativeTo(null);

	        //Display the window.
	        frame.pack();
	        frame.setVisible(true);
	 }
	 
	
    public static void main(String[] args) {
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //Turn off metal's use of bold fonts
                UIManager.put("swing.boldMetal", Boolean.FALSE);
                createAndShowGUI();
            }
        });
    }
	
	
}