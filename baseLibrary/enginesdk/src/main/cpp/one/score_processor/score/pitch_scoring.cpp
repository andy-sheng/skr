#include "pitch_scoring.h"

#define LOG_TAG "PitchScoring"

#define RECORD_WAVY_LINE_DELAY          150     //为了打分点更准确。延迟一段时间

#define LOGOPEN 0

PitchScoring::PitchScoring() {
    calBasebandUtil = new CCalcBaseband();
    isInitialization = false;
    mNotesMaxLen = 0;
    mCurScore = 0;
    mLastScore = 0;
    mCurrentLineLevelSum = 0;
    mCurrentLineSampleCount = 0;
}

PitchScoring::~PitchScoring() {

}

void PitchScoring::getRenderData(long currentTimeMills, float *meta) {
    if (isNeedDestroy) {
        return;
    }
    float currentLevel = -10;
    float conf = -10;
    float lastLevel = -1;
    RecordLevel *headRecordLevel = NULL;
    RecordLevel *preRecordLevel = NULL;
    int resultCode = recordLevelQueue->peek(&headRecordLevel, false);
    if (resultCode > 0) {
        preRecordLevel = headRecordLevel;
    }
    while ((resultCode > 0) &&
           (headRecordLevel->getTimeMills() + UNIT_LEN_IN_MS) < currentTimeMills) {
        preRecordLevel = headRecordLevel;
        recordLevelQueue->pop(false);
        resultCode = recordLevelQueue->peek(&headRecordLevel, false);
    }
    if (resultCode > 0) {
        currentLevel = headRecordLevel->getLevel();
        conf = headRecordLevel->getConf();
    }
    if (NULL != meta) {
        meta[0] = currentLevel;
        meta[1] = conf;
    }
}

// 44100 1 2
void
PitchScoring::onInit(int sampleRateParam, int channelNumParam, int bitParam, char *melFilePath) {
    LOGI("onInit melFilePath=%s", melFilePath);
    //1:初始化参数
    sampleRate = sampleRateParam;
    channelNum = channelNumParam;
    bitDepth = bitParam;
//	pcmFile = fopen("/mnt/sdcard/test.pcm", "wb+");
    //2:解密mel文件并且将note音符全部取出来
    MelChordAna melChordAna(sampleRate * channelNum);
    melChordAna.InitByMelFile(melFilePath, 0);
    mNotesMaxLen = melChordAna.GetToneScoreTempl(mMelodyNotes);
    //3:对每个音符的开始和结束时间进行修正
//    for (int i = 0; i < mMelodyNotes.size(); i++) {
//        MelodyNote &melodyNote = mMelodyNotes.at(i);
//        melodyNote.beginTimeMs += RECORD_WAVY_LINE_DELAY;
//        melodyNote.endTimeMs += RECORD_WAVY_LINE_DELAY;
//    }
    //4:开启新的线程初始化基频处理器
    pthread_create(&initBasebandThread, NULL, startInitBasebandThread, this);
}

void *PitchScoring::startInitBasebandThread(void *ptr) {
    PitchScoring *scoring = (PitchScoring *) ptr;
    if (scoring->calBasebandUtil) {
        scoring->calBasebandUtil->Init(scoring->sampleRate, scoring->channelNum);
    }
    scoring->isInitialization = true;
    return NULL;
}

void PitchScoring::doScoring(short *buffer, int bufferSize, long currentTime) {
    if (!isInitialization || isNeedDestroy) {
        return;
    }
    BaseScoring::mergeRecordRaw(buffer, bufferSize, currentTime);
}

