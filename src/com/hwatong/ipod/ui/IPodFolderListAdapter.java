package com.hwatong.ipod.ui;

import com.hwatong.media.common.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class IPodFolderListAdapter extends BaseAdapter{
	private String[] item = null ;
	private Context mContext ;

	public IPodFolderListAdapter(String[] strings , Context context) {
		super();
		this.item = strings;
		this.mContext = context ;
	}

	@Override
	public int getCount() {
		if(item!=null){
			return item.length;
		}
		return 0;
	}

	@Override
	public String getItem(int position) {
		if(item!=null){
			return item[position];
		}		
		return null;
	}

	@Override
	public long getItemId(int position) {	
		return position;
	}
	
    /**
     * 
     */
	public void onClickFloder(){
		
	}
	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.ipod_folder_list, parent, false);
			convertView.setTag(viewHolder);
			viewHolder.name = (TextView) convertView.findViewById(R.id.folder_item_name);
			viewHolder.name.setText(item[position]);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		return convertView;
	}
	
	public class ViewHolder{
		public TextView name ;
	}
}
