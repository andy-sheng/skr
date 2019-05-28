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

	//Ŀ������(dB) - ����(db)����
	float P_x_db[MAX_POINT];
	float k[MAX_POINT+1];
	float b_db[MAX_POINT+1];
	float x_clipdb;
	int Plen;

	float Aalpha;
	float Abeta;
	float Ralpha;
	float Rbeta;

	/*����:*/	
	int Bypass;//������Expander���������ź�ֱ�ӳ�ȥ,���ȥ����SideChainһ������ʹÿ֡��һ���жϡ���
	SideChainUSE SideChain;//����(SideChain==1)����sidechain�ź���Ϊ�����źţ�����
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