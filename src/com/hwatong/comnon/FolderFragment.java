package com.hwatong.comnon;


import java.io.File;
import com.hwatong.media.R;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class FolderFragment extends Fragment {
	
	private Context mContext ;
	/**
	 * 默认类型music
	 */
	private Type type = Type.MUSIC ;
	/**
	 * 路径
	 */
	private String mPath = Constant.ROOT_DIR_PATH;

	private TextView mPathView ;
	private ListView mListView ;
	private View mView ;
	private FolderListAdapter mAdapter;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mContext = getActivity();
		mView = inflater.inflate(R.layout.fragment_folder, container,false);
		mPathView = (TextView) mView.findViewById(R.id.folder_path);
		mListView = (ListView) mView.findViewById(R.id.list_folder);
		update();
		return mView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				String path = (String) mAdapter.getItem(position);
				if(new File(path).isDirectory()){
					mPath = path ;
					update();
				}
			}
		});
	}
	
	private void update() {
		mAdapter = new FolderListAdapter(new File(mPath).list(), mContext , type);
		mListView.setAdapter(mAdapter);
		mPathView.setText(mPath);
		mAdapter.notifyDataSetChanged();
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

    /**
     * 枚举所有类型
     */
	public enum Type{
		PICTURE ,
		VEDIO ,
		MUSIC
	}
	
}
