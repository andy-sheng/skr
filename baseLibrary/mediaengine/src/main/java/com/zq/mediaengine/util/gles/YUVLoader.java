package com.zq.mediaengine.util.gles;

import android.opengl.GLES20;
import android.util.Log;

import com.zq.mediaengine.framework.ImgTexFrame;

import java.nio.ByteBuffer;

/**
 * Load I420 data to texture.
 */

public class YUVLoader {
    private static final String TAG = "YUVLoader";

    // Simple vertex shader, without transform.
    public static final String YUV420P_VS = "" +
            "precision mediump float;\n" +
            "attribute vec4 aPosition;\n" +
            "attribute vec4 aTextureCoord;\n" +
            "varying vec2 vTextureCoord;\n" +
            "void main() {\n" +
            "    gl_Position = aPosition;\n" +
            "    vTextureCoord = aTextureCoord.xy;\n" +
            "}\n";

    private static final String YUV420P_FS = "" +
            "precision mediump float;\n" +
            "varying vec2 vTextureCoord;\n" +
            "uniform sampler2D sTextureY;\n" +
            "uniform sampler2D sTextureU;\n" +
            "uniform sampler2D sTextureV;\n" +
            "void main() {\n" +
            "    mediump vec3 yuv;\n" +
            "    lowp vec3 rgb;\n" +
            "    yuv.x = texture2D(sTextureY, vTextureCoord).r;\n" +
            "    yuv.y = texture2D(sTextureU, vTextureCoord).r - 0.5;\n" +
            "    yuv.z = texture2D(sTextureV, vTextureCoord).r - 0.5;\n" +
            "    rgb = mat3( 1,       1,        1,\n" +
            "                0,       -0.39465, 2.03211,\n" +
            "                1.13983, -0.58060, 0 ) * yuv;\n" +
            "    gl_FragColor = vec4(rgb, 1);\n" +
            "}";

    private GLRender mGLRender;
    private int mProgramId = 0;
    private int mOutTexture = ImgTexFrame.NO_TEXTURE;
    private int mWidth;
    private int mHeight;
    private int[] mYUVTextures;
    private int[] mViewPort = new int[4];

    public YUVLoader(GLRender glRender) {
        mGLRender = glRender;
    }

    public void reset() {
        if (mYUVTextures != null) {
            GLES20.glDeleteTextures(3, mYUVTextures, 0);
            mYUVTextures = null;
        }
        if (mOutTexture != ImgTexFrame.NO_TEXTURE) {
            mGLRender.getFboManager().unlock(mOutTexture);
            mOutTexture = ImgTexFrame.NO_TEXTURE;
        }
        if (mProgramId != 0) {
            GLES20.glDeleteProgram(mProgramId);
            GLES20.glGetError();
            mProgramId = 0;
        }
    }

    public void onFboCacheCleared() {
        mOutTexture = ImgTexFrame.NO_TEXTURE;
    }

