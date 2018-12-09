#ifndef KALA_AUDIOBASE_PHONOGRAPH_H
#define KALA_AUDIOBASE_PHONOGRAPH_H

#include "KalaInterfaces.h"
#include <vector>

class CPhonograph : public IKalaDspProcessor
{
public:
    CPhonograph();
    virtual ~CPhonograph();
	void Reset();

	int Init(int inSampleRate, int inChannel);	// set sample rate and channel;
	void Uninit();	// uninit

	// process input buffer and output size.
	int Process(short* inBuffer, int inSize);

    int Process(float * inBuffer, int inSize);

	int ProcessLRIndependent(float * inLeft, float * inRight, int inOutSize);

private:
    void* handles;
    int m_samplerate;
    int m_channels;
    std::vector<float> data;
};
#endif
