import java.io.*;
import java.util.*;


public class TimeSVD {


    private static int RAND_MAX = 32767;

    private static double[] Tu;           //variable for mean time of user
    private static double[] Alpha_u;
    private static double[] Bi;
    private static double[][] Bi_Bin;
    private static double[] Bu;
    private static ArrayList<Map<Integer,Double>> Bu_t = new ArrayList();
    private static ArrayList<Map<Integer,Double>> Dev = new ArrayList();    //save the result of CalDev(userId,time)
    private static double[][] Qi;
    private static double[][] Pu;
    private static double[][] y;
    private static double[][] sumMW;    //save the sum of Pu
    private static String train_file;
    private static String cross_file;
    private static String test_file;
    private static String out_file;
    private static Map <Integer,ArrayList<Pair <Pair<Integer,Double>, Integer>>> train_data = new HashMap();
    private static ArrayList <Pair <Pair<Integer,Integer>, Pair <Integer, Double>>> test_data = new ArrayList();


    private static int userNum = 602;  //number of users
    private static int itemNum = 3375;   //number of items
    private static int timeNum = 5115;    //number of days(time)
    private static int binNum = 30;       //number of time bins
    private static double AVG = 3.60073;  //average score
    private static double G_alpha = 0.00001;        //gamma for alpha
    private static double L_alpha = 0.0004;   //learning rate for alpha
    private static double L_pq = 0.015;       //learning rate for Pu & Qi
    private static double G = 0.007;                //general gamma
    private static double Decay = 0.9;        //learning rate decay factor
    private static double L = 0.005;          //general learning rate
    private static int factor = 50;           //number of factors

    private static int binSize = 70;

    private static int Nrecc = 300;
    private static Map <Integer, Integer> itemToNum = new HashMap<Integer, Integer>();
    private static Map <Integer, Integer> timeToNum = new HashMap<Integer, Integer>();
    private static Map <Integer, Integer> userToNum = new HashMap<Integer, Integer>();
    private static Map <Integer, Integer> numToUser = new HashMap<Integer, Integer>();

    private static Map<Integer,Map<Integer,Integer>> tempUserItems = new HashMap<>();

    private static Set<Integer> itemsSet = new HashSet<Integer>();

    private static int referringTimestamp = 1377820800; // toys_amazon

    private static int minTimestamp;


