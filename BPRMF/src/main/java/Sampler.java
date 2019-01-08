import java.util.*;
import java.util.stream.Collectors;

public class Sampler {

    private Random random;
    /** Rating Map           */ private HashMap<Integer, HashMap<Integer, Double>> ratingsMap;

    private boolean sampleNegativeItemsEmpirically;
    private int maxSamples;
    private int numUsers;
    private int numItems;
    /** Item Set             */ private Set<Integer> itemsSet = new HashSet<>();
    /** Item Set             */ private ArrayList<Integer> itemsArrayList;

    public Sampler(boolean sampleNegativeItemsEmpirically, Random random, HashMap<Integer, HashMap<Integer, Double>> ratingsMap, int maxSample){
        this.sampleNegativeItemsEmpirically = sampleNegativeItemsEmpirically;
        this.random = random;
        this.ratingsMap = ratingsMap;
        this.numUsers = ratingsMap.size();
        ratingsMap.entrySet().stream().map(entry -> entry.getValue().keySet()).forEachOrdered(itemsSet::addAll);
        itemsArrayList = new ArrayList<>(itemsSet);
        this.numItems = itemsSet.size();
        this.maxSamples = maxSample;
    }

    private int uniformUser(){
        ArrayList<Integer> indexList = new ArrayList<>(ratingsMap.keySet());
        return indexList.get(random.nextInt(indexList.size()));
    }

    private int sampleUser(){
        while (true) {
            int u = uniformUser();
            int numItemsForUser = ratingsMap.get(u).size();

            if (numItemsForUser == 0 || numItemsForUser == numItems){
                continue;
            }
                return u;
        }
    }

    private int sampleNegativeItem(ArrayList<Integer> UserItems){
        int j = randomItem();
        while (UserItems.contains(j)){
            j = randomItem();
        }
        return j;
    }

    public int otherItemMymedialite(ArrayList<Integer> UserItems){
        int j = itemsArrayList.get(random.nextInt(itemsArrayList.size()));
        while (UserItems.contains(j)){
            j = itemsArrayList.get(random.nextInt(itemsArrayList.size()));
        }
        return j;
    }

    public int randomItem(){
        int i;
        if(sampleNegativeItemsEmpirically){
            int u = uniformUser();
            ArrayList<Integer> indexList = new ArrayList<>(ratingsMap.get(u).keySet());
            i = indexList.get(random.nextInt(indexList.size()));
        }else{
            i = itemsArrayList.get(random.nextInt(itemsArrayList.size()));
        }
        return i;
    }


    public Map<Integer,ArrayList<Integer>> sampleTripleMymedialite(int numLossSample){

        ArrayList<Integer> listU = new ArrayList<>();
        ArrayList<Integer> listI = new ArrayList<>();
        ArrayList<Integer> listJ = new ArrayList<>();


        for (int k = 0; k < numLossSample; k++){
            int u = sampleUser();
            // Sample positive items
            ArrayList<Integer> indexList = new ArrayList<>(ratingsMap.get(u).keySet());
            int i = indexList.get(random.nextInt(indexList.size()));
            int j = otherItemMymedialite(indexList);
            listU.add(u);
            listI.add(i);
            listJ.add(j);
        }
        Map<Integer,ArrayList<Integer>> tempMap = new HashMap<>();
        tempMap.put(1,listU);
        tempMap.put(2,listI);
        tempMap.put(3,listJ);
        return tempMap;
    }


    public  Map<Integer,ArrayList<Integer>> Sample(String samplerName, int numPosEvents){
        // TODO not uniform user sampling
        Map<Integer,ArrayList<Integer>> TripleMap;
        if (samplerName == "withReplacement") {
//            TripleMap = IterateWithReplacementUniformUser(numPosEvents);
            TripleMap = new HashMap<>();
        }
        else
            TripleMap = sampleTripleMymedialite(numPosEvents);
        return TripleMap;
    }



}
