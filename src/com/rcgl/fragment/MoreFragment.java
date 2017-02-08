package com.rcgl.fragment;

import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcgl.R;
import com.rcgl.activity.basic.MainTabActivity;
import com.rcgl.activity.chat.ChatActivity;
import com.rcgl.activity.message.MessageActivity;
import com.rcgl.activity.nearby.NearbyActivity;
import com.rcgl.activity.user.LoginActivity;
import com.rcgl.activity.user.UserInfoActivity;
import com.rcgl.app.MuApp;
import com.rcgl.app.MyApplication;
import com.rcgl.util.DataCleanManager;
import com.rcgl.util.NetUtil;
import com.rcgl.util.ServerUrlUtil;
import com.rcgl.util.ToastUtils;
import com.rcgl.util.image.ImageUtils;
import com.rcgl.util.image.MyImageLoader;
/** 
 * 更多模块
 * @author lims
 * @date 2015-04-08
 */
public class MoreFragment extends Fragment implements OnClickListener {
	/** 用户头像 */
	private ImageView user_photo = null;
	/** 用户名（账号） */
	private TextView tv_username = null;
	/** 用户昵称 */
	private TextView tv_nickname = null;
	/** 注销登录按钮 */
	private TextView exit_login = null;
	/** 标题栏的标题文字 */
	private TextView bar_title;
	/** 标题栏的查询日程图标 */
	private ImageView bar_search;
	/** 标题栏的更多图标 */
	private ImageView iv_setting;
	/** 消息栏 */
	private LinearLayout messageLayout;
	/** 消息图标 */
	private ImageView message_image;
	/** 消息文字 */
	private TextView message_tap;
	/** 实时test */
	private LinearLayout linearLayout_socket;
	/** 附近的人 */
	private LinearLayout linearLayout_location;
	/** 清除缓存 */
	private LinearLayout linearLayout_delete_cache;
	/** 处理用户头像 */
	Bitmap bitmap;
	/** 是否已登录 */
	Boolean isLogin = false;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {	
		return inflater.inflate(R.layout.fragment_more, null);		
	}	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		MyApplication.initImageLoader(getActivity());
		MuApp.getInstance().addActivity(getActivity());
		
