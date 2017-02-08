package com.rcgl.activity.nearby;

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
import org.json.JSONException;
import org.json.JSONObject;

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
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.rcgl.R;
import com.rcgl.activity.friend.FriendInfoActivity;
import com.rcgl.activity.friend.ReadFriendScheduleActivity;
import com.rcgl.activity.message.MessageActivity;
import com.rcgl.adapter.LocationAdapter;
import com.rcgl.bean.LocationBean;
import com.rcgl.util.LoadingDialog;
import com.rcgl.util.NetUtil;
import com.rcgl.util.ServerUrlUtil;
import com.rcgl.util.ToastUtils;

/**
 * 查看附近的人
 * @author lims
 * @date 2015-05-05
 */
public class NearbyActivity extends Activity implements OnClickListener {
	/** ListView 的数据 */
	private List<LocationBean> mLocationList;
	/** 地位适配器 */
	private LocationAdapter locationAdapter;
	/** ListView */
	private ListView mListView;
	/** 标题栏返回键 */
	private ImageView iv_back;
	/** 标题栏标题 */
	private TextView tv_title;
	/** 标题栏刷新键 */
	private ImageView iv_reflash;
	/** 处理中状态图标 */
	private Dialog pd;
	/** http请求参数 */
	List<NameValuePair> pairList = new ArrayList<NameValuePair>();
	/** 百度sdk */
	private LocationClient locationClient = null;
	/** 隔多久更新一次 */
	private static final int UPDATE_TIME = 10000;
	/** 纬度 */
	private double latitude = 0.0;
	/** 经度 */
	private double longitude = 0.0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nearby);
		
		getEntities();
		initListeners();
		initView();
		
		LoadingDialog.showLoadingDialog(NearbyActivity.this, "");
		getJingduAndWeidu();//获取经纬度
	}
	
	private void getEntities(){
		iv_back = (ImageView) findViewById(R.id.bar_image_back);
		tv_title = (TextView) findViewById(R.id.bar_title);
		iv_reflash = (ImageView) findViewById(R.id.bar_reflash);
		mListView = (ListView) findViewById(R.id.listView);
	}
	
	private void initListeners(){
		iv_back.setOnClickListener(this);
		iv_reflash.setOnClickListener(this);
	}
	
	private void initView(){
		tv_title.setText("附近的人");
	}
	
	@Override
	public void onClick(View v) {
		Intent intent = new Intent();
		switch(v.getId()) {
		case R.id.bar_image_back://返回
			NearbyActivity.this.finish();
			break;
		case R.id.bar_reflash://刷新
			LoadingDialog.showLoadingDialog(NearbyActivity.this, "");
			location(longitude,latitude);//定位附近的人
			break;
		}
	}
	
	private void getJingduAndWeidu() {
		locationClient = new LocationClient(this);
		// 设置定位条件
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true); // 是否打开GPS
		option.setCoorType("bd09ll"); // 设置返回值的坐标类型。
		option.setPriority(LocationClientOption.NetWorkFirst); // 设置定位优先级
		option.setProdName("RCGLLocation"); // 设置产品线名称。强烈建议您使用自定义的产品线名称，方便我们以后为您提供更高效准确的定位服务。
		option.setScanSpan(UPDATE_TIME); // 设置定时定位的时间间隔。单位毫秒
		locationClient.setLocOption(option);
		// 注册位置监听器
		locationClient.registerLocationListener(new BDLocationListener() {
			@Override
			public void onReceiveLocation(BDLocation location) {
				if (location == null) {
					return;
				}
				latitude = location.getLatitude();
				longitude = location.getLongitude();
				
				location(longitude,latitude);//定位附近的人
				
				if (location.getLocType() == BDLocation.TypeGpsLocation) {
					
				} else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
					
				}
			}

			@Override
			public void onReceivePoi(BDLocation location) {
			}
			
		});
		locationClient.start();
		locationClient.requestLocation();
		
	}
	
	/** 获取定位，longitude为经度，latitude为纬度 */
	private void location(double longitude, double latitude){
		SharedPreferences sharedPreferences = getSharedPreferences("loginUserSP", Context.MODE_PRIVATE);
		int userid = sharedPreferences.getInt("userid", 0);
		
		
		NameValuePair pair_userid = new BasicNameValuePair("userid",userid+"");
		NameValuePair pair_longitude = new BasicNameValuePair("longitude",longitude+"");
		NameValuePair pair_latitude = new BasicNameValuePair("latitude",latitude+"");
        
        pairList = new ArrayList<NameValuePair>();
        pairList.add(pair_userid);
        pairList.add(pair_longitude);
        pairList.add(pair_latitude);
        
        Thread loadLocationThread = new Thread(loadLocation_runnable);
        loadLocationThread.start();
	}
	
	Runnable loadLocation_runnable =  new Runnable() {
    	@Override
		public void run() {
    		try
            {	
    			HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList,HTTP.UTF_8);
                HttpPost httpPost = new HttpPost(ServerUrlUtil.Server_NearbyServlet_URL);
                httpPost.setEntity(requestHttpEntity);
                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse httpResponse = httpClient.execute(httpPost);
                String strResult = EntityUtils.toString(httpResponse.getEntity());  
                Log.e("location", strResult);
                //若状态码为200 ok 
                if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK && !strResult.equals("-1")) {  
                	showNearby(strResult);
                }  
                else {  
                	loadLocation_handler.sendEmptyMessage(0);
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
     * @date 2015-02-03
     */
	Handler loadLocation_handler = new Handler() {
		@Override
		//当有消息发送出来的时候就执行Handler的这个方法 
	    public void handleMessage(Message msg) {
			super.handleMessage(msg);  
			if(msg.what==200){
				//配置适配器  
				locationAdapter = new LocationAdapter(getApplicationContext(),mLocationList);
          		mListView.setAdapter(locationAdapter);
        		//mListView.setOnItemLongClickListener(onLongClickItem);
        		mListView.setOnItemClickListener(onClickItem);
			}
			else {
				ToastUtils.showToast(getApplicationContext(),"定位失败");
			}
            //只要执行到这里就关闭对话框  
			LoadingDialog.closeLoadingDialog(); 
	    }
	};
	
	
	/** 
	 * 读取JSONArray数据显示好友到手机客户端
	 * @param nearbyStr JSONArray数据
	 * @throws JSONException
	 * @author lims
	 * @date 2015-05-05
	 */
	private void showNearby(String nearbyStr) throws JSONException{
		JSONObject jsonObject = new JSONObject(nearbyStr);
        JSONArray jsonArray = jsonObject.getJSONArray("location");
        LocationBean ltbean=null;//存放好友信息
        mLocationList=new ArrayList<LocationBean>();
        for(int i=0;i<jsonArray.length();i++){
        	JSONObject obj=jsonArray.getJSONObject(i);
        	ltbean=new LocationBean();
        	ltbean.setUserid(Integer.parseInt(obj.getString("userid")));
        	ltbean.setUsername(obj.getString("username"));
        	ltbean.setPhoto(obj.getString("photo"));
        	ltbean.setDistance(obj.getString("distance"));
        	ltbean.setIsfriend(Integer.parseInt(obj.getString("isfriend")));
        	ltbean.setTime(obj.getString("time"));
        	mLocationList.add(ltbean);
        }
        
        //冒泡排序
        for (int i = 0; i < mLocationList.size(); i++) {
  			for(int j = 0; j<mLocationList.size()-i-1; j++){
  				//这里-i主要是每遍历一次都把最大的i个数沉到最底下去了，没有必要再替换了
  				if(Integer.parseInt(mLocationList.get(j).getDistance())>Integer.parseInt(mLocationList.get(j+1).getDistance())){
  					LocationBean temp = mLocationList.get(j);
  					mLocationList.set(j, mLocationList.get(j+1));
  					mLocationList.set(j+1, temp);
  				}
  			}
  		}		
        
        loadLocation_handler.sendEmptyMessage(200);
	}
	
	/** 短按时事件 */
	OnItemClickListener onClickItem = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if(!NetUtil.isConnect(getApplicationContext())){
            	ToastUtils.showToast(getApplicationContext(), "网络未连接");
            	return;
            }
			if(mLocationList.get(position).getIsfriend()!=0){
				Intent intent = new Intent();
	    		intent.setClass(getApplicationContext().getApplicationContext(), ReadFriendScheduleActivity.class);
	    		intent.putExtra("friendname", mLocationList.get(position).getUsername().toString());
	    		intent.putExtra("friendid", Integer.valueOf(mLocationList.get(position).getUserid()));
	    		startActivity(intent);
			}
			else {
				ToastUtils.showToast(getApplicationContext(), "你们不是好友哦");
			}
		}
	};
	
}

