import fig.basic.Pair;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yishuihan on 17-6-28.
 */
public class Xmediod {
    public Xmediod(){

    }
    public static void main(String[] args) throws Exception {
        MyTimeClock mt = new MyTimeClock("1100");
        mt.tt();
        String file = "/home/badou/data/kmediod/word_ten2000.txt";
        LoadMatric loadMatric = new LoadMatric();
        loadMatric.buildSentencesIndexMap(file);
        HashMap<String, Integer> s_i_map = loadMatric.getSentencesIndexMap();
        HashMap <Integer, String> i_s_map = loadMatric.getIndexSentencesMap();
        System.out.println("load sentences map finised");
        mt.mm();


        String topN_file = "/home/badou/data/kmediod/word_ten2000_top200_cosin.txt";
        loadMatric.buildSentTopNMatMap(topN_file, s_i_map);
        ConcurrentHashMap<Integer, HashMap <Integer, Double>> sparse_mat = loadMatric.getSparseMat();
        System.out.println("load sentences similarity matrix finised");
        mt.mm();
        Set<Integer> set_s = i_s_map.keySet();
        UtilXmediod utilXmediod = new UtilXmediod(sparse_mat, set_s);
        Set<Integer> centroids = utilXmediod.getInitCentroids(10, set_s);
        System.out.println(centroids.size());
//        for(Integer c :centroids)
//            System.out.println(c+" "+ i_s_map.get(c));
        System.out.println("init centroids finished");
        mt.mm();
        String log1 = "/home/badou/data/kmediod/word_ten2000_x1_log.txt";
        PrintWriter pw1 = new PrintWriter(new OutputStreamWriter(new FileOutputStream(log1), "utf-8"), true);
        HashMap <Integer, ArrayList <Integer>> clusters = new HashMap <>();
        Set<Integer> new_centroids = new HashSet<>();
        Set<Integer> add_centroids = new HashSet <>();
        ArrayList <Integer> tmp_list = null;
        int iter = 0;
        int iter_count = 15;
        while (true) {

            clusters.clear();
            System.out.println("**"+centroids.size());
            for (Integer n : set_s) {
                int c = utilXmediod.getClosedCenter(n, centroids);
                if (centroids.contains(c)) {
                    if (clusters.containsKey(c)) {
                        tmp_list = clusters.get(c);
                        tmp_list.add(n);
                    } else {
                        tmp_list = new ArrayList <>();
                        tmp_list.add(n);
                        clusters.put(c, tmp_list);
                    }
                } else {
                    if (clusters.containsKey(-1)) {
                        tmp_list = clusters.get(-1);
                        tmp_list.add(n);
                    } else {
                        tmp_list = new ArrayList <>();
                        tmp_list.add(n);
                        clusters.put(-1, tmp_list);
                    }
                }

                //            Double sim = utilKmediod.getPairCenterPointSim(c,n);
                //            if(centroids.contains(c)){
                //                pw1.println("<"+i_s_map.get(n)+","+i_s_map.get(c)+">--"+sim);
                //            }
                //            else {
                //                pw1.println("<"+i_s_map.get(n)+">"+sim);
                //            }


            }


            System.out.println("-"+clusters.size());


            //先更新
            new_centroids.clear();
            for (Map.Entry <Integer, ArrayList <Integer>> entry : clusters.entrySet()) {
                Integer c = entry.getKey();
                ArrayList <Integer> cluster_k = entry.getValue();
                Set <Integer> cluster_k_set = new HashSet <>(cluster_k);
                Integer new_c = utilXmediod.updateCenter(cluster_k_set, c);
                new_centroids.add(new_c);
            }
            System.out.println("+"+new_centroids.size());

            clusters.clear();
            for (Integer n : set_s) {
                int c = utilXmediod.getClosedCenter(n, new_centroids);
                if (new_centroids.contains(c)) {
                    if (clusters.containsKey(c)) {
                        tmp_list = clusters.get(c);
                        tmp_list.add(n);
                    } else {
                        tmp_list = new ArrayList <>();
                        tmp_list.add(n);
                        clusters.put(c, tmp_list);
                    }
                } else {
                    if (clusters.containsKey(-1)) {
                        tmp_list = clusters.get(-1);
                        tmp_list.add(n);
                    } else {
                        tmp_list = new ArrayList <>();
                        tmp_list.add(n);
                        clusters.put(-1, tmp_list);
                    }
                }
            }
            System.out.println("-"+clusters.size());

            //判定是否要+1
            add_centroids.clear();
            for (Map.Entry <Integer, ArrayList <Integer>> entry : clusters.entrySet()) {
                Integer c = entry.getKey();
                ArrayList <Integer> cluster_k = entry.getValue();
                Set <Integer> cluster_k_set = new HashSet <>(cluster_k);
                Set <Integer> new_c = utilXmediod.AddCenter(cluster_k_set, c);
                add_centroids.addAll(new_c);

            }
            for(Integer tmp_c :add_centroids){
                if(tmp_c.equals(null)){
                    System.err.println("null");
                }
            }
            System.out.println("++"+add_centroids.size());
            //        for(Integer c:new_centroids){
            //            System.out.println(c+" "+ i_s_map.get(c));
            //        }
            new_centroids.clear();
            new_centroids.addAll(add_centroids);
            for(Integer c:centroids){
                new_centroids.remove(c);
            }
            iter++;
            if(new_centroids.size()==0 && iter>=iter_count)
                break;
            centroids.clear();
            centroids.addAll(add_centroids);
            System.out.println("*"+centroids.size());

            System.out.println("finish iter: "+iter);
            mt.mm();
        }
        System.out.println("converge finished!");
        System.out.println(iter);
        mt.mm();
        for(Map.Entry<Integer,ArrayList<Integer>> entry : clusters.entrySet()){
            Integer c = entry.getKey();
            ArrayList<Integer> list1 = entry.getValue();
            if(c != -1)
                pw1.print(i_s_map.get(c)+"\n");
            else
                pw1.print(String.valueOf(-1)+"\n");
            ArrayList<Pair<Integer,Double>> list2 = new ArrayList <>();
            for(Integer i: list1){
                double sim = utilXmediod.getPairCenterPointSim(c,i);
                if(sim +1 <1e-6)
                    sim = -sim;
                Pair<Integer,Double> pair = new Pair <>(i,sim);
                utilXmediod.addSortedClusterList(list2,pair);

            }
            for(Pair<Integer,Double> pair: list2){
                pw1.printf("%s : %.4f \t",i_s_map.get(pair.getFirst()),pair.getSecond());
            }
            pw1.print("\n");
        }
        System.out.println("output finished");
        mt.dd();
        System.out.println(iter);
        pw1.flush();
        pw1.close();
        System.out.println("finished");
    }




}
