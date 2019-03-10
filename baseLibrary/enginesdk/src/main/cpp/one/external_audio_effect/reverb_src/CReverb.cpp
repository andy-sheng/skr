#include "reverb_inc/MSdcommon.h"
#include "reverb_inc/CReverb.h"
#include "reverb_src/verb/revmodel.h"

#define MAX_VERB_VALUE	19
#define MIN_VERB_VALUE	0
#define MAX_NUMBER_OF_VERB MAX_VERB_VALUE +1
const char g_p_verb_id_names[][64] = {
	{ "studio" },
	{ "KTV" },
	{ "concert" },
	{ "theater" },
	{ "ktv 2" },
	{ "concert 2" },
	{ "theater 2" },
	{ "7" },
	{ "8" },
	{"9"},
	{"10"},
	{ "11" },
	{"12"},
	{"13"},
	{ "14" },
	{ "15" },
	{ "16" },
	{ "17" },
	{ "distant" },
	{ "custom" }
};

#ifndef SHORTMAX
#define SHORTMAX 32767
#endif
#ifndef SHORTMIN
#define SHORTMIN -32768
#endif

#ifndef Clip_short	
#define Clip_short(x) (short)((x)>SHORTMAX?SHORTMAX:((x)<SHORTMIN?SHORTMIN:(x)))
#endif


int CReverb::Init(int sampleRate, int channel)
{
	revmodel* pmd = new revmodel;
	if (pmd == NULL)
	{
		return err_kala_audio_base_h_malloc_null;
	}

	pmd->mute();

	m_channel = channel;
	m_sampleRate = sampleRate;

	m_pvb = (void*)pmd;

	return 0;
}

void CReverb::Reset()
{
	revmodel* pmv = (revmodel*)m_pvb;
	if (pmv != NULL)
	{
		pmv->mute();
	}
	return;
}

void CReverb::Uninit()
{
	revmodel* pmv = (revmodel*)m_pvb;
	if (pmv != NULL)
	{
		delete pmv;
		pmv = NULL;

		m_pvb = NULL;
	}

}

int CReverb::GetLatence()
{
	return 0;
}

void CReverb::GetIdRange(int* maxVal, int* minVal)
{
	*maxVal = MAX_VERB_VALUE;
	*minVal = MIN_VERB_VALUE;
}

int CReverb::GetIdDefault()
{
	return KALA_VB_RECORD_STUDIO;
}

int  CReverb::SetRoomSize(float roomSize)
{
	if (roomSize > 1.0f || roomSize < 0.0f)
		return err_kala_audio_base_h_null;

	revmodel* pmd = (revmodel*)m_pvb;
	if (pmd == NULL)
	{
		return err_kala_audio_base_h_null;
	}
	pmd->setroomsize(roomSize);
	return 0;
}

float  CReverb::GetRoomSize()
{
	revmodel* pmd = (revmodel*)m_pvb;
	float roomSize = pmd->getroomsize();
	return roomSize;
}

int  CReverb::SetWet(float wet)
{
	if (wet > 1.0f || wet < 0.0f)
		return err_kala_audio_base_h_null;
	revmodel* pmd = (revmodel*)m_pvb;
	if (pmd == NULL)
	{
		return err_kala_audio_base_h_null;
	}
	pmd->setwet(wet/3.0f);
	return 0;
}

float  CReverb::GetWet()
{
	revmodel* pmd = (revmodel*)m_pvb;
	float wet = pmd->getwet();
	wet *= 3.0f;
	return wet;
}

