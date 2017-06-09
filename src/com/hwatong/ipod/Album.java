package com.hwatong.ipod;

import android.os.Parcel;
import android.os.Parcelable;

public class Album implements Parcelable {
	public final String mTitle;
	public final String mId;

	public Album(String title, String id){
		mTitle = title;
		mId = id;
	}

	public static final Parcelable.Creator<Album> CREATOR = new Parcelable.Creator<Album>() {
		public Album createFromParcel(Parcel in) {
			return new Album(in);
		}

		public Album[] newArray(int size) {
			return new Album[size];
		}
	};

	private Album(Parcel in) {
		mTitle = in.readString();
		mId = in.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeString(mTitle);
		dest.writeString(mId);
	}
}
