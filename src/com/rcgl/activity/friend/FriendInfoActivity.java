package com.rcgl.activity.friend;

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
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.rcgl.R;
import com.rcgl.activity.basic.MainTabActivity;
import com.rcgl.app.MuApp;
import com.rcgl.app.MyApplication;
import com.rcgl.util.ServerUrlUtil;
import com.rcgl.util.ToastUtils;
import com.rcgl.util.image.MyImageLoader;
/**
 * 接受到陌生人的好友请求时看到的个人信息
 * @author lims
 * @date 2015-04-29
 */
public class FriendInfoActivity extends Activity implements OnClickListener {
	/** 标题栏返回 */
	private ImageView bar_image_back;
	/** 头像 */
	private ImageView iv_photo;
	/** 通用名 */
	private TextView tv_name;
	/** 同意添加 */
	private Button bt_allow;
	/** 删除 */
	private Button bt_delete;
	/** http请求参数 */
	List<NameValuePair> pairList;
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_info);
        
        //处理关闭退出
        MuApp.getInstance().addActivity(this);
        MyApplication.initImageLoader(this);
        
        getEntities();
        initListeners();
        initView();
        
	}
	
	private void getEntities(){
		bar_image_back = (ImageView) findViewById(R.id.bar_image_back);
		iv_photo = (ImageView) findViewById(R.id.iv_photo);
		tv_name = (TextView) findViewById(R.id.tv_name);
		bt_allow = (Button) findViewById(R.id.bt_allow);
		bt_delete = (Button) findViewById(R.id.bt_delete);
	}
	private void initListeners(){
		bar_image_back.setOnClickListener(this);
		bt_allow.setOnClickListener(this);
		bt_delete.setOnClickListener(this);
	}
	private void initView(){
		tv_name.setText(getIntent().getStringExtra("fname"));
		String photoUrl = ServerUrlUtil.SERVER_BASCE_URL+"/rcgl/upload/"+getIntent().getStringExtra("photo");
		MyImageLoader.loadPhoto(photoUrl, iv_photo, 0);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		switch(v.getId()){
		case R.id.bar_image_back:
			finish();
			break;
		case R.id.bt_allow:
			final Dialog allowDialog = new Dialog(FriendInfoActivity.this, R.style.option_dialog);
			allowDialog.setContentView(R.layout.dialog_confirm);
            ((TextView) allowDialog.findViewById(R.id.dialog_title)).setText("确定添加为好友吗？");
            Button btn_allow_yes = (Button) allowDialog.findViewById(R.id.btn_yes);
            btn_allow_yes.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					//添加好友
					addFriend(getIntent().getStringExtra("messageid").toString(),getIntent().getStringExtra("fid"));
					allowDialog.dismiss();
				}
			});
            Button btn_allow_no = (Button) allowDialog.findViewById(R.id.btn_no);
            btn_allow_no.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					allowDialog.dismiss();
				}
			});
            allowDialog.show();
			break;
		case R.id.bt_delete:
			final Dialog deleteDialog = new Dialog(FriendInfoActivity.this, R.style.option_dialog);
			deleteDialog.setContentView(R.layout.dialog_confirm);
            ((TextView) deleteDialog.findViewById(R.id.dialog_title)).setText("确定删除该请求吗？");
            Button btn_delete_yes = (Button) deleteDialog.findViewById(R.id.btn_yes);
            btn_delete_yes.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ToastUtils.showToast(FriendInfoActivity.this, "删除该请求");
					deleteDialog.dismiss();
				}
			});
            Button btn_delete_no = (Button) deleteDialog.findViewById(R.id.btn_no);
            btn_delete_no.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					deleteDialog.dismiss();
				}
			});
            deleteDialog.show();
			break;
		}
	}
	
	/** 同意添加好友请求，messageid为消息对应ID，fid为好友ID */
	private void addFriend(String messageid,String fid) {
		SharedPreferences sharedPreferences = getSharedPreferences("loginUserSP", Context.MODE_PRIVATE);
		int userid = sharedPreferences.getInt("userid", 0);
		pairList = new ArrayList<NameValuePair>();
		NameValuePair pair_messageid = new BasicNameValuePair("messageid",messageid);
		NameValuePair pair_uid = new BasicNameValuePair("userid",userid+"");
		NameValuePair pair_fid = new BasicNameValuePair("friendid",fid);
		
		pairList.add(pair_messageid);
        pairList.add(pair_uid);
        pairList.add(pair_fid);
		
		//显示进度框提示
        //pd = LoadingDialog.createLoadingDialog(getApplicationContext(), "添加好友中…");
        //pd.show();
        
        //创建线程
        Thread addFriendThread = new Thread(addFriendRunnable);
        //启动线程
        addFriendThread.start();
	}	
		
	/** http post方式发送消息输入数据进行登录请求 */
    Runnable addFriendRunnable =  new Runnable() {
    	@Override
		public void run() {
    		try
            {	
                HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList,HTTP.UTF_8);
                HttpPost httpPost = new HttpPost(ServerUrlUtil.Server_AddFriend_URL);
                httpPost.setEntity(requestHttpEntity);
                HttpClient httpClient = new DefaultHttpClient();
                // 发送请求
                HttpResponse httpResponse = httpClient.execute(httpPost);
                
                //若状态码为200 ok 
                if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK)   
                {  
                  //读返回数据  
                  String strResult = EntityUtils.toString(httpResponse.getEntity());  
                  AddFriendHandler.sendEmptyMessage(200);
                  
                  Log.e("strResult", strResult);
                  
                  //将response返回数据写入到资源文件中
                  writeFile("myfriend.txt",strResult);
          		
                }  
                else  
                {  
                	AddFriendHandler.sendEmptyMessage(0);
                } 
                
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
		}
	};	
		
	/**
     * http请求处理函数
     * @author lims
     * @date 2015-02-03
     */
	Handler AddFriendHandler = new Handler() {
		@Override
		//当有消息发送出来的时候就执行Handler的这个方法 
	    public void handleMessage(Message msg) {
			super.handleMessage(msg);  
			if(msg.what==200){
				ToastUtils.showToast(getApplicationContext(), "添加好友成功");
				
				Intent intent = new Intent();
	    		intent.setClass(getApplicationContext(), MainTabActivity.class);
	    		intent.putExtra("page_tab", 1);
	    		startActivity(intent);
			}
			else {
				ToastUtils.showToast(getApplicationContext(), "添加好友失败");
			}
            //只要执行到这里就关闭对话框  
            //pd.dismiss(); 
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
