package com.rcgl.util.image;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.rcgl.R;

/**
 * 图片加载
 * @author lims
 * @date 2015-04-23
 */
public class MyImageLoader {
	private static ImageLoader imageLoader = ImageLoader.getInstance();
	private static DisplayImageOptions image_options;		// DisplayImageOptions是用于设置图片显示的类
	private static DisplayImageOptions photo_options;		// DisplayImageOptions是用于设置图片显示的类
	
										// 创建配置过得DisplayImageOption对象
	
	private static void init(int corner){
		// 使用DisplayImageOptions.Builder()创建DisplayImageOptions
		image_options = new DisplayImageOptions.Builder()
			.showStubImage(R.drawable.loading_4x3)			// 设置图片下载期间显示的图片
			.showImageForEmptyUri(R.drawable.image_empty)	// 设置图片Uri为空或是错误的时候显示的图片
			.showImageOnFail(R.drawable.image_error)		// 设置图片加载或解码过程中发生错误显示的图片	
			.cacheInMemory(true)						// 设置下载的图片是否缓存在内存中
			.cacheOnDisc(true)							// 设置下载的图片是否缓存在SD卡中
			.displayer(new RoundedBitmapDisplayer(0))	// 设置成圆角图片,括号中参数为角度
			.build();
		
		photo_options = new DisplayImageOptions.Builder()
			.showStubImage(R.drawable.loading_4x3)			// 设置图片下载期间显示的图片
			.showImageForEmptyUri(R.drawable.default_photo)	// 设置图片Uri为空或是错误的时候显示的图片
			.showImageOnFail(R.drawable.default_photo)		// 设置图片加载或解码过程中发生错误显示的图片	
			.cacheInMemory(true)						// 设置下载的图片是否缓存在内存中
			.cacheOnDisc(true)							// 设置下载的图片是否缓存在SD卡中
			.displayer(new RoundedBitmapDisplayer(corner))	// 设置成圆角图片,括号中参数为角度
			.build();
	}
	
	/**
	 * 图片加载第一次显示监听器
	 * @author Administrator
	 *
	 */
	private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {
		
		static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

		@Override
		public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
			if (loadedImage != null) {
				ImageView imageView = (ImageView) view;
				// 是否第一次显示
				boolean firstDisplay = !displayedImages.contains(imageUri);
				if (firstDisplay) {
					// 图片淡入效果
					FadeInBitmapDisplayer.animate(imageView, 500);
					displayedImages.add(imageUri);
				}
			}
		}
	}
	
	/**
	 * 显示图片
	 * @param iamgeUrl 图片url
	 * @param imageView 显示图片的控件
	 * @author lims
	 * @date 2015-04-23
	 */
	public static void loadImage(String iamgeUrl, ImageView imageView){
		init(0);
		ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
		/**
		 * 显示图片
		 * 参数1：图片url
		 * 参数2：显示图片的控件
		 * 参数3：显示图片的设置
		 * 参数4：监听器
		 */
		imageLoader.displayImage(iamgeUrl, imageView, image_options, animateFirstListener);
	}
	
	/**
	 * 显示头像
	 * @param iamgeUrl 图片url
	 * @param imageView 显示图片的控件
	 * @author lims
	 * @date 2015-04-23
	 */
	public static void loadPhoto(String iamgeUrl, ImageView imageView, int corner){
		init(corner);
		ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
		/**
		 * 显示图片
		 * 参数1：图片url
		 * 参数2：显示图片的控件
		 * 参数3：显示图片的设置
		 * 参数4：监听器
		 */
		imageLoader.displayImage(iamgeUrl, imageView, photo_options, animateFirstListener);
	}
	
}
