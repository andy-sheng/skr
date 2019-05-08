package com.zq.mediaengine.filter.imgtex;

import android.content.Context;
import android.util.Log;

import com.zq.mediaengine.framework.ImgTexFrame;
import com.zq.mediaengine.framework.SinkPin;
import com.zq.mediaengine.framework.SrcPin;
import com.zq.mediaengine.util.gles.GLRender;

/**
 * @hide
 */

public class ImgBeautySimpleFilter extends ImgFilterBase {
    private static final String TAG = "ImgBeautySimpleFilter";

    private ImgBeautyGrindSimpleFilter mGrindSimpleFilter;
    private ImgBeautySpecialEffectsFilter mSpecialEffectsFilter;
    private ImgBeautyAdjustSkinColorFilter mAdjustSkinColorFilter;

    public ImgBeautySimpleFilter(GLRender glRender, Context context) {
        mGrindSimpleFilter = new ImgBeautyGrindSimpleFilter(glRender);
        try {
            mSpecialEffectsFilter = new ImgBeautySpecialEffectsFilter(glRender,
                    context, "13_nature.png");
        } catch (Exception e) {
            Log.e(TAG, "Resource missing!");
        }
        if (mSpecialEffectsFilter != null) {
            mGrindSimpleFilter.getSrcPin().connect(mSpecialEffectsFilter.getSinkPin());
        }

        String pinkUri = "assets://Resource/0_pink.png";
        String ruddyUri = "assets://Resource/0_ruddy2.png";
        try {
            mAdjustSkinColorFilter = new ImgBeautyAdjustSkinColorFilter(glRender, context,
                    pinkUri, ruddyUri);
        } catch (Exception e) {
            Log.e(TAG, "Resource missing!");
        }
        if (mAdjustSkinColorFilter != null) {
            if (mSpecialEffectsFilter != null) {
                mSpecialEffectsFilter.getSrcPin().connect(mAdjustSkinColorFilter.getSinkPin());
            } else {
                mGrindSimpleFilter.getSrcPin().connect(mAdjustSkinColorFilter.getSinkPin());
            }
        }

        setWhitenRatio(0.3f);
        setGrindRatio(0.5f);
        setRuddyRatio(-0.3f);
    }

    @Override
    public void setOnErrorListener(OnErrorListener listener) {
        super.setOnErrorListener(listener);
        mGrindSimpleFilter.setOnErrorListener(mErrorListener);
        if (mSpecialEffectsFilter != null) {
            mSpecialEffectsFilter.setOnErrorListener(mErrorListener);
        }
        if (mAdjustSkinColorFilter != null) {
            mAdjustSkinColorFilter.setOnErrorListener(mErrorListener);
        }
    }

    @Override
    public boolean isGrindRatioSupported() {
        return true;
    }

    @Override
    public boolean isWhitenRatioSupported() {
        return mSpecialEffectsFilter != null;
    }

    @Override
    public boolean isRuddyRatioSupported() {
        return mAdjustSkinColorFilter != null;
    }

    @Override
    public void setGrindRatio(float ratio) {
        super.setGrindRatio(ratio);
        mGrindSimpleFilter.setGrindRatio(ratio);
    }

    @Override
    public void setWhitenRatio(float ratio) {
        super.setWhitenRatio(ratio);
        if (mSpecialEffectsFilter != null) {
            mSpecialEffectsFilter.setIntensity(ratio);
        }
    }

    /**
     * Set the ruddy ratio.
     *
     * @param ratio the ratio between -1.0f~1.0f
     */
    @Override
    public void setRuddyRatio(float ratio) {
        super.setRuddyRatio(ratio);
        if (mAdjustSkinColorFilter != null) {
            mAdjustSkinColorFilter.setRuddyRatio(ratio);
        }
    }

    @Override
    public int getSinkPinNum() {
        return 1;
    }

    @Override
    public SinkPin<ImgTexFrame> getSinkPin(int idx) {
        return mGrindSimpleFilter.getSinkPin();
    }

    @Override
    public SrcPin<ImgTexFrame> getSrcPin() {
        if (mAdjustSkinColorFilter != null) {
            return mAdjustSkinColorFilter.getSrcPin();
        } else if (mSpecialEffectsFilter != null) {
            return mSpecialEffectsFilter.getSrcPin();
        } else {
            return mGrindSimpleFilter.getSrcPin();
        }
    }

    public void setGLRender(GLRender glRender) {
        mGrindSimpleFilter.setGLRender(glRender);
        if (mAdjustSkinColorFilter != null) {
            mAdjustSkinColorFilter.setGLRender(glRender);
        }
        if (mSpecialEffectsFilter != null) {
            mSpecialEffectsFilter.setGLRender(glRender);
        }
    }

    public void setScaleLumance(float scaleLumance) {
        mGrindSimpleFilter.setScaleLumance(scaleLumance);
    }
}
