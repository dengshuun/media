package com.hwatong.ipodui;

import java.util.List;

import com.hwatong.comnon.Constant;
import com.hwatong.ipod.IService;
import com.hwatong.ipod.NowPlaying;
import com.hwatong.ipod.Playlist;
import com.hwatong.media.R;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;

public class IPodMainActivity extends Activity implements OnClickListener{
	/**
	 * IPod上层接口
	 */
	private IService mService = null;
	
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			
			Log.d(Constant.TAG_IPOD , "msg:"+msg.what);
			switch (msg.what) {
			case Constant.MSG_MEDIAITEM_RECEIVED:
				
				break;
			case Constant.MSG_MEDIALIBRARYINFORMATION_RECEIVED:
				
				break;
			case Constant.MSG_MEDIAPLAYLIST_RECEIVED:
				refreshPlayList(getPlayList());
				break;
			case Constant.MSG_NOWPLAYING_RECEIVED:
				refreshProgress(getNowing(false));
				break;
			case Constant.MSG_PROBE_DEVICE:
				
				break;
			case Constant.MSG_REMOVE_DEVICE:
				
				break;
			case Constant.MSG_START_FILETRANSFER:
				
				break;
			case Constant.MSG_STOP_FILETRANSFER:
				
				break;
			}
		}
	};
	/**
	 * IPod ,事件回调
	 */
	private com.hwatong.ipod.ICallback.Stub mCallback = new com.hwatong.ipod.ICallback.Stub() {
		
		@Override
		public void onNowPlayingReceived() throws RemoteException {
			handler.removeMessages(Constant.MSG_NOWPLAYING_RECEIVED);
			handler.sendEmptyMessage(Constant.MSG_NOWPLAYING_RECEIVED);			
		}
		
		@Override
		public void onMediaPlaylistReceived() throws RemoteException {
			handler.removeMessages(Constant.MSG_MEDIAPLAYLIST_RECEIVED);
			handler.sendEmptyMessage(Constant.MSG_MEDIAPLAYLIST_RECEIVED);			
		}
		
		@Override
		public void onMediaItemReceived() throws RemoteException {
			handler.removeMessages(Constant.MSG_MEDIAITEM_RECEIVED);
			handler.sendEmptyMessage(Constant.MSG_MEDIAITEM_RECEIVED);			
		}
		
		@Override
		public void onDetached() throws RemoteException {
			/**
			 * IPod设备移除
			 */
			handler.removeMessages(Constant.MSG_REMOVE_DEVICE);
			handler.sendEmptyMessage(Constant.MSG_REMOVE_DEVICE);			
		}
		
		@Override
		public void onAttached() throws RemoteException {
			/**
			 * IPod设备插入
			 */
			handler.removeMessages(Constant.MSG_PROBE_DEVICE);
			Message m = Message.obtain(handler, Constant.MSG_PROBE_DEVICE, 1, 0);
			handler.sendMessage(m);
		}
	};
	/**
	 * IPod服务连接
	 */
	private ServiceConnection mServiceConnection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			if(mService!=null){
				try {
					mService.unregisterCallback(mCallback);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			mService = null ;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mService = IService.Stub.asInterface(service);
			
			if(mService!=null){
				try {
					mService.registerCallback(mCallback);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	};
	/**
	 * 接收媒体播放状态 ， 根据需求释放IPOD
	 */
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(Constant.TAG_IPOD , "action:"+intent.getAction());
			String tag = null;
			if (intent.hasExtra("tag")) {
				tag = intent.getStringExtra("tag");
			}
			if (!tag.equals("iPod")) {
				finish();
			}
		}


	}; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ipod);
		bindService(new Intent(Constant.IPOD_SERVICE), mServiceConnection ,Context.BIND_AUTO_CREATE);
		registerReceiver(mReceiver , new IntentFilter(Constant.MEDIA_PLAY_STATUS));
		initUI();
	}
	/**
	 * 更新歌曲列表
	 * @param playList
	 */
	protected void refreshPlayList(List<Playlist> playList) {
		
	}

	/**
	 * 更新播放歌曲进度，信息
	 * @param nowing
	 */
	protected void refreshProgress(NowPlaying nowing) {
		
		
	}

	private void initUI() {
		
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(mServiceConnection);
		unregisterReceiver(mReceiver);
	}
	
	
	
	/**
	 * 获取所有的歌曲信息
	 * @return
	 */
	private List<Playlist> getPlayList(){
		List<Playlist> list = null ;
		if(mService != null){
			try {
				list = mService.getPlaylistList();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return list ;
	}
	/**
	 * 获取当前歌曲信息
	 * @return
	 */
	private NowPlaying getNowing(boolean full){
		NowPlaying nowPlaying = null ;
		if(mService != null){
			try {
				mService.getNowPlaying(full);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return nowPlaying ;
	}
	
	@Override
	public void onClick(View v) {
	}
}
