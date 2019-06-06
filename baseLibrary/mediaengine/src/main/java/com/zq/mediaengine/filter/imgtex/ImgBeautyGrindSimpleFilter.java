package com.zq.mediaengine.filter.imgtex;

import android.opengl.GLES20;

import com.zq.mediaengine.framework.CredtpModel;
import com.zq.mediaengine.framework.ImgTexFormat;
import com.zq.mediaengine.util.gles.GLRender;


/**
 * Grind simple filter.
 *
 * @hide
 */

public class ImgBeautyGrindSimpleFilter extends ImgTexFilter {
    private int mSingleStepOffsetLoc;
    private int mParamsLoc;
    private int mScaleLumanceLoc;
    private final Object mParamsLock = new Object();
    private float mParams[];
    private float mScaleLumance = 1.0f;

    private ImgTexFormat mInputFormat;

    public ImgBeautyGrindSimpleFilter(GLRender glRender) {
        super(glRender, BASE_VERTEX_SHADER, CredtpModel.BEAUTY_GRIND_SIMPLE_FILTER);
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

    public void setScaleLumance(float scaleLumance) {
        mScaleLumance = scaleLumance;
    }

    @Override
    protected void onFormatChanged(ImgTexFormat format) {
        super.onFormatChanged(format);
        mInputFormat = format;
    }

    @Override
    protected void onInitialized() {
        mSingleStepOffsetLoc = getUniformLocation("singleStepOffset");
        mParamsLoc = getUniformLocation("params");
        mScaleLumanceLoc = getUniformLocation("scaleLumance");
        GLES20.glUniform1f(mScaleLumanceLoc, mScaleLumance);

        GLES20.glUniform2f(mSingleStepOffsetLoc,
                (2.0f / mInputFormat.width),
                (2.0f / mInputFormat.height));
        setGrindRatio(mGrindRatio);
    }

    @Override
    protected void onDrawArraysPre() {
        synchronized (mParamsLock) {
            GLES20.glUniform4fv(mParamsLoc, 1, mParams, 0);
        }
    }
}
