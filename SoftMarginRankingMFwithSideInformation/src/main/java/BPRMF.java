import java.util.*;
import java.util.stream.IntStream;


public class BPRMF {


    private Random random;

    DataModel dataModel;
    private float learningRate;
    private float biasRegularization;
    private float userRegularization;
    private float positiveItemRegularization;
    private float negativeItemRegularization;
    private boolean updateNegativeItemsFactors;
    private int D;
    private boolean updateUsers;
    private boolean updateItems;

    private int numUsers;
    private int numItems;

    private Sampler sampler;

    private ArrayList<Triple> lossSamples;


    public BPRMF(int D, float learningRate, float biasRegularization, float userRegularization, float positiveItemRegularization, float negativeItemRegularization, boolean updateNegativeItemsFactors, boolean updateUsers, boolean updateItems){
        this.D = D;
        this.learningRate = learningRate;
        this.biasRegularization = biasRegularization;
        this.userRegularization = userRegularization;
        this.positiveItemRegularization = positiveItemRegularization;
        this.negativeItemRegularization = negativeItemRegularization;
        this.updateNegativeItemsFactors = updateNegativeItemsFactors;
        this.updateUsers = updateUsers;
        this.updateItems = updateItems;
    }




    // gaussian version
    public void initializeData(DataModel dataModel, Sampler samp, float initMean, float initStdDev){
        this.dataModel = dataModel;
        this.random = this.dataModel.getRandom();
        sampler = samp;

        numUsers = dataModel.getnUsers();
        numItems = dataModel.getnItems();
    }


    public void createLossSamples(int numLossSamples){
        System.out.println("sampling " + numLossSamples + " <user,item i,item j> triples...");

        Map<Integer, ArrayList<Integer>> tempMap = sampler.sampleTripleMymedialite(numLossSamples);
        lossSamples = new ArrayList<>();
        ArrayList<Integer> listU = tempMap.get(1);
        ArrayList<Integer> listI = tempMap.get(2);
        ArrayList<Integer> listJ = tempMap.get(3);
        for (int i = 0; i < listU.size(); i++){
            Triple<Integer,Integer,Integer> triple = new Triple<>(listU.get(i),listI.get(i), listJ.get(i));
            lossSamples.add(triple);
        }
    }

    public void Train(DataModel dataModel,Sampler samp, int numIters, float initMean, float initStdDev, String samplerName){
        initializeData(dataModel, samp, initMean, initStdDev);

        // apply rule of thumb to decide num samples over which to compute loss
        int numLossSamples  = (int)(Math.sqrt(numUsers) * 100);
        createLossSamples(numLossSamples);
        int numPosEvents = dataModel.getNumRatings();

        for (int it = 0; it < numIters; it++) {
            System.out.println("starting iteration " + it);

            Map<Integer, ArrayList<Integer>> tempMap = sampler.Sample(samplerName, numPosEvents);
            ArrayList<Integer> listU = tempMap.get(1);
            ArrayList<Integer> listI = tempMap.get(2);
            ArrayList<Integer> listJ = tempMap.get(3);

            for (int i = 0; i < listU.size(); i++){
                updateFactors(listU.get(i),listI.get(i), listJ.get(i), updateUsers, updateItems, it);
            }
        }
    }



    public void updateFactors(int u, int i, int j, boolean update_u, boolean update_i, int it){
        boolean update_j = updateNegativeItemsFactors;

        double[] userFactors = dataModel.getUserFactors(u);
        double[] itemFactors_i = dataModel.getItemFactors(i);
        double[] itemFactors_j = dataModel.getItemFactors(j);
        double itemBias_i = dataModel.getItemBias(i);
        double itemBias_j = dataModel.getItemBias(j);
        double x_uij = dataModel.predict(u,i)-dataModel.predict(u,j);

        if (x_uij > 0) return;

        // update bias terms
        if (update_i) {
            double bias_update = 1 - biasRegularization * itemBias_i;
            dataModel.update_i_bias(i,itemBias_i + learningRate * bias_update);
        }
        if (update_j){
            double bias_update = -1 - biasRegularization * itemBias_j;
            dataModel.update_i_bias(j,itemBias_j + learningRate * bias_update);
        }


        for (int f = 0; f < D; f++) {
            double w_uf = userFactors[f];
            double h_if = itemFactors_i[f];
            double h_jf = itemFactors_j[f];

            if (update_u) {
                double update = (h_if - h_jf) - userRegularization * w_uf;
                userFactors[f] =  (w_uf + learningRate * update);
            }

            if (update_i) {
                double update = w_uf  - positiveItemRegularization * h_if;
                itemFactors_i[f] =  (h_if + learningRate * update);
            }

            if (update_j) {
                double update = -w_uf  - negativeItemRegularization * h_jf;
                itemFactors_j[f] =  (h_jf + learningRate * update);
            }
        }
        dataModel.update_u(u,userFactors);
        dataModel.update_i(i,itemFactors_i);
        dataModel.update_i(j,itemFactors_j);

    }



    public double scoreItems(int u, int i){
        return dataModel.predict(u,i);
    }





    public double dotProduct(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i] * b[i];
        }
        return sum;
    }


}
