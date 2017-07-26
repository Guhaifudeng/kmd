package src.main.java2;

import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by yishuihan on 17-7-24.
 */
public class Test2 {
    public static void main(String[] args) throws  Exception{
        MyTimeClock mt = new MyTimeClock("--1--");
        mt.tt();
        PriorityBlockingQueue<Integer> integers = new PriorityBlockingQueue <>();
        HashSet<Integer> integers1 = new HashSet <>();

        for(int i =1;i<=1200000;i++)
            integers.add(i);
        for(int i=0;i<3000;i = i+100){
            integers1.add(i);
        }
        mt.mm();
//        for(Integer integer:integers1)
//            integers.remove(integer);
        integers.removeAll(integers1);
//        Iterator<Integer> iterator = integers.iterator();
//        while(iterator.hasNext()){
//            System.out.println(iterator.next());
//        }
        mt.mm();
        int pre_max = 0;
        while(!integers.isEmpty()){
            int now = integers.poll();
            if(now > pre_max){
                pre_max = now;
            }else{
                System.err.println("bug");
            }
            //System.out.println(integers.poll());
        }
    mt.mm();
//        AA aa = new AA();
//        BB bb = new BB();
//        Thread t1 = null;
//        Thread t2 = null;
//        for(int i = 0;i<10;i++){
//            t1 = new Thread(aa);
//            t2 = new Thread(bb);
//            t1.start();
//            t1.join();
//            t2.start();
//            t2.join();
//        }
    }
}
//class AA implements Runnable{
//
//
//    @Override
//    public void run() {
//        System.out.println("1-1");
//    }
//}
//class BB implements Runnable{
//
//    @Override
//    public void run() {
//        System.out.println("2-2");
//    }
//}