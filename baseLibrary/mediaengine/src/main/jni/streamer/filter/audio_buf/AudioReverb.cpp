#include "AudioReverb.h"
#include "assert.h"
#include "log.h"

AudioReverb::AudioReverb()
{
    mScenario = 3;
    mSampleFmt = SAMPLE_FMT_S16;
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

int AudioReverb::Config(int sampleFmt, int sampleRate, int channels) {
    if (sampleFmt != SAMPLE_FMT_S16) {
        LOGE("AudioReverb only support SAMPLE_FMT_S16!");
        return -1;
    }
    if (mSampleRate != sampleRate || mChannels != channels) {
        mReCreate = true;
    }
    mSampleFmt = sampleFmt;
    mSampleRate = sampleRate;
    mChannels = channels;
    return 0;
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
        case AUDIO_REVERB_PRESET_BATHROOM:
            mParams.roomSize = 16;
            mParams.preDelay = 8;
            mParams.reverberance = 80;
            mParams.hfDamping = 0;
            mParams.toneLow = 0;
            mParams.toneHigh = 100;
            mParams.wetGain = -6;
            mParams.dryGain = 0;
            mParams.stereoWidth = 100;
            mParams.wetOnly = false;
            break;
        case AUDIO_REVERB_PRESET_SMALLROOM:
            mParams.roomSize = 30;
            mParams.preDelay = 10;
            mParams.reverberance = 50;
            mParams.hfDamping = 50;
            mParams.toneLow = 50;
            mParams.toneHigh = 100;
            mParams.wetGain = -1;
            mParams.dryGain = -1;
            mParams.stereoWidth = 100;
            mParams.wetOnly = false;
            break;
        case AUDIO_REVERB_PRESET_MEDIUMROOM:
            mParams.roomSize = 75;
            mParams.preDelay = 10;
            mParams.reverberance = 40;
            mParams.hfDamping = 50;
            mParams.toneLow = 100;
            mParams.toneHigh = 70;
            mParams.wetGain = -1;
            mParams.dryGain = -1;
            mParams.stereoWidth = 70;
            mParams.wetOnly = false;
            break;
        case AUDIO_REVERB_PRESET_LARGEROOM:
            mParams.roomSize = 85;
            mParams.preDelay = 10;
            mParams.reverberance = 40;
            mParams.hfDamping = 50;
            mParams.toneLow = 100;
            mParams.toneHigh = 80;
            mParams.wetGain = 0;
            mParams.dryGain = -6;
            mParams.stereoWidth = 90;
            mParams.wetOnly = false;
            break;
        case AUDIO_REVERB_PRESET_CHURCHHALL:
            mParams.roomSize = 90;
            mParams.preDelay = 32;
            mParams.reverberance = 60;
            mParams.hfDamping = 50;
            mParams.toneLow = 100;
            mParams.toneHigh = 50;
            mParams.wetGain = 0;
            mParams.dryGain = -12;
            mParams.stereoWidth = 100;
            mParams.wetOnly = false;
            break;
        case AUDIO_REVERB_RNB:
            mParams.roomSize = 64;
            mParams.preDelay = 77;
            mParams.reverberance = 62;
            mParams.hfDamping = 15;
            mParams.toneLow = 100;
            mParams.toneHigh = 100;
            mParams.wetGain = -5.5;
            mParams.dryGain = 0;    // -3.6
            mParams.stereoWidth = 83;
            mParams.wetOnly = false;
            break;
        case AUDIO_REVERB_ROCK:
            mParams.roomSize = 85;
            mParams.preDelay = 76;
            mParams.reverberance = 89;
            mParams.hfDamping = 26;
            mParams.toneLow = 100;
            mParams.toneHigh = 100;
            mParams.wetGain = 0;
            mParams.dryGain = -3;   // -5
            mParams.stereoWidth = 98;
            mParams.wetOnly = false;
            break;
        case AUDIO_REVERB_POPULAR:
            mParams.roomSize = 95;
            mParams.preDelay = 84;
            mParams.reverberance = 81;
            mParams.hfDamping = 50;
            mParams.toneLow = 100;
            mParams.toneHigh = 100;
            mParams.wetGain = -4;
            mParams.dryGain = -1;   // -5
            mParams.stereoWidth = 100;
            mParams.wetOnly = false;
            break;
        case AUDIO_REVERB_DANCE:
            mParams.roomSize = 84;
            mParams.preDelay = 66;
            mParams.reverberance = 35;
            mParams.hfDamping = 20;
            mParams.toneLow = 100;
            mParams.toneHigh = 100;
            mParams.wetGain = -5.6;
            mParams.dryGain = 0;    // -4
            mParams.stereoWidth = 100;
            mParams.wetOnly = false;
            break;
        case AUDIO_REVERB_NEW_CENT:
            mParams.roomSize = 92;
            mParams.preDelay = 95;
            mParams.reverberance = 78;
            mParams.hfDamping = 42;
            mParams.toneLow = 100;
            mParams.toneHigh = 100;
            mParams.wetGain = -2.7;
            mParams.dryGain = -2;    // -4.5
            mParams.stereoWidth = 89;
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
    if (mScenario == AUDIO_REVERB_LEVEL_0) {
        return;
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
        mScenario = scenario;
        mReCreate = true;
    }
}

int AudioReverb::init(int idx, int sampleFmt, int sampleRate, int channels, int bufferSamples) {
    int ret = Config(sampleFmt, sampleRate, channels);
    if (ret < 0) {
        return ret;
    }
    mBufferSamples = bufferSamples;
    filterInit(sampleFmt, sampleRate, channels, bufferSamples);
    return 0;
}

int AudioReverb::process(int idx, uint8_t *inBuf, int inSize) {
    ReverbProcess((short*) inBuf, inSize / 2);
    return filterProcess(mSampleFmt, mSampleRate, mChannels, mBufferSamples, inBuf, inSize);
}
