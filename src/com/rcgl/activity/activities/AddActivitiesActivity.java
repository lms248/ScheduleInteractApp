package com.rcgl.activity.activities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
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
import com.rcgl.util.image.Base64Coder;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;
/**
 * 添加活动
 * @author lims
 * @date 2015-03-24
 */
public class AddActivitiesActivity extends FragmentActivity implements OnClickListener, OnDateSetListener, TimePickerDialog.OnTimeSetListener {
	/** 活动输入框 */
	private EditText activities;
	/** 日期选择 */
	private TextView chooseDate;
	/** 显示日期时间 */
	private TextView showTime;
	/** 是否限制人数选项 */
	private CheckBox number_limit_check;
	/** 是否限制人数标记 */
	private String number_limit_tag = "0";
	/** 处理中状态图标 */
	private Dialog pd;
	/** 标题栏的标题文字 */
	private TextView bar_title;
	/** 标题栏的左端图片 */
	private ImageView bar_image_return;
	/** 标题栏的左端图片添加活动按钮 */
	private ImageView bar_image_addActivities;
	/** 上限数左端文字，“上限” */
	private TextView max_limit_tab;
	/** 上限数输入框 */
	private EditText max_number_people;
	/** 上限数右端文字 ，“人” */
	private TextView people_tab;
	
