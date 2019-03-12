//how to use
//mEcho.channel = channel;
//mEcho.samplerate = samplerate;
//setBMB(&mEcho);
//EchoReset_API(&mEcho);
//EchoCalcu_API(&mEcho);
//
//EchoRun_API(&mEcho, in, framelen, out);

#ifndef EFFECT_SDK_API_H
#define EFFECT_SDK_API_H

#include "effect_control.h"

#ifdef __cplusplus
extern "C"
{
#endif
	
	void EchoReset_API(Echo_s *mEcho);
	void EchoCalcu_API(Echo_s *mEcho);
	void EchoRun_API(Echo_s *mEcho,short *input,int inlen,	short *output);
	
	void setBMB(Echo_s *mEcho);
#ifdef __cplusplus
}
#endif

#endif