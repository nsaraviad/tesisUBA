package Main;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.apache.commons.collections15.Transformer;

import java.awt.Dimension; 
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.ScrollPane;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import javafx.scene.shape.*;


public class Main {
	
	public static void main(String[] args) throws FileNotFoundException, IOException, XmlPullParserException{
		
		OSMtoGraph osmFile = new OSMtoGraph();
	    osmFile.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    osmFile.setSize(250, 150);
	    osmFile.setLocationRelativeTo(null); //centra el frame
	    osmFile.setVisible(true); //pone visible en frame
	}
}
