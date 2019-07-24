package com.component.mediaengine.filter.imgtex;

import android.graphics.RectF;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import com.component.mediaengine.framework.ImgTexFormat;
import com.component.mediaengine.framework.ImgTexFrame;
import com.component.mediaengine.util.gles.GLProgramLoadException;
import com.component.mediaengine.util.gles.GLRender;
import com.component.mediaengine.util.gles.GlUtil;
import com.component.mediaengine.util.gles.TexTransformUtil;

import java.nio.FloatBuffer;

/**
 * Video frame mixer with openGLES.<br/>
 * <p>
 * Has maximum {@link #getSinkPinNum()} input pins, the lower sink pin index number
 * means the lower frame z order, that is, the higher sink pin index frame shown
 * in top of lower ones.<br/>
 * <p>
 * The mixer operation occurred while the frame with main index number arrived.
 */
public class ImgTexMixer extends ImgTexFilterBase {
    private static final String TAG = "ImgTexMixer";

    public static final int SCALING_MODE_FULL_FILL = 0;
    public static final int SCALING_MODE_BEST_FIT = 1;
    public static final int SCALING_MODE_CENTER_CROP = 2;
    public static final int SCALING_MODE_CROP = 3;

    private static final String FRAGMENT_SHADER_BODY =
            "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform  float alpha;\n" +
                    "void main() {\n" +
                    " vec4 tc = texture2D(sTexture, vTextureCoord);" +
                    " tc = tc * alpha;" +
                    " gl_FragColor = tc;\n" +
                    "}\n";

    private static final int MAX_INPUT_TEX = 8;

    protected String mVertexShader;
    protected String mFragmentShader;
    protected String mFragmentShaderOES;

    protected int mProgram;
    protected int mProgramOES;

    private int mOutWidth, mOutHeight;
    private ImgTexFormat[] mInputFormats;
    private RectF[] mRenderRects;
    private RectF[] mRenderRectsInternal;
    private RectF[] mCropRects;
    private float[] mRenderAlphas;
    private int[] mRenderScalingMode;
    private boolean[] mMirrors;
    private boolean[] mFlipVerticals;
    private ImgTexFormat mOutFormat;

    private FloatBuffer[] mTexCoordsBuf;
    private FloatBuffer[] mVertexCoordsBuf;

    public ImgTexMixer(GLRender glRender) {
        super(glRender);
        mVertexShader = GlUtil.BASE_VERTEX_SHADER;
        mFragmentShader = GlUtil.FRAGMENT_SHADER_HEADER + FRAGMENT_SHADER_BODY;
        mFragmentShaderOES = GlUtil.FRAGMENT_SHADER_OES_HEADER + FRAGMENT_SHADER_BODY;

        mInputFormats = new ImgTexFormat[getSinkPinNum()];
        mRenderRects = new RectF[getSinkPinNum()];
        mRenderRectsInternal = new RectF[getSinkPinNum()];
        mRenderAlphas = new float[getSinkPinNum()];
        mRenderScalingMode = new int[getSinkPinNum()];
        mMirrors = new boolean[getSinkPinNum()];
        mFlipVerticals = new boolean[getSinkPinNum()];
        mTexCoordsBuf = new FloatBuffer[getSinkPinNum()];
        mVertexCoordsBuf = new FloatBuffer[getSinkPinNum()];
        mCropRects = new RectF[getSinkPinNum()];
        for (int i = 0; i < getSinkPinNum(); i++) {
            mRenderRects[i] = new RectF(0.f, 0.f, 1.f, 1.f);
            mRenderRectsInternal[i] = new RectF(mRenderRects[i]);
            mCropRects[i] = new RectF(0.f, 0.f, 1.0f, 1.f);
            mRenderAlphas[i] = 1.0f;
            mRenderScalingMode[i] = SCALING_MODE_FULL_FILL;
            mTexCoordsBuf[i] = TexTransformUtil.getTexCoordsBuf();
            mVertexCoordsBuf[i] = TexTransformUtil.getVertexCoordsBuf();
        }
    }

