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
import com.hwatong.media.common.Utils;
import com.hwatong.statusbarinfo.aidl.IStatusBarInfo;

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
import android.widget.Button;
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
    private List<MediaItem> mediaItems = null;
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
    private TextView mCurrentProgress;
    private TextView mAllProgress;
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
     * 循环模式
     */
    private Button mLoopModeOne;
    private Button mLoopModeTwo;
    private final int[] PLAY_MODE_RES = {R.drawable.folder_cycle_gray, R.drawable.single_cycle_red, R.drawable.folder_cycle_red};

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
                    statusBarInfo.setCurrentPageName("ipod");
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
                    updateNowPlaying(getNowing(true));
                    refreshPlayList();
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
                    mService.play();
                    mLoopModeOne.setSelected(mService.getNowPlaying(true).mPlaybackShuffleMode);
                    mLoopModeTwo.setBackground(getResources().getDrawable(PLAY_MODE_RES[mService.getNowPlaying(true).mPlaybackRepeatMode]));
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
        startService(new Intent(Constant.IPOD_SERVICE));
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
        mLoopModeOne = (Button) findViewById(R.id.btn_loop_mode);
        mLoopModeOne.setOnClickListener(this);
        mLoopModeTwo = (Button) findViewById(R.id.btn_loop_mode2);
        mLoopModeTwo.setOnClickListener(this);

        mIPodFolderFragment = (IPodFolderFragment) getFragmentManager().findFragmentById(R.id.ipod_music_folder_layout);

        mIPodRightIcon = (ImageView) findViewById(R.id.ipod_right_icon);
        mMediaItemsView = (ListView) findViewById(R.id.ipod_list_music);
        mCurrentProgress = (TextView) findViewById(R.id.ipod_music_curtime);
        mAllProgress = (TextView) findViewById(R.id.ipod_music_alltime);
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
        handler.removeMessages(Constant.MSG_MEDIAPLAYLIST_RECEIVED);
        handler.sendEmptyMessage(Constant.MSG_MEDIAPLAYLIST_RECEIVED);
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
            Log.d(Constant.TAG_IPOD, "onClick");
            if (mService == null) {
                Log.d(Constant.TAG_IPOD, "Ipod Service is mull");
                return;
            }
            try {
                if (getNowing(false) != null && getNowing(false).mPlaybackStatus) {
                    Log.d(Constant.TAG_IPOD, "playing:" + getNowing(false).mPlaybackStatus);
                    mService.pause();
                    mPlayStatus.setImageResource(R.drawable.btn_music_play);
                } else {
                    mService.play();
                    mPlayStatus.setImageResource(R.drawable.btn_music_pause);
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
        } else if (mLoopModeOne == v) {
            if (mService == null) {
                Log.d(Constant.TAG_IPOD, "Ipod Service is mull");
                return;
            }
            try {
                mService.shuffle();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if (mLoopModeTwo == v) {
            if (mService == null) {
                Log.d(Constant.TAG_IPOD, "Ipod Service is mull");
                return;
            }
            try {
                mService.repeat();
            } catch (RemoteException e) {
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
     */
    private void refreshPlayList() {
        if (mediaItems == null) {
            mediaItems = getAllMusic();
        }
        if (mediaItems.size() == 0) {
            mMediaItemsView.setVisibility(View.INVISIBLE);
            mIPodRightIcon.setVisibility(View.VISIBLE);
        } else if (mAdapter == null || mediaItems.size() != mAdapter.getCount()) {
            mIPodRightIcon.setVisibility(View.INVISIBLE);
            mMediaItemsView.setVisibility(View.VISIBLE);
            mAdapter = new IPodAdapter(this, mediaItems);
            mAdapter.setmNowPlaying(getNowing(true));
            mMediaItemsView.setAdapter(mAdapter);
            mMediaItemsView.setSelector(R.drawable.media_list_item_selector);
            mAdapter.notifyDataSetChanged();
        }
        mMediaItemsView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MediaItem item = (MediaItem) mAdapter.getItem(position);
                try {
                    Log.d(Constant.TAG_IPOD, item.mId + " item " + mService.getNowPlaying(true).mId);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                play(position);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * 更新播放歌曲进度，信息
     *
     * @param nowing
     */
    private void updateNowPlaying(NowPlaying nowing) {
        if (nowing == null || mService == null) {
            Log.d(Constant.TAG_IPOD, "NowPlaying update error " + "nowing " + nowing + "mService " + mService);
            return;
        }
        Log.d(Constant.TAG_IPOD, "NowPlaying" + "nowing " + nowing.mId);
        mAdapter.setmNowPlaying(nowing);
        mAdapter.notifyDataSetChanged();
        miPodSeekBar.setMax(nowing.mPlaybackDurationInMilliseconds);
        miPodSeekBar.setProgress(nowing.mPlaybackElapsedTimeInMilliseconds);
        mSongText.setText(nowing.mTitle == null ? getResources().getString(R.string.txt_song) : nowing.mTitle);
        mSingerText.setText(nowing.mArtist == null ? getResources().getString(R.string.txt_singer) : nowing.mArtist);
        mAlbumText.setText(nowing.mAlbum == null ? getResources().getString(R.string.txt_album) : nowing.mAlbum);
        mAllProgress.setText("-" + Utils.formatetime(nowing.mPlaybackDurationInMilliseconds - nowing.mPlaybackElapsedTimeInMilliseconds));
        mCurrentProgress.setText(Utils.formatetime(nowing.mPlaybackElapsedTimeInMilliseconds));

        mPlayStatus.setImageResource(nowing.mPlaybackStatus ? R.drawable.btn_music_pause : R.drawable.btn_music_play);

        try {
            mLoopModeOne.setSelected(mService.getNowPlaying(true).mPlaybackShuffleMode);
            mLoopModeTwo.setBackground(getResources().getDrawable(PLAY_MODE_RES[mService.getNowPlaying(true).mPlaybackRepeatMode]));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
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
     * @param Identifiers         匹配ID
     * @param NumberOfIdentifiers 匹配数量
     * @param ItemsStartingIndex  播放位置
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
     * @param id
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
        play(0);
    }

    private void play(int position) {
        String[] Identifiers = new String[mediaItems.size()];
        for (int i = 0; i < mediaItems.size(); i++) {
            Identifiers[i] = mediaItems.get(i).mId;
        }
        playByList(Identifiers, Identifiers.length, position);
    }
}
