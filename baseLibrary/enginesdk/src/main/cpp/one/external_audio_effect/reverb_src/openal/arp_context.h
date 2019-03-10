
#ifndef _ARP_CONTEXT_H_
#define _ARP_CONTEXT_H_

#include <stdint.h>
#include "arp_effect_const.h"

class Arp_Reverb;

class Arp_Context
{
public:
	Arp_Context();
	~Arp_Context();

public:

	int32_t Arp_Ctx_Init(int32_t nQcChannel, int32_t nBjChannel, int32_t nFreq, int32_t nPrestID, float vocalRate, float accRate);

	int32_t Arp_Ctx_ResetID(int32_t nNewID, float vocalRate, float accRate);

	int32_t Arp_Ctx_Mixing_OLD( int16_t * psQcData, int16_t * psBjData, int32_t nInSampleNum, int16_t * psOutData, int32_t *pnOutSampleNum );

	int32_t Arp_Ctx_Mixing(int16_t * psQcData,  int16_t * psBjData,	 int32_t nInSampleNum,int16_t *pOut);

	int32_t Arp_Ctx_Uninit();

private:

	// 混音函数
	void	 mix_s16(int16_t *pQCData, int16_t *pBJData, int32_t nSampleNum, int16_t *pOutData);
	// 单通道变双通道，sample_num为单通道采样点数
	void	 mono2Stereo(int16_t *psInData, int32_t sample_num, int16_t * psOutData);

private:
	int32_t m_nQcChannel;
	int32_t m_nBjChannel;
	float	m_fvocalRate;
	float	m_faccRate;
	int32_t m_nNewId;
	int32_t m_nChannelMax;

	int32_t m_nLeft;
	int32_t m_nLeft_Last;
	int32_t m_nPos;

	int32_t m_nInit;


	//float    m_pfQcBuffer[ARP_EFFECT_BUFFERSIZE * 2];
	int16_t  m_psQcBuffer[ARP_EFFECT_BUFFERSIZE * 2];
	int16_t  m_psBjBuffer[ARP_EFFECT_BUFFERSIZE * 2];

	Arp_Reverb * m_pReverb;
};

#endif//_ARP_CONTEXT_H_
