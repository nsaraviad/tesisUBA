package Solver;

import java.awt.geom.Area;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import GraphComponents.DirectedEdge;
import GraphComponents.Pair;
import Parsing.OsmParserAndCustomizer;
import Polygons.MapPolygon;
import Polygons.PolygonsOperator;
import jscip.*;

//SYSTEM SOLVER CLASS

public class SystemSolver {
	
	public void solve(LinkedList<MapPolygon>[] polygons,int totalPolygonsCount, OsmParserAndCustomizer p){
		//Variables decl
		Variable[] vars = new Variable[totalPolygonsCount];
		double[] vals= new double[totalPolygonsCount];
		double edgeInPolygon;
	    DirectedEdge temp_edge;
	    LinkedList<Long> temp_polPoints;
	    double covered;
	    LinkedList<Integer> edgequad;
	    LinkedList<MapPolygon> quadPolygons;
	    int id_temp_polygon;
	    HashSet<Integer> coveredByPol= new HashSet<Integer>();
	    boolean edgeIsCovered;
	    MapPolygon actual_pol;
	    Area temp_area;
	    LinkedList<DirectedEdge> notConv= new LinkedList<DirectedEdge>();
	    	   
	    //Model
		System.loadLibrary("jscip");
		Scip scip= new Scip();
		scip.create("solver");
		
		/* FUNCION OBJETIVO
		 Sum(0,cantPol -1) x_i donde x_i= 0 si el i-ésimo polígono pert sol, 0 si no
		*/
		
		//Se crean las variables del modelo (total de polígonos)
		for(int i=0;i<totalPolygonsCount;i++)
			vars[i] = scip.createVar("x_"+i, 0.0, 1.0, 1.0, SCIP_Vartype.SCIP_VARTYPE_BINARY);
			
		
		//Restricciones
		PolygonsOperator pol_op= new PolygonsOperator();
		
		
	    //para todo eje
	    for(int e=0;e < p.getEdges().size();e++){
	    	temp_edge= p.getEdges().get(e);
	    	
	    	//cuadrantes en donde se encuentra el eje
	    	edgequad= temp_edge.getPertQuad();
	    	covered=0; 
	    	coveredByPol.clear();
	    	clearInZeros(totalPolygonsCount, vals);
	    	
	    	//poligonos del/los cuadrante/s del eje
	    	for(int q=0;q < edgequad.size();q++ ){
	    		quadPolygons= polygons[edgequad.get(q)];
	    		
	    		for(int pol=0;pol < quadPolygons.size() ;pol++){
					//itero sobre cada polígono
	    			actual_pol = quadPolygons.get(pol);
	    			id_temp_polygon= actual_pol.getPolygonId();
					temp_area= actual_pol.getPolArea();
				
					//Verifico si el eje es cubierto por el poligono
					edgeInPolygon= pol_op.checkIfEdgeIsInPolygon(temp_edge,temp_area,p);
					covered = covered +  edgeInPolygon;
					
					edgeIsCovered= (edgeInPolygon == 1);
					
					//si el eje es cubierto por el poligono, se lo agrega al conjunto de poligonos que lo cubren
					if(edgeIsCovered)
						coveredByPol.add(id_temp_polygon);
	    		}
			}
	    	
	    	//Restricciones para los ejes cubierto por al menos un nodo
	     	//if (covered == 0 )
	     	//	notConv.add(temp_edge);//{
	     	//Solo se agregan restricciones para los ejes cubiertos por al menos un polígono
	    	if(covered > 0){
	     	
	     		Iterator it= coveredByPol.iterator();
	     		int i=0;
	     		
	     		//Se pone en 1 aquellos coeficientes de polígonos en donde el eje es cubierto  
	     		while(it.hasNext()){
	     			vals[(int) it.next()]= 1;
	     			i++;
	     		}

	     		//Add linear constraint
	     		Constraint cons = scip.createConsLinear("edgeCovered" + e, vars, vals,1,scip.infinity());
	     		scip.addCons(cons);
	     		scip.releaseCons(cons);
	     	}
			
	    }
		
	    scip.solve();
		
		Solution sol= scip.getBestSol();
	
	    //for( int s = 0; allsols != null && s < allsols.length; ++s )
	         //System.out.println("solution (x,y) = (" + scip.getSolVal(allsols[s], x) + ", " + scip.getSolVal(allsols[s], y) + ") with objective value " + scip.getSolOrigObj(allsols[s]));
	    	for(int i=0;i<totalPolygonsCount;i++)
	    		if(scip.getSolVal(sol,vars[i]) > 0)
	    			System.out.println("solution " + i + " = " + scip.getSolVal(sol, vars[i] ) );
		}

	private void clearInZeros(int totalPolygonsCount, double[] vals) {
		for(int k=0;k < totalPolygonsCount; k++)
			vals[k]=0;
	}
		
}
