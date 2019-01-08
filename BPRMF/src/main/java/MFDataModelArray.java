import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;



public class MFDataModelArray implements DataModel {
    /** random               */ private Random random;
    /** Rating Map           */ private HashMap<Integer, HashMap<Integer, Double>> ratingsMap = new HashMap<>();
    /** Users Factors Matrix */ private double[][] ufMatrix;
    /** Items Factors Matrix */ private double[][] ifMatrix;
    /** Items Bias Matrix    */ private double[] ibMatrix;
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
        initializeData();
    }
    private void initializeData(){
        System.out.println(ratingsMap.get(1));
        ibMatrix = new double[nItems];
        Arrays.fill(ibMatrix,0);


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
    public void update_i_bias(int i, double ib){
        ibMatrix[Publicitems.get(i)]=ib;
    }
    public double predict(int u, int i){
        double[] u_f = ufMatrix[Publicusers.get(u)];
        double[] i_f = ifMatrix[Publicitems.get(i)];
        return IntStream.range(0,numberOfFactors).mapToDouble(f -> u_f[f]*i_f[f]).sum() + ibMatrix[Publicitems.get(i)];
    }
    public double[] getUserFactors(int u){
        return ufMatrix[Publicusers.get(u)];
    }
    public double[] getItemFactors(int i){
        return ifMatrix[Publicitems.get(i)];
    }
    public double getItemBias(int i){
        return ibMatrix[Publicitems.get(i)];
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


}
