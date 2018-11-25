//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BitmapUtil {
  private static final String TAG = "Util";

  public BitmapUtil() {
  }

  public static String getBase64FromBitmap(Bitmap bitmap) {
    String base64Str = null;
    ByteArrayOutputStream baos = null;

    try {
      if (bitmap != null) {
        baos = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.JPEG, 60, baos);
        byte[] bitmapBytes = baos.toByteArray();
        base64Str = Base64.encodeToString(bitmapBytes, 2);
        baos.flush();
        baos.close();
      }
    } catch (IOException var12) {
      var12.printStackTrace();
    } finally {
      try {
        if (baos != null) {
          baos.flush();
          baos.close();
        }
      } catch (IOException var11) {
        var11.printStackTrace();
      }

    }

    return base64Str;
  }

  public static Bitmap getBitmapFromBase64(String base64Str) {
    if (TextUtils.isEmpty(base64Str)) {
      return null;
    } else {
      byte[] bytes = Base64.decode(base64Str, 2);
      return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
  }

  public static Bitmap getResizedBitmap(Context context, Uri uri, int widthLimit, int heightLimit) throws IOException {
    String path = null;
    Bitmap result = null;
    if (uri.getScheme().equals("file")) {
      path = uri.getPath();
    } else {
      if (!uri.getScheme().equals("content")) {
        return null;
      }

      Cursor cursor = context.getContentResolver().query(uri, new String[]{"_data"}, (String)null, (String[])null, (String)null);
      cursor.moveToFirst();
      path = cursor.getString(0);
      cursor.close();
    }

    ExifInterface exifInterface = new ExifInterface(path);
    Options options = new Options();
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeFile(path, options);
    int orientation = exifInterface.getAttributeInt("Orientation", 0);
    int width;
    if (orientation == 6 || orientation == 8 || orientation == 5 || orientation == 7) {
      width = widthLimit;
      widthLimit = heightLimit;
      heightLimit = width;
    }

    width = options.outWidth;
    int height = options.outHeight;
    int sampleW = 1;

    int sampleH;
    for(sampleH = 1; width / 2 > widthLimit; sampleW <<= 1) {
      width /= 2;
    }

    while(height / 2 > heightLimit) {
      height /= 2;
      sampleH <<= 1;
    }

    options = new Options();
    int sampleSize;
    if (widthLimit != 2147483647 && heightLimit != 2147483647) {
      sampleSize = Math.max(sampleW, sampleH);
    } else {
      sampleSize = Math.max(sampleW, sampleH);
    }

    options.inSampleSize = sampleSize;

    Bitmap bitmap;
    try {
      bitmap = BitmapFactory.decodeFile(path, options);
    } catch (OutOfMemoryError var22) {
      var22.printStackTrace();
      options.inSampleSize <<= 1;
      bitmap = BitmapFactory.decodeFile(path, options);
    }

    Matrix matrix = new Matrix();
    if (bitmap == null) {
      return bitmap;
    } else {
      int w = bitmap.getWidth();
      int h = bitmap.getHeight();
      if (orientation == 6 || orientation == 8 || orientation == 5 || orientation == 7) {
        int tmp = w;
        w = h;
        h = tmp;
      }

      switch(orientation) {
        case 2:
          matrix.preScale(-1.0F, 1.0F);
          break;
        case 3:
          matrix.setRotate(180.0F, (float)w / 2.0F, (float)h / 2.0F);
          break;
        case 4:
          matrix.preScale(1.0F, -1.0F);
          break;
        case 5:
          matrix.setRotate(90.0F, (float)w / 2.0F, (float)h / 2.0F);
          matrix.preScale(1.0F, -1.0F);
          break;
        case 6:
          matrix.setRotate(90.0F, (float)w / 2.0F, (float)h / 2.0F);
          break;
        case 7:
          matrix.setRotate(270.0F, (float)w / 2.0F, (float)h / 2.0F);
          matrix.preScale(1.0F, -1.0F);
          break;
        case 8:
          matrix.setRotate(270.0F, (float)w / 2.0F, (float)h / 2.0F);
      }

      float xS = (float)widthLimit / (float)bitmap.getWidth();
      float yS = (float)heightLimit / (float)bitmap.getHeight();
      matrix.postScale(Math.min(xS, yS), Math.min(xS, yS));

      try {
        result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return result;
      } catch (OutOfMemoryError var21) {
        var21.printStackTrace();
        Log.e("ResourceCompressHandler", "OOMHeight:" + bitmap.getHeight() + "Width:" + bitmap.getHeight() + "matrix:" + xS + " " + yS);
        return null;
      }
    }
  }

  public static Bitmap getRotateBitmap(float degrees, Bitmap bm) {
    int bmpW = bm.getWidth();
    int bmpH = bm.getHeight();
    Matrix mt = new Matrix();
    mt.setRotate(degrees);
    return Bitmap.createBitmap(bm, 0, 0, bmpW, bmpH, mt, true);
  }

  private static Options decodeBitmapOptionsInfo(Context context, Uri uri) {
    InputStream input = null;
    Options opt = new Options();

    Options var5;
    try {
      if (uri.getScheme().equals("content")) {
        input = context.getContentResolver().openInputStream(uri);
      } else if (uri.getScheme().equals("file")) {
        input = new FileInputStream(uri.getPath());
      }

      opt.inJustDecodeBounds = true;
      BitmapFactory.decodeStream((InputStream)input, (Rect)null, opt);
      Options var4 = opt;
      return var4;
    } catch (FileNotFoundException var15) {
      if (input == null) {
        input = getFileInputStream(uri.getPath());
      }

      opt.inJustDecodeBounds = true;
      BitmapFactory.decodeStream((InputStream)input, (Rect)null, opt);
      var5 = opt;
    } finally {
      if (null != input) {
        try {
          ((InputStream)input).close();
        } catch (IOException var14) {
          ;
        }
      }

    }

    return var5;
  }

  private static Bitmap rotateBitMap(String srcFilePath, Bitmap bitmap) {
    ExifInterface exif = null;

    try {
      exif = new ExifInterface(srcFilePath);
    } catch (IOException var6) {
      var6.printStackTrace();
    }

    float degree = 0.0F;
    if (exif != null) {
      switch(exif.getAttributeInt("Orientation", 0)) {
        case 3:
          degree = 180.0F;
          break;
        case 6:
          degree = 90.0F;
          break;
        case 8:
          degree = 270.0F;
      }
    }

    if (degree != 0.0F) {
      Matrix matrix = new Matrix();
      matrix.setRotate(degree, (float)bitmap.getWidth(), (float)bitmap.getHeight());
      Bitmap b2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
      if (b2 != null && bitmap != b2) {
        bitmap.recycle();
        bitmap = b2;
      }
    }

    return bitmap;
  }

  public static InputStream getFileInputStream(String path) {
    FileInputStream fileInputStream = null;

    try {
      fileInputStream = new FileInputStream(new File(path));
    } catch (FileNotFoundException var3) {
      var3.printStackTrace();
    }

    return fileInputStream;
  }

  public static String saveBitmap(Bitmap bitmap, File file) {
    try {
      FileOutputStream out = new FileOutputStream(file);
      bitmap.compress(CompressFormat.JPEG, 100, out);
      out.flush();
      out.close();
    } catch (FileNotFoundException var3) {
      var3.printStackTrace();
    } catch (IOException var4) {
      var4.printStackTrace();
    }

    return file.getPath();
  }
}
