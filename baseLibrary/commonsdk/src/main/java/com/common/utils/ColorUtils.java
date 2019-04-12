package com.common.utils;

import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;

/**
 * 颜色处理类
 *
 * @author zhangliangming
 */
public class ColorUtils {

    ColorUtils() {

    }

    /**
     * 解析颜色
     *
     * @param colorStr #ffffff 颜色字符串
     * @param alpha    0-255 透明度
     * @return
     */
    public int parserColor(String colorStr, int alpha) {
        int color = Color.parseColor(colorStr);
        int red = (color & 0xff0000) >> 16;
        int green = (color & 0x00ff00) >> 8;
        int blue = (color & 0x0000ff);
        return Color.argb(alpha, red, green, blue);
    }

    /**
     * 解析颜色
     *
     * @param color Color.WHITE
     * @param alpha 0-255 透明度
     * @return
     */
    public int parserColor(int color, int alpha) {
        int red = (color & 0xff0000) >> 16;
        int green = (color & 0x00ff00) >> 8;
        int blue = (color & 0x0000ff);
        return Color.argb(alpha, red, green, blue);
    }

    public int parserColor(int color) {
        int red = (color & 0xff0000) >> 16;
        int green = (color & 0x00ff00) >> 8;
        int blue = (color & 0x0000ff);
        return Color.argb(255, red, green, blue);
    }


    /**
     * 解析颜色
     *
     * @param colorStr #ffffff 颜色字符串
     * @return
     */
    public int parserColor(String colorStr) {
        return Color.parseColor(colorStr);
    }


    public static int toDarkenColor(@ColorInt int color, @FloatRange(from = 0f, to = 1f) float value) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= value;//HSV指Hue、Saturation、Value，即色调、饱和度和亮度，此处表示修改亮度
        return Color.HSVToColor(hsv);
    }
}
