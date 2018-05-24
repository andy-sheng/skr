package com.wali.live.common.barrage.view.utils;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.base.log.MyLog;

/**
 * Created by anping on 17/2/17.
 */

public class CommentVfansLevelCache {

    private static LruCache<String, Bitmap> sLevelCache = null;

    private static final int LEVEL_CACHE_MAX_SIZE = 50 * 1024;

    public static Bitmap getLevelOrLike(String level) {
        if (sLevelCache != null && sLevelCache.size() > 0) {
            return sLevelCache.get(level);
        }
        return null;
    }

    public static void clear() {
        if (sLevelCache != null) {
            if (sLevelCache.size() > 0) {
                sLevelCache.evictAll();
            }
            sLevelCache = null;
        }
    }

    public static void setLevelOrLike(String level, Bitmap bitmap) {
        if (sLevelCache == null) {
            sLevelCache = new LruCache<String, Bitmap>(LEVEL_CACHE_MAX_SIZE) {
                protected int sizeOf(String key, Bitmap value) {
                    int size = value.getHeight() * value.getRowBytes();
                    MyLog.v("testLevelSize" + size);
                    return size;
                }
            };
        }
        sLevelCache.put(level, bitmap);
    }
}
