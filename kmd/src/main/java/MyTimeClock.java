/**
 * Created by yishuihan on 17-6-21.
 */
public class MyTimeClock {
    String idt = "";
    long startTime = 0;
    long endTime = 0;
    public MyTimeClock(String idt){
        this.idt = idt;
    }
    public void tt(){
        System.out.println(this.idt);
        startTime = System.currentTimeMillis();//start time
        System.out.println("start: " + startTime);
    }
    public void dd(){
        endTime = System.currentTimeMillis();    //获取结束时间
        System.out.println("end : "+ endTime);
        System.out.println("程序运行时间：" + (endTime - startTime) + "ms");    //输出程序运行时间
        System.out.println("程序运行时间：" + (endTime - startTime)/(1000*60) + "minute");    //输出程序运行时间
    }


}
