package com.rcgl.activity.schedule;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.http.util.EncodingUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.rcgl.R;
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
import com.rcgl.util.LoadingDialog;
import com.rcgl.util.ToastUtils;

public class SearchScheduleActivity extends Activity implements OnClickListener {
	/** 搜索框输入文字 */
	private EditText search_text;
	/** 搜索框清除按钮 */
	private ImageView search_clear;
	/** 搜索界面关闭按钮 */
	private TextView search_close;
	/** 搜索关键字 */
	private String keyword = "";
	
	/** 存放该用户所有日程数据 */
	private List<ScheduleBean>mScheduleList;
	/** 存放该用户显示日程数据 */
	private List<ScheduleBean>mScheduleListShow;
	private ScheduleAdapter scheduleAdapter;
	/** 滑动ListView */
	private SwipeMenuListView mListView;
	/** 本地日程文件 */
    String localScheduleStr = null;
    /** 长按设置弹窗 */
	private Dialog settigDialog;
	/** 弹窗标题 */
	private TextView dialog_title;
	/** 弹窗“编辑日程” */
	private TextView dialog_edit;
	/** 弹窗“开启或关闭通知提醒” */
	private TextView dialog_alarm;
	/** 弹窗“设为开放或私密” */
	private TextView dialog_open;
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_search);
        
        //处理关闭退出
        MuApp.getInstance().addActivity(this);
        
        getEntities();
		initListeners();
		initView();
		initDatas();
	}
	
	private void getEntities(){
		search_text = (EditText) findViewById(R.id.search_text);
		search_clear = (ImageView) findViewById(R.id.search_clear);
		search_close = (TextView) findViewById(R.id.search_close);
		mListView = (SwipeMenuListView) findViewById(R.id.listView);
	}
	private void initListeners(){
		search_clear.setOnClickListener(this);
		search_close.setOnClickListener(this);
		search_text.addTextChangedListener(textWatcher);
	}
	private void initView(){
		search_text.setHint("请输入日程关键字");
		search_close.setText("取消");
	}
	private void initDatas(){
		/** 显示本地所有日程的线程 */
		keyword = "";
        Thread showLocalScheduleThread = new Thread(showLocalSchedule_runnable);
        showLocalScheduleThread.start();
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.search_clear://清空搜索框
			search_text.setText("");
			search_clear.setVisibility(View.GONE);
			break;
		case R.id.search_close://关闭查询界面
			//关掉虚拟软键盘
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);    
	        imm.hideSoftInputFromWindow(search_text.getWindowToken(), 0) ; 
			SearchScheduleActivity.this.finish();
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
        	 else search_clear.setVisibility(View.VISIBLE);
        	 /** 显示本地所有日程的线程 */
             Thread showLocalScheduleThread = new Thread(showLocalSchedule_runnable);
             showLocalScheduleThread.start();                  
        }

		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub
		}                    
    };
	
	// step 1. create a MenuCreator
	SwipeMenuCreator creator = new SwipeMenuCreator() {
		@Override
		public void create(SwipeMenu menu) {
			SwipeMenuItem openItem = new SwipeMenuItem(getApplicationContext());
			openItem.setBackground(new ColorDrawable(Color.rgb(0x99, 0xCC,0xCC)));
			openItem.setWidth(dp2px(90));
			openItem.setIcon(R.drawable.tab_yes);
			//openItem.setTitle("Open");
			openItem.setTitleSize(18);
			//openItem.setTitleColor(Color.WHITE);
			menu.addMenuItem(openItem);
			SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
			deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,0x3F, 0x25)));
			deleteItem.setWidth(dp2px(90));
			deleteItem.setIcon(R.drawable.ic_delete);
			menu.addMenuItem(deleteItem);
		}
	};
	
	private int dp2px(int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				getResources().getDisplayMetrics());
	}

	/** 侧滑时点击事件 */
	SwipeMenuListView.OnMenuItemClickListener onMenuClickItem = new OnMenuItemClickListener() {
		@Override
		public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
			switch (index) {
			case 0:
				// open
				ToastUtils.showToast(getApplicationContext(),position+"标记为已完成");
				break;
			case 1:
				//delete(item);
				mScheduleList.remove(position);
				scheduleAdapter.notifyDataSetChanged();
				break;
			}
			return false;
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
	
	/** 长按时事件 */
	SwipeMenuListView.OnItemLongClickListener onLongClickItem = new OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				final int position, long id) {
			//此处直接new一个Dialog对象出来，在实例化的时候传入主题
			settigDialog = new Dialog(getApplicationContext(), R.style.option_dialog);
            //设置它的ContentView
			settigDialog.setContentView(R.layout.dialog_schedule_setting);
            ((TextView) settigDialog.findViewById(R.id.dialog_title)).setText(mScheduleList.get(position).getDotime());
            dialog_edit = (TextView) settigDialog.findViewById(R.id.dialog_edit);
            dialog_edit.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					//读取SharedPreferences中的数据
					SharedPreferences sharedPreferences = getSharedPreferences("loginUserSP", Context.MODE_PRIVATE);
					//getString()第二个参数为缺省值，如果preference中不存在该key，将返回缺省值
					int userid = sharedPreferences.getInt("userid", 0);
					if(userid==0){
						ToastUtils.showToast(getApplicationContext(),"你还未登录哦，先去登录再来？");
						return;
					}
					else {
						Intent intent = new Intent();
			    		intent.setClass(getApplicationContext(), UpdateScheduleActivity.class);
			    		intent.putExtra("scheduleid",mScheduleList.get(position).getScheduleid());
			    		intent.putExtra("content",mScheduleList.get(position).getContent());   
			    		intent.putExtra("dotime",mScheduleList.get(position).getDotime()); 
			    		intent.putExtra("open",mScheduleList.get(position).getOpen());
			    		startActivity(intent);
					}
				}
			});
            dialog_open = (TextView) settigDialog.findViewById(R.id.dialog_open);
            dialog_open.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ToastUtils.showToast(getApplicationContext(),""+mScheduleList.get(position).getScheduleid());
				}
			});
            settigDialog.show();
			return false;
		}
	};		
		
	/** 短按时事件 */
	@SuppressLint("NewApi") 
	OnItemClickListener onClickItem = new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if(((TextView) view.findViewById(R.id.schedule_content)).getMaxLines()==1){
				
				((TextView) view.findViewById(R.id.schedule_content)).setMaxLines(100);
			}
			else {
				((TextView) view.findViewById(R.id.schedule_content)).setMaxLines(1);
			}
		}
	};		
			
	
	/** 显示本地全部日程线程处理 */
	Runnable showLocalSchedule_runnable =  new Runnable() {
    	@Override
		public void run() {
    		try
            {	
                localScheduleStr = readFile("myschedule.txt");//读取本地日程资源文件
                if(localScheduleStr==null||localScheduleStr.equals("")){
              	  return;
                }
                
                showSchedule(localScheduleStr);
                showSchedule_handler.sendEmptyMessage(200);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
		}
	};
	
	/** 显示本地日程handler */
	Handler showSchedule_handler = new Handler() {
		@Override
		//当有消息发送出来的时候就执行Handler的这个方法 
	    public void handleMessage(Message msg) {
			super.handleMessage(msg);  
			if(msg.what==200){
                scheduleAdapter = new ScheduleAdapter(getApplicationContext(),mScheduleList);
          		mListView.setAdapter(scheduleAdapter);
        		mListView.setMenuCreator(creator);
        		mListView.setOnMenuItemClickListener(onMenuClickItem);
        		mListView.setOnSwipeListener(swipeListener);
        		//listView.setCloseInterpolator(new BounceInterpolator());
        		mListView.setOnItemLongClickListener(onLongClickItem);
        		mListView.setOnItemClickListener(onClickItem);
			}
			else {
				ToastUtils.showToast(getApplicationContext(),"获取用户日程失败");
			}
            //只要执行到这里就关闭对话框  
			LoadingDialog.closeLoadingDialog(); 
	    }
	};
	
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
		    schedule.setImage(DayManage.showDayImage(obj.getString("dotime").substring(8, 10)));
		    schedule.setTime(obj.getString("time"));
		    mScheduleList.add(schedule);
        }
	}
	
		
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
