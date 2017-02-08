package com.rcgl.activity.basic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcgl.R;
import com.rcgl.app.MuApp;
import com.rcgl.app.MyApplication;

public class WelcomeActivity extends Activity {
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // 去除标题  必须在setContentView()方法之前调用
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); // 设置全屏

        setContentView(R.layout.activity_welcome);
        MuApp.getInstance().addActivity(this);
        
        /*MyApplication.initImageLoader(getApplicationContext());
        ImageLoader.getInstance().clearMemoryCache();
		ImageLoader.getInstance().clearDiskCache();*/
        
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				Intent intent = new Intent();
	    		intent.setClass(WelcomeActivity.this, MainTabActivity.class);
	    		startActivity(intent);
	    		WelcomeActivity.this.finish();
			}
		}, 2000);
        
	}
	
	
}
