#include "NatureMix_control.h"
#include "../../amplitude/Dynamic_LIB/Dynamic_SDK_API.h"
#include "../../amplitude/Level_LIB/Level_SDK_API.h"
#include "../../common/functions.h"
#include "SKR_NatureMix_functions.h"
#include <stdlib.h>
#include <assert.h>

#define MAXWEIGHT (80.0)
//#define SEELEVEL

#ifdef SEELEVEL
#include <stdio.h>
#endif

void NatureMixReset_API(NatureMix_ID *mNatureMix)
{
	LevelReset_API(&mNatureMix->LevelForNatureMix);
	DynamicReset_API(&mNatureMix->DyForNatureMix);
	mNatureMix->memfstframe = 1;
}

void NatureMixCalcu_API(NatureMix_ID *mNatureMix)
{
	mNatureMix->DyForNatureMix.Chanel = mNatureMix->chanel;
	mNatureMix->DyForNatureMix.samplerate = mNatureMix->samplerate;
	mNatureMix->LevelForNatureMix.channel = mNatureMix->chanel;
	mNatureMix->LevelForNatureMix.samplerate = mNatureMix->samplerate;
	Options_for_TRAE_NatureMix(mNatureMix);
	DynamicCalcu_API(&mNatureMix->DyForNatureMix);
	LevelCalcu_API(&mNatureMix->LevelForNatureMix);
}
void NatureMixCalcu_API_ForVMic(NatureMix_ID *mNatureMix)
{
	mNatureMix->DyForNatureMix.Chanel = mNatureMix->chanel;
	mNatureMix->DyForNatureMix.samplerate = mNatureMix->samplerate;
	mNatureMix->LevelForNatureMix.channel = mNatureMix->chanel;
	mNatureMix->LevelForNatureMix.samplerate = mNatureMix->samplerate;
	Options_for_TRAE_NatureMix_ForVMic(mNatureMix);
	DynamicCalcu_API(&mNatureMix->DyForNatureMix);
	LevelCalcu_API(&mNatureMix->LevelForNatureMix);
}

