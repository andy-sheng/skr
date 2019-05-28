package com.zq.mediaengine.capture.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

/**
 * Class to hold params for audio recording.
 */

public class AudioRecordParams {
    public static int AUDIO_FORMAT_DEFAULT = AudioFormat.ENCODING_PCM_16BIT;
    public static int AUDIO_SOURCE_DEFAULT = MediaRecorder.AudioSource.MIC;
    public static int AUDIO_RATE_DEFAULT = 44100;
    public static int AUDIO_CHANNEL_MONO = AudioFormat.CHANNEL_IN_MONO;
    public static int AUDIO_CHANNEL_STEREO = AudioFormat.CHANNEL_IN_STEREO;

    public int format = AUDIO_FORMAT_DEFAULT;
    public int source = AUDIO_SOURCE_DEFAULT;
    public int channel = AUDIO_CHANNEL_MONO;
    public int rate = AUDIO_RATE_DEFAULT;
    public int bufferSize;

    public AudioRecordParams() {
        getAudioBufferSize();
    }

    public AudioRecordParams(int format, int source, int channel, int rate) {
        this.format = format;
        this.source = source;
        this.channel = channel;
        this.rate = rate;
        getAudioBufferSize();
    }

    public AudioRecordParams(int rate) {
        this.rate = rate;
        getAudioBufferSize();
    }

    public int getFormat() {
        return format;
    }

    public void setFormat(int mAudioFormat) {
        this.format = mAudioFormat;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int mAudioSource) {
        this.source = mAudioSource;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int mAudioChannel) {
        this.channel = mAudioChannel;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int mAudioRate) {
        this.rate = mAudioRate;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    private void getAudioBufferSize() {
        bufferSize = AudioRecord.getMinBufferSize(rate,
                channel, format);
    }

}
