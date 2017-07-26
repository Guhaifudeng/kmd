package src.main.java2;

import fig.basic.Pair;

import java.io.*;
import java.util.*;

/**
 * Created by yishuihan on 17-7-17.
 */
public class TestSimFunc {
    public static void main(String[] args) throws Exception{
        String topN_file = "";
        String class_file = "";
        String out_file = "";

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
                out_file = args[i+1];
            }else if(arg.equalsIgnoreCase("class")){
                class_file = args[i+1];
            }
        }
        MyTimeClock mt = new MyTimeClock("---11---");
        mt.tt();
        TestSimFuncTool testSimFuncTool = new TestSimFuncTool();

        //HashMap<String,ArrayList<Pair<String,Double>>> tmp_map = testSimFuncTool.getTmp_map();
        testSimFuncTool.buildMatSimMapWithSet(topN_file);
        testSimFuncTool.buildClassMap(class_file);
        HashMap<String,HashSet<String>> class_map = testSimFuncTool.getClass_map();
        HashMap<String,HashMap<String,Double>> mat_map = testSimFuncTool.getMat_map();

        //output
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(out_file),"utf-8"),true);
//        Set<String> class_keys = class_map.keySet();
        int ind =0;
        Comparetor2 comparetor2 = new Comparetor2();
        PriorityQueue<PairSimOutput> pairSimOutputs = new PriorityQueue <>(comparetor2);

        for(Map.Entry<String,HashSet<String>> entry1: class_map.entrySet()){
            String self_id = entry1.getKey();
            HashSet<String> self_set = entry1.getValue();
            Integer self_num = self_set.size();
            Double self_link_rate = 0.0;
            Double self_ave_sim = -1.0;
            String maxSim_id = "";
            Integer maxSim_num = 0;
            Double maxSim_link_rate = 0.0;
            Double maxSim_ave_sim = -1.0;
            String maxLink_id = "";
            Integer maxLink_num = 0;
            Double maxLink_link_rate = 0.0;
            Double maxLink_ave_sim = -1.0;
            for(Map.Entry<String,HashSet<String>> entry2: class_map.entrySet()){
                String head_id = entry2.getKey();
                HashSet<String> other_set = entry2.getValue();
                if(head_id.equals(self_id)){//self
                    self_ave_sim = testSimFuncTool.CalAveSim(self_set,other_set,mat_map);
                    self_link_rate = testSimFuncTool.CalLinkRate(self_set,other_set,mat_map);
                    self_num = other_set.size();
                    self_id = head_id;
                }else{//other
                    Double tmp_link_ave_rate = testSimFuncTool.CalLinkRate(self_set,other_set,mat_map);
                    Double tmp_ave_sim = testSimFuncTool.CalAveSim(self_set,other_set,mat_map);
                    if(maxSim_ave_sim < tmp_ave_sim){
                        maxSim_ave_sim = tmp_ave_sim;
                        maxSim_id = head_id;
                        maxSim_num = other_set.size();
                        maxSim_link_rate = tmp_link_ave_rate;
                    }
                    if(maxLink_link_rate < tmp_link_ave_rate){
                        maxLink_ave_sim = tmp_ave_sim;
                        maxLink_id = head_id;
                        maxLink_num = other_set.size();
                        maxLink_link_rate = tmp_link_ave_rate;
                    }
                }
            }
            PairSimOutput pairSimOutput = new PairSimOutput(self_id,self_num,self_link_rate,self_ave_sim);
            pairSimOutput.setMaxLink_id(maxLink_id);
            pairSimOutput.setMaxLink_ave_sim(maxLink_ave_sim);
            pairSimOutput.setMaxLink_link_rate(maxLink_link_rate);
            pairSimOutput.setMaxLink_num(maxLink_num);

            pairSimOutput.setMaxSim_ave_sim(maxSim_ave_sim);
            pairSimOutput.setMaxSim_link_rate(maxSim_link_rate);
            pairSimOutput.setMaxSim_num(maxSim_num);
            pairSimOutput.setMaxSim_id(maxSim_id);

            pairSimOutputs.add(pairSimOutput);

            System.out.println(ind++);
//            if(ind >10){
//                break;
//            }

        }
        while (!pairSimOutputs.isEmpty()){
            PairSimOutput pairSimOutput = pairSimOutputs.poll();
            String s ="";
            boolean bool_1 = true;
            boolean bool_0 = true;
            boolean bool_2 = true;
            if(pairSimOutput.getSelf_ave_sim() <0.3){
                bool_0 = false;
            }
            if(pairSimOutput.getMaxSim_ave_sim() > pairSimOutput.getSelf_ave_sim()){
                bool_2 = false;
            }
            if(pairSimOutput.getMaxLink_link_rate() > pairSimOutput.getSelf_link_rate()){
                bool_1 = false;
            }
            s += String.format("self:%-15s\t%d\t%.4f\t%.4f\t%s\n",pairSimOutput.getSelf_id(),pairSimOutput.getSelf_num(),
                    pairSimOutput.getSelf_link_rate(),pairSimOutput.getSelf_ave_sim(),bool_0);
            s += String.format("link:%-15s\t%d\t%.4f\t%.4f\t%s\n",pairSimOutput.getMaxLink_id(),pairSimOutput.getMaxLink_num(),
                    pairSimOutput.getMaxLink_link_rate(),pairSimOutput.getMaxLink_ave_sim(),bool_1);
            s += String.format("sim-:%-15s\t%d\t%.4f\t%.4f\t%s\n",pairSimOutput.getMaxSim_id(),pairSimOutput.getMaxSim_num(),
                    pairSimOutput.getMaxSim_link_rate(),pairSimOutput.getMaxSim_ave_sim(),bool_2);
            s += "\n";
            pw.print(s);
        }


    }


}
class TestSimFuncTool{


