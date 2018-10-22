package com.wali.live.modulechannel.model.viewmodel;


import java.io.Serializable;

/**
 * Created by lan on 16/9/14.
 *
 * @module 频道
 */
public abstract class BaseJumpItem implements Serializable {
    protected final String TAG = getTAG();

    protected String mSchemeUri;

    // 曝光打点
    protected String mExposureTag;
    protected boolean mIsExposured;

    public String getTAG() {
        return getClass().getSimpleName();
    }

    public String getSchemeUri() {
        return mSchemeUri;
    }

    // 打点传的recommend tag
    public String getRecommendTag() {
        //TOdo-暂时注释了
//        if (mExposureTag == null) {
//            mExposureTag = SchemeUtils.getRecommendTag(mSchemeUri);
//        }
//        return mExposureTag;
        return null;
    }

    public boolean isExposured() {
        return mIsExposured;
    }

    public void setIsExposured(boolean mIsExposured) {
        this.mIsExposured = mIsExposured;
    }
}
