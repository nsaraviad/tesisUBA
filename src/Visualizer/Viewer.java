package Visualizer;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;
import org.openstreetmap.gui.jmapviewer.interfaces.MapPolygon;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.LinkedList;

public class Viewer
{
	// Datos privados
	private JFrame _frame;
	
	// Constructores
	public Viewer(LinkedList<LinkedList<Coordinate>> lista)
	{
		initialize(lista);
	}

	// Inicializa el mapa
	private void initialize(LinkedList<LinkedList<Coordinate>> lista)
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		_frame = new JFrame();
		_frame.setBounds(100, 100, 900, 600);
		_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				
		JPanel panelSuperior = new JPanel();
		_frame.getContentPane().add(panelSuperior, BorderLayout.NORTH);
		
		JPanel panelIzquierdo = new JPanel();
		_frame.getContentPane().add(panelIzquierdo, BorderLayout.WEST);
		
		JPanel panelInferior = new JPanel();
		_frame.getContentPane().add(panelInferior, BorderLayout.SOUTH);
		
		JPanel panelDerecho = new JPanel();
		_frame.getContentPane().add(panelDerecho, BorderLayout.EAST);
		
		JMapViewer mapViewer = new JMapViewer();
		_frame.getContentPane().add(mapViewer, BorderLayout.CENTER);
		
		Color[] colors= new Color[7];
		initializeColorsArray(colors);
		
		// Agregamos varios marcadores
		for(int l=0;l<lista.size();l++){
			//for(Coordinate c: lista.get(l))
				//mapViewer.addMapMarker(new MapMarkerDot(c.getLat(), c.getLon()));
				MapPolygonImpl new_mpol= new MapPolygonImpl(lista.get(l));
				
				//Colors components R,G,B
				int redComp, greenComp, blueComp;
				
				redComp= colors[(lista.get(l).size() % 7)].getRed();
				greenComp= colors[(lista.get(l).size() % 7)].getGreen();
				blueComp= colors[(lista.get(l).size() % 7)].getBlue();
				
				Color col= new Color(redComp,greenComp,blueComp,80);
				
				new_mpol.setColor(colors[(lista.get(l).size() % 7)]);
				new_mpol.setBackColor(col);
				new_mpol.setStroke(new BasicStroke(0));
				
				
				mapViewer.addMapPolygon(new_mpol);
			
		}
			
		// Centramos el mapa sobre los marcadores
		//mapViewer.setDisplayToFitMapMarkers();
		mapViewer.setDisplayToFitMapPolygons();
		mapViewer.setZoom(13);
	}
	
	private void initializeColorsArray(Color[] colors) {
		colors[0]= Color.RED;
		colors[1]= Color.BLUE;
		colors[2]= Color.CYAN;
		colors[3]= Color.GREEN;
		colors[4]= Color.YELLOW;
		colors[5]= Color.ORANGE;
		colors[6]= Color.MAGENTA;
	}

	// Muestra el frame
	public void mostrar()
	{
		_frame.setVisible(true);
	}
}
