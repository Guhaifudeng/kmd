package src.main.java2;

import fig.basic.Pair;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by yishuihan on 17-7-14.
 */
public class SentCluster3 {
    public static void main(String[] args) throws Exception{

        String topN_file = "";
        String out = "";
        String out2 = "";
        String log = "";
        String log2 = "";
        String set_sent = "";
        Integer threadnum = 1;
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
                log = args[i+1];
            }else if(arg.equalsIgnoreCase("log2")){
                log2 = args[i+1];
            }else if(arg.equalsIgnoreCase("set")){
                set_sent = args[i+1];
            }else if(arg.equalsIgnoreCase("thread")){
                threadnum = Integer.valueOf(args[i+1]);
            }

        }


        MyTimeClock mt = new MyTimeClock("---11011---");
        mt.tt();
        //引入sent map

        LoadData2 loadData2 = new LoadData2();

        loadData2.buildSentencesIndexMap(topN_file);
        HashMap<String, Integer> s_i_map = loadData2.getSentencesIndexMap();
        HashMap <Integer, String> i_s_map = loadData2.getIndexSentencesMap();

        System.out.println("load sentences map finised");
        PrintWriter pw_set = new PrintWriter(new OutputStreamWriter(new FileOutputStream(set_sent),"utf-8"),true);
        for(Map.Entry<Integer,String> tt:i_s_map.entrySet()){
            String str = tt.getValue();
            pw_set.println(str);
        }
        pw_set.flush();
        pw_set.close();
        System.out.println("output senteces set finished! " + i_s_map.size());
        //引入sent top_n map
        //设置超参数
        //k=60
        Integer knn =300;
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
        Double threshold1 = 1.0;
        Double threshold2 = 0.6;
        Double miss_value = -1.0;
        Integer min_cluster_max_num = 2;
        Boolean use_min_cluster_max_num = true;
        chameleonTool.setMinClusterMaxNum(min_cluster_max_num);
        chameleonTool.setMissVsalue(miss_value);
        chameleonTool.setThreshold(threshold2);

        //组件
        HashSet<Cluster> clusters_set = new HashSet <>();
        Queue<Integer> has_point = new ArrayDeque<>();
        ComparetorPair comparetorPair = new ComparetorPair();
        PriorityBlockingQueue<ClusterPair> clusters_priQueue = new PriorityBlockingQueue <>(10000,comparetorPair);

        has_point.addAll(set_s);//加入全部点索引
        HashSet<Integer> point_flag = new HashSet<>();
        //第二阶段　合并－初始化
        //第二阶段　设置超参数
        Double minMetric = 0.00;
        Double minRC = 0.6;
        Double minRI = 0.0;
        Double alpha1 = 1.0;//RI^alph1 * RC^alpha2
        Double alpha2 = 2.0; //RI^alph1 * RI^alph2
        String sort_para = "maxOpt";
        chameleonTool.setAlpha1(alpha1);
        chameleonTool.setAlpha2(alpha2);
        //设置输出
        out += "_"+alpha1+":"+alpha2+"_"+min_cluster_max_num+"_"+use_min_cluster_max_num+"_"+String.valueOf(threshold2)+
                "_knn_"+knn+"_minRC_"+minRC+"_minRI_"+minRI+"_minMetric_"+ minMetric+"_"+sort_para+"_step1.txt";
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(out),"utf-8"),true);
        //设置输出
//
        out2 +="_"+alpha1+":"+alpha2+ "_"+min_cluster_max_num+"_"+use_min_cluster_max_num+"_"+String.valueOf(threshold2)+
                "_knn_"+knn+"_minRC_"+minRC+"_minRI_"+minRI+"_minMetric_"+ minMetric+"_"+sort_para+"_step2.txt";
        PrintWriter pw2 = new PrintWriter(new OutputStreamWriter(new FileOutputStream(out2),"utf-8"),true);
