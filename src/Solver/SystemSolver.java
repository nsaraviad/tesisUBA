package Solver;

import java.util.LinkedList;
import Parsing.ParseOSM;
import PolygonsOpers.PolygonsOperator;
import jscip.*;

//SYSTEM SOLVER CLASS

public class SystemSolver {
	
	public void solve(LinkedList<LinkedList<Long>> polygons,ParseOSM p){
	
	System.loadLibrary("jscip");
	
	Scip scip= new Scip();
	
	scip.create("solver");
	
	int polSize= polygons.size();
	Variable[] vars = new Variable[polSize];
	double[] vals= new double[polSize];
	
	Variable temp_var;
	
	//Se crean las variables del modelo
	for(int i=0;i<polygons.size();i++){
		temp_var = scip.createVar("x"+ i, 0.0, 1.0, 1.0, SCIP_Vartype.SCIP_VARTYPE_BINARY);
		vars[i]= temp_var;
	}
	
	//Restricciones
    double edgeInPolygon;
    
    PolygonsOperator pol_op= new PolygonsOperator();
	
    for(int e=0;e < p.getEdges().size();e++){
		for(int pol=0;pol < polSize;pol++){
				edgeInPolygon= pol_op.checkIfEdgeIsInPolygon(p.getEdges().get(e), polygons.get(pol), p);
				vals[pol]= edgeInPolygon;
		}
		Constraint cons = scip.createConsLinear("allEdgesCovered"+e, vars, vals,1.0,scip.infinity());
		scip.addCons(cons);
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
