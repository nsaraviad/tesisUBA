package Polygons;

import GraphComponents.*;
import MapOptimizer.MapQuadrantsGenerator;
import Parsing.OsmParserAndCustomizer;
import java.awt.geom.Area;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import org.omg.CORBA.DoubleSeqHelper;
import Visualizer.CoordinatesConversor;


public class PolygonsGeneratorFromFilteredEntries extends PolygonsGenerator {
	
	//Members
	private RoadGraph rg;
	private OsmParserAndCustomizer p;
	private HashMap<Long,GraphNode> nodes;
	private HashMap<Long,Pair> nodosInterseccionEnCaminos;
	private Map<Long,LinkedList<AdjacencyInfo>> adjLst;
	private LinkedList<MapPolygon>[] polygons;
	private int polygons_counter;
	private HashMap<Integer,MapPolygon> hashPolygons;
	private PolygonsOperator polygons_op= new PolygonsOperator();
	
	//Constructor
	public PolygonsGeneratorFromFilteredEntries(OsmParserAndCustomizer g) {
		this.rg= g.getRoadGraph();
		this.p= g;
		this.nodes= rg.getNodes();
		this.adjLst= rg.getAdjLst();
		this.polygons= new LinkedList[g.cityQuadrants.length];
		this.nodosInterseccionEnCaminos= new HashMap<Long,Pair>();
		this.hashPolygons= new HashMap<Integer,MapPolygon>();
	}
	
	//Método encargado de generar todos los polígonos a ser considerados
	public void generatePolygons(){
		
		//Variables
		HashSet<Long> res= new HashSet<Long>();
		Map<Long,Integer> distancesToNode1,distancesToNode2;
		int cantIntersecciones, p2;
		LinkedList<Long> p1;
		LinkedList<AdjacencyInfo>[] pathsNode1, pathsNode2;
		LinkedList<Long>[] visitedNodes1,visitedNodes2;
		LinkedList<Integer> dimensiones;
		long[] resultado;
		boolean esElpol;
		boolean nodoPart;
		
		//Initialize
		initializePolygonsArray();
		
		distancesToNode1= new HashMap();
		distancesToNode2= new HashMap();
		
		pathsNode1= new LinkedList[4];
		pathsNode2= new LinkedList[4];
		
		visitedNodes1= new LinkedList[4];
		visitedNodes2= new LinkedList[4];
		
		dimensiones= new LinkedList<Integer>();
		initializePaths(pathsNode1, pathsNode2);
		initializeVisitedNodes(visitedNodes1,visitedNodes2);
		
		resultado= new long[4];
		polygons_counter= 0;
		
		//Algoritmo busqueda de polygonos
		for(Iterator<Entry<Long, GraphNode>> it_node_1= nodes.entrySet().iterator();it_node_1.hasNext();){
			
			Entry<Long, GraphNode> entry1= it_node_1.next();
			
			//itero sobre los demas nodos
			for(Iterator<Entry<Long, GraphNode>> it_node_2=nodes.entrySet().iterator();
																					it_node_2.hasNext();){
				Entry<Long, GraphNode> entry2= it_node_2.next();
				
				
				//si cumplen la condiciones de selección
				if(theyAreSelectableNodes(entry1, entry2)){
					
					//RESETEO VARIABLES
					cantIntersecciones= 0;
					clearLists(pathsNode1, pathsNode2);
					res.clear();
					clearLists(visitedNodes1,visitedNodes2);
					distancesToNode1.clear();
					distancesToNode2.clear();
					nodosInterseccionEnCaminos.clear();
					
					//Se agrega a los nodos como visitados 
					addEntryNodeAsVisited(visitedNodes1, entry1);
					addEntryNodeAsVisited(visitedNodes2, entry2);
					
					//Se agregan inicialmente los adyacentes
					addAdjacents(pathsNode1, visitedNodes1, distancesToNode1, entry1);
					addAdjacents(pathsNode2, visitedNodes2, distancesToNode2, entry2);
					
					//Se chequea si hay intersecciones iniciales
					cantIntersecciones = checkForInitialsIntersect(res,cantIntersecciones, entry1, entry2);
					
					//Avanzar un nodo por cada camino si no se armo el poligono y mientras pueda seguir avanzando
					while((cantIntersecciones < 2) && 
							puedaAvanzarEnAlgunaDir(pathsNode1,pathsNode2,res,visitedNodes1,visitedNodes2,
									distancesToNode1, distancesToNode2)){
						
						Pair p= verificarSiHayInterseccionesYAgregarRef(visitedNodes1,visitedNodes2);
						
						p1= (LinkedList<Long>) p.getFirst();
						p2= (int) p.getSecond();
						
						//update variables
						cantIntersecciones= p2;
						res.addAll(p1);
					}
					
					//SE OBTIENE UN NUEVO POLÍGONO
					//Se agrega a la lista de poligonos obtenidos
					if((cantIntersecciones == 2) && validIntersections(res)){
						dimensiones= calculateDistances(res,distancesToNode1,distancesToNode2);
						
						LinkedList<Long> newPolygonPoints= new LinkedList<Long>();
						
						//ARMADO DEL POLÍGONO
						poligonAssembling(res, pathsNode1, pathsNode2,dimensiones, entry1, entry2, newPolygonPoints);
						
						/*Se agrega el polígono al/los conjuntos de polígonos pertenecientes a cada caudrante
						analizando sus esquinas*/
						addNewPolygonToRespectiveQuadrants(newPolygonPoints,polygons_counter,entry1);
						polygons_counter++;
					}
				}
			}
		}
	}

	
	private void initializePolygonsArray() {
		for(int p=0;p<polygons.length;p++)
			polygons[p]= new LinkedList();
	}

