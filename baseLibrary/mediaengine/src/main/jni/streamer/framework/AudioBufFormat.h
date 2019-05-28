//
// AudioBufFormat
//

#ifndef KSYSTREAMERANDROIDSDK_AUDIOBUFFERFORMAT_H
#define KSYSTREAMERANDROIDSDK_AUDIOBUFFERFORMAT_H

class AudioBufFormat {
public:
    int sampleFormat;
    int sampleRate;
    int channels;
    AudioBufFormat() {

    }
    AudioBufFormat(int sampleFormat, int sampleRate, int channels) {
        this->sampleFormat = sampleFormat;
        this->sampleRate = sampleRate;
        this->channels = channels;
    }

    AudioBufFormat(AudioBufFormat *format) {
        this->sampleFormat = format->sampleFormat;
        this->sampleRate = format->sampleRate;
        this->channels = format->channels;
    }

    bool equal(AudioBufFormat *other) {
        if(other->sampleFormat == this->sampleFormat &&
                other->sampleRate == this->sampleRate &&
                other->channels == this->channels) {
            return true;
        }
        return false;
    }
};

#endif //KSYSTREAMERANDROIDSDK_AUDIOBUFFERFORMAT_H
