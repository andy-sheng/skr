#ifndef SKR_EFFECT_CONTROL_H
#define SKR_EFFECT_CONTROL_H

#include <stdio.h>
#include "common/defines.h"


#ifdef __cplusplus
extern "C"
{
#endif

#define MAX_DELAYMS (280)
#define MAXORDER_DELAY (MAX_DELAYMS*48)

	typedef struct Echo_memory{
		short meminputL[MAXORDER_DELAY+SKR_MAX_FRAME_SAMPLE_MONO];
		float memoutputL[MAXORDER_DELAY+SKR_MAX_FRAME_SAMPLE_MONO];
		short meminputR[MAXORDER_DELAY+SKR_MAX_FRAME_SAMPLE_MONO];
		float memoutputR[MAXORDER_DELAY+SKR_MAX_FRAME_SAMPLE_MONO];

		//float aD;
		//float bD;
		int Echo_D;

		int channel;
		int samplerate;
		float Echo_a;
		float Echo_g;
		float Echo_b;
		float Echo_D_ms;
		float Echo_depth_ms;
		float Echo_freqHz;
	}Echo_s;


#ifdef __cplusplus
}
#endif


#endif
