package com.rcgl.activity.activities;

import java.io.FileInputStream;
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
import org.apache.http.util.EncodingUtils;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.rcgl.R;
import com.rcgl.adapter.FriendAdapter;
import com.rcgl.app.MuApp;
import com.rcgl.app.MyApplication;
import com.rcgl.bean.UserBean;
import com.rcgl.data.ShareConstants;
import com.rcgl.swipemenulistview.util.SwipeMenuListView;
import com.rcgl.util.ServerUrlUtil;
import com.rcgl.util.ToastUtils;

public class ShareActivitiesActivity extends Activity implements OnClickListener {
	/** ListView 的数据 */
	private List<UserBean>mFriendList;
	/** 好友适配器 */
	private FriendAdapter friendAdapter;
	/** 滑动ListView */
	private ListView mListView;
	/** 标题栏的标题文字 */
	private TextView bar_title;
	/** 标题栏的返回图标 */
	private ImageView bar_image_back;
	/** 标题栏的发送分享图标 */
	private ImageView bar_image_send;
	
	/** 登录的用户ID */
	int userid = 0;
	
	/** http请求参数 */
    List<NameValuePair> pairList;
    /** 本地好友文件 */
    String localFriendStr = null;
    /** 搜索关键字 */
	private String keyword = "";
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_share);
        
        MyApplication.initImageLoader(getApplicationContext());
        MuApp.getInstance().addActivity(this);
        
        getEntities();
		initListeners();
		initView();
		initDatas();
	}
	
	private void getEntities(){
		bar_title = (TextView)findViewById(R.id.bar_title);
		bar_image_back = (ImageView)findViewById(R.id.bar_image_back);
        bar_image_send = (ImageView) findViewById(R.id.bar_image_send);
        mListView = (ListView) findViewById(R.id.listView);
	}
	private void initListeners(){
		bar_image_back.setOnClickListener(this);
		bar_image_send.setOnClickListener(this);
	}
	private void initView(){
		bar_title.setText("活动分享");
	}
	private void initDatas(){
		ShareConstants.tofriends = "";
		
		SharedPreferences sharedPreferences = getSharedPreferences("loginUserSP", Context.MODE_PRIVATE);
		userid = sharedPreferences.getInt("userid", 0);
        
		//显示进度框提示
        //LoadingDialog.showLoadingDialog(getApplicationContext(), "");
        
        /** 显示本地好友的线程 */
        Thread showLocalFriendThread = new Thread(showLocalFriend_runnable);
        showLocalFriendThread.start();
        
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.bar_image_back://标题栏返回图标
			finish();
			break;
		case R.id.bar_image_send://标题栏返回图标
			//ToastUtils.showToast(getApplicationContext(), MyConstants.tofriends);
			final Dialog shareDialog = new Dialog(ShareActivitiesActivity.this, R.style.option_dialog);
			shareDialog.setContentView(R.layout.dialog_confirm);
            ((TextView) shareDialog.findViewById(R.id.dialog_title)).setText("确定提交分享吗？");
            Button btn_yes = (Button) shareDialog.findViewById(R.id.btn_yes);
            btn_yes.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					shareActivities();//分享活动
					shareDialog.dismiss();
				}
			});
            Button btn_no = (Button) shareDialog.findViewById(R.id.btn_no);
            btn_no.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					shareDialog.dismiss();
				}
			});
            shareDialog.show();
			
			break;
		}
	}
	
	
	/** 长按时事件 */
	SwipeMenuListView.OnItemLongClickListener onLongClickItem = new OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			ToastUtils.showToast(getApplicationContext(),position + " long click");
			return false;
		}
	};
	
	/** 短按时事件 */
	OnItemClickListener onClickItem = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			ToastUtils.showToast(getApplicationContext(),position + " short click");
		}
	};
	
	/** 分享活动 */
	private void shareActivities(){
		NameValuePair pair_userid = new BasicNameValuePair("uid",userid+"");
		NameValuePair pair_friendid = new BasicNameValuePair("fid",ShareConstants.tofriends);
        NameValuePair pair_activitiesid = new BasicNameValuePair("activitiesid",ShareConstants.activitiesid);
        NameValuePair pair_type = new BasicNameValuePair("type","2");
        
        pairList = new ArrayList<NameValuePair>();
        pairList.add(pair_userid);
        pairList.add(pair_friendid);
        pairList.add(pair_activitiesid);
        pairList.add(pair_type);
        
        //显示进度框提示
        //LoadingDialog.showLoadingDialog(getApplicationContext(), "");
        //创建线程
        Thread activitiesThread = new Thread(http_runnable);
        //启动线程
        activitiesThread.start();
	}
	
	
    Runnable showLocalFriend_runnable =  new Runnable() {
    	@Override
		public void run() {
    		try
            {	
    			showFriend_handler.sendEmptyMessage(200);
    			
    			localFriendStr = readFile("myfriend.txt");//读取本地好友资源文件
                if(localFriendStr==null||localFriendStr.equals("")){
              	  return;
                }
                
                showFriend(localFriendStr);
                
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
     * @date 2015-03-26
     */
	Handler showFriend_handler = new Handler() {
		@Override
		//当有消息发送出来的时候就执行Handler的这个方法 
	    public void handleMessage(Message msg) {
			super.handleMessage(msg);  
			if(msg.what==200){
				//配置适配器  
                friendAdapter = new FriendAdapter(getApplicationContext(),mFriendList);
          		mListView.setAdapter(friendAdapter);
        		mListView.setOnItemLongClickListener(onLongClickItem);
        		mListView.setOnItemClickListener(onClickItem);
        		
			}
			else {
				ToastUtils.showToast(getApplicationContext(),"获取用户列表失败");
			}
            //只要执行到这里就关闭对话框  
			//LoadingDialog.closeLoadingDialog(); 
	    }
	};
	
	/** 
	 * 读取JSONArray数据显示好友到手机客户端
	 * @param friendStr JSONArray数据
	 * @throws JSONException
	 * @author lims
	 * @date 2015-04-01
	 */
	private void showFriend(String friendStr) throws JSONException{
		JSONObject jsonObject = new JSONObject(friendStr);
        JSONArray jsonArray = jsonObject.getJSONArray("user");
        Log.e("userinfo", jsonArray.getString(0));
        
        UserBean fbean=null;//存放好友信息
        mFriendList=new ArrayList<UserBean>();
        for(int i=0;i<jsonArray.length();i++){
        	JSONObject obj=jsonArray.getJSONObject(i);
        	if(keyword.length()>0 && !obj.getString("username").contains(keyword)){
        		continue;
        	}
        	fbean=new UserBean();
        	fbean.setUserid(Integer.parseInt(obj.getString("id")));
        	fbean.setUsername(obj.getString("username"));
        	fbean.setPhoto(obj.getString("photo"));
        	fbean.setTime(obj.getString("time"));
        	fbean.setCheckBoxVisibility(true);
        	mFriendList.add(fbean);
        }
	}
	
	/** http post方式发送用户输入数据进行获取活动请求 */
    Runnable http_runnable =  new Runnable() {
    	@Override
		public void run() {
    		try
            {	
                HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList,HTTP.UTF_8);
                HttpPost httpPost = new HttpPost(ServerUrlUtil.Server_SendMessage_URL);
                // 将请求体内容加入请求中
                httpPost.setEntity(requestHttpEntity);
                // 需要客户端对象来发送请求
                HttpClient httpClient = new DefaultHttpClient();
                // 发送请求
                HttpResponse httpResponse = httpClient.execute(httpPost);
                //读返回数据  
                String strResult = EntityUtils.toString(httpResponse.getEntity());  
                //若状态码为200 ok 
                if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK && !strResult.equals("-1")) {
                	httpHandler.sendEmptyMessage(200);
                }  
                else {
                	httpHandler.sendEmptyMessage(0);
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
     * @date 2015-03-30
     */
	Handler httpHandler = new Handler() {
		@Override
		//当有消息发送出来的时候就执行Handler的这个方法 
	    public void handleMessage(Message msg) {
			super.handleMessage(msg);  
			//Toast.makeText(getApplicationContext(), msg.toString(), Toast.LENGTH_SHORT).show();
			if(msg.what==200){
				ToastUtils.showToast(getApplicationContext(), "分享成功");
			}
			else {
				ToastUtils.showToast(getApplicationContext(), "分享失败");
			}
            //只要执行到这里就关闭对话框  
			//LoadingDialog.closeLoadingDialog();
	    }
	};
	
	/** 读取资源文件数据 */ 
	public String readFile(String fileName) throws IOException{   
	  String res="";   
	  try{   
	         FileInputStream fin = openFileInput(fileName);   
	         int length = fin.available();   
	         byte [] buffer = new byte[length];   
	         fin.read(buffer);       
	         res = EncodingUtils.getString(buffer, "UTF-8");   
	         fin.close();       
	     }   
	     catch(Exception e){   
	         e.printStackTrace();   
	     }   
	     return res;   
	  
	}


}
