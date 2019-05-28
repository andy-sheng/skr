package com.zq.mediaengine.framework;

import android.opengl.Matrix;

import com.zq.mediaengine.util.gles.FboManager;

/**
 * Image texture frame definition.
 */
public class ImgTexFrame extends AVFrameBase {
    public static final int NO_TEXTURE = -1;
    private static final float[] TEX_MATRIX = new float[16];

    public ImgTexFormat format;
    public int textureId;
    public final float[] texMatrix;

    // FboManager for frame reuse
    private FboManager fboManager = null;

    public ImgTexFrame(ImgTexFormat format, FboManager fboManager, int textureId,
                       float[] texMatrix, long pts) {
        this(format, textureId, texMatrix, pts);
        this.fboManager = fboManager;
    }

    public ImgTexFrame(ImgTexFormat format, int textureId, float[] texMatrix, long pts) {
        this.format = format;
        this.textureId = textureId;
        this.pts = pts;
        this.flags = 0;
        if (texMatrix == null || texMatrix.length != 16) {
            this.texMatrix = TEX_MATRIX;
            Matrix.setIdentityM(this.texMatrix, 0);
        } else {
            this.texMatrix = texMatrix;
        }
    }

    public ImgTexFrame(ImgTexFrame frame) {
        format = frame.format;
        textureId = frame.textureId;
        texMatrix = frame.texMatrix;
        pts = frame.pts;
        flags = frame.flags;
        if (frame.isRefCounted()) {
            fboManager = frame.fboManager;
            fboManager.lock(frame.textureId);
        }
    }

    @Override
    public boolean isRefCounted() {
        return fboManager != null && textureId != NO_TEXTURE;
    }

    @Override
    synchronized public void ref() {
        if (isRefCounted()) {
            fboManager.lock(textureId);
        }
    }

    @Override
    synchronized public void unref() {
        if (isRefCounted()) {
            fboManager.unlock(textureId);
        }
    }
}
