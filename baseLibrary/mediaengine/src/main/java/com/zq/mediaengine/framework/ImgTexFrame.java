package com.zq.mediaengine.framework;

import android.opengl.Matrix;

/**
 * Image texture frame definition.
 */
public class ImgTexFrame extends AVFrameBase {
    public static final int NO_TEXTURE = -1;
    private static final float[] TEX_MATRIX = new float[16];

    public ImgTexFormat format;
    public int textureId;
    public final float[] texMatrix;

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
}
