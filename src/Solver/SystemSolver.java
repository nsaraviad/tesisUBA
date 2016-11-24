package Solver;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;




import GraphComponents.DirectedEdge;
import GraphComponents.Pair;
import Parsing.OsmParserAndCustomizer;
import PolygonsOpers.PolygonsOperator;
import jscip.*;

//SYSTEM SOLVER CLASS

public class SystemSolver {
	
	
	public void solve(LinkedList<Pair>[] polygons,int polygonsCount, OsmParserAndCustomizer p){
	
		//Variables decl
		//Variable[] vars = new Variable[polygonsCount];
		//double[] vals= new double[polygonsCount];
		double edgeInPolygon;
	    DirectedEdge temp_edge;
	    LinkedList<Long> temp_pol;
	    double covered;
	    LinkedList ninguno= new LinkedList<DirectedEdge>();
	    LinkedList edgequad;
	    LinkedList<Pair> quadPolygons;
	    Pair temp_pair;
	    int id_temp_polygon;
	    Set coveredByPol= new HashSet<Integer>();
	    
		
	    //Model
		System.loadLibrary("jscip");
		Scip scip= new Scip();
		scip.create("solver");
		
		//Se crean las variables del modelo (total de polígonos)
		//for(int i=0;i<polygonsCount;i++)
		//	vars[i] = scip.createVar("x"+ i, 0.0, 1.0, 1.0, SCIP_Vartype.SCIP_VARTYPE_BINARY);
		
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
	    		quadPolygons= polygons[(int) edgequad.get(q)];
	    		
	    		for(int pol=0;pol < quadPolygons.size() ;pol++){
					temp_pair= quadPolygons.get(pol);
					temp_pol= (LinkedList<Long>) temp_pair.getFirst();
					id_temp_polygon= (int) temp_pair.getSecond();
					//Verifico si el eje es cubierto por el poligono
					edgeInPolygon= pol_op.checkIfEdgeIsInPolygon(temp_edge,temp_pol , p);
					covered = covered +  edgeInPolygon;
					//vals[id_temp_polygon]= edgeInPolygon;
					
					if(edgeInPolygon == 1)
						coveredByPol.add(id_temp_polygon);
						
	    		}
			}
	    	
	    	
	    	//Restricciones para los ejes cubierto por al menos un nodo
	     	if (covered > 0 ){
	     		
	     		//VARSS
	     		Variable[] vars = new Variable[coveredByPol.size()];
	     		Iterator it= coveredByPol.iterator();
	     		
	     		int i=0;
	     		while(it.hasNext()){
	     			int idPol= (int) it.next();
	    			vars[i] = scip.createVar("x"+idPol, 0.0, 1.0, 1.0, SCIP_Vartype.SCIP_VARTYPE_BINARY);
	    			i++;
	     		}
	     		
	     		//VALS
	     		
	     		//Constraint cons = scip.createConsLinear("edgeCovered" + e, vars, vals,1,scip.infinity());
	     		//scip.addCons(cons);
	     	}
			
	    }
		
	    scip.solve();
		
		// print all solutions
	    Solution[] allsols = scip.getSols();
	
	    for( int s = 0; allsols != null && s < allsols.length; ++s )
	         //System.out.println("solution (x,y) = (" + scip.getSolVal(allsols[s], x) + ", " + scip.getSolVal(allsols[s], y) + ") with objective value " + scip.getSolOrigObj(allsols[s]));
	    	for(int i=0;i<polSize;i++)
	    		System.out.println("solution " + i + " = " + scip.getSolVal(allsols[s], vars[i] ) );
		}
}
