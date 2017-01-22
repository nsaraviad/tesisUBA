package Visualizer;

import GraphComponents.Pair;

//Mapeo coordenadas geográficas (latitud, longitud) a puntos en el plano 2D.
public class CoordinatesConversor {
		
		private static double radius= 6378137;  //earth radius in meters
	
	
		public static double convertLatitudeToPoint(double latitude){
			//double yTile = (1 - Math.log(Math.tan(Math.toRadians(latitude)) + 1 / Math.cos(Math.toRadians(latitude))) / Math.PI) / 2; 
			//return yTile;
		
			return Math.log(Math.tan(Math.PI/4 + Math.toRadians(latitude)/2))*radius;
		}
		
		
		public static double convertLongitudeToPoint(double longitude){
			//double xTile =  ((longitude + 180)/360);
			//return xTile;
			return Math.toRadians(longitude)*radius;
		}

		
		public static double convertPointToLatitud(double yPoint) {
			//Conversión de punto en el plano (eje y) a coordenada geoespacial (latitud).
			return Math.toDegrees(Math.atan(Math.exp(yPoint/radius))*2 - Math.PI/2);
		}
		
		public static double convertPointToLongitude(double xPoint) {
			//Conversión de punto en el plano (eje x) a coordenada geoespacial (longitud).
			return Math.toDegrees(xPoint/radius);
		}
		
}		

