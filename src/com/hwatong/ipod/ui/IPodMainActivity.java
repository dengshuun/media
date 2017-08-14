package com.hwatong.ipod.ui;

import java.util.ArrayList;
import java.util.List;

import com.hwatong.ipod.Album;
import com.hwatong.ipod.Artist;
import com.hwatong.ipod.Genre;
import com.hwatong.ipod.IService;
import com.hwatong.ipod.MediaItem;
import com.hwatong.ipod.NowPlaying;
import com.hwatong.ipod.Playlist;
import com.hwatong.media.common.R;
import com.hwatong.media.common.Constant;
import com.hwatong.statusbarinfo.aidl.IStatusBarInfo;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;

public class IPodMainActivity extends Activity implements OnClickListener {
	/**
	 * 播放列表
	 */
	private List<MediaItem> mediaItems = new ArrayList<MediaItem>();
	/**
	 * 播放状态
	 */
	private ImageView mPlayStatus;
	/**
	 * 下一首
	 */
	private ImageView mNext;
	/**
	 * 上一首
	 */
	private ImageView mPre;
	/**
	 * 返回键
	 */
	private View mBack;
	private View mButtonFolder;
	private View mMainLayout;
	/**
	 * IPod上层接口
	 */
	private IService mService = null;
	private IPodFolderFragment mIPodFolderFragment;
	/**
	 * 歌曲信息
	 */
	private TextView mSongText;
	private TextView mSingerText;
	private TextView mAlbumText;
	private SeekBar miPodSeekBar;
	/**
	 * 底部文件夹
	 */
	private ImageView mFloderIcon;
	private TextView mFlodertxt;
	/**
	 * 界面右部布局
	 */
	private ListView mMediaItemsView;
	private ImageView mIPodRightIcon;

