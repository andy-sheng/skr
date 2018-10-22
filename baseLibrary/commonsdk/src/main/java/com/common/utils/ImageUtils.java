package com.common.utils;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageUtils {

    public static final String IMG_URL_POSTFIX = "@style@"; //img  url 的后缀,如果有后缀,不再去拼接@style

    ImageUtils() {
    }

    /**
     * 运营位显示图片时先去网络获取图片的宽高信息
     *
     * @param url 图片地址
     * @return 宽，高
     */
    public Integer[] getImageWidthAndHeightFromNetwrok(String url) {
        Integer option[] = new Integer[2];
        try {
            URL m_url = new URL(url);
            HttpURLConnection con = (HttpURLConnection) m_url.openConnection();
            InputStream in = con.getInputStream();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, options);

            int height = options.outHeight;
            int width = options.outWidth;
            option[0] = width;
            option[1] = height;
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return option;
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
    public String getSizeSuffix(SIZE dimenSize) {
        StringBuilder sb = new StringBuilder();
        sb.append("@style@");
        switch (dimenSize){
            case ORIGIN:
                sb.append("original");
                break;
            case SIZE_160:
                sb.append("160");
                break;
            case SIZE_320:
                sb.append("320");
                break;
            case SIZE_480:
                sb.append("480");
                break;
            case SIZE_640:
                sb.append("640");
                break;
        }
        return sb.toString();
    }

    public static String getSizeSuffixJpg(SIZE dimenSize) {
        StringBuilder sb = new StringBuilder();
        sb.append("@style@");
        switch (dimenSize){
            case ORIGIN:
                return "";
            case SIZE_160:
                sb.append("160");
                break;
            case SIZE_320:
                sb.append("320");
                break;
            case SIZE_480:
                sb.append("480");
                break;
            case SIZE_640:
                sb.append("640");
                break;
        }
        sb.append("jpg");
        return sb.toString();
    }

    public  enum SIZE{
        ORIGIN,SIZE_160,SIZE_320,SIZE_480,SIZE_640
    }

    /**
     * 通知系统相册有变化，触发相册表刷新
     * @param file
     */
    public void notifyGalleryChangeByBroadcast(File file) {
        if(file!=null){
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(file);
            mediaScanIntent.setData(contentUri);
            U.app().sendBroadcast(mediaScanIntent);
        }
    }
}
