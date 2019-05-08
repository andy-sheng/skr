#include <jni.h>

#include <string>
#include <pthread.h>

#include "value.h"

#ifndef ANDROID_STREAMER_JNI_UTIL_H
#define ANDROID_STREAMER_JNI_JNI_UTIL_H

#define JNI_NULL NULL


jboolean ToJBool(bool value);

bool ToCppBool(jboolean value);

jstring ToJString(JNIEnv* env, const std::string& value);

std::string ToCppString(JNIEnv* env, jstring value);


jobject ToJObject(JNIEnv* env, const Value& value);

char* ToCString(JNIEnv* env, jstring jstr);

bool IsJavaInstanceOf(JNIEnv* env, jobject object,
                      const std::string& class_name);

#endif
