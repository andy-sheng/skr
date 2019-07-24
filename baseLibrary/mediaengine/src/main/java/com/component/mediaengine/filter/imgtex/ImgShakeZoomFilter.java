package com.component.mediaengine.filter.imgtex;

import com.component.mediaengine.framework.CredtpModel;
import com.component.mediaengine.util.gles.GLRender;

/**
 * zoom filter
 */

public class ImgShakeZoomFilter extends ImgEffectFilterBase {
    private static final String OFFSTEP = "fstep";
    private float[] mGradientValue = new float[1];

    public ImgShakeZoomFilter(GLRender glRender) {
        super(glRender);
        setGradientName(OFFSTEP);
        setFragment(CredtpModel.BEAUTY_SHAKE_ZOOM);
        setMAXGradientFactorValue(0.1f);  //设置渐变因子的最大值，必须在设置framescount之前进行设置
        setGradientFactorFrameCount(10);
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
}