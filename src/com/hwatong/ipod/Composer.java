package com.hwatong.ipod;

import android.os.Parcel;
import android.os.Parcelable;

public class Composer implements Parcelable {
	public final String mName;
	public final String mId;

	public Composer(String name, String id){
		mName = name;
		mId = id;
	}

	public static final Parcelable.Creator<Composer> CREATOR = new Parcelable.Creator<Composer>() {
		public Composer createFromParcel(Parcel in) {
			return new Composer(in);
		}

		public Composer[] newArray(int size) {
			return new Composer[size];
		}
	};

	private Composer(Parcel in) {
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
