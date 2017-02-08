package com.rcgl.util.notification;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.rcgl.R;
import com.rcgl.activity.basic.MainTabActivity;

public class NotificationUtil{
	
	public static void initNotification(Context context, String time, String msg, int id){
		//创建Intent对象，action为ELITOR_CLOCK，附加信息为字符串“你该打酱油了”
		/*Intent intent = new Intent("MyNotification");
		intent.putExtra("time",time); 
		intent.putExtra("msg",msg); */ 

		//定义一个PendingIntent对象，PendingIntent.getBroadcast包含了sendBroadcast的动作。
		//也就是发送了action 为"ELITOR_CLOCK"的intent
		//PendingIntent pi = PendingIntent.getBroadcast(context,0,intent,0);  

		//AlarmManager对象,注意这里并不是new一个对象，Alarmmanager为系统级服务
		NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);  
		
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
		Notification notification = new Notification(R.drawable.heart,msg,longDate);
		//添加振动
		long[] vibrate = {0,300,200,500}; 
		notification.vibrate = vibrate;
		//添加LED灯提醒
		notification.defaults |= Notification.DEFAULT_LIGHTS;
		
		Intent notificationIntent = new Intent(context,MainTabActivity.class); //点击该通知后要跳转的Activity
		PendingIntent contentIntent = PendingIntent.getActivity(context,id,notificationIntent,0);
		notification.setLatestEventInfo(context, time, msg, contentIntent);

		//把Notification传递给NotificationManager
		notificationManager.notify(id,notification);
		 
		//notificationManager.set(AlarmManager.RTC_WAKEUP, longDate, pi);
	}
}
