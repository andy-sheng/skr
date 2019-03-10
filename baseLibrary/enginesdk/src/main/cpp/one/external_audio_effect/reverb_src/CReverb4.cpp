#include <float.h>
#include "reverb_inc/CReverb4.h"
#include "reverb_src/openal/arp_reverb.h"
#include "reverb_inc/CReverb.h"
#include "reverb_inc/MSdcommon.h"

#define MAX_VERB_VALUE	19
#define MIN_VERB_VALUE	10
#define MAX_NUMBER_OF_VERB (MAX_VERB_VALUE -MIN_VERB_VALUE +1)
#define REQUEST_BUFFER_SIZE 256

#define LEFT_CHANGE_PERIOD (m_sample_rate * 1.5) //3s
#define RIGHT_CHANGE_PERIOD (m_sample_rate * 1.5) //2s
#define LINE_LENGTH_PERIOD (m_sample_rate * 1.5)


const char g_p_verb_id_names[MAX_NUMBER_OF_VERB][64] = {
	{"no effect"},
	{"ktv"},
	{"wennuan"},
	{"cixing"},
	{"kongling"},
	{"youyuan"},
	{"mihuan"},
	{"laochangpian"},
	{"nouse"},
	{"distant"}
};
CReverb4::CReverb4()
{
	m_pTmpAudio = NULL;
	m_nTmpAudioLength = 0;
}

CReverb4::~CReverb4()
{
	Uninit();
}

int CReverb4::Init(int sampeRate, int channel)
{
	int ret;
	m_channel = channel;
	m_sample_rate = sampeRate;
	m_index_left = LEFT_CHANGE_PERIOD*0.15;
	m_index_right = 0;
	m_line_num = LINE_LENGTH_PERIOD;
	m_old_reverb = NULL;
	m_openal_reverb = NULL;

	/* include Creverb */
	CReverb* poldverb = new(std::nothrow) CReverb;
	if (poldverb == NULL)
	{
		return err_kala_audio_base_h_malloc_null;
	}
	ret = poldverb->Init(sampeRate, channel);
	if (ret<0)
	{
		return ret;
	}

	poldverb->SetTypeId(KALA_VB_KTV_V40_QUICKLY);
	m_old_reverb = (void*)poldverb;	

	/* phonograhp here */
	ret = m_phonograph.Init(sampeRate,channel);
	if (ret<0)
	{
		poldverb->Uninit();
		delete poldverb;
		return ret;
	}

	/* LPF and HPF filters here */
	ret = m_filters.Init(sampeRate,channel);
	if (ret<0)
	{
		poldverb->Uninit();
		delete poldverb;
		m_phonograph.Uninit();
		return ret;
	}
    m_filters.setFilterType(LOW_PASS_FILTER);

	/* openal here*/
	Arp_Reverb *popenalverb = new(std::nothrow) Arp_Reverb;
	if (popenalverb == NULL)
	{
		poldverb->Uninit();
		delete poldverb;
		m_old_reverb = NULL;
		m_phonograph.Uninit();
		m_filters.Uninit();
		return err_kala_audio_base_h_malloc_null;
	}
	ret = popenalverb->Init(channel, sampeRate, 0);
	if (ret < 0)
	{
		poldverb->Uninit();
		delete poldverb;
		m_old_reverb = NULL;
		m_phonograph.Uninit();
		m_filters.Uninit();
		
		return ret;
	}
	m_openal_reverb = (void	*)popenalverb;

	ret = m_slow_flanging.Init(channel, sampeRate);
	if (ret != 0)
	{
		poldverb->Uninit();
		delete poldverb;
		m_old_reverb = NULL;
		m_phonograph.Uninit();
		m_filters.Uninit();
		popenalverb->Uninit();
		delete popenalverb;
		m_openal_reverb = NULL;
		return ret;
	}

	return 0;

}

void CReverb4::GetIdRange(int* maxVal, int* minVal)
{
	CReverb* poldVerb = (CReverb*)m_old_reverb;
	return poldVerb->GetIdRange(maxVal,minVal);
}

int CReverb4::GetIdDefault()
{
	CReverb* poldVerb = (CReverb*)m_old_reverb;
	return poldVerb->GetIdDefault();
}

