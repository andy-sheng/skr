#include "calc_score.hpp"
#include <time.h>
#include <queue>
#include <algorithm>

#ifdef ANDROID
#include <common/CommonTools.h>
#define LOG_TAG "CalcScore"
#else
#define LOGD(...) fprintf(stderr, __VA_ARGS__);fprintf(stderr, "\n")
#endif

#define DEBUG 1

using namespace std;

static inline double getCurrentTimestamp() {
    struct timespec stamp;
    clock_gettime(CLOCK_MONOTONIC, &stamp);
    int64_t nsec = (int64_t) stamp.tv_sec*1000000000LL + stamp.tv_nsec;
    return nsec / 1000000.0;
}

CalcScore::CalcScore(int sampleRate) {
    m_sampleRate = sampleRate;
    // FFT变换时用的step size为480
    m_stepTimeMs = 480 * 1000.f / sampleRate;

    m_pitchDetector = new CPitchDetection(m_sampleRate);
}

CalcScore::~CalcScore() {
    if (m_pitchDetector) {
        delete m_pitchDetector;
        m_pitchDetector = NULL;
    }
}

int CalcScore::LoadMelp(std::string filename, int startStamp) {
    int ret = 0;
    m_melpfilename = filename;
    m_lastTimeStamp = startStamp;
    return ret;
}

void CalcScore::Flow(short *data, int len) {
    float f_buf[len];
    for (int i=0; i<len; i++) {
        f_buf[i] = data[i] / 32767.0;
    }
    m_pitchDetector->Process(f_buf, len);
    m_totalSamples += len;
}

void CalcScore::Flow(float *data, int len) {
    m_pitchDetector->Process(data, len);
    m_totalSamples += len;
}

