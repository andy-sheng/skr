
//mcovreverb->channelin = channel;
//mcovreverb->channelout = channel;
//mcovreverb->samplerate = samplerate;
//mcovreverb->reverbkind = 9;----------------CD 11
//mcovreverb->xsame = 0;
//mcovreverb->wet = 1;
//mcovreverb->xframelen = nLen;
//CovReverbV2Reset_API(mcovreverb);
//CovReverbV2Calcu_API(mcovreverb);
//
//CovReverbV2Run_API(mcovreverb, x1, framelen, y);

#ifndef COVREVERB_SDK_API_H
#define COVREVERB_SDK_API_H

#include "CovReverb_control.h"

#ifdef __cplusplus
extern "C"
{
#endif

	void CovReverbReset_API(CovReverb_s *mReverb);
	void CovReverbCalcu_API(CovReverb_s *mReverb);
	void CovReverbRun_API(CovReverb_s *mReverb,short *input,int inlen,	short *output);

	void CovReverbV2Reset_API(CovReverbV2_s *mReverb);
	void CovReverbV2Calcu_API(CovReverbV2_s *mReverb);
	void CovReverbV2Run_API(CovReverbV2_s *mReverb,short *input,int inlen,	short *output);

#ifdef __cplusplus
}
#endif

#endif