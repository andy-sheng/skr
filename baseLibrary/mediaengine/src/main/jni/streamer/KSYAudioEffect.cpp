//
// Created by sujia on 2017/3/22.
//

#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include "KSYAudioEffect.h"

#define AUDIO_PITCH_LEVEL_1 -3
#define AUDIO_PITCH_LEVEL_2 -2
#define AUDIO_PITCH_LEVEL_3 -1
#define AUDIO_PITCH_LEVEL_4 0
#define AUDIO_PITCH_LEVEL_5 1
#define AUDIO_PITCH_LEVEL_6 2
#define AUDIO_PITCH_LEVEL_7 3

#define AUDIO_EFFECT_TYPE_PITCH 9
#define AUDIO_EFFECT_TYPE_FEMALE 10
#define AUDIO_EFFECT_TYPE_MALE 11
#define AUDIO_EFFECT_TYPE_HEROIC 12
#define AUDIO_EFFECT_TYPE_ROBOT 13
#define AUDIO_EFFECT_TYPE_ALIEN 14
#define AUDIO_EFFECT_TYPE_FLANGER 15
#define AUDIO_EFFECT_TYPE_CHORUS 16
#define AUDIO_EFFECT_TYPE_EARWAX 17

#define DEBUG 0
#if DEBUG
FILE* out_file;
#endif

/* The function that will be called to input samples into the effects mChain.
 *
 */
static int input_drain(
        sox_effect_t * effp, sox_sample_t * obuf, size_t * osamp)
{
    AudioEffectInbuffer* thiz = (AudioEffectInbuffer*) effp->priv;
    /* ensure that *osamp is a multiple of the number of channels. */
    *osamp -= *osamp % effp->out_signal.channels;

    /* Read up to *osamp samples into obuf; store the actual number read
     * back to *osamp */
    bool done = false;
    if (thiz->in_left < *osamp) {
        done = true;
        *osamp = thiz->in_left;
    }

    for (size_t i=0; i < *osamp; i++, thiz->in_pos++)
    {
        *obuf++ = SOX_SIGNED_16BIT_TO_SAMPLE(thiz->in_buf[thiz->in_pos],);
    }
    thiz->in_left -= *osamp;

    if (done) {
        return SOX_EOF;
    }
    return *osamp? SOX_SUCCESS : SOX_EOF;
}

/* The function that will be called to output samples from the effects chain.
 *
 */
static int output_flow(sox_effect_t *effp LSX_UNUSED, sox_sample_t const * ibuf,
                       sox_sample_t * obuf LSX_UNUSED, size_t * isamp, size_t * osamp)
{
    AudioEffectOutBuffer* thiz = (AudioEffectOutBuffer*) effp->priv;
    /* Write out *isamp samples */
    SOX_SAMPLE_LOCALS;
    int clips;

    for (size_t i = 0; i < *isamp; i++, thiz->out_pos++)
    {
        thiz->out_buf[thiz->out_pos] = SOX_SAMPLE_TO_SIGNED_16BIT(ibuf[i], clips);
    }
    thiz->out_length += *isamp * thiz->signal.precision / 8;

    /* Outputting is the last `effect' in the effect chain so always passes
     * 0 samples on to the next effect (as there isn't one!) */
    *osamp = 0;

    return SOX_SUCCESS; /* All samples output successfully */
}

/* A `stub' effect handler to handle inputting samples to the effects
 * chain; the only function needed for this example is `drain' */
static sox_effect_handler_t const * input_handler(void)
{
    static sox_effect_handler_t handler = {
            "input", NULL, SOX_EFF_MCHAN, NULL, NULL, NULL,
            input_drain, NULL, NULL, sizeof(AudioEffectInbuffer)
    };
    return &handler;
}

/* A `stub' effect handler to handle outputting samples from the effects
 * chain; the only function needed for this example is `flow' */
