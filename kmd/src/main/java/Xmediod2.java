import fig.basic.Pair;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yishuihan on 17-6-29.
 */
public class Xmediod2 {

    public Xmediod2(){

    }
    public static void main(String[] args) throws Exception {
        String set_file = "";
        String topN_file = "";
        String out = "";
        String label_file = "";
        for (int i = 0; i < args.length; i += 1) {
            String arg = args[i];
            if (arg.startsWith("--")) {
                arg = arg.substring(2);
            } else if (arg.startsWith("-")) {
                arg = arg.substring(1);
            }

            if (arg.equalsIgnoreCase("mat")) {
                topN_file = args[i + 1];
            } else if (arg.equalsIgnoreCase("set")) {
                set_file = args[i + 1];

            } else if (arg.equalsIgnoreCase("out")) {
                out = args[i + 1];
            } else if (arg.equalsIgnoreCase("label")) {
                label_file = args[i + 1];

            }
        }
        MyTimeClock mt = new MyTimeClock("0011");
        mt.tt();

        LoadMatric loadMatric = new LoadMatric();
        loadMatric.buildSentencesIndexMap(set_file);
        HashMap<String, Integer> s_i_map = loadMatric.getSentencesIndexMap();
        HashMap <Integer, String> i_s_map = loadMatric.getIndexSentencesMap();
        System.out.println("load sentences map finised");
        mt.mm();



        loadMatric.buildSentTopNMatMap(topN_file, s_i_map);
        ConcurrentHashMap<Integer, HashMap <Integer, Double>> sparse_mat = loadMatric.getSparseMat();
        System.out.println("load sentences similarity matrix finised");


        loadMatric.buildSentencesLabelMap(label_file);
        HashMap<String,String> s_f_map = loadMatric.getSentencesLabelMap();
        System.out.println("load labels map finished");
        mt.mm();
        Set<Integer> set_s = i_s_map.keySet();
        UtilXmediod2 utilXmediod2 = new UtilXmediod2(sparse_mat, set_s);
        Set<Integer> centroids = utilXmediod2.getInitCentroids(300, set_s);
        System.out.println(centroids.size());
//        for(Integer c :centroids)
//            System.out.println(c+" "+ i_s_map.get(c));
        System.out.println("init centroids finished");
        mt.mm();

        utilXmediod2.setMinSim(0.3);
        PrintWriter pw1 = new PrintWriter(new OutputStreamWriter(new FileOutputStream(out), "utf-8"), true);
        HashMap <Integer, ArrayList<Integer>> clusters = new HashMap <>();
        Set<Integer> new_centroids = new HashSet<>();
        Set<Integer> add_centroids = new HashSet <>();
        ArrayList <Integer> tmp_list = null;
        int iter = 0;
        int iter_count = 15;
        int end_count = 0;
        while (true) {

            clusters.clear();
            System.out.println("**"+centroids.size());
            for (Integer n : set_s) {
                int c = utilXmediod2.getClosedCenter(n, centroids);
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
                Integer new_c = utilXmediod2.updateCenter(cluster_k_set, c);
                new_centroids.add(new_c);
            }
            System.out.println("+"+new_centroids.size());

            clusters.clear();
            for (Integer n : set_s) {
                int c = utilXmediod2.getClosedCenter(n, new_centroids);
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
                Set <Integer> new_c = utilXmediod2.AddCenter(cluster_k_set, c);
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
            if(new_centroids.size() ==0){
                end_count++;
            }
            if(end_count==5 && iter>=iter_count)
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
        //for(Map.Entry<Integer,Arra>)
        for(Map.Entry<Integer,ArrayList<Integer>> entry : clusters.entrySet()){
            Integer c = entry.getKey();
            ArrayList<Integer> list1 = entry.getValue();
            if(c != -1){
                pw1.print(i_s_map.get(c));
                pw1.println("-------------------------------");
            }
            else{
                System.out.println("---------------"+list1.size()+"----------------");
                pw1.print(String.valueOf(-1));
                pw1.println("-------------------------------");
            }
            ArrayList<Pair<Integer,Double>> list2 = new ArrayList <>();
            for(Integer i: list1){
                double sim = utilXmediod2.getPairCenterPointSim(c,i);
                if(sim +1 <1e-6)
                    sim = -sim;
                Pair<Integer,Double> pair = new Pair <>(i,sim);
                utilXmediod2.addSortedClusterList(list2,pair);

            }

            for(Pair<Integer,Double> pair:list2){
                pw1.print(s_f_map.get(i_s_map.get(pair.getFirst()))+"\t");
            }
            pw1.print("\n");
            for(Pair<Integer,Double> pair: list2){
                pw1.print(i_s_map.get(pair.getFirst())+"\t");
                //pw1.printf("%s : %.4f \t",i_s_map.get(pair.getFirst()),pair.getSecond());

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
