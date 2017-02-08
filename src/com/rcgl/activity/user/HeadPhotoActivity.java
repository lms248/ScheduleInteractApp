package com.rcgl.activity.user;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcgl.R;
import com.rcgl.app.MuApp;
import com.rcgl.app.MyApplication;
import com.rcgl.util.NetUtil;
import com.rcgl.util.ServerUrlUtil;
import com.rcgl.util.ToastUtils;
import com.rcgl.util.image.AsyncBitmapLoader;
import com.rcgl.util.image.AsyncBitmapLoader.ImageCallBack;
import com.rcgl.util.image.Base64Coder;
import com.rcgl.util.image.ImageUtils;
import com.rcgl.util.image.MyImageLoader;
/**
 * 修改用户头像
 * @author lims
 * @date 2015-04-08
 */
public class HeadPhotoActivity extends Activity implements OnClickListener {

	/** 显示图片 */
	private ImageView image;
	/** 拍照按钮 */
	private Button take;
	/** 从相册读取照片按钮 */
	private Button selete;
	/** 拍摄照片名 */
	private String imageName;
	/** 上传的bitmap */
	private Bitmap upbitmap;
	/** 返回图片按钮 */
	private ImageView back;
	/** 上传图片按钮 */
	private ImageView submit;
	/** 处理用户头像 */
	Bitmap bitmap;
	
