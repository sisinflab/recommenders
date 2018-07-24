package utils;


public class RatingStructure implements Comparable<RatingStructure> {

	long timestamp;
	float rating;

	public RatingStructure(long timestamp, float rating) {
		this.timestamp = timestamp;
		this.rating = rating;

	}
	
	public long getTimestamp() {
		return timestamp;
	} 

	public float getRating(){
		return rating;
	}
	
	
	
	@Override
	public int compareTo(RatingStructure t) {
		if (t == null) {
			return Integer.MIN_VALUE;
		}
		if (t.getTimestamp() > this.getTimestamp()) {
			return -1;
		} 
		else if(t.getTimestamp() == this.getTimestamp()){
			return 0;
		}
		else {
			return 1;
		}
	}
	
	@Override
	public String toString() {
		String s =this.rating + "," + this.timestamp ; 
		return s;
	}

}