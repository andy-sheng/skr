package com.wali.live.utils;

import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.LruCache;

import com.base.global.GlobalData;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.BaseImage;
import com.base.image.fresco.image.ImageFactory;
import com.base.image.fresco.processor.BlurPostprocessor;
import com.base.image.fresco.processor.GrayPostprocessor;
import com.base.log.MyLog;
import com.base.utils.Constants;
import com.base.utils.image.ImageUtils;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.live.module.common.R;

import java.io.File;

/**
 * Created by linjinbin on 16/2/25.
 */
public class AvatarUtils {
    private final static String TAG = AvatarUtils.class.getSimpleName();

    public static final int SIZE_TYPE_AVATAR_SOURCE = 0;         //原图
    public static final int SIZE_TYPE_AVATAR_SMALL = 1;          //小图：160
    public static final int SIZE_TYPE_AVATAR_MIDDLE = 2;         //中图：320
    public static final int SIZE_TYPE_AVATAR_LARGE = 3;          //大图：480
    public static final int SIZE_TYPE_AVATAR_XLARGE = 4;         //超大：640

    final static int MAX_CACHE_SIZE = 500;
    private static final LruCache<Long, Long> mAvatarTimeCache = new LruCache<>(MAX_CACHE_SIZE);

    /**
     * 此方法不带时间戳,默认调用传0的方法,尽量使用下面带时间戳的方法
     * 等确定都有事件戳后删除该方法
     */
    @Deprecated
    public static void loadAvatarByUid(final SimpleDraweeView draweeView, final long uid, final boolean isCircle) {
        loadAvatarByUidTs(draweeView, uid, 0, SIZE_TYPE_AVATAR_SMALL, isCircle);
    }

    /**
     * 使用服务器时间戳加载头像,使用此方法,否则以本地事件戳的话,本地缓冲的作用没有什么意义
     */
    public static void loadAvatarByUidTs(final SimpleDraweeView draweeView, final long uid, final long avatarTs, final boolean isCircle) {
        loadAvatarByUidTs(draweeView, uid, avatarTs, SIZE_TYPE_AVATAR_SMALL, isCircle);
    }

    public static void loadAvatarByUidTs(final SimpleDraweeView draweeView, final long uid, final long avatarTs, final int sizeType, final boolean isCircle) {
        loadAvatarByUidTs(draweeView, uid, avatarTs, sizeType, isCircle, false);
    }

    public static void loadAvatarNoLoading(final SimpleDraweeView draweeView, final long uid, final long avatarTs, final int sizeType, final boolean isCircle) {
        loadAvatarByUidTsNoLoading(draweeView, uid, avatarTs, sizeType, isCircle, false);
    }

    public static void loadAvatarByUidTs(final SimpleDraweeView draweeView, final long uid, final long avatarTs, final int sizeType, final boolean isCircle, final boolean isBlur) {
        // 加0保护,采用默认头像
        if (uid == 0) {
            loadAvatarByRes(draweeView, isCircle ? R.drawable.avatar_default_a : R.drawable.avatar_default_b,
                    isCircle, isBlur);
            return;
        }
        String url = getAvatarUrlByUidTs(uid, sizeType, avatarTs);
        loadAvatarByUrl(draweeView, url, isCircle, isBlur, isCircle ? R.drawable.avatar_default_a : R.drawable.avatar_default_b);
    }

    public static void loadAvatarByUidTsNoLoading(final SimpleDraweeView draweeView, final long uid, final long avatarTs, final int sizeType, final boolean isCircle, final boolean isBlur) {
        // 加0保护,采用默认头像
        if (uid == 0) {
            loadAvatarByRes(draweeView, isCircle ? R.drawable.avatar_default_a : R.drawable.avatar_default_b,
                    isCircle, isBlur);
            return;
        }
        String url = getAvatarUrlByUidTs(uid, sizeType, avatarTs);
        loadAvatarByUrl(draweeView, url, isCircle, isBlur, 0);
    }

    public static void loadLiveShowLargeAvatar(final SimpleDraweeView draweeView, final long uid, final long avatarTs, final int sizeType, final boolean isCircle, final boolean isBlur) {
        if (uid == 0) {
            loadAvatarByRes(draweeView, R.drawable.live_show_avatar_loading, isCircle, isBlur);
            return;
        }
        String url = getAvatarUrlByUidTs(uid, sizeType, avatarTs);
        loadAvatarByUrl(draweeView, url, isCircle, isBlur, R.drawable.live_show_avatar_loading);
    }

