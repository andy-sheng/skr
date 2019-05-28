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
#include <cstdio>
#include <cmath>
#include <algorithm>

#include "YinUtil.h"
#include "audio_engine_common.h"
#include "mathUtil.hpp"
#include "autorap_logger.hpp"

using namespace std;

YinUtil::YinUtil(){

}

YinUtil::~YinUtil(){
    DELETEARRAYSAFE(m_inBuffer);
    DELETEARRAYSAFE(m_inBufferPower);
    DELETEARRAYSAFE(m_audioTransformedReal);
    DELETEARRAYSAFE(m_audioTransformedImag);
    DELETEARRAYSAFE(m_nullImag);
    DELETEARRAYSAFE(m_kernel);
    DELETEARRAYSAFE(m_kernelTransformedReal);
    DELETEARRAYSAFE(m_kernelTransformedImag);
    DELETEARRAYSAFE(m_yinStyleACFReal);
    DELETEARRAYSAFE(m_yinStyleACFImag);
    DELETEARRAYSAFE(m_powerTerms);
    DELETEOBJSAFE(m_fftWrapper);
}

void YinUtil::reallocate(int framesize){
    if(framesize != m_frameSize){
        m_frameSize=framesize;
        DELETEARRAYSAFE(m_inBuffer);
        DELETEARRAYSAFE(m_inBufferPower);
        DELETEARRAYSAFE(m_audioTransformedReal);
        DELETEARRAYSAFE(m_audioTransformedImag);
        DELETEARRAYSAFE(m_nullImag);
        DELETEARRAYSAFE(m_kernel);
        DELETEARRAYSAFE(m_kernelTransformedReal);
        DELETEARRAYSAFE(m_kernelTransformedImag);
        DELETEARRAYSAFE(m_yinStyleACFReal);
        DELETEARRAYSAFE(m_yinStyleACFImag);
        DELETEARRAYSAFE(m_powerTerms);
        DELETEOBJSAFE(m_fftWrapper);

        m_inBuffer = new float[m_frameSize];
        m_inBufferPower = new float[m_frameSize];
        m_audioTransformedReal = new float[m_frameSize];
        m_audioTransformedImag = new float[m_frameSize];
        m_nullImag = new float[m_frameSize];
        m_kernel = new float[m_frameSize];
        m_kernelTransformedReal = new float[m_frameSize];
        m_kernelTransformedImag = new float[m_frameSize];
        m_yinStyleACFReal = new float[m_frameSize];
        m_yinStyleACFImag = new float[m_frameSize];
        m_powerTerms = new float[m_frameSize/2];
        m_fftWrapper = new CFFTWrapper(m_frameSize);
    }
}

void 
YinUtil::slowDifference(const double *in, double *yinBuffer, const size_t yinBufferSize)
{
    LOG4ARProfiling;
    yinBuffer[0] = 0;
    double delta ;
    int startPoint = 0;
    int endPoint = 0;
    for (int i = 1; i < yinBufferSize; ++i) {
        yinBuffer[i] = 0;
        startPoint = int(yinBufferSize)/2 - i/2;
        endPoint = startPoint + int(yinBufferSize);
        for (int j = startPoint; j < endPoint; ++j) {
            delta = in[i+j] - in[j];
            yinBuffer[i] += delta * delta;
        }
    }    
}