int CReverb4::SetTypeId(int typeID)
{
	if (typeID < MIN_VERB_VALUE)
	{
		// old verb
		if (typeID == 0)
		{
			typeID = KALA_VB_NO_EFFECT_QUICKLY;
		}
		else if ((typeID == 1) || (typeID == 4))
		{
			typeID = KALA_VB_KTV_V40_QUICKLY;	// map verb v1.5 to this verison, ktv ---> ktv
		}
		else if ((typeID == 2) || (typeID == 5))
		{
			typeID = KALA_VB_ETHEREAL_QUICKLY;	// map verb v1.5 to this verison, yan chang hui  ---> ethereal
		}
		else if (typeID == 3 || typeID == 6)
		{
			typeID = KALA_VB_DISTANT_QUICKLY;   // map verb v1.5 to this verison, juchang ---> distant
		}
		else
		{
			typeID = KALA_VB_NO_EFFECT_QUICKLY;
		}
	}
	else
	{
		if (typeID > KALA_VB_EFFECT_MAX)
		{
			typeID = KALA_VB_EFFECT_MAX;
		}
	}

	m_id = typeID;

	if (m_id == KALA_VB_DISTANT_QUICKLY)
	{
		CReverb* poldVerb = (CReverb*)m_old_reverb;
		return poldVerb->SetTypeId(KALA_VB_NEW_CONCERT);
	}
	else if(m_id == KALA_VB_OLD_DISTANT_QUICKLY)
	{
		CReverb* poldVerb = (CReverb*)m_old_reverb;
		return poldVerb->SetTypeId(KALA_VB_ID_15);
	}
	else if (m_id == KALA_VB_KTV_V40_QUICKLY)
	{
		CReverb* poldVerb = (CReverb*)m_old_reverb;
		return poldVerb->SetTypeId(KALA_VB_KTV_V40_QUICKLY);
	}
	else if (m_id == KALA_VB_KARAOKE_QUICKLY)
	{
		CReverb* poldVerb = (CReverb*)m_old_reverb;
		return poldVerb->SetTypeId(KALA_VB_ID_18);
	}
	else
	{
		// 	Creverb* poldVerb = (Creverb*)m_old_reverb;
		// 	return poldVerb->SetTypeId(typeID);
		Arp_Reverb* popenalVerb = (Arp_Reverb*)m_openal_reverb;

		return popenalVerb->ResetEffectID(typeID - MIN_VERB_VALUE);
	}
}

int  CReverb4::SetRoomSize(float roomSize)
{
	CReverb* poldVerb = (CReverb*)m_old_reverb;
	if (poldVerb == NULL)
	{
		return err_kala_audio_base_h_null;
	}
	poldVerb->SetRoomSize(roomSize);
	return 0;
}

float  CReverb4::GetRoomSize()
{
	CReverb* poldVerb = (CReverb*)m_old_reverb;
	return poldVerb->GetRoomSize();
}

int  CReverb4::SetWet(float wet)
{
	CReverb* poldVerb = (CReverb*)m_old_reverb;
	if (poldVerb == NULL)
	{
		return err_kala_audio_base_h_null;
	}
	poldVerb->SetWet(wet);
	return 0;
}

float  CReverb4::GetWet()
{
	CReverb* poldVerb = (CReverb*)m_old_reverb;
	return poldVerb->GetWet();
}

int CReverb4::GetTypeId()
{
	return  m_id;
}

char* CReverb4::GetNameById(int typeID)
{
	if (typeID < KALA_VB_NO_EFFECT_QUICKLY )
	{
		typeID = KALA_VB_NO_EFFECT_QUICKLY;
	}
	else if (typeID > KALA_VB_EFFECT_MAX )
	{
		typeID = KALA_VB_EFFECT_MAX;
	}
	return (char*)g_p_verb_id_names[typeID - MIN_VERB_VALUE];
}

int CReverb4::GetLatence()
{
	return 0;
}

