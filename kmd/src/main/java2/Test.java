package src.main.java2;

import fig.basic.Pair;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by yishuihan on 17-7-6.
 */
public class Test {
    public static void main(String[] args) throws Exception{
//        Queue<Double> hello =new PriorityQueue<Double>(30000*3000);
//        for(int i =0;i<30000;i++){
//            for(int j =0;i<30000;j++){
//                hello.add(Double.valueOf(i*30000+j));
//            }
//        }
//        PriorityQueue<Integer> test = new PriorityQueue <>(2);
//        //test.
//        test.add(1);
//        test.add(22);
//        test.add(3);
//        test.add(4);
//
//        while (!test.isEmpty()){
//            Integer a = test.poll();
//            System.out.println(a);
//        }
//    A a1 = new A(1);
//    A a2 = new A(2);
//    A a3 = a1;
//    a1 = a2;
//    a2 = a3;
//        System.out.println(String.format("%4s\t","hello")+String.format("%4s\t","hello"));
//        System.out.println(String.format("%4s\t","hhelloeello")+String.format("%4s\t","hellohello"));
//        String[] aa = String.valueOf("").split("\t");
//        System.out.println(aa[0].length());
        Integer threadnum  = -1;
        for (int i = 0; i < args.length; i += 1) {
            String arg = args[i];
            if (arg.startsWith("--")) {
                arg = arg.substring(2);
            } else if (arg.startsWith("-")) {
                arg = arg.substring(1);
            }

            if (arg.equalsIgnoreCase("th")) {
                threadnum = Integer.valueOf(args[i + 1]);
            }
        }
        MyTimeClock mt = new MyTimeClock("--44--");
        mt.tt();
        Integer flag  = null;
        int[][] mat = new int[10005][10005];
        int[][] o_mat = new int[10005][10005];
        for(int i =0;i<10000;i++){
            for(int j=0;j<10000;j++){
                mat[i][j] = 1;
            }
        }
        mt.mm();
        ConcurrentLinkedQueue<Pair<Integer,Integer>> queue = new ConcurrentLinkedQueue <>();
        ConcurrentLinkedQueue<C> out = new ConcurrentLinkedQueue <>();
        Thread t = null;
        for(int i = 0;i<threadnum;i++){
            BT bt= new BT(queue,flag,mat,out);
            t = new Thread(bt);
            t.start();
        }
        System.out.println(Thread.activeCount());
        for(int i =0;i<10000;i++){
            for(int j =0;j<10000;j++){
                int sum = 0;
                queue.add(new Pair <>(i,j));
                while (queue.size()>8000){
                    Thread.sleep(10);
                }
            }
            if(i % 1000 ==0){
                System.out.println("\t"+i+"\t");
                mt.mm();
            }
        }
//        for(int i =0;i<10000;i++){
//            for(int j =0;j<10000;j++){
//                int sum = 0;
//                for(int k=0;k<10000;k++){
//                    sum += mat[i][k] * mat[k][j];
//                }
//                C c = new C(i,j,sum);
//                out.add(c);
//            }
//            if(i % 100 ==0){
//                System.out.println("\t"+i+"\t");
//                mt.mm();
//            }
//        }
        mt.mm();
        flag = new Integer(0);
        System.out.println("hello : "+out.size());
        while (!out.isEmpty()){
            C c = out.poll();
            o_mat[c.i][c.j] = c.sum;
        }
        mt.mm();
        mt.dd();
    }
}
class  A{
    private int id;
    A(int id){
        this.id = id;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
class BT implements  Runnable{
    ConcurrentLinkedQueue<Pair<Integer,Integer>> queue = null;
    Integer end_flag = null;
    int[][] mat = null;
    int[][] out_mat = null;
    ConcurrentLinkedQueue<C> out = null;//new ConcurrentLinkedQueue <>();
    //PriorityBlockingQueue<C>
    public BT(ConcurrentLinkedQueue<Pair<Integer,Integer>> queue,Integer end_flag,int[][] mat,ConcurrentLinkedQueue<C> out){
        this.queue = queue;
        this.end_flag = end_flag;
        this.mat = mat;
        this.out = out;
        //this.out_mat = out_mat;

    }
    @Override
    public void run() {
        while(end_flag==null || !queue.isEmpty()){
            while (!queue.isEmpty()) {

                Pair <Integer, Integer> pair = queue.poll();
                if(pair == null) //multithreading
                    continue;
                Integer n = pair.getFirst();
                Integer m = pair.getFirst();
                Integer sum = 0;
                for (int i = 0; i < 10000; i++) {
                    sum += mat[m][i] * mat[i][n];
                }

               // out_mat[n][m] = sum;
                C c = new C(n,m,sum);
                out.add(c);
            }
        }
    }
}
class C{
    Integer i = -1;
    Integer j = -1;
    Integer sum = -1;
    public C(Integer i,Integer j,Integer sum){
        this.i = i;
        this.j = j;
        this.sum = sum;
    }
    public Integer getI() {
        return i;
    }

    public void setI(Integer i) {
        this.i = i;
    }

    public Integer getJ() {
        return j;
    }

    public void setJ(Integer j) {
        this.j = j;
    }

    public Integer getSum() {
        return sum;
    }

    public void setSum(Integer sum) {
        this.sum = sum;
    }
}