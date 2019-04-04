#ifndef SKR_DYNAMIC_FUNCTIONS_H
#define SKR_DYNAMIC_FUNCTIONS_H

#include <math.h>
#include "../../common/functions.h"

#ifdef __cplusplus
extern "C"
{
#endif

void Gain_d_To_Gain(float *gd,float *g,float *memgi_1,float *memgdi_1,float alphaA,float betaA,float alphaR,float betaR,int len);
static __inline float Gain_d_dB(float x_db,float *k,float *b_db,int Plen,float *Px_db)
{
	int i;
	float gd_dB = 0;
	if (x_db <= Px_db[0])
	{
		return gd_dB = k[0]*x_db + b_db[0];
	}
	for (i=1;i<Plen;i++)
	{
		if (x_db > Px_db[i-1] && x_db <= Px_db[i])
		{
			return gd_dB = k[i]*x_db + b_db[i];
		}
	}
	if (x_db > Px_db[Plen-1])
	{
		return gd_dB = k[Plen]*x_db + b_db[Plen];
	}
	return gd_dB;

}
#ifdef __cplusplus
}
#endif

#endif