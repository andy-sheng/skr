//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;

import io.rong.common.RLog;
import io.rong.imageloader.core.DisplayImageOptions;
import io.rong.imageloader.core.DisplayImageOptions.Builder;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imageloader.core.assist.FailReason;
import io.rong.imageloader.core.assist.ImageSize;
import io.rong.imageloader.core.assist.LoadedFrom;
import io.rong.imageloader.core.display.CircleBitmapDisplayer;
import io.rong.imageloader.core.display.RoundedBitmapDisplayer;
import io.rong.imageloader.core.display.SimpleBitmapDisplayer;
import io.rong.imageloader.core.imageaware.ImageViewAware;
import io.rong.imageloader.core.listener.ImageLoadingListener;
import io.rong.imageloader.core.listener.ImageLoadingProgressListener;
import io.rong.imageloader.core.process.BitmapProcessor;
import io.rong.imkit.R;
import io.rong.imkit.utilities.RongUtils;

public class AsyncImageView extends ImageView {
    private static final String TAG = "AsyncImageView";
    private boolean isCircle;
    private float minShortSideSize = 0.0F;
    private int mCornerRadius = 0;
    private static final int AVATAR_SIZE = 80;
    private Drawable mDefaultDrawable;
    private WeakReference<Bitmap> mWeakBitmap;
    private WeakReference<Bitmap> mShardWeakBitmap;
    private boolean mHasMask;

    public AsyncImageView(Context context) {
        super(context);
    }