//
        log += "_"+alpha1+":"+alpha2+"_"+min_cluster_max_num+"_"+use_min_cluster_max_num+"_"+String.valueOf(threshold2)+
                "_knn_"+knn+"_minRC_"+minRC+"_minRI_"+minRI+"_minMetric_"+
                minMetric+"_"+sort_para+"_log1.txt";
        PrintWriter pw_log = new PrintWriter(new OutputStreamWriter(new FileOutputStream(log),"utf-8"),true);
        log2 += "_"+alpha1+":"+alpha2+"_"+min_cluster_max_num+"_"+use_min_cluster_max_num+"_"+String.valueOf(threshold2)+
                "_knn_"+knn+"_minRC_"+minRC+"_minRI_"+minRI+"_minMetric_"+
                minMetric+"_"+sort_para+"_log2.txt";
        PrintWriter pw_log2 = new PrintWriter(new OutputStreamWriter(new FileOutputStream(log2),"utf-8"),true);

        HashSet<Cluster> out_result = new HashSet <>();
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
            new_cluster.setSEC(threshold2);
            new_cluster.setAlpha2(alpha2);
            new_cluster.setAlpha1(alpha1);
            new_cluster.addToPotSizeList(new_cluster.getPoints().get(0),new_cluster.getPointSize());
            clusters_set.add(new_cluster);
            size += new_cluster.getPointSize();
        }
        point_flag.clear();
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

        //第二阶段　合并－运行
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
            if(i % 2000 == 0)
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
        //未符合最初条件的
        int max_potNum = 0;
        for(Cluster cluster: clusters_set){
            cluster_num++;

            if(max_potNum < cluster.getPointSize()){
                max_potNum = cluster.getPointSize();
            }
        }
        //add object to output result
        out_result.addAll(clusters_set);
        //add keys
        clusters_set.clear();
        clusters_set.addAll(c_f_map.keySet());
        System.out.println("delete not uesed:"+clusters_set.size()+"-"+clusters_priQueue.size() );
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
            Double RC = pair.getRC();
            Double RI = pair.getRI();
            {

                //###
                c1.addToPotSizeList(c2.getPoints().get(0),c2.getPointSize());

                c1 = chameleonTool.mergeTwoClustersToOne(c1,c2,opt,RI,RC);
                if(c1.getPointSize()>200){
                    System.out.println(i_s_map.get(c1.getPoints().get(0)) +"\t"+c1.getPointSize());
                }
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
//                while (in_queue.size()>5000){
//                    //System.out.println("+"+in_queue.size());
//                    Thread.sleep(1);
//                    //System.out.println("-"+in_queue.size());
//                }
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
                out_result.add(c1);
                if(max_potNum < c1.getPointSize()){
                    max_potNum = c1.getPointSize();
                }

            }else {
                c1.setMaxOpt(Double.MIN_VALUE);
                c1.setMaxRI(Double.MIN_VALUE);
                c1.setMaxRC(Double.MIN_VALUE);
                clusters_set.add(c1);
                if(max_potNum < c1.getPointSize()){
                    max_potNum = c1.getPointSize();
                }
            }
            if(clusters_set.size() % 2000==0){
                System.out.println("-"+clusters_set.size()+"-"+clusters_priQueue.size());
                System.out.println("del time : "+ delT2000);
                System.out.println("max time : "+ maxT2000);
                pw2.flush();
                mt.mm();
                maxT2000 = 0;
                delT2000 = 0;

            }
            maxT += tmp3 - tmp2;
            delT += tmp2 - tmp1;
            maxT2000 += tmp3 - tmp2;
            delT2000 += tmp2 - tmp1;

        }
        System.out.println("second step finished"+"-"+clusters_set.size()+"-"+clusters_priQueue.size());
        System.out.println("del time : "+ delT);
        System.out.println("max time : "+ maxT);
        mt.dd();
        //第二阶段结果
        out_result.addAll(clusters_set);
        cluster_num += clusters_set.size();
        clusters_set.clear();
        clusters_priQueue.clear();
        sparse_mat.clear();
        System.out.println("output size: "+out_result.size());


        for(int ind = max_potNum;ind >= 1;ind--){
            int num =0;
            for(Cluster tmp: out_result){

                if(tmp.getPointSize() == ind){
                    pw2.print(ind+"----"+i_s_map.get(tmp.getMaxNearID())+"------------------------------------------------\n");
                    num++;
                    clusters_set.add(tmp);
                    ArrayList<Integer> pots = tmp.getPoints();
                    for(Integer it :pots){
                        pw2.print(i_s_map.get(it)+"\n");
                    }
                    pw_log2.println(i_s_map.get(tmp.getPoints().get(0))+"\t"+ind);
                }

            }
            out_result.removeAll(clusters_set);
            clusters_set.clear();
            pw2.print(ind+":"+num+"++++++++++++++++++++++++++++++++++++++++++++++\n");
            pw_log.println("p"+ind+"\t"+"t"+num);
        }
        mt.mm();
        pw.flush();
        pw.close();
        pw2.flush();
        pw.close();
        pw_log.flush();
        pw_log2.flush();
        pw_log.close();
        pw_log2.close();
        System.out.println("output step two finished");
        System.out.println("all finished! cluster num: "+cluster_num);
        mt.dd();


    }
}
