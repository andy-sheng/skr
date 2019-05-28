/************************************************************************/
/* Phonograph Eimulator                                                 */
/* written by evieng, 7.21,2013                                       */
/* last modifiey by evieng, 6.6,2013                                  */
/* copy right reserved                                                  */
/************************************************************************/

#include "math.h"
#include "stdlib.h"
#include "stdio.h"
#include "memory.h"
#include "reverb_inc/MSdcommon.h"
#include "reverb_inc/CFilters.h"
#include "reverb_inc/BiquadFilter.h"

// This is a trick. When enabled, all channels refer to the 1st channel
// #define PSEUDO_MULTICHANNELS 1   // defined in KTYPED.h

#define VERSION_ID_NUMBER	100 /* 7.21,2013 */

#ifdef _MSC_VER
// MSVC build for Windows, and it's (expected to be) able to handle true stereo in real time
#define PSEUDO_MULTICHANNELS 0
#else
#define PSEUDO_MULTICHANNELS 1
#endif

using std::vector;
using namespace BiquadFilter;


#define HPF_FREQ (2000.0f / 44100.0f)
#define LPF_FREQ (6000.0f / 44100.0f)
#define HPFQ 0.5f
#define LPFQ 0.5f

#define BPF_LOW_EDGE (600.0f / 44100.0f)
#define BPF_HIGH_EDGE (8000.0f / 44100.0f)

class CBaseFilters
{
public:
    CBaseFilters();
    virtual ~CBaseFilters();
	void reset();

    bool isAllocated() const;
	int setFilterType(int ftype)
	{
		if (ftype < LOW_PASS_FILTER)
		{
			m_fType = LOW_PASS_FILTER;
		}
		else if (ftype > BAND_PASS_FILTER)
		{
			m_fType = BAND_PASS_FILTER;
		}
		else
			m_fType = ftype;
		return 0;
	}

    void filtering(std::vector<float> *x);

private:
    LPFilter* lpf;
    HPFilter* hpf;
	BPFilter* bpf;
	int m_fType;
};

bool CBaseFilters::isAllocated() const
{
    return lpf != NULL && hpf != NULL && bpf != NULL;
}



void CBaseFilters::filtering(std::vector<float> *x)
{
	if (m_fType == LOW_PASS_FILTER)
	{
		lpf->filtering(x);
	}
	else if (m_fType == HIGH_PASS_FILTER)
	{
		hpf->filtering(x);
	}
	else if (m_fType == BAND_PASS_FILTER)
	{
		bpf->filtering(x);
	}
	else
		lpf->filtering(x);
    //hpf->filtering(x);
}



CBaseFilters::CBaseFilters()
{
	m_fType = LOW_PASS_FILTER;
    hpf = new HPFilter(HPF_FREQ, HPFQ);    
    lpf = new LPFilter(LPF_FREQ, LPFQ); 
	bpf = new BPFilter(BPF_LOW_EDGE, BPF_HIGH_EDGE);

    if (!hpf || !lpf || !bpf)
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
		if (bpf)
		{
			delete bpf;
			bpf = NULL;
		}
    }
}

void CBaseFilters::reset()
{
	if (hpf)
	{
		hpf->reset();
	}
	if (lpf)
	{
		lpf->reset();
	}
	if (bpf)
	{
		bpf->reset();
	}
}

CBaseFilters::~CBaseFilters()
{
    delete lpf;
    delete hpf;
	delete bpf;
    lpf = NULL;
    hpf = NULL;
	bpf = NULL;
}

CFilters::CFilters()
{
    m_samplerate = 0;
    m_channels = 0;
    handles = NULL;
}

CFilters::~CFilters()
{
    Uninit();
}

