package com.zq.mediaengine.filter.imgtex;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import com.zq.mediaengine.framework.ImgTexFormat;
import com.zq.mediaengine.framework.ImgTexFrame;
import com.zq.mediaengine.framework.SinkPin;
import com.zq.mediaengine.util.CredtpWrapper;
import com.zq.mediaengine.util.gles.GLProgramLoadException;
import com.zq.mediaengine.util.gles.GLRender;
import com.zq.mediaengine.util.gles.GlUtil;
import com.zq.mediaengine.util.gles.TexTransformUtil;

import java.nio.FloatBuffer;

/**
 * Base class of filters implement by openGLES with default shader.
 */
public class ImgTexFilter extends ImgTexFilterBase {
    private static final String TAG = "ImgTexFilter";
    public static final String BASE_VERTEX_SHADER = GlUtil.BASE_VERTEX_SHADER;
    public static final String BASE_FRAGMENT_SHADER_BODY = GlUtil.BASE_FRAGMENT_SHADER_BODY;

    protected String mVertexShader;
    protected String mFragmentShaderBody;
    protected String mFragmentShader;
    protected int mTextureTarget = GLES20.GL_TEXTURE_2D;
    private ImgTexFormat mOutFormat;

    protected int mProgramId;
    protected int muTexMatrixLoc;
    protected int maPositionLoc;
    protected int maTextureCoordLoc;

    protected boolean mMirror;

    public ImgTexFilter(GLRender glRender) {
        super(glRender);
        init(BASE_VERTEX_SHADER, GlUtil.BASE_FRAGMENT_SHADER_BODY);
    }

    protected ImgTexFilter(GLRender glRender,
                           final String vertexShader,
                           final int fragmentShaderType) {
        super(glRender);
        String fragmentShader = CredtpWrapper.getInstance().getCredtpByType(fragmentShaderType);
        init(vertexShader, fragmentShader);
    }

    public ImgTexFilter(GLRender glRender, String vertexShader, String fragmentShader) {
        super(glRender);
        init(vertexShader, fragmentShader);
    }

    protected void init(String vertexShader, String fragmentShader) {
        mVertexShader = vertexShader;
        mFragmentShaderBody = fragmentShader;
    }

    /**
     * Get the SinkPin of this 1x1 filter.
     *
     * @return The input port.
     */
    @Override
    public SinkPin<ImgTexFrame> getSinkPin() {
        return getSinkPin(0);
    }

    /**
     * Mirror the image in SrcPin.
     *
     * @param mirror mirror the SrcPin image or not
     */
    public void setMirror(boolean mirror) {
        mMirror = mirror;
    }

    /**
     * On input pin format changed
     *
     * @param format the input pin format
     */
    protected void onFormatChanged(final ImgTexFormat format) {
    }

    /**
     * Called after the program loaded, developers can set customized parameters
     * in openGL program here.
     */
    protected void onInitialized() {
    }

    /**
     * Called right before GLES20.glDrawArrays called.<br/>
     * Developers could bind extra textures here if needed.
     */
    protected void onDrawArraysPre() {
    }

    /**
     * Called right after GLES20.glDrawArrays called.<br/>
     * Developers could unbind extra textures here if needed.
     */
    protected void onDrawArraysAfter() {
    }

    @Override
    public int getSinkPinNum() {
        return 1;
    }

    @Override
    protected ImgTexFormat getSrcPinFormat() {
        return mOutFormat;
    }

    @Override
    public void onFormatChanged(final int inIdx, final ImgTexFormat format) {
        if (inIdx == mMainSinkPinIndex) {
            if (format.colorFormat == ImgTexFormat.COLOR_EXTERNAL_OES) {
                mTextureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
                mFragmentShader = GlUtil.FRAGMENT_SHADER_OES_HEADER + mFragmentShaderBody;
            } else {
                mTextureTarget = GLES20.GL_TEXTURE_2D;
                mFragmentShader = GlUtil.FRAGMENT_SHADER_HEADER + mFragmentShaderBody;
            }

            mOutFormat = new ImgTexFormat(ImgTexFormat.COLOR_RGBA,
                    format.width, format.height);
            onFormatChanged(format);
        }
    }