    HashMap<String,ArrayList<Pair<String,Double>>> tmp_map = null;
    HashMap<String,HashSet<String>> class_map = null;
    HashMap<String,HashMap<String,Double>> mat_map = null;

    public TestSimFuncTool(){
        tmp_map = new HashMap <>();
        class_map = new HashMap <>();
        mat_map = new HashMap <>();
    }
    public TestSimFuncTool(Boolean set_flag){
        mat_map = new HashMap <>();
        class_map = new HashMap <>();
    }
    public double CalLinkRate(HashSet<String> a,HashSet<String> b, HashMap<String,HashMap<String,Double>> mat_map){
        HashMap<String,HashMap<String,Double>> mat_map_tmp = mat_map;
        HashSet<String> a_tmp = a;
        HashSet<String> b_tmp = b;
        double link_rate = 0;
        for(String a_e:a_tmp){
            int tmp_num =0;
            for(String b_e:b_tmp){
                if(mat_map_tmp.containsKey(a_e))
                    if(mat_map_tmp.get(a_e).containsKey(b_e)){
                        if(mat_map_tmp.get(a_e).get(b_e)>0.0)
                            tmp_num++;
                    }
            }
            link_rate += 1.0*tmp_num/b_tmp.size();
        }
        link_rate = link_rate/a_tmp.size();
        return link_rate;
    }
    public double CalAveSim(HashSet<String> a,HashSet<String> b, HashMap<String,HashMap<String,Double>> mat_map){
        HashMap<String,HashMap<String,Double>> mat_map_tmp = mat_map;
        HashSet<String> a_tmp = a;
        HashSet<String> b_tmp = b;
        int tmp_num =0;
        double sum_sim = 0;
        for(String a_e:a_tmp){

            for(String b_e:b_tmp){
                if(mat_map.containsKey(a_e))
                    if(mat_map.get(a_e).containsKey(b_e)){
                        tmp_num++;
                        sum_sim += mat_map.get(a_e).get(b_e);
                    }
            }

        }
        return  sum_sim/tmp_num;
    }

    public HashMap <String, HashMap <String, Double>> getMat_map() {
        return mat_map;
    }

    public void setMat_map(HashMap <String, HashMap <String, Double>> mat_map) {
        this.mat_map = mat_map;
    }

    public void buildClassMap(String class_file) throws Exception{


        File rf = new File(class_file);
        if(!rf.isFile()){
            System.err.println(class_file+" is invalid!");
            System.exit(0);
        }
        String line = "";
        int line_count_e = 0;
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(rf),"utf-8"));
        int line_ind = 0;

        HashSet<String> class_set = new HashSet <>();
        String head_id = "";

        while((line = br.readLine())!= null){
            line_ind++;
            if(line.equals("") || line.equals(" "))
            {


                line_count_e =0;
                class_map.put(head_id,class_set);
                class_set = new HashSet <>();
                continue;
            }
            line_count_e++;
            if(line_count_e>5){
                class_set.add(line);
            }
            if(line_count_e==4){
                head_id = line;
            }
        }
    }
    public void buildMatSimMap(String topN_file) throws Exception{
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(topN_file),"utf-8"));;
        ArrayList<Pair<String,Double>> i_topN = null;
        int sent_count = 0;
        i_topN = new ArrayList <>();
        String headId = "";
        String line = "";
        while((line = br.readLine())!=null){

            String[] str_list = line.split("\t");
            if(str_list.length == 1){
                if(str_list[0].length() ==0) {
                    sent_count++;
                    if (sent_count % 500 == 0) {
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
    }
    public void buildMatSimMapWithSet(String topN_file) throws Exception{
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(topN_file),"utf-8"));;
        HashMap<String,Double> i_topN = null;
        int sent_count = 0;
        i_topN = new HashMap <>();
        String headId = "";
        String line = "";
        while((line = br.readLine())!=null){

            String[] str_list = line.split("\t");
            if(str_list.length == 1){
                if(str_list[0].length() ==0) {
                    sent_count++;
                    if (sent_count % 500 == 0) {
                        System.out.println("dealt sents: " + sent_count);
                    }
                    mat_map.put(headId,i_topN);
                }else{
                    headId = line;
                    //System.out.println(str_list[0]);
                    i_topN = new HashMap <>();

                }


            }else if(str_list.length == 2) {
                Pair<String,Double> pair = new Pair <>(str_list[0],Double.valueOf(str_list[1]));
                i_topN.put(pair.getFirst(),pair.getSecond());

            }else {
                System.err.println("data format wrong");
            }


        }
    }

    public HashMap <String, ArrayList <Pair <String, Double>>> getTmp_map() {
        return tmp_map;
    }

    public void setTmp_map(HashMap <String, ArrayList <Pair <String, Double>>> tmp_map) {
        this.tmp_map = tmp_map;
    }

    public HashMap <String, HashSet <String>> getClass_map() {
        return class_map;
    }

    public void setClass_map(HashMap <String, HashSet <String>> class_map) {
        this.class_map = class_map;
    }
}
class PairSimOutput{

