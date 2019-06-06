LOCAL_PATH := $(call my-dir)

APP_JNI_ROOT := $(LOCAL_PATH)
PREBUILT_PATH := $(APP_JNI_ROOT)/prebuilt
FFMPEG_PATH := $(PREBUILT_PATH)/ffmpeg
LIB_YUV_PATH := $(PREBUILT_PATH)/libyuv
LIB_SOX_PATH := $(PREBUILT_PATH)/libsox
DENOISE_PATH := $(APP_JNI_ROOT)/denoise
WEBRTC_APM_PATH := $(PREBUILT_PATH)/webrtc_apm

include $(call all-subdir-makefiles)