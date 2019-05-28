#ifndef Resample_CONTROL_H
#define Resample_CONTROL_H

#ifdef __cplusplus
extern "C"
{
#endif

#include "SKR_Resample_typedefs.h"

//#define RESAMPLE_MAXORDER 26  

#define RESAMPLE_I_MAX 10000
#define RESAMPLE_D_MAX 10000

typedef struct Resample_channel_memory{
	Resample_MEM memL;
	Resample_MEM memR;
	
	float bj[MAXNS*(2+1)];//j
	float aj[MAXNS*(2+1)];//j
	int I;
	int D;
	int LinearxIndexDelta[RESAMPLE_I_MAX];
	float w1[RESAMPLE_I_MAX];
	float w2[RESAMPLE_I_MAX];
	void (*resample)(Resample_MEM *mem,struct Resample_channel_memory *mResample,short *input,int inLen,short *output,int *outLen);
	//float b[RESAMPLE_MAXORDER+1];未采用直接I型。。
	//float a[RESAMPLE_MAXORDER+1];

	/*settings:*/
	int insamplerate;
	int chanel;
	int outsamplerate;
	int filterOrder;
	int DDSampling;//当I=1时，只有抽取，这时是直接抽还是滤波后抽，由此决定。因为可能被抽取序列可能不需要低通，这个有用户决定...
	int LinearOption;//为0时不许线性内插，为1时允许线性内插，为2时采用线性内插，为3时不滤镜像，为4时不去混叠，为5时不做任何滤波
	//int IThreshold;//启用线性内插的条件
	//int DThreshold;
}Resample_ID;


#ifdef __cplusplus
}
#endif

#endif