    public TimeSVD(double[] bi, double[] bu, int k, double[][] qi, double[][] pu, String trainFile, String crossFile, String testFile, String outFile, String trainingSetFile, int timestamp) throws IOException{

        minTimestamp = timestamp;
        referringTimestamp = referringTimestamp - minTimestamp;
        referringTimestamp = Math.round(referringTimestamp/(24*3600));

        System.out.println(referringTimestamp);

        Set<Integer> users = new HashSet<>();
        Set<Integer> times = new HashSet<>();
        ArrayList<Double> avg = new ArrayList();
        BufferedReader r = new BufferedReader(new FileReader(trainingSetFile));

        String l;
        while((l = r.readLine()) != null){
            String[] sl = l.split("\t");
            users.add(Integer.parseInt(sl[0]));
            itemsSet.add(Integer.parseInt(sl[1]));
            avg.add(Double.parseDouble(sl[2]));
            int t = Integer.parseInt(sl[3]);
            t = t - minTimestamp;
            t = Math.round(t/(24*3600));

            times.add(t);
        }

        userNum = users.size();
        itemNum = itemsSet.size();
        timeNum = referringTimestamp;

        binNum = timeNum/binSize + 1;


        AVG = 0;
        for(Double rate:avg){
            AVG += rate;
        }
        AVG = AVG/avg.size();


        test_file = testFile;
        out_file = outFile;


        if(bi == null){
            Bi = new double[itemNum];
            Arrays.fill(Bi, 0);
        }else{
            Bi = bi;
        }

        if(bu == null){
            Bu = new double[userNum];
            Arrays.fill(Bu, 0);
        }
        else{
            Bu = bu;
        }

        Alpha_u = new double[userNum];
        Arrays.fill(Alpha_u, 0);

        Bi_Bin = new double[itemNum][binNum];

        for (double[] row:Bi_Bin){
            Arrays.fill(row, 0);
        }


        if(qi == null){
            Qi = new double[itemNum][factor];
            y = new double[itemNum][factor];
            for(int i=0;i<itemNum;i++){
                for(int j=0;j<factor;j++){
                    int randomNum = (int)(Math.random() * RAND_MAX);
                    Qi[i][j] = 0.1 * (randomNum / (RAND_MAX + 1.0)) / Math.sqrt(factor);
                    y[i][j] = 0.0;
                }
            }
        }
        else{
            Qi = qi;
        }

        if(pu == null){
            sumMW = new double[userNum][factor];
            Pu = new double[userNum][factor];
            for(int i=0;i<userNum;i++){
                for(int j=0;j<factor;j++){
                    int randomNum = (int)(Math.random() * RAND_MAX);
                    sumMW[i][j] = 0.1 * (randomNum / (RAND_MAX + 1.0)) / Math.sqrt(factor);
                    Pu[i][j] = 0.1 * (randomNum / (RAND_MAX + 1.0)) / Math.sqrt(factor);
                }
            }
        }   else{
            Pu = pu;
        }

        BufferedReader reader = new BufferedReader(new FileReader(trainFile));

        int userId,itemId;
        double rating;
        int t;
        String line;

        Map <Integer,ArrayList<Pair <Pair<Integer,Double>, Integer> >> temp_train_data = new HashMap();

        Map<Integer,Integer> innerMap;
        while((line = reader.readLine()) != null){
            String splittedLine[] = line.split("\t");
            userId = Integer.parseInt(splittedLine[0]);
            itemId = Integer.parseInt(splittedLine[1]);
            rating = Double.parseDouble(splittedLine[2]);
            t = Integer.parseInt(splittedLine[3]);
            t = t - minTimestamp;
            t = Math.round(t/(24*3600));

            innerMap = tempUserItems.get(userId);
            if(innerMap == null){
                innerMap = new HashMap<>();
            }
            innerMap.put(itemId,itemId);
            tempUserItems.put(userId,innerMap);


            temp_train_data.computeIfAbsent(userId, k1 -> new ArrayList());
            temp_train_data.get(userId).add(new Pair(new Pair(itemId,rating),t));
        }
        reader.close();



        reader = new BufferedReader(new FileReader(crossFile));
        while((line = reader.readLine()) != null) {
            String splittedLine[] = line.split("\t");
            userId = Integer.parseInt(splittedLine[0]);
            itemId = Integer.parseInt(splittedLine[1]);
            rating = Double.parseDouble(splittedLine[2]);
            t = Integer.parseInt(splittedLine[3]);
            t = t - minTimestamp;
            t = Math.round(t/(24*3600));
            test_data.add(new Pair(new Pair(userId, itemId), new Pair(t,rating)));
        }
        reader.close();

        Set<Integer> allId = new HashSet<Integer>();
        Set<Integer> allItems = new HashSet<Integer>();
        Set<Integer> allTimes = new HashSet<Integer>();
        for (Integer user : temp_train_data.keySet()) {
            allId.add(user);
            ArrayList<Pair<Pair<Integer, Double>, Integer>> inner = temp_train_data.get(user);
            for (Pair<Pair<Integer, Double>, Integer> i : inner) {
                allItems.add(i.first.first);
                allTimes.add(i.second);
            }
        }

        ArrayList<Integer> allIdList = new ArrayList(allId);
        Collections.sort(allIdList);
        int count = 0;
        for (Integer id:allIdList){
            train_data.put(count, temp_train_data.get(id));
            userToNum.put(id, count);
            numToUser.put(count, id);
            count++;
        }

        ArrayList<Integer> allItemsList = new ArrayList(allItems);
        Collections.sort(allItemsList);
        count = 0;
        for(Integer item:allItemsList){
            itemToNum.put(item, count);
            count++;
        }

        ArrayList<Integer> allTimesList = new ArrayList<Integer>(allTimes);
        Collections.sort(allTimesList);
        count = 0;
        for (Integer time:allTimesList){
            timeToNum.put(time, count);
            count++;
        }



        /////////////////


        Tu = new double[userNum];
        for(int i=0;i<userNum;i++){
            int tmp = 0;
            if(train_data.get(i).size()==0)
            {
                Tu[i] = 0.0;
                continue;
            }
            for(int j=0;j<train_data.get(i).size();j++){
                tmp += train_data.get(i).get(j).second;
            }
            Tu[i] = tmp/train_data.get(i).size();
        }

        for(int i=0;i<userNum;i++){
            Map<Integer,Double> tmp = new HashMap();
            for(int j=0;j<train_data.get(i).size();j++){
                tmp.putIfAbsent(train_data.get(i).get(j).second, 0.0000001);
            }
            Bu_t.add(tmp);
        }

        for(int i=0;i<userNum;i++){
            Map<Integer,Double> tmp = new HashMap();
            Dev.add(tmp);
        }

    }