    /**
     * 构造一个人的头像的BaseImage对象
     *
     * @param uid
     * @param avatarTs
     * @param sizeType
     * @param isCircle
     * @param isBlur
     * @param width
     * @param height
     * @return
     */
    public static BaseImage buildAvatarImage(final long uid, final long avatarTs, final int sizeType, final boolean isCircle, final boolean isBlur, final int width,
                                             final int height) {
        BaseImage avatarImg = null;
        if (uid == 0) {
            return avatarImg;
        }
        String url = getAvatarUrlByUidTs(uid, sizeType, avatarTs);
        return getImage(url, isCircle, isBlur, width, height);
    }

    @NonNull
    private static BaseImage getImage(String url, boolean isCircle, boolean isBlur, int width, int height) {
        BaseImage avatarImg = ImageFactory.newHttpImage(url).setWidth(width).setHeight(height)
                .setIsCircle(isCircle)
                .setFailureDrawable(GlobalData.app().getResources().getDrawable(
                        R.drawable.live_show_avatar_loading))
                .setFailureScaleType(
                        isCircle ? ScalingUtils.ScaleType.CENTER_INSIDE : ScalingUtils.ScaleType.CENTER_CROP)
                .build();
        if (isBlur) {
            avatarImg.setPostprocessor(new BlurPostprocessor());
        } else {
            avatarImg.setLoadingDrawable(GlobalData.app().getResources().getDrawable(
                    R.drawable.live_show_avatar_loading));
            avatarImg.setLoadingScaleType(
                    isCircle ? ScalingUtils.ScaleType.CENTER_INSIDE : ScalingUtils.ScaleType.CENTER_CROP);
        }
        return avatarImg;
    }

    /**
     * 构造滑动切换直播时占位的模糊图像，如果主播设置了封面，则用封面；如果没有，使用主播的头像
     *
     * @param uid
     * @param avatarTs
     * @param coverUrl
     * @param width
     * @param height
     * @return
     */
    public static BaseImage buildBlurPlaceholderImage(long uid, long avatarTs, String coverUrl, int width, int height) {
        BaseImage avatarImg = null;
        if (uid == 0) {
            return avatarImg;
        }
        // LiveShow.getCoverUrlOfXXX()在coverUrl为空字符串的情况下返回不是空，而是@style@XXX
        String url = (!TextUtils.isEmpty(coverUrl) && !coverUrl.startsWith("@") && !coverUrl.startsWith("null")) ?
                coverUrl : getAvatarUrlByUidTs(uid, SIZE_TYPE_AVATAR_SMALL, avatarTs);
        MyLog.d(TAG, "mengban url:" + url + ", coverUrl:" + coverUrl + ", avatarTs:" + avatarTs);
        return getImage(url, false, true, width, height);
    }

    /**
     * 预加载某一个人的头像图片
     *
     * @param uid
     * @param avatarTs
     * @param sizeType
     * @param isCircle
     * @param isBlur
     * @param width
     * @param height
     */
    public static void preLoadAvatar(final long uid, final long avatarTs, final int sizeType, final boolean isCircle, final boolean isBlur, final int width,
                                     final int height) {
        String url = getAvatarUrlByUidTs(uid, sizeType, avatarTs);
        FrescoWorker.preLoadImg(url, isBlur ? new BlurPostprocessor() : null, width, height);
    }

    public static void preLoadImage(@NonNull String imageUrl, boolean isBlur, int width, int height) {
        if (!TextUtils.isEmpty(imageUrl)) {
            FrescoWorker.preLoadImg(imageUrl, isBlur ? new BlurPostprocessor() : null, width, height);
        }
    }

    public static void loadAvatarByUidTsGray(final SimpleDraweeView draweeView, final long uid, final long avatarTs, final boolean isCircle, final boolean isGray) {
        if (null == draweeView) {
            return;
        }
        String url = getAvatarUrlByUidTs(uid, SIZE_TYPE_AVATAR_SMALL, avatarTs);
        BaseImage avatarImg = ImageFactory.newHttpImage(url)
                .setIsCircle(isCircle)
                .setFailureDrawable(GlobalData.app().getResources().getDrawable(
                        isCircle ? R.drawable.avatar_default_a : R.drawable.avatar_default_b))
                .setFailureScaleType(
                        isCircle ? ScalingUtils.ScaleType.CENTER_INSIDE : ScalingUtils.ScaleType.CENTER_CROP)
                .build();
        // 设置模糊,模糊效果不设置加载图
        if (isGray) {
            avatarImg.setPostprocessor(new GrayPostprocessor());
        }
        avatarImg.setLoadingDrawable(GlobalData.app().getResources().getDrawable(
                isCircle ? R.drawable.avatar_default_a : R.drawable.avatar_default_b));
        avatarImg.setLoadingScaleType(
                isCircle ? ScalingUtils.ScaleType.CENTER_INSIDE : ScalingUtils.ScaleType.CENTER_CROP);
        FrescoWorker.loadImage(draweeView, avatarImg);
    }

