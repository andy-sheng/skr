#include "reverb_inc/MSdcommon.h"
#define _USE_MATH_DEFINES
#include <math.h>
#include "reverb_inc/slowFlanger.h"

#define MAX_DELAY_SAMPLE (2.53) //
#define MIN_DELAY_SAMPLE (0.0)

int CSlowFlanging::Init( int channel, int sample_rate)
{
	
	m_iwr_l = 0;
	m_circle_buffer_l = NULL;
	m_iwr_fb_l = 0;
	m_circle_buffer_fb_l = NULL;
	m_iwr_r = 0;
	m_circle_buffer_r = NULL;
	m_iwr_fb_r = 0;
	m_circle_buffer_fb_r = NULL;
	m_channel = channel;
	if (channel > 2 || channel < 1)
	{
		return err_kala_audio_base_h_param_invalid;
	}
	m_sample_rate = sample_rate;
	m_oscillation_period = 3787.0f*(float)m_sample_rate/1000.0f;
	m_samples_processed_left = 0;
	m_samples_processed_right = m_oscillation_period/2;
#define MAX_DELAY_SAMPLE_COUNT (int(MAX_DELAY_SAMPLE*(float)m_sample_rate/1000.0f)+2)
	//m_delayed_samples = std::vector<float>(MAX_DELAY_SAMPLE_COUNT,0);
	m_circle_buffer_l = new(std::nothrow) float[MAX_DELAY_SAMPLE_COUNT];
	if (m_circle_buffer_l == NULL)
	{
		UnInit();
		return err_kala_audio_base_h_malloc_null;
	}
	//memset(m_circle_buffer,0,MAX_DELAY_SAMPLE_COUNT);
	for(int i =0;i<MAX_DELAY_SAMPLE_COUNT;i++) m_circle_buffer_l[i]=0.0f;

	m_circle_buffer_fb_l = new(std::nothrow) float[MAX_DELAY_SAMPLE_COUNT];
	if (m_circle_buffer_fb_l == NULL)
	{
		UnInit();
		return err_kala_audio_base_h_malloc_null;
	}
	//memset(m_circle_buffer_fb,0.0,MAX_DELAY_SAMPLE_COUNT);
	for(int i =0;i<MAX_DELAY_SAMPLE_COUNT;i++) m_circle_buffer_fb_l[i]=0.0f;
	if (m_channel == 2)
	{
		m_circle_buffer_r = new(std::nothrow) float[MAX_DELAY_SAMPLE_COUNT];
		if (m_circle_buffer_r == NULL)
		{
			UnInit();
			return err_kala_audio_base_h_malloc_null;
		}
		//memset(m_circle_buffer,0,MAX_DELAY_SAMPLE_COUNT);
		for(int i =0;i<MAX_DELAY_SAMPLE_COUNT;i++) m_circle_buffer_r[i]=0.0f;

		m_circle_buffer_fb_r = new(std::nothrow) float[MAX_DELAY_SAMPLE_COUNT];
		if (m_circle_buffer_fb_r == NULL)
		{
			UnInit();
			return err_kala_audio_base_h_malloc_null;
		}
		//memset(m_circle_buffer_fb,0.0,MAX_DELAY_SAMPLE_COUNT);
		for(int i =0;i<MAX_DELAY_SAMPLE_COUNT;i++) m_circle_buffer_fb_r[i]=0.0f;
	}
	return 0;
}

void CSlowFlanging::Reset()
{
	m_iwr_l = 0;
	m_iwr_fb_l = 0;
	m_iwr_r = 0;
	m_iwr_fb_r = 0;
	m_samples_processed_left = 0;
	m_samples_processed_right = m_oscillation_period / 2;
	if (m_circle_buffer_l != NULL)
	{
		memset(m_circle_buffer_l, MAX_DELAY_SAMPLE_COUNT * sizeof(float),0);
	}
	if (m_circle_buffer_fb_l != NULL)
	{
		memset(m_circle_buffer_fb_l, MAX_DELAY_SAMPLE_COUNT * sizeof(float), 0);
	}
	if (m_circle_buffer_r != NULL)
	{
		memset(m_circle_buffer_r, MAX_DELAY_SAMPLE_COUNT * sizeof(float), 0);
	}
	if (m_circle_buffer_fb_r != NULL)
	{
		memset(m_circle_buffer_fb_r, MAX_DELAY_SAMPLE_COUNT * sizeof(float), 0);
	}
}

