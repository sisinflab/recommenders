import java.util.*;
import java.util.stream.Collectors;

public class Item_kNN_Model {
    /** Rating Map           */ private  HashMap<Integer, HashMap<Integer, AbstractMap.SimpleEntry<Float, Long>>> ratingsMap;
    /** Number of Recs       */ private  int numberOfRecs;
    /** Number of Neighbors  */ private  int numberOfNeighs;
    /** Beta value           */ private double beta;
    /** Last timestamp       */ private long referringTimestamp;
    /**   */ public ItemSimilarityObject sim;


    public Item_kNN_Model(HashMap<Integer, HashMap<Integer, AbstractMap.SimpleEntry<Float, Long>>> ratingsMap,int numberOfNeighs, int numberOfRecs, double beta, long referringTimestamp){
        this.ratingsMap = ratingsMap;
        this.numberOfNeighs = numberOfNeighs;
        this.numberOfRecs = numberOfRecs;
        this.beta = beta;
        this.referringTimestamp = referringTimestamp;
        sim = new ItemSimilarityObject(ratingsMap, numberOfNeighs);
    }

    public List<String> scoreItems(int user){
        System.out.println(user);
        ArrayList<AbstractMap.SimpleEntry<Integer, Float>> recs = new ArrayList<>();
        Set<Integer> userItems = ratingsMap.get(user).keySet();
        Set<Integer> candidateItems = new HashSet<>(sim.getItemSet());
        candidateItems.removeAll(userItems);
        candidateItems.stream().map(item -> {
            ArrayList<AbstractMap.SimpleEntry<Integer, Float>> itemNeighs = sim.getNeighbors(item);
            HashMap<Integer, Float> neighborsMap = new HashMap<>();
            itemNeighs.stream().forEach(entry -> neighborsMap.put(entry.getKey(), entry.getValue()));
            HashSet<Integer> intersection = new HashSet<>();
            itemNeighs.stream().mapToInt(entry -> entry.getKey()).forEachOrdered(intersection::add);
            intersection.retainAll(userItems);
            if (intersection.size() > 0) {
                float sum = (float) intersection.stream().mapToDouble(item_j -> {
                    HashMap<Integer, AbstractMap.SimpleEntry<Float, Long>> userMap = ratingsMap.get(user);
                    Long timestamp = userMap.get(item_j).getValue();
                    float rating = userMap.get(item_j).getKey();
                    long diff = referringTimestamp - timestamp;
                    int ndays = Math.round(diff/(24*3600));
                    float decay = (float)Math.exp(-(beta * (ndays)));
                    return neighborsMap.get(item_j) * decay * rating;
                }).sum();
                float den = (float) itemNeighs.stream().mapToDouble(entry -> entry.getValue()).sum();
                AbstractMap.SimpleEntry<Integer, Float> rec = new AbstractMap.SimpleEntry<Integer, Float>(item, ((den != 0) || (sum != 0) ? sum / den : 0f));
                return new AbstractMap.SimpleEntry<>(item, ((den != 0) || (sum != 0) ? sum / den : 0f));
            }else{
                return null;
            }
        }).filter(Objects::nonNull).forEach(recs::add);

        Collections.sort(recs,Collections.reverseOrder(Comparator.comparing(AbstractMap.SimpleEntry::getValue)));
        if (recs.size()<numberOfRecs){
        }else{
            recs = new ArrayList<>(recs.subList(0,numberOfRecs));
        }
        return recs.stream().map(rec -> user+"\t"+rec.getKey()+"\t"+rec.getValue()).collect(Collectors.toList());
    }
}
