package com.wali.live.livesdk.live.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.base.log.MyLog;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Created by chenyong on 2017/2/14.
 */

public class ImageUtils {
    public static final String AVATAR_TEMP_DIR = "/Xiaomi/WALI_LIVE_SDK/.temp/";      //临时文件路径, 比如裁剪后的图片临时文件存放在这里

    public static Bitmap readArgbtmap(Context context, int resId) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
        // 获取资源图片
        InputStream is = context.getResources().openRawResource(resId);
        return BitmapReader.decodeBmpFromStream(is, opt);
    }

    public static void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    public static byte[] bitmap2Byte(Bitmap bitmap) {
        int height = bitmap.getHeight();
        int size = bitmap.getRowBytes() * height;
        ByteBuffer b = ByteBuffer.allocate(size);
        bitmap.copyPixelsToBuffer(b);
        return b.array();
    }

    public static boolean saveToFile(Bitmap bitmap, String path) {
        return saveToFile(bitmap, path, false);
    }

    public static boolean saveToFile(Bitmap bitmap, String path, boolean saveToPng) {
        try {
            if (bitmap != null) {
                FileOutputStream outputStream = new FileOutputStream(path);
                bitmap.compress(saveToPng ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.close();
                return true;
            }
        } catch (Exception e) {
            MyLog.e("ImageUtils", "saveToFile e=" + e);
        }
        return false;
    }
}
