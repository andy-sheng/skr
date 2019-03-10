#include "Filter_control.h"
#include "common/functions.h"

//#include "SKR_Filter_functions.h"
#include <stdlib.h>
#include <assert.h>


static void SKRfir(short *output, short *input, int inlen, short *mem_input, float *b, short blen)
{
	short i,j;
	short *x;
	float sum;
	int border;
	
	border = blen-1;
		
	for(i=0;i<inlen;i++)mem_input[i+border]=input[i];
	x=mem_input+ border;

	for(i=0;i<inlen;i++)
	{
		for(sum=0,j=0;j<blen;j++)
		{
			sum += b[j]*x[i-j];
		}
		output[i]=stoshort(sum);
	}
	for (i = 0; i<border; i++)mem_input[i] = mem_input[i + inlen];
}
static void SKRiir(short *output, short *input, int inlen,float *mem_output, short *mem_input, float *b, short blen, float *a, short alen)
{
	short i,j;
	float sumx,sumy;
	short *x;
	float *y;
	//short memx[SKR_MAX_FRAME_SAMPLE_MONO+MAXORDER];//short *memx = (short *)malloc((inlen+blen-1)*sizeof(short));
	//float memy[SKR_MAX_FRAME_SAMPLE_MONO+MAXORDER];//float *memy = (float *)malloc((inlen+alen-1)*sizeof(float));
	int border;
	int aorder;

	border = blen-1;
	aorder = alen-1;

	//x=memx+border;
	//y=memy+aorder;

	for (i = 0; i<inlen; i++)mem_input[i + border] = input[i];
	x=mem_input+ border;		
	y=mem_output+ aorder;

	for(i=0;i<inlen;i++)
	{
		for(sumx=0,j=0;j<blen;j++)sumx += b[j]*x[i-j];
		for(sumy=0,j=1;j<alen;j++)sumy += a[j]*y[i-j];
		y[i]=(sumx-sumy);
	}
	//for(i=0;i<aorder-inlen;i++)mem_output[i]=mem_output[inlen+i];
	//for(;i<aorder;i++)mem_output[i]=y[inlen-aorder+i];
	//for(i=0;i<border-inlen;i++)mem_input[i]=mem_input[inlen+i];
	//for(;i<border;i++)mem_input[i]=input[inlen-border+i];

	for(i=0;i<inlen;i++)output[i]=stoshort(y[i]);

	for (i = 0; i<border; i++)mem_input[i] = mem_input[i + inlen];
	for (i = 0; i<aorder; i++)
	{
		if (fabs(mem_output[i + inlen])<0.000001)
		{
			mem_output[i + inlen] = 0;
		}
		mem_output[i] = mem_output[i + inlen];
	}

	//free(memx);
	//free(memy);
}
static void filterNOrNsSec(float *b,float *a,int n,int ns,float *x,int len,float *px,float *py)
{
	int i,j,k,n1;
	
	if (n == 2)
	{
		n1=2+1;
		for(j=0;j<ns;j++)
		{
			for(k=0;k<len;k++)
			{
				px[j*n1+0]=x[k];
				x[k]=b[j*n1+0]*px[j*n1+0];
				for(i=1;i<=2;i++)
				{
					x[k]+=b[j*n1+i]*px[j*n1+i]-a[j*n1+i]*py[j*n1+i];
				}
				if(fabs(x[k])>1.0e10)
				{
					assert(0);
				}
				if(fabs(x[k])<0.000001)
				{
					x[k] = 0;
					//if(x[k]!=0)
					//{
					//	x[k]=x[k];
					//}
				}
				//for(i=n;i>=2;i--)
				{
					px[j*n1+2]=px[j*n1+1];
					py[j*n1+2]=py[j*n1+1];
				}
				px[j*n1+1]=px[j*n1+0];
				py[j*n1+1]=x[k];
			}
		}
	} 
	else if(n == 4)
	{
		n1=n+1;
		for(j=0;j<ns;j++)
		{
			for(k=0;k<len;k++)
			{
				px[j*n1+0]=x[k];
				x[k]=b[j*n1+0]*px[j*n1+0];
				for(i=1;i<=n;i++)
				{
					x[k]+=b[j*n1+i]*px[j*n1+i]-a[j*n1+i]*py[j*n1+i];
				}
				if(fabs(x[k])>1.0e10)
				{
					assert(0);
				}
				if(fabs(x[k])<0.000001)
				{
					x[k] = 0;
				}
				//for(i=n;i>=2;i--)
				{
					px[j*n1+4]=px[j*n1+3];
					py[j*n1+4]=py[j*n1+3];

					px[j*n1+3]=px[j*n1+2];
					py[j*n1+3]=py[j*n1+2];

					px[j*n1+2]=px[j*n1+1];
					py[j*n1+2]=py[j*n1+1];
				}
				px[j*n1+1]=px[j*n1+0];
				py[j*n1+1]=x[k];
			}
		}
	}
	else
	{
		assert(0);
	}


}


