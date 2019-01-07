import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.*;



public class WRMF {


    private Random random;
    MFDataModelArray dataModel;
    private int D = 10;
    private float alpha;
    private float regularization;

    public WRMF(int D, float alpha, float regularization){
        this.D = D;
        this.alpha = alpha;
        this.regularization = regularization;
    }

    // gaussian version
    public void initializeData(MFDataModelArray dataModel){
        this.dataModel = dataModel;
    }


    public void Train(MFDataModelArray dataModel, int numIters){
        initializeData(dataModel);

        for (int it = 0; it < numIters; it++) {
            System.out.println("starting iteration " + it);
            OptimizeUserFactors(dataModel.getFeedbackUserMatrix());
            OptimizeItemFactors(dataModel.getFeedbackItemMatrix());
        }
    }

    private void OptimizeUserFactors(HashMap<Integer, ArrayList<Integer>> userFeedback){
        double[][] HH = ComputeSquareMatrix(dataModel.getIfMatrix());
        userFeedback.entrySet().stream().forEach(user -> {
            OptimizeUserFactors2(user.getKey(), user.getValue(), HH);
        });
    }
    private void OptimizeUserFactors2(int user, ArrayList<Integer> items, double[][] HH){
        double[][] HC_minus_IH = new double[D][D];
        for (int f_1 = 0; f_1 < D; f_1++){
            for (int f_2 = f_1; f_2 < D; f_2++)
            {
                double d = 0;
                for (int i : items){
                    double[] h_i = dataModel.getItemFactors(i);
                    d += h_i[f_1] * h_i[f_2];
                    HC_minus_IH[f_1][f_2] = d * alpha;
                    HC_minus_IH[f_2][f_1] = d * alpha;
                }
            }
        }
        double[] HCp = new double[D];
        for (int f = 0; f < D; f++){
            double d = 0;
            for (int i : items){
                double[] h_i = dataModel.getItemFactors(i);
                d += h_i[f];
                HCp[f] = d * (1 + alpha);
            }
        }
        double[][] m = new double[D][D];
        for (int f_1 = 0; f_1 < D; f_1++){
            for (int f_2 = f_1; f_2 < D; f_2++)
            {
                double d = HH[f_1][f_2] + HC_minus_IH[f_1][f_2];
                if (f_1 == f_2)
                    d += regularization;
                m[f_1][f_2] = d;
                m[f_2][f_1] = d;
            }
        }
        RealMatrix m_matrix = MatrixUtils.createRealMatrix(m);
        double[][] m_inv = MatrixUtils.inverse(m_matrix).getData();
        double[] u_f = new double[D];
        for (int f = 0; f < D; f++){
            double d = 0;
            for (int f_2 = 0; f_2 < D; f_2++){
                d += m_inv[f][f_2] * HCp[f_2];
            }
            u_f[f] = (float) d;
        }
        dataModel.update_u(user,u_f);
    }


    private void OptimizeItemFactors(HashMap<Integer, ArrayList<Integer>> itemFeedback){
        double[][] HH = ComputeSquareMatrix(dataModel.getUfMatrix());
        itemFeedback.entrySet().stream().forEach(item -> {
            OptimizeItemFactors2(item.getKey(), item.getValue(), HH);
        });
    }
    private void OptimizeItemFactors2(int item, ArrayList<Integer> users, double[][] HH){
        double[][] HC_minus_IH = new double[D][D];
        for (int f_1 = 0; f_1 < D; f_1++){
            for (int f_2 = f_1; f_2 < D; f_2++)
            {
                double d = 0;
                for (int u : users){
                    double[] h_u = dataModel.getUserFactors(u);
                    d += h_u[f_1] * h_u[f_2];
                    HC_minus_IH[f_1][f_2] = d * alpha;
                    HC_minus_IH[f_2][f_1] = d * alpha;
                }
            }
        }
        double[] HCp = new double[D];
        for (int f = 0; f < D; f++){
            double d = 0;
            for (int u : users){
                double[] h_u = dataModel.getUserFactors(u);
                d += h_u[f];
                HCp[f] = d * (1 + alpha);
            }
        }
        double[][] m = new double[D][D];
        for (int f_1 = 0; f_1 < D; f_1++){
            for (int f_2 = f_1; f_2 < D; f_2++)
            {
                double d = HH[f_1][f_2] + HC_minus_IH[f_1][f_2];
                if (f_1 == f_2)
                    d += regularization;
                m[f_1][f_2] = d;
                m[f_2][f_1] = d;
            }
        }
        RealMatrix m_matrix = MatrixUtils.createRealMatrix(m);
        double[][] m_inv = MatrixUtils.inverse(m_matrix).getData();
        double[] i_f = new double[D];
        for (int f = 0; f < D; f++){
            double d = 0;
            for (int f_2 = 0; f_2 < D; f_2++){
                d += m_inv[f][f_2] * HCp[f_2];
            }
            i_f[f] = (float) d;
        }
        dataModel.update_i(item,i_f);
    }


    private double[][] ComputeSquareMatrix(double[][] m){
        int dim1 = m.length;
        double[][] mm = new double[D][D];
        // mm is symmetric
        for (int f_1 = 0; f_1 < D; f_1++)
            for (int f_2 = f_1; f_2 < D; f_2++)
            {
                double d = 0;
                for (int i = 0; i < dim1; i++)
                    d += m[i][f_1] * m[i][f_2];
                mm[f_1][f_2] = d;
                mm[f_2][f_1] = d;
            }
        return mm;
    }

    public double scoreItems(int u, int i){
        return dataModel.predict(u,i);
    }
}
