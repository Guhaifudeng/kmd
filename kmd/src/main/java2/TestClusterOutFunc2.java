package src.main.java2;

import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import java.io.*;
import java.util.*;

/**
 * Created by yishuihan on 17-7-17.
 */
public class TestClusterOutFunc2 {
    public static void main(String[] args) throws Exception{
        String dir  ="/home/badou/data/kmediod/chemeleon/360/2/";
        String class_file = dir+"word3387_0.6_30000_class_pair.txt";
        String class_out_file = dir+"word3387_0.6_30000_class_pair_analysis.txt";
        String cluster_file = dir+ "word3387_0.6_30000_cluster.txt";
        String cluster_out_file  = dir+"word3387_0.6_30000_cluster_analysis.txt";

        for (int i = 0; i < args.length; i += 1) {
            String arg = args[i];
            if (arg.startsWith("--")) {
                arg = arg.substring(2);
            } else if (arg.startsWith("-")) {
                arg = arg.substring(1);
            }

            if(arg.equalsIgnoreCase("class")) {
                class_file = args[i + 1];
            }else  if(arg.equalsIgnoreCase("sout")){
                class_out_file = args[i+1];
            }else if(arg.equalsIgnoreCase("cluster")){
                cluster_file = args[i+1];
            }else if(arg.equalsIgnoreCase("tout")){
                class_out_file = args[i+1];
            }
        }

        HashMap<Integer,HashSet<Integer>> class_id_e = null;
        HashMap<String,Integer> class_s_i = null;
        HashMap<Integer,String> class_i_s = null;
        HashMap<Integer,HashSet<Integer>> cluster_id_e = null;
        HashMap<String,Integer> cluster_s_i = null;
        HashMap<Integer,String> cluster_i_s = null;
        HashMap<Integer,Integer> class_e_id = null;
        HashMap<Integer,Integer> cluster_e_id = null;

        TestClusterOutTool2 testClusterOutTool2 = new TestClusterOutTool2();
        testClusterOutTool2.buildClusterWordData(cluster_file);
        testClusterOutTool2.buildClassWordData(class_file);

        class_e_id = testClusterOutTool2.getClass_e_id();
        class_i_s = testClusterOutTool2.getClass_i_s();
        class_s_i = testClusterOutTool2.getClass_s_i();
        class_id_e = testClusterOutTool2.getClass_id_e();

        cluster_e_id = testClusterOutTool2.getCluster_e_id();
        cluster_id_e = testClusterOutTool2.getCluster_id_e();
        cluster_i_s = testClusterOutTool2.getCluster_i_s();
        cluster_s_i = testClusterOutTool2.getCluster_s_i();

        PriorityQueue<PairIdNum> class_pri = testClusterOutTool2.getClass_pri();
        PriorityQueue<PairIdNum> cluster_pri = testClusterOutTool2.getCluster_pri();

        PrintWriter pw_cla = new PrintWriter(new OutputStreamWriter(new FileOutputStream(class_out_file)));
        //seg class
        HashMap<Integer,HashSet<Integer>> out_map = new HashMap <>();
        HashSet<Integer> out_set = new HashSet <>();
        int sub_id;
        while(!class_pri.isEmpty()){
            PairIdNum pairIdNum = class_pri.poll();
            Integer head_id = pairIdNum.getId();
            HashSet<Integer> tmp_class_e = class_id_e.get(head_id);
            for(Integer e:tmp_class_e){
                Integer cluster_id = cluster_e_id.get(e);
                if(out_map.containsKey(cluster_id)){
                    out_set = out_map.get(cluster_id);
                    out_set.add(e);
                }else {
                    out_set = new HashSet <>();
                    out_set.add(e);
                    out_map.put(cluster_id,out_set);
                }
            }

            pw_cla.print("id: "+class_i_s.get(head_id)+"\ts: "+class_id_e.get(head_id).size()+"\t g: "+out_map.size()+"\n");
            for(Map.Entry<Integer,HashSet<Integer>> entry_out : out_map.entrySet()){
                Integer cluster_id = entry_out.getKey();
                HashSet<Integer> cluster_e = entry_out.getValue();
                Iterator<Integer> iterator = cluster_e.iterator();
                sub_id = iterator.next();
                pw_cla.print(cluster_i_s.get(sub_id)+"\t"+cluster_e.size()+"\t"+cluster_id_e.get(cluster_id).size()+"\n");

            }
            pw_cla.print("--------------------------------------------------------------\n");
            out_map.clear();
            out_set.clear();
        }
        pw_cla.flush();
        pw_cla.close();
        System.out.println("class seg finished!");



        PrintWriter pw_clu = new PrintWriter(new OutputStreamWriter(new FileOutputStream(cluster_out_file)));
        //seg cluster
        while(!cluster_pri.isEmpty()){
            PairIdNum pairIdNum = cluster_pri.poll();
            Integer head_id = pairIdNum.getId();
            HashSet<Integer> tmp_cluster_e = cluster_id_e.get(head_id);
            for(Integer e:tmp_cluster_e){
                Integer class_id = class_e_id.get(e);
                if(out_map.containsKey(class_id)){
                    out_set = out_map.get(class_id);
                    out_set.add(e);
                }else {
                    out_set = new HashSet <>();
                    out_set.add(e);
                    out_map.put(class_id,out_set);
                }
            }

            pw_clu.print("id: "+cluster_i_s.get(head_id)+"\ts: "+cluster_id_e.get(head_id).size()+"\t g: "+out_map.size()+"\n");
            for(Map.Entry<Integer,HashSet<Integer>> entry_out : out_map.entrySet()){
                Integer class_id = entry_out.getKey();
                HashSet<Integer> class_e = entry_out.getValue();
                Iterator<Integer> iterator = class_e.iterator();
                sub_id = iterator.next();
                pw_clu.print(cluster_i_s.get(sub_id)+"\t"+class_e.size()+"\t"+class_id_e.get(class_id).size()+"\n");

            }
            pw_clu.print("--------------------------------------------------------------\n");
            out_map.clear();
            out_set.clear();
        }
        pw_clu.flush();
        pw_clu.close();
        System.out.println("cluster seg finished!");
    }
}
class TestClusterOutTool2{
    private HashMap<Integer,HashSet<Integer>> class_id_e = null;
    private HashMap<String,Integer> class_s_i = null;
    private HashMap<Integer,String> class_i_s = null;
    private HashMap<Integer,HashSet<Integer>> cluster_id_e = null;
    private HashMap<String,Integer> cluster_s_i = null;
    private HashMap<Integer,String> cluster_i_s = null;
    private HashMap<Integer,Integer> class_e_id = null;
    private HashMap<Integer,Integer> cluster_e_id = null;
    private PriorityQueue<PairIdNum> class_pri = null;
    private PriorityQueue<PairIdNum> cluster_pri = null;
    public TestClusterOutTool2(){
        this.class_i_s = new HashMap <>();
        this.class_id_e = new HashMap <>();
        this.class_s_i = new HashMap <>();
        this.class_e_id = new HashMap <>();

        this.cluster_i_s = new HashMap <>();
        this.cluster_s_i = new HashMap <>();
        this.cluster_id_e = new HashMap <>();
        this.cluster_e_id = new HashMap <>();
        Comparator3 comparator3 = new Comparator3();
        class_pri = new PriorityQueue <>(comparator3);
        cluster_pri = new PriorityQueue <>(comparator3);
    }
    public void buildClassData(String class_file) throws Exception{
        File rf = new File(class_file);
        if(!rf.isFile()){
            System.err.println(class_file+" is invalid!");
            System.exit(0);
        }
        String line = "";
        int line_count_e = 0;
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(rf),"utf-8"));
        String head_id = "";
        int sent_num = 0;
        HashSet<Integer> tmp_set = null;
        int class_num = 0;
        int line_ind = 0;
        while((line = br.readLine())!= null){
            line_ind++;
            if(line.equals("") || line.equals(" "))
            {
                line_count_e =0;
                continue;
            }
            line_count_e++;
            if(line_count_e>5){
                sent_num++;
                tmp_set.add(cluster_s_i.get(line));
            }
            if(line_count_e==4){

                class_num++;
                head_id = line;
                if(class_s_i.containsKey(head_id))
                    System.out.println(head_id);
                class_s_i.put(head_id,class_s_i.size());

                tmp_set = new HashSet <>();
                class_id_e.put(class_s_i.get(head_id),tmp_set);


                PairIdNum pairIdNum = new PairIdNum(class_s_i.get(head_id),line_ind);
                class_pri.add(pairIdNum);
            }
        }
        for(Map.Entry<String,Integer> entry:class_s_i.entrySet()){
            class_i_s.put(entry.getValue(),entry.getKey());
        }
        for(Map.Entry<Integer,HashSet<Integer>> entry: class_id_e.entrySet()){
            Integer id = entry.getKey();
            HashSet<Integer> tmp_e = entry.getValue();
            for(Integer e:tmp_e){
                class_e_id.put(e,id);
            }
        }
        if(class_e_id.size()!=cluster_s_i.size()||class_i_s.size()!=class_id_e.size()|| class_e_id.size()!= sent_num ||class_i_s.size()!=class_num){

            System.err.println("error 215");
            System.out.println(class_e_id.size());
            System.out.println(sent_num);
            System.out.println(cluster_i_s.size());
            System.out.println(cluster_s_i.size());

            System.out.println("\n"+class_num);
            System.out.println(class_id_e.size());
            System.out.println(class_i_s.size());
            System.out.println(class_s_i.size());
        }
        System.out.println("class sent num "+ sent_num);
        System.out.println("class size " + class_num);


    }

    public void buildClusterData(String cluster_file) throws Exception{
        File rf = new File(cluster_file);
        if(!rf.isFile()){
            System.err.println(cluster_file+" is invalid!");
            System.exit(0);
        }
        String line = "";
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(rf),"utf-8"));

        int sent_num = 0;
        boolean isFirst = true;
        String head_id = "";
        HashSet<Integer> tmp_set = null;
        int cluster_num = 0;
        int line_ind = 0;
        while((line = br.readLine())!= null){
            line_ind++;
            if(line.endsWith("--"))
            {
                cluster_num++;
                tmp_set = new HashSet <>();
                isFirst = true;
                continue;
            }
            if(isFirst){
                head_id = line;
                cluster_s_i.put(head_id,cluster_s_i.size());
                tmp_set.add(cluster_s_i.get(head_id));
                cluster_id_e.put(cluster_s_i.get(head_id),tmp_set);
                isFirst = false;
                PairIdNum pairIdNum = new PairIdNum(cluster_s_i.get(head_id),line_ind);
                cluster_pri.add(pairIdNum);
            }else {

                cluster_s_i.put(line, cluster_s_i.size());
                tmp_set.add(cluster_s_i.get(line));
            }
            sent_num++;
        }

        for(Map.Entry<String,Integer> entry:cluster_s_i.entrySet()){
            cluster_i_s.put(entry.getValue(),entry.getKey());
        }
        for(Map.Entry<Integer,HashSet<Integer>> entry: cluster_id_e.entrySet()){
            Integer id = entry.getKey();
            HashSet<Integer> tmp_e = entry.getValue();
            for(Integer e:tmp_e){
                cluster_e_id.put(e,id);
            }
        }
        if(cluster_e_id.size()!=cluster_s_i.size()||cluster_id_e.size()!=cluster_num|| cluster_e_id.size()!= sent_num){
            System.err.println("error 283");
            System.out.println(cluster_e_id.size());
            System.out.println(sent_num);
            System.out.println(cluster_i_s.size());
            System.out.println(cluster_s_i.size());

            System.out.println("\n"+cluster_num);
            System.out.println(cluster_id_e.size());
        }
        System.out.println("cluster sent num "+ sent_num);
        System.out.println("cluster size " + cluster_num+"\n");

    }

    public void buildClassWordData(String class_f_e_file) throws Exception{
        BufferedReader br_cla = new BufferedReader(new InputStreamReader(new FileInputStream(class_f_e_file),"utf-8"));
        int line_ind= 0;
        String line = "";
        HashSet<Integer> set_e = null;
        Integer id = -1;
        int sent_num = 0;
        int class_num =0;
        boolean flag = true;
        while ((line = br_cla.readLine())!=null){
            line_ind++;
            line = line.trim();
            if(line.equals("")){
                class_id_e.put(id,set_e);
                flag = true;
                continue;
            }
            String[] str = line.split("\t");
            //System.out.println(str.length);
            if(str.length == 1 && flag){
                class_s_i.put(str[0],class_s_i.size());
                id = class_s_i.get(str[0]);
                set_e = new HashSet <>();
                PairIdNum pairIdNum = new PairIdNum(id,line_ind);
                class_pri.add(pairIdNum);
                class_num++;
                flag = false;
            }else {
                for(String e:str){
                    if(!cluster_s_i.containsKey(e))
                        System.err.println("error 323 "+e);
                    sent_num++;
                    Integer tem_e = cluster_s_i.get(e);
                    if(tem_e==null){
                        System.out.println(e);
                    }
                    if(set_e == null){
                        System.out.println(set_e);
                    }
                    set_e.add(tem_e);
                    //System.out.println(class_i_s.get(id));
                }
            }
        }
        for(Map.Entry<String,Integer> entry:class_s_i.entrySet()){
            class_i_s.put(entry.getValue(),entry.getKey());
        }
        for(Map.Entry<Integer,HashSet<Integer>> entry: class_id_e.entrySet()){
            Integer idt = entry.getKey();
            HashSet<Integer> tmp_e = entry.getValue();
            for(Integer e:tmp_e){
                class_e_id.put(e,idt);
            }
        }
        if(class_e_id.size()!=cluster_s_i.size()||class_i_s.size()!=class_id_e.size()|| class_e_id.size()!= sent_num ||class_i_s.size()!=class_num){

            System.err.println("error 342");
            System.out.println(class_e_id.size());
            System.out.println(sent_num);
            System.out.println(cluster_i_s.size());
            System.out.println(cluster_s_i.size());

            System.out.println("\n"+class_num);
            System.out.println(class_id_e.size());
            System.out.println(class_i_s.size());
            System.out.println(class_s_i.size());
        }
        System.out.println("class sent num "+ sent_num);
        System.out.println("class size " + class_num);
    }
    public void buildClusterWordData(String cluster_f_e_file) throws Exception{
        BufferedReader br_clu = new BufferedReader(new InputStreamReader(new FileInputStream(cluster_f_e_file),"utf-8"));
        int line_ind= 0;
        String line = "";
        HashSet<Integer> set_e = null;
        Integer id = -1;
        int sent_num = 0;
        int cluster_num =0;
        boolean flag = true;
        while ((line = br_clu.readLine())!=null){
            line_ind++;
            line = line.trim();
            if(line.equals("")){
                cluster_id_e.put(id,set_e);
                flag = true;
                continue;

            }
            String[] str = line.split("\t");
            if(str.length == 1 && flag){
                cluster_s_i.put(str[0],cluster_s_i.size());
                id = cluster_s_i.get(str[0]);
                set_e = new HashSet <>();
                PairIdNum pairIdNum = new PairIdNum(id,line_ind);
                cluster_pri.add(pairIdNum);
                cluster_num++;
                flag = false;
            }else {
                for(String e:str){
                    if(!cluster_s_i.containsKey(e))
                        cluster_s_i.put(e,cluster_s_i.size());
                    sent_num++;
                    Integer tem_e = cluster_s_i.get(e);
                    set_e.add(tem_e);
                }
            }
        }
        for(Map.Entry<String,Integer> entry:cluster_s_i.entrySet()){
            cluster_i_s.put(entry.getValue(),entry.getKey());
        }
        for(Map.Entry<Integer,HashSet<Integer>> entry: cluster_id_e.entrySet()){
            Integer idt = entry.getKey();
            HashSet<Integer> tmp_e = entry.getValue();
            for(Integer e:tmp_e){
                cluster_e_id.put(e,idt);
            }
        }
        if(cluster_e_id.size()!=cluster_s_i.size()||cluster_id_e.size()!=cluster_num|| cluster_e_id.size()!= sent_num){
            System.err.println("error 400");
            System.out.println("-"+cluster_e_id.size());
            System.out.println("-"+sent_num);
            System.out.println(cluster_i_s.size());
            System.out.println(cluster_s_i.size());

            System.out.println("\n"+cluster_num);
            System.out.println(cluster_id_e.size());
        }
        System.out.println("cluster sent num "+ sent_num);
        System.out.println("cluster size " + cluster_num+"\n");



    }

    public PriorityQueue <PairIdNum> getClass_pri() {
        return class_pri;
    }

    public void setClass_pri(PriorityQueue <PairIdNum> class_pri) {
        this.class_pri = class_pri;
    }

    public PriorityQueue <PairIdNum> getCluster_pri() {
        return cluster_pri;
    }

    public void setCluster_pri(PriorityQueue <PairIdNum> cluster_pri) {
        this.cluster_pri = cluster_pri;
    }

    public HashMap <Integer, Integer> getClass_e_id() {
        return class_e_id;
    }

    public void setClass_e_id(HashMap <Integer, Integer> class_e_id) {
        this.class_e_id = class_e_id;
    }

    public HashMap <Integer, Integer> getCluster_e_id() {
        return cluster_e_id;
    }

    public void setCluster_e_id(HashMap <Integer, Integer> cluster_e_id) {
        this.cluster_e_id = cluster_e_id;
    }

    public HashMap <Integer, HashSet <Integer>> getClass_id_e() {
        return class_id_e;
    }

    public void setClass_id_e(HashMap <Integer, HashSet <Integer>> class_id_e) {
        this.class_id_e = class_id_e;
    }

    public HashMap <String, Integer> getClass_s_i() {
        return class_s_i;
    }

    public void setClass_s_i(HashMap <String, Integer> class_s_i) {
        this.class_s_i = class_s_i;
    }

    public HashMap <Integer, String> getClass_i_s() {
        return class_i_s;
    }

    public void setClass_i_s(HashMap <Integer, String> class_i_s) {
        this.class_i_s = class_i_s;
    }

    public HashMap <Integer, HashSet <Integer>> getCluster_id_e() {
        return cluster_id_e;
    }

    public void setCluster_id_e(HashMap <Integer, HashSet <Integer>> cluster_id_e) {
        this.cluster_id_e = cluster_id_e;
    }

    public HashMap <String, Integer> getCluster_s_i() {
        return cluster_s_i;
    }

    public void setCluster_s_i(HashMap <String, Integer> cluster_s_i) {
        this.cluster_s_i = cluster_s_i;
    }

    public HashMap <Integer, String> getCluster_i_s() {
        return cluster_i_s;
    }

    public void setCluster_i_s(HashMap <Integer, String> cluster_i_s) {
        this.cluster_i_s = cluster_i_s;
    }
}
class PairIdNum{
    int id  =-1;
    int ind = -1;
    public PairIdNum(int id,int ind){
        this.id  = id;
        this.ind = ind;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getInd() {
        return ind;
    }

    public void setInd(int ind) {
        this.ind = ind;
    }
}
class  Comparator3 implements Comparator{
    public Comparator3() {
    }
    public int compare(Object o1, Object o2) {
        PairIdNum s1=(PairIdNum)o1;
        PairIdNum s2=(PairIdNum)o2;

        int s1_pre = s1.getInd();
        int s2_pre = s2.getInd();//;
//        Double s1_pre = s1.getOpt();
//        Double s2_pre = s2.getOpt();//;
        if(s1_pre>s2_pre)//Up exchange
            return 1;
        else
            return -1;
    }
}
class CharWordSentUtil{
    public  static boolean isNumOrEnlishChar(char c){
        if(c>='0' && c<= '9')
            return true;
        if(c>='a' && c<='z')
            return  true;
        if(c>='A' && c<='Z')
            return true;
        return false;
    }
}