void 
YinUtil::fastDifference(const double *in, double *yinBuffer, const size_t yinBufferSize)
{
    LOG4ARProfiling;
    // DECLARE AND INITIALISE
    // initialisation of most of the arrays here was done in a separate function,
    // with all the arrays as members of the class... moved them back here.
    
    size_t frameSize = 2 * yinBufferSize;
    reallocate(int(frameSize));

    memset(yinBuffer,0, yinBufferSize * sizeof(double));
    memset(m_inBuffer,0,m_frameSize * sizeof(float));
    memset(m_inBufferPower,0,m_frameSize * sizeof(float));
    memset(m_audioTransformedReal,0,m_frameSize* sizeof(float));
    memset(m_audioTransformedImag,0,m_frameSize* sizeof(float));
    memset(m_nullImag,0,m_frameSize* sizeof(float));
    memset(m_kernel,0,m_frameSize * sizeof(float));
    memset(m_kernelTransformedReal,0,m_frameSize * sizeof(float));
    memset(m_kernelTransformedImag,0,m_frameSize * sizeof(float));
    memset(m_yinStyleACFReal,0,m_frameSize * sizeof(float));
    memset(m_yinStyleACFImag,0,m_frameSize * sizeof(float));
    memset(m_powerTerms,0,m_frameSize/2 * sizeof(float));

    for (size_t j = 0; j < frameSize; ++j) //todo: 可能是个bug，因为in的长度是yinBufferSize
    {
        m_inBuffer[j] = in[j];
    }
    CMathUtil::PowerArray(m_inBuffer,m_inBufferPower,int(frameSize));
    // POWER TERM CALCULATION
    // ... for the power terms in equation (7) in the Yin paper
    CMathUtil::SumArray(m_inBufferPower, m_powerTerms[0], int(yinBufferSize));
    
    // now iteratively calculate all others (saves a few multiplications)
    float tmpbuf[yinBufferSize];
    CMathUtil::FillArray(tmpbuf, 0, int(yinBufferSize));
    CMathUtil::SubArray(m_inBufferPower, m_inBufferPower+1+yinBufferSize, tmpbuf, int(yinBufferSize)-1);
    for (size_t tau = 1; tau < yinBufferSize; ++tau){
        m_powerTerms[tau] =m_powerTerms[tau-1]-tmpbuf[tau-1]; 
    }
//    for (size_t tau = 1; tau < yinBufferSize; ++tau) {
//        m_powerTerms[tau] = m_powerTerms[tau-1]  - m_inBufferPower[tau-1] + m_inBufferPower[tau+yinBufferSize];
//    }
    
    // YIN-STYLE AUTOCORRELATION via FFT
    // 1. data
    //Vamp::FFT::forward(frameSize, in, nullImag, audioTransformedReal, audioTransformedImag);
    m_fftWrapper->FftForward(m_inBuffer, m_audioTransformedReal, m_audioTransformedImag);
    // 2. half of the data, disguised as a convolution kernel
    memcpy(m_kernel, m_inBuffer, sizeof(float)*yinBufferSize);
    CMathUtil::ReverseArray(m_kernel, int(yinBufferSize));
    
   // Vamp::FFT::forward(frameSize, kernel, nullImag, kernelTransformedReal, kernelTransformedImag);
    m_fftWrapper->FftForward(m_kernel, m_kernelTransformedReal, m_kernelTransformedImag);

    // 3. convolution via complex multiplication -- written into
//    for (size_t j = 0; j < frameSize; ++j) { //todo: 优化
//        m_yinStyleACFReal[j] = m_audioTransformedReal[j]*m_kernelTransformedReal[j] - m_audioTransformedImag[j]*m_kernelTransformedImag[j]; // real
//        m_yinStyleACFImag[j] = m_audioTransformedReal[j]*m_kernelTransformedImag[j] + m_audioTransformedImag[j]*m_kernelTransformedReal[j]; // imaginary
//    }

    float tmp1[frameSize];CMathUtil::FillArray(tmp1, 0, int(frameSize));
    float tmp2[frameSize];CMathUtil::FillArray(tmp2, 0, int(frameSize));
    CMathUtil::MultiArrayByWeights(tmp1, m_audioTransformedReal, m_kernelTransformedReal, int(frameSize));
    CMathUtil::MultiArrayByWeights(tmp2, m_audioTransformedImag, m_kernelTransformedImag, int(frameSize));
    CMathUtil::SubArray(tmp1, tmp2, m_yinStyleACFReal, int(frameSize));
    CMathUtil::FillArray(tmp1, 0, int(frameSize));
    CMathUtil::FillArray(tmp2, 0, int(frameSize));
    CMathUtil::MultiArrayByWeights(tmp1, m_audioTransformedReal, m_kernelTransformedImag, int(frameSize));
    CMathUtil::MultiArrayByWeights(tmp2, m_audioTransformedImag, m_kernelTransformedReal, int(frameSize));
    CMathUtil::AddArray(tmp1, tmp2, m_yinStyleACFImag, int(frameSize));
    
    //Vamp::FFT::inverse(frameSize, yinStyleACFReal, yinStyleACFImag, audioTransformedReal, audioTransformedImag);
    m_fftWrapper->FftInverse(m_yinStyleACFReal, m_yinStyleACFImag, m_audioTransformedReal);
    
    // CALCULATION OF difference function
    // ... according to (7) in the Yin paper.
//    for (size_t j = 0; j < yinBufferSize; ++j) {
//        // taking only the real part
//        yinBuffer[j] = m_powerTerms[0] + m_powerTerms[j] - 2 * m_audioTransformedReal[j+yinBufferSize-1];
//    }
    CMathUtil::FillArray(tmpbuf, 0, int(yinBufferSize));
    CMathUtil::MultiArrayByScale(m_audioTransformedReal+yinBufferSize-1, tmpbuf, 2, int(yinBufferSize));
    CMathUtil::SubArray(m_powerTerms, tmpbuf, tmpbuf, int(yinBufferSize));
    CMathUtil::AddArrayByScale(tmpbuf, tmpbuf, m_powerTerms[0], int(yinBufferSize));
    CMathUtil::Float2Double(tmpbuf, yinBuffer, int(yinBufferSize));
}

