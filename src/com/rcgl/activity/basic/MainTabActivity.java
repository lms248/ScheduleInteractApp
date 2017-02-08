package com.rcgl.activity.basic;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.rcgl.R;
import com.rcgl.app.MuApp;
import com.rcgl.fragment.ActivitiesFragment;
import com.rcgl.fragment.FriendFragment;
import com.rcgl.fragment.MoreFragment;
import com.rcgl.fragment.ScheduleFragment;
import com.rcgl.util.ToastUtils;

/**
 * @author lms
 * 功能描述：自定义TabHost
 */
public class MainTabActivity extends FragmentActivity {
	
	//定义FragmentTabHost对象
	private FragmentTabHost mTabHost;
	
	//定义一个布局
	private LayoutInflater layoutInflater;
		
	//定义数组来存放Fragment界面
	private Class fragmentArray[] = {ScheduleFragment.class,FriendFragment.class,
			ActivitiesFragment.class,MoreFragment.class};
	
	//定义数组来存放按钮图片
	private int mImageViewArray[] = {R.drawable.tab_schedule_btn,R.drawable.tab_friend_btn,
			R.drawable.tab_activities_btn,R.drawable.tab_more_btn};
	
	//Tab选项卡的文字
	private String mTextviewArray[] = {"日程", "好友", "活动", "更多"};
	
	/** 标题栏的标题文字 */
	private TextView bar_title;
	/** 标题栏的右端图片 */
	private ImageView bar_image;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_tab_layout);
        
        initView();
        
        bar_title = (TextView)findViewById(R.id.bar_title);
        bar_title.setText("日程管理");
        bar_image = (ImageView)findViewById(R.id.bar_image);
        
        int page_tab;
        page_tab = getIntent().getIntExtra("page_tab", 0);
        mTabHost.setCurrentTab(page_tab);
        
        MuApp.getInstance().addActivity(this);
        
	}
	
	/**
	 * 初始化组件
	 */
	private void initView(){
		//实例化布局对象
		layoutInflater = LayoutInflater.from(this);
				
		//实例化TabHost对象，得到TabHost
		mTabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
		mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);	
		
		//得到fragment的个数
		int count = fragmentArray.length;	
				
		for(int i = 0; i < count; i++){	
			//为每一个Tab按钮设置图标、文字和内容
			TabSpec tabSpec = mTabHost.newTabSpec(mTextviewArray[i]).setIndicator(getTabItemView(i));
			//将Tab按钮添加进Tab选项卡中
			mTabHost.addTab(tabSpec, fragmentArray[i], null);
			//设置Tab按钮的背景
			//mTabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.selector_tab_background);
		}
	}
				
	/**
	 * 给Tab按钮设置图标和文字
	 */
	private View getTabItemView(int index){
		View view = layoutInflater.inflate(R.layout.tab_item_view, null);
	
		ImageView imageView = (ImageView) view.findViewById(R.id.imageview);
		imageView.setImageResource(mImageViewArray[index]);
		
		TextView textView = (TextView) view.findViewById(R.id.textview);		
		textView.setText(mTextviewArray[index]);
	
		return view;
	}
	
    
    private static Boolean isQuit = false;
	private Timer timer = new Timer();
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (isQuit == false) {
				isQuit = true;
				ToastUtils.showToast(getBaseContext(), "再次点击退出应用");
				TimerTask task = null;
				task = new TimerTask() {
					@Override
					public void run() {
						isQuit = false;
					}
				};
				timer.schedule(task, 2000);
			} else {
				MuApp.getInstance().exit();
			}
		} else {
		}
		return false;
	}
    
}
