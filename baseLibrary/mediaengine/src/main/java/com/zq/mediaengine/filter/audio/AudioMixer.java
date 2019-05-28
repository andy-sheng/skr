package com.zq.mediaengine.filter.audio;

import android.util.Log;

import com.zq.mediaengine.framework.AVConst;
import com.zq.mediaengine.framework.AudioBufFormat;
import com.zq.mediaengine.framework.AudioBufFrame;
import com.zq.mediaengine.framework.SinkPin;
import com.zq.mediaengine.framework.SrcPin;
import com.zq.mediaengine.util.LibraryLoader;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

/**
 * Audio mixer.<br/>
 * With maximum {@link #getSinkPinNum()} input pins(input tracks), and an output pin.
 * <p>
 * This module would mix all of the input audio tracks into one.
 */

public class AudioMixer {
    private static final String TAG = "AudioMixer";
    private static final int MAX_SINKPIN_NUM = 8;
    private static final boolean VERBOSE = false;
    private static final int INSTANCE_UNINIT = 0;

    /**
     * Input pins
     */
    private List<SinkPin<AudioBufFrame>> mSinkPins;

    /**
     * Output pins
     */
    private SrcPin<AudioBufFrame> mSrcPin;

    private long mInstance = INSTANCE_UNINIT;
    private int mMainSinkPinIndex = 0;
    private float[][] mInputVolumes;
    private float mLeftOutputVolume;
    private float mRightOutputVolume;
    private boolean mMute;
    private boolean mBlockingMode;

    private AudioBufFormat mInFormats[];
    private AudioBufFormat mOutFormat;

    public AudioMixer() {
        mSinkPins = new LinkedList<>();
        mSrcPin = new AudioBufSrcPin();
        mLeftOutputVolume = 1.0f;
        mRightOutputVolume = 1.0f;
        mInputVolumes = new float[getSinkPinNum()][2];
        for (int i = 0; i < getSinkPinNum(); i++) {
            mSinkPins.add(new AudioMixerSinkPin(i));
            mInputVolumes[i][0] = 1.0f;
            mInputVolumes[i][1] = 1.0f;
        }
        mInFormats = new AudioBufFormat[getSinkPinNum()];
        mInstance = _init();
    }

    /**
     * Get sink pin by index
     *
     * @param index index
     * @return SinPin object or null
     */
    public SinkPin<AudioBufFrame> getSinkPin(int index) {
        if (mSinkPins.size() > index) {
            return mSinkPins.get(index);
        }
        return null;
    }

    /**
     * Get output pin.
     *
     * @return output pin instance
     */
    public SrcPin<AudioBufFrame> getSrcPin() {
        return mSrcPin;
    }

    /**
     * Set main input pin, generally the mic input
     *
     * @param index index of the main input pin, default 0
     */
    public final void setMainSinkPinIndex(int index) {
        mMainSinkPinIndex = index;
        _setMainIdx(mInstance, index);
    }

    /**
     * Set input audio source volume,
     * the source audio data would multiply this value before mix.
     *
     * @param idx SinkPin index
     * @param vol volume in [0.0f-1.0f]
     */
    public void setInputVolume(int idx, float vol) {
        setInputVolume(idx, vol, vol);
    }

    /**
     * Set input audio source volume for left and right channel,
     * the source audio data would multiply this value before mix.
     *
     * @param idx SinkPin index
     * @param leftVol left channel volume in [0.0f-1.0f]
     * @param rightVol right channel volume in [0.0f-1.0f]
     */
    public void setInputVolume(int idx, float leftVol, float rightVol) {
        if (idx < mInputVolumes.length) {
            mInputVolumes[idx][0] = leftVol;
            mInputVolumes[idx][1] = rightVol;
            _setInputVolume(mInstance, idx, leftVol, rightVol);
        }
    }

    /**
     * return input audio source left channel volume
     *
     * @param idx SinkPin index
     * @return volume in [0.0f-1.0f], null if idx if out of range
     */
    public float getInputVolume(int idx) {
        if (idx < mInputVolumes.length) {
            return mInputVolumes[idx][0];
        } else {
            return 0;
        }
    }
    /**
     * return input audio source volume
     *
     * @param idx SinkPin index
     * @param isLeftChannel is left channel or right channel
     * @return volume in [0.0f-1.0f], null if idx if out of range
     */
    public float getInputVolume(int idx, boolean isLeftChannel) {
        if (idx < mInputVolumes.length) {
            if(isLeftChannel) {
                return mInputVolumes[idx][0];
            } else {
                return mInputVolumes[idx][1];
            }
        } else {
            return 0;
        }
    }

    /**
     * Set output audio volume, the mixer result would multiply this value.
     *
     * @param vol volume in [0.0f-1.0f]
     */
    public void setOutputVolume(float vol) {
        setOutputVolume(vol, vol);
    }

    /**
     * Set output audio volume for left and right channel, the mixer result would multiply this
     * value.
     * @param leftVol left channel volume
     * @param rightVol right channel volume
     */
    public void setOutputVolume(float leftVol, float rightVol) {
        mLeftOutputVolume = leftVol;
        mRightOutputVolume = rightVol;
        _setOutputVolume(mInstance, mLeftOutputVolume, mRightOutputVolume);
    }

    /**
     * Get output audio volume.If mono, return left channel volume
     *
     * @return output volume, default value 1.0f
     */
    public float getOutputVolume() {
        return mLeftOutputVolume;
    }

