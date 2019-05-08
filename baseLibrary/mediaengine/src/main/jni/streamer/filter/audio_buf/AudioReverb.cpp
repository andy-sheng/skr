#include "AudioReverb.h"
#include "assert.h"

AudioReverb::AudioReverb()
{
    mScenario = 3;
    mSampleRate = 44100;
    mChannels = 1;
    mBufferSamples = 0;
    mReverbImp = NULL;
    mReCreate = false;
}

AudioReverb::~AudioReverb()
{
    if (mReverbImp) {
        reverb_delete(&mReverbImp->reverb);
        mReverbImp = NULL;
    }
}

void AudioReverb::Config(int sampleRate, int channels) {
    if (mSampleRate != sampleRate || mChannels != channels) {
        mReCreate = true;
    }
    mSampleRate = sampleRate;
    mChannels = channels;
}

void AudioReverb::Init()
{
    switch(mScenario)
    {
        case AUDIO_REVERB_LEVEL_1:
            mParams.roomSize = 10;
            mParams.preDelay = 20;
            mParams.reverberance = 50;
            mParams.hfDamping = 50;
            mParams.toneLow = 50;
            mParams.toneHigh = 100;
            mParams.wetGain = -6;
            mParams.dryGain = -2;
            mParams.stereoWidth = 100;
            mParams.wetOnly = false;
            break;
        case AUDIO_REVERB_LEVEL_2:
            mParams.roomSize = 60;
            mParams.preDelay = 20;
            mParams.reverberance = 60;
            mParams.hfDamping = 50;
            mParams.toneLow = 50;
            mParams.toneHigh = 100;
            mParams.wetGain = -4;
            mParams.dryGain = -2;
            mParams.stereoWidth = 100;
            mParams.wetOnly = false;
            break;
        case AUDIO_REVERB_LEVEL_3:
            mParams.roomSize = 70;
            mParams.preDelay = 60;
            mParams.reverberance = 70;
            mParams.hfDamping = 50;
            mParams.toneLow = 50;
            mParams.toneHigh = 100;
            mParams.wetGain = -3;
            mParams.dryGain = -2;
            mParams.stereoWidth = 100;
            mParams.wetOnly = false;
            break;
        case AUDIO_REVERB_LEVEL_4:
            mParams.roomSize = 100;
            mParams.preDelay = 50;
            mParams.reverberance = 80;
            mParams.hfDamping = 50;
            mParams.toneLow = 50;
            mParams.toneHigh = 100;
            mParams.wetGain = -2;
            mParams.dryGain = -2;
            mParams.stereoWidth = 100;
            mParams.wetOnly = false;
            break;
        default:
            mParams.roomSize = 30;
            mParams.preDelay = 10;
            mParams.reverberance = 90;
            mParams.hfDamping = 50;
            mParams.toneLow = 50;
            mParams.toneHigh = 100;
            mParams.wetGain = -2;
            mParams.dryGain = -2;
            mParams.stereoWidth = 100;
            mParams.wetOnly = false;
    }
    mDryMult = (float) (mParams.wetOnly ? 0 : dB_to_linear(mParams.dryGain));

    reverb_create(&mReverbImp->reverb,
                   mSampleRate,
                   mParams.wetGain,
                   mParams.roomSize,
                   mParams.reverberance,
                   mParams.hfDamping,
                   mParams.preDelay,
                   mParams.stereoWidth * ((mChannels == 2) ? 1 : 0),
                   mParams.toneLow,
                   mParams.toneHigh,
                   REVERB_BLOCK,
                   mReverbImp->wet);
}

void AudioReverb::ReverbProcess(short *buf, const int samples)
{
    assert(REVERB_FRAMELEN > samples);

    if (mReCreate) {
        if (mReverbImp) {
            reverb_delete(&mReverbImp->reverb);
            mReverbImp = NULL;
        }
        mReCreate = false;
    }
    if (mReverbImp == NULL) {
        mReverbImp = &mReverbImp_;
        Init();
    }

    for (int i = 0; i < samples; i++){
        mInputC[i] = buf[i];
    }
    mReverbImp->dry = (float*)fifo_write(&mReverbImp->reverb.input_fifo, (size_t) samples, mInputC);
    reverb_process(&mReverbImp->reverb, (size_t) samples);
    float *dry = mReverbImp->dry;
    float *wet = mReverbImp->wet[0];
    for (int i = 0; i < samples; i++)
    {
        mOutputC[i] = (mDryMult * dry[i] + wet[i]);
        
        buf[i] = (short) mOutputC[i];
        if (mOutputC[i] > 32767.0)
            buf[i] = 32767;
        if (mOutputC[i] < -32768.0)
            buf[i] = -32768;
    }
}

void  AudioReverb::ReverbSet(int scenario) {
    if (mScenario != scenario) {
        mReCreate = true;
    }
    mScenario = scenario;
}

int AudioReverb::init(int idx, int sampleRate, int channels, int bufferSamples) {
    Config(sampleRate, channels);
    mBufferSamples = bufferSamples;
    filterInit(sampleRate, channels, bufferSamples);
    return 0;
}

int AudioReverb::process(int idx, uint8_t *inBuf, int inSize) {
    ReverbProcess((short*) inBuf, inSize / 2);
    return filterProcess(mSampleRate, mChannels, mBufferSamples, inBuf, inSize);
}
