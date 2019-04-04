#ifndef NATUREMIX_SDK_API_H
#define NATUREMIX_SDK_API_H

#include "NatureMix_control.h"

#ifdef __cplusplus
extern "C"
{
#endif

	void NatureMixReset_API(NatureMix_ID *mNatureMix);
	void NatureMixCalcu_API(NatureMix_ID *mNatureMix);
	void NatureMixCalcu_API_ForVMic(NatureMix_ID *mNatureMix);
	void NatureMixRun_API(NatureMix_ID *mNatureMix,	short *input[],int inLen,short *output,float *sidechain);    
    void NatureMixRun_API_FIXWeight(NatureMix_ID *mNatureMix, float fweight[], short *input[],int inLen,short *output, float *sidechain);
    void ComlimRun_API(NatureMix_ID *mNatureMix, float *input,int inLen,short *output);
	void ComlimRun_API_intIshortO(NatureMix_ID *mNatureMix, int *input,int inLen,short *output);

#ifdef __cplusplus
}
#endif

#endif