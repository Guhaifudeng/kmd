/*
* 该程序耗费不少精力，转载请注明出处，未经许可请勿发表
* 该程序使用了多线程处理分类数据
* 该程序在编写时遇到不少问题
* 该程序由neohua与qinchuanqing共同编写
* 主要解决线程安全问题
* 程序可以继续改进，将读入数据的同时处理数据的方法同时写入线程，提高效率
* 同时对每次读入的缓存做限制，已解决内存控制问题
*

*/


import java.awt.BufferCapabilities;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.Buffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public  class Hello {


    public Hello() {
        // TODO 自动生成的构造函数存根

    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO 自动生成的方法存根

        flff fl=new flff();
    }

}


class jvff
{
    int k;// 指定划分的簇数
    double[][] center; // 上一次各簇质心的位置
    double[] crita; // 存放每次运行的满意度
    String temp="";
    BufferedReader reader=null;
    String[] data;
    Vector<Vector> trueData;
    Vector<Double> tem;
    int linenumber;
    int tt;
    int min[];
    flff fl;
    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    public double[][] getCenter() {
        return center;
    }

    public void setCenter(double[][] center) {
        this.center = center;
    }

    public double[] getCrita() {
        return crita;
    }

    public void setCrita(double[] crita) {
        this.crita = crita;
    }


    public jvff() {
        // TODO 自动生成的构造函数存根
        System.out.println("聚类开始时间"+new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()));
        try {
            reader = new BufferedReader(new FileReader("g:/all22_3_26_01.csv"));//g:/all22_3_26_01.csv//g:/testbx.csv
        } catch (FileNotFoundException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        }
        System.out.println(new SimpleDateFormat("聚类到内存时间"+"yyyyMMddHHmmssSSS").format(new Date()));
        data=new String[]{};
        trueData=new Vector();
        tem=new Vector<Double>();
        try {
            while((temp=reader.readLine())!=null)
            {
                data=temp.split(",");
                for(int i=0; i<data.length; i++)
                {
                    tem.add(Double.valueOf(data[i]));
                }
                trueData.add(tem);
                tem=new Vector<Double>();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            reader.close();
        } catch (IOException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        }
        linenumber=trueData.size();
        System.out.println(linenumber);      //输出行数
        System.out.println(new SimpleDateFormat("聚类读取文件时间"+"yyyyMMddHHmmssSSS").format(new Date()));
        Scanner s=new Scanner(System.in);
        System.out.println("输入分类数");
        k = s.nextInt();
        System.out.println(new SimpleDateFormat("聚类算法开始时间"+"yyyyMMddHHmmssSSS").format(new Date()));
        center = new double[k][2];
        crita =new double[k];
        tt=linenumber/k;
        for (int i=0;i<k;i++) {
            Random random = new Random(System.currentTimeMillis());
            int ret =random.nextInt(tt)+i*tt;   //将数据分为k部分，随机从每个部分抽一行记录存放到center数组中
            for(int j=0;j<2;j++){
                center[i][j] =  (double)trueData.get(ret).get(j);
            }
            System.out.println(center[i][0]+","+center[i][1]);           //输出初始质心
        }

        for(int i=0;i<linenumber;i++){
            min=new int[linenumber];
            //计算满意度
            for(int j=0;j<k;j++){
                //用欧几里得距离公式计算满意度
                crita[j]=java.lang.StrictMath.pow(((java.lang.StrictMath.pow(((double)trueData.get(i).get(0)-center[j][0]),(double)k))+(java.lang.StrictMath.pow(((double)trueData.get(i).get(1)-center[j][1]),(double)k))),(1.0/k));
                //System.out.println(crita[j]);//测试置信度
            }
            //比较满意度，求最小值
            for(int j=1;j<k;j++){
                if(crita[j]<crita[min[i]]){
                    min[i]=j;
                }
            }
            //更新质心
            //System.out.println("更新前"+(double)trueData.get(i).get(0)+","+(double)trueData.get(i).get(1));
            center[min[i]][0]=((double)trueData.get(i).get(0)+center[min[i]][0])/2.0;
            center[min[i]][1]=((double)trueData.get(i).get(1)+center[min[i]][1])/2.0;
            //System.out.println("更新后"+center[min][0]+","+center[min][1]);//测试
            //System.out.println("第"+i+"行数据分类到："+min[i]);
        }
        System.out.println(new SimpleDateFormat("聚类算法结束时间"+"yyyyMMddHHmmssSSS").format(new Date()));

        System.out.println("*****************聚类最终结果*************************");
        for(int i=0;i<k;i++){
            System.out.println(center[i][0]+","+center[i][1]);
        }

    }
}


