package src.main.java2;

import fig.basic.Pair;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yishuihan on 17-7-3.
 */
public class Chameleon4 {
    public static void main(String[] args) throws Exception{
        String file = "";
        String label_file = "";
        String topN_file = "";
        String out = "";
        String out2 = "";
        String log_file = "";
        String log2_file = "";
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
            }else  if(arg.equalsIgnoreCase("log2")){
                log2_file = args[i+1];
            }

        }


        MyTimeClock mt = new MyTimeClock("1100");
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
        //设置超参数
        //k=60
        Integer knn = 150;
        loadData.buildSentTopNMatMap(topN_file, s_i_map,knn);
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
        Integer min_cluster_max_num = 20;
        Boolean use_min_cluster_max_num = true;
        chameleonTool.setMinClusterMaxNum(min_cluster_max_num);
        chameleonTool.setMissVsalue(miss_value);
        chameleonTool.setThreshold(threshold);
        //设置输出
        out += "_"+min_cluster_max_num+"_"+use_min_cluster_max_num+"_"+String.valueOf(threshold)+"_step1.txt";
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(out),"utf-8"),true);
        ComparetorERC4 comparetorByERC4 = new ComparetorERC4();
        Queue<Cluster> clusters_set = new PriorityQueue<>(set_s.size(),comparetorByERC4);
        Queue<Integer> has_point = new ArrayDeque<>();
        //System.out.println("hello:"+set_s.size());
        has_point.addAll(set_s);//加入全部点索引
        HashSet<Integer> point_flag = new HashSet<>();
        //第二阶段　合并－初始化
        //第二阶段　设置超参数
        Double minMetric =1.0;
        Double minRC = 0.811;
        Double minRI = 1.2;
        Double minESC = 0.4;
        Double alpha = 2.0;//RI*RC^alpha
        String sort_para = "RC_DOWN";
        chameleonTool.setAlpha(alpha);
        //设置输出
//        out2+= "_minRC_"+String.valueOf(minRC).substring(0,3)+"_minRI_"+String.valueOf(minRI).substring(0,3)+"_minMetirc_"+
//                String.valueOf(minMetric).substring(0,3)+"_step2.txt";
        out2 += "_"+min_cluster_max_num+"_"+use_min_cluster_max_num+"_"+String.valueOf(threshold)+
                "_knn_"+knn+min_cluster_max_num+"_minRC_"+minRC+"_minRI_"+minRI+"_minMetirc_"+ minMetric+"_"+sort_para+"_step2.txt";
        PrintWriter pw2 = new PrintWriter(new OutputStreamWriter(new FileOutputStream(out2),"utf-8"),true);
