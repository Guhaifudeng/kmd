package src.main.java2;

import fig.basic.Pair;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yishuihan on 17-7-13.
 */
public class LoadData2 {
    private HashMap<String ,Integer> s_i_map = null;
    private HashMap<Integer ,String> i_s_map = null;

    ConcurrentHashMap<Integer,HashMap<Integer,Double>> sparse_mat = null;
    public LoadData2(){
        s_i_map = new HashMap <>();
        i_s_map = new HashMap <>();
        sparse_mat = new ConcurrentHashMap <>();

    }

    public ConcurrentHashMap<Integer,HashMap<Integer,Double>> getSparseMat(){
        return  this.sparse_mat;
    }
    public boolean buildSentTopNMatMap(String topN_file,HashMap<String,Integer> s_i_map,Integer Knn) throws Exception{

        File rf = new File(topN_file);
        if(!rf.isFile()){
            System.err.println(topN_file+" is invalid!");
            System.exit(0);
            return false;
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(rf),"utf-8"));
        String line = "";
        HashMap<Integer ,Double> i_topN = null;
        Integer list0_i = 0;
        int sent_count = 0;
        boolean is_first = true;
        int line_num = 0;
        i_topN = new HashMap <>();
        while((line = br.readLine())!=null){


            String[] str_list = line.split("\t");
            if(str_list.length == 1){
                if(str_list[0].length() ==0) {
                    sent_count++;
                    if (sent_count % 5000 == 0) {
                        System.out.println("dealt sents: " + sent_count);
                    }
                    is_first = true;
                    sparse_mat.put(list0_i, i_topN);

                }else{
                    if(!s_i_map.containsKey(str_list[0]))
                        System.err.println("sentences error");
                    else {
                        list0_i = s_i_map.get(str_list[0]);
                        //System.out.println(str_list[0]);
                        i_topN = new HashMap <>();
                        line_num = 0;
                    }
                }

            }else if(str_list.length == 2) {
                if(is_first){
                    is_first = false;
                    continue;
                }
                if(!s_i_map.containsKey(str_list[0])){
                    System.err.println("sentences error");
                }
                Integer tmp0_i = s_i_map.get(str_list[0]);
                Double  tmp1_i = Double.valueOf(str_list[1]);
                tmp1_i = (tmp1_i+1.0)/2;

                if(i_topN.size()< Knn)
                    i_topN.put(tmp0_i,tmp1_i);
                line_num++;
            }else {
                System.err.println("data format wrong");
            }
        }
        System.out.println("dealt sents: "+ sent_count);
        System.out.println("sent knn mat: "+ sparse_mat.size());
        return true;
    }
    public boolean buildSentencesIndexMap(String file) throws Exception{
        File rf = new File(file);
        if(!rf.isFile()){
            System.err.println(file+" is invalid!");
            System.exit(0);
            return false;
        }
        String line = "";
        int line_count_e = 0;
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(rf),"utf-8"));
//        PriorityQueue<String> priorityQueue = new PriorityQueue <>();
        while((line = br.readLine())!= null){
            if(line.equals("") || line.equals(" "))
            {
                line_count_e++;
                continue;
            }
            String[] str = line.split("\t");
            String line1 = str[0];
            if(!s_i_map.containsKey(line1)&&line1.length()>0 && str.length ==1){
                this.s_i_map.put(line1,this.s_i_map.size());
            }
            else {
//                if(line1.length()>0 && s_i_map.containsKey(line1) && str.length == 1)
//                    priorityQueue.add("\t"+line);
            }
        }
