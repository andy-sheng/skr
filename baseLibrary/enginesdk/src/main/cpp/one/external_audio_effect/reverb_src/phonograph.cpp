/************************************************************************/
/* Phonograph Eimulator                                                 */
/* copy right reserved                                                  */
/************************************************************************/

#include "math.h"
#include "stdlib.h"
#include "stdio.h"
#include "memory.h"
#include "reverb_inc/MSdcommon.h"
#include "reverb_inc/phonograph.h"
#include "reverb_inc/BiquadFilter.h"

// This is a trick. When enabled, all channels refer to the 1st channel
#define PSEUDO_MULTICHANNELS 0   // defined in KTYPED.h

#define VERSION_ID_NUMBER	100 /* 7.21,2013 */

using std::vector;
using namespace BiquadFilter;


#define HPF_FREQ 1712 / 44100.0f
#define LPF_FREQ 2222 / 44100.0f
#define HPFQ 2.1f
#define LPFQ 2.1f


class CPhonographFilters
{
public:
    CPhonographFilters();
    virtual ~CPhonographFilters();
	void reset();

    bool isAllocated() const;

    void filtering(std::vector<float> *x);

private:
    LPFilter* lpf;
    HPFilter* hpf;
};

bool CPhonographFilters::isAllocated() const
{
    return lpf != NULL && hpf != NULL;
}



void CPhonographFilters::filtering(std::vector<float> *x)
{
    hpf->filtering(x);
    lpf->filtering(x);
}



CPhonographFilters::CPhonographFilters()
{
    hpf = new HPFilter(HPF_FREQ, HPFQ);    
    lpf = new LPFilter(LPF_FREQ, LPFQ);   

    if (!hpf || !lpf)
    {
        if (hpf)
        {
            delete hpf;
            hpf = NULL;
        }
        if (lpf)
        {
            delete lpf;
            lpf = NULL;
        }
    }
}

void CPhonographFilters::reset()
{
	if (hpf)
	{
		hpf->reset();
	}
	if (lpf)
	{
		lpf->reset();
	}
}

CPhonographFilters::~CPhonographFilters()
{
    delete lpf;
    delete hpf;

    lpf = NULL;
    hpf = NULL;
}

CPhonograph::CPhonograph()
{
    m_samplerate = 0;
    m_channels = 0;
    handles = NULL;
}

void CPhonograph::Reset()
{
	if (handles)
	{
		for (int chn = 0; chn < m_channels; chn++)
		{
			CPhonographFilters* filters = ((CPhonographFilters**)handles)[chn];

			if (filters)
			{
				filters->reset();
			}

		}
	}
}

CPhonograph::~CPhonograph()
{
    Uninit();
}

int CPhonograph::Init(int inSampleRate, int inChannel)
{
    m_samplerate = inSampleRate;
    m_channels = inChannel;


    handles = new CPhonographFilters*[m_channels];

    if (!handles)
    {
        return err_kala_audio_base_h_malloc_null;
    }

    for (int chn = 0; chn < m_channels; chn++)
    {
        CPhonographFilters* filters = new CPhonographFilters();
       
        ((CPhonographFilters**)handles)[chn] = filters;

        if (!filters->isAllocated())
        {
            do{
                CPhonographFilters* filters = ((CPhonographFilters**)handles)[chn];
                delete filters;

            } while (chn--);

            delete [] (CPhonographFilters**)handles;
            handles = NULL;

            return err_kala_audio_base_h_malloc_null;
        }

    }

    return 0;
}

void CPhonograph::Uninit()
{
    if (handles)
    {
        for (int chn = 0; chn < m_channels; chn++)
        {
            CPhonographFilters* filters = ((CPhonographFilters**)handles)[chn];

            if (filters)
            {
                delete filters;
            }

        }

        delete [] (CPhonographFilters**)handles;
        handles = NULL;
    }
}

int CPhonograph::Process(short* inBuffer, int inSize)
{
    if (0 != (inSize % m_channels))
    {
        return err_kala_audio_base_h_param_invalid;
    }

    if (handles)
    {

        data.reserve(inSize / m_channels);
        data.resize(inSize / m_channels);

        for (int chn = 0; chn < m_channels; chn++)
        {
            CPhonographFilters* filters = ((CPhonographFilters**)handles)[chn];
            short* audio = chn + inBuffer;

            if (filters)
            {
                if (PSEUDO_MULTICHANNELS && chn>0)
                {
                    short* audioref = (short*)inBuffer;

                    for (unsigned int i = 0; i < data.size(); i++){
                        audio[0] = audioref[0];
                        audio += m_channels;
                        audioref += m_channels;
                    }
                }
                else
                {

                    for (unsigned int i = 0; i < data.size(); i++){
                        //                    data[i]= audio[i * m_channels + chn] / 32768.0;
                        data[i]= audio[0] / 32768.0f; 
                        audio += m_channels;
                    }

                    filters->filtering(&data);

                    short* audio = chn + inBuffer;

                    for (unsigned int i = 0; i < data.size(); i++){
                        int sample = (int)(32767.0f * data[i]);
//                        int sample = (int)(20767.0f * data[i]);
#if 1
                        if (sample>32767)
                            sample = 32767;
                        else if (sample<-32768)
                            sample = -32768;
#endif
                        //                   audio[i * m_channels + chn] = (short)sample;
                        audio[0] = (short)sample;
                        audio += m_channels;
                    }
                } // if (PSEUDO_MULTICHANNELS && chn>0)

            }

        }
    }

    return inSize;
}

int CPhonograph::ProcessLRIndependent(float * inLeft, float * inRight, int inOutSize)
{
	if (2 != m_channels)
	{
		return err_kala_audio_base_h_param_invalid;
	}
	data.reserve(inOutSize);
	data.resize(inOutSize);
	if (handles) {
		data.assign(inLeft, inLeft + inOutSize);
		CPhonographFilters* filters = ((CPhonographFilters**)handles)[0];
		filters->filtering(&data);
		for (unsigned int i = 0; i < data.size(); i++) {
			inLeft[i] = data[i];
		}

		data.assign(inRight, inRight + inOutSize);
		filters = ((CPhonographFilters**)handles)[1];
		filters->filtering(&data);
		for (unsigned int i = 0; i < data.size(); i++) {
			inRight[i] = data[i];
		}
	}
	return inOutSize;
}

int CPhonograph::Process(float* inBuffer, int inSize) {
    if (handles) {
        data.reserve(inSize / m_channels);
        data.resize(inSize / m_channels);
        for (int chn = 0; chn < m_channels; chn++) {
            CPhonographFilters* filters = ((CPhonographFilters**)handles)[chn];
            float* audio = chn + inBuffer;
            if (filters) {
                for (unsigned int i = 0; i < data.size(); i++) {
                    data[i]= audio[0];
                    audio += m_channels;
                }
                filters->filtering(&data);
                float* audio = chn + inBuffer;
                for (unsigned int i = 0; i < data.size(); i++) {
                    float sample = data[i];
//                    float sample = (20767.0f / 32768) * data[i];
#if 1
                    if (sample > (32767.0f / 32768))
                        sample = 32767.0f / 32768;
                    else if (sample < -1.0f)
                        sample = -1.0f;
#endif
                    audio[0] = sample;
                    audio += m_channels;
                }
            }
        }
    }
    return inSize;
}