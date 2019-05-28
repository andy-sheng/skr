#include "Resample_control.h"
#include "SKR_Resample_functions.h"
#include "SKR_Resample_defines.h"
#include <assert.h>
#include <stdlib.h>


void ResampleReset_API(Resample_ID *mResample)
{
	int i;
	/*for (i=0;i<RESAMPLE_MAXORDER;i++)
	{
		mResample->memfilterin[i]=0;
		mResample->memfilterout[i]=0;
	}*/
	mResample->memL.meminput[0] = 0;
	mResample->memL.meminternalinput[0] = 0.0;
	for (i=0;i<MAXNS*(2+1);i++)
	{
		mResample->memL.mempx[i]=0;
		mResample->memL.mempy[i]=0;
	}
	mResample->memL.memDindex = 0;
	mResample->memL.memk = mResample->I - 1;//注意！每次calcu后仍然要reset mem，因为这个索引号是针对上一次D、I的某帧末结果，此号可能超出新D、I索引表长度。但是可以先reset...
	mResample->memL.memLindex = 0;

	mResample->memR.meminput[0] = 0;
	mResample->memR.meminternalinput[0] = 0.0;
	for (i=0;i<MAXNS*(2+1);i++)
	{
		mResample->memR.mempx[i]=0;
		mResample->memR.mempy[i]=0;
	}
	mResample->memR.memDindex = 0;
	mResample->memR.memk = mResample->I - 1;
	mResample->memR.memLindex = 0;
}


void ResampleCalcu_API(Resample_ID *mResample)
{
	int gcd;
	int i;
	float internalfL;

	if (mResample->insamplerate == mResample->outsamplerate)
	{
		return;
	}
	
	//计算，D，I
	gcd = Gcd(mResample->insamplerate,mResample->outsamplerate);
	mResample->I = mResample->outsamplerate/gcd;
	mResample->D = mResample->insamplerate/gcd;

	if (mResample->I>RESAMPLE_I_MAX||mResample->D>RESAMPLE_D_MAX||mResample->I==mResample->D||mResample->filterOrder>2*MAXNS)
	{
//		assert(0);
	}
	//根据D,I算

	
	switch(mResample->LinearOption)//为0时不许线性内插，为1时允许线性内插，为2以后便采用线性内插，为3时不滤镜像，为4时不去混叠，为5时不做任何滤波
	{
	case 0:
		if (1 == mResample->D)
		{	
			mResample->resample = I_Shannon;
		}
		else if (1 == mResample->I)
		{	
			if (mResample->DDSampling == 1)
			{
				mResample->resample = DD_Resample;
			} 
			else
			{
				mResample->resample = D_Shannon;
			}
		}
		else
		{
			mResample->resample = I_D_Shannon;
		}
		break;
	case 1://暂时不判断启用条件了，允许就用
	case 2:
		if (mResample->D>mResample->I)
		{
			mResample->resample = D_I_Linear;
		} 
		else
		{
			mResample->resample = I_D_Linear;
		}
		break;
	case 3:
		if (mResample->D>mResample->I)
		{
			mResample->resample = D_I_Linear;
		} 
		else
		{
			mResample->resample = D_ID_Linear;
		}
		break;
	case 4:
		if (mResample->D>mResample->I)
		{
			mResample->resample = D_ID_Linear;
		} 
		else
		{
			mResample->resample = I_D_Linear;
		}
		break;
	case 5:
		mResample->resample = D_ID_Linear;
		break;
	}
	if(mResample->resample == D_Shannon||mResample->resample == I_Shannon||mResample->resample == I_D_Shannon)
	{
		//计算滤波系统截止频率
		internalfL/*guiyihua*/ = 1.0/(2*THEMAXOF(mResample->D,mResample->I));
		ChebyII_Lowpassc(mResample->filterOrder,internalfL*TRANSITIONBAND,internalfL,ATTENUATION_DB,mResample->bj,mResample->aj);
		for (i=0;i<2+1;i++)
		{
			mResample->bj[i] *= mResample->I;
		}
	} 
	else
	{
		CalcuLinearParameter(mResample->D,mResample->I,mResample->LinearxIndexDelta,mResample->w1,mResample->w2);
		if (mResample->D>mResample->I)
		{
			internalfL/*guiyihua*/ = 1.0/(2*((float)mResample->D/mResample->I));
		} 
		else
		{
			internalfL/*guiyihua*/ = 1.0/(2*((float)mResample->I/mResample->D));
		}
		ChebyII_Lowpassc(mResample->filterOrder,internalfL*TRANSITIONBAND,internalfL,ATTENUATION_DB,mResample->bj,mResample->aj);
	}
	ResampleReset_API(mResample);
}

void ResampleInit_API(Resample_ID *mResample)
{
	ResampleCalcu_API(mResample);
	ResampleReset_API(mResample);
}

void ResampleRun_API
(Resample_ID *mResample,
 short *input,
 int inLen,
 short *output,
 int *outLen
 )
{
	short *inL;
	short *outL;
	short *inR;
	short *outR;
	int inRlen;
	int i,j;

	if (mResample->insamplerate == mResample->outsamplerate)
	{
		*outLen = inLen;
		if (input != output)
		{
			for (i=0;i<inLen;i++)
			{
				output[i] = input[i];
			}
		} 
		return;
	}
	
	if (mResample->chanel == 1)
	{
		mResample->resample(&mResample->memL,mResample,input,inLen,output,outLen);
	} 
	else if(mResample->chanel == 2)
	{
		inRlen = inLen/2;

		inL = (short *)malloc(sizeof(short)*inRlen);
		inR = (short *)malloc(sizeof(short)*inRlen);
		for (j=0,i=0;j<inRlen;j++,i+=2)
		{
			inL[j] = input[i];
			inR[j] = input[i+1];
		}
		outL = (short *)malloc((int)(sizeof(short)*inRlen*((float)mResample->I/mResample->D + 1)));//+1就应该可以了
		outR = (short *)malloc((int)(sizeof(short)*inRlen*((float)mResample->I/mResample->D + 1)));
	
		mResample->resample(&mResample->memL,mResample,inL,inRlen,outL,outLen);
		mResample->resample(&mResample->memR,mResample,inR,inRlen,outR,outLen);
		
		for (i=0,j=0;i<(*outLen);j+=2,i++)
		{
			output[j] = outL[i];
			output[j+1] = outR[i];
		}
		
		(*outLen) = (*outLen)*2;

		free(outR);
		free(outL);
		
		free(inL);
		free(inR);
	}
	else
	{
		assert(0);
	}
	
}


