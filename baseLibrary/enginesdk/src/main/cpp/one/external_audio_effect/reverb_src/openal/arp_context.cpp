
#include "arp_context.h"
#include "arp_reverb.h"
#include "arp_effect_error.h"
#include <stdlib.h>

#include <string.h>

#define arp_min(a,b) (((a) < (b)) ? (a) : (b))
#define arp_max(a,b) (((a) > (b)) ? (a) : (b))
int32_t K[5] = {0,28672,32256,32704,32760};

Arp_Context::Arp_Context()
	:m_nQcChannel(1)
	,m_nBjChannel(1)
	,m_nNewId(0)
	,m_nChannelMax(1)
	,m_nLeft(0)
	,m_nLeft_Last(0)
	,m_nPos(0)
	,m_nInit(0)
	,m_pReverb(NULL)
	,m_faccRate(0.0f)
	,m_fvocalRate(0.0f)
{

}

int32_t Arp_Context::Arp_Ctx_Init( int32_t nQcChannel, int32_t nBjChannel, int32_t frequency, int32_t nPresetId
									, float vocalRate, float accRate)
{
	if (m_nInit != 0)
	{
		return ARP_EFFECT_INST_INITED;
	}
	int32_t nRet = ARP_EFFECT_SUCCESS;
	if( nQcChannel != ARP_EFFECT_MONO && nQcChannel != ARP_EFFECT_STEREO )
	{
		return ARP_EFFECT_NOT_SUPPORT_CHANNEL;
	}
	if( nBjChannel != ARP_EFFECT_MONO && nBjChannel != ARP_EFFECT_STEREO )
	{
		return ARP_EFFECT_NOT_SUPPORT_CHANNEL;
	}
// 	if(	   frequency != ARP_EFFECT_8K
// 		&& frequency != ARP_EFFECT_16K
// 		&& frequency != ARP_EFFECT_22K
// 		&& frequency != ARP_EFFECT_32K
// 		&& frequency != ARP_EFFECT_44K
// 		&& frequency != ARP_EFFECT_48K)
	if (frequency != ARP_EFFECT_44K)
	{
		return ARP_EFFECT_NOT_SUPPORT_FREQUENCY;
	}
	if (nPresetId < 0	 || nPresetId >= ARP_EFFECT_ID_MAX)
	{
		return ARP_EFFECT_NOT_SUPPORT_EFFECT_ID;
	}
	if ( vocalRate < 0	 || vocalRate > ARP_MIX_MAX_RATE)
	{
		return ARP_EFFECT_NOT_SUPPORT_RATE;
	}
	if ( accRate < 0	 || accRate > ARP_MIX_MAX_RATE)
	{
		return ARP_EFFECT_NOT_SUPPORT_RATE;
	}

	m_nQcChannel = nQcChannel;
	m_nBjChannel = nBjChannel;
	m_fvocalRate = vocalRate;
	m_faccRate	 = accRate;
	m_nChannelMax = arp_max(m_nQcChannel, m_nBjChannel);

	m_pReverb	= new Arp_Reverb;
	nRet = m_pReverb->Init(nQcChannel, frequency, nPresetId);
	if (nRet != ARP_EFFECT_SUCCESS)
	{
		if (m_pReverb)
		{
			delete m_pReverb;
			m_pReverb = NULL;
		}
		return nRet;
	}


	m_nInit = 1;
	return nRet;
}

int32_t Arp_Context::Arp_Ctx_ResetID( int32_t nNewID, float vocalRate, float accRate )
{
	if (m_nInit != 1)
	{
		return ARP_EFFECT_INST_UNINIT;
	}
	if (nNewID < 0		 || nNewID >= ARP_EFFECT_ID_MAX )
	{
		return ARP_EFFECT_NOT_SUPPORT_EFFECT_ID;
	}
	if ( vocalRate < 0	 || vocalRate > ARP_MIX_MAX_RATE)
	{
		return ARP_EFFECT_NOT_SUPPORT_RATE;
	}
	if ( accRate < 0	 || accRate > ARP_MIX_MAX_RATE)
	{
		return ARP_EFFECT_NOT_SUPPORT_RATE;
	}
	m_fvocalRate = vocalRate;
	m_faccRate	 = accRate;
	return m_pReverb->ResetEffectID(nNewID);
}

