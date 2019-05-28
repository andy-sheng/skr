//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.plugin.image;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.WindowManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.rong.common.FileUtils;
import io.rong.common.RLog;
import io.rong.imkit.utilities.RongUtils;

public class AlbumBitmapCacheHelper {
    private static final String TAG = "AlbumBitmapCacheHelper";
    private static volatile io.rong.imkit.plugin.image.AlbumBitmapCacheHelper instance = null;
    private LruCache<String, Bitmap> cache;
    private static int cacheSize;
    private ArrayList<String> currentShowString;
    private Context mContext;
    ThreadPoolExecutor tpe;

    private AlbumBitmapCacheHelper() {
        this.tpe = new ThreadPoolExecutor(2, 5, 10L, TimeUnit.SECONDS, new LinkedBlockingQueue());
        this.cache = new LruCache<String, Bitmap>(cacheSize) {
            protected int sizeOf(String key, Bitmap value) {
                int result;
                if (VERSION.SDK_INT >= 19) {
                    result = value.getAllocationByteCount();
                } else if (VERSION.SDK_INT >= 12) {
                    result = value.getByteCount();
                } else {
                    result = value.getRowBytes() * value.getHeight();
                }

                return result;
            }
        };
        this.currentShowString = new ArrayList();
    }

    public void releaseAllSizeCache() {
        this.cache.evictAll();
        this.cache.resize(1);
    }

    public void releaseHalfSizeCache() {
        this.cache.resize((int) (Runtime.getRuntime().maxMemory() / 1024L / 8L));
    }

    public void resizeCache() {
        this.cache.resize((int) (Runtime.getRuntime().maxMemory() / 1024L / 4L));
    }

    private void clearCache() {
        this.cache.evictAll();
        this.cache = null;
        this.tpe = null;
        instance = null;
    }

    public static io.rong.imkit.plugin.image.AlbumBitmapCacheHelper getInstance() {
        if (instance == null) {
            Class var0 = io.rong.imkit.plugin.image.AlbumBitmapCacheHelper.class;
            synchronized (io.rong.imkit.plugin.image.AlbumBitmapCacheHelper.class) {
                if (instance == null) {
                    instance = new io.rong.imkit.plugin.image.AlbumBitmapCacheHelper();
                }
            }
        }

        return instance;
    }

    public static void init(Context context) {
        RLog.d("AlbumBitmapCacheHelper", "init");
        cacheSize = calculateMemoryCacheSize(context);
        io.rong.imkit.plugin.image.AlbumBitmapCacheHelper helper = getInstance();
        helper.mContext = context.getApplicationContext();
    }

    public void uninit() {
        RLog.d("AlbumBitmapCacheHelper", "uninit");
        this.tpe.shutdownNow();
        this.clearCache();
    }

    public Bitmap getBitmap(String path, int width, int height, io.rong.imkit.plugin.image.AlbumBitmapCacheHelper.ILoadImageCallback callback, Object... objects) {
        Bitmap bitmap = this.getBitmapFromCache(path, width, height);
        if (bitmap != null) {
            Log.e("AlbumBitmapCacheHelper", "getBitmap from cache");
        } else {
            this.decodeBitmapFromPath(path, width, height, callback, objects);
        }

        return bitmap;
    }

