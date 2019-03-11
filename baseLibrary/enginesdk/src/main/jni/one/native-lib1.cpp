#include <jni.h>
#include <string>
#include "com_engine_score_ICbScoreProcessor.h"
#include <score_processor/score/base_scoring.h>
#include <score_processor/score/pitch_scoring.h>

#define LOG_TAG "ICbScoreProcessor"

#define FILEOPEN 0

FILE *scoreFile = NULL;
BaseScoring *scoring = NULL;

extern "C"
JNIEXPORT jint JNICALL
Java_com_engine_score_ICbScoreProcessor_process1(JNIEnv *env, jobject instance, jboolean needScore,
                                                 jboolean needRestart,
                                                 jbyteArray samplesJni,
                                                 jint length, jint channels, jint samplesPerSec,
                                                 jlong currentTimeMills,
                                                 jstring melFile_) {
    if (FILEOPEN) {
        LOGI("needScore=%d,needRestart=%d,currentTimeMills=%lld", needScore, needRestart,
             currentTimeMills);
    }
    if (!needScore) {
        if (FILEOPEN && scoreFile != NULL) {
            fclose(scoreFile);
            scoreFile = NULL;
        }
        return -1;
    }
    if (needRestart) {
        if (scoring != NULL) {
            scoring->destroy();
            delete scoring;
            scoring = NULL;
        }
        if (FILEOPEN && scoreFile != NULL) {
            fclose(scoreFile);
            scoreFile = NULL;
        }
    }
    if (scoring == NULL) {
        const char *melFilePath = env->GetStringUTFChars(melFile_, 0);
        scoring = new PitchScoring();

        scoring->getMinBufferSize(samplesPerSec,
                                  1, 2 * 8, length / 2 / channels);
        scoring->init(samplesPerSec, 1,
                      2 * 8, (char *) melFilePath);
        env->ReleaseStringUTFChars(melFile_, melFilePath
        );
    }

    byte *data = (byte *) env->GetByteArrayElements(samplesJni, 0);
// 2048 512 下来
    short *samples = (short *) data; // 长度只有1024了

//2:送入打分处理器
    if (NULL != scoring) {
        if (channels == 2) {
            for (int i = 0; i < length / 2 / channels; i++) {
                samples[i] = samples[i * 2];// 0123456-->0246
            }
// 512个 short 类型
            if (FILEOPEN && scoreFile == NULL) {
                scoreFile = fopen("/mnt/sdcard/score_input.pcm", "wb+");
            }
            if (FILEOPEN && scoreFile != NULL) {
                fwrite(samples, sizeof(short), length / 2 / channels, scoreFile);
            }
            scoring->doScoring(samples, length / 2 / channels, currentTimeMills);
        } else if (channels == 1) {
//2:送入打分处理器
// 512个 short 类型
            scoring->doScoring(samples, length / 2, currentTimeMills);
        }
    }
    env->ReleaseByteArrayElements(samplesJni, (jbyte *) data, JNI_ABORT);
    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_engine_score_ICbScoreProcessor_getScore1(JNIEnv *env, jobject instance) {
    if (NULL != scoring) {
        return scoring->getScore();
    }
    return -1;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_engine_score_ICbScoreProcessor_destroyScoreProcessor(JNIEnv *env, jobject instance) {
    if (NULL != scoring) {
        scoring->destroy();
        delete scoring;
        scoring = NULL;
    }
    if (FILEOPEN && scoreFile != NULL) {
        fclose(scoreFile);
        scoreFile = NULL;
    }
}