int CalcScore::GetScore(int curTimeStamp) {
    float score = 0;
    double timestamp0 = getCurrentTimestamp();
    m_pitchDetector->MarkAsFinished();
    double timestamp1 = getCurrentTimestamp();
    vector<PitchElement> pitchTrack = m_pitchDetector->GetPitchData();
    vector<MelodyNote> noteVec = _GetRangeNote(m_lastTimeStamp, curTimeStamp);

    if (pitchTrack.empty() || noteVec.empty()) {
        LOGD("Got empty list");
        return 0;
    }

    vector<NoteTranscription> pitchNoteVec = m_pitchDetector->GetNoteTranscription();
    vector<NoteTranscription>::iterator itPitchNote = pitchNoteVec.begin();
    LOGD("pitch note transcription: ");
    for (; itPitchNote != pitchNoteVec.end(); itPitchNote++) {
        LOGD("%.1fms -> %.1fms note: %.2f freq: %f", itPitchNote->startTimeMs, itPitchNote->endTimeMs, itPitchNote->note, itPitchNote->freq);
    }

    vector<float> pitchNoteVecDTW;
    vector<PitchElement> shifedPitchTrack;
    vector<PitchElement>::iterator itPitch = pitchTrack.begin();
    for (; itPitch != pitchTrack.end(); itPitch++) {
        bool drop = itPitch->endTimeMs < noteVec.begin()->beginTimeMs;
//        cout << itPitch->startFrameIndex << "(" << itPitch->startTimeMs << "ms)" << "->" << itPitch->endFrameIndex << "(" << itPitch->endTimeMs << "ms)" << " note: " << itPitch->note << " freq: " << itPitch->freq << " conf: " << itPitch->conf << " drop: " << drop << endl;
        if (!drop) {
            shifedPitchTrack.push_back(*itPitch);
            pitchNoteVecDTW.push_back(itPitch->note);
        }
    }

    vector<float> melNoteVecDTW;
    vector<MelodyNote> shifedNoteVec = noteVec;//_calcCurStcShift(pitchTrack, noteVec);
    vector<MelodyNote>::iterator itMel = shifedNoteVec.begin();
    LOGD("mel note list: ");
    for (; itMel != shifedNoteVec.end(); itMel++) {
        LOGD("%dms -> %dms note: %d expos: %d", itMel->beginTimeMs, itMel->endTimeMs, itMel->note_org, itMel->exhibitionPos);
        float currentTime = max(itMel->beginTimeMs, 0);
        for (; currentTime < itMel->endTimeMs - m_stepTimeMs / 2; currentTime += m_stepTimeMs) {
            melNoteVecDTW.push_back(itMel->note_org);
        }
    }

    // 计算DTW和欧氏距离(当两者数量差别较大时，加上距离惩罚)
    float lenLimitRatio = 0.33;
    float minLen = min(pitchNoteVecDTW.size(), melNoteVecDTW.size());
    float maxLen = max(pitchNoteVecDTW.size(), melNoteVecDTW.size());
    float lenRatio = minLen / maxLen;
    float calLen = (lenRatio > lenLimitRatio) ? melNoteVecDTW.size() : minLen;

    // 计算DTW距离(允许统一的唱高或唱低)
    int noteOffset = 0;
    float dtwDist = _DTWDistance(pitchNoteVecDTW, melNoteVecDTW, calLen);
    LOGD("DTWDistance 0: %f", dtwDist);
    if (dtwDist > 1) {
        // 尝试按照唱高3,6,9分来计算
        vector<float> pitchNoteOffsetVecDTW = pitchNoteVecDTW;
        bool ignore6 = false;
        bool ignore9 = false;
        for (int off = 3; off < 12; off += 3) {
            if ((off == 6 && ignore6) || (off == 9 && ignore9)) {
                continue;
            }
            for (int i = 0; i < pitchNoteVecDTW.size(); i++) {
                float noteVal = ((int)((pitchNoteVecDTW[i] + off) * 1000) % 12000) / 1000.0f;
                pitchNoteOffsetVecDTW[i] = noteVal;
            }
            float dist = _DTWDistance(pitchNoteOffsetVecDTW, melNoteVecDTW, calLen);
            LOGD("DTWDistance %d: %f", off, dist);
            if (off == 3) {
                if (dist + 0.2f < dtwDist) {
                    ignore9 = true;
                } else if (dist - 0.2f > dtwDist) {
                    ignore6 = true;
                }
            }
            if (dist < dtwDist) {
                dtwDist = dist;
                noteOffset = off;
                if (dtwDist < 1) {
                    break;
                }
            }
        }
    }

    // 计算欧氏距离（当两个序列长度差别较大, 或DTW距离过大时，不再计算）
    float eucDist = dtwDist * 2;
#ifndef ANDROID
    if (lenRatio > lenLimitRatio && dtwDist < 1.5) {
        if (noteOffset) {
            for (int i = 0; i < shifedPitchTrack.size(); i++) {
                float noteVal = ((int)((pitchNoteVecDTW[i] + noteOffset) * 1000) % 12000) / 1000.0f;
                shifedPitchTrack[i].note = noteVal;
            }
        }
        eucDist = _EucDistance(shifedPitchTrack, shifedNoteVec, calLen);
    }
#endif

    // 计算得分
    score = _CalScore(eucDist, dtwDist);
    // 对和标准音高有偏差的情况，进行罚分
    if (noteOffset) {
        score *= 0.92;
        LOGD("noteOffset: %d down scale score to: %f", noteOffset, score);
    }

    double timestamp2 = getCurrentTimestamp();
    LOGD("GetScore time elapsed: %f pitch: %f calc: %f",
         timestamp2 - timestamp0, timestamp1 - timestamp0, timestamp2 - timestamp1);

    //score = _Matched(pitchTrack, shifedNoteVec);
    m_lastTimeStamp = curTimeStamp;
    m_pitchDetector->Reset(); // 基频检测重置，为下次检测做准备，reset 不干净，delete 掉重新 new
    if (m_pitchDetector) {
        delete m_pitchDetector;
        m_pitchDetector = NULL;
    }
    m_pitchDetector = new CPitchDetection(m_sampleRate);
    return (int)(score + 0.5f);
}

