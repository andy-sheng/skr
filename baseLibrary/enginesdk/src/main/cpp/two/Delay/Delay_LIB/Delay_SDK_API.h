/////////////////////////////////better than adapt's framing_LIB(lower delay,easier understand)/////////////////////////////////////////
#ifndef DELAY_SDK_API_H
#define DELAY_SDK_API_H

#include "Delay_control.h"

#ifdef __cplusplus
extern "C"
{
#endif

	void BufresetAPI(Buf16_s *buf16);
	void putinAPI(Buf16_s *buf16,short *input,int inlen);
	void putinAPI_loop(Buf16_s *buf16,short *input,int inlen);
	void putinAPI_loop_onlychannelLin(Buf16_s *buf16,short *input,int inlen);
	void putin_iNormalizeAPI(Buf16_s *buf16,float *input,int inlen);
	void putin_iNormalizeAPI_loop(Buf16_s *buf16,float *input,int inlen);
	int putoutAPI(Buf16_s *buf16,short *output,int outlen);//if no data output 0
	int putoutAPI_loop(Buf16_s *buf16,short *output,int outlen);//if no data output 0
	void putoutAPI_onlymove(Buf16_s *buf16,int outlen);
	void putoutAPI_onlymove_loop(Buf16_s *buf16,int outlen);

	int queuehave_API(Buf16_s *buf16);
	int fastmoveAPI_loop(Buf16_s *buf16,int move);
	int insert0frontAPI_loop(Buf16_s *buf16,int n);
	int insert0behindAPI_loop(Buf16_s *buf16,int n);


	int BufDelayRun_API(Buf16_s *buf16,short *input,int inlen,	short *output);//in and then out
	int putoutAPI_ForReframe(Buf16_s *buf16,short *output,int outlen);//if not len enough for outlen return 0 and donothing
	int putoutAPI_ForReframe_0(Buf16_s *buf16,short *output,int outlen);//if not len enough for outlen return 0 and output 0

	int BufDelayRun_API_loop(Buf16_s *buf16,short *input,int inlen,	short *output);//in and then out
	int putoutAPI_ForReframe_loop(Buf16_s *buf16,short *output,int outlen);//if not len enough for outlen return 0 and donothing
	int putoutAPI_ForReframe_0_loop(Buf16_s *buf16,short *output,int outlen);//if not len enough for outlen return 0 and output 0


#ifdef __cplusplus
}
#endif

#endif