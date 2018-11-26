//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.subscaleview.decoder;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build.VERSION;
import android.support.annotation.Keep;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

import io.rong.subscaleview.SubsamplingScaleImageView;
import io.rong.subscaleview.decoder.ImageRegionDecoder;

public class SkiaPooledImageRegionDecoder implements ImageRegionDecoder {
    private static final String TAG = io.rong.subscaleview.decoder.SkiaPooledImageRegionDecoder.class.getSimpleName();
    private static boolean debug = false;
    private io.rong.subscaleview.decoder.SkiaPooledImageRegionDecoder.DecoderPool decoderPool;
    private final ReadWriteLock decoderLock;
    private static final String FILE_PREFIX = "file://";
    private static final String ASSET_PREFIX = "file:///android_asset/";
    private static final String RESOURCE_PREFIX = "android.resource://";
    private final Config bitmapConfig;
    private Context context;
    private Uri uri;
    private long fileLength;
    private final Point imageDimensions;
    private final AtomicBoolean lazyInited;

    @Keep
    public SkiaPooledImageRegionDecoder() {
        this((Config) null);
    }

    public SkiaPooledImageRegionDecoder(Config bitmapConfig) {
        this.decoderPool = new io.rong.subscaleview.decoder.SkiaPooledImageRegionDecoder.DecoderPool();
        this.decoderLock = new ReentrantReadWriteLock(true);
        this.fileLength = 9223372036854775807L;
        this.imageDimensions = new Point(0, 0);
        this.lazyInited = new AtomicBoolean(false);
        Config globalBitmapConfig = SubsamplingScaleImageView.getPreferredBitmapConfig();
        if (bitmapConfig != null) {
            this.bitmapConfig = bitmapConfig;
        } else if (globalBitmapConfig != null) {
            this.bitmapConfig = globalBitmapConfig;
        } else {
            this.bitmapConfig = Config.RGB_565;
        }

    }

    @Keep
    public static void setDebug(boolean debug) {
        debug = debug;
    }

    public Point init(Context context, Uri uri) throws Exception {
        this.context = context;
        this.uri = uri;
        this.initialiseDecoder();
        return this.imageDimensions;
    }

    private void lazyInit() {
        if (this.lazyInited.compareAndSet(false, true) && this.fileLength < 9223372036854775807L) {
            this.debug("Starting lazy init of additional decoders");
            Thread thread = new Thread() {
                public void run() {
                    while (io.rong.subscaleview.decoder.SkiaPooledImageRegionDecoder.this.decoderPool != null && io.rong.subscaleview.decoder.SkiaPooledImageRegionDecoder.this.allowAdditionalDecoder(io.rong.subscaleview.decoder.SkiaPooledImageRegionDecoder.this.decoderPool.size(), io.rong.subscaleview.decoder.SkiaPooledImageRegionDecoder.this.fileLength)) {
                        try {
                            if (io.rong.subscaleview.decoder.SkiaPooledImageRegionDecoder.this.decoderPool != null) {
                                long start = System.currentTimeMillis();
                                io.rong.subscaleview.decoder.SkiaPooledImageRegionDecoder.this.debug("Starting decoder");
                                io.rong.subscaleview.decoder.SkiaPooledImageRegionDecoder.this.initialiseDecoder();
                                long end = System.currentTimeMillis();
                                io.rong.subscaleview.decoder.SkiaPooledImageRegionDecoder.this.debug("Started decoder, took " + (end - start) + "ms");
                            }
                        } catch (Exception var5) {
                            io.rong.subscaleview.decoder.SkiaPooledImageRegionDecoder.this.debug("Failed to start decoder: " + var5.getMessage());
                        }
                    }

                }
            };
            thread.start();
        }

    }

