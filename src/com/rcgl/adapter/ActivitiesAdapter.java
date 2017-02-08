package com.rcgl.adapter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rcgl.R;
import com.rcgl.bean.ActivitiesBean;
import com.rcgl.util.ServerUrlUtil;
import com.rcgl.util.image.MyImageLoader;


public  class ActivitiesAdapter extends BaseAdapter {
  	
	private  List<ActivitiesBean> activitieslist = null;
	private LayoutInflater mlayoutInflater;
	Context context;
	protected TextView tv_already_number_people;
	
	/** 存放holder */
	Map<String, Object> holderMap = new HashMap<String, Object>();
	
	public ActivitiesAdapter(Context context,List<ActivitiesBean> activitieslist) {
				super();
				this.mlayoutInflater = LayoutInflater.from(context);
				this.context=context;
				this.activitieslist=activitieslist;
			}
	
		@Override
		public int getCount() {
			if(activitieslist==null) return 0;
			return activitieslist.size();
		}

		@Override
		public Object getItem(int position) {
			return activitieslist.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = View.inflate(context,
						R.layout.item_activities, null);
				new ViewHolder(convertView);
			}
			ViewHolder holder = (ViewHolder) convertView.getTag();
			
			String photoURL = ServerUrlUtil.SERVER_BASCE_URL+"/rcgl/upload/"+activitieslist.get(position).getUserphoto();
			String imageURL = ServerUrlUtil.SERVER_BASCE_URL+"/rcgl/upload/"+activitieslist.get(position).getImage();
			MyImageLoader.loadPhoto(photoURL, holder.iv_photo, 90);
			MyImageLoader.loadImage(imageURL, holder.iv_image);
			
			holder.tv_username.setText(activitieslist.get(position).getUsername());
			holder.tv_dotime.setText(activitieslist.get(position).getDotime());
			holder.tv_content.setText(activitieslist.get(position).getContent());
			if(activitieslist.get(position).getSort().equals("1")){
				holder.tv_sort.setText("运动");
				holder.tv_sort.setBackgroundColor(Color.parseColor("#FF9933"));
			}
			else if(activitieslist.get(position).getSort().equals("2")){
				holder.tv_sort.setText("旅行");
				holder.tv_sort.setBackgroundColor(Color.parseColor("#99CC99"));
			}
			else if(activitieslist.get(position).getSort().equals("3")){
				holder.tv_sort.setText("学习");
				holder.tv_sort.setBackgroundColor(Color.parseColor("#FFCC66"));
			}
			else if(activitieslist.get(position).getSort().equals("4")){
				holder.tv_sort.setText("休闲");
				holder.tv_sort.setBackgroundColor(Color.parseColor("#66CCCC"));
			}
			else {
				holder.tv_sort.setText("其他");
				holder.tv_sort.setBackgroundColor(Color.parseColor("#CCCC99"));
			}
			if(activitieslist.get(position).getMax_number_people()==0){
				holder.tv_max_number_people.setText("无人数限制");
			}
			else {
				holder.tv_max_number_people.setText("限制："+activitieslist.get(position).getMax_number_people()+"人");
			}
			holder.tv_already_number_people.setText("已报："+activitieslist.get(position).getAlready_number_people()+"人");
			
			if(activitieslist.get(position).getIsJoin().equals("-1")){
				holder.iv_yue.setVisibility(View.VISIBLE);
				holder.iv_yue.setImageResource(R.drawable.wofa);
			}
			else if(activitieslist.get(position).getIsJoin().equals("1")){
				holder.iv_yue.setVisibility(View.VISIBLE);
				holder.iv_yue.setImageResource(R.drawable.woyue);
			}
			else {
				holder.iv_yue.setVisibility(View.GONE);
			}
			
			if(activitieslist.get(position).getIsCollect().equals("1")){
				holder.iv_collect.setVisibility(View.VISIBLE);
			}
			else {
				holder.iv_collect.setVisibility(View.GONE);
			}
			
			holderMap.put(""+position, holder);
			
			return convertView;
		}

		class ViewHolder {
			ImageView iv_photo;
			ImageView iv_image;
			TextView tv_username;
			TextView tv_dotime;
			TextView tv_content;
			TextView tv_max_number_people;
			TextView tv_already_number_people;
			TextView tv_sort;
			ImageView iv_yue;//标记是否已报名
			ImageView iv_collect;//标记是否已收藏
			public ViewHolder(View view) {
				iv_photo = (ImageView) view.findViewById(R.id.userPhoto);
				iv_image = (ImageView) view.findViewById(R.id.image);
				tv_username = (TextView) view.findViewById(R.id.username);
				tv_dotime = (TextView) view.findViewById(R.id.activities_time);
				tv_content = (TextView) view.findViewById(R.id.activities_content);
				tv_max_number_people = (TextView) view.findViewById(R.id.max_mumber_people);
				tv_already_number_people = (TextView) view.findViewById(R.id.already_mumber_people);
				tv_sort = (TextView) view.findViewById(R.id.tab_sort);
				iv_yue = (ImageView) view.findViewById(R.id.tab_yue);
				iv_collect = (ImageView) view.findViewById(R.id.tab_collect);
				view.setTag(this);
			}
		}
		
}
