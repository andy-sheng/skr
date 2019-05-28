#ifndef Resample_SDK_API_H
#define Resample_SDK_API_H

#include "Resample_control.h"

#ifdef __cplusplus
extern "C"
{
#endif

void ResampleReset_API(Resample_ID *mResample);//注意必须在Calcu后Reset

void ResampleCalcu_API(Resample_ID *mResample);//Calcu后必须Reset----已经改为末尾加reset了

void ResampleInit_API(Resample_ID *mResample);

void ResampleRun_API(Resample_ID *mResample, short *input, int inLen, short *output, int *outLen);

#ifdef __cplusplus
}
#endif

#endif