int CReverb4::FadeLeftRight(char* inBuffer, int inSize)
{

	if(m_channel != 2 ) //one channel not process
		return inSize;

	if (inSize % 4 != 0)
		return err_kala_audio_base_h_param_invalid;

	//get float data
	float *databuffer = (float*)malloc(inSize/2*sizeof(float));
	if(databuffer == NULL)
	{
		return err_kala_audio_base_h_malloc_null;
	}
	
	for (int i = 0; i < inSize/2; i++)
	{
		databuffer[i] = *((short*)inBuffer+i) >= 0 ? (float)(*((short*)inBuffer+i)) / 32767.0f : (float)(*((short*)inBuffer+i)) / 32768.0f;
	}

	for (int i = 0; i < inSize/2; i+=2)
	{
		if (m_index_left >= LEFT_CHANGE_PERIOD) 
		{
			m_index_left = 0;
		}
		
		if (m_index_right >= RIGHT_CHANGE_PERIOD)
		{
			m_index_right = 0;
		}
		
		float rdx_left = 0.5*cos((double)2*m_index_left*3.1415926/(double)((LEFT_CHANGE_PERIOD-1)));
		float rdx_right = 0.5*cos((double)2*m_index_right*3.1415926/(double)((RIGHT_CHANGE_PERIOD-1)));
		databuffer[i] = rdx_left > 0 ?  (float)databuffer[i]* (-rdx_left + 1) : (float)databuffer[i] * (rdx_left + 1);//((cos((float)m_index*3.1415926f/(float)((LFO_BUFFER_LEN-1))))*(cos((float)m_index*3.1415926f/(float)((LFO_BUFFER_LEN-1)))))
		databuffer[i+1] = rdx_right < 0 ? (float)databuffer[i+1] * (rdx_right+1) : (float)databuffer[i+1] * (-rdx_right + 1);  //((cos((float)m_index*3.1415926f/(float)((LFO_BUFFER_LEN-1))))*(cos((float)m_index*3.1415926f/(float)((LFO_BUFFER_LEN-1)))))
		
		if (m_index_left == 0 || m_index_right == 0 || m_index_left == LEFT_CHANGE_PERIOD / 2 || m_index_right == RIGHT_CHANGE_PERIOD / 2)
		{
			if (m_line_num == 0)
			{
				m_line_num = LINE_LENGTH_PERIOD;
			}
			else
				m_line_num--;

		}
		
		if (m_line_num == LINE_LENGTH_PERIOD)
		{
			m_index_left++;
			m_index_right++;
		}
		
		
	}
	for (int i = 0; i < inSize/2; i++)
	{
		*((short*)inBuffer+i) = databuffer[i] >= 0 ? (short)(databuffer[i]*32767.0f) : (short)(databuffer[i]*32768.0f);
	}
	
	if (databuffer != NULL)
	{
		free(databuffer);
		databuffer = NULL;
	}
	
	return inSize;
}

