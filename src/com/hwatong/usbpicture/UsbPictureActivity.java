package com.hwatong.usbpicture;

import java.io.File;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hwatong.media.PictureEntry;
import com.hwatong.media.common.Constant;
import com.hwatong.media.common.FolderFragment;
import com.hwatong.media.common.FolderFragment.Type;
import com.hwatong.media.common.LoadingDialog;
import com.hwatong.media.common.PhotoView;
import com.hwatong.media.common.R;
import com.hwatong.media.common.Utils;
import com.hwatong.media.common.ViewPagerScroller;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class UsbPictureActivity extends Activity implements OnClickListener {

    private PictureAdapter mPictureAdapter;
    private com.hwatong.media.IService mService;

    private static final int MSG_VOLUME_STATE_CHANGED = 0;
    private static final int MSG_PRESCAN_CHANGED = 1;
    private static final int MSG_PICTURE_LIST_CHANGED = 2;

    private String mLastPlayPath;// 最后记录的图片地址
    private int mNowPlayPosition;

    private ViewPager mPictureViewPager;
    private PictureLoader mPictureLoader;
    private int mOffset = 0; // viewPager的位置

    private LoadingDialog mLoadingDialog;

    private RelativeLayout btnBack;
    private RelativeLayout mPictureControlView;
    private ListView mPictureList;
    private TextView tvFileName;
    private ImageView ivPicturePlay;
    private ImageView ivPicturePre;
    private ImageView ivPictureNext;
    private ImageView ivFolderIcon;
    private ImageView ivFolderThumbnailsIcon;
    private TextView tvFolderThumbnails;
    private Button btnPictureFullScreen;
    private TextView mNoPictureFile;

    private LinearLayout mFolder;
    private LinearLayout mFolderThumbnails;
    private LinearLayout mPictureBottomBar;
    private FragmentTransaction mTransaction;
    private FragmentManager mFragmentManager;
    private FolderFragment mFolderFragment;

    private boolean isThumbnails;
    private boolean isFullScreen;

    private static final int mMaxSize = 100; // ViewPager 滾動轮播的最大值
    private boolean mPictureIsPlaying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb_picture);

        // 接收广播
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.hwatong.voice.CLOSE_PICTURE");
        registerReceiver(mReceiver, filter);

        initFolderFragment();
        initView();
        timeUpdateHandler.removeMessages(MSG_TIME_TOOL);
        timeUpdateHandler.sendEmptyMessageDelayed(MSG_TIME_TOOL, 3000);
    }

    private void initFolderFragment() {

        mFragmentManager = getFragmentManager();
        mFolderFragment = new FolderFragment();
        mFolderFragment.setType(Type.PICTURE);
    }

    private void initView() {

        isThumbnails = false;
        isFullScreen = false;

        mPictureLoader = new PictureLoader(this);
        mLoadingDialog = new LoadingDialog(this, -1);
        mPictureAdapter = new PictureAdapter(this, mFolderFragment.getPath(), 0);
        mPictureList = (ListView) findViewById(R.id.list_picture);
        mFolder = (LinearLayout) findViewById(R.id.btn_folder);
        mFolderThumbnails = (LinearLayout) findViewById(R.id.btn_folder_thumbnails);
        tvFolderThumbnails = (TextView) findViewById(R.id.txt_folder_thumbnails);
        btnBack = (RelativeLayout) findViewById(R.id.btn_back);
        tvFileName = (TextView) findViewById(R.id.file_name);
        ivPicturePlay = (ImageView) findViewById(R.id.picture_play);
        ivPicturePre = (ImageView) findViewById(R.id.picture_previous);
        ivPictureNext = (ImageView) findViewById(R.id.picture_next);
        ivFolderIcon = (ImageView) findViewById(R.id.img_folder_icon);
        ivFolderThumbnailsIcon = (ImageView) findViewById(R.id.img_folder_thumbnails_icon);
        btnPictureFullScreen = (Button) findViewById(R.id.btn_picture_resize);
        mPictureBottomBar = (LinearLayout) findViewById(R.id.usb_picture_bottom_bar);
        mPictureViewPager = (ViewPager) findViewById(R.id.pager_picture);
        mPictureControlView = (RelativeLayout) findViewById(R.id.picture_control_view);
        mNoPictureFile = (TextView) findViewById(R.id.text_no_picture);

        mPictureList.setAdapter(mPictureAdapter);
        mPictureList.setSelector(R.drawable.media_list_item_selector);
        ivPicturePlay.setOnClickListener(this);
        ivPicturePre.setOnClickListener(this);
        ivPictureNext.setOnClickListener(this);
        btnPictureFullScreen.setOnClickListener(this);
        mFolder.setOnClickListener(this);
        mFolderThumbnails.setOnClickListener(this);
        btnBack.setOnClickListener(this);

        mPictureViewPager.setPageMargin((int) (getResources().getDisplayMetrics().density * 15));
        mPictureViewPager.setPageTransformer(true, new DepthPageTransformer());
        new ViewPagerScroller(this).initViewPagerScroll(mPictureViewPager);
        mPictureList.setOnItemClickListener(mPictureOnItemClickListener);
    }

    private final OnItemClickListener mPictureOnItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (Constant.DEBUG)
                Log.i(Constant.TAG_USB_PICTURE, "onItemClick " + position);
            pictureMoveTo(position, false);
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(Constant.TAG_USB_PICTURE, "--- 接收到广播， action: " + action);
            if ("com.hwatong.voice.CLOSE_PICTURE".equals(action)) {
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
        mLastPlayPath = Utils.getPlayingPicturePath(this);// 取最后播放的路径
        bindService(new Intent("com.remote.hwatong.statusinfoservice"), mStatusBarConnection, BIND_AUTO_CREATE);
        default_option();
    }

    @Override
    protected void onPause() {
        if (Constant.DEBUG)
            Log.i(Constant.TAG_USB_PICTURE, "onPause");

        if (mService != null) {
            try {
                mService.unregisterCallback(mCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        mService = null;
        unbindService(mMediaServiceConnection);

        mHandler.removeCallbacksAndMessages(null);

        mStatusBarInfo = null;
        unbindService(mStatusBarConnection);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (Constant.DEBUG)
            Log.i(Constant.TAG_USB_PICTURE, "onDestroy");

        mPictureLoader.waitComplete();
        mPictureLoader = null;
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    private final ServiceConnection mMediaServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (Constant.DEBUG)
                Log.i(Constant.TAG_USB_PICTURE, "onServiceConnected");

            mService = com.hwatong.media.IService.Stub.asInterface(service);

            try {
                mService.registerCallback(mCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            onPictureListChanged();

            mPictureViewPager.setAdapter(mPagerAdapter);
            mPictureViewPager.setOnPageChangeListener(mOnPageChangeListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (Constant.DEBUG)
                Log.i(Constant.TAG_USB_PICTURE, "onServiceDisconnected");
            mService = null;
        }
    };

    private final com.hwatong.media.ICallback mCallback = new com.hwatong.media.ICallback.Stub() {

        @Override
        public void onVolumeStateChanged(String path) {
            if (Constant.DEBUG)
                Log.i(Constant.TAG_USB_PICTURE, "onVolumeStateChanged " + path);

            mHandler.removeMessages(MSG_VOLUME_STATE_CHANGED);
            Message m = Message.obtain(mHandler, MSG_VOLUME_STATE_CHANGED, path);
            mHandler.sendMessage(m);
        }

        @Override
        public void onUsbStateChanged(String path, String oldState, String newState) {
            if (Constant.DEBUG)
                Log.i(Constant.TAG_USB_PICTURE, "onUsbStateChanged " + path + " " + oldState + " -> " + newState);
        }

        @Override
        public void onUsbScanChanged(String path, String oldState, String newState) {
            if (Constant.DEBUG)
                Log.i(Constant.TAG_USB_PICTURE, "onUsbScanChanged " + path + " " + oldState + " -> " + newState);
        }

        @Override
        public void onUsbPrescanChanged(String path, String oldState, String newState) {
            if (Constant.DEBUG)
                Log.i(Constant.TAG_USB_PICTURE, "onUsbPrescanChanged " + path + " " + oldState + " -> " + newState);

            mHandler.removeMessages(MSG_PRESCAN_CHANGED);
            Message m = Message.obtain(mHandler, MSG_PRESCAN_CHANGED, path);
            mHandler.sendMessage(m);
        }

        @Override
        public void onMusicListChanged() {
            if (Constant.DEBUG)
                Log.i(Constant.TAG_USB_PICTURE, "onMusicListChanged");
        }

        @Override
        public void onVideoListChanged() {
            if (Constant.DEBUG)
                Log.i(Constant.TAG_USB_PICTURE, "onVideoListChanged");
        }

        @Override
        public void onPictureListChanged() {
            if (Constant.DEBUG)
                Log.i(Constant.TAG_USB_PICTURE, "onPictureListChanged");

            mHandler.removeMessages(MSG_PICTURE_LIST_CHANGED);
            mHandler.sendEmptyMessage(MSG_PICTURE_LIST_CHANGED);
        }

    };

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case MSG_VOLUME_STATE_CHANGED:
                    onVolumeStateChanged((String) msg.obj);
                    break;

                case MSG_PRESCAN_CHANGED:
                    onPrescanChanged((String) msg.obj);
                    break;

                case MSG_PICTURE_LIST_CHANGED:
                    onPictureListChanged();
                    break;
            }
        }
    };

    public void onVolumeStateChanged(String path) {
        if (mService != null) {
            try {
                boolean state = mService.getVolumeState(path);

                if (Constant.DEBUG)
                    Log.i(Constant.TAG_USB_PICTURE, "onVolumeStateChanged " + path + ", " + state);

                if (!state) {
                    finish();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void onPrescanChanged(String path) {
        if (mService != null) {
            try {
                String state = mService.getUsbPrescanState(path);

                if ("prescan".equals(state)) {
                    mLoadingDialog.show();
                } else if ("stop".equals(state)) {
                    mLoadingDialog.dismiss();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void onPictureListChanged() {
        if (Constant.DEBUG)
            Log.i(Constant.TAG_USB_PICTURE, "onPictureListChanged");
        mPictureAdapter.notifyData(mFolderFragment.getPath());

        if (mPictureAdapter.getCount() > 0) {
            mPictureList.setVisibility(View.VISIBLE);
            mNoPictureFile.setVisibility(View.GONE);
        } else {
            mPictureList.setVisibility(View.GONE);
            mNoPictureFile.setVisibility(View.VISIBLE);
        }
    }

    private void default_option() {

        if (mPictureAdapter.getCount() > 0) {
            mLastPlayPath = Utils.getPlayingPicturePath(UsbPictureActivity.this);
            File file = new File(mLastPlayPath);
            if (file.exists()) {
                int pos = mPictureAdapter.getPictureListPosition(mLastPlayPath);
                if (Constant.DEBUG)
                    Log.i(Constant.TAG_USB_PICTURE, "default_option " + mLastPlayPath + " " + pos);

                if (pos == -1) {
                    pictureMoveTo(0, false);
                    mPictureList.setSelection(0);
                } else {
                    pictureMoveTo(pos, false);
                    mPictureList.setSelection(pos);
                }
            } else {
                pictureMoveTo(0, false);
                mPictureList.setSelection(0);
            }
        }
    }

    private void pictureMoveTo(int position, boolean save) {
        PictureEntry entry = mPictureAdapter.getItem(position);
        if (entry != null) {
            if (Constant.DEBUG)
                Log.i(Constant.TAG_USB_PICTURE, "pictureMoveTo " + entry.mFilePath);

            // removeUpdate();
            mPictureViewPager.setCurrentItem(position, true);
            mPictureAdapter.setmNowPlayPosition(position);
            mNowPlayPosition = position;

            tvFileName.setText(Utils.getNameFromFilename(Utils.getExtFromFilename(entry.mFilePath)));

            if (save)
                Utils.savePlayingPicturePath(UsbPictureActivity.this, entry.mFilePath);
        }
    }

    @SuppressLint("HandlerLeak")
    private class PictureLoader extends Handler implements Runnable {
        private static final int PICTURE_WIDTH = 800;
        private static final int PICTURE_HEIGHT = 400;

        @SuppressWarnings("unused")
        private final Context mContext;

        private boolean exit;
        private boolean done;

        private String mFilePath;

        private Thread mLoader;

        private Bitmap mBitmap;

        private ImageView mImageView;

        private PictureLoader(Context context) {
            mContext = context;
        }

        private void load(ImageView imageView, String filePath) {
            waitComplete();

            if (mLoader == null && filePath != null) {
                mImageView = imageView;
                mFilePath = filePath;
                exit = false;
                done = false;
                mLoader = new Thread(this);
                mLoader.start();
            }
        }

        private synchronized void waitComplete() {
            if (mLoader != null) {
                exit = true;
                notifyAll();
                while (!done) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (mBitmap != null)
                    mImageView.setImageBitmap(mBitmap);
            }
            mLoader = null;
            mBitmap = null;
            mFilePath = null;
            mImageView = null;
            removeMessages(0);
        }

        @Override
        public void run() {
            Bitmap bm = null;

            if (mFilePath != null) {
                final BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(mFilePath, opt);

                opt.inSampleSize = 1;
                while (opt.outWidth / opt.inSampleSize > PICTURE_WIDTH && opt.outHeight / opt.inSampleSize > PICTURE_HEIGHT) {
                    opt.inSampleSize *= 2;
                }

                opt.inJustDecodeBounds = false;
                bm = BitmapFactory.decodeFile(mFilePath, opt);
            }

            if (bm == null) {
                final BitmapFactory.Options opt = new BitmapFactory.Options();
                bm = BitmapFactory.decodeResource(getResources(), R.drawable.picture_icon_small, opt);
            }

            synchronized (this) {
                mBitmap = bm;
                removeMessages(0);
                sendEmptyMessage(0);
                while (!exit && mBitmap != null) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                done = true;
                notifyAll();
            }
        }

        @Override
        public void handleMessage(Message msg) {
            synchronized (this) {
                if (mBitmap != null) {
                    mImageView.setImageBitmap(mBitmap);
                    mBitmap = null;
                    notifyAll();
                }
            }
        }

    }

    @Override
    public void onClick(View v) {

        timeUpdateHandler.removeMessages(MSG_TIME_TOOL);
        timeUpdateHandler.sendEmptyMessageDelayed(MSG_TIME_TOOL, 3000);

        switch (v.getId()) {

            case R.id.btn_folder:
                openFolder();
                break;

            case R.id.btn_folder_thumbnails:
                if (isThumbnails) {
                    changeToList();
                } else {
                    changeToThumbnails();
                }
                break;

            case R.id.btn_back:
                // if (mPictureIsPlaying) {
                // removeUpdate();
                // }
                if (mFolderFragment.isVisible()) {
                    mTransaction = mFragmentManager.beginTransaction();
                    mTransaction.hide(mFolderFragment).commit();
                    mFolderThumbnails.setVisibility(View.GONE);
                    ivFolderIcon.setImageResource(R.drawable.img_folder_icon);
                    timeUpdateHandler.removeMessages(MSG_TIME_TOOL);
                    timeUpdateHandler.removeMessages(MSG_VIEWPAGER_NOTIFY);
                    timeUpdateHandler.sendEmptyMessage(MSG_VIEWPAGER_NOTIFY);
                } else {
                    onBackPressed();
                }
                break;

            case R.id.btn_picture_resize:
                // if (mPictureIsPlaying) {
                // removeUpdate();
                // }
                if (isFullScreen) {
                    quitFullScreen();
                    btnPictureFullScreen.setBackgroundResource(R.drawable.btn_vedio_full_screen);
                } else {
                    enterFullScreen();
                    btnPictureFullScreen.setBackgroundResource(R.drawable.btn_half_screen);
                }
                timeUpdateHandler.removeMessages(MSG_VIEWPAGER_NOTIFY);
                timeUpdateHandler.sendEmptyMessage(MSG_VIEWPAGER_NOTIFY);
                break;

            case R.id.picture_previous:
                // if (mPictureIsPlaying) {
                // removeUpdate();
                // }
                if (mPictureAdapter.getCount() != 0) {
                    if (mNowPlayPosition != 0) {
                        mNowPlayPosition = (mNowPlayPosition - 1) % mPictureAdapter.getCount();
                    } else {
                        mNowPlayPosition = mPictureAdapter.getCount() - 1;
                    }
                    mPictureAdapter.setmNowPlayPosition(mNowPlayPosition);
                    pictureMoveTo(mNowPlayPosition, true);
                }
                break;

            case R.id.picture_next:
                // if (mPictureIsPlaying) {
                // removeUpdate();
                // }
                if (mPictureAdapter.getCount() != 0) {
                    mNowPlayPosition = (mNowPlayPosition + 1) % mPictureAdapter.getCount();
                    mPictureAdapter.setmNowPlayPosition(mNowPlayPosition);
                    pictureMoveTo(mNowPlayPosition, true);
                }
                break;

            case R.id.picture_play:
                if (!mPictureIsPlaying) {
                    startUpdate();
                } else {
                    removeUpdate();
                }
                break;

            default:
                break;
        }
    }

    /**
     * 离开全屏模式
     */
    private void quitFullScreen() {
        isFullScreen = false;
        mPictureList.setVisibility(View.VISIBLE);
        mPictureBottomBar.setVisibility(View.VISIBLE);
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
        mPictureList.setVisibility(View.GONE);
        mPictureBottomBar.setVisibility(View.GONE);
        timeUpdateHandler.sendEmptyMessageDelayed(MSG_TIME_TOOL, 3000);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    // 文件夹改为列表模式
    private void changeToList() {
        Log.d(Constant.TAG_USB_PICTURE, "List");
        mFolderFragment.setListOrThumbnails(isThumbnails);
        ivFolderThumbnailsIcon.setImageResource(R.drawable.img_folder_thumbnails_icon);
        tvFolderThumbnails.setText(R.string.folder_thumbnails);
        isThumbnails = false;
    }

    // 文件夹改为缩略图模式
    private void changeToThumbnails() {
        Log.d(Constant.TAG_USB_PICTURE, "Thumbnails");
        mFolderFragment.setListOrThumbnails(isThumbnails);
        ivFolderThumbnailsIcon.setImageResource(R.drawable.img_folder_list_icon);
        tvFolderThumbnails.setText(R.string.folder_list);
        isThumbnails = true;
    }

    // 处理文件夹点击的事件
    public void onFolderItemClick(File file) {
        Log.e("sss", "event---->" + file.getName());

        if (file.isDirectory()) {
            mPictureAdapter.notifyData(file.toString());
            timeUpdateHandler.removeMessages(MSG_VIEWPAGER_NOTIFY);
            timeUpdateHandler.sendEmptyMessage(MSG_VIEWPAGER_NOTIFY);
            if (mPictureAdapter.getCount() > 0) {
                mPictureList.setVisibility(View.VISIBLE);
                mNoPictureFile.setVisibility(View.GONE);
            } else {
                mPictureList.setVisibility(View.GONE);
                mNoPictureFile.setVisibility(View.VISIBLE);
            }
        } else {
            pictureMoveTo(mPictureAdapter.getPictureListPosition(file.toString()), true);
        }
    }

    // 打开文件夹
    private void openFolder() {
        if (!mFolderFragment.isAdded()) {
            mTransaction = mFragmentManager.beginTransaction();
            mTransaction.add(R.id.usb_picture_folder_layout, mFolderFragment).commit();
        } else if (mFolderFragment.isHidden()) {
            mTransaction = mFragmentManager.beginTransaction();
            mTransaction.show(mFolderFragment).commit();
        } else if (!mFolderFragment.getPath().equals(Constant.ROOT_DIR_PATH)) {
            mFolderFragment.setPath(Utils.getPreDirectory(mFolderFragment.getPath()));
            mPictureAdapter.notifyData(mFolderFragment.getPath());
            if (mPictureAdapter.getCount() > 0) {
                mPictureList.setVisibility(View.VISIBLE);
                mNoPictureFile.setVisibility(View.GONE);
            } else {
                mPictureList.setVisibility(View.GONE);
                mNoPictureFile.setVisibility(View.VISIBLE);
            }
        }
        ivFolderIcon.setImageResource(R.drawable.folder_icon_back);
        mFolderThumbnails.setVisibility(View.VISIBLE);
    }

    /**
     * 切换图片时图片大小和透明度的设置
     */
    public class DepthPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        @SuppressLint("NewApi")
        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) {
                view.setAlpha(0);

            } else if (position <= 0) {
                view.setAlpha(1);
                view.setTranslationX(0);
                view.setScaleX(1);
                view.setScaleY(1);

            } else if (position <= 1) {
                view.setAlpha(1 - position);

                view.setTranslationX(pageWidth * -position);
                float scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

            } else {
                view.setAlpha(0);
            }
        }
    }

    private final PagerAdapter mPagerAdapter = new PagerAdapter() {
        @Override
        public int getCount() {
            return mMaxSize;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public int getItemPosition(Object object) {
            if (mPictureAdapter.getCount() > 0) {
                return POSITION_NONE;// 避免全屏时图片不在中间。强迫viewpager重绘所有item的目的。
            }
            return POSITION_UNCHANGED;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            if (Constant.DEBUG)
                Log.i(Constant.TAG_USB_PICTURE, "mPager instantiateItem " + position);

            if (position < 0) {
                finish();
                return null;
            }

            final View view;

            int posInList = -1;
            if (mPictureAdapter.getCount() != 0) {
                posInList = (position + mOffset) % mPictureAdapter.getCount();
            }

            final PictureEntry entry = mPictureAdapter.getItem(posInList);

            if (entry != null && Utils.getTypeFromFilename(entry.mFilePath).equalsIgnoreCase("gif")) {
                view = getLayoutInflater().inflate(R.layout.gif, null);
                ImageView gifView = (ImageView) view.findViewById(R.id.piture_gif);
                // 使用Gilde加载gif图片
                Glide.with(getApplication()).load(entry.mFilePath).asGif().fitCenter().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(gifView);
                view.setOnClickListener(mPictureOnClicListener);
            } else if (entry != null) {
                view = new PhotoView(UsbPictureActivity.this);
                ((PhotoView) view).enable();
                ((PhotoView) view).setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                mPictureLoader.load((PhotoView) view, entry.mFilePath);
                ((PhotoView) view).setOnClickListener(mPictureOnClicListener);
            } else {
                view = null;
            }
            if (view != null)
                container.addView(view);

            if (Constant.DEBUG)
                Log.i(Constant.TAG_USB_PICTURE, "mPager instantiateItem");
            return view;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            if (Constant.DEBUG)
                Log.i(Constant.TAG_USB_PICTURE, "mPager setPrimaryItem " + position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if (Constant.DEBUG)
                Log.i(Constant.TAG_USB_PICTURE, "mPager destroyItem " + position);
            container.removeView((View) object);
        }
    };

    private final View.OnClickListener mPictureOnClicListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mPictureControlView.getVisibility() == View.VISIBLE) {
                setToolVisible(View.GONE);
            } else {
                setToolVisible(View.VISIBLE);
            }
        }
    };

    public int getStartPageIndex(int i) {
        if (i < 1)
            return 0;
        int index = mMaxSize / 2;
        int remainder = index % i;
        index = index - remainder;
        return index;
    }

    private static final int MSG_TIME_PLAY = 0;// 播放图片时间更新
    private static final int MSG_TIME_TOOL = 1;// 隐藏工具栏
    private static final int MSG_VIEWPAGER_NOTIFY = 2; //刷新ViewPager

    @SuppressLint("HandlerLeak")
    private Handler timeUpdateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_TIME_PLAY:
                    if (Constant.DEBUG)
                        Log.i(Constant.TAG_USB_PICTURE, "timeUpdateHandler");
                    if (mPictureViewPager == null || mPictureList == null)
                        return;
                    int i = mPictureViewPager.getCurrentItem();
                    if (mPictureList.getCount() == 0 || mPictureList.getCount() == 1) {
                        removeUpdate();
                        return;
                    }
                    if (i < 0 || i + 1 >= mMaxSize) {
                        mPictureViewPager.setCurrentItem(getStartPageIndex(mPictureList.getCount()) + 1, false);
                        mPictureViewPager.setCurrentItem(getStartPageIndex(mPictureList.getCount()), true);
                    } else {
                        mPictureViewPager.setCurrentItem(i + 1);
                    }
                    timeUpdateHandler.sendEmptyMessageDelayed(MSG_TIME_PLAY, 4000);
                    break;
                case MSG_TIME_TOOL:
                    if (mPictureControlView.getVisibility() == View.VISIBLE) {
                        setToolVisible(View.GONE);
                    } else {
                        setToolVisible(View.VISIBLE);
                    }
                    break;
                case MSG_VIEWPAGER_NOTIFY:
                    mPagerAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }

        }
    };

    // 停止图片播放
    private void removeUpdate() {
        if (Constant.DEBUG)
            Log.i(Constant.TAG_USB_PICTURE, "removeUpdate");

        if (!mPictureIsPlaying)
            return;

        mPictureIsPlaying = false;
        ivPicturePlay.setImageResource(R.drawable.btn_music_play);
        if (timeUpdateHandler == null || mPictureBottomBar == null)
            return;
        timeUpdateHandler.removeMessages(MSG_TIME_PLAY);

    }

    // 图片播放
    private void startUpdate() {
        if (Constant.DEBUG)
            Log.i(Constant.TAG_USB_PICTURE, "startUpdate");

        mPictureIsPlaying = true;
        ivPicturePlay.setImageResource(R.drawable.btn_music_pause);
        if (timeUpdateHandler == null || mPictureBottomBar == null)
            return;
        timeUpdateHandler.removeMessages(MSG_TIME_PLAY);
        timeUpdateHandler.sendEmptyMessageDelayed(MSG_TIME_PLAY, 4000);
        timeUpdateHandler.removeMessages(MSG_TIME_TOOL);
        timeUpdateHandler.sendEmptyMessageDelayed(MSG_TIME_TOOL, 3000);
    }

    /**
     * 工具栏的显示/隐藏
     *
     * @param visible 工具栏当前是否可见
     */
    private void setToolVisible(int visible) {

        if (visible == View.VISIBLE) {

            mPictureControlView.setVisibility(View.VISIBLE);
            tvFileName.setVisibility(View.VISIBLE);
            mPictureControlView.startAnimation(AnimationUtils.loadAnimation(UsbPictureActivity.this, R.anim.popup_show));
            tvFileName.startAnimation(AnimationUtils.loadAnimation(UsbPictureActivity.this, R.anim.popup_show_up));
            timeUpdateHandler.removeMessages(MSG_TIME_TOOL);
            timeUpdateHandler.sendEmptyMessageDelayed(MSG_TIME_TOOL, 3000);

        } else {
            timeUpdateHandler.removeMessages(MSG_TIME_TOOL);
            mPictureControlView.startAnimation(AnimationUtils.loadAnimation(UsbPictureActivity.this, R.anim.popup_hide));
            tvFileName.startAnimation(AnimationUtils.loadAnimation(UsbPictureActivity.this, R.anim.popup_hide_up));
            mPictureControlView.setVisibility(View.GONE);
            tvFileName.setVisibility(View.GONE);
        }

    }

    private final ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        /**
         * 此方法是页面跳转完后得到调用，arg0是你当前选中的页面的Position（位置编号）
         */
        @Override
        public void onPageSelected(int arg0) {
            if (Constant.DEBUG)
                Log.i(Constant.TAG_USB_PICTURE, "mPager onPageSelected : " + arg0);

            if (mPictureList.getCount() != 0) {
                int posInList = (arg0 + mOffset) % mPictureList.getCount();
                if (mPictureAdapter.getItem(posInList) != null) {
                    mLastPlayPath = mPictureAdapter.getItem(posInList).mFilePath;
                    mPictureAdapter.setmNowPlayPosition(posInList);
                } else
                    mLastPlayPath = null;
            }
            tvFileName.setText(Utils.getNameFromFilename(Utils.getExtFromFilename(mLastPlayPath)));
            // 切换图片后保存当前图片
            Utils.savePlayingPicturePath(UsbPictureActivity.this, mLastPlayPath);
        }

        /**
         * 当页面在滑动的时候会调用此方法，在滑动被停止之前，此方法回一直得到 调用。其中三个参数的含义分别为：
         *
         * @param arg0
         *            :当前页面，及你点击滑动的页面
         * @param arg1
         *            :当前页面偏移的百分比
         * @param arg2
         *            :当前页面偏移的像素位置
         */

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        /**
         * 此方法是在状态改变的时候调用
         *
         * @param arg0
         *            这个参数有三种状态 （0，1，2） 当页面开始滑动的时候，三种状态的变化顺序为（1，2，0）
         */
        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

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
                    mStatusBarInfo.setCurrentPageName("usb_picture");
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

}
