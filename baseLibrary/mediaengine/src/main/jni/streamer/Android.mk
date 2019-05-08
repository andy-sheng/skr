LOCAL_PATH := $(call my-dir)

############################
##   deps
############################
include $(CLEAR_VARS)

FFMPEG_LIB := ../../../../$(FFMPEG_PATH)/$(TARGET_ARCH_ABI)/lib
LIB_YUV_LIB := ../../../../$(LIB_YUV_PATH)/$(TARGET_ARCH_ABI)/lib
LIB_SOX_LIB := ../../../../$(LIB_SOX_PATH)/$(TARGET_ARCH_ABI)/lib

include $(CLEAR_VARS)
LOCAL_MODULE := avutil
LOCAL_SRC_FILES := $(FFMPEG_LIB)/libavutil.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := swresample
LOCAL_SRC_FILES := $(FFMPEG_LIB)/libswresample.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := yuv_static
LOCAL_SRC_FILES := $(LIB_YUV_LIB)/libyuv_static.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libsox
LOCAL_SRC_FILES := $(LIB_SOX_LIB)/libsox.a
include $(PREBUILT_STATIC_LIBRARY)

############################
##   streamer
############################

STREAMER_CONLYFLAGS := -std=c99
STREAMER_CPPFLAGS := -frtti

STREAMER_C_INCLUDES := $(LOCAL_PATH)
STREAMER_C_INCLUDES += $(LOCAL_PATH)/include
STREAMER_C_INCLUDES += $(PREBUILT_PATH)/include
STREAMER_C_INCLUDES += $(FFMPEG_PATH)/$(TARGET_ARCH_ABI)/include
STREAMER_C_INCLUDES += $(LIB_YUV_PATH)/$(TARGET_ARCH_ABI)/include
STREAMER_C_INCLUDES += $(LIB_SOX_PATH)/$(TARGET_ARCH_ABI)/include
STREAMER_C_INCLUDES += $(DENOISE_PATH)

STREAMER_SRC_FILES := utils/jni_util.cpp
STREAMER_SRC_FILES += utils/value.cpp
STREAMER_SRC_FILES += utils/util.c

STREAMER_SRC_FILES += watermark.cpp

STREAMER_SRC_FILES += jni/util/jni_cache.cpp
STREAMER_SRC_FILES += jni/util/DataConvertUtility.cpp
STREAMER_SRC_FILES += filter/img_buf/img_pre_process.cpp
STREAMER_SRC_FILES += jni/jni_img_pre_process.cpp

STREAMER_SRC_FILES += filter/audio_buf/AudioReverb.cpp
STREAMER_SRC_FILES += jni/jni_audio_reverb.cpp

ifneq ($(TARGET_ARCH_ABI),armeabi)
STREAMER_SRC_FILES += color_format_convert.c.arm.neon
else
STREAMER_SRC_FILES += color_format_convert.c
endif
STREAMER_SRC_FILES += jni_ColorFormatConvert.c

STREAMER_SRC_FILES += cipher/CipherUtility.cpp
STREAMER_SRC_FILES += jni/jni_decrypt.cpp

STREAMER_SRC_FILES += jni/jni_string.cpp

STREAMER_SRC_FILES += audio/thread_util.c
STREAMER_SRC_FILES += audio/audio_resample.c
STREAMER_SRC_FILES += audio/audio_utils_fifo.c
STREAMER_SRC_FILES += audio/AudioRecord.cpp
STREAMER_SRC_FILES += audio/AudioFilterBase.cpp
STREAMER_SRC_FILES += audio/AudioPlay.cpp
STREAMER_SRC_FILES += audio/AudioResample.cpp
STREAMER_SRC_FILES += audio/AudioMixer.cpp
STREAMER_SRC_FILES += jni_AudioRecord.cpp
STREAMER_SRC_FILES += jni_AudioPlay.cpp
STREAMER_SRC_FILES += jni_AudioResample.cpp
STREAMER_SRC_FILES += jni_AudioMixer.cpp

STREAMER_SRC_FILES += jni_KSYAudioEffectWrapper.cpp
STREAMER_SRC_FILES += KSYAudioEffect.cpp

STREAMER_STATIC_LIBRARIES := Denoise_export
STREAMER_STATIC_LIBRARIES += yuv_static swresample avutil \
                             gnustl_static libsox

# build shared library
include $(CLEAR_VARS)
LOCAL_MODULE := unionstreamer
LOCAL_LDLIBS := -llog -landroid -lOpenSLES -lEGL -lGLESv2 -ljnigraphics -lz

LOCAL_CONLYFLAGS := $(STREAMER_CONLYFLAGS)
LOCAL_CPPFLAGS := $(STREAMER_CPPFLAGS)
LOCAL_C_INCLUDES := $(STREAMER_C_INCLUDES)
LOCAL_SRC_FILES := $(STREAMER_SRC_FILES)
LOCAL_SRC_FILES += utils/jni_init.cpp

LOCAL_STATIC_LIBRARIES := $(STREAMER_STATIC_LIBRARIES)

LOCAL_DISABLE_FATAL_LINKER_WARNINGS := true

include $(BUILD_SHARED_LIBRARY)

$(call import-module,android/cpufeatures)
