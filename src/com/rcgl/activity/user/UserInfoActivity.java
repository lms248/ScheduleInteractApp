package com.rcgl.activity.user;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.rcgl.R;
import com.rcgl.activity.basic.MainTabActivity;
import com.rcgl.app.MuApp;
import com.rcgl.app.MyApplication;
import com.rcgl.util.LoadingDialog;
import com.rcgl.util.NetUtil;
import com.rcgl.util.ServerUrlUtil;
import com.rcgl.util.ToastUtils;
import com.rcgl.util.image.AsyncBitmapLoader;
import com.rcgl.util.image.MyImageLoader;
import com.rcgl.util.image.AsyncBitmapLoader.ImageCallBack;
import com.rcgl.util.image.ImageUtils;
/**
 * 用户个人资料信息
 * @author lims
 * @date 2015-03-31
 */
public class UserInfoActivity extends Activity implements OnClickListener {
	/** 标题栏的标题文字 */
	private TextView bar_title;
	/** 标题栏的左端图片 */
	private ImageView bar_image_return;
	/** 标题栏的左端图片添加活动按钮 */
	private ImageView bar_image_submit;
	/** 用户头像 */
	private static ImageView iv_photo;
	/** 用户名 */
	private TextView tv_username;
	/** 修改密码 */
	private TextView tv_change_password;
	/** 昵称编辑框 */
	private EditText et_nickname;
	/** 个性签名编辑框 */
	private EditText et_signature;
	/** 手机号码编辑框 */
	private EditText et_phone;
	/** 邮箱编辑框 */
	private EditText et_email;
	/** 加载效果 */
	private Dialog pd;
	/** http提交数据列表 */
    List<NameValuePair> pairList;
    /** 处理用户头像 */
	static Bitmap bitmap;
    
    /** 用户头像 */
    String userphoto = "";
    /** 当前所登录的用户名（账号）*/
    String username = null;
    /** 用户昵称*/
    String nickname = null;
    /** 签名描述*/
    String signature = null;
    /** 用户手机号 */
    String userPhone = null;
    /** 用户邮箱 */
    String userEmail = null;
    
    static SharedPreferences sharedPreferences;
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        
        MyApplication.initImageLoader(this);
        MuApp.getInstance().addActivity(this);
        
		sharedPreferences = getSharedPreferences("loginUserSP", Context.MODE_PRIVATE);
		
