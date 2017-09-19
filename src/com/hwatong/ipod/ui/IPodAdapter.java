package com.hwatong.ipod.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hwatong.ipod.MediaItem;
import com.hwatong.ipod.NowPlaying;
import com.hwatong.media.common.R;

public class IPodAdapter extends BaseAdapter {
	private final Context mContext;
	NowPlaying mNowPlaying = null;
	List<MediaItem> list = new ArrayList<MediaItem>();
	/**
	 * 被选中的item
	 */
	private int mSelectedIndex = -1;

	public IPodAdapter(Context conrext, List<MediaItem> list) {
		mContext = conrext;
		this.list = list;
	}

	public void setSelected(int selectedIndex) {
		this.mSelectedIndex = selectedIndex;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int i) {
		if (i < 0 || i >= list.size())
			return null;
		return list.get(i);
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	/**
	 * 设置当前正在播放的歌曲
	 * 
	 * @param nowPlaying
	 */
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

		if (mNowPlaying != null && list.get(position).mId == mNowPlaying.mId || mSelectedIndex == position) {
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
		myViewHolder.mName.setText(list.get(position).mTitle);
		return convertView;
	}

	private class ViewHolder {
		private TextView mName;
		private ImageView isPlaying;
	}
}
