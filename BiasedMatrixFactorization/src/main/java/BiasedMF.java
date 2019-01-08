import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class BiasedMF {


    private Random random;
    MFDataModelArray dataModel;
    private float learningRate = 0.05F;
    private float biasRegularization = 1.0F;
    private float userRegularization = 0.0025F;
    private float itemRegularization = 0.0025F;
    private int D = 10;
    private boolean updateUsers = true;
    private boolean updateItems = false;
    private boolean BoldDriver = false;
    private double last_loss;
    private double current_learnrate;
    private double learning_rate_decay;
    boolean FrequencyRegularization;
    private float BiasLearnRate;

    public BiasedMF(int D, float learningRate, float biasRegularization, float userRegularization, float itemRegularization, float BiasLearnRate, double learning_rate_decay, boolean updateUsers, boolean updateItems, boolean BoldDriver, boolean FrequencyRegularization){
        this.D = D;
        this.learningRate = learningRate;
        this.current_learnrate = learningRate;
        this.biasRegularization = biasRegularization;
        this.userRegularization = userRegularization;
        this.itemRegularization = itemRegularization;
        this.BiasLearnRate = BiasLearnRate;
        this.learning_rate_decay = learning_rate_decay;
        this.updateUsers = updateUsers;
        this.updateItems = updateItems;
        this.BoldDriver = BoldDriver;
        this.FrequencyRegularization = FrequencyRegularization;
    }

    // gaussian version
    public void initializeData(MFDataModelArray dataModel){
        this.dataModel = dataModel;
        this.random = this.dataModel.getRandom();

        if (BoldDriver)
            last_loss = ComputeObjective();
    }


    public void Train(MFDataModelArray dataModel, int numIters){
        initializeData(dataModel);

        System.out.println("initial loss " + last_loss);
        for (int it = 0; it < numIters; it++) {
            System.out.println("starting iteration " + it);
            List<Integer> ratingIndices = shuffle(dataModel.getNumRatings());
            for (int r : ratingIndices){
                AbstractMap.SimpleEntry<Integer, Integer> pair = dataModel.getRatingPair(r);
                iterateRMSE(pair.getKey(),pair.getValue(), updateUsers,updateItems, it);
            }
            UpdateLearnRate();
            System.out.println("iteration " + it + ": loss = " + last_loss);
        }
        System.out.println(": loss = " + last_loss);
    }

    public void UpdateLearnRate()
    {
        if (BoldDriver)
        {
            double loss = ComputeObjective();
            if (loss > last_loss)
                current_learnrate *= 0.5f;
            else if (loss < last_loss)
                current_learnrate *= 1.05f;
            last_loss = loss;
        }
        else
        {
            current_learnrate *= learning_rate_decay;
        }
    }


    public List<Integer> shuffle(int num){
        List<Integer> intlist = IntStream.range(0,num).boxed().collect(Collectors.toList());
        Collections.shuffle(intlist, random);
        return intlist;
    }

    //AGGIORNATA RMSE
    @SuppressWarnings("Duplicates")
    public void iterateRMSE(int u, int i, boolean update_u, boolean update_i, int it){

        double[] userFactors = dataModel.getUserFactors(u);
        double[] itemFactors = dataModel.getItemFactors(i);
        double userBias = dataModel.getUserBias(u);
        double itemBias = dataModel.getItemBias(i);
        double score = dataModel.predict(u,i);
        double sig_score =  (1.0/(1.0+Math.exp(-score)));

        double prediction = dataModel.getMinRating() + sig_score * dataModel.getRatingRangeSize();
        double err = dataModel.getRating(u,i) - prediction;

        //compute_gradient_common
        float gradient_common = (float) (err * sig_score * (1 - sig_score) * dataModel.getRatingRangeSize());

        float user_reg_weight = FrequencyRegularization ? (float) (userRegularization / Math.sqrt(dataModel.getUserItems(u).size())) : userRegularization;
        float item_reg_weight = FrequencyRegularization ? (float) (itemRegularization / Math.sqrt(dataModel.getItemPopularity(i))) : itemRegularization;

        // update bias terms
        if (update_u){
            double d =  (gradient_common - biasRegularization * user_reg_weight * userBias);
            dataModel.update_u_bias(u,userBias + BiasLearnRate * current_learnrate * d);
        }
        if (update_i) {
            double d = (gradient_common - biasRegularization * item_reg_weight * itemBias);
            dataModel.update_i_bias(i,itemBias + BiasLearnRate * current_learnrate * d);
        }


        for (int f = 0; f < D; f++) {
            double w_uf = userFactors[f];
            double h_if = itemFactors[f];

            if (update_u) {
                double update = gradient_common * h_if - user_reg_weight * w_uf;
                userFactors[f] =  (w_uf + current_learnrate * update);
            }

            if (update_i) {
                double update = gradient_common * w_uf - item_reg_weight * h_if;
                itemFactors[f] =  (h_if + current_learnrate * update);
            }
        }
        dataModel.update_u(u,userFactors);
        dataModel.update_i(i,itemFactors);

    }


    //AGGIORNATA RMSE
    public double ComputeObjective(){

        AtomicReference<Double> complexity = new AtomicReference<>((double) 0);
        if(FrequencyRegularization){
            dataModel.getUsersSet().stream().forEach(user -> {
                int userRatings =dataModel.getUserItems(user).size();
                if(userRatings > 0){
                    complexity.updateAndGet(v -> (v + (userRegularization / Math.sqrt(userRatings))*Math.pow(euclideanNorm(dataModel.getUserFactors(user)),2) ));
                    complexity.updateAndGet(v -> (v + (userRegularization / Math.sqrt(userRatings)) * biasRegularization * Math.pow(dataModel.getUserBias(user),2) ));
                }
            });
            dataModel.getItemsSet().stream().forEach(item -> {
                int itemRatings = dataModel.getItemPopularity(item);
                complexity.updateAndGet(v -> (v + (itemRegularization / Math.sqrt(itemRatings))*Math.pow(euclideanNorm(dataModel.getItemFactors(item)),2) ));
                complexity.updateAndGet(v -> (v + (itemRegularization / Math.sqrt(itemRatings)) * biasRegularization * Math.pow(dataModel.getItemBias(item),2) ));
            });
        }else{
            dataModel.getUsersSet().stream().forEach(user -> {
                int userRatings =dataModel.getUserItems(user).size();
                complexity.updateAndGet(v -> (v + userRatings * userRegularization * Math.pow(euclideanNorm(dataModel.getUserFactors(user)),2) ));
                complexity.updateAndGet(v -> (v + userRatings * userRegularization * biasRegularization * Math.pow(dataModel.getUserBias(user),2) ));
            });
            dataModel.getItemsSet().stream().forEach(item -> {
                int itemRatings = dataModel.getItemPopularity(item);
                complexity.updateAndGet(v -> (v + itemRatings *itemRegularization * Math.pow(euclideanNorm(dataModel.getItemFactors(item)),2) ));
                complexity.updateAndGet(v -> (v + itemRatings *itemRegularization * biasRegularization * Math.pow(dataModel.getItemBias(item),2) ));
            });
        }
        return ComputeLoss() + complexity.get();
    }

    //AGGIORNATA RMSE
    public double ComputeLoss(){return dataModel.computeSquaredErrorSum();}

    //AGGIORNATA RMSE
    public double euclideanNorm(double[] array){return Math.sqrt(Arrays.stream(array).map(d -> d * d).sum());}

    public double scoreItems(int u, int i){
        return dataModel.predictMyMediaLite(u,i);
    }

    public Random getRandom() {
        return random;
    }

}