vector<MelodyNote> CalcScore::_GetRangeNote(int start, int end) {
    vector<MelodyNote> curNoteVec;
    MelChordAna* melChordAna = new MelChordAna(m_sampleRate * m_channels);
    melChordAna->InitByMelFile(m_melpfilename, 0);

    LOGD("_GetRangeNote start: %d end: %d", start, end);
    vector<MelodyNote> melodyNotes;
    int maxLine = melChordAna->GetToneScoreTempl(melodyNotes) + 1;
    //cout << "Line:" << maxLine << endl;
    vector<MelodyNote>::iterator it = melodyNotes.begin();
    // start end落在mel note范围内时，只要有效长度超过一半，也算进去。
    for (; it != melodyNotes.end() && (it->beginTimeMs + it->endTimeMs) / 2 < start; it++);
    for (; it != melodyNotes.end() && (it->beginTimeMs + it->endTimeMs) / 2 <= end; it++) {
        MelodyNote tmpNote;
        tmpNote.beginTimeMs = it->beginTimeMs - start;
        tmpNote.endTimeMs = it->endTimeMs - start;
        if (tmpNote.beginTimeMs < 0 || tmpNote.endTimeMs < 0) {
            cout << "LoadMelp time error: " << tmpNote.beginTimeMs << "  " << tmpNote.endTimeMs << endl;
            cout << "range start, end " << start << ", " << end << endl;
        }
        tmpNote.beginTime = tmpNote.beginTimeMs * m_sampleRate / 1000.0;
        tmpNote.endTime = tmpNote.endTimeMs * m_sampleRate / 1000.0;
        tmpNote.note = it->note;
        tmpNote.note_org = it->note_org;
        tmpNote.exhibitionPos = it->exhibitionPos;
        curNoteVec.push_back(tmpNote);
    }

    delete melChordAna;
    return curNoteVec;
}

int CalcScore::_Matched(std::vector<PitchElement> vocPitchVec, std::vector<MelodyNote> tempNoteVec) {
    int score = 0;
    if (vocPitchVec.empty() || tempNoteVec.empty()) {
        return score;
    }
    int matchedNoteNum = 0;
    vector<PitchElement>::iterator itPitch = vocPitchVec.begin();
    for (vector<MelodyNote>::iterator itNote = tempNoteVec.begin();
         itNote != tempNoteVec.end(); itNote++) {

        // 1.找到与模板匹配位置匹配的音高值
        for (; itPitch != vocPitchVec.end() && itPitch->endFrameIndex < itNote->beginTime; itPitch ++);

        // 2.计算当前位置的 pitch 序列中位数值，作为与 note 计算匹配的参考值
        std::vector<float> v;
        for (; itPitch != vocPitchVec.end() && itPitch->endFrameIndex < itNote->endTime; itPitch++) {
            v.push_back(itPitch->freq);
        }

        // 3. 伴音差距小于 1 认为是匹配的计数加一
        if (!v.empty()) {
            sort(v.begin(), v.end(), greater<float>());
            float median = v[v.size()/2];
            float curNote = 69000.5 + 12000 * log10f(median / 440.0) / log10f(2.0);
            curNote = float((int) curNote % 12000) / 1000.0;
            float diffNote = _NoteDiff(curNote, itNote->note_org);
            if (diffNote < 1) {
                matchedNoteNum ++;
            }
            cout << "median: " << median << " curNote: " << curNote << " note_org: " << itNote->note_org << " diffNote: " << diffNote << " matched: " << matchedNoteNum << endl;
        }
    }
    float matchedRatio = float(matchedNoteNum) / tempNoteVec.size();
    score = matchedRatio * 100;

    return score;
}

