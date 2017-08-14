package com.hwatong.media.common;

import java.io.File;
import java.io.FileFilter;

import com.hwatong.usbmusic.UsbMusicActivity;
import com.hwatong.usbpicture.UsbPictureActivity;
import com.hwatong.usbvideo.UsbVideoActivity;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

public class FolderFragment extends Fragment {

	UsbMusicActivity mUsbMusicActivity = null;
	UsbVideoActivity mUsbVideoActivity = null;
	UsbPictureActivity mUsbPictureActivity = null;

	private Context mContext;
	/**
	 * 默认类型music
	 */
	private Type type = Type.MUSIC;
	/**
	 * 路径
	 */
	private String mPath = Constant.ROOT_DIR_PATH;

	private TextView mPathView;
	private ListView mListView;
	private GridView mGridView;
	private View mView;
	private FolderListAdapter mListAdapter;
	private FolderGridAdapter mGridAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mContext = getActivity();
		mView = inflater.inflate(R.layout.fragment_folder, container, false);
		mPathView = (TextView) mView.findViewById(R.id.folder_path);
		mListView = (ListView) mView.findViewById(R.id.list_folder);
		mGridView = (GridView) mView.findViewById(R.id.grid_folder);
		update();
		return mView;
	}

	@Override
	public void onResume() {
		super.onResume();

		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				File file = (File) mListAdapter.getItem(position);
				if (file.isDirectory()) {
					mPath = file.toString();
					update();
					if (mUsbMusicActivity != null) {
						mUsbMusicActivity.onFolderItemClick(file);
					} else if (mUsbPictureActivity != null) {
						mUsbPictureActivity.onFolderItemClick(file);
					} else if (mUsbVideoActivity != null) {
						mUsbVideoActivity.onFolderItemClick(file);
					}
				} else {
					if (mUsbMusicActivity != null) {
						mUsbMusicActivity.onFolderItemClick(file);
					} else if (mUsbPictureActivity != null) {
						mUsbPictureActivity.onFolderItemClick(file);
					} else if (mUsbVideoActivity != null) {
						mUsbVideoActivity.onFolderItemClick(file);
					}
				}
			}
		});

		mGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				File file = (File) mGridAdapter.getItem(position);
				mGridAdapter.setmSelectedIndex(position);
				if (file.isDirectory()) {
					mPath = file.toString();
					update();
					mUsbPictureActivity.onFolderItemClick(file);
				} else {
					mUsbPictureActivity.onFolderItemClick(file);
				}
			}

		});
	}

	private void update() {
		mListAdapter = new FolderListAdapter(getListFiles(), mContext, type);
		mListView.setAdapter(mListAdapter);
		mListView.setSelector(R.drawable.folder_item_selector);
		mPathView.setText(mPath.replace(mPath.contains(Constant.ROOT_DIR_PATH + "/") ? Constant.ROOT_DIR_PATH + "/" : Constant.ROOT_DIR_PATH,
				Constant.IPOD_ROOT_DIR_PATH));
		mListAdapter.notifyDataSetChanged();
		if (type.equals(Type.PICTURE)) {
			mGridAdapter = new FolderGridAdapter(getListFiles(), mContext, type);
			mGridView.setAdapter(mGridAdapter);
			mGridAdapter.notifyDataSetChanged();
		}
	}

	/**
	 * 根据Type的类型，返回对应的文件列表
	 * 
	 * @return
	 */
	private File[] getListFiles() {
		File[] files = null;
		File file = new File(mPath);
		if (file.exists()) {
			files = file.listFiles(new FileFilter() {

				@Override
				public boolean accept(File pathname) {
					MediaFile.MediaFileType fileType = MediaFile.getFileType(pathname.getName());
					Log.d("sss", "fileType " + fileType);
					if (fileType == null) {
						return pathname.isDirectory();
					} else if (type.equals(Type.PICTURE)) {
						return MediaFile.isImageFileType(fileType.fileType);
					} else if (type.equals(Type.VEDIO)) {
						return MediaFile.isVideoFileType(fileType.fileType);
					} else if (type.equals(Type.MUSIC)) {
						return MediaFile.isAudioFileType(fileType.fileType);
					}
					return false;
				}
			});
		}
		return files;
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onAttach(Activity activity) {
		if (activity instanceof UsbMusicActivity) {
			mUsbMusicActivity = (UsbMusicActivity) activity;
		} else if (activity instanceof UsbPictureActivity) {
			mUsbPictureActivity = (UsbPictureActivity) activity;
		} else if (activity instanceof UsbVideoActivity) {
			mUsbVideoActivity = (UsbVideoActivity) activity;
		}
		super.onAttach(activity);
	}

	@Override
	public void onDetach() {
		if (mUsbMusicActivity != null) {
			mUsbMusicActivity = null;
		} else if (mUsbPictureActivity != null) {
			mUsbPictureActivity = null;
		} else if (mUsbVideoActivity != null) {
			mUsbVideoActivity = null;
		}
		super.onDetach();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	public String getPath() {
		return mPath;
	}

	public void setPath(String path) {
		this.mPath = path;
		update();
	}

	// 处理圖片縮略圖的事件
	public void setListOrThumbnails(Boolean isThumbnails) {
		Log.e("sss", " " + isThumbnails);
		if (!isThumbnails) {
			mListView.setVisibility(View.GONE);
			mGridView.setVisibility(View.VISIBLE);
		} else {
			mListView.setVisibility(View.VISIBLE);
			mGridView.setVisibility(View.GONE);
		}
	}

	/**
	 * 枚举所有类型
	 */
	public enum Type {
		PICTURE, VEDIO, MUSIC
	}

}
