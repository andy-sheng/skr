package com.common.image.fresco.processor;

import android.graphics.Bitmap;

import com.facebook.imagepipeline.request.BasePostprocessor;

/**
 * 灰色
 * Created by yurui on 4/5/16.
 */
public class GrayPostprocessor extends BasePostprocessor {

    @Override
    public void process(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int []pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        int alpha = 0xFF << 24;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];
                int red = ((grey & 0x00FF0000) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);
                grey = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
                grey = getMixColor(grey, 0xFF, 0.3f);//蒙上30%的白色
                grey = alpha | (grey << 16) | (grey << 8) | grey;
                pixels[width * i + j] = grey;
            }
        }
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    public int getMixColor(int c0, int c1, float a1) {
        return (int) (c0 * (1 - a1) + c1 * a1);
    }
}