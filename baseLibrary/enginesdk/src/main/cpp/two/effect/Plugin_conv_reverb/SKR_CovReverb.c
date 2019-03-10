#include <stdlib.h>
#include <assert.h>
#include <math.h>
#include <stdio.h>
#include "CovReverb_control.h"
#include "common/functions.h"
#include "common/defines.h"
#include "smallhamon.h"
#include "jucam.h"
#include "lowshower.h"
#include "shower.h"
#include "smokeybar.h"
#include "thetrackingroom.h"
#include "cd.h"
#include "tandc.h"

void CovReverbReset_API(CovReverb_s *mReverb)
{	
	FilterlongfirReset_API(&mReverb->mfir);
	FilterlongfirReset_API(&mReverb->mfir2);
}
void CovReverbCalcu_API(CovReverb_s *mReverb)
{
	const short *preverbhL;
	const short *preverbhR;
	const short *preverbhLR;
	const short *preverbhRL;
	short tmpl[6144];///////should have const float *preverbhL...
	short tmpr[6144];
	int reverbLlen;
	int reverbRlen;
	int reverbLlen2;
	int reverbRlen2;
	float implusegain_1;
	int icov;
	const short *icovxL;
	const short *icovxR;
	float *icovoutL;
	float *icovoutR;
	int icovxlen;
	int have;
	int i,j;
	short *rsh;
	int rsoutlen;
	float difgain;


	icovoutL = (float *)malloc(sizeof(float)*48000);
	icovoutR = (float *)malloc(sizeof(float)*48000);
	rsh = (short *)malloc(sizeof(short)*96000);

	icov = 0;
	have = 0;
	switch(mReverb->reverbkind)//these IRs are all at 48khz
	{
	case TANDC:
		preverbhL = g_tritandcloseC48k;
		preverbhR = g_tritandcloseC48k;
		reverbLlen = 13000;//9000..
		reverbRlen = 13000;//9000
		have = 1;
		implusegain_1 = 0.0001;
		break;
	case CD:
		preverbhL = g_cdC48k;
		preverbhR = g_cdC48k;
		reverbLlen = 32000;//9000..
		reverbRlen = 32000;//9000
		have = 1;
		implusegain_1 = 0.0001;
		break;
	case SMALLLHAMON:
		preverbhL = g_smallhamon_mp48L;
		preverbhR = g_smallhamon_mp48R;
		reverbLlen = 27000;//9000..
		reverbRlen = 27000;//9000
		have = 1;
		implusegain_1 = 0.0001;
		break;
	case JUCAM:
		preverbhL = g_jucam48L;
		preverbhR = g_jucam48R;
		reverbLlen = 14000;
		reverbRlen = 14000;
		have = 1;
		implusegain_1 = 0.00004;
		break;
	case SMOKEYBAR:
		preverbhL = g_smokeybar48L;
		preverbhR = g_smokeybar48R;
		reverbLlen = 4000;
		reverbRlen = 4000;
		have = 1;
		implusegain_1 = 0.00004;
		break;
	case SHOWER:
		preverbhL = g_shower48LL;
		preverbhR = g_shower48RR;
		preverbhRL = g_shower48RL;
		preverbhLR = g_shower48LR;
		reverbLlen = 29000;
		reverbRlen = 29000;
		reverbLlen2 = 29000;
		reverbRlen2 = 29000;
		have = 2;
		implusegain_1 = 0.0001;
		break;
	case LOWSHOWER:
		preverbhL = g_lowshower48LL;
		preverbhR = g_lowshower48RR;
		preverbhRL = g_lowshower48RL;
		preverbhLR = g_lowshower48LR;
		reverbLlen = 12400;
		reverbRlen = 12400;
		reverbLlen2 = 12400;
		reverbRlen2 = 12400;
		have = 2;
		implusegain_1 = 0.0001;
		break;
	
	case THETRACKINGROOM:
		preverbhL = g_thetrackingroomL;
		preverbhR = g_thetrackingroomR;
		reverbLlen = 13800;
		reverbRlen = 13800;
		have = 1;
		implusegain_1 = 0.0001;
		break;

	default:
		//assert(0);
		have = 0;
		break;
	}
	if (icov == 1)
	{
		mReverb->iirf.filtertype = 0;

		FilterfReset_API(&mReverb->iirf);
		for (i=0;i<icovxlen;i++)
		{
			mReverb->iirf.a[i] = icovxL[i]/32767.0;
		}		

		mReverb->iirf.b[0] = 1.0/mReverb->iirf.a[0];
		mReverb->iirf.blen = 1;

		for (i=0;i<icovxlen;i++)
		{
			mReverb->iirf.a[i] /= mReverb->iirf.a[0];
		}
		mReverb->iirf.alen = icovxlen;
		for (i=0;i<reverbLlen;i+=100)
		{
			FilterfRun_API_shortin(&mReverb->iirf,preverbhL+i,100,icovoutL+i);
		}

		FilterfReset_API(&mReverb->iirf);
		for (i=0;i<icovxlen;i++)
		{
			mReverb->iirf.a[i] = icovxR[i]/32767.0;
		}		

		mReverb->iirf.b[0] = 1.0/mReverb->iirf.a[0];
		mReverb->iirf.blen = 1;

		for (i=0;i<icovxlen;i++)
		{
			mReverb->iirf.a[i] /= mReverb->iirf.a[0];
		}
		mReverb->iirf.alen = icovxlen;
		for (i=0;i<reverbLlen;i+=100)
		{
			FilterfRun_API_shortin(&mReverb->iirf,preverbhR+i,100,icovoutR+i);
		}
	}

	if (have == 1)
	{
		if (icov == 1)
		{
			for (i=0;i<reverbLlen-icovxlen+1;i++)
			{
				mReverb->mfir.b[i]=icovoutL[i]*implusegain_1;
			}
			for (i=0;i<reverbRlen-icovxlen+1;i++)
			{
				mReverb->mfir.bR[i]=icovoutR[i]*implusegain_1;
			}
			mReverb->mfir.blen = reverbLlen-icovxlen+1;
			mReverb->mfir.bRlen = reverbRlen-icovxlen+1;
		} 
		else
		{
			for (i=0;i<reverbLlen;i++)
			{
				mReverb->mfir.b[i] = preverbhL[i]*implusegain_1;
			}
			for (i=0;i<reverbRlen;i++)
			{
				mReverb->mfir.bR[i] = preverbhR[i]*implusegain_1;
			}
			mReverb->mfir.blen = reverbLlen;
			mReverb->mfir.bRlen = reverbRlen;
		}
		mReverb->have = 1;
	}
	if (have == 2)//now when have==2 no icov
	{
		if (mReverb->xsame == 1 || mReverb->channelin == 1)
		{
			for (i=0;i<reverbLlen;i++)
			{
				mReverb->mfir.b[i]=(preverbhL[i]+preverbhRL[i])*implusegain_1;
			}
			for (i=0;i<reverbRlen;i++)
			{
				mReverb->mfir.bR[i]=(preverbhR[i]+preverbhLR[i])*implusegain_1;
			}
			mReverb->mfir.blen = reverbLlen;
			mReverb->mfir.bRlen = reverbRlen;
			//assert(reverbLlen == reverbRlen2 == reverbLlen2 == reverbRlen);
			mReverb->have = 1;
		} 
		else
		{
			for (i=0;i<reverbLlen;i++)
			{
				mReverb->mfir.b[i]=preverbhL[i]*implusegain_1;
			}
			for (i=0;i<reverbRlen;i++)
			{
				mReverb->mfir.bR[i]=preverbhR[i]*implusegain_1;
			}
			mReverb->mfir.blen = reverbLlen;
			mReverb->mfir.bRlen = reverbRlen;

			for (i=0;i<reverbLlen;i++)
			{
				mReverb->mfir2.b[i]=preverbhRL[i]*implusegain_1;//fir2's is RL
			}
			for (i=0;i<reverbRlen;i++)
			{
				mReverb->mfir2.bR[i]=preverbhLR[i]*implusegain_1;
			}
			mReverb->mfir2.blen = reverbLlen;
			mReverb->mfir2.bRlen = reverbRlen;

			//assert(reverbLlen == reverbRlen2 == reverbLlen2 == reverbRlen);
			mReverb->have = 2;
		}
	}
	//dry and wet
	mReverb->dry = 1 - mReverb->wet;
	if (mReverb->have == 1)
	{
		for (i = 0; i < mReverb->mfir.blen; i++)
		{
			mReverb->mfir.b[i] *= mReverb->wet;
			mReverb->mfir.bR[i] *= mReverb->wet;
		}
		mReverb->mfir.b[0] += mReverb->dry;
		mReverb->mfir.bR[0] += mReverb->dry;
	}

	if (mReverb->have == 2)
	{
		for (i = 0; i < mReverb->mfir.blen; i++)
		{
			mReverb->mfir.b[i] *= mReverb->wet;
			mReverb->mfir.bR[i] *= mReverb->wet;
		}
		if (1)//1 -right 0:may be better
		{
			for (i = 0; i < mReverb->mfir2.blen; i++)
			{
				mReverb->mfir2.b[i] *= mReverb->wet;
				mReverb->mfir2.bR[i] *= mReverb->wet;
			}
		}
		mReverb->mfir.b[0] += mReverb->dry;
		mReverb->mfir.bR[0] += mReverb->dry;
	}
	if (mReverb->samplerate != 48000)
	{
		for (i=0,j=0;i<mReverb->mfir.blen*2;i+=2,j++)
		{
			rsh[i] = stoshort(mReverb->mfir.b[j]*32767);
			rsh[i+1] = stoshort(mReverb->mfir.bR[j]*32767);
		}

		difgain = 48000.0/mReverb->samplerate/32767.0;

		for (i=0,j=0;i<rsoutlen;i+=2,j++)
		{
			mReverb->mfir.b[j] = rsh[i]*difgain;
			mReverb->mfir.bR[j]= rsh[i+1]*difgain;
		}

		mReverb->mfir.blen =mReverb->mfir.bRlen = rsoutlen/2;

		if (mReverb->have == 2)
		{
			for (i=0,j=0;i<mReverb->mfir2.blen*2;i+=2,j++)
			{
				rsh[i] = stoshort(mReverb->mfir2.b[j]*32767);
				rsh[i+1] = stoshort(mReverb->mfir2.bR[j]*32767);
			}

			difgain = 48000.0/mReverb->samplerate/32767.0;

			for (i=0,j=0;i<rsoutlen;i+=2,j++)
			{
				mReverb->mfir2.b[j] = rsh[i]*difgain;
				mReverb->mfir2.bR[j]= rsh[i+1]*difgain;
			}

			mReverb->mfir2.blen =mReverb->mfir2.bRlen = rsoutlen/2;
		}
	}

	if (mReverb->channelin == 1 && mReverb->channelout == 1)//this case have will not be 2
	{
		mReverb->mfir.chanel = 1;
		if (mReverb->mfir.blen>=64)
		{
			mReverb->mfir.filtertype = FIR_FFTCONV;
		} 
		else
		{
			mReverb->mfir.filtertype = FIR_DIRCTCONV;
		}
	} 
	else if(mReverb->channelin == 1 && mReverb->channelout == 2)//this case have will not be 2
	{
		mReverb->mfir.chanel = 1;
		if (mReverb->mfir.blen>=64)
		{
			mReverb->mfir.filtertype = FIR_FFTCONV_HDIFF;
		} 
		else
		{
			mReverb->mfir.filtertype = FIR_DIRCTCONV_HDIFF;
		}
	}
	else if (mReverb->channelin == 2 && mReverb->channelout == 2)
	{
		if (mReverb->xsame == 1)//this case have will not be 2
		{
			mReverb->mfir.chanel = 2;
			if (mReverb->mfir.blen>= 111164)
			{
				mReverb->mfir.filtertype = FIR_FFTCONV;
			} 
			else
			{
				mReverb->mfir.filtertype = FIR_DIRCTCONV;
			}
		} 
		else
		{
			mReverb->mfir.chanel = 2;
			if (mReverb->mfir.blen>=64||mReverb->have == 2)
			{
				mReverb->mfir.filtertype = FIR_FFTCONV;
			} 
			else
			{
				mReverb->mfir.filtertype = FIR_DIRCTCONV;
			}
			if (mReverb->have == 2)
			{
				mReverb->mfir2.chanel = 2;
				mReverb->mfir2.filtertype = FIR_FFTCONV_XFAN;
			}
		}
	}
	else if(mReverb->channelin == 2 && mReverb->channelout == 1)
	{
		assert(0);
	}

	if (mReverb->mfir.filtertype == FIR_FFTCONV||mReverb->mfir.filtertype == FIR_FFTCONV_XFAN||mReverb->mfir.filtertype == FIR_FFTCONV_HDIFF||mReverb->mfir.filtertype == FIR_FFTCONV_XSAME)
	{
		FilterlongfirCalcu_API_forFFT(&mReverb->mfir);
	} 
	if (mReverb->have == 2)
	{
		FilterlongfirCalcu_API_forFFT(&mReverb->mfir2);
	}
	free(icovoutL);
	free(icovoutR);
	free(rsh);
}
void CovReverbRun_API(CovReverb_s *mReverb,short *input,int inlen,	short *output)
{
	short y1[SKR_MAX_FRAME_SAMPLE_STEREO];
	short y2[SKR_MAX_FRAME_SAMPLE_STEREO];
	int i;


	if(mReverb->have == 1)
	{
		FilterlongfirRun_API(&mReverb->mfir,input,inlen,output);
	} 
	else
	{
		FilterlongfirRun_API(&mReverb->mfir,input,inlen,y1);
		FilterlongfirRun_API(&mReverb->mfir2,input,inlen,y2);
		for (i = 0; i < inlen; i++)
		{
			output[i] = stoshort(y1[i] + y2[i]);
		}

	}
}