    private void initialiseDecoder() throws Exception {
        String uriString = this.uri.toString();
        long fileLength = 9223372036854775807L;
        BitmapRegionDecoder decoder;
        if (uriString.startsWith("android.resource://")) {
            String packageName = this.uri.getAuthority();
            Resources res;
            if (this.context.getPackageName().equals(packageName)) {
                res = this.context.getResources();
            } else {
                PackageManager pm = this.context.getPackageManager();
                res = pm.getResourcesForApplication(packageName);
            }

            int id = 0;
            List<String> segments = this.uri.getPathSegments();
            int size = segments.size();
            if (size == 2 && ((String) segments.get(0)).equals("drawable")) {
                String resName = (String) segments.get(1);
                id = res.getIdentifier(resName, "drawable", packageName);
            } else if (size == 1 && TextUtils.isDigitsOnly((CharSequence) segments.get(0))) {
                try {
                    id = Integer.parseInt((String) segments.get(0));
                } catch (NumberFormatException var37) {
                    ;
                }
            }

            try {
                AssetFileDescriptor descriptor = this.context.getResources().openRawResourceFd(id);
                fileLength = descriptor.getLength();
            } catch (Exception var36) {
                ;
            }

            decoder = BitmapRegionDecoder.newInstance(this.context.getResources().openRawResource(id), false);
        } else if (uriString.startsWith("file:///android_asset/")) {
            String assetName = uriString.substring("file:///android_asset/".length());

            try {
                AssetFileDescriptor descriptor = this.context.getAssets().openFd(assetName);
                fileLength = descriptor.getLength();
            } catch (Exception var35) {
                ;
            }

            decoder = BitmapRegionDecoder.newInstance(this.context.getAssets().open(assetName, 1), false);
        } else if (uriString.startsWith("file://")) {
            decoder = BitmapRegionDecoder.newInstance(uriString.substring("file://".length()), false);

            try {
                File file = new File(uriString);
                if (file.exists()) {
                    fileLength = file.length();
                }
            } catch (Exception var34) {
                ;
            }
        } else {
            InputStream inputStream = null;

            try {
                ContentResolver contentResolver = this.context.getContentResolver();
                inputStream = contentResolver.openInputStream(this.uri);
                decoder = BitmapRegionDecoder.newInstance(inputStream, false);

                try {
                    AssetFileDescriptor descriptor = contentResolver.openAssetFileDescriptor(this.uri, "r");
                    if (descriptor != null) {
                        fileLength = descriptor.getLength();
                    }
                } catch (Exception var33) {
                    ;
                }
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Exception var32) {
                        ;
                    }
                }

            }
        }

        this.fileLength = fileLength;
        this.imageDimensions.set(decoder.getWidth(), decoder.getHeight());
        this.decoderLock.writeLock().lock();

        try {
            if (this.decoderPool != null) {
                this.decoderPool.add(decoder);
            }
        } finally {
            this.decoderLock.writeLock().unlock();
        }

    }

    public Bitmap decodeRegion(Rect sRect, int sampleSize) {
        this.debug("Decode region " + sRect + " on thread " + Thread.currentThread().getName());
        if (sRect.width() < this.imageDimensions.x || sRect.height() < this.imageDimensions.y) {
            this.lazyInit();
        }

        this.decoderLock.readLock().lock();

        try {
            if (this.decoderPool != null) {
                BitmapRegionDecoder decoder = this.decoderPool.acquire();

                try {
                    if (decoder != null && !decoder.isRecycled()) {
                        Options options = new Options();
                        options.inSampleSize = sampleSize;
                        options.inPreferredConfig = this.bitmapConfig;
                        Bitmap bitmap = decoder.decodeRegion(sRect, options);
                        if (bitmap == null) {
                            throw new RuntimeException("Skia image decoder returned null bitmap - image format may not be supported");
                        }

                        Bitmap var6 = bitmap;
                        return var6;
                    }
                } finally {
                    if (decoder != null) {
                        this.decoderPool.release(decoder);
                    }

                }
            }

            throw new IllegalStateException("Cannot decode region after decoder has been recycled");
        } finally {
            this.decoderLock.readLock().unlock();
        }
    }

    public synchronized boolean isReady() {
        return this.decoderPool != null && !this.decoderPool.isEmpty();
    }

    public synchronized void recycle() {
        this.decoderLock.writeLock().lock();

        try {
            if (this.decoderPool != null) {
                this.decoderPool.recycle();
                this.decoderPool = null;
                this.context = null;
                this.uri = null;
            }
        } finally {
            this.decoderLock.writeLock().unlock();
        }

    }

    protected boolean allowAdditionalDecoder(int numberOfDecoders, long fileLength) {
        if (numberOfDecoders >= 4) {
            this.debug("No additional decoders allowed, reached hard limit (4)");
            return false;
        } else if ((long) numberOfDecoders * fileLength > 20971520L) {
            this.debug("No additional encoders allowed, reached hard memory limit (20Mb)");
            return false;
        } else if (numberOfDecoders >= this.getNumberOfCores()) {
            this.debug("No additional encoders allowed, limited by CPU cores (" + this.getNumberOfCores() + ")");
            return false;
        } else if (this.isLowMemory()) {
            this.debug("No additional encoders allowed, memory is low");
            return false;
        } else {
            this.debug("Additional decoder allowed, current count is " + numberOfDecoders + ", estimated native memory " + fileLength * (long) numberOfDecoders / 1048576L + "Mb");
            return true;
        }
    }

    private int getNumberOfCores() {
        return VERSION.SDK_INT >= 17 ? Runtime.getRuntime().availableProcessors() : this.getNumCoresOldPhones();
    }

    private int getNumCoresOldPhones() {
        try {
            File dir = new File("/sys/devices/system/cpu/");

            class CpuFilter implements FileFilter {
                CpuFilter() {
                }

                public boolean accept(File pathname) {
                    return Pattern.matches("cpu[0-9]+", pathname.getName());
                }
            }

            File[] files = dir.listFiles(new CpuFilter());
            return files.length;
        } catch (Exception var3) {
            return 1;
        }
    }

    private boolean isLowMemory() {
        ActivityManager activityManager = (ActivityManager) this.context.getSystemService("activity");
        if (activityManager != null) {
            MemoryInfo memoryInfo = new MemoryInfo();
            activityManager.getMemoryInfo(memoryInfo);
            return memoryInfo.lowMemory;
        } else {
            return true;
        }
    }

    private void debug(String message) {
        if (debug) {
            Log.d(TAG, message);
        }

    }

    private static class DecoderPool {
        private final Semaphore available;
        private final Map<BitmapRegionDecoder, Boolean> decoders;

        private DecoderPool() {
            this.available = new Semaphore(0, true);
            this.decoders = new ConcurrentHashMap();
        }

        private synchronized boolean isEmpty() {
            return this.decoders.isEmpty();
        }

        private synchronized int size() {
            return this.decoders.size();
        }

        private BitmapRegionDecoder acquire() {
            this.available.acquireUninterruptibly();
            return this.getNextAvailable();
        }

        private void release(BitmapRegionDecoder decoder) {
            if (this.markAsUnused(decoder)) {
                this.available.release();
            }

        }

        private synchronized void add(BitmapRegionDecoder decoder) {
            this.decoders.put(decoder, false);
            this.available.release();
        }

        private synchronized void recycle() {
            while (!this.decoders.isEmpty()) {
                BitmapRegionDecoder decoder = this.acquire();
                decoder.recycle();
                this.decoders.remove(decoder);
            }

        }

        private synchronized BitmapRegionDecoder getNextAvailable() {
            Iterator var1 = this.decoders.entrySet().iterator();

            Entry entry;
            do {
                if (!var1.hasNext()) {
                    return null;
                }

                entry = (Entry) var1.next();
            } while ((Boolean) entry.getValue());

            entry.setValue(true);
            return (BitmapRegionDecoder) entry.getKey();
        }

        private synchronized boolean markAsUnused(BitmapRegionDecoder decoder) {
            Iterator var2 = this.decoders.entrySet().iterator();

            Entry entry;
            do {
                if (!var2.hasNext()) {
                    return false;
                }

                entry = (Entry) var2.next();
            } while (decoder != entry.getKey());

            if ((Boolean) entry.getValue()) {
                entry.setValue(false);
                return true;
            } else {
                return false;
            }
        }
    }
}
