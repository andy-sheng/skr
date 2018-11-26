//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.photoview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import java.lang.ref.WeakReference;

import io.rong.photoview.Compat;
import io.rong.photoview.DefaultOnDoubleTapListener;
import io.rong.photoview.IPhotoView;
import io.rong.photoview.gestures.OnGestureListener;
import io.rong.photoview.gestures.VersionedGestureDetector;
import io.rong.photoview.log.LogManager;
import io.rong.photoview.scrollerproxy.ScrollerProxy;

public class PhotoViewAttacher implements IPhotoView, OnTouchListener, OnGestureListener, OnGlobalLayoutListener {
    private static final String LOG_TAG = "PhotoViewAttacher";
    private static final boolean DEBUG = Log.isLoggable("PhotoViewAttacher", 3);
    static final Interpolator sInterpolator = new AccelerateDecelerateInterpolator();
    int ZOOM_DURATION;
    static final int EDGE_NONE = -1;
    static final int EDGE_LEFT = 0;
    static final int EDGE_RIGHT = 1;
    static final int EDGE_BOTH = 2;
    static int SINGLE_TOUCH = 1;
    private float mMinScale;
    private float mMidScale;
    private float mMaxScale;
    private boolean mAllowParentInterceptOnEdge;
    private boolean mBlockParentIntercept;
    private WeakReference<ImageView> mImageView;
    private GestureDetector mGestureDetector;
    private io.rong.photoview.gestures.GestureDetector mScaleDragDetector;
    protected final Matrix mBaseMatrix;
    private final Matrix mDrawMatrix;
    private final Matrix mSuppMatrix;
    private final RectF mDisplayRect;
    private final float[] mMatrixValues;
    private io.rong.photoview.PhotoViewAttacher.OnMatrixChangedListener mMatrixChangeListener;
    private io.rong.photoview.PhotoViewAttacher.OnPhotoTapListener mPhotoTapListener;
    private io.rong.photoview.PhotoViewAttacher.OnViewTapListener mViewTapListener;
    private OnLongClickListener mLongClickListener;
    private io.rong.photoview.PhotoViewAttacher.OnScaleChangeListener mScaleChangeListener;
    private io.rong.photoview.PhotoViewAttacher.OnSingleFlingListener mSingleFlingListener;
    private int mIvTop;
    private int mIvRight;
    private int mIvBottom;
    private int mIvLeft;
    private io.rong.photoview.PhotoViewAttacher.FlingRunnable mCurrentFlingRunnable;
    private int mScrollEdge;
    private float mBaseRotation;
    private boolean mZoomEnabled;
    private ScaleType mScaleType;

    private static void checkZoomLevels(float minZoom, float midZoom, float maxZoom) {
        if (minZoom >= midZoom) {
            throw new IllegalArgumentException("MinZoom has to be less than MidZoom");
        } else if (midZoom >= maxZoom) {
            throw new IllegalArgumentException("MidZoom has to be less than MaxZoom");
        }
    }

    private static boolean hasDrawable(ImageView imageView) {
        return null != imageView && null != imageView.getDrawable();
    }

    private static boolean isSupportedScaleType(ScaleType scaleType) {
        if (null == scaleType) {
            return false;
        } else {
            switch (scaleType) {
                case MATRIX:
                    throw new IllegalArgumentException(scaleType.name() + " is not supported in PhotoView");
                default:
                    return true;
            }
        }
    }

    private static void setImageViewScaleTypeMatrix(ImageView imageView) {
        if (null != imageView && !(imageView instanceof IPhotoView) && !ScaleType.MATRIX.equals(imageView.getScaleType())) {
            imageView.setScaleType(ScaleType.MATRIX);
        }

    }

    public PhotoViewAttacher(ImageView imageView) {
        this(imageView, true);
    }

