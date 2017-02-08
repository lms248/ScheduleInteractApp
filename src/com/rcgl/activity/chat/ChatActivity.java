package com.rcgl.activity.chat;

import java.net.URI;
import java.net.URISyntaxException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.drafts.Draft_76;
import org.java_websocket.handshake.ServerHandshake;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rcgl.R;
import com.rcgl.util.ToastUtils;
/**
 * 友聊——聊天室
 * @author lims
 * @date 2015-05-13
 */
public class ChatActivity extends Activity implements OnClickListener{
	/** 返回上一界面 */
	private ImageView iv_back;
	/** 消息列表 */
	private LinearLayout linearLayout_chat_message;
	Draft []drafts={new Draft_17(),new Draft_76()};
	/** 发送内容 */
	private EditText et_content;
	/** 发送按钮 */
	private Button bt_send;
	private  WebSocketClientUtil mWebSocketClientUtil;
	private String url;
	/** 聊天窗口消息 */
	private String chat_message;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        
        getEntities();
        initListeners();
        initDatas();
        
	}
	
	private void getEntities(){
		iv_back = (ImageView)findViewById(R.id.bar_image_back);
		linearLayout_chat_message = (LinearLayout)findViewById(R.id.chat_message);
		et_content = (EditText)findViewById(R.id.et_send_message);
		bt_send = (Button)findViewById(R.id.bt_send_message);
	}
	private void initListeners(){
		iv_back.setOnClickListener(this);
		bt_send.setOnClickListener(this);
	}
	public void initDatas(){
		url="ws://124.172.173.116:20004/rcgl/RCGLServer/message";
		//url="ws://172.16.10.122:8080/rcgl/RCGLServer/message";
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						if(mWebSocketClientUtil==null){
							mWebSocketClientUtil=new WebSocketClientUtil(new URI(url), drafts[0]);
							mWebSocketClientUtil.connect();
						}
					} catch (URISyntaxException e) {
						e.printStackTrace();
					} 
				}
			}).start();
	}
	
	@Override
	public void onClick(View view) {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);  
		switch (view.getId()) {
		case R.id.bar_image_back://返回上一次界面
			//关掉虚拟软键盘
	        imm.hideSoftInputFromWindow(et_content.getWindowToken(), 0) ;
			finish();
			break;
		case R.id.bt_send_message://发送
			mWebSocketClientUtil.send(et_content.getText().toString());
			//关掉虚拟软键盘
	        imm.hideSoftInputFromWindow(et_content.getWindowToken(), 0) ;
	        et_content.setText("");
			break;
		default:
			break;
		}
	}
	
	public class WebSocketClientUtil extends WebSocketClient {
		public WebSocketClientUtil(URI serverUri, Draft draft) {
			super(serverUri, draft);
		}

		@Override
		public void onClose(int arg0, String arg1, boolean arg2) {
			
			System.out.println(arg0+";"+arg1+";"+arg2);
		}

		@Override
		public void onError(Exception arg0) {
			Log.e("websockeError.", arg0+"");
		}

		@Override
		public void onMessage(String s) {
			final CharSequence message=s;
			Log.e("onMessage", message+"");
			chat_message = s;
			showMessageHandler.sendEmptyMessage(200);
		}

		@Override
		public void onOpen(ServerHandshake arg0) {
			System.out.println(arg0+"------------------");
			Log.e("websockeOpen.", arg0.getHttpStatusMessage());
		}
	}
	@Override
	public void onBackPressed() {
		this.finish();
//		mWebSocketClientUtil=null;
		super.onBackPressed();
	}
	
	
	/**
     * 处理窗口显示聊天数据
     * @author lims
     * @date 2015-05-13
     */
	Handler showMessageHandler = new Handler() {
		@Override
		//当有消息发送出来的时候就执行Handler的这个方法 
	    public void handleMessage(Message msg) {
			super.handleMessage(msg);  
			if(msg.what==200){
				LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
				View layout = inflater.inflate(R.layout.item_chat_message, null);
				ImageView userPhoto = (ImageView) layout.findViewById(R.id.iv_photo);
				TextView chatMessage = (TextView)layout.findViewById(R.id.tv_message);
				chatMessage.setText(chat_message);
				linearLayout_chat_message.addView(layout);
			}
			else {
				ToastUtils.showToast(getApplicationContext(),"发送异常");
			}
	    }
	};
}
