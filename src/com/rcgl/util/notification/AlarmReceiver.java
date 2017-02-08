package com.rcgl.util.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
	//NotificationHandler nHandler;

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.d("MyTag", "onclock......................");
		String time = intent.getStringExtra("time");
		String msg = intent.getStringExtra("msg");
		int id = intent.getIntExtra("id", 0);
		Toast.makeText(context,msg,Toast.LENGTH_SHORT).show();
		
		/*nHandler = NotificationHandler.getInstance(context);
		nHandler.createSimpleNotification(context,time,msg);*/
		NotificationUtil.initNotification(context, time, msg, id);
	}
	
}
