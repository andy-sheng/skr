#ifndef pitch_NCC_CONTROL_H
#define pitch_NCC_CONTROL_H

#define N_FRAMELEN (160)
#include "../../filter/Filter_LIB/Filter_SDK_API.h"

#define MAXPITCHWINDOWLEN (50) //1s
#define PREPBUFLEN (6)

#ifdef __cplusplus
extern "C"
{
#endif
	typedef struct pitch_NCC_channel_memory{
		float sideoutZperiod;

		Filter_s mLP;
		Filter_s mshuzhi;
		short membuf[N_FRAMELEN*2];
		int memExternalSomein;//we may use AGC's "somein" to help ELP

		float ELPThreshold;
		float ZperiodThreshod;
	}pitch_NCC_ID;

	typedef struct AutoPitchDelta_channel_memory{
		int memprepitchbuf[PREPBUFLEN];
		int memPitchpreped[MAXPITCHWINDOWLEN];
		int memlastpitchAbandon;
		int memsumpitch;
		int memcontine;
		//float mempitchdelta;

		float mempitchdelta1;
		float mempitchdelta2;
		float mempitchdelta3;
		float mempitchdelta4;
		float mempitchdelta5;
		float mempitchdelta6;

		//int memfirstset;
		float mempitchavg;//

		unsigned int memtotalpitchframe;
		float mempitchavg_g;//sumpitch/sumpitchframe

		int memmanwatch;
		int memwomanwatch;
		int memmidwatch;

		//float firstpitchdelta;
		int giveup_behind;
		int giveup_front;
		//int bigerthanfrontpitch_g;
		//int smallerthanfrontpitch_g;
		//int bigerthanbehindpitch_g;
		//int smallerthanbehindpitch_g;

		int pitchwindowlen;
		int contine;
		int bigerthanfrontpitch;
		int smallerthanfrontpitch;
		int bigerthanbehindpitch;
		int smallerthanbehindpitch;

		int soundmod;

	}APD_ID;

#ifdef __cplusplus
}
#endif

#endif