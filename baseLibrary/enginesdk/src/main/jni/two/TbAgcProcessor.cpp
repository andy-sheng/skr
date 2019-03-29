#include <jni.h>
#include <string>
#include "../../cpp/two/amplitude/libvoln/libvoln.h"
#include "../../cpp/one/common/CommonTools.h"

FILE *inputFile2 = NULL;

FILE *outputFile2 = NULL;

int flag2 = -1;

void *pvoln;

#define LOG_TAG "ITBAgcEngine"

#define FILEOPEN 1

extern "C"
JNIEXPORT jint JNICALL
Java_com_engine_effect_ITbAgcProcessor_init(JNIEnv *env, jobject ins) {
    flag2 = 0;
    return flag2;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_engine_effect_ITbAgcProcessor_process(JNIEnv *env, jobject ins, jbyteArray samplesJni,
                                               jint len,
                                               jint channels, jint sampleRate) {
    if (flag2 == -1) {
        return flag2;
    }
    if (flag2 <= 0) {
        SKR_agc_create(&pvoln);
        SKR_agc_reset(pvoln);

        int dykind = 0;
        float maxgaindB = 20.f;
        float fstgaindB = 15.f;
        int needDC = 1;
        SKR_agc_config(pvoln, sampleRate, channels, dykind, maxgaindB, fstgaindB, needDC);
        flag2 = 1;
    }

    //无符号整型，加起来和testIn不一定对得上
    unsigned char *data = (unsigned char *) env->GetByteArrayElements(samplesJni, 0);

    // 2048 512 下来
    short *samples = (short *) data; // 长度只有1024了
    if (FILEOPEN) {
        LOGI("before SKR_agc_proc");
    }
    SKR_agc_proc(pvoln, samples, len / 2, samples);
    if (FILEOPEN) {
        LOGI("after SKR_agc_proc");
    }
    env->ReleaseByteArrayElements(samplesJni, reinterpret_cast<jbyte *>(data), 0);
    return flag2;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_engine_effect_ITbAgcProcessor_destroyAgcProcessor(JNIEnv *env, jobject instance) {
    flag2 = -1;
    SKR_agc_free(pvoln);
    return flag2;
}
