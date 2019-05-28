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
	float k;//�����ֱ�ߵ�б��
	float b_db;//���ұߵ�ֱ�ߵĽؾ�	
	Point_db P_db[MAX_POINT];//�յ���,��Щ��������ǵ�����..
	int PLen;
}Y_X_db_Curve;

typedef enum
{
	NOUSE = 0,
	GAIN_D_DB,
	//GAIN_DB,����ʵ�������dBֵ����
	GAIN_D,
	GAIN,
} SideChainUSE;


#ifdef __cplusplus
}
#endif


#endif