//
// Created by 昝晓飞 on 17/3/14.
//

#include "apmwrapper.h"
#include "include/log.h"

#undef LOG_TAG
#define LOG_TAG "APMWrapper"

APMWrapper::APMWrapper() :
        mSamplesPerFrame(0),
        mFrameSize(0),
        mFifoBuffer(NULL),
        mFifoSize(0),
        mAPM(NULL),
        mAudioFrame(NULL),
        mOutData(NULL),
        mBufferSamples(0) {
}

APMWrapper::~APMWrapper() {
    if (mAPM != NULL) {
        delete mAPM;
        mAPM = NULL;
    }
    if (mAudioFrame != NULL) {
        delete mAudioFrame;
        mAudioFrame = NULL;
    }

    if (mFifoBuffer) {
        audio_utils_fifo_deinit(&mFifo);
        free(mFifoBuffer);
        mFifoBuffer = NULL;
    }
    if (mOutData) {
        free(mOutData);
        mOutData = NULL;
    }
}

int APMWrapper::Create() {
    mAPM = AudioProcessing::Create();
    if (mAPM == NULL) {
        //LOGE("[APM][Create] createAPM failed!");
        return -1;
    }
    //init audio frame
    mAudioFrame = new AudioFrame();
    mAudioFrame->sample_rate_hz_ = SAMPLE_RATE_HZ;
    mAudioFrame->num_channels_ = NUM_INPUT_CHANNEL;
    mAudioFrame->samples_per_channel_ = AudioProcessing::kChunkSizeMs * SAMPLE_RATE_HZ / 1000;
    mSamplesPerFrame = mAudioFrame->samples_per_channel_ * mAudioFrame->num_channels_;

    return 0;
}

/**
 * Initializes internal states, while retaining all user settings
 * This should be called before beginning to process a new audio stream
 * however, it is not necessary to call before processing the first stream after creation
 * It is also not necessary to call if the audio parameters (sample rate and number of channels)
 * have changed. Passing updated parameters
 */
int APMWrapper::Initialize() {
    return mAPM->Initialize();
}

/**
 * Processes a 10 ms |frame| of the primary audio stream. On the client-side,
 * this is the near-end (or captured) audio.
 */
int APMWrapper::ProcessStream(int16_t **out, int16_t *data, int len) {
    int start = 0, size = 0, ret = 0;
    audio_utils_fifo_write(&mFifo, (char *) data, len / mFrameSize);

    while (audio_utils_fifo_get_remain(&mFifo) >= mAudioFrame->samples_per_channel_) {

        audio_utils_fifo_read(&mFifo, (char *) mAudioFrame->data_,
                              mAudioFrame->samples_per_channel_);

        ret = mAPM->ProcessStream(mAudioFrame);
        if (ret >= 0) {
            std::copy(mAudioFrame->data_, mAudioFrame->data_ + mSamplesPerFrame,
                      mOutData + start);
        } else {
            LOGD("[APM]%s,ret = %d", __FUNCTION__, ret);
        }

        start += mSamplesPerFrame;
    }
    *out = mOutData;
    size = start * 2;
    return size;
}

/**
 * A filtering component which removes DC offset and low-frequency noise.
 */
int APMWrapper::EnableHighPassFilter(bool enable) {
    return mAPM->high_pass_filter()->Enable(enable);
}

int APMWrapper::EnableNs(bool enable) {
    return mAPM->noise_suppression()->Enable(enable);
}

int APMWrapper::SetNsLevel(int level) {
    if (level < NoiseSuppression::kLow || level > NoiseSuppression::kVeryHigh) {
        return -1;
    }

    return mAPM->noise_suppression()->set_level((NoiseSuppression::Level) level);
}

int APMWrapper::EnableVAD(bool enable) {
    return mAPM->voice_detection()->Enable(enable);
}

int APMWrapper::SetVADLikelihood(int likelihood) {
    if (likelihood < VoiceDetection::kVeryLowLikelihood ||
        likelihood > VoiceDetection::kHighLikelihood) {
        return -1;
    }

    return mAPM->voice_detection()->set_likelihood((VoiceDetection::Likelihood) likelihood);
}

int APMWrapper::Config(int samplerate, int channels) {
    mAudioFrame->sample_rate_hz_ = samplerate;
    mAudioFrame->num_channels_ = channels;
    mAudioFrame->samples_per_channel_ =
            AudioProcessing::kChunkSizeMs * mAudioFrame->sample_rate_hz_ / 1000;
    mSamplesPerFrame = mAudioFrame->samples_per_channel_ * mAudioFrame->num_channels_;

    mFrameSize = channels * 2;
    mFifoSize = samplerate * 300 / 1000;     // 300ms
    mFifoBuffer = (uint8_t *) malloc((size_t) (mFrameSize * mFifoSize));
    audio_utils_fifo_init(&mFifo, (size_t) mFifoSize, (size_t) mFrameSize, mFifoBuffer);

    mOutData = (int16_t *) malloc(mSamplesPerFrame * 16);
    return 0;
}

int APMWrapper::init(int idx, int sampleRate, int channels, int bufferSamples) {
    mBufferSamples = bufferSamples;
    Config(sampleRate, channels);
    filterInit(sampleRate, channels, bufferSamples);
    return 0;
}

int APMWrapper::process(int idx, uint8_t *inBuf, int inSize) {
    int ret = 0;
    int16_t *outBuf = NULL;
    int outSize = ProcessStream(&outBuf, (int16_t *) inBuf, inSize);
    if (outBuf != NULL && outSize > 0) {
        ret = filterProcess(mAudioFrame->sample_rate_hz_, mAudioFrame->num_channels_,
                            mBufferSamples, (uint8_t *) outBuf, outSize);
    } else {
        if (outSize != 0) {
            LOGE("%s, %d data failed!", __FUNCTION__, outSize);
            ret = -1;
        }
    }
    return ret;
}


