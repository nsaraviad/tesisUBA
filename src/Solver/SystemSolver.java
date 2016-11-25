package Solver;

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
	
	public void solve(LinkedList<MapPolygon>[] polygons,int polygonsCount, OsmParserAndCustomizer p){
		//Variables decl
		Variable[] vars = new Variable[polygonsCount];
		double edgeInPolygon;
	    DirectedEdge temp_edge;
	    LinkedList<Long> temp_pol;
	    double covered;
	    LinkedList<Integer> edgequad;
	    LinkedList<MapPolygon> quadPolygons;
	    int id_temp_polygon;
	    HashSet<Integer> coveredByPol= new HashSet<Integer>();
	    
		
	    //Model
		System.loadLibrary("jscip");
		Scip scip= new Scip();
		scip.create("solver");
		
		//Se crean las variables del modelo (total de polígonos)
		for(int i=0;i<polygonsCount;i++)
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
	    	
	    	//poligonos del/los cuadrante/s del eje
	    	for(int q=0;q < edgequad.size();q++ ){
	    		quadPolygons= polygons[edgequad.get(q)];
	    		
	    		for(int pol=0;pol < quadPolygons.size() ;pol++){
					//itero sobre cada polígono
	    			temp_pol= quadPolygons.get(pol).getPolygonPoints();
					id_temp_polygon=quadPolygons.get(pol).getPolygonId();
					
					//Verifico si el eje es cubierto por el poligono
					edgeInPolygon= pol_op.checkIfEdgeIsInPolygon(temp_edge,temp_pol,p);
					covered = covered +  edgeInPolygon;
					
					if(edgeInPolygon == 1)
						coveredByPol.add(id_temp_polygon);
	    		}
			}
	    	
	    	//Restricciones para los ejes cubierto por al menos un nodo
	     	if (covered > 0 ){
	     		
	     		Variable[] varsConst= new Variable[coveredByPol.size()];
	     		double[] valsConst= new double[coveredByPol.size()];
	     		
	     		Iterator it= coveredByPol.iterator();
	     		
	     		//Se completan vars y vals
	     		int i=0;
	     		while(it.hasNext()){
	     			int idPol= (int) it.next();
	    			varsConst[i] = scip.createVar("x"+idPol, 0.0, 1.0, 1.0, SCIP_Vartype.SCIP_VARTYPE_BINARY);
	    			valsConst[i]= 1;
	    			i++;
	     		}

	     		//Add linear constraint
	     		Constraint cons = scip.createConsLinear("edgeCovered" + e, varsConst, valsConst,1,scip.infinity());
	     		scip.addCons(cons);
	     	}
			
	    }
		
	    scip.solve();
		
		// print all solutions
	    Solution[] allsols = scip.getSols();
	
	    for( int s = 0; allsols != null && s < allsols.length; ++s )
	         //System.out.println("solution (x,y) = (" + scip.getSolVal(allsols[s], x) + ", " + scip.getSolVal(allsols[s], y) + ") with objective value " + scip.getSolOrigObj(allsols[s]));
	    	for(int i=0;i<polygonsCount;i++)
	    		System.out.println("solution " + i + " = " + scip.getSolVal(allsols[s], vars[i] ) );
		}
		
}
