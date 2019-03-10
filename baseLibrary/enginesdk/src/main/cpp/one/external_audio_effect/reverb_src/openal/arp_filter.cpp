
#include <math.h>
#include <stdlib.h>
#include "arp_filter.h"
#include "arp_fixed_generic.h"

#ifndef ARP_MAX_
#define ARP_MAX_
#define ARP_MAX(a, b)	((a) > (b) ? (a) : (b))
#endif /* ARP_MAX_ */

//================================================================== Arp_Filter

Arp_Filter::Arp_Filter()
	:m_x0(0)
	,m_x1(0)
	,m_y0(0)
	,m_y1(0)
{

}

Arp_Filter::~Arp_Filter()
{

}

void Arp_Filter::SetParam( int32_t type, float gain, float freq_mult )
{
	float alpha;
	float w0 = ARP_2PI * freq_mult;
	float a0 = 0.0f;
	float a1 = 0.0f;
	float a2 = 0.0f;
	float b0 = 0.0f;
	float b1 = 0.0f;
	float b2 = 0.0f;

	gain = ARP_MAX(gain, 0.00001f);
//	float temp = sinf(w0);
//	float temp2 = sqrtf(2.6710856f);

	switch(type)
	{
	case ARP_FILTER_HighShelf:
		alpha = sinf(w0)/2.0f*sqrtf((gain + 1.0f/gain)*(1.0f/0.75f - 1.0f) + 2.0f);
		b0 =       gain*((gain+1.0f) + (gain-1.0f)*cosf(w0) + 2.0f*sqrtf(gain)*alpha);
		b1 = -2.0f*gain*((gain-1.0f) + (gain+1.0f)*cosf(w0));
		b2 =       gain*((gain+1.0f) + (gain-1.0f)*cosf(w0) - 2.0f*sqrtf(gain)*alpha);
		a0 =          (gain+1.0f) - (gain-1.0f)*cosf(w0) + 2.0f*sqrtf(gain)*alpha;
		a1 =  2.0f*((gain-1.0f) - (gain+1.0f)*cosf(w0));
		a2 =        (gain+1.0f) - (gain-1.0f)*cosf(w0) - 2.0f*sqrtf(gain)*alpha;
		break;
	case ARP_FILTER_LowShelf:
		alpha = sinf(w0)/2.0f*sqrtf((gain + 1.0f/gain)*(1.0f/0.75f - 1.0f) + 2.0f);
		b0 =       gain*((gain+1.0f) - (gain-1.0f)*cosf(w0) + 2.0f*sqrtf(gain)*alpha);
		b1 =  2.0f*gain*((gain-1.0f) - (gain+1.0f)*cosf(w0));
		b2 =       gain*((gain+1.0f) - (gain-1.0f)*cosf(w0) - 2.0f*sqrtf(gain)*alpha);
		a0 =          (gain+1.0f) + (gain-1.0f)*cosf(w0) + 2.0f*sqrtf(gain)*alpha;
		a1 = -2.0f*((gain-1.0f) + (gain+1.0f)*cosf(w0));
		a2 =        (gain+1.0f) + (gain-1.0f)*cosf(w0) - 2.0f*sqrtf(gain)*alpha;
		break;
	default:
		break;
	}

	m_a1 = QCONST16( a1/a0, 14);
	m_a2 = QCONST16( a2/a0, 14);
	m_b0 = QCONST16( b0/a0, 14);
	m_b1 = QCONST16( b1/a0, 14);
	m_b2 = QCONST16( b2/a0, 14);
}

void Arp_Filter::Process( short * pdata, int32_t len )
{
	short in;

	for(int32_t i = 0; i < len; ++i)
	{
		in = pdata[i];
		// pdata[i] = m_b0*in + m_b1*m_x0 + m_b2*m_x1 - m_a1*m_y0 - m_a2*m_y1;
		pdata[i] = MULT16_16_P14(m_b0,in)+MULT16_16_P14(m_b1,m_x0)+MULT16_16_P14(m_b2,m_x1)-MULT16_16_P14(m_a1,m_y0)-MULT16_16_P14(m_a2, m_y1);
		m_x1 = m_x0;
		m_x0 = in;
		m_y1 = m_y0;
		m_y0 = pdata[i];
	}
}

