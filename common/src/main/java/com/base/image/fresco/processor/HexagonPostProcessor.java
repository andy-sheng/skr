package com.base.image.fresco.processor;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

import com.facebook.imagepipeline.nativecode.Bitmaps;
import com.facebook.imagepipeline.request.BasePostprocessor;

/**
 * Created by liuyanyan on 2017/12/8.
 * @module vip入场动画
 * @description 头像处理为六边形
 */

public class HexagonPostProcessor extends BasePostprocessor {

    @Override
    public void process(Bitmap destBitmap, Bitmap sourceBitmap) {

        int width = sourceBitmap.getWidth();
        int height = sourceBitmap.getHeight();
        Bitmap.Config localConfig = Bitmap.Config.ARGB_8888;
        Bitmap localBitmap = Bitmap.createBitmap(width, height, localConfig);
        Canvas localCanvas = new Canvas(localBitmap);
        Paint localPaint = new Paint(1);
        localPaint.setColor(Color.BLACK);
        float centerX = width / 2;
        float centerY = height / 2;
        float radius = width / 2;
        double radian30 = 30 * Math.PI / 180;
        float a = (float) (radius * Math.sin(radian30));
        float b = (float) (radius * Math.cos(radian30));

        Path localPath = new Path();
        localPath.moveTo(centerX, 0);
        localPath.lineTo(centerX + b, centerY - a);
        localPath.lineTo(centerX + b, centerY + a);
        localPath.lineTo(centerX, height);
        localPath.lineTo(centerX - b, centerY + a);
        localPath.lineTo(centerX - b, centerY - a);
        localPath.close();
        localCanvas.drawPath(localPath, localPaint);

        localPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        localCanvas.drawBitmap(sourceBitmap, 0, 0, localPaint);

        Bitmaps.copyBitmap(destBitmap, localBitmap);
    }
}
