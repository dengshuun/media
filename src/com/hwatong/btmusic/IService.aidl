package com.hwatong.btmusic;

import com.hwatong.btmusic.BtDevice;
import com.hwatong.btmusic.ICallback;
import com.hwatong.btmusic.NowPlaying;

interface IService {
	void registerCallback(ICallback callback);
	void unregisterCallback(ICallback callback);	

	BtDevice getConnectDevice();
	
    NowPlaying getNowPlaying();
	
	void playPause();
	void play();
	void pause();
	void stop();
	void previous();
	void next();
}
