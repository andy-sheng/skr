package com.zq.mediaengine.filter.imgtex;

import android.content.Context;
import android.util.Log;

import com.zq.mediaengine.framework.ImgTexFrame;
import com.zq.mediaengine.framework.PinAdapter;
import com.zq.mediaengine.framework.SinkPin;
import com.zq.mediaengine.framework.SrcPin;
import com.zq.mediaengine.util.gles.GLRender;

/**
 * Beauty face filter.
 */

public class ImgBeautySmoothFilter extends ImgFilterBase {
    private static final String TAG = "ImgBeautyFaceFilter";

    private PinAdapter<ImgTexFrame> mSinkAdapter;
    private PinAdapter<ImgTexFrame> mSrcAdapter;
    private ImgBeautySkinDetectFilter mSkinDetectFilter;
    private ImgBeautyGrindFaceFilter mGrindFaceFilter;
    private ImgBeautySpecialEffectsFilter mSpecialEffectsFilter;

    public ImgBeautySmoothFilter(GLRender glRender) {
        init(glRender, null);
    }

    public ImgBeautySmoothFilter(GLRender glRender, Context context) {
        init(glRender, context);
    }

    private void init(GLRender glRender, Context context) {
        mSinkAdapter = new PinAdapter<>();
        mSrcAdapter = new PinAdapter<>();
        mSkinDetectFilter = new ImgBeautySkinDetectFilter(glRender);
        mGrindFaceFilter = new ImgBeautyGrindFaceFilter(glRender);

        try {
            mSpecialEffectsFilter = new ImgBeautySpecialEffectsFilter(glRender, context,
                    ImgBeautySpecialEffectsFilter.IMG_SPECIAL_EFFECT_SWEETY);
        } catch (Exception e) {
            Log.e(TAG, "Resource missing, ruddy is unusable!");
        }

        mSinkAdapter.mSrcPin.connect(mSkinDetectFilter.getSinkPin());
        mSinkAdapter.mSrcPin.connect(mGrindFaceFilter.getSinkPin(0));
        mSkinDetectFilter.getSrcPin().connect(mGrindFaceFilter.getSinkPin(1));
        if (mSpecialEffectsFilter != null) {
            mGrindFaceFilter.getSrcPin().connect(mSpecialEffectsFilter.getSinkPin());
            mSpecialEffectsFilter.getSrcPin().connect(mSrcAdapter.mSinkPin);
        } else {
            mGrindFaceFilter.getSrcPin().connect(mSrcAdapter.mSinkPin);
        }

        setGrindRatio(0.4f);
        setWhitenRatio(0.2f);
        setRuddyRatio(0.8f);
    }

    @Override
    public void setOnErrorListener(OnErrorListener listener) {
        super.setOnErrorListener(listener);
        mSkinDetectFilter.setOnErrorListener(mErrorListener);
        mGrindFaceFilter.setOnErrorListener(mErrorListener);
        if (mSpecialEffectsFilter != null) {
            mSpecialEffectsFilter.setOnErrorListener(mErrorListener);
        }
    }

    @Override
    public boolean isGrindRatioSupported() {
        return true;
    }

    @Override
    public boolean isWhitenRatioSupported() {
        return true;
    }

    @Override
    public boolean isRuddyRatioSupported() {
        return mSpecialEffectsFilter != null;
    }

    @Override
    public void setGrindRatio(float ratio) {
        super.setGrindRatio(ratio);
        mGrindFaceFilter.setGrindRatio(ratio);
    }

    @Override
    public void setWhitenRatio(float ratio) {
        super.setWhitenRatio(ratio);
        mGrindFaceFilter.setWhitenRatio(ratio);
    }

    @Override
    public void setRuddyRatio(float ratio) {
        super.setRuddyRatio(ratio);
        if (mSpecialEffectsFilter != null) {
            mSpecialEffectsFilter.setIntensity(ratio);
        }
    }

    @Override
    public int getSinkPinNum() {
        return 1;
    }

    @Override
    public SinkPin<ImgTexFrame> getSinkPin(int idx) {
        return mSinkAdapter.mSinkPin;
    }

    @Override
    public SrcPin<ImgTexFrame> getSrcPin() {
        return mSrcAdapter.mSrcPin;
    }

    @Override
    public String getVersion() {
        return "1.2";
    }

    public void setGLRender(GLRender glRender) {
        mSkinDetectFilter.setGLRender(glRender);
        mGrindFaceFilter.setGLRender(glRender);
        if(mSpecialEffectsFilter != null) {
            mSpecialEffectsFilter.setGLRender(glRender);
        }
    }
}
