/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_engine_effect_ITbAgcProcessor */

#ifndef _Included_com_engine_effect_ITbAgcProcessor
#define _Included_com_engine_effect_ITbAgcProcessor
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL
Java_com_engine_effect_ITbAgcProcessor_init
        (JNIEnv *, jobject);

JNIEXPORT jint JNICALL
Java_com_engine_effect_ITbAgcProcessor_process(JNIEnv *env, jobject instance,
                                                   jbyteArray samples_, jobject byteBuffer,
                                                   jint length, jint channels,
                                                   jint samplesPerSec);

JNIEXPORT jint JNICALL
Java_com_engine_effect_ITbAgcProcessor_destroyAgcProcessor(JNIEnv *env, jobject instance);


#ifdef __cplusplus
}
#endif
#endif
