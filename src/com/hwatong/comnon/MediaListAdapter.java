package com.hwatong.comnon;

import java.util.List;

import com.hwatong.media.R;



import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MediaListAdapter extends BaseAdapter{
	/**
	 * 所有的item
	 */
	private List<String> items = null ;
	private Context mContext = null;
	/**
	 * 正在播放的位置
	 */
	private int mPayingPostion = -1 ;
	
	public MediaListAdapter(List<String> items , Context context) {
		this.items = items;
		this.mContext  = context ;
	}
	
	@Override
	public int getCount() {
		if(items!=null){
			return items.size();
		}
		return 0;
	}
	
	@Override
	public Object getItem(int position) {
		if(items!=null){
			return items.get(position);
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
			convertView = LayoutInflater.from(mContext).inflate(R.layout.media_list, parent, false);
			viewHolder.name = (TextView) convertView.findViewById(R.id.item);
			viewHolder.isPlaying = (ImageView) convertView.findViewById(R.id.playing);
			
			if(position == mPayingPostion){
				/**
				 * 播放的样式
				 */
				viewHolder.isPlaying.setVisibility(View.VISIBLE);
				viewHolder.name.setTextColor(Color.RED);
			} else {
				/**
				 * 没有播放的样式
				 */
				viewHolder.isPlaying.setVisibility(View.INVISIBLE);
				viewHolder.name.setTextColor(Color.WHITE);
			}
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		return convertView;
	}
	
	public void setPlaying(int postion){
		mPayingPostion = postion ;
	}
	
	public class ViewHolder{
		public TextView name ;
		public ImageView isPlaying ;
	}
}
