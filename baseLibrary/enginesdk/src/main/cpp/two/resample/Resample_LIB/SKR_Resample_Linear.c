#include "Resample_control.h"
#include "SKR_Resample_functions.h"
#include <stdlib.h>

void D_ID_Linear(Resample_MEM *mem,Resample_ID *mResample, short *input, int inLen, short *output, int *outLen )//无滤波
{
	int i;
	int index;

	index = mem->memLindex;

	for (i=0;index<inLen;i++)
	{	
		if (index == 0)
		{
			output[i] = (short)(mem->meminput[0]*mResample->w2[mem->memk]+input[index]*mResample->w1[mem->memk]);
		}
		else
		{
			output[i] = (short)(input[index-1]*mResample->w2[mem->memk]+input[index]*mResample->w1[mem->memk]);
		}
		mem->memk = Loopadd(mem->memk,1,mResample->I);
		index += mResample->LinearxIndexDelta[mem->memk];
		
	}
	mem->memLindex = index - inLen;
	mem->meminput[0] = input[inLen - 1];
	*outLen = i;
}

void I_D_Linear(Resample_MEM *mem,Resample_ID *mResample, short *input, int inLen, short *output, int *outLen )
{
	int i;
	int index;
	float *x;

	x = (float*)malloc((inLen*(int)((float)mResample->I/mResample->D+1))*sizeof(float));
	index = mem->memLindex;

	for (i=0;index<inLen;i++)
	{	
		if (index == 0)
		{
			x[i] = mem->meminput[0]*mResample->w2[mem->memk]+input[index]*mResample->w1[mem->memk];
		}
		else
		{
			x[i] = input[index-1]*mResample->w2[mem->memk]+input[index]*mResample->w1[mem->memk];
		}
		mem->memk = Loopadd(mem->memk,1,mResample->I);
		index += mResample->LinearxIndexDelta[mem->memk];

	}
	mem->memLindex = index - inLen;
	mem->meminput[0] = input[inLen - 1];
	*outLen = i;

	filterc(mResample->bj,mResample->aj,2,mResample->filterOrder/2,x,*outLen,mem->mempx,mem->mempy);
	for (i=0;i<*outLen;i++)
	{
		output[i]=stoshort(x[i]);
	}

	free(x);

}
void D_I_Linear(Resample_MEM *mem,Resample_ID *mResample, short *input, int inLen, short *output, int *outLen )
{
	int i;
	int index;
	float *x;
	float *internalinput;

	x = (float*)malloc((inLen*(int)((float)mResample->I/mResample->D+1))*sizeof(float));
	internalinput = (float*)malloc((inLen)*sizeof(float));

	for(i=0;i<inLen;i++)
	{
		internalinput[i]= (float)input[i];
	}
	filterc(mResample->bj,mResample->aj,2,mResample->filterOrder/2,internalinput,inLen,mem->mempx,mem->mempy);
	
	index = mem->memLindex;
	for (i=0;index<inLen;i++)
	{	
		if (index == 0)
		{
			x[i] = mem->meminternalinput[0]*mResample->w2[mem->memk]+internalinput[index]*mResample->w1[mem->memk];
		}
		else
		{
			x[i] = internalinput[index-1]*mResample->w2[mem->memk]+internalinput[index]*mResample->w1[mem->memk];
		}
		mem->memk = Loopadd(mem->memk,1,mResample->I);
		index += mResample->LinearxIndexDelta[mem->memk];
	}
	mem->memLindex = index - inLen;
	mem->meminternalinput[0] = internalinput[inLen - 1];
	*outLen = i;

	for (i=0;i<*outLen;i++)
	{
		output[i]=stoshort(x[i]);
	}

	free(x);
	free(internalinput);

}