package com.component.mediaengine.framework;

/**
 * Audio buf format.
 */

public class AudioBufFormat {
    // native module ptr
    public long nativeModule;

    public int sampleFormat;
    public int sampleRate;
    public int channels;

    /**
     * Create AudioBufFormat with given params.
     *
     * @param sampleFormat sample format, see {@link AVConst}
     * @param sampleRate   sample rate in Hz
     * @param channels     channel number, 1 for mono, 2 for stereo
     */
    public AudioBufFormat(int sampleFormat, int sampleRate, int channels) {
        this.sampleFormat = sampleFormat;
        this.sampleRate = sampleRate;
        this.channels = channels;
        this.nativeModule = 0;
    }

    public AudioBufFormat(AudioBufFormat format) {
        this.sampleFormat = format.sampleFormat;
        this.sampleRate = format.sampleRate;
        this.channels = format.channels;
        this.nativeModule = format.nativeModule;
    }

    private AudioBufFormat() {
    }

    public boolean equals(AudioBufFormat format) {
        return (format != null) &&
                (sampleFormat == format.sampleFormat) &&
                (sampleRate == format.sampleRate) &&
                (channels == format.channels);
    }
}
