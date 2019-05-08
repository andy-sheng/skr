LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
# 默认的包含头文件
LOCAL_C_INCLUDES := $(LOCAL_PATH)

LOCAL_MODULE    := Denoise_export
LOCAL_SRC_FILES := Denoise_export.cpp

LOCAL_CFLAGS := -D__cpusplus -g
#LOCAL_LDLIBS := -lz -llog
#LOCAL_LDLIBS := -llog
LOCAL_ARM_MODE := arm

ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
# 采用NEON优化技术, 添加了才能使用 arm_neon.h 头文件
# -mfloat-abi=softfp -mfpu=neon 使用 arm_neon.h 必须
LOCAL_CFLAGS += -mfloat-abi=softfp -mfpu=neon -march=armv7-a -mtune=cortex-a8 -flax-vector-conversions
LOCAL_ARM_NEON := true
endif

ifeq ($(TARGET_ARCH_ABI),arm64-v8a)
LOCAL_CFLAGS += -flax-vector-conversions
endif

ifneq ($(TARGET_ARCH_ABI),armeabi)
LOCAL_CFLAGS += -DENABLE_NEON
endif

include $(BUILD_STATIC_LIBRARY)