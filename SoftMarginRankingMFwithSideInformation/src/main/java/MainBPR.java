import java.io.*;
import java.util.*;
import ItemsAttributesManager.*;


public class MainBPR {

    /** Rating Map           */ private static HashMap<Integer, HashMap<Integer, Double>> ratingsMap = new HashMap<>();
    /** Data Model           */ private static DataModel dataModel;
    /** Learning Rate        */ private static float learningRate = 0.1F;
    /** Bias regularization  */ private static float biasRegularization = 0.0F;
    /** User regularization  */ private static float userRegularization = 0.0025F;
    /** Positive Item Reg    */ private static float positiveItemRegularization = 0.0025F;
    /** Negative Item Reg    */ private static float negativeItemRegularization = 0.00025F;
    /** Update Neg Item Fac  */ private static boolean updateNegativeItemsFactors = true;
    /** Number of Factors    */ private static int D = 10; // numFactors
    /** Update Users Fac     */ private static boolean updateUsers = true;
    /** Update Items Fac     */ private static boolean updateItems = true;
    /** Number of iterations */ private static int numIters = 30;
    /** Type of sampling     */ private static String samplerName = "withoutReplacement";
    /** Initial val of Mean  */ private static float initMean = 0;
    /** Initial val of St.Dev*/ private static float initStdDev = 0.1F;
    /** Debug Mode           */ private static int debug = 1;
    /** Popularity threshold */ private static int threshold = 0;
    private static boolean additive = true;
    /** Number of Recs       */ private static int numberOfRecs = 100;
    /** Rating File          */ private static String filePath = "";
    /** Recommendation File  */ private static String outPath = "";
    /** Attribute File       */ private static String fileAttrPath = "";
    /** Attributes Names     */private static String featuresNames = "";
    private static String propertiesPath = "properties.conf";

    public static void main(String[] args) throws IOException {


        System.out.println( "" );
        System.out.println( "" );
        System.out.println( "Soft Margin Ranking MF with side information algorithm" );
        System.out.println( "This implementation was realized using Oracle Java 8" );
        System.out.println( "Usage: java -jar SoftMarginRankingMFwithSideInformation fileVotes fileAttrPath fileRecs featuresNames numberOfRecs numIters" );
        System.out.println( "These parameters are set by default but you can override them passing alternative ones" );
        System.out.println( "" );
        System.out.println( "" );

        for (int i = 0; i < args.length; i++) {
            System.out.println(args[i]);
            if (i==0)filePath = args[i];
            if (i==1)fileAttrPath = args[i];
            if (i==2)outPath = args[i];
            if (i==3)featuresNames = args[i];
            if (i==4)numberOfRecs = Integer.parseInt(args[i]);
            if (i==5)numIters = Integer.parseInt(args[i]);
            if (i==6)additive = Boolean.parseBoolean(args[i]);
        }
        HashMap<Integer, ArrayList<Integer>> map = ItemsAttributesManager.loadMap(filePath,fileAttrPath, featuresNames, threshold,propertiesPath,additive);


        ratingsMap = Utils.Companion.loadRatingsFile(filePath,"\t",0,1,2);

        HashSet<Integer> originalUsers = new HashSet<>(ratingsMap.keySet());
        ratingsMap = ratingsMapWitSideInformation(ratingsMap, map);

        dataModel = new MFDataModelArray(ratingsMap,D,initMean,initStdDev);
        BPRMF model = new BPRMF(D, learningRate, biasRegularization, userRegularization, positiveItemRegularization, negativeItemRegularization, updateNegativeItemsFactors, updateUsers, updateItems);

        boolean sampleNegativeItemsEmpirically = true;
        int maxSamples = 10;
        Sampler sampler = new Sampler(sampleNegativeItemsEmpirically, dataModel.getRandom(),ratingsMap, maxSamples);

        model.Train(dataModel, sampler, numIters, initMean, initStdDev, samplerName);
        PrintWriter out = new PrintWriter(new FileWriter(outPath));

        ratingsMap.keySet().parallelStream().map(user -> {
            System.out.println(debug);
            debug++;
            HashSet<Integer> candidateItems = new HashSet<>(dataModel.getItemsSet());
            HashSet<Integer> userItems = new HashSet<>(ratingsMap.get(user).keySet());
            candidateItems.removeAll(userItems);
            ArrayList<AbstractMap.SimpleEntry<Integer,Double>> itemList = new ArrayList<>();
            for (int item : candidateItems){
                itemList.add(new AbstractMap.SimpleEntry<>(item,model.scoreItems(user,item)));
            }
            Collections.sort(itemList,Collections.reverseOrder(Comparator.comparing(AbstractMap.SimpleEntry::getValue)));
            if (itemList.size()>numberOfRecs){
                itemList = new ArrayList<>(itemList.subList(0,numberOfRecs));
            }
            return new AbstractMap.SimpleEntry<>(user,itemList);
        }).forEachOrdered(entry -> entry.getValue().stream().forEach(e-> out.println(entry.getKey()+"\t"+e.getKey()+"\t"+e.getValue())));

        out.close();
    }

    private static HashMap<Integer, HashMap<Integer, Double>> ratingsMapWitSideInformation(HashMap<Integer, HashMap<Integer, Double>> ratingsMap, HashMap<Integer, ArrayList<Integer>> map){
        Map<Integer, Integer> PublicFeatures = new HashMap<>();
        int k = ratingsMap.keySet().stream().mapToInt(v -> v).max().orElse(ratingsMap.size()) + 1;

        for (int item : map.keySet()){
            for (int feature : map.get(item)){
                if (!PublicFeatures.containsKey(feature)){
                    PublicFeatures.put(feature,k);
                    ratingsMap.putIfAbsent(k, new HashMap<>());
                    HashMap<Integer, Double> featureItems = ratingsMap.get(k);
                    featureItems.put(item,1.);
                    ratingsMap.put(k,featureItems);
                    k++;
                }
            }
        }
        return ratingsMap;
    }
}
