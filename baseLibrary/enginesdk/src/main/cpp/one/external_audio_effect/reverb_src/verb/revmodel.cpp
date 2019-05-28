// Reverb model implementation
//
// Written by Jezar at Dreampoint, June 2000
// http://www.dreampoint.co.uk
// This code is public domain

#include "revmodel.h"
#include "reverb_inc/MSdcommon.h"

revmodel::revmodel()
{
	//
	mallocBuffers();

	// Tie the components to their buffers
	combL[0].setbuffer(bufcombL1,combtuningL1);
	combR[0].setbuffer(bufcombR1,combtuningR1);
	combL[1].setbuffer(bufcombL2,combtuningL2);
	combR[1].setbuffer(bufcombR2,combtuningR2);
	combL[2].setbuffer(bufcombL3,combtuningL3);
	combR[2].setbuffer(bufcombR3,combtuningR3);
	combL[3].setbuffer(bufcombL4,combtuningL4);
	combR[3].setbuffer(bufcombR4,combtuningR4);
	combL[4].setbuffer(bufcombL5,combtuningL5);
	combR[4].setbuffer(bufcombR5,combtuningR5);
	combL[5].setbuffer(bufcombL6,combtuningL6);
	combR[5].setbuffer(bufcombR6,combtuningR6);
	combL[6].setbuffer(bufcombL7,combtuningL7);
	combR[6].setbuffer(bufcombR7,combtuningR7);
	combL[7].setbuffer(bufcombL8,combtuningL8);
	combR[7].setbuffer(bufcombR8,combtuningR8);
	allpassL[0].setbuffer(bufallpassL1,allpasstuningL1);
	allpassR[0].setbuffer(bufallpassR1,allpasstuningR1);
	allpassL[1].setbuffer(bufallpassL2,allpasstuningL2);
	allpassR[1].setbuffer(bufallpassR2,allpasstuningR2);
	allpassL[2].setbuffer(bufallpassL3,allpasstuningL3);
	allpassR[2].setbuffer(bufallpassR3,allpasstuningR3);
	allpassL[3].setbuffer(bufallpassL4,allpasstuningL4);
	allpassR[3].setbuffer(bufallpassR4,allpasstuningR4);

	// Set default values
	allpassL[0].setfeedback(0.5f);
	allpassR[0].setfeedback(0.5f);
	allpassL[1].setfeedback(0.5f);
	allpassR[1].setfeedback(0.5f);
	allpassL[2].setfeedback(0.5f);
	allpassR[2].setfeedback(0.5f);
	allpassL[3].setfeedback(0.5f);
	allpassR[3].setfeedback(0.5f);
	setwet(initialwet);
	setroomsize(initialroom);
	setdry(initialdry);
	setdamp(initialdamp);
	setwidth(initialwidth);
	setmode(initialmode);

	// Buffer will be full of rubbish - so we MUST mute them
	mute();
}

revmodel::~revmodel()
{
	safe_free(bufallpassL1);
	safe_free(bufallpassL2);
	safe_free(bufallpassL3);
	safe_free(bufallpassL4);
	safe_free(bufallpassR1);
	safe_free(bufallpassR2);
	safe_free(bufallpassR3);
	safe_free(bufallpassR4);

	safe_free(bufcombL1);
	safe_free(bufcombL2);
	safe_free(bufcombL3);
	safe_free(bufcombL4);
	safe_free(bufcombL5);
	safe_free(bufcombL6);
	safe_free(bufcombL7);
	safe_free(bufcombL8);

	safe_free(bufcombR1);
	safe_free(bufcombR2);
	safe_free(bufcombR3);
	safe_free(bufcombR4);
	safe_free(bufcombR5);
	safe_free(bufcombR6);
	safe_free(bufcombR7);
	safe_free(bufcombR8);

}

