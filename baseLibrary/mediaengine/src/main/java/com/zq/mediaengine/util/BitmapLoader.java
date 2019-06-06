package com.zq.mediaengine.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.InputStream;

/**
 * Util class to load Bitmap from file.
 */

public class BitmapLoader {
    private final static String TAG = "BitmapLoader";
    private final static int MAX_PIC_LEN = 2048;

    public static Bitmap loadBitmap(Context context, String uri) {
        return loadBitmap(context, uri, MAX_PIC_LEN, MAX_PIC_LEN);
    }

    public static Bitmap loadBitmap(Context context, String uri, int maxWidth, int maxHeight) {
        String path;
        String filePrefix = "file://";
        String assetsPrefix = "assets://";
        Bitmap origin;
        Bitmap scaled;

        if (context == null || uri == null || uri.isEmpty()) {
            Log.e(TAG, "loadBitmap " + uri + " failed!");
            return null;
        }

        if (uri.startsWith(filePrefix)) {
            path = uri.substring(filePrefix.length());
            origin = BitmapFactory.decodeFile(path);
        } else if (uri.startsWith(assetsPrefix)) {
            path = uri.substring(assetsPrefix.length());
            origin = loadAssets(context, path);
        } else {
            path = uri;
            origin = BitmapFactory.decodeFile(path);
            if (origin == null) {
                origin = loadAssets(context, path);
            }
        }

        if (origin == null) {
            Log.e(TAG, "loadBitmap " + uri + " failed!");
            return null;
        }

        if (maxWidth == 0 && maxHeight == 0) {
            if (origin.getWidth() > origin.getHeight()) {
                maxWidth = MAX_PIC_LEN;
            } else {
                maxHeight = MAX_PIC_LEN;
            }
        }

        // scale bitmap
        scaled = origin;
        if ((origin.getWidth() > maxWidth && maxWidth > 0) ||
                (origin.getHeight() > maxHeight && maxHeight > 0)) {
            int w, h;
            if (origin.getWidth() > maxWidth && maxWidth > 0) {
                w = maxWidth;
                h = w * origin.getHeight() / origin.getWidth();
            } else {
                h = maxHeight;
                w = h * origin.getWidth() / origin.getHeight();
            }
            w = w / 2 * 2;
            h = h / 2 * 2;
            scaled = Bitmap.createScaledBitmap(origin, w, h, true);
            origin.recycle();
        }
        return scaled;
    }

    private static Bitmap loadAssets(Context context, String path) {
        InputStream stream = null;
        try {
            stream = context.getAssets().open(path);
            return BitmapFactory.decodeStream(stream);
        } catch (Exception ignored) {
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
