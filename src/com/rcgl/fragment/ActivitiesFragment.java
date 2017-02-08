package com.rcgl.fragment;

import java.io.FileInputStream;
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
import org.apache.http.util.EncodingUtils;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.rcgl.R;
import com.rcgl.activity.activities.ActivitiesDetailActivity;
import com.rcgl.activity.activities.AddActivitiesActivity;
import com.rcgl.activity.activities.MyActivitiesActivity;
import com.rcgl.adapter.ActivitiesAdapter;
import com.rcgl.app.MuApp;
import com.rcgl.app.MyApplication;
import com.rcgl.bean.ActivitiesBean;
import com.rcgl.data.ShareConstants;
import com.rcgl.refresh.view.PullToRefreshView;
import com.rcgl.util.LoadingDialog;
import com.rcgl.util.NetUtil;
import com.rcgl.util.ServerUrlUtil;
import com.rcgl.util.ToastUtils;
import com.rcgl.util.image.ImageUtils;
import com.rcgl.util.image.MyImageLoader;
/**
 * 活动主界面
 * @author lims
 * @date 2015-04-10（改）
 */
public class ActivitiesFragment extends Fragment implements OnClickListener{
	
	private List<ActivitiesBean>mActivitiesList;
	/** 标题栏的标签显示按钮 */
	private ImageView bar_image_tab;
	/** 标题栏的标题文字 */
	private TextView bar_title;
	/** 标题栏的添加活动图标 */
	private ImageView bar_image_add;
	/** item_activities的布局 */
	private LinearLayout linearLayout_item;
	/** 界面主布局 */
	private LinearLayout linearLayout_activities;
	/** 活动适配器 */
	public static ActivitiesAdapter activitiesAdapter;
	/** 活动实体类 */
	private ActivitiesBean activitiesBean;
	/** 发布者头像 */
	private ImageView userPhoto;
	/** 活动时间 */
	private TextView activities_time;
	/** 活动内容 */
	private TextView activities_content;
	/** 限制人数 */
	private TextView max_number_people;
	/** 已报名人数 */
	private TextView already_number_people;
	/** 报名按钮 */
	private TextView join_activities;
	/** 取消报名按钮 */
	private TextView exit_activities; 
	/** 界面ListView */
	private ListView mListView = null;
	/** ListView头部的用户名 */
	private TextView head_username;
	/** 分类标签栏 */
	private LinearLayout sort_tab;
	/** 标签“全部” */
	private TextView tab_all;
	/** 标签“运动” */
	private TextView tab_sport;
	/** 标签“旅行” */
	private TextView tab_travel;
	/** 标签“学习” */
	private TextView tab_study;
	/** 标签“休闲” */
	private TextView tab_casual;
	/** 显示活动分类，“0”显示全部，“1”显示运动，“2”显示旅行，“3”显示学习，“4”显示休闲 */
	private String sort = "0";
	
	/** 当前标签提示 */
	private TextView tab_tip;
	/** 当前显示的一行 */
	public static int listview_position = 0;
	
	/** http请求参数 */
    List<NameValuePair> pairList;
	
	private Dialog pd;
	
	/** 判断内容是否单行 */
	Boolean isSingleLine = true;
	
	Boolean have_content = true;
	
	public static final int REFRESH_DELAY = 1000;
    private PullToRefreshView mPullToRefreshView;
	
