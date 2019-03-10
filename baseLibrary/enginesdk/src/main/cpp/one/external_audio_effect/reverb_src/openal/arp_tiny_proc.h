#ifndef _ARP_TINY_PROC_H_
#define _ARP_TINY_PROC_H_

#include "arp_effect_const.h"

#include <math.h>				// for powf
#include "arp_fixed_generic.h"

//======================================================= fwli2
//=比较大小
inline float maxf(float a, float b)
{ return ((a > b) ? a : b); }
inline float minf(float a, float b)
{ return ((a > b) ? b : a); }
inline float clampf(float val, float min, float max)
{ return minf(max, maxf(min, val)); }
inline uint minu(uint a, uint b)
{ return ((a > b) ? b : a); }
inline uint maxu(uint a, uint b)
{ return ((a > b) ? a : b); }

//======================================================= fwli2
//=快速计算float转uint，直接舍去小数（注意与四舍五入的区别）
inline int fastf2i(float f)
{
#ifdef HAVE_LRINTF
	return lrintf(f);
#elif defined(_MSC_VER) && defined(_M_IX86)
	int i;
	__asm fld f
	__asm fistp i
	return i;
#else
	return (int)f;
#endif
}
inline uint fastf2u(float f)
{
	return fastf2i(f);
}

//======================================================= fwli2
inline float lerp(int16_t val1, int16_t val2, int16_t mu)
{
	return val1 + MULT16_16_P15((val2-val1),mu);
}

//======================================================= fwli2
//=将一个非2的n次方的数转为大于它的最小的2的n次方数，转为8等等
inline uint NextPowerOf2(uint value)
{
	if(value > 0)
	{
		value--;
		value |= value>>1;
		value |= value>>2;
		value |= value>>4;
		value |= value>>8;
		value |= value>>16;
	}
	return value+1;
}

//======================================================= fwli2
// Given the allocated sample buffer, this function updates each delay line
// offset.
inline void RealizeLineOffset(short *sampleBuffer, DelayLine *Delay)
{
	//Delay->Line = &sampleBuffer[(ALintptrEXT)Delay->Line];
	Delay->Line = &sampleBuffer[(long int)Delay->Line];
}

//计算一条延迟线的长度并保存mask和offset
inline uint CalcLineLength(float length, int offset, uint frequency, DelayLine *Delay)
{
 	uint samples;

 	// All line lengths are powers of 2, calculated from their lengths, with
 	// an additional sample in case of rounding errors.
 	samples = NextPowerOf2(fastf2u(length * frequency) + 1);
 	// All lines share a single sample buffer.
 	Delay->Mask = samples - 1;
 	Delay->Line = (short*)((int64_t)offset);
 	// Return the sample count for accumulation.
 	return samples;
 }


//======================================================= fwli2
// Basic delay line input/output routines.
static inline int DelayLineOut(DelayLine *Delay, uint offset)
{
	return Delay->Line[offset&Delay->Mask];
}

 static inline void DelayLineIn(DelayLine *Delay, uint offset, short in)
 {
 	Delay->Line[offset&Delay->Mask] = in;
 }

 // Attenuated delay line output routine.
 static inline int  AttenuatedDelayLineOut(DelayLine *Delay, uint offset, short coeff)
 {
 	// return coeff * Delay->Line[offset&Delay->Mask];
	return MULT16_16_P15(coeff, Delay->Line[offset&Delay->Mask]);
	//return MULT16_16_P15(coeff, 16384);
 }

 // Basic attenuated all-pass input/output routine.
 static inline int16_t AllpassInOut(DelayLine *Delay, uint outOffset, uint inOffset, int16_t in, int16_t feedCoeff, int16_t coeff)
 {
 	int16_t out, feed;
 	out = DelayLineOut(Delay, outOffset);
 	feed = MULT16_16_P15(feedCoeff, in);
 	DelayLineIn(Delay, inOffset, (MULT16_16_P15(feedCoeff,(out - feed)) + in));
 	return MULT16_16_P15(coeff , out) - feed;
 }

 //======================================================== fwli2
 // Calculate a decay coefficient given the length of each cycle and the time
 // until the decay reaches -60 dB.
 inline float CalcDecayCoeff(float length, float decayTime)
 {
	 return powf(0.001f/*-60 dB*/, length/decayTime);
 }

 // Calculate a decay length from a coefficient and the time until the decay
 // reaches -60 dB.
 inline float CalcDecayLength(float coeff, float decayTime)
 {
	 return log10f(coeff) * decayTime / log10f(0.001f)/*-60 dB*/;
 }

 // Calculate the mixing matrix coefficients given a diffusion factor.
 inline void CalcMatrixCoeffs(float diffusion, float *x, float *y)
 {
	 float n, t;

	 // The matrix is of order 4, so n is sqrt (4 - 1).
	 n = sqrtf(3.0f);
	 t = diffusion * atanf(n);

	 // Calculate the first mixing matrix coefficient.
	 *x = cosf(t);
	 // Calculate the second mixing matrix coefficient.
	 *y = sinf(t) / n;
 }


 // Calculate the limited HF ratio for use with the late reverb low-pass
