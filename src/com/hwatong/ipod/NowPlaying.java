package com.hwatong.ipod;

import android.os.Parcel;
import android.os.Parcelable;

public class NowPlaying implements Parcelable {
	public static final int PlaybackRepeatMode_Off = 0;
	public static final int PlaybackRepeatMode_One = 1;
	public static final int PlaybackRepeatMode_All = 2;

	public final String mId;
	public final String mTitle;
	public final String mAlbum;
	public final String mArtist;

	public final int mPlaybackDurationInMilliseconds;
	public final int mPlaybackElapsedTimeInMilliseconds;

	public final android.graphics.Bitmap mArtwork;

	public final int mPlaybackRepeatMode;
	public final boolean mPlaybackShuffleMode;

	public final boolean mPlaybackStatus;

	public static final Parcelable.Creator<NowPlaying> CREATOR = new Parcelable.Creator<NowPlaying>() {
		public NowPlaying createFromParcel(Parcel in) {
			return new NowPlaying(in);
		}

		public NowPlaying[] newArray(int size) {
			return new NowPlaying[size];
		}
	};

	private NowPlaying(Parcel in) {
		mId = in.readString();
		mTitle = in.readString();
		mAlbum = in.readString();
		mArtist = in.readString();
		mPlaybackDurationInMilliseconds = in.readInt();
		mPlaybackElapsedTimeInMilliseconds = in.readInt();
        if (in.readInt() != 0) {
			mArtwork = android.graphics.Bitmap.CREATOR.createFromParcel(in);
		} else {
			mArtwork = null;
		}
		mPlaybackRepeatMode = in.readInt();
		mPlaybackShuffleMode = in.readInt() != 0;

		mPlaybackStatus = in.readInt() != 0;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeString(mId);
		dest.writeString(mTitle);
		dest.writeString(mAlbum);
		dest.writeString(mArtist);
		dest.writeInt(mPlaybackDurationInMilliseconds);
		dest.writeInt(mPlaybackElapsedTimeInMilliseconds);
		if (mArtwork != null) {
			dest.writeInt(1);
			mArtwork.writeToParcel(dest, 0);
        } else {
            dest.writeInt(0);
        }
		dest.writeInt(mPlaybackRepeatMode);
		dest.writeInt(mPlaybackShuffleMode ? 1 : 0);
		dest.writeInt(mPlaybackStatus ? 1 : 0);
	}
}
