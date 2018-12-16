#include <jni.h>
#include <string>
#include "media_ushow_audio_effect_IFAudioEffectEngine.h""
#include "audio_effect_adapter.h"
#include "audio_effect_live_processor.h"
#include "audio_effect_processor_factory.h"
#include "CommonTools.h"

#include "SMAudioEffectProcessor.h"

#define LOG_TAG "IFAudioEffectEngine"

//SMAudioEffectProcessor* effectProcessor = NULL;
AudioEffectProcessor* effectProcessor = NULL;
//FILE* sourcePCMFile = NULL;
//FILE* targetPCMFile = NULL;

JNIEXPORT void JNICALL Java_media_ushow_audio_1effect_IFAudioEffectEngine_initAudioEffect
        (JNIEnv * env, jobject obj, jobject audioEffectJNI) {
    effectProcessor = AudioEffectProcessorFactory::GetInstance()->buildLiveAudioEffectProcessor();
    AudioEffect* audioEffect = AudioEffectAdapter::GetInstance()->buildAudioEffect(audioEffectJNI, env);
    effectProcessor->init(audioEffect);
//    sourcePCMFile = fopen("/mnt/sdcard/source.pcm", "wb+");
    //targetPCMFile = fopen("/mnt/sdcard/target.pcm", "wb+");
}

JNIEXPORT void JNICALL Java_media_ushow_audio_1effect_IFAudioEffectEngine_setAudioEffect
        (JNIEnv * env, jobject obj, jobject audioEffectJNI) {
    if(NULL != effectProcessor) {
        AudioEffect* audioEffect = AudioEffectAdapter::GetInstance()->buildAudioEffect(audioEffectJNI, env);
        effectProcessor->setAudioEffect(audioEffect);
//        effectProcessor->onEffectSelect(EFFECT_WARM);
    }
}

JNIEXPORT void JNICALL Java_media_ushow_audio_1effect_IFAudioEffectEngine_processAudioFrames
        (JNIEnv * env, jobject obj, jbyteArray samplesJni, jint numOfSamples, jint bytesPerSample, jint channels, jint sampleRate) {
//    if(NULL == effectProcessor) {
//        effectProcessor = new SMAudioEffectProcessor();
//        effectProcessor->initEffect(sampleRate, channels, numOfSamples);
//    }
    if(NULL != effectProcessor) {
        byte* data = (byte*)env->GetByteArrayElements(samplesJni, 0);
        short* samples = (short*)data;
        int length = numOfSamples * 2;
//        fwrite(samples, sizeof(short), length, sourcePCMFile);
        if(channels == 2) {
            for(int i = 0; i < length / 2; i++) {
                samples[i] = samples[i * 2];
            }
        }
        effectProcessor->process(samples, length, 0, 0);
        //fwrite(samples, sizeof(short), length, targetPCMFile);
//        float* bufferInFloat = new float[length];
//        short_to_float(samples, bufferInFloat, length);
//        effectProcessor->Process(bufferInFloat, length, 0);
//        float_to_short(bufferInFloat, samples, length);
//        if(bufferInFloat) {
//            delete[] bufferInFloat;
//        }
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
//    if(sourcePCMFile) {
//        fclose(sourcePCMFile);
//    }
    //if(targetPCMFile) {
      //  fclose(targetPCMFile);
    //}
}