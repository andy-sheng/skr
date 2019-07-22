package com.zq.mediaengine.publisher;

import android.text.TextUtils;
import android.util.Log;

import com.common.utils.U;
import com.zq.mediaengine.framework.AVBufFrame;
import com.zq.mediaengine.framework.AudioBufFormat;
import com.zq.mediaengine.framework.SinkPin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class RawFrameWriter {
    private static final String TAG = "RawFrameWriter";

    private Object mFormat;
    private String mPath;
    private String mTempPath;
    private FileChannel mFileChannel;

    public SinkPin<? extends AVBufFrame> getSinkPin() {
        return mSinkPin;
    }

    public void start(String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        synchronized (this) {
            try {
                mPath = path;
                mTempPath = path;
                String ext = path.substring(path.lastIndexOf('.') + 1);
                if ("wav".equals(ext)) {
                    String name = path.substring(0, path.lastIndexOf('.'));
                    mTempPath = name + ".pcm";
                }

                File file = new File(mTempPath);
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                mFileChannel = new FileOutputStream(file).getChannel();
                Log.d(TAG, "start with " + path);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                mFileChannel = null;
            }
        }
    }

    public void stop() {
        synchronized (this) {
            if (mFileChannel != null) {
                try {
                    mFileChannel.close();
                    Log.d(TAG, "stop");
                    rawEncode();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    mFileChannel = null;
                }
            }
        }
    }

    private void rawEncode() {
        if (mFormat == null) {
            return;
        }

        String subfix = mPath.substring(mPath.lastIndexOf('.') + 1);
        if ("wav".equals(subfix) && mFormat instanceof AudioBufFormat) {
            pcmToWav();
        }
    }

    private void pcmToWav() {
        AudioBufFormat format = (AudioBufFormat) mFormat;
        File file = new File(mTempPath);
        File destFile = new File(mPath);
        int byteRate = format.sampleRate * format.channels * 2;
        Log.d(TAG, "pcmToWav: " + format.sampleRate + "Hz" + " " + format.channels + " " + byteRate + "bytes/s");
        try {
            U.getMediaUtils().rawToWave(file, destFile, format.channels, format.sampleRate, byteRate);
            file.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private SinkPin<? extends AVBufFrame> mSinkPin = new SinkPin<AVBufFrame>() {
        @Override
        public void onFormatChanged(Object format) {
            mFormat = format;
        }

        @Override
        public void onFrameAvailable(AVBufFrame frame) {
            synchronized (RawFrameWriter.this) {
                if (mFileChannel != null && frame.buf != null) {
                    try {
                        mFileChannel.write(frame.buf);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        frame.buf.rewind();
                    }
                }
            }
        }

        @Override
        public synchronized void onDisconnect(boolean recursive) {
            super.onDisconnect(recursive);
            stop();
        }
    };
}
