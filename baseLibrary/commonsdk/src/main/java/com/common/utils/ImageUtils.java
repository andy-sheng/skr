package com.common.utils;

import android.graphics.BitmapFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageUtils {
    ImageUtils() {
    }

    /**
     * 运营位显示图片时先去网络获取图片的宽高信息
     *
     * @param url 图片地址
     * @return 宽，高
     */
    public static Integer[] getImageWidthAndHeightFromNetwrok(String url) {
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
}
