package com.hwatong.media.common;


public class Constant {
	
	public static boolean DEBUG = true;
	
	public static String MEDIA_PLAY_STATUS = "com.hwatong.media.START";
	
	/**
	 * ipod回调状态，信息
	 */
	public static final String IPOD_SERVICE = "com.hwatong.ipod.service";
	public static final int MSG_NOWPLAYING_RECEIVED = 0;
	public static final int MSG_MEDIALIBRARYINFORMATION_RECEIVED = 1;
	public static final int MSG_MEDIAITEM_RECEIVED = 2;
	public static final int MSG_MEDIAPLAYLIST_RECEIVED = 3;
	public static final int MSG_START_FILETRANSFER = 100;
	public static final int MSG_STOP_FILETRANSFER = 101;
	public static final int MSG_PROBE_DEVICE = 20;
	public static final int MSG_REMOVE_DEVICE = 21;
	/**
	 * ipod TAG
	 */
	public static final String TAG_IPOD = "com.hwatong.ipod.ui";
	
	public static final String PLAY_LIST = "播放列表";

	public static final String GENRE = "类型";

	public static final String ARTIST = "艺术家";

	public static final String ALBUM = "专辑";
	
	
	/**
	 * 蓝牙服务
	 */
	public static String BTPHONE_SERVICE = "com.hwatong.btmusic.service";
	
	
	/**
	 * 蓝牙音乐回调状态，信息
	 */
	public static final int MSG_HFP_CONNECTED = 1;
	public static final int MSG_HFP_DISCONNECTED = 2;
	public static final int MSG_MUSIC_PLAYING = 3;
	public static final int MSG_MUSIC_STOP = 4;
	public static final int MSG_MUSIC_INFO = 5;
	/**
	 * bt music TAG
	 */
	public static final String TAG_BT_MUSIC = "com.hwatong.btmusic";

	/**
	 * Ipod根目录
	 */
	public static final String IPOD_ROOT_DIR_PATH = "根目录//";	
	
	/**
	 * 媒体根目录
	 */
	public static final String ROOT_DIR_PATH = "/mnt/udisk";
	public static final String MEDIA_ROOT_DIR_PATH = "根目录//";
	
	/**
	 * usb music TAG
	 */
	public static final String TAG_USB_MUSIC = "com.hwatong.usbmusic";
	
	/**
	 * usb picture TAG
	 */
	public static final String TAG_USB_PICTURE = "com.hwatong.usbpicture";
	
	/**
	 * usb video TAG
	 */
	public static final String TAG_USB_VIDEO = "com.hwatong.usbvideo";


}
