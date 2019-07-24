package com.component.mediaengine.filter.imgtex;

import android.graphics.RectF;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import com.component.mediaengine.framework.ImgTexFormat;
import com.component.mediaengine.framework.ImgTexFrame;
import com.component.mediaengine.framework.SinkPin;
import com.component.mediaengine.util.CredtpWrapper;
import com.component.mediaengine.util.gles.GLProgramLoadException;
import com.component.mediaengine.util.gles.GLRender;
import com.component.mediaengine.util.gles.GlUtil;
import com.component.mediaengine.util.gles.TexTransformUtil;

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
    protected RectF mEffectRect;
    private FloatBuffer mTexCoordsBuf;
    private FloatBuffer mVertexCoordsBuf;

    protected boolean mMirror;
    protected boolean mFlipVertical;

    public ImgTexFilter(GLRender glRender) {
        super(glRender);
        init(BASE_VERTEX_SHADER, BASE_FRAGMENT_SHADER_BODY);
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
        mEffectRect = new RectF(0.f, 0.f, 1.f, 1.f);
        mTexCoordsBuf = genTexCoords(mEffectRect);
        mVertexCoordsBuf = genVertexCoordsBuf(mEffectRect);
    }

    /**
     * Set effect rect to main input pin.
     *
     * @param x x position for left top of frame from main pin, should be 0~1
     * @param y y position for left top of frame from main pin, should be 0~1
     * @param w width for frame from main pin to show, should be 0~1
     * @param h height for frame from main pin to show, should be 0~1
     */
    public void setEffectRect(float x, float y, float w, float h) {
        mEffectRect.set(x, y, x + w, y + h);
        mTexCoordsBuf = genTexCoords(mEffectRect);
        mVertexCoordsBuf = genVertexCoordsBuf(mEffectRect);
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
     * Flip source image vertically while rendering to the next module.
     *
     * @param flipVertical  flip or not
     */
    public void setFlipVertical(boolean flipVertical) {
        mFlipVertical = flipVertical;
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
            int textureTarget;
            if (format.colorFormat == ImgTexFormat.COLOR_EXTERNAL_OES) {
                textureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
                mFragmentShader = GlUtil.FRAGMENT_SHADER_OES_HEADER + mFragmentShaderBody;
            } else {
                Log.e(TAG, "create texture 2d");
                textureTarget = GLES20.GL_TEXTURE_2D;
                mFragmentShader = GlUtil.FRAGMENT_SHADER_HEADER + mFragmentShaderBody;
            }

            // check texture format, delete current program if needed
            if (mInited && textureTarget != mTextureTarget) {
                if (mProgramId != 0) {
                    // onFormatChanged always running in GL thread now
                    GLES20.glDeleteProgram(mProgramId);
                    mProgramId = 0;
                }
                mInited = false;
            }
            mTextureTarget = textureTarget;

            mOutFormat = new ImgTexFormat(ImgTexFormat.COLOR_RGBA,
                    format.width, format.height);
            onFormatChanged(format);
        }
    }

    @Override
    public void onDraw(final ImgTexFrame[] frames) {
        int textureId = frames[mMainSinkPinIndex].textureId;
        float[] texMatrix = frames[mMainSinkPinIndex].texMatrix;

        if (textureId == ImgTexFrame.NO_TEXTURE) {
            return;
        }

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
        super.onRelease();
        if (mProgramId != 0) {
            GLES20.glDeleteProgram(mProgramId);
            mProgramId = 0;
        }
    }

    protected FloatBuffer getTexCoords() {
        if (mTexCoordsBuf == null) {
            genTexCoords(mEffectRect);
        }
        return mTexCoordsBuf;
    }

    private FloatBuffer genTexCoords(RectF rect) {
        // Tex coords
        float left = rect.left;
        float right = 1.0f - rect.right;
        float top = rect.top;
        float bottom = 1.0f - rect.bottom;

        return TexTransformUtil.getTexCoordsBuf(left, top, right,
                bottom, 0, mMirror, mFlipVertical);
    }

    protected int getUniformLocation(String uniformName) {
        int loc = GLES20.glGetUniformLocation(mProgramId, uniformName);
        GlUtil.checkLocation(loc, uniformName);
        return loc;
    }

    protected FloatBuffer getVertexCoords() {
        if (mVertexCoordsBuf == null) {
            mVertexCoordsBuf = genVertexCoordsBuf(mEffectRect);
        }
        return mVertexCoordsBuf;
    }

    private FloatBuffer genVertexCoordsBuf(RectF rect) {
        float vertexArray[] = {
                -1 + 2 * rect.left,  1 - 2 * rect.bottom,   // 0 bottom left
                -1 + 2 * rect.right, 1 - 2 * rect.bottom,   // 1 bottom right
                -1 + 2 * rect.left,  1 - 2 * rect.top,      // 2 top left
                -1 + 2 * rect.right, 1 - 2 * rect.top,      // 3 top right
        };
        return GlUtil.createFloatBuffer(vertexArray);
    }
}