int CSlowFlanging::ProcessLRIndependent(float * inLeft, float * inRight, float * outLeft, float * outRight, int inOutSize)
{
	if (2 != m_channel)
	{
		return err_kala_audio_base_h_param_invalid;
	}

	int sample_count_l = ProcessSample(inLeft, outLeft, inOutSize, m_samples_processed_left, m_circle_buffer_l, m_iwr_l, m_circle_buffer_fb_l, m_iwr_fb_l);
	int sample_count_r = ProcessSample(inRight, outRight, inOutSize, m_samples_processed_right, m_circle_buffer_r, m_iwr_r, m_circle_buffer_fb_r, m_iwr_fb_r);
	if (sample_count_l != inOutSize || sample_count_r != inOutSize)
	{
		return err_kala_audio_base_h_unknown;
	}
	return inOutSize;
}

int CSlowFlanging::Process(const float* in_buffer, float* out_buffer, int size)
{
	if (m_channel == 1)
	{
		int sample_count = ProcessSample(in_buffer, out_buffer, size, m_samples_processed_left, m_circle_buffer_l, m_iwr_l, m_circle_buffer_fb_l, m_iwr_fb_l);
		if (sample_count != size)
		{
			return err_kala_audio_base_h_unknown;
		}
	}
	else if (m_channel == 2)
	{
		if (size % 2 != 0)
		{
			return err_kala_audio_base_h_param_invalid;
		}
		float *in_samples_l = new(std::nothrow) float[size / 2];
		if (in_samples_l == NULL)
		{
			return err_kala_audio_base_h_malloc_null;
		}
		float *in_samples_r = new(std::nothrow) float[size / 2];
		if (in_samples_r == NULL)
		{
			if (in_samples_l != NULL)
			{
				delete[] in_samples_l;
				in_samples_l = NULL;
			}
			return err_kala_audio_base_h_malloc_null;
		}


		for (int i = 0; i < size / 2; i++)
		{
			in_samples_l[i] = in_buffer[2*i];
			in_samples_r[i] = in_buffer[2*i + 1];
		}

		float *out_samples_l = new(std::nothrow) float[size / 2];
		if (out_samples_l == NULL)
		{
			if (in_samples_l != NULL)
			{
				delete[] in_samples_l;
				in_samples_l = NULL;
			}
			if (in_samples_r != NULL)
			{
				delete[] in_samples_r;
				in_samples_r = NULL;
			}
			return err_kala_audio_base_h_malloc_null;
		}
		float *out_samples_r = new(std::nothrow) float[size / 2];
		if (out_samples_r == NULL)
		{
			if (in_samples_l != NULL)
			{
				delete[] in_samples_l;
				in_samples_l = NULL;
			}
			if (in_samples_r != NULL)
			{
				delete[] in_samples_r;
				in_samples_r = NULL;
			}
			if (out_samples_l != NULL)
			{
				delete[] out_samples_l;
				out_samples_l = NULL;
			}
			return err_kala_audio_base_h_malloc_null;
		}

		int sample_count_l = ProcessSample(in_samples_l, out_samples_l, size / 2, m_samples_processed_left, m_circle_buffer_l, m_iwr_l, m_circle_buffer_fb_l, m_iwr_fb_l);
		int sample_count_r = ProcessSample(in_samples_r, out_samples_r, size / 2, m_samples_processed_right, m_circle_buffer_r, m_iwr_r, m_circle_buffer_fb_r, m_iwr_fb_r);
		if (sample_count_l != size / 2 || sample_count_r != size / 2)
		{
			if (in_samples_l != NULL)
			{
				delete[] in_samples_l;
				in_samples_l = NULL;
			}
			if (in_samples_r != NULL)
			{
				delete[] in_samples_r;
				in_samples_r = NULL;
			}
			if (out_samples_l != NULL)
			{
				delete[] out_samples_l;
				out_samples_l = NULL;

			}
			if (out_samples_r != NULL)
			{
				delete[] out_samples_r;
				out_samples_r = NULL;
			}
			return err_kala_audio_base_h_unknown;
		}

		for (int i = 0; i < size / 2; i++)
		{
			out_buffer[2 * i] = out_samples_l[i];
			out_buffer[2 * i + 1] = out_samples_r[i];
		}

		if (in_samples_l != NULL)
		{
			delete[] in_samples_l;
			in_samples_l = NULL;
		}
		if (in_samples_r != NULL)
		{
			delete[] in_samples_r;
			in_samples_r = NULL;
		}
		if (out_samples_l != NULL)
		{
			delete[] out_samples_l;
			out_samples_l = NULL;

		}
		if (out_samples_r != NULL)
		{
			delete[] out_samples_r;
			out_samples_r = NULL;
		}
	}
	return size;
}

