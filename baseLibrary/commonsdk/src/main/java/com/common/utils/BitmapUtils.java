package com.common.utils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;

import com.common.log.MyLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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
            e.printStackTrace();
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
            bitmap.recycle();
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
    public  Bitmap rotateBitmapByDegree(String path, int degree) {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        return rotateBitmapByDegree(bitmap,degree);
    }

    /**
     * 获取我们需要的整理过旋转角度的Uri
     * @param activity  上下文环境
     * @param path      路径
     * @return          正常的Uri
     */
    public Uri getRotatedUri(Activity activity, String path){
        int degree = getBitmapDegree(path);
        if (degree != 0){
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            Bitmap newBitmap = rotateBitmapByDegree(bitmap,degree);
            return Uri.parse(MediaStore.Images.Media.insertImage(activity.getContentResolver(),newBitmap,null,null));
        }else{
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

}