vector<MelodyNote> CalcScore::_calcCurStcShift(vector<PitchElement> vocPitchVec, vector<MelodyNote> tempNoteVec) {

    vector<MelodyNote> shiftedNoteVec = tempNoteVec;
    if (vocPitchVec.empty()  || tempNoteVec.empty()) {
        return shiftedNoteVec;
    }

    float factor = m_sampleRate / 1000.0;
    int curStcNoteLen = tempNoteVec.back().endTime - tempNoteVec.front().beginTime;
    int curPitchLen = vocPitchVec.back().endFrameIndex - vocPitchVec.front().startFrameIndex;
    cout << "note : " << tempNoteVec.front().beginTime / factor << "   " << tempNoteVec.back().endTime / factor << "  " << curStcNoteLen / factor << "  " << tempNoteVec.size() << endl;
    cout << "pitch : " << vocPitchVec.front().startFrameIndex / factor << "  " << vocPitchVec.back().endFrameIndex / factor << "  " << curPitchLen / factor << endl;


    // 1. 判定整体偏移的大小
    vector<PitchElement>::iterator itV_c = vocPitchVec.begin();
    for (; itV_c != vocPitchVec.end() && itV_c->endFrameIndex < tempNoteVec.front().beginTime - 400 * factor; itV_c++);

    int lastPitchStart = itV_c->startFrameIndex;
    int vocPitchStart = itV_c->startFrameIndex;
    int pitch_continuous_cnt = 0;
    for (; itV_c != vocPitchVec.end() && itV_c->endFrameIndex < tempNoteVec.back().endTime; itV_c ++) {
        if(itV_c->startFrameIndex - lastPitchStart < 50 * factor) {
            pitch_continuous_cnt ++;
        } else {
            pitch_continuous_cnt = 0;
            vocPitchStart = itV_c->startFrameIndex;
        }
        if (pitch_continuous_cnt > 10) {
            break;
        }
        lastPitchStart = itV_c->startFrameIndex;
    }

    int shiftTime = tempNoteVec.front().beginTime - vocPitchStart;
    if (abs(shiftTime) < 400 * factor) {
        for (vector<MelodyNote>::iterator itT = shiftedNoteVec.begin(); itT != shiftedNoteVec.end(); itT++) {
            itT->beginTime -= shiftTime;
            itT->endTime -= shiftTime;
        }
    }
    cout << "shift time: " << shiftTime / factor << "  voc pitch start: "<< vocPitchStart / factor << endl;
    cout << "-------------------------" << endl;

    return shiftedNoteVec;
}

static void printFloatVector(const char* prefix, vector<float>& vec) {
    string slog;
    char tmp[64];
    vector<float>::iterator it1 = vec.begin();
    slog += prefix;
    slog += "\n";
    for (; it1 != vec.end(); it1++) {
        sprintf(tmp, "%.2f", *it1);
        slog += tmp;
        slog += ",";
    }
    slog += "}";
    LOGD("%s", slog.c_str());
}

float CalcScore::_EucDistance(vector<PitchElement>& vocPitchVec, vector<MelodyNote>& tempNoteVec, float len) {
    if (vocPitchVec.size() == 0 || vocPitchVec.size() > 1000 ||
        tempNoteVec.size() == 0 || tempNoteVec.size() > 1000) {
        return 1000;
    }

    float distance = 0;
    vector<PitchElement>::iterator itPitch = vocPitchVec.begin();
    vector<PitchElement>::iterator itNextPitch = itPitch + 1;
    vector<MelodyNote>::iterator itMel = tempNoteVec.begin();
    vector<MelodyNote>::iterator itNextMel = itMel + 1;
    float currentPitchTime = itPitch->startTimeMs;
    float currentNoteTime = itMel->beginTimeMs;
    float currentTime = min(currentPitchTime, currentNoteTime);
    float nextPitchTime = itPitch->endTimeMs;
    float nextNoteTime = itMel->endTimeMs;
    bool pitchEnd = false;
    bool noteEnd = false;

    if (itNextPitch != vocPitchVec.end()) {
        nextPitchTime = itNextPitch->startTimeMs;
    }
    if (itNextMel != tempNoteVec.end()) {
        nextNoteTime = itNextMel->beginTimeMs;
    }

    LOGD("cal eucDistance currentTime: %f pitchTime: %f noteTime: %f",
         currentTime, currentPitchTime, currentNoteTime);

    vector<float> pitchNoteVector;
    vector<float> melodyNoteVector;
    int totalCount = 0;
    float harfStepTimeMs = m_stepTimeMs / 2;
    while (!pitchEnd || !noteEnd) {
        if (currentTime > nextPitchTime - harfStepTimeMs) {
            if (itNextPitch == vocPitchVec.end()) {
                pitchEnd = true;
            } else {
                itPitch = itNextPitch;
                itNextPitch++;
                currentPitchTime = itPitch->startTimeMs;
                if (itNextPitch != vocPitchVec.end()) {
                    nextPitchTime = itNextPitch->startTimeMs;
                } else {
                    nextPitchTime = itPitch->endTimeMs;
                }
            }
        }
        pitchNoteVector.push_back(itPitch->note);

        if (currentTime > nextNoteTime - harfStepTimeMs) {
            if (itNextMel == tempNoteVec.end()) {
                noteEnd = true;
            } else {
                itMel = itNextMel;
                itNextMel++;
                currentNoteTime = itMel->beginTimeMs;
                if (itNextMel != tempNoteVec.end()) {
                    nextNoteTime = itNextMel->beginTimeMs;
                } else {
                    nextNoteTime = itMel->endTimeMs;
                }
            }
        }
        melodyNoteVector.push_back(itMel->note_org);

        // 距离累加
        distance += _NoteDiff(itPitch->note, itMel->note_org);
        currentTime += m_stepTimeMs;
        totalCount++;
    }

#if DEBUG
    printFloatVector("euc pitchNote: {", pitchNoteVector);
    printFloatVector("euc melNote: {", melodyNoteVector);
#endif

    LOGD("cal EucDistance with count %d val = %f/%f", totalCount, distance, len);
    return distance / len;
}

