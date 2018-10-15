package com.common.image.model;

import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.common.image.fresco.IFrescoCallBack;
import com.facebook.drawee.drawable.ProgressBarDrawable;
import com.facebook.drawee.drawable.ScalingUtils.ScaleType;
import com.facebook.imagepipeline.common.Priority;
import com.facebook.imagepipeline.request.Postprocessor;

/**
 * Created by lan on 15-12-14.
 */
public class ImageFactory {
    public static ImageFactory.Builder newHttpImage(String url) {
        return new ImageFactory.Builder().setUrl(url);
    }

    public static ImageFactory.Builder newLocalImage(String path) {
        return new ImageFactory.Builder().setPath(path);
    }

    public static ImageFactory.Builder newResImage(int resId) {
        return new ImageFactory.Builder().setResId(resId);
    }

    public static class Builder {
        private BaseImage mBaseImage;

        private Builder() {
        }

        protected ImageFactory.Builder setUrl(String url) {
            mBaseImage = new HttpImage(url);
            return this;
        }

        protected ImageFactory.Builder setPath(String path) {
            mBaseImage = new LocalImage(path);
            return this;
        }

        protected ImageFactory.Builder setResId(int resId) {
            mBaseImage = new ResImage(resId);
            return this;
        }

        public ImageFactory.Builder setWidth(int width) {
            mBaseImage.setWidth(width);
            return this;
        }

        public ImageFactory.Builder setHeight(int height) {
            mBaseImage.setHeight(height);
            return this;
        }

        public ImageFactory.Builder setScaleType(ScaleType scaleType) {
            mBaseImage.setScaleType(scaleType);
            return this;
        }

        public ImageFactory.Builder setFailureDrawable(Drawable failure) {
            mBaseImage.setFailureDrawable(failure);
            return this;
        }

        public ImageFactory.Builder setLoadingDrawable(Drawable loading) {
            mBaseImage.setLoadingDrawable(loading);
            return this;
        }

        public ImageFactory.Builder setFailureScaleType(ScaleType failureScaleType) {
            mBaseImage.setFailureScaleType(failureScaleType);
            return this;
        }

        public ImageFactory.Builder setLoadingScaleType(ScaleType loadingScaleType) {
            mBaseImage.setLoadingScaleType(loadingScaleType);
            return this;
        }

        public ImageFactory.Builder setIsCircle(boolean isCircle) {
            mBaseImage.setIsCircle(isCircle);
            return this;
        }

        public ImageFactory.Builder setPostprocessor(Postprocessor postprocessor) {
            mBaseImage.setPostprocessor(postprocessor);
            return this;
        }

        public ImageFactory.Builder setAutoPlayAnimation(boolean isAutoPlayAnim) {
            mBaseImage.setIsAutoPlayAnimation(isAutoPlayAnim);
            return this;
        }

        public ImageFactory.Builder setCornerRadius(int cornerRadius) {
            mBaseImage.setCornerRadius(cornerRadius);
            return this;
        }

        public ImageFactory.Builder setCornerRadii(float[] cornerRadii) {
            mBaseImage.setCornerRadii(cornerRadii);
            return this;
        }

        public ImageFactory.Builder setBorderWidth(int borderWidth) {
            mBaseImage.setBorderWidth(borderWidth);
            return this;
        }

        public ImageFactory.Builder setBorderColor(int borderColor) {
            mBaseImage.setBorderColor(borderColor);
            return this;
        }

        public ImageFactory.Builder setCallBack(IFrescoCallBack callBack) {
            mBaseImage.setCallBack(callBack);
            return this;
        }

        public ImageFactory.Builder setLowImageUri(Uri lowImageUri) {
            mBaseImage.setLowImageUri(lowImageUri);
            return this;
        }

        public ImageFactory.Builder setCircle(boolean circle) {
            mBaseImage.setCircle(circle);
            return this;
        }

        public ImageFactory.Builder setRequestPriority(Priority requestPriority) {
            mBaseImage.setRequestPriority(requestPriority);
            return this;
        }

        public ImageFactory.Builder setProgressiveRenderingEnabled(boolean progressiveRenderingEnabled) {
            mBaseImage.setProgressiveRenderingEnabled(progressiveRenderingEnabled);
            return this;
        }

        public ImageFactory.Builder setProgressBarDrawable(ProgressBarDrawable progressBarDrawable) {
            mBaseImage.setProgressBarDrawable(progressBarDrawable);
            return this;
        }

        public BaseImage build() {
            return mBaseImage;
        }
    }
}