	private void addNewPolygonToRespectiveQuadrants(LinkedList<Long> nuevoPoligono,int idFromNewPolygon, 
														Entry<Long, GraphNode> entry1) {
		int polQuad;
		polQuad= getPolygonQuad(entry1);
		
		//Nuevo polígono
		createAndAddNewPolygon(nuevoPoligono, idFromNewPolygon, polQuad);
		
	}

	private int getPolygonQuad(Entry<Long, GraphNode> entry1) {
		GraphNode polygonEntry1;
		int indexQuadrantPoligonEntry1;
		
		//Se obtiene el cuadrante al que pertenece la entrada 1
		polygonEntry1= nodes.get(entry1.getKey());
			
		//indice del cuadrante al que pertenece el vertice
		indexQuadrantPoligonEntry1= p.getNodeQuadrant(polygonEntry1);
			
		return indexQuadrantPoligonEntry1;
		
	}

	private void createAndAddNewPolygon(LinkedList<Long> newPolygonPoints,int idFromNewPolygon, int polygon_quad_index) {
		
		Pair p= polygons_op.calculatePolygonAreaAndPoints(newPolygonPoints, rg);
		
		Area newPolArea= (Area) p.getFirst();
		
		MapPolygon newPolygon= new MapPolygon(idFromNewPolygon,(Pair) p.getSecond(),newPolArea);
		polygons[polygon_quad_index].add(newPolygon);
		
		//se agrega al mapa de poligonos general
		addToHashMapPolygons(idFromNewPolygon, newPolygon);
	}

	private void addToHashMapPolygons(int idFromNewPolygon,MapPolygon newPolygon) {
		hashPolygons.put(idFromNewPolygon,newPolygon);
	}

	private boolean validIntersections(HashSet<Long> res) {
		// Se chequea que los nodos en la intersección sean válidos (distintos, que no sean vecinos y que no estén en una msima dirección)
		//nodosDistintos(entry1, entry2) && esDeGrado4(entry1) && esDeGrado4(entry2) && theyAreNotNeighbors(entry1,entry2) && noDirectPathBetween(entry1,entry2);
		
		boolean distincts, neighbors, theyAreNotInSamePath;
		long intersect_1, intersect_2;
		
		Iterator<Long> iter= res.iterator();
		
		//Nodos de la intersección
		intersect_1= iter.next();
		intersect_2= iter.next();
		
		//Si son distintos
		distincts= (intersect_1 != intersect_2);
		
		//Si son vecinos
		neighbors= checkIfTheyAreNeighbors(intersect_1,intersect_2);
		
		//si están en la misma dirección
		theyAreNotInSamePath= notInSamePath(intersect_1,intersect_2); 
		
		return (distincts && !neighbors && theyAreNotInSamePath);
	}

