import java.io.*;
import java.util.*;

public class MainWRMF {

    /** Rating File          */ private static String filePath = "";
    /** Recommendation File  */ private static String outPath = "";
    /** Rating Map           */ private static HashMap<Integer, HashMap<Integer, Double>> ratingsMap = new HashMap<>();
    /** Data Model           */ private static MFDataModelArray dataModel;
    /** Number of Factors    */ private static int D = 200;
    /** Number of iterations */ private static int numIters = 15;
    /** Initial val of Mean  */ private static float initMean = 0;
    /** Initial val of St.Dev*/ private static float initStdDev = 0.1F;
    /** Weight/Confidence Par*/ private static float alpha = 1F;
    /** Debug Mode           */ private static int debug = 1;
    /** Regularization       */ private static float regularization = 0.015F;
    /** Number of Recs       */ private static int numberOfRecs = 100;



        public static void main(String[] args) throws IOException {

        System.out.println( "" );
        System.out.println( "" );
        System.out.println( "WRMF algorithm" );
        System.out.println( "This implementation was realized using Oracle Java 8" );
        System.out.println( "Usage: java -jar WRMF fileVotes fileRecs numberOfFactors numberOfRecs" );
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
        WRMF model = new WRMF(D, alpha, regularization);
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
