//
// Created by 昝晓飞 on 17/3/14.
//

#ifndef KSYAPMDEMO_APMWRAPPER_H
#define KSYAPMDEMO_APMWRAPPER_H

#include <audio/audio_utils_fifo.h>
#include "webrtc/modules/audio_processing/include/audio_processing.h"
#include "webrtc/modules/include/module_common_types.h"
#include "audio/AudioFilterBase.h"
#include <algorithm>

using namespace std;
using namespace webrtc;

const int SAMPLE_RATE_HZ = AudioProcessing::kSampleRate48kHz;
const int NUM_INPUT_CHANNEL = 2;
const int REVERSE_SAMPLE_RATE_HZ = AudioProcessing::kSampleRate48kHz;
const int NUM_REVERSE_CHANNEL = 2;

const int NS_LEVEL_LOW = NoiseSuppression::kLow;
const int NS_LEVEL_MODERATE = NoiseSuppression::kModerate;
const int NS_LEVEL_HIGH = NoiseSuppression::kHigh;
const int NS_LEVEL_VERYHIGH = NoiseSuppression::kVeryHigh;

const int VAD_LIKELIHOOD_VERYLOW = VoiceDetection::kVeryLowLikelihood;
const int VAD_LIKELIHOOD_LOW = VoiceDetection::kLowLikelihood;
const int VAD_LIKELIHOOD_MODERATE = VoiceDetection::kModerateLikelihood;
const int VAD_LIKELIHOOD_HIGH = VoiceDetection::kHighLikelihood;

class APMWrapper : public AudioFilterBase {
public:
    APMWrapper();

    ~APMWrapper();

    int Create();

    int Initialize(void);

    int ProcessStream(int16_t **out, int16_t *data, int len);

    int EnableHighPassFilter(bool enable);

    int EnableNs(bool enable);

    int SetNsLevel(int level);

    int EnableVAD(bool enable);

    int SetVADLikelihood(int likelihood);

    int Config(int samplerate, int channels);

    int init(int idx, int sampleRate, int channels, int bufferSamples);

    int process(int idx, uint8_t *inBuf, int inSize);

private:
    AudioProcessing *mAPM;
    AudioFrame *mAudioFrame;  //This class holds up to 60 ms of super-wideband (32 kHz) stereo audio

    int mSamplesPerFrame;
    int mBufferSamples;
    audio_utils_fifo mFifo;
    int mFrameSize;
    int mFifoSize;
    uint8_t *mFifoBuffer;
    int16_t *mOutData;
};

#endif //KSYAPMDEMO_APMWRAPPER_H
