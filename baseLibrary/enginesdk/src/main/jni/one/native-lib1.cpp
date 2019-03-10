#include <jni.h>
#include <string>
#include "audio_effect_adapter.h"
#include "audio_effect_live_processor.h"
#include "audio_effect_processor_factory.h"
#include "SMAudioEffectProcessor.h"
#include "CommonTools.h"

extern "C"
JNIEXPORT jstring
JNICALL
Java_media_ushow_audio_1effect_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT jint JNICALL
Java_media_ushow_audio_1effect_AudioEffectProcessor_process(JNIEnv *env, jclass type,
                                                            jstring inputFilePath_,
                                                            jstring outputFilePath_) {
    const char *inputFilePath = env->GetStringUTFChars(inputFilePath_, 0);
    const char *outputFilePath = env->GetStringUTFChars(outputFilePath_, 0);
    int channels = 1;
    int audioSampleRate = 44100;
    bool isUnAccom = true;
    long long startTimeMills = currentTimeMills();
    AudioEffect* audioEffect = AudioEffectAdapter::GetInstance()->buildDefaultAudioEffect(channels, audioSampleRate, isUnAccom);
    AudioEffectProcessor* effectProcessor = AudioEffectProcessorFactory::GetInstance()->buildLiveAudioEffectProcessor();
    effectProcessor->init(audioEffect);
    FILE* inputFile = fopen(inputFilePath, "rb+");
    FILE* outputFile = fopen(outputFilePath, "wb+");
    if(inputFile && outputFile) {
        int bufferSize = 1024 * 10;
        SInt16* buffer = new SInt16[bufferSize];
        int actualSize = 0;
        while((actualSize = fread(buffer, sizeof(SInt16), bufferSize, inputFile)) > 0) {
            //process
//            memset(buffer, 0, sizeof(SInt16) * bufferSize);
            for(int i = 0; i < actualSize / 2; i++) {
                buffer[i] = buffer[i * 2];
            }
            effectProcessor->process(buffer, actualSize, 0, 0);
            fwrite(buffer, sizeof(SInt16), actualSize, outputFile);
        }
        if(buffer) {
            delete[] buffer;
        }
        fclose(inputFile);
        fclose(outputFile);
    }

    if (NULL != effectProcessor) {
        effectProcessor->destroy();
        delete effectProcessor;
        effectProcessor = NULL;
    }
    env->ReleaseStringUTFChars(inputFilePath_, inputFilePath);
    env->ReleaseStringUTFChars(outputFilePath_, outputFilePath);
    int wasteTimeMills = currentTimeMills() - startTimeMills;
    return wasteTimeMills;
}

extern "C"
JNIEXPORT jint JNICALL
Java_media_ushow_audio_1effect_AudioEffectProcessor_processWithEffect(JNIEnv *env, jclass type,
                                                                      jobject audioEffectJNI,
                                                                      jstring inputFilePath_,
                                                                      jstring outputFilePath_) {
    const char *inputFilePath = env->GetStringUTFChars(inputFilePath_, 0);
    const char *outputFilePath = env->GetStringUTFChars(outputFilePath_, 0);
    long long startTimeMills = currentTimeMills();
    AudioEffect* audioEffect = AudioEffectAdapter::GetInstance()->buildAudioEffect(audioEffectJNI, env);
    AudioEffectProcessor* effectProcessor = AudioEffectProcessorFactory::GetInstance()->buildLiveAudioEffectProcessor();
    effectProcessor->init(audioEffect);
    FILE* inputFile = fopen(inputFilePath, "rb+");
    FILE* outputFile = fopen(outputFilePath, "wb+");
    if(inputFile && outputFile) {
        int bufferSize = 1024 * 10;
        SInt16* buffer = new SInt16[bufferSize];
        int actualSize = 0;
        while((actualSize = fread(buffer, sizeof(SInt16), bufferSize, inputFile)) > 0) {
            //process
//            memset(buffer, 0, sizeof(SInt16) * bufferSize);
            for(int i = 0; i < actualSize / 2; i++) {
                buffer[i] = buffer[i * 2];
            }
            effectProcessor->process(buffer, actualSize, 0, 0);
            fwrite(buffer, sizeof(SInt16), actualSize, outputFile);
        }
        if(buffer) {
            delete[] buffer;
        }
        fclose(inputFile);
        fclose(outputFile);
    }

    if (NULL != effectProcessor) {
        effectProcessor->destroy();
        delete effectProcessor;
        effectProcessor = NULL;
    }
    env->ReleaseStringUTFChars(inputFilePath_, inputFilePath);
    env->ReleaseStringUTFChars(outputFilePath_, outputFilePath);
    int wasteTimeMills = currentTimeMills() - startTimeMills;
    return wasteTimeMills;
}

extern "C"
JNIEXPORT jint JNICALL
Java_media_ushow_audio_1effect_AudioEffectProcessor_processWithExternalEffect(JNIEnv *env,
                                                                              jclass type,
                                                                              jobject audioEffectJNI,
                                                                              jstring inputFilePath_,
                                                                              jstring outputFilePath_) {
    const char *inputFilePath = env->GetStringUTFChars(inputFilePath_, 0);
    const char *outputFilePath = env->GetStringUTFChars(outputFilePath_, 0);
    int channels = 2;
    int audioSampleRate = 44100;
    long long startTimeMills = currentTimeMills();
    int bufferSize = 1024 * 5;
    SMAudioEffectProcessor* effectProcessor = new SMAudioEffectProcessor();
    effectProcessor->initEffect(audioSampleRate, channels, bufferSize);
    effectProcessor->onEffectSelect(EFFECT_WARM);
    FILE* inputFile = fopen(inputFilePath, "rb+");
    FILE* outputFile = fopen(outputFilePath, "wb+");
    if(inputFile && outputFile) {
        SInt16* buffer = new SInt16[bufferSize];
        float* bufferInFloat = new float[bufferSize];
        int actualSize = 0;
        while((actualSize = fread(buffer, sizeof(SInt16), bufferSize, inputFile)) > 0) {
            short_to_float(buffer, bufferInFloat, actualSize);
            effectProcessor->Process(bufferInFloat, actualSize, 0);
            float_to_short(bufferInFloat, buffer, actualSize);
            fwrite(buffer, sizeof(SInt16), actualSize, outputFile);
        }
        if(buffer) {
            delete[] buffer;
        }
        fclose(inputFile);
        fclose(outputFile);
    }

    if (NULL != effectProcessor) {
        delete effectProcessor;
        effectProcessor = NULL;
    }
    env->ReleaseStringUTFChars(inputFilePath_, inputFilePath);
    env->ReleaseStringUTFChars(outputFilePath_, outputFilePath);
    int wasteTimeMills = currentTimeMills() - startTimeMills;
    return wasteTimeMills;
}