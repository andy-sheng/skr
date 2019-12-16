package com.zq.mediaengine.publisher;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.util.Log;

import com.zq.mediaengine.framework.AudioPacket;
import com.zq.mediaengine.framework.ImgPacket;

import java.io.IOException;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class MediaMuxerPublisher extends Publisher {
    private static final String TAG = "MediaMuxerPublisher";
    private static boolean VERBOSE = false;

    private MediaMuxer mMediaMuxer;
    private int mAudioTrackIndex;
    private int mVideoTrackIndex;

    public MediaMuxerPublisher() {
        super(TAG);
        setAutoWork(true);
        setBlockingMode(true);
    }

    @Override
    protected int doStart(String uri) {
        Log.d(TAG, "doStart");
        try {
            mMediaMuxer = new MediaMuxer(uri, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            Log.e(TAG, "Create MediaMuxer with path " + uri + " failed!");
            return ERROR_IO;
        }
        return 0;
    }

    @Override
    protected int doAddAudioTrack(AudioPacket packet) {
        if (mAudioTrackAdded) {
            return 0;
        }

        int ret = 0;
        try {
            MediaFormat mediaFormat = packet.format.toMediaFormat();
            mediaFormat.setByteBuffer("csd-0", packet.buf);
            packet.buf.rewind();
            mAudioTrackIndex = mMediaMuxer.addTrack(mediaFormat);
            Log.d(TAG, "add audio track");
            if (mVideoTrackAdded || mIsAudioOnly) {
                Log.d(TAG, "start mux");
                mMediaMuxer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            ret = ERROR_UNKNOWN;
        }
        return ret;
    }

    @Override
    protected int doAddVideoTrack(ImgPacket packet) {
        if (mVideoTrackAdded) {
            return 0;
        }

        int ret = 0;
        try {
            MediaFormat mediaFormat = packet.format.toMediaFormat();
            mediaFormat.setByteBuffer("csd-0", packet.buf);
            packet.buf.rewind();
            mVideoTrackIndex = mMediaMuxer.addTrack(mediaFormat);
            Log.d(TAG, "add video track");
            if (mAudioTrackAdded || mIsVideoOnly) {
                Log.d(TAG, "start mux");
                mMediaMuxer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            ret = ERROR_UNKNOWN;
        }
        return ret;
    }

    @Override
    protected int doWriteAudioPacket(AudioPacket packet) {
        try {
            if (VERBOSE) {
                Log.d(TAG, "doWriteAudioPacket size: " + packet.buf.limit() + " pts: " + packet.pts);
            }
            MediaCodec.BufferInfo bufferInfo = packet.toBufferInfo();
            mMediaMuxer.writeSampleData(mAudioTrackIndex, packet.buf, bufferInfo);
        } catch (Exception e) {
            e.printStackTrace();
            return ERROR_UNKNOWN;
        }
        return 0;
    }

    @Override
    protected int doWriteVideoPacket(ImgPacket packet) {
        try {
            if (VERBOSE) {
                Log.d(TAG, "doWriteVideoPacket size: " + packet.buf.limit() + " pts: " + packet.pts);
            }
            MediaCodec.BufferInfo bufferInfo = packet.toBufferInfo();
            mMediaMuxer.writeSampleData(mVideoTrackIndex, packet.buf, bufferInfo);
        } catch (Exception e) {
            e.printStackTrace();
            return ERROR_UNKNOWN;
        }
        return 0;
    }

    @Override
    protected void doAbort() {
        // do nothing
    }

    @Override
    protected void doStop() {
        try {
            Log.d(TAG, "doStop");
            mMediaMuxer.stop();
            mMediaMuxer.release();
            mMediaMuxer = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doRelease() {
        Log.d(TAG, "doRelease");
        if (mMediaMuxer != null) {
            doStop();
        }
    }

    @Override
    protected boolean isAddExtraForVideoKeyFrame() {
        return false;
    }
}
