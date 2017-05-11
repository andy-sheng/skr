package com.wali.live.livesdk.live.manager;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.WindowManager;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.mi.live.engine.base.GalileoConstants;
import com.mi.live.engine.streamer.IStreamer;
import com.mi.milink.sdk.base.CustomHandlerThread;
import com.wali.live.livesdk.live.opengl.GLRendererOES;
import com.wali.live.livesdk.live.utils.AudioRecordWorker;
import com.wali.live.livesdk.live.utils.LooperScheduler;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;

/**
 * Created by chenyong on 2016/10/17.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ScreenRecordManager implements SurfaceTexture.OnFrameAvailableListener {

    private final static String TAG = "ScreenRecordManager";

    private static final int MSG_SCREENSHOT = 100;

    private static final int SCREEN_RECORD_DURATION = 66;
    private static final int AUDIO_FREQUENCY = 44100;

    private int mDisplayWidth = GalileoConstants.GAME_LOW_RESOLUTION_WIDTH;
    private int mDisplayHeight = GalileoConstants.GAME_LOW_RESOLUTION_HEIGHT;

    private IStreamer mStreamer;
    private long mScreenRecordId;
    private ImageReader mImageReader;
    private int mScreenDensity;
    private VirtualDisplay mVirtualDisplay;
    private boolean mIsPause = false;
    private boolean mIsLandscape;
    private MediaProjection mMediaProjection;
    private boolean mIsRecording;
    private AudioRecordWorker mAudioRecordWorker;
    private final ImageData mLastImgData = new ImageData();
    private final ImageData mImageData = new ImageData();
    private Subscription mScreenRecordSubscription;
    private GLRendererOES mGLRendererOES;
    private SurfaceTexture mSurfaceTexture;
    private Surface mSurface;
    private boolean mIsFrameAvailable;
    private float[] mMatrix = new float[16];
    private ByteBuffer mPixelBuffer;
    private Intent mIntent;

    private CustomHandlerThread mCustomHandlerThread = new CustomHandlerThread("ScreenRecord") {
        @Override
        protected void processMessage(Message message) {

        }
    };

    public ScreenRecordManager(IStreamer streamer, int width, int height, Intent intent) {
        mStreamer = streamer;
        mDisplayWidth = width;
        mDisplayHeight = height;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            WindowManager windowManager = (WindowManager) GlobalData.app().getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics metrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(metrics);
            mScreenDensity = metrics.densityDpi;
            mIntent = intent;
            mMediaProjection = ((MediaProjectionManager) GlobalData.app()
                    .getSystemService(Context.MEDIA_PROJECTION_SERVICE)).getMediaProjection(Activity.RESULT_OK, intent);
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        mIsFrameAvailable = true;
    }

    public interface OnScreenshotReadyListener {
        void onScreenshotReady(Bitmap bitmap);
    }

    public void getScreenshot(final OnScreenshotReadyListener listener) {
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
                if (mLastImgData == null || mLastImgData.data == null) {
                    return;
                }
                if (mImageData.stride > 0 && mImageData.height > 0) {
                    Bitmap bitmap = Bitmap.createBitmap(mImageData.stride, mImageData.height, Bitmap.Config.ARGB_8888);
                    ByteBuffer byteBuffer = ByteBuffer.wrap(mImageData.data);
                    bitmap.copyPixelsFromBuffer(byteBuffer);
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, mImageData.width, mImageData.height);
                    listener.onScreenshotReady(bitmap);
                }
            }
        });
    }

    public void setOrientation(boolean isLandscape) {
        if (mStreamer != null && mIsLandscape != isLandscape) {
            mIsLandscape = isLandscape;
            if (isLandscape) {
                mStreamer.setUpOutputFrameResolution(mDisplayHeight, mDisplayWidth);
            } else {
                mStreamer.setUpOutputFrameResolution(mDisplayWidth, mDisplayHeight);
            }
            resetVirtualDisplay();
        }
    }

    private void resetVirtualDisplay() {
        MyLog.w(TAG, "resetVirtualDisplay");
        releaseVirtualDisplay();
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
                mImageReader = ImageReader.newInstance(mIsLandscape ? mDisplayHeight : mDisplayWidth, mIsLandscape ? mDisplayWidth : mDisplayHeight, PixelFormat.RGBA_8888, 2);
                try {
                    mVirtualDisplay = mMediaProjection.createVirtualDisplay(
                            "screen-mirror",
                            mIsLandscape ? mDisplayHeight : mDisplayWidth,
                            mIsLandscape ? mDisplayWidth : mDisplayHeight,
                            mScreenDensity,
                            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                            mImageReader.getSurface(),
                            null,
                            null);
                } catch (SecurityException e) {
                    MyLog.e(TAG, e);
                    if ("Invalid media projection".equals(e.getMessage())) {
                        mMediaProjection = ((MediaProjectionManager) GlobalData.app()
                                .getSystemService(Context.MEDIA_PROJECTION_SERVICE)).getMediaProjection(Activity.RESULT_OK, mIntent);
                        try {
                            mVirtualDisplay = mMediaProjection.createVirtualDisplay(
                                    "screen-mirror",
                                    mIsLandscape ? mDisplayHeight : mDisplayWidth,
                                    mIsLandscape ? mDisplayWidth : mDisplayHeight,
                                    mScreenDensity,
                                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                                    mImageReader.getSurface(),
                                    null,
                                    null);
                        } catch (Exception e1) {
                            MyLog.e(TAG, e1);
                        }
                    }
                } catch (Exception e) {
                    MyLog.e(TAG, e);
                }
//            mGLRendererOES = new GLRendererOES();
//            mGLRendererOES.init(mDisplayWidth, mDisplayHeight);
//            mSurfaceTexture = new SurfaceTexture(mGLRendererOES.getVideoTexture());
//            mSurfaceTexture.setDefaultBufferSize(mDisplayWidth, mDisplayHeight);
//            mSurfaceTexture.setOnFrameAvailableListener(this);
//            mSurface = new Surface(mSurfaceTexture);
//            mPixelBuffer = ByteBuffer.allocate(mDisplayWidth * mDisplayHeight * 4);
//            mVirtualDisplay = mMediaProjection.createVirtualDisplay(
//                    "screen-mirror",
//                    mIsLandscape ? mDisplayHeight : mDisplayWidth,
//                    mIsLandscape ? mDisplayWidth : mDisplayHeight,
//                    mScreenDensity,
//                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
//                    mSurface,
//                    null,
//                    null);
                if (mStreamer != null && mScreenRecordId != 0) {
                    mStreamer.startAddExtra(mScreenRecordId, 0, 0, 1, 1, 1, 1, 1);
                }
            }
        });
    }

    private void releaseVirtualDisplay() {
        mCustomHandlerThread.post(new Runnable() {
            @Override
            public void run() {
                if (mVirtualDisplay != null) {
                    mVirtualDisplay.release();
                    mVirtualDisplay = null;
                }
                if (mImageReader != null) {
                    mImageReader.close();
                    mImageReader = null;
                }
                if (mSurface != null) {
                    mSurface.release();
                    mSurface = null;
                }
                if (mSurfaceTexture != null) {
                    mSurfaceTexture.release();
                    mSurfaceTexture = null;
                }
                if (mGLRendererOES != null) {
                    mGLRendererOES.release();
                    mGLRendererOES = null;
                }
            }
        });
    }

    public void startScreenRecord() {
        if (mStreamer != null && mScreenRecordId == 0) {
            if (mIsRecording) {
                return;
            }
            resetVirtualDisplay();
            mScreenRecordId = System.currentTimeMillis();
            mStreamer.startAddExtra(mScreenRecordId, 0, 0, 1, 1, 1, 1, 1);
            mStreamer.setVideoMainStream(mScreenRecordId, true);
            registerScreenRecord();
            mIsRecording = true;
            mAudioRecordWorker = new AudioRecordWorker();
            mAudioRecordWorker.post(new Runnable() {
                @Override
                public void run() {
                    startRecord();
                }
            });
        }
    }

    public void pause() {
        mIsPause = true;
        if (mScreenRecordSubscription != null && !mScreenRecordSubscription.isUnsubscribed()) {
            mScreenRecordSubscription.unsubscribe();
        }
    }

    public void resume() {
        if (mIsRecording) {
            registerScreenRecord();
            mAudioRecordWorker.post(new Runnable() {
                @Override
                public void run() {
                    mIsPause = false;
                    startRecord();
                }
            });
        }
    }

    private void registerScreenRecord() {
        if (mScreenRecordSubscription != null && !mScreenRecordSubscription.isUnsubscribed()) {
            mScreenRecordSubscription.unsubscribe();
        }
        mScreenRecordSubscription =
                Observable.interval(SCREEN_RECORD_DURATION, TimeUnit.MILLISECONDS)
                        .onBackpressureDrop()
                        .observeOn(new LooperScheduler(mCustomHandlerThread.getHandler()))
                        .subscribe(new Subscriber<Long>() {
                            @Override
                            public void onCompleted() {
                            }

                            @Override
                            public void onError(Throwable e) {
                                MyLog.e(TAG, "startScreenRecord failed, exception=" + e);
                            }

                            @Override
                            public void onNext(Long aLong) {
                                if (!mIsPause && mScreenRecordId != 0) {
                                    ImageData imageData = captureImageData(mImageReader);
//                                        ImageData imageData = captureImageData();
                                    if (imageData != null && imageData.checkSize()) {
                                        mLastImgData.copyFrom(imageData);
                                    }
                                    IStreamer streamer = mStreamer;
                                    if (streamer != null && mLastImgData.checkData()) {
                                        streamer.putExtraDetailInfoWithTimestamp(mLastImgData.width, mLastImgData.height, mLastImgData.data, mLastImgData.stride,
                                                GalileoConstants.TYPE_BGR, GalileoConstants.SCREEN_FRAME, mScreenRecordId, System.currentTimeMillis());
                                    }
                                }
                            }
                        });
    }

    public void stopScreenRecord() {
        if (mScreenRecordSubscription != null && !mScreenRecordSubscription.isUnsubscribed()) {
            mScreenRecordSubscription.unsubscribe();
        }
        releaseVirtualDisplay();
        if (mStreamer != null && mScreenRecordId != 0) {
            mScreenRecordId = 0;
            mStreamer.stopAddExtra(mScreenRecordId);
        }
        if (mIsRecording) {
            mIsRecording = false;
            mAudioRecordWorker.destroy();
            mAudioRecordWorker = null;
        }
    }

    private ImageData captureImageData(ImageReader imageReader) {
        if (imageReader == null) {
            return null;
        }
        Image image = imageReader.acquireLatestImage();
        if (image == null) {
            return null;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        mImageData.resize(width, height, rowStride / pixelStride, pixelStride);
        buffer.get(mImageData.data);
        image.close();
        return mImageData;
    }

    private ImageData captureImageData() {
        if (mIsFrameAvailable) {
            mIsFrameAvailable = false;
            mSurfaceTexture.updateTexImage();
            mSurfaceTexture.getTransformMatrix(mMatrix);
            mGLRendererOES.draw(mMatrix, System.nanoTime());
            GLES20.glReadPixels(0, 0, mDisplayWidth, mDisplayHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, mPixelBuffer);
            mImageData.resize(mDisplayWidth, mDisplayHeight, mDisplayWidth, 4);
            mPixelBuffer.get(mImageData.data);
            mPixelBuffer.position(0);
            return mImageData;
        } else {
            return null;
        }
    }

    private Bitmap startCapture(ImageReader imageReader) {
        if (imageReader == null) {
            return null;
        }
        Image image = imageReader.acquireLatestImage();
        if (image == null) {
            return null;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
        image.close();
//        ImageUtils.saveToFile(bitmap, "/sdcard/screen.png");
        return bitmap;
    }

    public void destroy() {
        stopScreenRecord();
        mCustomHandlerThread.destroy();
        mStreamer = null;
    }

    private void startRecord() {
        int bufferSize = 2 * AudioRecord.getMinBufferSize(AUDIO_FREQUENCY, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.MIC, AUDIO_FREQUENCY, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        final byte[] buffer = new byte[bufferSize * 2];
        record.startRecording();
        while (mIsRecording && !mIsPause) {
            int size = record.read(buffer, 0, buffer.length);
            final long timestamp = System.currentTimeMillis() - 300;
            IStreamer streamer = mStreamer;
            if (streamer != null && mScreenRecordId != 0) {
                streamer.putExtraAudioFrameWithTimestamp(size / 4, 2, 2, AUDIO_FREQUENCY, buffer, timestamp);
            }
        }
        record.stop();
        record.release();
    }

    private static class ImageData {
        private int width;
        private int height;
        private int stride;
        private int pixelStride;
        private byte[] data = new byte[0];

        public boolean checkSize() {
            return width > 0 && height > 0;
        }

        public boolean checkData() {
            return data != null && data.length > 0;
        }

        public void resize(int width, int height, int stride, int pixelStride) {
            if (stride < width) {
                stride = width;
            }
            int length = pixelStride * stride * height;
            if (data == null || data.length != length) {
                data = new byte[length];
            }
            this.width = width;
            this.height = height;
            this.stride = stride;
            this.pixelStride = pixelStride;
        }

        public void copyFrom(ImageData other) {
            if (other == null) {
                return;
            }
            width = other.width;
            height = other.height;
            stride = other.stride;
            pixelStride = other.stride;
            if (other.data == null) {
                data = null;
            }
            if (data == null || data.length != other.data.length) {
                data = other.data.clone();
            } else {
                System.arraycopy(other.data, 0, data, 0, data.length);
            }
        }
    }
}