void 
YinUtil::cumulativeDifference(double *yinBuffer, const size_t yinBufferSize)
{
    LOG4ARProfiling;
    size_t tau;
    
    yinBuffer[0] = 1;
    
    double runningSum = 0;
    
    for (tau = 1; tau < yinBufferSize; ++tau) {
        runningSum += yinBuffer[tau];
        if (runningSum == 0)
        {
            yinBuffer[tau] = 1;
        } else {
            yinBuffer[tau] *= tau / runningSum;
        }
    }    
}

int 
YinUtil::absoluteThreshold(const double *yinBuffer, const size_t yinBufferSize, const double thresh)
{
    LOG4ARProfiling;
    size_t tau;
    size_t minTau = 0;
    double minVal = 1000.;
    
    // using Joren Six's "loop construct" from TarsosDSP
    tau = 2;
    while (tau < yinBufferSize)
    {
        if (yinBuffer[tau] < thresh)
        {
            while (tau+1 < yinBufferSize && yinBuffer[tau+1] < yinBuffer[tau])
            {
                ++tau;
            }
            return int(tau);
        } else {
            if (yinBuffer[tau] < minVal)
            {
                minVal = yinBuffer[tau];
                minTau = tau;
            }
        }
        ++tau;
    }
    if (minTau > 0)
    {
        return int(-minTau);
    }
    return 0;
}

static float uniformDist[100] = {0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000,0.0100000};
static float betaDist1[100] = {0.028911,0.048656,0.061306,0.068539,0.071703,0.071877,0.069915,0.066489,0.062117,0.057199,0.052034,0.046844,0.041786,0.036971,0.032470,0.028323,0.024549,0.021153,0.018124,0.015446,0.013096,0.011048,0.009275,0.007750,0.006445,0.005336,0.004397,0.003606,0.002945,0.002394,0.001937,0.001560,0.001250,0.000998,0.000792,0.000626,0.000492,0.000385,0.000300,0.000232,0.000179,0.000137,0.000104,0.000079,0.000060,0.000045,0.000033,0.000024,0.000018,0.000013,0.000009,0.000007,0.000005,0.000003,0.000002,0.000002,0.000001,0.000001,0.000001,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000};
static float betaDist2[100] = {0.012614,0.022715,0.030646,0.036712,0.041184,0.044301,0.046277,0.047298,0.047528,0.047110,0.046171,0.044817,0.043144,0.041231,0.039147,0.036950,0.034690,0.032406,0.030133,0.027898,0.025722,0.023624,0.021614,0.019704,0.017900,0.016205,0.014621,0.013148,0.011785,0.010530,0.009377,0.008324,0.007366,0.006497,0.005712,0.005005,0.004372,0.003806,0.003302,0.002855,0.002460,0.002112,0.001806,0.001539,0.001307,0.001105,0.000931,0.000781,0.000652,0.000542,0.000449,0.000370,0.000303,0.000247,0.000201,0.000162,0.000130,0.000104,0.000082,0.000065,0.000051,0.000039,0.000030,0.000023,0.000018,0.000013,0.000010,0.000007,0.000005,0.000004,0.000003,0.000002,0.000001,0.000001,0.000001,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000};
static float betaDist3[100] = {0.006715,0.012509,0.017463,0.021655,0.025155,0.028031,0.030344,0.032151,0.033506,0.034458,0.035052,0.035331,0.035332,0.035092,0.034643,0.034015,0.033234,0.032327,0.031314,0.030217,0.029054,0.027841,0.026592,0.025322,0.024042,0.022761,0.021489,0.020234,0.019002,0.017799,0.016630,0.015499,0.014409,0.013362,0.012361,0.011407,0.010500,0.009641,0.008830,0.008067,0.007351,0.006681,0.006056,0.005475,0.004936,0.004437,0.003978,0.003555,0.003168,0.002814,0.002492,0.002199,0.001934,0.001695,0.001481,0.001288,0.001116,0.000963,0.000828,0.000708,0.000603,0.000511,0.000431,0.000361,0.000301,0.000250,0.000206,0.000168,0.000137,0.000110,0.000088,0.000070,0.000055,0.000043,0.000033,0.000025,0.000019,0.000014,0.000010,0.000007,0.000005,0.000004,0.000002,0.000002,0.000001,0.000001,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000};
static float betaDist4[100] = {0.003996,0.007596,0.010824,0.013703,0.016255,0.018501,0.020460,0.022153,0.023597,0.024809,0.025807,0.026607,0.027223,0.027671,0.027963,0.028114,0.028135,0.028038,0.027834,0.027535,0.027149,0.026687,0.026157,0.025567,0.024926,0.024240,0.023517,0.022763,0.021983,0.021184,0.020371,0.019548,0.018719,0.017890,0.017062,0.016241,0.015428,0.014627,0.013839,0.013068,0.012315,0.011582,0.010870,0.010181,0.009515,0.008874,0.008258,0.007668,0.007103,0.006565,0.006053,0.005567,0.005107,0.004673,0.004264,0.003880,0.003521,0.003185,0.002872,0.002581,0.002312,0.002064,0.001835,0.001626,0.001434,0.001260,0.001102,0.000959,0.000830,0.000715,0.000612,0.000521,0.000440,0.000369,0.000308,0.000254,0.000208,0.000169,0.000136,0.000108,0.000084,0.000065,0.000050,0.000037,0.000027,0.000019,0.000014,0.000009,0.000006,0.000004,0.000002,0.000001,0.000001,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000};
static float single10[100] = {0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,1.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000};
static float single15[100] = {0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,1.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000};
static float single20[100] = {0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,1.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000,0.00000};

