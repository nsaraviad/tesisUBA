package SolutionProcessing;

import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import Auxiliar.ListOperator;
import Geom.AreaOperator;
import GraphComponents.Pair;
import Polygons.MapPolygon;
import Polygons.PolygonAreaComparator;
import Polygons.PolygonsGenerator;
import Solver.SystemSolver;

public class SolutionProcessor {
	
	
	public void processingAndMergeSmallPolygons(LinkedList<MapPolygon> polygonsInSolution) {
		AreaOperator areaOp= new AreaOperator();
		double avg= areaOp.calculateAreaSizeAverageFor(polygonsInSolution);
		
		MapPolygon pol, minAreaSizeNeighbor;
		HashSet<MapPolygon> cantExpand= new HashSet<MapPolygon>();
		
		
		while(!acceptableSizeOfAllPols(polygonsInSolution,avg) && canExpandPolygons(polygonsInSolution, cantExpand)){
			checkAndMergePolygons(polygonsInSolution, areaOp, avg, cantExpand);
		}
	}
	
	private boolean canExpandPolygons(LinkedList<MapPolygon> polygonsInSolution,HashSet<MapPolygon> cantExpand) {
		return cantExpand.size() < polygonsInSolution.size();
	}
	
	
	private void checkAndMergePolygons(LinkedList<MapPolygon> polygonsInSolution, AreaOperator areaOp,
			double avg, HashSet<MapPolygon> cantExpand) {
			MapPolygon pol,minAreaSizeNeighbor;
			
			for(Iterator<MapPolygon> it= polygonsInSolution.iterator();it.hasNext();){
				pol= it.next();
			
				if(!cantExpand.contains(pol)){
						double areasize= areaOp.getAreaSize(pol);
			
						if(areasize < (avg*0.5)){
							minAreaSizeNeighbor= searchMinAreaSizeNeighbor(pol,polygonsInSolution,avg,cantExpand);
			
							if(minAreaSizeNeighbor != null){
			
								//neighbor found
								mergePolygonsAreasAndUpdatePoints(minAreaSizeNeighbor,pol);
								it.remove(); //removes polygon pol
							}
							else{ 
									//neighbor not found
									cantExpand.add(pol);
								}
			
						}else{
								cantExpand.add(pol);
						  	 }
				}
			}
	}
	
	private void mergePolygonsAreasAndUpdatePoints(MapPolygon minAreaSizeNeighbor,	MapPolygon pol) {
		// Merge pol area with neighbor area
		minAreaSizeNeighbor.getPolArea().add(pol.getPolArea());
		modifyPolygonsPoints(minAreaSizeNeighbor);
	}

	private MapPolygon searchMinAreaSizeNeighbor(MapPolygon pol,LinkedList<MapPolygon> polygonsInSolution,double avg,HashSet cantExpand){
		// Search the minimum area size neighbor polygon for pol
		AreaOperator op= new AreaOperator();
		MapPolygon neighbor,temp_pol;
		double minArea, temp_area;
		LinkedList<MapPolygon> neighbors= new LinkedList<MapPolygon>();
		
		//search neighbors polygons set
		findingNeighborsPols(pol, polygonsInSolution, neighbors);
		
		//Initialize
		neighbor= null;
		minArea= Double.MAX_VALUE;
		
		//iterates over the solution
		for(int p=0;p<neighbors.size();p++){
			temp_pol= neighbors.get(p);
			temp_area= op.getAreaSize(temp_pol);
			if((pol.getPolygonId()!= temp_pol.getPolygonId()) && 
						(temp_area < minArea) && (!cantExpand.contains(temp_pol))){
				//updates neighbor
				neighbor= temp_pol;
				minArea= temp_area;
			}
		}
			return neighbor;
	}

	private void findingNeighborsPols(MapPolygon pol,
			LinkedList<MapPolygon> polygonsInSolution,
			LinkedList<MapPolygon> neighbors) {
		MapPolygon temp_pol;
		for(int p=0;p<polygonsInSolution.size();p++){
			temp_pol= polygonsInSolution.get(p);
			if(areNeighborsPolygons(pol,temp_pol))
				neighbors.add(temp_pol);
		}
	}
	
	private boolean areNeighborsPolygons(MapPolygon thisPol, MapPolygon otherPol) {
		// checks if both polygons are neighbors
		
		LinkedList<Double> thisXPoints,thisYPoints,otherXPoints,otherYPoints;
		
		thisXPoints= thisPol.getPolygonxPoints();
		thisYPoints= thisPol.getPolygonyPoints();
		otherXPoints= otherPol.getPolygonxPoints();
		otherYPoints= otherPol.getPolygonyPoints();
		
		ListOperator op= new ListOperator();
		
		return op.containsIntersects(thisXPoints,otherXPoints) && 
				op.containsIntersects(thisYPoints,otherYPoints);
	}

	
	private boolean acceptableSizeOfAllPols(LinkedList<MapPolygon> polygonsInSolution,double avgAreaSize) {
		//Verify that all polygons have acceptable area size (>= average area size)
		boolean accept= true;
		
		for(int p=0;(p<polygonsInSolution.size() && accept);p++)
			accept= accept & checkIfPolAreaSizeAreInRangeWithAverage(polygonsInSolution.get(p),avgAreaSize);
		
		return accept;
	}
	
