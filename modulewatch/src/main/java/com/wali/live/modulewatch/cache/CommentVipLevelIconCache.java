package com.wali.live.modulewatch.cache;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.view.View;

import com.common.utils.U;
import com.wali.live.modulewatch.utils.VipLevelUtil;

/**
 * Created by guoxiao on 17-5-2.
 */

public class CommentVipLevelIconCache {
    private static final int LEVEL_CACHE_MAX_SIZE = 50 * 1024;//KB
    private static LruCache<Integer, Bitmap> sVipLevelIconCache = new LruCache<Integer, Bitmap>(LEVEL_CACHE_MAX_SIZE) {
        @Override
        protected int sizeOf(Integer key, Bitmap value) {
            int size = value.getHeight() * value.getRowBytes();
            return size;
        }
    };

    public static Bitmap getVipLevelIconBitmap(int level, View view) {
        level = level > VipLevelUtil.MAX_LEVEL_IMAGE_NO ? VipLevelUtil.MAX_LEVEL_IMAGE_NO : level;
        Bitmap levelBitmap = getLevelIcon(level);
        if (null == levelBitmap) {
            levelBitmap = U.getBitmapUtils().convertViewToBitmap(view);
            setLevelIcon(level, levelBitmap);

        }
        return levelBitmap;

    }

    private static Bitmap getLevelIcon(int level) {
        return sVipLevelIconCache.get(level);
    }

    private static void setLevelIcon(int key, Bitmap bitmap) {
        sVipLevelIconCache.put(key, bitmap);
    }

    public static void clear() {
        if (sVipLevelIconCache != null) {
            if (sVipLevelIconCache.size() > 0) {
                sVipLevelIconCache.evictAll();
            }
        }
    }
}
