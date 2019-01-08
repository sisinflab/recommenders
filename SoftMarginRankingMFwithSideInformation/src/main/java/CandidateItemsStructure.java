public class CandidateItemsStructure implements Comparable<CandidateItemsStructure>{

    int item;
    float similarity;

    public CandidateItemsStructure(int item, float similarity) {
        this.item = item;
        this.similarity = similarity;

    }

    public int getItem() {
        return item;
    }

    public float getSimilarity(){
        return similarity;
    }


    @Override
    public int compareTo(CandidateItemsStructure t) {
        if (t == null) {
            return Integer.MAX_VALUE;
        }
        if (t.getSimilarity() < this.getSimilarity()) {
            return -1;
        }
        else if(t.getSimilarity() == this.getSimilarity()){
            return 0;
        }
        else {
            return 1;
        }
    }


}