	/** 点击控件操作时，更新的数据*/ 
	//public static ActivitiesBean updateActivitiesData;
	/** 点击控件操作时，更新的List数据位置 */ 
	//public static int updatePosition;
	/** 接收Adapter数据 */
	/*public static Handler updataDatahandler;*/
	/** 用户对活动的操作 */
	//public static String updateOperate;
	/* 读取SharedPreferences中的数据 */
	SharedPreferences sharedPreferences;
	/** 用户名 */
	String username;
	/** 用户id */
	int userid;
	/** 处理用户头像 */
	Bitmap bitmap;
	/** 标题栏的查询日程图标 */
	private ImageView bar_search;
	/** 搜索栏输入框 */
	private EditText search_text;
	/** 搜索框清除按钮 */
	private ImageView search_clear;
	/** 搜索关键字 */
	private String keyword = "";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        getActivity().findViewById(R.id.bar).setVisibility(View.GONE);
		return inflater.inflate(R.layout.fragment_activities, null);		
	}	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		MyApplication.initImageLoader(getActivity());
		MuApp.getInstance().addActivity(getActivity());
		
		//读取SharedPreferences中的数据
		sharedPreferences = getActivity().getSharedPreferences("loginUserSP", Context.MODE_PRIVATE);
		//getString()第二个参数为缺省值，如果preference中不存在该key，将返回缺省值
		userid = sharedPreferences.getInt("userid", 0);
		username = sharedPreferences.getString("username", "未登录");
		
		getEntities();
		initListeners();
		initView();
		
		showActivitiesThread(runnable);//显示活动内容
        
        /**
         * 接收ActivitiesAdapter数据
         * @date 2015-03-26
         */
        /*updataDatahandler = oprateActivitiesHandler;*/
    	
    	mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				listview_position = position;
				if(position==0){
					Intent intent = new Intent();
					intent.setClass(getActivity(), MyActivitiesActivity.class);
					intent.putExtra("listview_position", listview_position);
					intent.putExtra("obj", "1");
					startActivity(intent);
					return;
				}
				Intent intent = new Intent();
				ShareConstants.activitiesid = mActivitiesList.get(position-1).getActivitiesid()+"";
				intent.setClass(getActivity(), ActivitiesDetailActivity.class);
				intent.putExtra("userid", mActivitiesList.get(position-1).getUserid());
				intent.putExtra("username", mActivitiesList.get(position-1).getUsername());
				intent.putExtra("activitiesid", mActivitiesList.get(position-1).getActivitiesid());
				intent.putExtra("image", mActivitiesList.get(position-1).getImage());
				intent.putExtra("title", mActivitiesList.get(position-1).getTitle());
				intent.putExtra("detail", mActivitiesList.get(position-1).getContent());
				intent.putExtra("dotime", mActivitiesList.get(position-1).getDotime());
				intent.putExtra("max_people", mActivitiesList.get(position-1).getMax_number_people());
				intent.putExtra("already_people", mActivitiesList.get(position-1).getAlready_number_people());
				intent.putExtra("participant", mActivitiesList.get(position-1).getParticipant());
				intent.putExtra("isJoin", mActivitiesList.get(position-1).getIsJoin());
				intent.putExtra("isCollect", mActivitiesList.get(position-1).getIsCollect());
				intent.putExtra("time", mActivitiesList.get(position-1).getTime());
				intent.putExtra("listview_position", listview_position);
				intent.putExtra("obj", "2");
				startActivity(intent);
			}
		});
    	
    	mPullToRefreshView = (PullToRefreshView) getActivity().findViewById(R.id.pull_to_refresh);
        mPullToRefreshView.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPullToRefreshView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPullToRefreshView.setRefreshing(false);
                        sort_tab.setVisibility(View.GONE);
                		bar_image_tab.setImageResource(R.drawable.tab_flag);
                		
                		if(NetUtil.isConnect(getActivity())){
                			//创建线程
                            Thread getActivitiesThread = new Thread(getActivities_runnable);
                            //启动线程
                            getActivitiesThread.start();
                		}
                		else {
                			ToastUtils.showToast(getActivity(),"网络未连接");
                		}
                		
                    }
                }, REFRESH_DELAY);
            }
        });
	}
	
	
	private void getEntities(){
		bar_image_tab = (ImageView)getActivity().findViewById(R.id.bar_tab);
		bar_title = (TextView)getActivity().findViewById(R.id.bar_title_activities);
        bar_image_add = (ImageView)getActivity().findViewById(R.id.bar_image_add);
		tab_tip = (TextView)getActivity().findViewById(R.id.bar_tab_text);
		tab_all = (TextView)getActivity().findViewById(R.id.tab_all);
		sort_tab = (LinearLayout)getActivity().findViewById(R.id.sort_tab);
		tab_sport = (TextView)getActivity().findViewById(R.id.tab_sport);
		tab_travel = (TextView)getActivity().findViewById(R.id.tab_travel);
		tab_study = (TextView)getActivity().findViewById(R.id.tab_study);
		tab_casual = (TextView)getActivity().findViewById(R.id.tab_casual);
		mListView = (ListView) getActivity().findViewById(R.id.activity_listview);
		bar_search = (ImageView)getActivity().findViewById(R.id.activities_bar_search);
        search_text = (EditText) getActivity().findViewById(R.id.search_text);
		search_clear = (ImageView) getActivity().findViewById(R.id.search_clear);
		
		LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.item_activities_head, null);
		userPhoto = (ImageView) layout.findViewById(R.id.userPhoto);
		head_username = (TextView)layout.findViewById(R.id.username);
		//加入listview头部
		mListView.addHeaderView(layout);
	}
	
	private void initListeners(){
		bar_image_tab.setOnClickListener(this);
        bar_image_add.setOnClickListener(this);
        tab_all.setOnClickListener(this);
		tab_sport.setOnClickListener(this);
		tab_travel.setOnClickListener(this);
		tab_study.setOnClickListener(this);
		tab_casual.setOnClickListener(this);
		bar_search.setOnClickListener(this);
		search_text.addTextChangedListener(textWatcher);
		search_clear.setOnClickListener(this);
		//监听响应ListView的滚动状态
    	mListView.setOnScrollListener(viewScroll);
	}
	
	private void initView(){
		sort_tab.setVisibility(View.GONE);
		bar_title.setText("活动园");
        bar_image_add.setImageResource(R.drawable.tab_add);
        head_username.setText(username);
        
        if(!NetUtil.isConnect(getActivity())){
        	ToastUtils.showToast(getActivity(),"网络未连接");
			userPhoto.setImageResource(R.drawable.default_photo);
			return;
		}
        
		if(sharedPreferences.getString("userphoto", "").equals("")){
			Bitmap bitmap = BitmapFactory.decodeResource(getActivity().getResources(),R.drawable.default_photo); 
			bitmap = ImageUtils.getRoundedCornerBitmap(bitmap,90f);
			userPhoto.setImageBitmap(bitmap);
		}
		else {
			String imageURL = ServerUrlUtil.SERVER_BASCE_URL+"/rcgl/upload/"+sharedPreferences.getString("userphoto", "");
        	MyImageLoader.loadPhoto(imageURL, userPhoto, 90);
		}
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		switch(v.getId()) {
		case R.id.bar_tab://活动分类查看标签
			if(sort_tab.getVisibility()==View.GONE){
				Log.e("tt", sort_tab.getVisibility()+"");
				sort_tab.setVisibility(View.VISIBLE);
				bar_image_tab.setImageResource(R.drawable.tab_close);
			}
			else{
				Log.e("ff", sort_tab.getVisibility()+"");
				sort_tab.setVisibility(View.GONE);
				/*RelativeLayout.LayoutParams mm = new RelativeLayout.LayoutParams(0, 0);
				sort_tab.setLayoutParams(mm);*/
				bar_image_tab.setImageResource(R.drawable.tab_flag);
			}
			break;
		case R.id.bar_image_add://添加活动页面
			SharedPreferences sharedPreferences = getActivity().getSharedPreferences("loginUserSP", Context.MODE_PRIVATE);
			int userid = sharedPreferences.getInt("userid", 0);
			if(userid==0){
				ToastUtils.showToast(getActivity(),"你还未登录哦，先去登录再来？");
				return;
			}
			else {
	    		intent.setClass(getActivity(), AddActivitiesActivity.class);
	    		startActivity(intent);
			}
			break;
		case R.id.tab_all://“全部”标签
			tab_tip.setText("全部");
			sort = "0";
			sort_tab.setVisibility(View.GONE);
			bar_image_tab.setImageResource(R.drawable.tab_flag);
			showActivitiesThread(runnable);
			break;
		case R.id.tab_sport://“运动”标签
			tab_tip.setText("运动");
			sort = "1";
			sort_tab.setVisibility(View.GONE);
			bar_image_tab.setImageResource(R.drawable.tab_flag);
			showActivitiesThread(runnable);
			break;
		case R.id.tab_travel://“旅行”标签
			tab_tip.setText("旅行");
			sort = "2";
			sort_tab.setVisibility(View.GONE);
			bar_image_tab.setImageResource(R.drawable.tab_flag);
			showActivitiesThread(runnable);
			break;
		case R.id.tab_study://“学习”标签
			tab_tip.setText("学习");
			sort = "3";
			sort_tab.setVisibility(View.GONE);
			bar_image_tab.setImageResource(R.drawable.tab_flag);
			showActivitiesThread(runnable);
			break;
		case R.id.tab_casual://“休闲”标签
			tab_tip.setText("休闲");
			sort = "4";
			sort_tab.setVisibility(View.GONE);
			bar_image_tab.setImageResource(R.drawable.tab_flag);
			showActivitiesThread(runnable);
			break;
		case R.id.activities_bar_search://标题栏查询图标按钮
			if(getActivity().findViewById(R.id.search_item).getVisibility()==View.GONE) {
				getActivity().findViewById(R.id.search_item).setVisibility(View.VISIBLE);
			}
			else {
				getActivity().findViewById(R.id.search_item).setVisibility(View.GONE);
				search_text.setText("");
				//关掉虚拟软键盘
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);    
		        imm.hideSoftInputFromWindow(search_text.getWindowToken(), 0) ; 
			}
			break;
		case R.id.search_clear://清空搜索栏
			search_text.setText("");
			search_clear.setVisibility(View.GONE);
			break;
		}
	}
	
	/** 搜索框文字监听 */
	private TextWatcher textWatcher = new TextWatcher() {  
        @Override 
        public void beforeTextChanged(CharSequence s, int start, int count,  
                int after) {  
            // TODO Auto-generated method stub  
        }  
 
        @Override    
        public void onTextChanged(CharSequence s, int start, int before,     
                int count) {     
        	 // TODO Auto-generated method stub
        	 keyword = search_text.getText().toString().trim();
        	 if(keyword.equals("")) search_clear.setVisibility(View.GONE);
        	 else {
        		 search_clear.setVisibility(View.VISIBLE);
        	 }
             /** 显示活动内容的线程 */
        	 //showActivitiesThread(runnable);
        	 //创建线程
             Thread activitiesThread = new Thread(runnable);
             activitiesThread.start();
        }

		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub
		}                    
    };
    
	/** 显示活动内容 */
	private void showActivitiesThread(Runnable r){
		//显示进度框提示
        pd = LoadingDialog.createLoadingDialog(getActivity(), "");
        pd.show();
        
        //创建线程
        Thread activitiesThread = new Thread(r);
        //启动线程
        activitiesThread.start();
	}
	
	/** 获取活动内容 */
    Runnable runnable =  new Runnable() {
    	@Override
		public void run() {
    		try
            {	
                String myactivities = null;
                myactivities = readFile("myactivities.txt");//读取本地活动资源文件
                if(myactivities==null||myactivities.equals("")){
              	  have_content = false;
              	  activitiesHandler.sendEmptyMessage(0);
              	  return;
                }
                JSONObject jsonObject = new JSONObject(myactivities);
                JSONArray jsonArray = jsonObject.getJSONArray("activities");
                Log.i("activities", jsonArray.toString());
                
                ActivitiesBean actbean=null;//存放活动信息
                mActivitiesList=new ArrayList<ActivitiesBean>();
                for(int i=0;i<jsonArray.length();i++){
          		  	JSONObject obj=jsonArray.getJSONObject(i);
          		  	if(!sort.equals("0")&&!sort.equals(obj.getString("sort"))){
          		  		continue;
          		  	}
          		  	if(keyword.length()>0 && !obj.getString("content").contains(keyword)){
          		  		continue;
          		  	}
          		  	actbean=new ActivitiesBean();
          		  	actbean.setActivitiesid(Integer.parseInt(obj.getString("activitiesid")));
          		  	actbean.setUserid(Integer.parseInt(obj.getString("userid")));
          		  	actbean.setUsername(obj.getString("username"));
          		    actbean.setUserphoto(obj.getString("photo"));
          		  	actbean.setContent(obj.getString("content"));
          		  	actbean.setImage(obj.getString("image"));
          		  	actbean.setDotime(obj.getString("dotime"));
          		  	actbean.setMax_number_people(Integer.valueOf(obj.getString("max_number_people")));
          		  	actbean.setAlready_number_people(Integer.valueOf(obj.getString("already_number_people")));
          		  	actbean.setParticipant(obj.getString("participant"));
          		  	actbean.setSort(obj.getString("sort"));
          		  	actbean.setIsJoin(obj.getString("isJoin"));
          		    actbean.setIsCollect(obj.getString("isCollect"));
				    actbean.setTime(obj.getString("time"));
				    mActivitiesList.add(actbean);
                }  
                activitiesHandler.sendEmptyMessage(200);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
		}
	};
	
	
	
	
	
	/**
     * 处理活动数据显示
     * @date 2015-02-03
     */
	Handler activitiesHandler = new Handler() {
		@Override
		//当有消息发送出来的时候就执行Handler的这个方法 
	    public void handleMessage(Message msg) {
			super.handleMessage(msg);  
			if(msg.what==200){
				//配置适配器  
                activitiesAdapter = new ActivitiesAdapter(getActivity(),mActivitiesList);
          		mListView.setAdapter(activitiesAdapter);
			}
			else if(msg.what==0){
				//配置适配器  
                activitiesAdapter = new ActivitiesAdapter(getActivity(),mActivitiesList);
          		mListView.setAdapter(activitiesAdapter);
			}
			else {
				ToastUtils.showToast(getActivity(),"获取活动失败");
			}
            //只要执行到这里就关闭对话框  
            pd.dismiss(); 
	    }
	};
	
	
	
	
	//响应ListView的滚动状态
	ListView.OnScrollListener viewScroll = new ListView.OnScrollListener(){
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			switch(scrollState){ 
			case OnScrollListener.SCROLL_STATE_IDLE://空闲状态 
				break; 
			case OnScrollListener.SCROLL_STATE_FLING://滚动状态
				break; 
			case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL://触摸后滚动 
				sort_tab.setVisibility(View.GONE);
				bar_image_tab.setImageResource(R.drawable.tab_flag);
				break; 
			}
		}
		
		/** * 正在滚动 * firstVisibleItem第一个Item的位置 * visibleItemCount 可见的Item的数量 * totalItemCount item的总数 */ 
		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
		}
		
	};
	
    /** 写数据到资源文件 */  
	private void writeFile(String fileName,String data) throws IOException{   
	  try{   
	        FileOutputStream fout = getActivity().openFileOutput(fileName, Context.MODE_PRIVATE);   
	        byte [] bytes = data.getBytes();   
	        fout.write(bytes);   
	        fout.close();   
	      }   
	        catch(Exception e){   
	        e.printStackTrace();   
	       }   
	}
	/** 读取资源文件数据 */ 
	public String readFile(String fileName) throws IOException{   
	  String res="";   
	  try{   
	         FileInputStream fin = getActivity().openFileInput(fileName);   
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
	
	
	/** http post方式发送用户输入数据进行查找活动请求 */
    Runnable getActivities_runnable =  new Runnable() {
    	@Override
		public void run() {
    		try{
    			if(!NetUtil.isConnect(getActivity())){
    				ToastUtils.showToast(getActivity(), "网络未连接");
    				pd.dismiss(); 
    				return;
    			}
    			SharedPreferences sharedPreferences = getActivity().getSharedPreferences("loginUserSP", Context.MODE_PRIVATE);
    			int userid = sharedPreferences.getInt("userid", 0);
    			if(userid==0){
    				ToastUtils.showToast(getActivity(), "亲爱的，快去登录吧");
    				return;
    			}
    			List<NameValuePair> pairList = new ArrayList<NameValuePair>();
    			NameValuePair pair_uid = new BasicNameValuePair("userid",String.valueOf(userid));
                pairList.add(pair_uid);
                HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList,HTTP.UTF_8);
                // URL使用基本URL即可，其中不需要加参数
                HttpPost httpPost = new HttpPost(ServerUrlUtil.Server_GetActivitiesList_URL);
                // 将请求体内容加入请求中
                httpPost.setEntity(requestHttpEntity);
                // 需要客户端对象来发送请求
                HttpClient httpClient = new DefaultHttpClient();
                // 发送请求
                HttpResponse httpResponse = httpClient.execute(httpPost);
                //读返回数据  
                String strResult = EntityUtils.toString(httpResponse.getEntity());  
                Log.i("getActivities", strResult);
                //若状态码为200 ok 
                if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK && !strResult.equals("-1")) {  
                	getActivitiesHandler.sendEmptyMessage(200);
                    //将response返回数据写入到资源文件中
                    writeFile("myactivities.txt",strResult);
                }  
                else  
                {  
                	getActivitiesHandler.sendEmptyMessage(0);
                } 
                
            }
            catch (Exception e){
                e.printStackTrace();
            }
    	}
    };
    
    /**
     * 请求活动处理函数
     * @author lims
     * @date 2015-02-03
     */
	Handler getActivitiesHandler = new Handler() {
		@Override
		//当有消息发送出来的时候就执行Handler的这个方法 
	    public void handleMessage(Message msg) {
			super.handleMessage(msg);  
			if(msg.what==200){
				ToastUtils.showToast(getActivity(),"刷新完成");
				//创建线程
		        Thread activitiesThread = new Thread(runnable);
		        //启动线程
		        activitiesThread.start();
			}
			else {
				ToastUtils.showToast(getActivity(),"获取活动失败");
			}
            //只要执行到这里就关闭对话框  
            pd.dismiss(); 
	    }
	};
	

}