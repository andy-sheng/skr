//
// Created by 昝晓飞 on 17/3/14.
//

#include "apmwrapper.h"
#include "include/log.h"
#include "audio/audio_resample.h"

#undef LOG_TAG
#define LOG_TAG "APMWrapper"

APMWrapper::APMWrapper() :
        mAPM(NULL),
        mHasVoice(true),
        mAnalogLevel(30),
        mSamplesPerFrame{0, 0},
        mFrameSize{0, 0},
        mFifoBuffer{NULL, NULL},
        mFifoSize{0, 0},
        mOutResample(NULL),
        mResample{NULL, NULL},
        mTempData{NULL, NULL},
        mBufferSamples{0, 0},
        mInSampleFmt{0, 0} {
}

APMWrapper::~APMWrapper() {
    if (mAPM != NULL) {
        delete mAPM;
        mAPM = NULL;
    }

    if (mOutResample) {
        ksy_swr_release(mOutResample);
        mOutResample = NULL;
    }

    for (int i = 0; i < 2; i++) {
        free(mInData[i]);
        mInData[i] = NULL;

        if (mResample[i]) {
            ksy_swr_release(mResample[i]);
            mResample[i] = NULL;
        }
    }

    for(int i = 0; i < 2; i++) {
        if (mFifoBuffer[i]) {
            audio_utils_fifo_deinit(&mFifo[i]);
            free(mFifoBuffer[i]);
            mFifoBuffer[i] = NULL;
        }
    }
}