	private boolean notInSamePath(long intersect_1, long intersect_2) {

		LinkedList<AdjacencyInfo> ady1,ady2;
		Set streetNames1,streetNames2, namesIntersection;
		
		streetNames1= new HashSet<String>();
		streetNames2= new HashSet<String>();
		
		ady1= adjLst.get(intersect_1);
		ady2= adjLst.get(intersect_2);
		
		getStreetNames(ady1, streetNames1);
		getStreetNames(ady2,streetNames2);
		
		//Check for intersection
		namesIntersection= new HashSet<String>(streetNames1);
		namesIntersection.retainAll(streetNames2);
		
		return namesIntersection.isEmpty();
	}

	private void poligonAssembling(HashSet<Long> res,
			LinkedList<AdjacencyInfo>[] pathsNode1,
			LinkedList<AdjacencyInfo>[] pathsNode2,
			LinkedList<Integer> dimensiones,
			Entry<Long, GraphNode> entry1,
			Entry<Long, GraphNode> entry2,
			LinkedList<Long> nuevoPoligono) {
		
		
		long intersect_1, intersect_2;
		int distanceTo1_int_1,distanceTo2_int_1,distanceTo1_int_2,distanceTo2_int_2;
		LinkedList<AdjacencyInfo> camino_int1_1, camino_int1_2, camino_int2_1,camino_int2_2;
		
		Iterator<Long> iter= res.iterator();
		
		//los elementos de la intersección
		intersect_1= iter.next();
		intersect_2= iter.next();
		
		//las distancias a los nodos
		distanceTo1_int_1= dimensiones.get(0);
		distanceTo2_int_1= dimensiones.get(1);
		distanceTo1_int_2= dimensiones.get(2);
		distanceTo2_int_2= dimensiones.get(3);
		
		//Los caminos que intersecan en cada vértice intersección
		camino_int1_1= pathsNode1[(int) (nodosInterseccionEnCaminos.get(intersect_1).getFirst())];
		camino_int1_2= pathsNode2[(int) (nodosInterseccionEnCaminos.get(intersect_1).getSecond())];
		camino_int2_1= pathsNode1[(int) (nodosInterseccionEnCaminos.get(intersect_2).getFirst())];
		camino_int2_2= pathsNode2[(int) (nodosInterseccionEnCaminos.get(intersect_2).getSecond())];
		
		
		//Se van concatenando los caminos para formar el contorno del polígono
		nuevoPoligono.add(intersect_1);
		addKElements(intersect_1,camino_int1_1, distanceTo1_int_1 - 1,nuevoPoligono);
		nuevoPoligono.add(entry1.getKey());
		addKElements(entry1.getKey(),camino_int2_1, distanceTo1_int_2 - 1,nuevoPoligono);
		nuevoPoligono.add(intersect_2);
		addKElements(intersect_2,camino_int2_2, distanceTo2_int_2 - 1,nuevoPoligono);
		nuevoPoligono.add(entry2.getKey());
		addKElements(entry2.getKey(),camino_int1_2, distanceTo2_int_1 - 1,nuevoPoligono);
	}

	
	
	private int checkForInitialsIntersect(HashSet<Long> res,
			int cantIntersecciones,
			Entry<Long, GraphNode> entry1,
			Entry<Long, GraphNode> entry2) {
		
		LinkedList<AdjacencyInfo> adjacentsTo1, adjacentsTo2;
		
		adjacentsTo1= adjLst.get(entry1.getKey());
		adjacentsTo2= adjLst.get(entry2.getKey());
		
		for(int i=0;i<adjacentsTo1.size();i++){
			if(adjacentsTo2.contains(adjacentsTo1.get(i))){
				cantIntersecciones++;
				res.add(adjacentsTo1.get(i).getAdjId());
			}
		}
		return cantIntersecciones;
	}

