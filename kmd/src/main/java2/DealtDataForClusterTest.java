package src.main.java2;

import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yishuihan on 17-7-18.
 */
public class DealtDataForClusterTest {
    public static void main(String[] args) throws Exception {
//        String str1 = "12add河南--";
//        String str2 = "河南abc12b";
//        String str3 = "河南　香港";
//        if(DealtDataForClusterTestTool.isNumOrEnlishChar(str1.charAt(0)) || DealtDataForClusterTestTool.isNumOrEnlishChar(str1.charAt(str1.length()-1)))
//            System.out.println("hello");
//        if(DealtDataForClusterTestTool.isNumOrEnlishChar(str2.charAt(0)) || DealtDataForClusterTestTool.isNumOrEnlishChar(str2.charAt(str2.length()-1)))
//            System.out.println("hello2");
//        if(DealtDataForClusterTestTool.isNumOrEnlishChar(str3.charAt(0)) || DealtDataForClusterTestTool.isNumOrEnlishChar(str3.charAt(str3.length()-1))){
//            System.out.println("hello3"+str3.charAt(0)+":"+str3.charAt(str3.length() -1));
//        }
//        String str4  ="冰住\t冰住\t冰住\t冰住\t";
//        //String[] str = str4.trim().split("\t");
//        String[] str = str4.split("\t");
//        System.out.println(str.length);
//
//        String dir = "/home/badou/data/kmediod/chemeleon/360/";
//        String class_s_f_file = dir + "word0.60_top150_30000_pair.txt";
//        String src = dir + "word0.60_top150_pair.txt";
//        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(src), "utf-8"));
//        PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(class_s_f_file),"utf-8"),true);
//
//        DealtDataForClusterTestTool testTool = new DealtDataForClusterTestTool();
//        //testTool.isNumOrEnlishChar();
//
//        String line = "";
//        String pre_line = "";
//
//        boolean flag = true;
//        HashMap<String,String> hashMap = new HashMap <>();
//        //HashSet<String> hashSet = new HashSet <>();
//        pre_line = br.readLine();
//        pre_line = pre_line.trim();
//        hashMap.put(pre_line,pre_line);
//        Pattern pattern = Pattern.compile(":");
//        while((line = br.readLine())!=null){
//            line = line.trim();
//            if(line.equals(""))
//                continue;
//            Matcher matcher = pattern.matcher(line);
//            if(matcher.find()){
//                String[] str = line.split("\t");
//                for(String e:str){
//                    hashMap.put(e.split(":")[0],pre_line);
////                    if(hashMap.size()>=30000)
////                        break;
//                    //hashSet.add(e.split(":")[0]+"\t"+pre_line);
//                }
////                if(hashMap.size()>=30000)
////                    break;
//            }else {
//                pre_line = line;
//                hashMap.put(line,line);
////                if(hashMap.size()>=30000)
////                    break;
//                //hashSet.add(line+"\t"+pre_line);
//            }
//        }
//        System.out.println(hashMap.size());
//        //System.out.println(hashSet.size());
//        for(Map.Entry<String,String> entry : hashMap.entrySet()){
//            String e = entry.getKey();
//            String f = entry.getValue();
//            pw.print(e+"\t"+f+"\n");
//        }
//        pw.flush();
//        pw.close();

        String dir = "/home/badou/data/kmediod/chemeleon/360/";
        for (int i = 0; i < args.length; i += 1) {
            String arg = args[i];
            if (arg.startsWith("--")) {
                arg = arg.substring(2);
            } else if (arg.startsWith("-")) {
                arg = arg.substring(1);
            }

            if (arg.equalsIgnoreCase("dir")) {
                dir = args[i + 1];
            }
        }
        String class_s_f_file = dir+"word0.60_top150_30000_pair.txt";
        String cluster_f_s_file = dir+"word0.60_top150_mat_1.0:2.0_2_true_0.65_knn_50_minRC_0.4_minRI_0.0_minMetric_0.0_maxOpt_step2.txt";
        String cla_out = dir+"word3387_0.6_30000_class_pair.txt";
        String clu_out = dir+"word3387_0.6_30000_cluster.txt";
        DealtDataForClusterTestTool testTool = new DealtDataForClusterTestTool();
        testTool.buildClassData(class_s_f_file);
        testTool.buildClusterData(cluster_f_s_file);
        HashMap<String,HashSet<String>> class_l_e = testTool.getClass_l_e();
        HashMap<String,HashSet<String>> cluster_l_e = testTool.getCluster_l_e();
        HashSet<String> class_e = testTool.getClass_e();
        HashSet<String> cluster_e = testTool.getCluster_e();
        PriorityQueue<PairStrSize> class_pri = testTool.getClass_pri();
        PriorityQueue<PairStrSize> cluster_pri = testTool.getCluster_pri();
        //out class
        PrintWriter pw_cla = new PrintWriter(new OutputStreamWriter(new FileOutputStream(cla_out),"utf-8"),true);
        PriorityQueue<String> queue = new PriorityQueue <>();
        while(!class_pri.isEmpty()){
            PairStrSize pairStrSize = class_pri.poll();
            String id = pairStrSize.getId();
            HashSet<String> out_e = class_l_e.get(id);
            queue.addAll(out_e);
            if(queue.size()!= out_e.size()){
                System.err.println("62");
            }
            pw_cla.print(id+"\n");
            while(!queue.isEmpty()){
                pw_cla.print(queue.poll()+"\t");
            }
            pw_cla.print("\n\n");

        }
        pw_cla.flush();
        pw_cla.close();
        //out cluster
        PrintWriter pw_clu = new PrintWriter(new OutputStreamWriter(new FileOutputStream(clu_out),"utf-8"),true);
        while(!cluster_pri.isEmpty()){
            PairStrSize pairStrSize = cluster_pri.poll();
            String id = pairStrSize.getId();
            HashSet<String> out_e = cluster_l_e.get(id);

            queue.addAll(out_e);
            if(queue.size() != out_e.size()){
                System.err.println("79");
            }
            pw_clu.print(id+"\n");
            while (!queue.isEmpty()){
                pw_clu.print(queue.poll()+"\t");
            }
            pw_clu.print("\n\n");


        }
        pw_clu.flush();
        pw_clu.close();
        if(testTool.checkClassClusterData(class_e,cluster_e)){
            System.out.println("ok");
        }else {
            System.err.println("wrong");
            //System.out.println();
            for(String e:class_e){
                System.out.println(e);
            }
            System.out.println("-");
            for(String e:cluster_e){
                System.out.println(e);
            }
            System.out.println(class_e.size());
            System.out.println(cluster_e.size());
        }

    }
}
class DealtDataForClusterTestTool{
    public  boolean isNumOrEnlishChar(char c){
        if(c>='0' && c<= '9')
            return true;
        if(c>='a' && c<='z')
            return  true;
        if(c>='A' && c<='Z')
            return true;
        return false;
    }

