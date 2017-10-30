package com.hwatong.usbmusic;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hwatong.music.MusicEntry;
import com.hwatong.media.common.Constant;
import com.hwatong.media.common.MediaFile;
import com.hwatong.media.common.R;
import com.hwatong.media.common.Utils;
import com.hwatong.music.NowPlaying;

public class MusicAdapter extends BaseAdapter {
	private final Context mContext;
	String mFilePath;
	NowPlaying mNowPlaying = null;
	private List<MusicEntry> mMusicDataList = new ArrayList<MusicEntry>();

	public void setmMusicDataList(List<MusicEntry> mMusicDataList) {
		this.mMusicDataList = mMusicDataList;
	}

	private int mSelectedIndex = -1;

	public MusicAdapter(Context conrext, String filePath) {
		mContext = conrext;
		mFilePath = filePath;
		fillList(mFilePath);
	}

	public void notifyData(String filePath) {
		fillList(filePath);
		notifyDataSetChanged();
	}

	private void fillList(String filePath) {
		mMusicDataList.clear();

		File file = new File(filePath);
		final File[] list = file.listFiles();
		if (list != null) {
			for (int i = 0; i < list.length; i++) {
				String path = list[i].toString();
				String fileTitle = Utils.getExtFromFilename(path);
				Log.i(Constant.TAG_USB_MUSIC, "fileTitle:" + fileTitle);

				MediaFile.MediaFileType fileType = MediaFile.getFileType(path);
				if (fileType != null && MediaFile.isAudioFileType(fileType.fileType)) {
					mMusicDataList.add(new MusicEntry(path));
				}
			}
		}

		Log.i(Constant.TAG_USB_MUSIC, "fillList | musicList size " + mMusicDataList.size());
	}

	public void notifyNowPlayingReceived() {
		if (mNowPlaying != null) {
			int position = getSongListPosition(mNowPlaying.mId);
			if (mSelectedIndex != position) {
				mSelectedIndex = position;
				this.notifyDataSetChanged();
			}
		}
	}

	public int getSongListPosition(String path) {
		if (path == null || path.isEmpty())
			return -1;

		synchronized (mMusicDataList) {
			int size = mMusicDataList.size();
			for (int i = 0; i < size; i++) {
				MusicEntry s = mMusicDataList.get(i);
				if (s.mFilePath.equals(path))
					return i;
			}
		}

		return -1;
	}

	public void setSelectedIndex(int i) {
		mSelectedIndex = i;
		notifyDataSetChanged();
	}

	public int getSelectedIndex() {
		return mSelectedIndex;
	}

	@Override
	public int getCount() {
		return mMusicDataList.size();
	}

	@Override
	public Object getItem(int i) {
		if (i < 0 || i >= mMusicDataList.size())
			return null;
		return mMusicDataList.get(i);
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	public void setmNowPlaying(NowPlaying nowPlaying) {
		this.mNowPlaying = nowPlaying;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder myViewHolder = null;
		if (convertView == null) {
			myViewHolder = new ViewHolder();
			LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = mInflater.inflate(R.layout.media_list, parent, false);
			myViewHolder.mName = (TextView) convertView.findViewById(R.id.item);
			myViewHolder.isPlaying = (ImageView) convertView.findViewById(R.id.playing);
			convertView.setTag(myViewHolder);
		} else {
			myViewHolder = (ViewHolder) convertView.getTag();
		}

		if (position == mSelectedIndex) {
			myViewHolder.isPlaying.setVisibility(View.VISIBLE);
			myViewHolder.mName.setEllipsize(TextUtils.TruncateAt.MARQUEE);
			myViewHolder.mName.setTextColor(android.graphics.Color.parseColor("#ff0000"));
			myViewHolder.mName.setSelected(true);
		} else {
			myViewHolder.isPlaying.setVisibility(View.INVISIBLE);
			myViewHolder.mName.setEllipsize(TextUtils.TruncateAt.END);
			myViewHolder.mName.setTextColor(android.graphics.Color.parseColor("#ffffff"));
			myViewHolder.mName.setSelected(false);
		}

		final MusicEntry mMusicMap = mMusicDataList.get(position);
		myViewHolder.mName.setText(Utils.getNameFromFilename(Utils.getExtFromFilename(mMusicMap.mFilePath)));
		Log.d(Constant.TAG_USB_MUSIC, "mSongName:" + mMusicMap.mFilePath);

		return convertView;
	}

	private class ViewHolder {
		private TextView mName;
		private ImageView isPlaying;
	}
}
