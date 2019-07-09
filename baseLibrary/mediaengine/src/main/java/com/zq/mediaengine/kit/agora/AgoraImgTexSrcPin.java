package com.zq.mediaengine.kit.agora;

import android.opengl.EGLContext;
import android.os.Build;
import android.util.Log;

import com.zq.mediaengine.framework.ImgTexFormat;
import com.zq.mediaengine.framework.ImgTexFrame;
import com.zq.mediaengine.framework.SrcPin;
import com.zq.mediaengine.util.gles.GLRender;
import com.zq.mediaengine.util.gles.TexTransformUtil;

import java.nio.ByteBuffer;

import io.agora.rtc.mediaio.IVideoSink;
import io.agora.rtc.mediaio.MediaIO;

public class AgoraImgTexSrcPin extends SrcPin<ImgTexFrame> implements IVideoSink {
    private final static String TAG = "AgoraImgTexSrcPin";

    private GLRender mGLRender;
    private ImgTexFormat mImgTexFormat;
    private int mRotation;
    private float[] mTexMatrix;

    public AgoraImgTexSrcPin(GLRender glRender) {
        if (glRender == null) {
            throw new IllegalArgumentException("glRender should not be null!");
        }
        mGLRender = glRender;
        mTexMatrix = new float[16];
    }

    @Override
    public boolean onInitialize() {
        Log.i(TAG, "onInitialize");
        return true;
    }

    @Override
    public boolean onStart() {
        Log.i(TAG, "onStart");
        mImgTexFormat = null;
        mRotation = 0;
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

    private long getNativeEglContext() {
        long ret = 0;
        EGLContext egl14Context = mGLRender.getEGLContext();
        if (egl14Context != null) {
            ret = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ?
                    egl14Context.getNativeHandle() : egl14Context.getHandle();
        } else {
            Log.e(TAG, "get EGL14Context from glRender failed!");
        }
        return ret;
    }

    @Override
    public long getEGLContextHandle() {
        long eglContextHandle = getNativeEglContext();
        Log.i(TAG, "getEGLContextHandle called with " + eglContextHandle + " returned");
        return eglContextHandle;
    }

    @Override
    public int getBufferType() {
        return MediaIO.BufferType.TEXTURE.intValue();
    }

    @Override
    public int getPixelFormat() {
        return MediaIO.PixelFormat.TEXTURE_2D.intValue();
    }

    @Override
    public void consumeByteBufferFrame(ByteBuffer buffer, int format, int width, int height, int rotation, long ts) {
        // do nothing
    }

    @Override
    public void consumeByteArrayFrame(byte[] data, int format, int width, int height, int rotation, long ts) {
        // do nothing
    }

    @Override
    public void consumeTextureFrame(int texId, int format, int width, int height, int rotation, long ts, float[] matrix) {
//        Log.d(TAG, "consumeTextureFrame texId: " + texId + " format: " + format +
//                " res: " + width + "x" + height + " rotation: " + rotation + " ts: " + ts);
        if (mImgTexFormat == null || mRotation != rotation) {
            int w = width;
            int h = height;
            if (rotation % 180 != 0) {
                w = height;
                h = width;
            }
            mRotation = rotation;
            mImgTexFormat = new ImgTexFormat(ImgTexFormat.COLOR_RGBA, w, h);
            onFormatChanged(mImgTexFormat);
        }

        // 当前未考虑matrix不为单位矩阵的情况，远程画面的matrix应该就是单位矩阵
        if (matrix == null) {
            matrix = mTexMatrix;
        }
        TexTransformUtil.calTransformMatrix(matrix, 1.0f, 1.0f, rotation);

        ImgTexFrame frame = new ImgTexFrame(mImgTexFormat, texId, matrix, ts);
        onFrameAvailable(frame);
    }
}
