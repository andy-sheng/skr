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

#include "PYinVamp.h"
#include "MonoNote.h"
#include "MonoPitch.h"
#include <vector>
#include <algorithm>
#include <cstdio>
#include <cmath>
#include <complex>
#include "mathUtil.hpp"
#include "audio_engine_common.h"
#include "autorap_logger.hpp"

using namespace std;


PYinVamp::PYinVamp(float inputSampleRate) :
    m_channels(0),
    m_stepSize(256),
    m_blockSize(2048),
    m_fmin(40),
    m_fmax(1600),
    m_yin(2048, inputSampleRate, 0.0),
    m_oSmoothedPitchTrack(0),
    m_oNotes(1),
    m_threshDistr(2.0f),
    m_outputUnvoiced(0.0f),
    m_preciseMode(false),
    m_lowAmp(0.1f),
    m_onsetSensitivity(0.5f),  // 取值 [0，1] note cut rms, 越接近 1 切分越多
    m_pruneThresh(0.065f), //minNoteTime: unit second
    m_pitchProb(0),
    m_timestamp(0),
    m_level(0),
    m_lastCalTimestamp(0),
    m_lastCalPitchIndex(0),
    m_lastCalNoteIndex(0)
{
    m_inputSampleRate = inputSampleRate;
}

PYinVamp::~PYinVamp()
{
    if (m_inBuffer) {
        delete[] m_inBuffer;
        m_inBuffer=NULL;
    }
}

bool
PYinVamp::initialise(size_t channels, size_t stepSize, size_t blockSize)
{
    if (channels < getMinChannelCount() ||
	channels > getMaxChannelCount()) return false;

    m_channels = channels;
    m_stepSize = stepSize;
    m_blockSize = blockSize;
    
    m_oSmoothedPitchTrack = FREQUENCY_FLAG;
    m_oNotes = NOTE_FLAG;
    
    if (m_inBuffer==NULL) {
        m_inBuffer = new float[m_blockSize];
    }
    CMathUtil::FillArray(m_inBuffer, 0, int(m_blockSize));
    reset();

    return true;
}

void
PYinVamp::process(const float *inputBuffers, RealTime timestamp)
{
    LOG4ARProfiling;
    float rms = 0;
    double dInputBuffers[m_blockSize];
    
//    for (size_t i = 0; i < m_blockSize; ++i) {
//        dInputBuffers[i] = inputBuffers[i];
//        rms += inputBuffers[i] * inputBuffers[i];
//    }
//    rms /= m_blockSize;
//    rms = sqrt(rms);

    CMathUtil::Float2Double((float *)inputBuffers, dInputBuffers, (int)m_blockSize);
    CMathUtil::SumSquareArray(inputBuffers, rms, (int)m_blockSize);
    rms /= m_blockSize;
    rms = sqrt(rms);
    
    bool isLowAmplitude = (rms < m_lowAmp);
    
    Yin::YinOutput yo = m_yin.processProbabilisticYin(dInputBuffers);

    m_level.push_back(yo.rms);

    // First, get the things out of the way that we don't want to output 
    // immediately, but instead save for later.
    vector<pair<double, double> > tempPitchProb;
    for (size_t iCandidate = 0; iCandidate < yo.freqProb.size(); ++iCandidate)
    {
        double tempPitch = 12 * std::log(yo.freqProb[iCandidate].first / 440) / std::log(2.) + 69;
        if (!isLowAmplitude)
        {
            tempPitchProb.push_back(pair<double, double>
                (tempPitch, yo.freqProb[iCandidate].second));
        } else {
            float factor = ((rms+0.01*m_lowAmp)/(1.01*m_lowAmp));
            tempPitchProb.push_back(pair<double, double>
                (tempPitch, yo.freqProb[iCandidate].second*factor));
        }
    }
    m_pitchProb.push_back(tempPitchProb);
    m_timestamp.push_back(timestamp);

    //每隔3s做一次Pitch和Note的计算
    if(timestamp >= (m_lastCalTimestamp + CAL_PITCH_INTERVAL)) {
        calRemainFeatures();
        m_lastCalTimestamp = m_timestamp[m_lastCalPitchIndex - 1];
    }
}