    public double MyTrain() throws IOException{
        double preRmse = 1000;
        double curRmse = 0;

        for(int i=0;i<1000;i++){
            Train();
            curRmse = Validate(AVG,Bu,Bi,Pu,Qi);
            System.out.println("test_Rmse in step " + i + ": " + curRmse);
            if (curRmse >= preRmse - 0.00005){
                break;
            }else{
                preRmse = curRmse;
            }
        }



        PrintWriter out = new PrintWriter(new FileWriter(out_file));
        BufferedReader reader = new BufferedReader(new FileReader(test_file));


        System.out.println(referringTimestamp);

        Iterator<Integer> it = train_data.keySet().iterator();
        while (it.hasNext()){
            ArrayList<SimilarityStructure> list = new ArrayList<>();
            int user = it.next();
            Map<Integer,Integer> innerMap = tempUserItems.get(numToUser.get(user));
            for(Integer item:itemsSet){
                if(innerMap.get(item) == null) {
                    if (itemToNum.get(item) != null) {
                        double score = predictScore(AVG, user, item, referringTimestamp);
                        float score2 = (float) score;
                        list.add(new SimilarityStructure(item, score2));
                    }
                }
            }
            Collections.sort(list);
            int exit = 0;
            for(SimilarityStructure struct:list){
                out.println(numToUser.get(user) + "\t" + struct.getItem() + "\t" + struct.getSimilarity());
                if (exit > Nrecc){
                    break;
                }
                exit++;
            }
        }



        reader.close();
        out.close();
        return curRmse;
    }

