/*TIA/EIA/IS-96-C*/

#include <stdlib.h>
#include "DCC_control.h"
#include "../../common/functions.h"
#include <assert.h>

//#define SEEDC //only for mono

#ifdef SEEDC
#include <stdio.h>
#endif

void DCCReset_API(DCC_ID *mDCC)
{
	mDCC->memLaverge[0] = 0.0;
	mDCC->memRaverge[0] = 0.0;
}

void DCCCalcu_API(DCC_ID *mDCC)
{
	mDCC->winlen = (int)(mDCC->samplerate * 0.02);
}

void DCCRun_API(DCC_ID *mDCC,short *input,int inLen,short *output)
{
	int i;
#ifdef SEEDC
	int j, inRlen;
#endif
	float sum;
	float averge;
	float sum2;
	float averge2;

#ifdef SEEDC
	FILE *fwavg, *frfilavg;
	
	fwavg = fopen("avg.pcm", "ab");
	frfilavg = fopen("filavg.pcm", "ab");
#endif

	if (mDCC->Chanel == 1)
	{
		for (sum=0,i=0;i<inLen;i++)
		{
			sum += input[i];
		}
		averge = sum / inLen;
		mDCC->memLaverge[0] = 0.15*averge + 0.85*mDCC->memLaverge[0];
		for (i=0;i<inLen;i++)
		{
			output[i] = stoshort(input[i] - mDCC->memLaverge[0]);
		}
#ifdef SEEDC
		for (i=0;i<inLen;i++)
		{
			input[i] = stoshort(averge);
		}
		fwrite(input, sizeof(short), inLen, fwavg);
		for (i=0;i<inLen;i++)
		{
			input[i] = stoshort(mDCC->memLaverge[0]);
		}
		fwrite(input, sizeof(short), inLen, frfilavg);
		fclose(fwavg);
		fclose(frfilavg);
#endif
	} 
	else if (mDCC->Chanel == 2)
	{
		for (sum=sum2=0.0,i=0;i<inLen;i+=2)
		{
			sum += input[i];
			sum2 += input[i+1];
		}
		averge = sum / (inLen /2);
		averge2 = sum2 / (inLen /2);

		mDCC->memLaverge[0] = 0.15*averge + 0.85*mDCC->memLaverge[0];
		mDCC->memRaverge[0] = 0.15*averge2 + 0.85*mDCC->memRaverge[0];

		for (i=0;i<inLen;i+=2)
		{
			output[i] = stoshort(input[i] - mDCC->memLaverge[0]);
			output[i+1] = stoshort(input[i+1] - mDCC->memRaverge[0]);
		}	
	}
	else
	{
		assert(0);
	}

}