void Arp_Filter::FilterClear()
{
	m_x0 = 0.0f;
	m_x1 = 0.0f;
	m_y0 = 0.0f;
	m_y1 = 0.0f;
}

float Arp_Filter::ProcessSingle( float sample )
{
	float outsmp =	m_b0 * sample +
					m_b1 * m_x0	+
					m_b2 * m_x1 -
					m_a1 * m_y0 -
					m_a2 * m_y1;
	m_x1 = m_x0;
	m_x0 = sample;
	m_y1 = m_y0;
	m_y0 = outsmp;

	return outsmp;
}


//================================================================== Arp_Mod

Arp_Mod::Arp_Mod()
	:m_Index(0)
	,m_Range(1)
	,m_Depth(0)
	,m_Coeff(0.0f)
	,m_Filter(0.0f)
{
	m_Delay.Mask = 0;
	m_Delay.Line = NULL;
}

void Arp_Mod::UpdateModulator( float modTime, float modDepth, uint frequency )
{
	uint range;
    range = maxu(fastf2u(modTime*frequency), 1);
    m_Index = (uint)(m_Index * (uint64)range / m_Range);
    m_Range = range;
    m_Depth = modDepth * MODULATION_DEPTH_COEFF * modTime / 2.0f / 2.0f * frequency;
}


float Arp_Mod::EAXModulation( float in, uint arp_offset)
{
	float sinus, frac;
	uint offset;
	float out0, out1;

	sinus = 1.0f - cosf(F_2PI *m_Index /m_Range);
	m_Filter = lerp(m_Filter,m_Depth,m_Coeff);

	// Calculate the read offset and fraction between it and the next sample.
	frac   = (1.0f + (m_Filter * sinus));
	offset = fastf2u(frac);
	frac  -= offset;

	// Get the two samples crossed by the offset, and feed the delay line with the next input sample.
	out0 = DelayLineOut(&m_Delay, arp_offset - offset);
	out1 = DelayLineOut(&m_Delay, arp_offset - offset - 1);
	DelayLineIn(&m_Delay, arp_offset, in);

	// Step the modulation index forward, keeping it bound to its range.
	m_Index = (m_Index + 1) % m_Range;

	// The output is obtained by linearly interpolating the two samples that were acquired above.
	return lerp(out0, out1, frac);
}


void Arp_Mod::EAXModulation_set( short * pdata, int32_t len, uint arp_offset )
{
//	float sinus, frac;
//	uint offset;
//	int16_t out1;
	int16_t out0;
	uint temp_offset = arp_offset;

	//for (int32_t i = 0; i < len; i++)
	//{
	//	sinus = 1.0f - cosf(F_2PI *m_Index /m_Range);
	//	m_Filter = lerp(m_Filter,m_Depth,m_Coeff);

	//	// Calculate the read offset and fraction between it and the next sample.
	//	frac   = (1.0f + (m_Filter * sinus));
	//	offset = fastf2u(frac);
	//	frac  -= offset;

	//	// Get the two samples crossed by the offset, and feed the delay line with the next input sample.
	//	out0 = DelayLineOut(&m_Delay, temp_offset - offset);
	//	out1 = DelayLineOut(&m_Delay, temp_offset - offset - 1);
	//	DelayLineIn(&m_Delay, temp_offset, pdata[i]);

	//	// Step the modulation index forward, keeping it bound to its range.
	//	m_Index = (m_Index + 1) % m_Range;

	//	// The output is obtained by linearly interpolating the two samples that were acquired above.
	//	pdata[i] = lerp(out0, out1, frac);
	//	temp_offset++;
	//}

	for (int32_t i = 0; i < len; i++)
	{
		out0 = DelayLineOut(&m_Delay, temp_offset - 1);
		DelayLineIn(&m_Delay, temp_offset, pdata[i]);
		pdata[i] = out0;
		temp_offset++;
	}


	//int32_t sinus, frac;
	//uint offset;
	//int16_t out0, out1;
	//uint temp_offset = arp_offset;

	//for (int32_t i = 0; i < len; i++)
	//{
	//	sinus = 32768 - F_cos_Q17(div_s(m_Index, m_Range, 16));
	//	m_Filter = lerp(m_Filter,m_Depth,m_Coeff);

	//	// Calculate the read offset and fraction between it and the next sample.
	//	frac   = (1.0f + (m_Filter * sinus));
	//	offset = fastf2u(frac);
	//	frac  -= offset;

	//	// Get the two samples crossed by the offset, and feed the delay line with the next input sample.
	//	out0 = DelayLineOut(&m_Delay, temp_offset - offset);
	//	out1 = DelayLineOut(&m_Delay, temp_offset - offset - 1);
	//	DelayLineIn(&m_Delay, temp_offset, pdata[i]);

	//	// Step the modulation index forward, keeping it bound to its range.
	//	m_Index = (m_Index + 1) % m_Range;

	//	// The output is obtained by linearly interpolating the two samples that were acquired above.
	//	pdata[i] = lerp(out0, out1, frac);
	//	temp_offset++;
	//}
}


