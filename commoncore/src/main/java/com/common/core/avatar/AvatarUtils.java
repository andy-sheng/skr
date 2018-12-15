package com.common.core.avatar;


import android.os.Build;
import android.text.TextUtils;
import android.util.LruCache;

import com.common.core.R;
import com.common.image.fresco.FrescoWorker;
import com.common.image.fresco.IFrescoCallBack;
import com.common.image.fresco.processor.BlurPostprocessor;
import com.common.image.model.BaseImage;
import com.common.image.model.ImageFactory;
import com.common.log.MyLog;
import com.common.utils.ImageUtils;
import com.common.utils.U;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;

/**
 * 方便头像加载的工具类，请维护好
 */
public class AvatarUtils {
    public final static String TAG = "AvatarUtils";

    /**
     * 头像链接拼接方式
     * 第一个%d：uid；
     * 第一个%s：用来裁切缩略图
     * 第二个%d：timeStamp，没有服务器时间戳，采用本地时间戳
     */
    public static String AVATAR_URL = "http://dl.zb.mi.com/%d%s?timestamp=%d";
    /**
     * 不带时间戳的头像拼接方法
     */
    public static String AVATAR_DEFAULT_URL = "http://dl.zb.mi.com/%d%s";

    final static int MAX_CACHE_SIZE = 500;
    /**
     * 缓存一些用户的头像时间戳，
     * 在一些拿不到时间戳的场景，也可保证头像显示正确
     */
    private static final LruCache<Long, Long> sAvatarTimeCache = new LruCache<>(MAX_CACHE_SIZE);


    /**
     * 加载头像
     *
     * @param draweeView
     */
    public static void loadAvatarByUrl(final SimpleDraweeView draweeView
            , LoadParams params) {
        String url = !TextUtils.isEmpty(params.url) ? params.url : getAvatarUrlByCustom(params.uid, params.sizeType, params.timestamp, params.isWebpFormat);
        BaseImage avatarImg;
        if (TextUtils.isEmpty(url)) {
            avatarImg = ImageFactory.newResImage(params.loadingAvatarResId).build();
        } else {
            avatarImg = ImageFactory.newHttpImage(url)
                    .setWidth(params.width)
                    .setHeight(params.height)
                    .setIsCircle(params.isCircle)
                    .setFailureDrawable(params.loadingAvatarResId > 0 ? U.app().getResources().getDrawable(
                            params.loadingAvatarResId) : null)
                    .setFailureScaleType(
                            params.isCircle ? ScalingUtils.ScaleType.CENTER_INSIDE : ScalingUtils.ScaleType.CENTER_CROP)
                    .build();
        }
        if (params.loadingAvatarResId > 0) {
            avatarImg.setLoadingDrawable(U.app().getResources().getDrawable(params.loadingAvatarResId));
            avatarImg.setLoadingScaleType(params.isCircle ? ScalingUtils.ScaleType.CENTER_INSIDE : ScalingUtils.ScaleType.CENTER_CROP);
        }
        if (params.isBlur) {
            avatarImg.setPostprocessor(new BlurPostprocessor());
        }
        FrescoWorker.loadImage(draweeView, avatarImg);
    }

    public static String getAvatarUrl(long uid) {
        return getAvatarUrlByCustom(uid, ImageUtils.SIZE.SIZE_160, 0, false);
    }

    public static String getAvatarUrl(long uid, long timestamp) {
        return getAvatarUrlByCustom(uid, ImageUtils.SIZE.SIZE_160, timestamp, false);
    }

    public static String getAvatarUrl(long uid, ImageUtils.SIZE sizeType, long timestamp) {
        return getAvatarUrlByCustom(uid, sizeType, timestamp, false);
    }

    /**
     * 得到头像的url
     *
     * @param uid
     * @param sizeType
     * @param timestamp
     * @param isWebpFormat
     * @return
     */
    public static String getAvatarUrlByCustom(long uid, ImageUtils.SIZE sizeType, long timestamp, boolean isWebpFormat) {
        Long cacheTimeStamp = sAvatarTimeCache.get(uid);
        if (cacheTimeStamp == null) {
            cacheTimeStamp = 0L;
        }

        if (cacheTimeStamp.longValue() >= timestamp) {
            timestamp = cacheTimeStamp.longValue();
        } else {
            sAvatarTimeCache.put(uid, timestamp);
            if (sAvatarTimeCache.size() > MAX_CACHE_SIZE * 1.5f) {
                MyLog.e(TAG, "mAvatarTimeCache.size is large than 150, evictAll");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    sAvatarTimeCache.trimToSize(MAX_CACHE_SIZE);
                }
            }
        }

        String url = String.format(AVATAR_DEFAULT_URL, uid
                , isWebpFormat ? U.getImageUtils().getSizeSuffix(sizeType) : U.getImageUtils().getSizeSuffixJpg(sizeType));
        if (timestamp == 0) {
            return url;
        } else {
            return url + "?timestamp=" + timestamp;
        }
    }

    public static LoadParams.Builder newParamsBuilder(long uid) {
        return new LoadParams.Builder().setUid(uid);
    }

    public static class LoadParams {

        String url;
        long uid;
        ImageUtils.SIZE sizeType = ImageUtils.SIZE.SIZE_160;
        long timestamp = 0 ;
        boolean isWebpFormat = false;
        boolean isCircle = false;
        boolean isBlur = false;
        int loadingAvatarResId = R.drawable.avatar_default_b;
        int width = 0;
        int height = 0;

        LoadParams() {
        }

        public void setUid(long uid) {
            this.uid = uid;
        }

        public void setSizeType(ImageUtils.SIZE sizeType) {
            this.sizeType = sizeType;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

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

        public static class Builder {
            private LoadParams mUploadParams = new LoadParams();

            Builder() {
            }

            public Builder setUid(long uid) {
                mUploadParams.setUid(uid);
                return this;
            }

            public Builder setSizeType(ImageUtils.SIZE sizeType) {
                mUploadParams.setSizeType(sizeType);
                return this;
            }

            public Builder setTimestamp(long timestamp) {
                mUploadParams.setTimestamp(timestamp);
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

            public Builder setUrl(String url) {
                mUploadParams.setUrl(url);
                return this;
            }

            public LoadParams build() {
                if (this.mUploadParams == null) {
                    this.mUploadParams = new LoadParams();
                }

                if (mUploadParams.uid == 0) {
                    MyLog.e(TAG,"LoadParams.Build must uid not 0");
//                    throw new IllegalArgumentException("");
                }

                return this.mUploadParams;
            }
        }
    }

}
