import org.omg.PortableInterceptor.INACTIVE;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.util.*;

/**
 * Created by yishuihan on 17-6-28.
 */
public class test {
    public static void main(String[] args){
        HashMap<Integer,ArrayList<Integer>> hello = new HashMap <>();
        ArrayList<Integer> tmp = new ArrayList <>();
        Set<Integer> set_k = new HashSet <>();
        for(int i = 0;i<10;i++){
            tmp.add(i+10);
            set_k.add(i+10);
        }
        hello.put(-1,tmp);
        tmp = hello.get(-1);
        System.out.println(tmp.toString());

        tmp.add(10);
        tmp = hello.get(-1);
        System.out.println(tmp.toString());

        Random rand = new Random(10);

        for(int i = 100;i>0;i--){
            int num = rand.nextInt(10);
            System.out.println(num);
        }

        test t = new test();
        for(int i =0;i<set_k.size();i++){
            System.out.print(t.getSetIntegerWithIndex(set_k,i)+"\t");
        }
        System.out.println();
        ArrayList<Integer> list_k = new ArrayList <>(set_k);
        for(int i =0;i<list_k.size();i++){
            System.out.print(list_k.get(i)+"\t");
        }
        System.out.println();
    }

    public  Integer getSetIntegerWithIndex(Set<Integer> set_k, int num){
        Iterator<Integer> iterator = set_k.iterator();
        if(num >= set_k.size())
            return -1;
        for(int i =0;i<num;i++){
            iterator.next();
        }
        return  iterator.next();
    }
}
