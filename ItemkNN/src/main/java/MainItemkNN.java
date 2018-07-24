import java.io.*;
import java.util.*;


public class MainItemkNN {

    /** Rating File          */ private static String fileVotes = "";
    /** Recommendation File  */ private static String fileRecs  = "";
    /** Rating Map           */ private static HashMap<Integer, HashMap<Integer, Float>> ratingsMap = new HashMap<>();
    /** Number of Recs       */ private static int numberOfRecs = 100;
    /** Number of Neighbors  */ private static int numberOfNeighs = 80;

    public static void main( String[] args ) throws IOException
    {
        System.out.println( "" );
        System.out.println( "" );
        System.out.println( "ItemkNN algorithm" );
        System.out.println( "This implementation was realized using Oracle Java 8" );
        System.out.println( "Usage: java -jar ItemkNN fileVotes fileRecs numberOfNeighs numberOfRecs" );
        System.out.println( "These parameters are set by default but you can override them passing alternative ones" );
        System.out.println( "" );
        System.out.println( "" );


        for (int i = 0; i < args.length; i++) {
            System.out.println(args[i]);
            if (i==0)fileVotes = args[i];
            if (i==1)fileRecs = args[i];
            if (i==2)numberOfNeighs = Integer.parseInt(args[i]);
            if (i==3)numberOfRecs = Integer.parseInt(args[i]);
        }

        ratingsMap = Utils.Companion.loadRatingsFile(fileVotes);
        Item_kNN_Model model = new Item_kNN_Model(ratingsMap,numberOfNeighs,numberOfRecs);
        HashSet<List<String>> results = new HashSet<>();
        ratingsMap.keySet().parallelStream().map(user -> model.scoreItems(user)).forEach(results::add);

        PrintWriter recs= new PrintWriter(new FileWriter(fileRecs));

        for (List<String> s : results) {
            if (s!=null){
                for (int i = 0; i < s.size(); i++) {
                    if (s.get(i)!=null){
                        recs.println(s.get(i));
                    }
                }
            }
        }
        recs.close();
    }
}


