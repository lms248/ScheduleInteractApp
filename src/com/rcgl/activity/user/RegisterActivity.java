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

import android.app.Activity;
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

/**
 * 用户注册
 * @author lims
 * @date 2015-02-03
 */
public class RegisterActivity extends Activity implements OnClickListener {
	private EditText mUsername;
	private EditText mPassword;
	private EditText mRepassword;
	private Button registerBtn;
	private TextView mLogin;
	/** 注册用户的id */
	private int uid;
	/** 标题栏的标题文字 */
	private TextView bar_title;
	/** 标题栏的返回图片 */
	private ImageView bar_image_back;
	
	/** 注册反馈 */
	private int r_feedback = 1;
	
	/** http提交数据列表 */
    List<NameValuePair> pairList;
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        MuApp.getInstance().addActivity(this);
        
        getEntities();
        initListeners();
	}
	
	private void getEntities(){
		bar_image_back = (ImageView)findViewById(R.id.bar_image_back);
		registerBtn = (Button)findViewById(R.id.registerBtn);
		mUsername = (EditText)findViewById(R.id.rg_username);
        mPassword = (EditText)findViewById(R.id.rg_password);
        mRepassword = (EditText)findViewById(R.id.rg_repassword);
        mLogin = (TextView)findViewById(R.id.rg_login); 
	}
	private void initListeners(){
		bar_image_back.setOnClickListener(this);
        registerBtn.setOnClickListener(this);
        mLogin.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		Intent intent=new Intent();
		switch(v.getId()){
		case R.id.bar_image_back://返回
    		intent.setClass(RegisterActivity.this, MainTabActivity.class);
    		intent.putExtra("page_tab", 3);
    		startActivity(intent);
    		RegisterActivity.this.finish();
			break;
		case R.id.registerBtn://注册
			onRegister();
			break;
		case R.id.rg_login://转登录界面
    		intent.setClass(RegisterActivity.this, LoginActivity.class);
    		startActivity(intent);
    		RegisterActivity.this.finish();
			break;
		}
	}
	
	private void onRegister(){
		//获取用户名和密码
		String username = mUsername.getText().toString().trim();
		String password = mPassword.getText().toString().trim();
        String repassword = mRepassword.getText().toString().trim();
        if(username.equals("")||password.equals("")||repassword.equals("")){
        	ToastUtils.showToast(getApplicationContext(), "账号和密码不能为空！");
        }
        else if(!password.equals(repassword)){
        	ToastUtils.showToast(getApplicationContext(), "密码和确认密码不一致！！");
        }
        else if(!username.matches("^[A-Za-z0-9]+$")){
        	ToastUtils.showToast(getApplicationContext(), "账号只能是字母、数字或两者组合！");
			return;
        }
        else{
        	NameValuePair pair_name = new BasicNameValuePair("username",username);
            NameValuePair pair_psw = new BasicNameValuePair("password", password);
            NameValuePair pair_repsw = new BasicNameValuePair("repassword", repassword);
            
            pairList = new ArrayList<NameValuePair>();
            pairList.add(pair_name);
            pairList.add(pair_psw);
            pairList.add(pair_repsw);
            
            if(!NetUtil.isConnect(getApplicationContext())){
            	ToastUtils.showToast(getApplicationContext(), "网络未连接");
            	return;
            }
            //显示进度框提示
            LoadingDialog.showLoadingDialog(RegisterActivity.this, "玩命注册中…");
            
            //创建线程
            Thread registerThread = new Thread(runnable);
            //启动线程
            registerThread.start();
        }
	}
    
    /** http post方式发送用户输入数据进行注册请求 */
    Runnable runnable =  new Runnable() {
    	@Override
		public void run() {
    		try
            {	
                HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList,HTTP.UTF_8);
                // URL使用基本URL即可，其中不需要加参数
                HttpPost httpPost = new HttpPost(ServerUrlUtil.Server_Register_URL);
                //httpPost.addHeader("content_Type", "application/json;charset=utf-8");
                // 将请求体内容加入请求中
                httpPost.setEntity(requestHttpEntity);
                // 需要客户端对象来发送请求
                HttpClient httpClient = new DefaultHttpClient();
                // 发送请求
                HttpResponse httpResponse = httpClient.execute(httpPost);
                //读返回数据  
                String strResult = EntityUtils.toString(httpResponse.getEntity());
                Log.e("strResult", strResult);
                if(strResult.equals("0")){
                	r_feedback = 0;//注册反馈
                	Log.e("注册反馈", "注册失败，该用户已存在！");
                }
                else if(strResult.equals("-1")){
                	r_feedback = -1;//注册反馈
                	Log.e("注册反馈", "注册失败！");
                }
                //若状态码为200 ok 
                if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK && r_feedback==1)   
                {  
                  
                  uid = Integer.valueOf(strResult);//注册用户的id
                  //将response返回数据写入到资源文件中
                  writeFile("myschedule.txt","");
                  writeFile("myfriend.txt","");
                  RegisterHandler.sendEmptyMessage(200);
                }  
                else  
                {  
                	RegisterHandler.sendEmptyMessage(0);
                }  
                //只要执行到这里就关闭对话框  
                LoadingDialog.closeLoadingDialog(); 
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
		}
	};
	
	/**
     * 注册处理函数
     * @author lims
     * @date 2015-02-03
     */
	Handler RegisterHandler = new Handler() {
		@Override
		//当有消息发送出来的时候就执行Handler的这个方法 
	    public void handleMessage(Message msg) {
			super.handleMessage(msg);  
			//Toast.makeText(getApplicationContext(), msg.toString(), Toast.LENGTH_SHORT).show();
			if(r_feedback==0){
				ToastUtils.showToast(getApplicationContext(), "注册失败，该用户已存在！");
				r_feedback=1;
			}
			else if(r_feedback==-1){
				ToastUtils.showToast(getApplicationContext(), "注册失败！");
				r_feedback=1;
			}
			else if(msg.what==200){
				/** 创建异或获取一个已经存在的sharedPreferences对象（单例的）*/
				SharedPreferences sharedPreferences = getSharedPreferences("loginUserSP", Context.MODE_PRIVATE);
				/** 创建数据编辑器 */
				Editor editor = sharedPreferences.edit();
				/** 传递需要保存的数据 */
				editor.putString("userphoto", "");
				editor.putInt("userid", uid);
				editor.putString("username", mUsername.getText().toString().trim());
				editor.putString("password", mPassword.getText().toString().trim());
				/** 保存数据 */
				editor.commit();
				
				Intent intent = new Intent();
	    		intent.setClass(RegisterActivity.this, MainTabActivity.class);
	    		/*intent.putExtra("user_photo", R.drawable.icon_long);
	    		intent.putExtra("user_tap", "lms");*/
	    		intent.putExtra("page_tab", 3);
	    		startActivity(intent);
	    		RegisterActivity.this.finish();
			}
			else {
				ToastUtils.showToast(getApplicationContext(), "注册失败！");
			}
            //只要执行到这里就关闭对话框  
			LoadingDialog.closeLoadingDialog(); 
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