int CFilters::Init(int inSampleRate, int inChannel)
{
    m_samplerate = inSampleRate;
    m_channels = inChannel;


    handles = new CBaseFilters*[m_channels];

    if (!handles)
    {
        return err_kala_audio_base_h_malloc_null;
    }

    for (int chn = 0; chn < m_channels; chn++)
    {
        CBaseFilters* filters = new CBaseFilters();
       
        ((CBaseFilters**)handles)[chn] = filters;

        if (!filters->isAllocated())
        {
            do{
                CBaseFilters* filters = ((CBaseFilters**)handles)[chn];
                delete filters;

            } while (chn--);

            delete [] (CBaseFilters**)handles;
            handles = NULL;

            return err_kala_audio_base_h_malloc_null;
        }

    }

    return 0;
}

void CFilters::Reset()
{
	if (handles)
	{
		for (int chn = 0; chn < m_channels; chn++)
		{
			CBaseFilters* filters = ((CBaseFilters**)handles)[chn];

			if (filters)
			{
				filters->reset();
			}

		}
	}
}

void CFilters::Uninit()
{
    if (handles)
    {
        for (int chn = 0; chn < m_channels; chn++)
        {
            CBaseFilters* filters = ((CBaseFilters**)handles)[chn];

            if (filters)
            {
                delete filters;
            }

        }

        delete [] (CBaseFilters**)handles;
        handles = NULL;
    }
}
int CFilters::setFilterType(int fType)
{
	if (handles)
	{
		for (int chn = 0; chn < m_channels; chn++)
		{
			CBaseFilters* filters = ((CBaseFilters**)handles)[chn];

			if (filters)
			{
				filters->setFilterType(fType);
			}

		}
		return 0;
	}
	else
		return err_kala_audio_base_h_malloc_null;
}

int CFilters::ProcessLRIndependent(float * inLeft, float * inRight, int inOutSize)
{
	if (2 != m_channels)
	{
		return err_kala_audio_base_h_param_invalid;
	}
	data.reserve(inOutSize);
	data.resize(inOutSize);
	if (handles) {
		data.assign(inLeft, inLeft + inOutSize);
		CBaseFilters* filters = ((CBaseFilters**)handles)[0];
		filters->filtering(&data);
		for (unsigned int i = 0; i < data.size(); i++) {
			inLeft[i] = data[i];
		}

		data.assign(inRight, inRight + inOutSize);
		filters = ((CBaseFilters**)handles)[1];
		filters->filtering(&data);
		for (unsigned int i = 0; i < data.size(); i++) {
			inRight[i] = data[i];
		}
	}
	return inOutSize;
}

int CFilters::Process(float* inBuffer, int inSize)
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
			CBaseFilters* filters = ((CBaseFilters**)handles)[chn];
			float* audio = inBuffer + chn;

			if (filters)
			{
				if (PSEUDO_MULTICHANNELS && chn > 0)
				{
					float* audioref = inBuffer;

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
						data[i] = audio[0];
						audio += m_channels;
					}

					filters->filtering(&data);

					float* audio = inBuffer + chn;

					for (unsigned int i = 0; i < data.size(); i++){
						*audio = data[i];
						audio += m_channels;
					}
				} // if (PSEUDO_MULTICHANNELS && chn>0)

			}

		}
	}
	return err_kala_audio_base_h_success;
}

int CFilters::Process(char* inBuffer, int inSize)
{
    if (0 != (inSize % (sizeof(short) * m_channels)))
    {
        return err_kala_audio_base_h_param_invalid;
    }

    if (handles)
    {

        data.reserve(inSize / sizeof(short) / m_channels);
        data.resize(inSize / sizeof(short) / m_channels);

        for (int chn = 0; chn < m_channels; chn++)
        {
            CBaseFilters* filters = ((CBaseFilters**)handles)[chn];
            short* audio = chn + (short*)inBuffer;

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

                    short* audio = chn + (short*)inBuffer;

                    for (unsigned int i = 0; i < data.size(); i++){
                        int sample = (int)(32767.0f * data[i]);

                        if (sample>32767)
                            sample = 32767;
                        else if (sample<-32768)
                            sample = -32768;

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