package com.component.mediaengine.filter.imgtex;

import com.component.mediaengine.framework.CredtpModel;
import com.component.mediaengine.framework.ImgTexFormat;
import com.component.mediaengine.util.gles.GLRender;

/**
 * shake illusion filter
 */

public class ImgShakeIllusionFilter extends ImgEffectFilterBase {
    private static final String TEMPERATURE = "temperature";
    private static final float TEMPERATURE_DEFAULT_VALUE = 3500.0f;
    private float[] mGradientValue = new float[1];
    private float mTemperature;
    private final float mFStep = 5.0f;

    public ImgShakeIllusionFilter(GLRender glRender) {
        super(glRender);
        setGradientName(TEMPERATURE);
        setFragment(CredtpModel.BEAUTY_SHAKE_ILLUSION);
        setEffectAuto(false);
        mTemperature = TEMPERATURE_DEFAULT_VALUE;
        //该滤镜一次渲染后的输出作为下一帧渲染的输入
        getSrcPin().connect(getVSinkPin(0));
    }

    @Override
    protected int getVSinkPinNum() {
        return 1;
    }

    @Override
    protected void onDisconnect(boolean recursive) {
        super.onDisconnect(recursive);
        getSrcPin().disconnect(getVSinkPin(0), false);
        mTemperature = TEMPERATURE_DEFAULT_VALUE;
    }

    @Override
    protected void onFormatChanged(final ImgTexFormat format) {
        //该滤镜一次渲染后的输出作为下一帧渲染的输入
        getSrcPin().connect(getVSinkPin(0));
        mTemperature = TEMPERATURE_DEFAULT_VALUE;
    }

    @Override
    protected float[] getGradientValue() {
        mTemperature = mTemperature + mFStep;
        if (mTemperature > 7500.0f) {
            mTemperature = 3500.0f;
        }

        float temperature = (mTemperature < 5000) ? (0.0004f * (mTemperature - 5000.0f)) :
                (0.00006f * (mTemperature - 5000.0f));

        mGradientValue[0] = temperature;
        return mGradientValue;
    }
}
