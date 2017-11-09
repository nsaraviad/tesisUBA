 
package Main;
 
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;

import javax.swing.*;
import javax.swing.filechooser.*;

import org.xmlpull.v1.XmlPullParserException;

import Auxiliar.ListOperator;
import Geom.AreaOperator;
import GraphComponents.Pair;
import Parsing.OsmParserAndCustomizer;
import Polygons.MapPolygon;
import Polygons.PolygonAreaComparator;
import Polygons.PolygonsGenerator;
import Polygons.PolygonsGeneratorFromFilteredEntries;
import Polygons.PolygonsOperator;
import SolutionProcessing.SolutionProcessor;
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
					ex.printStackTrace();
			}
        }
        else{
            log.append("Attachment cancelled by user." + newline);
            log.update(log.getGraphics());
        }
        
        //Reset the file chooser for the next time it's shown.
        fc.setSelectedFile(null);
    }
 	
    
    public void extractPolygonsInSolutionToList(PolygonsGenerator gen,	SystemSolver solv, LinkedList<MapPolygon> polygonsInSolution) {
		
		int id_Pol;
		//Ordered list by area size
		LinkedList<MapPolygon> orderedListByAreaSize= new LinkedList<MapPolygon>();
		
		SolutionProcessor proc= new SolutionProcessor();
		
		//Ordered polygons list by area size (mayor a menor)
		proc.orderListByPolygonAreaSize(gen, solv, orderedListByAreaSize);
		
		//Una vez ordenada la lista, aplico el algoritmo greedy
		proc.greedyAddingMapPolygon(orderedListByAreaSize, polygonsInSolution);
		
		//Processing solution polygons
		proc.processingAndMergeSmallPolygons(polygonsInSolution);
		
	}
    
    
		  
     private static void createAndShowGUI() {
	        //Create and set up the window.
	        JFrame frame = new JFrame("OSMz");
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