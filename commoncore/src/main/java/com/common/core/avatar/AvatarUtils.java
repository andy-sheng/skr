package com.common.core.avatar;


import android.text.TextUtils;

import com.common.core.R;
import com.common.image.fresco.FrescoWorker;
import com.common.image.fresco.processor.BlurPostprocessor;
import com.common.image.fresco.processor.GrayPostprocessor;
import com.common.image.model.BaseImage;
import com.common.image.model.HttpImage;
import com.common.image.model.ImageFactory;
import com.common.image.model.oss.OssImgFactory;
import com.common.image.model.oss.format.OssImgFormat;
import com.common.log.MyLog;
import com.common.utils.ImageUtils;
import com.common.utils.U;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;

/**
 * 方便头像加载的工具类，请维护好
 */
public class AvatarUtils {
    public final static String TAG = "AvatarUtils";

    final static int MAX_CACHE_SIZE = 500;
    /**
     * 缓存一些用户的头像时间戳，
     * 在一些拿不到时间戳的场景，也可保证头像显示正确
     */
//    private static final LruCache<Long, Long> sAvatarTimeCache = new LruCache<>(MAX_CACHE_SIZE);


    /**
     * 加载头像
     *
     * @param draweeView
     */
    public static void loadAvatarByUrl(final SimpleDraweeView draweeView
            , LoadParams params) {
        HttpImage httpImage = getAvatarUrl(params);
        if (httpImage == null) {
            BaseImage avatarImg = ImageFactory.newResImage(params.loadingAvatarResId).build();
            FrescoWorker.loadImage(draweeView, avatarImg);
        } else {
            FrescoWorker.loadImage(draweeView, httpImage);
        }
    }

    /**
     * 自行取得 HttpImage 做些处理
     *
     * @param params
     * @return
     */
    public static HttpImage getAvatarUrl(LoadParams params) {
        String url = params.url;
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        ImageFactory.Builder imgBuilder = ImageFactory.newPathImage(url)
                .setWidth(params.width)
                .setHeight(params.height)
                .setIsCircle(params.isCircle)
                .setFailureDrawable(params.loadingAvatarResId > 0 ? U.app().getResources().getDrawable(
                        params.loadingAvatarResId) : null)
                .setFailureScaleType(
                        params.isCircle ? ScalingUtils.ScaleType.CENTER_INSIDE : ScalingUtils.ScaleType.CENTER_CROP);

        if (params.sizeType != null && params.sizeType != ImageUtils.SIZE.ORIGIN) {
            imgBuilder.addOssProcessors(OssImgFactory.newResizeBuilder().setW(params.sizeType.getW()).build());
        }
        if (params.isWebpFormat) {
            imgBuilder.addOssProcessors(OssImgFactory.newFormatBuilder().setFormat(OssImgFormat.ImgF.webp).build());
        }
        if (params.loadingAvatarResId > 0) {
            imgBuilder.setLoadingDrawable(U.app().getResources().getDrawable(params.loadingAvatarResId));
            imgBuilder.setLoadingScaleType(params.isCircle ? ScalingUtils.ScaleType.CENTER_INSIDE : ScalingUtils.ScaleType.CENTER_CROP);
        }
        if (params.isBlur) {
            imgBuilder.setPostprocessor(new BlurPostprocessor());
        }
        if (params.mBorderWidth > 0) {
            imgBuilder.setBorderWidth(params.mBorderWidth);
            imgBuilder.setBorderColor(params.mBorderColor);
        }
        imgBuilder.setCornerRadii(params.mCornerRadii);
        imgBuilder.setCornerRadius(params.mCornerRadius);

        if (params.isGray) {
            imgBuilder.setPostprocessor(new GrayPostprocessor());
        }
        HttpImage avatarImg = (HttpImage) imgBuilder.build();
        return avatarImg;
    }
//
//    public static String getAvatarUrl(long uid, long timestamp) {
//        return getAvatarUrlByCustom(uid, ImageUtils.SIZE.SIZE_160, timestamp, false);
//    }
//
//    public static String getAvatarUrl(long uid, ImageUtils.SIZE sizeType, long timestamp) {
//        return getAvatarUrlByCustom(uid, sizeType, timestamp, false);
//    }

//    /**
//     * 得到头像的url
//     *
//     * @param uid
//     * @param sizeType
//     * @param timestamp
//     * @param isWebpFormat
//     * @return
//     */
//    public static String getAvatarUrlByCustom(long uid, ImageUtils.SIZE sizeType, long timestamp, boolean isWebpFormat) {
//        Long cacheTimeStamp = sAvatarTimeCache.get(uid);
//        if (cacheTimeStamp == null) {
//            cacheTimeStamp = 0L;
//        }
//
//        if (cacheTimeStamp.longValue() >= timestamp) {
//            timestamp = cacheTimeStamp.longValue();
//        } else {
//            sAvatarTimeCache.put(uid, timestamp);
//            if (sAvatarTimeCache.size() > MAX_CACHE_SIZE * 1.5f) {
//                MyLog.e(TAG, "mAvatarTimeCache.size is large than 150, evictAll");
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//                    sAvatarTimeCache.trimToSize(MAX_CACHE_SIZE);
//                }
//            }
//        }
//
//        String url = String.format(AVATAR_DEFAULT_URL, uid
//                , isWebpFormat ? U.getImageUtils().getSizeSuffix(sizeType) : U.getImageUtils().getSizeSuffixJpg(sizeType));
//        if (timestamp == 0) {
//            return url;
//        } else {
//            return url + "?timestamp=" + timestamp;
//        }
//    }

