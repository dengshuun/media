package com.hwatong.media.common;

import java.io.File;

import com.hwatong.media.common.FolderFragment.Type;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FolderGridAdapter extends BaseAdapter {

	private File[] item = null;
	private Context mContext;
	private Type mType;
	private int mSelectedIndex = -1;
	ThumbnailsLoader mThumbnailsLoader;

	public void setmSelectedIndex(int index) {
		this.mSelectedIndex = index;
		notifyDataSetChanged();
	}

	public FolderGridAdapter(File[] files, Context context, Type type) {
		super();
		this.item = files;
		this.mContext = context;
		this.mType = type;
		mThumbnailsLoader = new ThumbnailsLoader(context);
	}

	@Override
	public int getCount() {
		if (item != null) {
			return item.length;
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		if (item != null) {
			return item[position];
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.folder_grid_item, parent, false);
			viewHolder.type = (ImageView) convertView.findViewById(R.id.grid_icon);
			viewHolder.name = (TextView) convertView.findViewById(R.id.grid_name);
			viewHolder.frame = (ImageView) convertView.findViewById(R.id.folder_grid_frame);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		if (position == mSelectedIndex) {
			viewHolder.frame.setBackgroundResource(R.drawable.picture_grid_frame);
		} else {
			viewHolder.frame.setBackgroundResource(R.color.transparent);
		}

		File file = item[position];
		if (file.isDirectory()) {
			viewHolder.type.setImageResource(R.drawable.folder_icon_normal);
			viewHolder.name.setText(file.getName());
		} else if (mType == Type.PICTURE) {
			mThumbnailsLoader.loadBitmap(file.toString(), R.drawable.picture_grid_icon, viewHolder.type);
		}

		return convertView;
	}

	public class ViewHolder {
		public ImageView type;
		public TextView name;
		public ImageView frame;
	}
}
