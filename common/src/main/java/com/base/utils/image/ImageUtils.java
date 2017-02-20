package com.base.utils.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.base.log.MyLog;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by chengsimin on 16/9/12.
 */
public class ImageUtils {
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
}
