#ifndef DFTS_H
#define DFTS_H

#ifdef __cplusplus
extern "C"
{
#endif


#define MAXFFTN (65536)//2^16 


	typedef struct RFFT_st{
		int m;
		float cc[21][MAXFFTN];//log2(MAXFFTN) = 16, so 17 is ok....
		float ss[21][MAXFFTN];
	}RFFT_s;

	typedef struct IRFFT_st{
		int m;
		float cc[21][MAXFFTN];
		float ss[21][MAXFFTN];
		float cc3[21][MAXFFTN];
		float ss3[21][MAXFFTN];
		float sqrt1_2;
	}IRFFT_s;

#define MAX_FFT_N (65536) 


	//if the FFT-Len is equal,you can use only one "RealFFT_s"
	typedef struct RealFFT_st{
		int N;
		int fft_ip[(MAX_FFT_N>>3)];//32*4
		float fft_w[(MAX_FFT_N>>1)];//128*4
		float scale;
	}RealFFT_s;

	//void skrrdft_init(int n, int *ip, float *w);
	//void skrrdft(int, int, float *, int *, float *);
	//void skrcdft(int, int, float *, int *, float *);

	short skr_RealFFT_Init(RealFFT_s *sFFT_s,int FftLen);


	//	note1:real and image part--- r0,r(N/2),r1,i1,r2,i2,...,r(N/2-1),i(N/2-1);
	short skr_RealFFT_Run(RealFFT_s *sFFT_s,float* s, float* X, int FftLen);//s will not be changed
	short skr_RealFFT_Run2(RealFFT_s *sFFT_s,float* s,int FftLen);//s will be changed

	short skr_InverseRealFFT_Run(RealFFT_s *sFFT_s,float* X, float* s, int IfftLen);//X will not be changed
	short skr_InverseRealFFT_Run2(RealFFT_s *sFFT_s,float* X,int FftLen);//X will be changed
	void skr_FFTPowerSpectrum(float *X, int FftLen, float *lambda_X, int M21);//M21=N/2+1

	void skr_FFToutToXk(float *Re,float *Im,float *X,int FftLen);
	void skr_XkToFFTout(float *X,float *Re,float *Im,int FftLen);
	void skr_FFToutToXkN(float *Re,float *Im,float *X,int FftLen);



#ifdef __cplusplus
}
#endif

#endif