    /**
     * Set mixer output frame resolution.
     *
     * @param outWidth  output width
     * @param outHeight output height
     */
    public void setTargetSize(int outWidth, int outHeight) {
        mOutWidth = outWidth;
        mOutHeight = outHeight;
        mOutFormat = new ImgTexFormat(ImgTexFormat.COLOR_RGBA, mOutWidth, mOutHeight);
        for (int i = 0; i < getSinkPinNum(); i++) {
            updateCoordsBuf(i);
        }
    }

    /**
     * Set render rect to specific input pin.
     *
     * @param idx   dedicated sink pin index
     * @param rect  render rect of input frame from this pin, should be in (0, 0, 1, 1)
     * @param alpha alpha value to use while mixing frame from this pin
     */
    public void setRenderRect(int idx, RectF rect, float alpha) {
        if (idx < getSinkPinNum()) {
            mRenderRects[idx].set(rect);
            mRenderRectsInternal[idx].set(rect);
            mRenderAlphas[idx] = alpha;
            updateCoordsBuf(idx);
        }
    }

    /**
     * Set render rect to specific input pin.
     *
     * @param idx   dedicated sink pin index
     * @param x     x position for left top of frame from this pin, should be 0~1
     * @param y     y position for left top of frame from this pin, should be 0~1
     * @param w     width for frame from this pin to show, should be 0~1
     * @param h     height for frame from this pin to show, should be 0~1
     * @param alpha alpha value to use while mixing frame from this pin
     */
    public void setRenderRect(int idx, float x, float y, float w, float h, float alpha) {
        if (idx < getSinkPinNum()) {
            mRenderRects[idx].set(x, y, x + w, y + h);
            mRenderRectsInternal[idx].set(mRenderRects[idx]);
            mRenderAlphas[idx] = alpha;
            updateCoordsBuf(idx);
        }
    }

    public RectF getRenderRect(int idx) {
        if (idx < getSinkPinNum()) {
            return mRenderRects[idx];
        }
        return null;
    }

    /**
     * the crop rect for
     * @see ImgTexMixer#SCALING_MODE_CROP
     * @param idx  dedicated sink pin index
     * @param rect crop rect for this pin, should be in (0, 0, 1, 1)
     */
    public void setRectForCrop(int idx, RectF rect) {
        if (idx < getSinkPinNum()) {
            mCropRects[idx].set(rect);
            updateCoordsBuf(idx);
        }
    }

    /**
     * the crop rect for
     * @see ImgTexMixer#SCALING_MODE_CROP
     * @param idx dedicated sink pin index
     * @param x  x position for left top of frame from this pin, should be 0~1
     * @param y  y position for left top of frame from this pin, should be 0~1
     * @param w  width for frame from this pin to show, should be 0~1
     * @param h  height for frame from this pin to show, should be 0~1
     */
    public void setRectForCrop(int idx, float x, float y, float w, float h) {
        if (idx < getSinkPinNum()) {
            mCropRects[idx].set(x, y, x + w, y + h);
            updateCoordsBuf(idx);
        }
    }

    public RectF getRectForCrop(int idx) {
        if (idx < getSinkPinNum()) {
            return mCropRects[idx];
        }
        return null;
    }

    /**
     * Set scaling mode for specific input pin.
     *
     * @param idx  dedicated sink pin index
     * @param mode scaling mode, see {@link #SCALING_MODE_FULL_FILL},
     *             {@link #SCALING_MODE_BEST_FIT},
     *             {@link #SCALING_MODE_CENTER_CROP}
     */
    public void setScalingMode(int idx, int mode) {
        if (idx < getSinkPinNum()) {
            mRenderScalingMode[idx] = mode;
            updateCoordsBuf(idx);
        }
    }

    /**
     * Enable or disable frame mirror in specified sink pin while rendering to the next module.
     *
     * @param idx    dedicated sink pin index
     * @param mirror true to enable mirror, false to disable
     */
    public void setMirror(int idx, boolean mirror) {
        if (idx < mMirrors.length) {
            mMirrors[idx] = mirror;
            updateCoordsBuf(idx);
        }
    }

    /**
     * Flip source image vertically in specified sink pin while rendering to the next module.
     *
     * @param idx           dedicated sink pin index
     * @param flipVertical  flip or not
     */
    public void setFlipVertical(int idx, boolean flipVertical) {
        if (idx < mFlipVerticals.length) {
            mFlipVerticals[idx] = flipVertical;
        }
    }

