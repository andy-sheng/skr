#ifndef Resample_SDK_API_H
#define Resample_SDK_API_H

#include "Resample_control.h"

#ifdef __cplusplus
extern "C"
{
#endif

void ResampleReset_API(Resample_ID *mResample);//ע�������Calcu��Reset

void ResampleCalcu_API(Resample_ID *mResample);//Calcu�����Reset----�Ѿ���Ϊĩβ��reset��

void ResampleInit_API(Resample_ID *mResample);

void ResampleRun_API(Resample_ID *mResample, short *input, int inLen, short *output, int *outLen);

#ifdef __cplusplus
}
#endif

#endif