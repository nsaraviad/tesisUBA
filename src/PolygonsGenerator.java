import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;


public class PolygonsGenerator {
	
	//Miembros
	private RoadGraph rg;
	private  HashMap<Long,GraphNode> nodes;
	private Map<Long,LinkedList<AdyacencyInfo>> adyLst;
	private LinkedList<HashSet<Long>> polygons;
	
	//Constructor
	public PolygonsGenerator(ParseOSM g) {
		this.rg= g.getRoadGraph();
		this.nodes= rg.getNodes();
		this.adyLst= rg.getAdyLst();
		this.polygons= new LinkedList();
	}
	
	//Método encargado de generar todos los polígonos a ser considerados
	public void generatePolygons(){
		
		//Variables
		HashSet<Long> res= new HashSet<Long>();
		int cantIntersecciones, p2;
		LinkedList<AdyacencyInfo>[] pathsNode1, pathsNode2;
		LinkedList<Long> visitedNodes1, visitedNodes2, p1;
	
		
		//Inicializo
		pathsNode1= new LinkedList[4];
		pathsNode2= new LinkedList[4];
		
		visitedNodes1= new LinkedList<Long>();
		visitedNodes2= new LinkedList<Long>();
		
		for(int i=0;i < 4; i++){
			pathsNode1[i]= new LinkedList<AdyacencyInfo>();
			pathsNode2[i]= new LinkedList<AdyacencyInfo>();
		}
		
		
		
		
		//Algoritmo busqueda de polygonos
		for(Iterator<Entry<Long,LinkedList<AdyacencyInfo>>> it_node_1= adyLst.entrySet().iterator();
																					it_node_1.hasNext();){
			
			Map.Entry<Long,LinkedList<AdyacencyInfo>> entry1= it_node_1.next();
			
			//itero sobre los demas nodos
			for(Iterator<Entry<Long,LinkedList<AdyacencyInfo>>> it_node_2=adyLst.entrySet().iterator();
																					it_node_2.hasNext();){
				Map.Entry<Long,LinkedList<AdyacencyInfo>> entry2= it_node_2.next();
				
				//reseteo variables
				cantIntersecciones= 0;
				clearLists(pathsNode1, pathsNode2);
				res.clear();
				visitedNodes1.clear();
				visitedNodes2.clear();
				
				//Se verifica que (entry1,entry2) con entry1 != entry2 y que el grado(entry1)=grado(entry2)=4
				//y ademas no tiene que estar en la misma calle
				if((entry1.getKey() != entry2.getKey()) && esDeGrado4(entry1) && esDeGrado4(entry2)){
					
					//Agrego los dos nodos iniciales a la solución
					res.add(nodes.get(entry1.getKey()).getId());
					res.add(nodes.get(entry2.getKey()).getId());
					
					//Los agrego a los nodos visitados
					visitedNodes1.add(nodes.get(entry1.getKey()).getId());
					visitedNodes2.add(nodes.get(entry2.getKey()).getId());
					
					
					//Agrego los primeros adyacentes al nodo1
					for(int i=0; i < entry1.getValue().size(); i++){
						pathsNode1[i].add(entry1.getValue().get(i));
						visitedNodes1.add(entry1.getValue().get(i).getAdyId());
					}
					
					//Agrego los primeros adyacentes al nodo2
					for(int i=0; i < entry2.getValue().size(); i++){
						pathsNode2[i].add(entry2.getValue().get(i));
						visitedNodes2.add(entry2.getValue().get(i).getAdyId());
					}
					
					//Se chequea si hay intersecciones iniciales
					for(int i=0;i<entry1.getValue().size();i++){
						if(entry2.getValue().contains(entry1.getValue().get(i))){
							cantIntersecciones++;
							res.add(entry1.getValue().get(i).getAdyId());
						}
					}
					
					
					//Avanzar un nodo por cada camino si no se armo el poligono y mientras pueda
					//seguir avanzando
					while((cantIntersecciones < 2) && puedaAvanzarEnAlgunaDir(pathsNode1,pathsNode2,res,visitedNodes1,visitedNodes2)){
						
						p1=(LinkedList<Long>) verificarSiHayInterseccionesYAgregarRef(visitedNodes1,visitedNodes2).getFirst();
						p2= (int) verificarSiHayInterseccionesYAgregarRef(visitedNodes1,visitedNodes2).getSecond();
						
						Pair ret = new Pair(p1,p2);
						
						cantIntersecciones= (int)ret.getSecond();
						res.addAll((LinkedList<Long>) ret.getFirst());
					}
					//Se agrega a la lista de poligonos obtenidos
					if(cantIntersecciones >= 2)
						polygons.add(new HashSet<Long>(res));
				}
			}
		}
	}