    private void decodeBitmapFromPath(final String path, final int width, final int height, final io.rong.imkit.plugin.image.AlbumBitmapCacheHelper.ILoadImageCallback callback, final Object... objects) throws OutOfMemoryError {
        final Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                if (callback != null) {
                    callback.onLoadImageCallBack((Bitmap) msg.obj, path, objects);
                }

            }
        };
        this.tpe.execute(new Runnable() {
            public void run() {
                if (io.rong.imkit.plugin.image.AlbumBitmapCacheHelper.this.currentShowString.contains(path) && io.rong.imkit.plugin.image.AlbumBitmapCacheHelper.this.cache != null) {
                    Bitmap bitmap = null;
                    if (width != 0 && height != 0) {
                        String hash = RongUtils.md5(path + "_" + width + "_" + height);
                        String tempPath = FileUtils.getInternalCachePath(io.rong.imkit.plugin.image.AlbumBitmapCacheHelper.this.mContext, "image") + "/" + hash + ".temp";
                        File picFile = new File(path);
                        File tempFile = new File(tempPath);
                        if (tempFile.exists() && picFile.lastModified() <= tempFile.lastModified()) {
                            bitmap = BitmapFactory.decodeFile(tempPath);
                        }

                        if (bitmap == null) {
                            try {
                                bitmap = io.rong.imkit.plugin.image.AlbumBitmapCacheHelper.this.getBitmap(path, width, height);
                            } catch (OutOfMemoryError var11) {
                                bitmap = null;
                            }

                            if (bitmap != null && io.rong.imkit.plugin.image.AlbumBitmapCacheHelper.this.cache != null) {
                                bitmap = io.rong.imkit.plugin.image.AlbumBitmapCacheHelper.centerSquareScaleBitmap(bitmap, bitmap.getWidth() > bitmap.getHeight() ? bitmap.getHeight() : bitmap.getWidth());
                            }

                            if (bitmap != null) {
                                try {
                                    File file = new File(tempPath);
                                    if (!file.exists()) {
                                        file.createNewFile();
                                    } else {
                                        file.delete();
                                        file.createNewFile();
                                    }

                                    FileOutputStream fos = new FileOutputStream(file);
                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    bitmap.compress(CompressFormat.PNG, 100, baos);
                                    fos.write(baos.toByteArray());
                                    fos.flush();
                                    fos.close();
                                } catch (FileNotFoundException var9) {
                                    var9.printStackTrace();
                                } catch (IOException var10) {
                                    var10.printStackTrace();
                                }
                            }
                        } else if (io.rong.imkit.plugin.image.AlbumBitmapCacheHelper.this.cache != null) {
                            bitmap = io.rong.imkit.plugin.image.AlbumBitmapCacheHelper.centerSquareScaleBitmap(bitmap, bitmap.getWidth() > bitmap.getHeight() ? bitmap.getHeight() : bitmap.getWidth());
                        }
                    } else {
                        try {
                            bitmap = io.rong.imkit.plugin.image.AlbumBitmapCacheHelper.this.getBitmap(path, width, height);
                        } catch (OutOfMemoryError var12) {
                            var12.printStackTrace();
                        }
                    }

                    if (bitmap != null && io.rong.imkit.plugin.image.AlbumBitmapCacheHelper.this.cache != null) {
                        io.rong.imkit.plugin.image.AlbumBitmapCacheHelper.this.cache.put(path + "_" + width + "_" + height, bitmap);
                    }

                    Message msg = Message.obtain();
                    msg.obj = bitmap;
                    handler.sendMessage(msg);
                }
            }
        });
    }

    public static Bitmap centerSquareScaleBitmap(Bitmap bitmap, int edgeLength) {
        if (null != bitmap && edgeLength > 0) {
            int widthOrg = bitmap.getWidth();
            int heightOrg = bitmap.getHeight();
            int xTopLeft = (widthOrg - edgeLength) / 2;
            int yTopLeft = (heightOrg - edgeLength) / 2;
            if (xTopLeft == 0 && yTopLeft == 0) {
                return bitmap;
            } else {
                try {
                    Bitmap result = Bitmap.createBitmap(bitmap, xTopLeft, yTopLeft, edgeLength, edgeLength);
                    if (!bitmap.isRecycled()) {
                        bitmap.recycle();
                    }

                    return result;
                } catch (OutOfMemoryError var8) {
                    return bitmap;
                }
            }
        } else {
            return null;
        }
    }

    private int computeScale(Options options, int width, int height) {
        if (options == null) {
            return 1;
        } else {
            int widthScale = (int) ((float) options.outWidth / (float) width);
            int heightScale = (int) ((float) options.outHeight / (float) height);
            int scale = widthScale > heightScale ? widthScale : heightScale;
            if (scale < 1) {
                scale = 1;
            }

            return scale;
        }
    }

    private Bitmap getBitmapFromCache(String path, int width, int height) {
        return (Bitmap) this.cache.get(path + "_" + width + "_" + height);
    }

    public void addPathToShowlist(String path) {
        this.currentShowString.add(path);
    }

    public void removePathFromShowlist(String path) {
        this.currentShowString.remove(path);
    }

    private Bitmap getBitmap(String path, int widthLimit, int heightLimit) throws OutOfMemoryError {
        Bitmap bitmap = null;

        try {
            Options options = new Options();
            options.inJustDecodeBounds = true;
            options.inPreferredConfig = Config.RGB_565;
            BitmapFactory.decodeFile(path, options);
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt("Orientation", 0);
            int sampleSize;
            if (widthLimit == 0 && heightLimit == 0) {
                sampleSize = this.computeScale(options, ((WindowManager) ((WindowManager) this.mContext.getSystemService(Context.WINDOW_SERVICE))).getDefaultDisplay().getWidth(), ((WindowManager) ((WindowManager) this.mContext.getSystemService("window"))).getDefaultDisplay().getHeight());
                RLog.d("AlbumBitmapCacheHelper", "sampleSize:" + sampleSize);
            } else {
                if (orientation == 6 || orientation == 8 || orientation == 5 || orientation == 7) {
                    int tmp = widthLimit;
                    widthLimit = heightLimit;
                    heightLimit = tmp;
                }

                sampleSize = this.computeScale(options, widthLimit, heightLimit);
                RLog.d("AlbumBitmapCacheHelper", "sampleSize:" + sampleSize);
            }

            try {
                options = new Options();
                options.inJustDecodeBounds = false;
                options.inSampleSize = sampleSize;
                bitmap = BitmapFactory.decodeFile(path, options);
            } catch (OutOfMemoryError var14) {
                var14.printStackTrace();
                options.inSampleSize <<= 1;
                bitmap = BitmapFactory.decodeFile(path, options);
            }

            Matrix matrix = new Matrix();
            if (bitmap != null) {
                int w = bitmap.getWidth();
                int h = bitmap.getHeight();
                if (orientation == 6 || orientation == 8 || orientation == 5 || orientation == 7) {
                    int tmp = w;
                    w = h;
                    h = tmp;
                }

                switch (orientation) {
                    case 2:
                        matrix.preScale(-1.0F, 1.0F);
                        break;
                    case 3:
                        matrix.setRotate(180.0F, (float) w / 2.0F, (float) h / 2.0F);
                        break;
                    case 4:
                        matrix.preScale(1.0F, -1.0F);
                        break;
                    case 5:
                        matrix.setRotate(90.0F, (float) w / 2.0F, (float) h / 2.0F);
                        matrix.preScale(1.0F, -1.0F);
                        break;
                    case 6:
                        matrix.setRotate(90.0F, (float) w / 2.0F, (float) h / 2.0F);
                        break;
                    case 7:
                        matrix.setRotate(270.0F, (float) w / 2.0F, (float) h / 2.0F);
                        matrix.preScale(1.0F, -1.0F);
                        break;
                    case 8:
                        matrix.setRotate(270.0F, (float) w / 2.0F, (float) h / 2.0F);
                }

                if (widthLimit != 0 && heightLimit != 0) {
                    float xS = (float) widthLimit / (float) bitmap.getWidth();
                    float yS = (float) heightLimit / (float) bitmap.getHeight();
                    matrix.postScale(Math.min(xS, yS), Math.min(xS, yS));
                }

                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }
        } catch (IOException var15) {
            var15.printStackTrace();
        } catch (IllegalArgumentException var16) {
            var16.printStackTrace();
        }

        return bitmap;
    }

    static int calculateMemoryCacheSize(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService("activity");
        boolean largeHeap = (context.getApplicationInfo().flags & 1048576) != 0;
        int memoryClass = am.getMemoryClass();
        if (largeHeap) {
            memoryClass = am.getLargeMemoryClass();
        }

        return (int) (1048576L * (long) memoryClass / 8L);
    }

    public interface ILoadImageCallback {
        void onLoadImageCallBack(Bitmap var1, String var2, Object... var3);
    }
}
