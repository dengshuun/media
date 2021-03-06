package com.hwatong.ipod.ui;

import java.util.List;

import com.hwatong.ipod.Album;
import com.hwatong.ipod.Artist;
import com.hwatong.ipod.Genre;
import com.hwatong.ipod.Playlist;
import com.hwatong.media.common.Constant;
import com.hwatong.media.common.R;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class IPodFolderFragment extends Fragment implements OnClickFolderListener {
	IPodMainActivity mIPodMainActivity = null;
	/**
	 * 路径
	 */
	private String mPath = Constant.IPOD_ROOT_DIR_PATH;
	private TextView mPathView;
	private ListView mListView;
	private View mView;
	private IPodFolderListAdapter mAdapter;
	private String[] mContentUI = { Constant.PLAY_LIST, Constant.GENRE, Constant.ARTIST, Constant.ALBUM };
	private List<?> mContentData = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		mView = inflater.inflate(R.layout.fragment_folder, container, false);
		mPathView = (TextView) mView.findViewById(R.id.folder_path);
		mListView = (ListView) mView.findViewById(R.id.list_folder);
		mListView.setSelector(R.drawable.media_list_item_selector);
		update(mPath);
		return mView;
	}

	@Override
	public void onResume() {
		super.onResume();
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (isRootFolder()) {
					mPath = mPath + mAdapter.getItem(position);
					update(mPath);
				} else {
					Object obj = mContentData.get(position);
					if (obj instanceof Artist) {
						mIPodMainActivity.setPlayList(getString(R.string.ARTIST), ((Artist) obj).mId);
					} else if (obj instanceof Album) {
						mIPodMainActivity.setPlayList(getString(R.string.ALBUM), ((Album) obj).mId);
					} else if (obj instanceof Genre) {
						mIPodMainActivity.setPlayList(getString(R.string.GENRE), ((Genre) obj).mId);
					} else if (obj instanceof Playlist) {
						mIPodMainActivity.setPlayList(getString(R.string.PLAY_LIST), ((Playlist) obj).mId);
					}
				}
				mIPodMainActivity.onRoot();
			}
		});
	}

	/**
	 * 通过路径更新主界面
	 * 
	 * @param path
	 */
	private void update(String path) {
		if (isRootFolder()) {
			mContentUI = new String[] { getString(R.string.PLAY_LIST), getString(R.string.GENRE), getString(R.string.ARTIST), getString(R.string.ALBUM) };
		} else {
			String type = mPath.split("//")[1];
			if (type.equals(getString(R.string.PLAY_LIST))) {
				mContentData = mIPodMainActivity.getPlayList();
				if (mContentData == null) {
					mContentUI = new String[0];
				} else {
					mContentUI = new String[mContentData.size()];
					for (int i = 0; i < mContentData.size(); i++) {
						Playlist playlist = (Playlist) mContentData.get(i);
						mContentUI[i] = playlist.mName;
					}
				}
			} else if (type.equals(getString(R.string.GENRE))) {
				mContentData = mIPodMainActivity.getGenreList();
				if (mContentData == null) {
					mContentUI = new String[0];
				} else {
					mContentUI = new String[mContentData.size()];
					for (int i = 0; i < mContentData.size(); i++) {
						Genre genre = (Genre) mContentData.get(i);
						if (genre.mName == null || genre.mName.equals(" ")) {
							mContentUI[i] = getResources().getString(R.string.music_unknown);
						} else {
							mContentUI[i] = genre.mName;
						}
					}
				}
			} else if (type.equals(getString(R.string.ARTIST))) {
				mContentData = mIPodMainActivity.getArtistList();
				if (mContentData == null) {
					mContentUI = new String[0];
				} else {
					mContentUI = new String[mContentData.size()];
					for (int i = 0; i < mContentData.size(); i++) {
						Artist artist = (Artist) mContentData.get(i);
						if (artist.mName == null || artist.mName.equals(" ")) {
							mContentUI[i] = getResources().getString(R.string.music_unknown);
						} else {
							mContentUI[i] = artist.mName;
						}
					}
				}
			} else if (type.equals(getString(R.string.ALBUM))) {
				mContentData = mIPodMainActivity.getAlbumList();
				if (mContentData == null) {
					mContentUI = new String[0];
				} else {
					mContentUI = new String[mContentData.size()];
					for (int i = 0; i < mContentData.size(); i++) {
						Album album = (Album) mContentData.get(i);
						Log.d("111", "mTitle :" + album.mTitle + ".");
						if (album.mTitle == null || album.mTitle.equals(" ")) {
							mContentUI[i] = getResources().getString(R.string.music_unknown);
						} else {
							mContentUI[i] = album.mTitle;
						}
					}
				}
			}
		}
		mAdapter = new IPodFolderListAdapter(mContentUI, getActivity());
		mListView.setAdapter(mAdapter);
		mPathView.setText(mPath);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mIPodMainActivity = (IPodMainActivity) activity;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onClickFolder() {
		if (isRootFolder()) {
			mIPodMainActivity.onRoot();
			return true;
		}
		mPath = Constant.IPOD_ROOT_DIR_PATH;
		update(mPath);
		mIPodMainActivity.onRoot();
		return false;
	}

	/**
	 * 是否是根目录
	 * 
	 * @return
	 */
	public boolean isRootFolder() {
		return mPath.equals(Constant.IPOD_ROOT_DIR_PATH);
	}

}
