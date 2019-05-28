LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
# 默认的包含头文件
LOCAL_C_INCLUDES := $(LOCAL_PATH)

LOCAL_MODULE    := Denoise_export

ifneq ($(TARGET_ARCH_ABI),armeabi)
LOCAL_SRC_FILES := Denoise_export.cpp.arm.neon
else
LOCAL_SRC_FILES := Denoise_export.cpp
endif

LOCAL_CFLAGS += -flax-vector-conversions

include $(BUILD_STATIC_LIBRARY)