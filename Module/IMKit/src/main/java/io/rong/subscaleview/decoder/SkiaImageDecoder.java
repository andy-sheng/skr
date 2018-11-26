//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.subscaleview.decoder;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Rect;
import android.net.Uri;
import android.support.annotation.Keep;
import android.text.TextUtils;

import java.io.InputStream;
import java.util.List;

import io.rong.subscaleview.SubsamplingScaleImageView;
import io.rong.subscaleview.decoder.ImageDecoder;

public class SkiaImageDecoder implements ImageDecoder {
    private static final String FILE_PREFIX = "file://";
    private static final String ASSET_PREFIX = "file:///android_asset/";
    private static final String RESOURCE_PREFIX = "android.resource://";
    private final Config bitmapConfig;

    @Keep
    public SkiaImageDecoder() {
        this((Config) null);
    }

    public SkiaImageDecoder(Config bitmapConfig) {
        Config globalBitmapConfig = SubsamplingScaleImageView.getPreferredBitmapConfig();
        if (bitmapConfig != null) {
            this.bitmapConfig = bitmapConfig;
        } else if (globalBitmapConfig != null) {
            this.bitmapConfig = globalBitmapConfig;
        } else {
            this.bitmapConfig = Config.RGB_565;
        }

    }

    public Bitmap decode(Context context, Uri uri) throws Exception {
        String uriString = uri.toString();
        Options options = new Options();
        options.inPreferredConfig = this.bitmapConfig;
        Bitmap bitmap;
        if (uriString.startsWith("android.resource://")) {
            String packageName = uri.getAuthority();
            Resources res;
            if (context.getPackageName().equals(packageName)) {
                res = context.getResources();
            } else {
                PackageManager pm = context.getPackageManager();
                res = pm.getResourcesForApplication(packageName);
            }

            int id = 0;
            List<String> segments = uri.getPathSegments();
            int size = segments.size();
            if (size == 2 && ((String) segments.get(0)).equals("drawable")) {
                String resName = (String) segments.get(1);
                id = res.getIdentifier(resName, "drawable", packageName);
            } else if (size == 1 && TextUtils.isDigitsOnly((CharSequence) segments.get(0))) {
                try {
                    id = Integer.parseInt((String) segments.get(0));
                } catch (NumberFormatException var19) {
                    ;
                }
            }

            bitmap = BitmapFactory.decodeResource(context.getResources(), id, options);
        } else if (uriString.startsWith("file:///android_asset/")) {
            String assetName = uriString.substring("file:///android_asset/".length());
            bitmap = BitmapFactory.decodeStream(context.getAssets().open(assetName), (Rect) null, options);
        } else if (uriString.startsWith("file://")) {
            bitmap = BitmapFactory.decodeFile(uriString.substring("file://".length()), options);
        } else {
            InputStream inputStream = null;

            try {
                ContentResolver contentResolver = context.getContentResolver();
                inputStream = contentResolver.openInputStream(uri);
                bitmap = BitmapFactory.decodeStream(inputStream, (Rect) null, options);
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Exception var18) {
                        ;
                    }
                }

            }
        }

        if (bitmap == null) {
            throw new RuntimeException("Skia image region decoder returned null bitmap - image format may not be supported");
        } else {
            return bitmap;
        }
    }
}
