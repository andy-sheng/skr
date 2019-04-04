//just for 8kmono and N = 160 (analyzframe = 20ms)
#ifndef PITCH_NCC_SDK_API_H
#define PITCH_NCC_SDK_API_H

#include "pitch_NCC_control.h"

//this is suggest
#define ELPTHRESHOLD (30.0f) 
#define ZPTHRESHOLD (0.62f)


#ifdef __cplusplus
extern "C"
{
#endif

	void pitch_NCCReset_API(pitch_NCC_ID *mpitch_NCC);
	void pitch_NCCCalcu_API(pitch_NCC_ID *mpitch_NCC);
	int pitch_NCCRun_API(pitch_NCC_ID *mpitch_NCC,short *input,int inLen);

	void PitchAVGReset_API(APD_ID *mAPD);
	void PitchAVGCalcu_API(APD_ID *mAPD);
	float PitchAVGRun_API(APD_ID *mAPD,int pitch);
	float PeriodAVGRun_API(APD_ID *mAPD,int pitch,float period);

#ifdef __cplusplus
}
#endif

#endif