void FilterfReset_API(Filterf_s *mFilter)
{
	int i;
	for (i=0;i<MAXORDER_F+SKR_MAX_FRAME_SAMPLE_MONO;i++)
	{
		mFilter->memL.meminput[i]=0;
		mFilter->memL.memoutput[i]=0;
	}
}
void skriirf(float *output, float *input, int inlen,double *mem_output, float *mem_input, double *b, short blen, double *a, short alen)
{
	short i,j;
	double sumx,sumy;
	float *x;
	double *y;
	//short memx[SKR_MAX_FRAME_SAMPLE_MONO+MAXORDER];//short *memx = (short *)malloc((inlen+blen-1)*sizeof(short));
	//float memy[SKR_MAX_FRAME_SAMPLE_MONO+MAXORDER];//float *memy = (float *)malloc((inlen+alen-1)*sizeof(float));
	int border;
	int aorder;

	border = blen-1;
	aorder = alen-1;

	//x=memx+border;
	//y=memy+aorder;

	for(i=0;i<border;i++)mem_input[i]=mem_input[i+inlen];
	for(i=0;i<inlen;i++)mem_input[i+border]=input[i];
	x=mem_input+ border;

	for(i=0;i<aorder;i++)
	{
		if(fabs(mem_output[i+inlen])<0.000001)
		{
			mem_output[i+inlen] = 0;
		}
		mem_output[i]=mem_output[i+inlen];
	}
	y=mem_output+ aorder;

	for(i=0;i<inlen;i++)
	{
		for(sumx=0,j=0;j<blen;j++)sumx += b[j]*x[i-j];
		for(sumy=0,j=1;j<alen;j++)sumy += a[j]*y[i-j];
		y[i]=(sumx-sumy);
	}
	//for(i=0;i<aorder-inlen;i++)mem_output[i]=mem_output[inlen+i];
	//for(;i<aorder;i++)mem_output[i]=y[inlen-aorder+i];
	//for(i=0;i<border-inlen;i++)mem_input[i]=mem_input[inlen+i];
	//for(;i<border;i++)mem_input[i]=input[inlen-border+i];

	for(i=0;i<inlen;i++)output[i]=(float)(y[i]);

	//free(memx);
	//free(memy);
}
void skrfirf(float *output, float *input, int inlen, float *mem_input, double *b, short blen)
{
	short i,j;
	float *x;
	double sum;
	int border;

	border = blen-1;

	for(i=0;i<border;i++)mem_input[i]=mem_input[i+inlen];
	for(i=0;i<inlen;i++)mem_input[i+border]=input[i];
	x=mem_input+ border;

	for(i=0;i<inlen;i++)
	{
		for(sum=0,j=0;j<blen;j++)
		{
			sum += b[j]*x[i-j];
		}
		output[i]=(float)(sum);
	}

}
void FilterfRun_API(Filterf_s *mFilter,float *input,int inLen,float *output)
{
	if (mFilter->filtertype == -1)
	{
		skrfirf(output, input, inLen, mFilter->memL.meminput, mFilter->b, mFilter->blen);
	} 
	else if(mFilter->filtertype == 0)
	{
		skriirf(output, input, inLen,mFilter->memL.memoutput, mFilter->memL.meminput, mFilter->b, mFilter->blen, mFilter->a, mFilter->alen);
	}
	
}
void FilterfRun_API_shortin(Filterf_s *mFilter,short *input,int inLen,float *output)
{
	float inputf[SKR_MAX_FRAME_SAMPLE_MONO];
	int i;

	for (i=0;i<inLen;i++)
	{
		inputf[i] = input[i];
	}
	FilterfRun_API(mFilter,inputf,inLen,output);
}
void FilterlongfirReset_API(Filterlongfir_s *mFilter)
{
	int i;
	for (i=0;i<MAXORDER_BIG+SKR_MAX_FRAME_SAMPLE_MONO;i++)
	{
		mFilter->memL.meminput[i]=0;
		mFilter->memR.meminput[i]=0;
	}
	for (i=0;i<MAXORDER+SKR_MAX_FRAME_SAMPLE_MONO;i++)
	{
		mFilter->memL.memoutput[i]=0;
		mFilter->memR.memoutput[i]=0;

	}
	for (i=0;i<MAX_NS*(2+1);i++)
	{
		mFilter->memL.mempx[i] = 0;
		mFilter->memR.mempx[i] = 0;
		mFilter->memL.mempy[i] = 0;
		mFilter->memR.mempy[i] = 0;
	}
	mFilter->memL.memi = 0;
	mFilter->memR.memi = 0;
	mFilter->memL.memsumin = 0;
	mFilter->memR.memsumin = 0;

	BufresetAPI(&mFilter->mBufin);
	BufresetAPI(&mFilter->mBufout);

	mFilter->membufrear = -12345;
	mFilter->cmemcall = 0;
	mFilter->cmemblen = 0;
	mFilter->cmemfftN = 0;
}
void FilterlongfirCalcu_APItime(Filterlongfir_s *mFilter)//tmp for 3dcontine 
{
	int i;

	if (mFilter->cmemblen == mFilter->blen)
	{
		for (i=0;i<mFilter->blen;i++)
		{
			mFilter->cmemb[i] = mFilter->b[i];
			mFilter->cmembR[i] = mFilter->bR[i];
		}
		for (i=0;i<SKR_MAX_FRAME_SAMPLE_MONO+mFilter->blen;i++)
		{
			mFilter->cmeminputL[i] = mFilter->memL.meminput[i];
			mFilter->cmeminputR[i] = mFilter->memR.meminput[i];
		}
		mFilter->cmemcall = 1;
	}

	mFilter->cmemblen = mFilter->blen;
}
void FilterlongfirCalcu_API_forFFT(Filterlongfir_s *mFilter)
{
	int i;
	int hM;
	int rear;

	if (mFilter->filtertype == FIR_FFTCONV||mFilter->filtertype == FIR_FFTCONV_XFAN||mFilter->filtertype == FIR_FFTCONV_HDIFF||mFilter->filtertype == FIR_FFTCONV_XSAME)
	{
		hM = THEMAXOF(mFilter->blen,mFilter->bRlen);
		assert(mFilter->blen == mFilter->bRlen);
	} 
	else//"FIR_FFTCONV_HSAME"
	{
		hM = mFilter->blen;
	}
	
	//calcu fftN,offset
	for (mFilter->fftN=2;;mFilter->fftN*=2)
	{
		if (mFilter->fftN>= hM*2)
		{
			break;
		}
	}
	mFilter->offset = (mFilter->fftN - (hM - 1))*mFilter->chanel;

	assert(BUFFLEN>mFilter->fftN);
	assert(MAXFFTN>=mFilter->fftN);


	rear = mFilter->fftN*mFilter->chanel;
	if (mFilter->filtertype == FIR_FFTCONV_XSAME)
	{
		rear = mFilter->fftN;
	}
	
	if (mFilter->membufrear!=rear)
	{
		mFilter->mBufin.id = 0;
		mFilter->mBufout.id = 0;
		//calcu bufin's front rear,mono and stereo
		mFilter->mBufin.front = 0;
		mFilter->mBufin.rear = mFilter->fftN*mFilter->chanel;
		if (mFilter->filtertype == FIR_FFTCONV_XSAME)
		{
			mFilter->mBufin.rear = mFilter->fftN;
		}//mFilter->offset--wrong;//(mFilter->blen - 1)--wrong;

		mFilter->mBufout.front = 0;
		mFilter->mBufout.rear = 0;
	}

	mFilter->membufrear = rear;
#if FILTERUSESKRFFT
	rfftCalcu_API(&mFilter->mrfft,mFilter->fftN);
	irfftCalcu_API(&mFilter->mirfft,mFilter->fftN);
#else
	skr_RealFFT_Init(&mFilter->realfft,mFilter->fftN);
#endif
	if (mFilter->cmemfftN == mFilter->fftN)
	{
		for (i=0;i<mFilter->fftN;i++)
		{
			mFilter->cmemhLrfft[i] = mFilter->hLrfft[i];
			mFilter->cmemhRrfft[i] = mFilter->hRrfft[i];
		}
		mFilter->cmemcall = 1;
	}

	mFilter->cmemfftN = mFilter->fftN;



	if (mFilter->filtertype == FIR_FFTCONV||mFilter->filtertype == FIR_FFTCONV_XFAN||mFilter->filtertype == FIR_FFTCONV_HDIFF||mFilter->filtertype == FIR_FFTCONV_XSAME)
	{
		for (i=0;i<hM;i++)
		{
			mFilter->hLrfft[i] = mFilter->b[i];
			mFilter->hRrfft[i] = mFilter->bR[i];
		}
		for (;i<mFilter->fftN;i++)
		{
			mFilter->hLrfft[i] = 0;
			mFilter->hRrfft[i] = 0;
		}
#if FILTERUSESKRFFT
		rfftRun_API(&mFilter->mrfft,mFilter->hLrfft,mFilter->fftN);
		rfftRun_API(&mFilter->mrfft,mFilter->hRrfft,mFilter->fftN);
#else
		skr_RealFFT_Run2(&mFilter->realfft,mFilter->hLrfft,mFilter->fftN);
		skr_RealFFT_Run2(&mFilter->realfft,mFilter->hRrfft,mFilter->fftN);
#endif
	}
	else
	{
		for (i=0;i<hM;i++)
		{
			mFilter->hLrfft[i] = mFilter->b[i];
		}
		for (;i<mFilter->fftN;i++)
		{
			mFilter->hLrfft[i] = 0;
		}
#if FILTERUSESKRFFT
		rfftRun_API(&mFilter->mrfft,mFilter->hLrfft,mFilter->fftN);
#else
		skr_RealFFT_Run2(&mFilter->realfft,mFilter->hLrfft,mFilter->fftN);
#endif
	}

}
void FilterlongfirRun_API(Filterlongfir_s *mFilter,short *input,int inLen,short *output)
{
	int i,j;
	int inRlen;
	//float intlx[4096];//
	short x16L[SKR_MAX_FRAME_SAMPLE_MONO];
	short x16R[SKR_MAX_FRAME_SAMPLE_MONO];
	short x16L0[SKR_MAX_FRAME_SAMPLE_MONO];
	short x16R0[SKR_MAX_FRAME_SAMPLE_MONO];
	float idivinlen;
	float xL[SKR_MAX_FRAME_SAMPLE_MONO];
	float xR[SKR_MAX_FRAME_SAMPLE_MONO];


	if (mFilter->chanel == 2)//maybe HL != HR...
	{	
		switch (mFilter->filtertype)
		{
		case FIR_FFTCONV_XSAME:
			putinAPI_loop_onlychannelLin(&mFilter->mBufin,input,inLen);
			for (;queuehave_API(&mFilter->mBufin)>=mFilter->fftN;)
			{
				for (i=0,j=(mFilter->mBufin.front+1)%BUFFLEN;i<mFilter->fftN;i++)
				{
					mFilter->xLintl[i] = mFilter->mBufin.membuf[j];
					j = (j + 1)%BUFFLEN;
				}
#if FILTERUSESKRFFT
				rfftRun_API(&mFilter->mrfft,mFilter->xLintl,mFilter->fftN);

				rfftmut(mFilter->xLhL,mFilter->hLrfft,mFilter->xLintl,mFilter->fftN);
				irfftRun_API(&mFilter->mirfft,mFilter->xLhL,mFilter->fftN);

				rfftmut(mFilter->xRhR,mFilter->hRrfft,mFilter->xLintl,mFilter->fftN);
				irfftRun_API(&mFilter->mirfft,mFilter->xRhR,mFilter->fftN);
#else
				skr_RealFFT_Run2(&mFilter->realfft,mFilter->xLintl,mFilter->fftN);

				rfftmut2(mFilter->xLhL,mFilter->hLrfft,mFilter->xLintl,mFilter->fftN);
				skr_InverseRealFFT_Run2(&mFilter->realfft,mFilter->xLhL,mFilter->fftN);

				rfftmut2(mFilter->xRhR,mFilter->hRrfft,mFilter->xLintl,mFilter->fftN);
				skr_InverseRealFFT_Run2(&mFilter->realfft,mFilter->xRhR,mFilter->fftN);
#endif
				mFilter->mBufin.front = (mFilter->mBufin.front+mFilter->offset/2)%BUFFLEN;//offset has multed channel

				for (i = mFilter->blen-1;i<mFilter->fftN;i++)
				{
					mFilter->mBufout.rear = (mFilter->mBufout.rear + 1)%BUFFLEN;
					mFilter->mBufout.membuf[mFilter->mBufout.rear] = stoshort(mFilter->xLhL[i]);
					mFilter->mBufout.rear = (mFilter->mBufout.rear + 1)%BUFFLEN;
					mFilter->mBufout.membuf[mFilter->mBufout.rear] = stoshort(mFilter->xRhR[i]);
				}
			}
			putoutAPI_ForReframe_0_loop(&mFilter->mBufout,output,inLen);
			break;
		case FIR_FFTCONV:
			//inRlen = inLen/2;
			//ChanelConvert(1,2,inLen,input,NULL,x16L,x16R);
			putinAPI_loop(&mFilter->mBufin,input,inLen);
			for (;queuehave_API(&mFilter->mBufin)>=mFilter->fftN*2;)
			{
				for (i=0,j=(mFilter->mBufin.front+1)%BUFFLEN;i<mFilter->fftN;i++)//in our loopqueue buf[front] is null(see putoutloop())
				{
					mFilter->xLintl[i] = mFilter->mBufin.membuf[j];
					j = (j + 1)%BUFFLEN;
					mFilter->xRintl[i] = mFilter->mBufin.membuf[j];
					j = (j + 1)%BUFFLEN;
				}
#if FILTERUSESKRFFT
				rfftRun_API(&mFilter->mrfft,mFilter->xLintl,mFilter->fftN);
				rfftmut(mFilter->xLhL,mFilter->hLrfft,mFilter->xLintl,mFilter->fftN);
				irfftRun_API(&mFilter->mirfft,mFilter->xLhL,mFilter->fftN);

				rfftRun_API(&mFilter->mrfft,mFilter->xRintl,mFilter->fftN);
				rfftmut(mFilter->xRhR,mFilter->hRrfft,mFilter->xRintl,mFilter->fftN);
				irfftRun_API(&mFilter->mirfft,mFilter->xRhR,mFilter->fftN);
#else
				skr_RealFFT_Run2(&mFilter->realfft,mFilter->xLintl,mFilter->fftN);
				rfftmut2(mFilter->xLhL,mFilter->hLrfft,mFilter->xLintl,mFilter->fftN);
				skr_InverseRealFFT_Run2(&mFilter->realfft,mFilter->xLhL,mFilter->fftN);

				skr_RealFFT_Run2(&mFilter->realfft,mFilter->xRintl,mFilter->fftN);
				rfftmut2(mFilter->xRhR,mFilter->hRrfft,mFilter->xRintl,mFilter->fftN);
				skr_InverseRealFFT_Run2(&mFilter->realfft,mFilter->xRhR,mFilter->fftN);
#endif
				if (mFilter->cmemcall == 1)
				{
#if FILTERUSESKRFFT
					rfftmut(mFilter->xLhL0,mFilter->cmemhLrfft,mFilter->xLintl,mFilter->fftN);
					irfftRun_API(&mFilter->mirfft,mFilter->xLhL0,mFilter->fftN);

					rfftmut(mFilter->xRhR0,mFilter->cmemhRrfft,mFilter->xRintl,mFilter->fftN);
					irfftRun_API(&mFilter->mirfft,mFilter->xRhR0,mFilter->fftN);
#else
					rfftmut2(mFilter->xLhL0,mFilter->cmemhLrfft,mFilter->xLintl,mFilter->fftN);
					skr_InverseRealFFT_Run2(&mFilter->realfft,mFilter->xLhL0,mFilter->fftN);

					rfftmut2(mFilter->xRhR0,mFilter->cmemhRrfft,mFilter->xRintl,mFilter->fftN);
					skr_InverseRealFFT_Run2(&mFilter->realfft,mFilter->xRhR0,mFilter->fftN);
#endif
					idivinlen = 1.f/(mFilter->fftN-1);
					for (i=0;i<mFilter->fftN;i++)
					{
						mFilter->xLhL[i] = mFilter->xLhL[i]*i*idivinlen + mFilter->xLhL0[i]*(1-i*idivinlen);
						mFilter->xRhR[i] = mFilter->xRhR[i]*i*idivinlen + mFilter->xRhR0[i]*(1-i*idivinlen);
					}
					mFilter->cmemcall = 0;
				}
				mFilter->mBufin.front = (mFilter->mBufin.front+mFilter->offset)%BUFFLEN;//offset has multed channel

				for (i = mFilter->blen-1;i<mFilter->fftN;i++)
				{
					mFilter->mBufout.rear = (mFilter->mBufout.rear + 1)%BUFFLEN;
					mFilter->mBufout.membuf[mFilter->mBufout.rear] = stoshort(mFilter->xLhL[i]);
					mFilter->mBufout.rear = (mFilter->mBufout.rear + 1)%BUFFLEN;
					mFilter->mBufout.membuf[mFilter->mBufout.rear] = stoshort(mFilter->xRhR[i]);
				}
			}
			putoutAPI_ForReframe_0_loop(&mFilter->mBufout,output,inLen);
			break;
		case FIR_FFTCONV_XFAN:
			//inRlen = inLen/2;
			//ChanelConvert(1,2,inLen,input,NULL,x16L,x16R);
			putinAPI_loop(&mFilter->mBufin,input,inLen);
			for (;queuehave_API(&mFilter->mBufin)>=mFilter->fftN*2;)
			{
				for (i=0,j=(mFilter->mBufin.front+1)%BUFFLEN;i<mFilter->fftN;i++)//in our loopqueue buf[front] is null(see putoutloop())
				{
					mFilter->xRintl[i] = mFilter->mBufin.membuf[j];
					j = (j + 1)%BUFFLEN;
					mFilter->xLintl[i] = mFilter->mBufin.membuf[j];
					j = (j + 1)%BUFFLEN;
				}
#if FILTERUSESKRFFT
				rfftRun_API(&mFilter->mrfft,mFilter->xLintl,mFilter->fftN);
				rfftmut(mFilter->xLhL,mFilter->hLrfft,mFilter->xLintl,mFilter->fftN);
				irfftRun_API(&mFilter->mirfft,mFilter->xLhL,mFilter->fftN);

				rfftRun_API(&mFilter->mrfft,mFilter->xRintl,mFilter->fftN);
				rfftmut(mFilter->xRhR,mFilter->hRrfft,mFilter->xRintl,mFilter->fftN);
				irfftRun_API(&mFilter->mirfft,mFilter->xRhR,mFilter->fftN);
#else
				skr_RealFFT_Run2(&mFilter->realfft,mFilter->xLintl,mFilter->fftN);
				rfftmut2(mFilter->xLhL,mFilter->hLrfft,mFilter->xLintl,mFilter->fftN);
				skr_InverseRealFFT_Run2(&mFilter->realfft,mFilter->xLhL,mFilter->fftN);

				skr_RealFFT_Run2(&mFilter->realfft,mFilter->xRintl,mFilter->fftN);
				rfftmut2(mFilter->xRhR,mFilter->hRrfft,mFilter->xRintl,mFilter->fftN);
				skr_InverseRealFFT_Run2(&mFilter->realfft,mFilter->xRhR,mFilter->fftN);
#endif
				mFilter->mBufin.front = (mFilter->mBufin.front+mFilter->offset)%BUFFLEN;//offset has multed channel

				for (i = mFilter->blen-1;i<mFilter->fftN;i++)
				{
					mFilter->mBufout.rear = (mFilter->mBufout.rear + 1)%BUFFLEN;
					mFilter->mBufout.membuf[mFilter->mBufout.rear] = stoshort(mFilter->xLhL[i]);
					mFilter->mBufout.rear = (mFilter->mBufout.rear + 1)%BUFFLEN;
					mFilter->mBufout.membuf[mFilter->mBufout.rear] = stoshort(mFilter->xRhR[i]);
				}
			}
			putoutAPI_ForReframe_0_loop(&mFilter->mBufout,output,inLen);
			break;
		case FIR_DIRCTCONV_HSAME:
			inRlen = inLen/2;
			ChanelConvert(1,2,inLen,input,NULL,x16L,x16R);
			SKRfir(x16L, x16L, inRlen,mFilter->memL.meminput, mFilter->b, mFilter->blen);
			SKRfir(x16R, x16R, inRlen,mFilter->memR.meminput, mFilter->b, mFilter->blen);
			ChanelConvert(2,1,inRlen,x16L,x16R,output,NULL);
			break;
		case FIR_DIRCTCONV:
			inRlen = inLen/2;
			ChanelConvert(1,2,inLen,input,NULL,x16L,x16R);
			SKRfir(x16L, x16L, inRlen,mFilter->memL.meminput, mFilter->b, mFilter->blen);
			SKRfir(x16R, x16R, inRlen,mFilter->memR.meminput, mFilter->bR, mFilter->bRlen);
			if (mFilter->cmemcall == 1)
			{
				ChanelConvert(1,2,inLen,input,NULL,x16L0,x16R0);
				SKRfir(x16L0, x16L0, inRlen,mFilter->cmeminputL, mFilter->cmemb, mFilter->cmemblen);
				SKRfir(x16R0, x16R0, inRlen,mFilter->cmeminputR, mFilter->cmembR, mFilter->cmemblen);

				idivinlen = 1.f/(inRlen-1);
				for (i=0;i<inRlen;i++)
				{
					x16L[i] = x16L[i]*i*idivinlen + x16L0[i]*(1-i*idivinlen);
					x16R[i] = x16R[i]*i*idivinlen + x16R0[i]*(1-i*idivinlen);
				}
				mFilter->cmemcall = 0;
			}
			ChanelConvert(2,1,inRlen,x16L,x16R,output,NULL);
			break;
		case IIR_DIRCT1_HSAME:
			inRlen = inLen/2;
			ChanelConvert(1,2,inLen,input,NULL,x16L,x16R);
			SKRiir(x16L, x16L, inRlen,mFilter->memL.memoutput, mFilter->memL.meminput, mFilter->b, mFilter->blen, mFilter->a, mFilter->alen);
			SKRiir(x16R, x16R, inRlen,mFilter->memR.memoutput, mFilter->memR.meminput, mFilter->b, mFilter->blen, mFilter->a, mFilter->alen);
			ChanelConvert(2,1,inRlen,x16L,x16R,output,NULL);
			break;
		case IIR_SEC_HSAME:
			inRlen = inLen/2;
			ChanelConvert(1,2,inLen,input,NULL,x16L,x16R);
			for (i = 0;i<inRlen;i++)
			{
				xL[i] = x16L[i];
			}
			filterNOrNsSec(mFilter->b,mFilter->a,mFilter->n,mFilter->ns,xL,inRlen,mFilter->memL.mempx,mFilter->memL.mempy);
			for (i = 0;i<inRlen;i++)
			{
				x16L[i] = stoshort(xL[i]) ;
			}
			for (i = 0;i<inRlen;i++)
			{
				xR[i] = x16R[i];
			}
			filterNOrNsSec(mFilter->b,mFilter->a,mFilter->n,mFilter->ns,xR,inRlen,mFilter->memR.mempx,mFilter->memR.mempy);
			for (i = 0;i<inRlen;i++)
			{
				x16R[i] = stoshort(xR[i]) ;
			}
			ChanelConvert(2,1,inRlen,x16L,x16R,output,NULL);
			break;
		default:assert(0);
			break;
		}
	}
	else if (mFilter->chanel == 1)
	{
		switch (mFilter->filtertype)
		{
			case FIR_FFTCONV_HDIFF://mono in stereo out...
			putinAPI_loop(&mFilter->mBufin,input,inLen);
			for (;queuehave_API(&mFilter->mBufin)>=mFilter->fftN;)
			{
				for (i=0,j=(mFilter->mBufin.front+1)%BUFFLEN;i<mFilter->fftN;i++)
				{
					mFilter->xLintl[i] = mFilter->mBufin.membuf[j];
					j = (j + 1)%BUFFLEN;
				}
#if FILTERUSESKRFFT				
				rfftRun_API(&mFilter->mrfft,mFilter->xLintl,mFilter->fftN);
				rfftmut(mFilter->xLhL,mFilter->hLrfft,mFilter->xLintl,mFilter->fftN);
				irfftRun_API(&mFilter->mirfft,mFilter->xLhL,mFilter->fftN);

				rfftmut(mFilter->xRhR,mFilter->hRrfft,mFilter->xLintl,mFilter->fftN);
				irfftRun_API(&mFilter->mirfft,mFilter->xRhR,mFilter->fftN);
#else
				skr_RealFFT_Run2(&mFilter->realfft,mFilter->xLintl,mFilter->fftN);
				rfftmut2(mFilter->xLhL,mFilter->hLrfft,mFilter->xLintl,mFilter->fftN);
				skr_InverseRealFFT_Run2(&mFilter->realfft,mFilter->xLhL,mFilter->fftN);

				rfftmut2(mFilter->xRhR,mFilter->hRrfft,mFilter->xLintl,mFilter->fftN);
				skr_InverseRealFFT_Run2(&mFilter->realfft,mFilter->xRhR,mFilter->fftN);
#endif
				if (mFilter->cmemcall == 1)
				{
#if FILTERUSESKRFFT
					rfftmut(mFilter->xLhL0,mFilter->cmemhLrfft,mFilter->xLintl,mFilter->fftN);
					irfftRun_API(&mFilter->mirfft,mFilter->xLhL0,mFilter->fftN);

					rfftmut(mFilter->xRhR0,mFilter->cmemhRrfft,mFilter->xLintl,mFilter->fftN);
					irfftRun_API(&mFilter->mirfft,mFilter->xRhR0,mFilter->fftN);
#else
					rfftmut2(mFilter->xLhL0,mFilter->cmemhLrfft,mFilter->xLintl,mFilter->fftN);
					skr_InverseRealFFT_Run2(&mFilter->realfft,mFilter->xLhL0,mFilter->fftN);

					rfftmut2(mFilter->xRhR0,mFilter->cmemhRrfft,mFilter->xLintl,mFilter->fftN);
					skr_InverseRealFFT_Run2(&mFilter->realfft,mFilter->xRhR0,mFilter->fftN);
#endif
					idivinlen = 1.f/(mFilter->fftN-1);
					for (i=0;i<mFilter->fftN;i++)
					{
						mFilter->xLhL[i] = mFilter->xLhL[i]*i*idivinlen + mFilter->xLhL0[i]*(1-i*idivinlen);
						mFilter->xRhR[i] = mFilter->xRhR[i]*i*idivinlen + mFilter->xRhR0[i]*(1-i*idivinlen);
					}
					mFilter->cmemcall = 0;
				}

				mFilter->mBufin.front = (mFilter->mBufin.front+mFilter->offset)%BUFFLEN;

				///putinAPI_loop_floatin
				for (i = mFilter->blen-1;i<mFilter->fftN;i++)
				{
					mFilter->mBufout.rear = (mFilter->mBufout.rear + 1)%BUFFLEN;
					mFilter->mBufout.membuf[mFilter->mBufout.rear] = stoshort(mFilter->xLhL[i]);
					mFilter->mBufout.rear = (mFilter->mBufout.rear + 1)%BUFFLEN;
					mFilter->mBufout.membuf[mFilter->mBufout.rear] = stoshort(mFilter->xRhR[i]);
				}
			}
			putoutAPI_ForReframe_0_loop(&mFilter->mBufout,output,inLen*2);
			break;
		case FIR_FFTCONV:
			putinAPI_loop(&mFilter->mBufin,input,inLen);
			for (;queuehave_API(&mFilter->mBufin)>=mFilter->fftN;)
			{
				for (i=0,j=(mFilter->mBufin.front+1)%BUFFLEN;i<mFilter->fftN;i++)
				{
					mFilter->xLintl[i] = mFilter->mBufin.membuf[j];
					j = (j + 1)%BUFFLEN;
				}
#if FILTERUSESKRFFT				
				//SKRrfft(intlx,mFilter->fftN);//...
				rfftRun_API(&mFilter->mrfft,mFilter->xLintl,mFilter->fftN);
				rfftmut(mFilter->xLhL,mFilter->hLrfft,mFilter->xLintl,mFilter->fftN);
				irfftRun_API(&mFilter->mirfft,mFilter->xLhL,mFilter->fftN);
#else
				skr_RealFFT_Run2(&mFilter->realfft,mFilter->xLintl,mFilter->fftN);
				rfftmut2(mFilter->xLhL,mFilter->hLrfft,mFilter->xLintl,mFilter->fftN);
				skr_InverseRealFFT_Run2(&mFilter->realfft,mFilter->xLhL,mFilter->fftN);
#endif
				//fastmoveAPI_loop(&mFilter->mBufin,mFilter->offset);
				mFilter->mBufin.front = (mFilter->mBufin.front+mFilter->offset)%BUFFLEN;

				///putinAPI_loop_floatin
				for (i = mFilter->blen-1;i<mFilter->fftN;i++)
				{
					/*//we know inlen<<offset...
					if((mFilter->mBufout.rear+1)%BUFFLEN == mFilter->mBufout.front)//�����Ż�ȥ�����if���ж�һ�β���������
					{
						//break;
						assert(0);
						return;
					}*/
					mFilter->mBufout.rear = (mFilter->mBufout.rear + 1)%BUFFLEN;
					mFilter->mBufout.membuf[mFilter->mBufout.rear] = stoshort(mFilter->xLhL[i]);
				}
			}
			putoutAPI_ForReframe_0_loop(&mFilter->mBufout,output,inLen);
			break;
		case FIR_DIRCTCONV_HSAME:
		case FIR_DIRCTCONV:
			SKRfir(output, input, inLen,mFilter->memL.meminput, mFilter->b, mFilter->blen);
			break;
		case FIR_DIRCTCONV_HDIFF:
			SKRfir(x16L, input, inLen,mFilter->memL.meminput, mFilter->b, mFilter->blen);
			SKRfir(x16R, input, inLen,mFilter->memR.meminput, mFilter->bR, mFilter->bRlen);

			if (mFilter->cmemcall == 1)
			{
				SKRfir(x16L0, input, inLen,mFilter->cmeminputL, mFilter->cmemb, mFilter->cmemblen);
				SKRfir(x16R0, input, inLen,mFilter->cmeminputR, mFilter->cmembR, mFilter->cmemblen);

				idivinlen = 1.f/(inLen-1);
				for (i=0;i<inLen;i++)
				{
					x16L[i] = x16L[i]*i*idivinlen + x16L0[i]*(1-i*idivinlen);
					x16R[i] = x16R[i]*i*idivinlen + x16R0[i]*(1-i*idivinlen);
				}
				mFilter->cmemcall = 0;
			}
			ChanelConvert(2,1,inLen,x16L,x16R,output,NULL);
			break;
		case IIR_DIRCT1_HSAME:
			SKRiir(output, input, inLen,mFilter->memL.memoutput, mFilter->memL.meminput, mFilter->b, mFilter->blen, mFilter->a, mFilter->alen);
			break;
		case IIR_SEC_HSAME:
			for (i = 0;i<inLen;i++)
			{
				xL[i] = input[i];
			}
			filterNOrNsSec(mFilter->b,mFilter->a,mFilter->n,mFilter->ns,xL,inLen,mFilter->memL.mempx,mFilter->memL.mempy);
			for (i = 0;i<inLen;i++)
			{
				output[i] = stoshort(xL[i]) ;
			}
			break;
		default:assert(0);
			break;

		}
		
	}
	else
	{
		assert(0);
	}
}


