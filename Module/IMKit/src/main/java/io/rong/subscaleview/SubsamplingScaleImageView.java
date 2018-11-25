//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.subscaleview;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.annotation.AnyThread;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import io.rong.subscaleview.ImageSource;
import io.rong.subscaleview.ImageViewState;
import io.rong.subscaleview.Utils;
import io.rong.subscaleview.decoder.CompatDecoderFactory;
import io.rong.subscaleview.decoder.DecoderFactory;
import io.rong.subscaleview.decoder.ImageDecoder;
import io.rong.subscaleview.decoder.ImageRegionDecoder;
import io.rong.subscaleview.decoder.SkiaImageDecoder;
import io.rong.subscaleview.decoder.SkiaImageRegionDecoder;

public class SubsamplingScaleImageView extends View {
  private static final String TAG = io.rong.subscaleview.SubsamplingScaleImageView.class.getSimpleName();
  public static final int ORIENTATION_USE_EXIF = -1;
  public static final int ORIENTATION_0 = 0;
  public static final int ORIENTATION_90 = 90;
  public static final int ORIENTATION_180 = 180;
  public static final int ORIENTATION_270 = 270;
  private static final List<Integer> VALID_ORIENTATIONS = Arrays.asList(0, 90, 180, 270, -1);
  public static final int ZOOM_FOCUS_FIXED = 1;
  public static final int ZOOM_FOCUS_CENTER = 2;
  public static final int ZOOM_FOCUS_CENTER_IMMEDIATE = 3;
  private static final List<Integer> VALID_ZOOM_STYLES = Arrays.asList(1, 2, 3);
  public static final int EASE_OUT_QUAD = 1;
  public static final int EASE_IN_OUT_QUAD = 2;
  private static final List<Integer> VALID_EASING_STYLES = Arrays.asList(2, 1);
  public static final int PAN_LIMIT_INSIDE = 1;
  public static final int PAN_LIMIT_OUTSIDE = 2;
  public static final int PAN_LIMIT_CENTER = 3;
  private static final List<Integer> VALID_PAN_LIMITS = Arrays.asList(1, 2, 3);
  public static final int SCALE_TYPE_CENTER_INSIDE = 1;
  public static final int SCALE_TYPE_CENTER_CROP = 2;
  public static final int SCALE_TYPE_CUSTOM = 3;
  public static final int SCALE_TYPE_START = 4;
  private static final List<Integer> VALID_SCALE_TYPES = Arrays.asList(2, 1, 3, 4);
  public static final int ORIGIN_ANIM = 1;
  public static final int ORIGIN_TOUCH = 2;
  public static final int ORIGIN_FLING = 3;
  public static final int ORIGIN_DOUBLE_TAP_ZOOM = 4;
  private Bitmap bitmap;
  private boolean bitmapIsPreview;
  private boolean bitmapIsCached;
  private Uri uri;
  private int fullImageSampleSize;
  private Map<Integer, List<io.rong.subscaleview.SubsamplingScaleImageView.Tile>> tileMap;
  private boolean debug;
  private int orientation;
  private float maxScale;
  private float minScale;
  private int minimumTileDpi;
  private int panLimit;
  private int minimumScaleType;
  public static final int TILE_SIZE_AUTO = 2147483647;
  private int maxTileWidth;
  private int maxTileHeight;
  private Executor executor;
  private boolean eagerLoadingEnabled;
  private boolean panEnabled;
  private boolean zoomEnabled;
  private boolean quickScaleEnabled;
  private float doubleTapZoomScale;
  private int doubleTapZoomStyle;
  private int doubleTapZoomDuration;
  private float scale;
  private float scaleStart;
  private PointF vTranslate;
  private PointF vTranslateStart;
  private PointF vTranslateBefore;
  private Float pendingScale;
  private PointF sPendingCenter;
  private PointF sRequestedCenter;
  private int sWidth;
  private int sHeight;
  private int sOrientation;
  private Rect sRegion;
  private Rect pRegion;
  private boolean isZooming;
  private boolean isPanning;
  private boolean isQuickScaling;
  private int maxTouchCount;
  private GestureDetector detector;
  private GestureDetector singleDetector;
  private ImageRegionDecoder decoder;
  private final ReadWriteLock decoderLock;
  private DecoderFactory<? extends ImageDecoder> bitmapDecoderFactory;
  private DecoderFactory<? extends ImageRegionDecoder> regionDecoderFactory;
  private PointF vCenterStart;
  private float vDistStart;
  private final float quickScaleThreshold;
  private float quickScaleLastDistance;
  private boolean quickScaleMoved;
  private PointF quickScaleVLastPoint;
  private PointF quickScaleSCenter;
  private PointF quickScaleVStart;
  private io.rong.subscaleview.SubsamplingScaleImageView.Anim anim;
  private boolean readySent;
  private boolean imageLoadedSent;
  private io.rong.subscaleview.SubsamplingScaleImageView.OnImageEventListener onImageEventListener;
  private io.rong.subscaleview.SubsamplingScaleImageView.OnStateChangedListener onStateChangedListener;
  private OnLongClickListener onLongClickListener;
  private final Handler handler;
  private static final int MESSAGE_LONG_CLICK = 1;
  private Paint bitmapPaint;
  private Paint debugTextPaint;
  private Paint debugLinePaint;
  private Paint tileBgPaint;
  private io.rong.subscaleview.SubsamplingScaleImageView.ScaleAndTranslate satTemp;
  private Matrix matrix;
  private RectF sRect;
  private final float[] srcArray;
  private final float[] dstArray;
  private final float density;
  private static Config preferredBitmapConfig;

  public SubsamplingScaleImageView(Context context, AttributeSet attr) {
    super(context, attr);
    this.orientation = 0;
    this.maxScale = 2.0F;
    this.minScale = this.minScale();
    this.minimumTileDpi = -1;
    this.panLimit = 1;
    this.minimumScaleType = 1;
    this.maxTileWidth = 2147483647;
    this.maxTileHeight = 2147483647;
    this.executor = AsyncTask.THREAD_POOL_EXECUTOR;
    this.eagerLoadingEnabled = true;
    this.panEnabled = true;
    this.zoomEnabled = true;
    this.quickScaleEnabled = true;
    this.doubleTapZoomScale = 1.0F;
    this.doubleTapZoomStyle = 1;
    this.doubleTapZoomDuration = 500;
    this.decoderLock = new ReentrantReadWriteLock(true);
    this.bitmapDecoderFactory = new CompatDecoderFactory(SkiaImageDecoder.class);
    this.regionDecoderFactory = new CompatDecoderFactory(SkiaImageRegionDecoder.class);
    this.srcArray = new float[8];
    this.dstArray = new float[8];
    this.density = this.getResources().getDisplayMetrics().density;
    this.setMinimumDpi(160);
    this.setDoubleTapZoomDpi(160);
    this.setMinimumTileDpi(320);
    this.setGestureDetector(context);
    this.handler = new Handler(new Callback() {
      public boolean handleMessage(Message message) {
        if (message.what == 1 && io.rong.subscaleview.SubsamplingScaleImageView.this.onLongClickListener != null) {
          io.rong.subscaleview.SubsamplingScaleImageView.this.maxTouchCount = 0;
          io.rong.subscaleview.SubsamplingScaleImageView.super.setOnLongClickListener(io.rong.subscaleview.SubsamplingScaleImageView.this.onLongClickListener);
          io.rong.subscaleview.SubsamplingScaleImageView.this.performLongClick();
          io.rong.subscaleview.SubsamplingScaleImageView.super.setOnLongClickListener((OnLongClickListener)null);
        }

        return true;
      }
    });
    this.quickScaleThreshold = TypedValue.applyDimension(1, 20.0F, context.getResources().getDisplayMetrics());
  }

  public SubsamplingScaleImageView(Context context) {
    this(context, (AttributeSet)null);
  }

  public static Config getPreferredBitmapConfig() {
    return preferredBitmapConfig;
  }

  public static void setPreferredBitmapConfig(Config preferredBitmapConfig) {
    preferredBitmapConfig = preferredBitmapConfig;
  }

  public final void setOrientation(int orientation) {
    if (!VALID_ORIENTATIONS.contains(orientation)) {
      throw new IllegalArgumentException("Invalid orientation: " + orientation);
    } else {
      this.orientation = orientation;
      this.reset(false);
      this.invalidate();
      this.requestLayout();
    }
  }

  public final void setImage(ImageSource imageSource) {
    this.setImage(imageSource, (ImageSource)null, (ImageViewState)null);
  }

  public final void setBitmapAndFileUri(Bitmap bitmap, Uri fileUri) {
    int maxLoader = Utils.getMaxLoader();
    if (bitmap != null && bitmap.getWidth() < maxLoader && bitmap.getHeight() < maxLoader) {
      this.setImage(ImageSource.bitmap(bitmap));
      float bitmapHeight = (float)bitmap.getHeight();
      float bitmapWidth = (float)bitmap.getWidth();
      int viewHeight = Utils.getScreenHeight(this.getContext());
      int viewWidth = Utils.getScreenWidth(this.getContext());
      float heightScale = (float)viewHeight / bitmapHeight;
      float widthScale = (float)viewWidth / bitmapWidth;
      if ((double)(widthScale / heightScale) >= 1.5D) {
        this.setMaxScale(widthScale * 10.0F);
        PointF pointF = new PointF((float)this.getWidth() / widthScale / 2.0F, (float)this.getHeight() / widthScale / 2.0F);
        this.setScaleAndCenter(widthScale, pointF);
      }
    } else if (fileUri != null) {
      this.setImage(ImageSource.uri(fileUri));
    }

  }

  public final void setBitmapFitX(Bitmap bitmap) {
    int maxLoader = Utils.getMaxLoader();
    if (bitmap != null && bitmap.getWidth() < maxLoader && bitmap.getHeight() < maxLoader) {
      this.setImage(ImageSource.bitmap(bitmap));
      float bitmapWidth = (float)bitmap.getWidth();
      int viewWidth = Utils.getScreenWidth(this.getContext());
      float widthScale = (float)viewWidth / bitmapWidth;
      this.setMaxScale(widthScale * 10.0F);
      PointF pointF = new PointF((float)this.getWidth() / widthScale / 2.0F, (float)this.getHeight() / widthScale / 2.0F);
      this.setScaleAndCenter(widthScale, pointF);
    }

  }

  public final void setImage(ImageSource imageSource, ImageViewState state) {
    this.setImage(imageSource, (ImageSource)null, state);
  }

  public final void setImage(ImageSource imageSource, ImageSource previewSource) {
    this.setImage(imageSource, previewSource, (ImageViewState)null);
  }

  public final void setImage(ImageSource imageSource, ImageSource previewSource, ImageViewState state) {
    if (imageSource == null) {
      throw new NullPointerException("imageSource must not be null");
    } else {
      this.reset(true);
      if (state != null) {
        this.restoreState(state);
      }

      if (previewSource != null) {
        if (imageSource.getBitmap() != null) {
          throw new IllegalArgumentException("Preview image cannot be used when a bitmap is provided for the main image");
        }

        if (imageSource.getSWidth() <= 0 || imageSource.getSHeight() <= 0) {
          throw new IllegalArgumentException("Preview image cannot be used unless dimensions are provided for the main image");
        }

        this.sWidth = imageSource.getSWidth();
        this.sHeight = imageSource.getSHeight();
        this.pRegion = previewSource.getSRegion();
        if (previewSource.getBitmap() != null) {
          this.bitmapIsCached = previewSource.isCached();
          this.onPreviewLoaded(previewSource.getBitmap());
        } else {
          Uri uri = previewSource.getUri();
          if (uri == null && previewSource.getResource() != null) {
            uri = Uri.parse("android.resource://" + this.getContext().getPackageName() + "/" + previewSource.getResource());
          }

          io.rong.subscaleview.SubsamplingScaleImageView.BitmapLoadTask task = new io.rong.subscaleview.SubsamplingScaleImageView.BitmapLoadTask(this, this.getContext(), this.bitmapDecoderFactory, uri, true);
          this.execute(task);
        }
      }

      if (imageSource.getBitmap() != null && imageSource.getSRegion() != null) {
        this.onImageLoaded(Bitmap.createBitmap(imageSource.getBitmap(), imageSource.getSRegion().left, imageSource.getSRegion().top, imageSource.getSRegion().width(), imageSource.getSRegion().height()), 0, false);
      } else if (imageSource.getBitmap() != null) {
        this.onImageLoaded(imageSource.getBitmap(), 0, imageSource.isCached());
      } else {
        this.sRegion = imageSource.getSRegion();
        this.uri = imageSource.getUri();
        if (this.uri == null && imageSource.getResource() != null) {
          this.uri = Uri.parse("android.resource://" + this.getContext().getPackageName() + "/" + imageSource.getResource());
        }

        if (!imageSource.getTile() && this.sRegion == null) {
          io.rong.subscaleview.SubsamplingScaleImageView.BitmapLoadTask task = new io.rong.subscaleview.SubsamplingScaleImageView.BitmapLoadTask(this, this.getContext(), this.bitmapDecoderFactory, this.uri, false);
          this.execute(task);
        } else {
          io.rong.subscaleview.SubsamplingScaleImageView.TilesInitTask task = new io.rong.subscaleview.SubsamplingScaleImageView.TilesInitTask(this, this.getContext(), this.regionDecoderFactory, this.uri);
          this.execute(task);
        }
      }

    }
  }