static sox_effect_handler_t const * output_handler(void)
{
    static sox_effect_handler_t handler = {
            "output", NULL, SOX_EFF_MCHAN, NULL, NULL,
            output_flow, NULL, NULL, NULL, sizeof(AudioEffectOutBuffer)
    };
    return &handler;
}

static int update_status(sox_bool all_done, void * client_data) {
    KSYAudioEffect* thiz = (KSYAudioEffect*) client_data;
    return thiz->stop ? SOX_EOF : SOX_SUCCESS;
}

static int count = 0;

static void init_sox() {
    count++;
    if (count <= 1) {
        sox_init();
    }
}

static void quit_sox() {
    count--;
    if (count <= 0 ) {
        sox_quit();
    }
}

KSYAudioEffect::KSYAudioEffect() {
}

KSYAudioEffect::~KSYAudioEffect() {
}

void KSYAudioEffect::Init() {
    mEffectType = AUDIO_EFFECT_TYPE_PITCH;
    mPitchLevel = AUDIO_PITCH_LEVEL_4;
    mSampleRate = 44100;
    mChannels = 1;
    mBitsPerSample = 16;
//    sox_globals.bufsiz = 2048;

    mChain = NULL;
    mInBuffer = NULL;
    mOutBuffer = NULL;
    mReCreate = false;
    init_sox();
}

void KSYAudioEffect::setAudioFormat(int bits_per_sample, int sample_rate, int channels) {
    if (bits_per_sample != mBitsPerSample ||
            sample_rate != mSampleRate ||
               channels != mChannels) {
        mReCreate = true;
    }
    mSampleRate = sample_rate;
    mChannels = channels;
    mBitsPerSample = bits_per_sample;
}

void KSYAudioEffect::setEffectType(int type) {
    if (type != mEffectType) {
        mReCreate = true;
    }
    mEffectType = type;
}

void KSYAudioEffect::setPitchLevel(int level) {
    setEffectType(AUDIO_EFFECT_TYPE_PITCH);
    if (level != mPitchLevel) {
        mReCreate = true;
    }
    mPitchLevel = level;
}

void KSYAudioEffect::auto_effect(char const *name, int factor,
                               sox_signalinfo_t *interm_signal) {
    char f[12] = {0};
    snprintf(f, 12, "%d", factor);
    char* argv[] = {f, NULL};
    sox_effect_t *effect = sox_create_effect(sox_find_effect(name));
    sox_effect_options(effect, 1, argv);
    sox_add_effect(mChain, effect, interm_signal,
                   &mOutBuffer->signal);
    free(effect);
}

//echo gain-in gain-out delay decay
void KSYAudioEffect::add_echo_effect(sox_signalinfo_t *interm_signal) {
    //echo gain 75% delay 22ms
    char gain_in[] = "1";
    char gain_out[] = "0.75";
    char delay[] = "60";
    char decay[] = "0.75";
    char* argv[] = {gain_in, gain_out, delay, decay, NULL};
    int   argc   = (int)(sizeof(argv) / sizeof(argv[0])) - 1;

    sox_effect_t *effect = sox_create_effect(sox_find_effect("echo"));
    sox_effect_options(effect, argc, argv);
    sox_add_effect(mChain, effect, interm_signal,
                   &mOutBuffer->signal);
    free(effect);
}

//reverb reverberance HF-damping room-scale stereo-depth pre-delay wet-gain
void KSYAudioEffect::add_reverb_effect(sox_signalinfo_t *interm_signal) {
    char reverberance[] = "50";
    char hf_damping[] = "50";
    char room_scale[] = "40";
    char stereo_depth[] = "100";
    char pre_delay[] = "50";
    char weg_grain[] = "0";
    char* argv[] = {reverberance, hf_damping,room_scale, stereo_depth,
                    pre_delay,weg_grain, NULL};
    int   argc   = (int)(sizeof(argv) / sizeof(argv[0])) - 1;

    sox_effect_t *effect = sox_create_effect(sox_find_effect("reverb"));
    sox_effect_options( effect, argc, argv);
    sox_add_effect(mChain, effect, interm_signal,
                   &mOutBuffer->signal);
    free(effect);
}

