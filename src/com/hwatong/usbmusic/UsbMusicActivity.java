package com.hwatong.usbmusic;

import java.io.File;
import java.util.Random;

import com.hwatong.media.common.Constant;
import com.hwatong.media.common.FolderFragment;
import com.hwatong.media.common.FolderFragment.Type;
import com.hwatong.media.common.LoadingDialog;
import com.hwatong.media.common.MainActivity;
import com.hwatong.media.common.R;
import com.hwatong.media.common.Utils;
import com.hwatong.media.MusicEntry;
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
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class UsbMusicActivity extends Activity implements OnClickListener {

	private MusicAdapter mMusicAdapter;

	private com.hwatong.music.IService mMusicService;
	private com.hwatong.music.NowPlaying mNowPlaying;

	private Random mRandom = new Random(); // 随机模式产生随机数
	private IStatusBarInfo mStatusBarInfo; // 状态栏左上角信息

	private RelativeLayout btnBack;
	private ImageButton btnLoopMode;
	private ListView mMusicList;
	private ImageView ivMusicPlay;
	private ImageView ivMusicPre;
	private ImageView ivMusicNext;
	private ImageView ivFolderIcon;
	private TextView tvFolder;
	private TextView tvSong;
	private TextView tvSinger;
	private TextView tvAlbum;
	private SeekBar mMusicSeekBar;
	private TextView mMusicSongCurtime;
	private TextView mMusicSongOvertime;

	private LinearLayout mFolder;
	private FragmentTransaction mTransaction;
	private FragmentManager mFragmentManager;
	private FolderFragment mFolderFragment;

	private boolean mMusicUserSeekSong;

	// 加载提示
	private LoadingDialog mLoadingDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_usb_music);

		// 接收USB的插拔广播
		IntentFilter filter = new IntentFilter();
		filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		registerReceiver(mUsbReceiver, filter);

		initFolderFragment();
		initView();
		bindService(new Intent("com.hwatong.music.MUSIC_PALYBACK_SERVICE"), mMusicServiceConnection, BIND_AUTO_CREATE);
	}

	private final int[] PLAY_MODE_RES = { R.drawable.folder_random_selector, R.drawable.folder_cycle_selector, R.drawable.single_cycle_selector };

	private void initFolderFragment() {

		mFragmentManager = getFragmentManager();
		mFolderFragment = new FolderFragment();
		mFolderFragment.setType(Type.MUSIC);
	}

	private void initView() {

		mMusicAdapter = new MusicAdapter(this, mFolderFragment.getPath());
		mMusicList = (ListView) findViewById(R.id.list_music);
		mFolder = (LinearLayout) findViewById(R.id.btn_folder);
		btnBack = (RelativeLayout) findViewById(R.id.btn_back);
		btnLoopMode = (ImageButton) findViewById(R.id.btn_loop_mode);
		ivMusicPlay = (ImageView) findViewById(R.id.music_play);
		ivMusicPre = (ImageView) findViewById(R.id.music_previous);
		ivMusicNext = (ImageView) findViewById(R.id.music_next);
		ivFolderIcon = (ImageView) findViewById(R.id.img_folder_icon);
		tvFolder = (TextView) findViewById(R.id.txt_folder);
		tvSong = (TextView) findViewById(R.id.txt_song);
		tvSinger = (TextView) findViewById(R.id.txt_singer);
		tvAlbum = (TextView) findViewById(R.id.txt_album);
		mMusicSeekBar = (SeekBar) findViewById(R.id.music_seekbar);
		mMusicSongCurtime = (TextView) findViewById(R.id.music_curtime);
		mMusicSongOvertime = (TextView) findViewById(R.id.music_overtime);

		mLoadingDialog = new LoadingDialog(this, -1);
		mMusicSeekBar.setFocusable(false);
		mMusicList.setAdapter(mMusicAdapter);
		mMusicList.setSelector(R.drawable.media_list_item_selector);
		btnLoopMode.setOnClickListener(this);
		ivMusicPlay.setOnClickListener(this);
		ivMusicPre.setOnClickListener(this);
		ivMusicNext.setOnClickListener(this);
		mFolder.setOnClickListener(this);
		btnBack.setOnClickListener(this);
//		mMusicSeekBar.setOnSeekBarChangeListener(mSongSeekBarListener);
		mMusicList.setOnItemClickListener(mMusicOnItemClickListener);
	}

	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d(Constant.TAG_USB_MUSIC, "--- 接收到广播， action: " + action);
			if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
				finish();
				Log.e(Constant.TAG_USB_MUSIC, "USB device is Detached:");
			}
		}
	};

	private final ServiceConnection mMusicServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			if (Constant.DEBUG)
				Log.i(Constant.TAG_USB_MUSIC, "Music onServiceConnected");

			mMusicService = com.hwatong.music.IService.Stub.asInterface(service);

			try {
				mMusicService.registerCallback(mMusicListener);
			} catch (RemoteException e) {
				e.printStackTrace();
			}

			onMusicListChanged();
			onNowPlayingChanged(true);
			if (mMusicAdapter != null && mMusicAdapter.getCount() == 0) {
				mMusicList.setBackgroundResource(R.drawable.media_right_bg);
			}
			mMusicHandler.sendMessageDelayed(mMusicHandler.obtainMessage(2, 1, 0), 300);
			onMediaStatusChanged();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			if (Constant.DEBUG)
				Log.i(Constant.TAG_USB_MUSIC, "Music onServiceDisconnected");
			mMusicService = null;
		}
	};

	private final com.hwatong.music.ICallback mMusicListener = new com.hwatong.music.ICallback.Stub() {

		@Override
		public void onMediaStatusChanged() {
			if (Constant.DEBUG)
				Log.i(Constant.TAG_USB_MUSIC, "Music onMediaStatusChanged");
			mMusicHandler.removeMessages(0);
			mMusicHandler.sendEmptyMessage(0);
		}

		@Override
		public void onMusicListChanged() {
			if (Constant.DEBUG)
				Log.i(Constant.TAG_USB_MUSIC, "Music onMusicListChanged");
			mMusicHandler.removeMessages(1);
			mMusicHandler.sendEmptyMessageDelayed(1, 100);
		}

		@Override
		public void onNowPlayingChanged() {
			if (Constant.DEBUG)
				Log.i(Constant.TAG_USB_MUSIC, "Music onNowPlayingChanged");
			mMusicHandler.removeMessages(2);
			mMusicHandler.sendMessageDelayed(mMusicHandler.obtainMessage(2, 0, 0), 100);
		}

	};

	@SuppressLint("HandlerLeak")
	private final Handler mMusicHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case 0:
				onMediaStatusChanged();
				break;

			case 1:
				onMusicListChanged();
				break;

			case 2:
				onNowPlayingChanged(msg.arg1 == 1);
				break;
			}
		}
	};

	private void onNowPlayingChanged(boolean full) {
		if (mMusicService == null)
			return;

		try {
			mNowPlaying = mMusicService.getNowPlaying(full);
		} catch (RemoteException e) {
			e.printStackTrace();
			mNowPlaying = null;
		}

		if (mNowPlaying == null)
			return;

		if (Constant.DEBUG)
			Log.i(Constant.TAG_USB_MUSIC, "onNowPlayingChanged | " + mNowPlaying.mTitle + ", progress " + mNowPlaying.mPlaybackElapsedTimeInMilliseconds
					+ ", duration " + mNowPlaying.mPlaybackDurationInMilliseconds);

		if (!mMusicUserSeekSong) {
			mMusicSeekBar.setMax(mNowPlaying.mPlaybackDurationInMilliseconds);
			mMusicSeekBar.setProgress(mNowPlaying.mPlaybackElapsedTimeInMilliseconds);

			mMusicSongCurtime.setText(Utils.formatetime(mNowPlaying.mPlaybackElapsedTimeInMilliseconds));
			mMusicSongOvertime.setText(Utils.formatetime(mNowPlaying.mPlaybackDurationInMilliseconds));
		}

		updatePlayBtn(mNowPlaying.mPlaybackStatus);
		btnLoopMode.setImageDrawable(getResources().getDrawable(PLAY_MODE_RES[mNowPlaying.mPlaybackRepeatMode]));

		if (mNowPlaying.mTitle == null || !mNowPlaying.mTitle.equals(tvSong.getText())) {
			tvSong.setText(mNowPlaying.mTitle);

			if (mMusicAdapter != null) {
				mMusicAdapter.setmNowPlaying(mNowPlaying);
				mMusicAdapter.notifyNowPlayingReceived();
			}
		}
		tvSinger.setText(mNowPlaying.mArtist);
		tvAlbum.setText(mNowPlaying.mAlbum);
	}

	private boolean isPause = true;

	private void updatePlayBtn(boolean mStatus) {
		if (mStatus && isPause) {
			ivMusicPlay.setImageResource(R.drawable.btn_music_pause);
			isPause = false;
		} else if (!mStatus && !isPause) {
			ivMusicPlay.setImageResource(R.drawable.btn_music_play);
			isPause = true;
		}
	}

	private void onMediaStatusChanged() {
		if (mMusicService == null)
			return;

		try {
			int state = mMusicService.getMediaState();

			if ((state & 0x8000) != 0) {
				showTip();
			} else {
				hideTip();
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void showTip() {
		mLoadingDialog.show();
	}

	private void hideTip() {
		try {
			int state = mMusicService.getMediaState();

			if ((state & 0xff) > 0) {
				if (mMusicAdapter.getCount() == 0) {

				} else {
					mLoadingDialog.dismiss();
				}

			} else {
				finish();
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void onMusicListChanged() {
		mMusicAdapter.notifyData(mFolderFragment.getPath());

		if (mMusicAdapter.getCount() > 0) {
			mMusicList.setBackgroundResource(R.color.solid_black);
			if (mMusicService != null) {
				try {
					mMusicService.play();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		} else {
			mMusicList.setBackgroundResource(R.drawable.media_right_bg);
		}
	}

	// 音乐列表选项单击
	private final OnItemClickListener mMusicOnItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

			if (Constant.DEBUG)
				Log.i(Constant.TAG_USB_MUSIC, "mMusicOnItemClickListener " + position);

			if (position >= 0 && position < mMusicAdapter.getCount()) {
				final MusicEntry e = (MusicEntry) mMusicAdapter.getItem(position);
				musicEnterPlayer(e.mFilePath);
				Log.e("sss", "event---->" + e.mFilePath);
			}
		}

	};

	/**
	 * 音乐播放
	 */
	private void musicEnterPlayer(String path) {
		// Log.i(Constant.TAG_USB_MUSIC, "musicEnterPlayer!!!!!!");

		int pos = mMusicAdapter.getSongListPosition(path);
		mMusicAdapter.setSelectedIndex(pos);
		if (Constant.DEBUG)
			Log.i(Constant.TAG_USB_MUSIC, "musicEnterPlayer: pos " + pos + " path : " + path);
		if (pos == -1)
			return;

		if (mMusicService != null) {
			Log.i(Constant.TAG_USB_MUSIC, "musicEnterPlayer!!!!!!");
			try {
				mMusicService.open(path);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	// 处理文件夹点击的事件
	public void onFolderItemClick(File file) {
		Log.e("sss", "event---->" + file.getName());

		if (file.isDirectory()) {
			if (ivFolderIcon.getVisibility() == View.GONE) {
				ivFolderIcon.setImageResource(R.drawable.folder_icon_back);
				ivFolderIcon.setVisibility(View.VISIBLE);
				tvFolder.setVisibility(View.VISIBLE);
			}
			mMusicAdapter.notifyData(file.toString());
			if (mMusicAdapter.getCount() == 0) {
				mMusicList.setBackgroundResource(R.drawable.media_right_bg);
			} else {
				mMusicList.setBackgroundResource(R.color.solid_black);
			}
		} else {
			musicEnterPlayer(file.toString());
		}
	}

	@Override
	protected void onResume() {
		if (Constant.DEBUG)
			Log.i(Constant.TAG_USB_MUSIC, "onResume:");

		sendBroadcast(new Intent("com.hwatong.media.START").putExtra("tag", "USB"));
		bindService(new Intent("com.hwatong.music.MUSIC_PALYBACK_SERVICE"), mMusicServiceConnection, BIND_AUTO_CREATE);
		bindService(new Intent("com.remote.hwatong.statusinfoservice"), mStatusBarConnection, BIND_AUTO_CREATE);
		super.onResume();
	}

	@Override
	protected void onPause() {

		if (Constant.DEBUG)
			Log.i(Constant.TAG_USB_MUSIC, "onPause");
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		if (Constant.DEBUG)
			Log.i(Constant.TAG_USB_MUSIC, "onDestroy");
		if (mMusicService != null) {
			try {
				mMusicService.unregisterCallback(mMusicListener);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		mMusicService = null;
		mStatusBarInfo = null;
		unbindService(mStatusBarConnection);
		unbindService(mMusicServiceConnection);
		mMusicHandler.removeCallbacksAndMessages(null);
		unregisterReceiver(mUsbReceiver);

		super.onDestroy();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		if (Constant.DEBUG)
			Log.i(Constant.TAG_USB_MUSIC, "onRestart");
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (Constant.DEBUG)
			Log.i(Constant.TAG_USB_MUSIC, "onStart");
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (Constant.DEBUG)
			Log.i(Constant.TAG_USB_MUSIC, "onNewIntent: intent " + intent);
	}

	@Override
	protected void onStop() {
		if (Constant.DEBUG)
			Log.i(Constant.TAG_USB_MUSIC, "onStop");
		super.onStop();
	}

	@Override
	public void onBackPressed() {
		if (Constant.DEBUG)
			Log.i(Constant.TAG_USB_MUSIC, "onBackPressed");
		super.onBackPressed();
	}

	/**
	 * 音乐进度条监听
	 */
//	private OnSeekBarChangeListener mSongSeekBarListener = new OnSeekBarChangeListener() {
//
//		@Override
//		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//			if (fromUser) {
//				mMusicSongCurtime.setText(Utils.formatetime(progress));
//			}
//		}
//
//		@Override
//		public void onStartTrackingTouch(SeekBar seekBar) {
//			mMusicUserSeekSong = true;
//		}
//
//		@Override
//		public void onStopTrackingTouch(SeekBar seekBar) {
//			mMusicUserSeekSong = false;
//			if (mMusicService != null) {
//				try {
//					mMusicService.setNowPlayingInformation(seekBar.getProgress());
//					Log.i(Constant.TAG_USB_MUSIC, "mSongSeekBarListener = " + Utils.formatetime(seekBar.getProgress()));
//				} catch (RemoteException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//
//	};

	private int getPrePosition() {
		int prePos = -1;

		int mode = mNowPlaying.mPlaybackRepeatMode;
		int mCurPosition = mMusicAdapter.getSongListPosition(mNowPlaying.mId);
		int size = 0;
		synchronized (mMusicList) {
			size = mMusicList.getCount();
		}
		if (size == 0)
			return -1;

		switch (mode) {
		case com.hwatong.music.NowPlaying.PlaybackRepeatMode_Shuffle:
			prePos = mRandom.nextInt(size);
			while (prePos == mCurPosition && size > 1) {
				prePos = mRandom.nextInt(size);
			}
			break;
		case com.hwatong.music.NowPlaying.PlaybackRepeatMode_All:
		case com.hwatong.music.NowPlaying.PlaybackRepeatMode_One:
			prePos = mCurPosition - 1;
			if (prePos < 0)
				prePos = size - 1;
			break;
		}

		return prePos;
	}

	private int getNextPosition() {
		int nextPos = -1;

		int mode = mNowPlaying.mPlaybackRepeatMode;
		int mCurPosition = mMusicAdapter.getSongListPosition(mNowPlaying.mId);
		int size = 0;
		synchronized (mMusicList) {
			size = mMusicList.getCount();
		}

		if (size == 0)
			return -1;

		switch (mode) {
		case com.hwatong.music.NowPlaying.PlaybackRepeatMode_All:
		case com.hwatong.music.NowPlaying.PlaybackRepeatMode_One:
			nextPos = mCurPosition + 1;
			if (nextPos >= size)
				nextPos = 0;
			break;
		case com.hwatong.music.NowPlaying.PlaybackRepeatMode_Shuffle:
			nextPos = mRandom.nextInt(size);
			while (nextPos == mCurPosition && size > 1) {
				nextPos = mRandom.nextInt(size);
			}
			break;
		}

		return nextPos;
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {

		case R.id.music_previous:
			if (mMusicService != null) {
				previousSong();
			}
			break;

		case R.id.music_play:
			if (mMusicService != null) {
				try {
					mMusicService.playPause();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			break;

		case R.id.music_next:
			if (mMusicService != null) {
				nextSong();
			}
			break;

		case R.id.btn_loop_mode:
			if (mMusicService != null) {
				try {
					mMusicService.repeat();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			} else {
				Log.d(Constant.TAG_USB_MUSIC, "loop mode");
			}
			break;

		case R.id.btn_folder:
			openFolder();
			break;

		case R.id.btn_back:
			if (mFolderFragment.isVisible()) {
				if (ivFolderIcon.getVisibility() == View.GONE) {
					ivFolderIcon.setVisibility(View.VISIBLE);
					tvFolder.setVisibility(View.VISIBLE);
				}
				ivFolderIcon.setImageResource(R.drawable.folder_icon_normal);
				mTransaction = mFragmentManager.beginTransaction();
				mTransaction.hide(mFolderFragment).commit();
			} else {
				Intent intent = new Intent(this, MainActivity.class);
				startActivity(intent);
			}
			break;
		default:
			break;
		}
	}

	private void nextSong() {
		final MusicEntry entry = (MusicEntry) mMusicAdapter.getItem(getNextPosition());
		musicEnterPlayer(entry.mFilePath);
	}

	private void previousSong() {
		final MusicEntry entry = (MusicEntry) mMusicAdapter.getItem(getPrePosition());
		musicEnterPlayer(entry.mFilePath);
	}

	// 文件夹按钮的功能
	private void openFolder() {
		if (!mFolderFragment.isAdded()) {
			mTransaction = mFragmentManager.beginTransaction();
			mTransaction.add(R.id.usb_music_folder_layout, mFolderFragment).commit();
		} else if (mFolderFragment.isHidden()) {
			mTransaction = mFragmentManager.beginTransaction();
			mTransaction.show(mFolderFragment).commit();
		} else if (!mFolderFragment.getPath().equals(Constant.ROOT_DIR_PATH)) {
			mFolderFragment.setPath(Utils.getPreDirectory(mFolderFragment.getPath()));
			mMusicAdapter.notifyData(mFolderFragment.getPath());
			if (mMusicAdapter.getCount() == 0) {
				mMusicList.setBackgroundResource(R.drawable.media_right_bg);
			} else {
				mMusicList.setBackgroundResource(R.color.solid_black);
			}
		}
		if (mFolderFragment.getPath().equals(Constant.ROOT_DIR_PATH)) {
			ivFolderIcon.setVisibility(View.GONE);
			tvFolder.setVisibility(View.GONE);
		} else {
			ivFolderIcon.setImageResource(R.drawable.folder_icon_back);
		}
	}

	/**
	 * 状态栏服务
	 */
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
