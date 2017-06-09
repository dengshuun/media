package com.hwatong.btphone.aidl;

import android.os.Parcel;
import android.os.Parcelable;

public class BtDevice implements Parcelable {

	public static final int STATE_UNKNOWN = 0x00;
	public static final int STATE_NOTCONNECTED = 0x01;
	public static final int STATE_PAIRED = 0x02;
	public static final int STATE_CONNECTING = 0x03;
	public static final int STATE_CONNECTED = 0x04;
	public static final int STATE_DISCONNECTING = 0x05;

	public String name = "";
	public String addr = "";
	public int state = STATE_UNKNOWN;

	public BtDevice() {
	}
	public BtDevice(String name, String addr, int state) {
		this.name = name;
		this.addr = addr;
		this.state = state;
	}

	public static final Parcelable.Creator<BtDevice> CREATOR = new Parcelable.Creator<BtDevice>() {
		public BtDevice createFromParcel(Parcel in) {
			return new BtDevice(in);
		}

		public BtDevice[] newArray(int size) {
			return new BtDevice[size];
		}
	};

	private BtDevice(Parcel in) {
		readFromParcel(in);
	}	

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeString(name);
		dest.writeString(addr);
		dest.writeInt(state);
	}

	public void readFromParcel(Parcel in) {
		name = in.readString();
		addr = in.readString();
		state = in.readInt();
	}

	@Override
	public boolean equals(Object o) {
		if(this == o)
			return true;
		if(o == null)
			return false;
		if(getClass() != o.getClass())
			return false;
		BtDevice other = (BtDevice) o;
		if(addr.equals(other.addr)) {
			return true;
		}

		return false;
	}
}
