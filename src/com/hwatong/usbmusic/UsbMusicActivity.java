package com.hwatong.usbmusic;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

import com.hwatong.media.common.Constant;
import com.hwatong.media.common.FolderFragment;
import com.hwatong.media.common.FolderFragment.Type;
import com.hwatong.media.common.R;
import com.hwatong.media.common.Utils;
import com.hwatong.music.MusicEntry;
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
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class UsbMusicActivity extends Activity implements OnClickListener {

	private MusicAdapter mMusicAdapter;

	private com.hwatong.music.IService mMusicService;
	private com.hwatong.music.NowPlaying mNowPlaying;

	// private Random mRandom = new Random(); // 随机模式产生随机数
	private IStatusBarInfo mStatusBarInfo; // 状态栏左上角信息

	private RelativeLayout btnBack;
	private ImageButton mLoopMode;
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
	private TextView mNoMusicFile;

	private LinearLayout mFolder;
	private FragmentTransaction mTransaction;
	private FragmentManager mFragmentManager;
	private FolderFragment mFolderFragment;

	private boolean mMusicUserSeekSong;

	private final int[] PLAY_MODE_RES = { R.drawable.folder_random_selector, R.drawable.folder_cycle_selector, R.drawable.single_cycle_selector };

	// 加载提示
	// private LoadingDialog mLoadingDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_usb_music);

		// 接收广播
		IntentFilter filter = new IntentFilter();
		filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		filter.addAction("com.hwatong.voice.PLAY_MODE");
		filter.addAction("com.hwatong.voice.CLOSE_MUSIC");
		filter.addAction("com.hwatong.voice.PLAY_MUSIC");
		registerReceiver(mReceiver, filter);

		initFolderFragment();
		initView();
		bindService(new Intent("com.hwatong.music.MUSIC_PALYBACK_SERVICE"), mMusicServiceConnection, BIND_AUTO_CREATE);
	}

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
		mLoopMode = (ImageButton) findViewById(R.id.btn_loop_mode);
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
		mNoMusicFile = (TextView) findViewById(R.id.text_no_music);

		// mLoadingDialog = new LoadingDialog(this, -1);
		mMusicSeekBar.setFocusable(false);
		mMusicList.setAdapter(mMusicAdapter);
		mMusicList.setSelector(R.drawable.media_list_item_selector);
		mLoopMode.setOnClickListener(this);
		ivMusicPlay.setOnClickListener(this);
		ivMusicPre.setOnClickListener(this);
		ivMusicNext.setOnClickListener(this);
		mFolder.setOnClickListener(this);
		btnBack.setOnClickListener(this);
		mMusicSeekBar.setOnSeekBarChangeListener(mSongSeekBarListener);
		mMusicList.setOnItemClickListener(mMusicOnItemClickListener);
	}

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d(Constant.TAG_USB_MUSIC, "--- 接收到广播， action: " + action);
			if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action) || "com.hwatong.voice.CLOSE_MUSIC".equals(action)) {
				finish();
				Log.e(Constant.TAG_USB_MUSIC, "USB device is Detached:");
			}
			if ("com.hwatong.voice.PLAY_MODE".equals(action)) {
				String mode = null;
				if (intent.hasExtra("mode")) {
					mode = intent.getStringExtra("mode");
				}
				if ("loop".equals(mode)) {
					if (mMusicService != null) {
						try {
							mMusicService.setNowPlayingInformation(1);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
				} else if ("single_loop".equals(mode)) {
					if (mMusicService != null) {
						try {
							mMusicService.setNowPlayingInformation(2);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
				} else if ("random".equals(mode)) {
					if (mMusicService != null) {
						try {
							mMusicService.setNowPlayingInformation(0);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
				}
			}
			if ("com.hwatong.voice.PLAY_MUSIC".equals(action)) {
				String song = null;
				String artist = null;
				String path = null;
				if (intent.hasExtra("song")) {
					song = intent.getStringExtra("song");
				}
				if (intent.hasExtra("artist")) {
					artist = intent.getStringExtra("artist");
				}
				if (song != null) {
					path = getSongListPosition(song);
				} else if (artist != null) {
					path = getSongListPosition(artist);
				}
				if (path != null) {
					musicEnterPlayer(path);
				}
			}
		}
	};

	private String getSongListPosition(String song) {
		if (song == null || song.isEmpty())
			return null;

		List<MusicEntry> musicList = new ArrayList<MusicEntry>();
		try {
			if (mMusicService != null) {
				musicList = mMusicService.getMusicList();
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		synchronized (musicList) {
			int size = musicList.size();
			for (int i = 0; i < size; i++) {
				MusicEntry s = musicList.get(i);
				if (s.mFilePath.contains(song))
					return s.mFilePath;
			}
		}

		return null;
	}

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

			mMusicHandler.sendEmptyMessage(MSG_MEDIA_STATUS);
			mMusicHandler.sendEmptyMessageDelayed(MSG_MUSIC_LIST, 500);
			mMusicHandler.sendMessageDelayed(mMusicHandler.obtainMessage(MSG_NOW_PLAYING, 1, 0), 300);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			if (Constant.DEBUG)
				Log.i(Constant.TAG_USB_MUSIC, "Music onServiceDisconnected");
			mMusicService = null;
		}
	};

	private final com.hwatong.music.ICallback.Stub mMusicListener = new com.hwatong.music.ICallback.Stub() {

		@Override
		public void onMediaStatusChanged() {
			if (Constant.DEBUG)
				Log.i(Constant.TAG_USB_MUSIC, "Music onMediaStatusChanged");
			mMusicHandler.removeMessages(MSG_MEDIA_STATUS);
			mMusicHandler.sendEmptyMessage(MSG_MEDIA_STATUS);
		}

		@Override
		public void onMusicListChanged() {
			if (Constant.DEBUG)
				Log.i(Constant.TAG_USB_MUSIC, "Music onMusicListChanged");
			mMusicHandler.removeMessages(MSG_MUSIC_LIST);
			mMusicHandler.sendEmptyMessageDelayed(MSG_MUSIC_LIST, 500);
		}

		@Override
		public void onNowPlayingChanged() {
			if (Constant.DEBUG)
				Log.i(Constant.TAG_USB_MUSIC, "Music onNowPlayingChanged");
			mMusicHandler.removeMessages(MSG_NOW_PLAYING);
			mMusicHandler.sendMessageDelayed(mMusicHandler.obtainMessage(MSG_NOW_PLAYING, 0, 0), 100);
		}

		@Override
		public void onPlayListChanged() throws RemoteException {
			Log.i(Constant.TAG_USB_MUSIC, "Music onPlayListChanged");
			mMusicHandler.removeMessages(MSG_PLAY_LIST);
			mMusicHandler.sendEmptyMessageDelayed(MSG_PLAY_LIST, 500);
		}

	};

	private static final int MSG_MEDIA_STATUS = 0;
	private static final int MSG_MUSIC_LIST = 1;
	private static final int MSG_NOW_PLAYING = 2;
	private static final int MSG_PLAY_LIST = 3;
	@SuppressLint("HandlerLeak")
	private final Handler mMusicHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case MSG_MEDIA_STATUS:
				onMediaStatusChanged();
				break;

			case MSG_MUSIC_LIST:
				onMusicListChanged();
				break;

			case MSG_NOW_PLAYING:
				onNowPlayingChanged(msg.arg1 == 1);
				break;

			case MSG_PLAY_LIST:
				onPlayListChanged();
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
		mLoopMode.setImageDrawable(getResources().getDrawable(PLAY_MODE_RES[mNowPlaying.mPlaybackRepeatMode]));

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

	private void onPlayListChanged() {
		try {
			mMusicAdapter.setmMusicDataList(mMusicService.getPlayList());
			mMusicAdapter.notifyDataSetChanged();
			mMusicAdapter.notifyNowPlayingReceived();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
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
			Log.v(Constant.TAG_USB_MUSIC, "mMusicService.getMediaState(): " + state);
			if ((state & 0x8000) != 0) {
				// mLoadingDialog.show();
			} else {
				// mLoadingDialog.dismiss();
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void onMusicListChanged() {
		mMusicAdapter.notifyData(mFolderFragment.getPath());

		if (mMusicAdapter.getCount() > 0) {
			if (mMusicService != null) {
				try {
					Log.d(Constant.TAG_USB_MUSIC, "mMusicService.play() : " + mMusicService.getNowPlaying(true).mTitle);
					mMusicService.play();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			mMusicList.setVisibility(View.VISIBLE);
			mNoMusicFile.setVisibility(View.GONE);
		} else {
			mMusicList.setVisibility(View.GONE);
			mNoMusicFile.setVisibility(View.VISIBLE);
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
		if (Constant.DEBUG)
			Log.i(Constant.TAG_USB_MUSIC, "musicEnterPlayer : pos " + pos + " path : " + path);
		if (pos == -1)
			return;

		mMusicAdapter.setSelectedIndex(pos);
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
				tvFolder.setText(R.string.upper_level);
				tvFolder.setVisibility(View.VISIBLE);
			}
			// mMusicAdapter.notifyData(file.toString());
			if (mMusicAdapter.getCount() == 0) {
				mMusicList.setVisibility(View.GONE);
				mNoMusicFile.setVisibility(View.VISIBLE);
			} else {
				mMusicList.setVisibility(View.VISIBLE);
				mNoMusicFile.setVisibility(View.GONE);
			}
		} else {
			try {
				mMusicService.openDir(file.toString());
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onResume() {
		if (Constant.DEBUG)
			Log.i(Constant.TAG_USB_MUSIC, "onResume:");

		mMusicHandler.removeMessages(MSG_MUSIC_LIST);
		mMusicHandler.sendEmptyMessageDelayed(MSG_MUSIC_LIST, 500);
		sendBroadcast(new Intent("com.hwatong.media.START").putExtra("tag", "USB"));
		bindService(new Intent("com.hwatong.music.MUSIC_PALYBACK_SERVICE"), mMusicServiceConnection, BIND_AUTO_CREATE);
		bindService(new Intent("com.remote.hwatong.statusinfoservice"), mStatusBarConnection, BIND_AUTO_CREATE);
		super.onResume();
	}

	@Override
	protected void onPause() {

		mStatusBarInfo = null;
		unbindService(mStatusBarConnection);
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
		unbindService(mMusicServiceConnection);
		mMusicHandler.removeCallbacksAndMessages(null);
		unregisterReceiver(mReceiver);

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
	private OnSeekBarChangeListener mSongSeekBarListener = new OnSeekBarChangeListener() {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if (fromUser) {
				mMusicSongCurtime.setText(Utils.formatetime(progress));
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			mMusicUserSeekSong = true;
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			mMusicUserSeekSong = false;
			if (mMusicService != null) {
				try {
					mMusicService.setNowPlayingInformation(seekBar.getProgress());
					Log.i(Constant.TAG_USB_MUSIC, "mSongSeekBarListener = " + Utils.formatetime(seekBar.getProgress()));
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}

	};

	// private int getPrePosition() {
	// int prePos = -1;
	//
	// int mode = mNowPlaying.mPlaybackRepeatMode;
	// int mCurPosition = mMusicAdapter.getSongListPosition(mNowPlaying.mId);
	// int size = 0;
	// synchronized (mMusicList) {
	// size = mMusicList.getCount();
	// }
	// if (size == 0)
	// return -1;
	//
	// switch (mode) {
	// case com.hwatong.music.NowPlaying.PlaybackRepeatMode_Shuffle:
	// prePos = mRandom.nextInt(size);
	// while (prePos == mCurPosition && size > 1) {
	// prePos = mRandom.nextInt(size);
	// }
	// break;
	// case com.hwatong.music.NowPlaying.PlaybackRepeatMode_All:
	// case com.hwatong.music.NowPlaying.PlaybackRepeatMode_One:
	// prePos = mCurPosition - 1;
	// if (prePos < 0)
	// prePos = size - 1;
	// break;
	// }
	//
	// return prePos;
	// }
	//
	// private int getNextPosition() {
	// int nextPos = -1;
	//
	// int mode = mNowPlaying.mPlaybackRepeatMode;
	// int mCurPosition = mMusicAdapter.getSongListPosition(mNowPlaying.mId);
	// int size = 0;
	// synchronized (mMusicList) {
	// size = mMusicList.getCount();
	// }
	//
	// if (size == 0)
	// return -1;
	//
	// switch (mode) {
	// case com.hwatong.music.NowPlaying.PlaybackRepeatMode_All:
	// case com.hwatong.music.NowPlaying.PlaybackRepeatMode_One:
	// nextPos = mCurPosition + 1;
	// if (nextPos >= size)
	// nextPos = 0;
	// break;
	// case com.hwatong.music.NowPlaying.PlaybackRepeatMode_Shuffle:
	// nextPos = mRandom.nextInt(size);
	// while (nextPos == mCurPosition && size > 1) {
	// nextPos = mRandom.nextInt(size);
	// }
	// break;
	// }
	//
	// return nextPos;
	// }

	@Override
	public void onClick(View v) {

		switch (v.getId()) {

		case R.id.music_previous:
			if (mMusicService != null) {
				try {
					mMusicService.previous();
				} catch (Exception e) {
					e.printStackTrace();
				}
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
				try {
					mMusicService.next();
				} catch (Exception e) {
					e.printStackTrace();
				}
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
			if (mFolderFragment.isAdded()) {
				mTransaction = mFragmentManager.beginTransaction();
				mTransaction.remove(mFolderFragment).commit();
				ivFolderIcon.setImageResource(R.drawable.folder_icon_normal);
				tvFolder.setText(R.string.folder);
				if (ivFolderIcon.getVisibility() == View.GONE) {
					ivFolderIcon.setVisibility(View.VISIBLE);
					tvFolder.setVisibility(View.VISIBLE);
				}
			} else {
				// Intent intent = new Intent(this, MainActivity.class);
				// startActivity(intent);
				this.moveTaskToBack(false);
			}
			break;
		default:
			break;
		}
	}

	// private void nextSong() {
	// final MusicEntry entry = (MusicEntry)
	// mMusicAdapter.getItem(getNextPosition());
	// if (null != entry) {
	// musicEnterPlayer(entry.mFilePath);
	// }
	// }
	//
	// private void previousSong() {
	// final MusicEntry entry = (MusicEntry)
	// mMusicAdapter.getItem(getPrePosition());
	// if (null != entry) {
	// musicEnterPlayer(entry.mFilePath);
	// }
	// }

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
			// mMusicAdapter.notifyData(mFolderFragment.getPath());
			if (mMusicAdapter.getCount() == 0) {
				mMusicList.setVisibility(View.GONE);
				mNoMusicFile.setVisibility(View.VISIBLE);
			} else {
				mMusicList.setVisibility(View.VISIBLE);
				mNoMusicFile.setVisibility(View.GONE);
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
					mStatusBarInfo.setCurrentPageName("usb_music");
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	};

}