void PYinVamp::calRemainFeatures() {
    LOG4ARProfiling;
    //1:计算Frequency Path
    vector<float> freqPaths = m_pitchProcessor.process(m_pitchProb);
    m_freqPaths.insert(m_freqPaths.end(), freqPaths.begin(), freqPaths.end());
    //2:将Frequency转换为Pitch
    vector<float> freqPathsForNote(m_freqPaths.begin() + m_lastCalNoteIndex, m_freqPaths.end());
    std::vector<std::vector<std::pair<double, double> > > smoothedPitchs;
    for (size_t iFrame = 0; iFrame < freqPathsForNote.size(); ++iFrame) {
        std::vector<std::pair<double, double> > temp;
        if (freqPathsForNote[iFrame] > 0)
        {
            double tempPitch = 12 * std::log(freqPathsForNote[iFrame]/440)/std::log(2.) + 69;
            temp.push_back(std::pair<double,double>(tempPitch, .9));
        }
        smoothedPitchs.push_back(temp);
    }
    //3:利用Pitch计算Note Path
    vector<MonoNote::FrameOutput> notePaths = m_noteProcessor.process(smoothedPitchs);
    m_notePaths.insert(m_notePaths.end(), notePaths.begin(), notePaths.end());
    m_smoothedPitchs.insert(m_smoothedPitchs.end(), smoothedPitchs.begin(), smoothedPitchs.begin() + notePaths.size());
    //4:更新已经计算过的Index
    m_lastCalPitchIndex = m_freqPaths.size();
    m_lastCalNoteIndex = m_notePaths.size();
    vector<vector<pair<double, double> > > tempSubPitchProb(m_pitchProb.begin() + freqPaths.size(), m_pitchProb.end());
    m_pitchProb = tempSubPitchProb;
}

FeatureSet
PYinVamp::getRemainingFeatures()
{
    LOG4ARProfiling;
    //1:计算后续的所有未处理的Frequency
    m_pitchProcessor.markAsFinished();
    m_noteProcessor.markAsFinished();
    this->calRemainFeatures();
    //2:填充Frequency
    Feature f;
    f.hasTimestamp = true;
    f.hasDuration = false;
    for (size_t iFrame = 0; iFrame < m_freqPaths.size(); ++iFrame)
    {
        if (m_freqPaths[iFrame] < 0 && (m_outputUnvoiced==0)) continue;
        f.timestamp = m_timestamp[iFrame];
        f.values.clear();
        if (m_outputUnvoiced == 1)
        {
            f.values.push_back(fabs(m_freqPaths[iFrame]));
        } else {
            f.values.push_back(m_freqPaths[iFrame]);
        }
        m_featureSet[m_oSmoothedPitchTrack].push_back(f);
    }
    //3:填充Note
    f.hasTimestamp = true;
    f.hasDuration = true;
    f.values.clear();
    int onsetFrame = 0;
    bool isVoiced = 0;
    bool oldIsVoiced = 0;
    size_t nFrame = m_freqPaths.size();
    float minNoteFrames = (m_inputSampleRate*m_pruneThresh) / m_stepSize;
    
    
    std::vector<float> notePitchTrack; // collects pitches for one note at a time
    for (size_t iFrame = 0; iFrame < nFrame; ++iFrame)
    {
        isVoiced = m_notePaths[iFrame].noteState < 3
                   && m_smoothedPitchs[iFrame].size() > 0
                   && (iFrame >= nFrame-2
                       || ((m_level[iFrame]/m_level[iFrame+2]) > m_onsetSensitivity));
        // std::cerr << m_level[iFrame]/m_level[iFrame-1] << " " << isVoiced << std::endl;
        if (isVoiced && iFrame != nFrame-1)
        {
            if (oldIsVoiced == 0) // beginning of a note
            {
                onsetFrame = int(iFrame);
            }
            float pitch = m_smoothedPitchs[iFrame][0].first;
            notePitchTrack.push_back(pitch); // add to the note's pitch track
        } else { // not currently voiced
            if (oldIsVoiced == 1) // end of note
            {
                // std::cerr << notePitchTrack.size() << " " << minNoteFrames << std::endl;
                if (notePitchTrack.size() >= minNoteFrames)
                {
                    //std::sort(notePitchTrack.begin(), notePitchTrack.end());
                    CMathUtil::SortArray(&notePitchTrack[0], int(notePitchTrack.size()));
                    float medianPitch = notePitchTrack[notePitchTrack.size()/2];
                    float medianFreq = std::pow(2,(medianPitch - 69) / 12) * 440;
                    f.values.clear();
                    f.values.push_back(medianFreq);
                    f.timestamp = m_timestamp[onsetFrame];
                    f.duration = m_timestamp[iFrame] - m_timestamp[onsetFrame];
                    m_featureSet[m_oNotes].push_back(f);
                }
                notePitchTrack.clear();
            }
        }
        oldIsVoiced = isVoiced;
    }
    return m_featureSet;
}