    //HashMap<String,Integer> class_s_i = null;
    //HashMap<Integer,String> class_i_s = null;
    private HashMap<String,HashSet<String>> class_l_e = null;
    private HashMap<String,HashSet<String>> cluster_l_e = null;
    private HashSet<String> class_e = null;
    private HashSet<String> cluster_e = null;
    private PriorityQueue<PairStrSize> class_pri = null;
    private PriorityQueue<PairStrSize> cluster_pri = null;
    public DealtDataForClusterTestTool(){
        class_l_e = new HashMap <>();
        cluster_l_e = new HashMap <>();
        class_e = new HashSet <>();
        cluster_e = new HashSet <>();
        Comparator4 comparator4 = new Comparator4();
        cluster_pri = new PriorityQueue <>(comparator4);
        class_pri = new PriorityQueue <>(comparator4);
    }
    public void buildClassData(String class_s_f_file) throws Exception{
        BufferedReader br_cla = new BufferedReader(new InputStreamReader(new FileInputStream(class_s_f_file),"utf-8"));
        int line_ind= 0;
        String line = "";
        HashSet<String> set_e = null;
        while((line = br_cla.readLine())!=null){
            line_ind++;
            String[] str = line.split("\t");
            class_e.add(str[0]);
            if(class_l_e.containsKey(str[1])){
                set_e = class_l_e.get(str[1]);
                set_e.add(str[0]);
            }else {
                set_e = new HashSet <>();
                set_e.add(str[0]);
                class_l_e.put(str[1],set_e);
            }
        }
        for(Map.Entry<String,HashSet<String>> entry: class_l_e.entrySet()){
            String id = entry.getKey();
            Integer size = entry.getValue().size();
            PairStrSize pairStrSize = new PairStrSize(id,size);
            class_pri.add(pairStrSize);
        }

    }