    public static void loadAvatarByUidTsCorner(final SimpleDraweeView draweeView, final long uid, final long avatarTs, int cornerRadius, int borderColor, float borderSize) {
        if (null == draweeView) {
            return;
        }
        String url = getAvatarUrlByUidTs(uid, SIZE_TYPE_AVATAR_MIDDLE, avatarTs);
        BaseImage avatarImg = ImageFactory.newHttpImage(url)
                .setFailureDrawable(GlobalData.app().getResources().getDrawable(R.drawable.corners_bg_e5e5e5_30px))
                .setFailureScaleType(ScalingUtils.ScaleType.CENTER_CROP).setCornerRadius(cornerRadius)
                .build();
        avatarImg.setLoadingDrawable(GlobalData.app().getResources().getDrawable(R.drawable.corners_bg_e5e5e5_30px));
        avatarImg.setLoadingScaleType(ScalingUtils.ScaleType.CENTER_CROP);
        //avatarImg.setBorderColor(borderColor);
        //avatarImg.setBorderWidth(borderSize);
        FrescoWorker.loadImage(draweeView, avatarImg);
    }

    public static void loadAvatarByUrl(final SimpleDraweeView draweeView, final String url, final boolean isCircle, final boolean isBlur, int loadingAvatarResId) {
//        MyLog.v(TAG, "loadAvatarByUid url = " + url);
        BaseImage avatarImg;
        if (TextUtils.isEmpty(url)) {
            avatarImg = ImageFactory.newResImage(loadingAvatarResId).build();
        } else {
            avatarImg = ImageFactory.newHttpImage(url).setWidth(draweeView.getWidth()).setHeight(draweeView.getHeight())
                    .setIsCircle(isCircle)
                    .setFailureDrawable(loadingAvatarResId > 0 ? GlobalData.app().getResources().getDrawable(
                            loadingAvatarResId) : null)
                    .setFailureScaleType(
                            isCircle ? ScalingUtils.ScaleType.CENTER_INSIDE : ScalingUtils.ScaleType.CENTER_CROP)
                    .build();
        }
        // 设置模糊,模糊效果不设置加载图
        if (isBlur) {
            avatarImg.setPostprocessor(new BlurPostprocessor());
        } else {
            avatarImg.setLoadingDrawable(loadingAvatarResId > 0 ? GlobalData.app().getResources().getDrawable(
                    loadingAvatarResId) : null);
            avatarImg.setLoadingScaleType(
                    isCircle ? ScalingUtils.ScaleType.CENTER_INSIDE : ScalingUtils.ScaleType.CENTER_CROP);
        }
        FrescoWorker.loadImage(draweeView, avatarImg);
    }

    private static void loadAvatarByUrl(final SimpleDraweeView draweeView, final String url, final boolean isCircle, final boolean isBlur, int loadingAvatarResId, int width, int heigh) {
//        MyLog.v(TAG, "loadAvatarByUid url = " + url);
        BaseImage avatarImg;
        if (TextUtils.isEmpty(url)) {
            avatarImg = ImageFactory.newResImage(loadingAvatarResId).build();
        } else {
            avatarImg = ImageFactory.newHttpImage(url).setWidth(width).setHeight(heigh)
                    .setIsCircle(isCircle)
                    .setFailureDrawable(loadingAvatarResId > 0 ? GlobalData.app().getResources().getDrawable(
                            loadingAvatarResId) : null)
                    .setFailureScaleType(
                            isCircle ? ScalingUtils.ScaleType.CENTER_INSIDE : ScalingUtils.ScaleType.CENTER_CROP)
                    .build();
        }
        // 设置模糊,模糊效果不设置加载图
        if (isBlur) {
            avatarImg.setPostprocessor(new BlurPostprocessor());
        } else {
            avatarImg.setLoadingDrawable(loadingAvatarResId > 0 ? GlobalData.app().getResources().getDrawable(
                    loadingAvatarResId) : null);
            avatarImg.setLoadingScaleType(
                    isCircle ? ScalingUtils.ScaleType.CENTER_INSIDE : ScalingUtils.ScaleType.CENTER_CROP);
        }
        FrescoWorker.loadImage(draweeView, avatarImg);
    }

