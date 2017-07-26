import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by yishuihan on 17-6-24.
 */
public class MulThreadTest {
    public static void main(String[] args) throws Exception{
        MyTimeClock mt = new MyTimeClock("0");
        mt.tt();
        int count =100000;
        ConcurrentLinkedQueue<Integer> in = new ConcurrentLinkedQueue <>();
        String[] sim  =new String[20];
        for(int i = 0;i<20;i++){
            sim[i] = "";
        }

        System.out.println("-1-");
        Thread[] t = new Thread[20];
        for(int i = 0;i<20;i++){
            t[i]= new Thread(new thead_1(i,sim,in,count,1));
            //t[i].setDaemon(true);
            t[i].start();
        }

        System.out.println("-2-");

        for(int i = 0;i<20;i++){
            if(t[i].isAlive()){
                Thread.sleep(10);
                i--;
            }
        }
        for(int i = 0;i<20;i++){
            //System.out.println("id "+i+":"+sim[i].length());
        }
        System.out.println("-4-");
        System.out.println("finished!");
        mt.dd();
        for(int i = 0;i<20;i++){
            sim[i] = "";
        }

        for(int i = 0;i<20;i++){
            t[i]= new Thread(new thead_1(i,sim,in,count,2));
            //t[i].setDaemon(true);
            t[i].start();
        }

        System.out.println("-2-");

        for(int i = 0;i<20;i++){
            if(t[i].isAlive()){
                Thread.sleep(10);
                i--;
            }
        }
        for(int i = 0;i<20;i++){
            System.out.println("id "+i+":"+sim[i].length());
        }
        System.out.println("-4-");
        System.out.println("finished!");
        mt.dd();
    }
}

class thead_1 implements Runnable{

    String[] sim = null;
    int k = 0;
    boolean finised_flag = false;
    ConcurrentLinkedQueue<Integer> in = null;
    int count;
    int a = 0;
    public  thead_1(int k, String[] sim, ConcurrentLinkedQueue <Integer> in,int count,int a){
        this.in = in;
        this.k = k;
        this.sim = sim;
        finised_flag = false;
        this.count = count;
        this.a = a;
    }

    public boolean get_finished_flag(){
        return finised_flag;
    }
    public void run() {
        String b = "";
        for(int i = 0;i< count;i++){
            //sim[k] +=String.valueOf(a);
             b+= String.valueOf(a);
            //System.out.println(k);
        }
        //System.out.println(sim[k]);
        finised_flag = true;
    }
}