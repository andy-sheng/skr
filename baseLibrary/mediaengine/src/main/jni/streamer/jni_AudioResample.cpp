//
// Created by 乐 程 on 22/05/2017.
//

#include <jni.h>
#include <include/android_nio_utils.h>
#include "jni_AudioResample.h"
#include "audio/AudioResample.h"

static inline AudioResample* getInstance(jlong ptr) {
    return (AudioResample*)(intptr_t) ptr;
}

jlong Java_com_zq_mediaengine_filter_audio_AudioResample__1init
        (JNIEnv *env, jobject obj) {
    AudioResample* audioResample = new AudioResample();
    return (jlong)(intptr_t) audioResample;
}

void Java_com_zq_mediaengine_filter_audio_AudioResample__1setOutputFormat
        (JNIEnv *env, jobject obj, jlong instance, jint sampleFmt, jint sampleRate, jint channels) {
    getInstance(instance)->setOutputFormat(sampleFmt, sampleRate, channels);
}

jint Java_com_zq_mediaengine_filter_audio_AudioResample__1config
        (JNIEnv *env, jobject obj, jlong instance, jint sampleFormat, jint sampleRate, jint
        channels) {
    return getInstance(instance)->config(sampleFormat, sampleRate, channels);
}

void Java_com_zq_mediaengine_filter_audio_AudioResample__1attachTo
        (JNIEnv *env, jobject obj, jlong instance, jint idx, jlong ptr, jboolean detach) {
    getInstance(instance)->attachTo(idx, (AudioFilterBase*)(intptr_t) ptr, detach);
}

jint Java_com_zq_mediaengine_filter_audio_AudioResample__1read
        (JNIEnv *env, jobject obj, jlong instance, jobject byteBuffer, jint size) {
    if (byteBuffer == NULL) {
        return 0;
    }
    AutoBufferPointer abp(env, byteBuffer, JNI_TRUE);
    uint8_t *buf = (uint8_t*)abp.pointer();
    return getInstance(instance)->read(buf, size);
}

jobject Java_com_zq_mediaengine_filter_audio_AudioResample__1resample
        (JNIEnv *env, jobject obj, jlong instance, jobject byteBuffer, jint size) {
    if (byteBuffer == NULL) {
        return NULL;
    }

    jobject outByteBuffer = NULL;
    uint8_t* outBuf = NULL;
    int outSize = 0;

    {   // auto release pool to avoid JNI critical issue
        AutoBufferPointer abp(env, byteBuffer, JNI_FALSE);
        uint8_t *inBuf = (uint8_t *) abp.pointer();
        outSize = getInstance(instance)->resample(&outBuf, inBuf, size);
    }

    if (outSize > 0 && outBuf) {
        outByteBuffer = env->NewDirectByteBuffer(outBuf, outSize);
    }
    return outByteBuffer;
}

void Java_com_zq_mediaengine_filter_audio_AudioResample__1release
        (JNIEnv *env, jobject obj, jlong instance) {
    delete getInstance(instance);
}