    /**
     * Get max input pin number of current mixer.
     *
     * @return the max input pin number.
     */
    @Override
    public int getSinkPinNum() {
        return MAX_INPUT_TEX;
    }

    @Override
    protected ImgTexFormat getSrcPinFormat() {
        if (mOutFormat == null) {
            Log.w(TAG, "you must call setTargetSize");
        }
        return mOutFormat;
    }

    @Override
    public void onFormatChanged(int inIdx, ImgTexFormat format) {
        mInputFormats[inIdx] = format;
        updateCoordsBuf(inIdx);
    }

    @Override
    public void onDraw(final ImgTexFrame[] frames) {
        // We assume the input textures with alpha were pre-multiplied
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        for (int i = 0; i < frames.length; i++) {
            if (frames[i] != null) {
                drawTexture(frames[i], mTexCoordsBuf[i], mVertexCoordsBuf[i], mRenderAlphas[i]);
            }
        }
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    protected void onRelease() {
        super.onRelease();
        if (mProgram != 0) {
            GLES20.glDeleteProgram(mProgram);
            mProgram = 0;
        }
        if (mProgramOES != 0) {
            GLES20.glDeleteProgram(mProgramOES);
            mProgramOES = 0;
        }
    }

    private void drawTexture(ImgTexFrame frame, FloatBuffer texCoordsBuf,
                             FloatBuffer vertexCoordsBuf, float alpha) {
        int program;
        int textrueTarget;
        int uTexMatrixLoc;
        int aPositionLoc;
        int aTextureCoordLoc;
        int aAlphaLoc;
        float[] texMatrix = frame.texMatrix;
        int texture = frame.textureId;

        if (texture == ImgTexFrame.NO_TEXTURE) {
            return;
        }

        if (!mInited) {
            mProgram = 0;
            mProgramOES = 0;
            mInited = true;
        }

        if (frame.format.colorFormat == ImgTexFormat.COLOR_EXTERNAL_OES) {
            textrueTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
            if (mProgramOES == 0) {
                mProgramOES = GlUtil.createProgram(mVertexShader, mFragmentShaderOES);
                if (mProgramOES == 0) {
                    Log.e(TAG, "Created program " + mProgramOES + " failed");
                    throw new RuntimeException("Unable to create program");
                }
            }
            program = mProgramOES;
        } else {
            textrueTarget = GLES20.GL_TEXTURE_2D;
            if (mProgram == 0) {
                mProgram = GlUtil.createProgram(mVertexShader, mFragmentShader);
                if (mProgram == 0) {
                    Log.e(TAG, "Created program " + mProgram + " failed");
                    throw new GLProgramLoadException("Unable to create program");
                }
            }
            program = mProgram;
        }

        aPositionLoc = GLES20.glGetAttribLocation(program, "aPosition");
        GlUtil.checkLocation(aPositionLoc, "aPosition");
        aTextureCoordLoc = GLES20.glGetAttribLocation(program, "aTextureCoord");
        GlUtil.checkLocation(aTextureCoordLoc, "aTextureCoord");
        uTexMatrixLoc = GLES20.glGetUniformLocation(program, "uTexMatrix");
        GlUtil.checkLocation(uTexMatrixLoc, "uTexMatrix");

        GlUtil.checkGlError("draw start");
        // Select the program.
        GLES20.glUseProgram(program);
        GlUtil.checkGlError("glUseProgram");

        // Set the texture.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(textrueTarget, texture);

        // Copy the texture transformation matrix over.
        GLES20.glUniformMatrix4fv(uTexMatrixLoc, 1, false, texMatrix, 0);
        GlUtil.checkGlError("glUniformMatrix4fv");

        // Enable the "aPosition" vertex attribute.
        GLES20.glEnableVertexAttribArray(aPositionLoc);
        GlUtil.checkGlError("glEnableVertexAttribArray");

        GLES20.glVertexAttribPointer(aPositionLoc, TexTransformUtil.COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, TexTransformUtil.COORDS_STRIDE, vertexCoordsBuf);
        GlUtil.checkGlError("glVertexAttribPointer");

        // Enable the "aTextureCoord" vertex attribute.
        GLES20.glEnableVertexAttribArray(aTextureCoordLoc);
        GlUtil.checkGlError("glEnableVertexAttribArray");

        // Connect texBuffer to "aTextureCoord".
        GLES20.glVertexAttribPointer(aTextureCoordLoc, TexTransformUtil.COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, TexTransformUtil.COORDS_STRIDE, texCoordsBuf);
        GlUtil.checkGlError("glVertexAttribPointer");

        aAlphaLoc = GLES20.glGetUniformLocation(program, "alpha");
        GlUtil.checkLocation(aAlphaLoc, "alpha");
        GLES20.glUniform1f(aAlphaLoc, alpha);

        // Draw the rect.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, TexTransformUtil.COORDS_COUNT);
        GlUtil.checkGlError("glDrawArrays");

        // Done -- disable vertex array, texture, and program.
        GLES20.glDisableVertexAttribArray(aPositionLoc);
        GLES20.glDisableVertexAttribArray(aTextureCoordLoc);

        GLES20.glBindTexture(textrueTarget, 0);
        GLES20.glUseProgram(0);
    }

    private void updateCoordsBuf(int idx) {
        if (mOutWidth == 0 || mOutHeight == 0) {
            return;
        }

        ImgTexFormat format = mInputFormats[idx];
        if (format != null && format.width > 0 && format.height > 0) {
            if (mRenderRects[idx].width() == 0) {
                float w = mRenderRects[idx].height() * format.width / format.height;
                w = w * mOutHeight / mOutWidth;
                mRenderRectsInternal[idx].right = mRenderRects[idx].right + w;
            } else if (mRenderRects[idx].height() == 0) {
                float h = mRenderRects[idx].width() * format.height / format.width;
                h = h * mOutWidth / mOutHeight;
                mRenderRectsInternal[idx].bottom = mRenderRects[idx].bottom + h;
            }
        }

        RectF renderRect = mRenderRectsInternal[idx];
        if (format == null || format.width == 0 || format.height == 0 ||
                renderRect == null || renderRect.width() == 0 || renderRect.height() == 0) {
            return;
        }

        float sar = (float) format.width / (float) format.height;
        float dar = (mOutWidth * renderRect.width()) / (mOutHeight * renderRect.height());

        // Vertex coords
        float cropX, cropY;
        RectF rectF = renderRect;
        if (mRenderScalingMode[idx] == SCALING_MODE_BEST_FIT) {
            if (sar > dar) {
                cropX = 0;
                cropY = (1.0f - dar / sar) / 2;
            } else {
                cropY = 0;
                cropX = (1.0f - sar / dar) / 2;
            }
            Log.d(TAG, "sar=" + sar + " dar=" + dar + " cropX=" + cropX + " cropY=" + cropY);
            rectF = new RectF(renderRect.left + cropX, renderRect.top + cropY,
                    renderRect.right - cropX, renderRect.bottom - cropY);
            Log.d(TAG, "rectF=" + rectF);
        }
        mVertexCoordsBuf[idx] = genVertexCoordsBuf(rectF);

        // Tex coords
        float crop = 0.f;
        float left = 0.f;
        float right = 0.f;
        float top = 0.f;
        float bottom = 0.f;
        if (mRenderScalingMode[idx] == SCALING_MODE_CENTER_CROP) {
            if (sar > dar) {
                crop = 1.0f - dar / sar;
                left = right = crop / 2.0f;
            } else {
                crop = 1.0f - sar / dar;
                top = bottom = crop / 2.0f;
            }
        } else if (mRenderScalingMode[idx] == SCALING_MODE_CROP) {
            if (sar > dar) {
                crop = 1.0f - dar / sar;
                left = mCropRects[idx].left;
                right = crop - left;
            } else {
                crop = 1.0f - sar / dar;
                top = mCropRects[idx].top;
                bottom = crop - top;
            }
        }

        mTexCoordsBuf[idx] = TexTransformUtil.getTexCoordsBuf(left, top, right,
                bottom, 0, mMirrors[idx], mFlipVerticals[idx]);
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
