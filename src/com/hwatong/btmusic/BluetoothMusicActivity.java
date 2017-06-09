package com.hwatong.btmusic;

import com.hwatong.btphone.aidl.IGocsdkCallback.Stub;
import com.hwatong.btphone.aidl.IGocsdkService;
import com.hwatong.comnon.Constant;
import com.hwatong.media.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;

public class BluetoothMusicActivity extends Activity implements OnClickListener{
	
	/**
	 * 蓝牙音乐服务信息，以及回调
	 */
	private IGocsdkService mService = null ;
	private ServiceConnection mServiceConnection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			if(mService!=null){
				try {
					mService.unregisterCallback(callback);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			mService = null ;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mService = IGocsdkService.Stub.asInterface(service);
			if(mService!=null){
				try {
					mService.registerCallback(callback);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	};
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case Constant.MSG_HFP_CONNECTED:
				
				break;
			case Constant.MSG_HFP_DISCONNECTED:
				
				break;
			case Constant.MSG_MUSIC_PLAYING:
				
				break;
			case Constant.MSG_MUSIC_STOP:
				
				break;
			case Constant.MSG_MUSIC_INFO:
				
				break;
			}
		}
	};
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
            String tag = null;
            if(intent.hasExtra("tag")) {
                tag = intent.getStringExtra("tag");
            }
            if(!tag.equals("BTMUSIC")) {
            	finish();
            }
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bluetooth_music);
		//bindService(new Intent(Constant.BTPHONE_SERVICE), mServiceConnection , Context.BIND_AUTO_CREATE);
		registerReceiver(mReceiver , new IntentFilter(Constant.MEDIA_PLAY_STATUS));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(mServiceConnection);
		unregisterReceiver(mReceiver);
	}
	@Override
	public void onClick(View v) {
		
	}
	
	
	private Stub callback = new Stub() {
		
		@Override
		public void onVoiceDisconnected() throws RemoteException {
			
		}
		
		@Override
		public void onVoiceConnected() throws RemoteException {
			
		}
		
		@Override
		public void onVersionDate(String version) throws RemoteException {
			
		}
		
		@Override
		public void onTalking(String number) throws RemoteException {
			
		}
		
		@Override
		public void onSppStatus(int status) throws RemoteException {
			
		}
		
		@Override
		public void onSppDisconnect(int index) throws RemoteException {
			
		}
		
		@Override
		public void onSppData(int index, String data) throws RemoteException {
			
		}
		
		@Override
		public void onSppConnect(int index) throws RemoteException {
			
		}
		
		@Override
		public void onSimDone() throws RemoteException {
			
		}
		
		@Override
		public void onSimBook(String name, String number) throws RemoteException {
			
		}
		
		@Override
		public void onSignalBattery(String signal, String battery)
				throws RemoteException {
			
		}
		
		@Override
		public void onRingStop() throws RemoteException {
			
		}
		
		@Override
		public void onRingStart() throws RemoteException {
			
		}
		
		@Override
		public void onProfileEnbled(boolean[] enabled) throws RemoteException {
			
		}
		
		@Override
		public void onPhoneMessageText(String text) throws RemoteException {
			
		}
		
		@Override
		public void onPhoneMessage(String handle, boolean read, String time,
				String name, String num, String title) throws RemoteException {
			
		}
		
		@Override
		public void onPhoneBookDone() throws RemoteException {
			
		}
		
		@Override
		public void onPhoneBook(String name, String number) throws RemoteException {
			
			
		}
		
		@Override
		public void onPanStatus(int status) throws RemoteException {
			
			
		}
		
		@Override
		public void onPanDisconnect() throws RemoteException {
			
			
		}
		
		@Override
		public void onPanConnect() throws RemoteException {
			
			
		}
		
		@Override
		public void onOutGoingOrTalkingNumber(String number) throws RemoteException {
			
			
		}
		
		@Override
		public void onOppReceivedFile(String path) throws RemoteException {
			
			
		}
		
		@Override
		public void onOppPushSuccess() throws RemoteException {
			
			
		}
		
		@Override
		public void onOppPushFailed() throws RemoteException {
			
			
		}
		/**
		 * 音乐暂停状态
		 */
		@Override
		public void onMusicStopped() throws RemoteException {
			
			
		}
		/**
		 * 音乐播放转台
		 */
		@Override
		public void onMusicPlaying() throws RemoteException {
			handler.removeMessages(Constant.MSG_MUSIC_PLAYING);
			handler.sendEmptyMessage(Constant.MSG_MUSIC_PLAYING);
		}
		/**
		 * 音乐信息
		 */
		@Override
		public void onMusicInfo(String music_name, String artist_nameString,
				int duration, int pos, int total) throws RemoteException {
			handler.removeMessages(Constant.MSG_MUSIC_INFO);
			handler.sendEmptyMessage(Constant.MSG_MUSIC_INFO);
		}
		
		@Override
		public void onLocalAddress(String addr) throws RemoteException {
			
			
		}
		
		@Override
		public void onInitSucceed() throws RemoteException {
			
			
		}
		
		@Override
		public void onIncoming(String number) throws RemoteException {
			
			
		}
		
		@Override
		public void onInPairMode() throws RemoteException {
			
			
		}
		
		@Override
		public void onHidStatus(int status) throws RemoteException {
			
			
		}
		
		@Override
		public void onHidDisconnected() throws RemoteException {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onHidConnected() throws RemoteException {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onHfpStatus(int status) throws RemoteException {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onHfpRemote() throws RemoteException {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onHfpLocal() throws RemoteException {
			// TODO Auto-generated method stub
			
		}
		/**
		 * 蓝牙断开连接
		 */
		@Override
		public void onHfpDisconnected() throws RemoteException {
			handler.removeMessages(Constant.MSG_HFP_DISCONNECTED);
			handler.sendEmptyMessage(Constant.MSG_HFP_DISCONNECTED);			
		}
		/**
		 * 蓝牙连接
		 */
		@Override
		public void onHfpConnected() throws RemoteException {
			handler.removeMessages(Constant.MSG_HFP_CONNECTED);
			handler.sendEmptyMessage(Constant.MSG_HFP_CONNECTED);
			
		}
		
		@Override
		public void onHangUp() throws RemoteException {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onExitPairMode() throws RemoteException {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onDiscoveryDone() throws RemoteException {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onDiscovery(String name, String addr) throws RemoteException {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onCurrentPinCode(String code) throws RemoteException {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onCurrentName(String name) throws RemoteException {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onCurrentDeviceName(String name) throws RemoteException {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onCurrentAndPairList(int index, String name, String addr)
				throws RemoteException {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onCurrentAddr(String addr) throws RemoteException {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onConnecting() throws RemoteException {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onCalllogDone() throws RemoteException {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onCalllog(int type, String name, String number, String date)
				throws RemoteException {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onCallSucceed(String number) throws RemoteException {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onAvStatus(int status) throws RemoteException {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onAutoConnectAccept(boolean autoConnect, boolean autoAccept)
				throws RemoteException {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onA2dpDisconnected() throws RemoteException {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onA2dpConnected() throws RemoteException {
			// TODO Auto-generated method stub
			
		}
	};
}
