#ifndef DCC_CONTROL_H
#define DCC_CONTROL_H

#ifdef __cplusplus
extern "C"
{
#endif
	typedef struct DCC_channel_memory{
		float memLaverge[1];//һ��AR
		float memRaverge[1];

		int winlen;//V1:һ֡һ����ֵ���Ȳ��ô���
		
		int Chanel;
		int samplerate;//V1:һ֡һ����ֵ���Ȳ��ô���
	}DCC_ID;

#ifdef __cplusplus
}
#endif

#endif