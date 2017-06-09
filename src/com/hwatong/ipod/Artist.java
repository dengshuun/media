package com.hwatong.ipod;

import android.os.Parcel;
import android.os.Parcelable;

public class Artist implements Parcelable {
	public final String mName;
	public final String mId;

	public Artist(String name, String id){
		mName = name;
		mId = id;
	}

	public static final Parcelable.Creator<Artist> CREATOR = new Parcelable.Creator<Artist>() {
		public Artist createFromParcel(Parcel in) {
			return new Artist(in);
		}

		public Artist[] newArray(int size) {
			return new Artist[size];
		}
	};

	private Artist(Parcel in) {
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
