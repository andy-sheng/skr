package com.wali.live.watchsdk.bigturntable.manager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.NinePatch;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Environment;
import android.support.v4.util.LruCache;
import android.text.TextUtils;

import com.base.log.MyLog;
import com.base.utils.MD5;
import com.base.utils.sdcard.SDCardUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by zhujianning on 18-4-20.
 */

public class LoadDrawableManager {
    private static final String TAG = "LoadDrawableManager";
    private static final int MAX_MAP_SIZE = 600 * 1024;

    private static LoadDrawableManager sInstance;

    public static String BIG_TURN_TABLE_DIR = Environment.getExternalStorageDirectory() + "/Xiaomi/WALI_LIVE/turnTable";

    private LruCache<String, Drawable> mCacheMap;

    public static synchronized LoadDrawableManager getInstance() {
        boolean firstIn = false;
        if (sInstance == null) {
            synchronized (LoadDrawableManager.class) {
                firstIn = true;
                sInstance = new LoadDrawableManager();
            }
        }
        return sInstance;
    }

    public Drawable getDrawableFromCacheMap(String key) {
        if (mCacheMap != null && !TextUtils.isEmpty(key)) {
            return mCacheMap.get(key);
        }
        return null;
    }


    public void setDrawableToCacheMap(String key, Drawable drawable) {
        if (mCacheMap == null) {
            mCacheMap = new LruCache<String, Drawable>(MAX_MAP_SIZE) {
                protected int sizeOf(String key, Drawable value) {
                    if (value instanceof BitmapDrawable) {
                        BitmapDrawable bitmapDrawable = (BitmapDrawable) value;
                        Bitmap bitmap = bitmapDrawable.getBitmap();
                        int size = bitmap.getHeight() * bitmap.getRowBytes();
                        MyLog.v("testMedalSize" + size);
                        return size;
                    }
                    return 20;
                }
            };
        }

        if (!TextUtils.isEmpty(key) && drawable != null && mCacheMap.get(key) != null) {
            mCacheMap.put(key, drawable);
        }
    }

    public void clearMap() {
        if (mCacheMap != null) {
            if (mCacheMap.size() > 0) {
                mCacheMap.evictAll();
            }
            mCacheMap = null;
        }
    }

    public static Drawable getDrawableFromServer(String path) throws Exception {
        MyLog.d(TAG, "path:" + path);
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        File dir = null;
        dir = new File(BIG_TURN_TABLE_DIR);

        if (!dir.exists()) {
            dir.mkdirs(); // 创建文件夹
        }

        //根据服务器返回是否存在后缀构造路径
        String name;
        name = MD5.MD5_16(path);
        File file = null;
        file = new File(BIG_TURN_TABLE_DIR + "/" + name);
        MyLog.d(TAG, "file" + file.getPath());
        // 如果图片存在本地缓存目录，则不去服务器下载
        if (file.exists()) {
            return file2Drawable(file);
        } else {
            // 从网络上获取图片
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");
            conn.setUseCaches(false);
            conn.setDoInput(true);
            if (conn.getResponseCode() == 200) {
                InputStream is = conn.getInputStream();
                FileOutputStream fos = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
                is.close();
                fos.close();
                return file2Drawable(file);
            }
        }
        return null;
    }

    public String getKey(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }

        File dir = new File(BIG_TURN_TABLE_DIR);
        if (!dir.exists()) {
            dir.mkdirs(); // 创建文件夹
        }

        String name = MD5.MD5_16(url);
        File file = new File(BIG_TURN_TABLE_DIR + "/" + name);

        return file.getPath();
    }

    public static Drawable file2Drawable(File file) {
        if (file != null && file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
            byte[] chunk = bitmap.getNinePatchChunk();
            boolean result = NinePatch.isNinePatchChunk(chunk);
            if (result) {
                return new NinePatchDrawable(bitmap, chunk, new Rect(), null);
            } else {
                return new BitmapDrawable(bitmap);
            }
        }
        return null;
    }

    public Drawable getDrawableByUrl(String url) {
        Drawable drawable = null;
        if (!TextUtils.isEmpty(url)) {
            drawable = getDrawableFromCacheMap(getKey(url));

            if (drawable == null) {
                try {
                    drawable = getDrawableFromServer(url);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (drawable != null) {
                    setDrawableToCacheMap(getKey(url), drawable);
                }
            }

        }
        return drawable;
    }
}
