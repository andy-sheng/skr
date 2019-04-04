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
	//float b[RESAMPLE_MAXORDER+1];δ����ֱ��I�͡���
	//float a[RESAMPLE_MAXORDER+1];

	/*settings:*/
	int insamplerate;
	int chanel;
	int outsamplerate;
	int filterOrder;
	int DDSampling;//��I=1ʱ��ֻ�г�ȡ����ʱ��ֱ�ӳ黹���˲���飬�ɴ˾�������Ϊ���ܱ���ȡ���п��ܲ���Ҫ��ͨ��������û�����...
	int LinearOption;//Ϊ0ʱ���������ڲ壬Ϊ1ʱ���������ڲ壬Ϊ2ʱ���������ڲ壬Ϊ3ʱ���˾���Ϊ4ʱ��ȥ�����Ϊ5ʱ�����κ��˲�
	//int IThreshold;//���������ڲ������
	//int DThreshold;
}Resample_ID;


#ifdef __cplusplus
}
#endif

#endif