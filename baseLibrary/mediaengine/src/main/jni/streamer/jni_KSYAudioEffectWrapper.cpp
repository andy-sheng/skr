//
// Created by sujia on 2017/3/22.
//
#include "jni_KSYAudioEffectWrapper.h"
#include "KSYAudioEffect.h"
#include <stdlib.h>

static inline KSYAudioEffect* getInstance(jlong ptr)
{
    return (KSYAudioEffect*)(intptr_t) ptr;
}

jlong JNICALL Java_com_zq_mediaengine_filter_audio_AudioEffectWrapper_native_1init
        (JNIEnv *env , jobject object)
{
    KSYAudioEffect* thiz = new KSYAudioEffect();
    thiz->Init();
    return (jlong) thiz;
}

void Java_com_zq_mediaengine_filter_audio_AudioEffectWrapper_native_1set_1audio_1format
        (JNIEnv *env, jobject object, jlong instance, jint bitsPerSample, jint sampleRate, jint channels) {
    KSYAudioEffect* thiz = (KSYAudioEffect*) instance;
    thiz->setAudioFormat(bitsPerSample, sampleRate, channels);
}


void Java_com_zq_mediaengine_filter_audio_AudioEffectWrapper_native_1set_1effect_1type
        (JNIEnv *env, jobject object, jlong instance, jint type) {
    KSYAudioEffect* thiz = (KSYAudioEffect*) instance;
    thiz->setEffectType(type);
}

void Java_com_zq_mediaengine_filter_audio_AudioEffectWrapper_native_1set_1pitch_1level
        (JNIEnv *env, jobject object, jlong instance, jint type) {
    KSYAudioEffect* thiz = (KSYAudioEffect*) instance;
    thiz->setPitchLevel(type);
}

void Java_com_zq_mediaengine_filter_audio_AudioEffectWrapper_native_1process
        (JNIEnv *env , jobject object, jlong instance, jobject buf, jint len)
{
    KSYAudioEffect* thiz = (KSYAudioEffect*) instance;
    uint8_t* data = (uint8_t*) env->GetDirectBufferAddress(buf);
    thiz->processAudio(data, len);
}

void Java_com_zq_mediaengine_filter_audio_AudioEffectWrapper_native_1quit
        (JNIEnv *env, jobject object, jlong instance) {
    KSYAudioEffect* thiz = (KSYAudioEffect*) instance;
    thiz->quit();
}

void Java_com_zq_mediaengine_filter_audio_AudioEffectWrapper_attachTo__JIJZ
        (JNIEnv *env, jobject object, jlong instance, jint idx, jlong ptr, jboolean detach) {
    KSYAudioEffect* thiz = (KSYAudioEffect*) instance;
    thiz->attachTo(idx, (AudioFilterBase*)ptr, detach);
}

jint Java_com_zq_mediaengine_filter_audio_AudioEffectWrapper_native_1read
        (JNIEnv *env, jobject obj, jlong instance, jobject byteBuffer, jint size) {
    if (byteBuffer == NULL) {
        return 0;
    }
    uint8_t* buf = (uint8_t*)env->GetDirectBufferAddress(byteBuffer);
    return getInstance(instance)->read(buf, size);
}
