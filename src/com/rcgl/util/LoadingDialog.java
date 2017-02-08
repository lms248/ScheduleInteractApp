package com.rcgl.util;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rcgl.R;

/**
 * 加载动画
 * @author lims
 * @date 2015-02-03
 */
public class LoadingDialog {
	private static Dialog loadingDialog;
	/**
	 * 得到自定义的progressDialog
	 * @param context
	 * @param msg 显示的加载文字
	 * @return
	 */
	public static Dialog createLoadingDialog(Context context, String msg) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.loading_dialog, null);// 得到加载view
		LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialog_view);// 加载布局
		// loading_dialog.xml中的ImageView
		ImageView spaceshipImage = (ImageView) v.findViewById(R.id.img_loading);
		TextView tipTextView = (TextView) v.findViewById(R.id.tipTextView);// 提示文字
		// 加载动画
		Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(
				context, R.anim.loading_animation);
		// 使用ImageView显示动画
		spaceshipImage.startAnimation(hyperspaceJumpAnimation);
		tipTextView.setText(msg);// 设置加载信息

		Dialog loadingDialog = new Dialog(context, R.style.loading_dialog);// 创建自定义样式dialog

		loadingDialog.setCancelable(true);// 可以用“返回键”取消
		loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.FILL_PARENT));// 设置布局
		return loadingDialog;

	}
	
	/**
	 * 显示加载动画
	 * @param msg 显示的加载文字
	 * @author lims
	 * @date 2015-04-12
	 */
	public static void showLoadingDialog(Context context, String msg){
		loadingDialog = LoadingDialog.createLoadingDialog(context, msg);
		loadingDialog.show();
	}
	
	/**
	 * 关闭加载动画
	 * @author lims
	 * @date 2015-04-12 
	 */
	public static void closeLoadingDialog(){
		loadingDialog.dismiss();
	}
}