    /**
     * Get output audio volume.
     * @param isLeftChannel left or right channel
     * @return output volume, default value 1.0f
     */
    public float getOutputVolume(boolean isLeftChannel) {
        if(isLeftChannel) {
            return mLeftOutputVolume;
        } else {
            return mRightOutputVolume;
        }
    }

    /**
     * Mute mixer output or not
     *
     * @param mute true to mute mixer output, false otherwise
     */
    public void setMute(boolean mute) {
        mMute = mute;
        _setMute(mInstance, mute);
    }

    /**
     * check if audio muted or not
     *
     * @return true if muted, false otherwise
     */
    public boolean getMute() {
        return mMute;
    }

    /**
     * Set use blocking mode or not.
     *
     * @param blockingMode true to enable, false to disable
     */
    public void setBlockingMode(boolean blockingMode) {
        mBlockingMode = blockingMode;
        _setBlockingMode(mInstance, blockingMode);
    }

    /**
     * Check if in blocking mode.
     * <p>
     * Default value is false.
     *
     * @return true if enabled, false if disabled
     */
    public boolean getBlockingMode() {
        return mBlockingMode;
    }

    /**
     * Get maximum input pin number of current mixer.
     *
     * @return maximum input pin number
     */
    public int getSinkPinNum() {
        return MAX_SINKPIN_NUM;
    }

    public int getEmptySinkPin() {
        for (int i = 0; i < getSinkPinNum(); i++) {
            if (!mSinkPins.get(i).isConnected()) {
                return i;
            }
        }
        return -1;
    }

    synchronized public void release() {
        doRelease();
    }

    private void doRelease() {
        mSrcPin.disconnect(true);
        mSinkPins.clear();
        if (mInstance != INSTANCE_UNINIT) {
            _release(mInstance);
            mInstance = INSTANCE_UNINIT;
        }
    }

    synchronized protected void doDisconnect(int idx, boolean recursive) {
        if (mInstance != INSTANCE_UNINIT) {
            _destroy(mInstance, idx);
        }
        if (idx == mMainSinkPinIndex) {
            if (recursive) {
                doRelease();
            }
        }
    }

    synchronized protected void doFormatChanged(int idx, AudioBufFormat format) {
        if (format == null) {
            return;
        }

        mInFormats[idx] = format;
        Log.d(TAG, "doFormatChanged " + idx + " nativeModule=" + format.nativeModule);
        if (format.nativeModule != 0) {
            _attachTo(mInstance, idx, format.nativeModule, false);
        } else {
            _config(mInstance, idx, format.sampleFormat, format.sampleRate, format.channels, 1024, 300);
        }
        if (idx == mMainSinkPinIndex) {
            mOutFormat = new AudioBufFormat(format.sampleFormat,
                    format.sampleRate, format.channels);
            if (format.nativeModule != 0) {
                mOutFormat.nativeModule = mInstance;
            }
            mSrcPin.onFormatChanged(mOutFormat);
        }
    }

    protected void doFrameAvailable(int idx, AudioBufFrame frame) {
        if ((frame.flags & AVConst.FLAG_DETACH_NATIVE_MODULE) != 0) {
            if (frame.format.nativeModule != 0) {
                _attachTo(mInstance, idx, frame.format.nativeModule, true);
            }
            if(mInstance != INSTANCE_UNINIT) {
                _destroy(mInstance, idx);
            }
        }

        // handle eos, for blocking mode
        if ((frame.flags & AVConst.FLAG_END_OF_STREAM) != 0 && mInstance != INSTANCE_UNINIT) {
                _destroy(mInstance, idx);
        }

        if (frame.buf != null && frame.format.nativeModule == 0 && mInstance != INSTANCE_UNINIT) {
            _process(mInstance, idx, frame.buf, frame.buf.limit());
        }

        if (idx == mMainSinkPinIndex) {
            // read native if needed
            if (frame.buf != null && frame.format.nativeModule != 0) {
                int ret = _read(mInstance, frame.buf, frame.buf.limit());
                if (ret <= 0) {
                    Log.e(TAG, "readNative failed ret=" + ret);
                }
            }

            AudioBufFrame outFrame = new AudioBufFrame(frame);
            outFrame.format = mOutFormat;
            mSrcPin.onFrameAvailable(outFrame);
        }
    }

    private class AudioMixerSinkPin extends SinkPin<AudioBufFrame> {
        private int mIndex;

        public AudioMixerSinkPin(int idx) {
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
        public void onDisconnect(boolean recursive) {
            super.onDisconnect(recursive);
            doDisconnect(mIndex, recursive);
        }
    }

    private native long _init();

    private native void _setMainIdx(long instance, int idx);

    private native void _setMute(long instance, boolean mute);

    private native void _setBlockingMode(long instance, boolean blockingMode);

    private native void _setOutputVolume(long instance, float vol);

    private native void _setOutputVolume(long instance, float leftVol, float rightVol);

    private native void _setInputVolume(long instance, int idx, float vol);

    private native void _setInputVolume(long instance, int idx, float leftVol, float rightVol);

    private native void _attachTo(long instance, int idx, long ptr, boolean detach);

    private native int _config(long instance, int idx, int sampleFmt, int sampleRate,
                               int channels, int bufferSamples, int fifoSizeInMs);

    private native void _destroy(long instance, int idx);

    private native int _read(long instance, ByteBuffer buffer, int size);

    private native int _process(long instance, int idx, ByteBuffer buffer, int size);

    private native void _release(long instance);

    static {
        LibraryLoader.load();
    }
}