void NatureMixRun_API(NatureMix_ID *mNatureMix,	short *input[],int inLen,short *output, float *sidechain)
{
	int i,j;
	float x_db[SKR_MAX_FRAME_SAMPLE_MONO];
	int x[SKR_MAX_FRAME_SAMPLE_STEREO];
	float tmpx[SKR_MAX_FRAME_SAMPLE_STEREO] = {0};
	int inRlen;
	float weightfade[SKR_MAX_FRAME_SAMPLE_MONO];

#ifdef SEELEVEL
	FILE *fwx_db;
	short *xf32_db;
	fwx_db=fopen("xf32_db.pcm", "ab");
#endif
	for(i = 0;i<mNatureMix->mixnum;i++)
	{
		assert(fabs(mNatureMix->weight[i]) <=MAXWEIGHT);
	}
	if (mNatureMix->memfstframe ==1)
	{
		mNatureMix->memfstframe = 0;
		for (i = 0; i < mNatureMix->mixnum; i++)
		{
			mNatureMix->memlastframeweight[i] = mNatureMix->weight[i];
		}
	}

	assert(mNatureMix->comlim == 0 || mNatureMix->comlim == 1 || mNatureMix->comlim == 2 || mNatureMix->comlim == 3);

	if (mNatureMix->mixnum == 1 && fabs(mNatureMix->weight[0]) <= 1.0 && mNatureMix->comlim == 0)
	{
		if (mNatureMix->weight[0] == 1.0)
		{
			for(j=0;j<inLen;j++)
			{
				output[j] = input[0][j];	
			}
		}
		else
		{
			for(j=0;j<inLen;j++)
			{
				output[j] = (short)(input[0][j]*mNatureMix->weight[0]);	
			}
		}
	
		return;
	} 

	if (mNatureMix->DyForNatureMix.Chanel == 2)
	{
		inRlen = inLen/2;
	}
	else if(mNatureMix->DyForNatureMix.Chanel == 1)
	{
		inRlen = inLen;
	}
	else
	{
		assert(0);
	}
	
	

		
	for(i = 0;i<mNatureMix->mixnum;i++)
	{
		if (mNatureMix->memlastframeweight[i] == mNatureMix->weight[i])
		{
			for (j = 0; j < inLen; j++)
			{
				tmpx[j] += input[i][j] * mNatureMix->weight[i];//we swap these two for(),and add every sample to a float tmp variable then to int...
			}
		} 
		else
		{
			for (j = 0; j < inLen; j++)
			{
				tmpx[j] += input[i][j] * (mNatureMix->memlastframeweight[i] + j*(mNatureMix->weight[i] - mNatureMix->memlastframeweight[i])/(inLen-1));//实际上应该分左右声道然后inRlen来fade
			}
			mNatureMix->memlastframeweight[i] = mNatureMix->weight[i];
		}

	}





	for(j=0;j<inLen;j++)
	{
		x[j] = (int)(tmpx[j]);
	}

	if (mNatureMix->DADD != 1)
	{
		if (mNatureMix->sidechain == -1)
		{
			LevelRun_API_intin(&mNatureMix->LevelForNatureMix,x,inLen,sidechain);

#ifdef SEELEVEL
			xf32_db = (short *)malloc(inRlen*sizeof(short));
			for (i = 0;i<inRlen;i++)
			{
				xf32_db[i] = (short)(sidechain[i]*500);
			}
			fwrite(xf32_db, sizeof(short), inRlen, fwx_db);	
			free(xf32_db);
#endif
			DynamicRun_API_intinsout(&mNatureMix->DyForNatureMix,x,sidechain,inLen,output,NULL);
		} 
		else
		{
			LevelRun_API_intin(&mNatureMix->LevelForNatureMix,x,inLen,x_db);

#ifdef SEELEVEL
			xf32_db = (short *)malloc(inRlen*sizeof(short));
			for (i = 0;i<inRlen;i++)
			{
				xf32_db[i] = (short)(x_db[i]*500);
			}
			fwrite(xf32_db, sizeof(short), inRlen, fwx_db);	
			free(xf32_db);
#endif
			DynamicRun_API_intinsout(&mNatureMix->DyForNatureMix,x,x_db,inLen,output,sidechain);
		}
		

	}
	else
	{
		for(i=0;i<inLen;i++)
		{
			output[i] = stoshort((float)x[i]);
		}
	}

}