void PitchScoring::processRecordLevel(RecordLevel *recordLevel) {
    if (isNeedDestroy) {
        return;
    }
    //1:找出对应的音符位置
    long currentTimeMills = recordLevel->getTimeMills();
    int flag = 0;
    int singingIndex = getSingingIndex(currentTimeMills, &flag);
    if (LOGOPEN) {
        LOGI("processRecordLevel singingIndex=%d currentTimeMills=%ld", singingIndex,
             currentTimeMills);
    }
    if (singingIndex < 0) {
        //没找到这句对应的音符位置，直接返回
        mCurScore = 0;
        return;
    }
    MelodyNote melodyNote = mMelodyNotes.at(singingIndex);
    // 取出目标音高
    short targetNote = melodyNote.note_org;

    float conf = 0.0;
    float f0 = 0.0;
    //2:计算基频
    if (calBasebandUtil) {
//		fwrite(recordLevel->getSamples(), 2, AUDIO_DATA_SAMPLE_FOR_SCORE, pcmFile);
        calBasebandUtil->getFreqAndConf(recordLevel->getSamples(),
                                        AUDIO_DATA_SAMPLE_FOR_SCORE, &f0, &conf);
    }
    float note = -1;
    //3:根据基频计算note
    if (conf > 0.8) {
        note = (float) (69000.5 + 12000 * (log10(f0 / 440.0) / log10(2)));
        note = float((int) note % 12000) / 1000.0;
    }
    if (LOGOPEN) {
        LOGI("processRecordLevel conf=%.3f,note=%.2f,targetNote=%hd", conf, note, targetNote);
    }
    bool noteValid = false;
    //4:根据note计算score
    if (note > -0.5) {
        noteValid = true;
        float diffnote = noteDiff(note, targetNote);

        if (LOGOPEN) {
            LOGI("processRecordLevel diffnote=%.2f ", fabs(diffnote));
        }
        //diffnote = diffnote / 2.0;
        if (fabs(diffnote) <= 1.0) {
            diffnote = diffnote * fabs(diffnote);
        }
        int a = 100 - 100 * (fabs(diffnote) - 0.0);
        int b = mLastScore * 0.4;
        if (a > b) {
            mCurScore = a;
        } else {
            mCurScore = b;
//            if (mCurScore == 0) {
//                // 声音够大，也给他一个分
//                mCurScore = note * 10;
//            }
        }
//        if (fabs(diffnote) <= 1.0) {
//            mCurScore = 100 - 100 * (fabs(diffnote) - 0.0);
//        } else {
//            mCurScore = mLastScore * 0.4;
//        }
    } else {
        mCurScore = mLastScore * 0.2;
    }

    if (mCurScore > 100) {
        if (LOGOPEN) {
            LOGI("processRecordLevel mCurScore >100");
        }
        mCurScore = 100;
    }
    if (mCurScore < 0) {
        mCurScore = 0;
    }
    mLastScore = mCurScore;
    if (LOGOPEN) {
        LOGI("processRecordLevel mCurScore=%d targetNote=%hd", mCurScore, targetNote);
    }
    //5:根据score进行计算统计数据
    if (noteValid) {
        mCurrentLineLevelSum += mCurScore;
        mCurrentLineSampleCount++;
        if (LOGOPEN) {
            LOGI("processRecordLevel 得分有效 mCurrentLineLevelSum=%d mCurrentLineSampleCount=%d",
                 mCurrentLineLevelSum, mCurrentLineSampleCount);
        }
    }
}

int PitchScoring::getScore() {
    int lineScore = 0;
    //1:计算分数
    if (mCurrentLineSampleCount > 0) {
        lineScore = mCurrentLineLevelSum / mCurrentLineSampleCount;
    }
    float x = (float) lineScore * 0.01;
    x = (x * (1 - x) + x) * 0.2 + 0.8 * x;
    lineScore = 100 * (x * (1 - x) + x);
    if (LOGOPEN) {
        LOGI("getScore mCurrentLineSampleCount:%d mCurrentLineLevelSum:%d lineScore:%d",
             mCurrentLineSampleCount, mCurrentLineLevelSum, lineScore);
    }
    //2:清空统计数据
    mCurrentLineSampleCount = 0;
    mCurrentLineLevelSum = 0;
    return lineScore;
}

int PitchScoring::getSingingIndex(long currentTimeMills, int *flag) {
    //LOGI("currentTimeMills=%ld", currentTimeMills);
    int singingIndex = -1;
    int f = 100;// 宽松一点
    bool find = false;
    for (int j = 0; j < 1 && !find; j++) {
        f = j * UNIT_LEN_IN_MS / 5;// 不停地提高容忍度，确保能找到对应的音符，允许前后差最多250ms
        for (int i = 0; i < mMelodyNotes.size(); i++) {
            MelodyNote melodyNote = mMelodyNotes.at(i);
            int beginTimeMs = melodyNote.beginTimeMs;
            int endTimeMs = melodyNote.endTimeMs;
            if (beginTimeMs - f <= currentTimeMills && currentTimeMills < endTimeMs + f) {
                singingIndex = i;
                find = true;
                if (LOGOPEN) {
                    LOGI("");
                    LOGI("getSingingIndex i=%d beginTimeMs=%d endTimeMs=%d currentTimeMills=%ld note=%hd f=%d",
                         i, beginTimeMs, endTimeMs, currentTimeMills, melodyNote.note_org, f);
                }
                break;
            }
        }
        *flag = j;
    }

    return singingIndex;
}

float PitchScoring::noteDiff(float curNote, short targetNote) {
    float diff = curNote - targetNote;
    diff = diff > 6 ? (diff - 12) : diff;
    diff = diff < -6 ? (diff + 12) : diff;
    return diff;
}

int PitchScoring::destroy() {
    BaseScoring::destroy();
    if (NULL != calBasebandUtil && isInitialization) {
        delete calBasebandUtil;
        calBasebandUtil = NULL;
        isInitialization = false;
    }
//	fclose(pcmFile);
    return 0;
}
