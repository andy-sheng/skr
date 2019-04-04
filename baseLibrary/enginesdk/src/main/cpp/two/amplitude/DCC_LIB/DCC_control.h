#ifndef DCC_CONTROL_H
#define DCC_CONTROL_H

#ifdef __cplusplus
extern "C"
{
#endif
	typedef struct DCC_channel_memory{
		float memLaverge[1];//一阶AR
		float memRaverge[1];

		int winlen;//V1:一帧一个均值，先不用此量
		
		int Chanel;
		int samplerate;//V1:一帧一个均值，先不用此量
	}DCC_ID;

#ifdef __cplusplus
}
#endif

#endif