//================================================================== Arp_Early

Arp_Early::Arp_Early()
	:m_Gain(0.0f)
{
	uint index = 0;
	for(index = 0;index < 4;index++)
	{
		m_Coeff[index] = 0.0f;
		m_Delay[index].Mask = 0;
		m_Delay[index].Line = NULL;
		m_Offset[index] = 0;
	}
	for(index = 0;index < MaxChannels;index++)
	{
		m_PanGain[index] = 0.0f;
	}
}

void Arp_Early::UpdateEarlyLines( float reverbGain, float earlyGain, float lateDelay )
{
	uint index;

	// Calculate the early reflections gain (from the master effect gain, and
	// reflections gain parameters) with a constant attenuation of 0.5.
	m_Gain = QCONST16 (.5f * reverbGain * earlyGain, 15);

	// Calculate the gain (coefficient) for each early delay line using the
	// late delay time.  This expands the early reflections to the start of
	// the late reverb.
	for(index = 0;index < 4;index++)
	{
		m_Coeff[index] = QCONST16(CalcDecayCoeff(EARLY_LINE_LENGTH[index],lateDelay), 15);
	}
}

void Arp_Early::EarlyReflection( int16_t in, int16_t *out, uint arp_offset )
{
	int16_t d[4], v, f[4];

    // Obtain the decayed results of each early delay line.
    d[0] = AttenuatedDelayLineOut(&(m_Delay[0]), arp_offset - m_Offset[0], m_Coeff[0]);
    d[1] = AttenuatedDelayLineOut(&(m_Delay[1]), arp_offset - m_Offset[1], m_Coeff[1]);
    d[2] = AttenuatedDelayLineOut(&(m_Delay[2]), arp_offset - m_Offset[2], m_Coeff[2]);
    d[3] = AttenuatedDelayLineOut(&(m_Delay[3]), arp_offset - m_Offset[3], m_Coeff[3]);

    v = (d[0] + d[1] + d[2] + d[3]) * 0.5f;
    // The junction is loaded with the input here.
    v += in;

    // Calculate the feed values for the delay lines.
    f[0] = v - d[0];
    f[1] = v - d[1];
    f[2] = v - d[2];
    f[3] = v - d[3];

    // Re-feed the delay lines.
    DelayLineIn(&m_Delay[0], arp_offset, f[0]);
    DelayLineIn(&m_Delay[1], arp_offset, f[1]);
    DelayLineIn(&m_Delay[2], arp_offset, f[2]);
    DelayLineIn(&m_Delay[3], arp_offset, f[3]);

    // Output the results of the junction for all four channels.
    out[0] = m_Gain * f[0];
    out[1] = m_Gain * f[1];
    out[2] = m_Gain * f[2];
    out[3] = m_Gain * f[3];
}

