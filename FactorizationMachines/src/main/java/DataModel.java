import java.util.Random;
import java.util.Set;

public interface DataModel {
    void update_uf(int u, double[] fs);

    void update_if(int i, double[] fs);

    void update_u_w(int i, double ib);

    void update_i_w(int i, double ib);

    double predict(int u, int i);

    double[] getUserFactors(int u);

    double[] getItemFactors(int i);

    double getUserBias(int i);

    double getItemBias(int i);

    Random getRandom();

    int getnUsers();

    int getnItems();

    Set<Integer> getItemsSet();

    int getNumRatings();

    Set<Integer> getUsersSet();
}