	/** 标签“运动” */
	private TextView tab_sport;
	/** 标签“旅行” */
	private TextView tab_travel;
	/** 标签“学习” */
	private TextView tab_study;
	/** 标签“休闲” */
	private TextView tab_casual;
	/** 标签“其他” */
	private TextView tab_other;
	/** 添加图片按钮 */
	private ImageView add_image;
	/** 拍摄照片名 */
	private String imageName;
	/** 上传的bitmap */
	private Bitmap upbitmap = null;
	
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
        setContentView(R.layout.add_activities);
        
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
        activities = (EditText)findViewById(R.id.activities);
        chooseDate = (TextView)findViewById(R.id.chooseDate);
        showTime = (TextView)findViewById(R.id.show_date);
        number_limit_check = (CheckBox)findViewById(R.id.number_limit_check);
        bar_image_addActivities = (ImageView)findViewById(R.id.bar_image_add);
        max_limit_tab = (TextView)findViewById(R.id.max_limit_tab);
        max_number_people = (EditText)findViewById(R.id.max_people_number);
        people_tab = (TextView)findViewById(R.id.people_tab);
        tab_sport = (TextView)findViewById(R.id.tab_sport);
		tab_travel = (TextView)findViewById(R.id.tab_travel);
		tab_study = (TextView)findViewById(R.id.tab_study);
		tab_casual = (TextView)findViewById(R.id.tab_casual);
		tab_other = (TextView)findViewById(R.id.tab_orther);
		add_image = (ImageView)findViewById(R.id.add_image);
	}
	
	private void initListeners(){
		bar_image_return.setOnClickListener(this);
		bar_image_addActivities.setOnClickListener(this);
	    chooseDate.setOnClickListener(this);
	    add_image.setOnClickListener(this);
	    tab_sport.setOnClickListener(this);
		tab_travel.setOnClickListener(this);
		tab_study.setOnClickListener(this);
		tab_casual.setOnClickListener(this);
		tab_other.setOnClickListener(this);
	    
	    //给CheckBox设置事件监听 
        number_limit_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){ 
            @Override 
            public void onCheckedChanged(CompoundButton buttonView, 
                    boolean isChecked) { 
                if(isChecked){ 
                	number_limit_tag = "1"; 
                	max_limit_tab.setVisibility(View.VISIBLE);
                	max_number_people.setVisibility(View.VISIBLE);
                	people_tab.setVisibility(View.VISIBLE);
                }else{ 
                	number_limit_tag = "0"; 
                	max_limit_tab.setVisibility(View.INVISIBLE);
                	max_number_people.setVisibility(View.INVISIBLE);
                	people_tab.setVisibility(View.INVISIBLE);
                } 
            } 
        }); 
		
	}

	private void initView(){
		max_limit_tab.setVisibility(View.INVISIBLE);
        max_number_people.setVisibility(View.INVISIBLE);
        people_tab.setVisibility(View.INVISIBLE);
        
        Date now = new Date(); //new Date()为获取当前系统时间
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");///设置日期格式,hh为12小时制，HH为24小时制
		String time = df.format(now);
		showTime.setText(time);
	}
	
	/** http post方式发送用户输入数据进行查找活动请求 */
    Runnable addActivities_runnable =  new Runnable() {
    	@Override
		public void run() {
    		try{	
                HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList,HTTP.UTF_8);
                HttpPost httpPost = new HttpPost(ServerUrlUtil.Server_AddActivities_URL);
                httpPost.setEntity(requestHttpEntity);
                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse httpResponse = httpClient.execute(httpPost);
                String strResult = EntityUtils.toString(httpResponse.getEntity());  
                Log.i("addActivities", strResult);
                //若状态码为200 ok 
                if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK && !strResult.equals("-1")) {  
                	addActivitiesHandler.sendEmptyMessage(200);
                    //将response返回数据写入到资源文件中
                    writeFile("myactivities.txt",strResult);
                }  
                else {  
                	addActivitiesHandler.sendEmptyMessage(0);
                } 
                
            }
            catch (Exception e){
                e.printStackTrace();
            }
    	}
    };
	
	/**
     * 请求添加活动处理函数
     * @author lims
     * @date 2015-02-03
     */
	Handler addActivitiesHandler = new Handler() {
		@Override
		//当有消息发送出来的时候就执行Handler的这个方法 
	    public void handleMessage(Message msg) {
			super.handleMessage(msg);  
			if(msg.what==200){
				ToastUtils.showToast(getApplicationContext(), "添加活动成功");
				Intent intent = new Intent();
	    		intent.setClass(AddActivitiesActivity.this, MainTabActivity.class);
	    		intent.putExtra("page_tab", 2);
	    		startActivity(intent);
	    		AddActivitiesActivity.this.finish();
			}
			else {
				ToastUtils.showToast(getApplicationContext(), "添加活动失败");
			}
            //只要执行到这里就关闭对话框  
            pd.dismiss(); 
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
    	ToastUtils.showToast(AddActivitiesActivity.this, "时间选择完成");
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
	
	/**
     * 点击处理时间
     * @author lims
     * @date 2015-04-08
     */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		switch(v.getId()) {
		case R.id.bar_image_return://返回主界面
			intent.setClass(AddActivitiesActivity.this, MainTabActivity.class);
    		intent.putExtra("page_tab", 2);
    		startActivity(intent);
    		finish();
			break;
		case R.id.bar_image_add://发送添加活动请求 
			onAddActivities();
			break;
		case R.id.chooseDate://查找活动 
			datePickerDialog.setVibrate(true);
            datePickerDialog.setYearRange(1985, 2028);
            datePickerDialog.setCloseOnSingleTapDay(false);
            datePickerDialog.show(getSupportFragmentManager(), "datepicker");
			break;
		case R.id.add_image://添加图片
			onAddImage();
			break;
		case R.id.tab_sport://"运动"标签
			findViewById(R.id.tab_sport_image).setVisibility(View.VISIBLE);
			findViewById(R.id.tab_travel_image).setVisibility(View.INVISIBLE);
			findViewById(R.id.tab_study_image).setVisibility(View.INVISIBLE);
			findViewById(R.id.tab_casual_image).setVisibility(View.INVISIBLE);
			findViewById(R.id.tab_other_image).setVisibility(View.INVISIBLE);
			break;
		case R.id.tab_travel://"旅行"标签
			findViewById(R.id.tab_sport_image).setVisibility(View.INVISIBLE);
			findViewById(R.id.tab_travel_image).setVisibility(View.VISIBLE);
			findViewById(R.id.tab_study_image).setVisibility(View.INVISIBLE);
			findViewById(R.id.tab_casual_image).setVisibility(View.INVISIBLE);
			findViewById(R.id.tab_other_image).setVisibility(View.INVISIBLE);
			break;
		case R.id.tab_study://"学习"标签
			findViewById(R.id.tab_sport_image).setVisibility(View.INVISIBLE);
			findViewById(R.id.tab_travel_image).setVisibility(View.INVISIBLE);
			findViewById(R.id.tab_study_image).setVisibility(View.VISIBLE);
			findViewById(R.id.tab_casual_image).setVisibility(View.INVISIBLE);
			findViewById(R.id.tab_other_image).setVisibility(View.INVISIBLE);
			break;
		case R.id.tab_casual://"休闲"标签
			findViewById(R.id.tab_sport_image).setVisibility(View.INVISIBLE);
			findViewById(R.id.tab_travel_image).setVisibility(View.INVISIBLE);
			findViewById(R.id.tab_study_image).setVisibility(View.INVISIBLE);
			findViewById(R.id.tab_casual_image).setVisibility(View.VISIBLE);
			findViewById(R.id.tab_other_image).setVisibility(View.INVISIBLE);
			break;
		case R.id.tab_orther://"其他"标签
			findViewById(R.id.tab_sport_image).setVisibility(View.INVISIBLE);
			findViewById(R.id.tab_travel_image).setVisibility(View.INVISIBLE);
			findViewById(R.id.tab_study_image).setVisibility(View.INVISIBLE);
			findViewById(R.id.tab_casual_image).setVisibility(View.INVISIBLE);
			findViewById(R.id.tab_other_image).setVisibility(View.VISIBLE);
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case 1://读取拍照图片
			File temp = new File(Environment.getExternalStorageDirectory()+"/"+imageName);
			startPhotoZoom(Uri.fromFile(temp));
			break;
		case 2://读取本地图片
			if(data!=null){
				startPhotoZoom(data.getData());
			}
			break;
		case 3:// 取得裁剪后的图片
			if (data != null) {
				setPicToView(data);
			}
			break;
		default:
			break;
		}
	}
	
	/** 发送添加活动请求  */
	private void onAddActivities(){
		//读取SharedPreferences中的数据
		SharedPreferences sharedPreferences = getSharedPreferences("loginUserSP", Context.MODE_PRIVATE);
		//getString()第二个参数为缺省值，如果preference中不存在该key，将返回缺省值
		int userid = sharedPreferences.getInt("userid", 0);
		String content = activities.getText().toString().trim();
		
		if(userid==0){
			ToastUtils.showToast(getApplicationContext(), "你还未登录哦，去登录玩玩？");
			return;
		}
		
		if(content.equals("")){
			ToastUtils.showToast(getApplicationContext(), "活动内容不可为空");
			return;
		}
		Boolean isSort = false;
		String sort = "";
		if(findViewById(R.id.tab_sport_image).getVisibility()==View.VISIBLE){
			isSort = true;
			sort="1";
		}
		else if(findViewById(R.id.tab_travel_image).getVisibility()==View.VISIBLE){
			isSort = true;
			sort="2";
		}
		else if(findViewById(R.id.tab_study_image).getVisibility()==View.VISIBLE){
			isSort = true;
			sort="3";
		}
		else if(findViewById(R.id.tab_casual_image).getVisibility()==View.VISIBLE){
			isSort = true;
			sort="4";
		}
		else if(findViewById(R.id.tab_other_image).getVisibility()==View.VISIBLE){
			isSort = true;
			sort="5";
		}
		else isSort = false;
		
		if(isSort==false){
			ToastUtils.showToast(getApplicationContext(), "请先选择活动类型标签");
			return;
		}
		
		String activitiestime = showTime.getText().toString();
		String max_number_people_str = "0";
		if(number_limit_tag.equals("1")){
			if(max_number_people.getText()==null||max_number_people.getText().toString().trim().equals("")){
				ToastUtils.showToast(getApplicationContext(), "请输入限制人数");
				return;
			}
			else {
				max_number_people_str = max_number_people.getText().toString().trim();
			}
		}
		
		if(upbitmap==null){
			ToastUtils.showToast(getApplicationContext(), "请添加活动图片");
			return;
		}
		
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		upbitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		byte[] b = stream.toByteArray();
		/** 将图片流以字符串形式存储下来 */
		String image = new String(Base64Coder.encodeLines(b));
		
		NameValuePair pair_uid = new BasicNameValuePair("userid",String.valueOf(userid));
		NameValuePair pair_content = new BasicNameValuePair("content",content);
		NameValuePair pair_doTime = new BasicNameValuePair("doTime",activitiestime);
		NameValuePair pair_image = new BasicNameValuePair("image",image);
		NameValuePair pair_number_limit = new BasicNameValuePair("number_limit",number_limit_tag);
		NameValuePair pair_max_number_people = new BasicNameValuePair("max_number_people",max_number_people_str);
		NameValuePair pair_sort = new BasicNameValuePair("sort",sort);
        pairList = new ArrayList<NameValuePair>();
        pairList.add(pair_uid);
        pairList.add(pair_content);
        pairList.add(pair_doTime);
        pairList.add(pair_image);
        pairList.add(pair_number_limit);
        pairList.add(pair_max_number_people);
        pairList.add(pair_sort);
        
        final Dialog addActivitiesDialog = new Dialog(AddActivitiesActivity.this, R.style.option_dialog);
		addActivitiesDialog.setContentView(R.layout.dialog_confirm);
        ((TextView) addActivitiesDialog.findViewById(R.id.dialog_title)).setText("确定添加活动吗？");
        Button btn_yes = (Button) addActivitiesDialog.findViewById(R.id.btn_yes);
        btn_yes.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//显示进度框提示
		        pd = LoadingDialog.createLoadingDialog(AddActivitiesActivity.this, "添加中…");
		        pd.show();
		        
		        //创建线程
		        Thread addActivitiesThread = new Thread(addActivities_runnable);
		        //启动线程
		        addActivitiesThread.start();
		        
				addActivitiesDialog.dismiss();
			}
		});
        Button btn_no = (Button) addActivitiesDialog.findViewById(R.id.btn_no);
        btn_no.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				addActivitiesDialog.dismiss();
			}
		});
        addActivitiesDialog.show();
        
	}
	
	/** 添加图片 */
    private void onAddImage() {
    	new AlertDialog.Builder(AddActivitiesActivity.this)   
    	.setTitle("请选择获取图片途径")  
    	//.setMessage("请选择获取图片途径")  
    	.setNegativeButton("本地图片", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent();
				intent = new Intent(Intent.ACTION_PICK, null);
				intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
						"image/*");
				startActivityForResult(intent, 2);/* 取得相片后返回本画面 */ 
			}
		})
    	.setPositiveButton("拍照", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent();
				intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				SharedPreferences sharedPreferences = getSharedPreferences("loginUserSP", Context.MODE_PRIVATE);
				String username = sharedPreferences.getString("username", "");
				imageName = username + System.currentTimeMillis() + ".jpg";
				System.out.println(imageName);
				/*下面这句指定调用相机拍照后的照片存储的路径*/
				intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(
						Environment.getExternalStorageDirectory(), imageName)));
				startActivityForResult(intent, 1);
			}
		})  
    	.show(); 
    }
    
    /**
	 * 裁剪图片方法实现
	 * @param uri
	 */
	public void startPhotoZoom(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		// crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
		intent.putExtra("crop", "true");
		// aspectX aspectY 是宽高的比例
		intent.putExtra("aspectX", 4);
		intent.putExtra("aspectY", 3);
		// outputX outputY 是裁剪图片宽高
		intent.putExtra("outputX", 400);
		intent.putExtra("outputY", 300);
		intent.putExtra("scale", true);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, 3);
	}
	
	/**
	 * 保存裁剪之后的图片数据
	 * @param picdata
	 */
	private void setPicToView(Intent picdata) {
		Bundle extras = picdata.getExtras();
		if (extras != null) {
			// 取得SDCard图片路径做显示
			upbitmap = extras.getParcelable("data");
			Drawable drawable = new BitmapDrawable(null, upbitmap);
			add_image.setImageDrawable(drawable);
		}
	}
	
	private Bitmap decodeUriAsBitmap(Uri uri){
		Bitmap bitmap = null;
	    try {
	        bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	        return null;
	    }
	    return bitmap;
	}
	
	
	/** 取到绝对路径 */
	@SuppressWarnings("deprecation")
	protected String getAbsoluteImagePath(Uri uri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, proj, // Which columns to return
				null, // WHERE clause; which rows to return (all rows)
				null, // WHERE clause selection arguments (none)
				null); // Order-by clause (ascending by name)

		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}
    
}
