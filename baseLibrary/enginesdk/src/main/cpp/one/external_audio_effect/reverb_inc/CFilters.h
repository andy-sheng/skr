#ifndef KALA_AUDIOBASE_FILTERS_H
#define KALA_AUDIOBASE_FILTERS_H

#include "KalaInterfaces.h"
#include <vector>

#define LOW_PASS_FILTER		0
#define HIGH_PASS_FILTER	1
#define BAND_PASS_FILTER	2


class  CFilters
{
public:
    CFilters();
    virtual ~CFilters();

	int Init(int inSampleRate, int inChannel);	// set sample rate, channel and filter type;
	void Reset();
	void Uninit();	// uninit
	int setFilterType(int fType);

	// process input buffer and output size.
	int Process(char* inBuffer, int inSize);
	int Process(float* inBuffer, int inSize);

	int ProcessLRIndependent(float * inLeft, float * inRight, int inOutSize);

private:
    void* handles;
    int m_samplerate;
    int m_channels;
    std::vector<float> data;
};
#endif
