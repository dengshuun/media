package com.hwatong.media.common;

import android.content.Context;
import android.content.SharedPreferences;

public class VideoUtils {
	public static final String[] MEDIATYPE = { "3gp", "mp4", "avi", "flv", "mkv", "mov", "mpg" };
	public static final String SAVE_VIDEO_PROGRESS = "PlayingVideoProgress";
	public static final String SAVE_PLAY_VOLUME = "PlayVolume";
	public static final String SAVE_VIDEO_PATH = "PlayingSongPath";
	public static final String SAVE_LOOP_MODE = "LoopMode";

	public static boolean isMedia(String type) {
		type = type.toLowerCase();
		for (String end : MEDIATYPE) {
			if (type.equals(end))
				return true;
		}
		return false;
	}

	public static void savePlayingVideoProgress(Context con, int pos) {
		SharedPreferences pref = con.getSharedPreferences("Video", Context.MODE_PRIVATE);
		pref.edit().putInt(SAVE_VIDEO_PROGRESS, pos).apply();
	}

	public static int getPlayingVideoProgress(Context con) {
		SharedPreferences pref = con.getSharedPreferences("Video", Context.MODE_PRIVATE);
		return pref.getInt(SAVE_VIDEO_PROGRESS, 0);
	}

	public static void savePlayVolume(Context con, int vol) {
		SharedPreferences pref = con.getSharedPreferences("Video", Context.MODE_PRIVATE);
		pref.edit().putInt(SAVE_PLAY_VOLUME, vol).apply();
	}

	public static int getPlayVolume(Context con) {
		SharedPreferences pref = con.getSharedPreferences("Video", Context.MODE_PRIVATE);
		return pref.getInt(SAVE_PLAY_VOLUME, 50);
	}

	public static void savePlayingVideoPath(Context con, String path) {
		SharedPreferences pref = con.getSharedPreferences("Video", Context.MODE_PRIVATE);
		pref.edit().putString(SAVE_VIDEO_PATH, path).apply();
	}

	public static String getPlayingVideoPath(Context con) {
		SharedPreferences pref = con.getSharedPreferences("Video", Context.MODE_PRIVATE);
		return pref.getString(SAVE_VIDEO_PATH, "");
	}
	
	public static void saveLoopMode(Context con, int loopMode) {
		SharedPreferences pref = con.getSharedPreferences("Video", Context.MODE_PRIVATE);
		pref.edit().putInt(SAVE_LOOP_MODE, loopMode).apply();
	}

	public static int getLoopMode(Context con) {
		SharedPreferences pref = con.getSharedPreferences("Video", Context.MODE_PRIVATE);
		return pref.getInt(SAVE_LOOP_MODE, 0);
	}
}
