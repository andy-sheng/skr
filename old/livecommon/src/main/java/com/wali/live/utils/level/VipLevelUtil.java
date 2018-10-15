package com.wali.live.utils.level;

import android.support.annotation.DrawableRes;
import android.support.v4.util.Pair;

import com.base.log.MyLog;
import com.mi.live.data.R;

/**
 * Created by guoxiao on 17-5-2.
 */

public class VipLevelUtil {
    private static final String TAG = VipLevelUtil.class.getSimpleName();
    public static final int MAX_LEVEL_IMAGE_NO = 7;//最大等级图片，超过之后使用live_vip_7图片
    private static final String LEVEL_NAME_PREFIX = "live_vip_";
    private static final String LEVEL_DISABLE_SUFFIX = "_disable";

    /**
     * 获取VIP等级对应资源ID
     *
     * @param level VIP等级
     * @return Pair {@code <有无结果， 资源ID>}
     */
    public static Pair<Boolean, Integer> getLevelBadgeResId(int level, boolean isFrozen, boolean showFrozen) {
        if (level <= 0 || (isFrozen && !showFrozen)) {
            return Pair.create(false, -1);
        }
        int index = level > MAX_LEVEL_IMAGE_NO ? MAX_LEVEL_IMAGE_NO : level;
        if (index <= 0) {
            return Pair.create(false, -1);
        }
        String rName = LEVEL_NAME_PREFIX + index;
        if (isFrozen && showFrozen) {
            rName = rName + LEVEL_DISABLE_SUFFIX;
        }
        //反射获取
        try {
            @DrawableRes int drawableId = (Integer) R.drawable.class.getField(rName).get(null);
            return Pair.create(true, drawableId);
        } catch (NoSuchFieldException e) {
            MyLog.e(TAG, "can not found image :" + rName, e);
        } catch (IllegalAccessException e) {
            MyLog.e(TAG, e);
        }
        return Pair.create(false, -1);
    }

}