float CalcScore::_DTWDistance(vector<float>& pitchNoteVec, vector<float>& melNoteVec, float len) {
    if (pitchNoteVec.size() == 0 || pitchNoteVec.size() > 1000 ||
        melNoteVec.size() == 0 || melNoteVec.size() > 1000) {
        return 100;
    }

    vector<float> A = melNoteVec;
    vector<float> B = pitchNoteVec;
    float distance = 0;
    int I = A.size();
    int J = B.size();
    int i,j;
    int r = 10;
    int istart, imax;
    // 匹配距离
    int r2 = r + abs(I-J);
    float g1, g2, g3, distTmp;

    LOGD("cal DTWDistance I = %d J = %d r2 = %d", I, J, r2);

#if DEBUG
    printFloatVector("DTW pitchNote: {", pitchNoteVec);
    printFloatVector("DTW melNote: {", melNoteVec);
#endif

    // 创建和初始化矩阵
    vector<vector<float>> distArray(I, vector<float>(J));
    for (i = 0; i < I; i++) {
        for (j = 0; j < J; j++) {
            distArray[i][j] = 10000;
        }
    }

    distArray[0][0] = 2 * _NoteDiff(A[0], B[0]);
    for (i = 1; i <= r2 && i < I; i++) {
        distArray[i][0] = distArray[i-1][0] + _NoteDiff(A[i], B[0]);
    }
    for (j = 1; j <= r2 && j < J; j++) {
        distArray[0][j] = distArray[0][j-1] + _NoteDiff(A[0], B[j]);
    }

    for (j = 1; j < J; j++) {
        istart = j - r2;
        if (j <= r2) {
            istart = 1;
        }
        imax = j + r2;
        if (imax >= I) {
            imax = I - 1;
        }

        for (i = istart; i <= imax; i++) {
            distTmp = _NoteDiff(A[i], B[j]);
            g1 = distArray[i-1][j] + 2 * distTmp;
            g2 = distArray[i-1][j-1] + distTmp - 0.1;
            g3 = distArray[i][j-1] + distTmp;
            g2 = min(g1, g2);
            g3 = min(g2, g3);
            distArray[i][j] = g3;
        }
    }

    distance = distArray[I-1][J-1] / len;
    // 人声长于乐谱的情况
    if (J > I) {
        int step = min((J-I), J);
        step = max(step, J/2);
        for (j = 2; j < step; j++) {
            distance = min(distance, distArray[I-1][J-j] / len);
        }
        LOGD("DTW calc j = %d", j);
    }
    return distance;
}

static inline float getScore(float distance) {
    return 100 * pow(0.68, pow(abs(distance), 1.5));
}

float CalcScore::_CalScore(float eucDist, float dtwDist) {
    float rhythmDist = eucDist - dtwDist;
    float pitchScore = getScore(dtwDist);
    float rhythmScore = getScore(rhythmDist);
    float totalScore = 0.6 * pitchScore + 0.4 * rhythmScore;
    LOGD("DTWDist: %f eucDist: %f rhythmDist: %f pitchScore: %f rhythmScore: %f totalScore: %f",
         dtwDist, eucDist, rhythmDist, pitchScore, rhythmScore, totalScore);
    return totalScore;
}
