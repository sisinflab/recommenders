import java.util.Random;
import java.util.Set;

public interface DataModel {
    public void update_u(int u, double[] fs);
    public void update_i(int i, double[] fs);
    public void update_i_bias(int i, double ib);
    public double predict(int u, int i);
    public double[] getUserFactors(int u);
    public double[] getItemFactors(int i);
    public double getItemBias(int i);
    public Random getRandom();
    public int getnUsers();
    public int getnItems();
    public Set<Integer> getItemsSet();
    public int getNumRatings();
    public Set<Integer> getUsersSet();
}