class flff implements Runnable

{
    double []crita;

    int linenumber1;
    int linesize;
    Vector<Vector> trueData1;
    BufferedReader reader1 = null;
    int xcid=1;
    jvff jl;
    Thread t;
    int min=0;
    ArrayList al;
    private Lock lock=new ReentrantLock();
    int iac=0;
    int xcsize=100;
    int xcnum=0;

    public flff() {
        // TODO 自动生成的构造函数存根
        jl=new jvff();
        crita=new double [jl.getK()];

        al=new ArrayList();
        Scanner s1=new Scanner(System.in);
        System.out.println("输入测试数据");
        String file = s1.next();
        System.out.println(new SimpleDateFormat("分类开始时间"+"yyyyMMddHHmmssSSS").format(new Date()));

        System.out.println();
        try {
            reader1 = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e1) {
            // TODO 自动生成的 catch 块
            e1.printStackTrace();
        }
        System.out.println(new SimpleDateFormat("分类数据读入内存时间"+"yyyyMMddHHmmssSSS").format(new Date()));
        String temp1="";
        String[] data1=new String[]{};
        //Double[][] trueData=new Double[100][2];
        trueData1=new Vector();
        Vector<Double> tem1=new Vector<>();
        linenumber1=0;

        try {

            while((temp1=reader1.readLine())!=null)
            {
                data1=temp1.split(",");
                for(int i=0; i<data1.length; i++)
                {
                    tem1.add(Double.valueOf(data1[i]));
                }
                trueData1.add(tem1);
                tem1=new Vector<Double>();

                linenumber1++;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            reader1.close();
        } catch (IOException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        }
        System.out.println(trueData1.size());      //输出行数

/*
* 单线程没问题0线程执行1384次
* 这里想改成多线程
* 多线程出现每个线程全部执行1384次(问题已解决在run方法中循环变量i必须考虑线程安全，和初始问题)
*/

        ExecutorService exec=Executors.newCachedThreadPool();

        for(int i=0;i<3;i++){
            // exec.execute(this);
            t=new Thread(this);
            //System.out.println(Thread.currentThread().getName());
            t.start();
        }
        //exec.shutdown();



    }

    @Override
    public  void run() {

        //进行分类


        while(true) {
            synchronized (this) {

                //lock.lock();
                //计算满意度&&xcnum<xcsize
                if(iac<linenumber1){
                    //System.out.println(Thread.currentThread().getName()+"i=="+iac);

                    for(int j=0;j<jl.getK();j++){

                        //用欧几里得距离公式计算满意度

                        crita[j]=java.lang.StrictMath.pow(((java.lang.StrictMath.pow(((double)trueData1.get(iac).get(0)-jl.center[j][0]),(double)(jl.getK())))+(java.lang.StrictMath.pow(((double)trueData1.get(iac).get(1)-jl.center[j][1]),(double)jl.getK()))),(1.0/jl.getK()));

                        //System.out.println(crita[j]);//测试置信度
                    }
                    //比较满意度，求最小值
                    for(int j=1;j<jl.getK();j++){
                        if(crita[j]<crita[min]){
                            min=j;
                        }
                    }
                    //分类统计
                    al.add(min);



                    //System.out.println(classification[i]);//测试
                }else break;


            }
            iac++;
        }
        //lock.unlock();

        // }
        for(int i=0;i<al.size();i++){
            System.out.println(al.get(i));
        }
        System.out.println("al.size()"+al.size());
        System.out.println(new SimpleDateFormat("分类结束时间"+"yyyyMMddHHmmssSSS").format(new Date()));

    }


    public int getXcid() {
        return xcid;
    }

    public void setXcid(int xcid) {
        this.xcid = xcid;
    }


}

