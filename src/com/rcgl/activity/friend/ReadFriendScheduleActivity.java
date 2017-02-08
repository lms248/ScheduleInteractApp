package com.rcgl.activity.friend;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import android.content.ClipData.Item;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.rcgl.R;
import com.rcgl.activity.basic.MainTabActivity;
import com.rcgl.app.MuApp;
import com.rcgl.swipemenulistview.util.SwipeMenuListView;
import com.rcgl.util.DayManage;
import com.rcgl.util.LoadingDialog;
import com.rcgl.util.ServerUrlUtil;
import com.rcgl.util.ToastUtils;
import com.rcgl.util.image.ImageUtils;

public class ReadFriendScheduleActivity extends Activity implements OnClickListener{
	/** ListView 的数据 */
	private List<Map<String, Object>> listData;
	/** ListView 的暂存数据 */
	private List<Map<String,Object>> mList;
	/** 日程适配器 */
	private ScheduleAdapter scheduleAdapter;
	/** 滑动ListView */
	private ListView mListView;
	/** 标题栏的标题文字 */
	private TextView bar_title;
	/** 标题栏的返回图片 */
	private ImageView bar_image_back;
	
	/** 存放发送到服务端的数据 */
	List<NameValuePair> pairList;
	
	/** 判断该好友是否有日程 */
	Boolean hasSchedule = true;
	
