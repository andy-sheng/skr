package com.component.mediaengine.filter.imgtex;

import android.content.Context;

import com.component.mediaengine.framework.ImgTexFrame;
import com.component.mediaengine.framework.SinkPin;
import com.component.mediaengine.framework.SrcPin;
import com.component.mediaengine.util.gles.GLRender;

/**
 * Beauty pro filter.
 *
 几组推荐的效果参数  美白：whitenRatio=1.0,ruddyRatio=0.0;
 粉嫩：whitenRatio=0.3,ruddyRatio=-0.3；
 红润：whitenRatio=0.3,ruddyRatio=0.4；
 其中init 效果参数默认是粉嫩，磨皮参数默认是0.5；
 *
 */

public class ImgBeautyProFilter extends ImgFilterBase {
    private static final String TAG = "ImgBeautyProFilter";
    private ImgFilterBase mBaseFilter;

    public ImgBeautyProFilter(GLRender glRender, Context context) {
        this(glRender, context, 1);
    }

    public ImgBeautyProFilter(GLRender glRender, Context context, int idx) {
        if (idx == 1) {
            mBaseFilter = new ImgBeautySimpleFilter(glRender, context);
            setGrindRatio(0.5f);
            ((ImgBeautySimpleFilter) mBaseFilter).setScaleLumance(1.0f);
        } else if(idx == 2) {
            mBaseFilter = new ImgBeautyAdvanceFilter(glRender, context);
            setGrindRatio(0.5f);
        } else if(idx == 3) {
            mBaseFilter = new ImgBeautySimpleFilter(glRender, context);
            setGrindRatio(0.2f);
            ((ImgBeautySimpleFilter) mBaseFilter).setScaleLumance(0.7f);
        } else if(idx == 4) {
            mBaseFilter = new ImgBeautyAdvanceFilter(glRender, context);
            setGrindRatio(0.3f);
        } else {
            throw new IllegalArgumentException("only type 1-4 supported!");
        }

        setWhitenRatio(0.3f);
        setRuddyRatio(-0.3f);
    }

    @Override
    public void setOnErrorListener(OnErrorListener listener) {
        super.setOnErrorListener(listener);
        mBaseFilter.setOnErrorListener(listener);
    }

    @Override
    public boolean isGrindRatioSupported() {
        return mBaseFilter.isGrindRatioSupported();
    }

    @Override
    public boolean isWhitenRatioSupported() {
        return mBaseFilter.isWhitenRatioSupported();
    }

    @Override
    public boolean isRuddyRatioSupported() {
        return mBaseFilter.isRuddyRatioSupported();
    }

    @Override
    public void setGrindRatio(float ratio) {
        super.setGrindRatio(ratio);
        mBaseFilter.setGrindRatio(ratio);
    }

    @Override
    public void setWhitenRatio(float ratio) {
        super.setWhitenRatio(ratio);
        mBaseFilter.setWhitenRatio(ratio);
    }

    /**
     * Set the ruddy ratio.
     *
     * @param ratio the ratio between -1.0f~1.0f
     */
    @Override
    public void setRuddyRatio(float ratio) {
        super.setRuddyRatio(ratio);
        mBaseFilter.setRuddyRatio(ratio);
    }

    @Override
    public int getSinkPinNum() {
        return 1;
    }

    @Override
    public SinkPin<ImgTexFrame> getSinkPin(int idx) {
        return mBaseFilter.getSinkPin();
    }

    @Override
    public SrcPin<ImgTexFrame> getSrcPin() {
        return mBaseFilter.getSrcPin();
    }

    @Override
    public String getVersion() {
        return "2.4";
    }

    public void setGLRender(GLRender glRender) {
        if (mBaseFilter instanceof ImgBeautySimpleFilter) {
            ((ImgBeautySimpleFilter) mBaseFilter).setGLRender(glRender);
        } else if (mBaseFilter instanceof ImgBeautyAdvanceFilter) {
            ((ImgBeautyAdvanceFilter) mBaseFilter).setGLRender(glRender);
        }
    }
}
