import java.util.*;
import java.util.stream.Collectors;

public class Sampler {

    private Random random;
    private boolean sampleNegativeItemsEmpirically;
    private int numUsers;
    private int numItems;
    /** Rating Map           */ private HashMap<Integer, HashMap<Integer, Double>> ratingsMap;
    /** Item Set             */ private Set<Integer> itemsSet = new HashSet<>();
    /** Item Set             */ private ArrayList<Integer> itemsArrayList;

    public Sampler(boolean sampleNegativeItemsEmpirically, Random random, HashMap<Integer, HashMap<Integer, Double>> ratingsMap){
        this.sampleNegativeItemsEmpirically = sampleNegativeItemsEmpirically;
        this.random = random;
        this.ratingsMap = ratingsMap;
        this.numUsers = ratingsMap.size();
        ratingsMap.entrySet().stream().map(entry -> entry.getValue().keySet()).forEachOrdered(itemsSet::addAll);
        itemsArrayList = new ArrayList<>(itemsSet);
        this.numItems = itemsSet.size();
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

//    public static int numSample(int maxSamples, int n){
//        if (maxSamples == -1){
//            return n;
//        }
//        return Math.min(n, maxSamples);
//    }

    public Map<Integer,ArrayList<Integer>> uniformUserSampling(int numLossSample){

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

//    public static Map<Integer,ArrayList<Integer>> IterateWithReplacementUniformUser(int numPosEvents) {
//
//        ArrayList<Integer> listU = new ArrayList<>();
//        ArrayList<Integer> listI = new ArrayList<>();
//        ArrayList<Integer> listJ = new ArrayList<>();
//
//        int user_id, pos_item_id, neg_item_id;
//
//        // make a local copy of data as we're going to "forget" some entries
//        float[][] user_matrix = new float[data.length][data[0].length];
//        for (int i = 0; i< data.length;i++){
//            user_matrix[i] = Arrays.copyOf(data[i],data[i].length);
//
//        }
//
//        for (int k = 0; k < numPosEvents; k++)
//        {
//            while (true) // sampling with replacement
//            {
//                user_id = sampleUser();
//                ArrayList<Integer> user_items = getNonZeroIndices(user_matrix[user_id]);
//                // reset user if already exhausted
//                if (user_items.size() == 0){
//                    // reset user data if it's all been sampled
//                    for (int item_id:getNonZeroIndices(data[user_id])){
//                        user_matrix[user_id][item_id] = data[user_id][item_id];
//                    }
//                    user_items = getNonZeroIndices(user_matrix[user_id]);
//                }
//
//                pos_item_id = user_items.get(random.nextInt(user_items.size()));
//                user_matrix[user_id][pos_item_id] = 0; // temporarily forget positive observation
//                do {
//                    int min = 0;
//                    int max = numItems -1; /
//                    neg_item_id = random.nextInt((max - min) + 1) + min;
//                }while (user_items.contains(neg_item_id));
//
//                // sostituto di yield.
//                listU.add(user_id);
//                listI.add(pos_item_id);
//                listJ.add(neg_item_id);
//
//                break;
//            }
//        }
//
//        Map<Integer,ArrayList<Integer>> tempMap = new HashMap<>();
//        tempMap.put(1,listU);
//        tempMap.put(2,listI);
//        tempMap.put(3,listJ);
//
//        return tempMap;
//
//    }


//    public static Map<Integer,ArrayList<Integer>> generateUniformUserUniformItem(int value){
//
//        ArrayList<Integer> listU = new ArrayList<>();
//        ArrayList<Integer> listI = new ArrayList<>();
//        ArrayList<Integer> listJ = new ArrayList<>();
//
////        int numPosEvents = countNonZero2DArray(data);
////        for (int k = 0; k < numSample(numLossSample, numPosEvents); k++){ /
//        for (int k = 0; k < value; k++){
//            int u = sampleUser();
//            // Sample positive items
//            ArrayList<Integer> indexList = getNonZeroIndices(data[u]);
//            int i = indexList.get(random.nextInt(indexList.size()));
//            int j = sampleNegativeItem(indexList);
//
//            // sostituto di yield.
//            listU.add(u);
//            listI.add(i);
//            listJ.add(j);
//        }
//        Map<Integer,ArrayList<Integer>> tempMap = new HashMap<Integer, ArrayList<Integer>>();
//        tempMap.put(1,listU);
//        tempMap.put(2,listI);
//        tempMap.put(3,listJ);
//        return tempMap;
//    }

//    public static Map<Integer,ArrayList<Integer>> generateUniformUserUniformItemWithoutReplacement(int numLossSample){
//        ArrayList<Integer> listU = new ArrayList<>();
//        ArrayList<Integer> listI = new ArrayList<>();
//        ArrayList<Integer> listJ = new ArrayList<>();
//
//        // make a local copy of data as we're going to "forget" some entries
//        float[][] localData = data;
//
//        int numPosEvents = countNonZero2DArray(data);
//        for (int k = 0; k < numSample(numLossSample, numPosEvents); k++){
//            int u = sampleUser();
//            // sample positive item without replacement if we can
//            ArrayList<Integer> userItems = getNonZeroIndices(localData[u]);
//            if (userItems.size() == 0){
//                // reset user data if it's all been sampled
//                for (int ix:getNonZeroIndices(data[u])){
//                    localData[u][ix] = data[u][ix];
//                }
//                userItems = getNonZeroIndices(localData[u]);
//            }
//            int i = userItems.get(random.nextInt(userItems.size()));
//            // forget this item so we don't sample it again for the same user
//            localData[u][i] = 0;
//            int j = sampleNegativeItem(userItems);
//            listU.add(u);
//            listI.add(i);
//            listJ.add(j);
//        }
//
//
//        Map<Integer,ArrayList<Integer>> tempMap = new HashMap<Integer, ArrayList<Integer>>();
//        tempMap.put(1,listU);
//        tempMap.put(2,listI);
//        tempMap.put(3,listJ);
//        return tempMap;
//    }

    public  Map<Integer,ArrayList<Integer>> Sample(String samplerName, int numPosEvents){
        // TODO complete user sampling with replacement
        Map<Integer,ArrayList<Integer>> TripleMap;
        if (samplerName == "withReplacement") {
//            TripleMap = IterateWithReplacementUniformUser(numPosEvents);
            TripleMap = new HashMap<>();
        }
        else
            TripleMap = uniformUserSampling(numPosEvents);
        return TripleMap;
    }

}