	private void addEntryNodeAsVisited(LinkedList<Long>[] visitedNodes ,Entry<Long, GraphNode> entry1) {
		for(int i=0;i<4;i++)
			visitedNodes[i].add(entry1.getKey());
	}

	private void addKElements(	long lastAdded, LinkedList<AdjacencyInfo> way, int k,	LinkedList<Long> result) {
		//Agrega los k primeros elementos de way en result
		//Chequea que el orden sea el adecuado (que lastAdded sea el vecino del primero del camino a agregar)
		
		//Si hay elementos en el camino
		if(way.size()>0){
			//Se chequea que el primero del camino sea vecino de lastAdded
			if(checkIfTheyAreNeighbors(lastAdded,way.getFirst().getAdjId())){
		
				//Caso 1 (orden actual)
				addElementsFromTo(way,0,k,result,true);
			}else{
				//Caso 2 (reverso)
				addElementsFromTo(way,0,k,result,false);
			}
		}
	}
	

	private void addElementsFromTo(LinkedList<AdjacencyInfo> way, int from, int to,LinkedList<Long> result,boolean cond) {
		
		if((0 <= to) && (to <= way.size())){
			
			int i= from;
			int index;
			
			if(cond){
				while(i < to){
					result.add(way.get(i).getAdjId());
					i++;
				}	
			}
			else
			{
				while(i < to){
					result.add(way.get(to -1 - i).getAdjId());
					i++;
				}
			}
		}
	}
	

	private boolean checkIfTheyAreNeighbors(long lastAdded, long adjId) {
		// Se verifica si ady esta en la lista de adyacentes de lastAdded
		boolean theyAreNeighbors= false;
		long temp_ady;
		LinkedList<AdjacencyInfo> adys= adjLst.get(lastAdded);
		
		Iterator<AdjacencyInfo> it= adys.iterator();
		
		while(it.hasNext() && !theyAreNeighbors){
			temp_ady= it.next().getAdjId();
			theyAreNeighbors= (temp_ady == adjId);
		}
		
		return theyAreNeighbors;
	}

	private void initializeVisitedNodes(LinkedList<Long>[] visitedNodes1,LinkedList<Long>[] visitedNodes2) {
		//Se crean las listas vacías(una por dirección a tomar)
		for(int i=0;i<4;i++){
			visitedNodes1[i]= new LinkedList<Long>();
			visitedNodes2[i]= new LinkedList<Long>();
		}
		
	}

	private boolean theyAreSelectableNodes(Entry<Long, GraphNode> entry1,Entry<Long, GraphNode> entry2) {
		//Se verifica que (entry1,entry2) con entry1 != entry2 y que el grado(entry1)=grado(entry2)=4
		//y ademas no tiene que estar en la misma calle
		
		return  differentsNodes(entry1, entry2) && 
				distanceBetweenNodesInRange(entry1,entry2,0.7,1) &&
				graphNodesFilter(entry1,entry2) &&
			    theyAreNotNeighbors(entry1,entry2) && 
				noDirectPathBetween(entry1,entry2);
	}

	
	private boolean distanceBetweenNodesInRange(Entry<Long, GraphNode> entry1, Entry<Long, GraphNode> entry2,
												double minDist, double maxDist) {
		
		GraphNode node1, node2;
		double distanceBetweenNodes;
		double lat1,lat2,long1,long2;
		
		node1= rg.getNodes().get(entry1.getKey());
		node2= rg.getNodes().get(entry2.getKey());
		
		lat1= node1.getLat();
		lat2= node2.getLat();
		long1= node1.getLon();
		long2= node2.getLon();
		
		return  rg.getDistance(lat1, long1, lat2, long2) <= maxDist && 
				rg.getDistance(lat1, long1, lat2, long2) > minDist ;
	}

	private boolean graphNodesFilter(Entry<Long, GraphNode> entry1,Entry<Long, GraphNode> entry2) {
		return degrees234(entry1.getKey()) && degrees234(entry2.getKey()); 
	}
	
