//
//  CCalcBaseband.h
//  计算基频
//
//  Created by allenyang on 16/5/12.
//  Copyright © 2016年 allenyang. All rights reserved.
//

#ifndef CCalcBaseband_h
#define CCalcBaseband_h

#include "fft_wrapper.hpp"

class CCalcBaseband
{
private:
    float m_pfTune ;
    float m_pfConf ;

    CFFTWrapper* m_fmembvars;

    int m_fs; // Sample rate
    int m_channels;
    
    int m_cbsize; // size of circular buffer
    int m_cbsizeDivOverlap;
    int m_corrsize; // m_cbsize/2 + 1
    int m_cbiwr;
    int m_cbiwrol;
    float* m_cbi; // circular input buffer
    float* m_cbf; // circular formant correction buffer
    float* m_cbo; // circular output buffer
    
    float* m_cbwindow; // hann of length N/2, zeros for the rest
    float* m_acwinv; // inverse of autocorrelation of window
    int m_noverlap;
    
    float* m_ffttime;
    
    float m_inpitch; // Input pitch (semitones)
    float m_conf; // Confidence of pitch period estimate (between 0 and 1)
    
    float m_vthresh; // Voiced speech threshold
    
    int64_t m_ti4;
    
    float m_pmax; // Maximum allowable pitch period (seconds)
    float m_pmin; // Minimum allowable pitch period (seconds)
    
    int m_nmax; // Maximum period index for pitch prd est
    int m_nmin; // Minimum period index for pitch prd est
    
    float m_flamb;
    
    uint32_t m_totalSampleCount;
    
public:
    void Init(int sampleRate, int channelsx);
    //get max frequence ,confidence , semitones ,WARNING sampleCount必须是偶数！
    void getFreqAndConf(int16_t * inputSamples,uint32_t sampleCount,float *f0,float* _conf);
    void rePos();//seek to other position to start calculate
    void SeekFromStart(int frameNum);
    ~CCalcBaseband();
    CCalcBaseband();
    
};

#endif /* CCalcBaseband_h */
