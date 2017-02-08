package com.rcgl.activity.friend;

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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
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
import com.rcgl.util.image.MyImageLoader;
/**
 * 添加好友
 * @author lims
 *
 */
public class AddFriendActivity extends Activity {
	/** 搜索输入框 */
	private EditText mEditText;
	/** 搜索事件点击图标 */
	private ImageView mImageView;
	/** 查找出的用户头像 */
	private ImageView userPhoto;
	/** 查找出的用户名 */
	private TextView username;
	/** 加为好友事件按钮 */
	private TextView addfriend;
	/** 所添加的好友id */
	private int fid;
	/** 所添加的好友用户名 */
	private String fname;
	/** 所添加的好友照片路径 */
	private String photoURL;
	/** mark标记是否已为好友，0为否，1为是 */
	private String mark;
	/** 标题栏的标题文字 */
	private TextView bar_title;
	/** 标题栏的右端图片 */
	private ImageView bar_image;
	
	List<NameValuePair> pairList;
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
        setContentView(R.layout.add_friend);
        
        //处理关闭退出
        MuApp.getInstance().addActivity(this);
        MyApplication.initImageLoader(this);
        
        getEntities();
		initListeners();
		initView();
	}
	
	private void getEntities(){
		bar_image = (ImageView)findViewById(R.id.bar_image);
        mEditText = (EditText)findViewById(R.id.username1);
        mImageView = (ImageView)findViewById(R.id.search);
        userPhoto = (ImageView)findViewById(R.id.photo);
        username = (TextView)findViewById(R.id.username2);
        addfriend = (TextView)findViewById(R.id.addfriend);
	}
	
	private void initListeners(){
		mImageView.setOnClickListener(onSearchFriend);
        bar_image.setOnClickListener(return_main);
        addfriend.setOnClickListener(onAddFriendRequest);
	}
	
	private void initView(){
		mImageView.setImageResource(R.drawable.ic_search);
        userPhoto.setImageResource(R.drawable.default_photo);
        username.setText("柚橙");
        addfriend.setText("加为好友");
        
        findViewById(R.id.addUser).setVisibility(View.INVISIBLE);//设置查找用户时的结果区域是否可见
	}
	
	/** 查找好友 */
    View.OnClickListener onSearchFriend = new View.OnClickListener(){
		@Override
    	public void onClick(View view){
			if(!NetUtil.isConnect(getApplicationContext())){
            	ToastUtils.showToast(getApplicationContext(), "网络未连接");
            	return;
            }
			SharedPreferences sharedPreferences = getSharedPreferences("loginUserSP", Context.MODE_PRIVATE);
			int userid = sharedPreferences.getInt("userid", 0);
			String username = sharedPreferences.getString("username", "0");
			
			if(userid==0){
				ToastUtils.showToast(getApplicationContext(), "你还未登录哦，去登录玩玩？");
				return;
			}
			
			//获取查找的好友用户名
			String friendname = mEditText.getText().toString().trim();
			if(friendname.equals("")){
				ToastUtils.showToast(getApplicationContext(), "输入用户名不能为空");
				return;
			}
			if(username.equals(friendname)){
				ToastUtils.showToast(getApplicationContext(), "这是你的账号，请重新输入");
				return;
			}
			
			NameValuePair pair_username = new BasicNameValuePair("username",username);
			NameValuePair pair_friendname = new BasicNameValuePair("friendname",friendname);
			NameValuePair pair_id = new BasicNameValuePair("id",String.valueOf(userid));
            pairList = new ArrayList<NameValuePair>();
            pairList.add(pair_username);
            pairList.add(pair_friendname);
            pairList.add(pair_id);
            
            //显示进度框提示
            LoadingDialog.showLoadingDialog(AddFriendActivity.this, "加载中，马上就好…");
            
            //创建线程
            Thread searchfriendThread = new Thread(searchfriend_runnable);
            //启动线程
            searchfriendThread.start();
            
    	}
    };
	
    /** 发送添加好友请求 */
    View.OnClickListener onAddFriendRequest = new View.OnClickListener(){
		@Override
    	public void onClick(View view){
			if(!NetUtil.isConnect(getApplicationContext())){
            	ToastUtils.showToast(getApplicationContext(), "网络未连接");
            	return;
            }
			//读取SharedPreferences中的数据
			SharedPreferences sharedPreferences = getSharedPreferences("loginUserSP", Context.MODE_PRIVATE);
			//getString()第二个参数为缺省值，如果preference中不存在该key，将返回缺省值
			int userid = sharedPreferences.getInt("userid", 0);
			
			NameValuePair pair_uid = new BasicNameValuePair("uid",String.valueOf(userid));
			NameValuePair pair_fid = new BasicNameValuePair("fid",String.valueOf(fid));
			NameValuePair pair_type = new BasicNameValuePair("type","1");
            pairList = new ArrayList<NameValuePair>();
            pairList.add(pair_uid);
            pairList.add(pair_fid);
            pairList.add(pair_type);
            
            //显示进度框提示
            LoadingDialog.showLoadingDialog(AddFriendActivity.this, "请求中…");
            
            //创建线程
            Thread addfriendThread = new Thread(addfriend_runnable);
            //启动线程
            addfriendThread.start();
            
    	}
    };
    
    /** http post方式发送用户输入数据进行查找好友请求 */
    Runnable searchfriend_runnable =  new Runnable() {
    	@Override
		public void run() {
    		try{	
    			//Thread.sleep(2000);//测试显示进度框提示效果，最后需删掉
                HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList,HTTP.UTF_8);
                // URL使用基本URL即可，其中不需要加参数
                HttpPost httpPost = new HttpPost(ServerUrlUtil.Server_GetUser_URL);
                //httpPost.addHeader("content_Type", "application/json;charset=utf-8");
                // 将请求体内容加入请求中
                httpPost.setEntity(requestHttpEntity);
                // 需要客户端对象来发送请求
                HttpClient httpClient = new DefaultHttpClient();
                // 发送请求
                HttpResponse httpResponse = httpClient.execute(httpPost);
                //读返回数据  
                String strResult = EntityUtils.toString(httpResponse.getEntity());  
                Log.e("searchFriend", strResult);
                //若状态码为200 ok 
                if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK && !strResult.equals("-1")) {  
                	
                  /**  
                   * 当返回码为200时，做处理  
                   * 得到服务器端返回json数据，并做处理
                   **/
                  JSONObject jsonObject = new JSONObject(strResult);
                  JSONArray jsonArray = jsonObject.getJSONArray("user");
                  Log.e("userinfo", jsonArray.getString(0));
                  JSONObject jsonObject2 = new JSONObject();
                  jsonObject2 = jsonArray.getJSONObject(0);
                  Log.e("id", jsonObject2.getString("id"));
                  Log.e("username", jsonObject2.getString("username"));
                  
                  fid = Integer.parseInt(jsonObject2.getString("id"));//所添加好友的id
                  fname = jsonArray.getJSONObject(0).getString("username");//所添加好友的用户名
                  photoURL = jsonArray.getJSONObject(0).getString("photo");//所添加好友的照片路径
                  mark = jsonObject2.getString("mark");//mark标记是否已为好友，0为否，1为是
                  
                  searchfriend_Handler.sendEmptyMessage(200);
                  
                }  
                else  
                {  
                	searchfriend_Handler.sendEmptyMessage(0);
                } 
                
            }
            catch (Exception e){
                e.printStackTrace();
            }
		}
	};
    
	/** http post方式发送用户输入数据进行查找好友请求 */
    Runnable addfriend_runnable =  new Runnable() {
    	@Override
		public void run() {
    		try{	
    			//Thread.sleep(2000);//测试显示进度框提示效果，最后需删掉
                HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList,HTTP.UTF_8);
                // URL使用基本URL即可，其中不需要加参数
                HttpPost httpPost = new HttpPost(ServerUrlUtil.Server_SendMessage_URL);
                //httpPost.addHeader("content_Type", "application/json;charset=utf-8");
                // 将请求体内容加入请求中
                httpPost.setEntity(requestHttpEntity);
                // 需要客户端对象来发送请求
                HttpClient httpClient = new DefaultHttpClient();
                // 发送请求
                HttpResponse httpResponse = httpClient.execute(httpPost);
                //读返回数据  
                String strResult = EntityUtils.toString(httpResponse.getEntity());  
                Log.e("addFriendRequest", strResult);
                //若状态码为200 ok 
                if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK && !strResult.equals("-1")) {  
                	addFriendHandler.sendEmptyMessage(200);
                  /**  
                   * 当返回码为200时，做处理  
                   * 得到服务器端返回json数据，并做处理
                   **/
                }  
                else  
                {  
                	addFriendHandler.sendEmptyMessage(0);
                } 
                
            }
            catch (Exception e){
                e.printStackTrace();
            }
    	}
    };
	
	/**
     * 查找好友处理函数
     * @author lims
     * @date 2015-02-03
     */
	Handler searchfriend_Handler = new Handler() {
		@Override
		//当有消息发送出来的时候就执行Handler的这个方法 
	    public void handleMessage(Message msg) {
			super.handleMessage(msg);  
			//Toast.makeText(getApplicationContext(), msg.toString(), Toast.LENGTH_SHORT).show();
			if(msg.what==200){
				ToastUtils.showToast(getApplicationContext(), "查找用户成功");
				addfriend.setText("加为好友");
				addfriend.setClickable(true);
				username.setText(fname);//设置查找到的用户名
				String imageURL = ServerUrlUtil.SERVER_BASCE_URL+"/rcgl/upload/"+photoURL;
				MyImageLoader.loadPhoto(imageURL, userPhoto, 0);
                if(mark.equals("1")){
              	  addfriend.setText("已是好友");
              	  addfriend.setClickable(false);
                }
				
                findViewById(R.id.addUser).setVisibility(View.VISIBLE);
				
			}
			else {
				ToastUtils.showToast(getApplicationContext(), "您查找的用户不存在…");
				findViewById(R.id.addUser).setVisibility(View.INVISIBLE);
			}
            //只要执行到这里就关闭对话框  
			LoadingDialog.closeLoadingDialog(); 
	    }
	};
	
	/**
     * 请求添加好友处理函数
     * @author lims
     * @date 2015-02-03
     */
	Handler addFriendHandler = new Handler() {
		@Override
		//当有消息发送出来的时候就执行Handler的这个方法 
	    public void handleMessage(Message msg) {
			super.handleMessage(msg);  
			if(msg.what==200){
				ToastUtils.showToast(getApplicationContext(), "添加好友请求已发出");
				addfriend.setText("等待确认");
            	addfriend.setClickable(false);
			}
			else {
				ToastUtils.showToast(getApplicationContext(), "发送添加好友请求失败");
			}
            //只要执行到这里就关闭对话框  
			LoadingDialog.closeLoadingDialog();
	    }
	};
	
	/** 返回主界面 */
    View.OnClickListener return_main = new View.OnClickListener(){
		@Override
    	public void onClick(View view){
			Intent intent=new Intent();
    		intent.setClass(AddFriendActivity.this, MainTabActivity.class);
    		intent.putExtra("page_tab", 1);
    		startActivity(intent);
    		finish();
    	}
    };
	
    
}
