package Solver;

import java.awt.geom.Area;
import java.util.Arrays;
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
	
	//Atributos
	private LinkedList polygonsInSolution= new LinkedList<Integer>();
	
	
	
	//Métodos
	public void solve(LinkedList<MapPolygon>[] polygons,int totalPolygonsCount, OsmParserAndCustomizer p,
			boolean optionActivated, int maxOverlapping){
		//Variables decl
		Variable[] vars = new Variable[totalPolygonsCount];
		Variable[] varsEdges= new Variable[p.getEdges().size()];
		
		double[] vals= new double[totalPolygonsCount];
		double edgeInPolygon;
	    DirectedEdge temp_edge;
	    LinkedList<Long> temp_polPoints;
	    double covered;
	    int edgequad;
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
	
		if(optionActivated){
			//variable para la cobertura de aristas
			for(int a=0;a<p.getEdges().size();a++)
				varsEdges[a]= scip.createVar("y_"+a, 0.0, 1.0, 0.01, SCIP_Vartype.SCIP_VARTYPE_BINARY);
		}	
		
		//Restricciones
		PolygonsOperator pol_op= new PolygonsOperator();
		
	    //para todo eje
	    for(int e=0;e < p.getEdges().size();e++){
	    	temp_edge= p.getEdges().get(e);
	    	
	    	//cuadrante en donde se encuentra el eje
	    	edgequad= temp_edge.getPertQuad();
	    	covered=0; 
	    	coveredByPol.clear();
	    	
	    	quadPolygons= polygons[edgequad];
	    		
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
			
	    	//Solo se agregan restricciones para los ejes cubiertos por al menos un polígono
	    	if(covered > 0){
	     		
	    		//Array con poligonos que cubren al eje actual
	    		Variable[] inSol= new Variable[coveredByPol.size()];
	    		double[] valsOnTrue= new double[coveredByPol.size()];
	    		
	    		Arrays.fill(valsOnTrue, 1);
	    		Iterator<Integer> it= coveredByPol.iterator();
	    		int polygon_id;
	    		int index= 0;
	    		
	    		while(it.hasNext()){
	    			polygon_id= it.next();
	    			inSol[index]= vars[polygon_id];
	    			index++;
	    		}
	    		//RESTRICCIÓN 1 (TODAS LAS ARISTAS CUBIERTAS)
	    		Constraint cons_1 = scip.createConsLinear("edgeCovered" + e, inSol, valsOnTrue,1,scip.infinity());
	    		scip.addCons(cons_1);
	     		scip.releaseCons(cons_1);
	    		
	     		inSol= null;
	     		valsOnTrue= null;
	     	
	     		if(optionActivated && covered > 1){
		     		//RESTRICCIÓN 2 (LÍMITE EN COBERTURA PARA CADA ARISTA)
			    	Variable[] y_cons= new Variable[1];
			    	double[] vals_sol= new double[1];
			    	
			    	y_cons[0]= varsEdges[e];
			    	vals_sol[0]= maxOverlapping - 1; //k = #máxima de superposiciones permitidas
			    	
		     		Constraint cons_2 = scip.createConsLinear("maxCov" + e,y_cons,vals_sol,covered -1,scip.infinity());
			    	scip.addCons(cons_2);
			    	scip.releaseCons(cons_2);
			    }
	     	}
	    	
	    }
		
	    scip.solve();
		
		Solution sol= scip.getBestSol();
	
	   	for(int i=0;i<totalPolygonsCount;i++)
	   		if(scip.getSolVal(sol,vars[i]) > 0)
	   			polygonsInSolution.add(i);
	}


	
	public LinkedList<Integer> getPolygonsInSolution(){
		return polygonsInSolution;
	}
		
}
