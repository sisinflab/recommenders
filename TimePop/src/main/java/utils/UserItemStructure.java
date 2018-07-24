package utils;

public class UserItemStructure implements Comparable<UserItemStructure> {

	int userItemId;
	long timestamp;
	

	public UserItemStructure(int userId,long timestamp) {
		this.timestamp = timestamp;
		this.userItemId = userId; 

	}
	
	public long getTimestamp() {
		return timestamp;
	}

	public int getUserItem(){
		return userItemId;
	}
	
	
	

	public int compareTo(UserItemStructure t) {
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
	
	

}