	private boolean checkIfPolAreaSizeAreInRangeWithAverage(MapPolygon mapPolygon, double avgAreaSize) {
		// Compare the pol area size with area size average
		AreaOperator op= new AreaOperator();
		double polAreaSize= op.getAreaSize(mapPolygon);
		
		return ((avgAreaSize*0.7) <= polAreaSize);
	}
	
	public void orderListByPolygonAreaSize(PolygonsGenerator gen,SystemSolver solv,LinkedList<MapPolygon> orderedListByAreaSize) {
		int id_Pol;
		for(int s=0;s < solv.getPolygonsInSolution().size();s++){
			id_Pol= solv.getPolygonsInSolution().get(s);
			MapPolygon pol= gen.getPolygonWithId(id_Pol);
			
			orderedInsertByAreaSize(pol,orderedListByAreaSize);
		}
	}

	private void orderedInsertByAreaSize(MapPolygon pol,LinkedList<MapPolygon> orderedListByAreaSize) {
		//Inserta ordenadamente de menor a mayor por tamaño de area
		PolygonAreaComparator comp = new PolygonAreaComparator();
		
		if(orderedListByAreaSize.isEmpty()){
				orderedListByAreaSize.add(pol);
		}else if(comp.compare(pol.getPolArea(),orderedListByAreaSize.getFirst().getPolArea()) == 1){
				//area de pol es mayor al área del primero de la lista ordenada
				//agrego al comienzo
				orderedListByAreaSize.add(0, pol);
		}else if(comp.compare(pol.getPolArea(), orderedListByAreaSize.getLast().getPolArea()) == -11){
				//area de pol es menor al area del ultimo elemento de la lista ordenada
				//agrego al final
				orderedListByAreaSize.add(orderedListByAreaSize.size(), pol);
		}else{
				int i= 0;
				//mientras al área de pol sea menor al area del i-esimo poligono de la lista, se itera
				while(comp.compare(pol.getPolArea(), orderedListByAreaSize.get(i).getPolArea()) == -1){
					i++;
				}
				orderedListByAreaSize.add(i, pol);
		}
		
	}
	
	//greedy algorithm (Solo se agregan los poligonos que no se solapan)
	public void greedyAddingMapPolygon(LinkedList<MapPolygon> orderedPolygonsList,LinkedList<MapPolygon> polygonsInSolution) {
			
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
				processingPolygonAndAddToSol(p_polygon,polygonsInSolution);
			}
		}
		else
		{
				polygonsInSolution.add(p_polygon);
		}
	}

	private void processingPolygonAndAddToSol(MapPolygon p_polygon,LinkedList<MapPolygon> polygonsInSolution) {

		//El polígono sufrió modificaciones -> Los subpaths contienen la información del/los contorno/s de su área.
		if(p_polygon.getSubpathsX()!=null && !p_polygon.getSubpathsX().getFirst().isEmpty()){
			LinkedList<LinkedList<Double>> xSubpath= p_polygon.getSubpathsX();
			LinkedList<LinkedList<Double>> ySubpath= p_polygon.getSubpathsY();
						
			assert (xSubpath.size() == ySubpath.size());
				
			for(int k=0;k<xSubpath.size();k++){
				LinkedList<Double> temp_subpathX= xSubpath.get(k);
				LinkedList<Double> temp_subpathY= ySubpath.get(k);
				
				Area polArea= new AreaOperator().calculateArea(temp_subpathX, temp_subpathY);
				Pair polPoints= new Pair(temp_subpathX,temp_subpathY);
				MapPolygon newPol= new MapPolygon(p_polygon.getPolygonId()*2 + k,polPoints,polArea);
				
				polygonsInSolution.add(newPol);
			}
		}
		else //No hay subpaths(el área del poligono nunca se dividió)
		{
			//i-esimo polígono
			LinkedList<Double> xpoly= p_polygon.getPolygonxPoints();	
			LinkedList<Double> ypoly= p_polygon.getPolygonyPoints();
			
			Area polArea= new AreaOperator().calculateArea(xpoly, ypoly);
			Pair polPoints= new Pair(xpoly,ypoly);
			MapPolygon newPol= new MapPolygon(p_polygon.getPolygonId(),polPoints,polArea);
			
			polygonsInSolution.add(newPol);
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

}
