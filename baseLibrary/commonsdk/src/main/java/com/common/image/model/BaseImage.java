package com.common.image.model;

import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.common.image.fresco.IFrescoCallBack;
import com.common.utils.U;
import com.facebook.drawee.drawable.ProgressBarDrawable;
import com.facebook.drawee.drawable.ScalingUtils.ScaleType;
import com.facebook.imagepipeline.common.Priority;
import com.facebook.imagepipeline.request.Postprocessor;

/**
 * 使用 ImageFactory 来 new 图片
 * Created by lan on 15-12-14.
 */
public abstract class BaseImage {
    protected Uri mUri;

    protected int mWidth = U.getDisplayUtils().dip2px(40); //显示宽度

    protected int mHeight = U.getDisplayUtils().dip2px(40);//显示高度

    /**
     * FIT_XY           无视宽高比填充满
     * FIT_START        保持宽高比，缩放，直到一边到界
     * FIT_CENTER       同上，但是最后居中
     * FIT_END          同上上，但与显示边界右或下对齐
     * CENTER           居中无缩放
     * CENTER_INSIDE    使的图片都在边界内，与FIT_CENTER不同的是，只会缩小不会放大，默认是这个吧
     * CENTER_CROP      保持宽高比，缩小或放大，使两边都大于等于边界，居中。
     * FOCUS_CROP       同CENTER_CROP，但中心点可以设置
     * FIT_BOTTOM_START
     */
    protected ScaleType mScaleType;

    // 加载失败的图
    protected Drawable mFailureDrawable;
    protected ScaleType mFailureScaleType = ScaleType.CENTER_INSIDE;

    // 加载loading的图
    protected Drawable mLoadingDrawable;
    protected ScaleType mLoadingScaleType = ScaleType.CENTER_INSIDE;
    // 圆形
    protected boolean mIsCircle = false;
    // 自动播动画
    protected boolean mIsAutoPlayAnimation = false;
    // 后处理
    protected Postprocessor mPostprocessor;
    // fresco回调
    protected IFrescoCallBack mCallBack;
    //圆角矩形参数
    protected int mCornerRadius = 0;
    protected float[] mCornerRadii;
    //边框
    protected float mBorderWidth = 0;
    //边框颜色
    protected int mBorderColor = 0;
    //图片加载进度条
    protected ProgressBarDrawable mProgressBarDrawable = null;
    //低分辨率的图片uri
    protected Uri mLowImageUri = null;
    //加载的优先级
    protected Priority mRequestPriority = Priority.MEDIUM;
    //是否支持渐进式加载
    protected boolean mProgressiveRenderingEnabled = false;

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

    public Postprocessor getPostprocessor() {
        return mPostprocessor;
    }

    public int getCornerRadius() {
        return mCornerRadius;
    }

    public float[] getCornerRadii() {
        return mCornerRadii;
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

    public Uri getLowImageUri() {
        return mLowImageUri;
    }

    public Priority getRequestPriority() {
        return mRequestPriority;
    }

    public boolean isProgressiveRenderingEnabled() {
        return mProgressiveRenderingEnabled;
    }


    public ProgressBarDrawable getProgressBarDrawable() {
        return mProgressBarDrawable;
    }

    /*以下是参数对应的设置方法，尽量用ImageFactory builder*/
    public void setIsAutoPlayAnimation(boolean mIsAutoPlayAnimation) {
        this.mIsAutoPlayAnimation = mIsAutoPlayAnimation;
    }

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

    public void setLowImageUri(Uri lowImageUri) {
        this.mLowImageUri = lowImageUri;
    }

    public void setCornerRadii(float[] cornerRadii) {
        mCornerRadii = cornerRadii;
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

    public void setUri(Uri uri) {
        mUri = uri;
    }

    public void setCircle(boolean circle) {
        mIsCircle = circle;
    }

    public void setAutoPlayAnimation(boolean autoPlayAnimation) {
        mIsAutoPlayAnimation = autoPlayAnimation;
    }

    public void setRequestPriority(Priority requestPriority) {
        mRequestPriority = requestPriority;
    }

    public void setProgressiveRenderingEnabled(boolean progressiveRenderingEnabled) {
        mProgressiveRenderingEnabled = progressiveRenderingEnabled;
    }

    public void setProgressBarDrawable(ProgressBarDrawable progressBarDrawable) {
        this.mProgressBarDrawable = progressBarDrawable;
    }


}