int revmodel::mallocBuffers()
{
	//int ires; 

	bufallpassL1 = NULL;
	bufallpassL2 = NULL;
	bufallpassL3 = NULL;
	bufallpassL4 = NULL;
	bufallpassR1 = NULL;
	bufallpassR2 = NULL;
	bufallpassR3 = NULL;
	bufallpassR4 = NULL;

	bufcombL1 = NULL;
	bufcombL2 = NULL;
	bufcombL3 = NULL;
	bufcombL4 = NULL;
	bufcombL5 = NULL;
	bufcombL6 = NULL;
	bufcombL7 = NULL;
	bufcombL8 = NULL;

	bufcombR1 = NULL;
	bufcombR2 = NULL;
	bufcombR3 = NULL;
	bufcombR4 = NULL;
	bufcombR5 = NULL;
	bufcombR6 = NULL;
	bufcombR7 = NULL;
	bufcombR8 = NULL;

	bufcombL1 = (float*)malloc(combtuningL1*sizeof(float));
	bufcombL2 = (float*)malloc(combtuningL2*sizeof(float));
	bufcombL3 = (float*)malloc(combtuningL3*sizeof(float));
	bufcombL4 = (float*)malloc(combtuningL4*sizeof(float));
	bufcombL5 = (float*)malloc(combtuningL5*sizeof(float));
	bufcombL6 = (float*)malloc(combtuningL6*sizeof(float));
	bufcombL7 = (float*)malloc(combtuningL7*sizeof(float));
	bufcombL8 = (float*)malloc(combtuningL8*sizeof(float));
	
	bufcombR1 = (float*)malloc(combtuningR1*sizeof(float));
	bufcombR2 = (float*)malloc(combtuningR2*sizeof(float));
	bufcombR3 = (float*)malloc(combtuningR3*sizeof(float));
	bufcombR4 = (float*)malloc(combtuningR4*sizeof(float));
	bufcombR5 = (float*)malloc(combtuningR5*sizeof(float));
	bufcombR6 = (float*)malloc(combtuningR6*sizeof(float));
	bufcombR7 = (float*)malloc(combtuningR7*sizeof(float));
	bufcombR8 = (float*)malloc(combtuningR8*sizeof(float));

	bufallpassL1 = (float*)malloc(allpasstuningL1*sizeof(float));
	bufallpassL2 = (float*)malloc(allpasstuningL2*sizeof(float));
	bufallpassL3 = (float*)malloc(allpasstuningL3*sizeof(float));
	bufallpassL4 = (float*)malloc(allpasstuningL4*sizeof(float));
	bufallpassR1 = (float*)malloc(allpasstuningR1*sizeof(float));
	bufallpassR2 = (float*)malloc(allpasstuningR2*sizeof(float));
	bufallpassR3 = (float*)malloc(allpasstuningR3*sizeof(float));
	bufallpassR4 = (float*)malloc(allpasstuningR4*sizeof(float));

	memset(bufcombL1,0,combtuningL1*sizeof(float));
	memset(bufcombL2,0,combtuningL2*sizeof(float));
	memset(bufcombL3,0,combtuningL3*sizeof(float));
	memset(bufcombL4,0,combtuningL4*sizeof(float));
	memset(bufcombL5,0,combtuningL5*sizeof(float));
	memset(bufcombL6,0,combtuningL6*sizeof(float));
	memset(bufcombL7,0,combtuningL7*sizeof(float));
	memset(bufcombL8,0,combtuningL8*sizeof(float));
	memset(bufcombR1,0,combtuningR1*sizeof(float));
	memset(bufcombR2,0,combtuningR2*sizeof(float));
	memset(bufcombR3,0,combtuningR3*sizeof(float));
	memset(bufcombR4,0,combtuningR4*sizeof(float));
	memset(bufcombR5,0,combtuningR5*sizeof(float));
	memset(bufcombR6,0,combtuningR6*sizeof(float));
	memset(bufcombR7,0,combtuningR7*sizeof(float));
	memset(bufcombR8,0,combtuningR8*sizeof(float));

	memset(bufallpassL1,0,allpasstuningL1*sizeof(float));
	memset(bufallpassL2,0,allpasstuningL2*sizeof(float));
	memset(bufallpassL3,0,allpasstuningL3*sizeof(float));
	memset(bufallpassL4,0,allpasstuningL4*sizeof(float));
	memset(bufallpassR1,0,allpasstuningR1*sizeof(float));
	memset(bufallpassR2,0,allpasstuningR2*sizeof(float));
	memset(bufallpassR3,0,allpasstuningR3*sizeof(float));
	memset(bufallpassR4,0,allpasstuningR4*sizeof(float));

	return 0;


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
}

