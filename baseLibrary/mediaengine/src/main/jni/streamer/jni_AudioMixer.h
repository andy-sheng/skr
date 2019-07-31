/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_zq_mediaengine_filter_audio_AudioMixer */

#ifndef _Included_com_zq_mediaengine_filter_audio_AudioMixer
#define _Included_com_zq_mediaengine_filter_audio_AudioMixer
#ifdef __cplusplus
extern "C" {
#endif
#undef com_zq_mediaengine_filter_audio_AudioMixer_MAX_SINKPIN_NUM
#define com_zq_mediaengine_filter_audio_AudioMixer_MAX_SINKPIN_NUM 8L
#undef com_zq_mediaengine_filter_audio_AudioMixer_VERBOSE
#define com_zq_mediaengine_filter_audio_AudioMixer_VERBOSE 0L
#undef com_zq_mediaengine_filter_audio_AudioMixer_INSTANCE_UNINIT
#define com_zq_mediaengine_filter_audio_AudioMixer_INSTANCE_UNINIT 0L
/*
 * Class:     com_zq_mediaengine_filter_audio_AudioMixer
 * Method:    _init
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_zq_mediaengine_filter_audio_AudioMixer__1init
  (JNIEnv *, jobject);

/*
 * Class:     com_zq_mediaengine_filter_audio_AudioMixer
 * Method:    _setMainIdx
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_com_zq_mediaengine_filter_audio_AudioMixer__1setMainIdx
  (JNIEnv *, jobject, jlong, jint);

/*
 * Class:     com_zq_mediaengine_filter_audio_AudioMixer
 * Method:    _setMute
 * Signature: (JZ)V
 */
JNIEXPORT void JNICALL Java_com_zq_mediaengine_filter_audio_AudioMixer__1setMute
  (JNIEnv *, jobject, jlong, jboolean);

/*
 * Class:     com_zq_mediaengine_filter_audio_AudioMixer
 * Method:    _setBlockingMode
 * Signature: (JZ)V
 */
JNIEXPORT void JNICALL Java_com_zq_mediaengine_filter_audio_AudioMixer__1setBlockingMode
  (JNIEnv *, jobject, jlong, jboolean);

/*
 * Class:     com_zq_mediaengine_filter_audio_AudioMixer
 * Method:    _setOutputVolume
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_com_zq_mediaengine_filter_audio_AudioMixer__1setOutputVolume__JF
  (JNIEnv *, jobject, jlong, jfloat);

/*
 * Class:     com_zq_mediaengine_filter_audio_AudioMixer
 * Method:    _setOutputVolume
 * Signature: (JFF)V
 */
JNIEXPORT void JNICALL Java_com_zq_mediaengine_filter_audio_AudioMixer__1setOutputVolume__JFF
  (JNIEnv *, jobject, jlong, jfloat, jfloat);

/*
 * Class:     com_zq_mediaengine_filter_audio_AudioMixer
 * Method:    _setInputVolume
 * Signature: (JIF)V
 */
JNIEXPORT void JNICALL Java_com_zq_mediaengine_filter_audio_AudioMixer__1setInputVolume__JIF
  (JNIEnv *, jobject, jlong, jint, jfloat);

/*
 * Class:     com_zq_mediaengine_filter_audio_AudioMixer
 * Method:    _setInputVolume
 * Signature: (JIFF)V
 */
JNIEXPORT void JNICALL Java_com_zq_mediaengine_filter_audio_AudioMixer__1setInputVolume__JIFF
  (JNIEnv *, jobject, jlong, jint, jfloat, jfloat);

/*
 * Class:     com_zq_mediaengine_filter_audio_AudioMixer
 * Method:    _setDelay
 * Signature: (JIJ)V
 */
JNIEXPORT void JNICALL Java_com_zq_mediaengine_filter_audio_AudioMixer__1setDelay
        (JNIEnv *, jobject, jlong, jint, jlong);

/*
 * Class:     com_zq_mediaengine_filter_audio_AudioMixer
 * Method:    _attachTo
 * Signature: (JIJZ)V
 */
JNIEXPORT void JNICALL Java_com_zq_mediaengine_filter_audio_AudioMixer__1attachTo
  (JNIEnv *, jobject, jlong, jint, jlong, jboolean);

/*
 * Class:     com_zq_mediaengine_filter_audio_AudioMixer
 * Method:    _config
 * Signature: (JIIIII)I
 */
JNIEXPORT jint JNICALL Java_com_zq_mediaengine_filter_audio_AudioMixer__1config
  (JNIEnv *, jobject, jlong, jint, jint, jint, jint, jint, jint);

/*
 * Class:     com_zq_mediaengine_filter_audio_AudioMixer
 * Method:    _destroy
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_com_zq_mediaengine_filter_audio_AudioMixer__1destroy
  (JNIEnv *, jobject, jlong, jint);

/*
 * Class:     com_zq_mediaengine_filter_audio_AudioMixer
 * Method:    _read
 * Signature: (JLjava/nio/ByteBuffer;I)I
 */
JNIEXPORT jint JNICALL Java_com_zq_mediaengine_filter_audio_AudioMixer__1read
  (JNIEnv *, jobject, jlong, jobject, jint);

/*
 * Class:     com_zq_mediaengine_filter_audio_AudioMixer
 * Method:    _process
 * Signature: (JILjava/nio/ByteBuffer;I)I
 */
JNIEXPORT jint JNICALL Java_com_zq_mediaengine_filter_audio_AudioMixer__1process
  (JNIEnv *, jobject, jlong, jint, jobject, jint);

/*
 * Class:     com_zq_mediaengine_filter_audio_AudioMixer
 * Method:    _release
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_zq_mediaengine_filter_audio_AudioMixer__1release
  (JNIEnv *, jobject, jlong);

#ifdef __cplusplus
}
#endif
#endif