    @Override
    public void onDraw(final ImgTexFrame[] frames) {
        int textureId = frames[mMainSinkPinIndex].textureId;
        float[] texMatrix = frames[mMainSinkPinIndex].texMatrix;

        GlUtil.checkGlError("draw start");
        if (!mInited) {
            mProgramId = GlUtil.createProgram(mVertexShader, mFragmentShader);
            if (mProgramId == 0) {
                Log.e(TAG, "Created program " + mProgramId + " failed");
                throw new GLProgramLoadException("Unable to create program");
            }
            maPositionLoc = GLES20.glGetAttribLocation(mProgramId, "aPosition");
            GlUtil.checkLocation(maPositionLoc, "aPosition");
            maTextureCoordLoc = GLES20.glGetAttribLocation(mProgramId, "aTextureCoord");
            GlUtil.checkLocation(maTextureCoordLoc, "aTextureCoord");
            muTexMatrixLoc = GLES20.glGetUniformLocation(mProgramId, "uTexMatrix");
            GlUtil.checkLocation(muTexMatrixLoc, "uTexMatrix");

            // Select the program.
            GLES20.glUseProgram(mProgramId);
            GlUtil.checkGlError("glUseProgram");

            onInitialized();
            GlUtil.checkGlError("onInitialized " + this);
            mInited = true;
        } else {
            // Select the program.
            GLES20.glUseProgram(mProgramId);
            GlUtil.checkGlError("glUseProgram");
        }

        // Set the texture.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(mTextureTarget, textureId);
        GlUtil.checkGlError("glBindTexture");

        // Copy the texture transformation matrix over.
        GLES20.glUniformMatrix4fv(muTexMatrixLoc, 1, false, texMatrix, 0);
        GlUtil.checkGlError("glUniformMatrix4fv");

        // Enable the "aPosition" vertex attribute.
        GLES20.glEnableVertexAttribArray(maPositionLoc);
        GlUtil.checkGlError("glEnableVertexAttribArray");

        GLES20.glVertexAttribPointer(maPositionLoc, TexTransformUtil.COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, TexTransformUtil.COORDS_STRIDE, getVertexCoords());
        GlUtil.checkGlError("glVertexAttribPointer");

        // Enable the "aTextureCoord" vertex attribute.
        GLES20.glEnableVertexAttribArray(maTextureCoordLoc);
        GlUtil.checkGlError("glEnableVertexAttribArray");

        // Connect texBuffer to "aTextureCoord".
        GLES20.glVertexAttribPointer(maTextureCoordLoc, TexTransformUtil.COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, TexTransformUtil.COORDS_STRIDE, getTexCoords());
        GlUtil.checkGlError("glVertexAttribPointer");

        onDrawArraysPre();

        // Draw the rect.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, TexTransformUtil.COORDS_COUNT);
        GlUtil.checkGlError("glDrawArrays");

        // Done -- disable vertex array, texture, and program.
        GLES20.glDisableVertexAttribArray(maPositionLoc);
        GLES20.glDisableVertexAttribArray(maTextureCoordLoc);

        onDrawArraysAfter();

        GLES20.glBindTexture(mTextureTarget, 0);
        GLES20.glUseProgram(0);
    }

    @Override
    protected void onRelease() {
        if (mProgramId != 0) {
            GLES20.glDeleteProgram(mProgramId);
            mProgramId = 0;
        }
    }

    protected FloatBuffer getTexCoords() {
        return TexTransformUtil.getTexCoordsBuf();
    }

    protected int getUniformLocation(String uniformName) {
        int loc = GLES20.glGetUniformLocation(mProgramId, uniformName);
        GlUtil.checkLocation(loc, uniformName);
        return loc;
    }

    private FloatBuffer getVertexCoords() {
        if (mMirror) {
            return TexTransformUtil.getVertexMirrorCoordsBuf();
        } else {
            return TexTransformUtil.getVertexCoordsBuf();
        }
    }
}