void YinUtil::yinProb(const double *yinBuffer, const size_t prior, const size_t yinBufferSize,float outyinProbArray[],size_t minTau0, size_t maxTau0){
    LOG4ARProfiling;
    size_t minTau = 2;
    size_t maxTau = yinBufferSize;
    
    // adapt period range, if necessary
    if (minTau0 > 0 && minTau0 < maxTau0) minTau = minTau0;
    if (maxTau0 > 0 && maxTau0 < yinBufferSize && maxTau0 > minTau) maxTau = maxTau0;
    
    float minWeight = 0.01;
    int nThresholdInt = 100;
    size_t tau;
    float thresholds[nThresholdInt];CMathUtil::FillArray(thresholds, 0, nThresholdInt);
    float* distribution=NULL;
    CMathUtil::FillArray(outyinProbArray, 0, int(yinBufferSize));
    
    //    for (int i = 0; i < nThresholdInt; ++i)
    {
        switch (prior) {
            case 0:
                distribution = uniformDist;
                break;
            case 1:
                distribution = betaDist1;
                break;
            case 2:
                distribution = betaDist2;
                break;
            case 3:
                distribution = betaDist3;
                break;
            case 4:
                distribution = betaDist4;
                break;
            case 5:
                distribution = single10;
                break;
            case 6:
                distribution = single15;
                break;
            case 7:
                distribution = single20;
                break;
            default:
                distribution = uniformDist;
        }
        for (int i = 0; i < nThresholdInt; ++i)thresholds[i]=i+1;
        CMathUtil::MultiArrayByScale(thresholds, thresholds, 0.01, nThresholdInt);
    }
    
    
    int currThreshInd = nThresholdInt-1;
    tau = minTau;
    
    // double factor = 1.0 / (0.25 * (nThresholdInt+1) * (nThresholdInt + 1)); // factor to scale down triangular weight
    size_t minInd = 0;
    float minVal = 42.f;
    // while (currThreshInd != -1 && tau < maxTau)
    // {
    //     if (yinBuffer[tau] < thresholds[currThreshInd])
    //     {
    //         while (tau + 1 < maxTau && yinBuffer[tau+1] < yinBuffer[tau])
    //         {
    //             tau++;
    //         }
    //         // tau is now local minimum
    //         // std::cerr << tau << " " << currThreshInd << " "<< thresholds[currThreshInd] << " " << distribution[currThreshInd] << std::endl;
    //         if (yinBuffer[tau] < minVal && tau > 2){
    //             minVal = yinBuffer[tau];
    //             minInd = tau;
    //         }
    //         peakProb[tau] += distribution[currThreshInd];
    //         currThreshInd--;
    //     } else {
    //         tau++;
    //     }
    // }
    // double nonPeakProb = 1;
    // for (size_t i = minTau; i < maxTau; ++i)
    // {
    //     nonPeakProb -= peakProb[i];
    // }
    // 
    // std::cerr << tau << " " << currThreshInd << " "<< thresholds[currThreshInd] << " " << distribution[currThreshInd] << std::endl;
    float sumProb = 0;
    while (tau+1 < maxTau)
    {
        if (yinBuffer[tau] < thresholds[nThresholdInt-1] && yinBuffer[tau+1] < yinBuffer[tau])
        {
            while (tau + 1 < maxTau && yinBuffer[tau+1] < yinBuffer[tau])
            {
                tau++;
            }
            // tau is now local minimum
            // std::cerr << tau << " " << currThreshInd << " "<< thresholds[currThreshInd] << " " << distribution[currThreshInd] << std::endl;
            if (yinBuffer[tau] < minVal && tau > 2){
                minVal = yinBuffer[tau];
                minInd = tau;
            }
            currThreshInd = nThresholdInt-1;
            while (currThreshInd > -1 && thresholds[currThreshInd] > yinBuffer[tau]) {
                // std::cerr << distribution[currThreshInd] << std::endl;
                outyinProbArray[tau] += distribution[currThreshInd];
                currThreshInd--;
            }
            // peakProb[tau] = 1 - yinBuffer[tau];
            sumProb += outyinProbArray[tau];
            tau++;
        } else {
            tau++;
        }
    }
    
    if (outyinProbArray[minInd] > 1) {
        std::cerr << "WARNING: yin has prob > 1 ??? I'm returning all zeros instead." << std::endl;
        CMathUtil::FillArray(outyinProbArray, 0, int(yinBufferSize));
    }
    
    float nonPeakProb = 1;
    if (sumProb > 0) {
          //TODO 不能这么优化，会导致低八度检测的情况，源程序中对outyinProbArray[minInd]更新是对的
//        CMathUtil::MultiArrayByScale(outyinProbArray+minTau, outyinProbArray+minTau, minPeakValue/sumProb, maxTau-minTau);
//        CMathUtil::SumArray(outyinProbArray+minTau, nonPeakProb, maxTau-minTau);
//        nonPeakProb=-nonPeakProb;
        
        for (size_t i = minTau; i < maxTau; ++i)
        {
            outyinProbArray[i] = outyinProbArray[i] / sumProb * outyinProbArray[minInd];
            nonPeakProb -= outyinProbArray[i];
        }
    }
    if (minInd > 0)
    {
        // std::cerr << "min set " << minVal << " " << minInd << " " << nonPeakProb << std::endl; 
        outyinProbArray[minInd] += nonPeakProb * minWeight;
    }
}

