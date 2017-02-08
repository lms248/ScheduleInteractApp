package com.rcgl.util;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneUtil {

	public static String getImei(Context context) {
		TelephonyManager telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String imei = telephonyManager.getDeviceId();
		Log.i("id", "id="+imei);
		return imei;
	}
}
