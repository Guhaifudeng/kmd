package java;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yishuihan on 17-7-3.
 */
public class Chameleon {
    public static void main(String[] args) throws Exception{
        MyTimeClock mt = new MyTimeClock("1100");
        mt.tt();
        //引入sent map
        String file = "/home/badou/data/kmediod/word_ten300.txt";
        LoadData loadData = new LoadData();
        loadData.buildSentencesIndexMap(file);
        HashMap<String, Integer> s_i_map = loadData.getSentencesIndexMap();
        HashMap <Integer, String> i_s_map = loadData.getIndexSentencesMap();
        System.out.println("load sentences map finised");
        mt.mm();

        //引入sent top_n map
        String topN_file = "/home/badou/data/kmediod/word_ten300_top20_cosin.txt";
        loadData.buildSentTopNMatMap(topN_file, s_i_map);
        ConcurrentHashMap<Integer, HashMap <Integer, Double>> sparse_mat = loadData.getSparseMat();
        System.out.println("load sentences similarity matrix finised");
        mt.mm();
        Set<Integer> set_s = i_s_map.keySet();

        //创建辅助类
        ChameleonTool chameleonTool = new ChameleonTool(sparse_mat, set_s);

        //设置超参数
        Double threshold = 0.95;
        Double miss_value = -1.0;
        Integer min_cluster_max_num = 3;
        Boolean use_min_cluster_max_num = false;

        chameleonTool.setMinClusterMaxNum(min_cluster_max_num);
        chameleonTool.setMissVsalue(miss_value);
        chameleonTool.setThreshold(threshold);

        //第一阶段 构建最小簇
        HashSet<Cluster> clusters_set = new HashSet<>();
        HashSet<Integer> cluster_id = new HashSet <>();
        Queue<Integer> has_point = new ArrayDeque <>();

        has_point.addAll(set_s);//加入全部点索引
        while(!has_point.isEmpty()){
           Cluster new_cluster = new Cluster(has_point.size());
           Integer point = has_point.poll();
           new_cluster.addPoint(point);
           if(has_point.isEmpty())
               break;
           HashMap<Integer, Double> top_n = chameleonTool.getPointTopN(point);
           for(Map.Entry<Integer,Double>entry: top_n.entrySet()){
               Integer o2 = entry.getKey();
               Double sim_value = entry.getValue();
               if(use_min_cluster_max_num){
                    if(chameleonTool.ableAddPointToClusterWithThresholdAndNum(new_cluster,o2))
                    {
                        new_cluster.addPoint(o2);
                        has_point.remove(o2);
                    }
               }else {
                   if(chameleonTool.ableAddPointToClusterWithThreshold(new_cluster,o2)){
                       new_cluster.addPoint(o2);
                       has_point.remove(o2);
                   }
               }
           }
           clusters_set.add(new_cluster);
        }

        System.out.println("first step finished");
        String out = "/home/badou/data/kmediod/chemeleon/word_ten300_top20_step1.txt";
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(out),"utf-8"),true);
        Iterator<Cluster> clusterIterator = clusters_set.iterator();
        while(clusterIterator.hasNext()){
            Cluster cluster = clusterIterator.next();
            ArrayList<Integer> points = cluster.getPoints();
            for(Integer point:points){
                pw.print(i_s_map.get(point)+"\t");
            }
            pw.print("\n");
        }
        System.out.println("finished");
    }
}
