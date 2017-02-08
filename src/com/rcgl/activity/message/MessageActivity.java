package com.rcgl.activity.message;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.rcgl.R;
import com.rcgl.activity.activities.ActivitiesDetailActivity;
import com.rcgl.activity.basic.MainTabActivity;
import com.rcgl.activity.friend.FriendInfoActivity;
import com.rcgl.activity.user.LoginActivity;
import com.rcgl.app.MuApp;
import com.rcgl.swipemenulistview.util.SwipeMenuListView;
import com.rcgl.util.LoadingDialog;
import com.rcgl.util.NetUtil;
import com.rcgl.util.ServerUrlUtil;
import com.rcgl.util.ToastUtils;
/**
 * 消息显示和处理
 * @author lims
 */
public class MessageActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener {
	private List<Map<String, Object>> mAppList;
	private List<Map<String,Object>> mList;
	private MessageAdapter userAdapter;
	private ListView mListView;
	/** 标题栏的标题文字 */
	private TextView bar_title;
	/** 标题栏的返回图片 */
	private ImageView bar_image_back;
	/** http请求参数 */
	List<NameValuePair> pairList;
	/** 标记是否含有内容 */
	Boolean have_content = true;
	
	private static final int REFRESH_COMPLETE = 0X110;
	/** 下拉刷新处理 */
	private SwipeRefreshLayout mSwipeLayout;
	
	/** 登录用户ID */
	int userid = 0;
	
	/** 加载效果 */
	private Dialog pd;
	/** 活动数据 */
	private Map<String,String> activitiesMap;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        
        MuApp.getInstance().addActivity(this);        
        getEntities();
        initListeners();
        initView();
        initDatas();
        
