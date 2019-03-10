//PKFilter.cpp
#define _USE_MATH_DEFINES

#include "reverb_inc/BiquadFilter.h"
#include <cmath>

using std::vector;

#ifndef _MSC_VER

static float log2(float x)
{
	return log(x) / log(2.0);
}

#endif // _MSC_VER


namespace BiquadFilter{
	PKFilter::PKFilter(float low_edge, float high_edge, float gain){
		this->low_edge = low_edge;
		this->high_edge = high_edge;
		this->gain = gain;

		alloc();

		//init filter coefficient
		float bw = log2(high_edge / low_edge);
		float cutoff = low_edge * pow(2, bw / 2);
		float omega = 2.0 * M_PI* cutoff;
		float alpha = sin(omega) * sinh(log(2.0)) / 2.0 * bw * omega / sin(omega);
		float A = pow(10.0, (gain/40.0));

		a[0] = 1.0 + alpha / A;
		a[1] = -2.0 * cos(omega);
		a[2] = 1.0 - alpha / A;
		b[0] = 1.0 + alpha * A;
		b[1] = -2.0 * cos(omega);
		b[2] = 1.0 - alpha * A;
	}
}