int APMWrapper::Create() {
    mAPM = AudioProcessing::Create();
    if (mAPM == NULL) {
        LOGE("[APM][Create] createAPM failed!");
        return -1;
    }

    for (int i = 0; i < 2; i++) {
        mConfig[i].set_sample_rate_hz(SAMPLE_RATE_HZ);
        mConfig[i].set_num_channels(NUM_INPUT_CHANNEL);
        mSamplesPerFrame[i] = mConfig[i].num_samples();

        mInData[i] = (int16_t*) malloc(mConfig[i].num_samples() * sizeof(int16_t));
    }

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
    LOGE("ProcessStream data=0x%p len=%d", data, len);
    int idx = 0;
    int delay = 80;
    int start = 0, size = 0, ret = 0;
    audio_utils_fifo_write(&mFifo[0], (char *) data, len / mFrameSize[idx]);

    while (audio_utils_fifo_get_remain(&mFifo[idx]) >= mConfig[idx].num_frames()) {
        audio_utils_fifo_read(&mFifo[idx], mInData[idx], mConfig[idx].num_frames());
        size = mConfig[idx].num_samples() * getBytesPerSample(mInSampleFmt[idx]);
        ret = ksy_swr_convert(mResample[idx], (uint8_t***) &mTempData[idx], (uint8_t**) &mInData[idx], size);
        if (ret <= 0) {
            LOGE("apm %d resample to FLTP failed, err=%d", idx, ret);
        }

        SetStreamDelay(delay);
        delay -= 10;
        ret = mAPM->ProcessStream(mTempData[idx], mConfig[idx], mConfig[idx], mTempData[idx]);
        if (ret >= 0) {
            uint8_t **buf = NULL;
            size = mConfig[idx].num_samples() * getBytesPerSample(SAMPLE_FMT_FLTP);
            ret = ksy_swr_convert(mOutResample, &buf, (uint8_t**) mTempData[idx], size);
            if (ret > 0) {
                memcpy(data + start, buf[0], (size_t) ret);
            } else {
                LOGE("apm %d resample from FLTP failed, err=%d", idx, size);
            }
        } else {
            LOGD("[APM] ProcessStream failed, ret = %d", ret);
        }

        start += mSamplesPerFrame[idx];
    }

    *out = data;
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

int APMWrapper::EnableAECM(bool enable) {
    return mAPM->echo_control_mobile()->Enable(enable);
}

int APMWrapper::EnableAEC(bool enable) {
    if (enable) {
        mAPM->echo_cancellation()->enable_drift_compensation(false);
    }
    return mAPM->echo_cancellation()->Enable(true);
}

int APMWrapper::SetRoutingMode(int mode) {
    if (mode < EchoControlMobile::kQuietEarpieceOrHeadset ||
        mode > EchoControlMobile::kLoudSpeakerphone) {
        return -1;
    }
    return mAPM->echo_control_mobile()->set_routing_mode((EchoControlMobile::RoutingMode) mode);
}

int APMWrapper::SetStreamDelay(int delay) {
    return mAPM->set_stream_delay_ms(delay);
}

int APMWrapper::AnalyzeReverseStream(int16_t *data, int len) {
    LOGE("AnalyzeReverseStream data=0x%p len=%d", data, len);
    int idx = 1;
    int ret = 0, size = 0;
    audio_utils_fifo_write(&mFifo[1], (char *) data, len / mFrameSize[idx]);

    while (audio_utils_fifo_get_remain(&mFifo[idx]) >= mConfig[idx].num_frames()) {
        audio_utils_fifo_read(&mFifo[idx], mInData[idx], mConfig[idx].num_frames());
        size = mConfig[idx].num_samples() * getBytesPerSample(mInSampleFmt[idx]);
        ret = ksy_swr_convert(mResample[idx], (uint8_t***) &mTempData[idx], (uint8_t**) &mInData[idx], size);
        if (ret <= 0) {
            LOGE("apm %d resample to FLTP failed, err=%d", idx, ret);
        }

        ret = mAPM->ProcessReverseStream(mTempData[idx], mConfig[idx], mConfig[idx], mTempData[idx]);
        if (ret < 0) {
            LOGD("[APM] ProcessReverseStream failed, ret = %d", ret);
        }
    }
    return ret;
}

int APMWrapper::Config(int idx, int sampleFmt, int samplerate, int channels) {
    LOGE("Config idx=%d sampleFmt=%d samplerate=%d channels=%d", idx, sampleFmt, samplerate, channels);
    mInSampleFmt[idx] = sampleFmt;

    mConfig[idx].set_sample_rate_hz(samplerate);
    mConfig[idx].set_num_channels(channels);
    mSamplesPerFrame[idx] = mConfig[idx].num_samples();

    if (mResample[idx]) {
        ksy_swr_release(mResample[idx]);
    }
    mResample[idx] = ksy_swr_init(samplerate, channels, sampleFmt, samplerate, channels, SAMPLE_FMT_FLTP);

    if (idx == 0) {
        if (mOutResample) {
            ksy_swr_release(mOutResample);
        }
        mOutResample = ksy_swr_init(samplerate, channels, SAMPLE_FMT_FLTP, samplerate, channels,
                                    sampleFmt);
    }

    mFrameSize[idx] = channels * getBytesPerSample(sampleFmt);
    mFifoSize[idx] = samplerate * 300 / 1000;     // 300ms
    mFifoBuffer[idx] = (uint8_t *) malloc((size_t) (mFrameSize[idx] * mFifoSize[idx]));
    audio_utils_fifo_init(&mFifo[idx], (size_t) mFifoSize[idx], (size_t) mFrameSize[idx], mFifoBuffer[idx]);

    return 0;
}

int APMWrapper::init(int idx, int sampleFmt, int sampleRate, int channels, int bufferSamples) {
    mBufferSamples[idx] = bufferSamples;
    Config(idx, sampleFmt, sampleRate, channels);
    filterInit(sampleFmt, sampleRate, channels, bufferSamples);
    return 0;
}

int APMWrapper::process(int idx, uint8_t *inBuf, int inSize) {
    int ret = 0;
    if (idx == 0) {
        int16_t *outBuf = NULL;
        int outSize = ProcessStream(&outBuf, (int16_t *) inBuf, inSize);
        if (outBuf != NULL && outSize > 0) {
            ret = filterProcess(mInSampleFmt[idx], mConfig[idx].sample_rate_hz(),
                                mConfig[idx].num_channels(), mBufferSamples[idx],
                                (uint8_t *) outBuf, outSize);
        } else {
            if (outSize != 0) {
                LOGE("%s, %d data failed!", __FUNCTION__, outSize);
                ret = -1;
            }
        }
    } else {
        ret = AnalyzeReverseStream((int16_t*) inBuf, inSize);
    }
    return ret;
}