	private boolean degrees234(Long nodeKey) {
		return (degree2Part(nodeKey) || isDegree(nodeKey,3) || isDegree(nodeKey,4));
	}

	private boolean degree2Part(Long nodeKey) {
		//Retorna true si el nodo es de grado 2 y el angulo formado entre los ejes adyacentes esta proximo a los 90º
		boolean result= false;
		
		if(isDegree(nodeKey,2))
			ejesEntrantesEnAngulo(nodeKey);
		
		return result;
	}

	private boolean ejesEntrantesEnAngulo(Long nodeKey) {
		// Método encargado de analizar el angulo entre los ejes entrantes al nodo 
		long adj1,adj2;
		double m1,m2;
		LinkedList<AdjacencyInfo> adjacents;
		AdjacencyInfo result= null;
		float angle;
		boolean res= false;
		
		adjacents= adjLst.get(nodeKey); //los adyacentes al nodo (yo se que hay 2)
		
		//calculo de las 2 aristas (ady1,nod) y (ady2,nod)
		adj1= adjacents.getFirst().getAdjId();
		adj2= adjacents.getLast().getAdjId();
		
		//Si ambos nodos adyacentes están dentro de los limites de la ciudad
		if(adjacentInCity(adj1) && adjacentInCity(adj2)){
			m1= calculatePend(nodeKey,adj1);
			m2= calculatePend(nodeKey,adj2);
		
			//Calculo de angulo entre rectas con pendientes m1 y m2
			angle= Math.abs(angleBetween(m1,m2));
			res= (85<=angle) && (angle <= 95);
		}	
		
		return res;
	}
		

	private boolean differentsNodes(	Entry<Long, GraphNode> entry1,Entry<Long, GraphNode> entry2){
		return entry1.getKey() != entry2.getKey();
	}

	private LinkedList<Integer> calculateDistances(HashSet<Long> res, Map<Long, Integer> distancesToNode1,Map<Long, Integer> distancesToNode2) {
		
		//se calculan las dimensiones del poligono
		
		long actual;
		int distanceTo1,distanceTo2;
		LinkedList<Integer> setRes= new LinkedList<Integer>();
		
		Iterator<Long> it= res.iterator();
		
		//itero sobre los nodos de la interseccion
		while(it.hasNext()){
			actual= it.next();
			
			//agrego las distancias desde actual al nodo1 y nodo2
			distanceTo1= distancesToNode1.get(actual);
			distanceTo2= distancesToNode2.get(actual);
			setRes.add(distanceTo1);
			setRes.add(distanceTo2);
		}
		
		return setRes;
	}

	private void addAll(long[] resultado, LinkedList<Long> nuevoPoligono) {
		for(int i=0; i<resultado.length;i++)
			nuevoPoligono.add(resultado[i]);
	}


	private void addAdjacents(LinkedList<AdjacencyInfo>[] pathsNode,LinkedList<Long>[] visitedNodes,Map<Long,Integer> distancesToNode,
								Entry<Long, GraphNode> entry1) {

		AdjacencyInfo adj;
		LinkedList<AdjacencyInfo> adjacents= adjLst.get(entry1.getKey());
		
		//Agrego los primeros adyacentes al nodo1
		for(int i=0; i < adjacents.size(); i++){
			adj= adjacents.get(i);
			pathsNode[i].add(adj);
			visitedNodes[i].add(adj.getAdjId());
			distancesToNode.put(adj.getAdjId(),1); //distanci 1 al nodo
		}
	}
	
	private void initializePaths(LinkedList<AdjacencyInfo>[] pathsNode1,
			LinkedList<AdjacencyInfo>[] pathsNode2) {
		for(int i=0;i < 4; i++){
			pathsNode1[i]= new LinkedList<AdjacencyInfo>();
			pathsNode2[i]= new LinkedList<AdjacencyInfo>();
		}
	}

