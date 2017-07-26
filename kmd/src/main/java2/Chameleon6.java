package src.main.java2;

import com.sun.xml.internal.ws.api.ha.StickyFeature;
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
public class Chameleon6 {
    public static void main(String[] args) throws Exception{
        String file = "";
        String label_file = "";
        String topN_file = "";
        String out = "";
        String out2 = "";
        String log_file = "";
        String log2_file = "";
        String log3_file = "";
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
            }else if(arg.equalsIgnoreCase("log3")){
                log3_file = args[i+1];
            }

        }


        MyTimeClock mt = new MyTimeClock("1121");
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
        Integer knn =100;
        loadData.buildSentTopNMatMap(topN_file, s_i_map,knn);
        ConcurrentHashMap<Integer, HashMap <Integer, Double>> sparse_mat = loadData.getSparseMat();
        //System.out.println(s_i_map.size());
        System.out.println("load sentences similarity matrix finised!");
        System.out.println("set size: "+sparse_mat.size());
        //mt.mm();
        Set<Integer> set_s = i_s_map.keySet();

        //创建辅助类
        ChameleonTool chameleonTool = new ChameleonTool(sparse_mat, set_s);
        chameleonTool.setKnn(knn);
        //第一阶段 构建最小簇－初始化
        //第一阶段　设置超参数
        Double threshold1 = 0.95;
        Double threshold2 = 0.65;
        Double miss_value = -1.0;
        Integer min_cluster_max_num = 2;
        Boolean use_min_cluster_max_num = true;
        chameleonTool.setMinClusterMaxNum(min_cluster_max_num);
        chameleonTool.setMissVsalue(miss_value);
        chameleonTool.setThreshold(threshold2);

        //组件
        HashSet<Cluster> clusters_set = new HashSet <>();
        Queue<Integer> has_point = new PriorityQueue <>();
        has_point.addAll(set_s);//加入全部点索引
        HashSet<Integer> point_flag = new HashSet<>();
        //第二阶段　合并－初始化
        //第二阶段　设置超参数
        Double minMetric = 0.0;
        Double minRC = 0.4;
        Double minRI = 0.0;
        Double alpha1 = 1.0;//RI^alph1 * RC^alpha2
        Double alpha2 = 2.0; //RI^alph1 * RC^alph2
        String sort_para = "maxOpt";
        chameleonTool.setAlpha1(alpha1);
        chameleonTool.setAlpha2(alpha2);
        //设置输出
        out += "_"+alpha1+":"+alpha2+"_"+min_cluster_max_num+"_"+use_min_cluster_max_num+"_"+String.valueOf(threshold2)+
                "_knn_"+knn+"_minRC_"+minRC+"_minRI_"+minRI+"_minMetric_"+ minMetric+"_"+sort_para+"_step1.txt";
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(out),"utf-8"),true);
        //设置输出
//        out2+= "_minRC_"+String.valueOf(minRC).substring(0,3)+"_minRI_"+String.valueOf(minRI).substring(0,3)+"_minMetirc_"+
//                String.valueOf(minMetric).substring(0,3)+"_step2.txt";
        out2 +="_"+alpha1+":"+alpha2+ "_"+min_cluster_max_num+"_"+use_min_cluster_max_num+"_"+String.valueOf(threshold2)+
                "_knn_"+knn+"_minRC_"+minRC+"_minRI_"+minRI+"_minMetric_"+ minMetric+"_"+sort_para+"_step2.txt";
        PrintWriter pw2 = new PrintWriter(new OutputStreamWriter(new FileOutputStream(out2),"utf-8"),true);
