import java.io.*;
import java.util.*;

public class MainBPR {

    /** Rating Map                   */ private static HashMap<Integer, HashMap<Integer, Double>> ratingsMap = new HashMap<>();
    /** Data Model                   */ private static DataModel dataModel;
    /** Learning Rate                */ private static float learningRate = 0.05F;
    /** Bias regularization          */ private static float biasRegularization = 0.0F;
    /** User regularization          */ private static float userRegularization = 0.0025F;
    /** Positive Item Reg            */ private static float positiveItemRegularization = 0.0025F;
    /** Negative Item Reg            */ private static float negativeItemRegularization = 0.00025F;
    /** Update Neg Item Fac          */ private static boolean updateNegativeItemsFactors = true;
    /** Sample Neg Item empirically  */ private static boolean sampleNegativeItemsEmpirically = true;
    /** Number of Factors            */ private static int D = 10; // numFactors
    /** Update Users Fac             */ private static boolean updateUsers = true;
    /** Update Items Fac             */ private static boolean updateItems = true;
    /** Number of iterations         */ private static int numIters = 30;
    /** Type of sampling             */ private static String samplerName = "withoutReplacement";
    /** Initial val of Mean          */ private static float initMean = 0;
    /** Initial val of St.Dev        */ private static float initStdDev = 0.1F;
    /** Debug Mode                   */ private static int debug = 1;
    /** Number of Recs               */ private static int numberOfRecs = 100;


    public static void main(String[] args) throws IOException {

        System.out.println( "" );
        System.out.println( "" );
        System.out.println( "BPR Factorization machines" );
        System.out.println( "This implementation was realized using Oracle Java 8" );
        System.out.println( "Usage: java -jar FactorizationMachines filePath outPath" );
        System.out.println( "These parameters are set by default but you can override them passing alternative ones" );
        System.out.println( "" );
        System.out.println( "" );


        String filePath = "/home/starlord/Documents/DATASETS/toys_amazon/trainingset.tsv";
        String outPath = "/home/starlord/Desktop/BPRFM.tsv";

        for (int i = 0; i < args.length; i++) { // TODO parameterize everything
            System.out.println(args[i]);
            if (i==0)filePath = args[i];
            if (i==1)outPath = args[i];
        }
        ratingsMap = Utils.Companion.loadRatingsFile(filePath,"\t",0,1,2);
        dataModel = new FMDataModelArray(ratingsMap,D,initMean,initStdDev);
        BPRFM2d model = new BPRFM2d(D, learningRate, biasRegularization, userRegularization, positiveItemRegularization, negativeItemRegularization, updateNegativeItemsFactors, updateUsers, updateItems);
        Sampler sampler = new Sampler(sampleNegativeItemsEmpirically, dataModel.getRandom(),ratingsMap);
        model.Train(dataModel, sampler, numIters, initMean, initStdDev, samplerName);


        PrintWriter out = new PrintWriter(new FileWriter(outPath));
        for (int user : ratingsMap.keySet()){
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
            itemList.stream().forEach(e-> out.println(user+"\t"+e.getKey()+"\t"+e.getValue()));
        }
        out.close();
    }
}
