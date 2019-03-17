#include <jni.h>
#include <string>
#include <effect/Plugin_conv_reverb/CovReverb_control.h>
#include <effect/Plugin_conv_reverb/CovReverb_SDK_API.h>
#include <malloc.h>
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

#define FILEOPEN 1

extern "C"
JNIEXPORT jint JNICALL
Java_com_engine_effect_ITbEffectProcessor_init(JNIEnv *env, jobject ins) {
    flag = 0;
    return flag;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_engine_effect_ITbEffectProcessor_process1(JNIEnv *env, jobject ins, jbyteArray samplesJni,
                                                   jint len,
                                                   jint channels, jint sampleRate) {
    if (FILEOPEN) {
        LOGI("Java_com_engine_effect_ITbEffectProcessor_process1 flag=%d", flag);
    }
    if (flag == -1) {
        return flag;
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

    //无符号整型，加起来和testIn不一定对得上
    unsigned char *data = (unsigned char *) env->GetByteArrayElements(samplesJni, 0);

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

    // Release<Type>ArrayElements(<Type>Array arr , <Type>* array , jint mode)
    //
    //用这个函数可以选择将如何处理Java跟C++的数组，是提交，还是撤销等，内存释放还是不释放等
    //
    //mode可以取下面的值:
    //
    //0 ：对Java的数组进行更新并释放C/C++的数组
    //
    //JNI_COMMIT ：对Java的数组进行更新但是不释放C/C++的数组
    //
    //JNI_ABORT：对Java的数组不进行更新,释放C/C++的数组
    env->ReleaseByteArrayElements(samplesJni, reinterpret_cast<jbyte *>(data), 0);
    return flag;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_engine_effect_ITbEffectProcessor_process2(JNIEnv *env, jobject ins, jbyteArray samplesJni,
                                                   jint len,
                                                   jint channels, jint sampleRate) {
    if (FILEOPEN) {
        LOGI("Java_com_engine_effect_ITbEffectProcessor_process2 flag=%d", flag);
    }
    if (flag == -1) {
        return flag;
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

    unsigned char *data = (unsigned char *) env->GetByteArrayElements(samplesJni, 0);
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
    // Release<Type>ArrayElements(<Type>Array arr , <Type>* array , jint mode)
    //
    //用这个函数可以选择将如何处理Java跟C++的数组，是提交，还是撤销等，内存释放还是不释放等
    //
    //mode可以取下面的值:
    //
    //0 ：对Java的数组进行更新并释放C/C++的数组D/EngineManager
    //
    //JNI_COMMIT ：对Java的数组进行更新但是不释放C/C++的数组
    //
    //JNI_ABORT：对Java的数组不进行更新,释放C/C++的数组
    env->ReleaseByteArrayElements(samplesJni, reinterpret_cast<jbyte *>(data), 0);
    if (FILEOPEN) {
        LOGI("19");
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