  private void reset(boolean newImage) {
    this.debug("reset newImage=" + newImage);
    this.scale = 0.0F;
    this.scaleStart = 0.0F;
    this.vTranslate = null;
    this.vTranslateStart = null;
    this.vTranslateBefore = null;
    this.pendingScale = 0.0F;
    this.sPendingCenter = null;
    this.sRequestedCenter = null;
    this.isZooming = false;
    this.isPanning = false;
    this.isQuickScaling = false;
    this.maxTouchCount = 0;
    this.fullImageSampleSize = 0;
    this.vCenterStart = null;
    this.vDistStart = 0.0F;
    this.quickScaleLastDistance = 0.0F;
    this.quickScaleMoved = false;
    this.quickScaleSCenter = null;
    this.quickScaleVLastPoint = null;
    this.quickScaleVStart = null;
    this.anim = null;
    this.satTemp = null;
    this.matrix = null;
    this.sRect = null;
    if (newImage) {
      this.uri = null;
      this.decoderLock.writeLock().lock();

      try {
        if (this.decoder != null) {
          this.decoder.recycle();
          this.decoder = null;
        }
      } finally {
        this.decoderLock.writeLock().unlock();
      }

      if (this.bitmap != null && !this.bitmapIsCached) {
        this.bitmap.recycle();
      }

      if (this.bitmap != null && this.bitmapIsCached && this.onImageEventListener != null) {
        this.onImageEventListener.onPreviewReleased();
      }

      this.sWidth = 0;
      this.sHeight = 0;
      this.sOrientation = 0;
      this.sRegion = null;
      this.pRegion = null;
      this.readySent = false;
      this.imageLoadedSent = false;
      this.bitmap = null;
      this.bitmapIsPreview = false;
      this.bitmapIsCached = false;
    }

    if (this.tileMap != null) {
      Iterator var2 = this.tileMap.entrySet().iterator();

      while(var2.hasNext()) {
        Entry<Integer, List<io.rong.subscaleview.SubsamplingScaleImageView.Tile>> tileMapEntry = (Entry)var2.next();
        Iterator var4 = ((List)tileMapEntry.getValue()).iterator();

        while(var4.hasNext()) {
          io.rong.subscaleview.SubsamplingScaleImageView.Tile tile = (io.rong.subscaleview.SubsamplingScaleImageView.Tile)var4.next();
          tile.visible = false;
          if (tile.bitmap != null) {
            tile.bitmap.recycle();
            tile.bitmap = null;
          }
        }
      }

      this.tileMap = null;
    }

    this.setGestureDetector(this.getContext());
  }

