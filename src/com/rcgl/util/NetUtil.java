package com.rcgl.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/***
 * 工具类，检查当前网络状态
 * @author lims
 * @date 2015-04-11
 */
public class NetUtil {

    public static boolean isConnect(Context context) {

        // 获取手机所以连接管理对象（包括wi-fi，net等连接的管理）
    	try { 
	        ConnectivityManager connectivity = (ConnectivityManager) context 
	                .getSystemService(Context.CONNECTIVITY_SERVICE); 
	        if (connectivity != null) { 
	            // 获取网络连接管理的对象 
	            NetworkInfo info = connectivity.getActiveNetworkInfo(); 
	            if (info != null&& info.isConnected()) { 
	                // 判断当前网络是否已经连接 
	                if (info.getState() == NetworkInfo.State.CONNECTED) { 
	                    return true; 
	                } 
	            } 
	        } 
	    } catch (Exception e) { 
	    	// TODO: handle exception 
	    Log.v("error",e.toString()); 
	    } 
        return false;
    }
    
    /**
	 * 判断当前网络是否为wifi网络
	 * @param mContext
	 * @return
	 */
	public static boolean isWifi(Context context) {  
	    ConnectivityManager connectivityManager = (ConnectivityManager) context  
	            .getSystemService(Context.CONNECTIVITY_SERVICE);  
	    NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();  
	    if (activeNetInfo != null  
	            && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {  
	        return true;  
	    }  
	    return false;  
	}
    
}