//
// Created by qyvideo on 7/20/16.
//

#ifndef KSYSTREAMERANDROID_JNI_ONLOAD_H
#define KSYSTREAMERANDROID_JNI_ONLOAD_H

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif
jint JNI_OnLoad_Streamer(JavaVM* vm, void* reserved);
#ifdef __cplusplus
}
#endif

#endif //KSYSTREAMERANDROID_JNI_ONLOAD_H