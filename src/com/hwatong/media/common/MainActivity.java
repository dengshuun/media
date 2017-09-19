package com.hwatong.media.common;

import com.hwatong.btmusic.NowPlaying;
import com.hwatong.btmusic.ui.BluetoothMusicActivity;
import com.hwatong.ipod.ui.IPodMainActivity;
import com.hwatong.media.ICallback;
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
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
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
 */
public class MainActivity extends Activity implements OnClickListener {

    protected static final String TAG = "UsbMainActivity";

    private com.hwatong.media.IService mMediaService;
    private com.hwatong.btmusic.IService mBTService;
    private com.hwatong.ipod.IService mIPodService;

    private RelativeLayout rl_usb;
    private RelativeLayout rl_btmusic;
    private RelativeLayout rl_ipod;
    private ImageView rl_right;

    private TextView txt_usb;
    private TextView txt_btmusic;
    private TextView txt_ipod;

    private RelativeLayout btn_back;

    private boolean isUsbAvailable;
    private boolean isBtMusciAvailable;
    private boolean isIPodAvailable;
    private boolean isUsbMusicAvailable;
    private boolean isUsbVideoAvailable;
    private boolean isUsbPictureAvailable;

    // 加载提示
    private CustomDialog mLoadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 接收USB的插拔广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbReceiver, filter);

        initView();
    }

    private void initView() {
        mLoadingDialog = new CustomDialog(this, null);
        rl_usb = (RelativeLayout) findViewById(R.id.usb_layout);
        rl_btmusic = (RelativeLayout) findViewById(R.id.btmusic_layout);
        rl_ipod = (RelativeLayout) findViewById(R.id.ipod_layout);
        rl_right = (ImageView) findViewById(R.id.right_layout);

        txt_usb = (TextView) findViewById(R.id.txt_usb);
        txt_btmusic = (TextView) findViewById(R.id.txt_btmusic);
        txt_ipod = (TextView) findViewById(R.id.txt_ipod);

        btn_back = (RelativeLayout) findViewById(R.id.btn_back);

        rl_usb.setOnClickListener(this);
        rl_usb.setEnabled(false);
        rl_btmusic.setOnClickListener(this);
        rl_btmusic.setEnabled(false);
        rl_ipod.setOnClickListener(this);
        rl_ipod.setEnabled(false);
        btn_back.setOnClickListener(this);
    }

    private static final int MSG_SET_PAGE_NAME = 0;
    private static final int MSG_CHANGE_TO_MEDIA = 1;
    private static final int MSG_CHANGE_TO_USB = 2;
    private static final int MSG_SET_USB_ENABLE = 3;
    private static final int MSG_SET_USB_UNABLE = 4;
    private static final int MSG_PRESCAN_CHANGED = 5;
    private static final int MSG_SET_IPOD_ENABLE = 6;
    private static final int MSG_SET_IPOD_UNABLE = 7;
    private static final int MSG_SET_BTMUSIC_ENABLE = 8;
    private static final int MSG_SET_BTMUSIC_UNABLE = 9;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SET_PAGE_NAME:
                    setPageName();
                    break;
                case MSG_CHANGE_TO_MEDIA:
                    changeToMedia();
                    break;
                case MSG_CHANGE_TO_USB:
                    changeToUsb();
                    break;
                case MSG_SET_USB_ENABLE:
                    setUsbEnable();
                    break;
                case MSG_SET_USB_UNABLE:
                    setUsbUnable();
                    break;
                case MSG_SET_IPOD_ENABLE:
                    setIPodEnable();
                    break;
                case MSG_SET_IPOD_UNABLE:
                    setIPodUnable();
                    break;
                case MSG_SET_BTMUSIC_ENABLE:
                    setBtMusicEnable();
                    break;
                case MSG_SET_BTMUSIC_UNABLE:
                    setBtMusicUnable();
                    break;
                case MSG_PRESCAN_CHANGED:
                    onPrescanChanged((String) msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    private ServiceConnection mIPodServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (mIPodService != null) {
                try {
                    mIPodService.unregisterCallback(mIPodCallback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            mIPodService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mIPodService = com.hwatong.ipod.IService.Stub.asInterface(service);
            try {
                mIPodService.registerCallback(mIPodCallback);
                if (mIPodService.isAttached()) {
                    isIPodAvailable = true;
                    isUsbAvailable = false;
                    mHandler.removeMessages(MSG_SET_USB_UNABLE);
                    mHandler.sendEmptyMessage(MSG_SET_USB_UNABLE);
                    mHandler.removeMessages(MSG_SET_IPOD_ENABLE);
                    mHandler.sendEmptyMessage(MSG_SET_IPOD_ENABLE);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    private ServiceConnection mBTSeviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBTService = com.hwatong.btmusic.IService.Stub.asInterface(service);
            try {
                Log.d(TAG, "mBTService : " + mBTService + " mBtCallBack: " + mBtCallBack);
                mBTService.registerCallback(mBtCallBack);
                if (mBTService.isBtMusicConnected()) {
                    isBtMusciAvailable = true;
                    mHandler.removeMessages(MSG_SET_BTMUSIC_ENABLE);
                    mHandler.sendEmptyMessage(MSG_SET_BTMUSIC_ENABLE);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (mBTService != null) {
                try {
                    mBTService.unregisterCallback(mBtCallBack);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            mBTService = null;
        }

    };

    private com.hwatong.ipod.ICallback.Stub mIPodCallback = new com.hwatong.ipod.ICallback.Stub() {

        @Override
        public void onNowPlayingReceived() throws RemoteException {

        }

        @Override
        public void onMediaPlaylistReceived() throws RemoteException {

        }

        @Override
        public void onMediaItemReceived() throws RemoteException {

        }

        @Override
        public void onDetached() throws RemoteException {
            isIPodAvailable = false;
            mHandler.removeMessages(MSG_SET_IPOD_UNABLE);
            mHandler.sendEmptyMessage(MSG_SET_IPOD_UNABLE);
        }

        @Override
        public void onAttached() throws RemoteException {
            isIPodAvailable = true;
            isUsbAvailable = false;
            mHandler.removeMessages(MSG_SET_USB_UNABLE);
            mHandler.sendEmptyMessage(MSG_SET_USB_UNABLE);
            mHandler.removeMessages(MSG_SET_IPOD_ENABLE);
            mHandler.sendEmptyMessage(MSG_SET_IPOD_ENABLE);
        }
    };

    private com.hwatong.btmusic.ICallback.Stub mBtCallBack = new com.hwatong.btmusic.ICallback.Stub() {

        @Override
        public void onDisconnected() throws RemoteException {
            isBtMusciAvailable = false;
            mHandler.removeMessages(MSG_SET_BTMUSIC_UNABLE);
            mHandler.sendEmptyMessage(MSG_SET_BTMUSIC_UNABLE);
        }

        @Override
        public void onConnected() throws RemoteException {
            isBtMusciAvailable = true;
            mHandler.removeMessages(MSG_SET_BTMUSIC_ENABLE);
            mHandler.sendEmptyMessage(MSG_SET_BTMUSIC_ENABLE);
        }

        @Override
        public void nowPlayingUpdate(NowPlaying arg0) throws RemoteException {

        }
    };

    private final ServiceConnection mMediaServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (mMediaService != null) {
                try {
                    mMediaService.unregisterCallback(mMediaCallback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            mMediaService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMediaService = com.hwatong.media.IService.Stub.asInterface(service);
            try {
                mMediaService.registerCallback(mMediaCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            try {
                if (mMediaService.getUsbState(Constant.ROOT_DIR_PATH).equals("mounted")) {
                    Log.d(TAG, "mMediaService.getUsbState: " + mMediaService.getUsbState(Constant.ROOT_DIR_PATH));
                    isUsbAvailable = true;
                    mHandler.removeMessages(MSG_SET_USB_ENABLE);
                    mHandler.sendEmptyMessage(MSG_SET_USB_ENABLE);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    private final com.hwatong.media.ICallback.Stub mMediaCallback = new ICallback.Stub() {

        @Override
        public void onVolumeStateChanged(String arg0) throws RemoteException {

        }

        @Override
        public void onVideoListChanged() throws RemoteException {

        }

        @Override
        public void onUsbStateChanged(String arg0, String arg1, String arg2) throws RemoteException {

        }

        @Override
        public void onUsbScanChanged(String arg0, String arg1, String arg2) throws RemoteException {

        }

        @Override
        public void onUsbPrescanChanged(String path, String oldState, String newState) throws RemoteException {
            mHandler.removeMessages(MSG_PRESCAN_CHANGED);
            Message m = Message.obtain(mHandler, MSG_PRESCAN_CHANGED, path);
            mHandler.sendMessage(m);
        }

        @Override
        public void onPictureListChanged() throws RemoteException {

        }

        @Override
        public void onMusicListChanged() throws RemoteException {

        }
    };

    private void onPrescanChanged(String path) {
        if (mMediaService != null) {
            try {
                String state = mMediaService.getUsbPrescanState(path);

                if (Constant.DEBUG)
                    Log.i(TAG, "onPrescanChanged " + path + " ; " + state);

                if ("prescan".equals(state)) {
                    if (txt_usb.getText().equals(this.getString(R.string.txt_music))) {
                        mLoadingDialog.show();
                    }
                } else if ("stop".equals(state)) {
                    mLoadingDialog.dismiss();
                    isUsbMusicAvailable = mMediaService.getMusicList().size() != 0;
                    isUsbPictureAvailable = mMediaService.getPictureList().size() != 0;
                    isUsbVideoAvailable = mMediaService.getVideoList().size() != 0;
                    if (txt_usb.getText().equals(this.getString(R.string.txt_music))) {
                        if (isUsbMusicAvailable) {
                            setUsbEnable();
                        }
                        if (isUsbPictureAvailable) {
                            setIPodEnable();
                        }
                        if (isUsbVideoAvailable) {
                            setBtMusicEnable();
                        }
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void changeToUsb() {
        setUsbUnable();
        setBtMusicUnable();
        setIPodUnable();
        try {
            if (mMediaService != null) {
                String state = mMediaService.getUsbPrescanState(Constant.ROOT_DIR_PATH);
                if ("prescan".equals(state)) {
                    mLoadingDialog.show();
                }
                isUsbMusicAvailable = mMediaService.getMusicList().size() != 0;
                isUsbPictureAvailable = mMediaService.getPictureList().size() != 0;
                isUsbVideoAvailable = mMediaService.getVideoList().size() != 0;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        txt_usb.setText(R.string.txt_music);
        txt_usb.setBackgroundResource(R.drawable.usb_music_selector);
        txt_btmusic.setText(R.string.txt_video);
        txt_btmusic.setBackgroundResource(R.drawable.usb_video_selector);
        txt_ipod.setText(R.string.txt_picture);
        txt_ipod.setBackgroundResource(R.drawable.usb_picture_selector);
        rl_right.setBackgroundResource(R.drawable.usb_right_bg);
        if (isUsbMusicAvailable) {
            setUsbEnable();
        }
        if (isUsbVideoAvailable) {
            setBtMusicEnable();
        }
        if (isUsbPictureAvailable) {
            setIPodEnable();
        }
        if (!isUsbMusicAvailable) {
            setUsbUnable();
        }
        if (!isUsbVideoAvailable) {
            setBtMusicUnable();
        }
        if (!isUsbPictureAvailable) {
            setIPodUnable();
        }
    }

    private void setIPodUnable() {
        rl_ipod.setEnabled(false);
        txt_ipod.setEnabled(false);
    }

    private void setBtMusicUnable() {
        rl_btmusic.setEnabled(false);
        txt_btmusic.setEnabled(false);
    }

    private void setUsbUnable() {
        rl_usb.setEnabled(false);
        txt_usb.setEnabled(false);
    }

    private void setIPodEnable() {
        rl_ipod.setEnabled(true);
        txt_ipod.setEnabled(true);
    }

    private void setBtMusicEnable() {
        rl_btmusic.setEnabled(true);
        txt_btmusic.setEnabled(true);
    }

    private void setUsbEnable() {
        rl_usb.setEnabled(true);
        txt_usb.setEnabled(true);
    }

    private void changeToMedia() {
        setUsbUnable();
        setBtMusicUnable();
        setIPodUnable();
        txt_usb.setText(R.string.txt_usb);
        txt_usb.setBackgroundResource(R.drawable.bg_usb_selector);
        txt_btmusic.setText(R.string.txt_btmusic);
        txt_btmusic.setBackgroundResource(R.drawable.bg_btmusic_selector);
        txt_ipod.setText(R.string.txt_ipod);
        txt_ipod.setBackgroundResource(R.drawable.bg_ipod_selector);
        rl_right.setBackgroundResource(R.drawable.media_right_bg);
        if (isUsbAvailable) {
            setUsbEnable();
        }
        if (isBtMusciAvailable) {
            setBtMusicEnable();
        }
        if (isIPodAvailable) {
            setIPodEnable();
        }
        if (!isUsbAvailable) {
            setUsbUnable();
        }
        if (!isBtMusciAvailable) {
            setBtMusicUnable();
        }
        if (!isIPodAvailable) {
            setIPodUnable();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.usb_layout) {
            if (txt_usb.getText().equals(this.getString(R.string.txt_usb))) {
                mHandler.removeMessages(MSG_CHANGE_TO_USB);
                mHandler.sendEmptyMessage(MSG_CHANGE_TO_USB);
                mHandler.removeMessages(MSG_SET_PAGE_NAME);
                mHandler.sendEmptyMessage(MSG_SET_PAGE_NAME);
            } else {
                Intent i = new Intent(MainActivity.this, UsbMusicActivity.class);
                startActivity(i);
            }
        } else if (v.getId() == R.id.btmusic_layout) {
            if (txt_btmusic.getText().equals(this.getString(R.string.txt_btmusic))) {
                Intent i = new Intent(MainActivity.this, BluetoothMusicActivity.class);
                startActivity(i);
            } else {
                Intent i = new Intent(MainActivity.this, UsbVideoActivity.class);
                startActivity(i);
            }
        } else if (v.getId() == R.id.ipod_layout) {
            if (txt_ipod.getText().equals(this.getString(R.string.txt_ipod))) {
                Intent i = new Intent(MainActivity.this, IPodMainActivity.class);
                startActivity(i);
            } else {
                Intent i = new Intent(MainActivity.this, UsbPictureActivity.class);
                startActivity(i);
            }
        } else if (v.getId() == R.id.btn_back) {
            if (txt_usb.getText().equals(this.getString(R.string.txt_usb))) {
                Intent intent = new Intent("com.hwatong.launcher.MAIN");
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                this.moveTaskToBack(true);
            } else {
                mHandler.removeMessages(MSG_CHANGE_TO_MEDIA);
                mHandler.sendEmptyMessage(MSG_CHANGE_TO_MEDIA);
                mHandler.removeMessages(MSG_SET_PAGE_NAME);
                mHandler.sendEmptyMessage(MSG_SET_PAGE_NAME);
            }
        }
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            UsbDevice usbDevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            String deviceName = usbDevice.getDeviceName();
            Log.d(TAG, "--- 接收到广播， action: " + action);
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                mHandler.removeMessages(MSG_SET_USB_ENABLE);
                mHandler.sendEmptyMessage(MSG_SET_USB_ENABLE);
                isUsbAvailable = true;
                Log.e(TAG, "USB device is Attached: " + deviceName);
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                if (!txt_usb.getText().equals("USB")) {
                    mHandler.removeMessages(MSG_CHANGE_TO_MEDIA);
                    mHandler.sendEmptyMessage(MSG_CHANGE_TO_MEDIA);
                }
                mHandler.removeMessages(MSG_SET_USB_UNABLE);
                mHandler.sendEmptyMessage(MSG_SET_USB_UNABLE);
                isUsbAvailable = false;
                isUsbMusicAvailable = false;
                isUsbPictureAvailable = false;
                isUsbVideoAvailable = false;
                Log.e(TAG, "USB device is Detached: " + deviceName);
            }
        }
    };

    @Override
    protected void onResume() {
        if (Constant.DEBUG)
            Log.i(TAG, "onResume:");
        bindService(new Intent("com.remote.hwatong.statusinfoservice"), mStatusBarConnection, BIND_AUTO_CREATE);
        bindService(new Intent("com.hwatong.media.MediaScannerService"), mMediaServiceConnection, BIND_AUTO_CREATE);
        bindService(new Intent(Constant.BTPHONE_SERVICE), mBTSeviceConnection, Context.BIND_AUTO_CREATE);
        bindService(new Intent(Constant.IPOD_SERVICE), mIPodServiceConnection, BIND_AUTO_CREATE);
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (Constant.DEBUG)
            Log.i(TAG, "onPause:");
        mStatusBarInfo = null;
        unbindService(mStatusBarConnection);
        unbindService(mIPodServiceConnection);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        unregisterReceiver(mUsbReceiver);
        unbindService(mMediaServiceConnection);
        unbindService(mBTSeviceConnection);
        super.onDestroy();
    }

    /**
     * 状态栏服务
     */
    private IStatusBarInfo mStatusBarInfo; // 状态栏左上角信息
    private ServiceConnection mStatusBarConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mStatusBarInfo = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mStatusBarInfo = com.hwatong.statusbarinfo.aidl.IStatusBarInfo.Stub.asInterface(service);
            mHandler.removeMessages(MSG_SET_PAGE_NAME);
            mHandler.sendEmptyMessage(MSG_SET_PAGE_NAME);
        }

    };

    // 设置状态栏显示的名字
    private void setPageName() {
        try {
            if (mStatusBarInfo != null) {
                if (txt_usb.getText().equals(this.getString(R.string.txt_usb))) {
                    mStatusBarInfo.setCurrentPageName("media");
                } else {
                    mStatusBarInfo.setCurrentPageName("usb");
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