  private void setGestureDetector(final Context context) {
    this.detector = new GestureDetector(context, new SimpleOnGestureListener() {
      public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (io.rong.subscaleview.SubsamplingScaleImageView.this.panEnabled && io.rong.subscaleview.SubsamplingScaleImageView.this.readySent && io.rong.subscaleview.SubsamplingScaleImageView.this.vTranslate != null && e1 != null && e2 != null && (Math.abs(e1.getX() - e2.getX()) > 50.0F || Math.abs(e1.getY() - e2.getY()) > 50.0F) && (Math.abs(velocityX) > 500.0F || Math.abs(velocityY) > 500.0F) && !io.rong.subscaleview.SubsamplingScaleImageView.this.isZooming) {
          PointF vTranslateEnd = new PointF(io.rong.subscaleview.SubsamplingScaleImageView.this.vTranslate.x + velocityX * 0.25F, io.rong.subscaleview.SubsamplingScaleImageView.this.vTranslate.y + velocityY * 0.25F);
          float sCenterXEnd = ((float)(io.rong.subscaleview.SubsamplingScaleImageView.this.getWidth() / 2) - vTranslateEnd.x) / io.rong.subscaleview.SubsamplingScaleImageView.this.scale;
          float sCenterYEnd = ((float)(io.rong.subscaleview.SubsamplingScaleImageView.this.getHeight() / 2) - vTranslateEnd.y) / io.rong.subscaleview.SubsamplingScaleImageView.this.scale;
          (io.rong.subscaleview.SubsamplingScaleImageView.this.new AnimationBuilder(new PointF(sCenterXEnd, sCenterYEnd))).withEasing(1).withPanLimited(false).withOrigin(3).start();
          return true;
        } else {
          return super.onFling(e1, e2, velocityX, velocityY);
        }
      }

      public boolean onSingleTapConfirmed(MotionEvent e) {
        io.rong.subscaleview.SubsamplingScaleImageView.this.performClick();
        return true;
      }

      public boolean onDoubleTap(MotionEvent e) {
        if (io.rong.subscaleview.SubsamplingScaleImageView.this.zoomEnabled && io.rong.subscaleview.SubsamplingScaleImageView.this.readySent && io.rong.subscaleview.SubsamplingScaleImageView.this.vTranslate != null) {
          io.rong.subscaleview.SubsamplingScaleImageView.this.setGestureDetector(context);
          if (io.rong.subscaleview.SubsamplingScaleImageView.this.quickScaleEnabled) {
            io.rong.subscaleview.SubsamplingScaleImageView.this.vCenterStart = new PointF(e.getX(), e.getY());
            io.rong.subscaleview.SubsamplingScaleImageView.this.vTranslateStart = new PointF(io.rong.subscaleview.SubsamplingScaleImageView.this.vTranslate.x, io.rong.subscaleview.SubsamplingScaleImageView.this.vTranslate.y);
            io.rong.subscaleview.SubsamplingScaleImageView.this.scaleStart = io.rong.subscaleview.SubsamplingScaleImageView.this.scale;
            io.rong.subscaleview.SubsamplingScaleImageView.this.isQuickScaling = true;
            io.rong.subscaleview.SubsamplingScaleImageView.this.isZooming = true;
            io.rong.subscaleview.SubsamplingScaleImageView.this.quickScaleLastDistance = -1.0F;
            io.rong.subscaleview.SubsamplingScaleImageView.this.quickScaleSCenter = io.rong.subscaleview.SubsamplingScaleImageView.this.viewToSourceCoord(io.rong.subscaleview.SubsamplingScaleImageView.this.vCenterStart);
            io.rong.subscaleview.SubsamplingScaleImageView.this.quickScaleVStart = new PointF(e.getX(), e.getY());
            io.rong.subscaleview.SubsamplingScaleImageView.this.quickScaleVLastPoint = new PointF(io.rong.subscaleview.SubsamplingScaleImageView.this.quickScaleSCenter.x, io.rong.subscaleview.SubsamplingScaleImageView.this.quickScaleSCenter.y);
            io.rong.subscaleview.SubsamplingScaleImageView.this.quickScaleMoved = false;
            return false;
          } else {
            io.rong.subscaleview.SubsamplingScaleImageView.this.doubleTapZoom(io.rong.subscaleview.SubsamplingScaleImageView.this.viewToSourceCoord(new PointF(e.getX(), e.getY())), new PointF(e.getX(), e.getY()));
            return true;
          }
        } else {
          return super.onDoubleTapEvent(e);
        }
      }
    });
    this.singleDetector = new GestureDetector(context, new SimpleOnGestureListener() {
      public boolean onSingleTapConfirmed(MotionEvent e) {
        io.rong.subscaleview.SubsamplingScaleImageView.this.performClick();
        return true;
      }
    });
  }

  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    this.debug("onSizeChanged %dx%d -> %dx%d", oldw, oldh, w, h);
    PointF sCenter = this.getCenter();
    if (this.readySent && sCenter != null) {
      this.anim = null;
      this.pendingScale = this.scale;
      this.sPendingCenter = sCenter;
    }

  }

  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
    int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
    int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
    int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
    boolean resizeWidth = widthSpecMode != 1073741824;
    boolean resizeHeight = heightSpecMode != 1073741824;
    int width = parentWidth;
    int height = parentHeight;
    if (this.sWidth > 0 && this.sHeight > 0) {
      if (resizeWidth && resizeHeight) {
        width = this.sWidth();
        height = this.sHeight();
      } else if (resizeHeight) {
        height = (int)((double)this.sHeight() / (double)this.sWidth() * (double)parentWidth);
      } else if (resizeWidth) {
        width = (int)((double)this.sWidth() / (double)this.sHeight() * (double)parentHeight);
      }
    }

    width = Math.max(width, this.getSuggestedMinimumWidth());
    height = Math.max(height, this.getSuggestedMinimumHeight());
    this.setMeasuredDimension(width, height);
  }

  public boolean onTouchEvent(@NonNull MotionEvent event) {
    if (this.anim != null && !this.anim.interruptible) {
      this.requestDisallowInterceptTouchEvent(true);
      return true;
    } else {
      if (this.anim != null && this.anim.listener != null) {
        try {
          this.anim.listener.onInterruptedByUser();
        } catch (Exception var4) {
          Log.w(TAG, "Error thrown by animation listener", var4);
        }
      }

      this.anim = null;
      if (this.vTranslate == null) {
        if (this.singleDetector != null) {
          this.singleDetector.onTouchEvent(event);
        }

        return true;
      } else if (this.isQuickScaling || this.detector != null && !this.detector.onTouchEvent(event)) {
        if (this.vTranslateStart == null) {
          this.vTranslateStart = new PointF(0.0F, 0.0F);
        }

        if (this.vTranslateBefore == null) {
          this.vTranslateBefore = new PointF(0.0F, 0.0F);
        }

        if (this.vCenterStart == null) {
          this.vCenterStart = new PointF(0.0F, 0.0F);
        }

        float scaleBefore = this.scale;
        this.vTranslateBefore.set(this.vTranslate);
        boolean handled = this.onTouchEventInternal(event);
        this.sendStateChanged(scaleBefore, this.vTranslateBefore, 2);
        return handled || super.onTouchEvent(event);
      } else {
        this.isZooming = false;
        this.isPanning = false;
        this.maxTouchCount = 0;
        return true;
      }
    }
  }

  private boolean onTouchEventInternal(@NonNull MotionEvent event) {
    int touchCount = event.getPointerCount();
    switch(event.getAction()) {
      case 0:
      case 5:
      case 261:
        this.anim = null;
        this.requestDisallowInterceptTouchEvent(true);
        this.maxTouchCount = Math.max(this.maxTouchCount, touchCount);
        if (touchCount >= 2) {
          if (this.zoomEnabled) {
            float distance = this.distance(event.getX(0), event.getX(1), event.getY(0), event.getY(1));
            this.scaleStart = this.scale;
            this.vDistStart = distance;
            this.vTranslateStart.set(this.vTranslate.x, this.vTranslate.y);
            this.vCenterStart.set((event.getX(0) + event.getX(1)) / 2.0F, (event.getY(0) + event.getY(1)) / 2.0F);
          } else {
            this.maxTouchCount = 0;
          }

          this.handler.removeMessages(1);
        } else if (!this.isQuickScaling) {
          this.vTranslateStart.set(this.vTranslate.x, this.vTranslate.y);
          this.vCenterStart.set(event.getX(), event.getY());
          this.handler.sendEmptyMessageDelayed(1, 600L);
        }

        return true;
      case 1:
      case 6:
      case 262:
        this.handler.removeMessages(1);
        if (this.isQuickScaling) {
          this.isQuickScaling = false;
          if (!this.quickScaleMoved) {
            this.doubleTapZoom(this.quickScaleSCenter, this.vCenterStart);
          }
        }

        if (this.maxTouchCount <= 0 || !this.isZooming && !this.isPanning) {
          if (touchCount == 1) {
            this.isZooming = false;
            this.isPanning = false;
            this.maxTouchCount = 0;
          }

          return true;
        }

        if (this.isZooming && touchCount == 2) {
          this.isPanning = true;
          this.vTranslateStart.set(this.vTranslate.x, this.vTranslate.y);
          if (event.getActionIndex() == 1) {
            this.vCenterStart.set(event.getX(0), event.getY(0));
          } else {
            this.vCenterStart.set(event.getX(1), event.getY(1));
          }
        }

        if (touchCount < 3) {
          this.isZooming = false;
        }

        if (touchCount < 2) {
          this.isPanning = false;
          this.maxTouchCount = 0;
        }

        this.refreshRequiredTiles(true);
        return true;
      case 2:
        boolean consumed = false;
        if (this.maxTouchCount > 0) {
          float dx;
          float dy;
          float offset;
          float vLeftStart;
          float vTopStart;
          float vLeftNow;
          if (touchCount >= 2) {
            dx = this.distance(event.getX(0), event.getX(1), event.getY(0), event.getY(1));
            dy = (event.getX(0) + event.getX(1)) / 2.0F;
            offset = (event.getY(0) + event.getY(1)) / 2.0F;
            if (this.zoomEnabled && (this.distance(this.vCenterStart.x, dy, this.vCenterStart.y, offset) > 5.0F || Math.abs(dx - this.vDistStart) > 5.0F || this.isPanning)) {
              this.isZooming = true;
              this.isPanning = true;
              consumed = true;
              double previousScale = (double)this.scale;
              this.scale = Math.min(this.maxScale, dx / this.vDistStart * this.scaleStart);
              if (this.scale <= this.minScale()) {
                this.vDistStart = dx;
                this.scaleStart = this.minScale();
                this.vCenterStart.set(dy, offset);
                this.vTranslateStart.set(this.vTranslate);
              } else if (this.panEnabled) {
                vLeftStart = this.vCenterStart.x - this.vTranslateStart.x;
                vLeftStart = this.vCenterStart.y - this.vTranslateStart.y;
                vTopStart = vLeftStart * (this.scale / this.scaleStart);
                vLeftNow = vLeftStart * (this.scale / this.scaleStart);
                this.vTranslate.x = dy - vTopStart;
                this.vTranslate.y = offset - vLeftNow;
                if (previousScale * (double)this.sHeight() < (double)this.getHeight() && this.scale * (float)this.sHeight() >= (float)this.getHeight() || previousScale * (double)this.sWidth() < (double)this.getWidth() && this.scale * (float)this.sWidth() >= (float)this.getWidth()) {
                  this.fitToBounds(true);
                  this.vCenterStart.set(dy, offset);
                  this.vTranslateStart.set(this.vTranslate);
                  this.scaleStart = this.scale;
                  this.vDistStart = dx;
                }
              } else if (this.sRequestedCenter != null) {
                this.vTranslate.x = (float)(this.getWidth() / 2) - this.scale * this.sRequestedCenter.x;
                this.vTranslate.y = (float)(this.getHeight() / 2) - this.scale * this.sRequestedCenter.y;
              } else {
                this.vTranslate.x = (float)(this.getWidth() / 2) - this.scale * (float)(this.sWidth() / 2);
                this.vTranslate.y = (float)(this.getHeight() / 2) - this.scale * (float)(this.sHeight() / 2);
              }

              this.fitToBounds(true);
              this.refreshRequiredTiles(this.eagerLoadingEnabled);
            }
          } else {
            float multiplier;
            if (this.isQuickScaling) {
              dx = Math.abs(this.quickScaleVStart.y - event.getY()) * 2.0F + this.quickScaleThreshold;
              if (this.quickScaleLastDistance == -1.0F) {
                this.quickScaleLastDistance = dx;
              }

              boolean isUpwards = event.getY() > this.quickScaleVLastPoint.y;
              this.quickScaleVLastPoint.set(0.0F, event.getY());
              offset = Math.abs(1.0F - dx / this.quickScaleLastDistance) * 0.5F;
              if (offset > 0.03F || this.quickScaleMoved) {
                this.quickScaleMoved = true;
                multiplier = 1.0F;
                if (this.quickScaleLastDistance > 0.0F) {
                  multiplier = isUpwards ? 1.0F + offset : 1.0F - offset;
                }

                double previousScale = (double)this.scale;
                this.scale = Math.max(this.minScale(), Math.min(this.maxScale, this.scale * multiplier));
                if (this.panEnabled) {
                  vLeftStart = this.vCenterStart.x - this.vTranslateStart.x;
                  vTopStart = this.vCenterStart.y - this.vTranslateStart.y;
                  vLeftNow = vLeftStart * (this.scale / this.scaleStart);
                  float vTopNow = vTopStart * (this.scale / this.scaleStart);
                  this.vTranslate.x = this.vCenterStart.x - vLeftNow;
                  this.vTranslate.y = this.vCenterStart.y - vTopNow;
                  if (previousScale * (double)this.sHeight() < (double)this.getHeight() && this.scale * (float)this.sHeight() >= (float)this.getHeight() || previousScale * (double)this.sWidth() < (double)this.getWidth() && this.scale * (float)this.sWidth() >= (float)this.getWidth()) {
                    this.fitToBounds(true);
                    this.vCenterStart.set(this.sourceToViewCoord(this.quickScaleSCenter));
                    this.vTranslateStart.set(this.vTranslate);
                    this.scaleStart = this.scale;
                    dx = 0.0F;
                  }
                } else if (this.sRequestedCenter != null) {
                  this.vTranslate.x = (float)(this.getWidth() / 2) - this.scale * this.sRequestedCenter.x;
                  this.vTranslate.y = (float)(this.getHeight() / 2) - this.scale * this.sRequestedCenter.y;
                } else {
                  this.vTranslate.x = (float)(this.getWidth() / 2) - this.scale * (float)(this.sWidth() / 2);
                  this.vTranslate.y = (float)(this.getHeight() / 2) - this.scale * (float)(this.sHeight() / 2);
                }
              }

              this.quickScaleLastDistance = dx;
              this.fitToBounds(true);
              this.refreshRequiredTiles(this.eagerLoadingEnabled);
              consumed = true;
            } else if (!this.isZooming) {
              dx = Math.abs(event.getX() - this.vCenterStart.x);
              dy = Math.abs(event.getY() - this.vCenterStart.y);
              offset = this.density * 5.0F;
              if (dx > offset || dy > offset || this.isPanning) {
                consumed = true;
                this.vTranslate.x = this.vTranslateStart.x + (event.getX() - this.vCenterStart.x);
                this.vTranslate.y = this.vTranslateStart.y + (event.getY() - this.vCenterStart.y);
                multiplier = this.vTranslate.x;
                float lastY = this.vTranslate.y;
                this.fitToBounds(true);
                boolean atXEdge = multiplier != this.vTranslate.x;
                boolean atYEdge = lastY != this.vTranslate.y;
                boolean edgeXSwipe = atXEdge && dx > dy && !this.isPanning;
                boolean edgeYSwipe = atYEdge && dy > dx && !this.isPanning;
                boolean yPan = lastY == this.vTranslate.y && dy > offset * 3.0F;
                if (edgeXSwipe || edgeYSwipe || atXEdge && atYEdge && !yPan && !this.isPanning) {
                  if (dx > offset || dy > offset) {
                    this.maxTouchCount = 0;
                    this.handler.removeMessages(1);
                    this.requestDisallowInterceptTouchEvent(false);
                  }
                } else {
                  this.isPanning = true;
                }

                if (!this.panEnabled) {
                  this.vTranslate.x = this.vTranslateStart.x;
                  this.vTranslate.y = this.vTranslateStart.y;
                  this.requestDisallowInterceptTouchEvent(false);
                }

                this.refreshRequiredTiles(this.eagerLoadingEnabled);
              }
            }
          }
        }

        if (consumed) {
          this.handler.removeMessages(1);
          this.invalidate();
          return true;
        }
      default:
        return false;
    }
  }

  private void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    ViewParent parent = this.getParent();
    if (parent != null) {
      parent.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

  }

  private void doubleTapZoom(PointF sCenter, PointF vFocus) {
    if (!this.panEnabled) {
      if (this.sRequestedCenter != null) {
        sCenter.x = this.sRequestedCenter.x;
        sCenter.y = this.sRequestedCenter.y;
      } else {
        sCenter.x = (float)(this.sWidth() / 2);
        sCenter.y = (float)(this.sHeight() / 2);
      }
    }

    float doubleTapZoomScale = Math.min(this.maxScale, this.doubleTapZoomScale);
    boolean zoomIn = (double)this.scale <= (double)doubleTapZoomScale * 0.9D || this.scale == this.minScale;
    float targetScale = zoomIn ? doubleTapZoomScale : this.minScale();
    if (this.doubleTapZoomStyle == 3) {
      this.setScaleAndCenter(targetScale, sCenter);
    } else if (this.doubleTapZoomStyle != 2 && zoomIn && this.panEnabled) {
      if (this.doubleTapZoomStyle == 1) {
        (new io.rong.subscaleview.SubsamplingScaleImageView.AnimationBuilder(targetScale, sCenter, vFocus)).withInterruptible(false).withDuration((long)this.doubleTapZoomDuration).withOrigin(4).start();
      }
    } else {
      (new io.rong.subscaleview.SubsamplingScaleImageView.AnimationBuilder(targetScale, sCenter)).withInterruptible(false).withDuration((long)this.doubleTapZoomDuration).withOrigin(4).start();
    }

    this.invalidate();
  }

  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    this.createPaints();
    if (this.sWidth != 0 && this.sHeight != 0 && this.getWidth() != 0 && this.getHeight() != 0) {
      if (this.tileMap == null && this.decoder != null) {
        this.initialiseBaseLayer(this.getMaxBitmapDimensions(canvas));
      }

      if (this.checkReady()) {
        this.preDraw();
        float xScale;
        if (this.anim != null && this.anim.vFocusStart != null) {
          xScale = this.scale;
          if (this.vTranslateBefore == null) {
            this.vTranslateBefore = new PointF(0.0F, 0.0F);
          }

          this.vTranslateBefore.set(this.vTranslate);
          long scaleElapsed = System.currentTimeMillis() - this.anim.time;
          boolean finished = scaleElapsed > this.anim.duration;
          scaleElapsed = Math.min(scaleElapsed, this.anim.duration);
          this.scale = this.ease(this.anim.easing, scaleElapsed, this.anim.scaleStart, this.anim.scaleEnd - this.anim.scaleStart, this.anim.duration);
          float vFocusNowX = this.ease(this.anim.easing, scaleElapsed, this.anim.vFocusStart.x, this.anim.vFocusEnd.x - this.anim.vFocusStart.x, this.anim.duration);
          float vFocusNowY = this.ease(this.anim.easing, scaleElapsed, this.anim.vFocusStart.y, this.anim.vFocusEnd.y - this.anim.vFocusStart.y, this.anim.duration);
          this.vTranslate.x -= this.sourceToViewX(this.anim.sCenterEnd.x) - vFocusNowX;
          this.vTranslate.y -= this.sourceToViewY(this.anim.sCenterEnd.y) - vFocusNowY;
          this.fitToBounds(finished || this.anim.scaleStart == this.anim.scaleEnd);
          this.sendStateChanged(xScale, this.vTranslateBefore, this.anim.origin);
          this.refreshRequiredTiles(finished);
          if (finished) {
            if (this.anim.listener != null) {
              try {
                this.anim.listener.onComplete();
              } catch (Exception var9) {
                Log.w(TAG, "Error thrown by animation listener", var9);
              }
            }

            this.anim = null;
          }

          this.invalidate();
        }

        if (this.tileMap != null && this.isBaseLayerReady()) {
          int sampleSize = Math.min(this.fullImageSampleSize, this.calculateInSampleSize(this.scale));
          boolean hasMissingTiles = false;
          Iterator var4 = this.tileMap.entrySet().iterator();

          label205:
          while(true) {
            Entry tileMapEntry;
            Iterator var18;
            io.rong.subscaleview.SubsamplingScaleImageView.Tile tile;
            do {
              if (!var4.hasNext()) {
                var4 = this.tileMap.entrySet().iterator();

                while(true) {
                  do {
                    if (!var4.hasNext()) {
                      break label205;
                    }

                    tileMapEntry = (Entry)var4.next();
                  } while((Integer)tileMapEntry.getKey() != sampleSize && !hasMissingTiles);

                  var18 = ((List)tileMapEntry.getValue()).iterator();

                  while(var18.hasNext()) {
                    tile = (io.rong.subscaleview.SubsamplingScaleImageView.Tile)var18.next();
                    this.sourceToViewRect(tile.sRect, tile.vRect);
                    if (!tile.loading && tile.bitmap != null) {
                      if (this.tileBgPaint != null) {
                        canvas.drawRect(tile.vRect, this.tileBgPaint);
                      }

                      if (this.matrix == null) {
                        this.matrix = new Matrix();
                      }

                      this.matrix.reset();
                      this.setMatrixArray(this.srcArray, 0.0F, 0.0F, (float)tile.bitmap.getWidth(), 0.0F, (float)tile.bitmap.getWidth(), (float)tile.bitmap.getHeight(), 0.0F, (float)tile.bitmap.getHeight());
                      if (this.getRequiredRotation() == 0) {
                        this.setMatrixArray(this.dstArray, (float)tile.vRect.left, (float)tile.vRect.top, (float)tile.vRect.right, (float)tile.vRect.top, (float)tile.vRect.right, (float)tile.vRect.bottom, (float)tile.vRect.left, (float)tile.vRect.bottom);
                      } else if (this.getRequiredRotation() == 90) {
                        this.setMatrixArray(this.dstArray, (float)tile.vRect.right, (float)tile.vRect.top, (float)tile.vRect.right, (float)tile.vRect.bottom, (float)tile.vRect.left, (float)tile.vRect.bottom, (float)tile.vRect.left, (float)tile.vRect.top);
                      } else if (this.getRequiredRotation() == 180) {
                        this.setMatrixArray(this.dstArray, (float)tile.vRect.right, (float)tile.vRect.bottom, (float)tile.vRect.left, (float)tile.vRect.bottom, (float)tile.vRect.left, (float)tile.vRect.top, (float)tile.vRect.right, (float)tile.vRect.top);
                      } else if (this.getRequiredRotation() == 270) {
                        this.setMatrixArray(this.dstArray, (float)tile.vRect.left, (float)tile.vRect.bottom, (float)tile.vRect.left, (float)tile.vRect.top, (float)tile.vRect.right, (float)tile.vRect.top, (float)tile.vRect.right, (float)tile.vRect.bottom);
                      }

                      this.matrix.setPolyToPoly(this.srcArray, 0, this.dstArray, 0, 4);
                      canvas.drawBitmap(tile.bitmap, this.matrix, this.bitmapPaint);
                      if (this.debug) {
                        canvas.drawRect(tile.vRect, this.debugLinePaint);
                      }
                    } else if (tile.loading && this.debug) {
                      canvas.drawText("LOADING", (float)(tile.vRect.left + this.px(5)), (float)(tile.vRect.top + this.px(35)), this.debugTextPaint);
                    }

                    if (tile.visible && this.debug) {
                      canvas.drawText("ISS " + tile.sampleSize + " RECT " + tile.sRect.top + "," + tile.sRect.left + "," + tile.sRect.bottom + "," + tile.sRect.right, (float)(tile.vRect.left + this.px(5)), (float)(tile.vRect.top + this.px(15)), this.debugTextPaint);
                    }
                  }
                }
              }

              tileMapEntry = (Entry)var4.next();
            } while((Integer)tileMapEntry.getKey() != sampleSize);

            var18 = ((List)tileMapEntry.getValue()).iterator();

            while(true) {
              do {
                do {
                  if (!var18.hasNext()) {
                    continue label205;
                  }

                  tile = (io.rong.subscaleview.SubsamplingScaleImageView.Tile)var18.next();
                } while(!tile.visible);
              } while(!tile.loading && tile.bitmap != null);

              hasMissingTiles = true;
            }
          }
        } else if (this.bitmap != null) {
          xScale = this.scale;
          float yScale = this.scale;
          if (this.bitmapIsPreview) {
            xScale = this.scale * ((float)this.sWidth / (float)this.bitmap.getWidth());
            yScale = this.scale * ((float)this.sHeight / (float)this.bitmap.getHeight());
          }

          if (this.matrix == null) {
            this.matrix = new Matrix();
          }

          this.matrix.reset();
          this.matrix.postScale(xScale, yScale);
          this.matrix.postRotate((float)this.getRequiredRotation());
          this.matrix.postTranslate(this.vTranslate.x, this.vTranslate.y);
          if (this.getRequiredRotation() == 180) {
            this.matrix.postTranslate(this.scale * (float)this.sWidth, this.scale * (float)this.sHeight);
          } else if (this.getRequiredRotation() == 90) {
            this.matrix.postTranslate(this.scale * (float)this.sHeight, 0.0F);
          } else if (this.getRequiredRotation() == 270) {
            this.matrix.postTranslate(0.0F, this.scale * (float)this.sWidth);
          }

          if (this.tileBgPaint != null) {
            if (this.sRect == null) {
              this.sRect = new RectF();
            }

            this.sRect.set(0.0F, 0.0F, this.bitmapIsPreview ? (float)this.bitmap.getWidth() : (float)this.sWidth, this.bitmapIsPreview ? (float)this.bitmap.getHeight() : (float)this.sHeight);
            this.matrix.mapRect(this.sRect);
            canvas.drawRect(this.sRect, this.tileBgPaint);
          }

          canvas.drawBitmap(this.bitmap, this.matrix, this.bitmapPaint);
        }

        if (this.debug) {
          canvas.drawText("Scale: " + String.format(Locale.ENGLISH, "%.2f", this.scale) + " (" + String.format(Locale.ENGLISH, "%.2f", this.minScale()) + " - " + String.format(Locale.ENGLISH, "%.2f", this.maxScale) + ")", (float)this.px(5), (float)this.px(15), this.debugTextPaint);
          canvas.drawText("Translate: " + String.format(Locale.ENGLISH, "%.2f", this.vTranslate.x) + ":" + String.format(Locale.ENGLISH, "%.2f", this.vTranslate.y), (float)this.px(5), (float)this.px(30), this.debugTextPaint);
          PointF center = this.getCenter();
          canvas.drawText("Source center: " + String.format(Locale.ENGLISH, "%.2f", center.x) + ":" + String.format(Locale.ENGLISH, "%.2f", center.y), (float)this.px(5), (float)this.px(45), this.debugTextPaint);
          if (this.anim != null) {
            PointF vCenterStart = this.sourceToViewCoord(this.anim.sCenterStart);
            PointF vCenterEndRequested = this.sourceToViewCoord(this.anim.sCenterEndRequested);
            PointF vCenterEnd = this.sourceToViewCoord(this.anim.sCenterEnd);
            canvas.drawCircle(vCenterStart.x, vCenterStart.y, (float)this.px(10), this.debugLinePaint);
            this.debugLinePaint.setColor(-65536);
            canvas.drawCircle(vCenterEndRequested.x, vCenterEndRequested.y, (float)this.px(20), this.debugLinePaint);
            this.debugLinePaint.setColor(-16776961);
            canvas.drawCircle(vCenterEnd.x, vCenterEnd.y, (float)this.px(25), this.debugLinePaint);
            this.debugLinePaint.setColor(-16711681);
            canvas.drawCircle((float)(this.getWidth() / 2), (float)(this.getHeight() / 2), (float)this.px(30), this.debugLinePaint);
          }

          if (this.vCenterStart != null) {
            this.debugLinePaint.setColor(-65536);
            canvas.drawCircle(this.vCenterStart.x, this.vCenterStart.y, (float)this.px(20), this.debugLinePaint);
          }

          if (this.quickScaleSCenter != null) {
            this.debugLinePaint.setColor(-16776961);
            canvas.drawCircle(this.sourceToViewX(this.quickScaleSCenter.x), this.sourceToViewY(this.quickScaleSCenter.y), (float)this.px(35), this.debugLinePaint);
          }

          if (this.quickScaleVStart != null && this.isQuickScaling) {
            this.debugLinePaint.setColor(-16711681);
            canvas.drawCircle(this.quickScaleVStart.x, this.quickScaleVStart.y, (float)this.px(30), this.debugLinePaint);
          }

          this.debugLinePaint.setColor(-65281);
        }

      }
    }
  }

  private void setMatrixArray(float[] array, float f0, float f1, float f2, float f3, float f4, float f5, float f6, float f7) {
    array[0] = f0;
    array[1] = f1;
    array[2] = f2;
    array[3] = f3;
    array[4] = f4;
    array[5] = f5;
    array[6] = f6;
    array[7] = f7;
  }

  private boolean isBaseLayerReady() {
    if (this.bitmap != null && !this.bitmapIsPreview) {
      return true;
    } else if (this.tileMap == null) {
      return false;
    } else {
      boolean baseLayerReady = true;
      Iterator var2 = this.tileMap.entrySet().iterator();

      label42:
      while(true) {
        Entry tileMapEntry;
        do {
          if (!var2.hasNext()) {
            return baseLayerReady;
          }

          tileMapEntry = (Entry)var2.next();
        } while((Integer)tileMapEntry.getKey() != this.fullImageSampleSize);

        Iterator var4 = ((List)tileMapEntry.getValue()).iterator();

        while(true) {
          io.rong.subscaleview.SubsamplingScaleImageView.Tile tile;
          do {
            if (!var4.hasNext()) {
              continue label42;
            }

            tile = (io.rong.subscaleview.SubsamplingScaleImageView.Tile)var4.next();
          } while(!tile.loading && tile.bitmap != null);

          baseLayerReady = false;
        }
      }
    }
  }

  private boolean checkReady() {
    boolean ready = this.getWidth() > 0 && this.getHeight() > 0 && this.sWidth > 0 && this.sHeight > 0 && (this.bitmap != null || this.isBaseLayerReady());
    if (!this.readySent && ready) {
      this.preDraw();
      this.readySent = true;
      this.onReady();
      if (this.onImageEventListener != null) {
        this.onImageEventListener.onReady();
      }
    }

    return ready;
  }

  private boolean checkImageLoaded() {
    boolean imageLoaded = this.isBaseLayerReady();
    if (!this.imageLoadedSent && imageLoaded) {
      this.preDraw();
      this.imageLoadedSent = true;
      this.onImageLoaded();
      if (this.onImageEventListener != null) {
        this.onImageEventListener.onImageLoaded();
      }
    }

    return imageLoaded;
  }

  private void createPaints() {
    if (this.bitmapPaint == null) {
      this.bitmapPaint = new Paint();
      this.bitmapPaint.setAntiAlias(true);
      this.bitmapPaint.setFilterBitmap(true);
      this.bitmapPaint.setDither(true);
    }

    if ((this.debugTextPaint == null || this.debugLinePaint == null) && this.debug) {
      this.debugTextPaint = new Paint();
      this.debugTextPaint.setTextSize((float)this.px(12));
      this.debugTextPaint.setColor(-65281);
      this.debugTextPaint.setStyle(Style.FILL);
      this.debugLinePaint = new Paint();
      this.debugLinePaint.setColor(-65281);
      this.debugLinePaint.setStyle(Style.STROKE);
      this.debugLinePaint.setStrokeWidth((float)this.px(1));
    }

  }

  private synchronized void initialiseBaseLayer(Point maxTileDimensions) {
    this.debug("initialiseBaseLayer maxTileDimensions=%dx%d", maxTileDimensions.x, maxTileDimensions.y);
    this.satTemp = new io.rong.subscaleview.SubsamplingScaleImageView.ScaleAndTranslate(0.0F, new PointF(0.0F, 0.0F));
    this.fitToBounds(true, this.satTemp);
    this.fullImageSampleSize = this.calculateInSampleSize(this.satTemp.scale);
    if (this.fullImageSampleSize > 1) {
      this.fullImageSampleSize /= 2;
    }

    if (this.fullImageSampleSize == 1 && this.sRegion == null && this.sWidth() < maxTileDimensions.x && this.sHeight() < maxTileDimensions.y) {
      this.decoder.recycle();
      this.decoder = null;
      io.rong.subscaleview.SubsamplingScaleImageView.BitmapLoadTask task = new io.rong.subscaleview.SubsamplingScaleImageView.BitmapLoadTask(this, this.getContext(), this.bitmapDecoderFactory, this.uri, false);
      this.execute(task);
    } else {
      this.initialiseTileMap(maxTileDimensions);
      List<io.rong.subscaleview.SubsamplingScaleImageView.Tile> baseGrid = (List)this.tileMap.get(this.fullImageSampleSize);
      Iterator var3 = baseGrid.iterator();

      while(var3.hasNext()) {
        io.rong.subscaleview.SubsamplingScaleImageView.Tile baseTile = (io.rong.subscaleview.SubsamplingScaleImageView.Tile)var3.next();
        io.rong.subscaleview.SubsamplingScaleImageView.TileLoadTask task = new io.rong.subscaleview.SubsamplingScaleImageView.TileLoadTask(this, this.decoder, baseTile);
        this.execute(task);
      }

      this.refreshRequiredTiles(true);
    }

  }

  private void refreshRequiredTiles(boolean load) {
    if (this.decoder != null && this.tileMap != null) {
      int sampleSize = Math.min(this.fullImageSampleSize, this.calculateInSampleSize(this.scale));
      Iterator var3 = this.tileMap.entrySet().iterator();

      while(var3.hasNext()) {
        Entry<Integer, List<io.rong.subscaleview.SubsamplingScaleImageView.Tile>> tileMapEntry = (Entry)var3.next();
        Iterator var5 = ((List)tileMapEntry.getValue()).iterator();

        while(var5.hasNext()) {
          io.rong.subscaleview.SubsamplingScaleImageView.Tile tile = (io.rong.subscaleview.SubsamplingScaleImageView.Tile)var5.next();
          if (tile.sampleSize < sampleSize || tile.sampleSize > sampleSize && tile.sampleSize != this.fullImageSampleSize) {
            tile.visible = false;
            if (tile.bitmap != null) {
              tile.bitmap.recycle();
              tile.bitmap = null;
            }
          }

          if (tile.sampleSize == sampleSize) {
            if (this.tileVisible(tile)) {
              tile.visible = true;
              if (!tile.loading && tile.bitmap == null && load) {
                io.rong.subscaleview.SubsamplingScaleImageView.TileLoadTask task = new io.rong.subscaleview.SubsamplingScaleImageView.TileLoadTask(this, this.decoder, tile);
                this.execute(task);
              }
            } else if (tile.sampleSize != this.fullImageSampleSize) {
              tile.visible = false;
              if (tile.bitmap != null) {
                tile.bitmap.recycle();
                tile.bitmap = null;
              }
            }
          } else if (tile.sampleSize == this.fullImageSampleSize) {
            tile.visible = true;
          }
        }
      }

    }
  }

  private boolean tileVisible(io.rong.subscaleview.SubsamplingScaleImageView.Tile tile) {
    float sVisLeft = this.viewToSourceX(0.0F);
    float sVisRight = this.viewToSourceX((float)this.getWidth());
    float sVisTop = this.viewToSourceY(0.0F);
    float sVisBottom = this.viewToSourceY((float)this.getHeight());
    return sVisLeft <= (float)tile.sRect.right && (float)tile.sRect.left <= sVisRight && sVisTop <= (float)tile.sRect.bottom && (float)tile.sRect.top <= sVisBottom;
  }

  private void preDraw() {
    if (this.getWidth() != 0 && this.getHeight() != 0 && this.sWidth > 0 && this.sHeight > 0) {
      if (this.sPendingCenter != null && this.pendingScale != null) {
        this.scale = this.pendingScale;
        if (this.vTranslate == null) {
          this.vTranslate = new PointF();
        }

        this.vTranslate.x = (float)(this.getWidth() / 2) - this.scale * this.sPendingCenter.x;
        this.vTranslate.y = (float)(this.getHeight() / 2) - this.scale * this.sPendingCenter.y;
        this.sPendingCenter = null;
        this.pendingScale = null;
        this.fitToBounds(true);
        this.refreshRequiredTiles(true);
      }

      this.fitToBounds(false);
    }
  }

  private int calculateInSampleSize(float scale) {
    if (this.minimumTileDpi > 0) {
      DisplayMetrics metrics = this.getResources().getDisplayMetrics();
      float averageDpi = (metrics.xdpi + metrics.ydpi) / 2.0F;
      scale = (float)this.minimumTileDpi / averageDpi * scale;
    }

    int reqWidth = (int)((float)this.sWidth() * scale);
    int reqHeight = (int)((float)this.sHeight() * scale);
    int inSampleSize = 1;
    if (reqWidth != 0 && reqHeight != 0) {
      int power;
      if (this.sHeight() > reqHeight || this.sWidth() > reqWidth) {
        power = Math.round((float)this.sHeight() / (float)reqHeight);
        int widthRatio = Math.round((float)this.sWidth() / (float)reqWidth);
        inSampleSize = power < widthRatio ? power : widthRatio;
      }

      for(power = 1; power * 2 < inSampleSize; power *= 2) {
        ;
      }

      return power;
    } else {
      return 32;
    }
  }

  private void fitToBounds(boolean center, io.rong.subscaleview.SubsamplingScaleImageView.ScaleAndTranslate sat) {
    if (this.panLimit == 2 && this.isReady()) {
      center = false;
    }

    PointF vTranslate = sat.vTranslate;
    float scale = this.limitedScale(sat.scale);
    float scaleWidth = scale * (float)this.sWidth();
    float scaleHeight = scale * (float)this.sHeight();
    if (this.panLimit == 3 && this.isReady()) {
      vTranslate.x = Math.max(vTranslate.x, (float)(this.getWidth() / 2) - scaleWidth);
      vTranslate.y = Math.max(vTranslate.y, (float)(this.getHeight() / 2) - scaleHeight);
    } else if (center) {
      vTranslate.x = Math.max(vTranslate.x, (float)this.getWidth() - scaleWidth);
      vTranslate.y = Math.max(vTranslate.y, (float)this.getHeight() - scaleHeight);
    } else {
      vTranslate.x = Math.max(vTranslate.x, -scaleWidth);
      vTranslate.y = Math.max(vTranslate.y, -scaleHeight);
    }

    float xPaddingRatio = this.getPaddingLeft() <= 0 && this.getPaddingRight() <= 0 ? 0.5F : (float)this.getPaddingLeft() / (float)(this.getPaddingLeft() + this.getPaddingRight());
    float yPaddingRatio = this.getPaddingTop() <= 0 && this.getPaddingBottom() <= 0 ? 0.5F : (float)this.getPaddingTop() / (float)(this.getPaddingTop() + this.getPaddingBottom());
    float maxTx;
    float maxTy;
    if (this.panLimit == 3 && this.isReady()) {
      maxTx = (float)Math.max(0, this.getWidth() / 2);
      maxTy = (float)Math.max(0, this.getHeight() / 2);
    } else if (center) {
      maxTx = Math.max(0.0F, ((float)this.getWidth() - scaleWidth) * xPaddingRatio);
      maxTy = Math.max(0.0F, ((float)this.getHeight() - scaleHeight) * yPaddingRatio);
    } else {
      maxTx = (float)Math.max(0, this.getWidth());
      maxTy = (float)Math.max(0, this.getHeight());
    }

    vTranslate.x = Math.min(vTranslate.x, maxTx);
    vTranslate.y = Math.min(vTranslate.y, maxTy);
    sat.scale = scale;
  }

  private void fitToBounds(boolean center) {
    boolean init = false;
    if (this.vTranslate == null) {
      init = true;
      this.vTranslate = new PointF(0.0F, 0.0F);
    }

    if (this.satTemp == null) {
      this.satTemp = new io.rong.subscaleview.SubsamplingScaleImageView.ScaleAndTranslate(0.0F, new PointF(0.0F, 0.0F));
    }

    this.satTemp.scale = this.scale;
    this.satTemp.vTranslate.set(this.vTranslate);
    this.fitToBounds(center, this.satTemp);
    this.scale = this.satTemp.scale;
    this.vTranslate.set(this.satTemp.vTranslate);
    if (init && this.minimumScaleType != 4) {
      this.vTranslate.set(this.vTranslateForSCenter((float)(this.sWidth() / 2), (float)(this.sHeight() / 2), this.scale));
    }

  }

  private void initialiseTileMap(Point maxTileDimensions) {
    this.debug("initialiseTileMap maxTileDimensions=%dx%d", maxTileDimensions.x, maxTileDimensions.y);
    this.tileMap = new LinkedHashMap();
    int sampleSize = this.fullImageSampleSize;
    int xTiles = 1;
    int yTiles = 1;

    while(true) {
      int sTileWidth = this.sWidth() / xTiles;
      int sTileHeight = this.sHeight() / yTiles;
      int subTileWidth = sTileWidth / sampleSize;
      int subTileHeight = sTileHeight / sampleSize;
      Log.i("initialiseTileMap", " sampleSize = " + sampleSize);
      Log.i("initialiseTileMap", " sWidth() = " + this.sWidth() + " xTiles = " + xTiles);
      Log.i("initialiseTileMap", " sHeight() = " + this.sHeight() + " yTiles = " + yTiles);
      Log.i("initialiseTileMap", " sTileWidth = " + sTileWidth);
      Log.i("initialiseTileMap", " sTileWidth = " + sTileHeight);
      Log.i("initialiseTileMap", " subTileWidth = " + subTileWidth);
      Log.i("initialiseTileMap", " subTileHeight = " + subTileHeight);
      Log.i("initialiseTileMap", " maxTileDimensions.x = " + maxTileDimensions.x + " maxTileDimensions.y = " + maxTileDimensions.y);

      while(subTileWidth + xTiles + 1 > maxTileDimensions.x || (double)subTileWidth > (double)this.getWidth() * 1.25D && sampleSize < this.fullImageSampleSize) {
        ++xTiles;
        sTileWidth = this.sWidth() / xTiles;
        subTileWidth = sTileWidth / sampleSize;
      }

      while(subTileHeight + yTiles + 1 > maxTileDimensions.y || (double)subTileHeight > (double)this.getHeight() * 1.25D && sampleSize < this.fullImageSampleSize) {
        ++yTiles;
        sTileHeight = this.sHeight() / yTiles;
        subTileHeight = sTileHeight / sampleSize;
      }

      Log.i("initialiseTileMap", " 计算后的 xTiles = " + xTiles + " yTiles = " + yTiles + " sTileWidth = " + sTileWidth + " sTileHeight= " + sTileHeight);
      List<io.rong.subscaleview.SubsamplingScaleImageView.Tile> tileGrid = new ArrayList(xTiles * yTiles);

      for(int x = 0; x < xTiles; ++x) {
        for(int y = 0; y < yTiles; ++y) {
          io.rong.subscaleview.SubsamplingScaleImageView.Tile tile = new io.rong.subscaleview.SubsamplingScaleImageView.Tile();
          tile.sampleSize = sampleSize;
          tile.visible = sampleSize == this.fullImageSampleSize;
          tile.sRect = new Rect(x * sTileWidth, y * sTileHeight, x == xTiles - 1 ? this.sWidth() : (x + 1) * sTileWidth, y == yTiles - 1 ? this.sHeight() : (y + 1) * sTileHeight);
          tile.vRect = new Rect(0, 0, 0, 0);
          tile.fileSRect = new Rect(tile.sRect);
          tileGrid.add(tile);
        }
      }

      Log.i("initialiseTileMap", "tileGrid.size() = " + tileGrid.size());
      this.tileMap.put(sampleSize, tileGrid);
      if (sampleSize == 1) {
        return;
      }

      Log.i("initialiseTileMap", "----------------------------------------------");
      Log.i("initialiseTileMap", " 加大抽样率");
      sampleSize /= 2;
    }
  }

  private synchronized void onTilesInited(ImageRegionDecoder decoder, int sWidth, int sHeight, int sOrientation) {
    this.debug("onTilesInited sWidth=%d, sHeight=%d, sOrientation=%d", sWidth, sHeight, this.orientation);
    if (this.sWidth > 0 && this.sHeight > 0 && (this.sWidth != sWidth || this.sHeight != sHeight)) {
      this.reset(false);
      if (this.bitmap != null) {
        if (!this.bitmapIsCached) {
          this.bitmap.recycle();
        }

        this.bitmap = null;
        if (this.onImageEventListener != null && this.bitmapIsCached) {
          this.onImageEventListener.onPreviewReleased();
        }

        this.bitmapIsPreview = false;
        this.bitmapIsCached = false;
      }
    }

    this.decoder = decoder;
    this.sWidth = sWidth;
    this.sHeight = sHeight;
    this.sOrientation = sOrientation;
    this.checkReady();
    if (!this.checkImageLoaded() && this.maxTileWidth > 0 && this.maxTileWidth != 2147483647 && this.maxTileHeight > 0 && this.maxTileHeight != 2147483647 && this.getWidth() > 0 && this.getHeight() > 0) {
      this.initialiseBaseLayer(new Point(this.maxTileWidth, this.maxTileHeight));
    }

    this.invalidate();
    this.requestLayout();
  }

  private synchronized void onTileLoaded() {
    this.debug("onTileLoaded");
    this.checkReady();
    this.checkImageLoaded();
    if (this.isBaseLayerReady() && this.bitmap != null) {
      if (!this.bitmapIsCached) {
        this.bitmap.recycle();
      }

      this.bitmap = null;
      if (this.onImageEventListener != null && this.bitmapIsCached) {
        this.onImageEventListener.onPreviewReleased();
      }

      this.bitmapIsPreview = false;
      this.bitmapIsCached = false;
    }

    this.invalidate();
  }

  private synchronized void onPreviewLoaded(Bitmap previewBitmap) {
    this.debug("onPreviewLoaded");
    if (this.bitmap == null && !this.imageLoadedSent) {
      if (this.pRegion != null) {
        this.bitmap = Bitmap.createBitmap(previewBitmap, this.pRegion.left, this.pRegion.top, this.pRegion.width(), this.pRegion.height());
      } else {
        this.bitmap = previewBitmap;
      }

      this.bitmapIsPreview = true;
      if (this.checkReady()) {
        this.invalidate();
        this.requestLayout();
      }

    } else {
      previewBitmap.recycle();
    }
  }

  private synchronized void onImageLoaded(Bitmap bitmap, int sOrientation, boolean bitmapIsCached) {
    this.debug("onImageLoaded");
    if (this.sWidth > 0 && this.sHeight > 0 && (this.sWidth != bitmap.getWidth() || this.sHeight != bitmap.getHeight())) {
      this.reset(false);
    }

    if (this.bitmap != null && !this.bitmapIsCached) {
      this.bitmap.recycle();
    }

    if (this.bitmap != null && this.bitmapIsCached && this.onImageEventListener != null) {
      this.onImageEventListener.onPreviewReleased();
    }

    this.bitmapIsPreview = false;
    this.bitmapIsCached = bitmapIsCached;
    this.bitmap = bitmap;
    this.sWidth = bitmap.getWidth();
    this.sHeight = bitmap.getHeight();
    this.sOrientation = sOrientation;
    boolean ready = this.checkReady();
    boolean imageLoaded = this.checkImageLoaded();
    if (ready || imageLoaded) {
      this.invalidate();
      this.requestLayout();
    }

  }

  @AnyThread
  private int getExifOrientation(Context context, String sourceUri) {
    int exifOrientation = 0;
    if (sourceUri.startsWith("content")) {
      Cursor cursor = null;

      try {
        String[] columns = new String[]{"orientation"};
        cursor = context.getContentResolver().query(Uri.parse(sourceUri), columns, (String)null, (String[])null, (String)null);
        if (cursor != null && cursor.moveToFirst()) {
          int orientation = cursor.getInt(0);
          if (VALID_ORIENTATIONS.contains(orientation) && orientation != -1) {
            exifOrientation = orientation;
          } else {
            Log.w(TAG, "Unsupported orientation: " + orientation);
          }
        }
      } catch (Exception var12) {
        Log.w(TAG, "Could not get orientation of image from media store");
      } finally {
        if (cursor != null) {
          cursor.close();
        }

      }
    } else if (sourceUri.startsWith("file:///") && !sourceUri.startsWith("file:///android_asset/")) {
      try {
        ExifInterface exifInterface = new ExifInterface(sourceUri.substring("file:///".length() - 1));
        int orientationAttr = exifInterface.getAttributeInt("Orientation", 1);
        if (orientationAttr != 1 && orientationAttr != 0) {
          if (orientationAttr == 6) {
            exifOrientation = 90;
          } else if (orientationAttr == 3) {
            exifOrientation = 180;
          } else if (orientationAttr == 8) {
            exifOrientation = 270;
          } else {
            Log.w(TAG, "Unsupported EXIF orientation: " + orientationAttr);
          }
        } else {
          exifOrientation = 0;
        }
      } catch (Exception var11) {
        Log.w(TAG, "Could not get EXIF orientation of image");
      }
    }

    return exifOrientation;
  }

  private void execute(AsyncTask<Void, Void, ?> asyncTask) {
    asyncTask.executeOnExecutor(this.executor, new Void[0]);
  }

  private void restoreState(ImageViewState state) {
    if (state != null && state.getCenter() != null && VALID_ORIENTATIONS.contains(state.getOrientation())) {
      this.orientation = state.getOrientation();
      this.pendingScale = state.getScale();
      this.sPendingCenter = state.getCenter();
      this.invalidate();
    }

  }

  public void setMaxTileSize(int maxPixels) {
    this.maxTileWidth = maxPixels;
    this.maxTileHeight = maxPixels;
  }

  public void setMaxTileSize(int maxPixelsX, int maxPixelsY) {
    this.maxTileWidth = maxPixelsX;
    this.maxTileHeight = maxPixelsY;
  }

  private Point getMaxBitmapDimensions(Canvas canvas) {
    return new Point(Math.min(canvas.getMaximumBitmapWidth(), this.maxTileWidth), Math.min(canvas.getMaximumBitmapHeight(), this.maxTileHeight));
  }

  private int sWidth() {
    int rotation = this.getRequiredRotation();
    return rotation != 90 && rotation != 270 ? this.sWidth : this.sHeight;
  }

  private int sHeight() {
    int rotation = this.getRequiredRotation();
    return rotation != 90 && rotation != 270 ? this.sHeight : this.sWidth;
  }

  @AnyThread
  private void fileSRect(Rect sRect, Rect target) {
    if (this.getRequiredRotation() == 0) {
      target.set(sRect);
    } else if (this.getRequiredRotation() == 90) {
      target.set(sRect.top, this.sHeight - sRect.right, sRect.bottom, this.sHeight - sRect.left);
    } else if (this.getRequiredRotation() == 180) {
      target.set(this.sWidth - sRect.right, this.sHeight - sRect.bottom, this.sWidth - sRect.left, this.sHeight - sRect.top);
    } else {
      target.set(this.sWidth - sRect.bottom, sRect.left, this.sWidth - sRect.top, sRect.right);
    }

  }

  @AnyThread
  private int getRequiredRotation() {
    return this.orientation == -1 ? this.sOrientation : this.orientation;
  }

  private float distance(float x0, float x1, float y0, float y1) {
    float x = x0 - x1;
    float y = y0 - y1;
    return (float)Math.sqrt((double)(x * x + y * y));
  }

  public void recycle() {
    this.reset(true);
    this.bitmapPaint = null;
    this.debugTextPaint = null;
    this.debugLinePaint = null;
    this.tileBgPaint = null;
  }

  private float viewToSourceX(float vx) {
    return this.vTranslate == null ? 0 : (vx - this.vTranslate.x) / this.scale;
  }

  private float viewToSourceY(float vy) {
    return this.vTranslate == null ? 0 : (vy - this.vTranslate.y) / this.scale;
  }

  public void viewToFileRect(Rect vRect, Rect fRect) {
    if (this.vTranslate != null && this.readySent) {
      fRect.set((int)this.viewToSourceX((float)vRect.left), (int)this.viewToSourceY((float)vRect.top), (int)this.viewToSourceX((float)vRect.right), (int)this.viewToSourceY((float)vRect.bottom));
      this.fileSRect(fRect, fRect);
      fRect.set(Math.max(0, fRect.left), Math.max(0, fRect.top), Math.min(this.sWidth, fRect.right), Math.min(this.sHeight, fRect.bottom));
      if (this.sRegion != null) {
        fRect.offset(this.sRegion.left, this.sRegion.top);
      }

    }
  }

  public void visibleFileRect(Rect fRect) {
    if (this.vTranslate != null && this.readySent) {
      fRect.set(0, 0, this.getWidth(), this.getHeight());
      this.viewToFileRect(fRect, fRect);
    }
  }

  public final PointF viewToSourceCoord(PointF vxy) {
    return this.viewToSourceCoord(vxy.x, vxy.y, new PointF());
  }

  public final PointF viewToSourceCoord(float vx, float vy) {
    return this.viewToSourceCoord(vx, vy, new PointF());
  }

  public final PointF viewToSourceCoord(PointF vxy, PointF sTarget) {
    return this.viewToSourceCoord(vxy.x, vxy.y, sTarget);
  }

  public final PointF viewToSourceCoord(float vx, float vy, PointF sTarget) {
    if (this.vTranslate == null) {
      return null;
    } else {
      sTarget.set(this.viewToSourceX(vx), this.viewToSourceY(vy));
      return sTarget;
    }
  }

  private float sourceToViewX(float sx) {
    return this.vTranslate == null ? 0 : (sx * this.scale + this.vTranslate.x);
  }

  private float sourceToViewY(float sy) {
    return this.vTranslate == null ? 0 : sy * this.scale + this.vTranslate.y;
  }

  public final PointF sourceToViewCoord(PointF sxy) {
    return this.sourceToViewCoord(sxy.x, sxy.y, new PointF());
  }

  public final PointF sourceToViewCoord(float sx, float sy) {
    return this.sourceToViewCoord(sx, sy, new PointF());
  }

  public final PointF sourceToViewCoord(PointF sxy, PointF vTarget) {
    return this.sourceToViewCoord(sxy.x, sxy.y, vTarget);
  }

  public final PointF sourceToViewCoord(float sx, float sy, PointF vTarget) {
    if (this.vTranslate == null) {
      return null;
    } else {
      vTarget.set(this.sourceToViewX(sx), this.sourceToViewY(sy));
      return vTarget;
    }
  }

  private void sourceToViewRect(Rect sRect, Rect vTarget) {
    vTarget.set((int)this.sourceToViewX((float)sRect.left), (int)this.sourceToViewY((float)sRect.top), (int)this.sourceToViewX((float)sRect.right), (int)this.sourceToViewY((float)sRect.bottom));
  }

  private PointF vTranslateForSCenter(float sCenterX, float sCenterY, float scale) {
    int vxCenter = this.getPaddingLeft() + (this.getWidth() - this.getPaddingRight() - this.getPaddingLeft()) / 2;
    int vyCenter = this.getPaddingTop() + (this.getHeight() - this.getPaddingBottom() - this.getPaddingTop()) / 2;
    if (this.satTemp == null) {
      this.satTemp = new io.rong.subscaleview.SubsamplingScaleImageView.ScaleAndTranslate(0.0F, new PointF(0.0F, 0.0F));
    }

    this.satTemp.scale = scale;
    this.satTemp.vTranslate.set((float)vxCenter - sCenterX * scale, (float)vyCenter - sCenterY * scale);
    this.fitToBounds(true, this.satTemp);
    return this.satTemp.vTranslate;
  }

  private PointF limitedSCenter(float sCenterX, float sCenterY, float scale, PointF sTarget) {
    PointF vTranslate = this.vTranslateForSCenter(sCenterX, sCenterY, scale);
    int vxCenter = this.getPaddingLeft() + (this.getWidth() - this.getPaddingRight() - this.getPaddingLeft()) / 2;
    int vyCenter = this.getPaddingTop() + (this.getHeight() - this.getPaddingBottom() - this.getPaddingTop()) / 2;
    float sx = ((float)vxCenter - vTranslate.x) / scale;
    float sy = ((float)vyCenter - vTranslate.y) / scale;
    sTarget.set(sx, sy);
    return sTarget;
  }

  private float minScale() {
    int vPadding = this.getPaddingBottom() + this.getPaddingTop();
    int hPadding = this.getPaddingLeft() + this.getPaddingRight();
    if (this.minimumScaleType != 2 && this.minimumScaleType != 4) {
      return this.minimumScaleType == 3 && this.minScale > 0.0F ? this.minScale : Math.min((float)(this.getWidth() - hPadding) / (float)this.sWidth(), (float)(this.getHeight() - vPadding) / (float)this.sHeight());
    } else {
      return Math.max((float)(this.getWidth() - hPadding) / (float)this.sWidth(), (float)(this.getHeight() - vPadding) / (float)this.sHeight());
    }
  }

  private float limitedScale(float targetScale) {
    targetScale = Math.max(this.minScale(), targetScale);
    targetScale = Math.min(this.maxScale, targetScale);
    return targetScale;
  }

  private float ease(int type, long time, float from, float change, long duration) {
    switch(type) {
      case 1:
        return this.easeOutQuad(time, from, change, duration);
      case 2:
        return this.easeInOutQuad(time, from, change, duration);
      default:
        throw new IllegalStateException("Unexpected easing type: " + type);
    }
  }

  private float easeOutQuad(long time, float from, float change, long duration) {
    float progress = (float)time / (float)duration;
    return -change * progress * (progress - 2.0F) + from;
  }

  private float easeInOutQuad(long time, float from, float change, long duration) {
    float timeF = (float)time / ((float)duration / 2.0F);
    if (timeF < 1.0F) {
      return change / 2.0F * timeF * timeF + from;
    } else {
      --timeF;
      return -change / 2.0F * (timeF * (timeF - 2.0F) - 1.0F) + from;
    }
  }

  @AnyThread
  private void debug(String message, Object... args) {
    if (this.debug) {
      Log.d(TAG, String.format(message, args));
    }

  }

  private int px(int px) {
    return (int)(this.density * (float)px);
  }

  public final void setRegionDecoderClass(Class<? extends ImageRegionDecoder> regionDecoderClass) {
    if (regionDecoderClass == null) {
      throw new IllegalArgumentException("Decoder class cannot be set to null");
    } else {
      this.regionDecoderFactory = new CompatDecoderFactory(regionDecoderClass);
    }
  }

  public final void setRegionDecoderFactory(DecoderFactory<? extends ImageRegionDecoder> regionDecoderFactory) {
    if (regionDecoderFactory == null) {
      throw new IllegalArgumentException("Decoder factory cannot be set to null");
    } else {
      this.regionDecoderFactory = regionDecoderFactory;
    }
  }

  public final void setBitmapDecoderClass(Class<? extends ImageDecoder> bitmapDecoderClass) {
    if (bitmapDecoderClass == null) {
      throw new IllegalArgumentException("Decoder class cannot be set to null");
    } else {
      this.bitmapDecoderFactory = new CompatDecoderFactory(bitmapDecoderClass);
    }
  }

  public final void setBitmapDecoderFactory(DecoderFactory<? extends ImageDecoder> bitmapDecoderFactory) {
    if (bitmapDecoderFactory == null) {
      throw new IllegalArgumentException("Decoder factory cannot be set to null");
    } else {
      this.bitmapDecoderFactory = bitmapDecoderFactory;
    }
  }

  public final void getPanRemaining(RectF vTarget) {
    if (this.isReady()) {
      float scaleWidth = this.scale * (float)this.sWidth();
      float scaleHeight = this.scale * (float)this.sHeight();
      if (this.panLimit == 3) {
        vTarget.top = Math.max(0.0F, -(this.vTranslate.y - (float)(this.getHeight() / 2)));
        vTarget.left = Math.max(0.0F, -(this.vTranslate.x - (float)(this.getWidth() / 2)));
        vTarget.bottom = Math.max(0.0F, this.vTranslate.y - ((float)(this.getHeight() / 2) - scaleHeight));
        vTarget.right = Math.max(0.0F, this.vTranslate.x - ((float)(this.getWidth() / 2) - scaleWidth));
      } else if (this.panLimit == 2) {
        vTarget.top = Math.max(0.0F, -(this.vTranslate.y - (float)this.getHeight()));
        vTarget.left = Math.max(0.0F, -(this.vTranslate.x - (float)this.getWidth()));
        vTarget.bottom = Math.max(0.0F, this.vTranslate.y + scaleHeight);
        vTarget.right = Math.max(0.0F, this.vTranslate.x + scaleWidth);
      } else {
        vTarget.top = Math.max(0.0F, -this.vTranslate.y);
        vTarget.left = Math.max(0.0F, -this.vTranslate.x);
        vTarget.bottom = Math.max(0.0F, scaleHeight + this.vTranslate.y - (float)this.getHeight());
        vTarget.right = Math.max(0.0F, scaleWidth + this.vTranslate.x - (float)this.getWidth());
      }

    }
  }

  public final void setPanLimit(int panLimit) {
    if (!VALID_PAN_LIMITS.contains(panLimit)) {
      throw new IllegalArgumentException("Invalid pan limit: " + panLimit);
    } else {
      this.panLimit = panLimit;
      if (this.isReady()) {
        this.fitToBounds(true);
        this.invalidate();
      }

    }
  }

  public final void setMinimumScaleType(int scaleType) {
    if (!VALID_SCALE_TYPES.contains(scaleType)) {
      throw new IllegalArgumentException("Invalid scale type: " + scaleType);
    } else {
      this.minimumScaleType = scaleType;
      if (this.isReady()) {
        this.fitToBounds(true);
        this.invalidate();
      }

    }
  }

  public final void setMaxScale(float maxScale) {
    this.maxScale = maxScale;
  }

  public final void setMinScale(float minScale) {
    this.minScale = minScale;
  }

  public final void setMinimumDpi(int dpi) {
    DisplayMetrics metrics = this.getResources().getDisplayMetrics();
    float averageDpi = (metrics.xdpi + metrics.ydpi) / 2.0F;
    this.setMaxScale(averageDpi / (float)dpi);
  }

  public final void setMaximumDpi(int dpi) {
    DisplayMetrics metrics = this.getResources().getDisplayMetrics();
    float averageDpi = (metrics.xdpi + metrics.ydpi) / 2.0F;
    this.setMinScale(averageDpi / (float)dpi);
  }

  public float getMaxScale() {
    return this.maxScale;
  }

  public final float getMinScale() {
    return this.minScale();
  }

  public void setMinimumTileDpi(int minimumTileDpi) {
    DisplayMetrics metrics = this.getResources().getDisplayMetrics();
    float averageDpi = (metrics.xdpi + metrics.ydpi) / 2.0F;
    this.minimumTileDpi = (int)Math.min(averageDpi, (float)minimumTileDpi);
    if (this.isReady()) {
      this.reset(false);
      this.invalidate();
    }

  }

  public final PointF getCenter() {
    int mX = this.getWidth() / 2;
    int mY = this.getHeight() / 2;
    return this.viewToSourceCoord((float)mX, (float)mY);
  }

  public final float getScale() {
    return this.scale;
  }

  public final void setScaleAndCenter(float scale, PointF sCenter) {
    this.anim = null;
    this.pendingScale = scale;
    this.sPendingCenter = sCenter;
    this.sRequestedCenter = sCenter;
    this.invalidate();
  }

  public final void resetScaleAndCenter() {
    this.anim = null;
    this.pendingScale = this.limitedScale(0.0F);
    if (this.isReady()) {
      this.sPendingCenter = new PointF((float)(this.sWidth() / 2), (float)(this.sHeight() / 2));
    } else {
      this.sPendingCenter = new PointF(0.0F, 0.0F);
    }

    this.invalidate();
  }

  public final boolean isReady() {
    return this.readySent;
  }

  protected void onReady() {
  }

  public final boolean isImageLoaded() {
    return this.imageLoadedSent;
  }

  protected void onImageLoaded() {
  }

  public final int getSWidth() {
    return this.sWidth;
  }

  public final int getSHeight() {
    return this.sHeight;
  }

  public final int getOrientation() {
    return this.orientation;
  }

  public final int getAppliedOrientation() {
    return this.getRequiredRotation();
  }

  public final ImageViewState getState() {
    return this.vTranslate != null && this.sWidth > 0 && this.sHeight > 0 ? new ImageViewState(this.getScale(), this.getCenter(), this.getOrientation()) : null;
  }

  public final boolean isZoomEnabled() {
    return this.zoomEnabled;
  }

  public final void setZoomEnabled(boolean zoomEnabled) {
    this.zoomEnabled = zoomEnabled;
  }

  public final boolean isQuickScaleEnabled() {
    return this.quickScaleEnabled;
  }

  public final void setQuickScaleEnabled(boolean quickScaleEnabled) {
    this.quickScaleEnabled = quickScaleEnabled;
  }

  public final boolean isPanEnabled() {
    return this.panEnabled;
  }

  public final void setPanEnabled(boolean panEnabled) {
    this.panEnabled = panEnabled;
    if (!panEnabled && this.vTranslate != null) {
      this.vTranslate.x = (float)(this.getWidth() / 2) - this.scale * (float)(this.sWidth() / 2);
      this.vTranslate.y = (float)(this.getHeight() / 2) - this.scale * (float)(this.sHeight() / 2);
      if (this.isReady()) {
        this.refreshRequiredTiles(true);
        this.invalidate();
      }
    }

  }

  public final void setTileBackgroundColor(int tileBgColor) {
    if (Color.alpha(tileBgColor) == 0) {
      this.tileBgPaint = null;
    } else {
      this.tileBgPaint = new Paint();
      this.tileBgPaint.setStyle(Style.FILL);
      this.tileBgPaint.setColor(tileBgColor);
    }

    this.invalidate();
  }

  public final void setDoubleTapZoomScale(float doubleTapZoomScale) {
    this.doubleTapZoomScale = doubleTapZoomScale;
  }

  public final void setDoubleTapZoomDpi(int dpi) {
    DisplayMetrics metrics = this.getResources().getDisplayMetrics();
    float averageDpi = (metrics.xdpi + metrics.ydpi) / 2.0F;
    this.setDoubleTapZoomScale(averageDpi / (float)dpi);
  }

  public final void setDoubleTapZoomStyle(int doubleTapZoomStyle) {
    if (!VALID_ZOOM_STYLES.contains(doubleTapZoomStyle)) {
      throw new IllegalArgumentException("Invalid zoom style: " + doubleTapZoomStyle);
    } else {
      this.doubleTapZoomStyle = doubleTapZoomStyle;
    }
  }

  public final void setDoubleTapZoomDuration(int durationMs) {
    this.doubleTapZoomDuration = Math.max(0, durationMs);
  }

  public void setExecutor(Executor executor) {
    if (executor == null) {
      throw new NullPointerException("Executor must not be null");
    } else {
      this.executor = executor;
    }
  }

  public void setEagerLoadingEnabled(boolean eagerLoadingEnabled) {
    this.eagerLoadingEnabled = eagerLoadingEnabled;
  }

  public final void setDebug(boolean debug) {
    this.debug = debug;
  }

  public boolean hasImage() {
    return this.uri != null || this.bitmap != null;
  }

  public void setOnLongClickListener(OnLongClickListener onLongClickListener) {
    this.onLongClickListener = onLongClickListener;
  }

  public void setOnImageEventListener(io.rong.subscaleview.SubsamplingScaleImageView.OnImageEventListener onImageEventListener) {
    this.onImageEventListener = onImageEventListener;
  }

  public void setOnStateChangedListener(io.rong.subscaleview.SubsamplingScaleImageView.OnStateChangedListener onStateChangedListener) {
    this.onStateChangedListener = onStateChangedListener;
  }

  private void sendStateChanged(float oldScale, PointF oldVTranslate, int origin) {
    if (this.onStateChangedListener != null && this.scale != oldScale) {
      this.onStateChangedListener.onScaleChanged(this.scale, origin);
    }

    if (this.onStateChangedListener != null && !this.vTranslate.equals(oldVTranslate)) {
      this.onStateChangedListener.onCenterChanged(this.getCenter(), origin);
    }

  }

  public io.rong.subscaleview.SubsamplingScaleImageView.AnimationBuilder animateCenter(PointF sCenter) {
    return !this.isReady() ? null : new io.rong.subscaleview.SubsamplingScaleImageView.AnimationBuilder(sCenter);
  }

  public io.rong.subscaleview.SubsamplingScaleImageView.AnimationBuilder animateScale(float scale) {
    return !this.isReady() ? null : new io.rong.subscaleview.SubsamplingScaleImageView.AnimationBuilder(scale);
  }

  public io.rong.subscaleview.SubsamplingScaleImageView.AnimationBuilder animateScaleAndCenter(float scale, PointF sCenter) {
    return !this.isReady() ? null : new io.rong.subscaleview.SubsamplingScaleImageView.AnimationBuilder(scale, sCenter);
  }

  public static class DefaultOnStateChangedListener implements io.rong.subscaleview.SubsamplingScaleImageView.OnStateChangedListener {
    public DefaultOnStateChangedListener() {
    }

    public void onCenterChanged(PointF newCenter, int origin) {
    }

    public void onScaleChanged(float newScale, int origin) {
    }
  }

  public interface OnStateChangedListener {
    void onScaleChanged(float var1, int var2);

    void onCenterChanged(PointF var1, int var2);
  }

  public static class DefaultOnImageEventListener implements io.rong.subscaleview.SubsamplingScaleImageView.OnImageEventListener {
    public DefaultOnImageEventListener() {
    }

    public void onReady() {
    }

    public void onImageLoaded() {
    }

    public void onPreviewLoadError(Exception e) {
    }

    public void onImageLoadError(Exception e) {
    }

    public void onTileLoadError(Exception e) {
    }

    public void onPreviewReleased() {
    }
  }

  public interface OnImageEventListener {
    void onReady();

    void onImageLoaded();

    void onPreviewLoadError(Exception var1);

    void onImageLoadError(Exception var1);

    void onTileLoadError(Exception var1);

    void onPreviewReleased();
  }

  public static class DefaultOnAnimationEventListener implements io.rong.subscaleview.SubsamplingScaleImageView.OnAnimationEventListener {
    public DefaultOnAnimationEventListener() {
    }

    public void onComplete() {
    }

    public void onInterruptedByUser() {
    }

    public void onInterruptedByNewAnim() {
    }
  }

  public interface OnAnimationEventListener {
    void onComplete();

    void onInterruptedByUser();

    void onInterruptedByNewAnim();
  }

  public final class AnimationBuilder {
    private final float targetScale;
    private final PointF targetSCenter;
    private final PointF vFocus;
    private long duration;
    private int easing;
    private int origin;
    private boolean interruptible;
    private boolean panLimited;
    private io.rong.subscaleview.SubsamplingScaleImageView.OnAnimationEventListener listener;

    private AnimationBuilder(PointF sCenter) {
      this.duration = 500L;
      this.easing = 2;
      this.origin = 1;
      this.interruptible = true;
      this.panLimited = true;
      this.targetScale = io.rong.subscaleview.SubsamplingScaleImageView.this.scale;
      this.targetSCenter = sCenter;
      this.vFocus = null;
    }

    private AnimationBuilder(float scale) {
      this.duration = 500L;
      this.easing = 2;
      this.origin = 1;
      this.interruptible = true;
      this.panLimited = true;
      this.targetScale = scale;
      this.targetSCenter = io.rong.subscaleview.SubsamplingScaleImageView.this.getCenter();
      this.vFocus = null;
    }

    private AnimationBuilder(float scale, PointF sCenter) {
      this.duration = 500L;
      this.easing = 2;
      this.origin = 1;
      this.interruptible = true;
      this.panLimited = true;
      this.targetScale = scale;
      this.targetSCenter = sCenter;
      this.vFocus = null;
    }

    private AnimationBuilder(float scale, PointF sCenter, PointF vFocus) {
      this.duration = 500L;
      this.easing = 2;
      this.origin = 1;
      this.interruptible = true;
      this.panLimited = true;
      this.targetScale = scale;
      this.targetSCenter = sCenter;
      this.vFocus = vFocus;
    }

    public io.rong.subscaleview.SubsamplingScaleImageView.AnimationBuilder withDuration(long duration) {
      this.duration = duration;
      return this;
    }

    public io.rong.subscaleview.SubsamplingScaleImageView.AnimationBuilder withInterruptible(boolean interruptible) {
      this.interruptible = interruptible;
      return this;
    }

    public io.rong.subscaleview.SubsamplingScaleImageView.AnimationBuilder withEasing(int easing) {
      if (!io.rong.subscaleview.SubsamplingScaleImageView.VALID_EASING_STYLES.contains(easing)) {
        throw new IllegalArgumentException("Unknown easing type: " + easing);
      } else {
        this.easing = easing;
        return this;
      }
    }

    public io.rong.subscaleview.SubsamplingScaleImageView.AnimationBuilder withOnAnimationEventListener(io.rong.subscaleview.SubsamplingScaleImageView.OnAnimationEventListener listener) {
      this.listener = listener;
      return this;
    }

    private io.rong.subscaleview.SubsamplingScaleImageView.AnimationBuilder withPanLimited(boolean panLimited) {
      this.panLimited = panLimited;
      return this;
    }

    private io.rong.subscaleview.SubsamplingScaleImageView.AnimationBuilder withOrigin(int origin) {
      this.origin = origin;
      return this;
    }

    public void start() {
      if (io.rong.subscaleview.SubsamplingScaleImageView.this.anim != null && io.rong.subscaleview.SubsamplingScaleImageView.this.anim.listener != null) {
        try {
          io.rong.subscaleview.SubsamplingScaleImageView.this.anim.listener.onInterruptedByNewAnim();
        } catch (Exception var8) {
          Log.w(io.rong.subscaleview.SubsamplingScaleImageView.TAG, "Error thrown by animation listener", var8);
        }
      }

      int vxCenter = io.rong.subscaleview.SubsamplingScaleImageView.this.getPaddingLeft() + (io.rong.subscaleview.SubsamplingScaleImageView.this.getWidth() - io.rong.subscaleview.SubsamplingScaleImageView.this.getPaddingRight() - io.rong.subscaleview.SubsamplingScaleImageView.this.getPaddingLeft()) / 2;
      int vyCenter = io.rong.subscaleview.SubsamplingScaleImageView.this.getPaddingTop() + (io.rong.subscaleview.SubsamplingScaleImageView.this.getHeight() - io.rong.subscaleview.SubsamplingScaleImageView.this.getPaddingBottom() - io.rong.subscaleview.SubsamplingScaleImageView.this.getPaddingTop()) / 2;
      float targetScale = io.rong.subscaleview.SubsamplingScaleImageView.this.limitedScale(this.targetScale);
      PointF targetSCenter = this.panLimited ? io.rong.subscaleview.SubsamplingScaleImageView.this.limitedSCenter(this.targetSCenter.x, this.targetSCenter.y, targetScale, new PointF()) : this.targetSCenter;
      io.rong.subscaleview.SubsamplingScaleImageView.this.anim = new io.rong.subscaleview.SubsamplingScaleImageView.Anim();
      io.rong.subscaleview.SubsamplingScaleImageView.this.anim.scaleStart = io.rong.subscaleview.SubsamplingScaleImageView.this.scale;
      io.rong.subscaleview.SubsamplingScaleImageView.this.anim.scaleEnd = targetScale;
      io.rong.subscaleview.SubsamplingScaleImageView.this.anim.time = System.currentTimeMillis();
      io.rong.subscaleview.SubsamplingScaleImageView.this.anim.sCenterEndRequested = targetSCenter;
      io.rong.subscaleview.SubsamplingScaleImageView.this.anim.sCenterStart = io.rong.subscaleview.SubsamplingScaleImageView.this.getCenter();
      io.rong.subscaleview.SubsamplingScaleImageView.this.anim.sCenterEnd = targetSCenter;
      io.rong.subscaleview.SubsamplingScaleImageView.this.anim.vFocusStart = io.rong.subscaleview.SubsamplingScaleImageView.this.sourceToViewCoord(targetSCenter);
      io.rong.subscaleview.SubsamplingScaleImageView.this.anim.vFocusEnd = new PointF((float)vxCenter, (float)vyCenter);
      io.rong.subscaleview.SubsamplingScaleImageView.this.anim.duration = this.duration;
      io.rong.subscaleview.SubsamplingScaleImageView.this.anim.interruptible = this.interruptible;
      io.rong.subscaleview.SubsamplingScaleImageView.this.anim.easing = this.easing;
      io.rong.subscaleview.SubsamplingScaleImageView.this.anim.origin = this.origin;
      io.rong.subscaleview.SubsamplingScaleImageView.this.anim.time = System.currentTimeMillis();
      io.rong.subscaleview.SubsamplingScaleImageView.this.anim.listener = this.listener;
      if (this.vFocus != null) {
        float vTranslateXEnd = this.vFocus.x - targetScale * io.rong.subscaleview.SubsamplingScaleImageView.this.anim.sCenterStart.x;
        float vTranslateYEnd = this.vFocus.y - targetScale * io.rong.subscaleview.SubsamplingScaleImageView.this.anim.sCenterStart.y;
        io.rong.subscaleview.SubsamplingScaleImageView.ScaleAndTranslate satEnd = new io.rong.subscaleview.SubsamplingScaleImageView.ScaleAndTranslate(targetScale, new PointF(vTranslateXEnd, vTranslateYEnd));
        io.rong.subscaleview.SubsamplingScaleImageView.this.fitToBounds(true, satEnd);
        io.rong.subscaleview.SubsamplingScaleImageView.this.anim.vFocusEnd = new PointF(this.vFocus.x + (satEnd.vTranslate.x - vTranslateXEnd), this.vFocus.y + (satEnd.vTranslate.y - vTranslateYEnd));
      }

      io.rong.subscaleview.SubsamplingScaleImageView.this.invalidate();
    }
  }

  private static class ScaleAndTranslate {
    private float scale;
    private final PointF vTranslate;

    private ScaleAndTranslate(float scale, PointF vTranslate) {
      this.scale = scale;
      this.vTranslate = vTranslate;
    }
  }

  private static class Anim {
    private float scaleStart;
    private float scaleEnd;
    private PointF sCenterStart;
    private PointF sCenterEnd;
    private PointF sCenterEndRequested;
    private PointF vFocusStart;
    private PointF vFocusEnd;
    private long duration;
    private boolean interruptible;
    private int easing;
    private int origin;
    private long time;
    private io.rong.subscaleview.SubsamplingScaleImageView.OnAnimationEventListener listener;

    private Anim() {
      this.duration = 500L;
      this.interruptible = true;
      this.easing = 2;
      this.origin = 1;
      this.time = System.currentTimeMillis();
    }
  }

  private static class Tile {
    private Rect sRect;
    private int sampleSize;
    private Bitmap bitmap;
    private boolean loading;
    private boolean visible;
    private Rect vRect;
    private Rect fileSRect;

    private Tile() {
    }
  }

  private static class BitmapLoadTask extends AsyncTask<Void, Void, Integer> {
    private final WeakReference<io.rong.subscaleview.SubsamplingScaleImageView> viewRef;
    private final WeakReference<Context> contextRef;
    private final WeakReference<DecoderFactory<? extends ImageDecoder>> decoderFactoryRef;
    private final Uri source;
    private final boolean preview;
    private Bitmap bitmap;
    private Exception exception;

    BitmapLoadTask(io.rong.subscaleview.SubsamplingScaleImageView view, Context context, DecoderFactory<? extends ImageDecoder> decoderFactory, Uri source, boolean preview) {
      this.viewRef = new WeakReference(view);
      this.contextRef = new WeakReference(context);
      this.decoderFactoryRef = new WeakReference(decoderFactory);
      this.source = source;
      this.preview = preview;
    }

    protected Integer doInBackground(Void... params) {
      try {
        String sourceUri = this.source.toString();
        Context context = (Context)this.contextRef.get();
        DecoderFactory<? extends ImageDecoder> decoderFactory = (DecoderFactory)this.decoderFactoryRef.get();
        io.rong.subscaleview.SubsamplingScaleImageView view = (io.rong.subscaleview.SubsamplingScaleImageView)this.viewRef.get();
        if (context != null && decoderFactory != null && view != null) {
          view.debug("BitmapLoadTask.doInBackground");
          this.bitmap = ((ImageDecoder)decoderFactory.make()).decode(context, this.source);
          return view.getExifOrientation(context, sourceUri);
        }
      } catch (Exception var6) {
        Log.e(io.rong.subscaleview.SubsamplingScaleImageView.TAG, "Failed to load bitmap", var6);
        this.exception = var6;
      } catch (OutOfMemoryError var7) {
        Log.e(io.rong.subscaleview.SubsamplingScaleImageView.TAG, "Failed to load bitmap - OutOfMemoryError", var7);
        this.exception = new RuntimeException(var7);
      }

      return null;
    }

    protected void onPostExecute(Integer orientation) {
      io.rong.subscaleview.SubsamplingScaleImageView subsamplingScaleImageView = (io.rong.subscaleview.SubsamplingScaleImageView)this.viewRef.get();
      if (subsamplingScaleImageView != null) {
        if (this.bitmap != null && orientation != null) {
          if (this.preview) {
            subsamplingScaleImageView.onPreviewLoaded(this.bitmap);
          } else {
            subsamplingScaleImageView.onImageLoaded(this.bitmap, orientation, false);
          }
        } else if (this.exception != null && subsamplingScaleImageView.onImageEventListener != null) {
          if (this.preview) {
            subsamplingScaleImageView.onImageEventListener.onPreviewLoadError(this.exception);
          } else {
            subsamplingScaleImageView.onImageEventListener.onImageLoadError(this.exception);
          }
        }
      }

    }
  }

  private static class TileLoadTask extends AsyncTask<Void, Void, Bitmap> {
    private final WeakReference<io.rong.subscaleview.SubsamplingScaleImageView> viewRef;
    private final WeakReference<ImageRegionDecoder> decoderRef;
    private final WeakReference<io.rong.subscaleview.SubsamplingScaleImageView.Tile> tileRef;
    private Exception exception;

    TileLoadTask(io.rong.subscaleview.SubsamplingScaleImageView view, ImageRegionDecoder decoder, io.rong.subscaleview.SubsamplingScaleImageView.Tile tile) {
      this.viewRef = new WeakReference(view);
      this.decoderRef = new WeakReference(decoder);
      this.tileRef = new WeakReference(tile);
      tile.loading = true;
    }

    protected Bitmap doInBackground(Void... params) {
      try {
        io.rong.subscaleview.SubsamplingScaleImageView view = (io.rong.subscaleview.SubsamplingScaleImageView)this.viewRef.get();
        ImageRegionDecoder decoder = (ImageRegionDecoder)this.decoderRef.get();
        io.rong.subscaleview.SubsamplingScaleImageView.Tile tile = (io.rong.subscaleview.SubsamplingScaleImageView.Tile)this.tileRef.get();
        if (decoder != null && tile != null && view != null && decoder.isReady() && tile.visible) {
          view.debug("TileLoadTask.doInBackground, tile.sRect=%s, tile.sampleSize=%d", tile.sRect, tile.sampleSize);
          view.decoderLock.readLock().lock();

          Bitmap var5;
          try {
            if (!decoder.isReady()) {
              tile.loading = false;
              return null;
            }

            view.fileSRect(tile.sRect, tile.fileSRect);
            if (view.sRegion != null) {
              tile.fileSRect.offset(view.sRegion.left, view.sRegion.top);
            }

            var5 = decoder.decodeRegion(tile.fileSRect, tile.sampleSize);
          } finally {
            view.decoderLock.readLock().unlock();
          }

          return var5;
        } else if (tile != null) {
          tile.loading = false;
        }
      } catch (Exception var11) {
        Log.e(io.rong.subscaleview.SubsamplingScaleImageView.TAG, "Failed to decode tile", var11);
        this.exception = var11;
      } catch (OutOfMemoryError var12) {
        Log.e(io.rong.subscaleview.SubsamplingScaleImageView.TAG, "Failed to decode tile - OutOfMemoryError", var12);
        this.exception = new RuntimeException(var12);
      }

      return null;
    }

    protected void onPostExecute(Bitmap bitmap) {
      io.rong.subscaleview.SubsamplingScaleImageView subsamplingScaleImageView = (io.rong.subscaleview.SubsamplingScaleImageView)this.viewRef.get();
      io.rong.subscaleview.SubsamplingScaleImageView.Tile tile = (io.rong.subscaleview.SubsamplingScaleImageView.Tile)this.tileRef.get();
      if (subsamplingScaleImageView != null && tile != null) {
        if (bitmap != null) {
          tile.bitmap = bitmap;
          tile.loading = false;
          subsamplingScaleImageView.onTileLoaded();
        } else if (this.exception != null && subsamplingScaleImageView.onImageEventListener != null) {
          subsamplingScaleImageView.onImageEventListener.onTileLoadError(this.exception);
        }
      }

    }
  }

  private static class TilesInitTask extends AsyncTask<Void, Void, int[]> {
    private final WeakReference<io.rong.subscaleview.SubsamplingScaleImageView> viewRef;
    private final WeakReference<Context> contextRef;
    private final WeakReference<DecoderFactory<? extends ImageRegionDecoder>> decoderFactoryRef;
    private final Uri source;
    private ImageRegionDecoder decoder;
    private Exception exception;

    TilesInitTask(io.rong.subscaleview.SubsamplingScaleImageView view, Context context, DecoderFactory<? extends ImageRegionDecoder> decoderFactory, Uri source) {
      this.viewRef = new WeakReference(view);
      this.contextRef = new WeakReference(context);
      this.decoderFactoryRef = new WeakReference(decoderFactory);
      this.source = source;
    }

    protected int[] doInBackground(Void... params) {
      try {
        String sourceUri = this.source.toString();
        Context context = (Context)this.contextRef.get();
        DecoderFactory<? extends ImageRegionDecoder> decoderFactory = (DecoderFactory)this.decoderFactoryRef.get();
        io.rong.subscaleview.SubsamplingScaleImageView view = (io.rong.subscaleview.SubsamplingScaleImageView)this.viewRef.get();
        if (context != null && decoderFactory != null && view != null) {
          view.debug("TilesInitTask.doInBackground");
          this.decoder = (ImageRegionDecoder)decoderFactory.make();
          Point dimensions = this.decoder.init(context, this.source);
          int sWidth = dimensions.x;
          int sHeight = dimensions.y;
          int exifOrientation = view.getExifOrientation(context, sourceUri);
          if (view.sRegion != null) {
            view.sRegion.left = Math.max(0, view.sRegion.left);
            view.sRegion.top = Math.max(0, view.sRegion.top);
            view.sRegion.right = Math.min(sWidth, view.sRegion.right);
            view.sRegion.bottom = Math.min(sHeight, view.sRegion.bottom);
            sWidth = view.sRegion.width();
            sHeight = view.sRegion.height();
          }

          return new int[]{sWidth, sHeight, exifOrientation};
        }
      } catch (Exception var10) {
        Log.e(io.rong.subscaleview.SubsamplingScaleImageView.TAG, "Failed to initialise bitmap decoder", var10);
        this.exception = var10;
      }

      return null;
    }

    protected void onPostExecute(int[] xyo) {
      io.rong.subscaleview.SubsamplingScaleImageView view = (io.rong.subscaleview.SubsamplingScaleImageView)this.viewRef.get();
      if (view != null) {
        if (this.decoder != null && xyo != null && xyo.length == 3) {
          view.onTilesInited(this.decoder, xyo[0], xyo[1], xyo[2]);
        } else if (this.exception != null && view.onImageEventListener != null) {
          view.onImageEventListener.onImageLoadError(this.exception);
        }
      }

    }
  }
}