	/** 判断内容是否单行 */
	Boolean isSingleLine = true;
	
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_friend_schedule);
        
        MuApp.getInstance().addActivity(this);
        
        getEntities();
        initLinteners();		
        intiView();
        initDatas();
        
      	//显示进度框提示
        LoadingDialog.showLoadingDialog(ReadFriendScheduleActivity.this, "");
              
        //创建线程
        Thread loginThread = new Thread(runnable);
        //启动线程
        loginThread.start();
	}
	
	private void getEntities(){
		bar_title = (TextView)findViewById(R.id.bar_title);
        bar_image_back = (ImageView)findViewById(R.id.bar_image_back);
        mListView = (ListView) findViewById(R.id.listView);
	}
	
	private void initLinteners(){
		bar_image_back.setOnClickListener(this);
		
	}
	
	private void intiView(){
		String friendname = getIntent().getStringExtra("friendname");
        if(friendname.equals("")){
        	friendname = "好友日程";
        }
        else {
        	friendname = friendname+"的公开日程";
        }
        bar_title.setText(friendname);
	}
	
	private void initDatas(){
		/** 好友ID */
      	int userid = getIntent().getIntExtra("friendid", 0);
      	
      	pairList = new ArrayList<NameValuePair>();
      	NameValuePair pair_id = new BasicNameValuePair("userid",userid+"");
        pairList.add(pair_id);
      		
        mList = new ArrayList<Map<String,Object>>();
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()) {
		case R.id.bar_image_back: //返回主界面
			/*Intent intent=new Intent();
    		intent.setClass(ReadFriendScheduleActivity.this, MainTabActivity.class);
    		intent.putExtra("page_tab", 1);
    		startActivity(intent);*/
    		finish();
			break;
		}
		
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
				if(isSingleLine==true){
					((TextView) view.findViewById(R.id.schedule_content)).setSingleLine(false);
					isSingleLine=false;
				}
				else {
					((TextView) view.findViewById(R.id.schedule_content)).setSingleLine(true);
					isSingleLine=true;
				}
			}
		};
		
		/** http post方式发送日程输入数据进行登录请求 */
	    Runnable runnable =  new Runnable() {
	    	@Override
			public void run() {
	    		try
	            {	
	                HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList,HTTP.UTF_8);
	                HttpPost httpPost = new HttpPost(ServerUrlUtil.Server_GetFriendScheduleList_URL);
	                httpPost.setEntity(requestHttpEntity);
	                HttpClient httpClient = new DefaultHttpClient();
	                // 发送请求
	                HttpResponse httpResponse = httpClient.execute(httpPost);
	                
	                //若状态码为200 ok 
	                if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK)   
	                {  
	                  //读返回数据  
	                  String strResult = EntityUtils.toString(httpResponse.getEntity());  
	                  UserHandler.sendEmptyMessage(200);
	                  
	                  Log.i("strResult", strResult);
	                  
	                  if(strResult.equals("0")){
	                	  hasSchedule = false;
	                	  return;
	                  }
	                  
	                  /**  
	                   * 当返回码为200时，做处理  
	                   * 得到服务器端返回json数据，并做处理
	                   **/
	                  
	                  JSONObject jsonObject = new JSONObject(strResult);
	                  JSONArray jsonArray = jsonObject.getJSONArray("schedules");
	                  Log.i("scheduleInfo", jsonArray.getString(0));
	                  
	                  Date now = new Date(); //new Date()为获取当前系统时间
	          		  SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");///设置日期格式
	          		  String nowTime = df.format(now);
	          		  
	                  Map<String,Object> mMap;
	                  for(int i=0;i<jsonArray.length();i++){
	                	  if(nowTime.compareTo(jsonArray.getJSONObject(i).getString("dotime"))>0){
	            				continue;
	            			}
	                	  mMap = new HashMap<String,Object>();
	                	  mMap.put("scheduleid", jsonArray.getJSONObject(i).getString("scheduleid"));
	                	  mMap.put("dotime", jsonArray.getJSONObject(i).getString("dotime"));
	                	  mMap.put("content", jsonArray.getJSONObject(i).getString("content"));
	            		  mMap.put("img", R.drawable.rc_image);
	                      mList.add(mMap);
	                  }
	                  for(int i=0;i<jsonArray.length();i++){
	                	  if(nowTime.compareTo(jsonArray.getJSONObject(i).getString("dotime"))<=0){
	            				continue;
	            			}
	                	  mMap = new HashMap<String,Object>();
	                	  mMap.put("scheduleid", jsonArray.getJSONObject(i).getString("scheduleid"));
	                	  mMap.put("dotime", jsonArray.getJSONObject(i).getString("dotime"));
	                	  mMap.put("content", jsonArray.getJSONObject(i).getString("content"));
	            		  mMap.put("img", R.drawable.rc_image);
	                      mList.add(mMap);
	                  }
	                  Log.i("mList", mList.toString());
	                	
	          		
	                }  
	                else  
	                {  
	                	UserHandler.sendEmptyMessage(0);
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
		Handler UserHandler = new Handler() {
			@Override
			//当有消息发送出来的时候就执行Handler的这个方法 
		    public void handleMessage(Message msg) {
				super.handleMessage(msg);  
				//Toast.makeText(getApplicationContext(), msg.toString(), Toast.LENGTH_SHORT).show();
				if(!hasSchedule){
					//Toast.makeText(getApplicationContext(), "该好友没有可见日程", Toast.LENGTH_LONG).show();
					findViewById(R.id.no_content_tip).setVisibility(View.VISIBLE);
					//只要执行到这里就关闭对话框  
					LoadingDialog.closeLoadingDialog();  
					return;
				}
				
				if(msg.what==200){
					//配置适配器  
	                listData = mList;
	                scheduleAdapter = new ScheduleAdapter();
	          		mListView.setAdapter(scheduleAdapter);
	        		mListView.setOnItemLongClickListener(onLongClickItem);
	        		mListView.setOnItemClickListener(onClickItem);
				}
				else {
					ToastUtils.showToast(getApplicationContext(), "获取日程列表失败");
					//findViewById(R.id.no_content_tip).setVisibility(View.VISIBLE);
				}
	            //只要执行到这里就关闭对话框  
				LoadingDialog.closeLoadingDialog(); 
		    }
		};
		
		private void delete(Item item) {
			
		}

		private void open(Item item) {
			
		}
		
		private class ScheduleAdapter extends BaseAdapter {
			@Override
			public int getCount() {
				//Log.i("listData", listData.toString());
				return listData.size();
			}

			@Override
			public Object getItem(int position) {
				return listData.get(position);
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				if (convertView == null) {
					convertView = View.inflate(getApplicationContext(),
							R.layout.item_schedule, null);
					new ViewHolder(convertView);
				}
				ViewHolder holder = (ViewHolder) convertView.getTag();
				//Item item = getItem(position);
				String date = listData.get(position).get("dotime").toString();
				
				Date now = new Date(); //new Date()为获取当前系统时间
	    		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");///设置日期格式
	    		String nowTime = df.format(now);
				
				Bitmap bmSrc = BitmapFactory.decodeResource(convertView.getResources(),DayManage.showDayImage(date.substring(8, 10))); 
			    if(nowTime.compareTo(listData.get(position).get("dotime").toString())>0){
			    	bmSrc = ImageUtils.bitmap2Gray(bmSrc);
			    }
			    Bitmap rounded_bm = ImageUtils.getRoundedCornerBitmap(bmSrc,60f);
			    Drawable bitmapDrawable = new BitmapDrawable(rounded_bm);
			    holder.iv_icon.setImageDrawable(bitmapDrawable);
			    
				//holder.iv_icon.setImageResource(show_day_image(date.substring(8, 10)));
				holder.tv_dotime.setText(listData.get(position).get("dotime").toString());
				holder.tv_content.setText(listData.get(position).get("content").toString());
				return convertView;
			}

			class ViewHolder {
				ImageView iv_icon;
				TextView tv_dotime;
				TextView tv_content;

				public ViewHolder(View view) {
					iv_icon = (ImageView) view.findViewById(R.id.schedule_icon);
					tv_dotime = (TextView) view.findViewById(R.id.schedule_time);
					tv_content = (TextView) view.findViewById(R.id.schedule_content);
					view.setTag(this);
				}
			}
		}

		private int dp2px(int dp) {
			return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
					getResources().getDisplayMetrics());
		}
		

}
