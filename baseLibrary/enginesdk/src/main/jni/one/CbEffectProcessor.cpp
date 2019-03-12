#include <jni.h>
#include <string>
#include "com_engine_effect_ICbEffectProcessor.h"
#include "audio_effect_processor.h"
#include "audio_effect_processor_factory.h"
#include "audio_effect_adapter.h"

#define LOG_TAG "IFAudioEffectEngine"

#define FILEOPEN 1

FILE *effectFile1 = NULL;
AudioEffectProcessor *effectProcessor = NULL;

JNIEXPORT void JNICALL
Java_com_engine_effect_IFAudioEffectEngine_initAudioEffect(JNIEnv *env, jobject instance,
                                                           jobject audioEffectJNI) {
    if (effectProcessor != NULL) {
        effectProcessor->destroy();
        delete effectProcessor;
        effectProcessor = NULL;
    }
    effectProcessor = AudioEffectProcessorFactory::GetInstance()->buildLiveAudioEffectProcessor();
    AudioEffect *audioEffect = AudioEffectAdapter::GetInstance()->buildAudioEffect(audioEffectJNI,
                                                                                   env);
//    file = fopen("/mnt/sdcard/raw.pcm", "wb+");
    effectProcessor->init(audioEffect);
}

JNIEXPORT void JNICALL
Java_com_engine_effect_IFAudioEffectEngine_destroyAudioEffect
        (JNIEnv *env, jobject obj) {
    if (NULL != effectProcessor) {
        effectProcessor->destroy();
        delete effectProcessor;
        effectProcessor = NULL;
    }
}

JNIEXPORT void JNICALL Java_com_engine_effect_IFAudioEffectEngine_processAudioEffect
        (JNIEnv *env, jobject obj, jbyteArray samplesJni, jint len,
         jint channels, jint sampleRate) {
    if (NULL != effectProcessor) {
        byte *data = (byte *) env->GetByteArrayElements(samplesJni, 0);
        // 2048 512 下来
        short *samples = (short *) data; // 长度只有1024了

        //1:转换为单声道数据
        if (channels == 2) {
            for (int i = 0; i < len / 2 / channels; i++) {
                samples[i] = samples[i * 2];// 0123456-->0246
            }
        }else if(channels == 1){
        }

        //3:送入音效处理器
        if (FILEOPEN) {
            LOGI("before process");
        }
        effectProcessor->process(samples, len / 2, 0, 0);
        if (FILEOPEN) {
            LOGI("after process");
        }
        // 将 samples 赋值 给 samplesjni
//        env->SetByteArrayRegion(samplesJni, 0, len/2/channels, (jbyte *) samples);
        env->ReleaseByteArrayElements(samplesJni, (jbyte *) data, 0);
    }
}


