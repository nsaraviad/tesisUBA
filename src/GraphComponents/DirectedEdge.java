package GraphComponents;

public class DirectedEdge {
	 
	private final GraphNode startNode;
    private final GraphNode endNode;
    private double length;
    private boolean isOneway;
    private String type;
    private String name;
    
    	
    public DirectedEdge(GraphNode startNode, GraphNode endNode, double length, boolean isOneway,
    		String type, String name){//,  long way_id ) {
        
        this.startNode = startNode;
        this.endNode = endNode;
        this.isOneway = isOneway;
        this.length = length;
        this.type = type;
        this.name = name;
    
    }
    
    public DirectedEdge(GraphNode startNode, GraphNode endNode,
    		long way_id, float weight, String name,  boolean isOneway ) {
        
        this.startNode = startNode;
        this.endNode = endNode;
        this.isOneway = isOneway;
        this.length = length;
        this.type = null;
        this.name = name;
    
    }
    
	public DirectedEdge() {
		
        this.startNode = null;
        this.endNode = null;
        this.isOneway = false;
        this.length = 0.00;
        this.type = null;
        this.name = null;
	}
	
    public GraphNode from() {
        return startNode;
    }
 
 
    public GraphNode to() {
        return endNode;
    }
 
    public double getLength() {
        return length;
    }
    
    public boolean isOneway() {
        return isOneway;
    }
    
    public void setType(String type){
		this.type = type;
	}
    public String getType(){
		return type;
	}
    
    public void setName(String name) {
		this.name = name;
	}
            
    public double getWeight(){
    	return (this.length);
	}
    
    public String toString() {
        return startNode.getId()  + "-&gt;" + endNode.getId() + " " + length;
    }
 
	public String getName() {
		return name;
	}
	
	public float getWalkWeight(){
		float walk_weight = (float) ((this.length/3)*60);
		return walk_weight;
	}
	
	@Override 
	public int hashCode() { 
	    int hash = 1;
	    hash = hash+startNode.hashCode();
	    hash = hash+endNode.hashCode();
	    return hash;
	  }
}