	private boolean esDeGrado4(Map.Entry<Long,LinkedList<AdyacencyInfo>> entry1) {
		return entry1.getValue().size()==4;
	}

	//Método encargado de limpiar los caminos en cada nueva iteración
	private void clearLists(LinkedList<AdyacencyInfo>[] pathsNode1,LinkedList<AdyacencyInfo>[] pathsNode2) {
		for(int i=0; i < 4;i++){
			pathsNode1[i].clear();
			pathsNode2[i].clear();
		}
	}

	
	private Pair verificarSiHayInterseccionesYAgregarRef(LinkedList<Long> visitedNode1,LinkedList<Long> visitedNode2) {
		//Metodo encargado de chequear si hay nodos compartidos entre caminos de path1 y path2. En el caso de haber se incrementa la 
		//cantidad y se agraga dichas referencias al resultado
		LinkedList<Long> resRef= new LinkedList();
		int resCountIntersect= 0;
		
		for(int i=0;i<visitedNode1.size();i++){
			if(visitedNode2.contains(visitedNode1.get(i)) && !resRef.contains(visitedNode1.get(i))){
				resCountIntersect++;
				resRef.add(visitedNode1.get(i));
			}
		}
		
		return new Pair(resRef,resCountIntersect);
		
	}

	
	
	
	private boolean puedaAvanzarEnAlgunaDir(LinkedList<AdyacencyInfo>[] pathsNode1, LinkedList<AdyacencyInfo>[] pathsNode2, 
											Set res, LinkedList visitedNodes1, LinkedList visitedNodes2) {
		// Método que indica si es posible encotrar en cada ultimo elemento de cada camino un adyacente en su misma direccion (nombre de calle)
		//Si es posible avanza
		boolean res1, res2;
		res1= avanzarCaminosNodo(pathsNode1, res, visitedNodes1);
		res2= avanzarCaminosNodo(pathsNode2, res, visitedNodes2);
		return res1 && res2;
		
	}

	//Método encargado de avanzar (si es posible) un nodo en la misa direcion de cada camino del array pathNodes
	private boolean avanzarCaminosNodo(LinkedList<AdyacencyInfo>[] pathsNode, Set res, LinkedList visitedNodes) {
		String nameStreet;
		Long key_last;
		boolean puedeAvanzar= false;
		AdyacencyInfo temp_last, ady;
		
		
		for(int i=0;i < pathsNode.length;i++){
			temp_last= pathsNode[i].getLast();
			nameStreet= temp_last.getName();
			//Buscar adyacentes de temp_last enla misma direccion
			key_last= temp_last.getAdyId();
			
			ady = buscarAdyacenteConDireccion(key_last,nameStreet, res, visitedNodes); 
			
			//si encuentro adyacente en la misma direccion
			if(ady != null){
				pathsNode[i].add(ady);
				puedeAvanzar= true;
			}
			
		}
		
		return puedeAvanzar;
	}

	
	private AdyacencyInfo buscarAdyacenteConDireccion(Long key_last,String nameStreet, Set resList, LinkedList visitedNodes) {
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
				
				if((ady_temp.getName() != null) && (ady_temp.getName().equals(nameStreet)) && (!visitedNodes.contains(ady_temp.getAdyId())) &&
																(!resList.contains(ady_temp.getAdyId()))){
					res= adyacents.get(i);
					visitedNodes.add(res.getAdyId());
					found= true;
				}
				i++;
			}
		}
		return res;
	}
	
	
	public LinkedList<HashSet<Long>> getPolygons(){
		return polygons;
	}
	
	
	}


