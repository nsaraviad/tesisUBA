package GraphComponents;


public class AdjacencyInfo{

	private final long adyacent_id;
	private final double lenght;
	private final boolean isOneWayInfo;
	private final String type;
	private final String name;
	
	
	public AdjacencyInfo(long ady_id, double lgt, boolean oneWay, String typ, String nm){
		adyacent_id= ady_id;
		lenght= lgt;
		isOneWayInfo= oneWay;
		type= typ;
		name= nm;
	}
	
	public long getAdjId() {return adyacent_id;}
	public double getLenght() {return lenght;}
	public boolean getOneWay() {return isOneWayInfo;}
	public String getType() {return type;}
	public String getName() {return name;}
	
	
}