//        log_file += "_minRC_"+String.valueOf(minRC).substring(0,3)+"_minRI_"+String.valueOf(minRI).substring(0,3)+"_minMetirc_"+
//                String.valueOf(minMetric).substring(0,3)+"_log.txt";
        log_file += "_"+alpha1+":"+alpha2+"_"+min_cluster_max_num+"_"+use_min_cluster_max_num+"_"+String.valueOf(threshold2)+
                "_knn_"+knn+"_minRC_"+minRC+"_minRI_"+minRI+"_minMetric_"+
                minMetric+"_"+sort_para+"_log1.txt";
        log2_file += "_"+alpha1+":"+alpha2+"_"+min_cluster_max_num+"_"+use_min_cluster_max_num+"_"+String.valueOf(threshold2)+
                "_knn_"+knn+"_minRC_"+minRC+"_minRI_"+minRI+"_minMetric_"+
                minMetric+"_"+sort_para+"_log2.txt";
        log3_file += "_"+alpha1+":"+alpha2+"_"+min_cluster_max_num+"_"+use_min_cluster_max_num+"_"+String.valueOf(threshold2)+
                "_knn_"+knn+"_minRC_"+minRC+"_minRI_"+minRI+"_minMetric_"+
                minMetric+"_"+sort_para+"_log3.txt";
        PrintWriter log = new PrintWriter(new OutputStreamWriter(new FileOutputStream(log_file),"utf-8"),true);
        PrintWriter log2 = new PrintWriter(new OutputStreamWriter(new FileOutputStream(log2_file),"utf-8"),true);
        PrintWriter log3 = new PrintWriter(new OutputStreamWriter(new FileOutputStream(log3_file),"utf-8"),true);
        ArrayList<Cluster> result = new ArrayList <>();
        //第一阶段　构建最小簇－运行
        int size = 0;
        int cluster_num = 0;
        System.out.println("has point :" +has_point.size());
        while(!has_point.isEmpty()){
            Cluster new_cluster = new Cluster(has_point.size(),chameleonTool);

            new_cluster.initPoint();
            Integer point = has_point.poll();
            point_flag.add(point);
            new_cluster.addPoint(point);

            if(has_point.isEmpty()){
                size += new_cluster.getPointSize();
                new_cluster.setSEC(threshold2);
                new_cluster.setAlpha1(alpha1);
                new_cluster.setAlpha2(alpha2);
                clusters_set.add(new_cluster);
                break;
            }

            Double min_sim = Double.MAX_VALUE;
            Double max_sim = Double.MIN_VALUE;
            Integer max_o = -1;
            Integer min_o = -1;
            HashMap<Integer, Double> top_n = chameleonTool.getPointTopN(point);
            for(Map.Entry<Integer,Double>entry: top_n.entrySet()){
                Integer o2 = entry.getKey();

                if(point_flag.contains(o2))
                    continue;
                Double tmp_sim = chameleonTool.getPairCenterPointSim(new_cluster.getPoints().get(0),o2);
                if(tmp_sim < min_sim && tmp_sim>= threshold1){
                    min_sim = tmp_sim;
                    min_o = o2;
                }else if(tmp_sim <threshold1 && tmp_sim>max_sim && tmp_sim >= threshold2){
                    max_sim = tmp_sim;
                    max_o = o2;
                }
            }
            int a = 0;
            if(point_flag.contains(min_o) || point_flag.contains(max_o)){
                System.exit(1);
            }
            if(min_sim >= threshold1 && min_sim <Double.MIN_VALUE/2){
                new_cluster.addPoint(min_o);
                point_flag.add(min_o);
                has_point.remove(min_o);
                a++;

            }else if(max_sim>=threshold2){
                new_cluster.addPoint(max_o);
                point_flag.add(max_o);
                has_point.remove(max_o);
                a++;
            }

            if(a==2||new_cluster.getPointSize() ==3)
                System.exit(0);
            new_cluster.setSEC(threshold1);
            new_cluster.setAlpha2(alpha2);
            new_cluster.setAlpha1(alpha1);
            new_cluster.addToPotSizeList(new_cluster.getPoints().get(0),new_cluster.getPointSize());
            clusters_set.add(new_cluster);
            size += new_cluster.getPointSize();
        }
        System.out.println("real size="+size);
        System.out.println("delete size= "+point_flag.size());
        System.out.println("first step finished "+clusters_set.size());

        //第一阶段　结果
        Iterator<Cluster> clusterIterator = clusters_set.iterator();
        while(clusterIterator.hasNext()){
            Cluster cluster = clusterIterator.next();
            ArrayList<Integer> points = cluster.getPoints();
            pw.print("\n");
            pw.println(cluster.getMergeEdgeNum());
            pw.println("-opt+"+cluster.getOpt());
            pw.println("-RC++"+cluster.getRC());
            pw.println("-RI++"+cluster.getRI());
            for(Integer point:points){

                pw.print(i_s_map.get(point)+"\t");
            }
            pw.print("\n");
        }
        System.out.println("output step one finished");



        //heap
        mt.mm();

        while(!clusters_set.isEmpty()){
            Iterator<Cluster> iterator = clusters_set.iterator();
            Cluster now = null;
            if(iterator.hasNext()){
                now = iterator.next();
            }else {
                break;
            }
            int err_num = 0;
            Double max_Opt = Double.MIN_VALUE;
            Double max_RC = Double.MIN_VALUE;
            Double max_RI = Double.MIN_VALUE;
            Integer max_NearId = -1;
            Cluster merge_to = null;
            while (iterator.hasNext()){
                Cluster c1 = iterator.next();
                Double t_RC = chameleonTool.calRC(c1,now);
                Double t_RI = chameleonTool.calRI(c1,now);
                Double t_SEC = chameleonTool.calSEC(c1,now,true);
                Double t_opt = chameleonTool.calFunctionDefinedOptimization(t_RI,t_RC);

                {
                    if(t_opt>max_Opt &&  t_RC> max_RC) {
                        max_Opt = t_opt;
                        max_RC = t_RC;
                        max_RI = t_RI;
                        max_NearId = c1.getPoints().get(0);
                        merge_to = c1;
                    }
                }
            }

            if(max_Opt > minMetric && max_RC > minRC){
                if(now.getPointSize() < merge_to.getPointSize()){
                    Cluster tmp_c = merge_to;
                    merge_to = now ;
                    now = tmp_c;
                }
                MergeMess mergeMess = new MergeMess();
                mergeMess.setE_first(merge_to.getPoints().get(0));
                mergeMess.setE_size(merge_to.getPointSize());
                mergeMess.setRC(max_RC);
                mergeMess.setRI(max_RI);
                now.addToMergeCluList(mergeMess);
                now = chameleonTool.mergeTwoClustersToOne(now,merge_to,max_Opt,max_RI,max_RC);
                clusters_set.remove(merge_to);


            }else {
                clusters_set.remove(now);
                Cluster c1 = now;
                cluster_num++;
                boolean log2_flag =false;
                boolean log3_flag = false;
                ArrayList<Integer> points = c1.getPoints();
                //ArrayList<Pair<Integer,Integer>>  opt_sizes = c1.getPot_size_list();
                ArrayList<MergeMess> mergeMesses = c1.getMerge_clu_list();
                if(points.size()>=8){
                    log2_flag = true;
                    log2.println(points.size()+"-"+mergeMesses.size()+"-"+i_s_map.get(c1.getMaxNearID())+"----------------------------------------------------------------------------");
                    int size_sum = 0;
                    for(int i = 0;i<mergeMesses.size();i++){
                        size_sum += mergeMesses.get(i).getE_size();
                        log2.print(String.format("%s:%.2f:%.2f:%s:%s:\t",i_s_map.get(mergeMesses.get(i).getE_first()),mergeMesses.get(i).getRC(),mergeMesses.get(i).getRI(),mergeMesses.get(i).getE_size(),size_sum));
                    }
                    log2.print("\n");



                }else{
                    log3_flag = true;
                    log3.println(points.size()+"-"+mergeMesses.size()+"-"+i_s_map.get(c1.getMaxNearID())+"----------------------------------------------------------------------------");
                    int size_sum = 0;
                    for(int i = 0;i<mergeMesses.size();i++){
                        size_sum += mergeMesses.get(i).getE_size();
                        log3.print(String.format("%s:%.2f:%.2f:%s:%s:\t",i_s_map.get(mergeMesses.get(i).getE_first()),mergeMesses.get(i).getRC(),mergeMesses.get(i).getRI(),mergeMesses.get(i).getE_size(),size_sum));
                    }
                    log3.print("\n");

                    log3.print("opt+"+c1.getOpt()+"\t");
                    log3.print("RC++"+c1.getRC()+"\t");
                    log3.print("RI++"+c1.getRI()+"\n");
                    log3.print("mopt"+c1.getMaxOpt()+"\t");
                    log3.print("mRC+"+c1.getMaxRC()+"\t");
                    log3.print("mRI+"+c1.getMaxRI()+"\n");
                    pw2.println(c1.getMergeEdgeNum());
                    log3.print("SEC+"+c1.getSEC()+"\n");

                }


                pw2.print("\n");
                pw2.println(points.size()+"-"+mergeMesses.size()+"-"+i_s_map.get(c1.getMaxNearID())+"----------------------------------------------------------------------------");
                int size_sum = 0;
                for(int i = 0;i<mergeMesses.size();i++){
                    size_sum += mergeMesses.get(i).getE_size();
                    pw2.print(String.format("%s:%.2f:%.2f:%s:%s:\t",i_s_map.get(mergeMesses.get(i).getE_first()),mergeMesses.get(i).getRC(),mergeMesses.get(i).getRI(),mergeMesses.get(i).getE_size(),size_sum));
                }
                pw2.print("\n");
                pw2.print("opt+"+c1.getOpt()+"\t");
                pw2.print("RC++"+c1.getRC()+"\t");
                pw2.print("RI++"+c1.getRI()+"\n");
                pw2.print("mopt"+c1.getMaxOpt()+"\t");
                pw2.print("mRC+"+c1.getMaxRC()+"\t");
                pw2.print("mRI+"+c1.getMaxRI()+"\n");
                pw2.println(c1.getMergeEdgeNum());
                pw2.print("SEC+"+c1.getSEC()+"\n");
                for(Integer point:points){
                    if(log2_flag)
                        log2.print(s_f_map.get(i_s_map.get(point))+"\t");
                    if(log3_flag)
                        log3.print(s_f_map.get(i_s_map.get(point))+"\t");
                    pw2.print(s_f_map.get(i_s_map.get(point))+"\t");
                }
                if(log2_flag)
                    log2.print("\n");
                if(log3_flag)
                    log3.print("\n");
                pw2.print("\n");
                for(Integer point:points){
                    if(log2_flag)
                        log2.print(i_s_map.get(point)+"\t");
                    if(log3_flag)
                        log3.print(i_s_map.get(point)+"\t");
                    pw2.print(i_s_map.get(point)+"\t");
                }
                if(log2_flag)
                    log2.print("\n");
                if(log3_flag)
                    log3.print("\n");
                pw2.print("\n");

            }


            if(clusters_set.size() % 1000==0){
                System.out.println("--"+clusters_set.size()+"--");

                pw2.flush();
                log.flush();
                log2.flush();
                log3.flush();

            }

        }
        mt.mm();
        System.out.println("second step finished"+"-"+clusters_set.size()+"--");

        log.flush();
        log.close();
        pw.flush();
        pw.close();
        pw2.flush();
        pw.close();
        log2.flush();
        log2.close();
        log3.flush();
        log3.close();
        System.out.println("output step two finished");
        System.out.println("all finished! cluster num: "+cluster_num);
        mt.dd();
    }


}
