package src.main.java2;

import fig.basic.Pair;

import java.io.*;
import java.util.*;

/**
 * Created by yishuihan on 17-7-17.
 */
public class TestClusterOutFunc {
    public static void main(String args[]) throws Exception{
        String topN_file = "/home/badou/data/kmediod/sents/sent_top500.txt";
        String set_sent_topN_file =  "/home/badou/data/kmediod/sents/sent_top500_set.txt";
        LoadData2 loadData2 = new LoadData2();
        loadData2.buildSentencesIndexMap(topN_file);
        HashMap<String, Integer> s_i_map = loadData2.getSentencesIndexMap();
        HashMap <Integer, String> i_s_map = loadData2.getIndexSentencesMap();
        System.out.println("load sentences map finised");
        PrintWriter pw_set = new PrintWriter(new OutputStreamWriter(new FileOutputStream(set_sent_topN_file),"utf-8"),true);
        for(Map.Entry<Integer,String> tt:i_s_map.entrySet()){
            String str = tt.getValue();
            pw_set.println(str);
        }
        pw_set.flush();
        pw_set.close();
        System.out.println("output senteces set finished! " + i_s_map.size());

        String top500_file =  "/home/badou/data/kmediod/sents/sent_top500_1.0:3.0_2_true_0.5_knn_300_minRC_0.7_minRI_1.0_minMetric_0.0_maxOpt_step2.txt";
        String top500_right_file =  "/home/badou/data/kmediod/sents/sent_top500_1.0:3.0_2_true_0.5_knn_300_minRC_0.7_minRI_1.0_minMetric_0.0_maxOpt_step2_right.txt";
        PrintWriter pw_set2 = new PrintWriter(new OutputStreamWriter(new FileOutputStream(top500_right_file),"utf-8"),true);

        TestClusterFuncTool testClusterFuncTool = new TestClusterFuncTool();
        testClusterFuncTool.setSet_topN(s_i_map.keySet(),pw_set2);
        testClusterFuncTool.bulidClusterSentset(top500_file);
//
////        HashSet<String> set_sent_class = testClusterFuncTool.getSent_set();
////        for(String s1:set_sent_class){
////            pw_set2.println(s1);
////
////        }
//        pw_set2.flush();
//        pw_set2.close();
//        System.out.println("output class sentences set finised! "+ set_sent_class.size());

//        for(String s1:s_i_map.keySet()){
//            if(!set_sent_class.contains(s1)){
//                System.out.println(s1);
//            }
//        }
//        System.out.println("+++");
//        for(String s1:set_sent_class){
//            if(!s_i_map.containsKey(s1)){
//                System.out.println(s1);
//            }
//        }

    }
}
class TestClusterFuncTool{

    HashSet<String> sent_set = null;
    public TestClusterFuncTool(){
        sent_set = new HashSet <>();
    }
    PrintWriter log = null;
    HashSet<String> set_topN = null;
    public void setSet_topN(Set<String> set_topN,PrintWriter pw){
        this.log = pw;
        this.set_topN = new HashSet <String>(set_topN);

    }
    public void  bulidClusterSentset(String cluster_file) throws Exception{
              File rf = new File(cluster_file);
        if(!rf.isFile()){
            System.err.println(cluster_file+" is invalid!");
            System.exit(0);
        }
        String line = "";
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(rf),"utf-8"));

        int sent_num = 0;

        while((line = br.readLine())!= null){

            if(line.endsWith("---------"))
            {
                log.println(line.substring(0,4)+"-------------");

                continue;
            }
            if(line.endsWith("+++")){
                continue;
            }
            log.println(line);
            if(!set_topN.contains(line)){
                System.out.println("error");
            }
            sent_num++;


        }

        System.out.println("class sent num "+ sent_num);

    }
//    public void buildClassSentSet(String class_file) throws Exception{
//
//
//        File rf = new File(class_file);
//        if(!rf.isFile()){
//            System.err.println(class_file+" is invalid!");
//            System.exit(0);
//        }
//        String line = "";
//        int line_count_e = 0;
//        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(rf),"utf-8"));
//        int line_ind = 0;
//
//
//        String head_id = "";
//        int line_line = 0;
//        int sent_num = 0;
//        PriorityQueue<String> priorityQueue = new PriorityQueue <>();
//        while((line = br.readLine())!= null){
//            line_line++;
//            if(line.equals("") || line.equals(" "))
//            {
//
//
//                line_count_e =0;
//                //class_map.put(head_id,class_set);
//                log.println(line);
//                continue;
//            }
//            line_count_e++;
//            if(line_count_e>5){
//                sent_num++;
//                if(!sent_set.contains(line)&&set_topN.contains(line)) {
//                    sent_set.add(line);
//                    log.println(line);
//                }
//
//            }else {
//                log.println(line);
//            }
//            if(line_count_e==4){
//                line_ind++;
//                head_id = line;
//
//            }
//        }
////        while (!priorityQueue.isEmpty()){
////            System.out.println(priorityQueue.poll());
////        }
//        System.out.println("class num: "+ line_ind);
//        System.out.println("class sent num "+ sent_num);
//
//    }

    public HashSet <String> getSent_set() {
        return sent_set;
    }

    public void setSent_set(HashSet <String> sent_set) {
        this.sent_set = sent_set;
    }
}