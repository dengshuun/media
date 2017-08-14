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

public class FolderListAdapter extends BaseAdapter {

	private File[] item = null;
	private Context mContext;
	private Type mType;

	public FolderListAdapter(File[] files, Context context, Type type) {
		super();
		this.item = files;
		this.mContext = context;
		this.mType = type;
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
			convertView = LayoutInflater.from(mContext).inflate(R.layout.folder_list, parent, false);
			viewHolder.type = (ImageView) convertView.findViewById(R.id.folder_item_icon);
			viewHolder.name = (TextView) convertView.findViewById(R.id.folder_item_name);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		File file = item[position];

		int drawableId = 0;
		if (file.isDirectory()) {
			drawableId = R.drawable.folder_icon_normal;
		} else if (mType == Type.MUSIC) {
			drawableId = R.drawable.music_icon_small;
		} else if (mType == Type.PICTURE) {
			drawableId = R.drawable.picture_icon_small;
		} else if (mType == Type.VEDIO) {
			drawableId = R.drawable.picture_icon_small;
		}

		if (drawableId != 0) {
			viewHolder.type.setImageResource(drawableId);
		}
		viewHolder.name.setText(file.getName());

		return convertView;
	}

	public class ViewHolder {
		public ImageView type;
		public TextView name;
	}

}
