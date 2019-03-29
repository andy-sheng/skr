#include <stdlib.h>
#include "pitch_NCC_control.h"
#include "../../common/functions.h"
#include <assert.h>
#include <math.h>


void pitch_NCCReset_API(pitch_NCC_ID *mpitch_NCC)
{
	int i;

	for (i=0;i<N_FRAMELEN*2;i++)
	{
		mpitch_NCC->membuf[i] = 0;
	}
	FilterReset_API(&mpitch_NCC->mLP);
	FilterReset_API(&mpitch_NCC->mshuzhi);
	mpitch_NCC->memExternalSomein = 1;
	
}

void pitch_NCCCalcu_API(pitch_NCC_ID *mpitch_NCC)
{
	mpitch_NCC->mLP.b[0] = 0.008233;
	mpitch_NCC->mLP.b[1] = -0.004879;
	mpitch_NCC->mLP.b[2] = 0.007632;
	mpitch_NCC->mLP.b[3] = 0.007632;
	mpitch_NCC->mLP.b[4] = -0.004879;
	mpitch_NCC->mLP.b[5] = 0.008233;
	mpitch_NCC->mLP.blen = 6;

	mpitch_NCC->mLP.a[0] = 1.0;
	mpitch_NCC->mLP.a[1] = -3.6868;
	mpitch_NCC->mLP.a[2] = 5.8926;
	mpitch_NCC->mLP.a[3] = -5.0085;
	mpitch_NCC->mLP.a[4] = 2.2518;
	mpitch_NCC->mLP.a[5] = -0.4271;
	mpitch_NCC->mLP.alen = 6;

	mpitch_NCC->mLP.filtertype = 0;
	mpitch_NCC->mLP.chanel = 1;

	mpitch_NCC->mshuzhi.filtertype = 2;
	mpitch_NCC->mshuzhi.chanel = 1;
	mpitch_NCC->mshuzhi.blen = 9;
	

}
float SKR_EX(short *x, int len)
{
	int i;
	float sumtmp = 0;

	assert(len);
	for (i = 0; i < len; i++)
	{
		sumtmp += x[i];
	}
	return sumtmp / len;
}
float SKR_NCCF(short *s, int tao, int N)//(4-24)Normalized Cross Correlation Function
{
	int n;
	float tmp1, tmp2, tmp3;

	tmp1 = tmp2 = tmp3 = 0;

	for (n = 0; n < N; n++)
	{
		tmp1 += s[n] * s[n - tao];
		tmp2 += s[n] * s[n];
		tmp3 += s[n - tao] * s[n - tao];
	}
	return tmp1 / sqrt(tmp2*tmp3);
}
int pitch_NCCRun_API(pitch_NCC_ID *mpitch_NCC,short *input,int inLen)
{
	int i;
	float mu;
	short s_[N_FRAMELEN];
	short *s;
	float roumax1,roumax2,roumax3,routao,roumax;
	int maxtao1 = 0;
	int maxtao2 = 0;
	int maxtao3 = 0;
	int maxtao;
	int tao;
	float ELP;
	float SumSlpn2;
	//float Zperiod;
	mpitch_NCC->sideoutZperiod = 0;

	assert(inLen == N_FRAMELEN);

	/////////////////////////////////prepro/////////////////////////////////////////
	mu = SKR_EX(input,N_FRAMELEN);
	for (i = 0;i<N_FRAMELEN;i++)
	{
		s_[i] = input[i] - mu;//(4-27)
	}
	FilterRun_API(&mpitch_NCC->mLP,s_,N_FRAMELEN,s_);//(4-38)
	FilterRun_API(&mpitch_NCC->mshuzhi,s_,N_FRAMELEN,s_);//(4-40)
	//put s_ into membuf
	for (i=0;i<N_FRAMELEN;i++)
	{
		mpitch_NCC->membuf[i] = mpitch_NCC->membuf[i+N_FRAMELEN];
	}
	for (i=0;i<N_FRAMELEN;i++)
	{
		mpitch_NCC->membuf[i+N_FRAMELEN] = s_[i];
		//mpitch_NCC->membuf[i+N_FRAMELEN] = input[i];
	}
	s = &(mpitch_NCC->membuf[N_FRAMELEN]);
	
	//////////////////////////////////ELP////////////////////////////////////////
	SumSlpn2 = 0;
	for (i=0;i<N_FRAMELEN;i++)
	{
		SumSlpn2 += s[i]*s[i];
	}
	SumSlpn2 = SumSlpn2/N_FRAMELEN;
	ELP = 10*log10(SumSlpn2);


	if (ELP>mpitch_NCC->ELPThreshold && mpitch_NCC->memExternalSomein)
	{
		////////////////////////////////the three NCFF peaks//////////////////////////////////////////
		roumax1 = roumax2 = roumax3 = -1.0;
		for (tao = 80;tao<148;tao++)
		{
			routao = SKR_NCCF(s,tao,N_FRAMELEN);
			if (routao>roumax1)
			{
				roumax1 = routao;
				maxtao1 = tao;
			}
		}
		for (tao = 40;tao<80;tao++)
		{
			routao = SKR_NCCF(s,tao,N_FRAMELEN);
			if (routao>roumax2)
			{
				roumax2 = routao;
				maxtao2 = tao;
			}
		}
		for (tao = 20;tao<40;tao++)
		{
			routao = SKR_NCCF(s,tao,N_FRAMELEN);
			if (routao>roumax3)
			{
				roumax3 = routao;
				maxtao3 = tao;
			}
		}
		///////////////////////////////////postpro///////////////////////////////////////
		maxtao = maxtao1;
		roumax = roumax1;
		if (roumax2>=0.96*roumax)
		{
			roumax = roumax2;
			maxtao = maxtao2;
		}
		if (roumax3>=0.96*roumax)
		{
			roumax = roumax3;
			maxtao = maxtao3;
		}
		//////////////////////////////////Zperiod////////////////////////////////////////
		mpitch_NCC->sideoutZperiod = roumax + (roumax1+roumax2+roumax3)/3;
		//printf("zperiod %lf\n",Zperiod);
		if(mpitch_NCC->sideoutZperiod>mpitch_NCC->ZperiodThreshod)
		//if(Zperiod>0.62)
		{
			return maxtao;
		} 
		else
		{
			return 0;
		}

	} 
	else
	{
		return 0;
	}
}