int CReverb4::ProcessLRIndependent(float * inLeft, float * inRight, float * outLeft, float * outRight, int inOutSize)
{
	/* phonograh effect*/
	if (m_id == KALA_VB_PHONOGRAPH_QUICKLY)
	{
		memcpy(outLeft, inLeft, inOutSize * sizeof(float));
		memcpy(outRight, inRight, inOutSize * sizeof(float));
		return m_phonograph.ProcessLRIndependent(outLeft, outRight, inOutSize);
	}
	else if (m_id == KALA_VB_KTV_V40_QUICKLY || m_id == KALA_VB_DISTANT_QUICKLY
			 || m_id == KALA_VB_OLD_DISTANT_QUICKLY || m_id == KALA_VB_KARAOKE_QUICKLY)
	{
		// left old ktv effect here.
		CReverb* poldverb = (CReverb*)m_old_reverb;
		return poldverb->ProcessLRIndependent(inLeft, inRight, outLeft, outRight, inOutSize);

	}
	else if (m_id == KALA_VB_WARM_QUICKLY)
	{
		memcpy(outLeft, inLeft, inOutSize * sizeof(float));
		memcpy(outRight, inRight, inOutSize * sizeof(float));
		m_filters.ProcessLRIndependent(outLeft, outRight, inOutSize);

		if (m_nTmpAudioLength < inOutSize * 2)
		{
			if (m_pTmpAudio)
			{
				delete[] m_pTmpAudio;
				m_pTmpAudio = NULL;
			}
			m_pTmpAudio = new short[inOutSize * 2];
			m_nTmpAudioLength = inOutSize * 2;
		}

		for (int i = 0; i < inOutSize; i++)
		{
			m_pTmpAudio[i * 2] = (short)(outLeft[i] * 32767);
			m_pTmpAudio[i * 2 + 1] = (short)(outRight[i] * 32767);
		}
		Arp_Reverb* popenalVerb = (Arp_Reverb*)m_openal_reverb;
		int ret = popenalVerb->Process_set((char *)m_pTmpAudio, inOutSize * 2 * sizeof(short));
		for (int i = 0; i < inOutSize; i++)
		{
			outLeft[i] = ((float)m_pTmpAudio[i * 2]) / 32767.0;
			outRight[i] = ((float)m_pTmpAudio[i * 2 + 1]) / 32767.0;
		}
		return ret;
	}
	else if (m_id == KALA_VB_DIZZY_QUICKLY)
	{
		return m_slow_flanging.ProcessLRIndependent(inLeft, inRight, outLeft, outRight, inOutSize);
		/*CReverb* poldVerb = (CReverb*)m_old_reverb;
		return poldVerb->Process(inBuffer, inSize, outBuffer, outSize);*/
	}

	if (m_nTmpAudioLength < inOutSize * 2)
	{
		if (m_pTmpAudio)
		{
			delete[] m_pTmpAudio;
			m_pTmpAudio = NULL;
		}
		m_pTmpAudio = new short[inOutSize * 2];
		m_nTmpAudioLength = inOutSize * 2;
	}

	for (int i = 0; i < inOutSize; i++)
	{
		m_pTmpAudio[i * 2] = (short)(inLeft[i] * 32767);
		m_pTmpAudio[i * 2 + 1] = (short)(inRight[i] * 32767);
	}
	Arp_Reverb* popenalVerb = (Arp_Reverb*)m_openal_reverb;
	int ret = popenalVerb->Process_set((char *)m_pTmpAudio, inOutSize * 2 * sizeof(short));
	for (int i = 0; i < inOutSize; i++)
	{
		outLeft[i] = ((float)m_pTmpAudio[i * 2]) / 32767.0;
		outRight[i] = ((float)m_pTmpAudio[i * 2 + 1]) / 32767.0;
	}

	return ret;
}

int CReverb4::Process(float* inBuffer, int inSize, float* outBuffer, int outSize)
{
	/* phonograh effect*/
	if (m_id == KALA_VB_PHONOGRAPH_QUICKLY)
	{
		memcpy(outBuffer, inBuffer, outSize * sizeof(float));
		return m_phonograph.Process(outBuffer, outSize);
	}
	else if (m_id == KALA_VB_KTV_V40_QUICKLY || m_id == KALA_VB_DISTANT_QUICKLY
			 || m_id == KALA_VB_OLD_DISTANT_QUICKLY || m_id == KALA_VB_KARAOKE_QUICKLY)
	{
		// left old ktv effect here.
		CReverb* poldverb = (CReverb*)m_old_reverb;
		return poldverb->Process(inBuffer, inSize, outBuffer, outSize);

	}
	else if (m_id == KALA_VB_WARM_QUICKLY)
	{
		memcpy(outBuffer, inBuffer, outSize * sizeof(float));
		m_filters.Process(outBuffer, outSize);
		if (m_nTmpAudioLength < outSize)
		{
			if (m_pTmpAudio)
			{
				delete[] m_pTmpAudio;
				m_pTmpAudio = NULL;
			}
			m_pTmpAudio = new short[outSize];
			m_nTmpAudioLength = outSize;
		}

		for (int i = 0; i < outSize; i++)
		{
			m_pTmpAudio[i] = (short)(outBuffer[i] * 32767);
		}
		Arp_Reverb* popenalVerb = (Arp_Reverb*)m_openal_reverb;
		int ret = popenalVerb->Process_set((char *)m_pTmpAudio, outSize * sizeof(short));
		for (int i = 0; i < outSize; i++)
		{
			outBuffer[i] = ((float)m_pTmpAudio[i]) / 32767.0;
		}

		return ret;
	}
	else if (m_id == KALA_VB_DIZZY_QUICKLY)
	{
		return m_slow_flanging.Process(inBuffer, outBuffer, outSize);
		/*CReverb* poldVerb = (CReverb*)m_old_reverb;
		return poldVerb->Process(inBuffer, inSize, outBuffer, outSize);*/
	}

	if (m_nTmpAudioLength < inSize)
	{
		if (m_pTmpAudio)
		{
			delete[] m_pTmpAudio;
			m_pTmpAudio = NULL;
		}
		m_pTmpAudio = new short[inSize];
		m_nTmpAudioLength = inSize;
	}

	for (int i = 0; i < inSize; i++)
	{
		m_pTmpAudio[i] = (short)(inBuffer[i] * 32767);
	}
	Arp_Reverb* popenalVerb = (Arp_Reverb*)m_openal_reverb;
	int ret = popenalVerb->Process_set((char *)m_pTmpAudio, inSize * sizeof(short));
	for (int i = 0; i < inSize; i++)
	{
		outBuffer[i] = ((float)m_pTmpAudio[i]) / 32767.0;
	}

	return ret;
}

