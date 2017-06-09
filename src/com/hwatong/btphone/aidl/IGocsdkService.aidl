package com.hwatong.btphone.aidl;

import com.hwatong.btphone.aidl.IGocsdkCallback;
import com.hwatong.btphone.aidl.BtDevice;
import com.hwatong.btphone.aidl.MusicInfo;
import android.os.Parcelable;

interface IGocsdkService {
	void registerCallback(IGocsdkCallback callback);
	void unregisterCallback(IGocsdkCallback callback);	
	
//setting
	void openBluetooth();

	void closeBluetooth();

	void restBluetooth();
	
	void getLocalName();

	void setLocalName(String name);

	void getPinCode();

	void setPinCode(String pincode);
	
	void getLocalAddress();
	
	void getAutoConnectAnswer();
	
	void setAutoConnect();
	
	void cancelAutoConnect();
	
	void setAutoAnswer();
	
	void cancelAutoAnswer();
	
	void getVersion();
	
	String getLocalDeviceName();
//connect info
	void setPairMode();
	
	void cancelPairMode();

	void connectLast();
	
	void connectDevice(String addr);
	
	void connectA2dp(String addr);

	void connectHFP(String addr);
	
	void connectHid(String addr);
	
	void connectSpp(String addr);

	void disconnect();

	void disconnectA2DP();

	void disconnectHFP();
	
	void disconnectHid();
	
	void disconnectSpp();
	
	boolean isConnected();
	
	BtDevice getConnectDevice();
	
	String getCurSignalBattery();

//devices list
	void deletePair(String addr);

	void startDiscovery();

	void getPairList();

	void stopDiscovery();
	
	List<BtDevice> getDiscoverDevices();
//hfp
	void phoneAnswer();

	void phoneReject();
	
	void phoneFinish();

	void phoneDail(String phonenum);

	void phoneTransmitDTMFCode(char code);
	
	void phoneTransfer();

	void phoneTransferBack();

	void phoneMicOpenClose();

	void phoneVoiceDail();
	
	void cancelPhoneVoiceDail();
	
	int getPhoneCallState();
	
	String getCurPhoneNumber();
	
	String getCurPersonName();

	long getPhoneTalkTime();
//contacts
	void phoneBookStartUpdate();

	void callLogstartUpdate(int type);	
//music
	void musicPlayOrPause();
	
	void musicPlay();
	void musicPause();
	void musicPauseCmd();
	
	void musicStop();

	void musicPrevious();

	void musicNext();
	
	void musicMute();
	
	void musicUnmute();
	
	void musicBackground();
	
	void musicNormal();	
	
	boolean isMusicPlaying();
	
	MusicInfo getCurMusicInfo();
//hid
	void hidMouseMove(String point);
	
	void hidMouseUp(String point);
	
	void hidMousDown(String point);
	
	void hidHomeClick();
	
	void hidBackClick();
	
	void hidMenuClick(); 	
	
//spp
	void sppSendData(String addr ,String data);	
	
	void getMusicInfo();
	
	void inqueryHfpStatus();
	
	void getCurrentDeviceAddr();
	
	void getCurrentDeviceName();
	
	void setProfileEnabled(in boolean[] enabled);
	void getProfileEnabled();
	
	void getMessageSentList();
	void getMessageDeletedList();
	void getMessageInboxList();
	void getMessageText(String handle);
}