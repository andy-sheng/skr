#include <jni.h>
#include <string>
#include "media_ushow_audio_effect_IFAudioEffectEngine.h""
#include "audio_effect_adapter.h"
#include "audio_effect_live_processor.h"
#include "audio_effect_processor_factory.h"
#include "CommonTools.h"

#define LOG_TAG "IFAudioEffectEngine"


AudioEffectProcessor* effectProcessor = NULL;

JNIEXPORT void JNICALL Java_media_ushow_audio_1effect_IFAudioEffectEngine_initAudioEffect
        (JNIEnv * env, jobject obj, jobject audioEffectJNI) {
    effectProcessor = AudioEffectProcessorFactory::GetInstance()->buildLiveAudioEffectProcessor();
    AudioEffect* audioEffect = AudioEffectAdapter::GetInstance()->buildAudioEffect(audioEffectJNI, env);
    effectProcessor->init(audioEffect);
}

JNIEXPORT void JNICALL Java_media_ushow_audio_1effect_IFAudioEffectEngine_setAudioEffect
        (JNIEnv * env, jobject obj, jobject audioEffectJNI) {
    if(NULL != effectProcessor) {
        AudioEffect* audioEffect = AudioEffectAdapter::GetInstance()->buildAudioEffect(audioEffectJNI, env);
        effectProcessor->setAudioEffect(audioEffect);
    }
}

JNIEXPORT void JNICALL Java_media_ushow_audio_1effect_IFAudioEffectEngine_processAudioFrames
        (JNIEnv * env, jobject obj, jbyteArray samplesJni, jint numOfSamples, jint bytesPerSample, jint channels, jint sampleRate) {
    if(NULL != effectProcessor) {
        byte* data = (byte*)env->GetByteArrayElements(samplesJni, 0);
        short* samples = (short*)data;
        int length = numOfSamples / sizeof(short);
        if(channels == 2) {
            for(int i = 0; i < length / 2; i++) {
                samples[i] = samples[i * 2];
            }
        }
        effectProcessor->process(samples, length, 0, 0);
        env->SetByteArrayRegion(samplesJni, 0, numOfSamples, (jbyte*)samples);
        env->ReleaseByteArrayElements(samplesJni, (jbyte*)data, 0);
    }
}


JNIEXPORT void JNICALL Java_media_ushow_audio_1effect_IFAudioEffectEngine_destroyAudioEffect
        (JNIEnv * env, jobject obj){
    if(NULL != effectProcessor) {
        effectProcessor->destroy();
        delete effectProcessor;
        effectProcessor = NULL;
    }
}