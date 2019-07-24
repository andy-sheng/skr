package com.zq.mediaengine.filter.audio;

import com.zq.mediaengine.framework.AVConst;
import com.zq.mediaengine.framework.AudioBufFormat;
import com.zq.mediaengine.framework.AudioBufFrame;
import com.zq.mediaengine.framework.SinkPin;
import com.zq.mediaengine.framework.SrcPin;

/**
 * APM and Resample Filter manager for convenient use.
 */

public class AudioAPMFilterMgt {
    private final static String TAG = "AudioAPMFilterMgt";
    private final static int kSampleRate8kHz = 8000;
    private final static int kSampleRate16kHz = 16000;
    private final static int kSampleRate32kHz = 32000;
    private final static int kSampleRate44kHz = 44100;
    private final static int kSampleRate48kHz = 48000;

    public final static int AUDIO_NS_LEVEL_0 = APMWrapper.NS_LEVEL_LOW;
    public final static int AUDIO_NS_LEVEL_1 = APMWrapper.NS_LEVEL_MODERATE;
    public final static int AUDIO_NS_LEVEL_2 = APMWrapper.NS_LEVEL_HIGH;
    public final static int AUDIO_NS_LEVEL_3 = APMWrapper.NS_LEVEL_VERYHIGH;

    private APMSinkPin[] mSinkPins;
    private AudioBufSrcPin[] mSrcPins;
    private AudioResampleFilter[] mResampleFilters;

    private APMFilter mAPMFilter;
    private boolean mEnableNS = false;
    private int mNSLevel = APMWrapper.NS_LEVEL_MODERATE;

    public AudioAPMFilterMgt() {
        mSinkPins = new APMSinkPin[2];
        mSrcPins = new AudioBufSrcPin[2];
        mResampleFilters = new AudioResampleFilter[2];

        for (int i = 0; i < 2; i++) {
            mSinkPins[i] = new APMSinkPin(i);
            mSrcPins[i] = new AudioBufSrcPin();
            mResampleFilters[i] = new AudioResampleFilter();
        }

        mAPMFilter = new APMFilter();
        mAPMFilter.enableNs(mEnableNS);
        mAPMFilter.setNsLevel(mNSLevel);
    }

    /**
     * get input pin instance of AudioAPMFilterMgt
     *
     * @return input pin instance
     */
    public SinkPin<AudioBufFrame> getSinkPin() {
        return mSinkPins[0];
    }

    public SinkPin<AudioBufFrame> getReverseSinkPin() {
        return mSinkPins[1];
    }

    /**
     * get output pin instance of AudioAPMFilterMgt
     *
     * @return output pin instance
     */
    public SrcPin<AudioBufFrame> getSrcPin() {
        return mAPMFilter.getSrcPin();
    }

    public void release() {
        for (int i = 0; i < 2; i++) {
            mSrcPins[i].disconnect(true);
            mResampleFilters[i].release();
        }
    }

    private boolean isNeedResample(int sampleRate) {
        return sampleRate != kSampleRate8kHz &&
                sampleRate != kSampleRate16kHz &&
                sampleRate != kSampleRate32kHz &&
                sampleRate != kSampleRate48kHz;

//        return false;
    }

    private AudioBufFormat getResampleOutFormat(AudioBufFormat format) {
        int sampleRate = format.sampleRate;
        if (isNeedResample(sampleRate)) {
            if (sampleRate == kSampleRate44kHz) {
                sampleRate = kSampleRate48kHz;
            } else {
                sampleRate = kSampleRate32kHz;
            }
        }
        return new AudioBufFormat(AVConst.AV_SAMPLE_FMT_S16, sampleRate, format.channels);
    }

    private SinkPin<AudioBufFrame> getAPMSink(int idx) {
        return idx == 0 ? mAPMFilter.getSinkPin() : mAPMFilter.getReverseSinkPin();
    }

    private void doFormatChanged(int idx, AudioBufFormat format) {
        mResampleFilters[idx].getSrcPin().disconnect(false);
        mSrcPins[idx].disconnect(false);
        if (isNeedResample(format.sampleRate)) {
            AudioBufFormat outFormat = getResampleOutFormat(format);
            mSrcPins[idx].connect(mResampleFilters[idx].getSinkPin());
            mResampleFilters[idx].getSrcPin().connect(getAPMSink(idx));
            mResampleFilters[idx].setOutFormat(outFormat);
        } else {
            mSrcPins[idx].connect(getAPMSink(idx));
        }
        mSrcPins[idx].onFormatChanged(format);
    }

    private void doFrameAvailable(int idx, AudioBufFrame frame) {
        mSrcPins[idx].onFrameAvailable(frame);
    }

    private void doDisconnect(int idx, boolean recursive) {
        if (idx == 0 && recursive) {
            release();
        }
    }

    private class APMSinkPin extends SinkPin<AudioBufFrame> {
        private int mIndex;

        public APMSinkPin(int idx) {
            mIndex = idx;
        }

        @Override
        public void onFormatChanged(Object format) {
            doFormatChanged(mIndex, (AudioBufFormat) format);
        }

        @Override
        public void onFrameAvailable(AudioBufFrame frame) {
            doFrameAvailable(mIndex, frame);
        }

        @Override
        public synchronized void onDisconnect(boolean recursive) {
            doDisconnect(mIndex, recursive);
            super.onDisconnect(recursive);
        }
    }

    /**
     * Set audio ns level, default {@link #AUDIO_NS_LEVEL_1}.
     *
     * @param nsLevel ns level to be set
     * @see #AUDIO_NS_LEVEL_0
     * @see #AUDIO_NS_LEVEL_1
     * @see #AUDIO_NS_LEVEL_2
     * @see #AUDIO_NS_LEVEL_3
     */
    public void setAudioNSLevel(int nsLevel) {
        mNSLevel = nsLevel;
        mAPMFilter.setNsLevel(mNSLevel);
    }

    /**
     * set if enable use the NoiseSuppression
     *
     * @param enable true to enable, false to disable.
     */
    public void setEnableAudioNS(boolean enable) {
        if (mEnableNS == enable) {
            return;
        }
        mEnableNS = enable;
        mAPMFilter.enableNs(enable);
    }

    public int enableAECM(boolean enable) {
        return mAPMFilter.enableAECM(enable);
    }

    public int enableAEC(boolean enable) {
        return mAPMFilter.enableAEC(enable);
    }

    public int setRoutingMode(int mode) {
        return mAPMFilter.setRoutingMode(mode);
    }

    public int setStreamDelay(int delay) {
        return mAPMFilter.setStreamDelay(delay);
    }

    public boolean getNSState() {
        return mEnableNS;
    }
}
