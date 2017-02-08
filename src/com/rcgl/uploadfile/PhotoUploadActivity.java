package com.rcgl.uploadfile;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.rcgl.R;
import com.rcgl.activity.basic.MainTabActivity;
import com.rcgl.activity.user.LoginActivity;
import com.rcgl.util.ServerUrlUtil;

public class PhotoUploadActivity extends Activity {
    private String newName ="image.jpg";
    private String uploadFile ="/sdcard/image.JPG";
    private String actionUrl ="http://172.16.10.122:8080/rcgl/RCGLServer/UploadPhotoServlet";
    private TextView mText1;
    private TextView mText2;
    private Button mButton;
    @Override
      public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_upload);
        mText1 = (TextView) findViewById(R.id.myText2);
        //"文件路径：\n"+
        mText1.setText(uploadFile);
        mText2 = (TextView) findViewById(R.id.myText3);
        //"上传网址：\n"+
        mText2.setText(actionUrl);
        /* 设置mButton的onClick事件处理 */    
        mButton = (Button) findViewById(R.id.myButton);
        mButton.setOnClickListener(new View.OnClickListener()
        {
          public void onClick(View v)
          {
        	//创建线程
            Thread uploadThread = new Thread(upload_runnable);
            //启动线程
            uploadThread.start();
          }
        });
      }
    
    /** http post方式发送用户输入数据进行登录请求 */
    Runnable upload_runnable =  new Runnable() {
    	@Override
		public void run() {
    		String end ="\r\n";
            String twoHyphens ="--";
            String boundary ="*****";
            try
            {
              URL url =new URL(actionUrl);
              HttpURLConnection con=(HttpURLConnection)url.openConnection();
              /* 允许Input、Output，不使用Cache */
              con.setDoInput(true);
              con.setDoOutput(true);
              con.setUseCaches(false);
              /* 设置传送的method=POST */
              con.setRequestMethod("POST");
              /* setRequestProperty */
              con.setRequestProperty("Connection", "Keep-Alive");
              con.setRequestProperty("Charset", "UTF-8");
              con.setRequestProperty("Content-Type","multipart/form-data;boundary="+boundary);
              /* 设置DataOutputStream */
              DataOutputStream ds = new DataOutputStream(con.getOutputStream());
              ds.writeBytes(twoHyphens + boundary + end);
              ds.writeBytes("Content-Disposition: form-data; "+
                            "name=\"file1\";filename=\""+
                            newName +"\""+ end);
              ds.writeBytes(end);  
              /* 取得文件的FileInputStream */
              FileInputStream fStream =new FileInputStream(uploadFile);
              /* 设置每次写入1024bytes */
              int bufferSize =1024;
              byte[] buffer =new byte[bufferSize];
              int length =-1;
              /* 从文件读取数据至缓冲区 */
              while((length = fStream.read(buffer)) !=-1)
              {
                /* 将资料写入DataOutputStream中 */
                ds.write(buffer, 0, length);
              }
              ds.writeBytes(end);
              ds.writeBytes(twoHyphens + boundary + twoHyphens + end);
              /* close streams */
              fStream.close();
              ds.flush();
              /* 取得Response内容 */
              InputStream is = con.getInputStream();
              int ch;
              StringBuffer b =new StringBuffer();
              while( ( ch = is.read() ) !=-1 )
              {
                b.append( (char)ch );
              }
              /* 将Response显示于Dialog */
              //showDialog("上传成功"+b.toString().trim());
              uploadHandler.sendEmptyMessage(200);
              /* 关闭DataOutputStream */
              ds.close();
            }
            catch(Exception e)
            {
              	//showDialog("上传失败"+e);
              	//Toast.makeText(getApplicationContext(), "上传失败"+e, Toast.LENGTH_SHORT).show();
            	uploadHandler.sendEmptyMessage(0);
            }	
		}
	};
    
    
      /* 上传文件至Server的方法 */
      private void uploadFile()
      {
        String end ="\r\n";
        String twoHyphens ="--";
        String boundary ="*****";
        try
        {
          URL url =new URL(actionUrl);
          HttpURLConnection con=(HttpURLConnection)url.openConnection();
          /* 允许Input、Output，不使用Cache */
          con.setDoInput(true);
          con.setDoOutput(true);
          con.setUseCaches(false);
          /* 设置传送的method=POST */
          con.setRequestMethod("POST");
          /* setRequestProperty */
          con.setRequestProperty("Connection", "Keep-Alive");
          con.setRequestProperty("Charset", "UTF-8");
          con.setRequestProperty("Content-Type","multipart/form-data;boundary="+boundary);
          /* 设置DataOutputStream */
          DataOutputStream ds = new DataOutputStream(con.getOutputStream());
          ds.writeBytes(twoHyphens + boundary + end);
          ds.writeBytes("Content-Disposition: form-data; "+
                        "name=\"file1\";filename=\""+
                        newName +"\""+ end);
          ds.writeBytes(end);  
          /* 取得文件的FileInputStream */
          FileInputStream fStream =new FileInputStream(uploadFile);
          /* 设置每次写入1024bytes */
          int bufferSize =1024;
          byte[] buffer =new byte[bufferSize];
          int length =-1;
          /* 从文件读取数据至缓冲区 */
          while((length = fStream.read(buffer)) !=-1)
          {
            /* 将资料写入DataOutputStream中 */
            ds.write(buffer, 0, length);
          }
          ds.writeBytes(end);
          ds.writeBytes(twoHyphens + boundary + twoHyphens + end);
          /* close streams */
          fStream.close();
          ds.flush();
          /* 取得Response内容 */
          InputStream is = con.getInputStream();
          int ch;
          StringBuffer b =new StringBuffer();
          while( ( ch = is.read() ) !=-1 )
          {
            b.append( (char)ch );
          }
          /* 将Response显示于Dialog */
          //showDialog("上传成功"+b.toString().trim());
          uploadHandler.sendEmptyMessage(200);
          /* 关闭DataOutputStream */
          ds.close();
        }
        catch(Exception e)
        {
          	//showDialog("上传失败"+e);
          	//Toast.makeText(getApplicationContext(), "上传失败"+e, Toast.LENGTH_SHORT).show();
        	uploadHandler.sendEmptyMessage(0);
        }
      }
      
      /**
       * 登录处理函数
       * @author lims
       * @date 2015-02-03
       */
  	Handler uploadHandler = new Handler() {
  		@Override
  		//当有消息发送出来的时候就执行Handler的这个方法 
  	    public void handleMessage(Message msg) {
  			super.handleMessage(msg);  
  			//Toast.makeText(getApplicationContext(), msg.toString(), Toast.LENGTH_SHORT).show();
  			if(msg.what==200){
  				showDialog("上传成功");
  			}
  			else {
  				showDialog("上传失败");
  			}
            
  	    }
  	};
      
      /* 显示Dialog的method */
      private void showDialog(String mess)
      {
        new AlertDialog.Builder(PhotoUploadActivity.this).setTitle("Message")
         .setMessage(mess)
         .setNegativeButton("确定",new DialogInterface.OnClickListener()
         {
           public void onClick(DialogInterface dialog, int which)
           {          
           }
         })
         .show();
      }
    }
