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
    int codecId;
    AudioBufFormat() {

    }
    AudioBufFormat(int sampleFormat, int sampleRate, int channels, int codecId) {
        this->sampleFormat = sampleFormat;
        this->sampleRate = sampleRate;
        this->channels = channels;
        this->codecId = codecId;
    }

    AudioBufFormat(AudioBufFormat *format) {
        this->sampleFormat = format->sampleFormat;
        this->sampleRate = format->sampleRate;
        this->channels = format->channels;
        this->codecId = format->codecId;
    }

    bool equal(AudioBufFormat *other) {
        if(other->sampleFormat == this->sampleFormat &&
                other->sampleRate == this->sampleRate &&
                other->channels == this->channels &&
                other->codecId == this->codecId) {
            return true;
        }
        return false;
    }
};

#endif //KSYSTREAMERANDROIDSDK_AUDIOBUFFERFORMAT_H
