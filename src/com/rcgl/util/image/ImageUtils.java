package com.rcgl.util.image;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
/**
 * 图片处理
 * @author lims
 * @date 2015-04-01
 */
public class ImageUtils {
	
	/**
	  * 获取圆角图片
	  */
	 public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {
		 int w = bitmap.getWidth();
		 int h = bitmap.getHeight();
		 Bitmap output = Bitmap.createBitmap(w, h, Config.ARGB_8888);
		 Canvas canvas = new Canvas(output);
		 final int color = 0xff424242;
		 final Paint paint = new Paint();
		 final Rect rect = new Rect(0, 0, w, h);
		 final RectF rectF = new RectF(rect);
		 paint.setAntiAlias(true);
		 canvas.drawARGB(0, 0, 0, 0);
		 paint.setColor(color);
		 canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		 paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		 canvas.drawBitmap(bitmap, rect, rect, paint);
		
		 return output;
	}
	 
	 /**
	  * 图片转灰度
	  * 
	  * @param bmSrc
	  * @return
	  */
	 public static Bitmap bitmap2Gray(Bitmap bmSrc) {
		 int width, height;
		 height = bmSrc.getHeight();
		 width = bmSrc.getWidth();
		 Bitmap bmpGray = null;
		 bmpGray = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		 Canvas c = new Canvas(bmpGray);
		 Paint paint = new Paint();
		 ColorMatrix cm = new ColorMatrix();
		 cm.setSaturation(0);
		 ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
		 paint.setColorFilter(f);
		 c.drawBitmap(bmSrc, 0, 0, paint);

		 return bmpGray;
	 }
	 
	 /** 网络URL转BitMap */
	 public static Bitmap url2BitMap(String url){ 
	        URL myFileUrl = null;   
	        Bitmap bitmap = null;  
	        try {   
	            myFileUrl = new URL(url);   
	        } catch (MalformedURLException e) {   
	            e.printStackTrace();   
	        }   
	        try {   
	            HttpURLConnection conn = (HttpURLConnection) myFileUrl   
	              .openConnection();   
	            conn.setDoInput(true);   
	            conn.connect();   
	            InputStream is = conn.getInputStream();   
	            bitmap = BitmapFactory.decodeStream(is);   
	            is.close();   
	        } catch (IOException e) {   
	              e.printStackTrace();   
	        }   
	              return bitmap;   
	    }
	 
	 /** 从网络获取图片 */
	 public static InputStream getStreamFromURL(String imageURL) {
		 InputStream in=null;
		 try {
			 URL url=new URL(imageURL);
			 HttpURLConnection connection=(HttpURLConnection) url.openConnection();
			 in=connection.getInputStream();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return in;
	 }
	 
}
