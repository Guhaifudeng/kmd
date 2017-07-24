package java;

import java.util.ArrayList;

/**
 * Created by yishuihan on 17-7-3.
 */
public class Cluster {
    private Integer id = -1;
    private ArrayList<Integer> points = null;
    private Double EC = 0.0;
    private Integer merge_edge_num = 0;



    public Cluster(Integer id, ArrayList<Integer> points, Double EC){
        this.id = id;
        this.points = points;
        this.EC = EC;
    }
    public Cluster(Integer id,ArrayList<Integer> points){
        this.id = id;
        this.points = points;
    }
    public Cluster(Integer id){
        this.id = id;
    }
    public Cluster(){}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ArrayList <Integer> getPoints() {
        return points;
    }

    public void setPoints(ArrayList <Integer> points) {
        this.points = points;
    }

    public Double getEC() {
        return EC;
    }

    public void setEC(Double EC) {
        this.EC = EC;
    }
    public void addPoint(Integer point){
        this.points.add(point);
    }
    public Integer getMergeEdgeNum() {
        return merge_edge_num;
    }

    public Integer getPointSize(){
        return this.points.size();
    }
    public void setMergeEdgeNum(Integer merge_edge_num) {
        this.merge_edge_num = merge_edge_num;
    }
}
