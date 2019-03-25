//
//  pitch_detection.cpp
//  PitchCorrection
//
//  Created by zhouyu on 17/1/25.
//  Copyright © 2017年 zhouyu. All rights reserved.
//

#include "pitch_detection.hpp"
#include "mathUtil.hpp"

#define LPF_FOR_PITCH_INPUT 0

CPitchDetection::CPitchDetection(int iSampleRate) throw(CParamException)
{
    pYinVamp = new PYinVamp(iSampleRate);
    m_sampleRate = iSampleRate;
    m_windowSize = 2048;
    m_hopSize = 256;
    const int iChannels = 1;
    if(!pYinVamp->initialise(iChannels, m_hopSize, m_windowSize)) {
        throw CParamException("Invalid Param To Init PYinVamp.");
    }
    m_slideWindow = new CSlideWindow(m_windowSize, m_hopSize);
    m_slideWindow->SetHammWindow(false);
    m_tempDataBuf = new float[m_windowSize];
    m_totalSamples = 0;
    
    //m_pitchYinfft = new_aubio_pitchyinfft(m_sampleRate, m_windowSize);

    m_fftWrapper = new CFFTWrapper(m_windowSize);
    m_windowWeight = new float[m_windowSize];
    for (int k = 0; k < m_windowSize; k++) {
        m_windowWeight[k] = 0.5 - 0.5 * cos(2 * PI * k / (float)m_windowSize);
    }
    m_ti4 =0;
    m_conf =0;
    
    float m_ffttime[m_windowSize];
    float m_fftfreqre[m_windowSize/2+1];
    float m_fftfreqim[m_windowSize/2+1];
    
    
    // ---- Calculate autocorrelation of window ----
    
    //float LOG_N = round(arlog2(m_windowSize));
    
    m_acwinv = new float[m_windowSize];
    for (int ti = 0; ti < m_windowSize; ti++) {
        
        m_ffttime[ti] = m_windowWeight[ti];
    }
    m_fftWrapper->FftForward(m_windowWeight, m_fftfreqre, m_fftfreqim);
    for (int ti = 0; ti < m_windowSize/2+1; ti++) {
        m_fftfreqre[ti] = (m_fftfreqre[ti]) * (m_fftfreqre[ti]) + (m_fftfreqim[ti]) * (m_fftfreqim[ti]);
        m_fftfreqim[ti] = 0;
    }
    m_fftWrapper->FftInverse(m_fftfreqre, m_fftfreqim, m_ffttime);
    for (int ti = 1; ti < m_windowSize; ti++) {
        m_acwinv[ti] = m_ffttime[ti] / m_ffttime[0];
        if (m_acwinv[ti] > 0.000001) {
            m_acwinv[ti] = (float)1 / m_acwinv[ti];
        }
        else {
            m_acwinv[ti] = 0;
        }
    }
    m_acwinv[0] = 1;
    
    m_maxPrevEnergy = 0;
    
#if LPF_FOR_PITCH_INPUT
    m_inputLPFilter = new BiquadEQ(m_sampleRate, 32767);
    m_inputLPFilter->InitLPF(800);
    m_inputLPFilter1 = new BiquadEQ(m_sampleRate, 32767);
    m_inputLPFilter1->InitLPF(800);
    m_inputLPFilter2 = new BiquadEQ(m_sampleRate, 32767);
    m_inputLPFilter2->InitLPF(800);
#endif
}

CPitchDetection::~CPitchDetection()
{
    if(m_slideWindow)
    {
        delete m_slideWindow;
        m_slideWindow = NULL;
    }
    if(m_tempDataBuf)
    {
        delete[] m_tempDataBuf;
        m_tempDataBuf = NULL;
    }
//    if(m_pitchYinfft)
//    {
//        del_aubio_pitchyinfft(m_pitchYinfft);
//        m_pitchYinfft = NULL;
//    }
    
    if (m_fftWrapper!=NULL) {
        delete m_fftWrapper;
        m_fftWrapper = NULL;
    }
    if (m_acwinv!=NULL) {
        delete[] m_acwinv;
        m_acwinv = NULL;
    }
    if (m_windowWeight!=NULL) {
        delete[] m_windowWeight;
        m_windowWeight = NULL;
    }
    if(pYinVamp != NULL)
    {
        delete pYinVamp;
        pYinVamp = NULL;
    }
    
#if LPF_FOR_PITCH_INPUT
    if (m_inputLPFilter!=NULL) {
        delete m_inputLPFilter;
        m_inputLPFilter = NULL;
    }
    if (m_inputLPFilter1!=NULL) {
        delete m_inputLPFilter1;
        m_inputLPFilter1 = NULL;
    }
    if (m_inputLPFilter2!=NULL) {
        delete m_inputLPFilter2;
        m_inputLPFilter2 = NULL;
    }
#endif
}

