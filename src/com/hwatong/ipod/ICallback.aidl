package com.hwatong.ipod;

oneway interface ICallback {
	void onAttached();
    void onDetached();
	void onMediaItemReceived();
	void onMediaPlaylistReceived();
	void onNowPlayingReceived();
}