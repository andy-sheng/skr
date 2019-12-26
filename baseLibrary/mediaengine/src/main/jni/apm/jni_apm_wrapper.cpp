//
// Created by 昝晓飞 on 17/3/16.
//

#include <include/android_nio_utils.h>
#include "jni_apm_wrapper.h"
#include "apmwrapper.h"
#include "include/log.h"

static inline APMWrapper *getInstance(jlong ptr) {
    return (APMWrapper *) (intptr_t) ptr;
}
/*
 * Class:     com_zq_mediaengine_filter_audio_APMWrapper
 * Method:    create
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_zq_mediaengine_filter_audio_APMWrapper_create
        (JNIEnv *env, jobject thiz) {
    APMWrapper *apmWrapper = new APMWrapper();
    int result = apmWrapper->Create();
    if (result >= 0) {
        return (jlong) (intptr_t) apmWrapper;
    }
    return 0;
}

/*
 * Class:     com_zq_mediaengine_filter_audio_APMWrapper
 * Method:    setBypass
 * Signature: (JZ)I
 */
JNIEXPORT jint JNICALL Java_com_zq_mediaengine_filter_audio_APMWrapper_setBypass
        (JNIEnv *env, jobject thiz, jlong instance, jboolean enable) {
    return getInstance(instance)->SetBypass(enable);
}

/*
 * Class:     com_zq_mediaengine_filter_audio_APMWrapper
 * Method:    processStream
 * Signature: (JILjava/nio/ByteBuffer;I)Ljava/nio/ByteBuffer;
 */
JNIEXPORT jobject JNICALL Java_com_zq_mediaengine_filter_audio_APMWrapper_processStream
        (JNIEnv *env, jobject thiz, jlong instance, jint idx, jobject byteBuffer, jint insize) {
    APMWrapper *apmWrapper = getInstance(instance);

    AutoBufferPointer abp(env, byteBuffer, JNI_TRUE);
    short *buf = (short *)abp.pointer();

    int16_t *outBuf = NULL;
    jobject outByteBuffer = NULL;

    if (idx == 0) {
        int outSize = apmWrapper->ProcessStream(&outBuf, buf, insize);
        if (outSize > 0 && outBuf) {
            outByteBuffer = env->NewDirectByteBuffer((int8_t *) outBuf, outSize);
        } else {
            LOGE("[APM][processStream] APM processStream failed: %d", outSize);
        }
    } else {
        apmWrapper->AnalyzeReverseStream(buf, insize);
    }

    return outByteBuffer;
}

/*
 * Class:     com_zq_mediaengine_filter_audio_APMWrapper
 * Method:    enableHighPassFilter
 * Signature: (JZ)I
 */
JNIEXPORT jint JNICALL Java_com_zq_mediaengine_filter_audio_APMWrapper_enableHighPassFilter
        (JNIEnv *env, jobject thiz, jlong instance, jboolean enable) {
    return getInstance(instance)->EnableHighPassFilter(enable);
}

/*
 * Class:     com_zq_mediaengine_filter_audio_APMWrapper
 * Method:    enableNs
 * Signature: (JZ)I
 */
JNIEXPORT jint JNICALL Java_com_zq_mediaengine_filter_audio_APMWrapper_enableNs
        (JNIEnv *env, jobject thiz, jlong instance, jboolean enable) {
    return getInstance(instance)->EnableNs(enable);
}

/*
 * Class:     com_zq_mediaengine_filter_audio_APMWrapper
 * Method:    setNsLevel
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_com_zq_mediaengine_filter_audio_APMWrapper_setNsLevel
        (JNIEnv *env, jobject thiz, jlong instance, jint level) {
    APMWrapper *apmWrapper = getInstance(instance);
    if (apmWrapper != NULL) {
        return apmWrapper->SetNsLevel(level);
    }
    return -1;
}

/*
 * Class:     com_zq_mediaengine_filter_audio_APMWrapper
 * Method:    enableVAD
 * Signature: (JZ)I
 */
JNIEXPORT jint JNICALL Java_com_zq_mediaengine_filter_audio_APMWrapper_enableVAD
        (JNIEnv *env, jobject thiz, jlong instance, jboolean enable) {
    return getInstance(instance)->EnableVAD(enable);
}

/*
 * Class:     com_zq_mediaengine_filter_audio_APMWrapper
 * Method:    setVADLikelihood
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_com_zq_mediaengine_filter_audio_APMWrapper_setVADLikelihood
        (JNIEnv *env, jobject thiz, jlong instance, jint likelihood) {
    return getInstance(instance)->SetVADLikelihood(likelihood);
}

/*
 * Class:     com_zq_mediaengine_filter_audio_APMWrapper
 * Method:    enableAECM
 * Signature: (JZ)I
 */
JNIEXPORT jint JNICALL Java_com_zq_mediaengine_filter_audio_APMWrapper_enableAECM
        (JNIEnv *env, jobject thiz, jlong instance, jboolean enable) {
    return getInstance(instance)->EnableAECM(enable);
}

/*
 * Class:     com_zq_mediaengine_filter_audio_APMWrapper
 * Method:    enableAEC
 * Signature: (JZ)I
 */
JNIEXPORT jint JNICALL Java_com_zq_mediaengine_filter_audio_APMWrapper_enableAEC
        (JNIEnv *env, jobject thiz, jlong instance, jboolean enable) {
    return getInstance(instance)->EnableAEC(enable);
}

/*
 * Class:     com_zq_mediaengine_filter_audio_APMWrapper
 * Method:    setRoutingMode
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_com_zq_mediaengine_filter_audio_APMWrapper_setRoutingMode
        (JNIEnv *env, jobject thiz, jlong instance, jint mode) {
    return getInstance(instance)->SetRoutingMode(mode);
}

/*
 * Class:     com_zq_mediaengine_filter_audio_APMWrapper
 * Method:    setStreamDelay
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_com_zq_mediaengine_filter_audio_APMWrapper_setStreamDelay
        (JNIEnv *env, jobject thiz, jlong instance, jint delay) {
    return getInstance(instance)->SetStreamDelay(delay);
}

/*
 * Class:     com_zq_mediaengine_filter_audio_APMWrapper
 * Method:    config
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_com_zq_mediaengine_filter_audio_APMWrapper_config
        (JNIEnv *env, jobject, jlong instance, jint idx, jint sampleFmt, jint samplerate, jint channels) {
    return getInstance(instance)->Config(idx, sampleFmt, samplerate, channels);
}

/*
 * Class:     com_zq_mediaengine_filter_audio_APMWrapper
 * Method:    release
 * Signature: (JI)I
 */
JNIEXPORT void JNICALL Java_com_zq_mediaengine_filter_audio_APMWrapper_release
        (JNIEnv *env, jobject, jlong instance) {
    delete getInstance(instance);
}

JNIEXPORT jint Java_com_zq_mediaengine_filter_audio_APMWrapper_read
        (JNIEnv *env, jobject obj, jlong instance, jobject byteBuffer, jint size) {
    if (byteBuffer == NULL) {
        return 0;
    }
    uint8_t *buf = (uint8_t *) env->GetDirectBufferAddress(byteBuffer);
    return getInstance(instance)->read(buf, size);
}

JNIEXPORT void Java_com_zq_mediaengine_filter_audio_APMWrapper_attachTo
        (JNIEnv *env, jobject obj, jlong instance, jint idx, jlong ptr, jboolean detach) {
    getInstance(instance)->attachTo(idx, (AudioFilterBase *) (intptr_t) ptr, detach);
}
