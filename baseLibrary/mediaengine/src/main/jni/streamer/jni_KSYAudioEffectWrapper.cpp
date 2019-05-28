//
// Created by sujia on 2017/3/22.
//
#include "jni_KSYAudioEffectWrapper.h"
#include "KSYAudioEffect.h"
#include <stdlib.h>
#include <jni/util/jni_cache.h>
#include <include/android_nio_utils.h>

static inline KSYAudioEffect *getInstance(jlong ptr) {
    return (KSYAudioEffect *) (intptr_t) ptr;
}

jlong JNICALL Java_com_zq_mediaengine_filter_audio_KSYAudioEffectWrapper_native_1init
        (JNIEnv *env, jobject object) {
    KSYAudioEffect *thiz = new KSYAudioEffect();
    thiz->Init();
    return (jlong) thiz;
}

void Java_com_zq_mediaengine_filter_audio_KSYAudioEffectWrapper_native_1set_1audio_1format
        (JNIEnv *env, jobject object, jlong instance, jint sampleFmt, jint sampleRate,
         jint channels) {
    KSYAudioEffect *thiz = (KSYAudioEffect *) instance;
    thiz->setAudioFormat(sampleFmt, sampleRate, channels);
}


void Java_com_zq_mediaengine_filter_audio_KSYAudioEffectWrapper_native_1set_1effect_1type
        (JNIEnv *env, jobject object, jlong instance, jint type) {
    KSYAudioEffect *thiz = (KSYAudioEffect *) instance;
    thiz->setEffectType(type);
}

void Java_com_zq_mediaengine_filter_audio_KSYAudioEffectWrapper_native_1set_1pitch_1level
        (JNIEnv *env, jobject object, jlong instance, jint type) {
    KSYAudioEffect *thiz = (KSYAudioEffect *) instance;
    thiz->setPitchLevel(type);
}

void Java_com_zq_mediaengine_filter_audio_KSYAudioEffectWrapper_native_1add_1effect
        (JNIEnv *env, jobject object, jlong instance, jstring jname, jint jargc,
         jobjectArray jargv) {
    KSYAudioEffect *thiz = (KSYAudioEffect *) instance;

    int length = 0;
    if (jargv != NULL) {
        jboolean isCopy;
        const char *name = env->GetStringUTFChars(jname, &isCopy);

        GetJniCacheInstance()->CacheClass(env, JAVA_CLASS_PATH_AUDIOEFFECTOPTION);
        length = env->GetArrayLength(jargv);
        int i = 0;
        jstring jtemp[length];
        char *argv[length];
        for (; i < length; i++) {
            jboolean copy;
            jobject obj = env->GetObjectArrayElement(jargv, i);
            jtemp[i] = (jstring) JCOM_GET_FIELD_L(env, obj, JAVA_CLASS_PATH_AUDIOEFFECTOPTION,
                                                  JAVA_AUDIOEFFECTOPTION_FIELD_OPTION);
            argv[i] = (char *) env->GetStringUTFChars(jtemp[i], &copy);
        }
        thiz->addEffects(name, jargc, argv);

        env->ReleaseStringUTFChars(jname, name);
        i = 0;
        for (; i < length; i++) {
            env->ReleaseStringUTFChars(jtemp[i], argv[i]);
        }
    }
}

void Java_com_zq_mediaengine_filter_audio_KSYAudioEffectWrapper_native_1remove_1effects
        (JNIEnv *env, jobject object, jlong instance) {
    KSYAudioEffect *thiz = (KSYAudioEffect *) instance;
    thiz->removeEffects();
}

void Java_com_zq_mediaengine_filter_audio_KSYAudioEffectWrapper_native_1process
        (JNIEnv *env, jobject object, jlong instance, jobject byteBuffer, jint len) {
    KSYAudioEffect *thiz = (KSYAudioEffect *) instance;
    AutoBufferPointer abp(env, byteBuffer, JNI_TRUE);
    uint8_t *data = (uint8_t*)abp.pointer();
    thiz->processAudio(data, len);
}

void Java_com_zq_mediaengine_filter_audio_KSYAudioEffectWrapper_native_1quit
        (JNIEnv *env, jobject object, jlong instance) {
    KSYAudioEffect *thiz = (KSYAudioEffect *) instance;
    thiz->quit();
}

void Java_com_zq_mediaengine_filter_audio_KSYAudioEffectWrapper_attachTo
        (JNIEnv *env, jobject object, jlong instance, jint idx, jlong ptr, jboolean detach) {
    KSYAudioEffect *thiz = (KSYAudioEffect *) instance;
    thiz->attachTo(idx, (AudioFilterBase *) ptr, detach);
}

jint Java_com_zq_mediaengine_filter_audio_KSYAudioEffectWrapper_native_1read
        (JNIEnv *env, jobject obj, jlong instance, jobject byteBuffer, jint size) {
    if (byteBuffer == NULL) {
        return 0;
    }
    AutoBufferPointer abp(env, byteBuffer, JNI_TRUE);
    uint8_t *buf = (uint8_t*)abp.pointer();
    return getInstance(instance)->read(buf, size);
}
