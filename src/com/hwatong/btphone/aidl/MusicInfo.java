package com.hwatong.btphone.aidl;

import android.os.Parcel;
import android.os.Parcelable;

public class MusicInfo implements Parcelable {
	/***
	 * 歌曲名字
	 */
	public String name;
	/***
	 * 艺术家
	 */
	public String artist;
	/***
	 * 当前歌曲在总歌曲中的位置
	 */
	public int pos;
	/***
	 * 总的歌曲数目
	 */
	public int total;
	/***
	 * 歌曲时长
	 * 单位 ms
	 */
	public int duration;

	public MusicInfo() {
	}

	public MusicInfo(String name,String artist,int duration,int pos,int total){
		this.name = name;
		this.artist = artist;
		this.duration = duration;
		this.pos = pos;
		this.total = total;
	}

	public static final Parcelable.Creator<MusicInfo> CREATOR = new Parcelable.Creator<MusicInfo>() {
		public MusicInfo createFromParcel(Parcel in) {
			return new MusicInfo(in);
		}

		public MusicInfo[] newArray(int size) {
			return new MusicInfo[size];
		}
	};

	private MusicInfo(Parcel in) {
		readFromParcel(in);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeString(name);
		dest.writeString(artist);
		dest.writeInt(pos);
		dest.writeInt(total);
		dest.writeInt(duration);
	}

	public void readFromParcel(Parcel in) {
		name = in.readString();
		artist = in.readString();
		pos = in.readInt();
		total = in.readInt();
		duration = in.readInt();
	}
}
