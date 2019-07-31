/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_zq_mediaengine_filter_audio_AudioSLPlayer */

#ifndef _Included_com_zq_mediaengine_filter_audio_AudioSLPlayer
#define _Included_com_zq_mediaengine_filter_audio_AudioSLPlayer
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_zq_mediaengine_filter_audio_AudioSLPlayer
 * Method:    _init
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_zq_mediaengine_filter_audio_AudioSLPlayer__1init
        (JNIEnv *, jobject);

/*
 * Class:     com_zq_mediaengine_filter_audio_AudioSLPlayer
 * Method:    _config
 * Signature: (JIIIII)I
 */
JNIEXPORT jint JNICALL Java_com_zq_mediaengine_filter_audio_AudioSLPlayer__1config
        (JNIEnv *, jobject, jlong, jint, jint, jint, jint, jint);

/*
 * Class:     com_zq_mediaengine_filter_audio_AudioSLPlayer
 * Method:    _attachTo
 * Signature: (JIJZ)V
 */
JNIEXPORT void JNICALL Java_com_zq_mediaengine_filter_audio_AudioSLPlayer__1attachTo
        (JNIEnv *, jobject, jlong, jint, jlong, jboolean);

/*
 * Class:     com_zq_mediaengine_filter_audio_AudioSLPlayer
 * Method:    _setTuneLatency
 * Signature: (JZ)V
 */
JNIEXPORT void JNICALL Java_com_zq_mediaengine_filter_audio_AudioSLPlayer__1setTuneLatency
        (JNIEnv *, jobject, jlong, jboolean);

/*
 * Class:     com_zq_mediaengine_filter_audio_AudioSLPlayer
 * Method:    _setMute
 * Signature: (JZ)V
 */
JNIEXPORT void JNICALL Java_com_zq_mediaengine_filter_audio_AudioSLPlayer__1setMute
        (JNIEnv *, jobject, jlong, jboolean);

/*
 * Class:     com_zq_mediaengine_filter_audio_AudioSLPlayer
 * Method:    _getPosition
 * Signature: (J)J
 */
JNIEXPORT jlong Java_com_zq_mediaengine_filter_audio_AudioSLPlayer__1getPosition
		(JNIEnv *env, jobject obj, jlong instance);

/*
 * Class:     com_zq_mediaengine_filter_audio_AudioSLPlayer
 * Method:    _start
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_zq_mediaengine_filter_audio_AudioSLPlayer__1start
        (JNIEnv *, jobject, jlong);

/*
 * Class:     com_zq_mediaengine_filter_audio_AudioSLPlayer
 * Method:    _stop
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_zq_mediaengine_filter_audio_AudioSLPlayer__1stop
        (JNIEnv *, jobject, jlong);

/*
 * Class:     com_zq_mediaengine_filter_audio_AudioSLPlayer
 * Method:    _pause
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_zq_mediaengine_filter_audio_AudioSLPlayer__1pause
		(JNIEnv *, jobject, jlong);

/*
 * Class:     com_zq_mediaengine_filter_audio_AudioSLPlayer
 * Method:    _resume
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_zq_mediaengine_filter_audio_AudioSLPlayer__1resume
		(JNIEnv *, jobject, jlong);


/*
 * Class:     com_zq_mediaengine_filter_audio_AudioSLPlayer
 * Method:    _read
 * Signature: (JLjava/nio/ByteBuffer;I)I
 */
JNIEXPORT jint JNICALL Java_com_zq_mediaengine_filter_audio_AudioSLPlayer__1read
        (JNIEnv *, jobject, jlong, jobject, jint);

/*
 * Class:     com_zq_mediaengine_filter_audio_AudioSLPlayer
 * Method:    _write
 * Signature: (JLjava/nio/ByteBuffer;IZ)I
 */
JNIEXPORT jint JNICALL Java_com_zq_mediaengine_filter_audio_AudioSLPlayer__1write
        (JNIEnv *, jobject, jlong, jobject, jint, jboolean);

/*
 * Class:     com_zq_mediaengine_filter_audio_AudioSLPlayer
 * Method:    _release
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_zq_mediaengine_filter_audio_AudioSLPlayer__1release
        (JNIEnv *, jobject, jlong);

#ifdef __cplusplus
}
#endif
#endif