void revmodel::mute()
{
	int i;
	if (getmode() >= freezemode)
		return;

	for (i=0;i<numcombs;i++)
	{
		combL[i].mute();
		combR[i].mute();
	}
	for (i=0;i<numallpasses;i++)
	{
		allpassL[i].mute();
		allpassR[i].mute();
	}
}

void revmodel::processMono(float* inputL,float* outputL, long numsamples,int skip)
{
	float outL,input;

	while(numsamples-- > 0)
	{
		int i;
		outL = 0;
		input = (*inputL + *inputL) * gain;

		for(i=0; i<numcombs; i++)
		{
			outL += combL[i].process(input);
		}

		for(i=0; i<numallpasses; i++)
		{
			outL = allpassL[i].process(outL);
		}

		*outputL = outL*wet1 + outL*wet2 + *inputL*dry;
			
		inputL += skip;
		outputL += skip;
	}
}

void revmodel::processreplace(float *inputL, float *inputR, float *outputL, float *outputR, long numsamples, int skip)
{
	float outL,outR,input;

	while(numsamples-- > 0)
	{
		int i;
		outL = outR = 0;
		input = (*inputL + *inputR) * gain;

		// Accumulate comb filters in parallel
		for(i=0; i<numcombs; i++)
		{
			outL += combL[i].process(input);
			outR += combR[i].process(input);
		}

		// Feed through allpasses in series
		for(i=0; i<numallpasses; i++)
		{
			outL = allpassL[i].process(outL);
			outR = allpassR[i].process(outR);
		}

		// Calculate output REPLACING anything already there
		*outputL = outL*wet1 + outR*wet2 + *inputL*dry;
		*outputR = outR*wet1 + outL*wet2 + *inputR*dry;

		// Increment sample pointers, allowing for interleave (if any)
		inputL += skip;
		inputR += skip;
		outputL += skip;
		outputR += skip;
	}
}

void revmodel::processmix(float *inputL, float *inputR, float *outputL, float *outputR, long numsamples, int skip)
{
	float outL,outR,input;

	while(numsamples-- > 0)
	{
		int i;
		outL = outR = 0;
		input = (*inputL + *inputR) * gain;

		// Accumulate comb filters in parallel
		for(i=0; i<numcombs; i++)
		{
			outL += combL[i].process(input);
			outR += combR[i].process(input);
		}

		// Feed through allpasses in series
		for(i=0; i<numallpasses; i++)
		{
			outL = allpassL[i].process(outL);
			outR = allpassR[i].process(outR);
		}

		// Calculate output MIXING with anything already there
		*outputL += outL*wet1 + outR*wet2 + *inputL*dry;
		*outputR += outR*wet1 + outL*wet2 + *inputR*dry;

		// Increment sample pointers, allowing for interleave (if any)
		inputL += skip;
		inputR += skip;
		outputL += skip;
		outputR += skip;
	}
}

void revmodel::update()
{
// Recalculate internal values after parameter change

	int i;

	wet1 = wet*(width/2 + 0.5f);
	wet2 = wet*((1-width)/2);

	if (mode >= freezemode)
	{
		roomsize1 = 1;
		damp1 = 0;
		gain = muted;
	}
	else
	{
		roomsize1 = roomsize;
		damp1 = damp;
		gain = fixedgain;
	}

	for(i=0; i<numcombs; i++)
	{
		combL[i].setfeedback(roomsize1);
		combR[i].setfeedback(roomsize1);
	}

	for(i=0; i<numcombs; i++)
	{
		combL[i].setdamp(damp1);
		combR[i].setdamp(damp1);
	}
}

// The following get/set functions are not inlined, because
// speed is never an issue when calling them, and also
// because as you develop the reverb model, you may
// wish to take dynamic action when they are called.

void revmodel::setroomsize(float value)
{
	roomsize = (value*scaleroom) + offsetroom;
	update();
}

