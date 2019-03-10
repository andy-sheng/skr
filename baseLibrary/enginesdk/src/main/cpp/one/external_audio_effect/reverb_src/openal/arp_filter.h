
#ifndef _ARP_FILTER_H_
#define _ARP_FILTER_H_

#include "arp_effect_const.h"
#include "arp_tiny_proc.h"

#include <stdint.h>

class Arp_Filter
{
public:
	Arp_Filter();
	~Arp_Filter();

public:
	void FilterClear();
	void SetParam(int32_t type, float gain, float freq_mult);
	void Process(short * pdata, int32_t len);
	float ProcessSingle(float sample);

private:
	int16_t m_x0, m_x1;
	int16_t m_y0, m_y1;
	int16_t m_a1, m_a2;
	int16_t m_b0, m_b1, m_b2;
};

class Arp_Mod
{
public:
	Arp_Mod();
public:
	void UpdateModulator(float modTime, float modDepth, uint frequency);
	float EAXModulation(float in, uint arp_offset);
	void EAXModulation_set(short * pdata, int32_t len, uint arp_offset);
public:
	// Modulator delay line.
	DelayLine m_Delay;

	// The vibrato time is tracked with an index over a modulus-wrapped range (in samples).
	uint    m_Index;
	uint    m_Range;

	// The depth of frequency change (also in samples) and its filter.
	float   m_Depth;
	float   m_Coeff;
	float   m_Filter;
};

class Arp_Early
{
public:
	Arp_Early();
	void UpdateEarlyLines(float reverbGain, float earlyGain, float lateDelay);
	void EarlyReflection(int16_t in, int16_t *out, uint arp_offset);	
	void EarlyReflection_set(short *pdata, short (*out)[4], int32_t len, uint arp_offset);	
public:
	// Output gain for early reflections.
	short   m_Gain;

	// Early reflections are done with 4 delay lines.
	short   m_Coeff[4];
	DelayLine m_Delay[4];
	uint    m_Offset[4];

	// The gain for each output channel based on 3D panning (only for the
	// EAX path).
	float   m_PanGain[MaxChannels];
};

class Arp_Late {

public:
	Arp_Late();
	void UpdateLateLines(float reverbGain, float lateGain, float xMix, float density, float decayTime, float diffusion, float hfRatio, float cw, uint frequency);
	void LateReverb( float *in, float *out, uint arp_offset);
	void LateReverb_set( int16_t (*in)[4], int16_t (*out)[4], int32_t len, uint arp_offset);

public:
	// Output gain for late reverb.
	int16_t   m_Gain;
	// Attenuation to compensate for the modal density and decay rate of
	// the late lines.
	int16_t   m_DensityGain;
	// The feed-back and feed-forward all-pass coefficient.
	int16_t   m_ApFeedCoeff;
	// Mixing matrix coefficient.
	int16_t   m_MixCoeff;
	// Late reverb has 4 parallel all-pass filters.
	int16_t   m_ApCoeff[4];
	DelayLine m_ApDelay[4];
	uint    m_ApOffset[4];
	// In addition to 4 cyclical delay lines.
	int16_t   m_Coeff[4];
	DelayLine m_Delay[4];
	uint    m_Offset[4];
	// The cyclical delay lines are 1-pole low-pass filtered.
	int16_t   m_LpCoeff[4];
	int16_t   m_LpSample[4];
	// The gain for each output channel based on 3D panning (only for the
	// EAX path).
	int16_t   m_PanGain[MaxChannels];
} ;


class Arp_Echo 
{
public:
	Arp_Echo();
	void UpdateEchoLine(float reverbGain, float lateGain, float echoTime, float decayTime, float diffusion, float echoDepth, float hfRatio, float cw, uint frequency);
	void EAXEcho(float in, float *late, uint arp_offset);
	void EAXEcho_set(int16_t *in, int16_t (*late)[4], int32_t len, uint arp_offset);

public:
	// Attenuation to compensate for the modal density and decay rate of
	// the echo line.
	int16_t   m_DensityGain;

	// Echo delay and all-pass lines.
	DelayLine m_Delay;
	DelayLine m_ApDelay;

	int16_t   m_Coeff;
	int16_t   m_ApFeedCoeff;
	int16_t   m_ApCoeff;

	uint    m_Offset;
	uint    m_ApOffset;

	// The echo line is 1-pole low-pass filtered.
	int16_t   m_LpCoeff;
	int16_t   m_LpSample;

	// Echo mixing coefficients.
	int16_t   m_MixCoeff[2];
};

void Update3DPanning(Arp_Early * m_oEarly, Arp_Late * m_oLate, int32_t m_channels);

#endif /* _ARP_FILTER_H_ */