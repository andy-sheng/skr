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

#include <vector>
#include <cstdlib>
#include <cstdio>
#include <cmath>
#include <complex>

#include "Yin.h"
#include "audio_engine_common.h"
#include "autorap_logger.hpp"
//#include "MeanFilter.h"
#include "YinUtil.h"
#include "mathUtil.hpp"

using namespace std;

Yin::Yin(size_t frameSize, size_t inputSampleRate, double thresh, bool fast) : 
    m_frameSize(frameSize),
    m_inputSampleRate(inputSampleRate),
    m_thresh(thresh),
    m_threshDistr(2),
    m_yinBufferSize(frameSize/2),
    m_fast(fast)
{
    if (frameSize & (frameSize-1)) {
      //  throw "N must be a power of two";
    }
    m_yinBuffer = new double[m_yinBufferSize];
    m_peakProbability = new float[m_yinBufferSize];
}

Yin::~Yin() 
{
    if (m_yinBuffer){
        delete[] m_yinBuffer;
        m_yinBuffer=NULL;
    }
    if (m_peakProbability) {
        delete [] m_peakProbability;
        m_peakProbability=NULL;
    }
}

Yin::YinOutput
Yin::process(const double *in)  {
    
//    double* yinBuffer = new double[m_yinBufferSize];

    // calculate aperiodicity function for all periods
//    if (m_fast) m_yinUtil.fastDifference(in, yinBuffer, m_yinBufferSize);
//    else
        m_yinUtil.slowDifference(in, m_yinBuffer, m_yinBufferSize);

    m_yinUtil.cumulativeDifference(m_yinBuffer, m_yinBufferSize);

    int tau = 0;
    tau = m_yinUtil.absoluteThreshold(m_yinBuffer, m_yinBufferSize, m_thresh);
        
    double interpolatedTau;
    double aperiodicity;
    double f0;
    
    if (tau!=0)
    {
        interpolatedTau = m_yinUtil.parabolicInterpolation(m_yinBuffer, abs(tau), m_yinBufferSize);
        f0 = m_inputSampleRate * (1.0 / interpolatedTau);
    } else {
        interpolatedTau = 0;
        f0 = 0;
    }
    float ss=0;
    CMathUtil::SumArray(m_yinUtil.m_inBufferPower, ss, int(m_yinBufferSize));
    double rms = std::sqrt(ss/m_yinBufferSize);
    //double rms = std::sqrt(m_yinUtil.sumSquare(in, 0, m_yinBufferSize)/m_yinBufferSize);
    aperiodicity = m_yinBuffer[abs(tau)];
    // std::cerr << aperiodicity << std::endl;
    if (tau < 0) f0 = -f0;

    Yin::YinOutput yo(f0, 1-aperiodicity, rms);
    for (size_t iBuf = 0; iBuf < m_yinBufferSize; ++iBuf)
    {
        yo.salience.push_back(m_yinBuffer[iBuf] < 1 ? 1-m_yinBuffer[iBuf] : 0); // why are the values sometimes < 0 if I don't check?
    }

    return yo;
}

Yin::YinOutput
Yin::processProbabilisticYin(const double *in)  {
    LOG4ARProfiling;
//    double* yinBuffer = new double[m_yinBufferSize];

    // calculate aperiodicity function for all periods
    if (m_fast) m_yinUtil.fastDifference(in, m_yinBuffer, m_yinBufferSize);
    else m_yinUtil.slowDifference(in, m_yinBuffer, m_yinBufferSize);

    m_yinUtil.cumulativeDifference(m_yinBuffer, m_yinBufferSize);

    //vector<double> m_peakProbability = m_yinUtil.yinProb(m_yinBuffer, m_threshDistr, m_yinBufferSize);
    m_yinUtil.yinProb(m_yinBuffer, m_threshDistr, m_yinBufferSize, m_peakProbability);//TODO 会导致有些地方检出的pitch低八度
    
    // calculate overall "probability" from peak probability
    //float probSum = 0;
//    for (size_t iBin = 0; iBin < m_yinBufferSize; ++iBin)
//    {
//        probSum += m_peakProbability[iBin];
//    }

    float ss=0;
    CMathUtil::SumArray(m_yinUtil.m_inBufferPower,ss,int(m_yinBufferSize));
    //double rms = std::sqrt(m_yinUtil.sumSquare(in, 0, m_yinBufferSize)/m_yinBufferSize);
    double rms = std::sqrt(ss/m_yinBufferSize);
    Yin::YinOutput yo(0,0,rms);
    for (size_t iBuf = 0; iBuf < m_yinBufferSize; ++iBuf)
    {
        yo.salience.push_back(m_peakProbability[iBuf]);
        if (m_peakProbability[iBuf] > 0)
        {
            double currentF0 = 
                m_inputSampleRate * (1.0 /
                m_yinUtil.parabolicInterpolation(m_yinBuffer, iBuf, m_yinBufferSize));
            yo.freqProb.push_back(pair<double, double>(currentF0, m_peakProbability[iBuf]));
        }
    }
    
    // std::cerr << yo.freqProb.size() << std::endl;
    
//    delete [] yinBuffer;
    return yo;
}


int
Yin::setThreshold(double parameter)
{
    m_thresh = static_cast<float>(parameter);
    return 0;
}

int
Yin::setThresholdDistr(float parameter)
{
    m_threshDistr = static_cast<size_t>(parameter);
    return 0;
}

int
Yin::setFrameSize(size_t parameter)
{
    m_frameSize = parameter;
    m_yinBufferSize = m_frameSize/2;
    return 0;
}

int
Yin::setFast(bool parameter)
{
    m_fast = parameter;
    return 0;
}
