/* -*- c-basic-offset: 4 indent-tabs-mode: nil -*-  vi:set ts=8 sts=4 sw=4: */

/*
    pYIN - A fundamental frequency estimator for monophonic audio
    Centre for Digital Music, Queen Mary, University of London.
    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License as
    published by the Free Software Foundation; either version 2 of the
    License, or (at your option) any later version.  See the file
    COPYING included with this distribution for more information.
*/

#ifndef _PYINVAMP_H_
#define _PYINVAMP_H_

//#include <vamp-sdk/Plugin.h>

#include "Yin.h"
#include "MonoPitch.h"
#include "MonoNote.h"

typedef float RealTime ;

#define CAL_PITCH_INTERVAL  6 * 1000
typedef enum FEATURE_SET_KEY {
    FREQUENCY_FLAG = 0,
    NOTE_FLAG
} FEATURE_SET_KEY;

typedef struct Feature_t{
    bool hasDuration;
    bool hasTimestamp;
    RealTime timestamp;
    float duration;
    vector<double> values;
} Feature;

typedef struct tagFeatureSet
{
    int size;
    std::vector<std::vector<Feature>> features;
    tagFeatureSet(){
        size = 0;
        for(int i = 0; i < 2; i++) {
            std::vector<Feature> entity;
            features.push_back(entity);
        }
    }
    std::vector<Feature>& operator [](int i)
    {
        if(i >= size)
        {
            // 越界处理
        }
        return features[i];
    }
}FeatureSet;

class PYinVamp
{
public:
    PYinVamp(float inputSampleRate);
    virtual ~PYinVamp();
    
    /** 初始化方法，会进行Check一些Meta **/
    bool initialise(size_t channels, size_t stepSize, size_t blockSize);
    
    /** 处理一个窗口的数据 **/
    void process(const float *inputBuffers, RealTime timestamp);
    
    /** 计算结果 **/
    FeatureSet getRemainingFeatures();
    
    void reset();
    void resetRemovedFrames();
    void Flush();
    void Clear(unsigned int time);
    void SetLastCalTimestamp(unsigned int time);
    
    std::string getIdentifier() const;
    std::string getName() const;
    std::string getDescription() const;
    std::string getMaker() const;
    int getPluginVersion() const;
    std::string getCopyright() const;
    size_t getPreferredBlockSize() const;
    size_t getPreferredStepSize() const;
    size_t getMinChannelCount() const;
    size_t getMaxChannelCount() const;
    float getParameter(std::string identifier) const;
    void setParameter(std::string identifier, float value);
    std::string getCurrentProgram() const;
    void selectProgram(std::string name);

protected:
    float m_inputSampleRate;
    size_t m_channels;
    size_t m_stepSize;
    size_t m_blockSize;
    float m_fmin;
    float m_fmax;
    Yin m_yin;
    
    mutable int m_oSmoothedPitchTrack;
    mutable int m_oNotes;

    float m_threshDistr;
    float m_outputUnvoiced;
    bool m_preciseMode;
    float m_lowAmp;
    float m_onsetSensitivity;
    float m_pruneThresh;
    vector<vector<pair<double, double> > > m_pitchProb;
    vector<RealTime> m_timestamp;
    vector<float> m_level;
    
private:
    MonoPitch m_pitchProcessor;
    MonoNote m_noteProcessor;
    float m_lastCalTimestamp;
    size_t m_lastCalPitchIndex;
    size_t m_lastCalNoteIndex;
    
    FeatureSet m_featureSet;
    std::vector<float> m_freqPaths;
    std::vector<std::vector<std::pair<double, double> > > m_smoothedPitchs;
    std::vector<MonoNote::FrameOutput> m_notePaths;
    void calRemainFeatures();
    float *m_inBuffer=NULL;
};

#endif
