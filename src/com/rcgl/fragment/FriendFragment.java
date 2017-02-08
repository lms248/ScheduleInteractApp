package com.rcgl.fragment;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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
import org.apache.http.util.EncodingUtils;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.ClipData.Item;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.rcgl.R;
import com.rcgl.activity.friend.AddFriendActivity;
import com.rcgl.activity.friend.ReadFriendScheduleActivity;
import com.rcgl.adapter.FriendAdapter;
import com.rcgl.app.MuApp;
import com.rcgl.app.MyApplication;
import com.rcgl.bean.UserBean;
import com.rcgl.swipemenulistview.util.SwipeMenu;
import com.rcgl.swipemenulistview.util.SwipeMenuCreator;
import com.rcgl.swipemenulistview.util.SwipeMenuItem;
import com.rcgl.swipemenulistview.util.SwipeMenuListView;
import com.rcgl.swipemenulistview.util.SwipeMenuListView.OnMenuItemClickListener;
import com.rcgl.swipemenulistview.util.SwipeMenuListView.OnSwipeListener;
import com.rcgl.util.LoadingDialog;
import com.rcgl.util.NetUtil;
import com.rcgl.util.ServerUrlUtil;
import com.rcgl.util.ToastUtils;

public class FriendFragment extends Fragment implements OnClickListener{
	/** ListView 的数据 */
	private List<UserBean>mFriendList;
	/** 好友适配器 */
	private FriendAdapter friendAdapter;
	/** 滑动ListView */
	private SwipeMenuListView mListView;
	/** 标题栏的标题文字 */
	private TextView bar_title;
	/** 标题栏的查询日程图标 */
	private ImageView bar_search;
	/** 搜索栏输入框 */
	private EditText search_text;
	/** 搜索框清除按钮 */
	private ImageView search_clear;
	/** 标题栏的添加好友图标 */
	private ImageView bar_image;
	
	Boolean have_content = true;
	
	/** 登录的用户ID */
	int userid = 0;
	
