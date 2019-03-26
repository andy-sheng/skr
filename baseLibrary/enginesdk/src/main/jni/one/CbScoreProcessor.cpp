#include <jni.h>
#include <string>
#include "com_engine_score_ICbScoreProcessor.h"
#include <score_processor/score/base_scoring.h>
#include <score_processor/score/pitch_scoring.h>
#include <score_processor/score2/calc_score.hpp>

#define LOG_TAG "ICbScoreProcessor"

#define FILEOPEN 0

FILE *scoreFile1 = NULL;
BaseScoring *scoring1 = NULL;

FILE *scoreFile2 = NULL;
CalcScore *scoring2 = NULL;
long curTs = 0;

extern "C"
JNIEXPORT jint JNICALL
Java_com_engine_score_ICbScoreProcessor_process1(JNIEnv *env, jobject instance, jboolean needScore,
                                                 jboolean needRestart,
                                                 jbyteArray samplesJni,
                                                 jint length, jint channels, jint samplesPerSec,
                                                 jlong currentTimeMills,
                                                 jstring melFile_) {
    if (FILEOPEN) {
        LOGI("1needScore=%d,needRestart=%d,currentTimeMills=%lld", needScore, needRestart,
             currentTimeMills);
    }
    if (!needScore) {
        if (scoring1 != NULL) {
            scoring1->destroy();
            delete scoring1;
            scoring1 = NULL;
        }
        if (FILEOPEN && scoreFile1 != NULL) {
            fclose(scoreFile1);
            scoreFile1 = NULL;
        }
        return -1;
    }
    if (needRestart) {
        if (scoring1 != NULL) {
            scoring1->destroy();
            delete scoring1;
            scoring1 = NULL;
        }
        if (FILEOPEN && scoreFile1 != NULL) {
            fclose(scoreFile1);
            scoreFile1 = NULL;
        }
    }
    if (scoring1 == NULL) {
        const char *melFilePath = env->GetStringUTFChars(melFile_, 0);
        scoring1 = new PitchScoring();

        scoring1->getMinBufferSize(samplesPerSec,
                                   1, 2 * 8, length / 2 / channels);
        scoring1->init(samplesPerSec, 1,
                       2 * 8, (char *) melFilePath);
        env->ReleaseStringUTFChars(melFile_, melFilePath
        );
    }

    byte *data = (byte *) env->GetByteArrayElements(samplesJni, 0);
// 2048 512 下来
    short *samples = (short *) data; // 长度只有1024了

//2:送入打分处理器
    if (NULL != scoring1) {
        if (channels == 2) {
            for (int i = 0; i < length / 2 / channels; i++) {
                samples[i] = samples[i * 2];// 0123456-->0246
            }
// 512个 short 类型
            if (FILEOPEN && scoreFile1 == NULL) {
                scoreFile1 = fopen("/mnt/sdcard/score_input1.pcm", "wb+");
            }
            if (FILEOPEN && scoreFile1 != NULL) {
                fwrite(samples, sizeof(short), length / 2 / channels, scoreFile1);
            }
            scoring1->doScoring(samples, length / 2 / channels, currentTimeMills);
        } else if (channels == 1) {
//2:送入打分处理器
// 512个 short 类型
            if (FILEOPEN && scoreFile1 == NULL) {
                scoreFile1 = fopen("/mnt/sdcard/score_input1.pcm", "wb+");
            }
            if (FILEOPEN && scoreFile1 != NULL) {
                fwrite(samples, sizeof(short), length / 2 / channels, scoreFile1);
            }
            scoring1->doScoring(samples, length / 2, currentTimeMills);
        }
    }
    env->ReleaseByteArrayElements(samplesJni, (jbyte *) data, JNI_ABORT);
    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_engine_score_ICbScoreProcessor_getScore1(JNIEnv *env, jobject instance) {
    if (NULL != scoring1) {
        return scoring1->getScore();
    }
    return -1;
}


extern "C"
JNIEXPORT jint JNICALL
Java_com_engine_score_ICbScoreProcessor_process2(JNIEnv *env, jobject instance, jboolean needScore,
                                                 jboolean needRestart,
                                                 jbyteArray samplesJni,
                                                 jint length, jint channels, jint samplesPerSec,
                                                 jlong currentTimeMills,
                                                 jstring melFile_) {


    if (FILEOPEN) {
        LOGI("2needScore=%d,needRestart=%d,currentTimeMills=%lld", needScore, needRestart,
             currentTimeMills);
    }
    if (!needScore) {
        if (scoring2 != NULL) {
            //scoring2->destroy();
            delete scoring2;
            scoring2 = NULL;
        }
        if (FILEOPEN && scoreFile2 != NULL) {
            fclose(scoreFile2);
            scoreFile2 = NULL;
        }
        return -1;
    }
    if (needRestart) {
        if (scoring2 != NULL) {
            //scoring2->destroy();
            delete scoring2;
            scoring2 = NULL;
        }
        if (FILEOPEN && scoreFile2 != NULL) {
            fclose(scoreFile2);
            scoreFile2 = NULL;
        }
    }
    if (scoring2 == NULL) {
        const char *melFilePath = env->GetStringUTFChars(melFile_, 0);
        scoring2 = new CalcScore(samplesPerSec);
        scoring2->LoadMelp(melFilePath, currentTimeMills);
        env->ReleaseStringUTFChars(melFile_, melFilePath);
    }

    byte *data = (byte *) env->GetByteArrayElements(samplesJni, 0);
// 2048 512 下来
    short *samples = (short *) data; // 长度只有1024了
    curTs = currentTimeMills;
//2:送入打分处理器
    if (NULL != scoring2) {
        if (channels == 2) {
            for (int i = 0; i < length / 2 / channels; i++) {
                samples[i] = samples[i * 2];// 0123456-->0246
            }
// 512个 short 类型
            if (FILEOPEN && scoreFile2 == NULL) {
                scoreFile2 = fopen("/mnt/sdcard/score_input2.pcm", "wb+");
            }
            if (FILEOPEN && scoreFile2 != NULL) {
                fwrite(samples, sizeof(short), length / 2 / channels, scoreFile2);
            }
            scoring2->Flow(samples, length / 2 / channels);
        } else if (channels == 1) {
//2:送入打分处理器
// 512个 short 类型
            if (FILEOPEN && scoreFile2 == NULL) {
                scoreFile2 = fopen("/mnt/sdcard/score_input2.pcm", "wb+");
            }
            if (FILEOPEN && scoreFile2 != NULL) {
                fwrite(samples, sizeof(short), length / 2 / channels, scoreFile2);
            }
            scoring2->Flow(samples, length / 2);
        }
    }
    env->ReleaseByteArrayElements(samplesJni, (jbyte *) data, JNI_ABORT);
    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_engine_score_ICbScoreProcessor_getScore2(JNIEnv *env, jobject instance) {
    if (NULL != scoring2) {
        if (curTs > 0) {
            return scoring2->GetScore(curTs);
        }
    }
    return -1;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_engine_score_ICbScoreProcessor_destroyScoreProcessor(JNIEnv *env, jobject instance) {
    if (NULL != scoring1) {
        scoring1->destroy();
        delete scoring1;
        scoring1 = NULL;
    }
    if (NULL != scoring2) {
        //scoring2->destroy();
        delete scoring2;
        scoring2 = NULL;
    }
    if (FILEOPEN && scoreFile1 != NULL) {
        fclose(scoreFile1);
        scoreFile1 = NULL;
    }
    if (FILEOPEN && scoreFile2 != NULL) {
        fclose(scoreFile2);
        scoreFile2 = NULL;
    }
    curTs = 0;
}


