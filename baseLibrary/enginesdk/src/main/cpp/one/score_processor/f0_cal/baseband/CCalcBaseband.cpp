//
//  CCalcBaseband.cpp
//  Test
//
//  Created by allenyang on 16/5/12.
//  Copyright © 2016年 allenyang. All rights reserved.
//

/*
 vDSP中对于实数和复数的存储进行了优化，在进行fft变换前需要调用vDSP_ctoz进行格式转换。以下是格式说明
Data Packing for Real FFTs

The discrete Fourier transform functions in the vDSP API provide a unique case in data formatting to conserve memory. Real-to-complex discrete Fourier transforms write their output data in special packed formats so that the complex output requires no more memory than the real input.

Packing and Transformation Functions
Applications that call the real FFT may have to use two transformation functions, one before the FFT call and one after. This is required if the input array is not in the even-odd split configuration.

A real array A = {A[0],...,A[n]} must be transformed into an even-odd array AEvenOdd = {A[0],A[2],...,A[n-1],A[1],A[3],...A[n]} by means of the call vDSP_ctoz.

The result of a real FFT on AEvenOdd of dimension n is a complex array of dimension 2n, with a very special format:

{[DC,0],C[1],C[2],...,C[n/2],[NY,0],Cc[n/2],...,Cc[2],Cc[1]}

where:

Values DC and NY are the DC and Nyquist components (real values)
Array C is complex in a split representation
Array Cc is the complex conjugate of C in a split representation
For a real array A of size n , the results, which are complex, require 2 * n spaces. However, much of this data is either always zero or is redundant, so this data can be omitted. Thus, to fit this result into the same space as the input array, the algorithm throws away these unnecessary values. The real FFT stores its results as follows:

{[DC,NY],C[1],C[2],...,C[n/2]}.
*/

/*****************************************************************************/

#include <assert.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <stdio.h>
#define PI (float)3.14159265358979323846
#define L2SC (float)3.32192809488736218171
#include <iostream>
using namespace std;

/*****************************************************************************/

#include "CCalcBaseband.h"
using namespace std;

// DONE WITH FFT CODE


// The port numbers

#define AT_VOICECONF 0.7

#define LOG_TAG "CCalcBaseband"

void CCalcBaseband::Init(int sampleRate, int channels)
{
    
    int ti;
    
    m_fs = sampleRate;
    m_channels = channels;
    
    if ((sampleRate>=88200) /*&& !isFaseMode*/)
    {
        m_cbsize = 4096;
    }
    else
    {
        m_cbsize = 2048;
    }
    m_corrsize = m_cbsize / 2 + 1;
    
    m_pmax = 1/(float)70;  // max and min periods (ms)
    m_pmin = 1/(float)700; // eventually may want to bring these out as sliders
    
    m_nmax = int(sampleRate * m_pmax);
    if (m_nmax > m_corrsize) {
        m_nmax = m_corrsize;
    }
    m_nmin = (int)(sampleRate * m_pmin);
    
    m_cbi = new float[m_cbsize];
    memset(m_cbi, 0, sizeof(float) * m_cbsize);
    m_cbf = new float[m_cbsize];
    memset(m_cbf, 0, sizeof(float) * m_cbsize);
    
    m_cbiwr = 0;
    m_cbiwrol = 0;
    
    m_flamb = -(0.8517*sqrt(atan(0.06583*sampleRate))-0.1916);
    
    
    // Generate a window with a single raised cosine from N/4 to 3N/4
    m_cbwindow = new float[m_cbsize];
    memset(m_cbwindow, 0, sizeof(float) * m_cbsize);
    for (ti=0; ti<(m_cbsize / 2); ti++) {
        m_cbwindow[ti+m_cbsize/4] = -0.5*cos(4*PI*ti/(m_cbsize - 1)) + 0.5; //汉明窗滤波器
    }
    
    m_noverlap = 4;
    
    m_cbsizeDivOverlap = m_cbsize / m_noverlap ;
    
	m_fmembvars = new CFFTWrapper(m_cbsize);
    
    m_ffttime = new float[m_cbsize];
    float m_fftfreqre[m_cbsize];
    float m_fftfreqim[m_cbsize];
    
    m_acwinv = new float[m_cbsize];
    for (ti=0; ti<m_cbsize; ti++) {
        
        m_ffttime[ti] = m_cbwindow[ti];
    }
	m_fmembvars->FftForward(m_cbwindow, m_fftfreqre, m_fftfreqim);
    for (ti=0; ti<m_corrsize; ti++) {
        m_fftfreqre[ti] = (m_fftfreqre[ti])*(m_fftfreqre[ti]) + (m_fftfreqim[ti])*(m_fftfreqim[ti]);
        m_fftfreqim[ti] = 0;
    }
    m_fmembvars->FftInverse(m_fftfreqre, m_fftfreqim, m_ffttime);
    for (ti=1; ti<m_cbsize; ti++) {
        m_acwinv[ti] = m_ffttime[ti]/m_ffttime[0];
        if (m_acwinv[ti] > 0.000001) {
            m_acwinv[ti] = (float)1/m_acwinv[ti];
        }
        else {
            m_acwinv[ti] = 0;
        }
    }
    m_acwinv[0] = 1;
    // ---- END Calculate autocorrelation of window ----
    
    m_vthresh = AT_VOICECONF;  //  The voiced confidence (unbiased peak) threshold level
    m_pfTune = 440.0;
    m_pfConf = 0.0;
    
    m_ti4 = 0;
    
    m_conf = 0;
    m_inpitch = 0;
    m_totalSampleCount = 0;
    
    
}


