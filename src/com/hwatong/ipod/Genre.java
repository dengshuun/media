package com.hwatong.ipod;

import android.os.Parcel;
import android.os.Parcelable;

public class Genre implements Parcelable {
	public final String mName;
	public final String mId;

	public Genre(String name, String id){
		mName = name;
		mId = id;
	}

	public static final Parcelable.Creator<Genre> CREATOR = new Parcelable.Creator<Genre>() {
		public Genre createFromParcel(Parcel in) {
			return new Genre(in);
		}

		public Genre[] newArray(int size) {
			return new Genre[size];
		}
	};

	private Genre(Parcel in) {
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
