#ifndef calc_score_hpp
#define calc_score_hpp

#include "pitch_detection.hpp"
#include "mel_chord_ana.h"

class CalcScore {
  
public:
    CalcScore(int sampleRate);
    ~CalcScore();
    
    int LoadMelp(std::string filename, int startStamp);
    void Flow(short *data, int len);
    int GetScore(int curTimeStamp);
    
private:
    std::vector<MelodyNote> _GetRangeNote(int startIdx, int endIdx);
    int _Matched(std::vector<PitchElement> vocPitchVec, std::vector<MelodyNote> tempNoteVec);
    std::vector<MelodyNote> _calcCurStcShift(std::vector<PitchElement> vocPitchVec, std::vector<MelodyNote> tempNoteVec);
    float _NoteDiff(float curNote, float targetNote) {
        float diff = curNote - targetNote;
        diff = diff > 6 ? (diff-12): diff;
        diff = diff <-6 ? (diff+12): diff;
        return diff;
    }
    
private:
    int m_sampleRate = 44100;
    int m_channels = 1;
    int m_totalSamples = 0;
    int m_lastTimeStamp = 0;
    std::string m_melpfilename;
    
    CPitchDetection* m_pitchDetector;
    
    std::vector<MelodyNote> m_noteVec;

};

#endif /* calc_score_hpp */
