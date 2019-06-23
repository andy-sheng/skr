package com.common.recorder;

import android.media.MediaRecorder;

import com.common.log.MyLog;

import java.io.IOException;

public class MyMediaRecorder {
    public final static String TAG = "MyMediaRecorder";

    int audioSource = MediaRecorder.AudioSource.MIC;
    int outputFormat = MediaRecorder.OutputFormat.MPEG_4;
    int audioEncoder = MediaRecorder.AudioEncoder.AAC;
    int audioChannel = 1;
    int audioSamplingRate = 44100;
    int audioEncodingBitRate = 192000;
    long mStartRecordingTs = 0;
    int mDuration = 0;
    MediaRecorder mMediaRecorder;

//    String filePath;

    public int getAudioSource() {
        return audioSource;
    }

    public void setAudioSource(int audioSource) {
        this.audioSource = audioSource;
    }

    public int getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(int outputFormat) {
        this.outputFormat = outputFormat;
    }

    public int getAudioEncoder() {
        return audioEncoder;
    }

    public void setAudioEncoder(int audioEncoder) {
        this.audioEncoder = audioEncoder;
    }

    public int getAudioChannel() {
        return audioChannel;
    }

    public void setAudioChannel(int audioChannel) {
        this.audioChannel = audioChannel;
    }

    public int getAudioSamplingRate() {
        return audioSamplingRate;
    }

    public void setAudioSamplingRate(int audioSamplingRate) {
        this.audioSamplingRate = audioSamplingRate;
    }

    public int getAudioEncodingBitRate() {
        return audioEncodingBitRate;
    }

    public void setAudioEncodingBitRate(int audioEncodingBitRate) {
        this.audioEncodingBitRate = audioEncodingBitRate;
    }

    private void config(String filePath) {
        if (mMediaRecorder == null) {
            mMediaRecorder = new MediaRecorder();
        }
        mMediaRecorder.setAudioSource(audioSource);
        mMediaRecorder.setOutputFormat(outputFormat);
        mMediaRecorder.setOutputFile(filePath);
        mMediaRecorder.setAudioEncoder(audioEncoder);
        mMediaRecorder.setAudioChannels(audioChannel);
        mMediaRecorder.setAudioSamplingRate(audioSamplingRate);
        mMediaRecorder.setAudioEncodingBitRate(audioEncodingBitRate);
    }

    public void start(String filePath) {
        config(filePath);
        try {
            if (mMediaRecorder != null) {
                mMediaRecorder.prepare();
                mMediaRecorder.start();
            }
            mStartRecordingTs = System.currentTimeMillis();
        } catch (IOException e) {
            MyLog.e(TAG, e);
        }
    }

    public void stop() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();
        }
        mDuration = (int) (System.currentTimeMillis() - mStartRecordingTs);
    }

    public void destroy() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    /**
     * 单位ms
     *
     * @return
     */
    public int getDuration() {
        return mDuration;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        MyMediaRecorder mParams = new MyMediaRecorder();

        Builder() {
        }

        public Builder setAudioSource(int audioSource) {
            mParams.setAudioSource(audioSource);
            return this;
        }

        public Builder setOutputFormat(int outputFormat) {
            mParams.setOutputFormat(outputFormat);
            return this;
        }

        public Builder setAudioEncoder(int audioEncoder) {
            mParams.setAudioEncoder(audioEncoder);
            return this;
        }

        public Builder setAudioChannel(int audioChannel) {
            mParams.setAudioChannel(audioChannel);
            return this;
        }

        public Builder setAudioSamplingRate(int audioSamplingRate) {
            mParams.setAudioSamplingRate(audioSamplingRate);
            return this;
        }

        public Builder setAudioEncodingBitRate(int audioEncodingBitRate) {
            mParams.setAudioEncodingBitRate(audioEncodingBitRate);
            return this;
        }

        public MyMediaRecorder build() {
            return mParams;
        }

    }
}