// chorus gain-in gain-out delay decay speed depth -t|-s
void KSYAudioEffect::add_chorus_effect(sox_signalinfo_t *interm_signal) {
    //echo gain 75% delay 22ms
    char gain_in[] = "1";
    char gain_out[] = "1.6";
    char delay[] = "56";
    char decay[] = "0.75";
    char speed[] = "2";
    char depth[] = "2";
    char t[] = "-t";
    char* argv[] = {gain_in, gain_out, delay, decay,
                    speed, depth, t,  NULL};
    int   argc   = (int)(sizeof(argv) / sizeof(argv[0])) - 1;

    sox_effect_t *effect = sox_create_effect(sox_find_effect("chorus"));
    sox_effect_options( effect, argc, argv);
    sox_add_effect(mChain, effect, interm_signal,
                   &mOutBuffer->signal);
    free(effect);
}

/*
 * Makes audio easier to listen to on headphones.
 * Adds `cues' to 44.1kHz stereo (i.e. audio CD format) audio so that
 * when listened to on headphones the stereo image is moved from inside
 * your head (standard for headphones) to outside and in front of the
 * listener (standard for speakers).
 */
void KSYAudioEffect::add_earwax_effect(sox_signalinfo_t *interm_signal) {
    sox_effect_t *effect = sox_create_effect(sox_find_effect("earwax"));
    sox_add_effect(mChain, effect, interm_signal,
                   &mOutBuffer->signal);
    free(effect);
}

void KSYAudioEffect::add_flanger_effect(sox_signalinfo_t *interm_signal) {
    sox_effect_t *effect = sox_create_effect(sox_find_effect("earwax"));
    sox_add_effect(mChain, effect, interm_signal,
                   &mOutBuffer->signal);
    free(effect);
}

