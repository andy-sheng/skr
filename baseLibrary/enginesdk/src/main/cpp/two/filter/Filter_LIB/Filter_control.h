#ifndef FILTER_CONTROL_H
#define FILTER_CONTROL_H
#include "two/common/defines.h"
#include "two/Delay/Delay_LIB/Delay_SDK_API.h"
#include "two/common/DFTs.h"



#define MAXORDER_F 20000
#define MAXORDER 200
#define MAX_NS 50
#define MAXSHUZHIBLEN 256 //is also MAXSHUZHIORDER + 1;is also windowlen

#ifdef __cplusplus
extern "C"
{
#endif

	typedef struct Filter_memf{
		float meminput[MAXORDER_F+SKR_MAX_FRAME_SAMPLE_MONO];
		double memoutput[MAXORDER_F+SKR_MAX_FRAME_SAMPLE_MONO];

	}FilterIOMEMf;

	typedef struct Filter_memoryf{
		FilterIOMEMf memL;
		double b[MAXORDER_F+1];//j
		double a[MAXORDER_F+1];//j
		int blen;
		int alen;

		//int samplerate;
		int filtertype;
	}Filterf_s;


	
#define IIR_DIRCT1_HSAME (0)
#define IIR_SEC_HSAME (1)

#define FIR_DIRCTCONV_HDIFF (-6)
#define FIR_DIRCTCONV_HSAME (-1)
#define FIR_DIRCTCONV (-3)
#define FIR_FFTCONV (-2)
#define FIR_FFTCONV_XFAN (-7)
#define FIR_FFTCONV_XSAME (-4)
#define FIR_FFTCONV_HDIFF (-5)
#define DATAFILTER (2)

#define MAXORDER_BIG 32768 //reverb need
#define MAX_skrBIG 250

	typedef struct bFilter_mem{
		/*ֱ��I��*/
		short meminput[SKR_MAX_FRAME_SAMPLE_MONO + MAXORDER_BIG];
		int memi;
		int memsumin;
		float memoutput[SKR_MAX_FRAME_SAMPLE_MONO + MAXORDER];
		/*�Ľ׽ڼ���*/
		float mempx[MAX_NS*(4+1)];
		float mempy[MAX_NS*(4+1)];
	}BIGFilterIOMEM;
	
#define FILTERUSESKRFFT (0)	
	typedef struct bFilter_memory{
		BIGFilterIOMEM memL;
		BIGFilterIOMEM memR;
		Buf16_s mBufin;
		Buf16_s mBufout;
#if FILTERUSESKRFFT
		RFFT_s mrfft;
		IRFFT_s mirfft;
#else
		RealFFT_s realfft;
#endif
		float hLrfft[MAXFFTN];
		float hRrfft[MAXFFTN];
		float xLintl[MAXFFTN];
		float xRintl[MAXFFTN];
		float xLhL[MAXFFTN];
		float xRhR[MAXFFTN];
		float xLhL0[MAXFFTN];
		float xRhR0[MAXFFTN];
		int fftN;
		int offset;

		float cmemhLrfft[MAXFFTN];
		float cmemhRrfft[MAXFFTN];

		float cmemb[MAX_skrBIG*(400+1)];
		float cmembR[MAX_skrBIG*(400+1)];
		short cmeminputL[SKR_MAX_FRAME_SAMPLE_MONO + MAXORDER_BIG];
		short cmeminputR[SKR_MAX_FRAME_SAMPLE_MONO + MAXORDER_BIG];
		int cmemfftN;
		int cmemblen;
		int cmemcall;

		float b[MAX_skrBIG*(400+1)];//j
		float a[MAX_NS*(4+1)];//j
		
		int membufrear;


		int blen;
		int alen;
		int Order;
		int ns;
		int n;

		float bR[MAX_skrBIG*(400+1)];//j
		float aR[MAX_NS*(4+1)];//j
		
		
		int bRlen;
		int aRlen;
		int OrderR;
		int nsR;
		int nR;




		/*ָ��matlab�����ı��е�����*/
		
		int chanel;
		//int samplerate;//δʹ��
		//int Bypass;//δʹ��
		int filtertype;//-1:fir,0:iir,1:nsSec,
	}Filterlongfir_s;

	typedef struct FastCov_allout_memory{
		RealFFT_s realfft;

		float hrfft[MAXFFTN];
		int fftN;
		int blen;
	}FastCov_allout_s;

#define MAXMADDLSUB1 (35000) //the max xlen+hlen-1
#define MAXSUBBLEN (70000)//more or less
	typedef struct fastcovnodelay{
		//Buf16_s too large 
		RealFFT_s realfft;
		float subfft[MAXSUBBLEN];
		float subfftR[MAXSUBBLEN];
		int subnum;
		int fftN;
		int memhasinitfft;

		float memLaddMsub1[MAXMADDLSUB1];
		float memLaddMsub1R[MAXMADDLSUB1];
		
		int L;//sublen of hlen
		int M;//x's framelen
		int hlen;
		int channel;
		int filtertype;
	}FastCovVerylongh_s;

	//typedef struct oadd{
	//	float memLaddMsub1[MAXMADDLSUB1];
	//}overlapadd;




#ifdef __cplusplus
}
#endif

#endif