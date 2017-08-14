package com.hwatong.usbvideo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.hwatong.media.VideoEntry;
import com.hwatong.media.common.MediaFile;
import com.hwatong.media.common.R;
import com.hwatong.media.common.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class VideoAdapter extends BaseAdapter {
	private final Context mContext;
	private String mFilePath;

	public String getmFilePath() {
		return mFilePath;
	}

	private final List<VideoEntry> mVideoList = new ArrayList<VideoEntry>();

	private int mSelectedIndex = -1;

	public VideoAdapter(Context context, String filePath) {
		mContext = context;
		mFilePath = filePath;
		fillList(filePath);
	}

	public void notifyData(String filePath) {
		fillList(filePath);
		notifyDataSetChanged();
	}

	private void fillList(String filePath) {
		mVideoList.clear();
		File file = new File(filePath);
		final File[] files = file.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				String path = files[i].toString();
				MediaFile.MediaFileType fileType = MediaFile.getFileType(path);
				if (fileType != null && MediaFile.isVideoFileType(fileType.fileType)) {
					mVideoList.add(new VideoEntry(path));
				}
			}
		}
	}

	@SuppressLint("NewApi")
	public int getPositionByPath(String path) {
		if (path == null || path.isEmpty())
			return -1;

		int size = mVideoList.size();
		for (int i = 0; i < size; i++) {
			VideoEntry s = mVideoList.get(i);
			if (s.mFilePath.equals(path))
				return i;
		}
		return -1;
	}

	public void setSelectedIndex(int i) {
		mSelectedIndex = i;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mVideoList.size();
	}

	@Override
	public Object getItem(int i) {
		if (i < 0 || i >= mVideoList.size())
			return null;
		return mVideoList.get(i);
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder myViewHolder = null;
		if (convertView == null) {
			myViewHolder = new ViewHolder();
			LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = mInflater.inflate(R.layout.media_list, parent, false);
			myViewHolder.mImage = (ImageView) convertView.findViewById(R.id.playing);
			myViewHolder.mText = (TextView) convertView.findViewById(R.id.item);
			convertView.setTag(myViewHolder);
		} else {
			myViewHolder = (ViewHolder) convertView.getTag();
		}

		if (position == mSelectedIndex) {
			myViewHolder.mImage.setVisibility(View.VISIBLE);
			myViewHolder.mText.setSelected(true);
			myViewHolder.mText.setEllipsize(TextUtils.TruncateAt.MARQUEE);
			myViewHolder.mText.setTextColor(mContext.getResources().getColor(R.color.red));
		} else {
			myViewHolder.mImage.setVisibility(View.GONE);
			myViewHolder.mText.setSelected(true);
			myViewHolder.mText.setEllipsize(TextUtils.TruncateAt.END);
			myViewHolder.mText.setTextColor(mContext.getResources().getColor(R.color.white));
		}

		final VideoEntry mVideoEntry = mVideoList.get(position);
		myViewHolder.mText.setText(Utils.getNameFromFilename(Utils.getExtFromFilename(mVideoEntry.mFilePath)));
		return convertView;
	}

	private class ViewHolder {
		private ImageView mImage;
		private TextView mText;
	}
}
