package com.luqi.timer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class TimerManager {
	
	public TimerManager() {
	
		// TODO Auto-generated constructor stub
	}

	//时间间隔  
	public static void main(String[] args) {
		//new TimerManager();
		//new TimerManager(1);
	}
	
    private static final long PERIOD_DAY = 24 * 60 * 60 * 1000;  
    public TimerManager(Boolean flag) {  
         Calendar calendar = Calendar.getInstance();   
                  
         /*** 定制每日9:00执行方法 ***/  
  
         calendar.set(Calendar.HOUR_OF_DAY, 9);  
         calendar.set(Calendar.MINUTE, 0);  
         calendar.set(Calendar.SECOND, 0);  
            
         Date date=calendar.getTime(); //第一次执行定时任务的时间  
        
         //System.out.println("before 方法比较："+date.before(new Date()));  
         //如果第一次执行定时任务的时间 小于 当前的时间  
         //此时要在 第一次执行定时任务的时间 加一天，以便此任务在下个时间点执行。如果不加一天，任务会立即执行。循环执行的周期则以当前时间为准  
         if (date.before(new Date())&&!flag) {  
             date = this.addDay(date, 1);  
            // System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));  
         }  
         System.out.println("第一次执行早上9点的程序的时间是："+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));     
		/*
		 * Timer timer = new Timer();
		 * 
		 * AMTimerTask task = new AMTimerTask(0); //安排指定的任务在指定的时间开始进行重复的固定延迟执行。
		 * timer.schedule(task,date,PERIOD_DAY);
		 */
        }  
  
        // 增加或减少天数  
        public Date addDay(Date date, int num) {  
         Calendar startDT = Calendar.getInstance();  
         startDT.setTime(date);  
         startDT.add(Calendar.DAY_OF_MONTH, num);  
         return startDT.getTime();  
        }


    public TimerManager(int i, Boolean flag) {
        Calendar calendar = Calendar.getInstance();

        /*** 定制每日01:00执行方法 ***/

        calendar.set(Calendar.HOUR_OF_DAY, 1);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        Date date = calendar.getTime(); //第一次执行定时任务的时间
        // System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
        //  System.out.println("before 方法比较："+date.before(new Date()));
        //如果第一次执行定时任务的时间 小于 当前的时间
        //此时要在 第一次执行定时任务的时间 加一天，以便此任务在下个时间点执行。如果不加一天，任务会立即执行。循环执行的周期则以当前时间为准

        if (date.before(new Date()) && !flag) {
            date = this.addDay(date, 1);
            // System.out.println(date);
        }
        System.out.println("第一次执行1点的程序的时间是：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));

        Timer timer = new Timer();

        MyTask task = new MyTask(); //安排指定的任务在指定的时间开始进行重复的固定延迟执行。
        timer.schedule(task, date, PERIOD_DAY);

    }

    /**
     * 获取日志
     */
    public static void getLogManager() {

        Calendar calendar = Calendar.getInstance();

        /*** 定制每日12:00执行方法 ***/

        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        //如果第一次执行定时任务的时间 小于 当前的时间
        //此时要在 第一次执行定时任务的时间 加一天，以便此任务在下个时间点执行。如果不加一天，任务会立即执行。循环执行的周期则以当前时间为准
        if (calendar.getTime().before(new Date())) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);

        }
        System.out.println("第一次执行早上12点的程序的时间是：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime()));
        Timer timer = new Timer();
        TimerTask task = new LogTask();
        timer.schedule(task, calendar.getTime(), PERIOD_DAY);

    }

    /**
     * 获取日志
     */
    public static void getLogManagerNew() {

        Calendar calendar = Calendar.getInstance();

        /*** 定制每日21:00执行方法 ***/

        calendar.set(Calendar.HOUR_OF_DAY, 21);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        //如果第一次执行定时任务的时间 小于 当前的时间
        //此时要在 第一次执行定时任务的时间 加一天，以便此任务在下个时间点执行。如果不加一天，任务会立即执行。循环执行的周期则以当前时间为准
        if (calendar.getTime().before(new Date())) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);

        }
        System.out.println("第一次执行下午10点的程序的时间是：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime()));
        Timer timer = new Timer();
        TimerTask task = new LogTask();
        timer.schedule(task, calendar.getTime(), PERIOD_DAY);

    }


        public TimerManager(Boolean flag,int type) {  
            Calendar calendar = Calendar.getInstance();   
                     
            /*** 定制每日23:00执行方法 ***/
     
            calendar.set(Calendar.HOUR_OF_DAY,20);  
            calendar.set(Calendar.MINUTE, 0);  
            calendar.set(Calendar.SECOND, 0);  
               
            Date date=calendar.getTime(); //第一次执行定时任务的时间  
           
            //System.out.println("before 方法比较："+date.before(new Date()));  
            //如果第一次执行定时任务的时间 小于 当前的时间  
            //此时要在 第一次执行定时任务的时间 加一天，以便此任务在下个时间点执行。如果不加一天，任务会立即执行。循环执行的周期则以当前时间为准  
            if (date.before(new Date())&&!flag) {  
                date = this.addDay(date, 1);  
               // System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));  
            }  
            System.out.println("第一次执行晚上20点00分的程序的时间是："+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));     
		
		  Timer timer = new Timer();
		  
		  BoardTask task = new BoardTask();
		  //安排指定的任务在指定的时间开始进行重复的固定延迟执行。 
		  timer.schedule(task,date,PERIOD_DAY);
		 
           }

        /**
         * @Author Sherry
         * @Description 每日定时计算趋势图
         * @Date 4:19 PM 2020/1/6
        */
        public static void boardTaskTrendTask(){
            Calendar calendar = Calendar.getInstance();

            /*** 定制每日00:00执行方法 ***/
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY,0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            Timer timer = new Timer();
            BoardTaskTrendTask boardTaskTrendTask = new BoardTaskTrendTask();
            timer.scheduleAtFixedRate(boardTaskTrendTask,calendar.getTime(),PERIOD_DAY);
        }
        
}
