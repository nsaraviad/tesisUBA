import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;


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
					
					//Analizo las cuatro direcciones adyacentes a cada uno de los nodos
					LinkedList[] pathsNode1, pathsNode2;
					pathsNode1= new LinkedList[4];
					pathsNode2= new LinkedList[4];
					
					//Agrego los dos nodos a la solucion
					res.add(nodes.get(entry1.getKey()).getId());
					res.add(nodes.get(entry2.getKey()).getId());
					
					//Inicializo
					for(int i=0;i<4;i++){
						pathsNode1[i]= new LinkedList<AdyacencyInfo>();
						pathsNode2[i]= new LinkedList<AdyacencyInfo>();
					}
					
					//nodo1
					for(int i=0; i < entry1.getValue().size(); i++){
						pathsNode1[i].add(entry1.getValue().get(i));
					}
					
					//nodo2
					for(int i=0; i < entry2.getValue().size(); i++){
						pathsNode2[i].add(entry2.getValue().get(i));
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
					while((cantIntersecciones < 2) && puedaAvanzarEnAlgunaDir(pathsNode1,pathsNode2)){
						//avanzar un nodo en cada camino(que sea posible)
						//Verificar si encuentro en dos caminos distintos un mismo nodo (se agrega a res y se incrementa la cant de inters)
						verificarSiHayInterseccionesYAgregarRef(pathsNode1,pathsNode2,cantIntersecciones,res);
					}
					
				}
			
			}
		}
		
		}

	
	private void verificarSiHayInterseccionesYAgregarRef(LinkedList[] pathsNode1,LinkedList[] pathsNode2, 
															int cantIntersecciones, LinkedList res) {
		//Metodo encargado de chequear si hay nodos compartidos entre caminos de path1 y path2. En el caso de haber se incrementa la 
		//cantidad y se agraga dichas referencias al resultado
		
		
		//Obtener un arreglo con ultimos nodos de caminos de pathnodes1= 1
		//Obtener un arreglo con ultimos nodos de caminos de pathnodes2= 2
		
		//para cada uno de los nodos de 1, verificar si esta incluido en 2
		//si lo esta, cant++ y agregar a res.
		
	}

	
	
	
	private boolean puedaAvanzarEnAlgunaDir(LinkedList<AdyacencyInfo>[] pathsNode1, LinkedList<AdyacencyInfo>[] pathsNode2) {
		// Método que indica si es posible encotrar en cada ultimo elemento de cada camino un adyacente en su misma direccion (nombre de calle)
		//Si es posible avanza
		String nameStreet;
		Long key_last;
		boolean puedeAvanzar= false;
		AdyacencyInfo temp_last, ady;
		
		
		for(int i=0;i < pathsNode1.length;i++){
			temp_last= pathsNode1[i].getLast();
			nameStreet= temp_last.getName();
			//Buscar adyacentes de temp_last enla misma direccion
			key_last= temp_last.getAdyId();
			
			ady = buscarAdyacenteConDireccion(key_last,nameStreet); 
			
			//si encuentro adyacente en la misma direccion
			if(ady != null){
				pathsNode1[i].addLast(ady);
				puedeAvanzar= true;
			}
			
		}
		
		return puedeAvanzar;
	}

	private AdyacencyInfo buscarAdyacenteConDireccion(Long key_last,String nameStreet) {
		//Metodo que se encarga de buscar entre todos los adyacentes al nodo key_last aquel con mismo nombre 
		boolean found= false;
		LinkedList<AdyacencyInfo> adyacents;
		AdyacencyInfo res= null;
		adyacents= adyLst.get(key_last);
		
		int i= 0;
		while(i < adyacents.size() && !found){
			if(adyacents.get(i).getName().equals(nameStreet)){
				res= adyacents.get(i);
				found= true;
			}
		}
		
		return res;
	}
	
	
	
	}