		getEntities();
		initListeners();
		initView();
		initDatas();
		
	}
	
	private void getEntities(){
		bar_title = (TextView)getActivity().findViewById(R.id.bar_title);
		bar_search = (ImageView)getActivity().findViewById(R.id.bar_search);
		iv_setting = (ImageView)getActivity().findViewById(R.id.bar_image);
		
		user_photo = (ImageView)getActivity().findViewById(R.id.image_user);
		tv_username = (TextView)getActivity().findViewById(R.id.tv_username);
		tv_nickname = (TextView)getActivity().findViewById(R.id.tv_nickname);
		exit_login = (TextView)getActivity().findViewById(R.id.exit_login);
		message_image = (ImageView)getActivity().findViewById(R.id.iv_message);
		message_tap = (TextView)getActivity().findViewById(R.id.tv_message);
		messageLayout = (LinearLayout)getActivity().findViewById(R.id.linearLayout_message);
		linearLayout_socket = (LinearLayout)getActivity().findViewById(R.id.linearLayout_socket);
		linearLayout_location = (LinearLayout)getActivity().findViewById(R.id.linearLayout_location);
		linearLayout_delete_cache = (LinearLayout)getActivity().findViewById(R.id.linearLayout_delete_cache);
	}
	
	private void initListeners(){
		iv_setting.setOnClickListener(this);
		
		user_photo.setOnClickListener(this);
		tv_username.setOnClickListener(this);
		tv_nickname.setOnClickListener(this);
		exit_login.setOnClickListener(this);
		message_image.setOnClickListener(this);
		message_tap.setOnClickListener(this);
		messageLayout.setOnClickListener(this);
		linearLayout_socket.setOnClickListener(this);
		linearLayout_location.setOnClickListener(this);
		linearLayout_delete_cache.setOnClickListener(this);
	}
	
	private void initView(){
		getActivity().findViewById(R.id.bar).setVisibility(View.VISIBLE);
		bar_title.setText("更多");
		bar_search.setVisibility(View.GONE);
		iv_setting.setImageResource(R.drawable.ic_setting);
		
		exit_login.setVisibility(View.INVISIBLE);
		user_photo.setClickable(true);
		user_photo.setFocusable(true);
		tv_username.setClickable(true);
		tv_username.setFocusable(true);
		tv_nickname.setClickable(true);
		tv_nickname.setFocusable(true);
		message_image.setClickable(true);
		message_image.setFocusable(true);
		message_tap.setClickable(true);
		message_tap.setFocusable(true);
	}
	
	private void initDatas(){
		SharedPreferences sp = getActivity().getSharedPreferences("loginUserSP", Context.MODE_PRIVATE);
		String userphoto = sp.getString("userphoto", "");
		String username = sp.getString("username", "未登录");
		String nickname = sp.getString("nickname", "");
		if(!username.equals("未登录")){
			if(nickname.equals("")){
				tv_nickname.setText("未设置昵称");
			}
			else tv_nickname.setText(nickname);
			tv_username.setText("账号："+username);
			exit_login.setVisibility(View.VISIBLE);
			isLogin = true;
			
			if(!NetUtil.isConnect(getActivity())){
				ToastUtils.showToast(getActivity(),"网络未连接");
				user_photo.setImageResource(R.drawable.default_photo);
				return;
			}
			/** 异步加载图片并缓存到本地 */
			String imageURL = ServerUrlUtil.SERVER_BASCE_URL+"/rcgl/upload/"+userphoto;
			MyImageLoader.loadPhoto(imageURL, user_photo, 30);
			//Toast.makeText(getActivity(), imageURL, 0).show();
			/*if(userphoto.equals("")){
				Bitmap bitmap = BitmapFactory.decodeResource(getActivity().getResources(),R.drawable.default_photo); 
				bitmap = ImageUtils.getRoundedCornerBitmap(bitmap,30f);
				user_photo.setImageBitmap(bitmap);
			}
			else {
				bitmap=AsyncBitmapLoader.loadBitmap(user_photo, imageURL, new ImageCallBack() {  
	                @Override  
	                public void imageLoad(ImageView imageView, Bitmap bitmap) { 
	                	bitmap = ImageUtils.getRoundedCornerBitmap(bitmap,30f);
	                    imageView.setImageBitmap(bitmap);  
	                }  
	            });
				if(bitmap == null){    
	            	user_photo.setImageResource(R.drawable.default_photo);    
	            }    
	            else {    
	            	bitmap = ImageUtils.getRoundedCornerBitmap(bitmap,30f);
	            	user_photo.setImageBitmap(bitmap);    
	            }
			}*/
		}
		else {
			Bitmap bitmap = BitmapFactory.decodeResource(getActivity().getResources(),R.drawable.default_photo); 
			bitmap = ImageUtils.getRoundedCornerBitmap(bitmap,30f);
			user_photo.setImageBitmap(bitmap);
			isLogin = false;
		}
	}
	
	@Override
	public void onClick(View v) {
		Intent intent = new Intent();
		switch(v.getId()) {
		case R.id.bar_image://定位
			ToastUtils.showToast(getActivity(), "该功能尚未开放");
			break;
		case R.id.image_user://用户头像
		case R.id.tv_username://用户名
		case R.id.tv_nickname://用户昵称
			if(!NetUtil.isConnect(getActivity())){
				ToastUtils.showToast(getActivity(),"网络未连接");
				return;
			}
			if(isLogin){
				intent.setClass(getActivity(), UserInfoActivity.class);
	    		startActivity(intent);
	    		//getActivity().finish();
			}
			else {
	    		intent.setClass(getActivity(), LoginActivity.class);
	    		startActivity(intent);
	    		//getActivity().finish();
			}
    		break;
		case R.id.exit_login://注销登录
			final Dialog exitDialog = new Dialog(getActivity(), R.style.option_dialog);
			exitDialog.setContentView(R.layout.dialog_confirm);
            ((TextView) exitDialog.findViewById(R.id.dialog_title)).setText("确定注销登录吗？");
            Button btn_exit_yes = (Button) exitDialog.findViewById(R.id.btn_yes);
            btn_exit_yes.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					exitLogin();//注销登录
					exitDialog.dismiss();
				}
			});
            Button btn_exit_no = (Button) exitDialog.findViewById(R.id.btn_no);
            btn_exit_no.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					exitDialog.dismiss();
				}
			});
            exitDialog.show();
			break;
		case R.id.iv_message://消息栏图标
		case R.id.tv_message://消息栏文字
		case R.id.linearLayout_message://消息栏
    		intent.setClass(getActivity(), MessageActivity.class);
    		startActivity(intent);
    		break;
		case R.id.linearLayout_socket://socketest
			intent.setClass(getActivity(), ChatActivity.class);
			startActivity(intent);
			break;
		case R.id.linearLayout_location://附近的人
			if(!NetUtil.isConnect(getActivity())){
				ToastUtils.showToast(getActivity(), "网络未连接");
				return;
			}
			if(tv_username.getText().toString().equals("未登录")){
				ToastUtils.showToast(getActivity(), "你还未登录哦，先去登录再来？");
				return;
			}
			intent.setClass(getActivity(), NearbyActivity.class);
			startActivity(intent);
			break;
		case R.id.linearLayout_delete_cache://清除缓存
			final Dialog clearDialog = new Dialog(getActivity(), R.style.option_dialog);
			clearDialog.setContentView(R.layout.dialog_confirm);
            ((TextView) clearDialog.findViewById(R.id.dialog_title)).setText("你真的要清理缓存吗？");
            Button btn_clear_yes = (Button) clearDialog.findViewById(R.id.btn_yes);
            btn_clear_yes.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ImageLoader.getInstance().clearMemoryCache();		// 清除内存缓存
					ImageLoader.getInstance().clearDiskCache();	// 清除SD卡中的缓存
					ToastUtils.showToast(getActivity(), "缓存清理完成");
					clearDialog.dismiss();
				}
			});
            Button btn_clear_no = (Button) clearDialog.findViewById(R.id.btn_no);
            btn_clear_no.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					clearDialog.dismiss();
				}
			});
            clearDialog.show();
			break;
		}
	}
    
	/** 注销登录 */
	private void exitLogin(){
		/** 创建异或获取一个已经存在的sharedPreferences对象（单例的）*/
		SharedPreferences sharedPreferences = getActivity().getSharedPreferences("loginUserSP", Context.MODE_PRIVATE);
		/** 创建数据编辑器 */
		Editor editor = sharedPreferences.edit();
		/** 传递需要保存的数据 */
		editor.putInt("userid", 0);
		editor.putString("username", "未登录");
		editor.putString("password", "");
		editor.putString("userphoto", "");
		editor.putString("nickname", "");
		editor.putString("signature", "");
		editor.putString("userPhone", "");
		editor.putString("userEmail", "");
		/** 保存数据 */
		editor.commit();
		
		user_photo.setClickable(true);
		user_photo.setFocusable(true);
		tv_username.setClickable(true);
		tv_username.setFocusable(true);
		
		//将response返回数据写入到资源文件中
        try {
			writeFile("myschedule.txt","");
			writeFile("myfriend.txt","");
			writeFile("myactivities.txt","");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
        DataCleanManager.cleanFiles(getActivity());
        DataCleanManager.cleanExternalCache(getActivity());
        
		Intent intent = new Intent();
		intent.setClass(getActivity(), MainTabActivity.class);
		intent.putExtra("page_tab", 3);
		startActivity(intent);
		getActivity().finish();
	}
	
    /** 写数据到资源文件 */  
	private void writeFile(String fileName,String data) throws IOException{  
		try{
			FileOutputStream fout = getActivity().openFileOutput(fileName, Context.MODE_PRIVATE);   
			byte [] bytes = data.getBytes();   
			fout.write(bytes);   
			fout.close();
		}   
		catch(Exception e){
			e.printStackTrace();   
		}
	}
	
}
