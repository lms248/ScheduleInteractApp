package com.rcgl.fragment;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import org.apache.http.util.EncodingUtils;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.rcgl.R;
import com.rcgl.activity.schedule.AddScheduleActivity;
import com.rcgl.activity.schedule.UpdateScheduleActivity;
import com.rcgl.adapter.ScheduleAdapter;
import com.rcgl.app.MuApp;
import com.rcgl.bean.ScheduleBean;
import com.rcgl.swipemenulistview.util.SwipeMenu;
import com.rcgl.swipemenulistview.util.SwipeMenuCreator;
import com.rcgl.swipemenulistview.util.SwipeMenuItem;
import com.rcgl.swipemenulistview.util.SwipeMenuListView;
import com.rcgl.swipemenulistview.util.SwipeMenuListView.OnMenuItemClickListener;
import com.rcgl.swipemenulistview.util.SwipeMenuListView.OnSwipeListener;
import com.rcgl.util.DayManage;
import com.rcgl.util.ExpandableTextView;
import com.rcgl.util.LoadingDialog;
import com.rcgl.util.NetUtil;
import com.rcgl.util.ServerUrlUtil;
import com.rcgl.util.ToastUtils;

public class ScheduleFragment extends Fragment implements OnClickListener {
	/** 存放该用户所有日程数据 */
	private List<ScheduleBean>mScheduleList;
	/** 存放该用户显示日程数据 */
	private List<ScheduleBean>mScheduleListShow;
	private ScheduleAdapter scheduleAdapter;
	/** 滑动ListView */
	private SwipeMenuListView mListView;
	/** 长按设置弹窗 */
	private Dialog settigDialog;
	/** 日程内容 */
	private ExpandableTextView schedule_content;
	/** 标题栏的标题文字 */
	private TextView bar_title;
	/** 标题栏的查询日程图标 */
	private ImageView bar_search;
	/** 标题栏的添加日程图标 */
	private ImageView bar_add;
	
	/** 搜索框输入文字 */
	private EditText search_text;
	/** 搜索框清除按钮 */
	private ImageView search_clear;
	/** 搜索关键字 */
	private String keyword = "";
	/** 搜索框底部分割线*/
	private ImageView search_bottom_line;
	
	/** 弹窗标题 */
	private TextView dialog_title;
	/** 弹窗“编辑日程” */
	private TextView dialog_edit;
	/** 弹窗“开启或关闭通知提醒” */
	private TextView dialog_alarm;
	/** 弹窗“设为开放或私密” */
	private TextView dialog_open;
	/** 弹窗“标为已完成或未完成” */
	private TextView dialog_complete;
	
	/** 判断内容是否单行 */
	Boolean isSingleLine = true;
	
	Boolean have_content = true;
	
	/** http请求参数 */
    List<NameValuePair> pairList;
    /** 本地日程文件 */
    String localScheduleStr = null;
    
    /** ListView的位置定位 */
    int listview_index = 0;
    /** ListView的位置偏移量 */
    int listview_top = 0;
    