    public AsyncImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!this.isInEditMode()) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AsyncImageView);
            int resId = a.getResourceId(R.styleable.AsyncImageView_RCDefDrawable, 0);
            this.isCircle = a.getInt(R.styleable.AsyncImageView_RCShape, 0) == 1;
            this.minShortSideSize = a.getDimension(R.styleable.AsyncImageView_RCMinShortSideSize, 0.0F);
            this.mCornerRadius = (int) a.getDimension(R.styleable.AsyncImageView_RCCornerRadius, 0.0F);
            this.mHasMask = a.getBoolean(R.styleable.AsyncImageView_RCMask, false);
            if (resId != 0) {
                this.mDefaultDrawable = this.getResources().getDrawable(resId);
            }

            a.recycle();
            if (this.mDefaultDrawable != null) {
                DisplayImageOptions options = this.createDisplayImageOptions(resId, false);
                Drawable drawable = options.getImageForEmptyUri((Resources) null);
                Bitmap bitmap = this.drawableToBitmap(drawable);
                ImageViewAware imageViewAware = new ImageViewAware(this);
                options.getDisplayer().display(bitmap, imageViewAware, LoadedFrom.DISC_CACHE);
            }

        }
    }

    protected void onDraw(Canvas canvas) {
        if (this.mHasMask) {
            Bitmap bitmap = this.mWeakBitmap == null ? null : (Bitmap) this.mWeakBitmap.get();
            Drawable drawable = this.getDrawable();
            RCMessageFrameLayout parent = (RCMessageFrameLayout) this.getParent();
            Drawable background = parent.getBackgroundDrawable();
            if (bitmap != null && !bitmap.isRecycled()) {
                canvas.drawBitmap(bitmap, 0.0F, 0.0F, (Paint) null);
                this.getShardImage(background, bitmap, canvas);
            } else {
                int width = this.getWidth();
                int height = this.getHeight();
                if (width <= 0 || height <= 0) {
                    return;
                }

                try {
                    bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
                } catch (OutOfMemoryError var11) {
                    RLog.e("AsyncImageView", "onDraw OutOfMemoryError");
                    var11.printStackTrace();
                    System.gc();
                }

                if (bitmap != null) {
                    Canvas rCanvas = new Canvas(bitmap);
                    if (drawable != null) {
                        drawable.setBounds(0, 0, width, height);
                        drawable.draw(rCanvas);
                        if (background != null && background instanceof NinePatchDrawable) {
                            NinePatchDrawable patchDrawable = (NinePatchDrawable) background;
                            patchDrawable.setBounds(0, 0, width, height);
                            Paint maskPaint = patchDrawable.getPaint();
                            maskPaint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
                            patchDrawable.draw(rCanvas);
                        }

                        this.mWeakBitmap = new WeakReference(bitmap);
                    }

                    canvas.drawBitmap(bitmap, 0.0F, 0.0F, (Paint) null);
                    this.getShardImage(background, bitmap, canvas);
                }
            }
        } else {
            super.onDraw(canvas);
        }

    }

    private void getShardImage(Drawable drawable_bg, Bitmap bp, Canvas canvas) {
        int width = bp.getWidth();
        int height = bp.getHeight();
        Bitmap bitmap = this.mShardWeakBitmap == null ? null : (Bitmap) this.mShardWeakBitmap.get();
        if (width > 0 && height > 0) {
            if (bitmap != null && !bitmap.isRecycled()) {
                canvas.drawBitmap(bitmap, 0.0F, 0.0F, (Paint) null);
            } else {
                try {
                    bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
                } catch (OutOfMemoryError var14) {
                    RLog.e("AsyncImageView", "getShardImage OutOfMemoryError");
                    var14.printStackTrace();
                    System.gc();
                }

                if (bitmap != null) {
                    Canvas rCanvas = new Canvas(bitmap);
                    Paint paint = new Paint();
                    paint.setAntiAlias(true);
                    Rect rect = new Rect(0, 0, width, height);
                    Rect rectF = new Rect(0, 0, width, height);
                    BitmapDrawable drawable_in = new BitmapDrawable(bp);
                    drawable_in.setBounds(rectF);
                    drawable_in.draw(rCanvas);
                    if (drawable_bg instanceof NinePatchDrawable) {
                        NinePatchDrawable patchDrawable = (NinePatchDrawable) drawable_bg;
                        patchDrawable.setBounds(rect);
                        Paint maskPaint = patchDrawable.getPaint();
                        maskPaint.setXfermode(new PorterDuffXfermode(Mode.DST_OVER));
                        patchDrawable.draw(rCanvas);
                    }

                    this.mShardWeakBitmap = new WeakReference(bitmap);
                    canvas.drawBitmap(bitmap, 0.0F, 0.0F, paint);
                }
            }

        }
    }

    protected void onDetachedFromWindow() {
        Bitmap bitmap;
        if (this.mWeakBitmap != null) {
            bitmap = (Bitmap) this.mWeakBitmap.get();
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }

            this.mWeakBitmap = null;
        }

        if (this.mShardWeakBitmap != null) {
            bitmap = (Bitmap) this.mShardWeakBitmap.get();
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }

            this.mShardWeakBitmap = null;
        }

        super.onDetachedFromWindow();
    }

    public void invalidate() {
        Bitmap bitmap;
        if (this.mWeakBitmap != null) {
            bitmap = (Bitmap) this.mWeakBitmap.get();
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }

            this.mWeakBitmap = null;
        }

        if (this.mShardWeakBitmap != null) {
            bitmap = (Bitmap) this.mShardWeakBitmap.get();
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }

            this.mShardWeakBitmap = null;
        }

        super.invalidate();
    }

    public void setDefaultDrawable() {
        if (this.mDefaultDrawable != null) {
            DisplayImageOptions options = this.createDisplayImageOptions(0, false);
            Bitmap bitmap = this.drawableToBitmap(this.mDefaultDrawable);
            ImageViewAware imageViewAware = new ImageViewAware(this);
            options.getDisplayer().display(bitmap, imageViewAware, LoadedFrom.DISC_CACHE);
        }

    }

    public void setResource(Uri imageUri) {
        DisplayImageOptions options = this.createDisplayImageOptions(0, true);
        if (imageUri != null) {
            File file = new File(imageUri.getPath());
            if (!file.exists()) {
                ImageViewAware imageViewAware = new ImageViewAware(this);
                ImageLoader.getInstance().displayImage(imageUri.toString(), imageViewAware, options, (ImageLoadingListener) null, (ImageLoadingProgressListener) null);
            } else {
                Bitmap bitmap = this.getBitmap(imageUri);
                if (bitmap != null) {
                    this.setLayoutParam(bitmap);
                    this.setImageBitmap(bitmap);
                } else {
                    this.setImageBitmap((Bitmap) null);
                    LayoutParams params = this.getLayoutParams();
                    params.height = RongUtils.dip2px(80.0F);
                    params.width = RongUtils.dip2px(110.0F);
                    this.setLayoutParams(params);
                }
            }
        }

    }

    public void setCircle(boolean circle) {
        this.isCircle = circle;
    }

    public void setLocationResource(Uri imageUri, int defRes, final int w, final int h, final IImageLoadingListener loadingListener) {
        Builder builder = new Builder();
        DisplayImageOptions options = builder.resetViewBeforeLoading(false).cacheInMemory(false).cacheOnDisk(true).bitmapConfig(Config.ARGB_8888).showImageOnLoading(defRes).preProcessor(new BitmapProcessor() {
            public Bitmap process(Bitmap bitmap) {
                int widthOrg = bitmap.getWidth();
                int heightOrg = bitmap.getHeight();
                int xTopLeft = (widthOrg - w) / 2;
                int yTopLeft = (heightOrg - h) / 2;
                if (xTopLeft > 0 && yTopLeft > 0) {
                    try {
                        Bitmap result = Bitmap.createBitmap(bitmap, xTopLeft, yTopLeft, w, h);
                        if (!bitmap.isRecycled()) {
                            bitmap.recycle();
                        }

                        return result;
                    } catch (OutOfMemoryError var7) {
                        return null;
                    }
                } else {
                    return bitmap;
                }
            }
        }).build();
        ImageLoader.getInstance().displayImage(imageUri == null ? null : imageUri.toString(), this, options, new ImageLoadingListener() {
            public void onLoadingStarted(String imageUri, View view) {
            }

            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                loadingListener.onLoadingFail();
            }

            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                File file = ImageLoader.getInstance().getDiskCache().get(imageUri);
                if (file != null && file.exists()) {
                    loadingListener.onLoadingComplete(Uri.fromFile(file));
                } else {
                    loadingListener.onLoadingFail();
                }

            }

            public void onLoadingCancelled(String imageUri, View view) {
                loadingListener.onLoadingFail();
            }
        });
    }

    public void setResource(String imageUri, int defaultResId) {
        if (imageUri != null || defaultResId > 0) {
            DisplayImageOptions options = this.createDisplayImageOptions(defaultResId, true);
            ImageLoader.getInstance().displayImage(imageUri, this, options);
        }
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Config config = drawable.getOpacity() != -1 ? Config.ARGB_8888 : Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(width, height, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }

    public void setAvatar(String imageUri, int defaultResId) {
        ImageViewAware imageViewAware = new ImageViewAware(this);
        ImageSize imageSize = new ImageSize(80, 80);
        DisplayImageOptions options = this.createDisplayImageOptions(defaultResId, true);
        ImageLoader.getInstance().displayImage(imageUri, imageViewAware, options, imageSize, (ImageLoadingListener) null, (ImageLoadingProgressListener) null);
    }

    public void setAvatar(String uid, String name, int defaultResId) {
        ImageViewAware imageViewAware = new ImageViewAware(this);
        ImageSize imageSize = new ImageSize(80, 80);
        String[] arr = new String[]{name, uid};
        DisplayImageOptions options = this.createDisplayImageOptions(defaultResId, true, arr);
        String key = URLEncoder.encode(name);
        ImageLoader.getInstance().displayImage("avatar://" + uid + key, imageViewAware, options, imageSize, (ImageLoadingListener) null, (ImageLoadingProgressListener) null);
    }

    public void setAvatar(Uri imageUri) {
        if (imageUri != null) {
            ImageViewAware imageViewAware = new ImageViewAware(this);
            ImageSize imageSize = new ImageSize(80, 80);
            DisplayImageOptions options = this.createDisplayImageOptions(0, true);
            ImageLoader.getInstance().displayImage(imageUri.toString(), imageViewAware, options, imageSize, (ImageLoadingListener) null, (ImageLoadingProgressListener) null);
        }

    }

    private Bitmap getBitmap(Uri uri) {
        Bitmap bitmap = null;
        Options options = new Options();
        options.inJustDecodeBounds = true;
        options = new Options();

        try {
            bitmap = BitmapFactory.decodeFile(uri.getPath(), options);
        } catch (Exception var5) {
            RLog.e("AsyncImageView", "getBitmap Exception : " + uri);
            var5.printStackTrace();
        }

        return bitmap;
    }

    private DisplayImageOptions createDisplayImageOptions(int defaultResId, boolean cacheInMemory) {
        return this.createDisplayImageOptions(defaultResId, cacheInMemory, (Object) null);
    }

    private DisplayImageOptions createDisplayImageOptions(int defaultResId, boolean cacheInMemory, Object extraForDownloader) {
        Builder builder = new Builder();
        Drawable defaultDrawable = this.mDefaultDrawable;
        if (defaultResId > 0) {
            try {
                defaultDrawable = this.getContext().getResources().getDrawable(defaultResId);
            } catch (NotFoundException var7) {
                var7.printStackTrace();
            }
        }

        if (defaultDrawable != null) {
            builder.showImageOnLoading(defaultDrawable);
            builder.showImageForEmptyUri(defaultDrawable);
            builder.showImageOnFail(defaultDrawable);
        }

        if (extraForDownloader != null) {
            builder.extraForDownloader(extraForDownloader);
        }

        if (this.isCircle) {
            builder.displayer(new CircleBitmapDisplayer());
        } else if (this.mCornerRadius > 0) {
            builder.displayer(new RoundedBitmapDisplayer(this.mCornerRadius));
        } else {
            builder.displayer(new SimpleBitmapDisplayer());
        }

        DisplayImageOptions options = builder.resetViewBeforeLoading(false).cacheInMemory(cacheInMemory).cacheOnDisk(true).bitmapConfig(Config.RGB_565).build();
        return options;
    }

    public int getCornerRadius() {
        return this.mCornerRadius;
    }

    public void setCornerRadius(int mCornerRadius) {
        this.mCornerRadius = mCornerRadius;
    }

    private void setLayoutParam(Bitmap bitmap) {
        float width = (float) bitmap.getWidth();
        float height = (float) bitmap.getHeight();
        int minSize = 100;
        if (this.minShortSideSize > 0.0F) {
            if (width > this.minShortSideSize && height > this.minShortSideSize) {
                LayoutParams params = this.getLayoutParams();
                params.height = (int) height;
                params.width = (int) width;
                this.setLayoutParams(params);
            } else {
                float scale = width / height;
                int finalWidth;
                int finalHeight;
                if (scale > 1.0F) {
                    finalHeight = (int) (this.minShortSideSize / scale);
                    if (finalHeight < minSize) {
                        finalHeight = minSize;
                    }

                    finalWidth = (int) this.minShortSideSize;
                } else {
                    finalHeight = (int) this.minShortSideSize;
                    finalWidth = (int) (this.minShortSideSize * scale);
                    if (finalWidth < minSize) {
                        finalWidth = minSize;
                    }
                }

                LayoutParams params = this.getLayoutParams();
                params.height = finalHeight;
                params.width = finalWidth;
                this.setLayoutParams(params);
            }
        }

    }
}
