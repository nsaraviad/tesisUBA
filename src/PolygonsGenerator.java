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


public class PolygonsGenerator {
	
	//Miembros
	private RoadGraph rg;
	private  HashMap<Long,GraphNode> nodes;
	private HashMap<Long,Pair> nodosInterseccionEnCaminos;
	private Map<Long,LinkedList<AdyacencyInfo>> adyLst;
	private LinkedList<LinkedList<Long>> polygons;
	
	//Constructor
	public PolygonsGenerator(ParseOSM g) {
		this.rg= g.getRoadGraph();
		this.nodes= rg.getNodes();
		this.adyLst= rg.getAdyLst();
		this.polygons= new LinkedList();
		this.nodosInterseccionEnCaminos= new HashMap<Long,Pair>();
	}
	
	//Método encargado de generar todos los polígonos a ser considerados
	public void generatePolygons(){
		
		//Variables
		HashSet<Long> res= new HashSet<Long>();
		Map<Long,Integer> distancesToNode1,distancesToNode2;
		int cantIntersecciones, p2;
		LinkedList<Long> p1;
		LinkedList<AdyacencyInfo>[] pathsNode1, pathsNode2;
		
		//LinkedList<Long> visitedNodes1, visitedNodes2, p1;
		LinkedList<Long>[] visitedNodes1,visitedNodes2;
		
		LinkedList<Integer> dimensiones;
		long[] resultado;
		
		//Inicializo
		distancesToNode1= new HashMap();
		distancesToNode2= new HashMap();
		
		pathsNode1= new LinkedList[4];
		pathsNode2= new LinkedList[4];
		
		//visitedNodes1= new LinkedList<Long>();
		//visitedNodes2= new LinkedList<Long>();
		
		visitedNodes1= new LinkedList[4];
		visitedNodes2= new LinkedList[4];
		
		dimensiones= new LinkedList<Integer>();
		initializePaths(pathsNode1, pathsNode2);
		initializeVisitedNodes(visitedNodes1,visitedNodes2);
		
		
		resultado= new long[4];
		
		//Algoritmo busqueda de polygonos
		for(Iterator<Entry<Long,LinkedList<AdyacencyInfo>>> it_node_1= adyLst.entrySet().iterator();
																					it_node_1.hasNext();){
			
			Map.Entry<Long,LinkedList<AdyacencyInfo>> entry1= it_node_1.next();
			
			//itero sobre los demas nodos
			for(Iterator<Entry<Long,LinkedList<AdyacencyInfo>>> it_node_2=adyLst.entrySet().iterator();
																					it_node_2.hasNext();){
				Map.Entry<Long,LinkedList<AdyacencyInfo>> entry2= it_node_2.next();
				
				//RESETEO VARIABLES
				cantIntersecciones= 0;
				clearLists(pathsNode1, pathsNode2);
				res.clear();
				//visitedNodes1.clear();
				//visitedNodes2.clear();
				clearLists(visitedNodes1,visitedNodes2);
				distancesToNode1.clear();
				distancesToNode2.clear();
				nodosInterseccionEnCaminos.clear();
				
				if(theyAreSelectableNodes(entry1, entry2)){
					
					//Se agregan inicialmente los adyacentes
					addAdyacents(pathsNode1, visitedNodes1, distancesToNode1, entry1);
					addAdyacents(pathsNode2, visitedNodes2, distancesToNode2, entry2);
					
					
					//Se chequea si hay intersecciones iniciales
					for(int i=0;i<entry1.getValue().size();i++){
						if(entry2.getValue().contains(entry1.getValue().get(i))){
							cantIntersecciones++;
							res.add(entry1.getValue().get(i).getAdyId());
						}
					}
					
					//Avanzar un nodo por cada camino si no se armo el poligono y mientras pueda
					//seguir avanzando
					while((cantIntersecciones < 2) && 
							puedaAvanzarEnAlgunaDir(pathsNode1,pathsNode2,res,visitedNodes1,visitedNodes2,
													distancesToNode1, distancesToNode2)){
						
						Pair p= verificarSiHayInterseccionesYAgregarRef(visitedNodes1,visitedNodes2);
						
						p1= (LinkedList<Long>) p.getFirst();
						p2= (int) p.getSecond();
						
						//update de variables
						cantIntersecciones= p2;
						res.addAll(p1);
					}
					
					//SE OBTIENE UN NUEVO POLÍGONO
					//Se agrega a la lista de poligonos obtenidos
					if(cantIntersecciones == 2){
						ordenarResultado(res, entry1, entry2, resultado);
						dimensiones= calculateDistances(res,distancesToNode1,distancesToNode2);
						LinkedList<Long> nuevoPoligono= new LinkedList<Long>();
						addAll(resultado, nuevoPoligono);
						polygons.add(nuevoPoligono);
					}
				}
			}
		}
	}

	
	private void initializeVisitedNodes(LinkedList<Long>[] visitedNodes1,LinkedList<Long>[] visitedNodes2) {
		//Se crean las listas vacías(una por dirección a tomar)
		for(int i=0;i<4;i++){
			visitedNodes1[i]= new LinkedList<Long>();
			visitedNodes2[i]= new LinkedList<Long>();
		}
		
	}

