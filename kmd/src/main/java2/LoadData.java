package java;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yishuihan on 17-7-3.
 */
public class LoadData {
    private HashMap<String ,Integer> s_i_map = null;
    private HashMap<Integer ,String> i_s_map = null;
    ConcurrentHashMap<Integer,HashMap<Integer,Double>> sparse_mat = null;
    public LoadData(){
        s_i_map = new HashMap <>();
        i_s_map = new HashMap <>();
        sparse_mat = new ConcurrentHashMap <>();
    }
    public boolean buildSentencesIndexMap(String file) throws Exception{
        File rf = new File(file);
        if(!rf.isFile()){
            System.err.println(file+" is invalid!");
            System.exit(0);
            return false;
        }
        String line = "";
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(rf),"utf-8"));
        while((line = br.readLine())!= null){
            if(!s_i_map.containsKey(line))
                this.s_i_map.put(line,this.s_i_map.size());
        }
        return true;
    }
    public ConcurrentHashMap<Integer,HashMap<Integer,Double>> getSparseMat(){
        return  this.sparse_mat;
    }
    public boolean buildSentTopNMatMap(String topN_file,HashMap<String,Integer> s_i_map) throws Exception{

        File rf = new File(topN_file);
        if(!rf.isFile()){
            System.err.println(topN_file+" is invalid!");
            System.exit(0);
            return false;
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(rf),"utf-8"));
        String line = "";
        HashMap<Integer ,Double> i_topN = null;
        while((line = br.readLine())!=null){
            String[] str_list = line.split(",");

            Integer list0_i = s_i_map.get(str_list[0]);
            //System.out.println(str_list[0]);

            i_topN = new HashMap <>();
            for(int i =1;i<str_list.length;i++){
                String[] tmp = str_list[i].split(":");

                if(!s_i_map.containsKey(tmp[0])){
                    System.err.println("N error");
                }
                Integer tmp0_i = s_i_map.get(tmp[0]);
                Double tmp1_i = Double.valueOf(tmp[1]);
                i_topN.put(tmp0_i,tmp1_i);
                //System.out.println(tmp0_i+"\t"+tmp1_i);
            }
            if(!s_i_map.containsKey(str_list[0])){
                System.err.println("0 error");
            }

            if(i_topN.size()!=200 || str_list.length != 201)
                System.out.println(str_list.length);
            this.sparse_mat.put(list0_i,i_topN);
        }
        return true;
    }
    public boolean buildSentTopNMatMap(HashMap<Integer,String> i_s_map){
        return true;
    }

    public HashMap<String,Integer> getSentencesIndexMap(){
        return this.s_i_map;
    }
    public HashMap<Integer,String> getIndexSentencesMap(){

        for(Map.Entry<String,Integer>s_i :this.s_i_map.entrySet()){
            String sent = s_i.getKey();
            Integer ind = s_i.getValue();
            this.i_s_map.put(ind, sent);
        }
        return this.i_s_map;
    }
    public boolean buildSentencesIndexMap(List<String> list){
        if(list.size() == 0){
            System.err.println("the size of list is zero !");
            return false;
        }
        for(String s :list){
            if(!s_i_map.containsKey(s)){
                this.s_i_map.put(s,this.s_i_map.size());
            }
        }
        return true;
    }
}
