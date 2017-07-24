package java;



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
            return miss_value;
        }

    }
    public Double getPairCenterPointSim(int o1,int o2,boolean addEC){
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
                edge_num++;
                sum_edge_weight += this.getPairCenterPointSim(point1,point2,true);
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
                edge_num++;
                sum_edge_weight += this.getPairCenterPointSim(point1,point2,true);
            }
        }
        if(edge_num == 0){
            return new Pair<>(Double.MAX_VALUE,0);
        }else{
            return new Pair<>(sum_edge_weight,edge_num);
        }
    }
    //相对相连性
    public Double calRI(Cluster cluster1,Cluster cluster2){
        Double EC_1_2 = this.calEC(cluster1,cluster2);
        Double EC_1 = cluster1.getEC();
        Double EC_2 = cluster2.getEC();
        return 2*EC_1_2/(EC_1*EC_2);

    }
    //相对相似性
    public Double calRC(Cluster cluster1,Cluster cluster2){
        Pair<Double,Integer> SEC_pair= this.calSEC(cluster1,cluster2);
        Double SEC_1_2 = SEC_pair.getFirst()/SEC_pair.getSecond();
        Double SEC_1 = cluster1.getEC()/cluster1.getMergeEdgeNum();
        Double SEC_2 = cluster2.getEC()/cluster2.getMergeEdgeNum();
        Integer C_1 = cluster1.getPointSize();
        Integer C_2 = cluster2.getPointSize();
        Integer C_1_2 = C_1 + C_2;
        return (SEC_1_2*C_1_2)/(SEC_1*C_1+SEC_2*C_2);
    }

    public Cluster mergeTwoClustersToOne(Cluster cluster1,Cluster cluster2){
        ArrayList<Integer> point2 = cluster2.getPoints();
        Pair<Double,Integer> pair = this.calSEC(cluster1,cluster2);
        cluster1.setEC(pair.getFirst());
        cluster1.setMergeEdgeNum(pair.getSecond());
        for(Integer point:point2){
            cluster1.addPoint(point);
        }
        return cluster1;
    }
    public Double getMissValue() {
        return miss_value;
    }

    public void setMissVsalue(Double miss_value) {
        this.miss_value = miss_value;
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