    public int loadTexture(ByteBuffer buf, int width, int height, int strides[]) {
        if (buf == null || buf.limit() == 0 || width <= 0 || height <= 0) {
            return ImgTexFrame.NO_TEXTURE;
        }

        if (width != mWidth || height != mHeight) {
            reset();
        }
        mWidth = width;
        mHeight = height;

        if (mOutTexture == ImgTexFrame.NO_TEXTURE) {
            mOutTexture = mGLRender.getFboManager().getTextureAndLock(width, height);
        }
        int outFrameBuffer = mGLRender.getFboManager().getFramebuffer(mOutTexture);

        GLES20.glGetIntegerv(GLES20.GL_VIEWPORT, mViewPort, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, outFrameBuffer);
        GLES20.glViewport(0, 0, width, height);

        if (mProgramId == 0) {
            mProgramId = GlUtil.createProgram(YUV420P_VS, YUV420P_FS);
            if (mProgramId == 0) {
                Log.e(TAG, "Created program " + mProgramId + " failed");
                return ImgTexFrame.NO_TEXTURE;
            }
        }

        uploadTextures(buf, width, height, strides);
        GlUtil.checkGlError("upload textures");

        int aPositionLoc = GLES20.glGetAttribLocation(mProgramId, "aPosition");
        GlUtil.checkLocation(aPositionLoc, "aPosition");
        int aTextureCoordLoc = GLES20.glGetAttribLocation(mProgramId, "aTextureCoord");
        GlUtil.checkLocation(aTextureCoordLoc, "aTextureCoord");

        GlUtil.checkGlError("draw start");
        // Select the program.
        GLES20.glUseProgram(mProgramId);
        GlUtil.checkGlError("glUseProgram");

        GLES20.glUniform1i(GLES20.glGetUniformLocation(mProgramId, "sTextureY"), 0);
        GLES20.glUniform1i(GLES20.glGetUniformLocation(mProgramId, "sTextureU"), 1);
        GLES20.glUniform1i(GLES20.glGetUniformLocation(mProgramId, "sTextureV"), 2);
        GlUtil.checkGlError("glUniform1i for textures");

        // Enable the "aPosition" vertex attribute.
        GLES20.glEnableVertexAttribArray(aPositionLoc);
        GlUtil.checkGlError("glEnableVertexAttribArray");

        GLES20.glVertexAttribPointer(aPositionLoc, TexTransformUtil.COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, TexTransformUtil.COORDS_STRIDE,
                TexTransformUtil.getVertexCoordsBuf());
        GlUtil.checkGlError("glVertexAttribPointer");

        // Enable the "aTextureCoord" vertex attribute.
        GLES20.glEnableVertexAttribArray(aTextureCoordLoc);
        GlUtil.checkGlError("glEnableVertexAttribArray");

        // Connect texBuffer to "aTextureCoord".
        GLES20.glVertexAttribPointer(aTextureCoordLoc, TexTransformUtil.COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, TexTransformUtil.COORDS_STRIDE,
                TexTransformUtil.getTexCoordsBuf());
        GlUtil.checkGlError("glVertexAttribPointer");

        // Draw the rect.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, TexTransformUtil.COORDS_COUNT);
        GlUtil.checkGlError("glDrawArrays");

        // Done -- disable vertex array, texture, and program.
        GLES20.glDisableVertexAttribArray(aPositionLoc);
        GLES20.glDisableVertexAttribArray(aTextureCoordLoc);
        GLES20.glUseProgram(0);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glViewport(mViewPort[0], mViewPort[1], mViewPort[2], mViewPort[3]);

        return mOutTexture;
    }

    private void uploadTextures(ByteBuffer buf, int width, int height, int strides[]) {
        if (strides == null || strides.length != 3) {
            strides = new int[]{ width, width / 2, width /2 };
        }
        int[] widths = { width, width / 2, width / 2 };
        int[] heights = { height, height / 2, height / 2 };
        ByteBuffer[] buffers = new ByteBuffer[3];
        int pos = 0;
        for (int i = 0; i < 3; i++) {
            buffers[i] = buf.duplicate();
            buffers[i].position(pos);
            pos += strides[i] * heights[i];
            buffers[i].limit(pos);
        }

        if (mYUVTextures == null) {
            mYUVTextures = new int[3];
            GLES20.glGenTextures(3, mYUVTextures, 0);
            for (int i = 0; i < 3; i++) {
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + i);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mYUVTextures[i]);
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                        GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                        GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                        GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                        GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
                int align = calAlignment(strides[i] - widths[i]);
                GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, align);
                GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE,
                        widths[i], heights[i], 0, GLES20.GL_LUMINANCE,
                        GLES20.GL_UNSIGNED_BYTE, buffers[i]);
            }
        } else {
            for (int i = 0; i < 3; i++) {
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + i);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mYUVTextures[i]);
                int align = calAlignment(strides[i] - widths[i]);
                GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, align);
                GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, widths[i], heights[i],
                        GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, buffers[i]);
            }
        }
    }

    private int calAlignment(int diff) {
        int val = 1;
        while (val <= diff) {
            val *= 2;
        }
        return val;
    }
}
