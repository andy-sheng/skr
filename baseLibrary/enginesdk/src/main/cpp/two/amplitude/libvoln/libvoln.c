#include "../AGC_LIB/AGC_SDK_API.h"
#include <assert.h>
#include <stdlib.h>
#include "../../common/functions.h"
#include "../../Delay/Delay_LIB/Delay_SDK_API.h"

typedef struct VOLN_Mono_channel_memory{
	AGC_ID mAGC;
	Buf16_s mBuf;
	NatureMix_ID mNatureMix;
	DCC_ID mDCC;
	float memavergex_db_beforevmic;
	int DC;
}VOLN_ID;

int SKR_agc_create(void **mVOLN)
{
	mVOLN[0] = (void *)malloc(sizeof(VOLN_ID));
	if (mVOLN[0])
	{
		return 0;
	} 
	else
	{
		return -1;
	}

}
int SKR_agc_free(void *mVOLN)
{
	if (mVOLN)
	{
		free(mVOLN);
	}
	else
	{
		return -1;
	}
	return 0;
}

void SKR_agc_reset(void *mVOLN)
{
	VOLN_ID *pVOLN = (VOLN_ID *)mVOLN;

	DCCReset_API(&pVOLN->mDCC);
	AGCReset_API(&pVOLN->mAGC);
	BufresetAPI(&pVOLN->mBuf);
	pVOLN->mBuf.front = pVOLN->mBuf.rear = 0;
	NatureMixReset_API(&pVOLN->mNatureMix);
	pVOLN->memavergex_db_beforevmic = -90.0f;
	pVOLN->mNatureMix.weight[0] = 1.0f;

}
int SKR_agc_config(void *mVOLN,int samplerate,int channel,int dykind,float maxgaindB,float fstgaindB,int DC)
{
	AGC_ID *pAGC;
	NatureMix_ID *pNatureMix;
	DCC_ID *pDCC;


	if (channel !=1 && channel !=2)
	{
		return -2;
	}
	if (dykind < 0)
	{
		return -3;
	}
	if (maxgaindB>40||maxgaindB<0)
	{
		return -4;
	}
	if (fstgaindB>40||fstgaindB<0)
	{
		return -5;
	}
	if (DC !=0 && DC !=1)
	{
		return -6;
	}

	pAGC = &(((VOLN_ID *)mVOLN)->mAGC);
	pNatureMix = &(((VOLN_ID *)mVOLN)->mNatureMix);
	pDCC = &(((VOLN_ID *)mVOLN)->mDCC);

	((VOLN_ID *)mVOLN)->DC = DC;

	pAGC->chanel = channel;
	pAGC->samplerate = samplerate;
	pAGC->DyKind = dykind;
	pAGC->FeedbackKind = NORMIC_NOTUSEVBOOST;
	pAGC->SimpVADBypass = 1;
	pAGC->FBSimplex = 0;
	pAGC->PostAGC = 0;
	pAGC->boostinfo[0] = -1.0;
	pAGC->boostinfo[1] = -1.0;
	pAGC->boostinfo[2] = -1.0;
	pAGC->boostinfo[3] = -1.0;
	pAGC->postmoddB = 0;
	pAGC->vvolmaxdB = maxgaindB;
	pAGC->vvolmindB = 0;
	pAGC->vvolfstdB = fstgaindB;
	AGCCalcu_API(pAGC);

	pNatureMix->chanel = channel;
	pNatureMix->samplerate = samplerate;
	pNatureMix->mixnum = 1;
	pNatureMix->sidechain = -1;
	pNatureMix->DADD = 0;
	pNatureMix->comlim = 0;
	NatureMixCalcu_API_ForVMic(pNatureMix);


	pDCC->Chanel = channel;
	pDCC->samplerate = samplerate;
	DCCCalcu_API(pDCC);

	return 0;


}