float CPitchDetection::_calcEnergy(const float *data, const size_t length)
{
    float acc = 0;
    
    for(int i=0; i<length; i++)
    {
        acc += data[i] * data[i];
    }
    if(length)
    {
        return (acc / length);
    }
    else
    {
        return -1;
    }
}

void CPitchDetection::_boostInputWav(float *data, const size_t length)
{
    // 幅度增强的映射曲线为 sqrt(sin(-PI/2 : PI/2))
    
    float value=0;
    float sign=0;
    for(int i=0; i<length; i++)
    {
        data[i] = sinf(data[i] * PI / 2);
        
        value = fabsf(data[i]);
        sign = data[i]>0 ? 1:-1;
        
        data[i] = sign * sqrt(value);
    }
}

#if LPF_FOR_PITCH_INPUT
void CPitchDetection::_LPF1KHz(const float *data, float *LPF2, int sampleSize)
{
    float LPF[sampleSize];
    for(int i = 0; i < sampleSize; i++) {
        LPF[i] = m_inputLPFilter->BiQuad(data[i]);
    }
    float LPF1[sampleSize];
    for(int i = 0; i < sampleSize; i++) {
        LPF1[i] = m_inputLPFilter->BiQuad(LPF[i]);
    }
    for(int i = 0; i < sampleSize; i++) {
        LPF2[i] = m_inputLPFilter->BiQuad(LPF1[i]);
    }
}
#endif

void CPitchDetection::Process(const float *data, const size_t sampleSize)
{
    //LOG4ARProfiling;
    
    // 根据之前的最大能量值决定当前样本是否进行幅度增加
#if LPF_FOR_PITCH_INPUT
    // 1.低通滤波
    // 4KHz低通滤波器，滤Android机高频噪声
    float LPF[sampleSize];
    _LPF1KHz(data, LPF, (int)sampleSize);
    
    // 2.计算最大能量
    float rms = _calcEnergy(LPF, sampleSize);
    if(rms > m_maxPrevEnergy)
    {
        m_maxPrevEnergy = rms;
    }
    // 3.增加幅度
    float rmsThresh = 0.05; // 对应采样点平均幅度为 sqrt(rmsThresh)
    float boostData[sampleSize];
    memcpy(boostData, LPF, sampleSize*sizeof(float));
    if(m_maxPrevEnergy < rmsThresh && rms >= 0)
    {
        _boostInputWav(boostData, sampleSize);
    }
#else
    // 1.计算最大能量
    float rms = _calcEnergy(data, sampleSize);
    if(rms > m_maxPrevEnergy)
    {
        m_maxPrevEnergy = rms;
    }
    // 2.增加幅度
    float rmsThresh = 0.05; // 对应采样点平均幅度为 sqrt(rmsThresh)
    float boostData[sampleSize];
    memcpy(boostData, data, sampleSize*sizeof(float));
    if(m_maxPrevEnergy < rmsThresh && rms >= 0)
    {
        _boostInputWav(boostData, sampleSize);
    }
#endif
    
    size_t uiSlice = 64;
//    const float  * pSamples = data;
    const float * pSamples = boostData;

    for (size_t idx = 0; idx < sampleSize; idx += uiSlice)
    {
        if ((sampleSize - idx) < uiSlice)
        {
            uiSlice = sampleSize - idx;
        }
        m_totalSamples += uiSlice;
        float pTempData[64];
        memcpy(pTempData, pSamples, sizeof(float) * uiSlice);
        pSamples += uiSlice;
        bool ret = m_slideWindow->Process(pTempData, uiSlice);
        if (ret)
        {
            size_t centerLatency;
            size_t frontLatency;
            size_t retSize = m_slideWindow->CheckOutSlideWindow(m_tempDataBuf, centerLatency, frontLatency);
            if (retSize <= 0)
            {
                return ;
            }
    
            //计算基频
//            PitchElement curPitch;
            //Method 1 : 使用Aubio里面的Yinfft来计算基频
//            Process_Yinfft(m_pitchYinfft, m_tempDataBuf, curPitch, m_windowSize);
            //Method 2 : 使用AutoTune里面先计算倒谱在计算基频的计算方式
//            GetPitchElement(m_tempDataBuf,curPitch,m_windowSize);
            
//            if(curPitch.isVoiced)
//                curPitch.noteRegion = CalNoteRegion(curPitch.freq);
//            else
//                curPitch.noteRegion = 0;
//            curPitch.startFrameIndex = m_totalSamples - centerLatency - m_hopSize/2;
//            curPitch.endFrameIndex = curPitch.startFrameIndex + m_hopSize - 1;
//            m_pitchVector.push_back(curPitch);
            
            //Mehod 3 : 使用PYIN算法来及算基频
//            RealTime fTimestamp = 1000.0 * (m_totalSamples - centerLatency - m_hopSize / 2) / m_sampleRate;
            RealTime fTimestamp = 1000.0 * (m_totalSamples - frontLatency + m_hopSize * 0.75) / m_sampleRate; // 根据批量测试结果设定、by zhouyu
            // close by zhouyu
//            int offset = m_preciseMode ? m_blockSize/2 : m_blockSize/4;
//            float offsetTimeStamp = offset * 1000.0 / m_inputSampleRate;
//            fTimestamp = fTimestamp + offsetTimeStamp;
            pYinVamp->process(m_tempDataBuf, fTimestamp);
        }
    }
}

