



import fig.basic.Pair;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yishuihan on 17-6-28.
 */
public class UtilKmediod {
    private HashMap<Integer,Double> dissim_map = null;
    private Set<Integer> set_s = new HashSet <>();
    private ConcurrentHashMap<Integer,HashMap<Integer,Double>> sparse_mat = null;
    public UtilKmediod(ConcurrentHashMap<Integer,HashMap<Integer,Double>> sparse_mat , Set<Integer> set_s){
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
        HashMap<Integer,Double> topN_i = null;
        topN_i = sparse_mat.get(n);
        if(topN_i.containsKey(k)){
            return topN_i.get(k);
        }else{
            return -1.0;
        }

    }
    public Integer getClosedCenter(int n,Set<Integer> set_c){
        if(set_c.contains(n)){
            return n;
        }
        Double n_k_sim = Double.MIN_VALUE;
        int ind = n;
        for(Integer c:set_c){
            Double tmp_sim = this.getPairCenterPointSim(c,n);

            if( tmp_sim > n_k_sim ){
                n_k_sim = tmp_sim;
                ind = c;
            }
        }
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

    public Map<Integer, Double> getBestDissimilarPoint(Set<Integer> set_c,Map<Integer,Double> dissim_map){

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
        initDissimSet(set_k);
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
