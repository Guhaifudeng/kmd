package src.main.java2;



import fig.basic.Pair;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yishuihan on 17-7-3.
 */
public class ChameleonTool {
    private Set<Integer> set_s = new HashSet<>();
    private ConcurrentHashMap<Integer,HashMap<Integer,Double>> sparse_mat = null;
    private Double miss_value = -1.0;//缺失值补全　可能没用
    private Double threshold = -1.0;//初始最小聚类时阀值
    private Integer min_cluster_max_num = 3;//初始最小聚类时，最小聚类最大数目
    private Double alpha2 = 1.0;//相对互连性\相对相似性倾向
    private Double alpha1 = 1.0;//相对互连性\相对相似性倾向
    private Integer knn = 0;//

    public Integer getKnn() {
        return knn;
    }

    public void setKnn(Integer knn) {
        this.knn = knn;
    }

    public ChameleonTool(ConcurrentHashMap<Integer,HashMap<Integer,Double>> sparse_mat , Set<Integer> set_s){
        this.sparse_mat = sparse_mat;
        this.set_s = set_s;
    }
    public HashMap<Integer,Double> getPointTopN(Integer o){
        return this.sparse_mat.get(o);
    }

    public Double getPairCenterPointSim(int o1, int o2){

        HashMap<Integer,Double> topN_o1 = sparse_mat.get(o1);
        HashMap<Integer,Double> topN_o2 = sparse_mat.get(o2);
        if(topN_o1.containsKey(o2)){
            return topN_o1.get(o2);
        }else if(topN_o2.containsKey(o1)){
            return  topN_o2.get(o1);
        }else{
            return 0.0;
        }

    }
    public Double getPairCenterPointSim(int o1,int o2,boolean addEC){
        HashMap<Integer,Double> topN_o1 = sparse_mat.get(o1);
        HashMap<Integer,Double> topN_o2 = sparse_mat.get(o2);
//
        if(addEC==false){//SEC
            if(topN_o1.containsKey(o2)){
                return topN_o1.get(o2);
            }else if(topN_o2.containsKey(o1)){
                return  topN_o2.get(o1);
            }
        }else {//EC
            if(topN_o1.containsKey(o2)&&topN_o2.containsKey(o1)){
                return topN_o1.get(o2);
            }
        }
        return 0.0;
    }
    public Double getNearestPointSim(int o1){
        HashMap<Integer,Double> topN_o1 = sparse_mat.get(o1);
        Double max_sim = Double.MIN_VALUE;
        for(Map.Entry<Integer,Double> entry:topN_o1.entrySet()){
            if(max_sim <entry.getValue()){
                max_sim = entry.getValue();
            }

        }
        return max_sim;
    }
    public boolean ableAddPointToClusterWithThreshold(Cluster cluster,Integer o1){
        ArrayList<Integer> points = cluster.getPoints();
        for(Integer point: points){
            if(this.getPairCenterPointSim(point,o1)<threshold){
                return false;
            }
        }
        return true;
    }

