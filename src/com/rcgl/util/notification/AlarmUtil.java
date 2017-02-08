package com.rcgl.util.notification;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class AlarmUtil {
	
	/*AlarmManager确实只能接收一个闹钟，
	在源生的闹钟应用中是这么实现的，每次设定完闹钟之后，
	从数据库里面依次取出各个闹钟，然后计算哪个闹钟是最近的，
	之后将这个闹钟通过PendingIntent发送到AlarmManager中，
	当这个闹钟开始响之后，再执行上述操作，即将更新闹钟的功能放在setNextAlarm（）方法中，
	每次闹钟有更新的时候都调用setNextAlarm（）使AlarmManager里面的闹钟更新*/
	
	public static void initAlarm(Context context, String time, String msg, int id) {
		//创建Intent对象，action为ELITOR_CLOCK，附加信息为字符串“你该打酱油了”
		Intent intent = new Intent("MyNotification");
		intent.putExtra("time",time); 
		intent.putExtra("msg",msg); 
		intent.putExtra("id", id);

		//定义一个PendingIntent对象，PendingIntent.getBroadcast包含了sendBroadcast的动作。
		//也就是发送了action 为"ELITOR_CLOCK"的intent
		PendingIntent pi = PendingIntent.getBroadcast(context,id,intent,0);  

		//AlarmManager对象,注意这里并不是new一个对象，Alarmmanager为系统级服务
		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);  
		
		//先把字符串转成Date类型
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	    Date date;
		long longDate = 0;
		try {
			date = sdf.parse(time);
			//获取毫秒数
			longDate = date.getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		alarmManager.set(AlarmManager.RTC_WAKEUP, longDate, pi);
		
		//alarmManager.cancel(pi);//取消闹钟
	}
}