	private IPodAdapter mAdapter;

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
					statusBarInfo.setCurrentPageName("iPOD");
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	};

	/**
	 * IPod ,事件回调
	 */
	private com.hwatong.ipod.ICallback.Stub mCallback = new com.hwatong.ipod.ICallback.Stub() {

		@Override
		public void onNowPlayingReceived() throws RemoteException {
			Log.d(Constant.TAG_IPOD, "onNowPlayingReceived");
			handler.removeMessages(Constant.MSG_NOWPLAYING_RECEIVED);
			handler.sendEmptyMessage(Constant.MSG_NOWPLAYING_RECEIVED);
		}

		@Override
		public void onMediaPlaylistReceived() throws RemoteException {
			Log.d(Constant.TAG_IPOD, "onMediaPlaylistReceived");
			handler.removeMessages(Constant.MSG_MEDIAPLAYLIST_RECEIVED);
			handler.sendEmptyMessage(Constant.MSG_MEDIAPLAYLIST_RECEIVED);
		}

		@Override
		public void onMediaItemReceived() throws RemoteException {
			Log.d(Constant.TAG_IPOD, "onMediaItemReceived");
			handler.removeMessages(Constant.MSG_MEDIAITEM_RECEIVED);
			handler.sendEmptyMessage(Constant.MSG_MEDIAITEM_RECEIVED);
		}

		@Override
		public void onDetached() throws RemoteException {
			Log.d(Constant.TAG_IPOD, "onDetached");
			/**
			 * IPod设备移除
			 */
			handler.removeMessages(Constant.MSG_REMOVE_DEVICE);
			handler.sendEmptyMessage(Constant.MSG_REMOVE_DEVICE);
		}

		@Override
		public void onAttached() throws RemoteException {
			Log.d(Constant.TAG_IPOD, "onAttached");
			/**
			 * IPod设备插入
			 */
			handler.removeMessages(Constant.MSG_PROBE_DEVICE);
			Message m = Message.obtain(handler, Constant.MSG_PROBE_DEVICE, 1, 0);
			handler.sendMessage(m);
		}
	};

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case Constant.MSG_MEDIAITEM_RECEIVED:
				break;
			case Constant.MSG_MEDIALIBRARYINFORMATION_RECEIVED:
				break;
			case Constant.MSG_MEDIAPLAYLIST_RECEIVED:
				refreshPlayList();
				break;
			case Constant.MSG_NOWPLAYING_RECEIVED:
				updateNowPlaying(getNowing(true));
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
	 * IPod服务连接
	 */
	private ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.d(Constant.TAG_IPOD, "ipod service disconnected");
			if (mService != null) {
				try {
					mService.unregisterCallback(mCallback);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			mService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(Constant.TAG_IPOD, "ipod service connected ");
			mService = IService.Stub.asInterface(service);
			if (mService != null) {
				Log.d(Constant.TAG_IPOD, "ipodUI registerCallback");
				try {
					mService.registerCallback(mCallback);
					mediaItems = getAllMusic();
					refreshPlayList();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			} else {
				Log.d(Constant.TAG_IPOD, "ipod service is null");
			}

		}
	};
	/**
	 * 接收媒体播放状态 ， 根据需求释放IPOD
	 */
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(Constant.TAG_IPOD, "action:" + intent.getAction());
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
		setContentView(R.layout.activity_ipod_music);
		bindService(new Intent(Constant.IPOD_SERVICE), mServiceConnection, BIND_AUTO_CREATE);
		registerReceiver(mReceiver, new IntentFilter(Constant.MEDIA_PLAY_STATUS));
		initUI();

	}

	private void initUI() {

		mBack = findViewById(R.id.ipod_btn_back);
		mBack.setOnClickListener(this);
		mButtonFolder = findViewById(R.id.ipod_btn_folder);
		mButtonFolder.setOnClickListener(this);
		mPlayStatus = (ImageView) findViewById(R.id.ipod_music_play);
		mPlayStatus.setOnClickListener(this);
		mPre = (ImageView) findViewById(R.id.ipod_music_previous);
		mPre.setOnClickListener(this);
		mNext = (ImageView) findViewById(R.id.ipod_music_next);
		mNext.setOnClickListener(this);
		mMainLayout = findViewById(R.id.ipod_music_main_layout);
		mSongText = (TextView) findViewById(R.id.ipod_txt_song);
		mSingerText = (TextView) findViewById(R.id.ipod_txt_singer);
		mAlbumText = (TextView) findViewById(R.id.ipod_txt_album);
		miPodSeekBar = (SeekBar) findViewById(R.id.ipod_music_seekbar);
		mFloderIcon = (ImageView) findViewById(R.id.ipod_folder_icon);
		mFlodertxt = (TextView) findViewById(R.id.ipod_folder_txt);

		mIPodFolderFragment = (IPodFolderFragment) getFragmentManager().findFragmentById(R.id.ipod_music_folder_layout);

		mIPodRightIcon = (ImageView) findViewById(R.id.ipod_right_icon);
		mMediaItemsView = (ListView) findViewById(R.id.ipod_list_music);

		isShowFloderFragment(false);
	}

	private void isShowFloderFragment(boolean show) {
		if (show) {
			getFragmentManager().beginTransaction().show(mIPodFolderFragment).commit();
			mMainLayout.setVisibility(View.INVISIBLE);
			onRoot();
		} else {
			getFragmentManager().beginTransaction().hide(mIPodFolderFragment).commit();
			mMainLayout.setVisibility(View.VISIBLE);
			mFloderIcon.setImageResource(R.drawable.img_folder_icon);
			mFloderIcon.setVisibility(View.VISIBLE);
			mFlodertxt.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateNowPlaying(getNowing(true));
		bindService(new Intent("com.remote.hwatong.statusinfoservice"), statusBarConnection, BIND_AUTO_CREATE);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(mServiceConnection);
		unregisterReceiver(mReceiver);
		unbindService(statusBarConnection);
	}

	@Override
	public void onClick(View v) {
		if (mBack == v) {
			if (!mIPodFolderFragment.isHidden()) {
				isShowFloderFragment(false);
			} else {
				finish();
			}
		} else if (mButtonFolder == v) {
			if (mIPodFolderFragment.isHidden()) {
				isShowFloderFragment(true);
			} else {
				mIPodFolderFragment.onClickFolder();
			}
		} else if (mPlayStatus == v) {
			if (mService == null) {
				Log.d(Constant.TAG_IPOD, "Ipod Service is mull");
				return;
			}
			try {
				if (getNowing(false) != null && getNowing(false).mPlaybackStatus) {
					mService.pause();
				} else {
					mService.play();
				}
			} catch (RemoteException e) {
				Log.d(Constant.TAG_IPOD, "Remount error");
				e.printStackTrace();
			}
		} else if (mPre == v) {
			if (mService == null) {
				Log.d(Constant.TAG_IPOD, "Ipod Service is mull");
				return;
			}
			try {
				mService.previous();
			} catch (RemoteException e) {
				Log.d(Constant.TAG_IPOD, "Remount error");
				e.printStackTrace();
			}
		} else if (mNext == v) {
			if (mService == null) {
				Log.d(Constant.TAG_IPOD, "Ipod Service is mull");
				return;
			}
			try {
				mService.next();
			} catch (RemoteException e) {
				Log.d(Constant.TAG_IPOD, "Remount error");
				e.printStackTrace();
			}
		}
	}

	/**
	 * 目录回调
	 */
	public void onRoot() {
		mFloderIcon.setVisibility(mIPodFolderFragment.isRootFolder() ? View.INVISIBLE : View.VISIBLE);
		mFlodertxt.setVisibility(mIPodFolderFragment.isRootFolder() ? View.INVISIBLE : View.VISIBLE);
		if (mIPodFolderFragment.isRootFolder()) {
			mFloderIcon.setVisibility(View.INVISIBLE);
			mFlodertxt.setVisibility(View.INVISIBLE);
		} else {
			mFloderIcon.setVisibility(View.VISIBLE);
			mFloderIcon.setImageResource(R.drawable.folder_icon_back);
			mFlodertxt.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 更新歌曲列表
	 * 
	 * @param playList
	 */
	private void refreshPlayList() {
		if (mediaItems.size() == 0) {
			mMediaItemsView.setVisibility(View.INVISIBLE);
			mIPodRightIcon.setVisibility(View.VISIBLE);
		} else {
			mIPodRightIcon.setVisibility(View.INVISIBLE);
			mMediaItemsView.setVisibility(View.VISIBLE);
			mAdapter = new IPodAdapter(this, mediaItems);
			mMediaItemsView.setAdapter(mAdapter);
			mMediaItemsView.setSelector(R.drawable.media_list_item_selector);
			mAdapter.notifyDataSetChanged();
		}
	}

	/**
	 * 更新播放歌曲进度，信息
	 * 
	 * @param nowing
	 */
	private void updateNowPlaying(NowPlaying nowing) {
		if (nowing == null || mService == null) {
			Log.d(Constant.TAG_IPOD, "NowPlaying update error ");
			return;
		}
		miPodSeekBar.setMax(nowing.mPlaybackDurationInMilliseconds);
		miPodSeekBar.setProgress(nowing.mPlaybackElapsedTimeInMilliseconds);
		mSongText.setText(nowing.mTitle == null ? getResources().getString(R.string.txt_song) : nowing.mTitle);
		mSingerText.setText(nowing.mArtist == null ? getResources().getString(R.string.txt_singer) : nowing.mArtist);
		mAlbumText.setText(nowing.mAlbum == null ? getResources().getString(R.string.txt_album) : nowing.mAlbum);
	}

	/**
	 * 获取当前歌曲信息
	 * 
	 * @return
	 */
	public NowPlaying getNowing(boolean full) {
		NowPlaying nowPlaying = null;
		if (mService != null) {
			try {
				nowPlaying = mService.getNowPlaying(full);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return nowPlaying;
	}

	/**
	 * @param Identifiers
	 *            匹配ID
	 * @param NumberOfIdentifiers
	 *            匹配数量
	 * @param ItemsStartingIndex
	 *            播放位置
	 */
	public void playByList(String[] Identifiers, int NumberOfIdentifiers, int ItemsStartingIndex) {
		try {
			mService.playMediaLibraryItems(Identifiers, NumberOfIdentifiers, ItemsStartingIndex);
		} catch (RemoteException e) {
			Log.d(Constant.TAG_IPOD, "Remount error");
			e.printStackTrace();
		}
	}

	/**
	 * 获取艺术家列表
	 * 
	 * @return
	 */
	public List<Artist> getArtistList() {
		List<Artist> artists = null;
		if (mService != null) {
			try {
				artists = mService.getArtistList();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return artists;
	}

	/**
	 * 获取专辑列表
	 * 
	 * @return
	 */
	public List<Album> getAlbumList() {
		List<Album> albums = null;
		if (mService != null) {
			try {
				albums = mService.getAlbumList();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return albums;
	}

	/**
	 * 获取流派类型列表
	 * 
	 * @return
	 */
	public List<Genre> getGenreList() {
		List<Genre> genres = null;
		if (mService != null) {
			try {
				genres = mService.getGenreList();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return genres;
	}

	/**
	 * 获取所有播放列表
	 * 
	 * @return
	 */
	public List<Playlist> getPlayList() {
		List<Playlist> lists = null;
		if (mService != null) {
			try {
				lists = mService.getPlaylistList();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return lists;
	}

	/**
	 * 获取所有的歌曲
	 * 
	 * @return
	 */
	private List<MediaItem> getAllMusic() {
		List<MediaItem> list = new ArrayList<MediaItem>();
		if (mService != null) {
			try {
				int count = mService.getMediaItemCount();
				Log.d(Constant.TAG_IPOD, "count:" + count);
				for (int i = 0; i < count; i++) {
					list.add(mService.getMediaItem(i));
					Log.d(Constant.TAG_IPOD, mService.getMediaItem(i).mTitle);
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		} else {
			Log.d(Constant.TAG_IPOD, "mService is null");
		}
		return list;
	}

	/**
	 * @param type
	 * @param value
	 */
	public void setPlayList(String type, String id) {
		try {
			if (type.equals(Constant.GENRE)) {
				mediaItems = mService.getMediaItemsByGenre(id);
			} else if (type.equals(Constant.ARTIST)) {
				mediaItems = mService.getMediaItemsByArtist(id);
			} else if (type.equals(Constant.ALBUM)) {
				mediaItems = mService.getMediaItemsByAlbum(id);
			} else if (type.equals(Constant.PLAY_LIST)) {
				mediaItems = mService.getMediaItemsInPlaylist(id);
			}
			Log.d(Constant.TAG_IPOD, "mediaItems size:" + mediaItems.size());
			refreshPlayList();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
