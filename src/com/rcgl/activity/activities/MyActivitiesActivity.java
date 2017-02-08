package com.rcgl.activity.activities;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.util.EncodingUtils;
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
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.rcgl.R;
import com.rcgl.adapter.ActivitiesAdapter;
import com.rcgl.app.MuApp;
import com.rcgl.app.MyApplication;
import com.rcgl.bean.ActivitiesBean;
import com.rcgl.data.ShareConstants;
import com.rcgl.util.ServerUrlUtil;
import com.rcgl.util.ToastUtils;
import com.rcgl.util.image.MyImageLoader;
/**
 * 个人活动中心
 * @author lims
 * @date 2015-04-25
 */
public class MyActivitiesActivity extends Activity implements OnClickListener {
	/** 标题栏返回按钮 */
	private ImageView bar_back;
	/** 用户头像 */
	private ImageView iv_userPhoto;
	/** 用户名 */
	private TextView tv_username;
	/** 分类按钮“我发起” */
	private TextView tv_wofaqi;
	/** 分类按钮“我约” */
	private TextView tv_woyue;
	/** 分类按钮“收藏” */
	private TextView tv_collect;
	
	private List<ActivitiesBean>mActivitiesList;
	/** 活动适配器 */
	private ActivitiesAdapter activitiesAdapter;
	/** 界面ListView */
	private ListView mListView = null;
	/** 活动分类,0为未分类，1为“我发起”，2为“我约” */
	private int sort = 1;
	

	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_activities);
        
        MyApplication.initImageLoader(this);
        MuApp.getInstance().addActivity(this);
        
        getEntities();
        initListeners();
        initView();
        showActivitiesThread(runnable);//显示活动内容
        
        mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent();
				ShareConstants.activitiesid = mActivitiesList.get(position).getActivitiesid()+"";
				intent.setClass(MyActivitiesActivity.this, ActivitiesDetailActivity.class);
				intent.putExtra("userid", mActivitiesList.get(position).getUserid());
				intent.putExtra("username", mActivitiesList.get(position).getUsername());
				intent.putExtra("activitiesid", mActivitiesList.get(position).getActivitiesid());
				intent.putExtra("image", mActivitiesList.get(position).getImage());
				intent.putExtra("title", mActivitiesList.get(position).getTitle());
				intent.putExtra("detail", mActivitiesList.get(position).getContent());
				intent.putExtra("dotime", mActivitiesList.get(position).getDotime());
				intent.putExtra("max_people", mActivitiesList.get(position).getMax_number_people());
				intent.putExtra("already_people", mActivitiesList.get(position).getAlready_number_people());
				intent.putExtra("participant", mActivitiesList.get(position).getParticipant());
				intent.putExtra("isJoin", mActivitiesList.get(position).getIsJoin());
				intent.putExtra("isCollect", mActivitiesList.get(position).getIsCollect());
				intent.putExtra("time", mActivitiesList.get(position).getTime());
				startActivity(intent);
			}
		});
	}
	
	private void getEntities(){
		bar_back = (ImageView) findViewById(R.id.bar_back);
		iv_userPhoto = (ImageView) findViewById(R.id.userPhoto);
		tv_username = (TextView) findViewById(R.id.username);
		tv_wofaqi = (TextView) findViewById(R.id.tv_wofaqi);
		tv_woyue = (TextView) findViewById(R.id.tv_woyue);
		tv_collect = (TextView) findViewById(R.id.tv_collect);
		mListView = (ListView) findViewById(R.id.activity_listview);
	}
	
	private void initListeners(){
		bar_back.setOnClickListener(this);
		tv_wofaqi.setOnClickListener(this);
		tv_woyue.setOnClickListener(this);
		tv_collect.setOnClickListener(this);
	}
	
	private void initView(){
		SharedPreferences sharedPreferences = getSharedPreferences("loginUserSP", Context.MODE_PRIVATE);
		String imageURL = ServerUrlUtil.SERVER_BASCE_URL+"/rcgl/upload/"+sharedPreferences.getString("userphoto", "");
		String username = sharedPreferences.getString("username", "未登录");
        MyImageLoader.loadPhoto(imageURL, iv_userPhoto, 90);
        tv_username.setText(username);
		tv_wofaqi.setBackgroundResource(R.drawable.bar_join_activities);
		tv_woyue.setBackgroundResource(R.drawable.bar_exit_activities);
		tv_collect.setBackgroundResource(R.drawable.bar_exit_activities);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		switch(v.getId()){
		case R.id.bar_back:
			MyActivitiesActivity.this.finish();
			break;
		case R.id.tv_wofaqi:
			tv_wofaqi.setBackgroundResource(R.drawable.bar_join_activities);
			tv_woyue.setBackgroundResource(R.drawable.bar_exit_activities);
			tv_collect.setBackgroundResource(R.drawable.bar_exit_activities);
			sort=1;
			showActivitiesThread(runnable);//显示活动内容
			break;
		case R.id.tv_woyue:
			tv_wofaqi.setBackgroundResource(R.drawable.bar_exit_activities);
			tv_woyue.setBackgroundResource(R.drawable.bar_join_activities);
			tv_collect.setBackgroundResource(R.drawable.bar_exit_activities);
			sort=2;
			showActivitiesThread(runnable);//显示活动内容
			break;
		case R.id.tv_collect:
			tv_wofaqi.setBackgroundResource(R.drawable.bar_exit_activities);
			tv_woyue.setBackgroundResource(R.drawable.bar_exit_activities);
			tv_collect.setBackgroundResource(R.drawable.bar_join_activities);
			sort=3;
			showActivitiesThread(runnable);//显示活动内容
			break;
		}
	}
	
	
	/** 显示活动内容 */
	private void showActivitiesThread(Runnable r){
		
		//显示进度框提示
        //LoadingDialog.showLoadingDialog(getApplicationContext(), "");
        
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
          		  	if(sort==1 && !obj.getString("isJoin").equals("-1")){
          		  		continue;
          		  	}
          		  	else if(sort==2 && !obj.getString("isJoin").equals("1")){
          		  		continue;
          		  	}
          		  	else if(sort==3 && !obj.getString("isCollect").equals("1")){
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
                activitiesAdapter = new ActivitiesAdapter(getApplicationContext(),mActivitiesList);
          		mListView.setAdapter(activitiesAdapter);
			}
			else if(msg.what==0){
				//配置适配器  
                activitiesAdapter = new ActivitiesAdapter(getApplicationContext(),mActivitiesList);
          		mListView.setAdapter(activitiesAdapter);
			}
			else {
				ToastUtils.showToast(getApplicationContext(),"获取活动失败");
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
