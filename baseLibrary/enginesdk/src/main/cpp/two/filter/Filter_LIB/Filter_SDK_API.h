#ifndef FILTER_SDK_API_H
#define FILTER_SDK_API_H

#include "Filter_control.h"

#ifdef __cplusplus
extern "C"
{
#endif

	void FilterfReset_API(Filterf_s *mFilter);
	void FilterfRun_API(Filterf_s *mFilter,float *input,int inLen,float *output);
	void FilterfRun_API_shortin(Filterf_s *mFilter,short *input,int inLen,float *output);
		
	void FilterlongfirReset_API(Filterlongfir_s *mFilter);
	void FilterlongfirCalcu_APItime(Filterlongfir_s *mFilter);
	void FilterlongfirCalcu_API_forFFT(Filterlongfir_s *mFilter);
	void FilterlongfirRun_API(Filterlongfir_s *mFilter,short *input,int inLen,short *output);
	
	void FastCovAllOutInit_API(FastCov_allout_s *fastcov,float *b,int blen,int xlen);
	void FastCovAllOut_API(FastCov_allout_s *fastcov,float *out,int *outlen, float *x,int xlen);
	
	void FastCovVerylonghReset_API(FastCovVerylongh_s *mfcvl);
	void FastCovVerylonghCalcu_API(FastCovVerylongh_s *mfcvl,float *h,float *hR,int hlen,int M);
	void FastCovVerylonghOneFrame_API(FastCovVerylongh_s *mfcvl,float *allout,short *x,int xlen,int leftorright);
	void FastCovVerylonghRun_API(FastCovVerylongh_s *mfcvl,short *x,int xlen,short *y);
#ifdef __cplusplus
}
#endif

#endif