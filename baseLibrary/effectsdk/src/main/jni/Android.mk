LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := effect
LOCAL_SRC_FILES :=  $(LOCAL_PATH)/libs/armeabi-v7a/libeffect.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := effect_proxy

LOCAL_SRC_FILES :=  $(LOCAL_PATH)/byted_effect.cpp \
$(LOCAL_PATH)/face_detect.cpp \
$(LOCAL_PATH)/hand_detect.cpp \
$(LOCAL_PATH)/skeleton_detect.cpp \
$(LOCAL_PATH)/opengl_utils.cpp \
$(LOCAL_PATH)/yuv_utils.cpp \
$(LOCAL_PATH)/portrait_matting.cpp\
$(LOCAL_PATH)/hair_parser.cpp\
$(LOCAL_PATH)/face_verify.cpp\


LOCAL_C_INCLUDES +=   \
$(LOCAL_PATH)/include   \

LOCAL_CFLAGS += -std=c++11 -fexceptions -frtti
LOCAL_LDLIBS += -llog
LOCAL_LDLIBS += -lGLESv2
LOCAL_SHARED_LIBRARIES := libeffect

include $(BUILD_SHARED_LIBRARY)
