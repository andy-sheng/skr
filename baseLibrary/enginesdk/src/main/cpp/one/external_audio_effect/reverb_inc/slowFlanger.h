#ifndef __SLOW_FLANGING_H_
#define __SLOW_FLANGING_H_

#include <vector>


class CSlowFlanging
{
public:
	int Init(int channel, int sample_rate);
	void Reset();
	void UnInit();
	int Process(const char* in_buffer, char* out_buffer, int size);
	int Process(const float* in_buffer, float* out_buffer, int size);

	int ProcessLRIndependent(float * inLeft, float * inRight, float * outLeft, float * outRight, int inOutSize);
private:
	int ProcessSample(const float* in_samples, float* out_samples, int sample_count, unsigned int &samples_processed,
		float *circle_buffer, int &iwr, float *circle_buffer_fb, int &iwr_fb);

private:
	unsigned int m_samples_processed_left;
	unsigned int m_samples_processed_right;
	//float m_oscillation_period;
	int m_oscillation_period;
	//std::vector<float> m_delayed_samples;

	float * m_circle_buffer_l;
	float * m_circle_buffer_fb_l;
	int m_iwr_l;
	int m_iwr_fb_l;

	float * m_circle_buffer_r;
	float * m_circle_buffer_fb_r;
	int m_iwr_r;
	int m_iwr_fb_r;
	int m_channel;
	int m_sample_rate;
};
#endif