void PitchAVGReset_API(APD_ID *mAPD)
{
	int n;//n is frame num not sample
	for (n = 0;n<6;n++)
	{
		mAPD->memprepitchbuf[n] = 0;
	}
	for (n = 0;n<MAXPITCHWINDOWLEN;n++)
	{
		mAPD->memPitchpreped[n] = 0;
	}
	mAPD->memcontine = 0;
	mAPD->memsumpitch = 0;
	//mAPD->mempitchdelta = 0.0;
	mAPD->memmanwatch = mAPD->memwomanwatch = mAPD->memmidwatch = 0;
	mAPD->memlastpitchAbandon = 0;
	//mAPD->memfirstset = 0;

	mAPD->mempitchavg_g = 0;
	mAPD->memtotalpitchframe = 0;
}

void PitchAVGCalcu_API(APD_ID *mAPD)//
{
	mAPD->giveup_behind = 1;
	mAPD->giveup_front = 1;

	mAPD->pitchwindowlen = 8;//200ms
	mAPD->contine = mAPD->pitchwindowlen + 1;//mAPD->contine must >= mAPD->pitchwindowlen
	mAPD->bigerthanfrontpitch = 20;
	mAPD->smallerthanfrontpitch = 20;
	mAPD->bigerthanbehindpitch = 20;
	mAPD->smallerthanbehindpitch = 22;


}

