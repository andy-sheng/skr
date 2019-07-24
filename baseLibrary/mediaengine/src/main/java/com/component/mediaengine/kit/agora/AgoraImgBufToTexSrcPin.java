package com.component.mediaengine.kit.agora;

import android.util.Log;

import com.component.mediaengine.capture.ImgTexSrcPin;
import com.component.mediaengine.util.gles.GLRender;

import java.nio.ByteBuffer;

import io.agora.rtc.mediaio.IVideoSink;
import io.agora.rtc.mediaio.MediaIO;

public class AgoraImgBufToTexSrcPin extends ImgTexSrcPin implements IVideoSink {
    private final static String TAG = "AgoraImgTexSrcPin";

    public AgoraImgBufToTexSrcPin(GLRender glRender) {
        super(glRender);
    }

    @Override
    public boolean onInitialize() {
        Log.i(TAG, "onInitialize");
        return true;
    }

    @Override
    public boolean onStart() {
        Log.i(TAG, "onStart");
        return true;
    }

    @Override
    public void onStop() {
        Log.i(TAG, "onStop");
    }

    @Override
    public void onDispose() {
        Log.i(TAG, "onDispose");
    }

    @Override
    public long getEGLContextHandle() {
        return 0;
    }

    @Override
    public int getBufferType() {
        return MediaIO.BufferType.BYTE_BUFFER.intValue();
    }

    @Override
    public int getPixelFormat() {
        return MediaIO.PixelFormat.I420.intValue();
    }

    @Override
    public void consumeByteBufferFrame(ByteBuffer buffer, int format, int width, int height, int rotation, long ts) {
//        Log.d(TAG, "consumeByteBufferFrame " + width + "x" + height + " rotation: " + rotation + " ts: " + ts);
        updateYUVFrame(buffer, null, width, height, rotation, ts);
    }

    @Override
    public void consumeByteArrayFrame(byte[] data, int format, int width, int height, int rotation, long ts) {
        // do nothing
    }

    @Override
    public void consumeTextureFrame(int texId, int format, int width, int height, int rotation, long ts, float[] matrix) {
        // do nothing
    }
}