void Arp_Early::EarlyReflection_set( int16_t *pdata, int16_t (*out)[4], int32_t len, uint arp_offset )
{
	int16_t d[4],  f[4];
	int32_t v;
	uint temp_offset = arp_offset;
	for (int32_t i = 0; i < len; i++)
	{
		d[0] = AttenuatedDelayLineOut(&(m_Delay[0]), temp_offset - m_Offset[0], m_Coeff[0]);
		d[1] = AttenuatedDelayLineOut(&(m_Delay[1]), temp_offset - m_Offset[1], m_Coeff[1]);
		d[2] = AttenuatedDelayLineOut(&(m_Delay[2]), temp_offset - m_Offset[2], m_Coeff[2]);
		d[3] = AttenuatedDelayLineOut(&(m_Delay[3]), temp_offset - m_Offset[3], m_Coeff[3]);

		v = (d[0] + d[1] + d[2] + d[3]) >> 1;
		// The junction is loaded with the input here.
		v += pdata[i];

		f[0] = v - d[0];
		f[1] = v - d[1];
		f[2] = v - d[2];
		f[3] = v - d[3];

		// Re-feed the delay lines.
		DelayLineIn(&m_Delay[0], temp_offset, f[0]);
		DelayLineIn(&m_Delay[1], temp_offset, f[1]);
		DelayLineIn(&m_Delay[2], temp_offset, f[2]);
		DelayLineIn(&m_Delay[3], temp_offset, f[3]);

		out[i][0] = MULT16_16_P15 (m_Gain, f[0]);
		out[i][1] = MULT16_16_P15 (m_Gain, f[1]);
		out[i][2] = MULT16_16_P15 (m_Gain, f[2]);
		out[i][3] = MULT16_16_P15 (m_Gain, f[3]);



		temp_offset++;
	}
}

//======================================================================= Arp_Late

Arp_Late::Arp_Late()
{
	uint index= 0;
	m_Gain = 0.0f;
	m_DensityGain = 0.0f;
	m_ApFeedCoeff = 0.0f;
	m_MixCoeff = 0.0f;
	for(index = 0;index < 4;index++)
	{
		m_ApCoeff[index] = 0.0f;
		m_ApDelay[index].Mask = 0;
		m_ApDelay[index].Line = NULL;
		m_ApOffset[index] = 0;

		m_Coeff[index] = 0.0f;
		m_Delay[index].Mask = 0;
		m_Delay[index].Line = NULL;
		m_Offset[index] = 0;

		m_LpCoeff[index] = 0.0f;
		m_LpSample[index] = 0.0f;
	}
	for(index = 0;index < MaxChannels;index++)
	{
		m_PanGain[index] = 0.0f;
	}
}

void Arp_Late::UpdateLateLines( float reverbGain, float lateGain, float xMix, float density, float decayTime, float diffusion, float hfRatio, float cw, uint frequency )
{
	float length;
    uint index;

	float aCoeffTemp;
    m_Gain = QCONST16(reverbGain * lateGain * xMix, 15);


    length = (LATE_LINE_LENGTH[0] + LATE_LINE_LENGTH[1] + LATE_LINE_LENGTH[2] + LATE_LINE_LENGTH[3]) / 4.0f;
    length *= 1.0f + (density * LATE_LINE_MULTIPLIER);
    m_DensityGain = QCONST16( CalcDensityGain(CalcDecayCoeff(length, decayTime)), 15);

    // Calculate the all-pass feed-back and feed-forward coefficient.
    m_ApFeedCoeff = QCONST16( 0.5f * powf(diffusion, 2.0f), 15);

	/* 需要定标的值m_ApCoeff，m_LpCoeff，m_Coeff 在ID为0~5时，定标前的系数都在 (0.0,1.0f)之间 */
    for(index = 0;index < 4;index++)
    {
        // Calculate the gain (coefficient) for each all-pass line.
        m_ApCoeff[index] = QCONST16(CalcDecayCoeff(ALLPASS_LINE_LENGTH[index],decayTime), 15);

        // Calculate the length (in seconds) of each cyclical delay line.
        length = LATE_LINE_LENGTH[index] * (1.0f + (density * LATE_LINE_MULTIPLIER));

        // Calculate the delay offset for each cyclical delay line.
        m_Offset[index] = fastf2u(length * frequency);

        // Calculate the gain (coefficient) for each cyclical line.
        aCoeffTemp = CalcDecayCoeff(length, decayTime);

        // Calculate the damping coefficient for each low-pass filter.
        m_LpCoeff[index] = QCONST16( CalcDampingCoeff(hfRatio, length, decayTime, aCoeffTemp, cw), 15);

        // Attenuate the cyclical line coefficients by the mixing coefficient(x).
        m_Coeff[index] = QCONST16( aCoeffTemp * xMix, 15);
    }
}

