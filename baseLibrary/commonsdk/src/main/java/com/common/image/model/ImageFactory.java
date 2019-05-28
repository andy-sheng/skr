package com.common.image.model;

import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.common.image.fresco.IFrescoCallBack;
import com.common.image.model.oss.IOssParam;
import com.common.image.model.oss.OssImgResize;
import com.common.log.MyLog;
import com.common.utils.U;
import com.facebook.drawee.drawable.ProgressBarDrawable;
import com.facebook.drawee.drawable.ScalingUtils.ScaleType;
import com.facebook.imagepipeline.common.Priority;
import com.facebook.imagepipeline.request.Postprocessor;

/**
 * Created by lan on 15-12-14.
 */
public class ImageFactory {
    public static ImageFactory.Builder newPathImage(String url) {
        return new ImageFactory.Builder().setPath(url);
    }

//    public static ImageFactory.Builder newLocalImage(String path) {
//        return new ImageFactory.Builder().setPath(path);
//    }

    public static ImageFactory.Builder newResImage(int resId) {
        return new ImageFactory.Builder().setResId(resId);
    }

    public static class Builder {
        private BaseImage mBaseImage;

        private Builder() {
        }

//        protected ImageFactory.Builder setUrl(String url) {
//            mBaseImage = new HttpImage(url);
//            return this;
//        }

        protected ImageFactory.Builder setPath(String path) {
            if (path.startsWith("http")) {
                mBaseImage = new HttpImage(path);
            } else {
                mBaseImage = new LocalImage(path);
            }
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

        public ImageFactory.Builder setFitDrawable(boolean isFit) {
            mBaseImage.setAdjustViewWHbyImage(isFit);
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

        public ImageFactory.Builder setCornerRadius(float cornerRadius) {
            mBaseImage.setCornerRadius(cornerRadius);
            return this;
        }

        public ImageFactory.Builder setCornerRadii(float[] cornerRadii) {
            mBaseImage.setCornerRadii(cornerRadii);
            return this;
        }

        public ImageFactory.Builder setBorderWidth(float borderWidth) {
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

        public ImageFactory.Builder setTapToRetryEnabled(boolean tapToRetryEnabled) {
            mBaseImage.setTapToRetryEnabled(tapToRetryEnabled);
            return this;
        }

        /**
         * 阿里云oss强大的 oss处理库
         * 使用 {@link com.common.image.model.oss.OssImgFactory 构造处理效果}
         *
         * @cparam ossProcessors
         * @return
         */
        public ImageFactory.Builder addOssProcessors(IOssParam... ossProcessors) {
            if (mBaseImage instanceof HttpImage) {
                HttpImage httpImage = (HttpImage) mBaseImage;
                httpImage.addOssProcessors(ossProcessors);
            } else if (mBaseImage instanceof LocalImage) {
                for (IOssParam iOssParam : ossProcessors) {
                    if (iOssParam instanceof OssImgResize) {
                        OssImgResize ossImgResize = (OssImgResize) iOssParam;
                        if (ossImgResize.getW() > 0) {
                            setWidth(ossImgResize.getW());
                            if (ossImgResize.getH() > 0) {
                                setHeight(ossImgResize.getH());
                            } else {
                                int wh[] = U.getImageUtils().getImageWidthAndHeightFromFile(((LocalImage) mBaseImage).getPath());
                                if (wh != null) {
                                    if (wh[0] != 0) {
                                        setHeight(ossImgResize.getW() * wh[1] / wh[0]);
                                    }
                                }
                            }
                        }

                        break;
                    }
                }
                if (MyLog.isDebugLogOpen()) {
                    //throw new IllegalStateException("setOssProcessors only can be set In HttpImage");
                }
            }
            return this;
        }

        public <T extends BaseImage> T build() {
            return (T) mBaseImage;
        }
    }
}