    private final SparseBooleanArray mExpanded = new SparseBooleanArray();
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getActivity().findViewById(R.id.bar).setVisibility(View.VISIBLE);
		return inflater.inflate(R.layout.fragment_schedule, null);		
	}	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		MuApp.getInstance().addActivity(getActivity());
		
		getEntities();
		initListeners();
		initView();
		initDatas();
		
		/*// 保存当前第一个可见的item的索引和偏移量
		int listview_index = mListView.getFirstVisiblePosition();
		View v = mListView.getChildAt(0);
		int listview_top = (v == null) ? 0 : v.getTop();
		// ...
		//根据上次保存的index和偏移量恢复上次的位置
		mListView.setSelectionFromTop(index, top);*/
		
	}
	
	private void getEntities(){
		bar_title = (TextView) getActivity().findViewById(R.id.bar_title);
        bar_search = (ImageView) getActivity().findViewById(R.id.bar_search);
        search_text = (EditText) getActivity().findViewById(R.id.search_text);
		search_clear = (ImageView) getActivity().findViewById(R.id.search_clear);
		search_bottom_line = (ImageView) getActivity().findViewById(R.id.search_bottom_line);
        bar_add = (ImageView) getActivity().findViewById(R.id.bar_image);
        mListView = (SwipeMenuListView) getActivity().findViewById(R.id.listView);
		schedule_content = (ExpandableTextView) getActivity().findViewById(R.id.schedule_content);
	}
	private void initListeners(){
        bar_add.setOnClickListener(this);
        bar_search.setOnClickListener(this);
        search_clear.setOnClickListener(this);
		search_text.addTextChangedListener(textWatcher);
	}
	private void initView(){
		bar_title.setText("我的日程");
		bar_search.setImageResource(R.drawable.tab_search);
        bar_search.setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.no_content_tip).setVisibility(View.INVISIBLE);
        bar_add.setImageResource(R.drawable.tab_add);
	}
	private void initDatas(){
		//显示进度框提示
		LoadingDialog.showLoadingDialog(getActivity(), "");
        /** 显示本地日程的线程 */
		keyword = "";
        Thread showLocalScheduleThread = new Thread(showLocalSchedule_runnable);
        showLocalScheduleThread.start();
        
        if(NetUtil.isConnect(getActivity())){
        	/** 加载网络日程数据的线程 */
            Thread loadNetScheduleThread = new Thread(loadNetSchedule_runnable);
            loadNetScheduleThread.start();
		}		
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.bar_image://添加日程
			onAddSchedulePage();
			break;
		case R.id.bar_search://标题栏搜索图标
			if(getActivity().findViewById(R.id.search_item).getVisibility()==View.GONE) {
				getActivity().findViewById(R.id.search_item).setVisibility(View.VISIBLE);
				search_bottom_line.setVisibility(View.VISIBLE);
			}
			else {
				getActivity().findViewById(R.id.search_item).setVisibility(View.GONE);
				search_text.setText("");
				search_bottom_line.setVisibility(View.GONE);
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
        	//ToastUtils.showToast(getApplicationContext(), "beforeTextChanged");
        }  
 
         @Override    
        public void onTextChanged(CharSequence s, int start, int before,     
                int count) {     
        	// TODO Auto-generated method stub
        	 //ToastUtils.showToast(getApplicationContext(), "onTextChanged");
        	 keyword = search_text.getText().toString().trim();
        	 if(keyword.equals("")) search_clear.setVisibility(View.GONE);
        	 else search_clear.setVisibility(View.VISIBLE);
        	 /** 显示本地所有日程的线程 */
             Thread showLocalScheduleThread = new Thread(showLocalSchedule_runnable);
             showLocalScheduleThread.start();                  
        }

		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub
			//ToastUtils.showToast(getApplicationContext(), "afterTextChanged");
		}                    
    };
	
	// step 1. create a MenuCreator
	SwipeMenuCreator creator = new SwipeMenuCreator() {
		@Override
		public void create(SwipeMenu menu) {
			/*SwipeMenuItem openItem = new SwipeMenuItem(getActivity().getApplicationContext());
			openItem.setBackground(new ColorDrawable(Color.rgb(0x99, 0xCC,0xCC)));
			openItem.setWidth(dp2px(90));
			openItem.setIcon(R.drawable.ic_share);
			menu.addMenuItem(openItem);*/
			SwipeMenuItem deleteItem = new SwipeMenuItem(getActivity().getApplicationContext());
			deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,0x3F, 0x25)));
			deleteItem.setWidth(dp2px(90));
			deleteItem.setIcon(R.drawable.ic_delete);
			menu.addMenuItem(deleteItem);
		}
	};
	
	/** 添加日程页面 */
	private void onAddSchedulePage(){
		SharedPreferences sharedPreferences = getActivity().getSharedPreferences("loginUserSP", Context.MODE_PRIVATE);
		int userid = sharedPreferences.getInt("userid", 0);
		if(userid==0){
			ToastUtils.showToast(getActivity(),"你还未登录哦，先去登录再来？");
			return;
		}
		else {
			Intent intent = new Intent();
    		intent.setClass(getActivity(), AddScheduleActivity.class);
    		startActivity(intent);
    		//finish();
		}
	}
    
	/** 侧滑时点击事件 */
	SwipeMenuListView.OnMenuItemClickListener onMenuClickItem = new OnMenuItemClickListener() {
		@Override
		public boolean onMenuItemClick(final int position, SwipeMenu menu, int index) {
			switch (index) {
			/*case 0:
				// open
				ToastUtils.showToast(getActivity(),"日程分享功能尚在开发中");
				break;*/
			case 0:
				final Dialog deleteDialog = new Dialog(getActivity(), R.style.option_dialog);
				deleteDialog.setContentView(R.layout.dialog_confirm);
	            ((TextView) deleteDialog.findViewById(R.id.dialog_title)).setText("你真的要删除吗？");
	            Button btn_yes = (Button) deleteDialog.findViewById(R.id.btn_yes);
	            btn_yes.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						deleteSchedule(mScheduleList.get(position).getScheduleid());
						mScheduleList.remove(position);
						scheduleAdapter.notifyDataSetChanged();
						deleteDialog.dismiss();
					}
				});
	            Button btn_no = (Button) deleteDialog.findViewById(R.id.btn_no);
	            btn_no.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						deleteDialog.dismiss();
					}
				});
	            deleteDialog.show();
				break;
			}
			return false;
		}
	};
	
	
	/** 长按时事件 */
	SwipeMenuListView.OnItemLongClickListener onLongClickItem = new OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				final int position, long id) {
			//此处直接new一个Dialog对象出来，在实例化的时候传入主题
			settigDialog = new Dialog(getActivity(), R.style.option_dialog);
            //设置它的ContentView
			settigDialog.setContentView(R.layout.dialog_schedule_setting);
            ((TextView) settigDialog.findViewById(R.id.dialog_title)).setText(mScheduleList.get(position).getDotime());
            dialog_edit = (TextView) settigDialog.findViewById(R.id.dialog_edit);
            dialog_edit.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					schedule_edit(position);
				}
			});
            dialog_alarm = (TextView) settigDialog.findViewById(R.id.dialog_alarm);
            if(mScheduleList.get(position).getAlarm()==1){
            	dialog_alarm.setText("关闭通知提醒");
            }
            else dialog_alarm.setText("开启通知提醒");
            dialog_alarm.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(mScheduleList.get(position).getAlarm()==1){
						setAlarmStatus(mScheduleList.get(position).getScheduleid(),0);
					}
					else setAlarmStatus(mScheduleList.get(position).getScheduleid(),1);
					settigDialog.dismiss();
				}
			});
            
            dialog_open = (TextView) settigDialog.findViewById(R.id.dialog_open);
            if(mScheduleList.get(position).getOpen()==1){
            	dialog_open.setText("设为私密");
            }
            else dialog_open.setText("设为公开");
            dialog_open.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(mScheduleList.get(position).getOpen()==1){
						setOpenStatus(mScheduleList.get(position).getScheduleid(),0);
					}
					else setOpenStatus(mScheduleList.get(position).getScheduleid(),1);
					settigDialog.dismiss();
				}
			});
            
            dialog_complete = (TextView) settigDialog.findViewById(R.id.dialog_complete);
            if(mScheduleList.get(position).getStatus()==1){
            	dialog_complete.setText("标为未完成");
            }
            else dialog_complete.setText("标为已完成");
            dialog_complete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(mScheduleList.get(position).getStatus()==1){
						setCompleteStatus(mScheduleList.get(position).getScheduleid(),0);
					}
					else setCompleteStatus(mScheduleList.get(position).getScheduleid(),1);
					settigDialog.dismiss();
				}
			});
            
            settigDialog.show();
            
            //listview定位恢复记录
			listview_index = mListView.getFirstVisiblePosition();
			View v1 = mListView.getChildAt(0);
			listview_top = (v1 == null) ? 0 : v1.getTop();
            
			return false;
		}
	};
	
	private void schedule_edit(int position){
		//读取SharedPreferences中的数据
		SharedPreferences sharedPreferences = getActivity().getSharedPreferences("loginUserSP", Context.MODE_PRIVATE);
		//getString()第二个参数为缺省值，如果preference中不存在该key，将返回缺省值
		int userid = sharedPreferences.getInt("userid", 0);
		if(userid==0){
			ToastUtils.showToast(getActivity(),"你还未登录哦，先去登录再来？");
			return;
		}
		else {
			Intent intent = new Intent();
    		intent.setClass(getActivity(), UpdateScheduleActivity.class);
    		intent.putExtra("scheduleid",mScheduleList.get(position).getScheduleid());
    		intent.putExtra("content",mScheduleList.get(position).getContent());   
    		intent.putExtra("dotime",mScheduleList.get(position).getDotime()); 
    		intent.putExtra("alarm",mScheduleList.get(position).getAlarm());
    		intent.putExtra("open",mScheduleList.get(position).getOpen());
    		startActivity(intent);
		}
	}
	
	/** 短按时事件 */
	OnItemClickListener onClickItem = new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> parent, View view, final int position,
				long id) {
			TextView schedule_content = (TextView) view.findViewById(R.id.schedule_content);
			if(schedule_content.getLineCount()==1){
				schedule_content.setSingleLine(false);
			}
			else {
				schedule_content.setSingleLine(true);
			}
			/*if(((TextView) view.findViewById(R.id.schedule_content)).getMaxLines()==1){
				
				((TextView) view.findViewById(R.id.schedule_content)).setMaxLines(100);
				isSingleLine=false;
			}
			else {
				((TextView) view.findViewById(R.id.schedule_content)).setMaxLines(1);
				isSingleLine=true;
			}*/
		}
	};
	
	SwipeMenuListView.OnSwipeListener swipeListener = new OnSwipeListener() {
		@Override
		public void onSwipeStart(int position) {
			// swipe start
		}
		@Override
		public void onSwipeEnd(int position) {
			// swipe end
		}
	};
	
    Runnable showLocalSchedule_runnable =  new Runnable() {
    	@Override
		public void run() {
    		try
            {	
    			showSchedule_handler.sendEmptyMessage(200);
                
                localScheduleStr = readFile("myschedule.txt");//读取本地日程资源文件
                if(localScheduleStr==null||localScheduleStr.equals("")){
              	  have_content = false;
              	  show_no_content_tip();
              	  return;
                }
                
                showSchedule(localScheduleStr);
                
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
		}
	};
	
	Handler showSchedule_handler = new Handler() {
		@Override
		//当有消息发送出来的时候就执行Handler的这个方法 
	    public void handleMessage(Message msg) {
			super.handleMessage(msg);  
			if(msg.what==200){
				//配置适配器  
                scheduleAdapter = new ScheduleAdapter(getActivity(),mScheduleList);
          		mListView.setAdapter(scheduleAdapter);
        		mListView.setMenuCreator(creator);
        		mListView.setOnMenuItemClickListener(onMenuClickItem);
        		mListView.setOnSwipeListener(swipeListener);
        		//listView.setCloseInterpolator(new BounceInterpolator());
        		mListView.setOnItemLongClickListener(onLongClickItem);
        		mListView.setOnItemClickListener(onClickItem);
        		
        		ListView_position_handler.sendEmptyMessage(200);//定位到上一次位置
			}
			else {
				ToastUtils.showToast(getActivity(),"获取用户日程失败");
				show_no_content_tip();
			}
            //只要执行到这里就关闭对话框  
			LoadingDialog.closeLoadingDialog(); 
	    }
	};
	
	Runnable loadNetSchedule_runnable =  new Runnable() {
    	@Override
		public void run() {
    		try
            {	
    			if(!NetUtil.isConnect(getActivity())){
    				ToastUtils.showToast(getActivity(),"网络未连接");
    				return;
    			}
    			//读取SharedPreferences中的数据
    			SharedPreferences sharedPreferences = getActivity().getSharedPreferences("loginUserSP", Context.MODE_PRIVATE);
    			//getString()第二个参数为缺省值，如果preference中不存在该key，将返回缺省值
    			int userid = sharedPreferences.getInt("userid", 0);
    			NameValuePair pair_userid = new BasicNameValuePair("userid",userid+"");
                
                pairList = new ArrayList<NameValuePair>();
                pairList.add(pair_userid);
                
    			HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList,HTTP.UTF_8);
                HttpPost httpPost = new HttpPost(ServerUrlUtil.Server_GetScheduleList_URL);
                // 将请求体内容加入请求中
                httpPost.setEntity(requestHttpEntity);
                // 需要客户端对象来发送请求
                HttpClient httpClient = new DefaultHttpClient();
                // 发送请求
                HttpResponse httpResponse = httpClient.execute(httpPost);
                //读返回数据  
                String strResult = EntityUtils.toString(httpResponse.getEntity());  
                Log.e("strResult", strResult);
                //若状态码为200 ok 
                if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK && !strResult.equals("-1")) {  
                	//异步更新日程数据
                	if(localScheduleStr==null || !localScheduleStr.equals(strResult)){
                		localScheduleStr = strResult;
                		showSchedule(strResult);
                		showSchedule_handler.sendEmptyMessage(200);
                		writeFile("myschedule.txt",localScheduleStr);
                	}
                }  
                else {  
                	loadNetSchedule_handler.sendEmptyMessage(0);
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
	Handler loadNetSchedule_handler = new Handler() {
		@Override
		//当有消息发送出来的时候就执行Handler的这个方法 
	    public void handleMessage(Message msg) {
			super.handleMessage(msg);  
			if(msg.what==200){
				
			}
			else {
				ToastUtils.showToast(getActivity(),"更新失败");
			}
            //只要执行到这里就关闭对话框  
			LoadingDialog.closeLoadingDialog(); 
	    }
	};
	
	/**
     * http请求处理函数之删除日程
     * @date 2015-06-15
     */
	Handler deleteSchedule_handler = new Handler() {
		@Override
		//当有消息发送出来的时候就执行Handler的这个方法 
	    public void handleMessage(Message msg) {
			super.handleMessage(msg);  
			if(msg.what==200){
				ToastUtils.showToast(getActivity(),"删除日程完成");
			}
			else {
				ToastUtils.showToast(getActivity(),"删除日程失败");
			}
            //只要执行到这里就关闭对话框  
			//LoadingDialog.closeLoadingDialog(); 
	    }
	};
	
	/** 设置日程完成情况 ,complete==0为未完成，complete==1为完成*/
	private void setCompleteStatus(int scheduleid, int status){
		SharedPreferences sharedPreferences = getActivity().getSharedPreferences("loginUserSP", Context.MODE_PRIVATE);
		int userid = sharedPreferences.getInt("userid", 0);
		NameValuePair pair_userid = new BasicNameValuePair("userid",userid+"");
		NameValuePair pair_scheduleid = new BasicNameValuePair("scheduleid",scheduleid+"");
		NameValuePair pair_complete = new BasicNameValuePair("complete",status+"");
		NameValuePair pair_operate = new BasicNameValuePair("operate","complete");
		
        pairList = new ArrayList<NameValuePair>();
        pairList.add(pair_userid);
        pairList.add(pair_scheduleid);
        pairList.add(pair_complete);
        pairList.add(pair_operate);
        
        if(NetUtil.isConnect(getActivity())){
        	/** 管理日程数据的线程 */
            Thread manageScheduleThread = new Thread(manageSchedule_runnable);
            manageScheduleThread.start();
		}
	}
	
	/** 设置闹铃通知 ,alarm==0为不开启通知，alarm==1为开启通知*/
	private void setAlarmStatus(int scheduleid, int status){
		SharedPreferences sharedPreferences = getActivity().getSharedPreferences("loginUserSP", Context.MODE_PRIVATE);
		int userid = sharedPreferences.getInt("userid", 0);
		NameValuePair pair_userid = new BasicNameValuePair("userid",userid+"");
		NameValuePair pair_scheduleid = new BasicNameValuePair("scheduleid",scheduleid+"");
		NameValuePair pair_complete = new BasicNameValuePair("alarm",status+"");
		NameValuePair pair_operate = new BasicNameValuePair("operate","alarm");
		
        pairList = new ArrayList<NameValuePair>();
        pairList.add(pair_userid);
        pairList.add(pair_scheduleid);
        pairList.add(pair_complete);
        pairList.add(pair_operate);
        
        if(NetUtil.isConnect(getActivity())){
        	/** 管理日程数据的线程 */
            Thread manageScheduleThread = new Thread(manageSchedule_runnable);
            manageScheduleThread.start();
		}
	}
	
	/** 设置公开或私密 ,open==0为私密，open==1为公开*/
	private void setOpenStatus(int scheduleid, int status){
		SharedPreferences sharedPreferences = getActivity().getSharedPreferences("loginUserSP", Context.MODE_PRIVATE);
		int userid = sharedPreferences.getInt("userid", 0);
		NameValuePair pair_userid = new BasicNameValuePair("userid",userid+"");
		NameValuePair pair_scheduleid = new BasicNameValuePair("scheduleid",scheduleid+"");
		NameValuePair pair_complete = new BasicNameValuePair("open",status+"");
		NameValuePair pair_operate = new BasicNameValuePair("operate","open");
		
        pairList = new ArrayList<NameValuePair>();
        pairList.add(pair_userid);
        pairList.add(pair_scheduleid);
        pairList.add(pair_complete);
        pairList.add(pair_operate);
        
        if(NetUtil.isConnect(getActivity())){
        	/** 管理日程数据的线程 */
            Thread manageScheduleThread = new Thread(manageSchedule_runnable);
            manageScheduleThread.start();
		}
	}
	
	/** 删除日程 */
	private void deleteSchedule(int scheduleid){
		NameValuePair pair_scheduleid = new BasicNameValuePair("scheduleid",scheduleid+"");
		
        pairList = new ArrayList<NameValuePair>();
        pairList.add(pair_scheduleid);
        
        if(NetUtil.isConnect(getActivity())){
        	/** 管理日程数据的线程 */
            Thread deleteScheduleThread = new Thread(deleteSchedule_runnable);
            deleteScheduleThread.start();
		}
	}
	
	/** 日程管理线程，管理完成状态、闹铃通知设置和设置 */
	Runnable manageSchedule_runnable =  new Runnable() {
    	@Override
		public void run() {
    		try
            {	
    			if(!NetUtil.isConnect(getActivity())){
    				ToastUtils.showToast(getActivity(),"网络未连接");
    				return;
    			}
    			
    			HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList,HTTP.UTF_8);
                HttpPost httpPost = new HttpPost(ServerUrlUtil.Server_ScheduleManage_URL);
                // 将请求体内容加入请求中
                httpPost.setEntity(requestHttpEntity);
                // 需要客户端对象来发送请求
                HttpClient httpClient = new DefaultHttpClient();
                // 发送请求
                HttpResponse httpResponse = httpClient.execute(httpPost);
                //读返回数据  
                String strResult = EntityUtils.toString(httpResponse.getEntity());  
                Log.e("strResult", strResult);
                //若状态码为200 ok 
                if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK && !strResult.equals("-1")) {  
                	//异步更新日程数据
                	if(localScheduleStr==null || !localScheduleStr.equals(strResult)){
                		localScheduleStr = strResult;
                		showSchedule(strResult);
                		showSchedule_handler.sendEmptyMessage(200);
                		writeFile("myschedule.txt",localScheduleStr);
                	}
                }  
                else {  
                	loadNetSchedule_handler.sendEmptyMessage(0);
                } 
            }
    		catch (Exception e)
    		{
    			e.printStackTrace();
    		}
    	}
	};
	
	/** 删除日程线程 */
	Runnable deleteSchedule_runnable =  new Runnable() {
    	@Override
		public void run() {
    		try
            {	
    			if(!NetUtil.isConnect(getActivity())){
    				ToastUtils.showToast(getActivity(),"网络未连接");
    				return;
    			}
    			
    			HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList,HTTP.UTF_8);
                HttpPost httpPost = new HttpPost(ServerUrlUtil.Server_DeleteSchedule_URL);
                // 将请求体内容加入请求中
                httpPost.setEntity(requestHttpEntity);
                // 需要客户端对象来发送请求
                HttpClient httpClient = new DefaultHttpClient();
                // 发送请求
                HttpResponse httpResponse = httpClient.execute(httpPost);
                //读返回数据  
                String strResult = EntityUtils.toString(httpResponse.getEntity());  
                Log.e("strResult", strResult);
                //若状态码为200 ok 
                if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK && !strResult.equals("-1")) {  
                	deleteSchedule_handler.sendEmptyMessage(200);
                }  
                else {  
                	deleteSchedule_handler.sendEmptyMessage(0);
                } 
            }
    		catch (Exception e)
    		{
    			e.printStackTrace();
    		}
    	}
	};
	
	
	/** 提示是否有内容 */
	private void show_no_content_tip(){
		if(!have_content){
			getActivity().findViewById(R.id.no_content_tip).setVisibility(View.VISIBLE);
		}
	}
	private int dp2px(int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				getResources().getDisplayMetrics());
	}
	
	/** 
	 * 读取JSONArray数据显示日程到手机客户端
	 * @param scheduleStr JSONArray数据
	 * @throws JSONException
	 * @author lims
	 * @date 2015-04-01
	 */
	private void showSchedule(String scheduleStr) throws JSONException{
		Date now = new Date(); //new Date()为获取当前系统时间
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");///设置日期格式
		String nowTime = df.format(now);
        
        JSONObject jsonObject = new JSONObject(scheduleStr);
        JSONArray jsonArray = jsonObject.getJSONArray("schedules");
        Map<String,Object> mMap;
        ScheduleBean schedule=null;//存放日程信息
        mScheduleList=new ArrayList<ScheduleBean>();
        for(int i=0;i<jsonArray.length();i++){
  			JSONObject obj=jsonArray.getJSONObject(i);
  			if(nowTime.compareTo(obj.getString("dotime"))>0){
  				continue;
  			}
  			if(keyword.length()>0 && !obj.getString("content").contains(keyword)){
  				continue;
  			}
		    schedule=new ScheduleBean();
		    schedule.setScheduleid(Integer.parseInt(obj.getString("scheduleid")));
		    schedule.setUserid(Integer.parseInt(obj.getString("userid")));
		    schedule.setContent(obj.getString("content"));
		    schedule.setDotime(obj.getString("dotime"));
		    schedule.setOpen(Integer.parseInt(obj.getString("open")));
		    schedule.setAlarm(Integer.parseInt(obj.getString("alarm")));
		    schedule.setStatus(Integer.parseInt(obj.getString("status")));
		    schedule.setImage(DayManage.showDayImage(obj.getString("dotime").substring(8, 10)));
		    schedule.setTime(obj.getString("time"));
		    mScheduleList.add(schedule);
        }
        for(int i=jsonArray.length()-1;i>=0;i--){
  			JSONObject obj=jsonArray.getJSONObject(i);
  			if(nowTime.compareTo(obj.getString("dotime"))<=0){
  				continue;
  			}
  			if(keyword.length()>0 && !obj.getString("content").contains(keyword)){
  				continue;
  			}
		    schedule=new ScheduleBean();
		    schedule.setScheduleid(Integer.parseInt(obj.getString("scheduleid")));
		    schedule.setUserid(Integer.parseInt(obj.getString("userid")));
		    schedule.setContent(obj.getString("content"));
		    schedule.setDotime(obj.getString("dotime"));
		    schedule.setOpen(Integer.parseInt(obj.getString("open")));
		    schedule.setAlarm(0);
		    schedule.setStatus(Integer.parseInt(obj.getString("status")));
		    schedule.setImage(DayManage.showDayImage(obj.getString("dotime").substring(8, 10)));
		    schedule.setTime(obj.getString("time"));
		    mScheduleList.add(schedule);
        }
        
        mScheduleListShow=new ArrayList<ScheduleBean>();
        if(mScheduleList.size()>20){
        	for(int i=0;i<20;i++){
            	mScheduleListShow.add(mScheduleList.get(i));
            }
        }
        else mScheduleListShow = mScheduleList;
	}
	
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
	
	/**
     * ListView位置定位
     * @date 2015-05-02
     */
	Handler ListView_position_handler = new Handler() {
		@Override
		//当有消息发送出来的时候就执行Handler的这个方法 
	    public void handleMessage(Message msg) {
			super.handleMessage(msg);  
			/*if(msg.what==200){
				
			}
			else {
				
			}*/
			//根据上次保存的index和偏移量恢复上次的位置
			mListView.setSelectionFromTop(listview_index, listview_top);
            //只要执行到这里就关闭对话框  
			//LoadingDialog.closeLoadingDialog(); 
	    }
	};
	

}
