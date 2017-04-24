package com.wali.live.sdk.litedemo.fresco.image;

import android.net.Uri;

import com.facebook.drawee.drawable.ScalingUtils.ScaleType;
import com.facebook.imagepipeline.request.Postprocessor;
import com.little.glint.fresco.callback.IFrescoCallback;

/**
 * Created by lan on 15-12-14.
 */
public abstract class BaseImage {
    protected Uri mUri;

    protected int mWidth = 40 * 3;
    protected int mHeight = 40 * 3;

    protected ScaleType mScaleType;
    protected boolean mIsCircle = false;
    protected int mCornerRadius = 0;

    protected Postprocessor mPostprocessor;
    protected IFrescoCallback mCallBack;

    protected abstract void generateUri();

    public Uri getUri() {
        return mUri;
    }

    public int getWidth() {
        return mWidth;
    }

    public void setWidth(int width) {
        this.mWidth = width;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setHeight(int height) {
        this.mHeight = height;
    }

    public ScaleType getScaleType() {
        return mScaleType;
    }

    public void setScaleType(ScaleType scaleType) {
        this.mScaleType = scaleType;
    }

    public boolean isCircle() {
        return mIsCircle;
    }

    public void setIsCircle(boolean isCircle) {
        this.mIsCircle = isCircle;
    }

    public int getCornerRadius() {
        return mCornerRadius;
    }

    public void setCornerRadius(int cornerRadius) {
        this.mCornerRadius = cornerRadius;
    }

    public Postprocessor getPostprocessor() {
        return mPostprocessor;
    }

    public void setPostprocessor(Postprocessor postprocessor) {
        this.mPostprocessor = postprocessor;
    }

    public IFrescoCallback getCallBack() {
        return mCallBack;
    }

    public void setCallBack(IFrescoCallback callBack) {
        this.mCallBack = callBack;
    }
}