// Low-pass filter input/output routine for late reverb.
inline int16_t LateLowPassInOut(Arp_Late * m_oLate, uint index, int16_t in)
{
	in = lerp(in, m_oLate->m_LpSample[index], m_oLate->m_LpCoeff[index]);
	m_oLate->m_LpSample[index] = in;
	return in;
}

// Delay line output routine for late reverb.
inline int16_t LateDelayLineOut(Arp_Late * m_oLate, uint index, uint arp_offset)
{
	return AttenuatedDelayLineOut(&m_oLate->m_Delay[index],
								   arp_offset - m_oLate->m_Offset[index],
								   m_oLate->m_Coeff[index]);
}

// All-pass input/output routine for late reverb.
inline int16_t LateAllPassInOut(Arp_Late * m_oLate, uint index, int16_t in,uint arp_offset)
{
	return AllpassInOut(&m_oLate->m_ApDelay[index],
						arp_offset - m_oLate->m_ApOffset[index],
						arp_offset,
						in,
						m_oLate->m_ApFeedCoeff,
						m_oLate->m_ApCoeff[index]);
}

void Arp_Late::LateReverb( float *in, float *out, uint arp_offset )
{
	int16_t d[4], f[4];

    d[0] = LateLowPassInOut(this, 2, in[2] + LateDelayLineOut(this, 2 ,arp_offset));
    d[1] = LateLowPassInOut(this, 0, in[0] + LateDelayLineOut(this, 0 ,arp_offset));
    d[2] = LateLowPassInOut(this, 3, in[3] + LateDelayLineOut(this, 3 ,arp_offset));
    d[3] = LateLowPassInOut(this, 1, in[1] + LateDelayLineOut(this, 1 ,arp_offset));

    d[0] = LateAllPassInOut(this, 0, d[0], arp_offset);
    d[1] = LateAllPassInOut(this, 1, d[1], arp_offset);
    d[2] = LateAllPassInOut(this, 2, d[2], arp_offset);
    d[3] = LateAllPassInOut(this, 3, d[3], arp_offset);

    f[0] = d[0] + (m_MixCoeff * (         d[1] + -d[2] + d[3]));
    f[1] = d[1] + (m_MixCoeff * (-d[0]         +  d[2] + d[3]));
    f[2] = d[2] + (m_MixCoeff * ( d[0] + -d[1]         + d[3]));
    f[3] = d[3] + (m_MixCoeff * (-d[0] + -d[1] + -d[2]       ));

    out[0] = m_Gain * f[0];
    out[1] = m_Gain * f[1];
    out[2] = m_Gain * f[2];
    out[3] = m_Gain * f[3];

    DelayLineIn(&m_Delay[0], arp_offset, f[0]);
    DelayLineIn(&m_Delay[1], arp_offset, f[1]);
    DelayLineIn(&m_Delay[2], arp_offset, f[2]);
    DelayLineIn(&m_Delay[3], arp_offset, f[3]);
}

