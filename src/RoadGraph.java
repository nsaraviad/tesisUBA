import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class RoadGraph {

	public LinkedList<GraphNode> nodes;
	public LinkedList<DirectedEdge> edges;
	private LinkedList<Long> refBound;
	public LinkedList<GraphNode> nodesBoundary;
	
	public RoadGraph(){
			
		nodes = new LinkedList<GraphNode>();
		edges = new LinkedList<DirectedEdge>();
		refBound = new LinkedList<Long>();
		nodesBoundary = new LinkedList<GraphNode>();
	}
	
	//Método de Parseo
	public boolean osmGraphParser(XmlPullParser xrp, String nameArchive) throws XmlPullParserException, IOException{
		/*Initialization of temporary variables */
		boolean ret = false;
		boolean isOsmData = false;
		boolean isBoundary = false;
		
		GraphNode tempNode = new GraphNode();					
		GraphNode NULL_NODE = new GraphNode();					
		GraphWay tempWay = new GraphWay();
		Long tempRef;
		LinkedList<Long> tempRefsWayBound = new LinkedList<Long>();
		GraphWay NULL_WAY = new GraphWay();						
		LinkedList<GraphNode> allNodes = new LinkedList<GraphNode>();	
		LinkedList<GraphWay> allWays = new LinkedList<GraphWay>();
		
	
		if(xrp == null){
			return ret;
		}

		xrp.next();
		int eventType = xrp.getEventType();
		
		/*Parseo xml*/
		while (eventType != XmlPullParser.END_DOCUMENT) {
			switch(eventType){
			case XmlPullParser.START_DOCUMENT:
				break;
			case XmlPullParser.START_TAG:
				/*Checking the format*/
				if(xrp.getName().equals("osm")){
					isOsmData = true;
				}else {
					int attributeCount = xrp.getAttributeCount();
					/*Extracting the nodes and values*/
					if(xrp.getName().equals("node")){
						/*The node values are temporarily stored in tempNode*/
						tempNode = new GraphNode();
						for(int i = 0; i < attributeCount; i++){
							if(xrp.getAttributeName(i).equals("id")){
								tempNode.setId(Long.parseLong(xrp.getAttributeValue(i)));			
							} if(xrp.getAttributeName(i).equals("lat")){
								tempNode.setLat(Double.parseDouble(xrp.getAttributeValue(i)));	
							} if(xrp.getAttributeName(i).equals("lon")){
								tempNode.setLon(Double.parseDouble(xrp.getAttributeValue(i)));	
							}
						}
					}
					/*Obteniendo atributos del camino*/
					else if(xrp.getName().equals("tag")){
						if(tempNode == NULL_NODE)	{
							for(int i = 0; i < attributeCount; i++){
								if(xrp.getAttributeName(i).equals("k")
										&& xrp.getAttributeValue(i).equals("highway")){		
									String v = xrp.getAttributeValue(i + 1);
									if(isnotFilteredWay(v))
										tempWay.setType(v);									
								} else if(xrp.getAttributeName(i).equals("k")
										&& xrp.getAttributeValue(i).equals("name")){
									if(xrp.getAttributeValue(i+1).equals(nameArchive.substring(0, nameArchive.lastIndexOf('.')))){
										if(isBoundary){		
											//Se guardan la/las referencia/s de caminos que forman la frontera
											setRefBoundary(tempRefsWayBound);
											isBoundary= false;
										}
									}else{
										String v = xrp.getAttributeValue(i + 1);
										tempWay.setName(v);
									}
								} else if(xrp.getAttributeName(i).equals("k")
										&& xrp.getAttributeValue(i).equals("oneway")){	
									String v = xrp.getAttributeValue(i + 1);
									tempWay.setOneway(isOneWay(v));
								} else if(xrp.getAttributeName(i).equals("k")
										&& xrp.getAttributeValue(i).equals("admin_level") 
										&& xrp.getAttributeValue(i+1).equals("8")){	
											if(tempRefsWayBound.size()>0)
													isBoundary= true;
								} 
							}						
						}
						
					}  /* OBTENIENDO ROADWAYS */
					else if(xrp.getName().equals("way")){							
						tempWay = new GraphWay();
						for(int i = 0; i < attributeCount; i++){
							if(xrp.getAttributeName(i).equals("id")){
									tempWay.setId(Long.parseLong(xrp.getAttributeValue(i)));
							}
						}	
					} else if(xrp.getName().equals("nd")){										
						for(int i = 0; i < attributeCount; i++){
							if(xrp.getAttributeName(i).equals("ref")){							
								String v = xrp.getAttributeValue(i);
								long ref = Long.parseLong(v);
								tempWay.addRef(ref);
							}
						}
					} else if(xrp.getName().equals("relation")){										
						tempRefsWayBound.clear();
				    }else if(xrp.getName().equals("member") && xrp.getAttributeValue(0).equals("way")){
				    	for(int i = 0; i < attributeCount; i++){
							if(xrp.getAttributeName(i).equals("ref")){							
								String v = xrp.getAttributeValue(i);
								long ref = Long.parseLong(v);
								tempRefsWayBound.add(ref);
							}
						}
				    }
				}
				break;
			
			case XmlPullParser.END_TAG:
				if(isOsmData){
					if(xrp.getName().equals("osm")){
						ret = true;
					} else if(xrp.getName().equals("node")){						
						allNodes.add(tempNode);
						tempNode = NULL_NODE;		
					} else if(xrp.getName().equals("way")){							
						allWays.add(tempWay);
						tempWay = NULL_WAY;
					} 
				}
				break;
			}
			eventType = xrp.next();
		}
		/*RELACIONES NODO-EJES*/
		LinkedList<GraphWay> remainingWays = new LinkedList<GraphWay>();
		for(GraphWay way : allWays){	
			LinkedList<Long> refs = way.getRefs();
			boolean stop = false;
			for(Long ref : refs){							
				for(GraphNode node : allNodes){
					if(node.getId() == ref){
						remainingWays.add(way);
						stop = true;							
					}
					if(stop)
						break;
				}
				if(stop)
					break;
			}
		}
		if(remainingWays.size() == 0)	
			return false;
		
		for(GraphWay way : remainingWays){
			
			
			//Se descartan aquellos caminos no útiles para ser transitados. (agua, vías, etc)
			if(way.getType() != null){
			
				GraphNode firstNode = getNode(allNodes,(long) way.getRefs().get(0));
				for(int i = 1; i <= way.getRefs().size() - 1; i++){
					GraphNode nextNode = getNode(allNodes,(long) way.getRefs().get(i));
					double len = getDistance(firstNode.getLat(),firstNode.getLon(),
							nextNode.getLat(),nextNode.getLon());
	
										
					DirectedEdge tempEdge = new DirectedEdge(firstNode, nextNode,
							len,way.getOneway(),way.getType(),
							way.getName(),way.getId());
					
					edges.add(tempEdge);
	
					if(!nodes.contains(firstNode)){
						nodes.add(firstNode);							
					}
					firstNode = nextNode;
				}
	
				if(!nodes.contains(firstNode)){
					nodes.add(firstNode);										
				}
			}
			
		}
		
		//EXTRAER LOS NODOS PARA ARMAR LA ZONA FRONTERA (en nodesBoundary)
		getBoundary(allNodes, allWays);
		
		System.out.println("Boundary = "+ this.nodesBoundary.size());
		
		return ret;
	}

	private void getBoundary(LinkedList<GraphNode> allNodes,LinkedList<GraphWay> allWays) {
		GraphNode tempNode;
		GraphWay tempWay;
		Long tempRef;
		
		for(int k=0; k < refBound.size(); k++){
			tempRef = refBound.get(k);
			tempWay= getWayWithReference(allWays,tempRef); //me taigo el way con id= ref
			
			//Si se encontro camino
			if(tempWay != null){
				for(int j=tempWay.getRefs().size()-1; j >= 0; j--){
					tempRef = (Long) tempWay.getRefs().get(j);
					tempNode = getNodeWithReference(allNodes,tempRef);
					nodesBoundary.add(tempNode);
				}
				
			}
		}
	}
	
	
	//Devuelve el nodeo con id especificado. Si no lo encuentra devuelve null
	private GraphNode getNodeWithReference(LinkedList<GraphNode> nodes,Long ref) {

		//Sort lists
		Collections.sort(nodes, new NodeComparator());

		//Binary search
		BinSearch bs = new BinSearch();
		int index = bs.binSearchOverNodes(nodes,ref);
		
		return nodes.get(index);
	}
	
	
	//Devuelve el camino con id especificado. Si no lo encuentra devuelve null
	private GraphWay getWayWithReference(LinkedList<GraphWay> ways,Long ref) {
		
		//Sort list
		Collections.sort(ways, new WayComparator());
		
		//Binary search
		BinSearch bs= new BinSearch();
		int index = bs.binSearchOverWays(ways,ref);
		
		return ways.get(index);
	}

	
	private boolean isnotFilteredWay(String v) {
		// Metodo encargado de filtrar highways no considerables
		return (!v.equals("footway") && (!v.equals("service")) && (!v.equals("pedestrian")) && (!v.equals("raceway")) 
				&& (!v.equals("bridleway") && (!v.equals("steps")) && (!v.equals("path")) && (!v.equals("cycleway"))));
	}

	private boolean isOneWay(String v) {
		
		if(v.equals("yes")){
			return true;
		}else{
			return false;
		}
	}

	//Clase auxiliar para guardar informacion de ciertos atributos de las rutas (si es sentido unico)
	private OtherTags parseOtherTags(String v) {
		String[] other_tags = v.split(",");
		OtherTags output = new OtherTags();

		for(int i =0; i< other_tags.length;i++){
			Pattern p = Pattern.compile("\"([^\"]*)\"");
			Matcher m = p.matcher(other_tags[i]);
			int flag =0;
			while (m.find()) {
				if(m.group(1).equals("oneway")){
					flag = 1;
				}
				else{

					if(flag ==1){
						if(m.group(1).equals("yes"))
							output.isOneWay = true;
						else
							output.isOneWay = false;
						flag =0;
					}
					}
			}
		}

		return output;
	}
	
	// This is the slower version which is used during parsing
	
	private GraphNode getNode(LinkedList<GraphNode> list, long id){
		for(GraphNode node: list){
			if(node.getId() == id)
				return node;
		}
		return null;
	}
	
	
	/**
	 * Retorna la distancia entre dos puntos en Km dada Latitud/Longitud
	 * @param lat_1atitud del primer punto.
	 * @param lat_2 1atitud del segundo punto.
	 * @param lon_1 longitud del primer punto.
	 * @param lon_2 longitud del segundo punto.
	 * @return distancia en metros entre los dos puntos.
	 */
	public double getDistance(double lat_1, double lon_1, double lat_2, double lon_2) {
		double dLon = lon_2 - lon_1;
		double dLat = lat_2 - lat_1;
		lat_1 = Math.toRadians(lat_1);
		lon_1 = Math.toRadians(lon_1);
		lat_2 = Math.toRadians(lat_2);
		lon_2 = Math.toRadians(lon_2);
		dLon = Math.toRadians(dLon);
		dLat = Math.toRadians(dLat);

		double r = 6378137; // km
		double a = Math.sin(dLat/2)*Math.sin(dLat/2) + 
				Math.cos(lat_1)*Math.cos(lat_2) *
				Math.sin(dLon/2)*Math.sin(dLon/2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		return c*r;
	}
	

	public LinkedList<Long> getRefBoundary(){
		return refBound;
	}
	
	public void setRefBoundary(LinkedList<Long> l){
		refBound.addAll(l);
	}
	
	/*Inner class defined to store the road attributes temporarily*/
	class OtherTags
	{
		private boolean isOneWay;
	
		OtherTags(boolean isOneWay, int maxspeed)
		{
			this.isOneWay = isOneWay;
		}
		OtherTags()
		{
			this.isOneWay = false;
		}
	}

	public LinkedList<GraphNode> getNodesBoundary() {
		
		return nodesBoundary;
	}

}