void CovReverbV2Reset_API(CovReverbV2_s *mReverb)
{	
	FastCovVerylonghReset_API(&mReverb->mfir);
	FastCovVerylonghReset_API(&mReverb->mfir2);
}
void CovReverbV2Calcu_API(CovReverbV2_s *mReverb)
{
	const short *preverbhL;
	const short *preverbhR;
	const short *preverbhLR;
	const short *preverbhRL;
	short tmpl[6144];///////should have const float *preverbhL...
	short tmpr[6144];
	int reverbLlen;
	int reverbRlen;
	int reverbLlen2;
	int reverbRlen2;
	float implusegain_1;
	int icov;
	const short *icovxL;
	const short *icovxR;
	float *icovoutL;
	float *icovoutR;
	int icovxlen;
	int have;
	int i,j;
	short *rsh;
	int rsoutlen;
	float difgain;
	float mfirb[39000];
	float mfirbR[39000];
	float mfir2b[39000];
	float mfir2bR[39000];
	int mfirblen;
	int mfir2blen;


	icovoutL = (float *)malloc(sizeof(float)*48000);
	icovoutR = (float *)malloc(sizeof(float)*48000);
	rsh = (short *)malloc(sizeof(short)*96000);

	icov = 0;
	have = 0;
	switch(mReverb->reverbkind)//these IRs are all at 48khz
	{
	case CD:
		preverbhL = g_cdC441;
		preverbhR = g_cdC441;
		reverbLlen = 32000;//9000..
		reverbRlen = 32000;//9000
		have = 1;
		implusegain_1 = 0.0001;
		break;
	case SMALLLHAMON:
		preverbhL = g_smallhamon_mp48L;
		preverbhR = g_smallhamon_mp48R;
		reverbLlen = 27000;//9000..
		reverbRlen = 27000;//9000
		have = 1;
		implusegain_1 = 0.0001;
		break;
	case JUCAM:
		preverbhL = g_jucam48L;
		preverbhR = g_jucam48R;
		reverbLlen = 14000;
		reverbRlen = 14000;
		have = 1;
		implusegain_1 = 0.00004;
		break;
	case SMOKEYBAR:
		preverbhL = g_smokeybar48L;
		preverbhR = g_smokeybar48R;
		reverbLlen = 4000;
		reverbRlen = 4000;
		have = 1;
		implusegain_1 = 0.00004;
		break;
	case SHOWER:
		preverbhL = g_shower48LL;
		preverbhR = g_shower48RR;
		preverbhRL = g_shower48RL;
		preverbhLR = g_shower48LR;
		reverbLlen = 29000;
		reverbRlen = 29000;
		reverbLlen2 = 29000;
		reverbRlen2 = 29000;
		have = 2;
		implusegain_1 = 0.0001;
		break;
	case LOWSHOWER:
		preverbhL = g_lowshower48LL;
		preverbhR = g_lowshower48RR;
		preverbhRL = g_lowshower48RL;
		preverbhLR = g_lowshower48LR;
		reverbLlen = 12400;
		reverbRlen = 12400;
		reverbLlen2 = 12400;
		reverbRlen2 = 12400;
		have = 2;
		implusegain_1 = 0.0001;
		break;



	case THETRACKINGROOM:
		preverbhL = g_thetrackingroomL;
		preverbhR = g_thetrackingroomR;
		reverbLlen = 13800;
		reverbRlen = 13800;
		have = 1;
		implusegain_1 = 0.0001;
		break;

	default:
		//assert(0);
		have = 0;
		break;
	}
	if (icov == 1)
	{
		mReverb->iirf.filtertype = 0;

		FilterfReset_API(&mReverb->iirf);
		for (i=0;i<icovxlen;i++)
		{
			mReverb->iirf.a[i] = icovxL[i]/32767.0;
		}		

		mReverb->iirf.b[0] = 1.0/mReverb->iirf.a[0];
		mReverb->iirf.blen = 1;

		for (i=0;i<icovxlen;i++)
		{
			mReverb->iirf.a[i] /= mReverb->iirf.a[0];
		}
		mReverb->iirf.alen = icovxlen;
		for (i=0;i<reverbLlen;i+=100)
		{
			FilterfRun_API_shortin(&mReverb->iirf,preverbhL+i,100,icovoutL+i);
		}

		FilterfReset_API(&mReverb->iirf);
		for (i=0;i<icovxlen;i++)
		{
			mReverb->iirf.a[i] = icovxR[i]/32767.0;
		}		

		mReverb->iirf.b[0] = 1.0/mReverb->iirf.a[0];
		mReverb->iirf.blen = 1;

		for (i=0;i<icovxlen;i++)
		{
			mReverb->iirf.a[i] /= mReverb->iirf.a[0];
		}
		mReverb->iirf.alen = icovxlen;
		for (i=0;i<reverbLlen;i+=100)
		{
			FilterfRun_API_shortin(&mReverb->iirf,preverbhR+i,100,icovoutR+i);
		}
	}

	if (have == 1)
	{
		if (icov == 1)
		{
			for (i=0;i<reverbLlen-icovxlen+1;i++)
			{
				mfirb[i]=icovoutL[i]*implusegain_1;
			}
			for (i=0;i<reverbRlen-icovxlen+1;i++)
			{
				mfirbR[i]=icovoutR[i]*implusegain_1;
			}
			mfirblen = reverbLlen-icovxlen+1;
		} 
		else
		{
			for (i=0;i<reverbLlen;i++)
			{
				mfirb[i] = preverbhL[i]*implusegain_1;
			}
			for (i=0;i<reverbRlen;i++)
			{
				mfirbR[i] = preverbhR[i]*implusegain_1;
			}
			mfirblen = reverbLlen;
		}
		mReverb->have = 1;
	}
	if (have == 2)//now when have==2 no icov
	{
		if (mReverb->xsame == 1 || mReverb->channelin == 1)
		{
			for (i=0;i<reverbLlen;i++)
			{
				mfirb[i]=(preverbhL[i]+preverbhRL[i])*implusegain_1;
			}
			for (i=0;i<reverbRlen;i++)
			{
				mfirbR[i]=(preverbhR[i]+preverbhLR[i])*implusegain_1;
			}
			mfirblen = reverbLlen;
			//assert(reverbLlen == reverbRlen2 == reverbLlen2 == reverbRlen);
			mReverb->have = 1;
		} 
		else
		{
			for (i=0;i<reverbLlen;i++)
			{
				mfirb[i]=preverbhL[i]*implusegain_1;
			}
			for (i=0;i<reverbRlen;i++)
			{
				mfirbR[i]=preverbhR[i]*implusegain_1;
			}
			mfirblen = reverbLlen;

			for (i=0;i<reverbLlen;i++)
			{
				mfir2b[i]=preverbhRL[i]*implusegain_1;//fir2's is RL
			}
			for (i=0;i<reverbRlen;i++)
			{
				mfir2bR[i]=preverbhLR[i]*implusegain_1;
			}
			mfir2blen = reverbLlen;


			//assert(reverbLlen == reverbRlen2 == reverbLlen2 == reverbRlen);
			mReverb->have = 2;
		}
	}
	mReverb->dry = 1 - mReverb->wet;
	if (mReverb->have == 1)
	{
		for (i = 0; i < mfirblen; i++)
		{
			mfirb[i] *= mReverb->wet;
			mfirbR[i] *= mReverb->wet;
		}
		mfirb[0] += mReverb->dry;
		mfirbR[0] += mReverb->dry;
	}

	if (mReverb->have == 2)
	{
		for (i = 0; i < mfirblen; i++)
		{
			mfirb[i] *= mReverb->wet;
			mfirbR[i] *= mReverb->wet;
		}
		if (1)
		{
			for (i = 0; i < mfir2blen; i++)
			{
				mfir2b[i] *= mReverb->wet;
				mfir2bR[i] *= mReverb->wet;
			}
		}
		mfirb[0] += mReverb->dry;
		mfirbR[0] += mReverb->dry;

	}

	if (mReverb->channelin == 1 && mReverb->channelout == 1)//this case have will not be 2
	{
		mReverb->mfir.channel = 1;
		mReverb->mfir.filtertype = FIR_FFTCONV;
	} 
	else if(mReverb->channelin == 1 && mReverb->channelout == 2)//this case have will not be 2
	{
		mReverb->mfir.channel = 1;
		mReverb->mfir.filtertype = FIR_FFTCONV_HDIFF;
	}
	else if (mReverb->channelin == 2 && mReverb->channelout == 2)
	{
		if (mReverb->xsame == 1)//this case have will not be 2
		{
			mReverb->mfir.channel = 2;
			mReverb->mfir.filtertype = FIR_FFTCONV;

		} 
		else
		{
			mReverb->mfir.channel = 2;
			mReverb->mfir.filtertype = FIR_FFTCONV;

			if (mReverb->have == 2)
			{
				mReverb->mfir2.channel = 2;
				mReverb->mfir2.filtertype = FIR_FFTCONV_XFAN;
			}
		}
	}
	else if(mReverb->channelin == 2 && mReverb->channelout == 1)
	{
		assert(0);
	}




	FastCovVerylonghCalcu_API(&mReverb->mfir,mfirb,mfirbR,mfirblen,mReverb->xframelen);

	if (mReverb->have == 2)
	{
		FastCovVerylonghCalcu_API(&mReverb->mfir2,mfir2b,mfir2bR,mfir2blen,mReverb->xframelen);
	}
	free(icovoutL);
	free(icovoutR);
	free(rsh);
}
void CovReverbV2Run_API(CovReverbV2_s *mReverb,short *input,int inlen,	short *output)
{
	short y1[SKR_MAX_FRAME_SAMPLE_STEREO];
	short y2[SKR_MAX_FRAME_SAMPLE_STEREO];
	int i;


	if(mReverb->have == 1)
	{
		FastCovVerylonghRun_API(&mReverb->mfir,input,inlen,output);
	} 
	else
	{
		FastCovVerylonghRun_API(&mReverb->mfir,input,inlen,y1);
		FastCovVerylonghRun_API(&mReverb->mfir2,input,inlen,y2);
		for (i = 0; i < inlen; i++)
		{
			output[i] = stoshort(y1[i] + y2[i]);
		}
	}
}
