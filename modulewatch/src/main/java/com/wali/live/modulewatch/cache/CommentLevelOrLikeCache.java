package com.wali.live.modulewatch.cache;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

/**
 * Created by anping on 17/2/17.
 */

public class CommentLevelOrLikeCache {

    private static LruCache<String, Bitmap> sLevelCache = null;

    private static final int LEVEL_CACHE_MAX_SIZE = 150 * 1024;

    public static Bitmap getLevelOrLike(String level) {
        if (sLevelCache != null && sLevelCache.size() > 0) {

//                Map<String,Bitmap> datas  = sLevelCache.snapshot();
//                Set<String> keys = datas.keySet();
//                int count = 0;
//                for(String key:keys){
//                    Bitmap bitmap = datas.get(key);
//                    count = bitmap.getByteCount() + count;
//                }
//                MyLog.v("testCacheSize:"+count);

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
                    return size;
                }
            };
        }
        sLevelCache.put(level, bitmap);
    }
}