    private static void Train(){
        int userId, itemId;
        double rating;
        int time;
        for (userId = 0; userId < userNum; userId++) {
            int sz = train_data.get(userId).size();

            double sqrtNum = 0;
            double[] tmpSum = new double[factor];
            Arrays.fill(tmpSum,0);
            if (sz>1) sqrtNum = 1/(Math.sqrt(sz));
            for (int k = 0; k < factor; k++) {
                double sumy = 0;
                for (int i = 0; i < sz; i++) {
                    int itemI = itemToNum.get(train_data.get(userId).get(i).first.first);
                    sumy += y[itemI][k];
                }
                sumMW[userId][k] = sumy;
            }
            for (int i = 0; i < sz; i++) {
                itemId = train_data.get(userId).get(i).first.first;
                rating = train_data.get(userId).get(i).first.second;
                time = train_data.get(userId).get(i).second;


                double predict = predictScore(AVG,userId,itemId,time);

                itemId = itemToNum.get(itemId);

                double error = rating - predict;
                Bu[userId] += G * (error - L * Bu[userId]);
                Bi[itemId] += G * (error - L * Bi[itemId]);
                Bi_Bin[itemId][CalBin(time)] += G * (error - L * Bi_Bin[itemId][CalBin(time)]);
                Alpha_u[userId] += G_alpha * (error * CalDev(userId,time)  - L_alpha * Alpha_u[userId]);

                double oldBu = Bu_t.get(userId).get(time);
                oldBu += G * (error - L * oldBu);
                Bu_t.get(userId).put(time, oldBu);


                for(int k=0;k<factor;k++){
                    double uf = Pu[userId][k];
                    double mf = Qi[itemId][k];
                    Pu[userId][k] += G * (error * mf - L_pq * uf);
                    Qi[itemId][k] += G * (error * (uf+sqrtNum*sumMW[userId][k]) - L_pq * mf);
                    tmpSum[k] += error*sqrtNum*mf;


                }
            }
            for (int j = 0; j < sz; j++) {
                itemId = itemToNum.get(train_data.get(userId).get(j).first.first);
                for (int k = 0; k < factor; ++k) {
                    double tmpMW = y[itemId][k];
                    y[itemId][k] += G*(tmpSum[k]- L_pq *tmpMW);
                    sumMW[userId][k] += y[itemId][k] - tmpMW;
                }
            }
        }
        for (userId = 0; userId < userNum; userId++) {
            int sz = train_data.get(userId).size();
            double sqrtNum = 0;
            if (sz>1) sqrtNum = 1.0/Math.sqrt(sz);
            for (int k = 0; k < factor; ++k) {
                double sumy = 0;
                for (int i = 0; i < sz; ++i) {
                    int itemI = itemToNum.get(train_data.get(userId).get(i).first.first);
                    sumy += y[itemI][k];
                }
                sumMW[userId][k] = sumy;
            }
        }
        G *= Decay;
        G_alpha *= Decay;
    }

    private static double Validate(double avg, double[] bu, double[] bi, double[][] pu, double[][] qi){
        int userId,itemId,t;
        double rating;
        int n = 0;
        double rmse = 0;
        for (Pair ch:test_data){
            String[] splittedFirst = ch.first.toString().split(",");
            String[] splittedSecond = ch.second.toString().split(",");


            userId = userToNum.get(Integer.parseInt(splittedFirst[0].substring(1)));
            itemId = Integer.parseInt(splittedFirst[1].substring(1, splittedFirst[1].length() - 1));
            t = Integer.parseInt(splittedSecond[0].substring(1));
            rating = Double.parseDouble(splittedSecond[1].substring(1, splittedSecond[1].length() - 1));


            if(itemToNum.get(itemId) != null) {
                n++;
                double pScore = predictScore(avg, userId, itemId, t);
                rmse += (rating - pScore) * (rating - pScore);
            }

        }
        return Math.sqrt(rmse/n);
    }

    //   prediction formula:
    //   avg + Bu + Bi
    //   + Bi_Bin,t + Alpha_u*Dev + Bu_t
    //   + Qi^T(Pu + |R(u)|^-1/2 \sum yi
    private static Double predictScore(double avg, int userId, int itemId,int time){

        itemId = itemToNum.get(itemId); // TODO delete

        double tmp = 0.0;
        int sz = train_data.get(userId).size();
        double sqrtNum = 0;
        if (sz>1) sqrtNum = 1/(Math.sqrt(sz));
        for(int i=0;i<factor;i++){
            tmp += (Pu[userId][i] +sumMW[userId][i]*sqrtNum) * Qi[itemId][i];
        }

        double score = avg + Bu[userId] + Bi[itemId] + Bi_Bin[itemId][CalBin(time)] + Alpha_u[userId]*CalDev(userId,time) + tmp;

        if(score > 5){
            score = 5;
        }
        if(score < 1){
            score = 1;
        }
        return score;
    }

    //compute dev_u(t) = sign(t-tu)*|t-tu|^0.4 and save the result for saving the time
    private static double CalDev(int user, int timeArg) {
        if(Dev.get(user).get(timeArg)!=null)return Dev.get(user).get(timeArg);
        double tmp = Math.signum(timeArg - Tu[user]) * Math.pow((Math.abs(timeArg - Tu[user])), 0.4);
        Dev.get(user).put(timeArg, tmp);
        return tmp;
    }


    private static int CalBin(int timeArg) {
        return timeArg/binSize;
    }


}
