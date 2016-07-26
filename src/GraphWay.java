import java.util.LinkedList;
 
/**
 * Used for OSM parsing
 * 
 * Modified Class at : https://github.com/COMSYS/FootPath
 * 
 * @author Sandeep Sasidharan
 *
 */
public class GraphWay {
	// all nodes on this path ( ref0 -&gt; ref1 -&gt; ref2  -&gt; ...)
	private LinkedList refs;
	private long id;
    private String type;
    private String name;
    private boolean isOneway;
 
	public GraphWay() {
		this.refs = new LinkedList();
		this.id = 0;
		this.type = null;
		this.name = null;
	    this.isOneway = false;
	}
 
	public GraphWay(LinkedList refs, long id,boolean isOneway, String type, String name) {
		this.refs = refs;
		this.id = id;
	    this.isOneway = isOneway;
	    this.name = name;
	    this.type=type;
 	}
	
	public LinkedList getRefs() {
		return refs;
	}
	
	public long getId() {
		return id;
	}
	
	
	public void setRefs(LinkedList refs) {
		this.refs = refs;
	}
	
	public void setId(long l) {
		this.id = l;
	}
	
	
	public void addRef(long ref){
		this.refs.add(new Long(ref));
	}
    public void setType(String type){
		this.type = type;
	}
    
    public void setName(String name) {
		this.name = name;
	}
    
    public boolean getOneway() {
        return isOneway;
    }
    public String getType() {
        return type;
    }
    
    public void setOneway(boolean isOneway) {
        this.isOneway = isOneway;
    }
    public boolean isOneway() {
        return isOneway;
    }
    
 
    public String getName(){
    	return name;
    }

	
}