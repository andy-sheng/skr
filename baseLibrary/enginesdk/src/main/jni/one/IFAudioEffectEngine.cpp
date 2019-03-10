#include <jni.h>
#include <string>
#include "media_ushow_audio_effect_IFAudioEffectEngine.h""
#include "audio_effect_adapter.h"
#include "audio_effect_live_processor.h"
#include "audio_effect_processor_factory.h"
#include "CommonTools.h"
#include "base_scoring.h"
#include "pitch_scoring.h"
#include "CCalcBaseband.h"
#include "mel_chord_ana.h"

#define LOG_TAG "IFAudioEffectEngine"


AudioEffectProcessor *effectProcessor = NULL;
BaseScoring* scoring = NULL;
//FILE* file;
JNIEXPORT void JNICALL Java_media_ushow_audio_1effect_IFAudioEffectEngine_initAudioEffect
        (JNIEnv *env, jobject obj, jobject audioEffectJNI) {
    effectProcessor = AudioEffectProcessorFactory::GetInstance()->buildLiveAudioEffectProcessor();
    AudioEffect *audioEffect = AudioEffectAdapter::GetInstance()->buildAudioEffect(audioEffectJNI,
                                                                                   env);
//    file = fopen("/mnt/sdcard/raw.pcm", "wb+");
    effectProcessor->init(audioEffect);
}

JNIEXPORT void JNICALL Java_media_ushow_audio_1effect_IFAudioEffectEngine_setAudioEffect
        (JNIEnv *env, jobject obj, jobject audioEffectJNI) {
    if (NULL != effectProcessor) {
        AudioEffect *audioEffect = AudioEffectAdapter::GetInstance()->buildAudioEffect(
                audioEffectJNI, env);
        effectProcessor->setAudioEffect(audioEffect);
    }
}

JNIEXPORT void JNICALL Java_media_ushow_audio_1effect_IFAudioEffectEngine_processAudioFrames
        (JNIEnv *env, jobject obj, jbyteArray samplesJni, jint numOfSamples, jint bytesPerSample,
         jint channels, jint sampleRate, jlong currentTimeMills) {
    if (NULL != effectProcessor) {
        byte *data = (byte *) env->GetByteArrayElements(samplesJni, 0);
        // 2048 512 下来
        short *samples = (short *) data; // 长度只有1024了
        //1:转换为单声道数据
        int length = numOfSamples * 2; // 长度变为1024
//        fwrite(samples, 2, length, file);
        if (channels == 2) {
            for (int i = 0; i < length / 2; i++) {
                samples[i] = samples[i * 2];// 0123456-->0246
            }
            //2:送入打分处理器
            if(NULL != scoring) {
                // 512个 short 类型
                scoring->doScoring(samples, length / 2, currentTimeMills);
            }
        }else if(channels == 1){
            //2:送入打分处理器
            if(NULL != scoring) {
                // 512个 short 类型
                scoring->doScoring(samples, length, currentTimeMills);
            }
        }

        //3:送入音效处理器
        //effectProcessor->process(samples, length, 0, 0);
        env->SetByteArrayRegion(samplesJni, 0, numOfSamples, (jbyte *) samples);
        env->ReleaseByteArrayElements(samplesJni, (jbyte *) data, 0);
    }
}


JNIEXPORT void JNICALL Java_media_ushow_audio_1effect_IFAudioEffectEngine_destroyAudioEffect
        (JNIEnv *env, jobject obj) {
    if (NULL != effectProcessor) {
        effectProcessor->destroy();
        delete effectProcessor;
        effectProcessor = NULL;
    }
//    if(file) {
//        fclose(file);
//    }
}


/*** Begin Score Processor ***/
extern "C"
JNIEXPORT jint JNICALL
Java_media_ushow_score_ScoreProcessorService_init(JNIEnv *env, jobject instance, jint sampleRate,
                                                  jint channels, jint sampleFormat,
                                                  jint bufferSizeInShorts, jstring melFile_) {
    const char *melFilePath = env->GetStringUTFChars(melFile_, 0);
    scoring = new PitchScoring();
    LOGI("before scoring->getMinBufferSize channels is %d bufferSizeInShorts is %d\n", channels, bufferSizeInShorts);
    bufferSizeInShorts = scoring->getMinBufferSize(sampleRate, channels, sampleFormat, bufferSizeInShorts);
    LOGI("bufferSizeInShorts is %d \n", bufferSizeInShorts);
    scoring->init(sampleRate, channels, sampleFormat, (char*)melFilePath);
    LOGI("after scoring->init \n");
    env->ReleaseStringUTFChars(melFile_, melFilePath);
    return bufferSizeInShorts;

}


extern "C"
JNIEXPORT jint JNICALL
Java_media_ushow_score_ScoreProcessorService_getScore(JNIEnv *env, jobject instance) {
    int score = -1;
    if (NULL != scoring) {
        score = scoring->getScore();
    }
    return score;
}

extern "C"
JNIEXPORT void JNICALL
Java_media_ushow_score_ScoreProcessorService_setDestroyScoreProcessorFlag(JNIEnv *env,
                                                                          jobject instance,
                                                                          jboolean destroyScoreProcessorFlag) {
    if(NULL != scoring) {
        scoring->setNeedDestroy(destroyScoreProcessorFlag);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_media_ushow_score_ScoreProcessorService_destroy(JNIEnv *env, jobject instance) {
    if(NULL != scoring) {
        scoring->destroy();
        delete scoring;
        scoring = NULL;
    }
}