void KSYAudioEffect::addEffects() {
    if (mChain != NULL) {
        // destroy fifo
        if( mOutBuffer && mOutBuffer->fifoInited) {
            audio_utils_fifo_deinit(&mOutBuffer->fifo);
            mOutBuffer->fifoInited = false;
        }
        if (mOutBuffer && mOutBuffer->fifoBuffer) {
            free(mOutBuffer->fifoBuffer);
            mOutBuffer->fifoBuffer = NULL;
        }
        sox_delete_effects_chain(mChain);
        mChain = NULL;
        mInBuffer = NULL;
        mOutBuffer = NULL;
    }

    //mInBuffer and mOutBuffer is freed by sox_delete_effects_chain
    mInBuffer = (AudioEffectInbuffer*)calloc(1, sizeof(AudioEffectInbuffer));
    mOutBuffer = (AudioEffectOutBuffer*)calloc(1, sizeof(AudioEffectOutBuffer));
    mInBuffer->in_length = 0;
    mInBuffer->in_left = 0;
    mInBuffer->in_pos = 0;
    mOutBuffer->out_length = 0;
    mOutBuffer->out_pos =0;
    mOutBuffer->fifoInited = false;
    stop = false;

    mOutBuffer->frameSize = 2 * mChannels;
    mOutBuffer->fifoSamples = BUF_SIZE * 3;
    mOutBuffer->fifoBuffer = (uint8_t*) malloc((size_t) (mOutBuffer->fifoSamples *
            mOutBuffer->frameSize));
    assert(mFifoBuffer);
    audio_utils_fifo_init(&mOutBuffer->fifo, (size_t) mOutBuffer->fifoSamples,
                          (size_t) mOutBuffer->frameSize, mOutBuffer->fifoBuffer);
    mOutBuffer->fifoInited = true;

    mOutBuffer->signal.channels = mChannels;
    mOutBuffer->signal.rate = mSampleRate;
    mOutBuffer->signal.precision = mBitsPerSample;
    mOutBuffer->signal.mult = NULL;
    mOutBuffer->signal.length = 0;

    mOutBuffer->encoding.encoding = SOX_ENCODING_SIGN2;
    mOutBuffer->encoding.bits_per_sample = mBitsPerSample;
    mOutBuffer->encoding.compression = 0;
    mOutBuffer->encoding.reverse_bits = sox_option_default;
    mOutBuffer->encoding.reverse_bytes = sox_option_default;
    mOutBuffer->encoding.reverse_nibbles = sox_option_default;
    mOutBuffer->encoding.opposite_endian = sox_false;

    //create effect mChain
    mChain = sox_create_effects_chain(&mOutBuffer->encoding,
                                     &mOutBuffer->encoding);

    sox_signalinfo_t interm_signal = mOutBuffer->signal;
    sox_effect_t *effect = NULL;

    //The first effect in the effect chain must be something that
    // can source samples;
    effect = sox_create_effect(input_handler());
    effect->priv = mInBuffer;
    sox_add_effect(mChain, effect, &interm_signal,
                   &mOutBuffer->signal);
    free(effect);

    switch (mEffectType) {
        case AUDIO_EFFECT_TYPE_FEMALE:
            // pitch shift 600
            auto_effect("pitch", 600, &interm_signal);

            //high pass 120 hz
            auto_effect("highpass", 120, &interm_signal);
            break;
        case AUDIO_EFFECT_TYPE_MALE:
            //low pass 2000hz
            auto_effect("lowpass", 2000, &interm_signal);

            //pitch -400
            auto_effect("pitch", -400, &interm_signal);

            //amplify 180%
            auto_effect("vol", 1.8, &interm_signal);
            break;
        case AUDIO_EFFECT_TYPE_ROBOT:
            //pitch 100
            auto_effect("pitch", 300, &interm_signal);

            //echo gain 75% delay 22ms
            add_echo_effect(&interm_signal);

            //amplify 64%
            break;
        case AUDIO_EFFECT_TYPE_HEROIC:
            // pitch -100
            auto_effect("pitch", -100, &interm_signal);

            //reverb decay 100ms diffusion 20 wet 0db dry 0db
            add_reverb_effect(&interm_signal);
            break;
//        case AUDIO_EFFECT_TYPE_ALIEN:
//            // pitch -100
//            auto_effect("pitch", 900, &interm_signal);
//
//            //chorus gain 100% delay 56ms modulation frequency 2hz
//            //modulation depth 25%
//            add_chorus_effect(&interm_signal);
//
//            // amplify 116%
//            break;
//        case AUDIO_EFFECT_TYPE_CHORUS:
//            add_chorus_effect(&interm_signal);
//            break;
//        case AUDIO_EFFECT_TYPE_FLANGER:
//            add_flanger_effect(&interm_signal);
//            break;
//        case AUDIO_EFFECT_TYPE_EARWAX:
//            add_earwax_effect(&interm_signal);
//            break;
        case AUDIO_EFFECT_TYPE_PITCH:
            switch (mPitchLevel) {
                case AUDIO_PITCH_LEVEL_1:
                    auto_effect("pitch", -300, &interm_signal);
                    break;
                case AUDIO_PITCH_LEVEL_2:
                    auto_effect("pitch", -200, &interm_signal);
                    break;
                case AUDIO_PITCH_LEVEL_3:
                    auto_effect("pitch", -100, &interm_signal);
                    break;
                case AUDIO_PITCH_LEVEL_4:
                    break;
                case AUDIO_PITCH_LEVEL_5:
                    auto_effect("pitch", 100, &interm_signal);
                    break;
                case AUDIO_PITCH_LEVEL_6:
                    auto_effect("pitch", 200, &interm_signal);
                    break;
                case AUDIO_PITCH_LEVEL_7:
                    auto_effect("pitch", 300, &interm_signal);
                    break;
                default:
                    break;
            }
            break;
        default:
            break;
    }

    if (interm_signal.rate != mOutBuffer->signal.rate) {
        effect = sox_create_effect(sox_find_effect("rate"));
        sox_effect_options( effect, 0, NULL);
        effect->flows = 1;
        sox_add_effect(mChain, effect, &interm_signal,
                       &mOutBuffer->signal);
        free(effect);
    }

    if (interm_signal.channels != mOutBuffer->signal.channels) {
        effect = sox_create_effect(sox_find_effect("channels"));
        sox_effect_options( effect, 0, NULL);
        sox_add_effect(mChain, effect, &interm_signal,
                       &mOutBuffer->signal);
        free(effect);
    }

    //The last effect in the effect chain must be something that
    // only consumes samples
    effect = sox_create_effect(output_handler());
    effect->priv = mOutBuffer;
    sox_add_effect(mChain, effect, &interm_signal,
                   &mOutBuffer->signal);
    free(effect);

#if DEBUG
    out_file = fopen("/sdcard/out.pcm", "w");
#endif
}

