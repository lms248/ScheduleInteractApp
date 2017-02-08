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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.rcgl.R;
import com.rcgl.util.LoadingDialog;
import com.rcgl.util.NetUtil;
import com.rcgl.util.ServerUrlUtil;
import com.rcgl.util.ToastUtils;

/**
 * 修改用户密码
 * @author lims
 * @date 2015-04-09
 */
public class ChangePWActivity extends Activity implements OnClickListener {
	/** 标题栏返回键 */
	private ImageView iv_back;
	/** 标题栏标题 */
	private TextView tv_title;
	/** 旧密码输入框 */
	private EditText et_old_password;
	/** 新密码输入框 */
	private EditText et_new_password;
	/** 新密码确认输入框 */
	private EditText et_new_repassword;
	/** 提交修改按钮 */
	private Button bt_submit_change;
	/** 处理中状态图标 */
	private Dialog pd;
	/** http请求参数 */
	List<NameValuePair> pairList = new ArrayList<NameValuePair>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_password);
		
		getEntities();
		initListeners();
		initView();
		
	}
	
	private void getEntities(){
		iv_back = (ImageView) findViewById(R.id.bar_image_back);
		tv_title = (TextView) findViewById(R.id.bar_title);
		et_old_password = (EditText) findViewById(R.id.old_pw);
		et_new_password = (EditText) findViewById(R.id.new_pw);
		et_new_repassword = (EditText) findViewById(R.id.new_repw);
		bt_submit_change = (Button) findViewById(R.id.changeBtn);
	}
	
	private void initListeners(){
		iv_back.setOnClickListener(this);
		bt_submit_change.setOnClickListener(this);
	}
	
	private void initView(){
		tv_title.setText("修改密码");
	}
	
	@Override
	public void onClick(View v) {
		Intent intent = new Intent();
		switch(v.getId()) {
		case R.id.bar_image_back://返回
			ChangePWActivity.this.finish();
			break;
		case R.id.changeBtn://提交修改
			upChangePW();
			break;
		}
	}
	
	private void upChangePW(){
		SharedPreferences sp = getSharedPreferences("loginUserSP", Context.MODE_PRIVATE);
		int userid = sp.getInt("userid", 0);
		String old_password = sp.getString("password", "");
		String old_pw = et_old_password.getText().toString().trim();
		String new_pw = et_new_password.getText().toString().trim();
		String new_repw = et_new_repassword.getText().toString().trim();
		if(old_pw.equals("")){
			ToastUtils.showToast(getApplicationContext(),"旧密码不可为空");
		}
		else if(new_pw.equals("")){
			ToastUtils.showToast(getApplicationContext(),"新密码不可为空");
		}
		else if(new_repw.equals("")){
			ToastUtils.showToast(getApplicationContext(),"新密码确认不可为空");
		}
		else if(!new_pw.equals(new_repw)){
			ToastUtils.showToast(getApplicationContext(),"新密码与确认密码不一致，请重新输入");
			et_new_password.setText("");
			et_new_repassword.setText("");
		}
		else if(!old_password.equals(old_pw)){
			ToastUtils.showToast(getApplicationContext(),"旧密码有误，请重新输入");
		}
		else {
			pairList.add(new BasicNameValuePair("userid", userid+""));
			pairList.add(new BasicNameValuePair("password", old_pw));
			pairList.add(new BasicNameValuePair("newPassword", new_pw));
			if(!NetUtil.isConnect(getApplicationContext())){
            	ToastUtils.showToast(getApplicationContext(), "网络未连接");
            	return;
            }
			//显示进度框提示
            pd = LoadingDialog.createLoadingDialog(ChangePWActivity.this, "提交修改中…");
            pd.show();
            //创建线程
            Thread changePWThread = new Thread(changePWRunnable);
            changePWThread.start();
		}
	}
	
	Runnable changePWRunnable = new Runnable() {
		@Override
		public void run() {
			try {
				HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList,HTTP.UTF_8);
				HttpPost httpPost = new HttpPost(ServerUrlUtil.Server_ChangePW_URL);
				// 将请求体内容加入请求中
                httpPost.setEntity(requestHttpEntity);
                // 需要客户端对象来发送请求
                HttpClient httpClient = new DefaultHttpClient();
                // 发送请求
                HttpResponse httpResponse = httpClient.execute(httpPost);
                //读返回数据  
                String strResult = EntityUtils.toString(httpResponse.getEntity()); 
                if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK 
                		&& !strResult.equals("-1")) {
                	changePWHandler.sendEmptyMessage(200);
                    /** 创建异或获取一个已经存在的sharedPreferences对象（单例的）*/
                    SharedPreferences sharedPreferences = getSharedPreferences("loginUserSP", Context.MODE_PRIVATE);
  				  	/** 创建数据编辑器 */
  				  	Editor editor = sharedPreferences.edit();
  				  	/** 传递需要保存的数据 */
  				  	editor.putString("password", et_new_password.getText().toString().trim());
  				  	/** 保存数据 */
    				editor.commit();
                }
                else {
                	changePWHandler.sendEmptyMessage(0);
                }
			} catch (Exception e) {
				e.printStackTrace();
			}
            HttpPost httpPost = new HttpPost(ServerUrlUtil.Server_Login_URL);
			
		}
	};
	
	Handler changePWHandler = new Handler(){
		@Override
	    public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if(msg.what==200){
				ToastUtils.showToast(getApplicationContext(),"密码修改成功");
			}
			else {
				ToastUtils.showToast(getApplicationContext(),"密码修改失败");
			}
			//关闭缓冲图
			pd.dismiss();
		}
	};

}