	private boolean noDirectPathBetween(Entry<Long, GraphNode> entry1,Entry<Long, GraphNode> entry2) {
		// Se chequea que entry1 y entry2 no esten sobre una misma dirección (calle)
		LinkedList<AdjacencyInfo> ady1,ady2;
		Set streetNames1,streetNames2, namesIntersection;
		
		streetNames1= new HashSet<String>();
		streetNames2= new HashSet<String>();
		
		ady1= adjLst.get(entry1.getKey());
		ady2= adjLst.get(entry2.getKey());
		
		getStreetNames(ady1, streetNames1);
		getStreetNames(ady2,streetNames2);
		
		//Check for intersection
		namesIntersection= new HashSet<String>(streetNames1);
		namesIntersection.retainAll(streetNames2);
		
		return namesIntersection.isEmpty();
		
	}

	private void getStreetNames(LinkedList<AdjacencyInfo> ady, Set streetNames) {
		for(int i=0;i < ady.size();i++)
			streetNames.add(ady.get(i).getName());
	}

	private boolean theyAreNotNeighbors(Entry<Long, GraphNode> entry1,Entry<Long, GraphNode> entry2) {
		// Metodo que devuelve true si entry1 y entry2 no son adyacentes entre si. 
		LinkedList<AdjacencyInfo> adjacents1;
		boolean theyAreNeighbors= false;
		
		adjacents1= adjLst.get(entry1.getKey());
		
		//iterate over entry1 adyacents
		for(int i=0;(i < adjacents1.size()) && !theyAreNeighbors;i++){
			//Si hay un adyacente de entry1 que tenga el mismo id que entry2 (entonces serian vecinos)
			theyAreNeighbors= (adjacents1.get(i).getAdjId() == entry2.getKey());
		}
		
		return !theyAreNeighbors;
		
	}

	private boolean isDegree(Long node_id,int grado) {
		//devuelve true cuando el grado del nodo es igual algrado especificado
		int gradoNodo = adjLst.get(node_id).size();
		return (gradoNodo == grado);
	}

	//Método encargado de limpiar los caminos en cada nueva iteración
	private void clearLists(LinkedList[] pathsNode1,LinkedList[] pathsNode2) {
		for(int i=0; i < 4;i++){
			pathsNode1[i].clear();
			pathsNode2[i].clear();
		}
	}

	
	private Pair verificarSiHayInterseccionesYAgregarRef(LinkedList<Long>[] visitedNode1,LinkedList<Long>[] visitedNode2) {
		//Metodo encargado de chequear si hay nodos compartidos entre caminos de path1 y path2. En el caso de haber se incrementa la 
		//cantidad y se agrega dichas referencias al resultado
		LinkedList<Long> resRef= new LinkedList();
		Long visitNode;
		int resCountIntersect= 0;
		
		//Chequeo en cada dirección de nodo1
		for(int j=0; j<4;j++){
			for(int i=0;i<visitedNode1[j].size();i++){
				 visitNode = visitedNode1[j].get(i);
				 for(int k=0;k<4;k++){ //itero sobre los conjuntos de visitados del nodo 2
					 if(visitedNode2[k].contains(visitNode) && !resRef.contains(visitNode)){
							resCountIntersect++; 
							resRef.add(visitNode);
							nodosInterseccionEnCaminos.putIfAbsent(visitNode, new Pair(j,k)); 
							//Camino 1, Camino 2 (que caminos de nodo 1 y 2 se intersecan en visitNode)
						}	 
				 }
			}	
			
		}
		
		return new Pair(resRef,resCountIntersect);
		
	}
	
	
	private boolean puedaAvanzarEnAlgunaDir(LinkedList<AdjacencyInfo>[] pathsNode1, LinkedList<AdjacencyInfo>[] pathsNode2, 
											Set res, LinkedList<Long>[] visitedNodes1, LinkedList<Long>[] visitedNodes2,
											Map distancesToNode1, Map distancesToNode2) {
		// Método que indica si es posible encotrar en cada ultimo elemento de cada camino un adyacente en su misma direccion (nombre de calle)
		//Si es posible avanza
		boolean res1, res2;
		
		res1= avanzarCaminosNodo(pathsNode1, res, visitedNodes1,distancesToNode1);
		res2= avanzarCaminosNodo(pathsNode2, res, visitedNodes2, distancesToNode2);
		
		return res1 || res2;
	}