// filters.
inline float CalcLimitedHfRatio(float hfRatio, float airAbsorptionGainHF, float decayTime)
{
    float limitRatio;

    /* Find the attenuation due to air absorption in dB (converting delay
     * time to meters using the speed of sound).  Then reversing the decay
     * equation, solve for HF ratio.  The delay length is cancelled out of
     * the equation, so it can be calculated once for all lines.
     */
    limitRatio = 1.0f / (CalcDecayLength(airAbsorptionGainHF, decayTime) *
                         SPEEDOFSOUNDMETRESPERSEC);
    /* Using the limit calculated above, apply the upper bound to the HF
     * ratio. Also need to limit the result to a minimum of 0.1, just like the
     * HF ratio parameter. */
    return clampf(limitRatio, 0.1f, hfRatio);
}


// Calculate an attenuation to be applied to the input of any echo models to
// compensate for modal density and decay time.
inline float CalcDensityGain(float a)
{
    /* The energy of a signal can be obtained by finding the area under the
     * squared signal.  This takes the form of Sum(x_n^2), where x is the
     * amplitude for the sample n.
     *
     * Decaying feedback matches exponential decay of the form Sum(a^n),
     * where a is the attenuation coefficient, and n is the sample.  The area
     * under this decay curve can be calculated as:  1 / (1 - a).
     *
     * Modifying the above equation to find the squared area under the curve
     * (for energy) yields:  1 / (1 - a^2).  Input attenuation can then be
     * calculated by inverting the square root of this approximation,
     * yielding:  1 / sqrt(1 / (1 - a^2)), simplified to: sqrt(1 - a^2).
     */
    return sqrtf(1.0f - (a * a));
}


// Calculate the coefficient for a HF (and eventually LF) decay damping filter.
inline float CalcDampingCoeff(float hfRatio, float length, float decayTime, float decayCoeff, float cw)
{
    float coeff, g;

    // Eventually this should boost the high frequencies when the ratio
    // exceeds 1.
    coeff = 0.0f;
    if (hfRatio < 1.0f)
    {
        // Calculate the low-pass coefficient by dividing the HF decay
        // coefficient by the full decay coefficient.
        g = CalcDecayCoeff(length, decayTime * hfRatio) / decayCoeff;

        // Damping is done with a 1-pole filter, so g needs to be squared.
        g *= g;
        if(g < 0.9999f) /* 1-epsilon */
        {
            /* Be careful with gains < 0.001, as that causes the coefficient
             * head towards 1, which will flatten the signal. */
            g = maxf(g, 0.001f);
            coeff = (1 - g*cw - sqrtf(2*g*(1-cw) - g*g*(1 - cw*cw))) /
                    (1 - g);
        }

        // Very low decay times will produce minimal output, so apply an
        // upper bound to the coefficient.
        coeff = minf(coeff, 0.98f);
    }
    return coeff;
}

//========================================================= fwli2


#endif//_ARP_TINY_PROC_H_