std::vector<PitchElement> CPitchDetection::GetPitchData()
{
    return m_pitchVector;
}

std::vector<NoteTranscription> CPitchDetection::GetNoteTranscription()
{
    return m_noteTranscriptions;
}

void CPitchDetection::MarkAsFinished()
{
    //1:获取出pYinVamp中的计算结果
    FeatureSet featureSetOut = pYinVamp->getRemainingFeatures();
    //2:构造PitchVector
    vector<Feature> featurePitchs = featureSetOut[FREQUENCY_FLAG];
    for(int i = 0; i < featurePitchs.size(); i++) {
        PitchElement pitchElement = buildPitchElementByFeature(featurePitchs[i]);
        if(!m_pitchVector.empty() && abs(pitchElement.startFrameIndex - m_pitchVector.back().endFrameIndex) < m_hopSize/2)
            pitchElement.startFrameIndex = m_pitchVector.back().endFrameIndex + 1;
        m_pitchVector.push_back(pitchElement);
    }
    
    for( int i=1;i<m_pitchVector.size();i++ )
    {
        if( m_pitchVector[i-1].freq / m_pitchVector[i].freq > 1.9 && m_pitchVector[i-1].freq / m_pitchVector[i].freq < 2.1 ){
            if( - m_pitchVector[i-1].endFrameIndex + m_pitchVector[i].startFrameIndex < 0.1*m_sampleRate )
            {
                m_pitchVector[i].freq *= 2;
//              cout << i << " " << m_pitchVector[i-1].freq << " " << m_pitchVector[i].freq << endl;
            }
        }
    }
    
    //3:构造m_noteTranscriptions
    vector<Feature> featureNotes = featureSetOut[NOTE_FLAG];
    for(int i = 0; i < featureNotes.size(); i++) {
        NoteTranscription noteTranscription = buildNoteTranscriptionByFeture(featureNotes[i]);
        m_noteTranscriptions.push_back(noteTranscription);
    }
}

PitchElement CPitchDetection::buildPitchElementByFeature(Feature featurePitch)
{
    PitchElement pitchElement;
    pitchElement.startFrameIndex = featurePitch.timestamp * (m_sampleRate / 1000.0);
    pitchElement.endFrameIndex = pitchElement.startFrameIndex + m_hopSize - 1;
    pitchElement.freq = featurePitch.values[0];
    pitchElement.noteRegion = CalNoteRegion(featurePitch.values[0]);
    if(pitchElement.freq > 0)
        pitchElement.isVoiced = true;
    return pitchElement;
}


