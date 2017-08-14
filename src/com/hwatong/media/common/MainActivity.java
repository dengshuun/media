package com.hwatong.media.common;

import com.hwatong.btmusic.ui.BluetoothMusicActivity;
import com.hwatong.ipod.ui.IPodMainActivity;
import com.hwatong.media.common.R;
import com.hwatong.statusbarinfo.aidl.IStatusBarInfo;
import com.hwatong.usbmusic.UsbMusicActivity;
import com.hwatong.usbpicture.UsbPictureActivity;
import com.hwatong.usbvideo.UsbVideoActivity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 主界面
 * 
 * @author Administrator
 * 
 */
public class MainActivity extends Activity implements OnClickListener {

	protected static final String TAG = "UsbMainActivity";

	private RelativeLayout rl_usb;
	private RelativeLayout rl_btmusic;
	private RelativeLayout rl_ipod;
	private ImageView rl_right;

	private TextView txt_usb;
	private TextView txt_btmusic;
	private TextView txt_ipod;
	
	private RelativeLayout btn_back;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//接收USB的插拔广播
		IntentFilter filter = new IntentFilter();
		filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
		filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		registerReceiver(mUsbReceiver, filter);

		initView();
	}

	private void initView(){
		rl_usb = (RelativeLayout) findViewById(R.id.usb_layout);
		rl_btmusic = (RelativeLayout) findViewById(R.id.btmusic_layout);
		rl_ipod = (RelativeLayout) findViewById(R.id.ipod_layout);
		rl_right = (ImageView) findViewById(R.id.right_layout);

		txt_usb = (TextView) findViewById(R.id.txt_usb);
		txt_btmusic = (TextView) findViewById(R.id.txt_btmusic);
		txt_ipod = (TextView) findViewById(R.id.txt_ipod);

		btn_back = (RelativeLayout) findViewById(R.id.btn_back);

		rl_usb.setOnClickListener(this);
		rl_btmusic.setOnClickListener(this);
		rl_ipod.setOnClickListener(this);
		btn_back.setOnClickListener(this);
	}
	
	private void changeToUsb() {
		txt_usb.setText(R.string.txt_music);
		txt_usb.setBackgroundResource(R.drawable.usb_music_selector);
		txt_btmusic.setText(R.string.txt_video);
		txt_btmusic.setBackgroundResource(R.drawable.usb_video_selector);
		txt_ipod.setText(R.string.txt_picture);
		txt_ipod.setBackgroundResource(R.drawable.usb_picture_selector);
		rl_right.setBackgroundResource(R.drawable.usb_right_bg);
	}
	
	private void changeToMedia() {
		txt_usb.setText(R.string.txt_usb);
		txt_usb.setBackgroundResource(R.drawable.bg_usb_selector);
		txt_btmusic.setText(R.string.txt_btmusic);
		txt_btmusic.setBackgroundResource(R.drawable.bg_btmusic_selector);
		txt_ipod.setText(R.string.txt_ipod);
		txt_ipod.setBackgroundResource(R.drawable.bg_ipod_selector);
		rl_right.setBackgroundResource(R.drawable.media_right_bg);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.usb_layout) {
			if (txt_usb.getText().equals("USB")) {
				changeToUsb();
			} else {
				Intent i = new Intent(MainActivity.this, UsbMusicActivity.class);
				startActivity(i);
			}
		} else if (v.getId() == R.id.btmusic_layout) {
			if (txt_btmusic.getText().equals("蓝牙音乐")) {
				Intent i = new Intent(MainActivity.this,
						BluetoothMusicActivity.class);
				startActivity(i);
			} else {
				Intent i = new Intent(MainActivity.this, UsbVideoActivity.class);
				startActivity(i);
			}
		} else if (v.getId() == R.id.ipod_layout) {
			if (txt_ipod.getText().equals("IPOD")) {
				Intent i = new Intent(MainActivity.this, IPodMainActivity.class);
				startActivity(i);
			} else {
				Intent i = new Intent(MainActivity.this,
						UsbPictureActivity.class);
				startActivity(i);
			}
		}else if (v.getId() == R.id.btn_back) {
			if (txt_usb.getText().equals("USB")) {
				Intent intent = new Intent();
				intent.setAction("com.hwatong.launcher.MAIN");
				intent.addCategory("android.intent.category.DEFAULT");
				if (intent != null) {
					try {
						startActivity(intent);
					} catch (Exception e) {
						Log.e(TAG, "startApp error: " + e);
					}
				}
			} else {
				changeToMedia();
			}
		}
	}
	
	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			UsbDevice usbDevice = (UsbDevice) intent
					.getParcelableExtra(UsbManager.EXTRA_DEVICE);
			String deviceName = usbDevice.getDeviceName();
			Log.d(TAG, "--- 接收到广播， action: " + action);
			if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
				rl_usb.setEnabled(true);
				txt_usb.setEnabled(true);
				Log.e(TAG, "USB device is Attached: " + deviceName);
			} else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
				if(!txt_usb.getText().equals("USB")){
					changeToMedia();
				}
				rl_usb.setEnabled(false);
				txt_usb.setEnabled(false);
				Log.e(TAG, "USB device is Detached: " + deviceName);
			}
		}
	};

	@Override
	protected void onResume() {
		if (Constant.DEBUG)
			Log.i(TAG, "onResume:");

		bindService(new Intent("com.remote.hwatong.statusinfoservice"), mStatusBarConnection, BIND_AUTO_CREATE);
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy");
		unregisterReceiver(mUsbReceiver);
		mStatusBarInfo = null;
		unbindService(mStatusBarConnection);
		super.onDestroy();
	}
	
	/**
	 * 状态栏服务
	 */
	private IStatusBarInfo mStatusBarInfo; //状态栏左上角信息
	private ServiceConnection mStatusBarConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mStatusBarInfo = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mStatusBarInfo = com.hwatong.statusbarinfo.aidl.IStatusBarInfo.Stub.asInterface(service);
			try {
				if (mStatusBarInfo != null) {
					mStatusBarInfo.setCurrentPageName("launcher");
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	};
	
}
