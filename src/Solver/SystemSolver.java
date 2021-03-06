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
		
		double edgeInPolygon;
	    DirectedEdge temp_edge;
	    double covered;
	    int edgequad;
	    LinkedList<MapPolygon> quadPolygons;
	    int id_temp_polygon;
	    HashSet<Integer> coveredByPol= new HashSet<Integer>();
	    boolean edgeIsCovered;
	    MapPolygon actual_pol;
	    Area temp_area;
	    	   
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
			//variables para la cobertura de aristas
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
	    		
	     		
	     		if(optionActivated && covered > 0){
		     		
	     			//RESTRICCIÓN 2 (LÍMITE EN COBERTURA PARA CADA ARISTA)
			    	Variable[] multipleVars= new Variable[valsOnTrue.length + 1];
	     			double[] coefs= new double[valsOnTrue.length + 1];
	     			
	     			//ARMO LOS ARRAYS DE VARS
	     			createMultipleVariablesArray(varsEdges, e, inSol, multipleVars);
	     			
	     			//Coeficients filling
	     			fillingCoeficientsArray(maxOverlapping, valsOnTrue, coefs);
	     		
	     			//Add constraint 2
	     			Constraint cons_2 = scip.createConsLinear("constraint_2" + e, multipleVars, coefs,-scip.infinity(),1);
	     			scip.addCons(cons_2);
			    	scip.releaseCons(cons_2);
			    	
			    	//Free memory
			    	multipleVars= null;
			    	coefs= null;
			    	inSol=null;
			    	valsOnTrue=null;
			    }
	     	}
	    }

	    //set limits time param (in seconds)
	    scip.setRealParam("limits/time",600);
	    scip.solve();
		
		Solution sol= scip.getBestSol();
		
	   	for(int i=0;i<totalPolygonsCount;i++)
	   		if(scip.getSolVal(sol,vars[i]) > 0)
	   			polygonsInSolution.add(i);
	}

	private void fillingCoeficientsArray(int maxOverlapping,double[] valsOnTrue, double[] coefs) {
		Arrays.fill(coefs, 1);
		coefs[valsOnTrue.length]= 1 - maxOverlapping;
	}

	
	private void createMultipleVariablesArray(Variable[] varsEdges, int e, Variable[] inSol,
			Variable[] quadvars1) {
		
		//copia del array inSol 
		for(int j=0; j < inSol.length;j++)
			quadvars1[j]= inSol[j];
		
		//en la última posición guardo la variable yi
		quadvars1[inSol.length]= varsEdges[e];
	}

    public LinkedList<Integer> getPolygonsInSolution(){
		return polygonsInSolution;
	}
		
}