        //显示进度框提示、
        LoadingDialog.showLoadingDialog(MessageActivity.this , "");
        //创建线程
        Thread loginThread = new Thread(runnable);
        //启动线程
        loginThread.start();
	}
	
	private void getEntities(){
		bar_title = (TextView)findViewById(R.id.message_bar_title);
		bar_image_back = (ImageView)findViewById(R.id.bar_image_back);
		mListView = (ListView) findViewById(R.id.listView);
        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.id_swipe_ly);
	}
	
	private void initListeners(){
		bar_image_back.setOnClickListener(return_main);
		mSwipeLayout.setOnRefreshListener(this);
	}
	
	private void initView(){
		findViewById(R.id.no_content_tip).setVisibility(View.INVISIBLE);
		bar_title.setText("我的消息");
		mSwipeLayout.setColorScheme(android.R.color.holo_green_dark, android.R.color.holo_green_light,
				android.R.color.holo_orange_light, android.R.color.holo_red_light);
	}
	
	private void initDatas(){
		SharedPreferences sharedPreferences = getSharedPreferences("loginUserSP", Context.MODE_PRIVATE);
		userid = sharedPreferences.getInt("userid", 0);
		pairList = new ArrayList<NameValuePair>();
		NameValuePair pair_id = new BasicNameValuePair("userid",userid+"");
        pairList.add(pair_id);
		
        mList = new ArrayList<Map<String,Object>>();
        
	}
		
	/** 长按时事件 */
	SwipeMenuListView.OnItemLongClickListener onLongClickItem = new OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			ToastUtils.showToast(getApplicationContext(), position + " long click");
			return false;
		}
	};
	
	/** 短按时事件 */
	OnItemClickListener onClickItem = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			if(mAppList.get(position).get("type").toString().equals("1")){
				Intent intent = new Intent();
				intent.setClass(MessageActivity.this, FriendInfoActivity.class);
				intent.putExtra("messageid", mList.get(position).get("messageid")+"");
				intent.putExtra("fid", mList.get(position).get("fid")+"");
				intent.putExtra("fname", mList.get(position).get("fname")+"");
				intent.putExtra("photo", mList.get(position).get("photo")+"");
				intent.putExtra("activitiesid", mList.get(position).get("activitiesid")+"");
				intent.putExtra("type", mList.get(position).get("type")+"");
				startActivity(intent);
			}
			else if(mAppList.get(position).get("type").toString().equals("2")){
				//ToastUtils.showToast(getApplicationContext(), "查看活动");
				onActivitiesPage(mList.get(position).get("activitiesid")+"");
			}
		}
	};
	
	/** http post方式发送消息输入数据进行获取消息列表请求 */
    Runnable runnable =  new Runnable() {
    	@Override
		public void run() {
    		try
            {	
                HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList,HTTP.UTF_8);
                HttpPost httpPost = new HttpPost(ServerUrlUtil.Server_GetMessageList_URL);
                httpPost.setEntity(requestHttpEntity);
                HttpClient httpClient = new DefaultHttpClient();
                // 发送请求
                HttpResponse httpResponse = httpClient.execute(httpPost);
                //若状态码为200 ok 
                if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK)   
                {  
                  //读返回数据  
                  String strResult = EntityUtils.toString(httpResponse.getEntity());  
                  MessageHandler.sendEmptyMessage(200);
                  
                  Log.e("strResult", strResult);
                  
                  if(strResult.equals("0")){
                	  have_content = false;
                	  show_no_content_tip();
                	  return;
                  }
                  
                  JSONObject jsonObject = new JSONObject(strResult);
                  JSONArray jsonArray = jsonObject.getJSONArray("message");
                  Log.e("messageinfo", jsonArray.getString(0));
                  Map<String,Object> mMap;
                  have_content = false;
                  for(int i=0;i<jsonArray.length();i++){
                	  if(jsonArray.getJSONObject(i).getInt("status")==0){//status==0为未处理
                		  mMap = new HashMap<String,Object>();
                		  mMap.put("messageid", jsonArray.getJSONObject(i).getString("id"));
                		  mMap.put("fid", jsonArray.getJSONObject(i).getString("senderid"));
                		  mMap.put("fname", jsonArray.getJSONObject(i).getString("senderName"));
                		  mMap.put("photo", jsonArray.getJSONObject(i).getString("senderPhoto"));
                    	  mMap.put("activitiesid", jsonArray.getJSONObject(i).getString("activitiesid"));
                    	  if(jsonArray.getJSONObject(i).getString("type").equals("2")){
                    		  mMap.put("activities_content", jsonArray.getJSONObject(i).getString("activities_content"));
                    	  }
                    	  mMap.put("type", jsonArray.getJSONObject(i).getString("type"));
                    	  mMap.put("time", jsonArray.getJSONObject(i).getString("time"));
                		  mMap.put("img", R.drawable.ic_message);
                          mList.add(mMap);
                          have_content = true;
                	  }
                  }
                  if(!have_content){
                	  show_no_content_tip();
                	  return;
                  }
                  Log.e("mList", mList.toString());
                }  
                else  
                {  
                	MessageHandler.sendEmptyMessage(0);
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
	Handler MessageHandler = new Handler() {
		@Override
		//当有消息发送出来的时候就执行Handler的这个方法 
	    public void handleMessage(Message msg) {
			super.handleMessage(msg);  
			if(msg.what==200){
				//配置适配器  
                mAppList = mList;
                userAdapter = new MessageAdapter();
          		mListView.setAdapter(userAdapter);
        		/*mListView.setMenuCreator(creator);
        		mListView.setOnMenuItemClickListener(onMenuClickItem);
        		mListView.setOnSwipeListener(swipeListener);*/
        		mListView.setOnItemLongClickListener(onLongClickItem);
        		mListView.setOnItemClickListener(onClickItem);
			}
			else {
				ToastUtils.showToast(getApplicationContext(), "获取消息列表失败");
			}
            //只要执行到这里就关闭对话框  
			LoadingDialog.closeLoadingDialog();
            //pd.dismiss(); 
	    }
	};
	
	//////////////////////////
	private void onActivitiesPage(String activitiesid){
		if(!NetUtil.isConnect(getApplicationContext())){
        	ToastUtils.showToast(getApplicationContext(), "网络未连接");
        	return;
        }
		NameValuePair pair_userid = new BasicNameValuePair("userid",userid+"");
        NameValuePair pair_activitiesid = new BasicNameValuePair("activitiesid", activitiesid);
        
        pairList = new ArrayList<NameValuePair>();
        pairList.add(pair_userid);
        pairList.add(pair_activitiesid);
        
        //创建线程
        Thread loginThread = new Thread(activities_runnable);
        //启动线程
        loginThread.start();
	}
    
    /** http post方式发送用户输入数据进行登录请求 */
    Runnable activities_runnable =  new Runnable() {
    	@Override
		public void run() {
    		try
            {	
                HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList,HTTP.UTF_8);
                HttpPost httpPost = new HttpPost(ServerUrlUtil.Server_GetActivitiesData_URL);
                // 将请求体内容加入请求中
                httpPost.setEntity(requestHttpEntity);
                // 需要客户端对象来发送请求
                HttpClient httpClient = new DefaultHttpClient();
                // 发送请求
                HttpResponse httpResponse = httpClient.execute(httpPost);
                //读返回数据  
                String strResult = EntityUtils.toString(httpResponse.getEntity());  
                Log.i("activities", strResult);
                //若状态码为200 ok 
                if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK && !strResult.equals("-1"))   
                {  
                  
                  JSONObject jsonObject = new JSONObject(strResult);
                  JSONArray jsonArray = jsonObject.getJSONArray("activities");
                  Log.i("activities", jsonArray.getString(0));
                  JSONObject jsonObject2 = new JSONObject();
                  jsonObject2 = jsonArray.getJSONObject(0);
                  
                  activitiesMap = new HashMap<String,String>();
                  activitiesMap.put("activitiesid", jsonObject2.getString("activitiesid"));
                  activitiesMap.put("userid", jsonObject2.getString("userid"));
                  activitiesMap.put("username", jsonObject2.getString("username"));
                  activitiesMap.put("photo", jsonObject2.getString("photo"));
                  activitiesMap.put("dotime", jsonObject2.getString("dotime"));
                  activitiesMap.put("content", jsonObject2.getString("content"));
                  activitiesMap.put("image", jsonObject2.getString("image"));
                  activitiesMap.put("max_number_people", jsonObject2.getString("max_number_people"));
                  activitiesMap.put("already_number_people", jsonObject2.getString("already_number_people"));
                  activitiesMap.put("participant", jsonObject2.getString("participant"));
                  activitiesMap.put("sort", jsonObject2.getString("sort"));
                  activitiesMap.put("isJoin", jsonObject2.getString("isJoin"));
                  activitiesMap.put("isCollect", jsonObject2.getString("isCollect"));
                  activitiesMap.put("time", jsonObject2.getString("time"));
                  
                  activitiesHandler.sendEmptyMessage(200);
                  
                }  
                else  
                {  
                	activitiesHandler.sendEmptyMessage(0);
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
	Handler activitiesHandler = new Handler() {
		@Override
		//当有消息发送出来的时候就执行Handler的这个方法 
	    public void handleMessage(Message msg) {
			super.handleMessage(msg);  
			//Toast.makeText(getApplicationContext(), msg.toString(), Toast.LENGTH_SHORT).show();
			if(msg.what==200){
				
				Intent intent = new Intent();
	    		intent.setClass(MessageActivity.this, ActivitiesDetailActivity.class);
	    		intent.putExtra("userid", userid);
				intent.putExtra("username", activitiesMap.get("username"));
				intent.putExtra("activitiesid", activitiesMap.get("activitiesid"));
				intent.putExtra("image", activitiesMap.get("image"));
				intent.putExtra("detail", activitiesMap.get("content"));
				intent.putExtra("dotime", activitiesMap.get("dotime"));
				intent.putExtra("max_people", activitiesMap.get("max_number_people"));
				intent.putExtra("already_people", activitiesMap.get("already_number_people"));
				intent.putExtra("participant", activitiesMap.get("participant"));
				intent.putExtra("isJoin", activitiesMap.get("isJoin"));
				intent.putExtra("isCollect", activitiesMap.get("isCollect"));
				intent.putExtra("time", activitiesMap.get("time"));
	    		startActivity(intent);
	    		
			}
			else {
				ToastUtils.showToast(getApplicationContext(), "打开活动失败");
			}
	    }
	};
    
	/////////////////////////
		
	private class MessageAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			//Log.e("mAppList", mAppList.toString());
			return mAppList.size();
		}

		@Override
		public Object getItem(int position) {
			return mAppList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = View.inflate(getApplicationContext(),
						R.layout.item_message, null);
				new ViewHolder(convertView);
			}
			ViewHolder holder = (ViewHolder) convertView.getTag();
			//holder.iv_icon.setImageResource(R.drawable.ic_message);
			holder.tv_time.setText(mAppList.get(position).get("time").toString());
			if(mAppList.get(position).get("type").equals("1")){
				holder.tv_title.setText(mAppList.get(position).get("fname").toString()+" 请求添加你为好友");
				holder.tv_content.setVisibility(View.GONE);
				holder.iv_icon.setImageResource(R.drawable.ic_message_message);
			}
			else if(mAppList.get(position).get("type").equals("2")){
				holder.tv_title.setText(mAppList.get(position).get("fname").toString()+" 给你分享了一个活动");
				holder.tv_content.setVisibility(View.VISIBLE);
				holder.tv_content.setText("("+mAppList.get(position).get("activities_content").toString()+")");
				holder.iv_icon.setImageResource(R.drawable.ic_message_activities);
			}
			return convertView;
		}

		class ViewHolder {
			ImageView iv_icon;
			TextView tv_time;
			TextView tv_title;
			TextView tv_content;

			public ViewHolder(View view) {
				iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
				tv_time = (TextView) view.findViewById(R.id.tv_time);
				tv_title = (TextView) view.findViewById(R.id.tv_title);
				tv_content = (TextView) view.findViewById(R.id.tv_content);
				view.setTag(this);
			}
		}
	}	
		
	/** 提示是否有内容 */
	private void show_no_content_tip(){
		if(!have_content){
			findViewById(R.id.no_content_tip).setVisibility(View.VISIBLE);
		}
	}

	private int dp2px(int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				getResources().getDisplayMetrics());
	}
	
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg)
		{
			switch (msg.what)
			{
			case REFRESH_COMPLETE:
				/*mDatas.addAll(Arrays.asList("Lucene", "Canvas", "Bitmap"));
				mAdapter.notifyDataSetChanged();*/
				mSwipeLayout.setRefreshing(false);
				break;

			}
		};
	};
	
	public void onRefresh()
	{
		// Log.e("xxx", Thread.currentThread().getName());
		// UI Thread

		mHandler.sendEmptyMessageDelayed(REFRESH_COMPLETE, 2000);
		
		mList = new ArrayList<Map<String,Object>>();
        mListView = (ListView) findViewById(R.id.listView);
		 //创建线程
        Thread loginThread = new Thread(runnable);
        //启动线程
        loginThread.start();
		
        ToastUtils.showToast(getApplicationContext(), "刷新完成");

	}
	
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
	
	/** 返回主界面 */
    View.OnClickListener return_main = new View.OnClickListener(){
		@Override
    	public void onClick(View view){
			Intent intent=new Intent();
    		intent.setClass(MessageActivity.this, MainTabActivity.class);
    		intent.putExtra("page_tab", 3);
    		startActivity(intent);
    		finish();
    	}
    };	
}
