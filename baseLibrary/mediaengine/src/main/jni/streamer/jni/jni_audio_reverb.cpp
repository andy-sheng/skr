//
// Created by 昝晓飞 on 16/8/4.
//

#include "jni_audio_reverb.h"
#include "filter/audio_buf/AudioReverb.h"

jlong Java_com_zq_mediaengine_filter_audio_AudioReverbWrap_create
(JNIEnv *env, jobject thiz)
{
    AudioReverb* reverbInstance = new AudioReverb();
    return (jlong) reverbInstance;
}

void Java_com_zq_mediaengine_filter_audio_AudioReverbWrap_config
        (JNIEnv *env, jobject thiz, jlong instance, jint sampleRate, jint channels) {
    ((AudioReverb*) instance)->Config(sampleRate, channels);
}

void Java_com_zq_mediaengine_filter_audio_AudioReverbWrap_attachTo
        (JNIEnv *env, jobject thiz, jlong instance, jint idx, jlong ptr, jboolean detach) {
    ((AudioReverb*) instance)->attachTo(idx, (AudioFilterBase*) ptr, detach);
}

jboolean Java_com_zq_mediaengine_filter_audio_AudioReverbWrap_setLevel
(JNIEnv *env, jobject thiz, jlong instance, jint level)
{
    ((AudioReverb*) instance)->ReverbSet(level);
    return JNI_TRUE;
}

jint Java_com_zq_mediaengine_filter_audio_AudioReverbWrap_read
        (JNIEnv *env, jobject thiz, jlong instance, jobject jbuf, jint jsize) {
    uint8_t* buf = (uint8_t *) env->GetDirectBufferAddress(jbuf);
    return ((AudioReverb*) instance)->read(buf, jsize);
}

jint Java_com_zq_mediaengine_filter_audio_AudioReverbWrap_process
(JNIEnv *env, jobject thiz, jlong instance, jobject jbuf, jint jsize)
{
    short* buf = (short*) env->GetDirectBufferAddress(jbuf);
    int samples = jsize / 2;
    if (((AudioReverb*) instance) != NULL) {
        ((AudioReverb*) instance)->ReverbProcess(buf, samples);
    }
    return 0;
}

jboolean Java_com_zq_mediaengine_filter_audio_AudioReverbWrap_delete
(JNIEnv *env, jobject thiz, jlong instance)
{
    if (((AudioReverb*) instance) != NULL) {
        delete ((AudioReverb*) instance);
        return JNI_TRUE;
    }
    return JNI_FALSE;
}
