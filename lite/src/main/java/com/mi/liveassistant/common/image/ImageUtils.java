package com.mi.liveassistant.common.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.mi.liveassistant.common.log.MyLog;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Created by chengsimin on 16/9/12.
 */
public class ImageUtils {
    public static final String AVATAR_TEMP_DIR = "/Xiaomi/WALI_LIVE_SDK/.temp/";      //临时文件路径, 比如裁剪后的图片临时文件存放在这里

    public static Bitmap getLocalBitmap(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }

        Bitmap bm = null;
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(path.replace("file://", ""));
        } catch (Exception e) {
            MyLog.e(e);
        } finally {

        }

        if (inputStream == null) {
            return null;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inBitmap
        //options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inInputShareable = true;
        options.inPurgeable = true;
        // 当inJustDecodeBounds设为false,加载图片到内存
        //options.inJustDecodeBounds = false;
        bm = BitmapFactory.decodeStream(inputStream, null, options);
        try {
            inputStream.close();
        } catch (IOException e) {
            MyLog.e(e);
        }
        return bm;
    }

    /**
     * RGB565 to ARGB8888
     */
    public static Bitmap convertRGB565ToARGB8888(Bitmap img, boolean recycle) {
        int numPixels = img.getWidth() * img.getHeight();
        int[] pixels = new int[numPixels];
        img.getPixels(pixels, 0, img.getWidth(), 0, 0, img.getWidth(), img.getHeight());
        Bitmap result = Bitmap.createBitmap(img.getWidth(), img.getHeight(), Bitmap.Config.ARGB_8888);
        result.setPixels(pixels, 0, result.getWidth(), 0, 0, result.getWidth(), result.getHeight());
        if (recycle) {
            img.recycle();
        }
        return result;
    }

    /**
     * 使用StringFormat拼接，url记得使用%s
     */
    public static String getImageUrlByFormat(String url, int width, int height) {
        if (!TextUtils.isEmpty(url)) {
            return String.format(url, getAppendPart(width, height));
        }
        return url;
    }

    /**
     * 使用Append拼接在尾部
     */
    public static String getImageUrlByAppend(String url, int width, int height) {
        if (!TextUtils.isEmpty(url)) {
            StringBuilder sb = new StringBuilder();
            sb.append(url).append(getAppendPart(width, height));
            return sb.toString();
        }
        return url;
    }

    /**
     * 金山云原始拼接方法
     * 具体拼接规则
     * http://ks3.ksyun.com/doc/imghandle/api/handle.html
     */
    public static String getAppendPart(int width, int height) {
        StringBuilder sb = new StringBuilder();
        sb.append("@base@tag=imgScale&m=1&c=1&w=").append(width).append("&h=").append(height);
        return sb.toString();
    }

    /**
     * 简化拼接方法
     * 具体拼接规则
     * http://wiki.n.miui.com/pages/viewpage.action?pageId=18998589
     * 0 : 原始webp地址
     * 160 : 160尺寸地址
     * 320 : 320尺寸地址
     * 480 : 480尺寸地址
     */
    public static String getSimplePart(int dimenSize) {
        StringBuilder sb = new StringBuilder();
        sb.append("@style@").append(dimenSize == 0 ? "original" : String.valueOf(dimenSize));
        return sb.toString();
    }

    public static String getJpgSimplePart(int dimenSize) {
        StringBuilder sb = new StringBuilder();
        if (dimenSize == 0) {
            return "";
        }
        sb.append("@style@").append(String.valueOf(dimenSize)).append("jpg");
        return sb.toString();
    }

    public static Bitmap readArgbBitmap(Context context, int resId) {
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