void CCalcBaseband::rePos()
{
    m_cbiwr=0;
    m_cbiwrol=0;
    m_totalSampleCount=0;
}

//使用倒谱分析法来计算基频
void CCalcBaseband::getFreqAndConf(int16_t *inputSamples, uint32_t sampleCount, float *f0, float *_conf){

    int16_t* pfInput;
    uint32_t lSampleIndex;
    
    int N;
    int Nf;
    
    int64_t ti;
    int64_t ti2;
    int64_t ti3;
    int64_t ti4;
    float tf;
    float tf2;
    
    // Variables for cubic spline interpolator
    float fInSample;
    
    float pperiod;
    float inpitch;
    float conf;
    
    
    assert(sampleCount % 2 == 0);
    pfInput = inputSamples;
    
    ////////////////
    m_totalSampleCount += sampleCount;
    
    
    
    tf = (1+m_flamb)/(1-m_flamb);
    
    N = m_cbsize;
    Nf = m_corrsize;
    
    pperiod = m_pmax;
    inpitch = m_inpitch;
    conf = m_conf;
    
    //int loopcnt=0;
    
    /*******************
     *  MAIN DSP LOOP  *
     *******************/
    for (lSampleIndex = 0; lSampleIndex < sampleCount; lSampleIndex+=m_channels)  {
        
        // load data into circular buffer
        tf = ((float) *(pfInput+lSampleIndex)) / 32768.0; //TODO converter
        fInSample = tf;
        
        ti4 = m_cbiwr;
        m_cbi[ti4] = tf;
        
        m_cbf[ti4] = tf;
        
        // Input write pointer logic
        m_cbiwr++;
        if (m_cbiwr >= N) {
            m_cbiwr = 0;
        }
        
        m_cbiwrol++;
        if (m_cbiwrol >= m_cbsizeDivOverlap)
        {
            m_cbiwrol = 0;
        }
        
        
        // ********************
        // * Low-rate section *
        // ********************
        
        // Every N/noverlap samples, run pitch estimation / manipulation code
        if (m_cbiwrol == 0) { // 优化取模运算
            //loopcnt++;
            // ---- Obtain autocovariance ----
            
            // Window and fill FFT buffer
            ti2 = m_cbiwr;
            ti3 = ti2;
            for (ti=0; ti<N; ti++) { //TODO vDSP
                m_ffttime[ti] = (float)(m_cbi[ti3]*m_cbwindow[ti]); // 优化取模运算
                if (ti3 == 0)
                {
                    ti3 = N -1;
                }
                else
                {
                    ti3--;
                }
            }
            
			// 计算倒谱都统一抽取出函数
            m_fmembvars->CalcCepstrum(m_ffttime, m_ffttime);
            
            //  ---- END Obtain autocovariance ----
            
            
            //  ---- Calculate pitch and confidence ----
            
            // Calculate pitch period
            //   Pitch period is determined by the location of the max (biased)
            //     peak within a given range
            //   Confidence is determined by the corresponding unbiased height
            tf2 = 0;
            pperiod = m_pmin;
            
            int tmpMin = m_nmin + 1;
            
            for (; tmpMin < m_nmax; tmpMin++)
            {
                if (m_ffttime[tmpMin] > m_ffttime[tmpMin -1])
                {
                    break;
                }
            }
            
            int tmpMax = m_nmax -1;
            for (; tmpMax > tmpMin; tmpMax--)
            {
                if (m_ffttime[tmpMax] <= m_ffttime[tmpMax -1])
                {
                    break;
                }
            }//寻找波形的开始结束位置
            
            
            float partMax = 0;
            for (ti = tmpMin; ti < tmpMax; ti++)
            {
                if (partMax < m_ffttime[ti])
                {
                    partMax = m_ffttime[ti];
                }//找出波峰的位置
            }
            partMax = partMax * 0.5;
            
            ti4 = 0;
            float tmpFft = -1;
            float fScoreMin = 1000000000;
            float tmpScore = 0;
            float tmpPS = 0;
            for (ti = tmpMin; ti < tmpMax; ti++)
            {
                if ((tmpFft < m_ffttime[ti])
                    && ( m_ffttime[ti] >= m_ffttime[ti + 1])
                    && (m_ffttime[ti] > partMax))
                {
                    tmpScore = m_cbi[ (m_cbiwr + N / 2) % N] - m_cbi[(m_cbiwr + N / 2 - ti) % N];
                    tmpScore *= tmpScore;
                    tmpScore += 0.0001 * (ti - m_ti4) * (ti - m_ti4);
                    if (m_ti4 > 10)
                    {
                        tmpPS = log(float(ti) / m_ti4);
                        tmpPS *= tmpPS;
                        tmpScore += tmpPS * 0.0000005 ;
                    }
                    if (tmpScore < fScoreMin)
                    {
                        fScoreMin = tmpScore;
                        ti4 = ti;
                        tf2 = m_ffttime[ti];
                    }
                }
                tmpFft = m_ffttime[ti];
            }
            m_ti4 = ti4;
            
            if (tf2>0) {
                conf = tf2*m_acwinv[ti4];
                if (ti4>0 && ti4<Nf) {
                    // Find the center of mass in the vicinity of the detected peak
                    tf = m_ffttime[ti4-1]*(ti4-1);
                    tf = tf + m_ffttime[ti4]*(ti4);
                    tf = tf + m_ffttime[ti4+1]*(ti4+1);
                    tf = tf/(m_ffttime[ti4-1] + m_ffttime[ti4] + m_ffttime[ti4+1]);
                    pperiod = tf/m_fs;
                }
                else {
                    pperiod = (float)ti4/m_fs;
                }
            }
            
            // Convert to semitones
            tf = (float) -12*log10((float)m_pfTune*pperiod)*L2SC;
            
            if (conf>=m_vthresh) {
                inpitch = tf;
                m_inpitch = tf; // update pitch only if voiced
            }
            m_conf = conf;
            
            m_pfConf = conf;
            
//            LOGI("inpitch=%f,freq=%f \n",m_inpitch,1/pperiod);
            //  ---- END Calculate pitch and confidence ---- 计算极频和信心值结束
        }
    }
    
//    LOGI("loopcnt=%d \n ====================================\n",loopcnt);
    
    //return the last frequence and conf
    *f0   = 1/pperiod;
    *_conf      = conf;
    //LOGI("ccalcbaseband 1: f0=%f  conf=%f  inputSamples =%l  chanel = %d \n",*f0,*_conf,inputSamples,m_channels);
    return;
    
}

void CCalcBaseband::SeekFromStart(int frameNum)
{
    m_totalSampleCount = frameNum * m_channels;
}

CCalcBaseband::CCalcBaseband()
{
    m_fmembvars = NULL;
    m_cbi = NULL;
    m_cbf = NULL;
    m_cbwindow = NULL;
    m_acwinv = NULL;
}

/********************
 *  THE DESTRUCTOR! *
 ********************/
CCalcBaseband::~CCalcBaseband()
{
	if(m_fmembvars != NULL )
	{
		delete m_fmembvars;
		m_fmembvars = NULL;
	}
    if (m_cbi != NULL)
    {
        delete[] m_cbi;
        m_cbi = NULL;
    }
    if (m_cbf != NULL)
    {
        delete[] m_cbf;
        m_cbf = NULL;
    }
    if (m_cbwindow != NULL)
    {
        delete[] m_cbwindow;
        m_cbwindow = NULL;
    }
    if (m_acwinv != NULL)
    {
        delete[] m_acwinv;
        m_acwinv = NULL;
    }
    if (m_ffttime != NULL)
    {
        delete[] m_ffttime;
        m_ffttime = NULL;
    }
}