int CReverb::SetTypeId(int typeID)
{
	revmodel* pmd = (revmodel*)m_pvb;
	if (pmd == NULL)
	{
		return err_kala_audio_base_h_null;
	}

	// check input
	if (typeID<MIN_VERB_VALUE)
	{
		typeID = MIN_VERB_VALUE;
	}
	else if (typeID >MAX_VERB_VALUE)
	{
		typeID = MAX_VERB_VALUE;
	}

	m_id = typeID;

	/* it's better here to use id array than switch case, can optimization here later */
	switch (m_id)
	{
		/***************** v1.0, id 0~3, add here ************************************/
	case KALA_VB_RECORD_STUDIO:
	{
		pmd->setmode(0.2f);
		pmd->setroomsize(0);
		pmd->setdamp(0);
		pmd->setwet(0);
		pmd->setdry(0.8f);
		pmd->setwidth(0.5f);
		break;
	}
	case KALA_VB_KTV:
	{
		// for ktv
		pmd->setmode(0.2f);
		pmd->setroomsize(0.35f);
		pmd->setdamp(0.5f);
		pmd->setwet(0.20f);
		pmd->setdry(0.5f);
		pmd->setwidth(0.5f);

		break;
	}

	case KALA_VB_CONCERT:
	{
		// for concert ok
		pmd->setmode(0.3f);
		pmd->setroomsize(0.50f);
		pmd->setdamp(0.6f);
		pmd->setwet(0.3f);
		pmd->setdry(0.6f);
		pmd->setwidth(0.56f);
		break;
	}

	case KALA_VB_THEATER:
	{
		// for theater
		pmd->setmode(0.0);
		pmd->setroomsize(0.6f);
		pmd->setdamp(0.2f);
		pmd->setwet(0.4f);
		pmd->setdry(0.5f);
		pmd->setwidth(0.20f);
		break;
	}
	/***************** kala v2.6, id 0\4\5\6, add here ************************************/
	case KALA_VB_NEW_KTV:
	{

		// v2.2
		pmd->setmode(0.2f);
		pmd->setroomsize(0.7f);
		pmd->setdamp(0.9f);
		pmd->setwet(0.11f);
		pmd->setdry(0.44f);
		pmd->setwidth(0.5f);

		break;
	}
	case KALA_VB_NEW_CONCERT:
	{
		// v2.2
		pmd->setmode(0.3f);
		pmd->setroomsize(0.60f);
		pmd->setdamp(0.6f);
		pmd->setwet(0.18f);
		pmd->setdry(0.37f);
		pmd->setwidth(0.56f);

		break;
	}
	case KALA_VB_NEW_THEATER:
	{

		//old version 
		//pmd->setmode(0.0);
		//pmd->setroomsize(0.6f);
		//pmd->setdamp(0.2f);
		//pmd->setwet(0.4f);
		//pmd->setdry(0.5f);
		//pmd->setwidth(0.20f);

		// new 1.0
		//pmd->setmode(0.0);
		//pmd->setroomsize(0.8f);
		//pmd->setdamp(0.8f);
		//pmd->setwet(0.3f);
		//pmd->setdry(0.4f);
		//pmd->setwidth(0.50f);

		// v2.0
		//pmd->setmode(0.0);
		//pmd->setroomsize(0.8f);
		//pmd->setdamp(0.8f);
		//pmd->setwet(0.22f);
		//pmd->setdry(0.14f);
		//pmd->setwidth(0.50f);

		//v2.2
		pmd->setmode(0.0);
		pmd->setroomsize(0.8f);
		pmd->setdamp(0.8f);
		pmd->setwet(0.248f);
		pmd->setdry(0.182f);
		pmd->setwidth(0.50f);

		break;
	}
	/***************** kala v2.6, id 0\4\5\6, add here ************************************/
	case KALA_VB_ID_7:
	{
		// for ktv
		pmd->setmode(0.2f);
		pmd->setroomsize(0.35f);
		pmd->setdamp(0.5f);
		pmd->setwet(0.20f);
		pmd->setdry(0.5f);
		pmd->setwidth(0.5f);

		break;
	}
	case KALA_VB_ID_8:
	{
		// for ktv
		pmd->setmode(0.2f);
		pmd->setroomsize(0.35f);
		pmd->setdamp(0.5f);
		pmd->setwet(0.20f);
		pmd->setdry(0.5f);
		pmd->setwidth(0.5f);

		break;
	}
	case KALA_VB_ID_9:
	{
		// for ktv
		pmd->setmode(0.2f);
		pmd->setroomsize(0.35f);
		pmd->setdamp(0.5f);
		pmd->setwet(0.20f);
		pmd->setdry(0.5f);
		pmd->setwidth(0.5f);

		break;
	}
	case KALA_VB_ID_10:
	{
		// for ktv
		pmd->setmode(0.2f);
		pmd->setroomsize(0.35f);
		pmd->setdamp(0.5f);
		pmd->setwet(0.20f);
		pmd->setdry(0.5f);
		pmd->setwidth(0.5f);

		break;
	}
	case KALA_VB_ID_11:
	{
		// for ktv
		pmd->setmode(0.2f);
		pmd->setroomsize(0.35f);
		pmd->setdamp(0.5f);
		pmd->setwet(0.20f);
		pmd->setdry(0.5f);
		pmd->setwidth(0.5f);

		break;
	}
	case KALA_VB_ID_12:
	{
		// for ktv
		pmd->setmode(0.2f);
		pmd->setroomsize(0.4f);
		pmd->setdamp(0.69f);
		pmd->setwet(0.14f);
		pmd->setdry(0.22f);
		pmd->setwidth(0.82f);

		break;
	}
	case KALA_VB_ID_13:
	{
		// for ktv
		pmd->setmode(0.2f);
		pmd->setroomsize(0.41f);
		pmd->setdamp(0.18f);
		pmd->setwet(0.16f);
		pmd->setdry(0.24f);
		pmd->setwidth(0.68f);

		break;
	}
	case KALA_VB_ID_14:
	{
		// for ktv
		pmd->setmode(0.2f);
		pmd->setroomsize(0.79f);
		pmd->setdamp(0.63f);
		pmd->setwet(0.14f);
		pmd->setdry(0.26f);
		pmd->setwidth(0.92f);

		break;
	}
	case KALA_VB_ID_15:
	{
		// for ktv
		pmd->setmode(0.2f);
		pmd->setroomsize(0.84f);
		pmd->setdamp(0.25f);
		pmd->setwet(0.12f);
		pmd->setdry(0.22f);
		pmd->setwidth(0.76f);

		break;
	}
	case KALA_VB_ID_16:
	{
		// for ktv
		pmd->setmode(0.2f);
		pmd->setroomsize(0.88f);
		pmd->setdamp(0.42f);
		pmd->setwet(0.12f);
		pmd->setdry(0.17f);
		pmd->setwidth(0.84f);

		break;
	}
	case KALA_VB_ID_17:
	{
		// for ktv
		pmd->setmode(0.2f);
		pmd->setroomsize(0.35f);
		pmd->setdamp(0.5f);
		pmd->setwet(0.20f);
		pmd->setdry(0.5f);
		pmd->setwidth(0.5f);

		break;
	}
	case KALA_VB_ID_18:
	{
		// for ktv
		pmd->setmode(0.2f);
		pmd->setroomsize(0.65f);
		pmd->setdamp(0.25f);
		pmd->setwet(0.25f);
		pmd->setdry(0.6f);
		pmd->setwidth(0.95f);

		break;
	}
	default:
	{
		pmd->setmode(1);
		break;
	}
	}

	return 0;
}

