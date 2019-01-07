import java.util.*;
import java.util.stream.IntStream;



public class MFDataModelArray {
    /** random               */ private Random random;
    /** Rating Map           */ private HashMap<Integer, HashMap<Integer, Double>> ratingsMap = new HashMap<>();
    /** Feedback User Matrix */ private HashMap<Integer, ArrayList<Integer>> feedbackUserMatrix = new HashMap<>();
    /** Feedback Item Matrix */ private HashMap<Integer, ArrayList<Integer>> feedbackItemMatrix = new HashMap<>();
    /** Global Bias          */ private double globalBias;
    /** Min Rating           */ private double min_rating;
    /** Max Rating           */ private double max_rating;
    /** Max Rating           */ private double rating_range_size;
    /** Max Rating           */ private ArrayList<AbstractMap.SimpleEntry<Integer, Integer>> ratingsList = new ArrayList<>();
    /** Users Factors Matrix */ private double[][] ufMatrix;
    /** Items Factors Matrix */ private double[][] ifMatrix;
    /** Items Bias Matrix    */ private double[] ibMatrix;
    /** Users Bias Matrix    */ private double[] ubMatrix;
    /** Users Map            */ private Map<Integer, Integer> Publicusers = new HashMap<>();
    /** Items Map            */ private Map<Integer, Integer> Publicitems = new HashMap<>();
    /** Users Map            */ private Map<Integer, Integer> Privateusers = new HashMap<>();
    /** Items Map            */ private Map<Integer, Integer> Privateitems = new HashMap<>();
    /** Number of factors    */ private int numberOfFactors = 10;
    /** Number of Users      */ private int nUsers;
    /** Number of Items      */ private int nItems;
    /** Number of Ratings    */ private int numRatings;
    /** Initial Mean         */ private float initMean = 0;
    /** Initial Standard Dev */ private float initStdDev = 0.1F;
    /** Items Set            */ private Set<Integer> itemsSet = new HashSet<>();
    /** Users Set            */ private Set<Integer> usersSet;
    /** Popularity Map       */ private HashMap<Integer, Integer> popularity = new HashMap<>();

    public MFDataModelArray(HashMap<Integer, HashMap<Integer, Double>> ratingsMap, int numberOfFactors, float initMean, float initStdDev){
        this.random = new Random(42);
        this.ratingsMap = ratingsMap;
        this.numberOfFactors = numberOfFactors;
        this.initMean = initMean;
        this.initStdDev =initStdDev;
        this.numRatings = ratingsMap.values().stream().mapToInt(it->it.size()).sum();
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
        nUsers = Publicusers.size();
        nItems = Publicitems.size();
        ratingsMap.entrySet().stream().map(entry -> entry.getValue().keySet()).forEachOrdered(itemsSet::addAll);
        usersSet = new HashSet<>(ratingsMap.keySet());
        ratingsMap.values().stream().forEach(itemsMaps -> {
            itemsMaps.keySet().stream().forEach(item -> {
                popularity.putIfAbsent(item,0);
                popularity.compute(item, (a, v) -> v + 1);
            });
        });
        ratingsMap.entrySet().stream().forEach(user -> {
            user.getValue().keySet().stream().forEach(item -> ratingsList.add(new AbstractMap.SimpleEntry<>(user.getKey(),item)));
        });
        ratingsMap.entrySet().stream().forEach(user -> {
            user.getValue().keySet().stream().forEach(item -> {
                feedbackUserMatrix.putIfAbsent(user.getKey(), new ArrayList<>());
//                feedbackUserMatrix.compute(user.getKey(), (key, value) -> new ArrayList<>(value.add(item)));
                feedbackUserMatrix.get(user.getKey()).add(item);

                feedbackItemMatrix.putIfAbsent(item, new ArrayList<>());
                feedbackItemMatrix.get(item).add(user.getKey());
            });
        });
        initializeData();
    }
    private void initializeData(){
        System.out.println(ratingsMap.get(1));
        min_rating = ratingsMap.values().stream().mapToDouble(itemsMap -> itemsMap.values().stream().mapToDouble(e -> e).min().getAsDouble()).min().getAsDouble();
        max_rating = ratingsMap.values().stream().mapToDouble(itemsMap -> itemsMap.values().stream().mapToDouble(e -> e).max().getAsDouble()).max().getAsDouble();
        rating_range_size = max_rating - min_rating;
        double avg = ratingsMap.values().stream().mapToDouble(itemsMap -> itemsMap.values().stream().mapToDouble(e -> e).sum()).sum() / numRatings;
        avg = (avg - min_rating) / rating_range_size;
        globalBias = Math.log(avg / (1 - avg));

        ibMatrix = new double[nItems];
        Arrays.fill(ibMatrix,0);

        ubMatrix = new double[nUsers];
        Arrays.fill(ubMatrix,0);


        ufMatrix = new double[nUsers][numberOfFactors];
        for (int i = 0; i < nUsers;i++){
            for(int j = 0; j < numberOfFactors; j++) {
                ufMatrix[i][j] = random.nextGaussian() * initStdDev + initMean;
            }
        }

        ifMatrix = new double[nItems][numberOfFactors];
        for (int i = 0; i < nItems;i++){
            for(int j = 0; j < numberOfFactors; j++) {
                ifMatrix[i][j] = random.nextGaussian() * initStdDev + initMean;
            }
        }
    }

