package com.rcgl.activity.schedule;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.fourmob.datetimepicker.date.DatePickerDialog.OnDateSetListener;
import com.rcgl.R;
import com.rcgl.activity.basic.MainTabActivity;
import com.rcgl.app.MuApp;
import com.rcgl.util.LoadingDialog;
import com.rcgl.util.ServerUrlUtil;
import com.rcgl.util.ToastUtils;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;
/** 
 * 添加日程
 * @author lims
 *
 */
public class AddScheduleActivity extends FragmentActivity 
	implements OnDateSetListener, TimePickerDialog.OnTimeSetListener {
	/** 日程输入框 */
	private EditText schedule;
	/** 日期选择 */
	private TextView chooseDate;
	/** 显示日期时间 */
	private TextView showTime;
	/** 是否公开选项 */
	private CheckBox openRead;
	/** 是否设置通知提醒选项 */
	private CheckBox cb_alarm;
	/** 是否公开标记 */
	private String openTag = "1";
	/** 是否设置通知提醒标记 */
	private String alarmTag = "1";
	/** 处理中状态图标 */
	private Dialog pd;
	/** 标题栏的标题文字 */
	private TextView bar_title;
	/** 标题栏的左端返回图片 */
	private ImageView bar_image_return;
	/** 标题栏的左端图片添加日程按钮 */
	private ImageView bar_image_addSchedule;
	
	private Calendar calendar;
	private DatePickerDialog datePickerDialog;
	private TimePickerDialog timePickerDialog;
	
	/** 日期,如2015-03-07 */
	private String date;
	/** 时间，如12:12 */
	private String time;
	
	List<NameValuePair> pairList;
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
        setContentView(R.layout.add_schedule);
        
        MuApp.getInstance().addActivity(this);
        
        getEntities();
		initListeners();
		initView();
        
        calendar = Calendar.getInstance();
        datePickerDialog = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), true);
        timePickerDialog = TimePickerDialog.newInstance(this, calendar.get(Calendar.HOUR_OF_DAY) ,calendar.get(Calendar.MINUTE), false, false);
        
	}
	
	private void getEntities(){
		bar_image_return = (ImageView)findViewById(R.id.bar_image_return);
        schedule = (EditText)findViewById(R.id.schedule);
        chooseDate = (TextView)findViewById(R.id.chooseDate);
        showTime = (TextView)findViewById(R.id.show_date);
        openRead = (CheckBox)findViewById(R.id.openRead);
        cb_alarm = (CheckBox)findViewById(R.id.alarm);
        bar_image_addSchedule = (ImageView)findViewById(R.id.bar_image_add);
	}
	
	private void initListeners(){
		bar_image_return.setOnClickListener(return_main);
        chooseDate.setOnClickListener(onChooseDate);
        bar_image_addSchedule.setOnClickListener(onAddSchedule);
        
        //给CheckBox设置事件监听 
        openRead.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){ 
            @Override 
            public void onCheckedChanged(CompoundButton buttonView, 
                    boolean isChecked) { 
                if(isChecked){ 
                	openTag = "0"; 
                }else{ 
                	openTag = "1"; 
                } 
            } 
        }); 
      //给CheckBox设置事件监听 
        cb_alarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){ 
            @Override 
            public void onCheckedChanged(CompoundButton buttonView, 
                    boolean isChecked) { 
                if(isChecked){ 
                	alarmTag = "1"; 
                }else{ 
                	alarmTag = "0"; 
                } 
            } 
        });
	}
	
	private void initView(){
		Date now = new Date(); //new Date()为获取当前系统时间
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");///设置日期格式,hh为12小时制，HH为24小时制
		String time = df.format(now);
		showTime.setText(time);
	}
	
	/** 查找日程 */
    View.OnClickListener onChooseDate = new View.OnClickListener(){
		@Override
    	public void onClick(View view){
			
			datePickerDialog.setVibrate(true);
            datePickerDialog.setYearRange(1985, 2028);
            datePickerDialog.setCloseOnSingleTapDay(false);
            datePickerDialog.show(getSupportFragmentManager(), "datepicker");
    	}
    };
	
    /** 发送添加日程请求 */
    View.OnClickListener onAddSchedule = new View.OnClickListener(){
		@Override
    	public void onClick(View view){
			//读取SharedPreferences中的数据
			SharedPreferences sharedPreferences = getSharedPreferences("loginUserSP", Context.MODE_PRIVATE);
			//getString()第二个参数为缺省值，如果preference中不存在该key，将返回缺省值
			int userid = sharedPreferences.getInt("userid", 0);
			String content = schedule.getText().toString().trim();
			
			if(userid==0){
				ToastUtils.showToast(getApplicationContext(), "你还未登录哦，去登录玩玩？");
				return;
			}
			
			if(content.equals("")){
				ToastUtils.showToast(getApplicationContext(), "日程内容不可为空");
				return;
			}
			String scheduletime = showTime.getText().toString();
			
			NameValuePair pair_uid = new BasicNameValuePair("userid",String.valueOf(userid));
			NameValuePair pair_content = new BasicNameValuePair("content",content);
			NameValuePair pair_doTime = new BasicNameValuePair("doTime",scheduletime);
			NameValuePair pair_openRead = new BasicNameValuePair("openRead",openTag);
			NameValuePair pair_alarm = new BasicNameValuePair("alarm",alarmTag);
            pairList = new ArrayList<NameValuePair>();
            pairList.add(pair_uid);
            pairList.add(pair_content);
            pairList.add(pair_doTime);
            pairList.add(pair_openRead);
            pairList.add(pair_alarm);
            
            
            //显示进度框提示
            pd = LoadingDialog.createLoadingDialog(AddScheduleActivity.this, "添加中…");
            pd.show();
            
            //创建线程
            Thread addScheduleThread = new Thread(addSchedule_runnable);
            //启动线程
            addScheduleThread.start();
            
    	}
    };
    
    
    
	/** http post方式发送用户输入数据进行查找日程请求 */
    Runnable addSchedule_runnable =  new Runnable() {
    	@Override
		public void run() {
    		try{	
                HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList,HTTP.UTF_8);
                // URL使用基本URL即可，其中不需要加参数
                HttpPost httpPost = new HttpPost(ServerUrlUtil.Server_AddSchedule_URL);
                //httpPost.addHeader("content_Type", "application/json;charset=utf-8");
                // 将请求体内容加入请求中
                httpPost.setEntity(requestHttpEntity);
                // 需要客户端对象来发送请求
                HttpClient httpClient = new DefaultHttpClient();
                // 发送请求
                HttpResponse httpResponse = httpClient.execute(httpPost);
                //读返回数据  
                String strResult = EntityUtils.toString(httpResponse.getEntity());  
                Log.i("addSchedule", strResult);
                //若状态码为200 ok 
                if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK && !strResult.equals("-1")) {  
                	addScheduleHandler.sendEmptyMessage(200);
                    //将response返回数据写入到资源文件中
                    writeFile("myschedule.txt",strResult);
                }  
                else  
                {  
                	addScheduleHandler.sendEmptyMessage(0);
                } 
                
            }
            catch (Exception e){
                e.printStackTrace();
            }
    	}
    };
	
	
	/**
     * 请求添加日程处理函数
     * @author lims
     * @date 2015-02-03
     */
	Handler addScheduleHandler = new Handler() {
		@Override
		//当有消息发送出来的时候就执行Handler的这个方法 
	    public void handleMessage(Message msg) {
			super.handleMessage(msg);  
			if(msg.what==200){
				ToastUtils.showToast(getApplicationContext(), "添加日程成功");
				Intent intent = new Intent();
	    		intent.setClass(AddScheduleActivity.this, MainTabActivity.class);
	    		intent.putExtra("page_tab", 0);
	    		startActivity(intent);
	    		AddScheduleActivity.this.finish();
			}
			else {
				ToastUtils.showToast(getApplicationContext(), "添加日程失败");
			}
            //只要执行到这里就关闭对话框  
            pd.dismiss(); 
	    }
	};
	
	/** 返回主界面 */
    View.OnClickListener return_main = new View.OnClickListener(){
		@Override
    	public void onClick(View view){
			Intent intent=new Intent();
    		intent.setClass(AddScheduleActivity.this, MainTabActivity.class);
    		intent.putExtra("page_tab", 0);
    		startActivity(intent);
    		finish();
    	}
    };

	@Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
		String m = (month+1)+"";
		String d = day+"";
		if(month<9){
			m = "0"+(month+1);
		}
		if(day<10){
			d = "0"+day;
		}
		date = year + "-" + m + "-" + d;
		timePickerDialog.setVibrate(true);
        timePickerDialog.setCloseOnSingleTapMinute(false);
        timePickerDialog.show(getSupportFragmentManager(), "timepicker");
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
    	String h = hourOfDay+"";
		String min = minute+"";
		if(hourOfDay<10){
			h = "0"+hourOfDay;
		}
		if(minute<10){
			min = "0"+minute;
		}
    	time = h + ":" + min;
    	showTime.setText(date+" "+time);
    	ToastUtils.showToast(AddScheduleActivity.this, "时间选择完成");
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
}
