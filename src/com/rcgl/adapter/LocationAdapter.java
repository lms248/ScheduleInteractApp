package com.rcgl.adapter;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.rcgl.R;
import com.rcgl.bean.LocationBean;
import com.rcgl.util.ServerUrlUtil;
import com.rcgl.util.image.MyImageLoader;

/**
 * 定位数据适配器
 * @author lims
 * @date 2015-05-05
 */
public  class LocationAdapter extends BaseAdapter {
  	
	private  List<LocationBean> locationlist = null;
	private LayoutInflater mlayoutInflater;
	Context context;
	
	public LocationAdapter(Context context,List<LocationBean> locationlist) {
				super();
				this.mlayoutInflater = LayoutInflater.from(context);
				this.context=context;
				this.locationlist=locationlist;
			}
	
		@Override
		public int getCount() {
			if(locationlist==null) return 0;
			return locationlist.size();
		}

		@Override
		public Object getItem(int position) {
			return locationlist.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = View.inflate(context,R.layout.item_location, null);
				new ViewHolder(convertView);
			}
			ViewHolder holder = (ViewHolder) convertView.getTag();
			
		    String photoUrl = ServerUrlUtil.SERVER_BASCE_URL+"/rcgl/upload/"+locationlist.get(position).getPhoto();
		    MyImageLoader.loadPhoto(photoUrl, holder.iv_photo, 0);
		    
			holder.tv_name.setText(locationlist.get(position).getUsername());
			holder.tv_distance.setText(locationlist.get(position).getDistance()+"米");
			if(locationlist.get(position).getIsfriend()==1){
				holder.tv_friend.setText("好友");
				holder.tv_friend.setVisibility(View.VISIBLE);
			}
			else if(locationlist.get(position).getIsfriend()==2){
				holder.tv_friend.setText("我");
				holder.tv_friend.setVisibility(View.VISIBLE);
			}
			else holder.tv_friend.setVisibility(View.GONE);
			return convertView;
		}

		class ViewHolder {
			ImageView iv_photo;
			TextView tv_name;
			TextView tv_distance;
			TextView tv_friend;
			public ViewHolder(View view) {
				iv_photo = (ImageView) view.findViewById(R.id.iv_photo);
				tv_name = (TextView) view.findViewById(R.id.tv_name);
				tv_distance = (TextView) view.findViewById(R.id.tv_distance);
				tv_friend = (TextView) view.findViewById(R.id.tab_friend);
				view.setTag(this);
			}
		}
		
		
	}