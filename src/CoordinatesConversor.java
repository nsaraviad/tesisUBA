
//Clase encargada de mapear coordenadas geográficas (latitud, longitud) a puntos en el plano 2D.
public class CoordinatesConversor {

			
		public static double getTileNumberLat(double latitude){
			double yTile = (1 - Math.log(Math.tan(Math.toRadians(latitude)) + 1 / Math.cos(Math.toRadians(latitude))) / Math.PI) / 2;//*(1<<10); 
			return yTile;
		}
		
		
		public static double getTileNumberLong(double longitude){
			double xTile =  ((longitude + 180)/360);// *(1<<10));
			return xTile;
		}
}		

