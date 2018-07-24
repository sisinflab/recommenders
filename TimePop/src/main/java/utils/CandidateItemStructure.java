package utils;

public class CandidateItemStructure implements Comparable<CandidateItemStructure> {

	int itemId;
	int occurrences;
	

	public CandidateItemStructure(int itemId, int occurrences) {
		this.itemId = itemId;
		this.occurrences = occurrences;

	}
	
	public int getItemId() {
		return itemId; 
	}

	public int getOccurrences(){
		return occurrences;
	}
	
	
	

	public int compareTo(CandidateItemStructure t) {
		if (t == null) {
			return Integer.MAX_VALUE;
		}
		if (t.getOccurrences() < this.getOccurrences()) {
			return -1;
		} 
		else if(t.getOccurrences() == this.getOccurrences()){
			return 0;
		}
		else {
			return 1;
		}
	}
	
	

}