		getEntities();
		initListeners();
		initView();
	    initDatas();
	    
	}
	
	private void getEntities(){
		bar_image_return = (ImageView)findViewById(R.id.bar_image_return);
        bar_image_return.setOnClickListener(return_main);
        bar_image_submit = (ImageView)findViewById(R.id.bar_image_submit);
        bar_image_submit.setOnClickListener(onSubmit);
        iv_photo = (ImageView)findViewById(R.id.image_user);
        tv_username = (TextView)findViewById(R.id.user_tap);
        tv_change_password = (TextView)findViewById(R.id.change_password);
        et_nickname = (EditText)findViewById(R.id.et_nickname);
        et_signature = (EditText)findViewById(R.id.et_signature);
        et_phone = (EditText)findViewById(R.id.et_phone);
        et_email = (EditText)findViewById(R.id.et_email);
	}
	
	private void initListeners(){
		iv_photo.setOnClickListener(this);
		tv_change_password.setOnClickListener(this);
		et_nickname.setOnClickListener(this);
		et_signature.setOnClickListener(this);
		et_phone.setOnClickListener(this);
		et_email.setOnClickListener(this);
	}
	
	private void initView(){
		userphoto = sharedPreferences.getString("userphoto", "");
		username = sharedPreferences.getString("username", "未登录");
		nickname = sharedPreferences.getString("nickname", "");
		signature = sharedPreferences.getString("signature", "");
		userPhone = sharedPreferences.getString("userPhone", "");
		userEmail = sharedPreferences.getString("userEmail", "");
		tv_username.setText("账号："+username);
		et_nickname.setText(nickname);
		et_signature.setText(signature);
		et_phone.setText(userPhone);
		et_email.setText(userEmail);
	}
	
	@Override
	public void onClick(View v) {
		Intent intent = new Intent();
		switch(v.getId()){
		case R.id.image_user://用户头像
			if(!NetUtil.isConnect(getApplicationContext())){
				ToastUtils.showToast(getApplicationContext(), "网络未连接");
				return;
			}
    		intent.setClass(UserInfoActivity.this, HeadPhotoActivity.class);
    		startActivity(intent);
			break;
		case R.id.change_password://修改密码
			if(!NetUtil.isConnect(getApplicationContext())){
				ToastUtils.showToast(getApplicationContext(), "网络未连接");
				return;
			}
			intent.setClass(UserInfoActivity.this, ChangePWActivity.class);
			startActivity(intent);
			break;
		case R.id.et_nickname://昵称
			et_nickname.setFocusableInTouchMode(true);
			break;
		case R.id.et_signature://签名
			et_signature.setFocusableInTouchMode(true);
			break;
		case R.id.et_phone://手机号
			et_phone.setFocusableInTouchMode(true);
			break;
		case R.id.et_email://邮箱
			et_email.setFocusableInTouchMode(true);
			break;
		}
	}
	
	public static void initDatas(){
		/** 异步加载图片并缓存到本地 */
		String userphoto = sharedPreferences.getString("userphoto", "");
		String imageURL = ServerUrlUtil.SERVER_BASCE_URL+"/rcgl/upload/"+userphoto;
		MyImageLoader.loadPhoto(imageURL, iv_photo, 30);
		/*if(userphoto.equals("")){
			iv_photo.setImageResource(R.drawable.default_photo);
		}
		else {
			bitmap=AsyncBitmapLoader.loadBitmap(iv_photo, imageURL, new ImageCallBack() {  
	            @Override  
	            public void imageLoad(ImageView imageView, Bitmap bitmap) {  
	            	bitmap = ImageUtils.getRoundedCornerBitmap(bitmap,30f);
	                imageView.setImageBitmap(bitmap);  
	            }  
	        });
			if(bitmap == null){    
				iv_photo.setImageResource(R.drawable.default_photo);    
	        }    
	        else {    
	        	bitmap = ImageUtils.getRoundedCornerBitmap(bitmap,30f);
	        	iv_photo.setImageBitmap(bitmap);    
	        }
		}*/
		
	}
	
	private void startThread(Runnable r){
		if(!NetUtil.isConnect(getApplicationContext())){
        	ToastUtils.showToast(getApplicationContext(), "网络未连接");
        	return;
        }
		//显示进度框提示
        pd = LoadingDialog.createLoadingDialog(UserInfoActivity.this, "提交修改中…");
        pd.show();
        
        //创建线程
        Thread registerThread = new Thread(r);
        //启动线程
        registerThread.start();
	}
	
	/** 返回主界面 */
    View.OnClickListener return_main = new View.OnClickListener(){
		@Override
    	public void onClick(View view){
			Intent intent=new Intent();
    		intent.setClass(UserInfoActivity.this, MainTabActivity.class);
    		intent.putExtra("page_tab", 3);
    		startActivity(intent);
    		finish();
    	}
    };
    
    /** 提交修改数据 */
    View.OnClickListener onSubmit = new View.OnClickListener(){
		@Override
    	public void onClick(View view){
			if(!NetUtil.isConnect(getApplicationContext())){
            	ToastUtils.showToast(getApplicationContext(), "网络未连接");
            	return;
            }
			if(username.equals("未登录")) username = "";
			NameValuePair pair_username = new BasicNameValuePair("username",username);
			NameValuePair pair_nickname = new BasicNameValuePair("nickname",et_nickname.getText().toString().trim());
            NameValuePair pair_signature = new BasicNameValuePair("signature", et_signature.getText().toString().trim());
            NameValuePair pair_phone = new BasicNameValuePair("phone", et_phone.getText().toString().trim());
            NameValuePair pair_email = new BasicNameValuePair("email", et_email.getText().toString().trim());
            
            pairList = new ArrayList<NameValuePair>();
            pairList.add(pair_username);
            pairList.add(pair_nickname);
            pairList.add(pair_signature);
            pairList.add(pair_phone);
            pairList.add(pair_email);
			startThread(submit_runnable);
    	}
    };
    
    /** 处理发送http请求提交修改数据的线程 */
    Runnable submit_runnable =  new Runnable() {
    	@Override
		public void run() {
    		try
    		{	
                HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList,HTTP.UTF_8);
                // URL使用基本URL即可，其中不需要加参数
                HttpPost httpPost = new HttpPost(ServerUrlUtil.Server_UserInfoManage_URL);
                // 将请求体内容加入请求中
                httpPost.setEntity(requestHttpEntity);
                // 需要客户端对象来发送请求
                HttpClient httpClient = new DefaultHttpClient();
                // 发送请求
                HttpResponse httpResponse = httpClient.execute(httpPost);
                //读返回数据  
                String strResult = EntityUtils.toString(httpResponse.getEntity());
                Log.e("strResult", strResult);
                //若状态码为200 ok 
                if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK&&strResult.equals("1")) {  
                  
                  submitHandler.sendEmptyMessage(200);
                }  
                else {  
                	submitHandler.sendEmptyMessage(0);
                }  
                //只要执行到这里就关闭对话框  
                pd.dismiss(); 
            }
            catch(Exception e) {
                e.printStackTrace();
            }
		}
	};
	
	/**
     * 提交修改数据处理函数
     * @author lims
     * @date 2015-03-31
     */
	Handler submitHandler = new Handler() {
		@Override
		//当有消息发送出来的时候就执行Handler的这个方法 
	    public void handleMessage(Message msg) {
			super.handleMessage(msg);  
			if(msg.what==200){
				ToastUtils.showToast(getApplicationContext(),"修改成功");
				/** 创建异或获取一个已经存在的sharedPreferences对象（单例的）*/
				SharedPreferences sharedPreferences = getSharedPreferences("loginUserSP", Context.MODE_PRIVATE);
				/** 创建数据编辑器 */
				Editor editor = sharedPreferences.edit();
				/** 传递需要保存的数据 */
				editor.putString("nickname", et_nickname.getText().toString().trim());
				editor.putString("signature", et_signature.getText().toString().trim());
				editor.putString("userPhone", et_phone.getText().toString().trim());
				editor.putString("userEmail", et_email.getText().toString().trim());
				/** 保存数据 */
				editor.commit();
				
				Intent intent = new Intent();
	    		intent.setClass(UserInfoActivity.this, MainTabActivity.class);
	    		intent.putExtra("page_tab", 3);
	    		startActivity(intent);
	    		UserInfoActivity.this.finish();
			}
			else {
				ToastUtils.showToast(getApplicationContext(),"修改失败");
			}
            //只要执行到这里就关闭对话框  
            pd.dismiss(); 
	    }
	};

}
