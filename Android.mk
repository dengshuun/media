LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_PACKAGE_NAME := USBMedia_F70

LOCAL_CERTIFICATE := platform

LOCAL_STATIC_JAVA_LIBRARIES := pinyin4j android-support-v4 \
	com.hwatong.media \
	com.hwatong.music \
	com.hwatong.ipod \
	com.bumptech.glide \
	com.hwatong.btmusic \
	com.hwatong.systemui
	
	

LOCAL_OVERRIDES_PACKAGES := Gallery Gallery2 Camera

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

include $(BUILD_PACKAGE)
