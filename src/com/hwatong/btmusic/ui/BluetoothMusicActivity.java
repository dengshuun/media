package com.hwatong.btmusic.ui;

import com.hwatong.btmusic.ICallback;
import com.hwatong.btmusic.IService;
import com.hwatong.btmusic.NowPlaying;
import com.hwatong.media.common.R;
import com.hwatong.media.common.Constant;
import com.hwatong.statusbarinfo.aidl.IStatusBarInfo;

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
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class BluetoothMusicActivity extends Activity implements OnClickListener {
    /**
     * 蓝牙音乐播放、暂停
     */
    private ImageView mPlaySatue;
    /**
     * 上一首歌曲
     */
    private ImageView mPrevious;
    /**
     * 下一首歌曲
     */
    private ImageView mNext;
    /**
     * 返回
     */
    private View mBackIcon;
    /**
     * 歌曲名称
     */
    private TextView mSingName;
    /**
     * 蓝牙音乐服务信息，以及回调
     */
    private IService mService;
    private NowPlaying mNowPlaying;

    /**
     * 状态栏信息
     */
    private IStatusBarInfo statusBarInfo;
    private ServiceConnection statusBarConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            statusBarInfo = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            statusBarInfo = IStatusBarInfo.Stub.asInterface(service);
            try {
                if (statusBarInfo != null) {
                    statusBarInfo.setCurrentPageName("bluetooth_music");
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case Constant.MSG_HFP_CONNECTED:
                    mPlaySatue.setEnabled(true);
                    mNext.setEnabled(true);
                    mPrevious.setEnabled(true);
                    break;
                case Constant.MSG_HFP_DISCONNECTED:
                    mPlaySatue.setEnabled(false);
                    mNext.setEnabled(false);
                    mPrevious.setEnabled(false);
                    break;
                case Constant.MSG_MUSIC_PLAYING:
                    mPlaySatue.setImageResource(R.drawable.btn_music_pause);
                    break;
                case Constant.MSG_MUSIC_STOP:
                    mPlaySatue.setImageResource(R.drawable.btn_music_play);
                    break;
                case Constant.MSG_MUSIC_INFO:
                    if (mService == null) {
                        return;
                    }
                    try {
                        mNowPlaying = mService.getNowPlaying();
                        if (mNowPlaying != null) {
                            boolean isPlaying = Boolean.parseBoolean(mNowPlaying.get("playbackStatus"));
                            if (isPlaying) {
                                handler.removeMessages(Constant.MSG_MUSIC_PLAYING);
                                handler.sendEmptyMessage(Constant.MSG_MUSIC_PLAYING);
                            } else {
                                handler.removeMessages(Constant.MSG_MUSIC_STOP);
                                handler.sendEmptyMessage(Constant.MSG_MUSIC_STOP);
                            }
                            mSingName.setText(mNowPlaying.get("title"));
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String tag = null;
            String action = intent.getAction();
            Log.d(Constant.TAG_BT_MUSIC, "--- 接收到广播, action: " + action);
            if (intent.hasExtra("tag")) {
                tag = intent.getStringExtra("tag");
            }
            if ((tag != null && !tag.equals("BTMUSIC")) || "com.hwatong.voice.CLOSE_BTMUSIC".equals(action)) {
                finish();
            }
        }
    };

    private ICallback.Stub mCallback = new ICallback.Stub() {

        @Override
        public void onDisconnected() throws RemoteException {
            handler.removeMessages(Constant.MSG_HFP_DISCONNECTED);
            handler.sendEmptyMessage(Constant.MSG_HFP_DISCONNECTED);
        }

        @Override
        public void onConnected() throws RemoteException {
            handler.removeMessages(Constant.MSG_HFP_CONNECTED);
            handler.sendEmptyMessage(Constant.MSG_HFP_CONNECTED);
        }

        @Override
        public void nowPlayingUpdate(NowPlaying nowPlaying) throws RemoteException {
            handler.removeMessages(Constant.MSG_MUSIC_INFO);
            handler.sendEmptyMessage(Constant.MSG_MUSIC_INFO);
        }
    };

    private ServiceConnection mSeviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            mService = com.hwatong.btmusic.IService.Stub.asInterface(service);

            try {
                Log.d("dengshun", "mService : " + mService + " mCallback: " + mCallback);
                mService.registerCallback(mCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            try {
                mNowPlaying = mService.getNowPlaying();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_music);

        startService(new Intent(Constant.BTPHONE_SERVICE));
        bindService(new Intent(Constant.BTPHONE_SERVICE), mSeviceConnection, Context.BIND_AUTO_CREATE);
        // 接收广播
        registerReceiver(mReceiver, new IntentFilter("com.hwatong.voice.CLOSE_BTMUSIC"));
        registerReceiver(mReceiver, new IntentFilter(Constant.MEDIA_PLAY_STATUS));
    }

    private void initUI() {
        mPlaySatue = (ImageView) findViewById(R.id.play);
        mPlaySatue.setOnClickListener(this);
        mPrevious = (ImageView) findViewById(R.id.previous);
        mPrevious.setOnClickListener(this);
        mNext = (ImageView) findViewById(R.id.next);
        mNext.setOnClickListener(this);
        mBackIcon = (View) findViewById(R.id.btn_back);
        mBackIcon.setOnClickListener(this);
        mSingName = (TextView) findViewById(R.id.name);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initUI();
        bindService(new Intent("com.remote.hwatong.statusinfoservice"), statusBarConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mSeviceConnection);
        unregisterReceiver(mReceiver);
        unbindService(statusBarConnection);
    }

    @Override
    public void onClick(View v) {
        if (v == mBackIcon) {
            finish();
        }
        if (mService == null) {
            return;
        }
        if (v == mPlaySatue) {
            if (mNowPlaying != null) {
                boolean isPlaying = Boolean.parseBoolean(mNowPlaying.get("playbackStatus"));
                try {
                    if (isPlaying) {
                        mService.pause();
                        handler.removeMessages(Constant.MSG_MUSIC_STOP);
                        handler.sendEmptyMessage(Constant.MSG_MUSIC_STOP);
                    } else {
                        mService.play();
                        handler.removeMessages(Constant.MSG_MUSIC_PLAYING);
                        handler.sendEmptyMessage(Constant.MSG_MUSIC_PLAYING);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } else if (v == mPrevious) {
            try {
                mService.previous();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if (v == mNext) {
            try {
                mService.next();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
