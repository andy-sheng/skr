#ifndef SKR_DYNAMIC_DEFINES_H
#define SKR_DYNAMIC_DEFINES_H

#include "../../common/defines.h"

#ifdef __cplusplus
extern "C"
{
#endif

#define MAX_POINT (8)
#define SIGMA 0.95 //认为完成了0.95的逼近就完成了启动,和释放，注意，用于以指数逼近目标增益时

#ifdef __cplusplus
}
#endif

#endif