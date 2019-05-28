#include "calc_score.hpp"
#include <queue>
#include <algorithm>
#include <common/CommonTools.h>

#define LOG_TAG "CalcScore"

using namespace std;

CalcScore::CalcScore(int sampleRate) {
    m_sampleRate = sampleRate;

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

int CalcScore::GetScore(int curTimeStamp) {
    int score = 0;
    m_pitchDetector->MarkAsFinished();
    vector<PitchElement> pitchTrack = m_pitchDetector->GetPitchData();
    vector<MelodyNote> noteVec = _GetRangeNote(m_lastTimeStamp, curTimeStamp);

    //cout << pitchTrack.front().startFrameIndex / 44.1 << " - " << pitchTrack.back().endFrameIndex / 44.1 << endl;
    //cout << noteVec.front().beginTimeMs << " - " << noteVec.back().endTimeMs << endl;
    vector<MelodyNote> shifedNoteVec = _calcCurStcShift(pitchTrack, noteVec);
    score = _Matched(pitchTrack, shifedNoteVec);
    m_lastTimeStamp = curTimeStamp;
    m_pitchDetector->Reset(); // 基频检测重置，为下次检测做准备，reset 不干净，delete 掉重新 new
    if (m_pitchDetector) {
        delete m_pitchDetector;
        m_pitchDetector = NULL;
    }
    m_pitchDetector = new CPitchDetection(m_sampleRate);
    return score;
}

vector<MelodyNote> CalcScore::_GetRangeNote(int start, int end) {
    vector<MelodyNote> curNoteVec;
    MelChordAna* melChordAna = new MelChordAna(m_sampleRate * m_channels);
    melChordAna->InitByMelFile(m_melpfilename, 0);

    vector<MelodyNote> melodyNotes;
    int maxLine = melChordAna->GetToneScoreTempl(melodyNotes) + 1;
    //cout << "Line:" << maxLine << endl;
    vector<MelodyNote>::iterator it = melodyNotes.begin();
    for (; it!=melodyNotes.end() && it->beginTimeMs < start; it++);
    for (; it != melodyNotes.end() && it->endTimeMs < end; it++) {
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
        tmpNote.exhibitionPos = it->exhibitionPos;
        //cout << it->beginTimeMs << " " << it->endTimeMs << " " << it->note << " " << it->exhibitionPos <<endl;
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
            float diffNote = _NoteDiff(curNote, itNote->note);
            if (diffNote < 1) {
                matchedNoteNum ++;
            }
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
        if (pitch_continuous_cnt > 3) {
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
