#ifndef DCC_SDK_API_H
#define DCC_SDK_API_H

#include "DCC_control.h"

#ifdef __cplusplus
extern "C"
{
#endif

	void DCCReset_API(DCC_ID *mDCC);
	void DCCCalcu_API(DCC_ID *mDCC);
	void DCCRun_API(DCC_ID *mDCC,short *input,int inLen,short *output);

#ifdef __cplusplus
}
#endif

#endif