    public static LoadParams.Builder newParamsBuilder(String url) {
        return new LoadParams.Builder(url);
    }

    public static class LoadParams {

        String url; // 头像url
        ImageUtils.SIZE sizeType = ImageUtils.SIZE.SIZE_160;
        boolean isWebpFormat = false;
        boolean isCircle = false;
        boolean isBlur = false;
        int loadingAvatarResId = R.drawable.avatar_default_b;
        int width = 0;
        int height = 0;
        //边框
        protected float mBorderWidth = 0;
        //边框颜色
        protected int mBorderColor = 0;
        //圆角矩形参数
        protected float mCornerRadius = 0;
        protected float[] mCornerRadii;

        boolean isGray = false;

        LoadParams() {
        }

//        public void setUid(long uid) {
//            this.uid = uid;
//        }

        public void setSizeType(ImageUtils.SIZE sizeType) {
            this.sizeType = sizeType;
        }

//        public void setTimestamp(long timestamp) {
//            this.timestamp = timestamp;
//        }

        public void setWebpFormat(boolean webpFormat) {
            isWebpFormat = webpFormat;
        }

        public void setCircle(boolean circle) {
            isCircle = circle;
        }

        public void setBlur(boolean blur) {
            isBlur = blur;
        }

        public void setLoadingAvatarResId(int loadingAvatarResId) {
            this.loadingAvatarResId = loadingAvatarResId;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public void setBorderWidth(float borderWidth) {
            mBorderWidth = borderWidth;
        }

        public void setBorderColor(int borderColor) {
            mBorderColor = borderColor;
        }

        public void setCornerRadius(float cornerRadius) {
            mCornerRadius = cornerRadius;
        }

        public void setCornerRadii(float[] cornerRadii) {
            mCornerRadii = cornerRadii;
        }

        public void setGray(boolean gray) {
            isGray = gray;
        }

        public static class Builder {
            private LoadParams mUploadParams = new LoadParams();

            Builder(String url) {
                mUploadParams.setUrl(url);
            }

//            public Builder setUid(long uid) {
//                mUploadParams.setUid(uid);
//                return this;
//            }

            public Builder setSizeType(ImageUtils.SIZE sizeType) {
                mUploadParams.setSizeType(sizeType);
                return this;
            }

            public Builder setWebpFormat(boolean webpFormat) {
                mUploadParams.setWebpFormat(webpFormat);
                return this;
            }

            public Builder setCircle(boolean circle) {
                mUploadParams.setCircle(circle);
                return this;
            }

            public Builder setBlur(boolean blur) {
                mUploadParams.setBlur(blur);
                return this;
            }

            public Builder setLoadingAvatarResId(int loadingAvatarResId) {
                mUploadParams.setLoadingAvatarResId(loadingAvatarResId);
                return this;
            }

            public Builder setWidth(int width) {
                mUploadParams.setWidth(width);
                return this;
            }

            public Builder setHeight(int height) {
                mUploadParams.setHeight(height);
                return this;
            }

            public Builder setBorderWidth(float borderWidth) {
                mUploadParams.setBorderWidth(borderWidth);
                return this;
            }

            public Builder setBorderColor(int borderColor) {
                mUploadParams.setBorderColor(borderColor);
                return this;
            }

            public Builder setBorderColorBySex(boolean isMan) {
                mUploadParams.setBorderColor(isMan ? U.getColor(R.color.color_man_stroke_color) : U.getColor(R.color.color_woman_stroke_color));
                return this;
            }

            public Builder setCornerRadius(float cornerRadius) {
                mUploadParams.setCornerRadius(cornerRadius);
                return this;
            }

            public Builder setCornerRadii(float[] cornerRadii) {
                mUploadParams.setCornerRadii(cornerRadii);
                return this;
            }

            public Builder setGray(boolean gray) {
                mUploadParams.setGray(gray);
                return this;
            }

            public LoadParams build() {
                if (this.mUploadParams == null) {
                    this.mUploadParams = new LoadParams();
                }

                if (mUploadParams.url == null) {
                    MyLog.e(TAG, "LoadParams.Build url must not null");
//                    throw new IllegalArgumentException("");
                }
                if (mUploadParams.isCircle) {
                    if (mUploadParams.loadingAvatarResId == R.drawable.avatar_default_b) {
                        mUploadParams.loadingAvatarResId = R.drawable.avatar_default_circle;
                    }
                }
                return this.mUploadParams;
            }
        }
    }

}
