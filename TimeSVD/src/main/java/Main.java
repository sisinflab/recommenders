import java.io.*;

public class Main {
    private static String trainFile = "/home/starlord/Desktop/train.txt";
    private static String crossFile = "/home/starlord/Desktop/cross.txt";
    private static String testFile = "/home/starlord/Documents/DATASETS/toys_amazon/testset.tsv";
    private static String outputFile = "/home/starlord/Desktop/TimeSVD.tsv";
    private static String trainingSetFile = "/home/starlord/Documents/DATASETS/toys_amazon/trainingset.tsv";
    private static int minTimestamp = 1377820800;

    public static void main(String[] args) throws IOException {

        System.out.println( "" );
        System.out.println( "" );
        System.out.println( "TimeSVD++" );
        System.out.println( "This implementation was realized using Oracle Java 8" );
        System.out.println( "Usage: java -jar TimeSVD trainingSetFile outputFile testFile trainFile crossFile referringTimestamp" );
        System.out.println( "These parameters are set by default but you can override them passing alternative ones" );
        System.out.println( "" );
        System.out.println( "" );

        for (int i = 0; i < args.length; i++) {
            System.out.println(args[i]);
            if (i==0)trainingSetFile = args[i];
            if (i==1)outputFile = args[i];
            if (i==2)testFile = args[i];
            if (i==3)trainFile = args[i];
            if (i==4)crossFile = args[i];
            if (i==5)minTimestamp = Integer.parseInt(args[i]);
        }

        BufferedReader reader = new BufferedReader(new FileReader(trainingSetFile));
        PrintWriter ft = new PrintWriter(new FileWriter(trainFile));
        PrintWriter fc = new PrintWriter(new FileWriter(crossFile));

        String line;
        int k = 0;
        while((line = reader.readLine()) != null){
            String[] sl = line.split("\t");
            int timestamp = Integer.valueOf(sl[3]);
            if (timestamp<minTimestamp){
                minTimestamp = timestamp;
            }
            if((k % 100)==0){
                fc.println(line);
            }else{
                ft.println(line);
            }
            k++;
        }

        System.out.println(minTimestamp);

        reader.close();
        ft.close();
        fc.close();

        TimeSVD svd = new TimeSVD(null, null, 0, null, null, trainFile, crossFile, testFile, outputFile, trainingSetFile, minTimestamp);
        double rmse = svd.MyTrain();
    }
}
