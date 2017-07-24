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
public class Chameleon3 {
    public static void main(String[] args) throws Exception{
        String file = "";
        String label_file = "";
        String topN_file = "";
        String out = "";
        String out2 = "";
        String log_file = "";
        for (int i = 0; i < args.length; i += 1) {
            String arg = args[i];
            if (arg.startsWith("--")) {
                arg = arg.substring(2);
            } else if (arg.startsWith("-")) {
                arg = arg.substring(1);
            }

            if(arg.equalsIgnoreCase("mat")){
                topN_file= args[i+1];
            }else if(arg.equalsIgnoreCase("set")){
                file = args[i+1];
            }else if(arg.equalsIgnoreCase("label")){
                label_file = args[i+1];
            }else  if(arg.equalsIgnoreCase("out")){
                out = args[i+1];
            }else if(arg.equalsIgnoreCase("out2")){
                out2 = args[i+1];
            }else if(arg.equalsIgnoreCase("log")){
                log_file = args[i+1];
            }

        }


        MyTimeClock mt = new MyTimeClock("11001");
        mt.tt();
        //引入sent map

        LoadData loadData = new LoadData();
        loadData.buildSentencesIndexMap(file);
        HashMap<String, Integer> s_i_map = loadData.getSentencesIndexMap();
        HashMap <Integer, String> i_s_map = loadData.getIndexSentencesMap();
        System.out.println("load sentences map finised");
        //mt.mm();

        loadData.buildSentencesLabelMap(label_file);
        HashMap<String,String> s_f_map = loadData.getSentencesLabelMap();
        System.out.println("load labels map finished");

        //引入sent top_n map

        loadData.buildSentTopNMatMap(topN_file, s_i_map,100);
        ConcurrentHashMap<Integer, HashMap <Integer, Double>> sparse_mat = loadData.getSparseMat();
        //System.out.println(s_i_map.size());
        System.out.println("load sentences similarity matrix finised!");
        System.out.println("set size: "+sparse_mat.size());
        //mt.mm();
        Set<Integer> set_s = i_s_map.keySet();

        //创建辅助类
        ChameleonTool chameleonTool = new ChameleonTool(sparse_mat, set_s);
        //第一阶段 构建最小簇－初始化
        //第一阶段　设置超参数
        Double threshold = 0.9;
        Double miss_value = -1.0;
        Integer min_cluster_max_num = 10;
        Boolean use_min_cluster_max_num = true;
        chameleonTool.setMinClusterMaxNum(min_cluster_max_num);
        chameleonTool.setMissVsalue(miss_value);
        chameleonTool.setThreshold(threshold);
        //设置输出
        out += "_"+String.valueOf(threshold)+"_step1.txt";
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(out),"utf-8"),true);
        ComparetorPair comparetorPair = new ComparetorPair();


        HashSet<Cluster> clusters_set = new HashSet <>();
        HashMap<Integer,Cluster> ind_cluster_map = new HashMap <>();
        Queue<Integer> has_point = new ArrayDeque <>();
        //System.out.println("hello:"+set_s.size());
        has_point.addAll(set_s);//加入全部点索引
        HashSet<Integer> point_flag = new HashSet<>();
        //第二阶段　合并－初始化
        //第二阶段　设置超参数
        Double minMetric =1.8;
        Double minRC = 0.6;
        Double minRI = 2.0;
        Double alpha = 2.0;//RI*RC^alpha
        String sort_para = "rc";
        chameleonTool.setAlpha(alpha);
        //设置输出

        out2 += "_minRC_"+minRC+"_minRI_"+minRI+"_minMetirc_"+ minMetric+"_"+sort_para+"_step2.txt";
        PrintWriter pw2 = new PrintWriter(new OutputStreamWriter(new FileOutputStream(out2),"utf-8"),true);

        log_file += "_minRC_"+minRC+"_minRI_"+minRI+"_minMetirc_"+
                minMetric+"_"+sort_para+"_log.txt";
        PrintWriter log = new PrintWriter(new OutputStreamWriter(new FileOutputStream(log_file),"utf-8"),true);
        ArrayList<Cluster> result = new ArrayList <>();
        //第一阶段　构建最小簇－运行
        int size = 0;
        boolean first = true;
        while(!has_point.isEmpty()){
            Cluster new_cluster = new Cluster(has_point.size(),chameleonTool);

            new_cluster.initPoint();
            Integer point = has_point.poll();
            point_flag.add(point);
            new_cluster.addPoint(point);
            if(has_point.isEmpty()){
                size += new_cluster.getPointSize();
                new_cluster.setSEC(threshold);
                new_cluster.setAlpha(alpha);
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
            new_cluster.setSEC(threshold);
            new_cluster.setAlpha(alpha);
            clusters_set.add(new_cluster);
            ind_cluster_map.put(new_cluster.getId(),new_cluster);
            size += new_cluster.getPointSize();
        }
        System.out.println("real size="+size);
        System.out.println("first step finished "+clusters_set.size());
        //第一阶段　结果
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

        HashMap<Cluster,ArrayList<ClusterPair>> c_f_map = new HashMap<>();
        int count = 0;
        //第二阶段　合并－运行
        int rm_num = 0;
        Queue<ClusterPair> clusters_priQueue = new PriorityQueue <>(10000,comparetorPair);
        int i=0;int j=0;
        //初始化
        for(Cluster c1:clusters_set){
            j=0;
            for(Cluster c2:clusters_set){
                if(i<=j){
                    break;
                }

                Double RC = chameleonTool.calRC(c1,c2);
                Double RI = chameleonTool.calRI(c1,c2);
                Double Opt = chameleonTool.calFunctionDefinedOptimization(RC,RI);
//                        chameleonTool.mergeTwoClustersToOne(c1,c2,RI,RC);
                if(Opt>minMetric&& RC>minRC){
                    ClusterPair tmp_pair = new ClusterPair(c1,c2);
                    if(c_f_map.containsKey(c1)){
                        ArrayList<ClusterPair> pairs = c_f_map.get(c1);
                        pairs.add(tmp_pair);
                    }else {

                        ArrayList<ClusterPair> pairs = new ArrayList <>();
                        pairs.add(tmp_pair);
                        c_f_map.put(c1,pairs);
                    }

                    if(c_f_map.containsKey(c2)){
                        ArrayList<ClusterPair> pairs = c_f_map.get(c2);
                        pairs.add(tmp_pair);
                    }else {

                        ArrayList<ClusterPair> pairs = new ArrayList <>();
                        pairs.add(tmp_pair);
                        c_f_map.put(c2,pairs);
                    }

                    clusters_priQueue.add(tmp_pair);
                }
                j++;
            }

            i++;
        }


//        //
//        while(!clusters_set.isEmpty() && !clusters_priQueue.isEmpty()){
//            //String max_str = clusters_priQueue.poll();
//            String[] c_inds = max_str.split("-");
//            Integer c1_ind = Integer.valueOf(c_inds[0]);
//            Integer c2_ind = Integer.valueOf(c_inds[1]);
//            Cluster c1 = ind_cluster_map.get(c1_ind);
//            Cluster c2 = ind_cluster_map.get(c2_ind);
//            if(c1.getPointSize() < c2.getPointSize()){
//                Cluster tmp_c = c1;
//                c1 = c2;
//                c2 = tmp_c;
//            }
//            c1_ind = c1.getId();
//            c2_ind = c2.getId();
//            Double RC = chameleonTool.calRC(c1,c2);
//            Double RI = chameleonTool.calRI(c1,c2);
//            Double opt =chameleonTool.calFunctionDefinedOptimization(RI,RC);
//            if(opt>minMetric && RC>minRC){
//                Cluster cluster = chameleonTool.mergeTwoClustersToOne(c1,c2,opt,RI,RC);
//                //remove
//                clusters_set.remove(c2);
//                for(Cluster c2_r:clusters_set){
//                    Integer id_r = c2_r.getId();
//                    String str_r1 = id_r+"-"+c2_ind;
//                    String str_r2 = c2_ind+"-"+id_r;
//                    if(clusters_priQueue.contains())
//
//                }
//                //String new_c_ind = cluster.getId();
//            }
//
//        }

        System.out.println("second step finished");
        //第二阶段结果
        Iterator<Cluster> clusterIterator2 = result.iterator();

        while(clusterIterator2.hasNext()){
            Cluster cluster2 = clusterIterator2.next();
            ArrayList<Integer> points = cluster2.getPoints();
            pw2.print("\n");
            pw2.println("opt+"+cluster2.getOpt());
            pw2.println("RC++"+cluster2.getRC());
            pw2.println("RI++"+cluster2.getRI());
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
        System.out.println("all finished! cluster num: "+result.size());
        mt.dd();
    }


}
class ClusterPair {
    Cluster c1 = null;
    Cluster c2 = null;
    Double opt = 0.0;
    ClusterPair(Cluster c1,Cluster c2){
        this.c1 = c1;
        this.c2 = c2;
    }
    public Cluster getC1() {
        return c1;
    }

    public void setC1(Cluster c1) {
        this.c1 = c1;
    }

    public Cluster getC2() {
        return c2;
    }

    public void setC2(Cluster c2) {
        this.c2 = c2;
    }

    public Double getOpt() {
        return opt;
    }

    public void setOpt(Double opt) {
        this.opt = opt;
    }
}
class ComparetorPair implements Comparator{
    public int compare(Object o1, Object o2) {
        ClusterPair s1=(ClusterPair) o1;
        ClusterPair s2=(ClusterPair)o2;

        Double s1_pre = s1.getOpt();
        Double s2_pre = s2.getOpt();//;
//        Double s1_pre = s1.getSEC();
//        Double s2_pre = s2.getSEC();//;
        if(s1_pre<s2_pre)
            return 1;
        else
            return -1;
    }

}
