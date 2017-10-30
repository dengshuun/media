package com.hwatong.usbvideo;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import com.hwatong.media.VideoEntry;
import com.hwatong.media.common.Constant;
import com.hwatong.media.common.FolderFragment;
import com.hwatong.media.common.FolderFragment.Type;
import com.hwatong.media.common.R;
import com.hwatong.media.common.Utils;
import com.hwatong.media.common.VideoUtils;
import com.hwatong.media.common.VideoView;
import com.hwatong.statusbarinfo.aidl.IStatusBarInfo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class UsbVideoActivity extends Activity implements SurfaceHolder.Callback, OnClickListener, MediaPlayer.OnPreparedListener,
		MediaPlayer.OnVideoSizeChangedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

	private com.hwatong.media.IService mService;
	private boolean isFullScreen;
	private int mVideoProgress;
	private int mVideoPosition;
	private String mVideoFilePath; // 记录地址

	private Random mRandom = new Random(); // 随机模式产生随机数

	private VideoView mSurfaceView;
	private int mSurfaceWidth;
	private int mSurfaceHeight;
	private int mVideoWidth;
	private int mVideoHeight;

	private SurfaceHolder mSurfaceHolder;
	private AudioManager mAudioManager;
	private MediaPlayer mMediaPlayer;// 媒体对象

	private SeekBar mVideoSeekBar;
	private TextView mVideoName;
	private TextView mVideoTextCurTime;
	private TextView mVideoTextAllTime;
	private RelativeLayout mVideoControlView;

	private static final int STATE_IDLE = 0;
	private static final int STATE_PREPARED = 1;
	private static final int STATE_ERROR = 2;
	private static final int STATE_PLAYING = 3;
	private static final int STATE_PAUSED = 4;

	private int mState = STATE_IDLE;
	private boolean mRequestPlay;

	private static final int MSG_VOLUME_STATE_CHANGED = 0;
	private static final int MSG_PRESCAN_CHANGED = 1;
	private static final int MSG_VIDEO_LIST_CHANGED = 2;

	private VideoAdapter mVideoAdapter = null;
	private RelativeLayout btnBack;
	private Button btnLoopMode;
	private ListView mVideoList;
	private ImageView ivVideoPlay;
	private ImageView ivVideoPre;
	private ImageView ivVideoNext;
	private ImageView ivFolderIcon;
	private TextView tvFolder;
	private Button btnVideoFullScreen;
	private TextView mNoVideoFile;

	private LinearLayout mVideoBottomBar;
	private LinearLayout mFolder;
	private FragmentTransaction mTransaction;
	private FragmentManager mFragmentManager;
	private FolderFragment mFolderFragment;

	private static final int ALL = 0;
	private static final int ONE = 1;
	private static final int RANDOM = 2;

	private int loopMode = ONE;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_usb_video);

		// 接收广播
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.hwatong.voice.CLOSE_VIDEO");
		registerReceiver(mReceiver, filter);

		initFolderFragment();
		initView();
		mSurfaceView.getHolder().addCallback(this);// Surface回调函数：
		mSurfaceView.setKeepScreenOn(true);

		mAudioManager.requestAudioFocus(mAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
		timeUpdateHandler.sendEmptyMessageDelayed(MSG_TIME_TOOL, 3000);
	}

	private void initFolderFragment() {

		mFragmentManager = getFragmentManager();
		mFolderFragment = new FolderFragment();
		mFolderFragment.setType(Type.VEDIO);
	}

	private void initView() {

		mRequestPlay = true;
		isFullScreen = false;
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mVideoFilePath = VideoUtils.getPlayingVideoPath(this);

		mVideoAdapter = new VideoAdapter(this, mFolderFragment.getPath());
		mVideoList = (ListView) findViewById(R.id.list_video);
		mFolder = (LinearLayout) findViewById(R.id.btn_folder);
		btnBack = (RelativeLayout) findViewById(R.id.btn_back);
		btnLoopMode = (Button) findViewById(R.id.btn_loop_mode);
		ivVideoPlay = (ImageView) findViewById(R.id.video_play);
		ivVideoPre = (ImageView) findViewById(R.id.video_previous);
		ivVideoNext = (ImageView) findViewById(R.id.video_next);
		ivFolderIcon = (ImageView) findViewById(R.id.img_video_folder_icon);
		tvFolder = (TextView) findViewById(R.id.txt_video_folder);
		btnVideoFullScreen = (Button) findViewById(R.id.btn_vedio_full_screen);
		mVideoSeekBar = (SeekBar) findViewById(R.id.video_seekbar);
		mVideoTextCurTime = (TextView) findViewById(R.id.video_curtime);
		mVideoTextAllTime = (TextView) findViewById(R.id.video_alltime);
		mVideoName = (TextView) findViewById(R.id.video_name);
		mSurfaceView = (VideoView) findViewById(R.id.video_surfaceview);
		mVideoBottomBar = (LinearLayout) findViewById(R.id.usb_video_bottom_bar);
		mVideoControlView = (RelativeLayout) findViewById(R.id.video_control_view);
		mNoVideoFile = (TextView) findViewById(R.id.text_no_video);

		mVideoList.setAdapter(mVideoAdapter);
		mVideoList.setSelector(R.drawable.media_list_item_selector);
		mSurfaceView.setOnClickListener(this);
		btnLoopMode.setOnClickListener(this);
		ivVideoPlay.setOnClickListener(this);
		ivVideoPre.setOnClickListener(this);
		ivVideoNext.setOnClickListener(this);
		btnVideoFullScreen.setOnClickListener(this);
		mFolder.setOnClickListener(this);
		btnBack.setOnClickListener(this);
		mVideoSeekBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		mVideoList.setOnItemClickListener(mVideoOnItemClickListener);
	}

	/**
	 * 音乐列表点击事件
	 */
	private final OnItemClickListener mVideoOnItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			VideoEntry e = (VideoEntry) mVideoAdapter.getItem(position);
			if (!mVideoFilePath.equals(e.mFilePath))
				videoEnterPlayer(e.mFilePath, position, 0);
		}
	};

	/**
	 * 设置滑动进度条
	 */
	private final SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			if (mMediaPlayer != null) {
				mMediaPlayer.seekTo(arg0.getProgress());
				if (mMediaPlayer.isPlaying())
					timeUpdateHandler.sendMessage(timeUpdateHandler.obtainMessage(1));
			}
			timeUpdateHandler.sendEmptyMessageDelayed(MSG_TIME_TOOL, 3000);
		}

		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			timeUpdateHandler.removeMessages(1);
			timeUpdateHandler.removeMessages(MSG_TIME_TOOL);
		}

		@Override
		public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {

		}
	};

	/**
	 * 声音焦点监听
	 */
	OnAudioFocusChangeListener mAudioFocusChangeListener = new OnAudioFocusChangeListener() {
		@Override
		public void onAudioFocusChange(int focusChange) {
			switch (focusChange) {
			case AudioManager.AUDIOFOCUS_GAIN:// 指示申请得到的AudioFocus不知道会持续多久，一般是长期占有；
			case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:// 指示要申请的AudioFocus是暂时性的，会很快用完释放的；
				resume();
				break;
			case AudioManager.AUDIOFOCUS_LOSS:// 失去了Audio Focus，并将会持续很长的时间
				if (mMediaPlayer.isPlaying()) {
					pause();
				}
				break;
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:// 暂时失去AudioFocus，并会很快再次获得。必须停止Audio的播放，但是因为可能会很快再次获得AudioFocus，这里可以不释放Media资源；
				if (mMediaPlayer.isPlaying()) {
					pause();
				}
				break;
			default:
				break;
			}
		}
	};

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d(Constant.TAG_USB_VIDEO, "--- 接收到广播， action: " + action);
			if ("com.hwatong.voice.CLOSE_VIDEO".equals(action)) {
				finish();
			}
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		if (Constant.DEBUG)
			Log.i(Constant.TAG_USB_PICTURE, "onResume ");

		bindService(new Intent("com.hwatong.media.MediaScannerService"), mMediaServiceConnection, BIND_AUTO_CREATE);
		bindService(new Intent("com.remote.hwatong.statusinfoservice"), mStatusBarConnection, BIND_AUTO_CREATE);
	}

	@Override
	protected void onPause() {
		if (Constant.DEBUG)
			Log.i(Constant.TAG_USB_PICTURE, "onPause");

		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			pause();
		}

		if (mService != null) {
			try {
				mService.unregisterCallback(mCallback);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		mService = null;
		unbindService(mMediaServiceConnection);
		mStatusBarInfo = null;
		unbindService(mStatusBarConnection);
		mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		if (Constant.DEBUG)
			Log.i(Constant.TAG_USB_PICTURE, "onDestroy");
		unregisterReceiver(mReceiver);
		super.onDestroy();
	}

	// 处理文件夹点击的事件
	public void onFolderItemClick(File file) {
		Log.e("sss", "event---->" + file.getName());

		if (file.isDirectory()) {
			if (ivFolderIcon.getVisibility() == View.GONE) {
				ivFolderIcon.setImageResource(R.drawable.folder_icon_back);
				ivFolderIcon.setVisibility(View.VISIBLE);
				tvFolder.setText(R.string.upper_level);
				tvFolder.setVisibility(View.VISIBLE);
			}
			mVideoAdapter.notifyData(file.toString());
			if (mVideoAdapter.getCount() == 0) {
				mVideoList.setVisibility(View.GONE);
				mNoVideoFile.setVisibility(View.VISIBLE);
			} else {
				mVideoList.setVisibility(View.VISIBLE);
				mNoVideoFile.setVisibility(View.GONE);
			}
		} else {
			int position = mVideoAdapter.getPositionByPath(file.toString());
			videoEnterPlayer(file.toString(), position, 0);
		}
	}

	private final ServiceConnection mMediaServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			if (Constant.DEBUG)
				Log.i(Constant.TAG_USB_VIDEO, "onServiceConnected");

			mService = com.hwatong.media.IService.Stub.asInterface(service);

			try {
				mService.registerCallback(mCallback);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			mMediaHandler.removeMessages(MSG_VIDEO_LIST_CHANGED);
			mMediaHandler.sendEmptyMessage(MSG_VIDEO_LIST_CHANGED);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			if (Constant.DEBUG)
				Log.i(Constant.TAG_USB_VIDEO, "onServiceDisconnected");
			mService = null;
		}
	};

	private final com.hwatong.media.ICallback mCallback = new com.hwatong.media.ICallback.Stub() {

		@Override
		public void onVolumeStateChanged(String path) {
			if (Constant.DEBUG)
				Log.i(Constant.TAG_USB_VIDEO, "onVolumeStateChanged " + path);

			mMediaHandler.removeMessages(MSG_VOLUME_STATE_CHANGED);
			Message m = Message.obtain(mMediaHandler, MSG_VOLUME_STATE_CHANGED, path);
			mMediaHandler.sendMessage(m);
		}

		@Override
		public void onUsbStateChanged(String path, String oldState, String newState) {
			if (Constant.DEBUG)
				Log.i(Constant.TAG_USB_VIDEO, "onUsbStateChanged " + path + " " + oldState + " -> " + newState);
		}

		@Override
		public void onUsbScanChanged(String path, String oldState, String newState) {
			if (Constant.DEBUG)
				Log.i(Constant.TAG_USB_VIDEO, "onUsbScanChanged " + path + " " + oldState + " -> " + newState);
		}

		public void onUsbPrescanChanged(String path, String oldState, String newState) {
			if (Constant.DEBUG)
				Log.i(Constant.TAG_USB_VIDEO, "onUsbPrescanChanged " + path + " " + oldState + " -> " + newState);

			mMediaHandler.removeMessages(MSG_PRESCAN_CHANGED);
			Message m = Message.obtain(mMediaHandler, MSG_PRESCAN_CHANGED, path);
			mMediaHandler.sendMessage(m);
		}

		@Override
		public void onMusicListChanged() {
			if (Constant.DEBUG)
				Log.i(Constant.TAG_USB_VIDEO, "onMusicListChanged");
		}

		@Override
		public void onVideoListChanged() {
			if (Constant.DEBUG)
				Log.i(Constant.TAG_USB_VIDEO, "onVideoListChanged");

			mMediaHandler.removeMessages(MSG_VIDEO_LIST_CHANGED);
			mMediaHandler.sendEmptyMessage(MSG_VIDEO_LIST_CHANGED);
		}

		@Override
		public void onPictureListChanged() {
			if (Constant.DEBUG)
				Log.i(Constant.TAG_USB_VIDEO, "onPictureListChanged");
		}

	};

	/**
	 * 处理mediaService的回调
	 */
	@SuppressLint("HandlerLeak")
	private final Handler mMediaHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case MSG_VOLUME_STATE_CHANGED:
				onVolumeStateChanged((String) msg.obj);
				break;

			case MSG_PRESCAN_CHANGED:
				onPrescanChanged((String) msg.obj);
				break;

			case MSG_VIDEO_LIST_CHANGED:
				onVideoListChanged();
				break;
			}
		}
	};

	private void onVolumeStateChanged(String path) {
		if (mService != null) {
			try {
				boolean state = mService.getVolumeState(path);

				if (Constant.DEBUG)
					Log.i(Constant.TAG_USB_VIDEO, "onVolumeStateChanged " + path + ", " + state);

				if (!state) {
					finish();
				}

			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	private void onPrescanChanged(String path) {
		if (mService != null) {
			try {
				String state = mService.getUsbPrescanState(path);

				if (Constant.DEBUG)
					Log.i(Constant.TAG_USB_VIDEO, "onPrescanChanged " + path + ", " + state);

				if ("stop".equals(state)) {
					finish();
				}

			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	private void onVideoListChanged() {
		if (Constant.DEBUG)
			Log.i(Constant.TAG_USB_VIDEO, "onVideoListChanged");

		mVideoAdapter.notifyData(mFolderFragment.getPath());

		if (mVideoAdapter.getCount() > 0) {
			mVideoList.setVisibility(View.VISIBLE);
			mNoVideoFile.setVisibility(View.GONE);
		} else {
			mVideoList.setVisibility(View.GONE);
			mNoVideoFile.setVisibility(View.VISIBLE);
		}

		default_option();
	}

	private void default_option() {
		if (mVideoAdapter.getCount() > 0) {
			String path = VideoUtils.getPlayingVideoPath(UsbVideoActivity.this);
			int position = mVideoAdapter.getPositionByPath(path);
			if (position != -1) {
				int progress = VideoUtils.getPlayingVideoProgress(UsbVideoActivity.this);
				videoEnterPlayer(path, position, progress);
			} else {
				VideoEntry e = (VideoEntry) mVideoAdapter.getItem(0);
				videoEnterPlayer(e.mFilePath, 0, 0);
			}

			loopMode = VideoUtils.getLoopMode(UsbVideoActivity.this);
			if (loopMode == ONE) {
				btnLoopMode.setBackgroundResource(R.drawable.single_cycle_selector);
			} else if (loopMode == ALL) {
				btnLoopMode.setBackgroundResource(R.drawable.folder_cycle_selector);
			} else if (loopMode == RANDOM) {
				btnLoopMode.setBackgroundResource(R.drawable.folder_random_selector);
			}

		}
	}

	private void videoEnterPlayer(String path, int position, int progress) {
		mVideoAdapter.setSelectedIndex(position);

		mVideoFilePath = path;
		mVideoPosition = position;
		mVideoProgress = progress;

		openVideo();

	}

	/**
	 * SurfaceCallback回调接口，为视频播放做准备工作
	 */
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		if (Constant.DEBUG)
			Log.i(Constant.TAG_USB_VIDEO, "surfaceChanged " + width + "x" + height);

		mSurfaceWidth = width;
		mSurfaceHeight = height;

		if (mRequestPlay && mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight) {
			resume();
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {// 判断surface是否创建成功
		if (Constant.DEBUG)
			Log.i(Constant.TAG_USB_VIDEO, "surfaceCreated");

		mSurfaceHolder = holder;
		openVideo();

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (Constant.DEBUG)
			Log.d(Constant.TAG_USB_VIDEO, "surfaceDestroyed");

		mSurfaceHolder = null;
		release();
	}

	private boolean openVideo() {
		if (Constant.DEBUG)
			Log.d(Constant.TAG_USB_VIDEO, "openVideo " + mVideoFilePath);

		if (mVideoFilePath == null || mSurfaceHolder == null)
			return false;

		mAudioManager.requestAudioFocus(mAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

		release();

		try {
			mMediaPlayer = new MediaPlayer();

			mMediaPlayer.setOnPreparedListener(this);
			mMediaPlayer.setOnVideoSizeChangedListener(this);
			mMediaPlayer.setOnCompletionListener(this);
			mMediaPlayer.setOnErrorListener(this);

			mMediaPlayer.setDataSource(mVideoFilePath);
			mMediaPlayer.setDisplay(mSurfaceHolder);
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mMediaPlayer.setScreenOnWhilePlaying(true);
			mMediaPlayer.prepare(); // 加载视频资源，prepareAsync()在加载时会有某些文件无法正常播放的bug。
		} catch (IOException ex) {
			Log.w(Constant.TAG_USB_VIDEO, "Unable to open content: " + mVideoFilePath, ex);
			onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
			return false;
		} catch (IllegalArgumentException ex) {
			Log.w(Constant.TAG_USB_VIDEO, "Unable to open content: " + mVideoFilePath, ex);
			onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
			return false;
		}

		mVideoAdapter.setSelectedIndex(mVideoPosition);

		VideoUtils.savePlayingVideoPath(UsbVideoActivity.this, mVideoFilePath);

		return true;
	}

	public void resume() {
		if (Constant.DEBUG)
			Log.d(Constant.TAG_USB_VIDEO, "resume");

		mAudioManager.requestAudioFocus(mAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

		if (mMediaPlayer != null) {
			if (mState == STATE_PREPARED || mState == STATE_PAUSED) {

				mMediaPlayer.start();
				mState = STATE_PLAYING;

				ivVideoPlay.setImageResource(R.drawable.btn_music_pause);
				timeUpdateHandler.removeMessages(1);
				timeUpdateHandler.sendEmptyMessageDelayed(1, 800);
			}
		}
	}

	public void pause() {
		if (Constant.DEBUG)
			Log.d(Constant.TAG_USB_VIDEO, "pause");

		if (mMediaPlayer != null) {
			if (mState == STATE_PLAYING) {
				if (mMediaPlayer.isPlaying()) {
					mMediaPlayer.pause();
				}
				mState = STATE_PAUSED;

				ivVideoPlay.setImageResource(R.drawable.btn_music_play);
				timeUpdateHandler.removeMessages(1);

				// mMediaPlayer.setVolume(0.0f, 0.0f);
			}
		}
	}

	private void release() {
		if (Constant.DEBUG)
			Log.d(Constant.TAG_USB_VIDEO, "release");

		if (mMediaPlayer != null) {
			if (mState == STATE_PLAYING || mState == STATE_PAUSED) {
				mMediaPlayer.stop();
			}
			mMediaPlayer.reset();
			mMediaPlayer.release();
			mMediaPlayer = null;
			mState = STATE_IDLE;
		}
	}

	/**
	 * 更新播放进度条
	 */
	private static final int MSG_TIME_TOOL = 0x1111;// 隐藏工具栏
	@SuppressLint("HandlerLeak")
	private Handler timeUpdateHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (mMediaPlayer == null)
				return;
			int elapsed = mMediaPlayer.getCurrentPosition();
			int duration = mMediaPlayer.getDuration();
			mVideoSeekBar.setProgress(elapsed);
			mVideoTextCurTime.setText(Utils.formatetime(elapsed));
			mVideoTextAllTime.setText(Utils.formatetime(duration));
			// 实时更新数据库媒体播放的事件
			timeUpdateHandler.sendEmptyMessageDelayed(1, 1000);
			VideoUtils.savePlayingVideoProgress(UsbVideoActivity.this, elapsed);

			if (msg.what == MSG_TIME_TOOL) {
				if (mVideoControlView.getVisibility() == View.VISIBLE) {
					setToolVisible(View.GONE);
				} else {
					setToolVisible(View.VISIBLE);
				}
			}
		}
	};

	private void playUpMedia() {
		if (mVideoAdapter.getCount() == 0)
			return;

		timeUpdateHandler.removeMessages(1);

		mVideoPosition = getPrePosition();

		mVideoFilePath = ((VideoEntry) mVideoAdapter.getItem(mVideoPosition)).mFilePath;

		mVideoProgress = 0;

		openVideo();

	}

	private void playNextMedia(boolean force) {
		if (mVideoAdapter.getCount() == 0)
			return;

		timeUpdateHandler.removeMessages(1);

		if (force) {
			mVideoPosition = getNextPosition();
		}
		mVideoFilePath = ((VideoEntry) mVideoAdapter.getItem(mVideoPosition)).mFilePath;

		mVideoProgress = 0;

		openVideo();

	}

	private int getPrePosition() {
		int prePos = -1;

		int size = 0;
		synchronized (mVideoList) {
			size = mVideoList.getCount();
		}
		if (size == 0)
			return -1;

		switch (loopMode) {
		case RANDOM:
			prePos = mRandom.nextInt(size);
			while (prePos == mVideoPosition && size > 1) {
				prePos = mRandom.nextInt(size);
			}
			break;
		case ALL:
		case ONE:
			prePos = mVideoPosition - 1;
			if (prePos < 0)
				prePos = size - 1;
			break;
		}

		return prePos;
	}

	private int getNextPosition() {
		int nextPos = -1;

		int size = 0;
		synchronized (mVideoList) {
			size = mVideoList.getCount();
		}

		if (size == 0)
			return -1;

		switch (loopMode) {
		case ALL:
		case ONE:
			nextPos = mVideoPosition + 1;
			if (nextPos >= size)
				nextPos = 0;
			break;
		case RANDOM:
			nextPos = mRandom.nextInt(size);
			while (nextPos == mVideoPosition && size > 1) {
				nextPos = mRandom.nextInt(size);
			}
			break;
		}

		return nextPos;
	}

	@Override
	public void onClick(View v) {

		timeUpdateHandler.removeMessages(MSG_TIME_TOOL);
		timeUpdateHandler.sendEmptyMessageDelayed(MSG_TIME_TOOL, 3000);

		switch (v.getId()) {
		case R.id.btn_loop_mode:
			changeLoopMode();
			break;

		case R.id.video_previous:
			playUpMedia();
			break;

		case R.id.video_next:
			playNextMedia(true);
			break;

		case R.id.video_play:
			if (mState == STATE_PLAYING) {
				pause();
			} else if (mState == STATE_PREPARED || mState == STATE_PAUSED) {
				resume();
			}
			break;

		case R.id.btn_folder:
			openFolder();
			break;

		case R.id.btn_back:
			if (mFolderFragment.isVisible()) {
				ivFolderIcon.setVisibility(View.VISIBLE);
				tvFolder.setVisibility(View.VISIBLE);
				ivFolderIcon.setImageResource(R.drawable.img_folder_icon);
				tvFolder.setText(R.string.folder);
				mTransaction = mFragmentManager.beginTransaction();
				mTransaction.hide(mFolderFragment).commit();
				timeUpdateHandler.removeMessages(MSG_TIME_TOOL);
			} else {
				onBackPressed();
			}
			break;

		case R.id.btn_vedio_full_screen:
			if (isFullScreen) {
				quitFullScreen();
				btnVideoFullScreen.setBackgroundResource(R.drawable.btn_vedio_full_screen);
			} else {
				enterFullScreen();
				btnVideoFullScreen.setBackgroundResource(R.drawable.btn_half_screen);
			}
			break;

		case R.id.video_surfaceview:
			if (mVideoControlView.getVisibility() == View.VISIBLE) {
				setToolVisible(View.GONE);
			} else {
				setToolVisible(View.VISIBLE);
			}
			break;

		default:
			break;
		}
	}

	private void changeLoopMode() {
		if (mService != null) {
			if (loopMode == ONE) {
				btnLoopMode.setBackgroundResource(R.drawable.folder_cycle_selector);
				loopMode = ALL;
			} else if (loopMode == ALL) {
				btnLoopMode.setBackgroundResource(R.drawable.folder_random_selector);
				loopMode = RANDOM;
			} else if (loopMode == RANDOM) {
				btnLoopMode.setBackgroundResource(R.drawable.single_cycle_selector);
				loopMode = ONE;
			}
			VideoUtils.saveLoopMode(UsbVideoActivity.this, loopMode);
		}
	}

	/**
	 * 离开全屏模式
	 */
	private void quitFullScreen() {
		isFullScreen = false;
		mVideoList.setVisibility(View.VISIBLE);
		mVideoBottomBar.setVisibility(View.VISIBLE);
		final WindowManager.LayoutParams attrs = getWindow().getAttributes();
		attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setAttributes(attrs);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
	}

	/**
	 * 进入全屏模式
	 */
	private void enterFullScreen() {
		isFullScreen = true;
		mVideoList.setVisibility(View.GONE);
		mVideoBottomBar.setVisibility(View.GONE);
		timeUpdateHandler.removeMessages(MSG_TIME_TOOL);
		timeUpdateHandler.sendEmptyMessageDelayed(MSG_TIME_TOOL, 3000);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	/**
	 * 文件夾按鈕功能
	 */
	private void openFolder() {
		if (!mFolderFragment.isAdded()) {
			mTransaction = mFragmentManager.beginTransaction();
			mTransaction.add(R.id.usb_video_folder_layout, mFolderFragment).commit();
		}
		if (mFolderFragment.isHidden()) {
			mTransaction = mFragmentManager.beginTransaction();
			mTransaction.show(mFolderFragment).commit();
		} else if (!mFolderFragment.getPath().equals(Constant.ROOT_DIR_PATH)) {
			mFolderFragment.setPath(Utils.getPreDirectory(mFolderFragment.getPath()));
			mVideoAdapter.notifyData(mFolderFragment.getPath());
			if (mVideoAdapter.getCount() == 0) {
				mVideoList.setVisibility(View.GONE);
				mNoVideoFile.setVisibility(View.VISIBLE);
			} else {
				mVideoList.setVisibility(View.VISIBLE);
				mNoVideoFile.setVisibility(View.GONE);
			}
		}
		if (mFolderFragment.getPath().equals(Constant.ROOT_DIR_PATH)) {
			ivFolderIcon.setVisibility(View.GONE);
			tvFolder.setVisibility(View.GONE);
		} else {
			ivFolderIcon.setImageResource(R.drawable.folder_icon_back);
			tvFolder.setText(R.string.upper_level);
		}
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		mState = STATE_ERROR;
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		if (loopMode == ONE) {
			playNextMedia(false);
		} else {
			playNextMedia(true);
		}
	}

	@Override
	public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
		mVideoWidth = mp.getVideoWidth();
		mVideoHeight = mp.getVideoHeight();
		if (mVideoWidth != 0 && mVideoHeight != 0) {
			mSurfaceView.setVideoSize(mVideoWidth, mVideoHeight);
			if (mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight) {
				if (mRequestPlay) {
					resume();
				}
			}
		}
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		if (Constant.DEBUG)
			Log.i(Constant.TAG_USB_VIDEO, "onPrepared " + "mRequestPlay " + mRequestPlay + ", mVideo_Progress " + mVideoProgress);

		mState = STATE_PREPARED;

		mVideoWidth = mp.getVideoWidth();
		mVideoHeight = mp.getVideoHeight();
		Log.i(Constant.TAG_USB_VIDEO, "video size: " + mVideoWidth + "x" + mVideoHeight);

		int duration = mMediaPlayer.getDuration();

		if (mVideoProgress < 0 || mVideoProgress >= duration) {
			mVideoProgress = 0;
		}

		mMediaPlayer.seekTo(mVideoProgress);

		mVideoSeekBar.setProgress(mVideoProgress);
		mVideoSeekBar.setMax(duration);
		mVideoName.setText(Utils.getNameFromFilename(Utils.getExtFromFilename(mVideoFilePath)));

		mMediaPlayer.setVolume(1.0f, 1.0f);

		if (mVideoWidth != 0 && mVideoHeight != 0) {
			mSurfaceView.setVideoSize(mVideoWidth, mVideoHeight);
			if (mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight) {
				if (mRequestPlay) {
					resume();
				}
			}
		} else {
			if (mRequestPlay) {
				resume();
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
			if (mState == STATE_PLAYING) {
				pause();
			} else if (mState == STATE_PREPARED || mState == STATE_PAUSED) {
				resume();
			}
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
			resume();
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
			pause();
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {
			playUpMedia();
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_MEDIA_NEXT) {
			playNextMedia(true);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void setToolVisible(int visible) {

		if (visible == View.VISIBLE) {

			mVideoControlView.setVisibility(View.VISIBLE);
			mVideoName.setVisibility(View.VISIBLE);
			mVideoControlView.startAnimation(AnimationUtils.loadAnimation(UsbVideoActivity.this, R.anim.popup_show));
			mVideoName.startAnimation(AnimationUtils.loadAnimation(UsbVideoActivity.this, R.anim.popup_show_up));
			timeUpdateHandler.removeMessages(MSG_TIME_TOOL);
			timeUpdateHandler.sendEmptyMessageDelayed(MSG_TIME_TOOL, 3000);

		} else {
			timeUpdateHandler.removeMessages(MSG_TIME_TOOL);
			mVideoControlView.startAnimation(AnimationUtils.loadAnimation(UsbVideoActivity.this, R.anim.popup_hide));
			mVideoName.startAnimation(AnimationUtils.loadAnimation(UsbVideoActivity.this, R.anim.popup_hide_up));
			mVideoControlView.setVisibility(View.GONE);
			mVideoName.setVisibility(View.GONE);
		}

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
			try {
				if (mStatusBarInfo != null) {
					mStatusBarInfo.setCurrentPageName("usb_video");
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	};

}
