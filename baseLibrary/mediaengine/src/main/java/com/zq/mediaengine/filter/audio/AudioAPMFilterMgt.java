package com.zq.mediaengine.filter.audio;

import com.zq.mediaengine.framework.AVConst;
import com.zq.mediaengine.framework.AudioBufFormat;
import com.zq.mediaengine.framework.AudioBufFrame;
import com.zq.mediaengine.framework.PinAdapter;
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

    private SinkPin<AudioBufFrame> mInputSinkPin;
    private SrcPin<AudioBufFrame> mInputSrcPin;

    private PinAdapter<AudioBufFrame> mOutputFilter;
    private AudioResampleFilter mAudioResample;
    private APMFilter mAPMFilter;
    private AudioBufFormat mOutFormat;
    private boolean mEnableNS = false;
    private int mNSLevel = APMWrapper.NS_LEVEL_MODERATE;

    public AudioAPMFilterMgt() {
        mInputSinkPin = new AudioSinkAdapter();
        mInputSrcPin = new AudioBufSrcPin();
        mOutputFilter = new AudioBufPinAdapter();
        mAudioResample = new AudioResampleFilter();
        mAPMFilter = new APMFilter();

        mAPMFilter.enableNs(mEnableNS);
        mAPMFilter.setNsLevel(mNSLevel);

        mInputSrcPin.connect(mAudioResample.getSinkPin());
        mAudioResample.getSrcPin().connect(mAPMFilter.getSinkPin());
        mAPMFilter.getSrcPin().connect(mOutputFilter.mSinkPin);
    }

    /**
     * get input pin instance of AudioAPMFilterMgt
     *
     * @return input pin instance
     */
    public SinkPin<AudioBufFrame> getSinkPin() {
        return mInputSinkPin;
    }

    /**
     * get output pin instance of AudioAPMFilterMgt
     *
     * @return output pin instance
     */
    public SrcPin<AudioBufFrame> getSrcPin() {
        return mOutputFilter.mSrcPin;
    }

    protected void doRelease() {
    }

    public void release() {
        mInputSrcPin.disconnect(true);
        doRelease();
    }

    private class AudioSinkAdapter extends SinkPin<AudioBufFrame> {

        @Override
        public void onFormatChanged(Object format) {
            if (format == null) {
                return;
            }
            AudioBufFormat tmpFormat = (AudioBufFormat) format;
            switch (tmpFormat.sampleRate) {
                case kSampleRate8kHz:
                case kSampleRate16kHz:
                case kSampleRate32kHz:
                case kSampleRate48kHz: {
                    mOutFormat = new AudioBufFormat(AVConst.AV_SAMPLE_FMT_S16, tmpFormat.sampleRate, tmpFormat.channels);
                    break;
                }
                case kSampleRate44kHz:
                default: {
                    mOutFormat = new AudioBufFormat(AVConst.AV_SAMPLE_FMT_S16, kSampleRate48kHz, tmpFormat.channels);
                    break;
                }
            }
            mAudioResample.setOutFormat(mOutFormat);
            mInputSrcPin.onFormatChanged(format);
        }

        @Override
        public void onFrameAvailable(AudioBufFrame frame) {
            mInputSrcPin.onFrameAvailable(frame);
        }

        @Override
        public void onDisconnect(boolean recursive) {
            if (recursive) {
                release();
            }
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

    public boolean getNSState() {
        return mEnableNS;
    }
}
