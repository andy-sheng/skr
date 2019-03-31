#include <jni.h>
#include <string>
#include "../../cpp/two/amplitude/libvoln/libvoln.h"
#include "../../cpp/one/common/CommonTools.h"

FILE *inputFile2 = NULL;

FILE *outputFile2 = NULL;

int flag2 = -1;

void *pvoln = NULL;

#define LOG_TAG "ITBAgcEngine"

#define FILEOPEN 0

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
    if (inputFile2 == NULL && FILEOPEN) {
        inputFile2 = fopen("/mnt/sdcard/tb_agc_input.pcm", "wb+");
    }

    if (outputFile2 == NULL && FILEOPEN) {
        outputFile2 = fopen("/mnt/sdcard/tb_agc_output.pcm", "wb+");
    }
    if (FILEOPEN) {
        fwrite(samples, sizeof(short), len / 2, inputFile2);
    }
    SKR_agc_proc(pvoln, samples, len / 2, samples);
    if (FILEOPEN) {
        fwrite(samples, sizeof(short), len / 2, outputFile2);
    }
    env->ReleaseByteArrayElements(samplesJni, reinterpret_cast<jbyte *>(data), 0);
    return flag2;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_engine_effect_ITbAgcProcessor_destroyAgcProcessor(JNIEnv *env, jobject instance) {
    flag2 = -1;
    if (pvoln != NULL) {
        SKR_agc_free(pvoln);
        pvoln = NULL;
    }
    if (FILEOPEN && inputFile2 != NULL) {
        fclose(inputFile2);
    }
    if (FILEOPEN && outputFile2 != NULL) {
        fclose(outputFile2);
    }
    return flag2;
}