	/** http请求参数 */
    List<NameValuePair> pairList;
    /** 本地好友文件 */
    String localFriendStr = null;
    /** 搜索关键字 */
	private String keyword = "";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getActivity().findViewById(R.id.bar).setVisibility(View.VISIBLE);
		return inflater.inflate(R.layout.fragment_friend, null);		
	}	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		MyApplication.initImageLoader(getActivity());
		MuApp.getInstance().addActivity(getActivity());
		
		getEntities();
		initListeners();
		initView();
		initDatas();
	}
	
	private void getEntities(){
		bar_title = (TextView)getActivity().findViewById(R.id.bar_title);
        bar_search = (ImageView)getActivity().findViewById(R.id.bar_search);
        search_text = (EditText) getActivity().findViewById(R.id.search_text);
		search_clear = (ImageView) getActivity().findViewById(R.id.search_clear);
        bar_image = (ImageView)getActivity().findViewById(R.id.bar_image);
        mListView = (SwipeMenuListView) getActivity().findViewById(R.id.listView);
	}
	private void initListeners(){
		bar_image.setOnClickListener(this);
		bar_search.setOnClickListener(this);
		search_text.addTextChangedListener(textWatcher);
		search_clear.setOnClickListener(this);
	}
	private void initView(){
		bar_title.setText("我的好友");
        bar_search.setImageResource(R.drawable.tab_search);
        bar_search.setVisibility(View.VISIBLE);
        bar_image.setImageResource(R.drawable.tab_addfriend);
        getActivity().findViewById(R.id.no_content_tip).setVisibility(View.INVISIBLE);
	}
	private void initDatas(){
		SharedPreferences sharedPreferences = getActivity().getSharedPreferences("loginUserSP", Context.MODE_PRIVATE);
		userid = sharedPreferences.getInt("userid", 0);
        
		//显示进度框提示
        LoadingDialog.showLoadingDialog(getActivity(), "");
        
        /** 显示本地好友的线程 */
        Thread showLocalFriendThread = new Thread(showLocalFriend_runnable);
        showLocalFriendThread.start();
        
        if(NetUtil.isConnect(getActivity())){
        	/** 加载网络好友数据的线程 */
            Thread loadNetFriendThread = new Thread(loadNetFriend_runnable);
            loadNetFriendThread.start();
        }
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.bar_image://添加好友
			onAddFriendPage();
			break;
		case R.id.search_clear://清空搜索栏
			search_text.setText("");
			search_clear.setVisibility(View.GONE);
			break;
		case R.id.bar_search://标题栏搜索图标
			if(getActivity().findViewById(R.id.search_item).getVisibility()==View.GONE) {
				getActivity().findViewById(R.id.search_item).setVisibility(View.VISIBLE);
			}
			else {
				getActivity().findViewById(R.id.search_item).setVisibility(View.GONE);
				search_text.setText("");
				//关掉虚拟软键盘
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);    
		        imm.hideSoftInputFromWindow(search_text.getWindowToken(), 0) ; 
			}
			break;
		}
	}
	
	/** 搜索框文字监听 */
	private TextWatcher textWatcher = new TextWatcher() {  
        @Override 
        public void beforeTextChanged(CharSequence s, int start, int count,  
                int after) {  
            // TODO Auto-generated method stub  
        }  
 
         @Override    
        public void onTextChanged(CharSequence s, int start, int before,     
                int count) {     
        	 // TODO Auto-generated method stub
        	 keyword = search_text.getText().toString().trim();
        	 if(keyword.equals("")) search_clear.setVisibility(View.GONE);
        	 else search_clear.setVisibility(View.VISIBLE);
             /** 显示本地好友的线程 */
             Thread showLocalFriendThread = new Thread(showLocalFriend_runnable);
             showLocalFriendThread.start();
        }

		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub
		}                    
    };
	
	// step 1. create a MenuCreator
	SwipeMenuCreator creator = new SwipeMenuCreator() {
		@Override
		public void create(SwipeMenu menu) {
			SwipeMenuItem openItem = new SwipeMenuItem(getActivity().getApplicationContext());
			//openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,0xCE)));
			openItem.setBackground(new ColorDrawable(Color.rgb(0x99, 0xCC,0xCC)));
			openItem.setWidth(dp2px(90));
			openItem.setIcon(R.drawable.ic_action_submit);
			//openItem.setTitle("Open");
			openItem.setTitleSize(18);
			openItem.setTitleColor(Color.WHITE);
			menu.addMenuItem(openItem);

			SwipeMenuItem deleteItem = new SwipeMenuItem(getActivity().getApplicationContext());
			deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,0x3F, 0x25)));
			deleteItem.setWidth(dp2px(90));
			deleteItem.setIcon(R.drawable.ic_delete);
			menu.addMenuItem(deleteItem);
		}
	};
		
	
	/** 侧滑时点击事件 */
	SwipeMenuListView.OnMenuItemClickListener onMenuClickItem = new OnMenuItemClickListener() {
		@Override
		public boolean onMenuItemClick(final int position, SwipeMenu menu, int index) {
			switch (index) {
			case 0:
				//open(item);
				ToastUtils.showToast(getActivity(),"给"+mFriendList.get(position).getUsername()+"发送消息");
				break;
			case 1:
				//delete(item);
				final Dialog deleteDialog = new Dialog(getActivity(), R.style.option_dialog);
				deleteDialog.setContentView(R.layout.dialog_confirm);
	            ((TextView) deleteDialog.findViewById(R.id.dialog_title)).setText("你真的要删除吗？");
	            Button btn_yes = (Button) deleteDialog.findViewById(R.id.btn_yes);
	            btn_yes.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						mFriendList.remove(position);
						friendAdapter.notifyDataSetChanged();
						deleteDialog.dismiss();
					}
				});
	            Button btn_no = (Button) deleteDialog.findViewById(R.id.btn_no);
	            btn_no.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						deleteDialog.dismiss();
					}
				});
	            deleteDialog.show();
				break;
			}
			return false;
		}
	};
	
	/** 长按时事件 */
	SwipeMenuListView.OnItemLongClickListener onLongClickItem = new OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			ToastUtils.showToast(getActivity(),position + " long click");
			return false;
		}
	};
	
	/** 短按时事件 */
	OnItemClickListener onClickItem = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if(!NetUtil.isConnect(getActivity())){
            	ToastUtils.showToast(getActivity(), "网络未连接");
            	return;
            }
			Intent intent = new Intent();
    		intent.setClass(getActivity().getApplicationContext(), ReadFriendScheduleActivity.class);
    		intent.putExtra("friendname", mFriendList.get(position).getUsername().toString());
    		intent.putExtra("friendid", Integer.valueOf(mFriendList.get(position).getUserid()));
    		startActivity(intent);
		}
	};
	
	SwipeMenuListView.OnSwipeListener swipeListener = new OnSwipeListener() {
		@Override
		public void onSwipeStart(int position) {
			// swipe start
		}
				
		@Override
		public void onSwipeEnd(int position) {
			// swipe end
		}
	};
	
	/** 添加好友页面 */
	private void onAddFriendPage(){
		if(userid==0){
			ToastUtils.showToast(getActivity(),"你还未登录哦，先去登录再来？");
			return;
		}
		else {
			Intent intent = new Intent();
    		intent.setClass(getActivity(), AddFriendActivity.class);
    		startActivity(intent);
    		//finish();
		}
	}
	
    Runnable showLocalFriend_runnable =  new Runnable() {
    	@Override
		public void run() {
    		try
            {	
    			showFriend_handler.sendEmptyMessage(200);
    			
    			localFriendStr = readFile("myfriend.txt");//读取本地好友资源文件
                if(localFriendStr==null||localFriendStr.equals("")){
              	  have_content = false;
              	  show_no_content_tip();
              	  return;
                }
                
                showFriend(localFriendStr);
                
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
		}
	};
	
	/**
     * http请求处理函数
     * @author lims
     * @date 2015-03-26
     */
	Handler showFriend_handler = new Handler() {
		@Override
		//当有消息发送出来的时候就执行Handler的这个方法 
	    public void handleMessage(Message msg) {
			super.handleMessage(msg);  
			if(msg.what==200){
				//配置适配器  
                friendAdapter = new FriendAdapter(getActivity(),mFriendList);
          		mListView.setAdapter(friendAdapter);
        		mListView.setMenuCreator(creator);
        		mListView.setOnMenuItemClickListener(onMenuClickItem);
        		mListView.setOnSwipeListener(swipeListener);
        		//listView.setCloseInterpolator(new BounceInterpolator());
        		mListView.setOnItemLongClickListener(onLongClickItem);
        		mListView.setOnItemClickListener(onClickItem);
        		
			}
			else {
				ToastUtils.showToast(getActivity(),"获取用户列表失败");
				show_no_content_tip();
			}
            //只要执行到这里就关闭对话框  
			LoadingDialog.closeLoadingDialog(); 
	    }
	};
	
	Runnable loadNetFriend_runnable =  new Runnable() {
    	@Override
		public void run() {
    		try
            {	
    			//读取SharedPreferences中的数据
    			SharedPreferences sharedPreferences = getActivity().getSharedPreferences("loginUserSP", Context.MODE_PRIVATE);
    			//getString()第二个参数为缺省值，如果preference中不存在该key，将返回缺省值
    			int userid = sharedPreferences.getInt("userid", 0);
    			NameValuePair pair_userid = new BasicNameValuePair("userid",userid+"");
                
                pairList = new ArrayList<NameValuePair>();
                pairList.add(pair_userid);
                
    			HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList,HTTP.UTF_8);
                HttpPost httpPost = new HttpPost(ServerUrlUtil.Server_GetFriendList_URL);
                // 将请求体内容加入请求中
                httpPost.setEntity(requestHttpEntity);
                // 需要客户端对象来发送请求
                HttpClient httpClient = new DefaultHttpClient();
                // 发送请求
                HttpResponse httpResponse = httpClient.execute(httpPost);
                //读返回数据  
                String strResult = EntityUtils.toString(httpResponse.getEntity());  
                Log.e("strResult", strResult);
                //若状态码为200 ok 
                if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK && !strResult.equals("-1")) {  
                	//异步更新日程数据
                	if(localFriendStr==null || !localFriendStr.equals(strResult)){
                		localFriendStr = strResult;
                		showFriend(strResult);
                		showFriend_handler.sendEmptyMessage(200);
                		writeFile("myfriend.txt",localFriendStr);
                	}
                }  
                else {  
                	loadNetFriend_handler.sendEmptyMessage(0);
                } 
            
            }
    		catch (Exception e)
    		{
    			e.printStackTrace();
    		}
    	}
	};
	
	/**
     * http请求处理函数
     * @date 2015-02-03
     */
	Handler loadNetFriend_handler = new Handler() {
		@Override
		//当有消息发送出来的时候就执行Handler的这个方法 
	    public void handleMessage(Message msg) {
			super.handleMessage(msg);  
			if(msg.what==200){
				
			}
			else {
				ToastUtils.showToast(getActivity(),"获取网络好友数据失败");
			}
            //只要执行到这里就关闭对话框  
			LoadingDialog.closeLoadingDialog(); 
	    }
	};
	
	
	private void delete(Item item) {
		
	}

	private void open(Item item) {
		
	}
	
	
	/** 提示是否有内容 */
	private void show_no_content_tip(){
		if(!have_content){
			getActivity().findViewById(R.id.no_content_tip).setVisibility(View.VISIBLE);
		}
	}
	
	private int dp2px(int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				getResources().getDisplayMetrics());
	}
	
	/** 
	 * 读取JSONArray数据显示好友到手机客户端
	 * @param friendStr JSONArray数据
	 * @throws JSONException
	 * @author lims
	 * @date 2015-04-01
	 */
	private void showFriend(String friendStr) throws JSONException{
		JSONObject jsonObject = new JSONObject(friendStr);
        JSONArray jsonArray = jsonObject.getJSONArray("user");
        Log.e("userinfo", jsonArray.getString(0));
        
        UserBean fbean=null;//存放好友信息
        mFriendList=new ArrayList<UserBean>();
        for(int i=0;i<jsonArray.length();i++){
        	JSONObject obj=jsonArray.getJSONObject(i);
        	if(keyword.length()>0 && !obj.getString("username").contains(keyword)){
        		continue;
        	}
        	fbean=new UserBean();
        	fbean.setUserid(Integer.parseInt(obj.getString("id")));
        	fbean.setUsername(obj.getString("username"));
        	fbean.setPhoto(obj.getString("photo"));
        	fbean.setTime(obj.getString("time"));
        	fbean.setCheckBoxVisibility(false);
        	mFriendList.add(fbean);
        }
	}
	
	/** 写数据到资源文件 */  
	private void writeFile(String fileName,String data) throws IOException{   
	  try{   
	  
	        FileOutputStream fout = getActivity().openFileOutput(fileName, Context.MODE_PRIVATE);   
	  
	        byte [] bytes = data.getBytes();   
	  
	        fout.write(bytes);   
	  
	        fout.close();   
	      }   
	  
	        catch(Exception e){   
	        e.printStackTrace();   
	       }   
	}
	/** 读取资源文件数据 */ 
	public String readFile(String fileName) throws IOException{   
	  String res="";   
	  try{   
	         FileInputStream fin = getActivity().openFileInput(fileName);   
	         int length = fin.available();   
	         byte [] buffer = new byte[length];   
	         fin.read(buffer);       
	         res = EncodingUtils.getString(buffer, "UTF-8");   
	         fin.close();       
	     }   
	     catch(Exception e){   
	         e.printStackTrace();   
	     }   
	     return res;   
	  
	}

}
