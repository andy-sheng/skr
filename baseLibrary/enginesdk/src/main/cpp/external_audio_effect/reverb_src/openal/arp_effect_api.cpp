
#include "arp_effect_api.h"
#include "arp_context.h"

int32_t arp_effect_create( void ** ppInst, int32_t vocalChannels, int32_t accChannels, int32_t freq, int32_t prest_id, float vocalRate, float accRate)
{
	if ( *ppInst != NULL)
	{
		return ARP_EFFECT_INST_INITED;
	}
	Arp_Context * pCtx = new Arp_Context();
	int32_t nRet =  pCtx->Arp_Ctx_Init(vocalChannels, accChannels, freq, prest_id, vocalRate, accRate);
	if ( nRet != ARP_EFFECT_SUCCESS)
	{
		return nRet;
	}
	*ppInst = pCtx;
	return ARP_EFFECT_SUCCESS;
}

int32_t arp_effect_reset( void * pInst, int32_t new_id , float vocalRate, float accRate)
{
	if ( pInst == NULL )
	{
		return ARP_EFFECT_INST_UNINIT;
	}
	Arp_Context * pCtx = (Arp_Context *)pInst;
	return pCtx->Arp_Ctx_ResetID(new_id, vocalRate, accRate );
}

int32_t arp_effect_mix(void  * pInst, int16_t * vocal, int16_t * accompany, int32_t sample_in_num,int16_t *pOut)
{
	//int32_t nRet = ARP_EFFECT_SUCCESS;
	if ( pInst == NULL )
	{
		return ARP_EFFECT_INST_UNINIT;
	}
	if (vocal == NULL || accompany == NULL)
	{
		return ARP_EFFECT_POINT_NULL;
	}
	if (sample_in_num < 0)
	{
		return ARP_EFFECT_LEN_NEGATIVE;
	}
	Arp_Context * pCtx = (Arp_Context *)pInst;
	return pCtx->Arp_Ctx_Mixing(vocal, accompany, sample_in_num,pOut);
}

int32_t arp_effect_destory( void ** ppInst )
{
	if ( *ppInst == NULL)
	{
		return ARP_EFFECT_INST_UNINIT;
	}
	Arp_Context * pCtx = (Arp_Context *)( *ppInst );
	int32_t nRet = pCtx ->Arp_Ctx_Uninit();
	if (nRet != ARP_EFFECT_SUCCESS)
	{
		return nRet;
	}
	delete pCtx;
	*ppInst = NULL;
	return ARP_EFFECT_SUCCESS;
}
