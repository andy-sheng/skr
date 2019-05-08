#include "jni_AudioMixer.h"
#include "audio/AudioMixer.h"

static inline AudioMixer* getInstance(jlong ptr) {
    return (AudioMixer*)(intptr_t) ptr;
}

jlong Java_com_zq_mediaengine_filter_audio_AudioMixer__1init
        (JNIEnv *, jobject) {
    AudioMixer* thiz = new AudioMixer();
    return (jlong)(intptr_t) thiz;
}

void Java_com_zq_mediaengine_filter_audio_AudioMixer__1setMainIdx
        (JNIEnv *env, jobject obj, jlong instance, jint idx) {
    getInstance(instance)->setMainIdx(idx);
}

void Java_com_zq_mediaengine_filter_audio_AudioMixer__1setMute
        (JNIEnv *env, jobject obj, jlong instance, jboolean mute) {
    getInstance(instance)->setMute(mute);
}

void Java_com_zq_mediaengine_filter_audio_AudioMixer__1setBlockingMode
        (JNIEnv *env, jobject obj, jlong instance, jboolean blockingMode) {
    getInstance(instance)->setBlockingMode(blockingMode);
}

void Java_com_zq_mediaengine_filter_audio_AudioMixer__1setOutputVolume
        (JNIEnv *env, jobject obj, jlong instance, jfloat vol) {
    getInstance(instance)->setOutputVolume(vol);
}

void Java_com_zq_mediaengine_filter_audio_AudioMixer__1setInputVolume
        (JNIEnv *env, jobject obj, jlong instance, jint idx, jfloat vol) {
    getInstance(instance)->setInputVolume(idx, vol);
}

void Java_com_zq_mediaengine_filter_audio_AudioMixer__1attachTo
        (JNIEnv *env, jobject obj, jlong instance, jint idx, jlong ptr, jboolean detach) {
    getInstance(instance)->attachTo(idx, (AudioFilterBase*)(intptr_t) ptr, detach);
}

jint Java_com_zq_mediaengine_filter_audio_AudioMixer__1config
        (JNIEnv *env, jobject obj, jlong instance, jint idx, jint sampleRate, jint channels,
         jint bufferSamples, jint fifoSizeInMs) {
    return getInstance(instance)->config(idx, sampleRate, channels, bufferSamples,
                                         fifoSizeInMs, false);
}

void Java_com_zq_mediaengine_filter_audio_AudioMixer__1destroy
        (JNIEnv *env, jobject obj, jlong instance, jint idx) {
    getInstance(instance)->destroy(idx);
}

jint Java_com_zq_mediaengine_filter_audio_AudioMixer__1read
        (JNIEnv *env, jobject obj, jlong instance, jobject byteBuffer, jint size) {
    uint8_t* buf = (uint8_t*)env->GetDirectBufferAddress(byteBuffer);
    return getInstance(instance)->read(buf, size);
}

jint Java_com_zq_mediaengine_filter_audio_AudioMixer__1process
        (JNIEnv *env, jobject obj, jlong instance, jint idx, jobject byteBuffer, jint size) {
    uint8_t* buf = (uint8_t*)env->GetDirectBufferAddress(byteBuffer);
    return getInstance(instance)->process(idx, buf, size, false);
}

void Java_com_zq_mediaengine_filter_audio_AudioMixer__1release
        (JNIEnv *env, jobject obj, jlong instance) {
    delete getInstance(instance);
}

