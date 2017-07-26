package src.main.java2;

import org.omg.Messaging.SYNC_WITH_TRANSPORT;

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
        String file = "/home/badou/data/kmediod/chemeleon/word30000_top300_set_4.txt";
        LoadData loadData = new LoadData();
        loadData.buildSentencesIndexMap(file);
        HashMap<String, Integer> s_i_map = loadData.getSentencesIndexMap();
        HashMap <Integer, String> i_s_map = loadData.getIndexSentencesMap();
        System.out.println("load sentences map finised");
        //mt.mm();
        String label_file = "/home/badou/data/kmediod/chemeleon/word3000_kmediod_rand100_set4.txt";
        loadData.buildSentencesLabelMap(label_file);
        HashMap<String,String> s_f_map = loadData.getSentencesLabelMap();
        System.out.println("load labels map finished");

        //引入sent top_n map
        String topN_file = "/home/badou/data/kmediod/chemeleon/word30000_top300_mat_4.txt";
        loadData.buildSentTopNMatMap(topN_file, s_i_map,100);
        ConcurrentHashMap<Integer, HashMap <Integer, Double>> sparse_mat = loadData.getSparseMat();
        //System.out.println(s_i_map.size());
        System.out.println("load sentences similarity matrix finised!");
        System.out.println("set size: "+sparse_mat.size());
        //mt.mm();
        Set<Integer> set_s = i_s_map.keySet();

        //创建辅助类
        ChameleonTool chameleonTool = new ChameleonTool(sparse_mat, set_s);

        //设置超参数
        Double threshold = 0.8;
        Double miss_value = -1.0;
        Integer min_cluster_max_num = 3;
        Boolean use_min_cluster_max_num = false;

        chameleonTool.setMinClusterMaxNum(min_cluster_max_num);
        chameleonTool.setMissVsalue(miss_value);
        chameleonTool.setThreshold(threshold);
        //设置输出
        String out = "/home/badou/data/kmediod/chemeleon/word30000_top300_mat100_4_0.8_step1.txt";
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(out),"utf-8"),true);
         //第一阶段 构建最小簇
        Queue<Cluster> clusters_set = new ArrayDeque <>();
        Queue<Integer> has_point = new ArrayDeque <>();
        //System.out.println("hello:"+set_s.size());
        has_point.addAll(set_s);//加入全部点索引
        HashSet<Integer> point_flag = new HashSet<>();
        int size = 0;
        while(!has_point.isEmpty()){
           Cluster new_cluster = new Cluster(has_point.size(),chameleonTool);
           new_cluster.initPoint();
           Integer point = has_point.poll();
           point_flag.add(point);
           new_cluster.addPoint(point);
           if(has_point.isEmpty()){
               size += new_cluster.getPointSize();
               clusters_set.add(new_cluster);
               break;
           }

           HashMap<Integer, Double> top_n = chameleonTool.getPointTopN(point);
           for(Map.Entry<Integer,Double>entry: top_n.entrySet()){
               Integer o2 = entry.getKey();
               Double sim_value = entry.getValue();
               if(point_flag.contains(o2))
                   continue;
               if(use_min_cluster_max_num){
                    if(chameleonTool.ableAddPointToClusterWithThresholdAndNum(new_cluster,o2))
                    {
                        new_cluster.addPoint(o2);
                        point_flag.add(o2);
                        has_point.remove(o2);
                    }
               }else {
                   if(chameleonTool.ableAddPointToClusterWithThreshold(new_cluster,o2)){
                       new_cluster.addPoint(o2);
                       point_flag.add(o2);
                       has_point.remove(o2);
                   }
               }
           }
           clusters_set.add(new_cluster);
            size += new_cluster.getPointSize();
        }
        System.out.println("real size="+size);

        System.out.println("first step finished "+clusters_set.size());


        Iterator<Cluster> clusterIterator = clusters_set.iterator();
        while(clusterIterator.hasNext()){
            Cluster cluster = clusterIterator.next();
            ArrayList<Integer> points = cluster.getPoints();
            for(Integer point:points){
                pw.print(i_s_map.get(point)+"\t");
            }
            pw.print("\n");
        }
        System.out.println("output step one finished");

        //第二阶段　合并
        //设置超参数
        Double minMetric =1.0;
        Double minRC = 0.6;
        Double minRI = 1.0;
        Double alpha1 = 1.0;
        Double alpha2 = 2.0;//RI*RI^alpha
        chameleonTool.setAlpha1(alpha1);
        chameleonTool.setAlpha2(alpha2);
        //设置输出
        String out2 = "/home/badou/data/kmediod/chemeleon/word30000_top300_mat100_4_1.0_0.6_step2.txt";
        PrintWriter pw2 = new PrintWriter(new OutputStreamWriter(new FileOutputStream(out2),"utf-8"),true);
        String log_file = "/home/badou/data/kmediod/chemeleon/word30000_top300_mat100_4_1.0_0.6_log.txt";
        PrintWriter log = new PrintWriter(new OutputStreamWriter(new FileOutputStream(log_file),"utf-8"),true);
        ArrayList<Cluster> result = new ArrayList <>();
        int count = 0;

        int rm_num = 0;
        while(!clusters_set.isEmpty()){
            Cluster cluster = clusters_set.poll();
            if(clusters_set.isEmpty()){
                result.add(cluster);
                break;
            }
            count++;
            if (count % 100==0){
                System.out.println(count);
            }
            Iterator<Cluster> iterator = clusters_set.iterator();
            Double max_opt = Double.MIN_VALUE;
            Double max_RC = Double.MAX_VALUE;
            Double max_RI = Double.MAX_VALUE;
            Cluster max_cluster = null;
            while(iterator.hasNext()){
                Cluster tmp = iterator.next();
                Double opt = chameleonTool.calFunctionDefinedOptimization(cluster,tmp);
                Double RC = chameleonTool.calRC(cluster,tmp);
                Double RI = chameleonTool.calRI(cluster,tmp);
                if(opt > max_opt && RC>minRC &&RI>minRI){
                    max_opt = opt;
                    max_cluster = tmp;
                    max_RC = RC;
                    max_RI = RI;
                }
            }
//            if(max_cluster!=null)
//                log.println("-opt: "+max_opt+" -RC:"+max_RC+" -RI:"+max_RI+" now_size: "+cluster.getPointSize()+" max_size: "+max_cluster.getPointSize());
            if(max_opt >= minMetric && max_RC>=minRC && max_RI>=minRI){
                cluster = chameleonTool.mergeTwoClustersToOne(cluster,max_cluster,max_opt,max_RI,max_RC);
                clusters_set.remove(max_cluster);
                rm_num++;
                System.out.println("1:"+rm_num);
                clusters_set.add(cluster);
                //clusters_set.
            }else{
                result.add(cluster);
                clusters_set.remove(cluster);
                rm_num = 0;
                System.out.println("2:"+rm_num);
                //cluster = clusters_set.poll();
            }
            //System.out.println(clusters_set.size());
        }

        System.out.println("second step finished");

        Iterator<Cluster> clusterIterator2 = result.iterator();

        while(clusterIterator2.hasNext()){
            Cluster cluster2 = clusterIterator2.next();
            ArrayList<Integer> points = cluster2.getPoints();
            pw2.print("\n");
            for(Integer point:points){
                pw2.print(s_f_map.get(i_s_map.get(point))+"\t");
            }
            pw2.print("\n");
            for(Integer point:points){
                pw2.print(i_s_map.get(point)+"\t");
            }
            pw2.print("\n");
        }
        log.flush();
        log.close();
        pw.flush();
        pw.close();
        pw2.flush();
        pw.close();
        System.out.println("output step two finished");
        System.out.println("all finished! cluster num:"+result.size());
        mt.dd();
    }


}