float revmodel::getroomsize()
{
	return (roomsize-offsetroom)/scaleroom;
}

void revmodel::setdamp(float value)
{
	damp = value*scaledamp;
	update();
}

float revmodel::getdamp()
{
	return damp/scaledamp;
}

void revmodel::setwet(float value)
{
	wet = value*scalewet;
	update();
}

float revmodel::getwet()
{
	return wet/scalewet;
}

void revmodel::setdry(float value)
{
	dry = value*scaledry;
}

float revmodel::getdry()
{
	return dry/scaledry;
}

void revmodel::setwidth(float value)
{
	width = value;
	update();
}

float revmodel::getwidth()
{
	return width;
}

void revmodel::setmode(float value)
{
	mode = value;
	update();
}

float revmodel::getmode()
{
	if (mode >= freezemode)
		return 1;
	else
		return 0;
}

int revmodel::setverbID(int iID)
{
	int i;
	int j;
	float fval;
	float pParam[MAX_VERB_ID_NUMBER][6] = 
	{
		//{0.0f,	0.846f,	0.36f,	0.23f,	3.225f,	0.618f},	// 0,big room
		//{0.2f,	0.721f,	0.20f,	0.16f,	2.337f,	0.618f},	// 1,media room
		//{0.1f,	0.720f,	0.06f,	0.0f,	1.786f,	0.618f},	// 2,small room
		//{0.0f,	0.944f,	0.81f,	0.93f,	-2.55f,	0.618f},	// 3,教堂
		//{0.0f,	0.929f,	0.83f,	0.92f,	-5.62f,	0.618f},	// 4,剧场
		//{0.4f,	0.942f,	0.13f,	0.32f,	0.886f,	0.618f},	// 5,隧道
		//{0.4f,	0.912f,	0.66f,	0.36f,	3.225f,	0.618f},	// 6,音乐
		//{0.4f,	0.882f,	0.42f,	0.22f,	-0.479f,0.618f},	// 7,影院
		//{0.2f,	0.925f,	1.00f,	1.00f,	-5.621f,0.618f}		// 8,浴室

		{0.0f,	0.846f,	0.36f,	0.23f,	0.0f,	1.0f  },	// 0,big room
		{0.0f,	0.846f,	0.36f,	0.23f,	2.425f,	0.418f},	// 1,big room
		{0.2f,	0.721f,	0.20f,	0.16f,	2.107f,	0.418f},	// 2,media room
		{0.1f,	0.720f,	0.06f,	0.0f,	1.786f,	0.418f},	// 3,small room
		{0.0f,	0.944f,	0.81f,	0.93f,	1.05f,	0.118f},	// 4,教堂
		{0.0f,	0.929f,	0.83f,	0.92f,	0.82f,	0.218f},	// 5,剧场
		{0.4f,	0.942f,	0.13f,	0.32f,	0.886f,	0.618f},	// 6,隧道
		{0.4f,	0.912f,	0.66f,	0.36f,	1.225f,	0.318f},	// 7,音乐
		{0.4f,	0.882f,	0.42f,	0.22f,	-0.479f,0.618f},	// 8,影院
		{0.2f,	0.925f,	1.00f,	1.00f,	0.901f,	0.018f}		// 9,浴室
	};

	if ((iID<0)||(iID>= MAX_VERB_ID_NUMBER))
	{
		return -2;
	}

	i = iID;
	j = 0;
	//setwet(pParam[i][j++]);
	//setroomsize(pParam[i][j++]);
	//setdry(pParam[i][j++]);
	//setdamp(pParam[i][j++]);
	//setwidth(pParam[i][j++]);
	//setmode(pParam[i][j++]);

	setmode(pParam[i][j++]);
	fval = (pParam[i][j++] - 0.7f)/0.28f;
	setroomsize(fval);
	//setroomsize(pParam[i][j++]);
	setdamp(pParam[i][j++]);
	setwidth(pParam[i][j++]);
	
	fval = (pParam[i][j++]/3);
	setwet(fval);

	fval = (pParam[i][j++]/2);
	setdry(fval);

	return 0;
}

//ends
