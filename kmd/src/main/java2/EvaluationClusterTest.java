package src.main.java2;

import javafx.beans.binding.DoubleExpression;
import org.omg.CORBA.INTERNAL;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import javax.swing.event.DocumentEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by yishuihan on 17-7-27.
 */
public class EvaluationClusterTest {
        public static void main(String[] args) throws Exception{
            String dir  ="/home/badou/data/kmediod/chemeleon/360/2/";
            String class_file = dir+"word3387_0.6_30000_class_pair.txt";
            String cluster_file = dir+ "word3387_0.6_30000_cluster.txt";

            for (int i = 0; i < args.length; i += 1) {
                String arg = args[i];
                if (arg.startsWith("--")) {
                    arg = arg.substring(2);
                } else if (arg.startsWith("-")) {
                    arg = arg.substring(1);
                }

                if(arg.equalsIgnoreCase("class")) {
                    class_file = args[i + 1];
                }else if(arg.equalsIgnoreCase("cluster")){
                    cluster_file = args[i+1];
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

            ExternalMeasure externalMeasure = new ExternalMeasure();
            externalMeasure.setClassMess(class_id_e,class_e_id,class_i_s,class_s_i);
            externalMeasure.setClusterMess(cluster_id_e,cluster_e_id,cluster_i_s,cluster_s_i);
            Double rand = externalMeasure.calRand();
            System.out.println("rand " + rand );
            Double jaccard = externalMeasure.caJaccard();
            System.out.println("jaccard "+ jaccard);
            Double class_f = externalMeasure.calClassFMeasure();
            System.out.println("class F "+ class_f);
            Double cluster_f = externalMeasure.calClusterFMeasure();
            System.out.println("cluster F " + cluster_f);
            Double class_H = externalMeasure.calClassEntropy();
            System.out.println("class H "  + class_H);
            Double cluster_H = externalMeasure.calClusterEntropy();
            System.out.println("cluster H " + cluster_H);
        }
}
class ExternalMeasure{
    HashMap<Integer,HashSet<Integer>> class_id_e = null;
    HashMap<String,Integer> class_s_i = null;
    HashMap<Integer,String> class_i_s = null;
    HashMap<Integer,HashSet<Integer>> cluster_id_e = null;
    HashMap<String,Integer> cluster_s_i = null;
    HashMap<Integer,String> cluster_i_s = null;
    HashMap<Integer,Integer> class_e_id = null;
    HashMap<Integer,Integer> cluster_e_id = null;

    public void setClassMess(HashMap<Integer,HashSet<Integer>> class_id_e,HashMap<Integer,Integer> class_e_id,
                             HashMap<Integer,String> class_i_s,HashMap<String,Integer> class_s_i){
        this.class_id_e = class_id_e;
        this.class_e_id = class_e_id;
        this.class_i_s = class_i_s;
        this.class_s_i = class_s_i;
    }
    public void setClusterMess(HashMap<Integer,HashSet<Integer>> cluster_id_e, HashMap<Integer,Integer> cluster_e_id,
                               HashMap<Integer,String> cluster_i_s,HashMap<String,Integer> cluster_s_i){
        this.cluster_id_e = cluster_id_e;
        this.cluster_e_id = cluster_e_id;
        this.cluster_i_s = cluster_i_s;
        this.cluster_s_i = cluster_s_i;
    }
    //Rand
    Double calRand(){
        int TP = 0;
        int FP = 0;
        int TN = 0;
        int FN = 0;
        HashSet<Integer> set =  new HashSet <>(cluster_i_s.keySet());
        int i=0,j=0;
        for(Integer c1:set){
            j = 0;
            for(Integer c2:set){
                if(i<=j) break;
                Integer class_1 = class_e_id.get(c1);
                Integer class_2 = class_e_id.get(c2);
                Integer cluster_1 = cluster_e_id.get(c1);
                Integer cluster_2  = cluster_e_id.get(c2);
                if(class_1.equals(class_2) && cluster_1.equals(cluster_2))
                    TP++;
                if(class_1.equals(class_2) && !cluster_1.equals(cluster_2))
                    FP++;
                if(!class_1.equals(class_2) && !cluster_1.equals(cluster_2))
                    TN++;
                if(!class_1.equals(class_2) && cluster_1.equals(cluster_2))
                    FN++;
                j++;
            }
            i++;
        }
        if(TN+TP + FN +FP != set.size()*(set.size()-1)/2){
            System.err.println("EvaluationClusterTest err 96");
            System.exit(0);
        }
        return 1.0*(TP+TN)/(TP+TN+FN+FP);
    }
    //Jaccard
    Double caJaccard(){
        int TP = 0;
        int FP = 0;
        int TN = 0;
        int FN = 0;
        HashSet<Integer> set =  new HashSet <>(cluster_i_s.keySet());
        int i=0,j=0;
        for(Integer c1:set){
            j = 0;
            for(Integer c2:set){
                if(i<=j) break;
                Integer class_1 = class_e_id.get(c1);
                Integer class_2 = class_e_id.get(c2);
                Integer cluster_1 = cluster_e_id.get(c1);
                Integer cluster_2  = cluster_e_id.get(c2);
                if(class_1.equals(class_2) && cluster_1.equals(cluster_2))
                    TP++;
                if(class_1.equals(class_2) && !cluster_1.equals(cluster_2))
                    FP++;
                if(!class_1.equals(class_2) && !cluster_1.equals(cluster_2))
                    TN++;
                if(!class_1.equals(class_2) && cluster_1.equals(cluster_2))
                    FN++;
                j++;
            }
            i++;
        }
        if(TN+TP + FN +FP != set.size()*(set.size()-1)/2){
            System.err.println("EvaluationClusterTest err 96");
            System.exit(0);
        }
        return 1.0*(TP)/(TP+FN+FP);
    }

    Double calClassFMeasure(){
        Double precision = 0.0;
        Double recall = 0.0;
        Double class_F = 0.0;
        Double alpha = 1.0;
        Double classes_F = 0.0;
        Integer class_e_size = 0;
        for(Map.Entry<Integer,HashSet<Integer>> entry: class_id_e.entrySet()){
            Integer class_id = entry.getKey();
            HashSet<Integer> class_e = entry.getValue();
            Double max_class_F = 0.0;
            for(Map.Entry<Integer,HashSet<Integer>> entry1:cluster_id_e.entrySet()){
                Integer cluster_id = entry1.getKey();
                HashSet<Integer> cluster_e = entry1.getValue();
                HashSet<Integer> tmp_class_e = new HashSet <>();
                tmp_class_e.addAll(class_e);
                tmp_class_e.removeAll(cluster_e);
                Integer intersection_size  = class_e.size()-tmp_class_e.size();
//                if(intersection_size>0){
//                    System.exit(1);
//                }
                precision = 1.0 * intersection_size / cluster_e.size();
                recall = 1.0 * intersection_size / class_e.size();
                class_F = (1+alpha) /(1/precision + alpha/recall);

                if(class_F > max_class_F)
                    max_class_F = class_F;

            }
            class_e_size += class_e.size();
            classes_F += max_class_F * class_e.size()/ cluster_e_id.size();
        }
        if(class_e_size != cluster_e_id.size()){
            System.err.println("EvaluationClusterTest err 181");
            System.exit(0);
        }
        return classes_F;
    }




    Double calClusterFMeasure(){
        Double precision = 0.0;
        Double recall = 0.0;
        Double cluster_F = 0.0;
        Double alpha = 1.0;
        Double clusters_F = 0.0;
        Integer clusters_e_size = 0;
        for(Map.Entry<Integer,HashSet<Integer>> entry: cluster_id_e.entrySet()){
            Integer cluster_id = entry.getKey();
            HashSet<Integer> cluster_e = entry.getValue();
            Double max_cluster_F = 0.0;
            for(Map.Entry<Integer,HashSet<Integer>> entry1:class_id_e.entrySet()){
                Integer class_id = entry1.getKey();
                HashSet<Integer> class_e = entry1.getValue();
                HashSet<Integer> tmp_cluster_e = new HashSet <>();
                tmp_cluster_e.addAll(cluster_e);
                tmp_cluster_e.removeAll(class_e);
                Integer intersection_size  = cluster_e.size()-tmp_cluster_e.size();
                precision = 1.0 * intersection_size / class_e.size();
                recall = 1.0 * intersection_size / cluster_e.size();
                cluster_F = (1+alpha) /(1/precision + alpha/recall);

                if(cluster_F > max_cluster_F)
                    max_cluster_F = cluster_F;
            }
            clusters_e_size += cluster_e.size();
            clusters_F += max_cluster_F * cluster_e.size()/ cluster_e_id.size();
        }
        if(clusters_e_size != cluster_e_id.size()){
            System.err.println("EvaluationClusterTest err 181");
            System.exit(0);
        }
        return clusters_F;
    }

    Double calClusterEntropy(){

        Double cluster_entropy = 0.0;
        Double clusters_entropy = 0.0;
        Double cluster_entropy_i = 0.0;
        Integer clusters_e_size = 0;
        for(Map.Entry<Integer,HashSet<Integer>> entry: cluster_id_e.entrySet()){
            Integer cluster_id = entry.getKey();
            HashSet<Integer> cluster_e = entry.getValue();
            cluster_entropy = 0.0;
            for(Map.Entry<Integer,HashSet<Integer>> entry1:class_id_e.entrySet()){
                cluster_entropy_i = 0.0;
                Integer class_id = entry1.getKey();
                HashSet<Integer> class_e = entry1.getValue();
                HashSet<Integer> tmp_cluster_e = new HashSet <>();
                tmp_cluster_e.addAll(cluster_e);
                tmp_cluster_e.removeAll(class_e);
                Integer intersection_size  = cluster_e.size()-tmp_cluster_e.size();
                if(intersection_size!=0)
                    cluster_entropy_i = Math.log(1.0*cluster_e.size()/intersection_size) *intersection_size/cluster_e.size();
                cluster_entropy += cluster_entropy_i;
            }

            clusters_e_size += cluster_e.size();
            System.out.println(cluster_entropy+" " +cluster_e.size());
            clusters_entropy += cluster_entropy * cluster_e.size()/ cluster_e_id.size();
        }
        if(clusters_e_size != cluster_e_id.size()){
            System.err.println("EvaluationClusterTest err 181");
            System.exit(0);
        }
        return clusters_entropy;
    }

    Double calClassEntropy(){//类分的越少越好

        Double class_entropy = 0.0;
        Double classes_entropy = 0.0;
        Double class_entropy_i = 0.0;
        Integer classs_e_size = 0;
        for(Map.Entry<Integer,HashSet<Integer>> entry: class_id_e.entrySet()){
            Integer class_id = entry.getKey();
            HashSet<Integer> class_e = entry.getValue();
            class_entropy = 0.0;
            for(Map.Entry<Integer,HashSet<Integer>> entry1:cluster_id_e.entrySet()){
                class_entropy_i = 0.0;
                Integer cluster_id = entry1.getKey();
                HashSet<Integer> cluster_e = entry1.getValue();
                HashSet<Integer> tmp_class_e = new HashSet <>();
                tmp_class_e.addAll(class_e);
                tmp_class_e.removeAll(cluster_e);
                Integer intersection_size  = class_e.size()-tmp_class_e.size();
                if(intersection_size!=0)
                    class_entropy_i = Math.log(1.0*class_e.size()/intersection_size) *intersection_size/class_e.size();
                class_entropy += class_entropy_i;
            }

            classs_e_size += class_e.size();
            System.out.println(class_entropy+" " +class_e.size());
            classes_entropy += class_entropy * class_e.size()/ cluster_e_id.size();
        }
        if(classs_e_size != class_e_id.size()){
            System.err.println("EvaluationclassTest err 281");
            System.exit(0);
        }
        return classes_entropy;
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
}

