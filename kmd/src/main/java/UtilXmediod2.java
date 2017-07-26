import fig.basic.Pair;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yishuihan on 17-6-29.
 */
public class UtilXmediod2 {
    Double minsim = -1.0;
    private HashMap<Integer,Double> dissim_map = null;
    private Set<Integer> set_s = new HashSet<>();
    private ConcurrentHashMap<Integer,HashMap<Integer,Double>> sparse_mat = null;
    public UtilXmediod2(ConcurrentHashMap<Integer,HashMap<Integer,Double>> sparse_mat , Set<Integer> set_s){
        this.sparse_mat = sparse_mat;
        this.set_s = set_s;
    }
    public boolean initDissimSet(Set<Integer> set_k){
        this.dissim_map = new HashMap <>();
        Iterator<Integer> iterator = set_k.iterator();
        while(iterator.hasNext()){
            dissim_map.put(iterator.next(),Double.MAX_VALUE);
        }
        return true;
    }
    public Double getPairCenterPointSim(int k,int n){
        if(k == n){
            return 1.0;
        }
        HashMap<Integer,Double> topN_i = null;
        topN_i = sparse_mat.get(n);
        if(topN_i.containsKey(k)){
            return topN_i.get(k);
        }else{
            return -1.0;
        }

    }
    public void setMinSim(Double minsim){
        this.minsim = minsim;
    }
    public Double getMinSim(){
        return  this.minsim;
    }
    public Integer getClosedCenter(int n,Set<Integer> set_c){
        if(set_c.contains(n)){
            return n;
        }
        if(set_c.size()<=1){
            System.out.println(set_c);
        }
        Double n_k_sim = Double.MIN_VALUE;
        int ind = n;
        for(Integer c:set_c){
            //System.out.println(c+"-"+n);
            Double tmp_sim = this.getPairCenterPointSim(c,n);

            if( tmp_sim > n_k_sim ){
                n_k_sim = tmp_sim;
                ind = c;
            }
        }
        if(n_k_sim <= this.getMinSim()+1e-6)
            return -1;
        return ind;
    }
    public Integer updateCenter(Set<Integer> set_k,Integer c){
        int ind = c;
        Double sim_sum = 0.0;
        Double sim_max = Double.MIN_VALUE;
        for(Integer i:set_k){
            sim_sum = 0.0;
            for(Integer j:set_k){
                if(i.equals(j)){
                    continue;
                }
                sim_sum += this.getPairCenterPointSim(i,j);
            }
            if(sim_max < sim_sum){
                sim_max = sim_sum;
                ind = i;
            }
        }
        return ind;
    }
    public Set<Integer>AddCenter(Set<Integer> set_k,Integer c){
        Set<Integer> centers = new HashSet <>();
        if(set_k.size() <=3 || c == -1){
            centers.add(c);
            return centers;
        }


        Double max_sim = Double.MIN_VALUE;
        Double sum_sim = 0.0;
        for(Integer n :set_k){
            if(n.equals(c))
                continue;
            sum_sim += this.getPairCenterPointSim(c,n);
        }
        if(max_sim < sum_sim){
            max_sim = sum_sim;
            centers.add(c);
        }
        System.out.println("1："+sum_sim);
        sum_sim = 0.0;
        Set<Integer> set_centroids = this.getInitCentroids(2,set_k);
        for(Integer n : set_k){
            int c1 = this.getClosedCenter(n,set_centroids);
            if(n.equals(c1)){
                continue;
            }
            sum_sim+= this.getPairCenterPointSim(c1,n);
        }
        if(max_sim < sum_sim){
            max_sim = sum_sim;
            centers.clear();
            for(Integer c_tmp:set_centroids){
                centers.add(c_tmp);
            }
        }
        System.out.println("+2："+sum_sim);
        sum_sim = 0.0;
        double new_dissim = 0;
        Double dissimMax = Double.MIN_VALUE;
        int ind_max = c;
        for(Integer n: set_k){
                if(n.equals(c))
                    continue;
                new_dissim = Math.pow(1.0 - this.getPairCenterPointSim(c,n),2);
                if(new_dissim > dissimMax){
                    dissimMax = new_dissim;
                    ind_max = n;
                }
        }
        set_centroids.clear();
        set_centroids.add(c);
        set_centroids.add(ind_max);
        for(Integer n : set_k){
            int c1 = this.getClosedCenter(n,set_centroids);
            if(n.equals(c1)){
                continue;
            }
            sum_sim+= this.getPairCenterPointSim(c1,n);
        }
        if(max_sim < sum_sim){
            max_sim = sum_sim;
            centers.clear();
            for(Integer c_tmp:set_centroids){
                centers.add(c_tmp);
            }
        }
        System.out.println("-2："+sum_sim);
        return centers;
    }

    public Map<Integer, Double> getBestDissimilarPoint(Set<Integer> set_c, Map<Integer,Double> dissim_map){

        for(Map.Entry<Integer,Double> entry:dissim_map.entrySet()) {
            Integer p_i = entry.getKey();
            Double dissim_i = entry.getValue();
            double new_dissim = 0;
            for (Integer c : set_c) {
                new_dissim = Math.pow(1.0 - this.getPairCenterPointSim(c,p_i),2);
                if(new_dissim < dissim_i){
                    entry.setValue(new_dissim);
                }
            }

        }
        return dissim_map;
    }
    public Map<Integer,Double> getDissimMap(){
        return this.dissim_map;
    }
    public Set<Integer> getInitCentroids(int k,Set<Integer> set_k){
        Random random = new Random();
        int first = random.nextInt(set_k.size());
        List<Integer> list1 = new ArrayList<Integer>(set_k);
        first = list1.get(first);
        int cen_count = 1;
        this.initDissimSet(set_k);
        Map<Integer,Double> dissim_map = this.dissim_map;
        Set<Integer> centoids = new HashSet <>();
        centoids.add(first);
        dissim_map.remove(first);
        double sum_dissim = 0;
        double rand_dissim = 0;
        while(true) {
            dissim_map = getBestDissimilarPoint(centoids,dissim_map);
            sum_dissim = 0;
            for (Map.Entry <Integer, Double> entry : dissim_map.entrySet()) {
                sum_dissim += entry.getValue();
            }

            rand_dissim = random.nextDouble()*sum_dissim;
            //System.out.println(rand_dissim);
            for (Map.Entry <Integer, Double> entry : dissim_map.entrySet()) {
                rand_dissim = rand_dissim - entry.getValue();
                if(rand_dissim < 0){
                    centoids.add(entry.getKey());
                    dissim_map.remove(entry.getKey());
                    cen_count++;
                    break;
                }
            }
            if(cen_count % 50 ==0){
                System.out.println("c:"+cen_count);
            }
            if(cen_count == k){
                break;
            }
        }
        return centoids;
    }


    public   void addSortedClusterList (List<Pair<Integer,Double>> list, Pair<Integer,Double> ex) {
        int index = list.size();
        for (int i = 0; i < list.size(); i++) {
            Pair<Integer, Double> tt = list.get(i);
            if (tt.getSecond() < ex.getSecond()) {
                index = i;
                break;
            }
        }

        if (index == list.size()) {
            list.add(ex);
        } else {
            list.add(index, ex);
        }

    }
}
