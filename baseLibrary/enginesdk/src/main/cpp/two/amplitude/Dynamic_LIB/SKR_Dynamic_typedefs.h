#ifndef SKR_DYNAMIC_TYPEDEFS_H
#define SKR_DYNAMIC_TYPEDEFS_H

#include "SKR_Dynamic_defines.h"

#ifdef __cplusplus
extern "C"
{
#endif

typedef struct poit{
	float x_db;
	float y_db;
}Point_db;

typedef struct LevelCurve{
	float k;//最左边直线的斜率
	float b_db;//最右边的直线的截距	
	Point_db P_db[MAX_POINT];//拐点们,这些点横坐标是递增的..
	int PLen;
}Y_X_db_Curve;

typedef enum
{
	NOUSE = 0,
	GAIN_D_DB,
	//GAIN_DB,不看实际增益的dB值了先
	GAIN_D,
	GAIN,
} SideChainUSE;


#ifdef __cplusplus
}
#endif


#endif