void NatureMixRun_API_FIXWeight(NatureMix_ID *mNatureMix, float fweight[], short *input[],int inLen,short *output, float *sidechain)
{
	int i,j;
	float x_db[SKR_MAX_FRAME_SAMPLE_MONO];
	int x[SKR_MAX_FRAME_SAMPLE_STEREO];
	float tmpx[SKR_MAX_FRAME_SAMPLE_STEREO];
	int inRlen;


#ifdef SEELEVEL
	FILE *fwx_db;
	short *xf32_db;
	fwx_db=fopen("xf32_db.pcm", "ab");
#endif
	for(i = 0;i<mNatureMix->mixnum;i++)
	{
		assert(fabs(fweight[i]) <=MAXWEIGHT);
	}

	assert(mNatureMix->comlim == 0 || mNatureMix->comlim == 1 || mNatureMix->comlim == 2 || mNatureMix->comlim == 3);

	if (mNatureMix->mixnum == 1 && fabs(fweight[0]) <= 1.0 && mNatureMix->comlim == 0)
	{
		if (fweight[0] == 1.0)
		{
			for(j=0;j<inLen;j++)
			{
				output[j] = input[0][j];	
			}
		}
		else
		{
			for(j=0;j<inLen;j++)
			{
				output[j] = (short)(input[0][j]*fweight[0]);	
			}
		}
	
		return;
	} 

	if (mNatureMix->DyForNatureMix.Chanel == 2)
	{
		inRlen = inLen/2;
	}
	else if(mNatureMix->DyForNatureMix.Chanel == 1)
	{
		inRlen = inLen;
	}
	else
	{
		assert(0);
	}
	
	for(j=0;j<inLen;j++)
	{
		tmpx[j] = input[0][j]*fweight[0];	
	}		
	for(i = 1;i<mNatureMix->mixnum;i++)
	{
		for(j=0;j<inLen;j++)
		{
			tmpx[j] += input[i][j]*fweight[i];//we swap these two for(),and add every sample to a float tmp variable then to int...
		}
	}
	for(j=0;j<inLen;j++)
	{
		x[j] = (int)(tmpx[j]);
	}

	if (mNatureMix->DADD != 1)
	{
		if (mNatureMix->sidechain == -1)
		{
			LevelRun_API_intin(&mNatureMix->LevelForNatureMix,x,inLen,sidechain);

#ifdef SEELEVEL
			xf32_db = (short *)malloc(inRlen*sizeof(short));
			for (i = 0;i<inRlen;i++)
			{
				xf32_db[i] = (short)(sidechain[i]*500);
			}
			fwrite(xf32_db, sizeof(short), inRlen, fwx_db);	
			free(xf32_db);
#endif
			DynamicRun_API_intinsout(&mNatureMix->DyForNatureMix,x,sidechain,inLen,output,NULL);
		} 
		else
		{
			LevelRun_API_intin(&mNatureMix->LevelForNatureMix,x,inLen,x_db);

#ifdef SEELEVEL
			xf32_db = (short *)malloc(inRlen*sizeof(short));
			for (i = 0;i<inRlen;i++)
			{
				xf32_db[i] = (short)(x_db[i]*500);
			}
			fwrite(xf32_db, sizeof(short), inRlen, fwx_db);	
			free(xf32_db);
#endif
			DynamicRun_API_intinsout(&mNatureMix->DyForNatureMix,x,x_db,inLen,output,sidechain);
		}
		

	}
	else
	{
		for(i=0;i<inLen;i++)
		{
			output[i] = stoshort((float)x[i]);
		}
	}
}

void ComlimRun_API(NatureMix_ID *mNatureMix, float *input,int inLen,short *output)
{
	int j;
	float x_db[SKR_MAX_FRAME_SAMPLE_MONO];
	int x[SKR_MAX_FRAME_SAMPLE_STEREO];
	int inRlen;

    
	if (mNatureMix->DyForNatureMix.Chanel == 2)
	{
		inRlen = inLen/2;
	}
	else if(mNatureMix->DyForNatureMix.Chanel == 1)
	{
		inRlen = inLen;
	}
	else
	{
		assert(0);
	}
	

	for(j=0;j<inLen;j++)
	{
		x[j] = (int)(input[j]);
	}
    

    LevelRun_API_intin(&mNatureMix->LevelForNatureMix,x,inLen,x_db);
            
    DynamicRun_API_intinsout(&mNatureMix->DyForNatureMix,x,x_db,inLen,output,NULL);


    
}
void ComlimRun_API_intIshortO(NatureMix_ID *mNatureMix, int *input,int inLen,short *output)
{
	int j;
	float x_db[SKR_MAX_FRAME_SAMPLE_MONO];
	int inRlen;

	if (mNatureMix->DyForNatureMix.Chanel == 2)
	{
		inRlen = inLen/2;
	}
	else if(mNatureMix->DyForNatureMix.Chanel == 1)
	{
		inRlen = inLen;
	}
	else
	{
		assert(0);
	}

	LevelRun_API_intin(&mNatureMix->LevelForNatureMix,input,inLen,x_db);

	DynamicRun_API_intinsout(&mNatureMix->DyForNatureMix,input,x_db,inLen,output,NULL);



}

