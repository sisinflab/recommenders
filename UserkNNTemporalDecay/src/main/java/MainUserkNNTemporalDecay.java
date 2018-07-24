import java.io.*;
import java.util.*;


public class MainUserkNNTemporalDecay {

    /** Rating File          */ private static String fileVotes = "";
    /** Recommendation File  */ private static String fileRecs  = "";
    /** Rating Map           */ private static HashMap<Integer, HashMap<Integer, AbstractMap.SimpleEntry<Float, Long>>> ratingsMap = new HashMap<>();
    /** Beta value           */ private static double beta = 1.0/200.0;
    /** Last timestamp       */ private static long referringTimestamp = 1377820800L; //Amazon Toys
    /** Number of Recs       */ private static int numberOfRecs = 100;
    /** Number of Neighbors  */ private static int numberOfNeighs = 80;



    public static void main( String[] args ) throws IOException
    {
        System.out.println( "" );
        System.out.println( "" );
        System.out.println( "UserkNN with Temporal Decay" );
        System.out.println( "This implementation was realized using Oracle Java 8" );
        System.out.println( "Usage: java -jar UserkNNTemporalDecay fileVotes fileRecs numberOfNeighs referringTimestamp numberOfRecs beta" );
        System.out.println( "These parameters are set by default but you can override them passing alternative ones" );
        System.out.println( "" );
        System.out.println( "" );

        fileVotes = "/home/starlord/Documents/DATASETS/toys_amazon/trainingset.tsv";
        fileRecs = "/home/starlord/Desktop/User-kNN-TD.tsv";

        for (int i = 0; i < args.length; i++) {
            System.out.println(args[i]);
            if (i==0)fileVotes = args[i];
            if (i==1)fileRecs = args[i];
            if (i==2)numberOfNeighs = Integer.parseInt(args[i]);
            if (i==3)referringTimestamp = Long.parseLong(args[i]);
            if (i==4)numberOfRecs = Integer.parseInt(args[i]);
            if (i==5)beta = Double.parseDouble(args[i]);
        }


        ratingsMap = Utils.Companion.loadRatingsFile(fileVotes, "\t", 0, 1, 2, 3);
        User_kNN_Model model = new User_kNN_Model(ratingsMap, numberOfNeighs, numberOfRecs, beta, referringTimestamp);
        HashSet<List<String>> results = new HashSet<>();
        ratingsMap.keySet().parallelStream().map(model::scoreItems).forEach(results::add);

        PrintWriter recs= new PrintWriter(new FileWriter(fileRecs));

        for (List<String> s : results) {
            if (s!=null){
                for (String value : s) {
                    if (value != null) {
                        recs.println(value);
                    }
                }
            }
        }
        recs.close();
    }
}