int CSlowFlanging::Process(const char* in_buffer, char* out_buffer, int size)
{
	if (m_channel == 1)
	{
		if (size%2 != 0)
		{
			return -1;
		}
		float *in_samples = new(std::nothrow) float[size/2];
		if (in_samples == NULL)
		{
			return err_kala_audio_base_h_malloc_null;
		}

		for (int i = 0; i < size/2; i++)
		{
			in_samples[i] = *(((short*)in_buffer+i))/32767.0f;
		}

		float *out_samples = new float[size/2];
		if (out_samples == NULL)
		{
			if (in_samples != NULL)
			{
				delete[] in_samples;
				in_samples = NULL;
			}
			return err_kala_audio_base_h_malloc_null;
		}

		int sample_count = ProcessSample(in_samples, out_samples, size/2, m_samples_processed_left,m_circle_buffer_l,m_iwr_l,m_circle_buffer_fb_l,m_iwr_fb_l);
		if (sample_count != size/2)
		{
			if (in_samples != NULL)
			{
				delete[] in_samples;
				in_samples = NULL;
			}
			if (out_samples != NULL)
			{
				delete[] out_samples;
				out_samples = NULL;
			}
			return err_kala_audio_base_h_unknown;
		}

		for (int i = 0; i < size/2; i++)
		{
			*((short*)out_buffer+i) = (short)(fabs(out_samples[i]) > 1.0f ? out_samples[i]/fabs(out_samples[i])*32767.0f : out_samples[i]*32767.0f);
		}
		if (in_samples != NULL)
		{
			delete[] in_samples;
			in_samples = NULL;
		}
		if (out_samples != NULL)
		{
			delete[] out_samples;
			out_samples = NULL;
		}
	}
	else if (m_channel == 2)
	{
		if (size % 4 != 0)
		{
			return err_kala_audio_base_h_param_invalid;
		}
		float *in_samples_l = new(std::nothrow) float[size/4];
		if (in_samples_l == NULL)
		{
			return err_kala_audio_base_h_malloc_null;
		}
		float *in_samples_r = new(std::nothrow) float[size/4];
		if (in_samples_r == NULL)
		{
			if (in_samples_l != NULL)
			{
				delete[] in_samples_l;
				in_samples_l = NULL;
			}
			return err_kala_audio_base_h_malloc_null;
		}


		for (int i = 0; i < size/4; i++)
		{
			in_samples_l[i] = *(((short*)in_buffer+i*2))/32767.0f;
			in_samples_r[i] = *(((short*)in_buffer+i*2+1))/32767.0f;
		}

		float *out_samples_l = new(std::nothrow) float[size/4];
		if (out_samples_l == NULL)
		{
			if (in_samples_l != NULL)
			{
				delete[] in_samples_l;
				in_samples_l = NULL;
			}
			if (in_samples_r != NULL)
			{
				delete[] in_samples_r;
				in_samples_r = NULL;
			}
			return err_kala_audio_base_h_malloc_null;
		}
		float *out_samples_r = new(std::nothrow) float[size/4];
		if (out_samples_r == NULL)
		{
			if (in_samples_l != NULL)
			{
				delete[] in_samples_l;
				in_samples_l = NULL;
			}
			if (in_samples_r != NULL)
			{
				delete[] in_samples_r;
				in_samples_r = NULL;
			}
			if (out_samples_l != NULL)
			{
				delete[] out_samples_l;
				out_samples_l = NULL;
			}
			return err_kala_audio_base_h_malloc_null;
		}

		int sample_count_l = ProcessSample(in_samples_l, out_samples_l, size/4, m_samples_processed_left, m_circle_buffer_l,m_iwr_l,m_circle_buffer_fb_l,m_iwr_fb_l);
		int sample_count_r = ProcessSample(in_samples_r, out_samples_r, size/4, m_samples_processed_right, m_circle_buffer_r,m_iwr_r,m_circle_buffer_fb_r,m_iwr_fb_r);
		if (sample_count_l != size/4 || sample_count_r != size/4)
		{
			if (in_samples_l != NULL)
			{
				delete[] in_samples_l;
				in_samples_l = NULL;
			}
			if (in_samples_r != NULL)
			{
				delete[] in_samples_r;
				in_samples_r = NULL;
			}
			if (out_samples_l != NULL)
			{
				delete[] out_samples_l;
				out_samples_l = NULL;
			
			}
			if (out_samples_r != NULL)
			{
				delete[] out_samples_r;
				out_samples_r = NULL;
			}
			return err_kala_audio_base_h_unknown;
		}
		
		for (int i = 0; i < size/4; i++)
		{
			*((short*)out_buffer+2*i) = (short)(fabs(out_samples_l[i]) > 1.0f ? out_samples_l[i]/fabs(out_samples_l[i])*32767.0f : out_samples_l[i]*32767.0f);
			*((short*)out_buffer+2*i+1) = (short)(fabs(out_samples_r[i]) > 1.0f ? out_samples_r[i]/fabs(out_samples_r[i])*32767.0f : out_samples_r[i]*32767.0f);
		}

		if (in_samples_l != NULL)
		{
			delete[] in_samples_l;
			in_samples_l = NULL;
		}
		if (in_samples_r != NULL)
		{
			delete[] in_samples_r;
			in_samples_r = NULL;
		}
		if (out_samples_l != NULL)
		{
			delete[] out_samples_l;
			out_samples_l = NULL;

		}
		if (out_samples_r != NULL)
		{
			delete[] out_samples_r;
			out_samples_r = NULL;
		}
	}
	else
		;
	return size;
}

