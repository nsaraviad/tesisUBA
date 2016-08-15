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
	
	
	//Constructor
	public PolygonsGenerator(ParseOSM g) {
		this.rg= g.getRoadGraph();
		this.nodes= rg.getNodes();
		this.adyLst= rg.getAdyLst();
	}
	
	//Método encargado de generar todos los polígonos a ser considerados
	public void generatePolygons(){
		int cantIntersecciones;
		LinkedList<Long> res= new LinkedList<Long>();
				
		for(Iterator<Entry<Long,LinkedList<AdyacencyInfo>>> it_node_1= adyLst.entrySet().iterator();
																					it_node_1.hasNext();){
			
			Map.Entry<Long,LinkedList<AdyacencyInfo>> entry1= it_node_1.next();
			
			//itero sobre lso demas nodos
			for(Iterator<Entry<Long,LinkedList<AdyacencyInfo>>> it_node_2=adyLst.entrySet().iterator();
																					it_node_2.hasNext();){
				Map.Entry<Long,LinkedList<AdyacencyInfo>> entry2= it_node_2.next();
				
				cantIntersecciones= 0;
				
				//Se verifica que (entry1,entry2) con entry1 != entry2 y que el grado(entry1)=grado(entry2)=4
				if((entry1.getKey() != entry2.getKey()) && 
					entry1.getValue().size()==4 &&
					entry2.getValue().size()==4){
					
					LinkedList<AdyacencyInfo>[] pathsNode1;
					LinkedList<AdyacencyInfo>[] pathsNode2;
					
					LinkedList<Long> visitedNodes;
					//Analizo las cuatro direcciones adyacentes a cada uno de los nodos
					
					pathsNode1= new LinkedList[4];
					pathsNode2= new LinkedList[4];
					visitedNodes= new LinkedList<Long>();
					
					for(int i=0;i < 4; i++){
						pathsNode1[i]= new LinkedList<AdyacencyInfo>();
						pathsNode2[i]= new LinkedList<AdyacencyInfo>();
					}
					
					
					//Agrego los dos nodos a la solucion
					res.add(nodes.get(entry1.getKey()).getId());
					res.add(nodes.get(entry2.getKey()).getId());
					
					visitedNodes.add(nodes.get(entry1.getKey()).getId());
					visitedNodes.add(nodes.get(entry2.getKey()).getId());
					
					
					//nodo1
					for(int i=0; i < entry1.getValue().size(); i++){
						pathsNode1[i].add(entry1.getValue().get(i));
						visitedNodes.add(entry1.getValue().get(i).getAdyId());
					}
					
					//nodo2
					for(int i=0; i < entry2.getValue().size(); i++){
						pathsNode2[i].add(entry2.getValue().get(i));
						visitedNodes.add(entry2.getValue().get(i).getAdyId());
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
					while((cantIntersecciones < 2) && puedaAvanzarEnAlgunaDir(pathsNode1,pathsNode2,res,visitedNodes)){
						//avanzar un nodo en cada camino(que sea posible)
						//Verificar si encuentro en dos caminos distintos un mismo nodo (se agrega a res y se incrementa la cant de inters)
						
						LinkedList p1= (LinkedList) verificarSiHayInterseccionesYAgregarRef(pathsNode1,pathsNode2).getFirst();
						int p2= (int) verificarSiHayInterseccionesYAgregarRef(pathsNode1,pathsNode2).getSecond();
						
						Pair ret = new Pair(p1,p2);
						
						cantIntersecciones= cantIntersecciones + (int)ret.getSecond();
						res.addAll((LinkedList<Long>) ret.getFirst());
					}
					
				}
			
			}
		}
		
	}

	
	private Pair verificarSiHayInterseccionesYAgregarRef(LinkedList<AdyacencyInfo>[] pathsNode1,LinkedList<AdyacencyInfo>[] pathsNode2) {
		//Metodo encargado de chequear si hay nodos compartidos entre caminos de path1 y path2. En el caso de haber se incrementa la 
		//cantidad y se agraga dichas referencias al resultado
		LinkedList<Long> resRef= new LinkedList();
		int resCountIntersect= 0;
		Set<Long> lastNodesPath1, lastNodesPath2;
		lastNodesPath1= new HashSet<Long>();
		lastNodesPath2= new HashSet<Long>();
		
		//Obtener un arreglo con ultimos nodos de caminos de pathnodes1= 1
		//Obtener un arreglo con ultimos nodos de caminos de pathnodes2= 2
		for(int i=0;i < 4;i++){
			if(!pathsNode1[i].isEmpty())
				lastNodesPath1.add(pathsNode1[i].getLast().getAdyId());
			if(!pathsNode2[i].isEmpty())
				lastNodesPath2.add(pathsNode2[i].getLast().getAdyId());
		}
		
		Iterator it= lastNodesPath1.iterator();
		Long elem;
		//para cada uno de los nodos de 1, verificar si esta incluido en 2
		//si lo esta, cant++ y agregar a res.
		while(it.hasNext()){
			elem= (Long) it.next();
			if(lastNodesPath2.contains(elem)){
				resCountIntersect++;
				resRef.add(elem);
			}				
		}
		return new Pair(resRef,resCountIntersect);
		
	}

	
	
	
	private boolean puedaAvanzarEnAlgunaDir(LinkedList<AdyacencyInfo>[] pathsNode1, LinkedList<AdyacencyInfo>[] pathsNode2, 
											LinkedList res, LinkedList visitedNodes) {
		// Método que indica si es posible encotrar en cada ultimo elemento de cada camino un adyacente en su misma direccion (nombre de calle)
		//Si es posible avanza
		boolean res1, res2;
		res1= avanzarCaminosNodo(pathsNode1, res, visitedNodes);
		res2= avanzarCaminosNodo(pathsNode2, res, visitedNodes);
		return res1 && res2;
		
	}

	//Método encargado de avanzar (si es posible) un nodo en la misa direcion de cada camino del array pathNodes
	private boolean avanzarCaminosNodo(LinkedList<AdyacencyInfo>[] pathsNode, LinkedList res, LinkedList visitedNodes) {
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

	
	private AdyacencyInfo buscarAdyacenteConDireccion(Long key_last,String nameStreet, LinkedList resList, LinkedList visitedNodes) {
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
				if((ady_temp.getName().equals(nameStreet)) && (!visitedNodes.contains(ady_temp)) &&
																(!resList.contains(ady_temp.getAdyId()))){
					res= adyacents.get(i);
					found= true;
				}
				i++;
			}
		}
		return res;
	}
	
	
	
	}


