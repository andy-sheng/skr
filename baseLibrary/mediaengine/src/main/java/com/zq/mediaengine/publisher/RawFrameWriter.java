package com.zq.mediaengine.publisher;

import android.text.TextUtils;
import android.util.Log;

import com.zq.mediaengine.framework.AVBufFrame;
import com.zq.mediaengine.framework.SinkPin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class RawFrameWriter {
    private static final String TAG = "RawFrameWriter";

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
                File file = new File(path);
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
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    mFileChannel = null;
                }
            }
        }
    }

    private SinkPin<? extends AVBufFrame> mSinkPin = new SinkPin<AVBufFrame>() {
        @Override
        public void onFormatChanged(Object format) {

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