int CReverb4::Process(short* inBuffer, int inSize, short* outBuffer, int outSize)
{
	return NULL;
}
//
//int CReverb4::Process(char* inBuffer, int inSize, char* outBuffer, int outSize)
//{
//
//    if (inSize % (m_channel * 2) != 0)
//    {
//        return err_kala_audio_base_h_param_invalid;
//    }
//	memcpy(outBuffer, inBuffer,outSize);
//
//	/* phonograh effect*/ 
//	if (m_id == KALA_VB_PHONOGRAPH_QUICKLY)
//	{
//		return m_phonograph.Process(outBuffer,outSize);
//	}
//	else if (m_id == KALA_VB_KTV_V40_QUICKLY)
//	{
//		// left old ktv effect here.
//		CReverb* poldverb = (CReverb*)m_old_reverb;
//		return poldverb->Process(inBuffer,inSize, outBuffer,outSize);
//
//	}
//	else if (m_id == KALA_VB_WARM_QUICKLY)
//	{
//		//m_filters.setFilterType(LOW_PASS_FILTER);
//		m_filters.Process(outBuffer, outSize);
//	}       
//	else if (m_id == KALA_VB_DISTANT_QUICKLY)
//	{
//		CReverb* poldVerb = (CReverb*)m_old_reverb;
//		return poldVerb->Process(inBuffer, inSize, outBuffer, outSize);
//	}
//
//	else if (m_id == KALA_VB_DIZZY_QUICKLY)
//	{
//// 		Arp_Reverb* popenalVerb = (Arp_Reverb*)m_openal_reverb;
//// 		int proSize = popenalVerb->Process_set(outBuffer,outSize);
//// 		if (proSize != outSize)
//// 		{
//// 			return proSize;
//// 		}
//// 		return FadeLeftRight(outBuffer, outSize);
//		return m_slow_flanging.Process(inBuffer, outBuffer, outSize);
//	}
//	
//	Arp_Reverb* popenalVerb = (Arp_Reverb*)m_openal_reverb;
//	return popenalVerb->Process_set(outBuffer,outSize);
//}

void CReverb4::Reset()
{
	CReverb* poldverb = (CReverb*)m_old_reverb;

	if (poldverb != NULL)
	{
		poldverb->Reset();
	}

	m_filters.Reset();
	m_phonograph.Reset();
	Arp_Reverb* popenalVerb = (Arp_Reverb*)m_openal_reverb;

	if (popenalVerb != NULL)
	{
		popenalVerb->Reset();
	}
	m_slow_flanging.Reset();
}

void CReverb4::Uninit()
{
	CReverb* poldverb = (CReverb*)m_old_reverb;
	
	if (poldverb != NULL)
	{
		poldverb->Uninit();
		delete poldverb;
		m_old_reverb = NULL;
	}
	
	m_filters.Uninit();
	m_phonograph.Uninit();
	Arp_Reverb* popenalVerb = (Arp_Reverb*)m_openal_reverb;

	if (popenalVerb != NULL)
	{
		popenalVerb->Uninit();
		delete popenalVerb;
		m_openal_reverb = NULL;
	}
	m_slow_flanging.UnInit();

	if (m_pTmpAudio)
	{
		delete[] m_pTmpAudio;
		m_pTmpAudio = NULL;
	}
	m_nTmpAudioLength = 0;
}


