package com.rcgl.activity.activities;

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
import org.apache.http.util.EntityUtils;

import android.app.ActionBar.LayoutParams;
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
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.rcgl.R;
import com.rcgl.activity.basic.MainTabActivity;
import com.rcgl.app.MyApplication;
import com.rcgl.data.ShareConstants;
import com.rcgl.util.NetUtil;
import com.rcgl.util.ServerUrlUtil;
import com.rcgl.util.ToastUtils;
import com.rcgl.util.image.MyImageLoader;
/**
 * 活动详情
 * @author lims
 * @date 2015-04-03
 */
public class ActivitiesDetailActivity extends Activity implements OnClickListener{
	/** 标题栏的左端返回图片 */
	private ImageView bar_image_back;
	/** 标题栏的右端收藏图片 */
	private ImageView bar_image_collet;
	/** 标题栏的右端分享图片 */
	private ImageView bar_image_share;
	/** 顶端图片 */
	private ImageView iv_image;
	/** 活动内容 */
	private TextView activities_content;
	/** 活动地址 */
	private TextView activities_address;
	/** 活动时间 */
	private TextView activities_dotime;
	/** 活动已报人数 */
	private TextView activities_max_people;//max_mumber_people
	/** 活动已报人数 */
	private TextView activities_already_people;
	/** 发起人 */
	private TextView organizer_name;
	/** 参与者 */
	private TextView participant_name;
	/** 报名按钮 */
	private TextView join_activities;
	/** http请求参数 */
    List<NameValuePair> pairList;
    /* 读取SharedPreferences中的数据 */
	SharedPreferences sharedPreferences;
	/** 是否报名，-1为我发起，0为否，1为是 */
	private String isJoin = "0";
	/** 是否收藏，0为否，1为是 */
	String isCollect = "0";
	/** 已经报名人数 */
	int already_people = 0;
	
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_activities);
        
        MyApplication.initImageLoader(this);
        
		getEntities();
		initListeners();
		initView();
	}
	
	
	private void getEntities(){
		bar_image_back = (ImageView)findViewById(R.id.bar_image_back);
		bar_image_collet = (ImageView)findViewById(R.id.bar_image_love);
		bar_image_share = (ImageView)findViewById(R.id.bar_image_share);
		iv_image = (ImageView)findViewById(R.id.image);
		activities_content = (TextView)findViewById(R.id.content);
        activities_address = (TextView)findViewById(R.id.activities_address);
        activities_dotime = (TextView)findViewById(R.id.activities_time);
        activities_max_people = (TextView)findViewById(R.id.max_mumber_people);
        activities_already_people = (TextView)findViewById(R.id.already_mumber_people);
        organizer_name = (TextView)findViewById(R.id.organizer_name);
        participant_name = (TextView)findViewById(R.id.participant_name);
        join_activities = (TextView)findViewById(R.id.bar_join);
	}
	
	private void initListeners(){
		bar_image_back.setOnClickListener(this);
        bar_image_collet.setOnClickListener(this);
        bar_image_share.setOnClickListener(this);
        join_activities.setOnClickListener(this);
	}
	
	private void initView(){
        Intent intent = getIntent();
        already_people = intent.getIntExtra("already_people", 0);
        activities_content.setText(intent.getStringExtra("detail"));
        activities_address.setText("广州");
        activities_dotime.setText(intent.getStringExtra("dotime"));
        if(intent.getIntExtra("max_people", 0)==0){
        	activities_max_people.setText("无人数限制");
        }
        else {
        	activities_max_people.setText("限制："+intent.getIntExtra("max_people", 0)+"人");
        }
        activities_already_people.setText("已报："+intent.getIntExtra("already_people", 0)+"人");
        organizer_name.setText(intent.getStringExtra("username"));
        if(intent.getStringExtra("participant").equals("")){
        	participant_name.setText("");
        }
        else participant_name.setText(intent.getStringExtra("participant"));
        
        isCollect = intent.getStringExtra("isCollect");
        if(isCollect.equals("1")){
        	bar_image_collet.setImageResource(R.drawable.ic_action_favorite_select);
        }
        else {
        	bar_image_collet.setImageResource(R.drawable.ic_action_favorite);
        	isCollect = "0";
        }
        
        isJoin = intent.getStringExtra("isJoin");
        sharedPreferences = getSharedPreferences("loginUserSP", Context.MODE_PRIVATE);
		int participantid = sharedPreferences.getInt("userid", 0);
        if(participantid==intent.getIntExtra("userid", 0)){
        	join_activities.setVisibility(View.VISIBLE);
        	join_activities.setText("编辑活动");
        	join_activities.setBackgroundResource(R.drawable.bar_join_activities);
        }
        else if(isJoin.equals("1")){
			join_activities.setVisibility(View.VISIBLE);
        	join_activities.setText("退出报名");
        	join_activities.setBackgroundResource(R.drawable.bar_exit_activities);
        }
        else {
        	join_activities.setVisibility(View.VISIBLE);
        	join_activities.setText("我要报名");
        	join_activities.setBackgroundResource(R.drawable.bar_join_activities);
        }
        //显示顶端图片
        MyImageLoader.loadImage(ServerUrlUtil.SERVER_BASCE_URL+"/rcgl/upload/"+intent.getStringExtra("image"), iv_image);
        WindowManager windowManager = this.getWindowManager();
        int screenWidth = windowManager.getDefaultDisplay().getWidth();
        ViewGroup.LayoutParams lp = iv_image.getLayoutParams();
        lp.width = screenWidth;
        lp.height = LayoutParams.WRAP_CONTENT;
        iv_image.setLayoutParams(lp);

        iv_image.setMaxWidth(screenWidth);
        iv_image.setMaxHeight((int) (screenWidth * 0.56)); //这里其实可以根据需求而定，我这里测试为最大宽度的0.75倍
	}
	
	@Override
	public void onClick(View v) {
		Intent intent=new Intent();
		switch(v.getId()){
		case R.id.bar_image_back://返回
			//ActivitiesFragment.activitiesAdapter.notifyDataSetChanged();
			intent.setClass(ActivitiesDetailActivity.this, MainTabActivity.class);
			intent.putExtra("page_tab", 2);
			startActivity(intent);
    		finish();
			break;
		case R.id.bar_image_love://收藏活动
			if(!NetUtil.isConnect(getApplicationContext())){
				ToastUtils.showToast(getApplicationContext(), "网络未连接");
				return;
			}
			if(isCollect.equals("1")){
				bar_image_collet.setImageResource(R.drawable.ic_action_favorite);
				ToastUtils.showToast(getApplicationContext(),"成功取消收藏");
				isCollect = "0";
			}
			else {
				bar_image_collet.setImageResource(R.drawable.ic_action_favorite_select);
				ToastUtils.showToast(getApplicationContext(),"收藏成功，可到个人活动中心查看");
				isCollect = "1";
			}
			collectActivities();
			
			break;
		case R.id.bar_image_share://分享活动
			shareActivities();
			//ToastUtils.showToast(getApplicationContext(),"分享该活动");
			break;
		case R.id.bar_join://参加或退出活动
			if(!NetUtil.isConnect(getApplicationContext())){
				ToastUtils.showToast(getApplicationContext(), "网络未连接");
				return;
			}
			if(join_activities.getText().toString().equals("我要报名")){/** 报名参加活动 */
				final Dialog joinDialog = new Dialog(ActivitiesDetailActivity.this, R.style.option_dialog);
				joinDialog.setContentView(R.layout.dialog_confirm);
	            ((TextView) joinDialog.findViewById(R.id.dialog_title)).setText("确定报名活动吗？");
	            Button btn_yes = (Button) joinDialog.findViewById(R.id.btn_yes);
	            btn_yes.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						joinActivities();//参加活动
						sharedPreferences = getSharedPreferences("loginUserSP", Context.MODE_PRIVATE);
						String username = sharedPreferences.getString("username", "");
						if(participant_name.getText().toString().equals("")){
							participant_name.setText(username);
						}
						else {
							participant_name.setText(participant_name.getText().toString()+","+username);
						}
						activities_already_people.setText("已报："+(already_people+1)+"人");
						already_people += 1;
						ToastUtils.showToast(getApplicationContext(),"报名成功");
						join_activities.setText("退出报名");
						join_activities.setBackgroundResource(R.drawable.bar_exit_activities);

						joinDialog.dismiss();
					}
				});
	            Button btn_no = (Button) joinDialog.findViewById(R.id.btn_no);
	            btn_no.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						joinDialog.dismiss();
					}
				});
	            joinDialog.show();
				
				
			}
			else if(join_activities.getText().toString().equals("退出报名")){/** 退出活动 */
				final Dialog exitialog = new Dialog(ActivitiesDetailActivity.this, R.style.option_dialog);
				exitialog.setContentView(R.layout.dialog_confirm);
	            ((TextView) exitialog.findViewById(R.id.dialog_title)).setText("确定退出活动吗？");
	            Button btn_yes = (Button) exitialog.findViewById(R.id.btn_yes);
	            btn_yes.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						exitActivities();//退出活动
						sharedPreferences = getSharedPreferences("loginUserSP", Context.MODE_PRIVATE);
						String username = sharedPreferences.getString("username", "");
						if(participant_name.getText().toString().contains(","+username)){
							participant_name.setText(participant_name.getText().toString().replace(","+username, ""));
						}
						else if(participant_name.getText().toString().contains(username)){
							if(participant_name.getText().toString().replace(username, "").equals("")){
								participant_name.setText("");
							}
							else participant_name.setText(participant_name.getText().toString().replace(username, ""));
						}
						activities_already_people.setText("已报："+(already_people-1)+"人");
						already_people -= 1;
						ToastUtils.showToast(getApplicationContext(),"成功退出活动");
						join_activities.setText("我要报名");
						join_activities.setBackgroundResource(R.drawable.bar_join_activities);
						
						exitialog.dismiss();
					}
				});
	            Button btn_no = (Button) exitialog.findViewById(R.id.btn_no);
	            btn_no.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						exitialog.dismiss();
					}
				});
	            exitialog.show();
				
				
			}
			break;
		}
		
	}
	
	/** 参加活动 */
	private void joinActivities(){
		sharedPreferences = getSharedPreferences("loginUserSP", Context.MODE_PRIVATE);
		int participantid = sharedPreferences.getInt("userid", 0);
		Intent intent = getIntent();
		NameValuePair pair_userid = new BasicNameValuePair("userid",intent.getIntExtra("userid", 0)+"");
        NameValuePair pair_activitiesid = new BasicNameValuePair("activitiesid",intent.getIntExtra("activitiesid", 0)+"");
        NameValuePair pair_participantid = new BasicNameValuePair("participantid", participantid+"");
        NameValuePair pair_operate = new BasicNameValuePair("operate", "1");//1为参加活动
        
        pairList = new ArrayList<NameValuePair>();
        pairList.add(pair_userid);
        pairList.add(pair_activitiesid);
        pairList.add(pair_participantid);
        pairList.add(pair_operate);
        
        //显示进度框提示
        //LoadingDialog.showLoadingDialog(getApplicationContext(), "");
        //创建线程
        Thread activitiesThread = new Thread(http_runnable);
        //启动线程
        activitiesThread.start();
	}
	
	/** 退出活动 */
	private void exitActivities(){
		sharedPreferences = getSharedPreferences("loginUserSP", Context.MODE_PRIVATE);
		int participantid = sharedPreferences.getInt("userid", 0);
		Intent intent = getIntent();
		NameValuePair pair_userid = new BasicNameValuePair("userid",intent.getIntExtra("userid", 0)+"");
        NameValuePair pair_activitiesid = new BasicNameValuePair("activitiesid",intent.getIntExtra("activitiesid", 0)+"");
        NameValuePair pair_participantid = new BasicNameValuePair("participantid", participantid+"");
        NameValuePair pair_operate = new BasicNameValuePair("operate", "-1");//-1为退出活动
        
        pairList = new ArrayList<NameValuePair>();
        pairList.add(pair_userid);
        pairList.add(pair_activitiesid);
        pairList.add(pair_participantid);
        pairList.add(pair_operate);
        
        //显示进度框提示
        //LoadingDialog.showLoadingDialog(getApplicationContext(), "");
        //创建线程
        Thread activitiesThread = new Thread(http_runnable);
        //启动线程
        activitiesThread.start();
	}
	
	/** 收藏活动 */
	private void collectActivities(){
		sharedPreferences = getSharedPreferences("loginUserSP", Context.MODE_PRIVATE);
		int participantid = sharedPreferences.getInt("userid", 0);
		Intent intent = getIntent();
		NameValuePair pair_userid = new BasicNameValuePair("userid",intent.getIntExtra("userid", 0)+"");
        NameValuePair pair_activitiesid = new BasicNameValuePair("activitiesid",ShareConstants.activitiesid);
        NameValuePair pair_participantid = new BasicNameValuePair("participantid", participantid+"");
        NameValuePair pair_operate;
        if(isCollect.equals("1")) {
        	pair_operate = new BasicNameValuePair("operate", "2");//2为收藏活动
        }
        else {
        	pair_operate = new BasicNameValuePair("operate", "-2");//-2为取消活动收藏
        }
        
        pairList = new ArrayList<NameValuePair>();
        pairList.add(pair_userid);
        pairList.add(pair_activitiesid);
        pairList.add(pair_participantid);
        pairList.add(pair_operate);
        
        //显示进度框提示
        //LoadingDialog.showLoadingDialog(getApplicationContext(), "");
        //创建线程
        Thread activitiesThread = new Thread(http_runnable);
        //启动线程
        activitiesThread.start();
	}
	
	/** 分享活动 */
	private void shareActivities(){
		Intent intent = new Intent();
		intent.setClass(ActivitiesDetailActivity.this, ShareActivitiesActivity.class);
		intent.putExtra("activitiesid",ShareConstants.activitiesid);
		startActivity(intent);
	}
	
	/** http post方式发送用户输入数据进行获取活动请求 */
    Runnable http_runnable =  new Runnable() {
    	@Override
		public void run() {
    		try
            {	
                HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList,HTTP.UTF_8);
                HttpPost httpPost = new HttpPost(ServerUrlUtil.Server_ActivitiesManage_URL);
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
                if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK && !strResult.equals("-1")) {
                	httpHandler.sendEmptyMessage(200);
                	//将response返回数据写入到资源文件中
                    writeFile("myactivities.txt",strResult);
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
				
			}
			else {
				
			}
            //只要执行到这里就关闭对话框  
			//LoadingDialog.closeLoadingDialog();
	    }
	};
	
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
}
