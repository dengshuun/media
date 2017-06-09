package com.hwatong.comnon;


import com.hwatong.comnon.FolderFragment.Type;
import com.hwatong.media.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FolderListAdapter extends BaseAdapter{
	
	private String[] item = null ;
	private Context mContext ;
	private Type mType ;

	public FolderListAdapter(String[] strings , Context context, Type type) {
		super();
		this.item = strings;
		this.mContext = context ;
		this.mType = type ;
	}

	@Override
	public int getCount() {
		if(item!=null){
			return item.length;
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		if(item!=null){
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
			
			if(mType == Type.MUSIC){
				
			} else if(mType == Type.PICTURE){
				
			} else if(mType == Type.VEDIO){
				
			}
			
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		return convertView;
	}
	
	public class ViewHolder{
		public ImageView type ;
		public TextView name ;
	}

}
