#ifndef SKR_DYHNAMIC_CONTROL_H
#define SKR_DYHNAMIC_CONTROL_H

#include "SKR_Dynamic_defines.h"
#include "SKR_Dynamic_typedefs.h"

#ifdef __cplusplus
extern "C"
{
#endif

typedef struct Dynamic_Mono_channel_memory{
	float memgain;
	float memgain_d;

	//目标增益(dB) - 输入(db)曲线
	float P_x_db[MAX_POINT];
	float k[MAX_POINT+1];
	float b_db[MAX_POINT+1];
	float x_clipdb;
	int Plen;

	float Aalpha;
	float Abeta;
	float Ralpha;
	float Rbeta;

	/*配置:*/	
	int Bypass;//按下则Expander不工作，信号直接出去,如果去掉和SideChain一样，可使每帧少一次判断。。
	SideChainUSE SideChain;//按下(SideChain==1)则用sidechain信号作为控制信号，否则
	int Chanel;
	Y_X_db_Curve CurveOption;
	int samplerate;
	float DynamicAttackms;
	float DynamicReleasems;
	int neverclipping;
}Dynamic_ID;


#ifdef __cplusplus
}
#endif

#endif