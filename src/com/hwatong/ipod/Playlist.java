package com.hwatong.ipod;

import android.os.Parcel;
import android.os.Parcelable;

public class Playlist implements Parcelable {
	public final String mName;
	public final String mId;

	public Playlist(String name, String id){
		mName = name;
		mId = id;
	}

	public static final Parcelable.Creator<Playlist> CREATOR = new Parcelable.Creator<Playlist>() {
		public Playlist createFromParcel(Parcel in) {
			return new Playlist(in);
		}

		public Playlist[] newArray(int size) {
			return new Playlist[size];
		}
	};

	private Playlist(Parcel in) {
		mName = in.readString();
		mId = in.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeString(mName);
		dest.writeString(mId);
	}
}
