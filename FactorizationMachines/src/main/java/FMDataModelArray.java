import java.util.*;
import java.util.stream.IntStream;



public class FMDataModelArray implements DataModel {
    /** random               */ private Random random;
    /** Rating Map           */ private HashMap<Integer, HashMap<Integer, Double>> ratingsMap;
    /** Items Bias Matrix    */ private double globalBias;
    /** Features weights     */ private double[] wMatrix;
    /** Fact Interact Matrix */ private double[][] fiMatrix;
    /** Users Map            */ private Map<Integer, Integer> Publicusers = new HashMap<>();
    /** Users Map            */ private Map<Integer, Integer> Privateusers = new HashMap<>();
    /** Items Map            */ private Map<Integer, Integer> Publicitems = new HashMap<>();
    /** Items Map            */ private Map<Integer, Integer> Privateitems = new HashMap<>();
    /** Features Map         */ private Map<String, Integer> PublicAllFeatures = new HashMap<>();
    /** Features Map         */ private Map<Integer, String> PrivateAllFeatures = new HashMap<>();
    /** Number of features   */ private int numberOfFeatures;
    /** Number of factors    */ private int numberOfFactors;
    /** Number of Users      */ private int nUsers;
    /** Number of Items      */ private int nItems;
    /** Number of Ratings    */ private int numRatings;
    /** Initial Mean         */ private float initMean;
    /** Initial Standard Dev */ private float initStdDev;
    /** Items Set            */ private Set<Integer> itemsSet = new HashSet<>();
    /** Users Set            */ private Set<Integer> usersSet;

    public FMDataModelArray(HashMap<Integer, HashMap<Integer, Double>> ratingsMap, int numberOfFactors, float initMean, float initStdDev){
        this.random = new Random(42);
        this.ratingsMap = ratingsMap;
        this.numberOfFactors = numberOfFactors;
        this.initMean = initMean;
        this.initStdDev =initStdDev;
        this.numRatings = ratingsMap.values().stream().mapToInt(HashMap::size).sum();
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
        int z = 0;
        for (int user : Publicusers.keySet()){
            if (!PublicAllFeatures.containsKey("u_"+user)){
                PublicAllFeatures.put("u_"+user,z);
                PrivateAllFeatures.put(z,"u_"+user);
                z++;
            }
        }
        for (int item : Publicitems.keySet()){
            if (!PublicAllFeatures.containsKey("i_"+item)){
                PublicAllFeatures.put("i_"+item,z);
                PrivateAllFeatures.put(z,"i_"+item);
                z++;
            }
        }
        this.numberOfFeatures =  PrivateAllFeatures.size();
        nUsers = Publicusers.size();
        nItems = Publicitems.size();
        ratingsMap.entrySet().stream().map(entry -> entry.getValue().keySet()).forEachOrdered(itemsSet::addAll);
        usersSet = new HashSet<>(ratingsMap.keySet());
        initializeData();
    }
    private void initializeData(){

        globalBias = 0;

        wMatrix = new double[numberOfFeatures];
        for (int i = 0; i < numberOfFeatures;i++){
            wMatrix[i] = random.nextGaussian() * initStdDev + initMean;
        }

        fiMatrix = new double[numberOfFeatures][numberOfFactors];
        for (int i = 0; i < numberOfFeatures;i++){
            for(int z = 0; z < numberOfFactors; z++) {
                fiMatrix[i][z] = random.nextGaussian() * initStdDev + initMean;
            }
        }
    }

    @Override
    public void update_uf(int u, double[] fs){
        fiMatrix[PublicAllFeatures.get("u_"+u)]=fs;
    }
    @Override
    public void update_if(int i, double[] fs){
        fiMatrix[PublicAllFeatures.get("i_"+i)]=fs;
    }
    @Override
    public void update_u_w(int i, double uw){
        wMatrix[Publicitems.get(i)]=uw;
    }
    @Override
    public void update_i_w(int i, double iw){
        wMatrix[Publicitems.get(i)]=iw;
    }
    @Override
    public double predict(int u, int i){
        double u_w = wMatrix[Publicusers.get(u)];
        double i_w = wMatrix[Publicitems.get(i)];
        double[] u_f = fiMatrix[PublicAllFeatures.get("u_"+u)];
        double[] i_f = fiMatrix[PublicAllFeatures.get("i_"+i)];

        return globalBias + u_w + i_w + dotProduct(u_f,i_f);
    }
    @Override
    public double[] getUserFactors(int u){
        return fiMatrix[PublicAllFeatures.get("u_"+u)];
    }
    @Override
    public double[] getItemFactors(int i){
        return fiMatrix[PublicAllFeatures.get("i_"+i)];
    }
    @Override
    public double getUserBias(int i){
        return wMatrix[Publicitems.get(i)];
    }
    @Override
    public double getItemBias(int i){
        return wMatrix[Publicitems.get(i)];
    }
    @Override
    public Random getRandom() {
        return random;
    }
    @Override
    public int getnUsers() {
        return nUsers;
    }
    @Override
    public int getnItems() {
        return nItems;
    }
    @Override
    public Set<Integer> getItemsSet() {
        return itemsSet;
    }
    @Override
    public int getNumRatings() {
        return numRatings;
    }
    @Override
    public Set<Integer> getUsersSet() {
        return usersSet;
    }
    private double dotProduct(double[] x, double[] y) {return IntStream.range(0,x.length).mapToDouble(i->x[i]*y[i]).sum();}

}