	private boolean theyAreSelectableNodes(Map.Entry<Long, LinkedList<AdyacencyInfo>> entry1,Map.Entry<Long, LinkedList<AdyacencyInfo>> entry2) {
		
		//Se verifica que (entry1,entry2) con entry1 != entry2 y que el grado(entry1)=grado(entry2)=4
		//y ademas no tiene que estar en la misma calle
		
		return nodosDistintos(entry1, entry2) && esDeGrado4(entry1) && esDeGrado4(entry2) && theyAreNotNeighbors(entry1,entry2) && noDirectPathBetween(entry1,entry2);
	}

	private boolean nodosDistintos(
			Map.Entry<Long, LinkedList<AdyacencyInfo>> entry1,
			Map.Entry<Long, LinkedList<AdyacencyInfo>> entry2) {
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

	private void ordenarResultado(HashSet<Long> res,
			Map.Entry<Long, LinkedList<AdyacencyInfo>> entry1,
			Map.Entry<Long, LinkedList<AdyacencyInfo>> entry2, 
			long[] resultado) {
		
		resultado[0]= entry1.getKey();
		resultado[2]= entry2.getKey();
		
		Iterator<Long> iter= res.iterator();
		int i=0;
		while(iter.hasNext()){
			long elem= iter.next();
			resultado[2*i+1]= elem;
			i++;
		}
	}

	private void addAdyacents(LinkedList<AdyacencyInfo>[] pathsNode,LinkedList<Long>[] visitedNodes,
								Map<Long,Integer> distancesToNode,
								Map.Entry<Long, LinkedList<AdyacencyInfo>> entry) {

		AdyacencyInfo ady;
		//Agrego los primeros adyacentes al nodo1
		for(int i=0; i < entry.getValue().size(); i++){
			ady= entry.getValue().get(i);
			pathsNode[i].add(ady);
			visitedNodes[i].add(ady.getAdyId());
			//visitedNodes.add(ady.getAdyId());
			distancesToNode.put(ady.getAdyId(),1); //distanci 1 al nodo
		}
	}

	
	private int buscarDimensionMaximaEn(LinkedList<AdyacencyInfo>[] pathsNode) {
		// Busca la maxima dimension en el pathnodes
		int maxDim= pathsNode[0].size();
		
		for(int i=1; i < pathsNode.length; i++){
			if(pathsNode[i].size() > maxDim)
					maxDim= pathsNode[i].size();
		}
		
		return maxDim;
	}

	private void initializePaths(LinkedList<AdyacencyInfo>[] pathsNode1,
			LinkedList<AdyacencyInfo>[] pathsNode2) {
		for(int i=0;i < 4; i++){
			pathsNode1[i]= new LinkedList<AdyacencyInfo>();
			pathsNode2[i]= new LinkedList<AdyacencyInfo>();
		}
	}

	private boolean noDirectPathBetween(Entry<Long, LinkedList<AdyacencyInfo>> entry1,Entry<Long, LinkedList<AdyacencyInfo>> entry2) {
		// Se chequea que entry1 y entry2 no esten sobre una misma dirección (calle)
		LinkedList<AdyacencyInfo> ady1,ady2;
		Set streetNames1,streetNames2, namesIntersection;
		
		streetNames1= new HashSet<String>();
		streetNames2= new HashSet<String>();
		
		
		ady1= entry1.getValue();
		ady2= entry2.getValue();
		
		getStreetNames(ady1, streetNames1);
		getStreetNames(ady2,streetNames2);
		
		//Check for intersection
		namesIntersection= new HashSet<String>(streetNames1);
		namesIntersection.retainAll(streetNames2);
		
		return namesIntersection.isEmpty();
		
	}

	private void getStreetNames(LinkedList<AdyacencyInfo> ady, Set streetNames) {
		for(int i=0;i < ady.size();i++)
			streetNames.add(ady.get(i).getName());
	}

	private boolean theyAreNotNeighbors(Entry<Long, LinkedList<AdyacencyInfo>> entry1,Entry<Long, LinkedList<AdyacencyInfo>> entry2) {
		// Metodo que devuelve true si entry1 y entry2 no son adyacentes entre si. 
		LinkedList<AdyacencyInfo> adyacents1;
		boolean res= false;
		
		adyacents1= entry1.getValue();
		//iterate over entry1 adyacents
		for(int i=0;(i < adyacents1.size()) && !res;i++){
			//Si hay un adyacente de entry1 que tenga el mismo id que entry2 (entonces serian vecinos)
			res= (adyacents1.get(i).getAdyId() == entry2.getKey());
		}
		
		return !res;
		
	}

	private boolean esDeGrado4(Map.Entry<Long,LinkedList<AdyacencyInfo>> entry1) {
		return entry1.getValue().size()==4;
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
		//cantidad y se agraga dichas referencias al resultado
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
							//Camino 1, Camino 2 (caminos de nodo 1 y 2 que se intersecan en visitNode)
						}	 
				 }
			}	
			
		}
		
		return new Pair(resRef,resCountIntersect);
		
	}

	
	
	
	private boolean puedaAvanzarEnAlgunaDir(LinkedList<AdyacencyInfo>[] pathsNode1, LinkedList<AdyacencyInfo>[] pathsNode2, 
											Set res, LinkedList<Long>[] visitedNodes1, LinkedList<Long>[] visitedNodes2,
											Map distancesToNode1, Map distancesToNode2) {
		// Método que indica si es posible encotrar en cada ultimo elemento de cada camino un adyacente en su misma direccion (nombre de calle)
		//Si es posible avanza
		boolean res1, res2;
		res1= avanzarCaminosNodo(pathsNode1, res, visitedNodes1,distancesToNode1);
		res2= avanzarCaminosNodo(pathsNode2, res, visitedNodes2, distancesToNode2);
		return res1 && res2;
		
	}

	private boolean avanzarCaminosNodo(LinkedList<AdyacencyInfo>[] pathsNode, 
										Set res, LinkedList<Long>[] visitedNodes, 
										Map distancesToNode) {

		//Método encargado de avanzar (si es posible) un nodo en la misa dirección de cada camino del array pathNodes
		String nameStreet;
		Long key_last;
		int dist;
		boolean puedeAvanzar= false;
		AdyacencyInfo temp_last, ady;
		
		
		for(int i=0;i < pathsNode.length;i++){
			temp_last= pathsNode[i].getLast();
			nameStreet= temp_last.getName();
			//Buscar adyacentes de temp_last enla misma direccion
			key_last= temp_last.getAdyId();
			
			ady = buscarAdyacenteConDireccion(key_last,nameStreet, res, visitedNodes[i]); 
			dist= (int) distancesToNode.get(key_last); //distancia desde el nodo a ultimo nodo (key_last)
			
			//si encuentro adyacente en la misma direccion
			if(ady != null){
				pathsNode[i].add(ady);
				distancesToNode.put(ady.getAdyId(),dist+1); //nueva distancia al adyacente (dist + 1)
				puedeAvanzar= true;
			}
			
		}
		
		return puedeAvanzar;
	}

	
	private AdyacencyInfo buscarAdyacenteConDireccion(Long key_last,String nameStreet, Set resList, LinkedList i_visitedNodes) {
		//Metodo que se encarga de buscar entre todos los adyacentes al nodo key_last aquel con mismo nombre 
		boolean found= false;
		LinkedList<AdyacencyInfo> adyacents;
		AdyacencyInfo res= null;
		AdyacencyInfo ady_temp;
		adyacents= adyLst.get(key_last);
		
		if(nameStreet!=null){
			int i= 0;
			while(i < adyacents.size() && !found){
				ady_temp= adyacents.get(i);
				
				if((ady_temp.getName() != null) && (ady_temp.getName().equals(nameStreet)) && (!i_visitedNodes.contains(ady_temp.getAdyId())) &&
																(!resList.contains(ady_temp.getAdyId()))){
					res= adyacents.get(i);
					i_visitedNodes.add(res.getAdyId()); //se agrega a la lista de nodos visitados en esa dirección
					found= true;
				}
				i++;
			}
		}
		return res;
	}
	
	
	public LinkedList<LinkedList<Long>> getPolygons(){
		return polygons;
	}
	
	
	}


