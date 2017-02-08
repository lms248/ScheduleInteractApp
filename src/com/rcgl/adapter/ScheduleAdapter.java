package com.rcgl.adapter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rcgl.R;
import com.rcgl.bean.ScheduleBean;
import com.rcgl.util.image.ImageUtils;
import com.rcgl.util.notification.AlarmUtil;


public  class ScheduleAdapter extends BaseAdapter {
  	
	private  List<ScheduleBean> schedulelist = null;
	private LayoutInflater mlayoutInflater;
	Context context;
	/**
	 * 自定义接口，用于回调按钮点击事件到Activity
	 * @param context
	 */
	/*public interface Callback{
		public void click(View v);
	}*/
	
	public ScheduleAdapter(Context context,List<ScheduleBean> schedulelist) {
				super();
				this.mlayoutInflater = LayoutInflater.from(context);
				this.context=context;
				this.schedulelist=schedulelist;
			}
	
		@Override
		public int getCount() {
			if(schedulelist==null) return 0;
			return schedulelist.size();
		}

		@Override
		public Object getItem(int position) {
			return schedulelist.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = View.inflate(context,
						R.layout.item_schedule, null);
				new ViewHolder(convertView);
			}
			
			ViewHolder holder = (ViewHolder) convertView.getTag();
			
			Date now = new Date(); //new Date()为获取当前系统时间
    		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");///设置日期格式
    		String nowTime = df.format(now);
    		
			Bitmap bmSrc = BitmapFactory.decodeResource(convertView.getResources(),schedulelist.get(position).getImage()); 
		    if(nowTime.compareTo(schedulelist.get(position).getDotime())>0){
		    	bmSrc = ImageUtils.bitmap2Gray(bmSrc);
		    }
		    /*Bitmap rounded_bm = ImageUtils.getRoundedCornerBitmap(bmSrc,60f);
		    Drawable bitmapDrawable = new BitmapDrawable(rounded_bm);*/
		    Drawable bitmapDrawable = new BitmapDrawable(bmSrc);
			
			holder.iv_icon.setImageDrawable(bitmapDrawable);
			holder.tv_dotime.setText(schedulelist.get(position).getDotime());
			holder.tv_content.setText(schedulelist.get(position).getContent());
			if(schedulelist.get(position).getOpen()==0){
				holder.tv_noOpen.setVisibility(View.VISIBLE);
			}
			else holder.tv_noOpen.setVisibility(View.GONE);
			if(schedulelist.get(position).getAlarm()==1){
				holder.iv_alarm.setVisibility(View.VISIBLE);
				/** 通知提醒 */
				AlarmUtil.initAlarm(context,schedulelist.get(position).getDotime(), 
						schedulelist.get(position).getContent(),schedulelist.get(position).getScheduleid());
			}
			else holder.iv_alarm.setVisibility(View.GONE);
			if(schedulelist.get(position).getStatus()==1){
				holder.schedule_complete.setVisibility(View.VISIBLE);
			}
			else {
				holder.schedule_complete.setVisibility(View.GONE);
			}
			return convertView;
		}

		class ViewHolder {
			ImageView iv_icon;
			TextView tv_dotime;
			TextView tv_content;
			TextView tv_noOpen;
			ImageView iv_alarm;
			ImageView schedule_complete;
			public ViewHolder(View view) {
				iv_icon = (ImageView) view.findViewById(R.id.schedule_icon);
				tv_dotime = (TextView) view.findViewById(R.id.schedule_time);
				tv_content = (TextView) view.findViewById(R.id.schedule_content);
				tv_noOpen = (TextView) view.findViewById(R.id.tab_noOpen);
				iv_alarm = (ImageView) view.findViewById(R.id.schedule_alarm);
				schedule_complete = (ImageView) view.findViewById(R.id.schedule_complete);
				view.setTag(this);
			}
		}
		
	}