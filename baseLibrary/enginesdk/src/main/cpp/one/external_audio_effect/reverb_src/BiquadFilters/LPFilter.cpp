//LPFilter.cpp
#define _USE_MATH_DEFINES

#include "reverb_inc/BiquadFilter.h"
#include <cmath>

using std::vector;

namespace BiquadFilter{
	LPFilter::LPFilter(float cutoff, float Q){
		this->cutoff = cutoff;
		this->Q = Q;

		alloc();

		//init filter coefficient
		float omega = 2.0 * M_PI* cutoff;
		float alpha = sin(omega) / (2.0*Q);

		a[0] = 1.0 + alpha;
		a[1] = -2.0 * cos(omega);
		a[2] = 1.0 - alpha;
		b[0] = (1.0 - cos(omega)) / 2.0;
		b[1] = 1.0 - cos(omega);
		b[2] = (1.0 - cos(omega)) / 2.0;
	}
}