import java.util.*;
import java.util.stream.Collectors;

public class Item_kNN_Model {
    /** Rating Map           */ private  HashMap<Integer, HashMap<Integer, Float>> ratingsMap;
    /** Number of Recs       */ private  int numberOfRecs;
    /** Number of Neighbors  */ private  int numberOfNeighs;
    /** Number of Neighbors  */ public ItemSimilarityObject sim;

    public Item_kNN_Model(HashMap<Integer, HashMap<Integer, Float>> ratingsMap,int numberOfNeighs, int numberOfRecs){
        this.ratingsMap = ratingsMap;
        this.numberOfNeighs = numberOfNeighs;
        this.numberOfRecs = numberOfRecs;
        sim = new ItemSimilarityObject(ratingsMap, numberOfNeighs);
    }

    public List<String> scoreItems(int user){
        System.out.println(user);
        ArrayList<AbstractMap.SimpleEntry<Integer, Float>> recs = new ArrayList<>();
        Set<Integer> userItems = ratingsMap.get(user).keySet();
        HashMap<Integer,Float> userMap = ratingsMap.get(user);
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
                float sum = (float) intersection.stream().mapToDouble(neighborsMap::get).sum();
                float den = (float) itemNeighs.stream().mapToDouble(entry -> entry.getValue()).sum();
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
