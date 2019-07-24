package com.component.mediaengine.filter.imgtex;

import android.opengl.GLES20;

import com.component.mediaengine.framework.CredtpModel;
import com.component.mediaengine.framework.ImgTexFormat;
import com.component.mediaengine.util.gles.GLRender;
import com.component.mediaengine.util.gles.GlUtil;

import java.nio.FloatBuffer;

/**
 * Beauty illusion filter.
 */

public class ImgBeautyIllusionFilter extends ImgTexFilter {
    private int mParamsLoc;
    private final Object mParamsLock = new Object();
    private float mParams[];
    private ImgTexFormat mInputFormat;

    public ImgBeautyIllusionFilter(GLRender glRender) {
        super(glRender, GlUtil.BASE_VERTEX_SHADER, CredtpModel.BEAUTY_ILLUSION_FILTER);
    }

    @Override
    public boolean isGrindRatioSupported() {
        return true;
    }

    @Override
    public void setGrindRatio(float ratio) {
        super.setGrindRatio(ratio);
        synchronized (mParamsLock) {
            if (ratio < 0.2f) {
                mParams = new float[]{1.0f, 1.0f, 0.15f, 0.15f};
            } else if (ratio < 0.4f) {
                mParams = new float[]{0.8f, 0.9f, 0.2f, 0.2f};
            } else if (ratio < 0.6f) {
                mParams = new float[]{0.6f, 0.8f, 0.25f, 0.25f};
            } else if (ratio < 0.8f) {
                mParams = new float[]{0.4f, 0.7f, 0.38f, 0.3f};
            } else {
                mParams = new float[]{0.33f, 0.63f, 0.4f, 0.35f};
            }
        }
    }

    @Override
    public void onFormatChanged(ImgTexFormat format) {
        mInputFormat = format;
    }

    @Override
    protected void onInitialized() {
        int singleStepOffsetLoc = getUniformLocation("singleStepOffset");
        mParamsLoc = getUniformLocation("params");
        setGrindRatio(mGrindRatio);

        float[] texSize = new float[]{2.0f / mInputFormat.width, 2.0f / mInputFormat.height};
        GLES20.glUniform2fv(singleStepOffsetLoc, 1, FloatBuffer.wrap(texSize));
        GlUtil.checkGlError("glUniform2fv");
    }

    @Override
    protected void onDrawArraysPre() {
        synchronized (mParamsLock) {
            GLES20.glUniform4fv(mParamsLoc, 1, mParams, 0);
        }
    }
}
