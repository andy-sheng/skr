package com.component.mediaengine.util.audio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @hide
 */
public class PcmPlayer {
    public static final String TAG = "PcmPlayer";

    private static final boolean VERBOSE = true;
    private static final int QUEUE_SIZE = 4;

    private static final int CMD_STOP = 3;
    private static final int CMD_RELEASE = 4;

    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private AudioTrack mAudioTrack;
    private BlockingQueue<short[]> mAudioQueue;

    private int mSampleRate = 44100;
    private int mChannel = AudioFormat.CHANNEL_OUT_MONO;
    private int mSampleFormat = AudioFormat.ENCODING_PCM_16BIT;

    public PcmPlayer() {
        init();
    }

    public PcmPlayer(int sampleRate, int channel, int sampleFormat) {
        mSampleRate = sampleRate;
        mChannel = channel;
        mSampleFormat = sampleFormat;
        init();
    }

    private void init() {
        initThread();
        mAudioQueue = new ArrayBlockingQueue<>(QUEUE_SIZE);
        for (int i = 0; i < QUEUE_SIZE; i++) {
            short[] data = new short[2048];
            mAudioQueue.offer(data);
        }
    }

    private void initThread() {
        mHandlerThread = new HandlerThread("pcm_player_thread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case CMD_STOP:
                        doStop();
                        break;
                    case CMD_RELEASE:
                        mHandlerThread.quit();
                        break;
                    default:
                        break;
                }
            }
        };
    }

    private void doStop() {
        if (mAudioTrack != null) {
            mAudioTrack.pause();
            mAudioTrack.flush();
            mAudioTrack.stop();
            mAudioTrack.release();
            mAudioTrack = null;
        }
    }

    private void playInternal(final short[] pcm, final int off, final int len) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mAudioTrack == null) {
                    int minBufferSize = AudioTrack.getMinBufferSize(mSampleRate,
                            mChannel, mSampleFormat);
                    mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mSampleRate, mChannel,
                            mSampleFormat, minBufferSize, AudioTrack.MODE_STREAM);
                    mAudioTrack.play();
                }
                mAudioTrack.write(pcm, off, len);
                mAudioQueue.offer(pcm);
            }
        });
    }

    private short[] getPcmCache(int len) {
        short[] data = mAudioQueue.poll();
        if (data == null) {
            if (VERBOSE) {
                Log.w(TAG, "Dropped " + len * 2 + " bytes pcm data");
            }
            return null;
        }

        int dataLen = data.length;
        while (dataLen < len) {
            dataLen *= 2;
        }
        if (data.length < dataLen) {
            if (VERBOSE) {
                Log.d(TAG, "realloc pcm size from " + data.length + " to " + dataLen);
            }
            data = new short[dataLen];
        }
        return data;
    }

    public void play(short[] pcm, int off, int len) {
        if (pcm == null || pcm.length < off + len) {
            return;
        }
        short[] data = getPcmCache(len);
        if (data != null) {
            System.arraycopy(pcm, off, data, 0, len);
            playInternal(data, 0, len);
        }
    }

    public void play(short[] pcm) {
        play(pcm, 0, pcm.length);
    }

    public void play(ByteBuffer buffer) {
        if (buffer == null) {
            return;
        }
        int len = buffer.limit() / 2;
        short[] data = getPcmCache(len);
        if (data != null) {
            ShortBuffer shortBuffer = buffer.asShortBuffer();
            shortBuffer.limit(len);
            shortBuffer.get(data, 0, len);
            playInternal(data, 0, len);
        }
    }

    public void stop() {
        mHandler.removeCallbacksAndMessages(null);
        mHandler.sendEmptyMessage(CMD_STOP);
    }

    public void release() {
        if (mHandlerThread != null) {
            stop();
            mHandler.sendEmptyMessage(CMD_RELEASE);
            try {
                mHandlerThread.join();
            } catch (InterruptedException e) {
                Log.d(TAG, "Pcm Player Thread Interrupted!");
            }
            mHandlerThread = null;
            mHandler = null;
        }
        mAudioQueue.clear();
    }
}
