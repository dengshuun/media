package com.hwatong.usbpicture;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.hwatong.media.PictureEntry;
import com.hwatong.media.common.MediaFile;
import com.hwatong.media.common.R;
import com.hwatong.media.common.Utils;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class PictureAdapter extends BaseAdapter {
	private final Context mContext;
	private String mFilePath;
	private int mNowPlayPosition;

	private final List<PictureEntry> mPictureDataList = new ArrayList<PictureEntry>();

	public PictureAdapter(Context context, String filePath, int postion) {
		mContext = context;
		mFilePath = filePath;
		mNowPlayPosition = postion;
		fillList(mFilePath);
	}

	public void setmNowPlayPosition(int position) {
		this.mNowPlayPosition = position;
		notifyDataSetChanged();
	}

	public void notifyData(String filePath) {
		fillList(filePath);
		notifyDataSetChanged();
	}

	private void fillList(String filePath) {
		mPictureDataList.clear();

		File file = new File(filePath);
		final File[] files = file.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				String path = files[i].toString();

				MediaFile.MediaFileType fileType = MediaFile.getFileType(path);
				if (fileType != null && MediaFile.isImageFileType(fileType.fileType)) {
					mPictureDataList.add(new PictureEntry(path));
				}
			}
		}
	}

	public int getPictureListPosition(String path) {
		if (path == null || path.isEmpty())
			return -1;

		int size = mPictureDataList.size();
		for (int i = 0; i < size; i++) {
			PictureEntry e = (PictureEntry) mPictureDataList.get(i);
			if (e.mFilePath.equals(path))
				return i;
		}
		return -1;
	}

	public String getmFilePath() {
		return mFilePath;
	}

	@Override
	public int getCount() {
		return mPictureDataList.size();
	}

	@Override
	public PictureEntry getItem(int i) {
		if (i < 0 || i >= mPictureDataList.size())
			return null;
		return mPictureDataList.get(i);
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
			myViewHolder.mName = (TextView) convertView.findViewById(R.id.item);
			myViewHolder.isPlaying = (ImageView) convertView.findViewById(R.id.playing);
			convertView.setTag(myViewHolder);
		} else
			myViewHolder = (ViewHolder) convertView.getTag();

		if (position == mNowPlayPosition) {
			myViewHolder.isPlaying.setVisibility(View.VISIBLE);
			myViewHolder.mName.setSelected(true);
			myViewHolder.mName.setEllipsize(TextUtils.TruncateAt.MARQUEE);
			myViewHolder.mName.setTextColor(mContext.getResources().getColor(R.color.red));
		} else {
			myViewHolder.isPlaying.setVisibility(View.GONE);
			myViewHolder.mName.setSelected(false);
			myViewHolder.mName.setEllipsize(TextUtils.TruncateAt.END);
			myViewHolder.mName.setTextColor(mContext.getResources().getColor(R.color.white));
		}

		PictureEntry e = mPictureDataList.get(position);
		String title = Utils.getExtFromFilename(e.mFilePath);
		myViewHolder.mName.setText(Utils.getNameFromFilename(title));

		return convertView;
	}

	private class ViewHolder {
		private TextView mName;
		private ImageView isPlaying;
	}

}
