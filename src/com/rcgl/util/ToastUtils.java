package com.rcgl.util;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

public class ToastUtils {
	
	public static void showToast(Context context,String text){
		if( null == context|| TextUtils.isEmpty(text)){
			return;
		}
	 Toast.makeText(context,text,Toast.LENGTH_SHORT).show();
	}
}
