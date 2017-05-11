package com.wali.live.sdk.litedemo.fresco.image;

import com.facebook.drawee.drawable.ScalingUtils.ScaleType;
import com.facebook.imagepipeline.request.Postprocessor;
import com.wali.live.sdk.litedemo.fresco.callback.IFrescoCallback;

/**
 * Created by lan on 15-12-14.
 */
public class ImageFactory {
    public static Builder newHttpImage(String url) {
        return new Builder().setUrl(url);
    }

    public static Builder newLocalImage(String path) {
        return new Builder().setPath(path);
    }

    public static Builder newResImage(int resId) {
        return new Builder().setResId(resId);
    }

    public static class Builder {
        private BaseImage mBaseImage;

        private Builder() {
        }

        protected Builder setUrl(String url) {
            mBaseImage = new HttpImage(url);
            return this;
        }

        protected Builder setPath(String path) {
            mBaseImage = new LocalImage(path);
            return this;
        }

        protected Builder setResId(int resId) {
            mBaseImage = new ResImage(resId);
            return this;
        }

        public Builder setWidth(int width) {
            mBaseImage.setWidth(width);
            return this;
        }

        public Builder setHeight(int height) {
            mBaseImage.setHeight(height);
            return this;
        }

        public Builder setScaleType(ScaleType scaleType) {
            mBaseImage.setScaleType(scaleType);
            return this;
        }

        public Builder setIsCircle(boolean isCircle) {
            mBaseImage.setIsCircle(isCircle);
            return this;
        }

        public Builder setCornerRadius(int cornerRadius) {
            mBaseImage.setCornerRadius(cornerRadius);
            return this;
        }

        public Builder setPostprocessor(Postprocessor postprocessor) {
            mBaseImage.setPostprocessor(postprocessor);
            return this;
        }

        public Builder setCallBack(IFrescoCallback callBack) {
            mBaseImage.setCallBack(callBack);
            return this;
        }

        public BaseImage build() {
            return mBaseImage;
        }
    }
}
