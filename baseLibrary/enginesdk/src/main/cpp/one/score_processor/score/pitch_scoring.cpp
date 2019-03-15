#include "pitch_scoring.h"

#define LOG_TAG "PitchScoring"

#define RECORD_WAVY_LINE_DELAY          150     //为了打分点更准确。延迟一段时间

#define LOGOPEN 0

#define MODE 1

PitchScoring::PitchScoring() {
    calBasebandUtil = new CCalcBaseband();
    isInitialization = false;
    mNotesMaxLen = 0;
    mLastScore = 0;
    mCurrentLineLevelSum = 0;
    mCurrentLineSampleCount = 0;
    mIndex = -1;
    mIndexMaxScore = -1;
    mPcmTotal = 0;
    mPcmBufferSize = 0;
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

/**
 * 按普通人的听觉
    0 －2 0 分贝 很静、几乎感觉不到；
    2 0 －4 0 分贝安静、犹如轻声絮语；
    4 0 －6 0 分贝一般。普通室内谈话；
    6 0 －7 0 分贝吵闹、有损神经；
    7 0 －9 0 分贝很吵、神经细胞受到破坏。
    9 0 －1 0 0 分贝 吵闹加剧、听力受损；
    1 0 0 －1 2 0 分贝难以忍受、呆一分钟即暂时致聋。
 * @param buffer
 * @param bufferSize
 * @return
 */
int PitchScoring::computePcmDB(short *buffer, int bufferSize) {
    int db = 0;
    double sum = 0;

    for (int i = 0; i < bufferSize; i++) {
        short v = buffer[i];
        if (LOGOPEN && i % 1000 == 0) {
            LOGI("getPcmDB i=%d v=%hd", i, v);
        }
        sum += abs(v); //绝对值求和
    }
    if (LOGOPEN) {
        LOGI("getPcmDB sum=%lf", sum);
    }
    mPcmTotal += sum;
    mPcmBufferSize += bufferSize;
    sum = sum / bufferSize; //求平均值（2个字节表示一个振幅，所以振幅个数为：size/2个）

    if (sum > 0) {
        db = (int) (20.0 * log10(sum));
    }
    return db;
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
    computePcmDB(recordLevel->getSamples(), AUDIO_DATA_SAMPLE_FOR_SCORE);

    //1:找出对应的音符位置
    long currentTimeMills = recordLevel->getTimeMills();
    int curIndex = 0;
    int flag = 0;
    MelodyNote *melodyNote = getSingingIndex(currentTimeMills, &flag, &curIndex);

    if (melodyNote == NULL) {
        if (LOGOPEN) {
            LOGI("没找到这句对应的音符位置，直接返回");
        }
        mLastScore = 0;
        return;
    }
    float tsF = (currentTimeMills - melodyNote->beginTimeMs) /
                ((melodyNote->endTimeMs - melodyNote->beginTimeMs) * 1.0f);
    tsF = -2.0f * fabs(tsF - 0.5) + 0.5;
    // 取出目标音高
    short targetNote = melodyNote->note_org;
    float conf = 0.0;
    float f0 = 0.0;
    //2:计算基频
    if (calBasebandUtil) {
//		fwrite(recordLevel->getSamples(), 2, AUDIO_DATA_SAMPLE_FOR_SCORE, pcmFile);
        calBasebandUtil->getFreqAndConf(recordLevel->getSamples(),
                                        AUDIO_DATA_SAMPLE_FOR_SCORE, &f0, &conf);
    }
    float note = -1;
    // 十二平均律 ，不说话，按理 conf 会很低 ，如果置信度一致很低，考虑给低分
    // https://www.zhihu.com/question/282562547 计算 midi number
    /*
        人声：男：低音82～392Hz，基准音区64～523Hz；
        男中音123～493Hz，男高音164～698Hz；
        女：低音82～392Hz，基准音区160～1200Hz；
        女中音123～493Hz，女高音220～1.1KHz。
     */
    note = (float) (69000.5 + 12000 * (log10(f0 / 440.0) / log10(2)));
    note = float((int) note % 12000) / 1000.0;

    // 先算出 note 的diff
    float diffnote = noteDiff(note, targetNote);
    diffnote = fabs(diffnote);

    float confF = 10 * conf / 6.0 - 1 / 3.0;
    if (LOGOPEN) {
        LOGI("processRecordLevel conf=%.3f f0=%.2f confF=%.2f note=%.2f targetNote=%hd diffnote=%.2f lastScore=%d tsF=%.2f",
             conf, f0, confF,
             note, targetNote, diffnote,
             mLastScore, tsF);
    }
    if (confF < 0) {
        confF = 0;
    }
    float curScore = 0;
    bool noteValid = false;
    if (conf > 0.8) {
        if (diffnote <= 1.0) {
            curScore = 100 - 100 * diffnote * diffnote;
            // 得到了一个分，置信度也高 conf = 10/6 * a - 1/3
            // curScore = curScore * confF;
        } else {
            curScore = mLastScore * 0.4f;
        }
        noteValid = true;
    } else {
        curScore = mLastScore * 0.2f;
    }

//    if (diffnote <= 1.0) {
//        curScore = 100 - 100 * diffnote * diffnote;
//        // 得到了一个分，置信度也高 conf = 10/6 * a - 1/3
//        curScore = curScore * confF;
//    } else {
//        if(conf>0.8){
//            curScore = mLastScore - mLastScore * (diffnote * confF / 10.0);
//        }else{
//            // 唱得不准，考虑做衰减，衰减的程度由 置信度 和 diffNote共同确定
//            curScore = mLastScore - mLastScore * (diffnote * confF / 10.0);
//        }
//    }

//    if (LOGOPEN) {
//        LOGI("processRecordLevel 加分呗前 curScore=%.2f", curScore);
//    }
//    curScore = curScore + pcmF * 3;
//    if (LOGOPEN) {
//        LOGI("processRecordLevel 加演唱偏移因子前 curScore=%.2f", curScore);
//    }
//    curScore = curScore + tsF * curScore / 5.0f;
    if (LOGOPEN) {
        LOGI("processRecordLevel 最终 curScore=%.2f", curScore);
    }
    mLastScore = (int) curScore;

    if (curIndex > mIndex) {
        // 分数放入
        mIndex = curIndex;
        if (LOGOPEN) {
            LOGI("processRecordLevel 索引增加 分数放入 mPcmTotal=%d mPcmBufferSize=%d ", mPcmTotal,
                 mPcmBufferSize);
        }
        mCurrentLineLevelSum += mIndexMaxScore;
//      mCurrentLineLevelSum += (pcmF * 12);
        mCurrentLineSampleCount++;
        mIndexMaxScore = curScore;
    } else if (curIndex == mIndex) {
        if (curScore > mIndexMaxScore) {
            mIndexMaxScore = curScore;
        }
    } else {
        // 分数放入
        mIndex = curIndex;
        mCurrentLineSampleCount = 0;
        mCurrentLineLevelSum = 0;
        mIndexMaxScore = curScore;
    }
}

int PitchScoring::getScore() {
    int lineScore = 0;
    //1:计算分数
    if (mCurrentLineSampleCount > 0) {
        lineScore = mCurrentLineLevelSum / mCurrentLineSampleCount;
    }
    if (LOGOPEN) {
        LOGI("获取得分 mCurrentLineSampleCount:%d mCurrentLineLevelSum:%d lineScore:%d",
             mCurrentLineSampleCount, mCurrentLineLevelSum, lineScore);
    }

    // 50 - 79   90 - 99 加权了
    float x = (float) lineScore * 0.01;
    x = (x * (1 - x) + x) * 0.2 + 0.8 * x;
    lineScore = 100 * (x * (1 - x) + x);
    if (LOGOPEN) {
        LOGI("获取得分 lineScore1:%d", lineScore);
    }
    if (mPcmBufferSize > 0) {
        int average = mPcmTotal / mPcmBufferSize;
        int db = (int) (20.0 * log10(average));
        if (LOGOPEN) {
            LOGI("采样点平均值 mPcmTotal=%d mPcmBufferSize=%d average=%d db=%d", mPcmTotal,
                 mPcmBufferSize,
                 average, db);
        }
        float pcmF = (-1) * (db - 60) * (db - 60) / 625.0 + 1;
        mPcmTotal = 0;
        mPcmBufferSize = 0;
        int c = 7;
        if (db > 70) {
            lineScore = lineScore + c;
        } else {
            lineScore = lineScore + (db * c / 10 - 5 * c);
        }
    }
    if (LOGOPEN) {
        LOGI("获取得分 lineScore2:%d", lineScore);
    }
    if (lineScore < 0) {
        lineScore = 0;
    } else if (lineScore > 100) {
        lineScore = 100;
    }


    //2:清空统计数据
    mCurrentLineSampleCount = 0;
    mCurrentLineLevelSum = 0;
    return lineScore;
}

MelodyNote *PitchScoring::getSingingIndex(long currentTimeMills, int *flag, int *index) {
    //LOGI("currentTimeMills=%ld", currentTimeMills);
    int f = 100;// 宽松一点
    bool find = false;
    for (int j = 0; j < 1 && !find; j++) {
        f = j * UNIT_LEN_IN_MS / 5;// 不停地提高容忍度，确保能找到对应的音符，允许前后差最多250ms
        for (int i = 0; i < mMelodyNotes.size(); i++) {
            MelodyNote melodyNote = mMelodyNotes.at(i);
            int beginTimeMs = melodyNote.beginTimeMs;
            int endTimeMs = melodyNote.endTimeMs;
            if (beginTimeMs - f <= currentTimeMills && currentTimeMills < endTimeMs + f) {
                *index = i;
                find = true;
                if (LOGOPEN) {
                    LOGI("");
                    LOGI("getSingingIndex i=%d beginTimeMs=%d endTimeMs=%d currentTimeMills=%ld note=%hd f=%d",
                         i, beginTimeMs, endTimeMs, currentTimeMills, melodyNote.note_org, f);
                }
                return &melodyNote;
            }
        }
        *flag = j;
    }
    return NULL;
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
