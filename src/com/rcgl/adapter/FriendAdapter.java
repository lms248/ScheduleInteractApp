package com.rcgl.adapter;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.rcgl.R;
import com.rcgl.bean.UserBean;
import com.rcgl.data.ShareConstants;
import com.rcgl.util.ServerUrlUtil;
import com.rcgl.util.image.MyImageLoader;


public  class FriendAdapter extends BaseAdapter {
  	
	private  List<UserBean> userlist = null;
	private LayoutInflater mlayoutInflater;
	Context context;
	
	public FriendAdapter(Context context,List<UserBean> userlist) {
				super();
				this.mlayoutInflater = LayoutInflater.from(context);
				this.context=context;
				this.userlist=userlist;
			}
	
		@Override
		public int getCount() {
			if(userlist==null) return 0;
			return userlist.size();
		}

		@Override
		public Object getItem(int position) {
			return userlist.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = View.inflate(context,R.layout.item_friend, null);
				new ViewHolder(convertView);
			}
			ViewHolder holder = (ViewHolder) convertView.getTag();
			//Item item = getItem(position);
			
			/*Bitmap bmSrc = BitmapFactory.decodeResource(convertView.getResources(),userlist.get(position).getImage()); 
		    Bitmap rounded_bm = ImageUtils.getRoundedCornerBitmap(bmSrc,30f);
		    Drawable bitmapDrawable = new BitmapDrawable(rounded_bm);
		    holder.iv_photo.setImageDrawable(bitmapDrawable);*/
		    
		    String photoUrl = ServerUrlUtil.SERVER_BASCE_URL+"/rcgl/upload/"+userlist.get(position).getPhoto();
		    MyImageLoader.loadPhoto(photoUrl, holder.iv_photo, 0);
		    
		    //holder.iv_photo.setImageResource(R.drawable.icon_long);
			//holder.tv_name.setText(item.loadLabel(getActivity().getPackageManager()));
			//Log.e("listDate", listDate.toString());
			holder.tv_name.setText(userlist.get(position).getUsername());
			
			if(userlist.get(position).getCheckBoxVisibility()){
				holder.cb_select_friend.setVisibility(View.VISIBLE);
			}
			else {
				holder.cb_select_friend.setVisibility(View.GONE);
			}
			holder.cb_select_friend.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					// TODO Auto-generated method stub
					String userid = userlist.get(position).getUserid()+"";
					if(isChecked){
						if(ShareConstants.tofriends.equals("")){
							ShareConstants.tofriends = userid;
						}
						else {
							ShareConstants.tofriends = ShareConstants.tofriends+","+userid;
						}
					}
					else {
						String[] friends = ShareConstants.tofriends.split(",");
						ShareConstants.tofriends = "";
						for(int i=0; i<friends.length; i++){
							if(!friends[i].equals(userid)) {
								if(i==0 || (i==1 && friends[0].equals(userid))){
									ShareConstants.tofriends = friends[i];
								}
								else ShareConstants.tofriends += "," + friends[i];
							}
						}
					}
				}
			});
			
			return convertView;
		}

		class ViewHolder {
			ImageView iv_photo;
			TextView tv_name;
			CheckBox cb_select_friend;

			public ViewHolder(View view) {
				iv_photo = (ImageView) view.findViewById(R.id.iv_photo);
				tv_name = (TextView) view.findViewById(R.id.tv_name);
				cb_select_friend = (CheckBox) view.findViewById(R.id.cb_select_friend);
				view.setTag(this);
			}
		}
		
		
	}