int SKR_agc_config_int(void *mVOLN, int samplerate, int channel, int dykind, int maxgaindB, int fstgaindB, int DC)
{
	AGC_ID *pAGC;
	NatureMix_ID *pNatureMix;
	DCC_ID *pDCC;

	if (samplerate != 48000 && samplerate != 16000 && samplerate != 8000)
	{
		return -1;
	}
	if (channel != 1 && channel != 2)
	{
		return -2;
	}
	if (dykind < 0)
	{
		return -3;
	}
	if (maxgaindB > 40 || maxgaindB < 0)
	{
		return -4;
	}
	if (fstgaindB > 40 || fstgaindB < 0)
	{
		return -5;
	}
	if (DC != 0 && DC != 1)
	{
		return -6;
	}

	pAGC = &(((VOLN_ID *)mVOLN)->mAGC);
	pNatureMix = &(((VOLN_ID *)mVOLN)->mNatureMix);
	pDCC = &(((VOLN_ID *)mVOLN)->mDCC);

	((VOLN_ID *)mVOLN)->DC = DC;

	pAGC->chanel = channel;
	pAGC->samplerate = samplerate;
	pAGC->DyKind = dykind;
	pAGC->FeedbackKind = NORMIC_NOTUSEVBOOST;
	pAGC->SimpVADBypass = 1;
	pAGC->FBSimplex = 0;
	pAGC->PostAGC = 0;
	pAGC->boostinfo[0] = -1.0;
	pAGC->boostinfo[1] = -1.0;
	pAGC->boostinfo[2] = -1.0;
	pAGC->boostinfo[3] = -1.0;
	pAGC->postmoddB = 0;
	pAGC->vvolmaxdB = maxgaindB;
	pAGC->vvolmindB = 0;
	pAGC->vvolfstdB = fstgaindB;
	AGCCalcu_API(pAGC);

	pNatureMix->chanel = channel;
	pNatureMix->samplerate = samplerate;
	pNatureMix->mixnum = 1;
	pNatureMix->sidechain = -1;
	pNatureMix->DADD = 0;
	pNatureMix->comlim = 0;
	NatureMixCalcu_API_ForVMic(pNatureMix);


	pDCC->Chanel = channel;
	pDCC->samplerate = samplerate;
	DCCCalcu_API(pDCC);

	return 0;


}
void VOLN_VMICRun_API(void *mVOLN, short *input, int inLen, short *output)
{
	short *x_p[1];
	NatureMix_ID *pNatureMix;
	DCC_ID *pDCC;
	VOLN_ID *pVOLN;
	int i;
	float x_db_beforevmic[SKR_MAX_FRAME_SAMPLE_MONO];
	float avergex_db_beforevmic;

	float fgaininfo[8];
	int boost[8];


	pNatureMix = &(((VOLN_ID *)mVOLN)->mNatureMix);
	pDCC = &(((VOLN_ID *)mVOLN)->mDCC);
	pVOLN = (VOLN_ID *)mVOLN;

	if (pVOLN->DC == 1)
	{
		DCCRun_API(pDCC,input,inLen,output);
	} 
	else
	{
		if (input!=output)
		{
			for (i = 0;i<inLen;i++)
			{
				output[i] = input[i];
			}
		}
	}
	
	//maybe there is a copy that is not necessary

	x_p[0] = output;
	//pNatureMix->weight[0] = stgain/65535.0;
	NatureMixRun_API(pNatureMix,x_p,inLen,output,x_db_beforevmic);
	if (pNatureMix->DADD == 0 && pNatureMix->weight[0]>1.0 && pNatureMix->sidechain == -1)
	{
		avergex_db_beforevmic = 0.0;
		for (i=0;i<inLen;i++)
		{
			avergex_db_beforevmic += x_db_beforevmic[i];
		}
		pVOLN->memavergex_db_beforevmic = avergex_db_beforevmic/inLen;
	} 
	else
	{
		pVOLN->memavergex_db_beforevmic = -90.0;
	}


}
void VOLN_AGCRun_API(void *mVOLN, short *input, int inLen, short *output)
{
	float fgaininfo[8];
	int boost[8];
	AGC_ID *pAGC;
	VOLN_ID *pVOLN;
	short inputreframe[SKR_MAX_FRAME_SAMPLE_STEREO];
	int tmpoutlen;
	int framelen20ms;

	pAGC = &(((VOLN_ID *)mVOLN)->mAGC);
	pVOLN = (VOLN_ID *)mVOLN;

	boost[4] = 0;//单讲信息
	fgaininfo[5] = pVOLN->memavergex_db_beforevmic;//ec处理前音量信息，无信息填极小值,此处是vmic前的音量信息
	fgaininfo[6] = 12345;

	framelen20ms = pVOLN->mAGC.samplerate*0.02*pVOLN->mAGC.chanel;
	if (inLen != framelen20ms)
	{
		assert(pAGC->DyKind == 0);
		putinAPI_loop(&pVOLN->mBuf, input, inLen);
		do
		{
			tmpoutlen = putoutAPI_ForReframe_loop(&pVOLN->mBuf, inputreframe, framelen20ms);

			if (tmpoutlen)
			{
				AGCRun_API(pAGC, inputreframe, framelen20ms, inputreframe, fgaininfo, boost);
				pVOLN->mNatureMix.weight[0] = boost[0] / 65535.0;
			}
		} while (tmpoutlen != 0);
	}
	else
	{
		AGCRun_API(pAGC, input, inLen, output, fgaininfo, boost);
		pVOLN->mNatureMix.weight[0] = boost[0] / 65535.0;
	}

	

	//fwrite_findordef(NULL, 2, inLen / pAGC->chanel, "gain", pVOLN->mNatureMix.weight[0] * 1000, 1);

}

int SKR_agc_proc(void *mVOLN, short *input, int inLen, short *output)
{
	VOLN_VMICRun_API(mVOLN, input, inLen, output);
	VOLN_AGCRun_API(mVOLN, output, inLen, output);

	return 0;
}