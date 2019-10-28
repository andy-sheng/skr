package com.zq.mediaengine.kit.filter;

import android.util.Log;

import com.engine.Params;
import com.zq.mediaengine.filter.audio.AudioFilterBase;
import com.zq.mediaengine.framework.AudioBufFormat;
import com.zq.mediaengine.framework.AudioBufFrame;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

public class AudioDummyFilter extends AudioFilterBase {
    private static final String TAG = "AudioDummyFilter";

    private String mPath;
    private Params mConfig;
    private FileChannel mFileChannel;
    private RandomAccessFile mRandomAccessFile;

    public AudioDummyFilter() {
    }

    public void init(String path, Params config) {
        mPath = path;
        mConfig = config;
    }

    @Override
    protected AudioBufFormat doFormatChanged(AudioBufFormat format) {
        try {
            if (mRandomAccessFile != null) {
                Log.d(TAG, "close file: " + mPath);
                mFileChannel.close();
                mRandomAccessFile.close();
            }
            Log.d(TAG, "create file: " + mPath);
            mRandomAccessFile = new RandomAccessFile(mPath, "r");
            mFileChannel = mRandomAccessFile.getChannel();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return format;
    }

    @Override
    protected AudioBufFrame doFilter(AudioBufFrame frame) {
        if (mFileChannel == null || mConfig == null || mConfig.getAccTs() <= 0) {
            return frame;
        }
        frame.buf.clear();
        try {
            mFileChannel.read(frame.buf);
            frame.buf.flip();
            //Log.d(TAG, "read " + frame.buf.limit() + " bytes");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return frame;
    }

    @Override
    protected void doRelease() {
        try {
            if (mRandomAccessFile != null) {
                mFileChannel.close();
                mRandomAccessFile.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
