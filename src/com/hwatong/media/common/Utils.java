package com.hwatong.media.common;

import java.util.Arrays;

import android.content.Context;
import android.content.SharedPreferences;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class Utils {

	public static final String PICTUE_LIST_CHANGED = "com.hwatong.picturelistchange";
	public static final String SAVE_PICTURE_PATH = "PlayingPicturePath";

	/**
	 * 按照字符串排序
	 * 
	 * @return
	 */
	public static String[] sortByCharacter(String[] strs) {
		Arrays.sort(strs);
		return strs;
	}

	public static String getStringForChinese(String value) {
		String newValue = value;
		try {
			if (value.equals(new String(value.getBytes("ISO-8859-1"), "ISO-8859-1"))) {
				newValue = new String(value.getBytes("ISO-8859-1"), "GBK");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newValue;
	}

	// 保存播放的picture的路径
	public static void savePlayingPicturePath(Context con, String path) {
		SharedPreferences pref = con.getSharedPreferences("Picture", Context.MODE_PRIVATE);
		pref.edit().putString(SAVE_PICTURE_PATH, path).apply();
	}

	// 得到上次播放的picture的路径
	public static String getPlayingPicturePath(Context con) {
		SharedPreferences pref = con.getSharedPreferences("Picture", Context.MODE_PRIVATE);
		return pref.getString(SAVE_PICTURE_PATH, "");
	}

	/**
	 * 格式化时间 格式为 00:00 0:00:00
	 * 
	 * @param milliseconds
	 * @return
	 */
	public static String formatetime(int milliseconds) {
		String hour = String.valueOf(milliseconds / 3600000);
		String minute = String.valueOf((milliseconds % 3600000) / 60000);
		String second = String.valueOf(((milliseconds % 3600000) % 60000) / 1000);
		hour = deal(hour);
		minute = deal(minute);
		second = deal(second);
		if (hour == "00")
			return minute + ":" + second;
		return hour + ":" + minute + ":" + second;
	}

	/**
	 * 转换时间 1 11 为 01 11
	 * 
	 * @param time
	 * @return
	 */
	private static String deal(String time) {
		if (time.length() == 1) {
			if (time.equals("0"))
				time = "00";
			else
				time = "0" + time;
		}
		return time;
	}

	/**
	 * 去掉文件后缀
	 * 
	 * @param filename
	 * @return
	 */
	public static String getNameFromFilename(String filename) {
		if (filename == null)
			return "";
		int dotPosition = filename.lastIndexOf('.');
		if (dotPosition != -1) {
			return filename.substring(0, dotPosition);
		}
		return filename;
	}

	/**
	 * 带后缀的文件名
	 * 
	 * @param filename
	 * @return
	 */
	public static String getExtFromFilename(String filename) {
		if (filename == null)
			return "";
		int dotPosition = filename.lastIndexOf('.');
		int dotPosition2 = filename.lastIndexOf('/');
		if (dotPosition != -1 && dotPosition2 != -1 && Math.abs(dotPosition2 - dotPosition) > 1) {
			return filename.substring(filename.lastIndexOf("/") + 1, filename.length());
		}
		return "";
	}

	/**
	 * 最后一个文件名字
	 * 
	 * @param filename
	 * @return
	 */
	public static String getLastFromFilename(String filename) {
		if (filename == null)
			return "";
		int dotPosition = filename.lastIndexOf('/');
		if (dotPosition != -1) {
			return filename.substring(filename.lastIndexOf("/") + 1, filename.length());
		}
		return "";
	}

	public static String getTypeFromFilename(String filename) {
		if (filename == null)
			return "";
		int dotPosition = filename.lastIndexOf('.');
		if (dotPosition != -1) {
			return filename.substring(dotPosition + 1, filename.length());
		}
		return "";
	}

	/**
	 * 上一个目录
	 * 
	 * @param filename
	 * @return
	 */
	public static String getPreDirectory(String filename) {
		if (filename == null)
			return "";
		int dotPosition = filename.lastIndexOf('/');
		if (dotPosition != -1) {
			return filename.substring(0, filename.lastIndexOf("/"));
		}
		return "";
	}

	public static String getComFlg(String name) {
		if (name == null || name.equals(""))
			return "";

		String result = getPingYin(name.substring(0, 1));

		if (!result.equals("")) {
			result = result.substring(0, 1).toUpperCase();
			if (!result.matches("[A-Z]")) {
				result = "#";
			}
		} else {
			result = "#";
		}

		return result;
	}

	public static String getPingYin(String src) {
		if (src == null || src.equals(""))
			return "";

		char[] t1 = null;
		t1 = src.toCharArray();
		String[] t2 = new String[t1.length];
		HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
		format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		format.setVCharType(HanyuPinyinVCharType.WITH_V);
		StringBuilder result = new StringBuilder("");
		int l = t1.length;
		try {
			for (int i = 0; i < l; i++) {

				if (Character.toString(t1[i]).matches("[\\u4E00-\\u9FA5]+")) {
					t2 = PinyinHelper.toHanyuPinyinStringArray(t1[i], format);
					if (t2 != null)
						result.append(t2[0]);
				} else {
					result.append(Character.toString(t1[i]).toLowerCase());
				}
			}
			return result.toString().replaceAll(" ", "");
		} catch (BadHanyuPinyinOutputFormatCombination e1) {
			e1.printStackTrace();
		}
		return result.toString().replaceAll(" ", "");
	}
}
