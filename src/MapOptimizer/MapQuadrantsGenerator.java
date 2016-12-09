package MapOptimizer;

import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.util.LinkedList;

import org.openstreetmap.gui.jmapviewer.Coordinate;

import Parsing.OsmParserAndCustomizer;
import Visualizer.CoordinatesConversor;
import Visualizer.Viewer;
import GraphComponents.Pair;

public class MapQuadrantsGenerator {
	
	private double max_latit,max_longit,min_latit,min_longit;
	//private Area[] cityQuadrants= new Area[4];
	private Area[] cityQuadrants;
	OsmParserAndCustomizer pars;
	

	public MapQuadrantsGenerator(OsmParserAndCustomizer p){
		max_latit= p.getMaxLatit();
		max_longit= p.getMaxLongit();
		min_latit= p.getMinLatit();
		min_longit= p.getMinLongit();
		pars= p;
	}
	
	public void generateQuadrants() {
	
		double maxLat_temp, minLat_temp, maxLong_temp, minLong_temp;
		double factorWidth, factorHeight;
		double cityWidth, cityHeight;
		int cellsWidthCount,cellsHeightCount;
		LinkedList<LinkedList<Pair>> quadrantsPoints= new LinkedList<LinkedList<Pair>>();
		double[] xPoints,yPoints;
	
		//Initialize
		maxLat_temp= max_latit;
		minLat_temp= min_latit;
		maxLong_temp= max_longit;
		minLong_temp= min_longit;
		
		//factors
		factorWidth= 1.5;
		factorHeight= 1.5;
	
		//calculate city height and widht
		cityWidth= getCityWidhtDistance();
		cityHeight= getCityHeightDistance();
		
		//calculo de bloques en alto y ancho
		cellsWidthCount= (int) Math.rint(cityWidth / factorWidth);
		cellsHeightCount= (int) Math.rint(cityHeight / factorHeight);
		
		//earth radius
		int R= 6378137;
		
		//Offsets
		double offLat= cityHeight*1000/cellsHeightCount;
		double offlong= cityWidth*1000/cellsWidthCount;
		
		//to radians
		double dLat= offLat/R;
		double dLong= offlong/(R*Math.cos(Math.PI*min_latit/180));
		
		
		//create map grid
		for(int i=0;i < cellsHeightCount;i++){
			for(int j=0;j < cellsWidthCount;j++){
				//Armado del Cuadrante actual
				LinkedList quad_temp= new LinkedList<Pair>();
				quad_temp.add(new Pair(maxLat_temp,minLong_temp));
				quad_temp.add(new Pair(maxLat_temp,minLong_temp + (dLong*180/Math.PI)));
				quad_temp.add(new Pair(maxLat_temp - (dLat*180/Math.PI), minLong_temp + (dLong*180/Math.PI)));
				quad_temp.add(new Pair(maxLat_temp - (dLat*180/Math.PI),minLong_temp));
				
				//se agrega el quad al resultado
				quadrantsPoints.add(quad_temp);
				
				//muevo a lo ancho
				minLong_temp= minLong_temp + (dLong*180/Math.PI);
			}
			
			//muevo a lo alto
			maxLat_temp= maxLat_temp - (dLat*180/Math.PI);
			//vuelvo a posicionarme al comienzo de las cols
			minLong_temp= min_longit;
		}
		
		
		xPoints= new double[quadrantsPoints.size()];
		yPoints= new double[quadrantsPoints.size()];
		
		//Conversión y calculo area de cada cuadrante
		calculateQuadrantsAreas(quadrantsPoints, xPoints, yPoints);
		
		setResultsInOsmParser();
		
}
	

	private double getCityHeightDistance() {
		// calculo de la distancia (en km) del alto total de la ciudad
		return pars.getRoadGraph().getDistance(min_latit,min_longit,max_latit,min_longit);
	}

