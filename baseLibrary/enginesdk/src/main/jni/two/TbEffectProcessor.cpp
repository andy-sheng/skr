#include <jni.h>
#include <string>
#include <effect/Plugin_conv_reverb/CovReverb_control.h>
#include <effect/Plugin_conv_reverb/CovReverb_SDK_API.h>
#include <malloc.h>
#include <amplitude/libvoln/libvoln.h>
#include "com_engine_effect_ITbEffectProcessor.h"
#include "../../cpp/two/effect/effect_LIB/effect_control.h"
#include "../../cpp/two/effect/effect_LIB/effect_SDK_API.h"
#include "../../cpp/one/common/CommonTools.h"

FILE *inputFile = NULL;

FILE *outputFile = NULL;

int flag = -1;
Echo_s echo_s;
CovReverbV2_s *mcovreverb = NULL;

#define LOG_TAG "ITBEffectEngine"

#define FILEOPEN 0

extern "C"
JNIEXPORT jint JNICALL
Java_com_engine_effect_ITbEffectProcessor_init(JNIEnv *env, jobject ins) {
    flag = 0;
    return flag;
}

static void process1(uint8_t* data, int len, int channels, int sampleRate) {
    if (FILEOPEN) {
        LOGI("Java_com_engine_effect_ITbEffectProcessor_process1 flag=%d", flag);
    }
    if (flag == -1) {
        return;
    }
    if (flag != 1) {
        if (flag == 2) {
            if (mcovreverb) {
                free(mcovreverb);
                mcovreverb = NULL;
            }
        }
        echo_s.channel = channels;
        echo_s.samplerate = sampleRate;
        flag = 1;
        setBMB(&echo_s);
        EchoReset_API(&echo_s);
        EchoCalcu_API(&echo_s);
    }
    if (inputFile == NULL && FILEOPEN) {
        inputFile = fopen("/mnt/sdcard/tb_input.pcm", "wb+");
    }

    if (outputFile == NULL && FILEOPEN) {
        outputFile = fopen("/mnt/sdcard/tb_output.pcm", "wb+");
    }

    if (FILEOPEN) {
        int t = 0;
        for (int i = 0; i < len; i++) {
            t += data[i];
        }
        LOGI("Java_com_engine_effect_ITbEffectProcessor_process1 total=%d", t);
    }
    // 2048 512 下来
    short *samples = (short *) data; // 长度只有1024了

    if (FILEOPEN) {
        fwrite(samples, sizeof(short), len / 2, inputFile);
    }

    if (FILEOPEN) {
        LOGI("begin EchoRun_API");
    }
    EchoRun_API(&echo_s, samples, len / 2, samples);

    if (FILEOPEN) {
        LOGI("after EchoRun_API");
    }

    if (FILEOPEN) {
        fwrite(samples, sizeof(short), len / 2, outputFile);
    }
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_engine_effect_ITbEffectProcessor_process1(JNIEnv *env, jobject ins, jbyteArray samplesJni,
                                                   jobject byteBuffer, jint len,
                                                   jint channels, jint sampleRate) {
    uint8_t *data = NULL;

    if (samplesJni) {
        data = (uint8_t *) env->GetByteArrayElements(samplesJni, 0);
    } else if (byteBuffer) {
        data = (uint8_t*)env->GetDirectBufferAddress(byteBuffer);
    }

    if (data) {
        process1(data, len, channels, sampleRate);
    }

    if (samplesJni) {
        // 对Java的数组进行更新并释放C/C++的数组
        env->ReleaseByteArrayElements(samplesJni, (jbyte *) data, 0);
    }
    return flag;
}

static void process2(uint8_t* data, int len, int channels, int sampleRate) {
    if (FILEOPEN) {
        LOGI("Java_com_engine_effect_ITbEffectProcessor_process2 flag=%d", flag);
    }
    if (flag == -1) {
        return;
    }
    if (flag != 2) {
        if (FILEOPEN) {
            LOGI("11");
        }
        if (mcovreverb) {
            if (FILEOPEN) {
                LOGI("12");
            }
            free(mcovreverb);
            if (FILEOPEN) {
                LOGI("13");
            }
        }
        if (FILEOPEN) {
            LOGI("14");
        }
        mcovreverb = (CovReverbV2_s *) malloc(sizeof(CovReverbV2_s));
        mcovreverb->channelin = channels;
        mcovreverb->channelout = channels;
        mcovreverb->samplerate = sampleRate;
        mcovreverb->reverbkind = 11;//----------------CD 11
        mcovreverb->xsame = 1;
        mcovreverb->wet = 1;
        mcovreverb->xframelen = len / 2 / channels;
        if (FILEOPEN) {
            LOGI("15");
        }
        CovReverbV2Reset_API(mcovreverb);
        CovReverbV2Calcu_API(mcovreverb);
        flag = 2;
        if (FILEOPEN) {
            LOGI("16");
        }
    }
    if (FILEOPEN) {
        LOGI("17");
    }
    if (inputFile == NULL && FILEOPEN) {
        inputFile = fopen("/mnt/sdcard/tb_input.pcm", "wb+");
    }

    if (outputFile == NULL && FILEOPEN) {
        outputFile = fopen("/mnt/sdcard/tb_output.pcm", "wb+");
    }

    if (FILEOPEN) {
        int t = 0;
        for (int i = 0; i < len; i++) {
            t += data[i];
        }
        LOGI("Java_com_engine_effect_ITbEffectProcessor_process2 total=%d", t);
    }
    // 2048 512 下来
    short *samples = (short *) data; // 长度只有1024了

    if (FILEOPEN) {
        fwrite(samples, sizeof(short), len / 2, inputFile);
    }
    if (FILEOPEN) {
        LOGI("begin CovReverbV2Run_API");
    }
    CovReverbV2Run_API(mcovreverb, samples, len / 2, samples);
    if (FILEOPEN) {
        LOGI("after CovReverbV2Run_API");
    }
    if (FILEOPEN) {
        fwrite(samples, sizeof(short), len / 2, outputFile);
    }
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_engine_effect_ITbEffectProcessor_process2(JNIEnv *env, jobject ins, jbyteArray samplesJni,
                                                   jobject byteBuffer, jint len,
                                                   jint channels, jint sampleRate) {
    uint8_t *data = NULL;

    if (samplesJni) {
        data = (uint8_t *) env->GetByteArrayElements(samplesJni, 0);
    } else if (byteBuffer) {
        data = (uint8_t*)env->GetDirectBufferAddress(byteBuffer);
    }

    if (data) {
        process2(data, len, channels, sampleRate);
    }

    if (samplesJni) {
        // 对Java的数组进行更新并释放C/C++的数组
        env->ReleaseByteArrayElements(samplesJni, (jbyte *) data, 0);
    }
    return flag;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_engine_effect_ITbEffectProcessor_destroyEffectProcessor(JNIEnv *env, jobject instance) {
    if (flag == -1) {
        return flag;
    }
    flag = -1;
    if (FILEOPEN) {
        LOGI("1");
    }
    if (inputFile) {
        fclose(inputFile);
        inputFile = NULL;
    }
    if (FILEOPEN) {
        LOGI("2");
    }
    if (outputFile) {
        fclose(outputFile);
        outputFile = NULL;
    }
    if (FILEOPEN) {
        LOGI("3");
    }
    EchoReset_API(&echo_s);
    if (mcovreverb) {
        CovReverbV2Reset_API(mcovreverb);
        free(mcovreverb);
        mcovreverb = NULL;
    }
    if (FILEOPEN) {
        LOGI("4");
    }
    return flag;
}