    public void update_u(int u, double[] fs){
        ufMatrix[Publicusers.get(u)]=fs;
    }
    public void update_i(int i, double[] fs){
        ifMatrix[Publicitems.get(i)]=fs;
    }
    public void update_u_bias(int u, double ub){
        ubMatrix[Publicusers.get(u)]=ub;
    }
    public void update_i_bias(int i, double ib){
        ibMatrix[Publicitems.get(i)]=ib;
    }
    public double predict(int u, int i){
        double[] u_f = ufMatrix[Publicusers.get(u)];
        double[] i_f = ifMatrix[Publicitems.get(i)];
        return IntStream.range(0,numberOfFactors).mapToDouble(f -> u_f[f]*i_f[f]).sum();
    }
    public double computeSquaredErrorSum(){
        return ratingsMap.entrySet().stream().mapToDouble(user -> {
            return user.getValue().entrySet().stream().mapToDouble(item -> {
                return Math.pow(predict(user.getKey(),item.getKey()) - item.getValue(),2);
            }).sum();
        }).sum();
    }
    public double getRating(int u, int i){return ratingsMap.get(u).get(i);}
    public double[] getUserFactors(int u){
        return ufMatrix[Publicusers.get(u)];
    }
    public double[] getItemFactors(int i){
        return ifMatrix[Publicitems.get(i)];
    }
    public double getItemBias(int i){
        return ibMatrix[Publicitems.get(i)];
    }
    public double getUserBias(int u){
        return ubMatrix[Publicusers.get(u)];
    }
    public Random getRandom() {
        return random;
    }
    public int getnUsers() {
        return nUsers;
    }
    public int getnItems() {
        return nItems;
    }
    public Set<Integer> getItemsSet() {
        return itemsSet;
    }
    public int getNumRatings() {
        return numRatings;
    }
    public Set<Integer> getUsersSet() {
        return usersSet;
    }
    public Set<Integer> getUserItems(int user) {
        return ratingsMap.get(user).keySet();
    }
    public int getItemPopularity(int i) {
        return popularity.get(i);
    }
    public AbstractMap.SimpleEntry<Integer, Integer> getRatingPair(int index){return ratingsList.get(index);}
    public double getMinRating() {
        return min_rating;
    }
    public double getMaxRating() {
        return max_rating;
    }
    public double getRatingRangeSize() {
        return rating_range_size;
    }
    public double predictMyMediaLite(int u, int i){
        double[] u_f = ufMatrix[Publicusers.get(u)];
        double[] i_f = ifMatrix[Publicitems.get(i)];
        double score = globalBias + ubMatrix[Publicusers.get(u)] + ibMatrix[Publicitems.get(i)] + IntStream.range(0,numberOfFactors).mapToDouble(f -> u_f[f]*i_f[f]).sum();
        return (min_rating + (1 / ( 1 + Math.exp(-score))) * rating_range_size);
    }
    public double[][] getUfMatrix(){return ufMatrix;}
    public double[][] getIfMatrix(){return ifMatrix;}

    public HashMap<Integer, ArrayList<Integer>> getFeedbackUserMatrix() {
        return feedbackUserMatrix;
    }

    public HashMap<Integer, ArrayList<Integer>> getFeedbackItemMatrix() {
        return feedbackItemMatrix;
    }
}
