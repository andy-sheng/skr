package com.wali.live.common.gift.utils;

import android.graphics.drawable.Drawable;

import com.mi.live.data.config.GetConfigManager;

/**
 * Created by zjn on 16-12-1.
 */
public class DataformatUtils {

    public static Drawable getCertificationImgSource(int certificationType) {
        if (certificationType > 0) {
            return GetConfigManager.getInstance().getCertificationTypeDrawable(certificationType).certificationDrawable;
        } else {
            return null;
        }
    }

    public static Drawable getLevelSmallImgSource(int level) {
        if (level >= 0) {
            return GetConfigManager.getInstance().getLevelSmallDrawable(level);
        } else {
            return null;
        }
    }
}