    public boolean ableAddPointToClusterWithThresholdAndNum(Cluster cluster,Integer o1){

        ArrayList<Integer> points = cluster.getPoints();
        if(points.size()+1>this.min_cluster_max_num)
            return false;
        for(Integer point: points){
            if(this.getPairCenterPointSim(point,o1)<threshold){
                return false;
            }
        }
        return true;
    }
    //绝对相连性
    public Double calEC(Cluster cluster1,Cluster cluster2){

        ArrayList<Integer> points1 = cluster1.getPoints();
        ArrayList<Integer> points2 = cluster2.getPoints();
        Double sum_edge_weight = 0.0;
        int edge_num = 0;
        for(Integer point1:points1){
            for(Integer point2:points2){

                Double tmp = this.getPairCenterPointSim(point1,point2);
                if(tmp < 1e-12 && tmp >-1e-12){
                    continue;
                }
                sum_edge_weight += tmp;
                edge_num++;
            }
        }
        if(edge_num == 0){
            return Double.MIN_VALUE;
        }else{
            return sum_edge_weight;
        }
    }
    //绝对相似性
    public Pair<Double,Integer> calSEC(Cluster cluster1, Cluster cluster2){
        ArrayList<Integer> points1 = cluster1.getPoints();
        ArrayList<Integer> points2 = cluster2.getPoints();
        Double sum_edge_weight = 0.0;
        int edge_num = 0;
        for(Integer point1:points1){
            for(Integer point2:points2){
                Double tmp = this.getPairCenterPointSim(point1,point2,true);
                if(tmp < 1e-12 && tmp >-1e-12){
                    continue;
                }
                sum_edge_weight += tmp;
                edge_num++;
            }
        }
        if(edge_num == 0){
            return new Pair<>(Double.MIN_VALUE,1);
        }else{
            return new Pair<>(sum_edge_weight,edge_num);
        }
    }
    //绝对相似性
    public Double calSEC(Cluster cluster1, Cluster cluster2,boolean dir_sec){
        ArrayList<Integer> points1 = cluster1.getPoints();
        ArrayList<Integer> points2 = cluster2.getPoints();
        Double sum_edge_weight = 0.0;
        int edge_num = 0;
        for(Integer point1:points1){
            for(Integer point2:points2){
                Double tmp = this.getPairCenterPointSim(point1,point2,false);
                if(tmp < 1e-12 && tmp >-1e-12){
                    continue;
                }
                sum_edge_weight += tmp;
                edge_num++;
            }
        }
        if(edge_num == 0){
            return Double.MIN_VALUE;
        }else{
            return sum_edge_weight/edge_num;
        }
    }
    //相对相连性
    public Double calRI(Cluster cluster1,Cluster cluster2,Pair<Double,Integer> EC_Edge){

        Double EC_1_2 = EC_Edge.getFirst();
//        if(cluster1.getPointSize()==2 && cluster2.getPointSize()==2) {
//            EC_1_2 = EC_1_2 / 2;
//        }
        //if(cluster1.getPointSize()*cluster1.getPointSize()==2)
        Double EC_1 = cluster1.getEC();
        Double EC_2 = cluster2.getEC();
        return 2*EC_1_2/(EC_1 + EC_2);
    }
    public Double calRI(Cluster cluster1,Cluster cluster2){
        Double EC_1_2 = this.calEC(cluster1,cluster2);
//        if(cluster1.getPointSize()==2 && cluster2.getPointSize()==2) {
//            EC_1_2 = EC_1_2 / 2;
//        }
        //if(cluster1.getPointSize()*cluster1.getPointSize()==2)
        Double EC_1 = cluster1.getEC();
        Double EC_2 = cluster2.getEC();
        return 2*EC_1_2/(EC_1 + EC_2);

    }
    //相对相似性
    public Double calRC(Cluster cluster1,Cluster cluster2){
        Pair<Double,Integer> SEC_pair= this.calSEC(cluster1,cluster2);
        Double SEC_1_2 = SEC_pair.getFirst()/SEC_pair.getSecond();
        Double SEC_1 = cluster1.getSEC();
        Double SEC_2 = cluster2.getSEC();
        Integer C_1 = cluster1.getPointSize();
        Integer C_2 = cluster2.getPointSize();
        Integer C_1_2 = C_1 + C_2;
        Integer knn = this.getKnn();
        //Integer t_C1 = Math.min(C_1,knn);
        //Integer t_C2 = Math.min(C_2,knn);
        //Double smooth = Math.sqrt(1.0*SEC_pair.getSecond()/(t_C1*t_C2));
        Double smooth2 = 1.0;
        Integer max_C1_C2 = Math.max(C_1,C_2);
        Integer min_C1_C2 = Math.min(C_1,C_2);
        if(SEC_pair.getSecond() / max_C1_C2 <=  min_C1_C2*0.2 ){
             smooth2 = Math.sqrt(1.0*SEC_pair.getSecond()/(C_1*C_2));
        }

        Double smooth = Math.sqrt(SEC_1_2);
        return (SEC_1_2*C_1_2)/(SEC_1*C_1+SEC_2*C_2) * smooth * smooth2;// * SEC_pair.getSecond()/(C_1*C_2);
    }
    public Double calRC(Cluster cluster1,Cluster cluster2,Pair<Double,Integer> EC_Edge){
        Pair<Double,Integer> SEC_pair= EC_Edge;
        Double SEC_1_2 = SEC_pair.getFirst()/SEC_pair.getSecond();
        Double SEC_1 = cluster1.getSEC();
        Double SEC_2 = cluster2.getSEC();
        Integer C_1 = cluster1.getPointSize();
        Integer C_2 = cluster2.getPointSize();
        Integer C_1_2 = C_1 + C_2;
        Integer knn = this.getKnn();
        //Integer t_C1 = Math.min(C_1,knn);
        //Integer t_C2 = Math.min(C_2,knn);
        //Double smooth = Math.sqrt(1.0*SEC_pair.getSecond()/(t_C1*t_C2));
        Double smooth2 = 1.0;
        Integer max_C1_C2 = Math.max(C_1,C_2);
        Integer min_C1_C2 = Math.min(C_1,C_2);
        if(SEC_pair.getSecond() / max_C1_C2 <=  min_C1_C2*0.2 ){
            smooth2 = Math.sqrt(1.0*SEC_pair.getSecond()/(C_1*C_2));
        }

        Double smooth = Math.sqrt(SEC_1_2);
        return (SEC_1_2*C_1_2)/(SEC_1*C_1+SEC_2*C_2) * smooth * smooth2;// * SEC_pair.getSecond()/(C_1*C_2);
    }
//    //相对相似性
//    public Double calRC(Cluster cluster1,Cluster cluster2){
//        Pair<Double,Integer> SEC_pair= this.calSEC(cluster1,cluster2);
//        Double SEC_1_2 = SEC_pair.getFirst()/SEC_pair.getSecond();
//        Double SEC_1 = cluster1.getSEC();
//        Double SEC_2 = cluster2.getSEC();
//        Integer C_1 = cluster1.getPointSize();
//        Integer C_2 = cluster2.getPointSize();
//        Integer C_1_2 = C_1 + C_2;
//        return (SEC_1_2*C_1_2)/(SEC_1*C_1+SEC_2*C_2);// * SEC_pair.getSecond()/(C_1*C_2);
//    }
    public Double calFunctionDefinedOptimization(Cluster cluster1,Cluster cluster2){
        Double RI = this.calRI(cluster1,cluster2);
        Double RC = this.calRC(cluster1,cluster2);
        Double opt = Math.pow(RI,this.alpha1) * Math.pow(RC,this.alpha2);
        return opt;
    }
    public Double calFunctionDefinedOptimization(Double RI,Double RC){

        Double opt = Math.pow(RI,this.alpha1) * Math.pow(RC,this.alpha2);
        return opt;
    }
    public Cluster mergeTwoClustersToOne(Cluster cluster1,Cluster cluster2,Double opt,Double RI,Double RC){
        ArrayList<Integer> point2 = cluster2.getPoints();
        //Double EC = this.calEC(cluster1,cluster2);
        Pair<Double,Integer> pair = this.calSEC(cluster1,cluster2);

        cluster1.setSEC(pair.getFirst()/pair.getSecond());
//        if(pair.getSecond() / (cluster1.getMergeEdgeNum()+cluster2.getMergeEdgeNum()) >= 4){
//            EC = EC / EC/4;
//        }
//        if(cluster1.getPointSize()==2 && cluster2.getPointSize()==2) {
//            EC = EC / 2;
//        }
        cluster1.setEC(pair.getFirst());
        cluster1.setMergeEdgeNum(pair.getSecond());
        cluster1.setIs_merged(true);
        Double pre_opt = cluster1.getOpt();
        cluster1.setOpt(opt);
        cluster1.setRC(RC);
        cluster1.setRI(RI);
        for(Integer point:point2){
            cluster1.addPoint(point);
        }
        point2.clear();
        return cluster1;
    }
    public Cluster mergeTwoClustersToOne(Cluster cluster1,Cluster cluster2,Double opt,Double RI,Double RC,boolean notbalance){
        ArrayList<Integer> point2 = cluster2.getPoints();
        Pair<Double,Integer> pair = this.calSEC(cluster1,cluster2);
        cluster1.setEC(pair.getFirst());
        cluster1.setSEC(pair.getFirst()/pair.getSecond());
        cluster1.setMergeEdgeNum(pair.getSecond());
        cluster1.setIs_merged(true);
        Double pre_opt = cluster1.getOpt();
        cluster1.setOpt(opt*0.4+pre_opt*0.6);
        cluster1.setRC(RC);
        cluster1.setRI(RI);
        for(Integer point:point2){
            cluster1.addPoint(point);
        }
        point2.clear();
        return cluster1;
    }
    public Cluster mergeTwoClustersToOne(Cluster cluster1,Cluster cluster2,Double RI,Double RC){
        ArrayList<Integer> point2 = cluster2.getPoints();
        Pair<Double,Integer> pair = this.calSEC(cluster1,cluster2);
        cluster1.setEC(pair.getFirst());
        cluster1.setSEC(pair.getFirst()/pair.getSecond());
        cluster1.setMergeEdgeNum(pair.getSecond());
        cluster1.setIs_merged(true);
        Double opt = this.calFunctionDefinedOptimization(RI,RC);
        cluster1.setOpt(opt);
        cluster1.setRC(RC);
        cluster1.setRI(RI);
        for(Integer point:point2){
            cluster1.addPoint(point);
        }
        point2.clear();
        return cluster1;
    }
    public Double getMissValue() {
        return miss_value;
    }

    public void setMissVsalue(Double miss_value) {
        this.miss_value = miss_value;
    }

    public Double getAlpha2() {
        return alpha2;
    }

    public void setAlpha2(Double alpha2) {
        this.alpha2 = alpha2;
    }

    public Double getAlpha1() {
        return alpha1;
    }

    public void setAlpha1(Double alpha1) {
        this.alpha1 = alpha1;
    }

    public Double getThreshold() {
        return threshold;
    }

    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }

    public Integer getMinClusterMaxNum() {
        return min_cluster_max_num;
    }

    public void setMinClusterMaxNum(Integer min_cluster_max_num) {
        this.min_cluster_max_num = min_cluster_max_num;
    }

}