    /**
     * 加载本地图片用作头像
     *
     * @param draweeView
     * @param path
     * @param isCircle
     */
    public static void loadAvatarByLocalImg(final SimpleDraweeView draweeView, final String path, final boolean isCircle) {
        BaseImage avatarImg = null;
        if (TextUtils.isEmpty(path) || !(new File(path).exists()))
            avatarImg = ImageFactory.newResImage(R.drawable.avatar_default_b).build();
        else {
            avatarImg = ImageFactory.newLocalImage(path).setWidth(draweeView.getWidth()).setHeight(draweeView.getHeight())
                    .setIsCircle(isCircle)
                    .setFailureDrawable(GlobalData.app().getResources().getDrawable(R.drawable.avatar_default_b))
                    .setFailureScaleType(isCircle ? ScalingUtils.ScaleType.CENTER_INSIDE : ScalingUtils.ScaleType.CENTER_CROP).build();
        }
        FrescoWorker.loadImage(draweeView, avatarImg);
    }

    public static void loadAvatarByUrl(final SimpleDraweeView draweeView, final String url, boolean isCircle) {
        loadAvatarByUrl(draweeView, url, isCircle, false, R.drawable.avatar_default_b);
    }

    public static void loadCoverByUrl(final SimpleDraweeView draweeView, final String url, boolean isCircle) {
        loadAvatarByUrl(draweeView, url, isCircle, false, R.drawable.live_show_avatar_loading);
    }

    public static void loadCoverByUrl(final SimpleDraweeView draweeView, final String url, boolean isCircle, int loadingAvatarResId) {
        loadAvatarByUrl(draweeView, url, isCircle, false, loadingAvatarResId);
    }

    public static void loadCoverByUrl(final SimpleDraweeView draweeView, final String url, boolean isCircle, int loadingAvatarResId, int width, int heigh) {
        loadAvatarByUrl(draweeView, url, isCircle, false, loadingAvatarResId, width, heigh);
    }

    private static void loadAvatarByRes(final SimpleDraweeView draweeView, final int resId, final boolean isCircle, final boolean isBlur) {
        MyLog.v(TAG, "loadAvatarByRes");
        BaseImage avatarImg = ImageFactory.newResImage(resId)
                .setIsCircle(isCircle)
                .build();
        // 设置模糊
        if (isBlur) {
            avatarImg.setPostprocessor(new BlurPostprocessor());
        }
        FrescoWorker.loadImage(draweeView, avatarImg);
    }

    @Deprecated
    public static String getAvatarUrlByUid(long uid, int sizeType) {
        return getAvatarUrlByUidTs(uid, sizeType, 0);
    }

    public static String getAvatarUrlByUid(long uid, long timestamp) {
        return getAvatarUrlByUidTs(uid, AvatarUtils.SIZE_TYPE_AVATAR_SMALL, timestamp);
    }

    public static String getAvatarUrlByUidTs(long uid, int sizeType, long timestamp) {
        return getAvatarUrlByUidTsAndFormat(uid, sizeType, timestamp, false);
    }