//static void Float2Short( int16_t  * psDataOut, int32_t SampleNum,  float * pfDataIn)
//{
//	for (int32_t i = 0; i < SampleNum; i++)
//	{
//		int32_t nValue = (int32_t)(pfDataIn[i] * 32768.0f);
//		if (nValue >  32767) nValue = 32767;
//		if (nValue < -32768) nValue =-32768;
//		psDataOut[i] = nValue;
//	}
//}

//static void Short2Float(float * pfDataOut, int32_t SampleNum,  int16_t* psDataIn)
//{
//	for (int32_t i = 0; i < SampleNum; i++)
//	{
//		float fValue  = float (psDataIn[i] / 32768.0);
//		pfDataOut[i] = fValue;
//	}
// 	FILE *fp = fopen("b_r_f.pcm", "ab+");
// 	fwrite(pfDataOut, 4, 1024, fp);
// 	fclose(fp);
//}


int32_t Arp_Context::Arp_Ctx_Mixing( int16_t * psQcData, int16_t * psBjData, int32_t nInSampleNum, int16_t *pOut )
{
	if (m_nInit != 1)
	{
		return ARP_EFFECT_INST_UNINIT;
	}

	int32_t nRevebTimes  = nInSampleNum /  ARP_EFFECT_BUFFERSIZE;
	if (nInSampleNum - ARP_EFFECT_BUFFERSIZE * nRevebTimes > 0)
	{
		nRevebTimes++;
	}

	int32_t nCurrentUse  = 0;
	m_nPos			= 0;
	int16_t *pQcMix = NULL;
	int16_t *pBjMix = NULL;

	for (int32_t i  = 0; i < nRevebTimes; i++)
	{
		nCurrentUse =  arp_min( ARP_EFFECT_BUFFERSIZE, nInSampleNum -  m_nPos);
		// 清唱处理
		pQcMix = psQcData + m_nPos * m_nQcChannel;
		m_pReverb->Process_set((char*)pQcMix, nCurrentUse * m_nQcChannel);
		if (m_nQcChannel == 1 && m_nBjChannel == 2)
		{
			mono2Stereo(pQcMix, nCurrentUse, m_psQcBuffer);
			pQcMix = m_psQcBuffer;
		}
		// 背景处理
		pBjMix = psBjData+ m_nPos * m_nBjChannel;
		if (m_nQcChannel == 2 && m_nBjChannel == 1 )
		{
			mono2Stereo(pBjMix, nCurrentUse, m_psBjBuffer);
			pBjMix = m_psBjBuffer;
		}
		// 混音处理
		mix_s16(pQcMix, pBjMix, nCurrentUse * m_nChannelMax, pOut + m_nPos * m_nChannelMax);
		m_nPos     +=  nCurrentUse  ;
	}
	return ARP_EFFECT_SUCCESS;
}


void Arp_Context::mix_s16(int16_t *pQCData, int16_t *pBJData, int32_t nSampleNum, int16_t *pOutData)
{
	int32_t nSum = 0, pNSum;
	int32_t nj = 0, cj = 0, dj = 0;
	for(int32_t i = 0; i < nSampleNum; i++)
	{
		nSum = (int32_t)( float(pQCData[i])* m_fvocalRate + float(pBJData[i]) * m_faccRate);
		pNSum = abs(nSum);
		nj = arp_min(pNSum >> 15, 4);
		cj = pNSum & 32767;
		dj = (cj << 2) + (cj << 1) + cj;
		pOutData[i] = K[nj] + (dj >> ((nj << 1) + nj + 3));
		if(nSum < 0)
		{
			pOutData[i] = -pOutData[i];
		}
	}
}

void Arp_Context::mono2Stereo( int16_t *psInData, int32_t sample_num, int16_t * psOutData )
{
	int32_t temp = 0;
	for ( int32_t i = sample_num - 1; i >=  0; i-- )
	{
		temp = 2*i;
		psOutData[ temp ] = psOutData[ temp+1 ] = psInData[i];
	}
}

int32_t Arp_Context::Arp_Ctx_Uninit()
{
	if (m_nInit != 1)
	{
		return ARP_EFFECT_INST_UNINIT;
	}
	if (m_pReverb)
	{
		m_pReverb->Uninit();
		delete m_pReverb;
		m_pReverb = NULL;
	}
	m_nInit = 0;
	return ARP_EFFECT_SUCCESS;
}

Arp_Context::~Arp_Context()
{

}
