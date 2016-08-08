import java.io.Serializable;

public class DirectedEdge {
	 
	private final GraphNode startNode;
    private final GraphNode endNode;
    private double length;
    private boolean isOneway;
    private String type;
    private String name;
    
    //private float weight;
    //private long way_id; 

    /*
     	Constructor
    */
	
    public DirectedEdge(GraphNode startNode, GraphNode endNode, double length, boolean isOneway,
    		String type, String name){//,  long way_id ) {
        
        this.startNode = startNode;
        this.endNode = endNode;
        this.isOneway = isOneway;
        this.length = length;
        this.type = type;
        this.name = name;
        //this.way_id = way_id;
    }
    
    public DirectedEdge(GraphNode startNode, GraphNode endNode,
    		long way_id, float weight, String name,  boolean isOneway ) {
        
        this.startNode = startNode;
        this.endNode = endNode;
        this.isOneway = isOneway;
        this.length = length;
        this.type = null;
        this.name = name;
        //this.weight = weight;
        //this.way_id = way_id;
    }
    
	public DirectedEdge() {
		
        this.startNode = null;
        this.endNode = null;
        this.isOneway = false;
        this.length = 0.00;
        this.type = null;
        this.name = null;
        //this.weight = 0;
        //this.way_id = 0;
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
    
    /*public void setWayId(long way_id){
		this.way_id = way_id;
	}*/
    /*public long getWayId(){
		return this.way_id;
	}*/
    
    public void setName(String name) {
		this.name = name;
	}
            
    public double getWeight(){
    	//return (this.weight*60);
    	return (this.length);
	}
    
    public String toString() {
        return startNode.getId()  + "-&gt;" + endNode.getId() + " " + length;
    }
 
	public String getName() {
		// TODO Auto-generated method stub
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
	    //hash = hash+ Math.round(weight*1000);
	    return hash;
	  }
}