	//多线程通信
	private Handler uploadHandler;
	private ProgressDialog pd;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_headphoto);
		
		MyApplication.initImageLoader(this);
		MuApp.getInstance().addActivity(this);
		
		getEntities();
		initListeners();
		initDatas();
		
		uploadHandler=new MyHandler();
	}
	
	private void getEntities(){
		image = (ImageView) this.findViewById(R.id.iv_headPhoto);
		take = (Button) this.findViewById(R.id.take);
		selete = (Button) this.findViewById(R.id.selete);
		back=(ImageView)this.findViewById(R.id.bar_image_return);
		submit=(ImageView)this.findViewById(R.id.bar_image_submit);
	}
	
	private void initListeners(){
		take.setOnClickListener(this);
		selete.setOnClickListener(this);
		back.setOnClickListener(this);
		submit.setOnClickListener(this);
	}
	
	private void initDatas(){
		/** 异步加载图片并缓存到本地 */
		//AsyncBitmapLoader asyncBitmapLoader = new AsyncBitmapLoader();
		//String imageURL = "http://172.16.10.122:8080/rcgl/upload/8/photo/headphoto8.png";
		ImageLoader.getInstance().clearMemoryCache();
		ImageLoader.getInstance().clearDiskCache();
		SharedPreferences sp = getSharedPreferences("loginUserSP", Context.MODE_PRIVATE);
		String imageURL = ServerUrlUtil.SERVER_BASCE_URL+"/rcgl/upload/"+sp.getString("userphoto", "");
		MyImageLoader.loadPhoto(imageURL, image, 0);
		
		/*Log.e("imageURL", imageURL);
		if(sp.getString("userphoto", "").equals("")){
			image.setImageResource(R.drawable.default_photo);
		}
		else {
			bitmap=AsyncBitmapLoader.loadBitmap(image, imageURL, new ImageCallBack() {  
	            @Override  
	            public void imageLoad(ImageView imageView, Bitmap bitmap) {  
	            	bitmap = ImageUtils.getRoundedCornerBitmap(bitmap,0f);
	                imageView.setImageBitmap(bitmap);  
	            }  
	        });
			if(bitmap == null){    
				image.setImageResource(R.drawable.default_photo);    
	        }    
	        else {    
	        	bitmap = ImageUtils.getRoundedCornerBitmap(bitmap,0f);
	        	image.setImageBitmap(bitmap);    
	        }
		}*/
		
	}
	
	@Override
	public void onClick(View v) {
		Intent intent = new Intent();
		switch (v.getId()) {
		case R.id.take:
			intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			SharedPreferences sharedPreferences = getSharedPreferences("loginUserSP", Context.MODE_PRIVATE);
			String username = sharedPreferences.getString("username", "");
			imageName = username + System.currentTimeMillis() + ".jpg";
			System.out.println(imageName);
			/*下面这句指定调用相机拍照后的照片存储的路径*/
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(
					Environment.getExternalStorageDirectory(), imageName)));
			startActivityForResult(intent, 1);
			break;
		case R.id.selete:
			intent = new Intent(Intent.ACTION_PICK, null);
			intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
					"image/*");
			startActivityForResult(intent, 2);/* 取得相片后返回本画面 */ 
			break;
		case R.id.bar_image_return:
			HeadPhotoActivity.this.finish();
			UserInfoActivity.initDatas();
			break;
		case R.id.bar_image_submit:
			if(!NetUtil.isConnect(getApplicationContext())){
            	ToastUtils.showToast(getApplicationContext(), "网络未连接");
            	return;
            }
			pd = ProgressDialog.show(this, "图片修改中", "马上就好...", true, false);
			new Thread(new Runnable() {
				public void run() {
					upload();
					uploadHandler.sendMessage(new Message());
				}
			}).start();
			break;
		default:
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
				submit.setVisibility(View.VISIBLE);
			}
			break;
		default:
			break;
		}
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
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY 是裁剪图片宽高
		intent.putExtra("outputX", 200);
		intent.putExtra("outputY", 200);
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
			upbitmap = ImageUtils.getRoundedCornerBitmap(upbitmap,0f);
			Drawable drawable = new BitmapDrawable(null, upbitmap);
			image.setImageDrawable(drawable);
		}
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

	/** 上传至服务器 */
	public void upload() {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		upbitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		byte[] b = stream.toByteArray();
		/** 将图片流以字符串形式存储下来 */
		String file = new String(Base64Coder.encodeLines(b));
		HttpClient client = new DefaultHttpClient();
		/** 上传参数 */
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		SharedPreferences sharedPreferences = getSharedPreferences("loginUserSP", Context.MODE_PRIVATE);
		int userid = sharedPreferences.getInt("userid", 0);
		formparams.add(new BasicNameValuePair("file", file));
		formparams.add(new BasicNameValuePair("userid", userid+""));
		HttpPost post = new HttpPost(ServerUrlUtil.Server_UpdatePhoto_URL);
		UrlEncodedFormEntity entity;
		try {
			entity = new UrlEncodedFormEntity(formparams, "UTF-8");
			post.addHeader("Accept","text/javascript, text/html, application/xml, text/xml");
			post.addHeader("Accept-Charset", "GBK,utf-8;q=0.7,*;q=0.3");
			post.addHeader("Accept-Encoding", "gzip,deflate,sdch");
			post.addHeader("Connection", "Keep-Alive");
			post.addHeader("Cache-Control", "no-cache");
			post.addHeader("Content-Type", "application/x-www-form-urlencoded");
			post.setEntity(entity);
			HttpResponse response = client.execute(post);
			System.out.println(response.getStatusLine().getStatusCode());
			HttpEntity httpEntity = response.getEntity();
			String strResult = EntityUtils.toString(httpEntity);
			System.out.println(strResult);
			if (200 == response.getStatusLine().getStatusCode()) {
				/** 创建数据编辑器 */
				Editor editor = sharedPreferences.edit();
				/** 传递需要保存的数据 */
				editor.putString("userphoto", strResult);
				/** 保存数据 */
				editor.commit();
				System.out.println("修改完成");
			} else {
				System.out.println("修改失败");
			}
			client.getConnectionManager().shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private class MyHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			pd.dismiss();
			AsyncBitmapLoader.imageCache=new HashMap<String, SoftReference<Bitmap>>();
			initDatas();
			ToastUtils.showToast(getApplicationContext(), "头像修改成功");
		}
	}

}
