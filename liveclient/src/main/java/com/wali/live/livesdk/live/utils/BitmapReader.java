package com.wali.live.livesdk.live.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class BitmapReader {
    final static int SIZE_USE_BYTE_ARRAY = 700 * 700;

    public static Bitmap decodeBmpFromFile(String filePath) {
        return decodeBmpFromFile(filePath, null);
    }

    public static Bitmap decodeBmpFromFile(String filePath, BitmapFactory.Options options) {
        if (!TextUtils.isEmpty(filePath)) {
            return decodeBmpFromFile(new File(filePath), options);
        }
        return null;
    }

    public static Bitmap decodeBmpFromFile(File file) {
        return decodeBmpFromFile(file, null);
    }

    public static Bitmap decodeBmpFromFile(File file, BitmapFactory.Options options) {
        if (null != file) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                return decodeBmpFromStream(fis, options);
            } catch (FileNotFoundException e) {
//                MyLog.e(e);
            } finally {
                if (null != fis) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    public static Bitmap decodeBmpFromStream(InputStream is, BitmapFactory.Options options) {
        //MyLog.d("BitmapReader","BitmapReader options.inSampleSize" + options.inSampleSize);
        return BitmapFactory.decodeStream(is, null, options);
//        Bitmap bmp = null;
//        if (null != is) {
//            try {
//                byte[] data = readStream(is);
//                if (null != data) {
//                    bmp = BitmapFactory.decodeByteArray(data, 0, data.length, options);
//                    data = null;
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            } catch (OutOfMemoryError err) {
//                err.printStackTrace();
//                System.gc();
//                bmp = BitmapFactory.decodeStream(is, null, options);
//            }
//
//        }
//
//        return bmp;
    }

    /*
     * 得到图片字节流 数组大小
     */
    public static byte[] readStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        outStream.close();
        inStream.close();
        buffer = null;
        return outStream.toByteArray();
    }

    /**
     * Calculate an inSampleSize for use in a {@link BitmapFactory.Options} object when decoding bitmaps using the
     * decode* methods from {@link BitmapFactory}. This implementation calculates the closest inSampleSize that will
     * result in the final decoded bitmap having a width and height equal to or larger than the requested width and
     * height. This implementation does not ensure a power of 2 is returned for inSampleSize which can be faster when
     * decoding but results in a larger bitmap which isn't as useful for caching purposes.
     *
     * @param options   An options object with out* params already populated (run through a decode* method with
     *                  inJustDecodeBounds==true
     * @param reqWidth  The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return The value to be used for inSampleSize
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;

        int inSampleSize = 1;
        if ((height > reqHeight) || (width > reqWidth)) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger
            // inSampleSize).
            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down
            // further.
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while ((totalPixels / (inSampleSize * inSampleSize)) > totalReqPixelsCap) {
                inSampleSize++;
            }

        }
        return inSampleSize;
    }
}