NoteTranscription CPitchDetection::buildNoteTranscriptionByFeture(Feature featureNote)
{
    NoteTranscription noteTracscription;
    noteTracscription.startTimeMs = featureNote.timestamp;
    noteTracscription.endTimeMs = featureNote.timestamp + featureNote.duration;
    noteTracscription.freq = featureNote.values[0];
    return noteTracscription;
}

unsigned int CPitchDetection::GetWindowSize()
{
    return m_windowSize;
}

unsigned int CPitchDetection::GetHopSize()
{
    return m_hopSize;
}

int CPitchDetection::CalNoteRegion(double dFreq)
{
    int iNoteRegion = 0;
    if(dFreq < 30.868)
        iNoteRegion = 1;
    else if (dFreq < 32.703)
        iNoteRegion = 12;    //处于Region1和Region2的边界
    else if(dFreq < 61.735)
        iNoteRegion = 2;
    else if (dFreq < 65.406)
        iNoteRegion = 23;     //处于Region2和Region3的边界
    else if (dFreq < 123.47)
        iNoteRegion = 3;
    else if (dFreq < 130.81)
        iNoteRegion = 34;     //处于Region3和Region4的边界
    else if (dFreq < 246.94)
        iNoteRegion = 4;
    else if (dFreq < 261.63)
        iNoteRegion = 45;     //处于Region4和Region5的边界
    else if (dFreq < 493.88)
        iNoteRegion = 5;
    else if (dFreq < 523.25)
        iNoteRegion = 56;      //处于Region5和Region6的边界
    else if(dFreq < 987.77)
        iNoteRegion = 6;
    else if (dFreq < 1046.5)
        iNoteRegion = 67;      //处于Region6和Region7的边界
    else if (dFreq < 1975.5)
        iNoteRegion = 7;
    else if (dFreq < 2093.0)
        iNoteRegion = 78;      //处于Region7和Region8的边界
    else if (dFreq < 3951.1)
        iNoteRegion = 8;
    else if (dFreq < 4186)
        iNoteRegion = 89;      //处于Region8和Region9的边界
    else
        iNoteRegion = 9;
    return iNoteRegion;
        
}



//void CPitchDetection::Process_Yinfft(aubio_pitchyinfft_t* p, const float* pInput, PitchElement& pOutput, unsigned int uiLength)
//{
//    fvec_t input;
//    fvec_t* output = new_fvec(1);
//    float fInputData[m_windowSize];
//    for(int i = 0; i < m_windowSize; i++)
//        fInputData[i] = pInput[i];
//    input.data = fInputData;
//    input.length = m_windowSize;
//    aubio_pitchyinfft_do(p, &input, output);
//    if(output->data[0] > 0)
//    {
//        pOutput.freq = m_sampleRate / (output->data[0] + 0.0);
//    }
//    else
//    {
//        pOutput.freq = 0.0;
//    }
//    pOutput.conf = aubio_pitchyinfft_get_confidence(p);
//    pOutput.isVoiced = pOutput.isVoicedFunc(0.8);
//    if(pOutput.freq == 0)
//        pOutput.isVoiced = false;
//    del_fvec(output);
//}