int CSlowFlanging::ProcessSample(const float* in_samples, float* out_samples, int sample_count, unsigned int &samples_processed,
	float *circle_buffer, int &iwr, float *circle_buffer_fb, int &iwr_fb)
{
	//float r = (float)(MAX_DELAY_SAMPLE_COUNT - MIN_DELAY_SAMPLE_COUNT)*2.0f/(float)OSCILLATION_PERIOD;
#define MAX_DELAY_SAMPLE_COUNT (int(MAX_DELAY_SAMPLE*(float)m_sample_rate/1000.0f)+2)
#define AVE_DELAY_SAMPLE (((MAX_DELAY_SAMPLE) - (MIN_DELAY_SAMPLE))*(float)m_sample_rate/1000.0f/2)

	float gfb = 0.2f;
	float gff = 1.0f;
	for (int i = 0; i < sample_count; i++)
	{
		//int delay_index = (m_samples_processed % OSCILLATION_PERIOD) <= OSCILLATION_PERIOD / 2 ?
		//	(float)(m_samples_processed % OSCILLATION_PERIOD) * r  + MIN_DELAY_SAMPLE_COUNT 
		//	: - (float)(m_samples_processed % OSCILLATION_PERIOD) * r + 2 * MAX_DELAY_SAMPLE_COUNT - MIN_DELAY_SAMPLE_COUNT;
		// 		if (m_samples_processed > OSCILLATION_PERIOD)
		// 		{
		// 			break;
		// 		}
		float delay_t = (float)(AVE_DELAY_SAMPLE+MIN_DELAY_SAMPLE) + (AVE_DELAY_SAMPLE*sin(((float)(samples_processed)*2*M_PI)/(float)m_oscillation_period));
		//float delay_t_1 = (float)(MAX_DELAY_SAMPLE_COUNT/2) + (MAX_DELAY_SAMPLE_COUNT/2*sin(((float)(m_samples_processed+m_start_delay+1)*M_PI)/(float)OSCILLATION_PERIOD));
		//printf("%f\n",delay_t);

		int delay_n = int(delay_t);
		int delay_n_1 = delay_n + 1;//= delay_t_1 > delay_t ? delay_n+1 : delay_n-1;

		int delay_n_fb = delay_n;
		int delay_n_fb_1 = delay_n_fb + 1;
		float ita = delay_t - (float)delay_n;
		//int delay_n_1 = delay_n+1;

		delay_n = delay_n <= iwr ? iwr - delay_n : MAX_DELAY_SAMPLE_COUNT + iwr - delay_n;
		delay_n_1 = delay_n_1 <= iwr ? iwr - delay_n_1 : MAX_DELAY_SAMPLE_COUNT + iwr - delay_n_1;
		delay_n_fb = delay_n_fb <= iwr_fb ? iwr_fb - delay_n_fb : MAX_DELAY_SAMPLE_COUNT + iwr_fb - delay_n_fb;
		delay_n_fb_1 = delay_n_fb_1 <= iwr_fb ? iwr_fb - delay_n_fb_1 : MAX_DELAY_SAMPLE_COUNT + iwr_fb - delay_n_fb_1;
		//delay_n_1 = delay_n_1 <= m_iwr ? m_iwr - delay_n_1 : MAX_DELAY_SAMPLE_COUNT+1 + m_iwr - delay_n_1;
		//out_samples[i] = in_samples[i] + ((float)delay_n + 1.0f - delay_t)*m_circle_buffer[delay_n]+(delay_t-(float)delay_n)*m_circle_buffer[delay_n_1];
		//m_circle_buffer_fb[m_iwr_fb] = (m_circle_buffer[delay_n] + ita*(m_circle_buffer[delay_n_1]-m_circle_buffer[delay_n])) 
		//	+ gfb*(m_circle_buffer_fb[delay_n_fb]+ita*(m_circle_buffer_fb[delay_n_fb_1]-m_circle_buffer_fb[delay_n_fb]));

		//out_samples[i] = in_samples[i] + gff * m_circle_buffer_fb[m_iwr_fb];
		out_samples[i] = in_samples[i] + gff*(circle_buffer[delay_n] + ita*(circle_buffer[delay_n_1]-circle_buffer[delay_n])) + gfb*(circle_buffer_fb[delay_n_fb]+ita*(circle_buffer_fb[delay_n_fb_1]-circle_buffer_fb[delay_n_fb]));
		out_samples[i] = 0.6f*out_samples[i];
		samples_processed++;
		circle_buffer[iwr] = in_samples[i];
		iwr++;
		circle_buffer_fb[iwr_fb] = out_samples[i];
		iwr_fb++;
		iwr = iwr >= MAX_DELAY_SAMPLE_COUNT ? 0 : iwr;
		iwr_fb = iwr_fb >= MAX_DELAY_SAMPLE_COUNT ? 0 : iwr_fb;

	}
	return sample_count;
}

void CSlowFlanging::UnInit()
{
	//m_delayed_samples.clear();
	if (m_circle_buffer_l != NULL)
	{
		delete[] m_circle_buffer_l;
		m_circle_buffer_l = NULL;
	}
	if (m_circle_buffer_fb_l != NULL)
	{
		delete[] m_circle_buffer_fb_l;
		m_circle_buffer_fb_l = NULL;
	}
	if (m_circle_buffer_r != NULL)
	{
		delete[] m_circle_buffer_r;
		m_circle_buffer_r = NULL;
	}
	if (m_circle_buffer_fb_r != NULL)
	{
		delete[] m_circle_buffer_fb_r;
		m_circle_buffer_fb_r = NULL;
	}
}