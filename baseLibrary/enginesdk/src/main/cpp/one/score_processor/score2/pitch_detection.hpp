//
//  pitch_detection.hpp
//  PitchCorrection
//
//  Created by zhouyu on 17/1/25.
//  Copyright © 2017年 zhouyu. All rights reserved.
//

#ifndef pitch_detection_hpp
#define pitch_detection_hpp

#include <stdio.h>
//#include "pitchCorrection_types.hpp"
#include "slide_window.hpp"
#include "fft_wrapper.hpp"
#include "PYinVamp.h"

//using namespace pitch_correction;

//音频的pitch位置数据信息
typedef struct tagPitchElement
{
    int startFrameIndex = 0;
    int endFrameIndex = 0;
    int noteRegion = 0; //0:unvoiced, 1: -B0, 2:C1-B1, 3:C2-B2, 4:C3-B3, 5:C4-B4, 6:C5-B5, 7:C6-B6, 8: C7-B7, 9:C8-
    double freq = 0;
    double conf = 0;
    bool isVoiced;
    bool isVoicedFunc(double tolerance) {
        bool ret = false;
        if (conf > tolerance) {
            ret = true;
        }
        return ret;
    }
} PitchElement;

typedef struct tagNoteTranscription
{
    float startTimeMs;
    float endTimeMs;
    float freq;
}NoteTranscription;

class CPitchDetection {

public:
    CPitchDetection(int iSampleRate) throw(CParamException);
    ~CPitchDetection();
    
    void Process(const float data[], const size_t sampleSize);
    std::vector<PitchElement> GetPitchData();
    std::vector<NoteTranscription> GetNoteTranscription();
    void MarkAsFinished();
    void Flush();
    void Seek(unsigned int position);
    void SetLastPosition(unsigned int position);
    unsigned int GetWindowSize();
    unsigned int GetHopSize();
    void Reset();
    
private:
    unsigned int m_sampleRate;   //采样率
    unsigned int m_windowSize;   //buffer大小
    unsigned int m_hopSize;      //hopsize
    float* m_tempDataBuf;         //当前处理数据buf
    
    CSlideWindow* m_slideWindow = NULL;
    unsigned int m_totalSamples;
    /** 计算结果 **/
    std::vector<PitchElement> m_pitchVector;
    std::vector<NoteTranscription> m_noteTranscriptions;
    /**
     * @brief CalNoteRegion
     * return noteRegion  0: -B0, 1:C1-B1, 1:C2-B2, 3:C3-B3, 4:C4-B4, 5:C5-B5, 6:C6-B6, 7: C7-B7, 8:C8-
     */
    int CalNoteRegion(double dFreq);
    
    /** 使用PYin来计算基频 **/
    PYinVamp* pYinVamp = NULL;
    float m_maxPrevEnergy; // 记录之前的最大能量值
    PitchElement buildPitchElementByFeature(Feature featurePitch);
    NoteTranscription buildNoteTranscriptionByFeture(Feature featurePitch);
    
    float _calcEnergy(const float *data, const size_t length);
    void _boostInputWav(float *data, const size_t length);
    
    /** 使用Aubio来计算基频 **/
//    aubio_pitchyinfft_t* m_pitchYinfft;
//    void Process_Yinfft(aubio_pitchyinfft_t* p, const float* pInput, PitchElement& pOutput, unsigned int uiLength);
    
    /** 使用AutoTune倒谱的计算方法来及算基频 **/
    float *m_windowWeight;
    CFFTWrapper *m_fftWrapper;
    float m_ti4;
    float m_conf;
    float *m_acwinv;
    void GetPitchElement(const float *inData,PitchElement &curPitch,size_t windowSize);
    
#if LPF_FOR_PITCH_INPUT
    /** 使用Biquad滤波器来滤除过大的高频噪声 **/
    BiquadEQ *m_inputLPFilter = NULL;
    BiquadEQ *m_inputLPFilter1 = NULL;
    BiquadEQ *m_inputLPFilter2 = NULL;
    
    void _LPF1KHz(const float *data, float *LPF, int length);
#endif
};

#endif /* pitch_detection_hpp */
