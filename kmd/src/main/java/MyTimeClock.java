/**
 * Created by yishuihan on 17-6-21.
 */
public class MyTimeClock {
    String idt = "";
    long startTime = 0;
    long endTime = 0;
    long midTime = 0;
    public MyTimeClock(String idt){
        this.idt = idt;
    }
    public void tt(){
        System.out.println(this.idt);
        startTime = System.currentTimeMillis();//start time
        midTime = startTime;
        System.out.println("start: " + startTime);
    }
    public void mm(){
        endTime = System.currentTimeMillis();    //获取当前时间
        System.out.println("mid : "+ endTime);
        System.out.println("程序片：" + (endTime - midTime) + "ms");    //输出程序片运行时间
        System.out.println("程序片：" + (endTime - midTime)/(1000*60) + "minute");    //输出程序片运行时间
        System.out.print("\n\n");
        midTime = endTime;
    }
    public void dd(){
        endTime = System.currentTimeMillis();    //获取结束时间
        System.out.println("end : "+ endTime);
        System.out.println("程序运行时间：" + (endTime - startTime) + "ms");    //输出程序运行时间
        System.out.println("程序运行时间：" + (endTime - startTime)/(1000*60) + "minute");    //输出程序运行时间
        System.out.print("\n\n");
    }


}