void Arp_Late::LateReverb_set( int16_t (*in)[4], int16_t (*out)[4], int32_t len, uint arp_offset )
{
	int16_t d[4], f[4];
	uint temp_offset = arp_offset;
	for (int32_t i = 0; i < len; i++)
	{
		d[0] = LateLowPassInOut(this, 2, in[i][2] + LateDelayLineOut(this, 2 ,temp_offset));
		d[1] = LateLowPassInOut(this, 0, in[i][0] + LateDelayLineOut(this, 0 ,temp_offset));
		d[2] = LateLowPassInOut(this, 3, in[i][3] + LateDelayLineOut(this, 3 ,temp_offset));
		d[3] = LateLowPassInOut(this, 1, in[i][1] + LateDelayLineOut(this, 1 ,temp_offset));

		d[0] = LateAllPassInOut(this, 0, d[0], temp_offset);
		d[1] = LateAllPassInOut(this, 1, d[1], temp_offset);
		d[2] = LateAllPassInOut(this, 2, d[2], temp_offset);
		d[3] = LateAllPassInOut(this, 3, d[3], temp_offset);

		f[0] = d[0] + (MULT16_16_P14 ( m_MixCoeff , (         d[1] + -d[2] + d[3])));
		f[1] = d[1] + (MULT16_16_P14 ( m_MixCoeff , (-d[0]         +  d[2] + d[3])));
		f[2] = d[2] + (MULT16_16_P14 ( m_MixCoeff , ( d[0] + -d[1]         + d[3])));
		f[3] = d[3] + (MULT16_16_P14 ( m_MixCoeff , (-d[0] + -d[1] + -d[2]       )));

		out[i][0] = MULT16_16_P15 (m_Gain , f[0]);
		out[i][1] = MULT16_16_P15 (m_Gain , f[1]);
		out[i][2] = MULT16_16_P15 (m_Gain , f[2]);
		out[i][3] = MULT16_16_P15 (m_Gain , f[3]);

		DelayLineIn(&m_Delay[0], temp_offset, f[0]);
		DelayLineIn(&m_Delay[1], temp_offset, f[1]);
		DelayLineIn(&m_Delay[2], temp_offset, f[2]);
		DelayLineIn(&m_Delay[3], temp_offset, f[3]);

		temp_offset++;
	}
}

//======================================================================= Arp_Echo

Arp_Echo::Arp_Echo()
{
	m_DensityGain = 0.0f;
	m_Delay.Mask = 0;
	m_Delay.Line = NULL;
	m_ApDelay.Mask = 0;
	m_ApDelay.Line = NULL;
	m_Coeff = 0.0f;
	m_ApFeedCoeff = 0.0f;
	m_ApCoeff = 0.0f;
	m_Offset = 0;
	m_ApOffset = 0;
	m_LpCoeff = 0.0f;
	m_LpSample = 0.0f;
	m_MixCoeff[0] = 0.0f;
	m_MixCoeff[1] = 0.0f;
}


/* 效果ID0~5时，该函数内原始系数为（0~1.0]， m_MixCoeff[1]=1.0所以该值定标Q14，其他系数定标Q15 */
void Arp_Echo::UpdateEchoLine( float reverbGain, float lateGain, float echoTime, float decayTime, float diffusion, float echoDepth, float hfRatio, float cw, uint frequency )
{
	// Update the offset and coefficient for the echo delay line.
    m_Offset = fastf2u(echoTime * frequency);

    // Calculate the decay coefficient for the echo line.
    float CoeffTemp = CalcDecayCoeff(echoTime, decayTime);
	m_Coeff = QCONST16( CoeffTemp, 15);
    // Calculate the energy-based attenuation coefficient for the echo delay
	// line.
	m_DensityGain = QCONST16( CalcDensityGain(CoeffTemp), 15);

    // Calculate the echo all-pass feed coefficient.
    m_ApFeedCoeff = QCONST16( 0.5f * powf(diffusion, 2.0f), 15);

    // Calculate the echo all-pass attenuation coefficient.
    m_ApCoeff = QCONST16( CalcDecayCoeff(ECHO_ALLPASS_LENGTH, decayTime), 15);

    // Calculate the damping coefficient for each low-pass filter.
    m_LpCoeff = QCONST16( CalcDampingCoeff(hfRatio, echoTime, decayTime,CoeffTemp, cw), 15);

    /* Calculate the echo mixing coefficients.  The first is applied to the
     * echo itself.  The second is used to attenuate the late reverb when
     * echo depth is high and diffusion is low, so the echo is slightly
     * stronger than the decorrelated echos in the reverb tail.
     */
    m_MixCoeff[0] = QCONST16( reverbGain * lateGain * echoDepth , 14);
    m_MixCoeff[1] = QCONST16( 1.0f - (echoDepth * 0.5f * (1.0f - diffusion)), 14);
}

