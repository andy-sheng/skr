// Reverb model declaration
//
// Written by Jezar at Dreampoint, June 2000
// http://www.dreampoint.co.uk
// This code is public domain

#ifndef _revmodel_
#define _revmodel_

#include "comb.h"
#include "allpass.h"
#include "tuning.h"

#define MAX_VERB_ID_NUMBER	10	// fixed verb id number

class revmodel
{
public:
					revmodel();
					~revmodel();
			void	mute();
			void	processmix(float *inputL, float *inputR, float *outputL, float *outputR, long numsamples, int skip);
			void	processreplace(float *inputL, float *inputR, float *outputL, float *outputR, long numsamples, int skip);
			void	processMono	(float* inputL,float* outputL, long numsamples,int skip);
			void	setroomsize(float value);
			float	getroomsize();
			void	setdamp(float value);
			float	getdamp();
			void	setwet(float value);
			float	getwet();
			void	setdry(float value);
			float	getdry();
			void	setwidth(float value);
			float	getwidth();
			void	setmode(float value);
			float	getmode();
			int		setverbID(int iID);
private:
			void	update();
			int		mallocBuffers();
private:
	float	gain;
	float	roomsize,roomsize1;
	float	damp,damp1;
	float	wet,wet1,wet2;
	float	dry;
	float	width;
	float	mode;

	// The following are all declared inline 
	// to remove the need for dynamic allocation
	// with its subsequent error-checking messiness

	// Comb filters
	comb	combL[numcombs];
	comb	combR[numcombs];

	// Allpass filters
	allpass	allpassL[numallpasses];
	allpass	allpassR[numallpasses];

	// Buffers for the combs
	//float	bufcombL1[combtuningL1];
	//float	bufcombR1[combtuningR1];
	//float	bufcombL2[combtuningL2];
	//float	bufcombR2[combtuningR2];
	//float	bufcombL3[combtuningL3];
	//float	bufcombR3[combtuningR3];
	//float	bufcombL4[combtuningL4];
	//float	bufcombR4[combtuningR4];
	//float	bufcombL5[combtuningL5];
	//float	bufcombR5[combtuningR5];
	//float	bufcombL6[combtuningL6];
	//float	bufcombR6[combtuningR6];
	//float	bufcombL7[combtuningL7];
	//float	bufcombR7[combtuningR7];
	//float	bufcombL8[combtuningL8];
	//float	bufcombR8[combtuningR8];

	//// Buffers for the allpasses
	//float	bufallpassL1[allpasstuningL1];
	//float	bufallpassR1[allpasstuningR1];
	//float	bufallpassL2[allpasstuningL2];
	//float	bufallpassR2[allpasstuningR2];
	//float	bufallpassL3[allpasstuningL3];
	//float	bufallpassR3[allpasstuningR3];
	//float	bufallpassL4[allpasstuningL4];
	//float	bufallpassR4[allpasstuningR4];

	float*	bufcombL1;
	float*	bufcombR1;
	float*	bufcombL2;
	float*	bufcombR2;
	float*	bufcombL3;
	float*	bufcombR3;
	float*	bufcombL4;
	float*	bufcombR4;
	float*	bufcombL5;
	float*	bufcombR5;
	float*	bufcombL6;
	float*	bufcombR6;
	float*	bufcombL7;
	float*	bufcombR7;
	float*	bufcombL8;
	float*	bufcombR8;

	// Buffers for the all passes
	float*	bufallpassL1;
	float*	bufallpassR1;
	float*	bufallpassL2;
	float*	bufallpassR2;
	float*	bufallpassL3;
	float*	bufallpassR3;
	float*	bufallpassL4;
	float*	bufallpassR4;
};

#endif//_revmodel_

//ends
