package com.rcgl.app;



import java.util.ArrayList;
import java.util.List;

import com.gjj.pm.lib.task.ForegroundTaskExecutor;

import android.app.Activity;
import android.app.Application;

public class MuApp extends Application{
	
    private static MuApp mApp;
    
    public static MuApp getInstance() {
        return mApp;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;


    }
    public void killApp() {
        ForegroundTaskExecutor.executeTask(new Runnable() {
            @Override
            public void run() {
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
    }
    /**打开的activity**/
    private List<Activity> activities = new ArrayList<Activity>();
 

    /**
    * 新建了一个activity
    * @param activity
    */
    public void addActivity(Activity activity){
    activities.add(activity);
    } 
    /**
    *  结束指定的Activity
    * @param activity
    */
    public void finishActivity(Activity activity){
    if (activity!=null) {
    this.activities.remove(activity);
    activity.finish();
    activity = null;
    }
    } 
    /**
    * 应用退出，结束所有的activity
    */
    public void exit(){
    for (Activity activity : activities) {
    if (activity!=null) {
    activity.finish();
    }
    }
    System.exit(0);
    } 
}
