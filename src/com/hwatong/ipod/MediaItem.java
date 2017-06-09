package com.hwatong.ipod;

import android.os.Parcel;
import android.os.Parcelable;

public class MediaItem implements Parcelable {
	public final String mTitle;
	public final String mId;

	public MediaItem(String title, String id){
		mTitle = title;
		mId = id;
	}

	public static final Parcelable.Creator<MediaItem> CREATOR = new Parcelable.Creator<MediaItem>() {
		public MediaItem createFromParcel(Parcel in) {
			return new MediaItem(in);
		}

		public MediaItem[] newArray(int size) {
			return new MediaItem[size];
		}
	};

	private MediaItem(Parcel in) {
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