//        while (!priorityQueue.isEmpty()){
//            System.out.println(priorityQueue.poll());
//        }
        System.out.println("empty_line_e: "+ line_count_e);
        System.out.println("set_sent_size :"+ s_i_map.size());
        return true;
    }


    public HashMap<Integer,String> getIndexSentencesMap(){

        for(Map.Entry<String,Integer>s_i :this.s_i_map.entrySet()){
            String sent = s_i.getKey();
            Integer ind = s_i.getValue();
            this.i_s_map.put(ind, sent);
        }
        return this.i_s_map;
    }
    public HashMap<String,Integer> getSentencesIndexMap(){
        return this.s_i_map;
    }
    public  static  void  main(String args[]) throws Exception{
//
//
            String file = "/home/badou/data/kmediod/sents/sent_top500.txt";
            File rf = new File(file);
            if(!rf.isFile()){
                System.err.println(file+" is invalid!");
                System.exit(0);
            }
            String line = "";
            int line_count_e = 0;
            String dest_src = "/home/badou/data/kmediod/sents/seg_log.txt";
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file),"utf-8"));;
//            PrintWriter[] pw = new PrintWriter[25];
//            for(int i =0;i<20;i++){
//                 pw[i] = new PrintWriter(new OutputStreamWriter( new FileOutputStream(dest_src+"log"+i+".txt"),"utf-8"),true);
//            }
            PrintWriter log = new PrintWriter(new OutputStreamWriter(new FileOutputStream(dest_src),"utf-8"),true);
            Scanner s = new Scanner(System.in,"UTF-8");
            PrintStream out = null;
            try {
                out = new PrintStream(System.out, true, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String topN_file = "/home/badou/data/kmediod/sents/sent_top150.txt";

            HashMap<String,ArrayList<Pair<String,Double>>> tmp_map = new HashMap <>();
            ArrayList<Pair<String,Double>> i_topN = null;
            int sent_count = 0;
            i_topN = new ArrayList <>();
            String headId = "";
            while((line = br.readLine())!=null){

                String[] str_list = line.split("\t");
                if(str_list.length == 1){
                    if(str_list[0].length() ==0) {
                        sent_count++;
                        if (sent_count % 5000 == 0) {
                            System.out.println("dealt sents: " + sent_count);
                        }
                        tmp_map.put(headId,i_topN);
                    }else{
                            headId = line;
                            //System.out.println(str_list[0]);
                            i_topN = new ArrayList <>();

                    }


                }else if(str_list.length == 2) {
                    Pair<String,Double> pair = new Pair <>(str_list[0],Double.valueOf(str_list[1]));
                    i_topN.add(pair);

                }else {
                    System.err.println("data format wrong");
                }
            }


            while (true) {
                String[] lines = new String[2000];
                HashSet<String> line_set =  new HashSet <>();
                int o = 0;
                out.print("请输入短语：");
                while (true) {

                    String str = s.next();

                    if (str.length() == 1) {
                        out.println("input finished");
                        break;
                    }
                    log.println(str);
                    lines[o++] = str;
                    line_set.add(str);
                }
                log.println(o+"++++++++");
                int valid_num = 0;
                for (int i = 0; i < o; i++) {
                    String str = lines[i];
                    if (tmp_map.containsKey(str)) {
                        i_topN = tmp_map.get(str);
                        log.print(str + "\n");

                        for (Pair <String, Double> pair : i_topN) {
                            if(line_set.contains(pair.getFirst())&& pair.getSecond()>0.0){
                                log.println(pair.getFirst() + "\t" + pair.getSecond());
                                valid_num++;
                            }


                        }



                    } else {
                        out.println("none ! ! ! !");
                    }
                    log.println("-------------------------------------------------");

                }

                log.println("valid_num_rate : "+1.0*valid_num/(o*o));
                log.flush();
                log.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++");

                out.println("完成！！！");
            }

//        String file = "/home/badou/data/kmediod/sents/g.txt";
//        File rf = new File(file);
//        if(!rf.isFile()){
//            System.err.println(file+" is invalid!");
//            System.exit(0);
//        }
//        String line = "";
//        int line_count_e = 0;
//        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(rf),"utf-8"));
//        String dest_src = "/home/badou/data/kmediod/sents/log_g.txt";
//        PrintWriter log = new PrintWriter(new OutputStreamWriter(new FileOutputStream(dest_src),"utf-8"),true);
//        int line_ind = 0;
//        Comparetor1 comparetor1 = new Comparetor1();
//        PriorityQueue<Aa> priorityQueue = new PriorityQueue <>(comparetor1);
//        Aa tmp = null;
//        while((line = br.readLine())!= null){
//            line_ind++;
//            if(line.equals("") || line.equals(" "))
//            {
//
//                tmp.setA1(line_ind);
//                tmp.setA2(line_count_e-5);
//                priorityQueue.add(tmp);
//                line_count_e =0;
//                continue;
//            }
//            line_count_e++;
//            if(line_count_e==4){
//                tmp = new Aa();
//                tmp.setCl(line);
//            }
//
//        }
//        while (!priorityQueue.isEmpty()){
//            tmp = priorityQueue.poll();
//            log.println("l"+tmp.getA1()+"\t"+tmp.getA2()+"\t"+tmp.getCl());
//        }
//
    }
}
class Aa{
    int a1;
    int a2;
    Aa(){

    }
    Aa(int a1, int a2){
        this.a1 = a1;
        this.a2 = a2;
    }
    public int getA1() {
        return a1;
    }

    public void setA1(int a1) {
        this.a1 = a1;
    }

    public int getA2() {
        return a2;
    }

    public void setA2(int a2) {
        this.a2 = a2;
    }
    String cl ="";

    public String getCl() {
        return cl;
    }

    public void setCl(String cl) {
        this.cl = cl;
    }
}
class Comparetor1 implements Comparator {

    public int compare(Object o1, Object o2) {
        Aa s1=(Aa)o1;
        Aa s2=(Aa)o2;

        int s1_pre = s1.getA2();
        int s2_pre = s2.getA2();//;
//        Double s1_pre = s1.getOpt();
//        Double s2_pre = s2.getOpt();//;
        if(s1_pre<s2_pre)//UP exchange
            return 1;
        else
            return -1;
    }
}