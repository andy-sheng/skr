LOCAL_PATH := $(call my-dir)

WEBRTC_APM_LIB := ../../../../${WEBRTC_APM_PATH}/lib/$(TARGET_ARCH_ABI)

include $(CLEAR_VARS)
LOCAL_MODULE := webrtcapm
LOCAL_SRC_FILES := $(WEBRTC_APM_LIB)/libwebrtcapm.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE    := apm

LOCAL_C_INCLUDES := $(LOCAL_PATH) \
					$(APP_JNI_ROOT)/streamer \
					$(WEBRTC_APM_PATH)/include

LOCAL_SRC_FILES := apmwrapper.cpp \
				   jni_apm_wrapper.cpp

LOCAL_CPPFLAGS += -std=c++11
LOCAL_CFLAGS += -DWEBRTC_ANDROID
LOCAL_CFLAGS += -DWEBRTC_POSIX
LOCAL_CPPFLAGS += -frtti

LOCAL_STATIC_LIBRARIES := webrtcapm
LOCAL_SHARED_LIBRARIES := unionstreamer
LOCAL_LDLIBS += -L$(SYSROOT)/usr/lib -llog

LOCAL_DISABLE_FATAL_LINKER_WARNINGS := true

include $(BUILD_SHARED_LIBRARY)