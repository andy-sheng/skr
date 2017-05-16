package com.mi.liveassistant.avatar;

import com.mi.liveassistant.common.image.ImageUtils;
import com.mi.liveassistant.common.log.MyLog;

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

    public static String getAvatarUrlByUidTs(long uid, long timestamp) {
        return getAvatarUrlByUidTs(uid, AvatarUtils.SIZE_TYPE_AVATAR_SMALL, timestamp);
    }

    public static String getAvatarUrlByUidTs(long uid, int sizeType, long timestamp) {
        if (timestamp == 0) {
            MyLog.d(TAG, String.format("getAvatarUrlByUidTs %d timestamp is 0", uid));
        }
        return String.format(AvatarConstants.AVATAR_URL, uid, getAvatarSizeAppend(sizeType), timestamp);
    }

    /**
     * 根据sizeType 返回url里的size append part
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
}