int CReverb::GetTypeId()
{
	return m_id;
}

char* CReverb::GetNameById(int typeID)
{
	if (typeID <0)
	{
		typeID = 0;
	}
	else if (typeID > MAX_VERB_VALUE -1)
	{
		typeID = MAX_VERB_VALUE -1;
	}

	return (char*)g_p_verb_id_names[typeID];
}

int CReverb::Process(short * inChannel, int inSize, short * outBuffer, int outSize)
{
	/* check input */
	revmodel* pmd = (revmodel*)m_pvb;
	if (pmd == NULL)
	{
		return err_kala_audio_base_h_null;
	}

	if (inSize!= outSize)
	{
		return err_kala_audio_base_h_param_invalid;
	}

	inSize /= m_channel;
	outSize /= m_channel;

	/* for mono */
	if (m_channel == 1)
	{
		int j;
		short* pInSample	= inChannel;
		short* pOutSample	= outBuffer;

		for (j = 0; j < inSize; j++)
		{
			float saminL;
			float samoutL;

			saminL = pInSample[j]/32768.0f;
			//saminL = pInSample[j];
			pmd->processMono(&saminL,&samoutL, 1,0);

			pOutSample[j] = (short)Clip_short((int)(samoutL*32768.0f));
			//pOutSample[j] = (short)Clip_short((int)samoutL);
		}

	}
	else if (m_channel == 2)
	{
		int j;
		short* pInSample	= inChannel;
		short* pOutSample	= outBuffer;

		for (j = 0; j < inSize; j++)
		{
			float saminL,saminR;
			float samoutL,samoutR;

			saminL = pInSample[j+j]/32768.0f;
			saminR = pInSample[j+j +1]/32768.0f;

			pmd->processreplace(&saminL,&saminR,&samoutL,&samoutR, 1,0);

			pOutSample[j+j] = (short)Clip_short((int)(samoutL*32768.0f));
			pOutSample[j+j+1] = (short)Clip_short((int)(samoutR*32768.0f));
		}
	}

	return outSize;
}

int CReverb::ProcessLRIndependent(float * inLeft, float * inRight, float * outLeft, float * outRight, int inOutSize)
{
	if (2 != m_channel)
	{
		return err_kala_audio_base_h_param_invalid;
	}
	/* check input */
	revmodel* pmd = (revmodel*)m_pvb;
	if (pmd == NULL)
	{
		return err_kala_audio_base_h_null;
	}

	int j;
	for (j = 0; j < inOutSize; j++)
	{
		pmd->processreplace(inLeft + j, inRight + j, outLeft + j, outRight + j, 1, 0);
	}
	return inOutSize;
}

int CReverb::Process(float * inChannel, int inSize, float * outBuffer, int outSize)
{
    /* check input */
    revmodel* pmd = (revmodel*)m_pvb;
    if (pmd == NULL)
    {
        return err_kala_audio_base_h_null;
    }
    
    if (inSize!= outSize)
    {
        return err_kala_audio_base_h_param_invalid;
    }

	inSize /= m_channel;
	outSize /= m_channel;
    
    /* for mono */
    if (m_channel == 1)
    {
        int j;
        float* pInSample	= inChannel;
        float* pOutSample	= outBuffer;
        
        for (j = 0; j < inSize; j++)
        {
            float saminL;
            float samoutL;
            
            saminL = pInSample[j];
            //saminL = pInSample[j];
            pmd->processMono(&saminL,&samoutL, 1,0);
            
            pOutSample[j] = samoutL;
            //pOutSample[j] = (short)Clip_short((int)samoutL);
        }
        
    }
    else if (m_channel == 2)
    {
        int j;
        float* pInSample	= inChannel;
        float* pOutSample	= outBuffer;
        
        for (j = 0; j < inSize; j++)
        {
            float saminL,saminR;
            float samoutL,samoutR;
            
            saminL = pInSample[j+j];
            saminR = pInSample[j+j +1];
            
            pmd->processreplace(&saminL,&saminR,&samoutL,&samoutR, 1,0);
            
            pOutSample[j+j] = samoutL;
            pOutSample[j+j+1] = samoutR;
        }
    }
    
    return outSize;
}

