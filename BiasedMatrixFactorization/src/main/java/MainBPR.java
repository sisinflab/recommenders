import java.io.*;
import java.util.*;

public class MainBPR {

    /** Rating File          */ private static String filePath = "";
    /** Recommendation File  */ private static String outPath = "";
    /** Rating Map           */ private static HashMap<Integer, HashMap<Integer, Double>> ratingsMap = new HashMap<>();
    /** Data Model           */ private static MFDataModelArray dataModel;
    /** Learning Rate        */ private static float learningRate = 0.01F;
    /** Bias regularization  */ private static float biasRegularization = 0.01F;
    /** User regularization  */ private static float userRegularization = 0.015F;
    /** Item regularization  */ private static float itemRegularization = 0.015F;
    /** Bias learn rate      */ private static float BiasLearnRate = 1.0F;
    /** Learning rate decay  */ private static float learningRateDecay = 1.0F;
    /** Number of Factors    */ private static int D = 10;
    /** Update Users Fac     */ private static boolean updateUsers = true;
    /** Update Items Fac     */ private static boolean updateItems = true;
    /** Bold driver          */ private static boolean boldDriver = false;
    /** Frequency regularization */ private static boolean frequencyRegularization = false;
    /** Number of iterations */ private static int numIters = 30;
    /** Initial val of Mean  */ private static float initMean = 0;
    /** Initial val of St.Dev*/ private static float initStdDev = 0.1F;
    /** Debug Mode           */ private static int debug = 1;
    /** Number of Recs       */ private static int numberOfRecs = 100;



        public static void main(String[] args) throws IOException {

        System.out.println( "" );
        System.out.println( "" );
        System.out.println( "Biased Matrix Factorization algorithm" );
        System.out.println( "This implementation was realized using Oracle Java 8" );
        System.out.println( "Usage: java -jar BiasedMatrixFactorization algorithm fileVotes fileRecs numberOfFactors numberOfRecs" );
        System.out.println( "These parameters are set by default but you can override them passing alternative ones" );
        System.out.println( "" );
        System.out.println( "" );


        for (int i = 0; i < args.length; i++) {
            System.out.println(args[i]);
            if (i==0)filePath = args[i];
            if (i==1)outPath = args[i];
            if (i==2)D = Integer.parseInt(args[i]);
            if (i==3)numberOfRecs = Integer.parseInt(args[i]);
        }

        ratingsMap = Utils.Companion.loadRatingsFile(filePath,"\t",0,1,2);
        dataModel = new MFDataModelArray(ratingsMap,D,initMean,initStdDev);
        BiasedMF model = new BiasedMF(D, learningRate, biasRegularization, userRegularization, itemRegularization, BiasLearnRate, learningRateDecay, updateUsers, updateItems, boldDriver, frequencyRegularization);
        model.Train(dataModel, numIters);
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
}
