//
//  distribution.h
//  pitch_correction
//
//  Created by apple on 2017/3/28.
//  Copyright © 2017年 xiaokai.zhan. All rights reserved.
//

#ifndef _PYIN_distribution_h
#define _PYIN_distribution_h


#include <stdio.h>
#include <math.h>
namespace distribution{
    #define DISTRIBUTION_PI 3.141592653589793
    
    typedef struct tagNormal
    {
        double m_mean;//distribution mean
        double m_sd;  //standard deviation
        tagNormal(double mean, double sd) {
            m_mean = mean;
            m_sd = sd;
        }
    }Normal;
    
    /** ProbabilityDensityFunction **/
    inline double pdf(Normal nd, double x)
    {
        double mean = nd.m_mean;
        double sd = nd.m_sd;
        if(sd <= 0)
            return 0;
        double exponent = x - mean;
        exponent *= -exponent;
        exponent /= 2 * sd * sd;
        double result = exp(exponent);
        result /= sd * sqrt(2 * DISTRIBUTION_PI);
        return result;
    }
}

#endif /* _PYIN_distribution_h */
