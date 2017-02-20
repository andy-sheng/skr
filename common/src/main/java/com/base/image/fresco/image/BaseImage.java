package com.base.image.fresco.image;

import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.base.image.fresco.IFrescoCallBack;
import com.base.utils.display.DisplayUtils;
import com.facebook.drawee.drawable.ProgressBarDrawable;
import com.facebook.drawee.drawable.ScalingUtils.ScaleType;
import com.facebook.imagepipeline.common.Priority;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.Postprocessor;

/**
 * Created by lan on 15-12-14.
 */
public abstract class BaseImage {
    protected Uri mUri;

    public int mWidth = DisplayUtils.dip2px(40); //显示宽度

    public int mHeight = DisplayUtils.dip2px(40);//显示高度

    protected ScaleType mScaleType;

    // 加载失败的图
    protected Drawable mFailureDrawable;
    protected ScaleType mFailureScaleType = ScaleType.CENTER_INSIDE;

    // 加载loading的图
    protected Drawable mLoadingDrawable;
    protected ScaleType mLoadingScaleType = ScaleType.CENTER_INSIDE;
    // 圆形
    protected boolean mIsCircle = false;

    protected boolean mIsAutoPlayAnimation = false;
    // 后处理
    protected Postprocessor mPostprocessor;
    // fresco回调
    protected IFrescoCallBack mCallBack;

    protected int mCornerRadius = 0;

    protected float mBorderWidth = 0;

    protected int mBorderColor = 0;

    public ProgressBarDrawable mProgressBarDrawable = null;//图片加载进度条

    public Uri mLowImageUri = null;//低分辨率的图片uri

    public Priority requestPriority = Priority.MEDIUM;

    public interface OnPostProcessImageInfoListener {
        void processImageInfo(ImageInfo imageInfo);
    }

    public OnPostProcessImageInfoListener postProcessImageInfoListener;

    public void setOnPostImageInfoListener(OnPostProcessImageInfoListener listener) {
        postProcessImageInfoListener = listener;
    }

    // 生成uri
    protected abstract void generateUri();

    public Uri getUri() {
        return mUri;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public Drawable getFailureDrawable() {
        return mFailureDrawable;
    }

    public Drawable getLoadingDrawable() {
        return mLoadingDrawable;
    }

    public ScaleType getFailureScaleType() {
        return mFailureScaleType;
    }

    public ScaleType getLoadingScaleType() {
        return mLoadingScaleType;
    }

    public ScaleType getScaleType() {
        return mScaleType;
    }

    public boolean isCircle() {
        return mIsCircle;
    }

    public boolean isAutoPlayAnimation() {
        return mIsAutoPlayAnimation;
    }

    public void setIsAutoPlayAnimation(boolean mIsAutoPlayAnimation) {
        this.mIsAutoPlayAnimation = mIsAutoPlayAnimation;
    }

    public Postprocessor getPostprocessor() {
        return mPostprocessor;
    }

    public int getCornerRadius() {
        return mCornerRadius;
    }

    public float getBorderWidth() {
        return mBorderWidth;
    }

    public int getBorderColor() {
        return mBorderColor;
    }

    public IFrescoCallBack getCallBack() {
        return mCallBack;
    }

    /*以下是参数对应的设置方法，尽量用ImageFactory builder*/
    public void setWidth(int mWidth) {
        this.mWidth = mWidth;
    }

    public void setHeight(int mHeight) {
        this.mHeight = mHeight;
    }

    public void setFailureDrawable(Drawable failure) {
        this.mFailureDrawable = failure;
    }

    public void setLoadingDrawable(Drawable loading) {
        this.mLoadingDrawable = loading;
    }

    public void setFailureScaleType(ScaleType failureScaleType) {
        this.mFailureScaleType = failureScaleType;
    }

    public void setLoadingScaleType(ScaleType loadingScaleType) {
        this.mLoadingScaleType = loadingScaleType;
    }

    public void setScaleType(ScaleType mScaleType) {
        this.mScaleType = mScaleType;
    }

    public void setIsCircle(boolean isCircle) {
        this.mIsCircle = isCircle;
    }

    public void setPostprocessor(Postprocessor postprocessor) {
        this.mPostprocessor = postprocessor;
    }

    public void setCornerRadius(int cornerRadius) {
        this.mCornerRadius = cornerRadius;
    }

    public void setBorderWidth(float borderWidth) {
        mBorderWidth = borderWidth;
    }

    public void setBorderColor(int borderColor) {
        this.mBorderColor = borderColor;
    }

    public void setCallBack(IFrescoCallBack callBack) {
        this.mCallBack = callBack;
    }
}
