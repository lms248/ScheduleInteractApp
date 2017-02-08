package com.rcgl.util;

import com.rcgl.R;
/**
 * 日期样式管理
 * @author lims
 * @date 2015-04-12
 */
public class DayManage {
	/**
	 * 根据没用对应的天显示对应数字图片
	 * @param day
	 * @return int
	 * @author lims
	 * @date 2015-04-12
	 */
	public static int showDayImage(String day){
		int d=Integer.valueOf(day);
		switch(d){
		case 1: return R.drawable.day_bg_1;
		case 2: return R.drawable.day_bg_2;
		case 3: return R.drawable.day_bg_3;
		case 4: return R.drawable.day_bg_4;
		case 5: return R.drawable.day_bg_5;
		case 6: return R.drawable.day_bg_6;
		case 7: return R.drawable.day_bg_7;
		case 8: return R.drawable.day_bg_8;
		case 9: return R.drawable.day_bg_9;
		case 10: return R.drawable.day_bg_10;
		case 11: return R.drawable.day_bg_11;
		case 12: return R.drawable.day_bg_12;
		case 13: return R.drawable.day_bg_13;
		case 14: return R.drawable.day_bg_14;
		case 15: return R.drawable.day_bg_15;
		case 16: return R.drawable.day_bg_16;
		case 17: return R.drawable.day_bg_17;
		case 18: return R.drawable.day_bg_18;
		case 19: return R.drawable.day_bg_19;
		case 20: return R.drawable.day_bg_20;
		case 21: return R.drawable.day_bg_21;
		case 22: return R.drawable.day_bg_22;
		case 23: return R.drawable.day_bg_23;
		case 24: return R.drawable.day_bg_24;
		case 25: return R.drawable.day_bg_25;
		case 26: return R.drawable.day_bg_26;
		case 27: return R.drawable.day_bg_27;
		case 28: return R.drawable.day_bg_28;
		case 29: return R.drawable.day_bg_29;
		case 30: return R.drawable.day_bg_30;
		case 31: return R.drawable.day_bg_31;
		}
		return 0;
	}
}
