package src.main.java2;


import fig.basic.Pair;

import java.util.ArrayList;

/**
 * Created by yishuihan on 17-7-3.
 */
public class Cluster {
    private Integer id = -1;
    private ArrayList<Integer> points = null;
    private ArrayList<Pair<Integer,Integer>> pot_size_list = null;
    private ArrayList<MergeMess> merge_clu_list = null;
    private Double EC = Double.MIN_VALUE;
    private ChameleonTool chameleonTool = null;
    private Double opt = Double.MIN_VALUE;
    private Double RI = Double.MIN_VALUE;
    private Double RC = Double.MIN_VALUE;
    private Double alpha1 = 1.0;
    private Double maxRC = -0.1;
    private Double maxRI = -0.1;
    private Double maxOpt = -0.1;
    private Double alpha2 = 1.0;
    private Double SEC = 1.0;
    private Integer merge_edge_num = 1;
    private boolean is_merged ;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Cluster(Integer id, ChameleonTool chameleonTool){
        this.id = id;
        this.chameleonTool = chameleonTool;
        this.is_merged = false;
        this.pot_size_list = new ArrayList <>();
        this.merge_clu_list = new ArrayList <>();
        this.SEC = 1.0;
    }
    public void addToPotSizeList(Integer it,Integer num){
        this.pot_size_list.add(new Pair <>(it,num));
    }
    public void addToMergeCluList(MergeMess mergeMess){
        this.merge_clu_list.add(mergeMess);
    }
    public ArrayList <Pair<Integer,Integer>> getPot_size_list() {
        return pot_size_list;
    }

    public void setPot_size_list(ArrayList <Pair<Integer,Integer>> pot_size_list) {
        this.pot_size_list = pot_size_list;
    }

    public ArrayList <MergeMess> getMerge_clu_list() {
        return merge_clu_list;
    }

    public void setMerge_clu_list(ArrayList <MergeMess> merge_clu_list) {
        this.merge_clu_list = merge_clu_list;
    }


    public Double getSEC() {
        if(!isIs_merged())

        {
            if(this.points.size()==1){
                return 1.0;
                //return  this.chameleonTool.getNearestPointSim(this.getPoints().get(0));
                //return  this.chameleonTool.getThreshold();
            }
            else{
                Integer point1 = this.points.get(0);
                Integer point2 = this.points.get(this.getPointSize()/2);
                return this.chameleonTool.getPairCenterPointSim(point1,point2);
            }
        }
        return SEC;
    }

    public void setSEC(Double SEC) {
        this.SEC = SEC;
    }
    public Double getEC() {
        if(!is_merged){
            if(!is_merged){
                int point_size = this.getPointSize();
                if(point_size==1||point_size==2){
                    return this.getSEC();
                }else if(point_size ==3) {
                    return this.getSEC()*2;
                }else{
                    return  this.getSEC() * (int)(Math.pow((int)(point_size/2),2));
                }
            }
        }
        return EC;
        //return EC;
    }
    public void setEC(Double EC) {
        this.EC = EC;
    }
    public Double getRI() {
        if(!is_merged){
            int point_size = this.getPointSize();
            if(point_size==1||point_size==2){
                return 1.0;
            }else if(point_size ==3) {
                return 3.0;
            }else if(point_size >= 4){
                return  4.0;
            }
        }
        return RI;
    }

    public void setRI(Double RI) {
        this.RI = RI;
    }

    public Double getRC() {
        if(!is_merged){
            return 1.0;
        }

        return RC;
    }

    public void setRC(Double RC) {
        this.RC = RC;
    }

    public Double getAlpha1() {
        return alpha1;
    }

    public void setAlpha1(Double alpha1) {
        this.alpha1 = alpha1;
    }

    public Double getAlpha2() {
        return alpha2;
    }

    public void setAlpha2(Double alpha2) {
        this.alpha2 = alpha2;
    }

    public Double getOpt() {
        if(!is_merged){
            return Math.pow(this.getRI(),this.getAlpha1())*Math.pow(this.getRC(),this.getAlpha2());
        }
        return opt;
    }

    public void setOpt(Double opt) {
        this.opt = opt;
    }




    public ArrayList <Integer> getPoints() {
        return points;
    }

    public boolean isIs_merged() {
        return is_merged;
    }

    public void setIs_merged(boolean is_merged) {
        this.is_merged = is_merged;
    }


    public void addPoint(Integer point){
        this.points.add(point);
    }
    public void initPoint(){
        points = new ArrayList <>();
    }
    public Integer getMergeEdgeNum() {
        if(!is_merged){
            int point_size = this.getPointSize();
            if(point_size==1||point_size==2){
                return 1;
            }else if(point_size ==3) {
                return 2;
            }else {

                return  (int)(Math.pow((int)(point_size/2),2));
            }
        }
        return merge_edge_num;
    }

    public Integer getPointSize(){
        return this.points.size();
    }
    public void setMergeEdgeNum(Integer merge_edge_num) {
        this.merge_edge_num = merge_edge_num;
    }


    public Double getMaxRC() {
        return maxRC;
    }

    public void setMaxRC(Double maxRC) {

        this.maxRC = maxRC;


    }

    public Double getMaxRI() {
        return maxRI;
    }

    public void setMaxRI(Double maxRI) {

        this.maxRI = maxRI;
    }

    public Double getMaxOpt() {

        return maxOpt;
    }

    public void setMaxOpt(Double maxOpt) {
        this.maxOpt = maxOpt;
    }

    private Double global_MaxOpt = Double.MIN_VALUE;
    private Double global_MaxRC  = Double.MIN_VALUE;
    private Double global_MaxRI  = Double.MIN_VALUE;

    public Double getGlobal_MaxOpt() {
        return global_MaxOpt;
    }

    public void setGlobal_MaxOpt(Double global_MaxOpt) {
        if(global_MaxOpt>this.global_MaxOpt)
            this.global_MaxOpt = global_MaxOpt;
    }

    public Double getGlobal_MaxRC() {
        return global_MaxRC;
    }

    public void setGlobal_MaxRC(Double global_MaxRC) {
        if(global_MaxRC>this.global_MaxRC)
            this.global_MaxRC = global_MaxRC;
    }

    public Double getGlobal_MaxRI() {

        return global_MaxRI;
    }

    public void setGlobal_MaxRI(Double global_MaxRI) {
        if(global_MaxRI>this.getGlobal_MaxRI())
            this.global_MaxRI = global_MaxRI;
    }
    Integer maxNearID = -1;

    public Integer getMaxNearID() {
        return maxNearID;
    }

    public void setMaxNearID(Integer maxNearID) {
        this.maxNearID = maxNearID;
    }
}
class MergeMess{
    Integer e_first = -1;
    Integer e_size = -1;
    Double RI = -1.0;
    Double RC = -1.0;

    public Integer getE_first() {
        return e_first;
    }

    public void setE_first(Integer e_first) {
        this.e_first = e_first;
    }

    public Integer getE_size() {
        return e_size;
    }

    public void setE_size(Integer e_size) {
        this.e_size = e_size;
    }

    public Double getRI() {
        return RI;
    }

    public void setRI(Double RI) {
        this.RI = RI;
    }

    public Double getRC() {
        return RC;
    }

    public void setRC(Double RC) {
        this.RC = RC;
    }
}