	private boolean avanzarCaminosNodo(LinkedList<AdjacencyInfo>[] pathsNode, 
										Set res, LinkedList<Long>[] visitedNodes, 
										Map distancesToNode) {

		//Método encargado de avanzar (si es posible) un nodo en la misma dirección de cada camino del array pathNodes
		String nameStreet;
		Long key_last;
		int dist;
		boolean puedeAvanzar= false;
		boolean elNodo;
		AdjacencyInfo temp_last, ady;
		
		for(int i=0;i < pathsNode.length;i++){
			//Por lo menos tiene adyacente en el path (para los nodos de grado 3 hay path vacío!)
			if(pathsNode[i].size()>0){
				temp_last= pathsNode[i].getLast();
				nameStreet= temp_last.getName();
				//Buscar adyacentes de temp_last enla misma direccion
				key_last= temp_last.getAdjId();
				
				//AÑADIR AL MÉTODO buscarAdyacente  LA FUNCIONALIDAD DE ANALISIS DE ANGULOS ENTRE LAS RECTAS 
				ady = buscarAdyacentePorNombreDeCalle(key_last,nameStreet, res, visitedNodes[i]); 
				
				//Si no se encuentra adyacente por nombre de calle (o porque hubo cambio de nombre o porque no se puede
				//continuar avanzando por dicha calle). Para chequear si analizan los angulos.
				if(!adyacentFound(ady))
					ady= buscarAdyacentePorAnguloEntreAristas(pathsNode[i],res,visitedNodes[i]);
				
				dist= (int) distancesToNode.get(key_last); //distancia desde el nodo a ultimo nodo (key_last)
					
				//si encuentro adyacente en la misma direccion, avanzo. Caso contrario, no se puede continuar avanzando 
				//en la dirección.
				if(adyacentFound(ady)){
					pathsNode[i].add(ady);
					distancesToNode.put(ady.getAdjId(),dist+1); //nueva distancia al adyacente (dist + 1)
					puedeAvanzar= true;
				}
			}
		}
			
		return puedeAvanzar;
	}

	private boolean adyacentFound(AdjacencyInfo ady) {
		return ady!=null;
	}

	
	private AdjacencyInfo buscarAdyacentePorAnguloEntreAristas(LinkedList<AdjacencyInfo> path, 
														  Set res,
														  LinkedList<Long> visitedNodes) 
	{
		//Metodo encargado de elegir el proximo nodo a visitar en el path
		long node1,node2;
		double m1,m2;
		long key_last;
		LinkedList<AdjacencyInfo> adyacents;
		AdjacencyInfo result= null;
		long lastNode_id, ady_temp_id;
		float angle;
	
		lastNode_id= path.getLast().getAdjId();
		
		adyacents= adjLst.get(lastNode_id); //los adyacentes al nodo con id "key_last"
		
		//calculo de la pendiente sobre la que estoy "parado"
		node1= visitedNodes.get(visitedNodes.size() - 2);
		node2= visitedNodes.getLast();
		
		//Si ambos nodos están dentro de los limites de la ciudad
		if(adjacentInCity(node1) && adjacentInCity(node2)){
		
			m1= calculatePend(node1,node2);
				
			int i=0;
			while(i < adyacents.size()){
				
				ady_temp_id= adyacents.get(i).getAdjId(); //obtengo un adyacente
				
				//Si el adyacente está dentro de los límites de la ciudad, procedo
				if(adjacentInCity(ady_temp_id)){
				
					//Calculo m2 de la recta entre lastNode y ady_temp
					m2= calculatePend(lastNode_id,ady_temp_id);
					
					//Calculo de angulo entre rectas con pendientes m1 y m2
					angle= Math.abs(angleBetween(m1,m2));
					
					//Actualizo el angulo "actual que cumple"
					//Considero aquellos nodos de grado 2 que pueden continuar por un path
					if( ( ((0 < angle) && (angle < 45) && !visitedNodes.contains(ady_temp_id)) ) ||
						( (isDegree(lastNode_id,2)) &&  ((0 < angle) && (angle < 50) && !visitedNodes.contains(ady_temp_id)))	)
						result= adyacents.get(i);
				}	
				
				i++;
			}
			
			if((result != null) && (!visitedNodes.contains(result.getAdjId())) &&	(!res.contains(result.getAdjId())))
					visitedNodes.add(result.getAdjId()); //se agrega a la lista de nodos visitados en esa dirección
			
		}
		return result;	
	}

	
	private boolean adjacentInCity(long ady_temp_id) {
		return nodes.containsKey(ady_temp_id);
	}