void
PYinVamp::reset()
{
    m_yin.setThresholdDistr(m_threshDistr);
    m_yin.setFrameSize(m_blockSize);
    m_yin.setFast(!m_preciseMode);
    
    m_pitchProb.clear();
    m_timestamp.clear();
    m_level.clear();
}

void
PYinVamp::resetRemovedFrames()
{
    m_pitchProcessor.markAsStarted();
    m_noteProcessor.markAsStarted();
}

void
PYinVamp::Flush()
{
    m_pitchProcessor.markAsFinished();
    m_noteProcessor.markAsFinished();
    this->calRemainFeatures();
    this->resetRemovedFrames();
}

void
PYinVamp::Clear(unsigned int time)
{
    if(m_timestamp.empty())
        return;
    this->Flush();
    int iFrame = 0;
    while(iFrame < m_timestamp.size() && m_timestamp[iFrame] < time)
        iFrame++;
    m_timestamp.erase(m_timestamp.begin() + iFrame, m_timestamp.end());
    m_freqPaths.erase(m_freqPaths.begin() + iFrame, m_freqPaths.end());
    m_smoothedPitchs.erase(m_smoothedPitchs.begin() + iFrame, m_smoothedPitchs.end());
    m_notePaths.erase(m_notePaths.begin() + iFrame, m_notePaths.end());
    m_level.erase(m_level.begin() + iFrame, m_level.end());
    m_lastCalPitchIndex = m_freqPaths.size();
    m_lastCalNoteIndex = m_notePaths.size();
}
void
PYinVamp::SetLastCalTimestamp(unsigned int time)
{
    m_lastCalTimestamp = time;
}

string
PYinVamp::getIdentifier() const
{
    return "pyin";
}

string
PYinVamp::getName() const
{
    return "pYin";
}

string
PYinVamp::getDescription() const
{
    return "Monophonic pitch and note tracking based on a probabilistic Yin extension.";
}

string
PYinVamp::getMaker() const
{
    return "Matthias Mauch";
}

int
PYinVamp::getPluginVersion() const
{
    // Increment this each time you release a version that behaves
    // differently from the previous one
    return 2;
}

string
PYinVamp::getCopyright() const
{
    return "GPL";
}

size_t
PYinVamp::getPreferredBlockSize() const
{
    return 2048;
}

size_t
PYinVamp::getPreferredStepSize() const
{
    return 256;
}

size_t
PYinVamp::getMinChannelCount() const
{
    return 1;
}

size_t
PYinVamp::getMaxChannelCount() const
{
    return 1;
}


string
PYinVamp::getCurrentProgram() const
{
    return ""; // no programs
}

void
PYinVamp::selectProgram(string name)
{
}