std::vector<double>
YinUtil::yinProb(const double *yinBuffer, const size_t prior, const size_t yinBufferSize, const size_t minTau0, const size_t maxTau0) 
{
    LOG4ARProfiling;
    size_t minTau = 2;
    size_t maxTau = yinBufferSize;
    
    // adapt period range, if necessary
    if (minTau0 > 0 && minTau0 < maxTau0) minTau = minTau0;
    if (maxTau0 > 0 && maxTau0 < yinBufferSize && maxTau0 > minTau) maxTau = maxTau0;
    
    double minWeight = 0.01;
    int nThresholdInt = 100;
    size_t tau;
    float thresholds[nThresholdInt];CMathUtil::FillArray(thresholds, 0, nThresholdInt);
    float* distribution=NULL;
    std::vector<double> peakProb(yinBufferSize);// = std::vector<double>(yinBufferSize);
    
    //    for (int i = 0; i < nThresholdInt; ++i)
    {
        switch (prior) {
            case 0:
                distribution = uniformDist;
                break;
            case 1:
                distribution = betaDist1;
                break;
            case 2:
                distribution = betaDist2;
                break;
            case 3:
                distribution = betaDist3;
                break;
            case 4:
                distribution = betaDist4;
                break;
            case 5:
                distribution = single10;
                break;
            case 6:
                distribution = single15;
                break;
            case 7:
                distribution = single20;
                break;
            default:
                distribution = uniformDist;
        }
        for (int i = 0; i < nThresholdInt; ++i)thresholds[i]=i+1;
        CMathUtil::MultiArrayByScale(thresholds, thresholds, 0.01, nThresholdInt);
    }
    
    
    int currThreshInd = nThresholdInt-1;
    tau = minTau;
    
    // double factor = 1.0 / (0.25 * (nThresholdInt+1) * (nThresholdInt + 1)); // factor to scale down triangular weight
    size_t minInd = 0;
    float minVal = 42.f;
    // while (currThreshInd != -1 && tau < maxTau)
    // {
    //     if (yinBuffer[tau] < thresholds[currThreshInd])
    //     {
    //         while (tau + 1 < maxTau && yinBuffer[tau+1] < yinBuffer[tau])
    //         {
    //             tau++;
    //         }
    //         // tau is now local minimum
    //         // std::cerr << tau << " " << currThreshInd << " "<< thresholds[currThreshInd] << " " << distribution[currThreshInd] << std::endl;
    //         if (yinBuffer[tau] < minVal && tau > 2){
    //             minVal = yinBuffer[tau];
    //             minInd = tau;
    //         }
    //         peakProb[tau] += distribution[currThreshInd];
    //         currThreshInd--;
    //     } else {
    //         tau++;
    //     }
    // }
    // double nonPeakProb = 1;
    // for (size_t i = minTau; i < maxTau; ++i)
    // {
    //     nonPeakProb -= peakProb[i];
    // }
    // 
    // std::cerr << tau << " " << currThreshInd << " "<< thresholds[currThreshInd] << " " << distribution[currThreshInd] << std::endl;
    float sumProb = 0;
    while (tau+1 < maxTau)
    {
        if (yinBuffer[tau] < thresholds[nThresholdInt-1] && yinBuffer[tau+1] < yinBuffer[tau])
        {
            while (tau + 1 < maxTau && yinBuffer[tau+1] < yinBuffer[tau])
            {
                tau++;
            }
            // tau is now local minimum
            // std::cerr << tau << " " << currThreshInd << " "<< thresholds[currThreshInd] << " " << distribution[currThreshInd] << std::endl;
            if (yinBuffer[tau] < minVal && tau > 2){
                minVal = yinBuffer[tau];
                minInd = tau;
            }
            currThreshInd = nThresholdInt-1;
            while (currThreshInd > -1 && thresholds[currThreshInd] > yinBuffer[tau]) {
                // std::cerr << distribution[currThreshInd] << std::endl;
                peakProb[tau] += distribution[currThreshInd];
                currThreshInd--;
            }
            // peakProb[tau] = 1 - yinBuffer[tau];
            sumProb += peakProb[tau];
            tau++;
        } else {
            tau++;
        }
    }
    
    if (peakProb[minInd] > 1) {
        std::cerr << "WARNING: yin has prob > 1 ??? I'm returning all zeros instead." << std::endl;
        return(std::vector<double>(yinBufferSize));
    }
    
    double nonPeakProb = 1;
    if (sumProb > 0) {
        for (size_t i = minTau; i < maxTau; ++i)
        {
            peakProb[i] = peakProb[i] / sumProb * peakProb[minInd];
            nonPeakProb -= peakProb[i];
        }
    }
    if (minInd > 0)
    {
        // std::cerr << "min set " << minVal << " " << minInd << " " << nonPeakProb << std::endl; 
        peakProb[minInd] += nonPeakProb * minWeight;
    }
    return peakProb;
}

double
YinUtil::parabolicInterpolation(const double *yinBuffer, const size_t tau, const size_t yinBufferSize) 
{
    LOG4ARProfiling;
    // this is taken almost literally from Joren Six's Java implementation
    if (tau == yinBufferSize) // not valid anyway.
    {
        return static_cast<double>(tau);
    }
    
    double betterTau = 0.0;
    if (tau > 0 && tau < yinBufferSize-1) {
        float s0, s1, s2;
        s0 = yinBuffer[tau-1];
        s1 = yinBuffer[tau];
        s2 = yinBuffer[tau+1];
        
        double adjustment = (s2 - s0) / (2 * (2 * s1 - s2 - s0));
        
        if (abs(adjustment)>1) adjustment = 0;
        
        betterTau = tau + adjustment;
    } else {
        // std::cerr << "WARNING: can't do interpolation at the edge (tau = " << tau << "), will return un-interpolated value.\n";
        betterTau = tau;
    }
    return betterTau;
}

double 
YinUtil::sumSquare(const double *in, const size_t start, const size_t end)
{
    LOG4ARProfiling;
    double out = 0;
    for (size_t i = start; i < end; ++i)
    {
        out += in[i] * in[i];
    }
    return out;
}
