package Solver;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import GraphComponents.DirectedEdge;
import Parsing.OsmParserAndCustomizer;
import PolygonsOpers.PolygonsOperator;
import jscip.*;

//SYSTEM SOLVER CLASS

public class SystemSolver {
	
	/*
	public void solve(LinkedList[] polygons,OsmParserAndCustomizer p){
	
	System.loadLibrary("jscip");
	
	Scip scip= new Scip();
	
	scip.create("solver");
	
	int polSize= polygons.size();
	Variable[] vars = new Variable[polSize];
	double[] vals= new double[polSize];
	boolean esEje;
	//Se crean las variables del modelo
	for(int i=0;i<polygons.size();i++)
		vars[i] = scip.createVar("x"+ i, 0.0, 1.0, 1.0, SCIP_Vartype.SCIP_VARTYPE_BINARY);
	
	//Restricciones
    double edgeInPolygon;
    DirectedEdge temp_edge;
    LinkedList<Long> temp_pol;
    double covered;
    LinkedList ninguno= new LinkedList<DirectedEdge>();
    LinkedList edgequad;
    LinkedList<LinkedList<Long>> quadsPolygon; 
    
    PolygonsOperator pol_op= new PolygonsOperator();
	
    //para todo eje
    for(int e=0;e < p.getEdges().size();e++){
    	temp_edge= p.getEdges().get(e);
    	edgequad= temp_edge.getPertQuad();
    	
    	covered=0; 
    	
    	//poligonos del/los cuadrante/s del eje
    	for(int q=0;q < edgequad.size();q++ ){
    		quadsPolygon= polygons[(int) edgequad.get(q)];
    		
    		for(int pol=0;pol < quadsPolygon.size() ;pol++){
				temp_pol= quadsPolygon.get(pol);
				edgeInPolygon= pol_op.checkIfEdgeIsInPolygon(temp_edge,temp_pol , p);
				covered = covered +  edgeInPolygon;
				vals[pol]= edgeInPolygon;
    		}
			
    	}
    	
    	
    	//Solo se agregan las restricciones par aquellos ejes cubiertos por polígonos
     	if (covered == 0 ){
     		ninguno.add(temp_edge);
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
	}*/
}
