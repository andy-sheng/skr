//
// Created by 昝晓飞 on 17/3/14.
//

#ifndef KSYAPMDEMO_APMWRAPPER_H
#define KSYAPMDEMO_APMWRAPPER_H

#include <audio/audio_utils_fifo.h>
#include "webrtc/modules/audio_processing/include/audio_processing.h"
#include "webrtc/modules/include/module_common_types.h"
#include "audio/AudioFilterBase.h"
#include "audio/audio_resample.h"
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

const int AEC_ROUTING_MODE_HEADSET = EchoControlMobile::kQuietEarpieceOrHeadset;
const int AEC_ROUTING_MODE_EARPIECE = EchoControlMobile::kEarpiece;
const int AEC_ROUTING_MODE_LOUD_EARPIECE = EchoControlMobile::kLoudEarpiece;
const int AEC_ROUTING_MODE_SPEAKER_PHONE = EchoControlMobile::kSpeakerphone;
const int AEC_ROUTING_MODE_LOUD_SPEAKER_PHONE = EchoControlMobile::kLoudSpeakerphone;

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

    int EnableAECM(bool enable);

    int EnableAEC(bool enable);

    int SetRoutingMode(int mode);

    int SetStreamDelay(int delay);

    int AnalyzeReverseStream(int16_t *data, int len);

    int Config(int idx, int sampleFmt, int samplerate, int channels);

    int init(int idx, int sampleFmt, int sampleRate, int channels, int bufferSamples);

    int process(int idx, uint8_t *inBuf, int inSize);

private:
    AudioProcessing *mAPM;
    int mSamplesPerFrame[2];    // 10ms frame samples
    int mBufferSamples[2];      // 每次音频处理送进来的samples数
    audio_utils_fifo mFifo[2];
    int mFrameSize[2];
    int mFifoSize[2];
    uint8_t *mFifoBuffer[2];
    int mInSampleFmt[2];

    KSYSwr *mResample[2];
    KSYSwr *mOutResample;
    int16_t *mInData[2];
    float **mTempData[2];
    StreamConfig mConfig[2];

    bool mHasVoice;
    int mAnalogLevel;
};

#endif //KSYAPMDEMO_APMWRAPPER_H
