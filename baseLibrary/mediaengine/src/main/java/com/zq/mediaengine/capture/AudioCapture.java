package com.zq.mediaengine.capture;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.util.Log;

import com.zq.mediaengine.capture.audio.IKSYAudioRecord;
import com.zq.mediaengine.capture.audio.KSYAudioDummyRecord;
import com.zq.mediaengine.capture.audio.KSYAudioRecord;
import com.zq.mediaengine.capture.audio.KSYAudioSLRecord;
import com.zq.mediaengine.filter.audio.AudioBufSrcPin;
import com.zq.mediaengine.framework.AVConst;
import com.zq.mediaengine.framework.AudioBufFormat;
import com.zq.mediaengine.framework.AudioBufFrame;
import com.zq.mediaengine.framework.SrcPin;
import com.zq.mediaengine.util.audio.AudioUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Capture audio data from microphone.
 */

public class AudioCapture {
    private static final String TAG = "AudioCapture";
    private static final boolean VERBOSE = false;

    public static final int AUDIO_CAPTURE_TYPE_AUDIORECORDER = 1;
    public static final int AUDIO_CAPTURE_TYPE_OPENSLES = 2;
    public static final int AUDIO_CAPTURE_TYPE_DUMMY = 3;

    public static final int STATE_IDLE = 0;
    public static final int STATE_INITIALIZED = 1;
    public static final int STATE_RECORDING = 2;

    public static final int AUDIO_ERROR_UNKNOWN = -2005;
    public static final int AUDIO_START_FAILED = -2003;

    private int mSampleRate = 44100;
    private int mChannels = 1;
    private int mAudioCaptureType = AUDIO_CAPTURE_TYPE_AUDIORECORDER;
    private float mVolume = 1.0f;

    private Context mContext;
    private IKSYAudioRecord mAudioRecord;
    private Thread mThread;
    private Handler mMainHandler;
    private int mState;
    private boolean mEnableLatencyTest;
    private volatile boolean mStop;

    public SrcPin<AudioBufFrame> mSrcPin;

    private OnAudioCaptureListener mOnAudioCaptureListener;

    /**
     * AudioCapture status and error listener interface.
     */

    public interface OnAudioCaptureListener {
        /**
         * Notify AudioCapture status changed.
         *
         * @param status new status changed to
         */
        void onStatusChanged(int status);

        /**
         * Notify first audio packet received.
         *
         * @param time time in ms the first audio packet captured
         */
        void onFirstPacketReceived(long time);

        /**
         * Notify AudioCapture error occurred.
         *
         * @param errorCode error code.
         * @see #AUDIO_ERROR_UNKNOWN
         * @see #AUDIO_START_FAILED
         */
        void onError(int errorCode);
    }

    /**
     * Construct AudioCapture with default params.
     */
    public AudioCapture(Context context) {
        mContext = context;
        mSampleRate = AudioUtil.getNativeSampleRate(mContext);
        mChannels = 1;
        mSrcPin = new AudioBufSrcPin();
        mMainHandler = new Handler(Looper.getMainLooper());
        mState = STATE_IDLE;
    }

    public void setAudioCaptureType(int type) {
        if (isRecordingState() && mAudioCaptureType != type) {
            Log.d(TAG, "switch audio capture type from " + mAudioCaptureType + " to " + type);
            stop();
            start();
        }
        mAudioCaptureType = type;
    }

    public void setSampleRate(int sampleRate) {
        mSampleRate = sampleRate;
    }

    public void setChannels(int channels) {
        mChannels = channels;
    }

    public int getSampleRate() {
        return mSampleRate;
    }

    public int getChannels() {
        return mChannels;
    }

    public void setVolume(float volume) {
        mVolume = volume;
        if (mAudioRecord != null) {
            mAudioRecord.setVolume(volume);
        }
    }

    public float getVolume() {
        return mVolume;
    }

    /**
     * Set OnAudioCaptureListener listener.
     *
     * @param listener listener to set
     */
    public void setAudioCaptureListener(OnAudioCaptureListener listener) {
        mOnAudioCaptureListener = listener;
    }

    public SrcPin<AudioBufFrame> getSrcPin() {
        return mSrcPin;
    }

    /**
     * Get is it in recording state currently.
     *
     * @return true while in recording state, false otherwise.
     */
    public boolean isRecordingState() {
        return mThread != null;
    }

    /**
     * Start audio recording.<br/>
     * If audio recording already started, nothing will be done.
     */
    public void start() {
        if (mThread == null) {
            Log.d(TAG, "start");
            mStop = false;
            mThread = new AudioCaptureThread();
            mThread.start();
        }
    }

    /**
     * Stop audio recording.<br/>
     * If audio recording not started, nothing will be done.
     */
    public void stop() {
        if (mThread != null) {
            Log.d(TAG, "stop");
            mStop = true;
            mThread.interrupt();
            try {
                mThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                mThread = null;
            }
        }
    }

    public void setEnableLatencyTest(boolean enable) {
        mEnableLatencyTest = enable;
        if (mAudioRecord != null) {
            mAudioRecord.setEnableLatencyTest(enable);
        }
    }

    public boolean getEnableLatencyTest() {
        return mEnableLatencyTest;
    }