//        log_file += "_minRC_"+String.valueOf(minRC).substring(0,3)+"_minRI_"+String.valueOf(minRI).substring(0,3)+"_minMetirc_"+
//                String.valueOf(minMetric).substring(0,3)+"_log.txt";
        log_file += "_"+min_cluster_max_num+"_"+use_min_cluster_max_num+"_"+String.valueOf(threshold)+
                "_knn_"+knn+min_cluster_max_num+"_minRC_"+minRC+"_minRI_"+minRI+"_minMetirc_"+
                minMetric+"_"+sort_para+"_log.txt";
        log2_file += "_"+min_cluster_max_num+"_"+use_min_cluster_max_num+"_"+String.valueOf(threshold)+
                "_knn_"+knn+"_minRC_"+minRC+"_minRI_"+minRI+"_minMetirc_"+
                minMetric+"_"+sort_para+"_log2.txt";
        PrintWriter log = new PrintWriter(new OutputStreamWriter(new FileOutputStream(log_file),"utf-8"),true);
        PrintWriter log2 = new PrintWriter(new OutputStreamWriter(new FileOutputStream(log2_file),"utf-8"),true);
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

            System.out.println(new_cluster.isIs_merged());
            System.out.println(new_cluster.getEC());
            clusters_set.add(new_cluster);

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


        int count = 0;
        //第二阶段　合并－运行
        int rm_num = 0;
        HashSet<Cluster> over_set = new HashSet <>();
        while(!clusters_set.isEmpty()){
            Cluster cluster = clusters_set.poll();
//            log.println(i_s_map.get(cluster.getPoints().get(0)));
//            log.println("opt+"+cluster.getOpt());
//            log.println("RI +"+cluster.getRI());
//            log.println("RC +"+cluster.getRC());
//            log.println("SEC+"+cluster.getSEC());
//            log.println("EC +"+cluster.getEC());
//            log.println("pts+"+cluster.getPointSize());
//            log.println("ede+"+cluster.getMergeEdgeNum());
//            log.println("ism+"+cluster.isIs_merged());
//            log.println("");
            if(cluster.getPointSize()>70&&!over_set.contains(cluster)){
                System.out.println(i_s_map.get(cluster.getPoints().get(0)));
                over_set.add(cluster);
            }
            if(clusters_set.isEmpty()){
                result.add(cluster);
                break;
            }
            count++;
            if (count % 100==0){
                System.out.println(count);
                log.flush();
                log2.flush();
            }
            Iterator<Cluster> iterator = clusters_set.iterator();
            Double max_opt = Double.MIN_VALUE;
            Double max_RC = Double.MIN_VALUE;
            Double max_RI = Double.MIN_VALUE;
            Double max_ESC = Double.MIN_VALUE;
            Cluster max_cluster = null;
            //boolean
            while(iterator.hasNext()){
                Cluster tmp = iterator.next();
                Double opt = chameleonTool.calFunctionDefinedOptimization(cluster,tmp);
                Double RC = chameleonTool.calRC(cluster,tmp);
                Double RI = chameleonTool.calRI(cluster,tmp);
                Pair<Double,Integer> pair_sec = chameleonTool.calSEC(cluster,tmp);
                Double ESC = pair_sec.getFirst()/pair_sec.getSecond();
                if(tmp.isIs_merged()){
                    if(cluster.isIs_merged()){
                        if(opt > max_opt && RC>max_RC && ESC >minESC && RC >minRC){
                            max_opt = opt;
                            max_cluster = tmp;
                            max_RC = RC;
                            max_RI = RI;
                            max_ESC = ESC;

                        }
                    }else{
                        if(RC > max_RC && opt>max_opt && ESC >minESC && RC >minRC){
                            max_opt = opt;
                            max_cluster = tmp;
                            max_RC = RC;
                            max_RI = RI;
                            max_ESC = ESC;
                        }
                    }

                }else{
                    if(cluster.isIs_merged() ){
                        if(RC > max_RC && opt>max_opt&& ESC >minESC && RC >minRC ){
                            max_opt = opt;
                            max_cluster = tmp;
                            max_RC = RC;
                            max_RI = RI;
                            max_ESC = ESC;
                        }
                    }else{
                        if(RC > max_RC && ESC >max_ESC && RC >minRC){
                            max_opt = opt;
                            max_cluster = tmp;
                            max_RC = RC;
                            max_RI = RI;
                            max_ESC = ESC;
                        }
                    }

                }
            }
            //System.out.println("-"+max_opt+"\n"+max_RC+"\n"+max_RI);
//            if(max_cluster!=null)
//                log.println("-opt: "+max_opt+" -RC:"+max_RC+" -RI:"+max_RI+" now_size: "+cluster.getPointSize()+" max_size: "+max_cluster.getPointSize());
            if(max_cluster!=null&&(
                    (cluster.isIs_merged()&&max_cluster.isIs_merged()&& max_opt >= minMetric && max_RC>=minRC && max_RI>=minRI )||
                            (!max_cluster.isIs_merged()&&cluster.isIs_merged()&& max_RC >= minRC&& max_opt >= minMetric)||
                            (!cluster.isIs_merged()&& max_cluster.isIs_merged()&&max_RC >= minRC && max_opt >= minMetric )||
                            (!max_cluster.isIs_merged()&&!cluster.isIs_merged()&&max_RC>=minRC))){



                log.println(i_s_map.get(cluster.getPoints().get(0))+"--"+i_s_map.get(max_cluster.getPoints().get(0)));
                log.println(cluster.getPointSize()+"--"+max_cluster.getPointSize());
                log.println(cluster.isIs_merged()+"--"+max_cluster.isIs_merged());
                log.println(max_ESC);
                log.println("2Opt-"+cluster.getOpt()+"\t"+max_cluster.getOpt());
                log.println("2RC+-"+cluster.getRC()+"\t"+max_cluster.getRC());
                log.println("2RI+-"+cluster.getRI()+"\t"+max_cluster.getRI());


                clusters_set.remove(max_cluster);
                if(cluster.getPointSize()< max_cluster.getPointSize()){
                    Cluster cluster_tmp = cluster;
                    cluster = max_cluster;
                    max_cluster = cluster_tmp;
                }
                cluster = chameleonTool.mergeTwoClustersToOne(cluster,max_cluster,max_opt,max_RI,max_RC);
                log.println(cluster.getMergeEdgeNum());
                log.println("-opt+"+cluster.getOpt());
                log.println("-RC++"+cluster.getRC());
                log.println("-RI++"+cluster.getRI());
                log.println("");
                rm_num++;
                //System.out.println("1:"+rm_num);
                clusters_set.add(cluster);

                //clusters_set.
            }else{
                result.add(cluster);
                log.println(i_s_map.get(cluster.getPoints().get(0))+"-------------------" +
                        "---------------------------------------" );

                log.println(cluster.getPointSize());
                log.println(cluster.isIs_merged());
                log.println(cluster.getMergeEdgeNum());

                log.println("mopt+"+max_opt);
                log.println("mRC++"+max_RC);
                log.println("mRI++"+max_RI);
                if(max_cluster!=null)
                    log.println(i_s_map.get(max_cluster.getPoints().get(0)));
                else
                    log.println("error");
                log.println("+opt+"+cluster.getOpt()+" "+(max_opt >= minMetric));
                log.println("+RC++"+cluster.getRC()+" "+(max_RC>=minRC));
                log.println("+RI++"+cluster.getRI()+" "+(max_RI>=minRI));
                log.println("");
                cluster.setMaxOpt(max_opt);
                cluster.setMaxRI(max_RI);
                cluster.setMaxRC(max_RC);

                //写入log文件
                clusters_set.remove(cluster);
                rm_num = 0;
                ArrayList<Integer> points = cluster.getPoints();
                for(Integer point:points){
                    log2.print(s_f_map.get(i_s_map.get(point))+"\t");

                }
                log2.print("\n");
                for(Integer point:points){
                    log2.print(i_s_map.get(point)+"\t");
                }
                log2.print("\n\n");

                // System.out.println("2:"+rm_num);
                //cluster = clusters_set.poll();
            }
            //System.out.println(clusters_set.size());
        }

        System.out.println("second step finished");
        //第二阶段结果
        Iterator<Cluster> clusterIterator2 = result.iterator();

        boolean log2_flag = false;
        while(clusterIterator2.hasNext()){
            Cluster cluster2 = clusterIterator2.next();
            ArrayList<Integer> points = cluster2.getPoints();
            if(points.size()>70){
                log2_flag = true;
                log2.println("------------------------------------------------------------------------------");
            }
            pw2.print("\n");
            pw2.print("opt+"+cluster2.getOpt()+"\t");
            pw2.print("RC++"+cluster2.getRC()+"\t");
            pw2.print("RI++"+cluster2.getRI()+"\n");
            pw2.print("mopt"+cluster2.getMaxOpt()+"\t");
            pw2.print("mRC+"+cluster2.getMaxRC()+"\t");
            pw2.print("mRI+"+cluster2.getMaxRI()+"\n");
            pw2.print("SEC+"+cluster2.getSEC()+"\n");
            for(Integer point:points){
                if(log2_flag)
                    log2.print(s_f_map.get(i_s_map.get(point))+"\t");
                pw2.print(s_f_map.get(i_s_map.get(point))+"\t");
            }
            if(log2_flag) log2.print("\n");
            pw2.print("\n");
            for(Integer point:points){
                if(log2_flag)
                    log2.print(i_s_map.get(point)+"\t");
                pw2.print(i_s_map.get(point)+"\t");
            }
            if(log2_flag) log2.print("\n");
            pw2.print("\n");
            log2_flag =false;
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
class ComparetorERC4 implements Comparator {

    public int compare(Object o1, Object o2) {
        Cluster s1=(Cluster)o1;
        Cluster s2=(Cluster)o2;

        Double s1_pre = s1.getRC();
        Double s2_pre = s2.getRC();//;
//        Double s1_pre = s1.getOpt();
//        Double s2_pre = s2.getOpt();//;
        if(s1_pre>s2_pre)
            return 1;
        else
            return -1;
    }
}
