import java.util.*;
import java.util.stream.IntStream;

public class UserSimilarityObject {
    /** Rating Map           */ private HashMap<Integer, HashMap<Integer, AbstractMap.SimpleEntry<Float, Long>>> ratingsMap;
    /** Similarity Matrix    */ private Float[][] simMatrix;
    /** Neighbors Map        */ private Map<Integer, ArrayList<AbstractMap.SimpleEntry<Integer, Float>>> neighborsMap = new HashMap<>();
    /** Number of Users      */ private int nUsers;
    /** Users Map            */ private Map<Integer, Integer> Publicusers = new HashMap<>();
    /** Items Map            */ private Map<Integer, Integer> Publicitems = new HashMap<>();
    /** Users Map            */ private Map<Integer, Integer> Privateusers = new HashMap<>();
    /** Items Map            */ private Map<Integer, Integer> Privateitems = new HashMap<>();
    /** Number of Neighbors  */ private int numberOfNeighs;

    UserSimilarityObject(HashMap<Integer, HashMap<Integer, AbstractMap.SimpleEntry<Float, Long>>> ratingsMap, int numberOfNeighs){
        this.numberOfNeighs = numberOfNeighs;
        this.ratingsMap = ratingsMap;
        this.nUsers = ratingsMap.keySet().size();
        int i = 0;
        int k = 0;
        for (int user : ratingsMap.keySet()){
            for (int item : ratingsMap.get(user).keySet()){
                if (!Publicitems.containsKey(item)){
                    Publicitems.put(item,k);
                    Privateitems.put(k,item);
                    k++;
                }
            }
            Publicusers.put(user,i);
            Privateusers.put(i,user);
            i++;
        }
        this.simMatrix = new Float[nUsers][nUsers];

        Run();
    }


    private void Run(){
        ProcessBinaryCosineSimilarity();
        computeNeighbors();
    }

    private void computeNeighbors(){
        final int[] debug = {0};
        IntStream.range(0, simMatrix[0].length).parallel().mapToObj(i -> {
            ArrayList<AbstractMap.SimpleEntry<Integer, Float>> neighs = new ArrayList<>();
            IntStream.range(i+1, simMatrix[0].length).mapToObj(col -> new AbstractMap.SimpleEntry<>(Privateusers.get(col),simMatrix[i][col])).forEachOrdered(neighs::add);
            IntStream.range(0, i).mapToObj(row -> new AbstractMap.SimpleEntry<>(Privateusers.get(row),simMatrix[row][i])).forEachOrdered(neighs::add);
            Collections.sort(neighs,Collections.reverseOrder(Comparator.comparing(AbstractMap.SimpleEntry::getValue)));

            if (numberOfNeighs != -1){
                neighs = new ArrayList<>(neighs.subList(0,numberOfNeighs));
            }
            System.out.println(debug[0]);
            debug[0]++;
            return new AbstractMap.SimpleEntry<>(Privateusers.get(i),neighs);
        }).forEachOrdered(e -> neighborsMap.put(e.getKey(),e.getValue()));
    }




    private void ProcessBinaryCosineSimilarity(){
        final int[] debug = {0};
        IntStream.range(0, nUsers).parallel().forEach(i -> {
            System.out.println(debug[0]);
            debug[0]++;
            Map<Integer, AbstractMap.SimpleEntry<Float,Long>> user_i_map = ratingsMap.get(Privateusers.get(i));
            int finalI = i;
            IntStream.range(i+1, nUsers).forEach(item_j_index -> {
                simMatrix[finalI][item_j_index] = computeBinaryCosineSimilarity(user_i_map,ratingsMap.get(Privateusers.get(item_j_index)));
            });
        });
    }
    private float computeBinaryCosineSimilarity(Map<Integer, AbstractMap.SimpleEntry<Float, Long>> subject_i_map, Map<Integer, AbstractMap.SimpleEntry<Float, Long>> subject_j_map){
        HashSet<Integer> intersection = new HashSet<>(subject_i_map.keySet());
        intersection.retainAll(subject_j_map.keySet());
        float den = (float)(Math.sqrt(subject_i_map.size()*subject_j_map.size()));
        return (den!=0)?intersection.size()/den:0f;
    }

    public Map<Integer, ArrayList<AbstractMap.SimpleEntry<Integer, Float>>> getNeighborsMap() {
        return neighborsMap;
    }

    public ArrayList<AbstractMap.SimpleEntry<Integer, Float>> getNeighbors(int u) {
        return neighborsMap.get(u);
    }
}