float PitchAVGRun_API(APD_ID *mAPD,int pitch)//////it must be case3
{
	int n;
	//int *preP;//can remove to calcu
	int N;
	int pitchpreped;
	float pitchavg;
	float pitchdelta;
	float a1,b0;
	float middelta;
	float pitchtmp;

	///////////////////////////////prepro pitch(frame)///////////////////////////////////////////

	for (n=0;n<PREPBUFLEN - 1;n++ )
	{
		mAPD->memprepitchbuf[n] = mAPD->memprepitchbuf[n+1];
	}
	mAPD->memprepitchbuf[n] = pitch;
	//preP = (&(mAPD->memprepitchbuf[5])-giveup_behind);
	N = PREPBUFLEN - 1 - mAPD->giveup_behind;
	pitchpreped = mAPD->memprepitchbuf[N];
	for (n = 1;n<=mAPD->giveup_front;n++)
	{
		if (mAPD->memprepitchbuf[N - n] == 0)//
		{
			pitchpreped = 0;
		}
	}
	for (n = 1;n<=mAPD->giveup_behind;n++)
	{
		if (mAPD->memprepitchbuf[N + n] == 0)//
		{
			pitchpreped = 0;
		}
	}



	if (pitchpreped != 0 && mAPD->memprepitchbuf[N-1] != 0 && mAPD->memprepitchbuf[N+1] != 0)
	{
		if (mAPD->memprepitchbuf[N] - mAPD->memprepitchbuf[N-1] > mAPD->bigerthanfrontpitch && mAPD->memlastpitchAbandon == 0)
		{
			pitchpreped = 0;
		}
		if (mAPD->memprepitchbuf[N] - mAPD->memprepitchbuf[N-1] < -mAPD->smallerthanfrontpitch && mAPD->memlastpitchAbandon == 0)
		{
			pitchpreped = 0;
		}
		if (mAPD->memprepitchbuf[N] - mAPD->memprepitchbuf[N+1] > mAPD->bigerthanbehindpitch)
		{
			pitchpreped = 0;
		}
		if (mAPD->memprepitchbuf[N] - mAPD->memprepitchbuf[N+1] < -mAPD->smallerthanbehindpitch)
		{
			pitchpreped = 0;
		}
		if (pitchpreped == 0)
		{
			mAPD->memlastpitchAbandon = 1;
		}
	}
	else
	{
		mAPD->memlastpitchAbandon = 0;
	}
	///////////////////////////////window analyz///////////////////////////////////////////
	for (n=0;n<MAXPITCHWINDOWLEN - 1;n++ )
	{
		mAPD->memPitchpreped[n] = mAPD->memPitchpreped[n+1];
	}
	mAPD->memPitchpreped[n] = pitchpreped;

	mAPD->memsumpitch += mAPD->memPitchpreped[n];
	mAPD->memsumpitch -= mAPD->memPitchpreped[n - mAPD->pitchwindowlen];

	if (mAPD->memPitchpreped[n] != 0)
	{
		mAPD->memcontine++;
	}
	else
	{
		mAPD->memcontine = 0;
	}

	if (mAPD->memcontine > mAPD->contine)
	{
		mAPD->mempitchavg = (float)mAPD->memsumpitch/(float)mAPD->pitchwindowlen;
		{
			mAPD->mempitchavg_g = (mAPD->memtotalpitchframe*mAPD->mempitchavg_g + mAPD->mempitchavg)/(mAPD->memtotalpitchframe+1);
			mAPD->memtotalpitchframe ++;
		}
	}
	else
	{
		mAPD->mempitchavg = 0;
	}
	return mAPD->mempitchavg;
	//return mAPD->mempitchavg_g;
}
float PeriodAVGRun_API(APD_ID *mAPD,int pitch,float period)
{
	int n;
	//int *preP;//can remove to calcu
	int N;
	int pitchpreped;
	float pitchavg;
	float pitchdelta;
	float a1,b0;
	float middelta;
	float pitchtmp;

	///////////////////////////////prepro pitch(frame)///////////////////////////////////////////

	for (n=0;n<PREPBUFLEN - 1;n++ )
	{
		mAPD->memprepitchbuf[n] = mAPD->memprepitchbuf[n+1];
	}
	mAPD->memprepitchbuf[n] = pitch;
	//preP = (&(mAPD->memprepitchbuf[5])-giveup_behind);
	N = PREPBUFLEN - 1 - mAPD->giveup_behind;
	pitchpreped = mAPD->memprepitchbuf[N];
	for (n = 1;n<=mAPD->giveup_front;n++)
	{
		if (mAPD->memprepitchbuf[N - n] == 0)//
		{
			pitchpreped = 0;
		}
	}
	for (n = 1;n<=mAPD->giveup_behind;n++)
	{
		if (mAPD->memprepitchbuf[N + n] == 0)//
		{
			pitchpreped = 0;
		}
	}



	if (pitchpreped != 0 && mAPD->memprepitchbuf[N-1] != 0 && mAPD->memprepitchbuf[N+1] != 0)
	{
		if (mAPD->memprepitchbuf[N] - mAPD->memprepitchbuf[N-1] > mAPD->bigerthanfrontpitch && mAPD->memlastpitchAbandon == 0)
		{
			pitchpreped = 0;
		}
		if (mAPD->memprepitchbuf[N] - mAPD->memprepitchbuf[N-1] < -mAPD->smallerthanfrontpitch && mAPD->memlastpitchAbandon == 0)
		{
			pitchpreped = 0;
		}
		if (mAPD->memprepitchbuf[N] - mAPD->memprepitchbuf[N+1] > mAPD->bigerthanbehindpitch)
		{
			pitchpreped = 0;
		}
		if (mAPD->memprepitchbuf[N] - mAPD->memprepitchbuf[N+1] < -mAPD->smallerthanbehindpitch)
		{
			pitchpreped = 0;
		}
		if (pitchpreped == 0)
		{
			mAPD->memlastpitchAbandon = 1;
		}
	}
	else
	{
		mAPD->memlastpitchAbandon = 0;
	}
	///////////////////////////////window analyz///////////////////////////////////////////
	for (n=0;n<MAXPITCHWINDOWLEN - 1;n++ )
	{
		mAPD->memPitchpreped[n] = mAPD->memPitchpreped[n+1];
	}
	mAPD->memPitchpreped[n] = period*10000;

	mAPD->memsumpitch += mAPD->memPitchpreped[n];
	mAPD->memsumpitch -= mAPD->memPitchpreped[n - mAPD->pitchwindowlen];

	if (pitchpreped != 0)
	{
		mAPD->memcontine++;
	}
	else
	{
		mAPD->memcontine = 0;
	}

	if (mAPD->memcontine > mAPD->contine)
	{
		mAPD->mempitchavg = (float)mAPD->memsumpitch/(float)mAPD->pitchwindowlen;
		{
			mAPD->mempitchavg_g = (mAPD->memtotalpitchframe*mAPD->mempitchavg_g + mAPD->mempitchavg)/(mAPD->memtotalpitchframe+1);
			mAPD->memtotalpitchframe ++;
		}
	}
	else
	{
		mAPD->mempitchavg = 0;
	}
	return mAPD->mempitchavg/10000.0;
	//return mAPD->mempitchavg_g;
}
