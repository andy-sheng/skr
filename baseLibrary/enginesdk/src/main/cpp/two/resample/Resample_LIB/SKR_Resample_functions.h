#ifndef SKR_Resample_FUNCTIONS_H
#define SKR_Resample_FUNCTIONS_H

#include "Resample_control.h"
#include "../../common/functions.h"

#ifdef __cplusplus
extern "C"
{
#endif


int Gcd(int m,int n);
void CalcuLinearParameter(int D,int I,int *xIndexDelta,float *w1,float *w2);
void filterc(float *b,float *a,int n,int ns,float *x,int len,float *px,float *py);

void DD_Resample(Resample_MEM *mem,Resample_ID *mResample, short *input, int inLen, short *output, int *outLen );
void I_D_Shannon(Resample_MEM *mem,Resample_ID *mResample, short *input, int inLen, short *output, int *outLen );
void I_Shannon(Resample_MEM *mem,Resample_ID *mResample, short *input, int inLen, short *output, int *outLen );
void D_Shannon(Resample_MEM *mem,Resample_ID *mResample, short *input, int inLen, short *output, int *outLen );

void D_ID_Linear(Resample_MEM *mem,Resample_ID *mResample, short *input, int inLen, short *output, int *outLen );
void I_D_Linear(Resample_MEM *mem,Resample_ID *mResample, short *input, int inLen, short *output, int *outLen );
void D_I_Linear(Resample_MEM *mem,Resample_ID *mResample, short *input, int inLen, short *output, int *outLen );

#ifdef __cplusplus
}
#endif

#endif



