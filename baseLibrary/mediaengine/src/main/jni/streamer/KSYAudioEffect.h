//
// Created by sujia on 2017/3/22.
//

#ifndef KSYSTREAMERANDROIDSDK_AUDIOCHANGER_H
#define KSYSTREAMERANDROIDSDK_AUDIOCHANGER_H

#include <stdint.h>
#include <audio/AudioFilterBase.h>
#include <sox.h>
#include <stdbool.h>
#include <stdlib.h>
#include <stdio.h>
#include <audio/audio_utils_fifo.h>
#include <vector>
#include "log.h"
#include "audio/AudioFilterBase.h"

#define BUF_SIZE 1024*100

typedef struct AudioEffectInbuffer {
    int16_t in_buf[BUF_SIZE];
    uint16_t in_length;
    uint16_t in_pos;
    uint16_t in_left;
} AudioEffectInbuffer;

typedef struct AudioEffectOutBuffer {
    sox_signalinfo_t signal;
    sox_encodinginfo_t encoding;

    int16_t out_buf[BUF_SIZE];
    uint16_t out_pos;
    uint16_t out_length;

    int frameSize;
    bool fifoInited;
    audio_utils_fifo fifo;
    uint8_t* fifoBuffer;
    int fifoSamples;
} AudioEffectOutBuffer;

typedef struct AudioEffectUserDefineParams {
    char* name;
    int argc;
    char** argv;
}AudioEffectParams;

class KSYAudioEffect : public AudioFilterBase {
public:
    KSYAudioEffect();
    ~KSYAudioEffect();

    void Init();

    void setAudioFormat(int sample_fmt, int sample_rate, int channels);

    void setEffectType(int type);

    void setPitchLevel(int type);

    void processAudio(uint8_t *buf, int len);

    void quit();

    int init(int idx, int sampleFmt, int sampleRate, int channels, int bufferSamples);
    int process(int idx, uint8_t* inBuf, int inSize);
    void addEffects(char const *name, int argc, char* argv[]);
    void removeEffects();
    bool stop;
private:
    sox_effects_chain_t *mChain;

    AudioEffectInbuffer* mInBuffer;
    AudioEffectOutBuffer* mOutBuffer;
    std::vector<AudioEffectParams*> mUserDefineParams;

    int mEffectType;
    int mPitchLevel;
    int mSampleFmt;
    int mSampleRate;
    int mChannels;
    int mBufferSamples;

    bool mReCreate;

    void addEffects();
    void addUserEffects(sox_signalinfo_t *interm_signal);
    void auto_effect(char const *name, float factor,
                sox_signalinfo_t *interm_signal);
    void add_echo_effect(sox_signalinfo_t *interm_signal);
    void add_reverb_effect(sox_signalinfo_t *interm_signal);
    void add_chorus_effect(sox_signalinfo_t *interm_signal);
    void add_earwax_effect(sox_signalinfo_t *interm_signal);
    void add_flanger_effect(sox_signalinfo_t *interm_signal);
};

#endif //KSYSTREAMERANDROIDSDK_AUDIOCHANGER_H