static void addWindow(float *data , float *window , size_t len) {
    for (int i = 0; i < len; i++) {
        *(data + i) = *(data + i) * (*(window + i));
    }
}
void CPitchDetection::GetPitchElement(const float *inData,PitchElement &curPitch,size_t windowSize){
    
    float cepData[windowSize];
    memcpy(cepData, inData, windowSize*sizeof(float));
    
    addWindow(cepData,m_windowWeight, windowSize);
    m_fftWrapper->CalcCepstrum(cepData, cepData);
    
    float tf2 = 0;
    float m_pmin = 1 / (float)700;
    float m_pmax = 1 / (float)70;
    int m_corrsize = int(windowSize/2) +1 ;
    float conf = m_conf;
    
    int64_t m_nmax =(m_sampleRate * m_pmax);
    
    if (m_nmax > m_corrsize) {
        m_nmax = m_corrsize;
    }
    int64_t m_nmin = (uint64_t)(m_sampleRate * m_pmin);
    
    float  pperiod = m_pmin;
    
    int64_t tmpMin = m_nmin + 1;
    
    for (; tmpMin < m_nmax; tmpMin++)
    {
        if (cepData[tmpMin] > cepData[tmpMin - 1])
        {
            break;
        }
    }
    
    int64_t tmpMax = m_nmax - 1;
    for (; tmpMax > tmpMin; tmpMax--)
    {
        if (cepData[tmpMax] <= cepData[tmpMax - 1])
        {
            break;
        }
    }
    
    float partMax = 0;
    for (int64_t ti = tmpMin; ti < tmpMax; ti++)
    {
        if (partMax < cepData[ti])
        {
            partMax = cepData[ti];
        }//找出波峰的位置
    }
    partMax = partMax * 0.5;
    
    int64_t ti4 = 0;
    float tmpFft = -1;
    float fScoreMin = 1000000000;
    float tmpScore = 0;
    float tmpPS = 0;
    for (int64_t ti = tmpMin; ti < tmpMax; ti++)
    {
        if ((tmpFft < cepData[ti])
            && ( cepData[ti] >= cepData[ti + 1])
            && (cepData[ti] > partMax))
        {
            tmpScore = cepData[ m_windowSize / 2] - cepData[m_windowSize / 2 - ti];
            tmpScore *= tmpScore;
            
            if (m_ti4 > 10 && m_conf>0.8)
            {
                tmpScore += 0.0001 * (ti - m_ti4) * (ti - m_ti4);
                tmpPS = log(float(ti) / m_ti4);
                tmpPS *= tmpPS;
                tmpScore += tmpPS * 0.0000005 ;
            }
            if (tmpScore < fScoreMin)
            {
                fScoreMin = tmpScore;
                ti4 = ti;
                tf2 = cepData[ti];
            }
        }
        tmpFft = cepData[ti];
    }
    if(m_conf>0.8)m_ti4 = ti4;
    
    if (tf2 > 0) {
        conf = tf2 * m_acwinv[ti4];
        if (ti4 > 0 && ti4 < m_corrsize) {
            // Find the center of mass in the vicinity of the detected peak
            float tf = cepData[ti4 - 1] * (ti4 - 1);
            tf = tf + cepData[ti4] * (ti4);
            tf = tf + cepData[ti4 + 1] * (ti4 + 1);
            tf = tf / (cepData[ti4 - 1] + cepData[ti4] + cepData[ti4 + 1]);
            pperiod = tf / m_sampleRate;
        }
        else {
            pperiod = (float)ti4 / m_sampleRate;
        }
    }
    
    //    float tune = 440.0;
    float m_vthresh =0.7;
    // Convert to semitones
    //    float tf = (float)( -12 * arlog2(tune * pperiod) );
    //    float diffPitch = 0;
    if (conf >= m_vthresh) {
        //        diffPitch = tf - m_inpitch;
        //        inpitch = tf;
        //        m_inpitch = tf; // update pitch only if voiced
        curPitch.isVoiced = true;
        curPitch.freq = 1.0 / pperiod;
        curPitch.conf = conf;
        
    }else{
        curPitch.isVoiced = false;
        curPitch.freq = 1.0 / pperiod;
        curPitch.conf = conf;
    }
}



void CPitchDetection::Seek(unsigned int position)
{
    int time = position * (1000.0 / m_sampleRate);
    if(position > m_totalSamples)
        pYinVamp->Flush();
    else
        pYinVamp->Clear(time);
    pYinVamp->SetLastCalTimestamp(time);
    m_slideWindow->Flush();
    m_totalSamples = position;
}

void CPitchDetection::Reset() {
    if(pYinVamp != NULL)
    {
        delete pYinVamp;
        pYinVamp = NULL;
    }
    pYinVamp = new PYinVamp(m_sampleRate);
    if(!pYinVamp->initialise(1, m_hopSize, m_windowSize)) {
        throw CParamException("Invalid Param To Init PYinVamp.");
    }
    m_pitchVector.clear();
    m_noteTranscriptions.clear();
    m_maxPrevEnergy = 0;
}

