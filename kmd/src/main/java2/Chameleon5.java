package src.main.java2;
import fig.basic.Pair;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by yishuihan on 17-7-22.
 */

public class Chameleon5 {
    public static void main(String[] args) throws Exception{
        String file = "";
        String label_file = "";
        String topN_file = "";
        String out = "";
        String out2 = "";
        String log_file = "";
        String log2_file = "";
        String log3_file = "";
        Integer threadnum = 1;
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
            }else if(arg.equalsIgnoreCase("thread")){
                threadnum = Integer.valueOf(args[i + 1]);
            }

        }


        MyTimeClock mt = new MyTimeClock("---101---");
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
        Double threshold1 = 1.0;
        Double threshold2 = 0.65;
        Double miss_value = -1.0;
        Integer min_cluster_max_num = 2;
        Boolean use_min_cluster_max_num = true;
        chameleonTool.setMinClusterMaxNum(min_cluster_max_num);
        chameleonTool.setMissVsalue(miss_value);
        chameleonTool.setThreshold(threshold2);

        //组件
        HashSet<Cluster> clusters_set = new HashSet <>();
        Queue<Integer> has_point = new PriorityQueue<>();
        ComparetorPair comparetorPair = new ComparetorPair();
        PriorityBlockingQueue<ClusterPair> clusters_priQueue = new PriorityBlockingQueue <>(10000,comparetorPair);

        has_point.addAll(set_s);//加入全部点索引
        HashSet<Integer> point_flag = new HashSet<>();
        //第二阶段　合并－初始化
        //第二阶段　设置超参数
        Double minMetric = 0.0;
        Double minRC = 0.7;
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
        point_flag.clear();
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
        System.out.println("thread count: "+Thread.activeCount());
        HashMap<Cluster,ArrayList<ClusterPair>> c_f_map = new HashMap <>();

        //第二阶段　合并－运行

        mt.mm();
        //多线程
        Thread t = null;
        ThreadDealt[] threadDealts = new ThreadDealt[threadnum];
        ConcurrentLinkedQueue<Pair<Cluster,Cluster>> in_queue = new ConcurrentLinkedQueue <>();
        ConcurrentLinkedQueue<ClusterPair> out_queue = new ConcurrentLinkedQueue <>();
        for(int i =0;i<threadnum;i++){
            threadDealts[i] = new ThreadDealt(clusters_priQueue,out_queue,in_queue,chameleonTool);
            t = new Thread(threadDealts[i]);
            t.setDaemon(true);
            t.start();

        }
        ThreadDealtNext threadDealtN = new ThreadDealtNext(out_queue,c_f_map);
        t = new Thread(threadDealtN);
        t.setDaemon(true);
        t.start();
        int i=0;
        int j=0;
        //初始化
        for(Cluster c1:clusters_set){
            j=0;
            for(Cluster c2:clusters_set){
                if(i<=j){break;}
                in_queue.add(new Pair <>(c1,c2));
                while (in_queue.size()>10000){
                    //System.out.println("+"+in_queue.size());
                    Thread.sleep(10);
                    //System.out.println("-"+in_queue.size());
                }

                j++;
            }
            if(out_queue.size()> 4000){
                System.out.println("bug "+out_queue);
            }
            if(i % 1000 == 0)
                System.out.println(i+"-------"+clusters_priQueue.size());
            i++;
        }
        mt.mm();
        while (!in_queue.isEmpty()||!out_queue.isEmpty()){
            Thread.sleep(2);
        }
        for(i = 0;i<threadnum;i++){
            threadDealts[i].setIn_finished(true);
        }
        threadDealtN.setIn_finished(true);

        System.out.println("build priorityQueue:"+clusters_set.size()+"-"+clusters_priQueue.size() );
        //rm keys,then out
        mt.mm();
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
                pw2.print(i_s_map.get(opt_sizes.get(i).getFirst())+":"+opt_sizes.get(i).getSecond()+":"+size_sum+"\t");
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

                pw2.print(s_f_map.get(i_s_map.get(point))+"\t");
            }

            pw2.print("\n");
            for(Integer point:points){

                pw2.print(i_s_map.get(point)+"\t");
            }

            pw2.print("\n\n");
        }
        //add keys
        clusters_set.clear();
        clusters_set.addAll(c_f_map.keySet());
        System.out.println("delete not uesed:"+clusters_set.size()+"-"+clusters_priQueue.size() );
        System.out.println("thread count: "+Thread.activeCount());
        pw2.println("-----------------------------+++++++++++++++");
        //heap
        mt.mm();
        ThreadDealt2[] threadDealt2s = new ThreadDealt2[threadnum];
        TemMess temMess = new TemMess(false);
        for(int ii =0;ii<threadnum;ii++){
            threadDealt2s[ii] = new ThreadDealt2(clusters_priQueue,out_queue,in_queue,chameleonTool,temMess);
            threadDealt2s[ii].setThrehold(minMetric,minRC,minRI);
            t = new Thread(threadDealt2s[ii]);
            t.setDaemon(true);
            t.start();
        }
        ThreadDealt2Next threadDealt2Next  = new ThreadDealt2Next(out_queue,c_f_map);
        t =  new Thread(threadDealt2Next);
        t.setDaemon(true);
        t.start();
        boolean rm_flag = false;
        long delT = 0;
        long maxT = 0;
        long maxT2000 = 0;
        long delT2000 = 0;
        while(!clusters_set.isEmpty() && !clusters_priQueue.isEmpty()){
            long tmp1 = System.currentTimeMillis();
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
                //c1.addToPotSizeList(c2.getPoints().get(0),c2.getPointSize());
                MergeMess mergeMess = new MergeMess();
                mergeMess.setE_first(c2.getPoints().get(0));
                mergeMess.setE_size(c2.getPointSize());
                mergeMess.setRC(RC);
                mergeMess.setRI(RI);
                c1.addToMergeCluList(mergeMess);
                if(!in_queue.isEmpty()||!out_queue.isEmpty()){
                    System.err.println("没法搞呀没法搞呀没法搞呀");
                }
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

            long tmp2 = System.currentTimeMillis();
            temMess.setFlag(false);
            for(Cluster cluster:clusters_set){
                in_queue.add(new Pair <>(c1,cluster));

            }

            c1.setMaxOpt(temMess.getMaxOpt());
            c1.setMaxRC(temMess.getMaxRC());
            c1.setMaxRI(temMess.getMaxRI());
            c1.setMaxNearID(temMess.getMostNearClu());
            boolean end_flag = false;
            while (!end_flag || !in_queue.isEmpty() || !out_queue.isEmpty()){
                end_flag = true;
                for(j = 0;j<threadnum;j++){
                    if(threadDealt2s[j].getOut_finished()==false){
                        end_flag = false;
                        break;
                    }
                }
                Thread.sleep(1);


            }
            long tmp3 = System.currentTimeMillis();
            if(temMess.getFlag() == false){
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
                    for(i = 0;i<mergeMesses.size();i++){
                        size_sum += mergeMesses.get(i).getE_size();
                        log2.print(String.format("%s:%.2f:%.2f:%s:%s:\t",i_s_map.get(mergeMesses.get(i).getE_first()),mergeMesses.get(i).getRC(),mergeMesses.get(i).getRI(),mergeMesses.get(i).getE_size(),size_sum));
                    }
                    log2.print("\n");



                }else{
                    log3_flag = true;
                    log3.println(points.size()+"-"+mergeMesses.size()+"-"+i_s_map.get(c1.getMaxNearID())+"----------------------------------------------------------------------------");
                    int size_sum = 0;
                    for(i = 0;i<mergeMesses.size();i++){
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
                for(i = 0;i<mergeMesses.size();i++){
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

            }else {
                c1.setMaxOpt(Double.MIN_VALUE);
                c1.setMaxRI(Double.MIN_VALUE);
                c1.setMaxRC(Double.MIN_VALUE);
                clusters_set.add(c1);
            }

            if(clusters_set.size() % 1000==0){
                System.out.println("------------"+clusters_set.size()+"-"+clusters_priQueue.size());
                System.out.println("del time : "+ delT2000);
                System.out.println("max time : "+ maxT2000);
                pw2.flush();
                log.flush();
                log2.flush();
                log3.flush();
                mt.mm();
                delT2000 = 0;
                maxT2000  = 0;
            }
            maxT += tmp3 - tmp2;
            delT += tmp2 - tmp1;
            maxT2000 += tmp3 - tmp2;
            delT2000 += tmp2 - tmp1;

        }
        for(i = 0;i<threadnum;i++){
            threadDealt2s[i].setIn_finished(true);
        }
        threadDealt2Next.setIn_finished(true);
        mt.mm();
        System.out.println("second step finished"+"-"+clusters_set.size()+"-"+clusters_priQueue.size());
        System.out.println("del time : "+ delT);
        System.out.println("max time : "+ maxT);
        //第二阶段结果
        Iterator<Cluster> clusterIterator2 = clusters_set.iterator();
        pw2.println("-----------------------------++++++++++++++++++++++++++++++");
        while(clusterIterator2.hasNext()){
            cluster_num++;
            Cluster cluster2 = clusterIterator2.next();
            ArrayList<Integer> points = cluster2.getPoints();
            //ArrayList<Pair<Integer,Integer>>  opt_sizes = cluster2.getPot_size_list();
            ArrayList<MergeMess> mergeMesses = cluster2.getMerge_clu_list();
            boolean log2_flag = false;
            if(points.size()>=8){
                log2_flag = true;
                if(cluster2.getMaxNearID() ==-1)
                    log2.println(points.size()+"-"+mergeMesses.size()+"-"+"+++++++"+"----------------------------------------------------------------------------");
                else
                    log2.println(points.size()+"-"+mergeMesses.size()+"-"+i_s_map.get(cluster2.getMaxNearID())+"----------------------------------------------------------------------------");
                int size_sum = 0;
                for(i = 0;i<mergeMesses.size();i++){
                    size_sum += mergeMesses.get(i).getE_size();
                    log2.print(String.format("%s:%.2f:%.2f:%s:%s:\t",i_s_map.get(mergeMesses.get(i).getE_first()),mergeMesses.get(i).getRC(),mergeMesses.get(i).getRI(),mergeMesses.get(i).getE_size(),size_sum));
                }
                log2.print("\n");



            }
            pw2.print("\n");
            if(cluster2.getMaxNearID() == -1)
                pw2.println(points.size()+"-"+mergeMesses.size()+"-"+"+++++++"+"----------------------------------------------------------------------------");
            else
                pw2.println(points.size()+"-"+mergeMesses.size()+"-"+i_s_map.get(cluster2.getMaxNearID())+"----------------------------------------------------------------------------");
            int size_sum = 0;
            for(i = 0;i<mergeMesses.size();i++){
                size_sum += mergeMesses.get(i).getE_size();
                size_sum += mergeMesses.get(i).getE_size();
                pw2.print(String.format("%s:%.2f:%.2f:%s:%s:\t",i_s_map.get(mergeMesses.get(i).getE_first()),mergeMesses.get(i).getRC(),mergeMesses.get(i).getRI(),mergeMesses.get(i).getE_size(),size_sum));
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
class ThreadDealt implements Runnable{
    private Cluster c1 = null;
    private Cluster c2 = null;
    private boolean in_finished = false;
    private ConcurrentLinkedQueue<ClusterPair> out2_queue = null;
    private PriorityBlockingQueue<ClusterPair> out_queue = null;
    private ConcurrentLinkedQueue<Pair<Cluster,Cluster>> in_queue = null;
    private ChameleonTool chameleonTool = null;
    public ThreadDealt(PriorityBlockingQueue<ClusterPair> out_queue,ConcurrentLinkedQueue<ClusterPair> out2_queue,
                       ConcurrentLinkedQueue<Pair<Cluster,Cluster>> in_queue,ChameleonTool chameleonTool){
        this.out_queue = out_queue;
        this.out2_queue = out2_queue;
        this.in_queue = in_queue;
        this.chameleonTool = chameleonTool;
        in_finished = false;

    }

    public void setIn_finished(boolean in_finished) {
        this.in_finished = in_finished;
    }

    @Override
    public void run() {
        while (in_finished == false || !in_queue.isEmpty()){
            Pair<Cluster,Cluster> pair = in_queue.poll();
            if(pair ==null)
                continue;
            c1 = pair.getFirst();
            c2 = pair.getSecond();


            Pair<Double,Integer> pair1 = chameleonTool.calSEC(c1,c2);
            Double SEC = pair1.getFirst()/pair1.getSecond();
            Double RC = chameleonTool.calRC(c1,c2,pair1);
            Double RI = chameleonTool.calRI(c1,c2,pair1);
            Double opt = chameleonTool.calFunctionDefinedOptimization(RC,RI);

            if(RC>=0.3){
                ClusterPair tmp_pair = new ClusterPair(c1,c2);
                tmp_pair.setOpt(opt);
                tmp_pair.setRC(RC);
                tmp_pair.setRI(RI);
                tmp_pair.setSEC(SEC);
                out2_queue.add(tmp_pair);
                out_queue.add(tmp_pair);
            }
        }

    }
}
class ThreadDealtNext implements Runnable{
    private Cluster c1 = null;
    private Cluster c2 = null;
    private boolean in_finished = false;
    private boolean out_finished = false;
    private ConcurrentLinkedQueue<ClusterPair> inN_queue = null;
    private HashMap<Cluster,ArrayList<ClusterPair>> c_f_map = null;
    public ThreadDealtNext(ConcurrentLinkedQueue<ClusterPair> inN_queue,HashMap<Cluster,ArrayList<ClusterPair>> c_f_map){
        this.inN_queue = inN_queue;
        in_finished = false;
        this.c_f_map = c_f_map;
        out_finished = false;
    }

    public void setIn_finished(boolean in_finished) {
        this.in_finished = in_finished;
    }

    @Override
    public void run() {
        while (in_finished == false || !inN_queue.isEmpty()){
            out_finished = false;
            ClusterPair clusterPair = inN_queue.poll();
            if(clusterPair==null)
                continue;
            c1 = clusterPair.getC1();
            c2 = clusterPair.getC2();
            if(c_f_map.containsKey(c1)){
                ArrayList<ClusterPair> pairs = c_f_map.get(c1);
                pairs.add(clusterPair);
            }else {

                ArrayList<ClusterPair> pairs = new ArrayList <>();
                pairs.add(clusterPair);
                c_f_map.put(c1,pairs);
            }
            if(c_f_map.containsKey(c2)){
                ArrayList<ClusterPair> pairs = c_f_map.get(c2);
                pairs.add(clusterPair);
            }else {

                ArrayList<ClusterPair> pairs = new ArrayList <>();
                pairs.add(clusterPair);
                c_f_map.put(c2,pairs);
            }
            out_finished = true;

        }

    }
}

class ThreadDealt2 implements Runnable{
    private ConcurrentLinkedQueue<ClusterPair> out2_queue = null;
    private PriorityBlockingQueue<ClusterPair> out_queue = null;
    private boolean in_finished = false;
    private boolean out_finished = false;
    private ConcurrentLinkedQueue<Pair<Cluster,Cluster>> in_queue = null;
    private ChameleonTool chameleonTool = null;
    TemMess temMess = null;
    public ThreadDealt2(PriorityBlockingQueue<ClusterPair> out_queue,ConcurrentLinkedQueue<ClusterPair> out2_queue,ConcurrentLinkedQueue<Pair<Cluster,Cluster>> in_queue,
                        ChameleonTool chameleonTool,TemMess temMess){
        this.temMess = temMess;
        this.out_queue = out_queue;
        this.out2_queue = out2_queue;
        this.in_queue = in_queue;
        this.chameleonTool = chameleonTool;
        in_finished = false;

    }
    private Double minMetric = 0.0;
    private Double min_RC = 0.0;
    private Double min_RI = 0.0;
    public void setThrehold(Double minMetric,Double min_RC,Double min_RI){
        this.minMetric = minMetric;
        this.min_RC = min_RC;
        this.min_RI = min_RI;
    }
    public void setIn_finished(boolean in_finished) {
        this.in_finished = in_finished;
    }
    public boolean getOut_finished(){
        return  this.out_finished;
    }
    @Override
    public void run() {
        while (in_finished == false) {

            while (!in_queue.isEmpty()) {
                Pair <Cluster, Cluster> pair = in_queue.poll();
                if (pair == null)
                    continue;
                out_finished = false;
                Cluster c1 = pair.getFirst();
                Cluster cluster = pair.getSecond();
                Pair<Double,Integer> pair1 = chameleonTool.calSEC(c1, cluster);
                Double t_SEC = pair1.getFirst() / pair1.getSecond();
                Double t_RC = chameleonTool.calRC(c1, cluster,pair1);
                Double t_RI = chameleonTool.calRI(c1, cluster,pair1);
                Double t_opt = chameleonTool.calFunctionDefinedOptimization(t_RI, t_RC);
                if (t_opt > minMetric && t_RC > min_RC) {
                    ClusterPair tmp_pair = new ClusterPair(c1, cluster);
                    tmp_pair.setOpt(t_opt);
                    tmp_pair.setRC(t_RC);
                    tmp_pair.setRI(t_RI);
                    tmp_pair.setSEC(t_SEC);
                    out_queue.add(tmp_pair);
                    out2_queue.add(tmp_pair);

                    if(temMess.getFlag() == false)
                        temMess.setFlag(true);
                }


            }
            out_finished = true;
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
class ThreadDealt2Next implements Runnable{
    private Cluster c1 = null;
    private Cluster c2 = null;
    private boolean in_finished = false;
    private boolean out_finished  = false;
    private ConcurrentLinkedQueue<ClusterPair> inN_queue = null;
    private HashMap<Cluster,ArrayList<ClusterPair>> c_f_map = null;
    public ThreadDealt2Next(ConcurrentLinkedQueue<ClusterPair> inN_queue,HashMap<Cluster,ArrayList<ClusterPair>> c_f_map){
        this.inN_queue = inN_queue;
        in_finished = false;
        this.c_f_map = c_f_map;
    }
    public void setIn_finished(boolean in_finished) {
        this.in_finished = in_finished;
    }
    public boolean getOut_finished(){
        return this.out_finished;
    }
    @Override
    public void run() {
        while (in_finished == false){

            while(!inN_queue.isEmpty()){
                ClusterPair clusterPair = inN_queue.poll();
                if(clusterPair==null)
                    continue;
                out_finished = false;
                c1 = clusterPair.getC1();
                c2 = clusterPair.getC2();
                ArrayList<ClusterPair> tmp_pairs1 = c_f_map.get(c1);
                ArrayList<ClusterPair> tmp_pairs2 = c_f_map.get(c2);
                tmp_pairs1.add(clusterPair);
                tmp_pairs2.add(clusterPair);

            }

            out_finished = true;
            try {
                Thread.sleep(10);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }
}
class TemMess{
    boolean flag = false;
    boolean finish = false;

    public boolean isFinish() {
        return finish;
    }

    public void setFinish(boolean finish) {
        this.finish = finish;
    }

    Double maxOpt = -1.0;
    Double maxRI = -1.0;
    Double maxRC = -1.0;
    Integer mostNearClu = -1;
    TemMess(boolean flag){
        this.flag = flag;
    }
    public boolean getFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public Double getMaxOpt() {
        return maxOpt;
    }

    public void setMaxOpt(Double maxOpt) {
        this.maxOpt = maxOpt;
    }

    public Double getMaxRI() {
        return maxRI;
    }

    public void setMaxRI(Double maxRI) {
        this.maxRI = maxRI;
    }

    public Double getMaxRC() {
        return maxRC;
    }

    public void setMaxRC(Double maxRC) {
        this.maxRC = maxRC;
    }

    public Integer getMostNearClu() {
        return mostNearClu;
    }

    public void setMostNearClu(Integer mostNearClu) {
        this.mostNearClu = mostNearClu;
    }
}