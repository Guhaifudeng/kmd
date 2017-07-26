import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by yishuihan on 17-6-26.
 */
public class ComMulThreadTest {

    public static void main(String[] args) throws Exception{
        MyTimeClock mt = new MyTimeClock("0001");
        mt.tt();
        int count =10000;
        String[] sim  =new String[20];
        for(int i = 0;i<20;i++){
            sim[i] = "";
        }


        String b = "";
        for(int i = 0;i<20;i++){
            b="";
            for(int j = 0;j<count;j++)
                b += String.valueOf(1);
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
            b+= "";
            for(int j = 0;j<count;j++)
                b += String.valueOf(2);

        }

        System.out.println("-2-");


        for(int i = 0;i<20;i++){
            //System.out.println("id "+i+":"+sim[i].length());
        }
        System.out.println("-4-");
        System.out.println("finished!");
        mt.dd();
    }
}
