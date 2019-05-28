#include <stdlib.h>
#include "effect_control.h"
#include <assert.h>
#include <math.h>
#include <stdio.h>

#include "../../common/functions.h"
#include "../../common/defines.h"

void EchoReset_API(Echo_s *mEcho)
{
	int i;

	for (i=0;i<MAXORDER_DELAY+SKR_MAX_FRAME_SAMPLE_MONO;i++)
	{
		mEcho->meminputL[i] = 0;
		mEcho->memoutputL[i] = 0;
		mEcho->meminputR[i] = 0;
		mEcho->memoutputR[i] = 0;
	}
}
void setBMB(Echo_s *mEcho)
{
	mEcho->Echo_a = 0.18;
	mEcho->Echo_g = 0.502;
	mEcho->Echo_D_ms = 190.0;
	mEcho->Echo_depth_ms = 0.0;
	mEcho->Echo_b = 1.0;
	mEcho->Echo_freqHz = 0.0;
}

void EchoCalcu_API(Echo_s *mEcho)
{
	mEcho->Echo_D = mEcho->Echo_D_ms/1000.0 * mEcho->samplerate;
	assert(mEcho->Echo_D<MAXORDER_DELAY);
	//mEcho->aD = -mEcho->Echo_a*mEcho->Echo_g;
	//mEcho->bD = mEcho->Echo_a;



}
static void SKRiir_xDyD_2(short *output, short *input, int inlen,float *mem_output, short *mem_input, float a, float g,int D)
{
	short i,j;
	float sumx,sumy;
	short *x;
	float *y;
	int border;
	int aorder;

	border = D;
	aorder = D;

	for(i=0;i<border;i++)mem_input[i]=mem_input[i+inlen];
	for(i=0;i<inlen;i++)mem_input[i+border]=input[i];
	x=mem_input+ border;

	for(i=0;i<aorder;i++)mem_output[i]=mem_output[i+inlen];
	y=mem_output+ aorder;

	for(i=0;i<inlen;i++)
	{
		y[i]=y[i-aorder]*g + x[i-border];
	}

	for(i=0;i<inlen;i++)output[i]=stoshort(y[i]*a+input[i]);
}
static void SKRiir_xyD_Dn_2(short *output, short *input, int inlen,float *mem_output, short *mem_input, float a, float g,float b,int *D,int maxD)
{
	short i,j;
	float sumx,sumy;
	short *x;
	float *y;
	int border;
	int aorder;

	border = maxD;
	aorder = maxD;

	for(i=0;i<border;i++)mem_input[i]=mem_input[i+inlen];
	for(i=0;i<inlen;i++)mem_input[i+border]=input[i];
	x=mem_input+ border;

	for(i=0;i<aorder;i++)mem_output[i]=mem_output[i+inlen];
	y=mem_output+ aorder;

	for(i=0;i<inlen;i++)
	{
		y[i]=y[i-D[i]]*g + x[i-D[i]];
	}

	for(i=0;i<inlen;i++)output[i]=stoshort(y[i]*a+input[i]*b);
}
void EchoRun_API(Echo_s *mEcho,short *input,int inlen,	short *output)
{
	short x16L[SKR_MAX_FRAME_SAMPLE_MONO];
	short x16R[SKR_MAX_FRAME_SAMPLE_MONO];
	int d[SKR_MAX_FRAME_SAMPLE_MONO];
	int inRlen;
	int i,j;

	if (mEcho->channel == 1)
	{
		//SKRiir_xDyD(output, input, inlen,mEcho->memoutputL, mEcho->meminputL, mEcho->bD, mEcho->aD, mEcho->Echo_D);just the same

		if (mEcho->Echo_depth_ms == 0)
		{
			//assert(mEcho->Echo_b == 1);//we just run echo mod
			SKRiir_xDyD_2(output, input, inlen,mEcho->memoutputL, mEcho->meminputL, mEcho->Echo_a, mEcho->Echo_g, mEcho->Echo_D);
		} 

		

		
	} 
	else if(mEcho->channel == 2)
	{
		inRlen = inlen/2;

		ChanelConvert(1,2,inlen,input,NULL,x16L,x16R);
		if (mEcho->Echo_depth_ms == 0)
		{
			//assert(mEcho->Echo_b == 1);//we just run echo mod
			SKRiir_xDyD_2(x16L, x16L, inRlen,mEcho->memoutputL, mEcho->meminputL, mEcho->Echo_a, mEcho->Echo_g, mEcho->Echo_D);
			SKRiir_xDyD_2(x16R, x16R, inRlen,mEcho->memoutputR, mEcho->meminputR, mEcho->Echo_a, mEcho->Echo_g, mEcho->Echo_D);
		} 
		/*SKRiir_xDyD(x16L, x16L, inRlen,mEcho->memoutputL, mEcho->meminputL, mEcho->bD, mEcho->aD, mEcho->Echo_D);
		SKRiir_xDyD(x16R, x16R, inRlen,mEcho->memoutputR, mEcho->meminputR, mEcho->bD, mEcho->aD, mEcho->Echo_D);*/
		ChanelConvert(2,1,inRlen,x16L,x16R,output,NULL);
	}
	else
	{
		assert(0);
	}
}

