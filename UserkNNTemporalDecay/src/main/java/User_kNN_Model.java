import java.util.*;
import java.util.stream.Collectors;

public class User_kNN_Model {
    /** Rating Map           */ private  HashMap<Integer, HashMap<Integer, AbstractMap.SimpleEntry<Float, Long>>> ratingsMap = new HashMap<>();
    /** Number of Recs       */ private  int numberOfRecs;
    /** Number of Neighbors  */ private  int numberOfNeighs;
    /** Beta value           */ private  double beta;
    /** Last timestamp       */ private  long referringTimestamp;
    /**   */ public UserSimilarityObject sim;

    User_kNN_Model(HashMap<Integer, HashMap<Integer, AbstractMap.SimpleEntry<Float, Long>>> ratingsMap,int numberOfNeighs, int numberOfRecs, double beta, long referringTimestamp){
        this.ratingsMap = ratingsMap;
        this.numberOfNeighs = numberOfNeighs;
        this.numberOfRecs = numberOfRecs;
        this.beta = beta;
        this.referringTimestamp = referringTimestamp;
        sim = new UserSimilarityObject(ratingsMap, numberOfNeighs);
    }

    List<String> scoreItems(int user){
        System.out.println(user);
        ArrayList<AbstractMap.SimpleEntry<Integer, Float>> userNeighs = sim.getNeighbors(user);
        if (userNeighs==null)System.out.println(user+" error "+sim.getNeighbors(user));
        HashMap<Integer,Float> neighborsMap = new HashMap<>();
        userNeighs.stream().forEachOrdered(entry -> neighborsMap.put(entry.getKey(),entry.getValue()));
        HashSet<Integer> candidateItems = new HashSet<>();
        neighborsMap.keySet().stream().map(neigh -> ratingsMap.get(neigh).keySet()).forEachOrdered(candidateItems::addAll);
        ArrayList<AbstractMap.SimpleEntry<Integer, Float>> recs = new ArrayList<>();
        Set<Integer> userItems = ratingsMap.get(user).keySet();
        candidateItems.removeAll(userItems);
        candidateItems.stream().map(item -> {
            float sum = (float) neighborsMap.entrySet().stream().mapToDouble(entry -> {
                Map<Integer, AbstractMap.SimpleEntry<Float,Long>> neighMap = ratingsMap.get(entry.getKey());
                double decay = 0;
                if (neighMap.containsKey(item)) {
                    Long timestamp = neighMap.get(item).getValue();
                    long diff = referringTimestamp - timestamp;
                    int ndays = Math.round(diff/(24*3600));
                    decay = Math.exp(-(beta * (ndays)));
                }
                return entry.getValue() * decay;
            }).sum();
            float den = (float) neighborsMap.values().stream().mapToDouble(Number::doubleValue).sum();
            return new AbstractMap.SimpleEntry<>(item,((den!=0)? sum/den : 0f));
        }).forEachOrdered(recs::add);
        Collections.sort(recs,Collections.reverseOrder(Comparator.comparing(AbstractMap.SimpleEntry::getValue)));
        if (recs.size()>numberOfRecs){
            recs = new ArrayList<>(recs.subList(0,numberOfRecs));
        }
        return recs.stream().map(rec -> user+"\t"+rec.getKey()+"\t"+rec.getValue()).collect(Collectors.toList());
    }
}