	private float angleBetween(double m1, double m2) {
	//Cálculo de ángulo formado entre 2 rectas con pendientes m1 y m2
		float betha;
		double num, den, value;
		
		num= m2 - m1;
		den= 1 + (m1*m2);
		value= (num / den);
		
		betha= (float) Math.toDegrees(Math.atan(value));
		
		return betha;
	}

	private double calculatePend(long node1_id, long node2_id){
		double x1,y1,x2,y2;
		double num,den;
		
		//Pair(lat,long)
		Pair p1= convertToR2(node1_id);
		Pair p2= convertToR2(node2_id);
		
		x1= (double) p1.getFirst();
		y1= (double) p1.getSecond();
		
		x2= (double) p2.getFirst();
		y2= (double) p2.getSecond();
		
		//numerator 
		num= y2 - y1;
		
		//denominator
		den= x2 - x1;
		
		return (num/den);
	}

	private Pair convertToR2(long node_id) {
		double latit;
		double longit;
		double latitude;
		double longitude;
		GraphNode node;
		
		node= nodes.get(node_id);
		
		latitude=node.getLat();
		longitude= node.getLon();
		
		//Pair conversionToLatLon= CoordinatesConversor.getConvertedNumberFromLatLong(latitude, longitude);
		
		latit= CoordinatesConversor.convertLatitudeToPoint(latitude);
		longit= CoordinatesConversor.convertLongitudeToPoint(longitude);
		//latit= (double) conversionToLatLon.getSecond();
		//longit= (double) conversionToLatLon.getFirst();
		
		return new Pair(latit,longit);
	}
	
	
	
	private AdjacencyInfo buscarAdyacentePorNombreDeCalle(Long key_last,String nameStreet, Set resList, LinkedList i_visitedNodes) {
		//Metodo que se encarga de buscar entre todos los adyacentes al nodo key_last aquel con mismo nombre 
		boolean found= false;
		LinkedList<AdjacencyInfo> adyacents;
		AdjacencyInfo res= null;
		AdjacencyInfo ady_temp;
		adyacents= adjLst.get(key_last); //los adyacentes al nodo con id "key_last"
		
		
		if(adjacentInCity(key_last)){
			if(nameStreet!=null){
				int i= 0;
				while(i < adyacents.size() && !found){
					ady_temp= adyacents.get(i);
					  
					if((ady_temp.getName() != null) && (ady_temp.getName().equals(nameStreet)) && (!i_visitedNodes.contains(ady_temp.getAdjId())) &&
																	(!resList.contains(ady_temp.getAdjId()))){
						res= adyacents.get(i);
						i_visitedNodes.add(res.getAdjId()); //se agrega a la lista de nodos visitados en esa dirección
						found= true;
					}
					i++;
				}
			}
		}
		return res;
	}
	
	//Obtengo el conjunto de polígonos generados
	public LinkedList[] getPolygons(){
		return polygons;
	}
	
	//Los poligonos del iesimo cuadrante
	public LinkedList<MapPolygon> getPolygonsFromIQuadrant(int indexQuadrant){
		return polygons[indexQuadrant];
	}
	
	//La cantidad total de polígonos generados
	public int getPolygonsCount(){
		return polygons_counter;
	}
	
	//Obtener el polígono dado id
	public MapPolygon getPolygonWithId(int idPolygon){
		return hashPolygons.get(idPolygon); 
	}
	
}


