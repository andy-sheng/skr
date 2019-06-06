#include <stdlib.h>
#include "Resample_control.h"
#include "SKR_Resample_functions.h"

void DD_Resample
(Resample_MEM *mem,
 Resample_ID *mResample,
 short *input,
 int inLen,
 short *output,
 int *outLen
 )
{
	int i=0;

	for (;mem->memDindex < inLen;mem->memDindex += mResample->D,i++)
	{
		output[i] = input[mem->memDindex];
	}
	mem->memDindex =  mem->memDindex - inLen;

	*outLen = i;

}

void I_D_Shannon
(Resample_MEM *mem,
 Resample_ID *mResample,
 short *input,
 int inLen,
 short *output,
 int *outLen
 )
{
	int i;
	float *x;
	short *x16;
	
	x = (float*)malloc((inLen*mResample->I)*sizeof(float));
	x16 = (short*)malloc((inLen*mResample->I)*sizeof(short));
	
	//插零
	for (i=0;i<inLen*mResample->I;i++)
	{
		x[i]=0;
	}
	for (i=0;i<inLen;i++)
	{	
		x[i*mResample->I]=input[i];
	}
	
	//滤波
	filterc(mResample->bj,mResample->aj,2,mResample->filterOrder/2,x,inLen*mResample->I,mem->mempx,mem->mempy);
	for(i=0;i<inLen*mResample->I;i++)
	{
		x16[i]= stoshort(x[i]);
	}
	
	//抽取
	DD_Resample(mem,mResample,x16,inLen*mResample->I,output,outLen);
	
	free(x);
	free(x16);
}

void I_Shannon
(Resample_MEM *mem,
 Resample_ID *mResample,
 short *input,
  int inLen,
 short *output,
 int *outLen
 )
{
	int i;
	float *x;
		
	x = (float*)malloc((inLen*mResample->I)*sizeof(float));
	
	//插零
	for (i=0;i<inLen*mResample->I;i++)
	{
		x[i]=0;
	}
	/*for (i=0;i<inLen*mResample->I;i++)
	{	
		internalinput[i]=input[i/mResample->I];
	}*平顶PAM*/
	
	for (i=0;i<inLen;i++)
	{	
		x[i*mResample->I]=(float)input[i];
	}

	//滤波
	filterc(mResample->bj,mResample->aj,2,mResample->filterOrder/2,x,inLen*mResample->I,mem->mempx,mem->mempy);
	for (i=0;i<inLen*mResample->I;i++)
	{
		output[i] = stoshort(x[i]);
	}
	*outLen = inLen*mResample->I;

	free(x);
}

void D_Shannon
(Resample_MEM *mem,
 Resample_ID *mResample,
 short *input,
 int inLen,
 short *output,
 int *outLen
 )
{
	int i;
	float *x;
	short *x16;

	x16 = (short*)malloc((inLen)*sizeof(short));
	x = (float*)malloc((inLen)*sizeof(float));
	
		
	for(i=0;i<inLen;i++)
	{
		x[i]= (float)input[i];
	}
	//滤波
		
	filterc(mResample->bj,mResample->aj,2,mResample->filterOrder/2,x,inLen,mem->mempx,mem->mempy);
	for(i=0;i<inLen;i++)
	{
		x16[i]= stoshort(x[i]);
	}

	//抽取
	DD_Resample(mem,mResample,x16,inLen,output,outLen);

	free(x);
	free(x16);
	
}