    public PhotoViewAttacher(ImageView imageView, boolean zoomable) {
        this.ZOOM_DURATION = 200;
        this.mMinScale = 1.0F;
        this.mMidScale = 1.75F;
        this.mMaxScale = 3.0F;
        this.mAllowParentInterceptOnEdge = true;
        this.mBlockParentIntercept = false;
        this.mBaseMatrix = new Matrix();
        this.mDrawMatrix = new Matrix();
        this.mSuppMatrix = new Matrix();
        this.mDisplayRect = new RectF();
        this.mMatrixValues = new float[9];
        this.mScrollEdge = 2;
        this.mScaleType = ScaleType.FIT_CENTER;
        this.mImageView = new WeakReference(imageView);
        imageView.setDrawingCacheEnabled(true);
        imageView.setOnTouchListener(this);
        ViewTreeObserver observer = imageView.getViewTreeObserver();
        if (null != observer) {
            observer.addOnGlobalLayoutListener(this);
        }

        setImageViewScaleTypeMatrix(imageView);
        if (!imageView.isInEditMode()) {
            this.mScaleDragDetector = VersionedGestureDetector.newInstance(imageView.getContext(), this);
            this.mGestureDetector = new GestureDetector(imageView.getContext(), new SimpleOnGestureListener() {
                public void onLongPress(MotionEvent e) {
                    if (null != io.rong.photoview.PhotoViewAttacher.this.mLongClickListener) {
                        io.rong.photoview.PhotoViewAttacher.this.mLongClickListener.onLongClick(io.rong.photoview.PhotoViewAttacher.this.getImageView());
                    }

                }

                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    if (io.rong.photoview.PhotoViewAttacher.this.mSingleFlingListener != null) {
                        if (io.rong.photoview.PhotoViewAttacher.this.getScale() > 1.0F) {
                            return false;
                        } else {
                            return MotionEventCompat.getPointerCount(e1) <= io.rong.photoview.PhotoViewAttacher.SINGLE_TOUCH && MotionEventCompat.getPointerCount(e2) <= io.rong.photoview.PhotoViewAttacher.SINGLE_TOUCH ? io.rong.photoview.PhotoViewAttacher.this.mSingleFlingListener.onFling(e1, e2, velocityX, velocityY) : false;
                        }
                    } else {
                        return false;
                    }
                }
            });
            this.mGestureDetector.setOnDoubleTapListener(new DefaultOnDoubleTapListener(this));
            this.mBaseRotation = 0.0F;
            this.setZoomable(zoomable);
        }
    }

    public void setOnDoubleTapListener(OnDoubleTapListener newOnDoubleTapListener) {
        if (newOnDoubleTapListener != null) {
            this.mGestureDetector.setOnDoubleTapListener(newOnDoubleTapListener);
        } else {
            this.mGestureDetector.setOnDoubleTapListener(new DefaultOnDoubleTapListener(this));
        }

    }

    public void setOnScaleChangeListener(io.rong.photoview.PhotoViewAttacher.OnScaleChangeListener onScaleChangeListener) {
        this.mScaleChangeListener = onScaleChangeListener;
    }

    public void setOnSingleFlingListener(io.rong.photoview.PhotoViewAttacher.OnSingleFlingListener onSingleFlingListener) {
        this.mSingleFlingListener = onSingleFlingListener;
    }

    public boolean canZoom() {
        return this.mZoomEnabled;
    }

    public void cleanup() {
        if (null != this.mImageView) {
            ImageView imageView = (ImageView) this.mImageView.get();
            if (null != imageView) {
                ViewTreeObserver observer = imageView.getViewTreeObserver();
                if (null != observer && observer.isAlive()) {
                    observer.removeGlobalOnLayoutListener(this);
                }

                imageView.setOnTouchListener((OnTouchListener) null);
                this.cancelFling();
            }

            if (null != this.mGestureDetector) {
                this.mGestureDetector.setOnDoubleTapListener((OnDoubleTapListener) null);
            }

            this.mMatrixChangeListener = null;
            this.mPhotoTapListener = null;
            this.mViewTapListener = null;
            this.mImageView = null;
        }
    }

    public RectF getDisplayRect() {
        this.checkMatrixBounds();
        return this.getDisplayRect(this.getDrawMatrix());
    }

    public boolean setDisplayMatrix(Matrix finalMatrix) {
        if (finalMatrix == null) {
            throw new IllegalArgumentException("Matrix cannot be null");
        } else {
            ImageView imageView = this.getImageView();
            if (null == imageView) {
                return false;
            } else if (null == imageView.getDrawable()) {
                return false;
            } else {
                this.mSuppMatrix.set(finalMatrix);
                this.setImageViewMatrix(this.getDrawMatrix());
                this.checkMatrixBounds();
                return true;
            }
        }
    }

    public void setBaseRotation(float degrees) {
        this.mBaseRotation = degrees % 360.0F;
        this.update();
        this.setRotationBy(this.mBaseRotation);
        this.checkAndDisplayMatrix();
    }

    /**
     * @deprecated
     */
    public void setPhotoViewRotation(float degrees) {
        this.mSuppMatrix.setRotate(degrees % 360.0F);
        this.checkAndDisplayMatrix();
    }

    public void setRotationTo(float degrees) {
        this.mSuppMatrix.setRotate(degrees % 360.0F);
        this.checkAndDisplayMatrix();
    }

    public void setRotationBy(float degrees) {
        this.mSuppMatrix.postRotate(degrees % 360.0F);
        this.checkAndDisplayMatrix();
    }

    public ImageView getImageView() {
        ImageView imageView = null;
        if (null != this.mImageView) {
            imageView = (ImageView) this.mImageView.get();
        }

        if (null == imageView) {
            this.cleanup();
            LogManager.getLogger().i("PhotoViewAttacher", "ImageView no longer exists. You should not use this PhotoViewAttacher any more.");
        }

        return imageView;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public float getMinScale() {
        return this.getMinimumScale();
    }

    public float getMinimumScale() {
        return this.mMinScale;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public float getMidScale() {
        return this.getMediumScale();
    }

    public float getMediumScale() {
        return this.mMidScale;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public float getMaxScale() {
        return this.getMaximumScale();
    }

    public float getMaximumScale() {
        return this.mMaxScale;
    }

    public float getScale() {
        return (float) Math.sqrt((double) ((float) Math.pow((double) this.getValue(this.mSuppMatrix, 0), 2.0D) + (float) Math.pow((double) this.getValue(this.mSuppMatrix, 3), 2.0D)));
    }

    public ScaleType getScaleType() {
        return this.mScaleType;
    }

    public void onDrag(float dx, float dy) {
        if (!this.mScaleDragDetector.isScaling()) {
            if (DEBUG) {
                LogManager.getLogger().d("PhotoViewAttacher", String.format("onDrag: dx: %.2f. dy: %.2f", dx, dy));
            }

            ImageView imageView = this.getImageView();
            this.mSuppMatrix.postTranslate(dx, dy);
            this.checkAndDisplayMatrix();
            ViewParent parent = imageView.getParent();
            if (this.mAllowParentInterceptOnEdge && !this.mScaleDragDetector.isScaling() && !this.mBlockParentIntercept) {
                if ((this.mScrollEdge == 2 || this.mScrollEdge == 0 && dx >= 1.0F || this.mScrollEdge == 1 && dx <= -1.0F) && null != parent) {
                    parent.requestDisallowInterceptTouchEvent(false);
                }
            } else if (null != parent) {
                parent.requestDisallowInterceptTouchEvent(true);
            }

        }
    }

    public void onFling(float startX, float startY, float velocityX, float velocityY) {
        if (DEBUG) {
            LogManager.getLogger().d("PhotoViewAttacher", "onFling. sX: " + startX + " sY: " + startY + " Vx: " + velocityX + " Vy: " + velocityY);
        }

        ImageView imageView = this.getImageView();
        this.mCurrentFlingRunnable = new io.rong.photoview.PhotoViewAttacher.FlingRunnable(imageView.getContext());
        this.mCurrentFlingRunnable.fling(this.getImageViewWidth(imageView), this.getImageViewHeight(imageView), (int) velocityX, (int) velocityY);
        imageView.post(this.mCurrentFlingRunnable);
    }

    public void onGlobalLayout() {
        ImageView imageView = this.getImageView();
        if (null != imageView) {
            if (this.mZoomEnabled) {
                int top = imageView.getTop();
                int right = imageView.getRight();
                int bottom = imageView.getBottom();
                int left = imageView.getLeft();
                if (top != this.mIvTop || bottom != this.mIvBottom || left != this.mIvLeft || right != this.mIvRight) {
                    this.updateBaseMatrix(imageView.getDrawable());
                    this.mIvTop = top;
                    this.mIvRight = right;
                    this.mIvBottom = bottom;
                    this.mIvLeft = left;
                }
            } else {
                this.updateBaseMatrix(imageView.getDrawable());
            }
        }

    }

    public void onScale(float scaleFactor, float focusX, float focusY) {
        if (DEBUG) {
            LogManager.getLogger().d("PhotoViewAttacher", String.format("onScale: scale: %.2f. fX: %.2f. fY: %.2f", scaleFactor, focusX, focusY));
        }

        if ((this.getScale() < this.mMaxScale || scaleFactor < 1.0F) && (this.getScale() > this.mMinScale || scaleFactor > 1.0F)) {
            if (null != this.mScaleChangeListener) {
                this.mScaleChangeListener.onScaleChange(scaleFactor, focusX, focusY);
            }

            this.mSuppMatrix.postScale(scaleFactor, scaleFactor, focusX, focusY);
            this.checkAndDisplayMatrix();
        }

    }

    @SuppressLint({"ClickableViewAccessibility"})
    public boolean onTouch(View v, MotionEvent ev) {
        boolean handled = false;
        if (this.mZoomEnabled && hasDrawable((ImageView) v)) {
            ViewParent parent = v.getParent();
            switch (ev.getAction()) {
                case 0:
                    if (null != parent) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    } else {
                        LogManager.getLogger().i("PhotoViewAttacher", "onTouch getParent() returned null");
                    }

                    this.cancelFling();
                    break;
                case 1:
                case 3:
                    if (this.getScale() < this.mMinScale) {
                        RectF rect = this.getDisplayRect();
                        if (null != rect) {
                            v.post(new io.rong.photoview.PhotoViewAttacher.AnimatedZoomRunnable(this.getScale(), this.mMinScale, rect.centerX(), rect.centerY()));
                            handled = true;
                        }
                    }
                case 2:
            }

            if (null != this.mScaleDragDetector) {
                boolean wasScaling = this.mScaleDragDetector.isScaling();
                boolean wasDragging = this.mScaleDragDetector.isDragging();
                handled = this.mScaleDragDetector.onTouchEvent(ev);
                boolean didntScale = !wasScaling && !this.mScaleDragDetector.isScaling();
                boolean didntDrag = !wasDragging && !this.mScaleDragDetector.isDragging();
                this.mBlockParentIntercept = didntScale && didntDrag;
            }

            if (null != this.mGestureDetector && this.mGestureDetector.onTouchEvent(ev)) {
                handled = true;
            }
        }

        return handled;
    }

    public void setAllowParentInterceptOnEdge(boolean allow) {
        this.mAllowParentInterceptOnEdge = allow;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public void setMinScale(float minScale) {
        this.setMinimumScale(minScale);
    }

    public void setMinimumScale(float minimumScale) {
        checkZoomLevels(minimumScale, this.mMidScale, this.mMaxScale);
        this.mMinScale = minimumScale;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public void setMidScale(float midScale) {
        this.setMediumScale(midScale);
    }

    public void setMediumScale(float mediumScale) {
        checkZoomLevels(this.mMinScale, mediumScale, this.mMaxScale);
        this.mMidScale = mediumScale;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public void setMaxScale(float maxScale) {
        this.setMaximumScale(maxScale);
    }

    public void setMaximumScale(float maximumScale) {
        checkZoomLevels(this.mMinScale, this.mMidScale, maximumScale);
        this.mMaxScale = maximumScale;
    }

    public void setScaleLevels(float minimumScale, float mediumScale, float maximumScale) {
        checkZoomLevels(minimumScale, mediumScale, maximumScale);
        this.mMinScale = minimumScale;
        this.mMidScale = mediumScale;
        this.mMaxScale = maximumScale;
    }

    public void setOnLongClickListener(OnLongClickListener listener) {
        this.mLongClickListener = listener;
    }

    public void setOnMatrixChangeListener(io.rong.photoview.PhotoViewAttacher.OnMatrixChangedListener listener) {
        this.mMatrixChangeListener = listener;
    }

    public void setOnPhotoTapListener(io.rong.photoview.PhotoViewAttacher.OnPhotoTapListener listener) {
        this.mPhotoTapListener = listener;
    }

    public io.rong.photoview.PhotoViewAttacher.OnPhotoTapListener getOnPhotoTapListener() {
        return this.mPhotoTapListener;
    }

    public void setOnViewTapListener(io.rong.photoview.PhotoViewAttacher.OnViewTapListener listener) {
        this.mViewTapListener = listener;
    }

    public io.rong.photoview.PhotoViewAttacher.OnViewTapListener getOnViewTapListener() {
        return this.mViewTapListener;
    }

    public void setScale(float scale) {
        this.setScale(scale, false);
    }

    public void setScale(float scale, boolean animate) {
        ImageView imageView = this.getImageView();
        if (null != imageView) {
            this.setScale(scale, (float) (imageView.getRight() / 2), (float) (imageView.getBottom() / 2), animate);
        }

    }

    public void setScale(float scale, float focalX, float focalY, boolean animate) {
        ImageView imageView = this.getImageView();
        if (null != imageView) {
            if (scale < this.mMinScale || scale > this.mMaxScale) {
                LogManager.getLogger().i("PhotoViewAttacher", "Scale must be within the range of minScale and maxScale");
                return;
            }

            if (animate) {
                imageView.post(new io.rong.photoview.PhotoViewAttacher.AnimatedZoomRunnable(this.getScale(), scale, focalX, focalY));
            } else {
                this.mSuppMatrix.setScale(scale, scale, focalX, focalY);
                this.checkAndDisplayMatrix();
            }
        }

    }

    public void setScaleType(ScaleType scaleType) {
        if (isSupportedScaleType(scaleType) && scaleType != this.mScaleType) {
            this.mScaleType = scaleType;
            this.update();
        }

    }

    public void setZoomable(boolean zoomable) {
        this.mZoomEnabled = zoomable;
        this.update();
    }

    public void update() {
        ImageView imageView = this.getImageView();
        if (null != imageView) {
            if (this.mZoomEnabled) {
                setImageViewScaleTypeMatrix(imageView);
                this.updateBaseMatrix(imageView.getDrawable());
            } else {
                this.resetMatrix();
            }
        }

    }

    public Matrix getDisplayMatrix() {
        return new Matrix(this.getDrawMatrix());
    }

    public void getDisplayMatrix(Matrix matrix) {
        matrix.set(this.getDrawMatrix());
    }

    public Matrix getDrawMatrix() {
        this.mDrawMatrix.set(this.mBaseMatrix);
        this.mDrawMatrix.postConcat(this.mSuppMatrix);
        return this.mDrawMatrix;
    }

    private void cancelFling() {
        if (null != this.mCurrentFlingRunnable) {
            this.mCurrentFlingRunnable.cancelFling();
            this.mCurrentFlingRunnable = null;
        }

    }

    private void checkAndDisplayMatrix() {
        if (this.checkMatrixBounds()) {
            this.setImageViewMatrix(this.getDrawMatrix());
        }

    }

    private void checkImageViewScaleType() {
        ImageView imageView = this.getImageView();
        if (null != imageView && !(imageView instanceof IPhotoView) && !ScaleType.MATRIX.equals(imageView.getScaleType())) {
            throw new IllegalStateException("The ImageView's ScaleType has been changed since attaching a PhotoViewAttacher");
        }
    }

    private boolean checkMatrixBounds() {
        ImageView imageView = this.getImageView();
        if (null == imageView) {
            return false;
        } else {
            RectF rect = this.getDisplayRect(this.getDrawMatrix());
            if (null == rect) {
                return false;
            } else {
                float height = rect.height();
                float width = rect.width();
                float deltaX = 0.0F;
                float deltaY = 0.0F;
                int viewHeight = this.getImageViewHeight(imageView);
                if (height <= (float) viewHeight) {
                    switch (this.mScaleType) {
                        case FIT_START:
                            deltaY = -rect.top;
                            break;
                        case FIT_END:
                            deltaY = (float) viewHeight - height - rect.top;
                            break;
                        default:
                            deltaY = ((float) viewHeight - height) / 2.0F - rect.top;
                    }
                } else if (rect.top > 0.0F) {
                    deltaY = -rect.top;
                } else if (rect.bottom < (float) viewHeight) {
                    deltaY = (float) viewHeight - rect.bottom;
                }

                int viewWidth = this.getImageViewWidth(imageView);
                if (width <= (float) viewWidth) {
                    switch (this.mScaleType) {
                        case FIT_START:
                            deltaX = -rect.left;
                            break;
                        case FIT_END:
                            deltaX = (float) viewWidth - width - rect.left;
                            break;
                        default:
                            deltaX = ((float) viewWidth - width) / 2.0F - rect.left;
                    }

                    this.mScrollEdge = 2;
                } else if (rect.left > 0.0F) {
                    this.mScrollEdge = 0;
                    deltaX = -rect.left;
                } else if (rect.right < (float) viewWidth) {
                    deltaX = (float) viewWidth - rect.right;
                    this.mScrollEdge = 1;
                } else {
                    this.mScrollEdge = -1;
                }

                this.mSuppMatrix.postTranslate(deltaX, deltaY);
                return true;
            }
        }
    }

    private RectF getDisplayRect(Matrix matrix) {
        ImageView imageView = this.getImageView();
        if (null != imageView) {
            Drawable d = imageView.getDrawable();
            if (null != d) {
                this.mDisplayRect.set(0.0F, 0.0F, (float) d.getIntrinsicWidth(), (float) d.getIntrinsicHeight());
                matrix.mapRect(this.mDisplayRect);
                return this.mDisplayRect;
            }
        }

        return null;
    }

    public Bitmap getVisibleRectangleBitmap() {
        ImageView imageView = this.getImageView();
        return imageView == null ? null : imageView.getDrawingCache();
    }

    public void setZoomTransitionDuration(int milliseconds) {
        if (milliseconds < 0) {
            milliseconds = 200;
        }

        this.ZOOM_DURATION = milliseconds;
    }

    public IPhotoView getIPhotoViewImplementation() {
        return this;
    }

    private float getValue(Matrix matrix, int whichValue) {
        matrix.getValues(this.mMatrixValues);
        return this.mMatrixValues[whichValue];
    }

    protected void resetMatrix() {
        this.mSuppMatrix.reset();
        this.setRotationBy(this.mBaseRotation);
        this.setImageViewMatrix(this.getDrawMatrix());
        this.checkMatrixBounds();
    }

    private void setImageViewMatrix(Matrix matrix) {
        ImageView imageView = this.getImageView();
        if (null != imageView) {
            this.checkImageViewScaleType();
            imageView.setImageMatrix(matrix);
            if (null != this.mMatrixChangeListener) {
                RectF displayRect = this.getDisplayRect(matrix);
                if (null != displayRect) {
                    this.mMatrixChangeListener.onMatrixChanged(displayRect);
                }
            }
        }

    }

    protected void updateBaseMatrix(Drawable d) {
        ImageView imageView = this.getImageView();
        if (null != imageView && null != d) {
            float viewWidth = (float) this.getImageViewWidth(imageView);
            float viewHeight = (float) this.getImageViewHeight(imageView);
            int drawableWidth = d.getIntrinsicWidth();
            int drawableHeight = d.getIntrinsicHeight();
            this.mBaseMatrix.reset();
            float widthScale = viewWidth / (float) drawableWidth;
            float heightScale = viewHeight / (float) drawableHeight;
            if (this.mScaleType == ScaleType.CENTER) {
                this.mBaseMatrix.postTranslate((viewWidth - (float) drawableWidth) / 2.0F, (viewHeight - (float) drawableHeight) / 2.0F);
            } else {
                float scale;
                if (this.mScaleType == ScaleType.CENTER_CROP) {
                    scale = Math.max(widthScale, heightScale);
                    this.mBaseMatrix.postScale(scale, scale);
                    this.mBaseMatrix.postTranslate((viewWidth - (float) drawableWidth * scale) / 2.0F, (viewHeight - (float) drawableHeight * scale) / 2.0F);
                } else if (this.mScaleType == ScaleType.CENTER_INSIDE) {
                    scale = Math.min(1.0F, Math.min(widthScale, heightScale));
                    this.mBaseMatrix.postScale(scale, scale);
                    this.mBaseMatrix.postTranslate((viewWidth - (float) drawableWidth * scale) / 2.0F, (viewHeight - (float) drawableHeight * scale) / 2.0F);
                } else {
                    RectF mTempSrc = new RectF(0.0F, 0.0F, (float) drawableWidth, (float) drawableHeight);
                    RectF mTempDst = new RectF(0.0F, 0.0F, viewWidth, viewHeight);
                    if ((int) this.mBaseRotation % 180 != 0) {
                        mTempSrc = new RectF(0.0F, 0.0F, (float) drawableHeight, (float) drawableWidth);
                    }

                    switch (this.mScaleType) {
                        case FIT_START:
                            this.mBaseMatrix.setRectToRect(mTempSrc, mTempDst, ScaleToFit.START);
                            break;
                        case FIT_END:
                            this.mBaseMatrix.setRectToRect(mTempSrc, mTempDst, ScaleToFit.END);
                            break;
                        case FIT_CENTER:
                            this.mBaseMatrix.setRectToRect(mTempSrc, mTempDst, ScaleToFit.CENTER);
                            break;
                        case FIT_XY:
                            this.mBaseMatrix.setRectToRect(mTempSrc, mTempDst, ScaleToFit.FILL);
                    }
                }
            }

            this.resetMatrix();
        }
    }

    protected int getImageViewWidth(ImageView imageView) {
        return null == imageView ? 0 : imageView.getWidth() - imageView.getPaddingLeft() - imageView.getPaddingRight();
    }

    protected int getImageViewHeight(ImageView imageView) {
        return null == imageView ? 0 : imageView.getHeight() - imageView.getPaddingTop() - imageView.getPaddingBottom();
    }

    private class FlingRunnable implements Runnable {
        private final ScrollerProxy mScroller;
        private int mCurrentX;
        private int mCurrentY;

        public FlingRunnable(Context context) {
            this.mScroller = ScrollerProxy.getScroller(context);
        }

        public void cancelFling() {
            if (io.rong.photoview.PhotoViewAttacher.DEBUG) {
                LogManager.getLogger().d("PhotoViewAttacher", "Cancel Fling");
            }

            this.mScroller.forceFinished(true);
        }

        public void fling(int viewWidth, int viewHeight, int velocityX, int velocityY) {
            RectF rect = io.rong.photoview.PhotoViewAttacher.this.getDisplayRect();
            if (null != rect) {
                int startX = Math.round(-rect.left);
                int minX;
                int maxX;
                if ((float) viewWidth < rect.width()) {
                    minX = 0;
                    maxX = Math.round(rect.width() - (float) viewWidth);
                } else {
                    maxX = startX;
                    minX = startX;
                }

                int startY = Math.round(-rect.top);
                int minY;
                int maxY;
                if ((float) viewHeight < rect.height()) {
                    minY = 0;
                    maxY = Math.round(rect.height() - (float) viewHeight);
                } else {
                    maxY = startY;
                    minY = startY;
                }

                this.mCurrentX = startX;
                this.mCurrentY = startY;
                if (io.rong.photoview.PhotoViewAttacher.DEBUG) {
                    LogManager.getLogger().d("PhotoViewAttacher", "fling. StartX:" + startX + " StartY:" + startY + " MaxX:" + maxX + " MaxY:" + maxY);
                }

                if (startX != maxX || startY != maxY) {
                    this.mScroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY, 0, 0);
                }

            }
        }

        public void run() {
            if (!this.mScroller.isFinished()) {
                ImageView imageView = io.rong.photoview.PhotoViewAttacher.this.getImageView();
                if (null != imageView && this.mScroller.computeScrollOffset()) {
                    int newX = this.mScroller.getCurrX();
                    int newY = this.mScroller.getCurrY();
                    if (io.rong.photoview.PhotoViewAttacher.DEBUG) {
                        LogManager.getLogger().d("PhotoViewAttacher", "fling run(). CurrentX:" + this.mCurrentX + " CurrentY:" + this.mCurrentY + " NewX:" + newX + " NewY:" + newY);
                    }

                    io.rong.photoview.PhotoViewAttacher.this.mSuppMatrix.postTranslate((float) (this.mCurrentX - newX), (float) (this.mCurrentY - newY));
                    io.rong.photoview.PhotoViewAttacher.this.setImageViewMatrix(io.rong.photoview.PhotoViewAttacher.this.getDrawMatrix());
                    this.mCurrentX = newX;
                    this.mCurrentY = newY;
                    Compat.postOnAnimation(imageView, this);
                }

            }
        }
    }

    private class AnimatedZoomRunnable implements Runnable {
        private final float mFocalX;
        private final float mFocalY;
        private final long mStartTime;
        private final float mZoomStart;
        private final float mZoomEnd;

        public AnimatedZoomRunnable(float currentZoom, float targetZoom, float focalX, float focalY) {
            this.mFocalX = focalX;
            this.mFocalY = focalY;
            this.mStartTime = System.currentTimeMillis();
            this.mZoomStart = currentZoom;
            this.mZoomEnd = targetZoom;
        }

        public void run() {
            ImageView imageView = io.rong.photoview.PhotoViewAttacher.this.getImageView();
            if (imageView != null) {
                float t = this.interpolate();
                float scale = this.mZoomStart + t * (this.mZoomEnd - this.mZoomStart);
                float deltaScale = scale / io.rong.photoview.PhotoViewAttacher.this.getScale();
                io.rong.photoview.PhotoViewAttacher.this.onScale(deltaScale, this.mFocalX, this.mFocalY);
                if (t < 1.0F) {
                    Compat.postOnAnimation(imageView, this);
                }

            }
        }

        private float interpolate() {
            float t = 1.0F * (float) (System.currentTimeMillis() - this.mStartTime) / (float) io.rong.photoview.PhotoViewAttacher.this.ZOOM_DURATION;
            t = Math.min(1.0F, t);
            t = io.rong.photoview.PhotoViewAttacher.sInterpolator.getInterpolation(t);
            return t;
        }
    }

    public interface OnSingleFlingListener {
        boolean onFling(MotionEvent var1, MotionEvent var2, float var3, float var4);
    }

    public interface OnViewTapListener {
        void onViewTap(View var1, float var2, float var3);
    }

    public interface OnPhotoTapListener {
        void onPhotoTap(View var1, float var2, float var3);

        void onOutsidePhotoTap();
    }

    public interface OnScaleChangeListener {
        void onScaleChange(float var1, float var2, float var3);
    }

    public interface OnMatrixChangedListener {
        void onMatrixChanged(RectF var1);
    }
}
