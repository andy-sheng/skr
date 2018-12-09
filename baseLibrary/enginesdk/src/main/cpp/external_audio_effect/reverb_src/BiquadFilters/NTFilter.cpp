//NotchFilter.cpp
#define _USE_MATH_DEFINES

#include "reverb_inc/BiquadFilter.h"
#include <cmath>

#ifndef _MSC_VER

static float log2(float x)
{
	return log(x) / log(2.0);
}

#endif // _MSC_VER


using std::vector;

namespace BiquadFilter{
	NTFilter::NTFilter(float low_edge, float high_edge){
		this->low_edge = low_edge;
		this->high_edge = high_edge;

		alloc();

		//init filter coefficient
		float bw = log2(high_edge / low_edge);
		float cutoff = low_edge * pow(2, bw / 2);
		float omega = 2.0 * M_PI* cutoff;
		float alpha = sin(omega) * sinh(log(2.0)) / 2.0 * bw * omega / sin(omega);

		a[0] = 1.0 + alpha;
		a[1] = -2.0 * cos(omega);
		a[2] = 1.0 - alpha;
		b[0] = 1.0;
		b[1] = -2.0 * cos(omega);
		b[2] = 1.0;
	}
}