	private double getCityWidhtDistance() {
		// calculo de la distancia (en km) del ancho total de la ciudad
		return pars.getRoadGraph().getDistance(min_latit,min_longit,min_latit,max_longit);
	}

	
	/*
	public void generateQuadrants() {
		//Metodo encargado de subdividir el cuadrante principal que encierra a la ciudad entera en 4 subcuadrantes y los guarda en un array
		
		//Tengo 4 puntos extremos (max_x,min_y),(max_x,max_y),(min_x,max_y),(min_x,min_y)
		LinkedList quadrantsPoints= new LinkedList<LinkedList<Pair>>();
		LinkedList<Pair> t_quad;
		Pair midpoint;
		
		//Calculo puntos intermedios
		double mid_latit, mid_longit, latit, longit;
		double[] xPoints;
		double[] yPoints;
		
		//geopunto medio
		midpoint= midPoint(min_latit,min_longit,max_latit,max_longit);
		
		//Creacion cuadrantes
		prepareQuadrants(quadrantsPoints, (double)midpoint.getFirst(), (double)midpoint.getSecond());
		
		xPoints= new double[quadrantsPoints.size()];
		yPoints= new double[quadrantsPoints.size()];
		
		//Conversión y calculo area de cada cuadrante
		calculateQuadrantsAreas(quadrantsPoints, xPoints, yPoints);
		
		setResultsInOsmParser();
	}
*/


private void setResultsInOsmParser() {
		//Guarda los resultados en el parser
		pars.cityQuadrants= new Area[cityQuadrants.length];
		
		for(int q=0;q < cityQuadrants.length; q++)
			pars.cityQuadrants[q]= cityQuadrants[q];
	}

private void calculateQuadrantsAreas(LinkedList quadrantsPoints,double[] xPoints, double[] yPoints) {
	LinkedList<Pair> t_quad;
	double latit;
	double longit;
	
	//quadrants count
	cityQuadrants = new Area[quadrantsPoints.size()];
	
	
	for(int j=0;j<quadrantsPoints.size();j++){
			t_quad = (LinkedList<Pair>) quadrantsPoints.get(j);
			LinkedList<Coordinate> lista= new LinkedList<Coordinate>();
			
			for(int i=0;i<t_quad.size();i++){
				latit=   (double) t_quad.get(i).getFirst();
				longit= (double) t_quad.get(i).getSecond();
				lista.add(new Coordinate(latit,longit));
				xPoints[i]= CoordinatesConversor.getTileNumberLat(latit);
				yPoints[i]= CoordinatesConversor.getTileNumberLong(longit);
			}
		
			//ARMADO DEL PERÍMETRO DEL CUADRANTE 
			Path2D path= new Path2D.Double();
				
			path.moveTo(xPoints[0], yPoints[0]);
			for(int k=1;k < t_quad.size();k++)
				path.lineTo(xPoints[k], yPoints[k]);
						
			//CALCULO AREA CUADRANTE
			path.closePath();
			final Area area= new Area(path);
			cityQuadrants[j]= area;
		}
}


private Pair midPoint(double lat1, double long1, double lat2, double long2) {
	
	double dLon= Math.toRadians(long2 - long1);
	
	//to radians
	lat1= Math.toRadians(lat1);
	lat2=Math.toRadians(lat2);
	long1=Math.toRadians(long1);
	
	double Bx= Math.cos(lat2)*Math.cos(dLon);
	double By= Math.cos(lat2)*Math.sin(dLon);
	
	double lat3= Math.atan2(Math.sin(lat1) + Math.sin(lat2), Math.sqrt((Math.cos(lat1)+Bx)*(Math.cos(lat1)+Bx) + By*By));
	double long3= long1 + Math.atan2(By, Math.cos(lat1)+Bx);
	
	return new Pair(Math.toDegrees(lat3),Math.toDegrees(long3));
}

private void prepareQuadrants(LinkedList quadrantsPoints,double mid_latit, double mid_longit) {
	
	//Quadrants creation
	LinkedList quad1,quad2,quad3,quad4;
	quad1= new LinkedList<Pair>();
	quad2= new LinkedList<Pair>();
	quad3= new LinkedList<Pair>();
	quad4= new LinkedList<Pair>();
	
	//Quad_1
	quad1.add(new Pair(max_latit,min_longit));
	quad1.add(new Pair(max_latit,mid_longit));
	quad1.add(new Pair(mid_latit,mid_longit));
	quad1.add(new Pair(mid_latit,min_longit));
	
	quadrantsPoints.add(quad1);
	
	//Quad_2
	quad2.add(new Pair(max_latit,mid_longit));
	quad2.add(new Pair(max_latit,max_longit));
	quad2.add(new Pair(mid_latit,max_longit));
	quad2.add(new Pair(mid_latit,mid_longit));
			
	quadrantsPoints.add(quad2);
	
	
	//Quad_3
	quad3.add(new Pair(mid_latit,min_longit));
	quad3.add(new Pair(mid_latit,mid_longit));
	quad3.add(new Pair(min_latit,mid_longit));
	quad3.add(new Pair(min_latit,min_longit));
			
	quadrantsPoints.add(quad3);
	
	//Quad_4
	quad4.add(new Pair(mid_latit,mid_longit));
	quad4.add(new Pair(mid_latit,max_longit));
	quad4.add(new Pair(min_latit,max_longit));
	quad4.add(new Pair(min_latit,mid_longit));
		
	quadrantsPoints.add(quad4);
	
}

public Area[] getCityQuadrantsArea(){
	return cityQuadrants;
}

}