void KSYAudioEffect::processAudio(uint8_t *buf, size_t len) {
    if (mReCreate) {
        if (mChain != NULL) {
            sox_delete_effects_chain(mChain);
            mChain = NULL;
        }
        mReCreate = false;
    }
    if (mChain == NULL) {
        addEffects();
    }
    if (mChain != NULL) {
        stop = false;
        mInBuffer->in_length = len / (mOutBuffer->signal.precision / 8);
        mInBuffer->in_left = mInBuffer->in_length;
        mOutBuffer->out_length = 0;
        mInBuffer->in_pos = 0;
        mOutBuffer->out_pos = 0;
        memcpy(mInBuffer->in_buf, buf, len);

        sox_flow_effects(mChain, update_status, this);
    }
    if(mOutBuffer->out_length > 0) {
#if DEBUG
        fwrite(mOutBuffer->out_buf, 1, mOutBuffer->out_length, out_file);
#endif

        int inSamples = mOutBuffer->out_length / mOutBuffer->frameSize;
        int writeFrames = audio_utils_fifo_write(&mOutBuffer->fifo,
                                                 mOutBuffer->out_buf, (size_t) inSamples);
        int write = writeFrames * mOutBuffer->frameSize;
#if DEBUG
        LOGD("sox process write into fifo %d bytes", write);
#endif
    }

    int count = len / mOutBuffer->frameSize;
    int readFrames = audio_utils_fifo_read(&mOutBuffer->fifo, buf, (size_t) count);
    int read = readFrames * mOutBuffer->frameSize;
#if DEBUG
    LOGD("sox process read from fifo %d bytes", read);
#endif
}

void KSYAudioEffect::quit() {
    /* All done; tidy up: */
    stop = true;

    // destroy fifo
    if( mOutBuffer != NULL && mOutBuffer->fifoInited) {
        LOGD("mOutBuffer->fifoInited %d", mOutBuffer->fifoInited);
        audio_utils_fifo_deinit(&mOutBuffer->fifo);
        mOutBuffer->fifoInited = false;
    }
    if (mOutBuffer && mOutBuffer->fifoBuffer) {
        free(mOutBuffer->fifoBuffer);
        mOutBuffer->fifoBuffer = NULL;
    }

    if (mChain != NULL) {
        sox_delete_effects_chain(mChain);
        mChain = NULL;
    }
    quit_sox();

#if DEBUG
    if (out_file) {
        fclose(out_file);
    }
#endif
}


int KSYAudioEffect::init(int idx, int sampleRate, int channels, int bufferSamples) {
    mSampleRate = sampleRate;
    mChannels = channels;
    mBufferSamples = bufferSamples;

    setAudioFormat(mBitsPerSample, sampleRate, channels);
    filterInit(sampleRate, channels, bufferSamples);
    return 0;
}

int KSYAudioEffect::process(int idx, uint8_t *inBuf, int inSize) {
    processAudio(inBuf, inSize);

    return filterProcess(mSampleRate, mChannels, mBufferSamples, inBuf, inSize);
}