#include <stdint.h>
#include "jni_AudioRecord.h"
#include "audio/AudioRecord.h"
#include "audio/AudioPlay.h"

static inline AudioRecord* getInstance(jlong ptr) {
    return (AudioRecord*)(intptr_t) ptr;
}

jlong Java_com_zq_mediaengine_capture_audio_UnionAudioSLRecord__1init
        (JNIEnv *env, jobject obj, jint sampleRate, jint channels, jint bufferSize) {
    AudioRecord* thiz = new AudioRecord(sampleRate, channels, bufferSize);
    return (jlong)(intptr_t) thiz;
}

void Java_com_zq_mediaengine_capture_audio_UnionAudioSLRecord__1setVolume
        (JNIEnv *env, jobject obj, jlong ptr, jfloat volume) {
    getInstance(ptr)->setVolume(volume);
}

jint Java_com_zq_mediaengine_capture_audio_UnionAudioSLRecord__1start
        (JNIEnv *env, jobject obj, jlong ptr) {
    return getInstance(ptr)->start();
}

jint Java_com_zq_mediaengine_capture_audio_UnionAudioSLRecord__1stop
        (JNIEnv *env, jobject obj, jlong ptr) {
    return getInstance(ptr)->stop();
}

void Java_com_zq_mediaengine_capture_audio_UnionAudioSLRecord__1setEnableLatencyTest
        (JNIEnv *env, jobject obj, jlong ptr, jboolean enable) {
    getInstance(ptr)->setEnableLatencyTest(enable);
}

jint Java_com_zq_mediaengine_capture_audio_UnionAudioSLRecord__1read
        (JNIEnv *env, jobject obj, jlong ptr, jobject jByteBuffer, jint size) {
    uint8_t* buf = NULL;
    if (jByteBuffer) {
        buf = (uint8_t*) env->GetDirectBufferAddress(jByteBuffer);
    }
    return getInstance(ptr)->read(buf, size);
}

void Java_com_zq_mediaengine_capture_audio_UnionAudioSLRecord__1release
        (JNIEnv *env, jobject obj, jlong ptr) {
    delete getInstance(ptr);
}