    public void release() {
        stop();
        if (mMainHandler != null) {
            mMainHandler.removeCallbacksAndMessages(null);
            mMainHandler = null;
        }
        mSrcPin.disconnect(true);
    }

    private void postState(int state) {
        mState = state;
        if (mMainHandler != null) {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mOnAudioCaptureListener != null) {
                        mOnAudioCaptureListener.onStatusChanged(mState);
                    }
                }
            });
        }
    }

    private void postFirstPacketReceived(final long time) {
        if (mMainHandler != null) {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mOnAudioCaptureListener != null) {
                        mOnAudioCaptureListener.onFirstPacketReceived(time);
                    }
                }
            });
        }
    }

    private void postError(final int err) {
        if (mMainHandler != null) {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mOnAudioCaptureListener != null) {
                        mOnAudioCaptureListener.onError(err);
                    }
                }
            });
        }
    }

    private class AudioCaptureThread extends Thread {

        public void run() {
            int atomSize;
            int readSize;
            boolean firstPacketReceived = false;

            // set max priority to this thread
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);

            try {
                switch (mAudioCaptureType) {
                    case AUDIO_CAPTURE_TYPE_OPENSLES:
                        atomSize = AudioUtil.getNativeBufferSize(mContext, mSampleRate);
                        readSize = atomSize;
                        int threshold = mSampleRate * 20 / 1000;
                        while (readSize < threshold) {
                            readSize += atomSize;
                        }
                        mAudioRecord = new KSYAudioSLRecord(mSampleRate, mChannels, atomSize);
                        break;
                    case AUDIO_CAPTURE_TYPE_DUMMY:
                        atomSize = mSampleRate * 10 / 1000;
                        readSize = atomSize * 2;
                        mAudioRecord = new KSYAudioDummyRecord(mSampleRate, mChannels, atomSize);
                        break;
                    case AUDIO_CAPTURE_TYPE_AUDIORECORDER:
                    default:
                        int channelConfig = (mChannels == 1) ? AudioFormat.CHANNEL_IN_MONO :
                                AudioFormat.CHANNEL_IN_STEREO;
                        atomSize = AudioRecord.getMinBufferSize(mSampleRate, channelConfig,
                                AudioFormat.ENCODING_PCM_16BIT) / (2 * mChannels);
                        readSize = atomSize;
                        mAudioRecord = new KSYAudioRecord(mSampleRate, mChannels, atomSize);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                postError(AUDIO_START_FAILED);
                return;
            }
            postState(STATE_INITIALIZED);

            readSize *= mChannels * 2;
            Log.i(TAG, "atomSize:" + atomSize);
            Log.i(TAG, "readSize:" + readSize);
            Log.i(TAG, "sampleRate:" + mSampleRate);
            Log.i(TAG, "channels:" + mChannels);

            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(readSize);
            byteBuffer.order(ByteOrder.nativeOrder());
            if (VERBOSE) {
                Log.d(TAG, "ByteBuffer size: " + readSize);
            }

            // trigger format changed event
            AudioBufFormat outFormat = new AudioBufFormat(AVConst.AV_SAMPLE_FMT_S16,
                    mSampleRate, mChannels);
            // do not use audioLD if it's not native sample rate
            if (mSampleRate == AudioUtil.getNativeSampleRate(mContext)) {
                outFormat.nativeModule = mAudioRecord.getNativeModule();
            }
            mSrcPin.onFormatChanged(outFormat);

            mAudioRecord.setEnableLatencyTest(mEnableLatencyTest);
            mAudioRecord.setVolume(mVolume);
            if (mAudioRecord.startRecording() != 0) {
                Log.e(TAG, "start recording failed!");
                postError(AUDIO_START_FAILED);
                mAudioRecord.release();
                postState(STATE_IDLE);
                return;
            }
            postState(STATE_RECORDING);
            while (!mStop) {
                int read = mAudioRecord.read(byteBuffer, readSize);
                if (mStop) {
                    break;
                }
                if (read > 0) {
                    // minus audio frame duration
                    long pts = System.nanoTime() / 1000 / 1000;
                    pts -= (long) read * 1000 / 2 / mChannels / mSampleRate;
                    if (VERBOSE) {
                        Log.d(TAG, "read " + read + " bytes pts=" + pts);
                    }

                    AudioBufFrame frame = new AudioBufFrame(outFormat, byteBuffer, pts);
                    mSrcPin.onFrameAvailable(frame);

                    if (!firstPacketReceived) {
                        firstPacketReceived = true;
                        long time = System.nanoTime() / 1000 / 1000;
                        postFirstPacketReceived(time);
                    }
                } else if (read < 0) {
                    Log.e(TAG, "read error: " + read);
                    postError(AUDIO_ERROR_UNKNOWN);
                    break;
                }
            }

            // send eos
            AudioBufFrame frame = new AudioBufFrame(outFormat, null, 0);
            frame.flags |= AVConst.FLAG_DETACH_NATIVE_MODULE;
            mSrcPin.onFrameAvailable(frame);

            // stop
            mAudioRecord.stop();
            postState(STATE_INITIALIZED);

            // release
            IKSYAudioRecord audioRecord = mAudioRecord;
            mAudioRecord = null;
            audioRecord.release();
            postState(STATE_IDLE);
        }
    }
}