    String self_id = "";
    Integer self_num = 0;
    Double self_link_rate = 0.0;
    Double self_ave_sim = -1.0;
    String maxSim_id = "";
    Integer maxSim_num = 0;
    Double maxSim_link_rate = 0.0;
    Double maxSim_ave_sim = -1.0;
    String maxLink_id = "";
    Integer maxLink_num = 0;
    Double maxLink_link_rate = 0.0;
    Double maxLink_ave_sim = -1.0;
    public PairSimOutput(String self_id,Integer self_num,Double self_link_rate,Double self_ave_sim){
        this.self_ave_sim = self_ave_sim;
        this.self_id = self_id;
        this.self_link_rate = self_link_rate;
        this.self_num = self_num;
    }

    public String getSelf_id() {
        return self_id;
    }

    public void setSelf_id(String self_id) {
        this.self_id = self_id;
    }

    public Integer getSelf_num() {
        return self_num;
    }

    public void setSelf_num(Integer self_num) {
        this.self_num = self_num;
    }

    public Double getSelf_link_rate() {
        return self_link_rate;
    }

    public void setSelf_link_rate(Double self_link_rate) {
        this.self_link_rate = self_link_rate;
    }

    public Double getSelf_ave_sim() {
        return self_ave_sim;
    }

    public void setSelf_ave_sim(Double self_ave_sim) {
        this.self_ave_sim = self_ave_sim;
    }

    public String getMaxSim_id() {
        return maxSim_id;
    }

    public void setMaxSim_id(String maxSim_id) {
        this.maxSim_id = maxSim_id;
    }

    public Integer getMaxSim_num() {
        return maxSim_num;
    }

    public void setMaxSim_num(Integer maxSim_num) {
        this.maxSim_num = maxSim_num;
    }

    public Double getMaxSim_link_rate() {
        return maxSim_link_rate;
    }

    public void setMaxSim_link_rate(Double maxSim_link_rate) {
        this.maxSim_link_rate = maxSim_link_rate;
    }

    public Double getMaxSim_ave_sim() {
        return maxSim_ave_sim;
    }

    public void setMaxSim_ave_sim(Double maxSim_ave_sim) {
        this.maxSim_ave_sim = maxSim_ave_sim;
    }

    public String getMaxLink_id() {
        return maxLink_id;
    }

    public void setMaxLink_id(String maxLink_id) {
        this.maxLink_id = maxLink_id;
    }

    public Integer getMaxLink_num() {
        return maxLink_num;
    }

    public void setMaxLink_num(Integer maxLink_num) {
        this.maxLink_num = maxLink_num;
    }

    public Double getMaxLink_link_rate() {
        return maxLink_link_rate;
    }

    public void setMaxLink_link_rate(Double maxLink_link_rate) {
        this.maxLink_link_rate = maxLink_link_rate;
    }

    public Double getMaxLink_ave_sim() {
        return maxLink_ave_sim;
    }

    public void setMaxLink_ave_sim(Double maxLink_ave_sim) {
        this.maxLink_ave_sim = maxLink_ave_sim;
    }
}

class Comparetor2 implements Comparator {

    public int compare(Object o1, Object o2) {
        PairSimOutput s1=(PairSimOutput)o1;
        PairSimOutput s2=(PairSimOutput)o2;

        int s1_pre = s1.getSelf_num();
        int s2_pre = s2.getSelf_num();//;
//        Double s1_pre = s1.getOpt();
//        Double s2_pre = s2.getOpt();//;
        if(s1_pre<s2_pre)//Down exchange
            return 1;
        else
            return -1;
    }
}