#ifndef SKR_DYNAMIC_SDK_API_H
#define SKR_DYNAMIC_SDK_API_H

#include "Dynamic_control.h"
#include "SKR_Dynamic_typedefs.h"

#ifdef __cplusplus
extern "C"
{
#endif

void DynamicReset_API(Dynamic_ID *mDynamic);
void DynamicCalcu_API(Dynamic_ID *mDynamic);
void DynamicRun_API(Dynamic_ID *mDynamic, short *input, float *x_db, int inLen, short *output, float *sidechain);
//void DynamicRun_API_dinsout(Dynamic_ID *mDynamic, float *input, float *x_db, int inLen, short *output, float *sidechain);
void DynamicRun_API_intinsout(Dynamic_ID *mDynamic, int *input, float *x_db, int inLen, short *output, float *sidechain);

#ifdef __cplusplus
}
#endif

#endif