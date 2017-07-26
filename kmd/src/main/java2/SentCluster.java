package src.main.java2;
import fig.basic.Pair;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yishuihan on 17-7-3.
 */
public class SentCluster {
    public static void main(String[] args) throws Exception{

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

            if(arg.equalsIgnoreCase("mat")) {
                topN_file = args[i + 1];
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


        MyTimeClock mt = new MyTimeClock("111101101");
        mt.tt();
        //引入sent map

        LoadData2 loadData2 = new LoadData2();

        loadData2.buildSentencesIndexMap(topN_file);
        HashMap<String, Integer> s_i_map = loadData2.getSentencesIndexMap();
        HashMap <Integer, String> i_s_map = loadData2.getIndexSentencesMap();
        System.out.println("load sentences map finised");






        //引入sent top_n map
        //设置超参数
        //k=60
        Integer knn =200;
        loadData2.buildSentTopNMatMap(topN_file, s_i_map,knn);
        ConcurrentHashMap<Integer, HashMap <Integer, Double>> sparse_mat = loadData2.getSparseMat();
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
        Double threshold = 0.60;
        Double miss_value = -1.0;
        Integer min_cluster_max_num = 2;
        Boolean use_min_cluster_max_num = true;
        chameleonTool.setMinClusterMaxNum(min_cluster_max_num);
        chameleonTool.setMissVsalue(miss_value);
        chameleonTool.setThreshold(threshold);

        //组件
        HashSet<Cluster> clusters_set = new HashSet <>();
        Queue<Integer> has_point = new ArrayDeque <>();
        ComparetorPair comparetorPair = new ComparetorPair();
        Queue<ClusterPair> clusters_priQueue = new PriorityQueue <>(10000,comparetorPair);

        has_point.addAll(set_s);//加入全部点索引
        HashSet<Integer> point_flag = new HashSet<>();
        //第二阶段　合并－初始化
        //第二阶段　设置超参数
        Double minMetric = 0.50;
        Double minRC = 0.5;
        Double minRI = 0.0;
        Double alpha1 = 1.0;//RI^alph1 * RC^alpha2
        Double alpha2 = 2.0; //RI^alph1 * RI^alph2
        String sort_para = "maxOpt";
        chameleonTool.setAlpha1(alpha1);
        chameleonTool.setAlpha2(alpha2);
        //设置输出
        out += "_"+alpha1+":"+alpha2+"_"+min_cluster_max_num+"_"+use_min_cluster_max_num+"_"+String.valueOf(threshold)+
                "_knn_"+knn+"_minRC_"+minRC+"_minRI_"+minRI+"_minMetric_"+ minMetric+"_"+sort_para+"_step1.txt";
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(out),"utf-8"),true);
        //设置输出
//        out2+= "_minRC_"+String.valueOf(minRC).substring(0,3)+"_minRI_"+String.valueOf(minRI).substring(0,3)+"_minMetirc_"+
//                String.valueOf(minMetric).substring(0,3)+"_step2.txt";
        out2 +="_"+alpha1+":"+alpha2+ "_"+min_cluster_max_num+"_"+use_min_cluster_max_num+"_"+String.valueOf(threshold)+
                "_knn_"+knn+"_minRC_"+minRC+"_minRI_"+minRI+"_minMetric_"+ minMetric+"_"+sort_para+"_step2.txt";
        PrintWriter pw2 = new PrintWriter(new OutputStreamWriter(new FileOutputStream(out2),"utf-8"),true);
//        log_file += "_minRC_"+String.valueOf(minRC).substring(0,3)+"_minRI_"+String.valueOf(minRI).substring(0,3)+"_minMetirc_"+
//                String.valueOf(minMetric).substring(0,3)+"_log.txt";
        log_file += "_"+alpha1+":"+alpha2+"_"+min_cluster_max_num+"_"+use_min_cluster_max_num+"_"+String.valueOf(threshold)+
                "_knn_"+knn+"_minRC_"+minRC+"_minRI_"+minRI+"_minMetric_"+
                minMetric+"_"+sort_para+"_log1.txt";
        log2_file += "_"+alpha1+":"+alpha2+"_"+min_cluster_max_num+"_"+use_min_cluster_max_num+"_"+String.valueOf(threshold)+
                "_knn_"+knn+"_minRC_"+minRC+"_minRI_"+minRI+"_minMetric_"+
                minMetric+"_"+sort_para+"_log2.txt";
        log3_file += "_"+alpha1+":"+alpha2+"_"+min_cluster_max_num+"_"+use_min_cluster_max_num+"_"+String.valueOf(threshold)+
                "_knn_"+knn+"_minRC_"+minRC+"_minRI_"+minRI+"_minMetric_"+
                minMetric+"_"+sort_para+"_log3.txt";
        PrintWriter log = new PrintWriter(new OutputStreamWriter(new FileOutputStream(log_file),"utf-8"),true);
        PrintWriter log2 = new PrintWriter(new OutputStreamWriter(new FileOutputStream(log2_file),"utf-8"),true);
        PrintWriter log3 = new PrintWriter(new OutputStreamWriter(new FileOutputStream(log3_file),"utf-8"),true);
        ArrayList<Cluster> result = new ArrayList <>();
        //第一阶段　构建最小簇－运行
        int size = 0;
        int cluster_num = 0;
        while(!has_point.isEmpty()){
            Cluster new_cluster = new Cluster(has_point.size(),chameleonTool);

            new_cluster.initPoint();
            Integer point = has_point.poll();
            point_flag.add(point);
            new_cluster.addPoint(point);
            if(has_point.isEmpty()){
                size += new_cluster.getPointSize();
                new_cluster.setSEC(threshold);
                new_cluster.setAlpha1(alpha1);
                new_cluster.setAlpha2(alpha2);
                clusters_set.add(new_cluster);
                break;
            }

            Double max_sim = Double.MIN_VALUE;
            Integer max_o = 0;
            HashMap<Integer, Double> top_n = chameleonTool.getPointTopN(point);
            for(Map.Entry<Integer,Double>entry: top_n.entrySet()){
                Integer o2 = entry.getKey();

                if(point_flag.contains(o2))
                    continue;
                Double tmp_sim = chameleonTool.getPairCenterPointSim(new_cluster.getPoints().get(0),o2);
                if(tmp_sim > max_sim && tmp_sim>= threshold){
                    max_sim = tmp_sim;
                    max_o = o2;
                }
            }

            if(max_sim >= threshold){
                new_cluster.addPoint(max_o);
                point_flag.add(max_o);
                has_point.remove(max_o);

            }
            new_cluster.setSEC(threshold);
            new_cluster.setAlpha2(alpha2);
            new_cluster.setAlpha1(alpha1);
            new_cluster.addToPotSizeList(new_cluster.getPoints().get(0),new_cluster.getPointSize());
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
        mt.dd();
        HashMap<Cluster,ArrayList<ClusterPair>> c_f_map = new HashMap<>();
        int count = 0;
        //第二阶段　合并－运行

        int i=0;
        int j=0;
        //初始化
        for(Cluster c1:clusters_set){
            j=0;
            for(Cluster c2:clusters_set){
                if(i<=j){break;}

                Double RC = chameleonTool.calRC(c1,c2);
                Double RI = chameleonTool.calRI(c1,c2);
                Double SEC = chameleonTool.calSEC(c1,c2,true);
                Double opt = chameleonTool.calFunctionDefinedOptimization(RC,RI);


                {
                    c1.setGlobal_MaxOpt(opt);
                    c2.setGlobal_MaxOpt(opt);
                    c1.setGlobal_MaxRC(RC);
                    c2.setGlobal_MaxRC(RC);
                    c1.setGlobal_MaxRI(RI);
                    c2.setGlobal_MaxRI(RI);
                }
                if(RC>0.5){
                    ClusterPair tmp_pair = new ClusterPair(c1,c2);
                    tmp_pair.setOpt(opt);
                    tmp_pair.setRC(RC);
                    tmp_pair.setRI(RI);
                    tmp_pair.setSEC(SEC);
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
            if(i % 100 == 0)
                System.out.println(i+"-"+clusters_priQueue.size());
            i++;
        }
        System.out.println("build priorityQueue:"+clusters_set.size()+"-"+clusters_priQueue.size() );
        //rm keys,then out
        mt.dd();
        clusters_set.removeAll(c_f_map.keySet());
        for(Cluster cluster: clusters_set){
            cluster_num++;

            ArrayList<Integer> points = cluster.getPoints();
            ArrayList<Pair<Integer,Integer>>  opt_sizes = cluster.getPot_size_list();
            pw2.print("\n");
            pw2.println(points.size()+"-"+opt_sizes.size()+"-----------------------------------------------------------------------------");
            int size_sum = 0;
            for(i = 0;i<opt_sizes.size();i++){
                size_sum += opt_sizes.get(i).getSecond();
                pw2.print(i_s_map.get(opt_sizes.get(i).getFirst())+":"+opt_sizes.get(i).getSecond()+":"+size_sum+"\n");
            }
            pw2.print("\n");
            pw2.print("opt+ "+cluster.getOpt()+"\t");
            pw2.print("RC++ "+cluster.getRC()+"\t");
            pw2.print("RI++ "+cluster.getRI()+"\n");
            pw2.print("mopt "+cluster.getGlobal_MaxOpt()+"\t");
            pw2.print("mRC+ "+cluster.getGlobal_MaxRC()+"\t");
            pw2.print("mRI+ "+cluster.getGlobal_MaxRI()+"\n");
            pw2.println(cluster.getMergeEdgeNum());
            pw2.print("SEC+ "+cluster.getSEC()+"\n");
            for(Integer point:points){

                pw2.print(i_s_map.get(point)+"\n");
            }

            pw2.print("\n");
            for(Integer point:points){

                pw2.print(i_s_map.get(point)+"\n");
            }

            pw2.print("\n\n");
        }
        //add keys
        clusters_set.clear();
        clusters_set.addAll(c_f_map.keySet());
        System.out.println("delete not uesed:"+clusters_set.size()+"-"+clusters_priQueue.size() );
        pw2.println("-----------------------------+++++++++++++++");
        //heap
        mt.dd();
        while(!clusters_set.isEmpty() && !clusters_priQueue.isEmpty()){
            ClusterPair pair = clusters_priQueue.poll();
            Cluster c1 = pair.getC1();
            Cluster c2 = pair.getC2();
            if(c1.getPointSize() < c2.getPointSize()){
                Cluster tmp_c = c1;
                c1 = c2;
                c2 = tmp_c;
            }
            Double opt = pair.getOpt();
            Double SEC = pair.getSEC();
            Double RC = pair.getRC();
            Double RI = pair.getRI();
            //merge
//            if(RC>minRC && c1.getPointSize()/c2.getPointSize() > 10){
//                RI = c1.getRI();
//            }else if(RC>minRC && c1.getPointSize()/c2.getPointSize() >5){
//                RI = c1.getRI()*0.5 + RI*0.5;
//            }else if(RC>minRC && c1.getPointSize()/c2.getPointSize() >3){
//                RI = c1.getRI()*0.3 + RI*0.7;
//            }

            {
                log.println(i_s_map.get(c1.getPoints().get(0))+"--"+i_s_map.get(c2.getPoints().get(0)));
                log.println(c1.getPointSize()+"--"+c2.getPointSize());
                log.println(c1.isIs_merged()+"--"+c2.isIs_merged());
                log.println(SEC);
                log.println("2Opt-"+c1.getOpt()+"\t"+c2.getOpt());
                log.println("2RC+-"+c1.getRC()+"\t"+c2.getRC());
                log.println("2RI+-"+c1.getRI()+"\t"+c2.getRI());
                //###
                c1.addToPotSizeList(c2.getPoints().get(0),c2.getPointSize());

                c1 = chameleonTool.mergeTwoClustersToOne(c1,c2,opt,RI,RC);
                if(c1.getPointSize()>200){
                    System.out.println(i_s_map.get(c1.getPoints().get(0)) +"\t"+c1.getPointSize());
                }
                log.println(c1.getMergeEdgeNum());
                log.println("-opt+"+c1.getOpt());
                log.println("-RC++"+c1.getRC());
                log.println("-RI++"+c1.getRI());
                log.println("");
            }
            //update opt-move
            ArrayList<ClusterPair> pairs1 = c_f_map.get(c1);
            ArrayList<ClusterPair> pairs2 = c_f_map.get(c2);

            for(ClusterPair cl1:pairs1){
                clusters_priQueue.remove(cl1);
            }
            for(ClusterPair cl2:pairs2){
                clusters_priQueue.remove(cl2);
            }
            pairs1.clear();
            pairs2.clear();
            //updata opt-add

            clusters_set.remove(c2);
            clusters_set.remove(c1);
            int err_num = 0;
            Double max_Opt = Double.MIN_VALUE;
            Double max_RC = Double.MIN_VALUE;
            Double max_RI = Double.MIN_VALUE;
            Integer max_NearId = 0;
            for(Cluster cluster:clusters_set){
                Double t_RC = chameleonTool.calRC(c1,cluster);
                Double t_RI = chameleonTool.calRI(c1,cluster);
                Double t_SEC = chameleonTool.calSEC(c1,cluster,true);
                Double t_opt = chameleonTool.calFunctionDefinedOptimization(t_RI,t_RC);

                {
                    if(t_opt>max_RI && t_RC >max_RC) {
                        max_Opt = t_opt;
                        max_RC = t_RC;
                        max_RI = t_RI;
                        max_NearId = cluster.getPoints().get(0);
                    }
                }
                if(t_RI>minRI && t_RC>minRC){
                    ClusterPair tmp_pair = new ClusterPair(c1,cluster);
                    tmp_pair.setOpt(t_opt);
                    tmp_pair.setRC(t_RC);
                    tmp_pair.setRI(t_RI);
                    tmp_pair.setSEC(t_SEC);
                    ArrayList<ClusterPair> tmp_pairs1 = c_f_map.get(c1);
                    ArrayList<ClusterPair> tmp_pairs2 = c_f_map.get(cluster);
                    tmp_pairs1.add(tmp_pair);
                    tmp_pairs2.add(tmp_pair);

                    clusters_priQueue.add(tmp_pair);
                }else {
                    err_num++;
                }


            }
            c1.setMaxOpt(max_Opt);
            c1.setMaxRC(max_RC);
            c1.setMaxRI(max_RI);
            c1.setMaxNearID(max_NearId);
            if(err_num == clusters_set.size()){
                cluster_num++;
                boolean log2_flag =false;
                boolean log3_flag = false;
                ArrayList<Integer> points = c1.getPoints();
                ArrayList<Pair<Integer,Integer>>  opt_sizes = c1.getPot_size_list();

                if(points.size()>=3){
                    log2_flag = true;
                    log2.println(points.size()+"-"+opt_sizes.size()+"-"+i_s_map.get(c1.getMaxNearID())+"----------------------------------------------------------------------------");
                    int size_sum = 0;
                    for(i = 0;i<opt_sizes.size();i++){
                        size_sum += opt_sizes.get(i).getSecond();
                        log2.print(i_s_map.get(opt_sizes.get(i).getFirst())+":"+opt_sizes.get(i).getSecond()+":"+size_sum+"\n");
                    }
                    log2.print("\n");



                }else{
                    log3_flag = true;
                    log3.println(points.size()+"-"+opt_sizes.size()+"-"+i_s_map.get(c1.getMaxNearID())+"----------------------------------------------------------------------------");
                    int size_sum = 0;
                    for(i = 0;i<opt_sizes.size();i++){
                        size_sum += opt_sizes.get(i).getSecond();
                        log3.print(i_s_map.get(opt_sizes.get(i).getFirst())+":"+opt_sizes.get(i).getSecond()+":"+size_sum+"\n");
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
                pw2.println(points.size()+"-"+opt_sizes.size()+"-"+i_s_map.get(c1.getMaxNearID())+"----------------------------------------------------------------------------");
                int size_sum = 0;
                for(i = 0;i<opt_sizes.size();i++){
                    size_sum += opt_sizes.get(i).getSecond();
                    pw2.print(i_s_map.get(opt_sizes.get(i).getFirst())+":"+opt_sizes.get(i).getSecond()+":"+size_sum+"\n");
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
                if(log2_flag)
                    log2.print("\n");
                if(log3_flag)
                    log3.print("\n");
                pw2.print("\n");
                for(Integer point:points){
                    if(log2_flag)
                        log2.print(i_s_map.get(point)+"\n");
                    if(log3_flag)
                        log3.print(i_s_map.get(point)+"\n");
                    pw2.print(i_s_map.get(point)+"\n");
                }
                if(log2_flag)
                    log2.print("\n");
                if(log3_flag)
                    log3.print("\n");
                pw2.print("\n");

            }else {
                c1.setMaxOpt(Double.MIN_VALUE);
                c1.setMaxRI(Double.MIN_VALUE);
                c1.setMaxRC(Double.MIN_VALUE);
                clusters_set.add(c1);
            }
            if(clusters_set.size() % 100==0){
                System.out.println("-"+clusters_set.size()+"-"+clusters_priQueue.size());

                pw2.flush();
                log.flush();
                log2.flush();
                log3.flush();
            }

        }

        System.out.println("second step finished"+"-"+clusters_set.size()+"-"+clusters_priQueue.size());
        //第二阶段结果
        Iterator<Cluster> clusterIterator2 = clusters_set.iterator();
        pw2.println("-----------------------------++++++++++++++++++++++++++++++");
        while(clusterIterator2.hasNext()){
            Cluster cluster2 = clusterIterator2.next();
            ArrayList<Integer> points = cluster2.getPoints();
            ArrayList<Pair<Integer,Integer>>  opt_sizes = cluster2.getPot_size_list();
            boolean log2_flag = false;
            if(points.size()>=3){
                log2_flag = true;
                log2.println(points.size()+"-"+opt_sizes.size()+"-"+i_s_map.get(cluster2.getMaxNearID())+"----------------------------------------------------------------------------");
                int size_sum = 0;
                for(i = 0;i<opt_sizes.size();i++){
                    size_sum += opt_sizes.get(i).getSecond();
                    log2.print(i_s_map.get(opt_sizes.get(i).getFirst())+":"+opt_sizes.get(i).getSecond()+":"+size_sum+"\n");
                }
                log2.print("\n");



            }
            pw2.print("\n");
            pw2.println(points.size()+"-"+opt_sizes.size()+"-"+i_s_map.get(cluster2.getMaxNearID())+"----------------------------------------------------------------------------");
            int size_sum = 0;
            for(i = 0;i<opt_sizes.size();i++){
                size_sum += opt_sizes.get(i).getSecond();
                pw2.print(i_s_map.get(opt_sizes.get(i).getFirst())+":"+opt_sizes.get(i).getSecond()+":"+size_sum+"\n");
            }
            pw2.print("\n");
            pw2.print("opt+"+cluster2.getOpt()+"\t");
            pw2.print("RC++"+cluster2.getRC()+"\t");
            pw2.print("RI++"+cluster2.getRI()+"\n");
            pw2.print("mopt"+cluster2.getMaxOpt()+"\t");
            pw2.print("mRC+"+cluster2.getMaxRC()+"\t");
            pw2.print("mRI+"+cluster2.getMaxRI()+"\n");
            pw2.println(cluster2.getMergeEdgeNum());
            pw2.print("SEC+"+cluster2.getSEC()+"\n");

            if(log2_flag) log2.print("\n");
            pw2.print("\n");
            for(Integer point:points){
                if(log2_flag)
                    log2.print(i_s_map.get(point)+"\n");
                pw2.print(i_s_map.get(point)+"\n");
            }
            if(log2_flag) log2.print("\n");
            pw2.print("\n");
        }
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