    /**
     * @param uid
     * @param sizeType
     * @param timestamp
     * @param isWebpFormat 因为fresco加载webp时会有问题这里不要传true
     * @return
     */
    public static String getAvatarUrlByUidTsAndFormat(long uid, int sizeType, long timestamp, boolean isWebpFormat) {
        Long cacheTimeStamp = mAvatarTimeCache.get(uid);
        if (null != cacheTimeStamp) {
            if (timestamp == 0 && cacheTimeStamp.longValue() > 0) {
                timestamp = cacheTimeStamp;
            }
            if (timestamp > cacheTimeStamp) {
                mAvatarTimeCache.put(uid, timestamp);
                if (mAvatarTimeCache.size() > MAX_CACHE_SIZE * 1.5f) {
                    MyLog.e(TAG, "mAvatarTimeCache.size is large than 150, evictAll");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        mAvatarTimeCache.trimToSize(MAX_CACHE_SIZE);
                    }
                }
            }
        } else if (timestamp > 0) {
            mAvatarTimeCache.put(uid, timestamp);
        }
        if (timestamp == 0) {
            MyLog.d(TAG, String.format("getAvatarUrlByUidTs %d timestamp is 0", uid));
            return String.format(Constants.AVATAR_DEFAULT_URL, uid, isWebpFormat ? getAvatarSizeAppend(sizeType) : getJpgAvatarSizeAppend(sizeType));
        }
        return String.format(Constants.AVATAR_URL, uid, isWebpFormat ? getAvatarSizeAppend(sizeType) : getJpgAvatarSizeAppend(sizeType), timestamp);
    }

    /**
     * 根据sizeType 返回url里的 size append part
     */
    public static String getAvatarSizeAppend(int sizeType) {
        String appendPart = "";
        switch (sizeType) {
            case SIZE_TYPE_AVATAR_SOURCE:
                appendPart = ImageUtils.getSimplePart(0);
                break;
            case SIZE_TYPE_AVATAR_SMALL:
                appendPart = ImageUtils.getSimplePart(160);
                break;
            case SIZE_TYPE_AVATAR_MIDDLE:
                appendPart = ImageUtils.getSimplePart(320);
                break;
            case SIZE_TYPE_AVATAR_LARGE:
                appendPart = ImageUtils.getSimplePart(480);
                break;
            case SIZE_TYPE_AVATAR_XLARGE:
                appendPart = ImageUtils.getSimplePart(640);
                break;
            default:
                break;
        }
        return appendPart;
    }

    /**
     * 根据sizeType 返回 jpg url里的 size append part
     */
    public static String getJpgAvatarSizeAppend(int sizeType) {
        String appendPart = "";
        switch (sizeType) {
            case SIZE_TYPE_AVATAR_SOURCE:
                appendPart = ImageUtils.getJpgSimplePart(0);
                break;
            case SIZE_TYPE_AVATAR_LARGE:
                appendPart = ImageUtils.getJpgSimplePart(480);
                break;
            case SIZE_TYPE_AVATAR_MIDDLE:
                appendPart = ImageUtils.getJpgSimplePart(320);
                break;
            case SIZE_TYPE_AVATAR_SMALL:
                appendPart = ImageUtils.getJpgSimplePart(160);
                break;
            default:
                break;
        }
        return appendPart;
    }

    public static String getAvatarFromLargeToSmall(final long userId, long timestamp) {
        String imgUrl = "";
        File file;

        imgUrl = AvatarUtils.getAvatarUrlByUidTs(userId, AvatarUtils.SIZE_TYPE_AVATAR_LARGE, timestamp);
        file = FrescoWorker.getCacheFileFromFrescoDiskCache(imgUrl);
        if (file != null) {
            return file.getPath();
        }
        imgUrl = AvatarUtils.getAvatarUrlByUidTs(userId, AvatarUtils.SIZE_TYPE_AVATAR_MIDDLE, timestamp);
        file = FrescoWorker.getCacheFileFromFrescoDiskCache(imgUrl);
        if (file != null) {
            return file.getPath();
        }
        imgUrl = AvatarUtils.getAvatarUrlByUidTs(userId, AvatarUtils.SIZE_TYPE_AVATAR_SMALL, timestamp);
        file = FrescoWorker.getCacheFileFromFrescoDiskCache(imgUrl);
        if (file != null) {
            return file.getPath();
        }
        return "";
    }

    /**
     * 更新我关注的人
     *
     * @param uuid
     * @param timestamp
     */
    public static void updateMyFollowAvatarTimeStamp(long uuid, long timestamp) {
        mAvatarTimeCache.put(uuid, timestamp);
    }

    public static String getImgUrlByAvatarSize(String url, int sizeType) {
        if (!TextUtils.isEmpty(url)) {
            return url + getAvatarSizeAppend(sizeType);
        }
        return url;
    }

    public static String getCoverUrlOf480(String coverUrl) {
        //coverUrl为空则不拼接。
        if (TextUtils.isEmpty(coverUrl)) {
            return "";
        }
        return coverUrl + AvatarUtils.getAvatarSizeAppend(AvatarUtils.SIZE_TYPE_AVATAR_LARGE);
    }

    public static String getCoverUrlOf320(String in) {
        if (TextUtils.isEmpty(in)) {
            return "";
        }

        return in + AvatarUtils.getAvatarSizeAppend(AvatarUtils.SIZE_TYPE_AVATAR_MIDDLE);
    }

    public static String getCoverUrlOf160(String in) {
        if (TextUtils.isEmpty(in)) {
            return "";
        }

        return in + AvatarUtils.getAvatarSizeAppend(AvatarUtils.SIZE_TYPE_AVATAR_SMALL);
    }
}