void Arp_Echo::EAXEcho( float in, float *late, uint arp_offset )
{
	float out, feed;

	// Get the latest attenuated echo sample for output.
	feed = AttenuatedDelayLineOut(&m_Delay, arp_offset - m_Offset, m_Coeff);

	// Mix the output into the late reverb channels.
	out = m_MixCoeff[0] * feed;
	late[0] = (m_MixCoeff[1] * late[0]) + out;
	late[1] = (m_MixCoeff[1] * late[1]) + out;
	late[2] = (m_MixCoeff[1] * late[2]) + out;
	late[3] = (m_MixCoeff[1] * late[3]) + out;

	// Mix the energy-attenuated input with the output and pass it through
	// the echo low-pass filter.
	feed += m_DensityGain * in;
	feed = lerp(feed, m_LpSample, m_LpCoeff);
	m_LpSample = feed;

	// Then the echo all-pass filter.
	feed = AllpassInOut(&m_ApDelay,arp_offset - m_ApOffset,arp_offset, feed, m_ApFeedCoeff, m_ApCoeff);

	// Feed the delay with the mixed and filtered sample.
	DelayLineIn(&m_Delay, arp_offset, feed);
}

void Arp_Echo::EAXEcho_set( int16_t *in, int16_t (*late)[4], int32_t len, uint arp_offset )
{
	int16_t out, feed;
	uint temp_offset = arp_offset;

	for (int32_t i = 0; i < len; i++)
	{
		feed = AttenuatedDelayLineOut(&m_Delay, temp_offset - m_Offset, m_Coeff);

		// Mix the output into the late reverb channels.
		out = MULT16_16_P14( m_MixCoeff[0] , feed);
		late[i][0] = MULT16_16_P14( m_MixCoeff[1] , late[i][0]) + out;
		late[i][1] = MULT16_16_P14( m_MixCoeff[1] , late[i][1]) + out;
		late[i][2] = MULT16_16_P14( m_MixCoeff[1] , late[i][2]) + out;
		late[i][3] = MULT16_16_P14( m_MixCoeff[1] , late[i][3]) + out;

		// Mix the energy-attenuated input with the output and pass it through
		// the echo low-pass filter.
		feed += MULT16_16_P15( m_DensityGain , in[i]);
		feed = lerp(feed, m_LpSample, m_LpCoeff);
		m_LpSample = feed;

		// Then the echo all-pass filter.
		feed = AllpassInOut(&m_ApDelay,temp_offset - m_ApOffset,temp_offset, feed, m_ApFeedCoeff, m_ApCoeff);

		// Feed the delay with the mixed and filtered sample.
		DelayLineIn(&m_Delay, temp_offset, feed);

		temp_offset++;
	}
}


//======================================================================
void Update3DPanning(Arp_Early * m_oEarly, Arp_Late * m_oLate, int m_channels)
{
	if (m_channels == 1)
	{
		m_oEarly->m_PanGain[FrontCenter]	= 1.0f;
		m_oLate->m_PanGain[FrontCenter]		= 1.0f;
	}
	if (m_channels == 2)
	{
		m_oEarly->m_PanGain[FrontLeft]	= 1.0f;
		m_oEarly->m_PanGain[FrontRight]	= 1.0f;

		m_oLate->m_PanGain[FrontLeft]	= 1.0f;
		m_oLate->m_PanGain[FrontRight]	= 1.0f;
	}
}
