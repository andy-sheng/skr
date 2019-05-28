package com.common.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;

import com.common.log.MyLog;
import com.glidebitmappool.BitmapFactoryAdapter;
import com.glidebitmappool.BitmapPoolAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BitmapUtils {
    BitmapUtils() {
    }

    /**
     * 获取图片的旋转角度
     *
     * @param path 图片绝对路径
     * @return 图片的旋转角度
     */
    public int getBitmapDegree(String path) {
        int degree = 0;
        try {
            // 从指定路径下读取图片，并获取其EXIF信息
            ExifInterface exifInterface = new ExifInterface(path);
            // 获取图片的旋转信息
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            MyLog.e(e);
        }
        return degree;
    }

    /**
     * 将图片按照指定的角度进行旋转
     *
     * @param bitmap 需要旋转的图片
     * @param degree 指定的旋转角度
     * @return 旋转后的图片
     */
    public Bitmap rotateBitmapByDegree(Bitmap bitmap, int degree) {
        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (!bitmap.isRecycled()) {
            BitmapPoolAdapter.putBitmap(bitmap);
        }
        return newBitmap;
    }

    /**
     * 将图片按照指定的角度进行旋转
     *
     * @param path   需要旋转的图片的路径
     * @param degree 指定的旋转角度
     * @return 旋转后的图片
     */
    public Bitmap rotateBitmapByDegree(String path, int degree) {
        Bitmap bitmap = BitmapFactoryAdapter.decodeFile(path);
        return rotateBitmapByDegree(bitmap, degree);
    }

    /**
     * 获取我们需要的整理过旋转角度的Uri
     *
     * @param activity 上下文环境
     * @param path     路径
     * @return 正常的Uri
     */
    public Uri getRotatedUri(Activity activity, String path) {
        int degree = getBitmapDegree(path);
        if (degree != 0) {
            Bitmap bitmap = BitmapFactoryAdapter.decodeFile(path);
            Bitmap newBitmap = rotateBitmapByDegree(bitmap, degree);
            return Uri.parse(MediaStore.Images.Media.insertImage(activity.getContentResolver(), newBitmap, null, null));
        } else {
            return Uri.fromFile(new File(path));
        }
    }

    /**
     * 根据路径生成Bitmap
     *
     * @param path
     * @return
     */
    public Bitmap getLocalBitmap(String path) {
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
     * 根据视图生成Bitmap
     *
     * @param view
     * @return
     */
    public Bitmap convertViewToBitmap(View view) {
        if (view == null) {
            MyLog.d("convertViewToBitmap", "view == null");
            return null;
        }
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        if (bitmap == null) {
            MyLog.d("convertViewToBitmap", "bitmap == null");
        } else {
            bitmap = Bitmap.createBitmap(bitmap);
        }
        view.setDrawingCacheEnabled(false);
        return bitmap;
    }

    /**
     * 将 bitmap 的颜色 改成 tintColor
     * @param inBitmap
     * @param tintColor
     * @return
     */
    public Bitmap tintBitmap(Bitmap inBitmap, int tintColor) {
        if (inBitmap == null) {
            return null;
        }
        Bitmap outBitmap = BitmapPoolAdapter.getBitmap(inBitmap.getWidth(), inBitmap.getHeight(), inBitmap.getConfig());
        Canvas canvas = new Canvas(outBitmap);
        Paint paint = new Paint();
        paint.setColorFilter(new PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(inBitmap, 0, 0, paint);
        return outBitmap;
    }

    /**
     * 使用ARGB(255,0,0,0)也就是黑色替换bitmap中的0x616161，容忍度为50范围的颜色
     * @param crossimage
     * @return
     */
//    private Bitmap handleCrossImageBack(Bitmap crossimage){
//        // start with a Bitmap bmp
//        Bitmap newBmp = crossimage.copy(Bitmap.Config.ARGB_8888, true);
//        Canvas c = new Canvas(newBmp);
//
//        // get the int for the colour which needs to be removed
//        Paint paint = new Paint();// 去锯齿
//        paint.setAntiAlias(true);// 防抖动
//        paint.setDither(true);// 图像过滤
//        paint.setFilterBitmap(true);
//        paint.setARGB(255, 0, 0, 0); // ARGB for the color to replace,black replace gray
//        paint.setXfermode(new PorterDuffXfermode(0x616161, 50, PorterDuff.Mode.));
//        c.drawPaint(paint);
//        return newBmp;
//    }
    /**
     * CLEAR
     *
     *         [0,0]
     *
     *         清除画布上DST所占区域的的所有像素值。如Canvas#drawColor(Color.BLACK,Mode.CLEAR)将画布上的所有内容清除掉。
     *
     * SRC
     *
     *         [Sa,Sc]
     *
     *         显示上层绘制的图片。
     *
     * DST
     *
     *         [Da, Dc]
     *
     *         显示下层绘制的图片。
     *
     * SRC_OVER
     *
     *         [Sa + (1 - Sa)*Da, Rc = Sc + (1 - Sa)*Dc]
     *
     * DST_OVER
     *
     *         [Sa + (1 - Sa)*Da, Rc = Dc + (1 - Da)*Sc]
     *
     * SRC_IN
     *
     *         [Sa * Da, Sc * Da]
     *
     * DST_IN
     *
     *         [Sa * Da, Sa * Dc]
     *
     * SRC_OUT
     *
     *         [Sa * (1 - Da), Sc * (1 - Da)]
     *
     *         相交部分按公式计算，非相交部分则各保持原样。
     *
     * DST_OUT
     *
     *         [Da * (1 - Sa), Dc * (1 - Sa)]
     *
     *         相交部分按计算公式计算，DST中非相交部分保持原样。
     *
     * SRC_ATOP
     *
     *         [Da, Sc * Da + (1 - Sa) * Dc]
     *
     *         显示下层，但相交的部分显示上层。
     *
     * DST_ATOP
     *
     *         [Sa, Sa * Dc + Sc * (1 - Da)]
     *
     *         按公式计算。如两个都不透明，则相交的部分显示的为Dc。
     *
     * XOR
     *
     *         [Sa + Da - 2 * Sa * Da, Sc * (1 - Da) + (1 - Sa) * Dc]
     *
     *         相交部分，按公式计算。公式中，如果两个都不透明的话，最终什么都不会显示。
     *
     * DARKEN
     *
     *         [Sa + Da - Sa*Da, Sc*(1 - Da) + Dc*(1 - Sa) + min(Sc, Dc)]
     *
     *         相交部分按数学公式进行计算即可。
     *
     * LIGHTEN
     *
     *         [Sa + Da - Sa*Da,Sc*(1 - Da) + Dc*(1 - Sa) + max(Sc, Dc)]
     *
     *         相交部分按数学公式进行计算即可。
     *
     * MULTIPLY
     *
     *         [Sa * Da, Sc * Dc]
     *
     *         相交部分颜色相乘，得到的结果除以255就是最终的颜色。等效于ps中的正片叠底效果。
     *
     * SCREEN 
     *
     *         [Sa + Da - Sa * Da, Sc + Dc - Sc * Dc]
     *
     *
     */
}
