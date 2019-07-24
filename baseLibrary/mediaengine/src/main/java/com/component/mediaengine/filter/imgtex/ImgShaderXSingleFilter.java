package com.component.mediaengine.filter.imgtex;

import android.opengl.GLES20;

import com.component.mediaengine.framework.CredtpModel;
import com.component.mediaengine.framework.ImgTexFormat;
import com.component.mediaengine.util.gles.GLRender;

/**
 * x single filter
 */

public class ImgShaderXSingleFilter extends ImgEffectFilterBase {
    private static final String OFFSTEP = "fstep";
    private static final String ASPECTRATIO = "aspectRatio";
    private int mAspectRatioLoc = -1;
    private float[] mGradientValue = new float[1];
    private float mAspectRatio;

    public ImgShaderXSingleFilter(GLRender glRender) {
        super(glRender);
        setGradientName(OFFSTEP);
        setFragment(CredtpModel.BEAUTY_SHAKE_XSINGLE);
        setMAXGradientFactorValue(10);  //设置渐变因子的最大值，必须在设置framescount之前进行设置
        setGradientFactorFrameCount(1000);
        setEffectAuto(true);
    }

    @Override
    protected int getVSinkPinNum() {
        return 0;
    }

    @Override
    protected float[] getGradientValue() {
        mGradientValue[0] = getGradientFactorValue();
        return mGradientValue;
    }

    @Override
    protected void onFormatChanged(final ImgTexFormat format) {
        if (getSrcPinFormat().width > getSrcPinFormat().height) {
            mAspectRatio = getSrcPinFormat().width / getSrcPinFormat().height;
        } else {
            mAspectRatio = getSrcPinFormat().height / getSrcPinFormat().width;
        }
    }

    @Override
    public void onInitialized() {
        super.onInitialized();
        try {
            mAspectRatioLoc = getUniformLocation(ASPECTRATIO);
        } catch (RuntimeException e) {

        }
    }

    @Override
    public void onDrawArraysPre() {
        if (mAspectRatioLoc >= 0) {
            GLES20.glUniform1f(mAspectRatioLoc, mAspectRatio);
        }
        super.onDrawArraysPre();
    }
}