void FastCovAllOutInit_API(FastCov_allout_s *fastcov,float *b,int blen,int xlen)
{
	int i;

	fastcov->blen = blen;
	for (fastcov->fftN=2;;fastcov->fftN*=2)
	{
		if (fastcov->fftN >= blen+xlen-1)
		{
			break;
		}
	}
	skr_RealFFT_Init(&fastcov->realfft,fastcov->fftN);
	for (i=0;i<blen;i++)
	{
		fastcov->hrfft[i] = b[i];
	}
	for(;i<fastcov->fftN;i++)
	{
		fastcov->hrfft[i] = 0;
	}
	skr_RealFFT_Run2(&fastcov->realfft,fastcov->hrfft,fastcov->fftN);
}
void FastCovAllOut_API(FastCov_allout_s *fastcov,float *out,int *outlen, float *x,int xlen)
{
	float xadd0[MAX_FFT_N];
	int i;
	float themut[MAX_FFT_N];

	for (i=0;i<xlen;i++)
	{
		xadd0[i] = x[i];
	}
	for(;i<fastcov->fftN;i++)
	{
		xadd0[i] = 0;
	}
	skr_RealFFT_Run2(&fastcov->realfft,xadd0,fastcov->fftN);

	rfftmut2(themut,fastcov->hrfft,xadd0,fastcov->fftN);
	skr_InverseRealFFT_Run2(&fastcov->realfft,themut,fastcov->fftN);

	*outlen = fastcov->blen+xlen-1;
	for (i=0;i<*outlen;i++)
	{
		out[i] = themut[i];
	}

}
void FastCovVerylonghReset_API(FastCovVerylongh_s *mfcvl)
{
	int i;

	for (i = 0;i<MAXMADDLSUB1;i++)
	{
		mfcvl->memLaddMsub1[i] = 0;
		mfcvl->memLaddMsub1R[i] = 0;
	}
	mfcvl->memhasinitfft = 0;

}
void FastCovVerylonghCalcu_API(FastCovVerylongh_s *mfcvl,float *h,float *hR,int hlen,int M)
{
	int minfftN;
	int bigfftN;
	int minL;
	int bigL;
	int outsample;
	int i;
	int j;
	int k;
	int hid;
	int memhid;

	mfcvl->hlen = hlen;//hlen>>M
	mfcvl->M = M;

	for (minfftN=2;;minfftN*=2)
	{
		if (minfftN>M)
		{
			break;
		}
	}
	bigfftN = minfftN*2;

	minL = minfftN - M + 1;
	bigL = bigfftN - M + 1; 

	if(abs(minL - M)<abs(bigL - M)||bigL>hlen)
	{
		mfcvl->fftN = minfftN;
	}
	else
	{
		mfcvl->fftN = bigfftN;
	}
	mfcvl->L = mfcvl->fftN - M + 1;//best L

	if (mfcvl->memhasinitfft == 0 || mfcvl->fftN != mfcvl->memhasinitfft)
	{
		mfcvl->memhasinitfft = mfcvl->fftN;
		skr_RealFFT_Init(&mfcvl->realfft,mfcvl->fftN);
	}
	memhid = 0;
	if (hR == NULL)
	{
		for (outsample = 0,mfcvl->subnum = 0; outsample<hlen+M-1; outsample+=mfcvl->L,mfcvl->subnum ++)
		{
			for (i=0,j = mfcvl->subnum*mfcvl->fftN;j<(mfcvl->subnum+1)*mfcvl->fftN;j++,i++)
			{
				hid = i + memhid-(mfcvl->M-1);
				if (hid < hlen && hid >=0)
				{
					mfcvl->subfft[j] = h[hid];
				}
				else
				{
					mfcvl->subfft[j] = 0;
				}
				assert(j<MAXSUBBLEN);
			}
			memhid = hid+1;
			skr_RealFFT_Run2(&mfcvl->realfft,mfcvl->subfft+mfcvl->subnum*mfcvl->fftN,mfcvl->fftN);
		}
	} 
	else
	{
		for (outsample = 0,mfcvl->subnum = 0; outsample<hlen+M-1; outsample+=mfcvl->L,mfcvl->subnum ++)
		{
			for (i=0,j = mfcvl->subnum*mfcvl->fftN;j<(mfcvl->subnum+1)*mfcvl->fftN;j++,i++)
			{
				hid = i + memhid-(mfcvl->M-1);
				if (hid < hlen && hid >=0)
				{
					mfcvl->subfft[j] = h[hid];
					mfcvl->subfftR[j] = hR[hid];
				}
				else
				{
					mfcvl->subfft[j] = 0;
					mfcvl->subfftR[j] = 0;
				}
				assert(j<MAXSUBBLEN);
			}
			memhid = hid+1;
			skr_RealFFT_Run2(&mfcvl->realfft,mfcvl->subfft+mfcvl->subnum*mfcvl->fftN,mfcvl->fftN);
			skr_RealFFT_Run2(&mfcvl->realfft,mfcvl->subfftR+mfcvl->subnum*mfcvl->fftN,mfcvl->fftN);
		}
	}

}
void FastCovVerylonghOneFrame_API(FastCovVerylongh_s *mfcvl,float *allout,short *x,int xlen,int leftorright)
{
	int i;
	float xfft[5000];
	int j;
	float themut[5000];
	int outsample;

	for (i = 0;i<xlen;i++)
	{
		xfft[i] = x[i];
	}
	for (;i<mfcvl->fftN;i++)
	{
		xfft[i] = 0;
	}
	skr_RealFFT_Run2(&mfcvl->realfft,xfft,mfcvl->fftN);
	if (leftorright == 0)
	{
		for (i = 0;i<mfcvl->subnum;i++)
		{
			rfftmut2(themut,xfft,mfcvl->subfft+i*mfcvl->fftN,mfcvl->fftN);
			skr_InverseRealFFT_Run2(&mfcvl->realfft,themut,mfcvl->fftN);
			for (j = 0;j<mfcvl->L;j++)
			{
				allout[j+i*mfcvl->L] = themut[mfcvl->M - 1 + j];//if allout[i] != 0,i>=L+M-1,wrong
			}
		}
	} 
	else if(leftorright == 1)
	{
		for (i = 0;i<mfcvl->subnum;i++)
		{
			rfftmut2(themut,xfft,mfcvl->subfftR+i*mfcvl->fftN,mfcvl->fftN);
			skr_InverseRealFFT_Run2(&mfcvl->realfft,themut,mfcvl->fftN);
			for (j = 0;j<mfcvl->L;j++)
			{
				allout[j+i*mfcvl->L] = themut[mfcvl->M - 1 + j];//if allout[i] != 0,i>=L+M-1,wrong
			}
		}
	}
	else
	{
		assert(0);
	}

}
void FastCovVerylonghOneFrame(FastCovVerylongh_s *mfcvl,float *allout,float *xfft,float *subhfft)
{
	int i;
	int j;
	float themut[5000];

	for (i = 0;i<mfcvl->subnum;i++)
	{
		rfftmut2(themut,xfft,subhfft+i*mfcvl->fftN,mfcvl->fftN);
		skr_InverseRealFFT_Run2(&mfcvl->realfft,themut,mfcvl->fftN);
		for (j = 0;j<mfcvl->L;j++)
		{
			allout[j+i*mfcvl->L] = themut[mfcvl->M - 1 + j];
		}
	}
}
void FastCovVerylonghRun_API(FastCovVerylongh_s *mfcvl,short *x,int xlen,short *y)
{
	float allout[MAXMADDLSUB1];
	float xfft[5000];
	int i;

	if (mfcvl->channel == 2)
	{
		switch(mfcvl->filtertype)
		{
		case FIR_FFTCONV_XFAN:
			for (i = 0;i<xlen/2;i++)
			{
				xfft[i] = x[i*2+1];
			}
			for (;i<mfcvl->fftN;i++)
			{
				xfft[i] = 0;
			}
			skr_RealFFT_Run2(&mfcvl->realfft,xfft,mfcvl->fftN);

			FastCovVerylonghOneFrame(mfcvl,allout,xfft,mfcvl->subfft);

			for (i=0;i<mfcvl->hlen - 1;i++)
			{
				mfcvl->memLaddMsub1[i] = mfcvl->memLaddMsub1[i+mfcvl->M] + allout[i];
			}
			for(;i<mfcvl->hlen+mfcvl->M-1;i++)
			{
				mfcvl->memLaddMsub1[i] = allout[i];
			}

			for (i = 0;i<xlen/2;i++)
			{
				xfft[i] = x[i*2];
			}
			for (;i<mfcvl->fftN;i++)
			{
				xfft[i] = 0;
			}
			skr_RealFFT_Run2(&mfcvl->realfft,xfft,mfcvl->fftN);
			FastCovVerylonghOneFrame(mfcvl,allout,xfft,mfcvl->subfftR);

			for (i=0;i<mfcvl->hlen - 1;i++)
			{
				mfcvl->memLaddMsub1R[i] = mfcvl->memLaddMsub1R[i+mfcvl->M] + allout[i];
			}
			for(;i<mfcvl->hlen+mfcvl->M-1;i++)
			{
				mfcvl->memLaddMsub1R[i] = allout[i];
			}
			for (i = 0;i<xlen/2;i++)
			{
				y[i*2] = stoshort(mfcvl->memLaddMsub1[i]);
				y[i*2+1] = stoshort(mfcvl->memLaddMsub1R[i]);
			}
			break;
		case FIR_FFTCONV_XSAME:
			for (i = 0;i<xlen/2;i++)
			{
				xfft[i] = x[i*2];
			}
			for (;i<mfcvl->fftN;i++)
			{
				xfft[i] = 0;
			}
			skr_RealFFT_Run2(&mfcvl->realfft,xfft,mfcvl->fftN);

			FastCovVerylonghOneFrame(mfcvl,allout,xfft,mfcvl->subfft);

			for (i=0;i<mfcvl->hlen - 1;i++)
			{
				mfcvl->memLaddMsub1[i] = mfcvl->memLaddMsub1[i+mfcvl->M] + allout[i];
			}
			for(;i<mfcvl->hlen+mfcvl->M-1;i++)
			{
				mfcvl->memLaddMsub1[i] = allout[i];
			}
			FastCovVerylonghOneFrame(mfcvl,allout,xfft,mfcvl->subfftR);

			for (i=0;i<mfcvl->hlen - 1;i++)
			{
				mfcvl->memLaddMsub1R[i] = mfcvl->memLaddMsub1R[i+mfcvl->M] + allout[i];
			}
			for(;i<mfcvl->hlen+mfcvl->M-1;i++)
			{
				mfcvl->memLaddMsub1R[i] = allout[i];
			}
			for (i = 0;i<xlen/2;i++)
			{
				y[i*2] = stoshort(mfcvl->memLaddMsub1[i]);
				y[i*2+1] = stoshort(mfcvl->memLaddMsub1R[i]);
			}
			break;
		case FIR_FFTCONV:
			for (i = 0;i<xlen/2;i++)
			{
				xfft[i] = x[i*2];
			}
			for (;i<mfcvl->fftN;i++)
			{
				xfft[i] = 0;
			}
			skr_RealFFT_Run2(&mfcvl->realfft,xfft,mfcvl->fftN);

			FastCovVerylonghOneFrame(mfcvl,allout,xfft,mfcvl->subfft);

			for (i=0;i<mfcvl->hlen - 1;i++)
			{
				mfcvl->memLaddMsub1[i] = mfcvl->memLaddMsub1[i+mfcvl->M] + allout[i];
			}
			for(;i<mfcvl->hlen+mfcvl->M-1;i++)
			{
				mfcvl->memLaddMsub1[i] = allout[i];
			}

			for (i = 0;i<xlen/2;i++)
			{
				xfft[i] = x[i*2+1];
			}
			for (;i<mfcvl->fftN;i++)
			{
				xfft[i] = 0;
			}
			skr_RealFFT_Run2(&mfcvl->realfft,xfft,mfcvl->fftN);
			FastCovVerylonghOneFrame(mfcvl,allout,xfft,mfcvl->subfftR);

			for (i=0;i<mfcvl->hlen - 1;i++)
			{
				mfcvl->memLaddMsub1R[i] = mfcvl->memLaddMsub1R[i+mfcvl->M] + allout[i];
			}
			for(;i<mfcvl->hlen+mfcvl->M-1;i++)
			{
				mfcvl->memLaddMsub1R[i] = allout[i];
			}
			for (i = 0;i<xlen/2;i++)
			{
				y[i*2] = stoshort(mfcvl->memLaddMsub1[i]);
				y[i*2+1] = stoshort(mfcvl->memLaddMsub1R[i]);
			}
			break;
		}
	} 
	else if(mfcvl->channel == 1)
	{
		switch(mfcvl->filtertype)
		{
		case FIR_FFTCONV:
			FastCovVerylonghOneFrame_API(mfcvl,allout,x,xlen,0);

			for (i=0;i<mfcvl->hlen - 1;i++)
			{
				mfcvl->memLaddMsub1[i] = mfcvl->memLaddMsub1[i+mfcvl->M] + allout[i];
			}
			for(;i<mfcvl->hlen+mfcvl->M-1;i++)
			{
				mfcvl->memLaddMsub1[i] = allout[i];
			}
			for (i = 0;i<xlen;i++)
			{
				y[i] = stoshort(mfcvl->memLaddMsub1[i]);
			}
			break;
		case FIR_FFTCONV_HDIFF:
			for (i = 0;i<xlen;i++)
			{
				xfft[i] = x[i];
			}
			for (;i<mfcvl->fftN;i++)
			{
				xfft[i] = 0;
			}
			skr_RealFFT_Run2(&mfcvl->realfft,xfft,mfcvl->fftN);

			FastCovVerylonghOneFrame(mfcvl,allout,xfft,mfcvl->subfft);

			for (i=0;i<mfcvl->hlen - 1;i++)
			{
				mfcvl->memLaddMsub1[i] = mfcvl->memLaddMsub1[i+mfcvl->M] + allout[i];
			}
			for(;i<mfcvl->hlen+mfcvl->M-1;i++)
			{
				mfcvl->memLaddMsub1[i] = allout[i];
			}
			FastCovVerylonghOneFrame(mfcvl,allout,xfft,mfcvl->subfftR);

			for (i=0;i<mfcvl->hlen - 1;i++)
			{
				mfcvl->memLaddMsub1R[i] = mfcvl->memLaddMsub1R[i+mfcvl->M] + allout[i];
			}
			for(;i<mfcvl->hlen+mfcvl->M-1;i++)
			{
				mfcvl->memLaddMsub1R[i] = allout[i];
			}
			for (i = 0;i<xlen;i++)
			{
				y[i*2] = stoshort(mfcvl->memLaddMsub1[i]);
				y[i*2+1] = stoshort(mfcvl->memLaddMsub1R[i]);
			}
			break;
		}
	}
	else
	{
		assert(0);
	}
}