    public void buildClusterData(String cluster_l_e_file) throws Exception{
        BufferedReader br_clu = new BufferedReader(new InputStreamReader(new FileInputStream(cluster_l_e_file),"utf-8"));
        String line = "";
        HashSet<String> set_e = null;
        int line_ind_e = 0;
        boolean flag = false;
        while((line = br_clu.readLine())!=null){
            line = line.trim();
//            if(line.equals("") || line.equals(" ")){
//                continue;
//            }

            if(line.endsWith("---------")){
                line_ind_e = 0;
                flag = true;
                continue;
            }
            if(flag)
                line_ind_e++;
            if(line_ind_e == 7){
                String[] str = line.split("\t");
                String id = str[0];
                set_e = new HashSet <>();
                for(int i=0;i<str.length;i++){
                    set_e.add(str[i]);
                }
                cluster_l_e.put(id,set_e);
                cluster_e.addAll(set_e);
                PairStrSize pairStrSize = new PairStrSize(id,str.length);
                cluster_pri.add(pairStrSize);
                flag = false;
                line_ind_e++;
            }
        }


    }
    public boolean checkClassClusterData(HashSet<String> set_class,HashSet<String> set_cluster ){
        HashSet<String> tmp_class = new HashSet<>();
        tmp_class.addAll(set_class);
        HashSet<String> tmp_cluster = new HashSet <>();
        tmp_cluster.addAll(set_cluster);
        set_class.removeAll(tmp_cluster);
        set_cluster.removeAll(tmp_class);
        if(set_class.isEmpty()&& set_cluster.isEmpty()){
            return true;
        }else {
            return false;
        }


    }
    public HashMap <String, HashSet <String>> getClass_l_e() {
        return class_l_e;
    }

    public void setClass_l_e(HashMap <String, HashSet <String>> class_l_e) {
        this.class_l_e = class_l_e;
    }

    public HashMap <String, HashSet <String>> getCluster_l_e() {
        return cluster_l_e;
    }

    public void setCluster_l_e(HashMap <String, HashSet <String>> cluster_l_e) {
        this.cluster_l_e = cluster_l_e;
    }

    public HashSet <String> getClass_e() {
        return class_e;
    }

    public void setClass_e(HashSet <String> class_e) {
        this.class_e = class_e;
    }

    public HashSet <String> getCluster_e() {
        return cluster_e;
    }

    public void setCluster_e(HashSet <String> cluster_e) {
        this.cluster_e = cluster_e;
    }

    public PriorityQueue <PairStrSize> getClass_pri() {
        return class_pri;
    }

    public void setClass_pri(PriorityQueue <PairStrSize> class_pri) {
        this.class_pri = class_pri;
    }

    public PriorityQueue <PairStrSize> getCluster_pri() {
        return cluster_pri;
    }

    public void setCluster_pri(PriorityQueue <PairStrSize> cluster_pri) {
        this.cluster_pri = cluster_pri;
    }
    //public void buildClusterData(String )
}
class PairStrSize{
    String id = "";
    int size = 0;
    public PairStrSize(String id,int size){
        this.id = id;
        this.size = size;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}

class  Comparator4 implements Comparator {
    public Comparator4() {
    }
    public int compare(Object o1, Object o2) {
        PairStrSize s1=(PairStrSize)o1;
        PairStrSize s2=(PairStrSize)o2;

        int s1_pre = s1.getSize();
        int s2_pre = s2.getSize();//;
//        Double s1_pre = s1.getOpt();
//        Double s2_pre = s2.getOpt();//;
        if(s1_pre<s2_pre)//Down exchange
            return 1;
        else
            return -1;
    }
}