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

#ifndef _YINUTIL_H_
#define _YINUTIL_H_

#include <cmath>
#include <iostream>
#include <vector>
#include <exception>

//#include "vamp-sdk/FFT.h"
//#include "MeanFilter.h"
#include "fft_wrapper.hpp"

class YinUtil
{
public:
    YinUtil();
    ~YinUtil();
    double sumSquare(const double *in, const size_t startInd, const size_t endInd);
    void difference(const double *in, double *yinBuffer, const size_t yinBufferSize);
    void fastDifference(const double *in, double *yinBuffer, const size_t yinBufferSize);
    void slowDifference(const double *in, double *yinBuffer, const size_t yinBufferSize);
    void cumulativeDifference(double *yinBuffer, const size_t yinBufferSize);
    int absoluteThreshold(const double *yinBuffer, const size_t yinBufferSize, const double thresh);
    std::vector<double> yinProb(const double *yinBuffer, const size_t prior, const size_t yinBufferSize, size_t minTau = 0, size_t maxTau = 0);
    void yinProb(const double *yinBuffer, const size_t prior, const size_t yinBufferSize,float outyinProbArray[],size_t minTau = 0, size_t maxTau = 0);
    double parabolicInterpolation(const double *yinBuffer, const size_t tau,
                                         const size_t yinBufferSize);
    float *m_inBuffer=NULL;
    float *m_inBufferPower=NULL;
    float *m_audioTransformedReal=NULL;
    float *m_audioTransformedImag=NULL;
    float *m_nullImag=NULL;
    float *m_kernel=NULL;
    float *m_kernelTransformedReal=NULL;
    float *m_kernelTransformedImag=NULL;
    float *m_yinStyleACFReal=NULL;
    float *m_yinStyleACFImag=NULL;
    float *m_powerTerms=NULL;
    CFFTWrapper* m_fftWrapper=NULL;
    int m_frameSize=0;
private:
    void reallocate(int framesize);
};

#endif
