import java.util.*;
import java.util.stream.IntStream;

public class ItemSimilarityObject {
    /** Rating Map           */ private HashMap<Integer, HashMap<Integer, AbstractMap.SimpleEntry<Float, Long>>> ratingsMap = new HashMap<>();
    /** Item Rating Map      */ private HashMap<Integer, HashMap<Integer, Float>> itemRatingsMap = new HashMap<>();
    /** Similarity Matrix    */ private Float[][] simMatrix;
    /** Neighbors Map        */ private Map<Integer, ArrayList<AbstractMap.SimpleEntry<Integer, Float>>> neighborsMap = new HashMap<>();
    /** Number of Items      */ private int nItems;
    /** Users Map            */ private Map<Integer, Integer> Publicusers = new HashMap<>();
    /** Items Map            */ private Map<Integer, Integer> Publicitems = new HashMap<>();
    /** Users Map            */ private Map<Integer, Integer> Privateusers = new HashMap<>();
    /** Items Map            */ private Map<Integer, Integer> Privateitems = new HashMap<>();
    /** Number of Neighbors  */ private int numberOfNeighs = -1;
    /** Items Set            */ private Set<Integer> itemSet;

    public ItemSimilarityObject(HashMap<Integer, HashMap<Integer, AbstractMap.SimpleEntry<Float, Long>>> ratingsMap, int numberOfNeighs){
        this.numberOfNeighs = numberOfNeighs;
        this.ratingsMap = ratingsMap;
        int i = 0;
        int k = 0;
        for (int user : ratingsMap.keySet()){
            for (Map.Entry<Integer, AbstractMap.SimpleEntry<Float, Long>> entry : ratingsMap.get(user).entrySet()){
                int   item      = entry.getKey();
                float rating    = entry.getValue().getKey();
                HashMap<Integer, Float> itemMap = itemRatingsMap.get(item);
                if (itemMap==null) itemMap = new HashMap<>();
                itemMap.put(user,rating);
                itemRatingsMap.put(item,itemMap);
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
        this.nItems = Privateitems.keySet().size();
        this.simMatrix = new Float[nItems][nItems];
        this.itemSet = Publicitems.keySet();
        Run();
    }

    public ItemSimilarityObject(HashMap<Integer, HashMap<Integer, AbstractMap.SimpleEntry<Float, Long>>> ratingsMap){
        this.ratingsMap = ratingsMap;
        int i = 0;
        int k = 0;
        for (int user : ratingsMap.keySet()){
            for (Map.Entry<Integer, AbstractMap.SimpleEntry<Float, Long>> entry : ratingsMap.get(user).entrySet()){
                int   item      = entry.getKey();
                float rating    = entry.getValue().getKey();
                HashMap<Integer, Float> itemMap = itemRatingsMap.get(item);
                if (itemMap==null) itemMap = new HashMap<>();
                itemMap.put(user,rating);
                itemRatingsMap.put(item,itemMap);
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
        this.nItems = Privateitems.keySet().size();
        this.simMatrix = new Float[nItems][nItems];
        this.itemSet = Publicitems.keySet();
        Run();
    }

    public void Run(){
//        ProcessBinaryCosineSimilarity();
        ProcessCosineWithRatings();
        computeNeighbors();
    }

    private void ProcessCosineWithRatings(){
        final int[] debug = {0};
        IntStream.range(0, nItems).parallel().forEach(i -> {
            System.out.println(debug[0]);
            debug[0]++;
            Map<Integer, Float> item_i_map = itemRatingsMap.get(Privateitems.get(i));
            int finalI = i;
            IntStream.range(i+1, nItems).parallel().forEach(item_j_index -> {
                simMatrix[finalI][item_j_index] = computeCosineSimilarityWithRatings(item_i_map,itemRatingsMap.get(Privateitems.get(item_j_index)));
            });
        });
    }


    public float computeCosineSimilarityWithRatings(Map<Integer, Float> subject_i_map, Map<Integer, Float> subject_j_map){
        HashSet<Integer> union = new HashSet<>(subject_i_map.keySet());
        union.addAll(subject_j_map.keySet());
        Float num = 0f;
        Float userWeights = 0f;
        Float itemWeights = 0f;
        for (int feature : union){
            Float val   = subject_i_map.get(feature);
            float siw   = (val!=null)? val : 0f;
            val         = subject_j_map.get(feature);
            float sjw   = (val!=null)? val : 0f;
            num         = num + siw*sjw;
            userWeights = userWeights + (float)Math.pow(siw, 2);
            itemWeights = itemWeights + (float)Math.pow(sjw, 2);
        }
        float den = (float)(Math.sqrt(userWeights)*Math.sqrt(itemWeights));
        return (den!=0)?num/den:0f;
    }




    private void computeNeighbors(){
        final int[] debug = {0};
        IntStream.range(0, simMatrix[0].length).parallel().mapToObj(i -> {
            ArrayList<AbstractMap.SimpleEntry<Integer, Float>> neighs = new ArrayList<>();
            IntStream.range(i+1, simMatrix[0].length).mapToObj(col -> new AbstractMap.SimpleEntry<>(Privateitems.get(col),simMatrix[i][col])).forEachOrdered(neighs::add);
            IntStream.range(0, i).mapToObj(row -> new AbstractMap.SimpleEntry<>(Privateitems.get(row),simMatrix[row][i])).forEachOrdered(neighs::add);
            Collections.sort(neighs,Collections.reverseOrder(Comparator.comparing(AbstractMap.SimpleEntry::getValue)));

            if (numberOfNeighs != -1){
                neighs = new ArrayList<>(neighs.subList(0,numberOfNeighs));
            }
            System.out.println(debug[0]);
            debug[0]++;
            return new AbstractMap.SimpleEntry<>(Privateitems.get(i),neighs);
        }).forEachOrdered(e -> neighborsMap.put(e.getKey(),e.getValue()));
    }


    private void ProcessBinaryCosineSimilarity(){
        final int[] debug = {0};
        IntStream.range(0, nItems).parallel().forEach(i -> {
            System.out.println(debug[0]);
            debug[0]++;
            Map<Integer, Float> item_i_map = itemRatingsMap.get(Privateitems.get(i));
            int finalI = i;
            IntStream.range(i+1, nItems).forEach(item_j_index -> {
                simMatrix[finalI][item_j_index] = computeBinaryCosineSimilarity(item_i_map,itemRatingsMap.get(Privateitems.get(item_j_index)));
            });
        });
    }
    public float computeBinaryCosineSimilarity(Map<Integer, Float> subject_i_map, Map<Integer, Float> subject_j_map){
        HashSet<Integer> intersection = new HashSet<>(subject_i_map.keySet());
        intersection.retainAll(subject_j_map.keySet());
        float den = (float)(Math.sqrt(subject_i_map.size()*subject_j_map.size()));
        return (den!=0)?intersection.size()/den:0f;
    }

    public Map<Integer, ArrayList<AbstractMap.SimpleEntry<Integer, Float>>> getNeighborsMap() {
        return neighborsMap;
    }

    public ArrayList<AbstractMap.SimpleEntry<Integer, Float>> getNeighbors(int i) {
        return neighborsMap.get(i);
    }

    public Set<Integer> getItemSet() { return itemSet; }
}
