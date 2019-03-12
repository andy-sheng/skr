//LSFilter.cpp
#define _USE_MATH_DEFINES

#include "reverb_inc/BiquadFilter.h"
#include <cmath>

using std::vector;

namespace BiquadFilter{
	LSFilter::LSFilter(float cutoff, float Q, float gain){
		this->cutoff = cutoff;
		this->Q = Q;
		this->gain = gain;

		alloc();

		//init filter coefficient
		float omega = 2.0 * M_PI* cutoff;
		float A = pow(10.0, (gain / 40.0));
		float beta = sqrt(A) / Q;

		a[0] = (A + 1.0) + (A - 1.0) * cos(omega) + beta * sin(omega);
		a[1] = -2.0 * ((A - 1.0) + (A + 1.0) * cos(omega));
		a[2] = (A + 1.0) + (A - 1.0) * cos(omega) - beta * sin(omega);
		b[0] = A * ((A + 1.0) - (A - 1.0) * cos(omega) + beta * sin(omega));
		b[1] = 2.0 * A * ((A - 1.0) - (A + 1.0) * cos(omega));
		b[2] = A * ((A + 1.0) - (A - 1.0) * cos(omega) - beta * sin(omega));
	}
}