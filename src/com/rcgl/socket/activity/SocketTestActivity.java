package com.rcgl.socket.activity;

import java.net.URI;
import java.net.URISyntaxException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.drafts.Draft_76;
import org.java_websocket.handshake.ServerHandshake;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.rcgl.R;
/**
 * 友聊——聊天室
 * @author lims
 * @date 2015-05-13
 */
public class SocketTestActivity extends Activity implements OnClickListener{
	Draft []drafts={new Draft_17(),new Draft_76()};
	public EditText mContent;//fragment_socket_content(内容)
	public Button mSend;//fragment_socket_send（发送button）
	public TextView mReceive;//fragment_socket_receive_message(收到的消息)
	public  WebSocketClientUtil mWebSocketClientUtil;
	public String url;
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_socket);
        init();
	}

	public void init(){
		mContent=(EditText)findViewById(R.id.fragment_socket_content);
		mSend=(Button)findViewById(R.id.fragment_socket_send);
		mSend.setOnClickListener(this);
		mReceive=(TextView)findViewById(R.id.fragment_socket_receive_message);
		url="ws://124.172.173.116:20004/rcgl/RCGLServer/message";
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Log.v("Runnable","Runnable_coming");
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
		switch (view.getId()) {
		case R.id.fragment_socket_send://发送
			mWebSocketClientUtil.send(mContent.getText().toString());
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
		}

		@Override
		public void onError(Exception arg0) {
			Log.v("websockeError.", arg0+"");
		}

		@Override
		public void onMessage(String s) {
			final CharSequence message=s;
			Log.v("onMessage", message+"");
//			ToastUtils.ShowToast(getApplicationContext(),message+"");
			mReceive.setText(message);
		}

		@Override
		public void onOpen(ServerHandshake arg0) {
			Log.v("websockeOpen.", arg0.getHttpStatusMessage());
		}
	}
	@Override
	public void onBackPressed() {
		this.finish();
//		mWebSocketClientUtil=null;
		super.onBackPressed();
	}

}
