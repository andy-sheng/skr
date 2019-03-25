//
//  autorap_util.hpp
//  AutoRap
//
//  Created by allenyang on 16/9/7.
//  Copyright © 2016年 allenyang. All rights reserved.
//

#ifndef AUTORAP_UTIL_HPP
#define AUTORAP_UTIL_HPP

#include <stdlib.h>
#include <sstream>
#include <sys/time.h>
#include "audio_engine_common.h"

class CAutorapUtil
{
public:
    /* 从int转成float类型的同时，将数据范围调整为0.0~1.0
     */
    static void ConvertSampleFromInt16toFlt(const short src[], float dst[], const size_t n)
    {
#ifdef IOS
        //vDsp Implement
#else
        //android Implement
#endif
    }

    static void ConvertSampleFromFlttoInt16(const float src[], short dst[], const size_t n)
    {
#ifdef IOS
        //vDsp Implement
#else
        //android Implement
#endif
    }

    static std::string int2string(int v, bool blank = false) {
        std::ostringstream ostr;
        if (blank) {
            ostr << v << " ";
        } else {
            ostr << v;
        }

        return ostr.str();
    }
    static std::string double2string(double v, bool blank = false) {
        std::ostringstream ostr;
        if (blank) {
            ostr << v << " ";
        } else {
            ostr << v;
        }

        return ostr.str();
    }


    static std::string GetCurTimeInStr() {
        std::string curTimeStr;
        time_t now;
        time(&now);
        struct tm timenow;
        localtime_r(&now, &timenow);
        curTimeStr.append( CAutorapUtil::int2string(timenow.tm_year + 1900, false) + "_" +
                           CAutorapUtil::int2string(timenow.tm_mon + 1, false) + "_" +
                           CAutorapUtil::int2string(timenow.tm_mday, false) + "_" +
                           CAutorapUtil::int2string(timenow.tm_hour, false) + "_" +
                           CAutorapUtil::int2string(timenow.tm_min, false) + "_" +
                           CAutorapUtil::int2string(timenow.tm_sec, false));
        return curTimeStr;
    }

    static long GetCurrentMicroSecondTime() {
        struct timeval tv;
        gettimeofday(&tv, NULL);
        return tv.tv_sec * 1000 + tv.tv_usec;
    }
};

#endif
