//
//  SlideWindow.h
//  pitch_correction
//
//  Created by zhouyu on 2017/9/20.
//  Copyright © 2017年 xiaokai.zhan. All rights reserved.
//

#ifndef SlideWindow_h
#define SlideWindow_h

#include <string.h>
#include <math.h>
#include <stdlib.h>
#include "audio_exception.hpp"

template <class _DT_>
class CSlideWindowT;

typedef CSlideWindowT<short> CSlideWindowS;
typedef CSlideWindowT<char> CSlideWindowC;
typedef CSlideWindowT<int> CSlideWindowI;
typedef CSlideWindowT<double> CSlideWindowD;
typedef CSlideWindowT<float> CSlideWindow;

template <class _DT_>
class CSlideWindowT {
private:
    size_t m_dataBufCap;
    size_t m_windowSize;
    size_t m_step;
    size_t m_idx;
    size_t m_newDataSize;
    bool m_newWindowFlag = false;
    size_t m_latency;
    size_t m_isHamm;
    _DT_ * m_dataBuf;
    _DT_ * m_retBuf;
    _DT_ * m_hammFunc;
public:
    CSlideWindowT(size_t windowSize, size_t step)
    {
        if (windowSize < step)
        {
            throw CParamException("windowSize must greater than step.");
        }
        m_dataBufCap = windowSize * 2;
        if(windowSize<2)windowSize=2;
        m_windowSize = windowSize;
        m_step = step;
        m_idx = 0;
        m_newDataSize = 0;
        m_latency = 0;
        m_isHamm = false;
        m_dataBuf = new _DT_[m_dataBufCap];
        memset(m_dataBuf, 0, sizeof(_DT_) * m_dataBufCap);
        m_retBuf = new _DT_[m_windowSize];
        m_hammFunc = NULL;
    }
    virtual ~CSlideWindowT()
    {
        if (m_dataBuf)
        {
            delete[] m_dataBuf;
        }
        m_dataBuf = NULL;
        if (m_hammFunc)
        {
            delete[] m_hammFunc;
        }
        m_hammFunc = NULL;
        if (m_retBuf)
        {
            delete[] m_retBuf;
            m_retBuf = NULL;
        }
    }
    bool Process(const _DT_ inputBuf[], const size_t size)
    {
        size_t leaveSize = size;
        bool ret = false;
        while (leaveSize > 0)
        {
            size_t idleSize = m_dataBufCap - m_idx;
            size_t n = leaveSize;
            if (n > idleSize)
            {
                n = idleSize;
            }
            memcpy(m_dataBuf + m_idx, inputBuf + size - leaveSize, sizeof(_DT_) * n);
            m_idx += n;
            if (m_idx >= m_dataBufCap)
            {
                m_idx -= m_dataBufCap;
            }
            leaveSize -= n;
            m_newDataSize += n;
            m_latency += n;
            if (m_newDataSize >= m_windowSize)
            {
                m_latency = m_newDataSize;
                while (m_newDataSize >= m_windowSize)
                {
                    m_newDataSize -= m_step;
                }
                int tmpBeginP = int(m_idx) - int(m_newDataSize + m_step);
                size_t tmpN = m_windowSize;
                if (tmpBeginP < 0)
                {
                    size_t tmpN1 = 0 - tmpBeginP;
                    if (tmpN1 > tmpN)
                    {
                        tmpN1 = tmpN;
                    }
                    memcpy(m_retBuf, m_dataBuf + m_dataBufCap + tmpBeginP, sizeof(_DT_) * tmpN1);
                    tmpN -= tmpN1;
                    tmpBeginP = 0;
                }
                memcpy(m_retBuf + m_windowSize - tmpN, m_dataBuf + tmpBeginP, sizeof(_DT_) * tmpN);
                if (m_isHamm)
                {
                    HammingWindow(m_retBuf);
                }
                ret = true;
                m_newWindowFlag = true;
            }
        }
        return ret;
    }
    
    size_t CheckOutSlideWindow(_DT_ outputBuf[], size_t &centerLatency, size_t &frontLatency)
    {
        if (m_latency < m_windowSize)
        {
            return 0;
        }
        memcpy(outputBuf, m_retBuf, sizeof(_DT_) * m_windowSize);
        frontLatency = m_latency;
        centerLatency = frontLatency;
        if (centerLatency > m_windowSize / 2)
        {
            centerLatency -= m_windowSize / 2;
        }
        m_newWindowFlag = false;
        return m_windowSize;
    }
    
    size_t CheckOutCurWindow(_DT_ outputBuf[])
    {
        //TODO
        //throw CParamException("not implement");
        
        size_t endPos = m_idx;
        size_t leaveSize = m_windowSize;
        while (leaveSize > 0)
        {
            int beginPos = (int)(endPos - leaveSize);
            if (beginPos < 0)
            {
                beginPos = 0;
            }
            int tmpSize = (int)(endPos - beginPos);
            endPos = beginPos;
            if (endPos == 0)
            {
                endPos = m_windowSize;
            }
            memcpy(outputBuf + leaveSize - tmpSize, m_dataBuf + beginPos, sizeof(_DT_) * tmpSize);
            leaveSize -= tmpSize;
        }
        
        return m_windowSize;
    }
    
    void SetHammWindow(bool isValid)
    {
        m_isHamm = isValid;
        if (!m_hammFunc)
        {
            m_hammFunc = new _DT_[m_windowSize];
        }
        _DT_ tmpF = 6.2831853072 / (m_windowSize-1);
        for (size_t i = 0; i < m_windowSize; i++)
        {
            m_hammFunc[i] = 0.5 - 0.5 * cos(tmpF * i);
        }
    }
    
    void HammingWindow(_DT_ buffer[])
    {
        for (size_t i = 0; i < m_windowSize; i++)
        {
            buffer[i] *= m_hammFunc[i];
        }
    }
    
    //size_t CheckOutSlideWindow(_DT_ outputBuf[], size_t &centerLatency, size_t &frontLatency)
    size_t Flush(_DT_ outputBuf[], size_t &centerLatency, size_t &frontLatency)
    {
        if (m_newWindowFlag)
        {
            size_t ret = CheckOutSlideWindow(outputBuf, centerLatency, frontLatency);
        }
        else
        {
            memset(outputBuf, 0, sizeof(_DT_) * m_windowSize);
            if (m_newDataSize == 0)
            {
                return 0;
            }
            _DT_ tmpBuf[m_windowSize];
            size_t ret1 = CheckOutCurWindow(tmpBuf);
            memcpy(outputBuf, tmpBuf + (ret1 - m_newDataSize), sizeof(_DT_) * m_newDataSize);
            centerLatency = m_newDataSize - m_windowSize / 2;
            frontLatency = m_newDataSize;
        }
        m_newDataSize = 0;
        return m_windowSize;
    }
    
    void Flush()
    {
        m_newDataSize = 0;
        m_newWindowFlag = false;
    }
};

#endif /* SlideWindow_h */
