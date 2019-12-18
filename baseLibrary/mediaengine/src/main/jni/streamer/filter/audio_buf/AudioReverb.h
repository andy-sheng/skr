#ifndef KSYSTREAMERANDROID_AUDIOREVERB_H
#define KSYSTREAMERANDROID_AUDIOREVERB_H
#include "Reverb_libSoX.h"
#include <stdio.h>
#include <audio/AudioFilterBase.h>

struct ReverbParams
{
    double roomSize;
    double preDelay;
    double reverberance;
    double hfDamping;
    double toneLow;
    double toneHigh;
    double wetGain;
    double dryGain;
    double stereoWidth;
    bool wetOnly;
};

struct Reverb_priv_t
{
    reverb_t reverb;
    float *dry;
    float *wet[2];
};

#define REVERB_BLOCK 16384
#define REVERB_FRAMELEN (16384)

#define AUDIO_REVERB_LEVEL_0 0
#define AUDIO_REVERB_LEVEL_1 1
#define AUDIO_REVERB_LEVEL_2 2
#define AUDIO_REVERB_LEVEL_3 3
#define AUDIO_REVERB_LEVEL_4 4

#define AUDIO_REVERB_PRESET_BATHROOM 10
#define AUDIO_REVERB_PRESET_SMALLROOM 11
#define AUDIO_REVERB_PRESET_MEDIUMROOM 12
#define AUDIO_REVERB_PRESET_LARGEROOM 13
#define AUDIO_REVERB_PRESET_CHURCHHALL 14

#define AUDIO_REVERB_RNB 20
#define AUDIO_REVERB_ROCK 21
#define AUDIO_REVERB_POPULAR 22
#define AUDIO_REVERB_DANCE 23
#define AUDIO_REVERB_NEW_CENT 24

class AudioReverb : public AudioFilterBase {
public:
    AudioReverb();
    ~AudioReverb();

    int Config(int sampleFmt, int sampleRate, int channels);
    void Init();
    void ReverbProcess(short *buf, const int samples);
    void ReverbSet(int scenario);

    int init(int idx, int sampleFmt, int sampleRate, int channels, int bufferSamples);
    int process(int idx, uint8_t* inBuf, int inSize);
    
private:
    float mInputC[REVERB_FRAMELEN];
    float mOutputC[REVERB_FRAMELEN];

    int mSampleFmt;
    int mSampleRate;
    int mChannels;
    int mBufferSamples;
    float mDryMult;
    int mScenario;

    ReverbParams mParams;
    Reverb_priv_t* mReverbImp;
    Reverb_priv_t mReverbImp_;
    bool mReCreate;
};

#endif //KSYSTREAMERANDROID_AUDIOREVERB_H
