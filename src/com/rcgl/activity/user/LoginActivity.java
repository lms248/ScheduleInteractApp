package com.rcgl.activity.user;

import java.io.FileOutputStream;
import java.io.IOException;
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
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.rcgl.R;
import com.rcgl.activity.basic.MainTabActivity;
import com.rcgl.app.MuApp;
import com.rcgl.util.LoadingDialog;
import com.rcgl.util.NetUtil;
import com.rcgl.util.ServerUrlUtil;
import com.rcgl.util.ToastUtils;

public class LoginActivity extends Activity implements OnClickListener{
	/** 登录按钮 */
	private Button mLoginBtn;
	/** 注册页面跳转按钮 */
	private TextView mRegister;
	/** 取回密码跳转按钮 */
	private TextView mGetPassword;
	/** 处理中状态图标 */
	private Dialog pd;
	/** 账号输入框 */
	private EditText mUsername;
	/** 密码输入框 */
	private EditText mPassword;
	/** 登录用户的id */
	private int uid;
	/** 标题栏的标题文字 */
	private TextView bar_title;
	/** 标题栏的返回图片 */
	private ImageView bar_image_back;
	/** http请求参数 */
    List<NameValuePair> pairList;	
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        MuApp.getInstance().addActivity(this);
        
        getEntities();
        initListeners();
        initDatas();
        
	}
	
	private void getEntities(){
		bar_image_back = (ImageView)findViewById(R.id.bar_image_back);
        mUsername = (EditText)findViewById(R.id.lg_username);
        mPassword = (EditText)findViewById(R.id.lg_password);
        mLoginBtn = (Button)findViewById(R.id.loginBtn);
        mRegister = (TextView)findViewById(R.id.lg_register); 
	}
	
	private void initListeners(){
		bar_image_back.setOnClickListener(this);
        mLoginBtn.setOnClickListener(this);
        mRegister.setOnClickListener(this);
	}
	
	private void initDatas(){
		//将response返回数据写入到资源文件中
        try {
			writeFile("myschedule.txt","");
		} catch (IOException e) {
			e.printStackTrace();
		}
        try {
			writeFile("myfriend.txt","");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onClick(View v) {
		Intent intent=new Intent();
		switch(v.getId()) {
		case R.id.bar_image_back://返回
    		intent.setClass(LoginActivity.this, MainTabActivity.class);
    		intent.putExtra("page_tab", 3);
    		startActivity(intent);
    		LoginActivity.this.finish();
			break;
		case R.id.loginBtn://登录
			onLogin();
			break;
		case R.id.lg_register://转注册界面
    		intent.setClass(LoginActivity.this, RegisterActivity.class);
    		startActivity(intent);
    		LoginActivity.this.finish();
			break;
		}
		
	}
	
	private void onLogin(){
		Log.i("http", "Post request Login");
		
		//获取用户名和密码
		String username = mUsername.getText().toString().trim();
		String password = mPassword.getText().toString().trim();
		
		if(username.equals("")||password.equals("")){
			ToastUtils.showToast(getApplicationContext(), "用户名和密码不能为空！");
        }
		else {
			NameValuePair pair_name = new BasicNameValuePair("username",username);
            NameValuePair pair_psw = new BasicNameValuePair("password", password);
            
            pairList = new ArrayList<NameValuePair>();
            pairList.add(pair_name);
            pairList.add(pair_psw);
            
            if(!NetUtil.isConnect(getApplicationContext())){
            	ToastUtils.showToast(getApplicationContext(), "网络未连接");
            	return;
            }
            //显示进度框提示
            pd = LoadingDialog.createLoadingDialog(LoginActivity.this, "玩命登录中，马上就好…");
            pd.show();
            
            //创建线程
            Thread loginThread = new Thread(login_runnable);
            //启动线程
            loginThread.start();
		}
	}
    
    /** http post方式发送用户输入数据进行登录请求 */
    Runnable login_runnable =  new Runnable() {
    	@Override
		public void run() {
    		try
            {	
                HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList,HTTP.UTF_8);
                HttpPost httpPost = new HttpPost(ServerUrlUtil.Server_Login_URL);
                // 将请求体内容加入请求中
                httpPost.setEntity(requestHttpEntity);
                // 需要客户端对象来发送请求
                HttpClient httpClient = new DefaultHttpClient();
                // 发送请求
                HttpResponse httpResponse = httpClient.execute(httpPost);
                //读返回数据  
                String strResult = EntityUtils.toString(httpResponse.getEntity());  
                Log.i("login", strResult);
                //若状态码为200 ok 
                if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK && !strResult.equals("-1"))   
                {  
                  
                  LoginHandler.sendEmptyMessage(200);
                  
                  JSONObject jsonObject = new JSONObject(strResult);
                  JSONArray jsonArray = jsonObject.getJSONArray("user");
                  Log.i("userinfo", jsonArray.getString(0));
                  JSONObject jsonObject2 = new JSONObject();
                  jsonObject2 = jsonArray.getJSONObject(0);
                  Log.i("jsonObject2", jsonObject2.getString("username"));
                  
                  String id = jsonObject2.getString("id");
                  uid = Integer.valueOf(id);//登录用户的id
                  String registerTime = jsonObject2.getString("time");
                  String schedule_object = jsonObject2.getString("schedule_object");
                  String friend_object = jsonObject2.getString("friend_object");
                  String activities_object = jsonObject2.getString("activities_object");
                  Log.i("schedule_object", schedule_object);
                  Log.i("friend_object", friend_object);
                  Log.i("activities_object", activities_object);
                  
                  //将response返回数据写入到资源文件中
                  writeFile("myschedule.txt",schedule_object);
                  writeFile("myfriend.txt",friend_object);
                  writeFile("myactivities.txt",activities_object);
                  
                  /** 创建异或获取一个已经存在的sharedPreferences对象（单例的）*/
  				  SharedPreferences sharedPreferences = getSharedPreferences("loginUserSP", Context.MODE_PRIVATE);
  				  /** 创建数据编辑器 */
  				  Editor editor = sharedPreferences.edit();
  				  /** 传递需要保存的数据 */
  				  editor.putInt("userid", uid);
  				  editor.putString("username", mUsername.getText().toString().trim());
  				  editor.putString("password", mPassword.getText().toString().trim());
  				  editor.putString("userphoto", jsonObject2.getString("photo"));
  				  editor.putString("nickname", jsonObject2.getString("nickname")+"");
  				  editor.putString("signature", jsonObject2.getString("signature")+"");
  				  editor.putString("userPhone", jsonObject2.getString("userPhone")+"");
  				  editor.putString("userEmail", jsonObject2.getString("userEmail")+"");
  				  
  				  /** 保存数据 */
  				  editor.commit();
                }  
                else  
                {  
                	LoginHandler.sendEmptyMessage(0);
                } 
                
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
		}
	};
	
	/**
     * 登录处理函数
     * @author lims
     * @date 2015-02-03
     */
	Handler LoginHandler = new Handler() {
		@Override
		//当有消息发送出来的时候就执行Handler的这个方法 
	    public void handleMessage(Message msg) {
			super.handleMessage(msg);  
			//Toast.makeText(getApplicationContext(), msg.toString(), Toast.LENGTH_SHORT).show();
			if(msg.what==200){
				ToastUtils.showToast(getApplicationContext(), "登录成功");
				
				Intent intent = new Intent();
	    		intent.setClass(LoginActivity.this, MainTabActivity.class);
	    		/*intent.putExtra("user_photo", R.drawable.icon_long);
	    		intent.putExtra("user_tap", "lms");*/
	    		intent.putExtra("page_tab", 3);
	    		startActivity(intent);
	    		LoginActivity.this.finish();
			}
			else {
				ToastUtils.showToast(getApplicationContext(), "登录失败！用户名或密码错误…");
			}
            //只要执行到这里就关闭对话框  
            pd.dismiss(); 
	    }
	};
    
	
    /** 写数据到资源文件 */  
	private void writeFile(String fileName,String data) throws IOException{   
	  try{   
	  
	        FileOutputStream fout = openFileOutput(fileName, Context.MODE_PRIVATE);   
	  
	        byte [] bytes = data.getBytes();   
	  
	        fout.write(bytes);   
	  
	        fout.close();   
	      }   
	  
	        catch(Exception e){   
	        e.printStackTrace();   
	       }   
	}

}
