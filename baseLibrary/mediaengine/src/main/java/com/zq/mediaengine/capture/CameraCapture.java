package com.zq.mediaengine.capture;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.zq.mediaengine.capture.camera.CameraHolder;
import com.zq.mediaengine.capture.camera.CameraManager;
import com.zq.mediaengine.capture.camera.CameraUtil;
import com.zq.mediaengine.framework.AVConst;
import com.zq.mediaengine.framework.ImgBufFormat;
import com.zq.mediaengine.framework.ImgBufFrame;
import com.zq.mediaengine.framework.ImgTexFormat;
import com.zq.mediaengine.framework.ImgTexFrame;
import com.zq.mediaengine.framework.SrcPin;
import com.zq.mediaengine.util.FpsLimiter;
import com.zq.mediaengine.util.gles.GLRender;
import com.zq.mediaengine.util.gles.GlUtil;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The CameraCapture class is used to capture video frames from camera.
 */
public class CameraCapture implements SurfaceTexture.OnFrameAvailableListener {
    private final static String TAG = "CameraCapture";
    private final static boolean VERBOSE = true;
    private static final boolean TRACE_FPS_LIMIT = false;

    public final static int FACING_FRONT = Camera.CameraInfo.CAMERA_FACING_FRONT;
    public final static int FACING_BACK = Camera.CameraInfo.CAMERA_FACING_BACK;

    public final static int DEFAULT_PREVIEW_FPS = 15;

    public final static int CAMERA_STATE_IDLE = 0;
    public final static int CAMERA_STATE_INITIALIZING = 1;
    public final static int CAMERA_STATE_PREVIEWING = 2;
    public final static int CAMERA_STATE_STOPPING = 3;

    public final static int CAMERA_ERROR_UNKNOWN = -2001;
    public final static int CAMERA_ERROR_START_FAILED = -2002;
    public final static int CAMERA_ERROR_SERVER_DIED = -2006;
    public final static int CAMERA_ERROR_EVICTED = -2007;

    private final static int MSG_CAMERA_SETUP = 1;
    private final static int MSG_CAMERA_RELEASE = 2;
    private final static int MSG_CAMERA_SWITCH = 3;
    private final static int MSG_CAMERA_QUIT = 4;

    private static final int CAMERA_OPEN_DONE = 1;
    private static final int START_PREVIEW_DONE = 2;
    private static final int CLOSE_CAMERA_DONE = 3;
    private static final int SWITCH_CAMERA_DONE = 4;
    private static final int FIRST_FRAME_RENDERED = 5;
    private static final int CAMERA_FAILED = 11;

    /**
     * Source pin transfer ImgTexFrame, used for gpu path and preview
     */
    private final SrcPin<ImgTexFrame> mImgTexSrcPin;
    /**
     * Source pin transfer ImgBufFrame, used for fallback cpu path
     */
    private final SrcPin<ImgBufFrame> mImgBufSrcPin;

    private Context mContext;
    private OnCameraCaptureListener mOnCameraCaptureListener;

    private int mFacing = FACING_BACK;
    private Size mPresetPreviewSize;
    private float mPresetPreviewFps;
    private Size mPreviewSize;
    private float mPreviewFps;
    private int mOrientationDegrees;
    private String mFocusMode;

    private AtomicInteger mState;
    private final Object mCameraLock = new Object();
    private int mTextureId;
    private SurfaceTexture mSurfaceTexture;

    private CameraManager.CameraProxy mCameraDevice;
    private Camera.Parameters mParameters;

    private final Handler mMainHandler;
    private HandlerThread mCameraSetUpThread;
    private Handler mCameraSetupHandler;
    private ConditionVariable mSig = new ConditionVariable();
    private volatile boolean mStopping = false;

    private ImgTexFormat mImgTexFormat;
    private ImgBufFormat mImgBufFormat;

    private byte[] mPreviewBuf;
    private ByteBuffer mVideoDirectBuffer;

    private GLRender mGLRender;
    private boolean mTexInited = false;
    private boolean mBufInited = false;
    private boolean mFrameAvailable = false;
    private boolean mIsRecording = false;
    private boolean mFirstFrameRendered = false;

    // AE workaround
    private boolean mEnableExposureWorkaround = true;
    private boolean mEnableFrameDrop = true;
    private FpsLimiter mFpsLimiter;

    // Performance trace
    private float mCurrentFps;
    private long mLastTraceTime;
    private long mFrameDrawn;

    public CameraCapture(Context context, GLRender glRender) {
        mContext = context;
        mImgTexSrcPin = new SrcPin<>();
        mImgBufSrcPin = new SrcPin<>();
        mState = new AtomicInteger(CAMERA_STATE_IDLE);
        mMainHandler = new MainHandler(this, Looper.getMainLooper());
        mFpsLimiter = new FpsLimiter();
        initCameraSetupThread();

        mPresetPreviewSize = new Size(1280, 720);
        mPresetPreviewFps = DEFAULT_PREVIEW_FPS;
        mGLRender = glRender;
        mGLRender.addListener(mGLReadyListener);
        mGLRender.addListener(mGLSizeChangedListener);
        mGLRender.addListener(mGLDrawFrameListener);
        mGLRender.addListener(mGLReleasedListener);
    }

    public SrcPin<ImgTexFrame> getImgTexSrcPin() {
        return mImgTexSrcPin;
    }

    public SrcPin<ImgBufFrame> getImgBufSrcPin() {
        return mImgBufSrcPin;
    }

    /**
     * Set CameraCapture Listener.<br/>
     * Should be set before call {@link #start(int)}
     *
     * @param listener listener implemented by user
     */
    public void setOnCameraCaptureListener(OnCameraCaptureListener listener) {
        mOnCameraCaptureListener = listener;
    }

    /**
     * Set preset preview size of camera preview.<br/>
     * Should be set before call {@link #start(int)}
     * <p>
     * <p>The preset value may be different with the value finally used for preview.<br/>
     * The actual preview size can be get by {@link #getTargetPreviewSize} after
     * {@link OnCameraCaptureListener#onStarted() onStarted()} called.
     *
     * @param width  preset width, default value is 1280
     * @param height preset height, default value is 720
     */
    public void setPreviewSize(int width, int height) {
        if (width > height) {
            mPresetPreviewSize = new Size(width, height);
        } else {
            //noinspection SuspiciousNameCombination
            mPresetPreviewSize = new Size(height, width);
        }
    }

    /**
     * Set preset preview fps.<br/>
     * Should be set before call {@link #start(int)}
     * <p>
     * <p>The preset fps may be different with the value finally used for preview.<br/>
     * The actual preview fps can be get by {@link #getTargetPreviewFps()} after
     * {@link OnCameraCaptureListener#onStarted() onStarted()} called.
     *
     * @param fps preset fps, default value is 15.0f
     */
    public void setPreviewFps(float fps) {
        mPresetPreviewFps = fps;
    }

    /**
     * Fix dark preview in some device(such as Pixel) with 24/30fps.
     *
     * @param enableExposureWorkaround true to enable, false to disable, default: true
     */
    public void setEnableExposureWorkaround(boolean enableExposureWorkaround) {
        mEnableExposureWorkaround = enableExposureWorkaround;
    }

    /**
     * Drop frame if the real preview fps greater than the set value.
     *
     * @param enableFrameDrop true to enable, false to disable, default: true
     */
    public void setEnableFrameDrop(boolean enableFrameDrop) {
        mEnableFrameDrop = enableFrameDrop;
    }

    /**
     * Set preview orientation.<br/>
     * Should be set before call {@link #start(int)}
     *
     * @param degrees the rotate degrees of current Activity.<br/>
     *                Acceptable value: 0, 90, 180, 270,
     *                other values will be ignored.<br/>
     *                Default value is 0.
     */
    public void setOrientation(int degrees) {
        if (mOrientationDegrees == degrees) {
            return;
        }

        mOrientationDegrees = degrees;
        if (mState.get() == CAMERA_STATE_PREVIEWING) {
            setDisplayOrientation();
            mTexInited = false;
            mBufInited = false;
        }
    }

    /**
     * Get camera display orientation.<br/>
     * Should be called after {@link #start(int)}
     *
     * @return Camera display orientation in degrees.
     */
    public int getCameraDisplayOrientation() {
        return CameraUtil.getDisplayOrientation(mOrientationDegrees, getCameraId(mFacing));
    }

    /**
     * Start camera preview.<br/>
     * Can only be called on mState IDLE.
     *
     * @param facing camera facing, acceptable value {@link #FACING_BACK}, {@link #FACING_FRONT}
     */
    synchronized public void start(int facing) {
        Log.d(TAG, "start");
        mFacing = facing;
        mCameraSetupHandler.removeCallbacksAndMessages(null);
        mCameraSetupHandler.sendEmptyMessage(MSG_CAMERA_SETUP);
    }

    /**
     * Stop camera preview.<br/>
     * Can be called on mState {@link #CAMERA_STATE_INITIALIZING},
     * {@link #CAMERA_STATE_PREVIEWING}.
     */
    synchronized public void stop() {
        Log.d(TAG, "stop");
        mSig.close();
        mStopping = true;
        mCameraSetupHandler.removeCallbacksAndMessages(null);
        mCameraSetupHandler.sendEmptyMessage(MSG_CAMERA_RELEASE);
        mSig.block();
        mStopping = false;
        Log.d(TAG, "stopped");
    }

    /**
     * Switch front/back camera if supported.
     *
     * @return true if success, false if failed or on invalid mState.
     */
    synchronized public boolean switchCamera() {
        if (mState.get() != CAMERA_STATE_PREVIEWING) {
            Log.e(TAG, "Call start on invalid state");
            return false;
        }

        if (mFacing == FACING_BACK && getCameraId(FACING_FRONT) < 0) {
            return false;
        }

        mCameraSetupHandler.removeMessages(MSG_CAMERA_SWITCH);
        mCameraSetupHandler.sendEmptyMessage(MSG_CAMERA_SWITCH);
        return true;
    }

    /**
     * Check if flash torch mode supported of current camera.<br/>
     * Should be called after {@link OnCameraCaptureListener#onStarted() onStarted()} called.
     *
     * @return false if not supported or called on invalid mState, true if supported.
     */
    synchronized public boolean isTorchSupported() {
        if (mState.get() != CAMERA_STATE_PREVIEWING || mParameters == null) {
            return false;
        }
        List<String> flashModes = mParameters.getSupportedFlashModes();
        if (flashModes == null || flashModes.size() == 0) {
            return false;
        }
        return flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH);
    }

    /**
     * Toggle torch of current camera.
     *
     * @param open true to turn on, false to turn off.
     * @return true if success, false if failed or on invalid mState.
     */
    synchronized public boolean toggleTorch(boolean open) {
        if (mState.get() != CAMERA_STATE_PREVIEWING || mParameters == null) {
            return false;
        }
        List<String> flashModes = mParameters.getSupportedFlashModes();
        if (null == flashModes || flashModes.size() == 0) {
            return false;
        }
        if (open && flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
            mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        } else if (!open && flashModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
            mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        } else {
            return false;
        }
        if (!mCameraDevice.setParametersNoException(mParameters)) {
            Log.e(TAG, "Toggle flash failed!");
            mParameters = mCameraDevice.getParameters();
            return false;
        }
        return true;
    }

    /**
     * Get parameters of current camera.<br/>
     * Should be called on state {@link #CAMERA_STATE_PREVIEWING}.
     *
     * @return Camera parameters instance or null on invalid state.
     */
    synchronized public Camera.Parameters getCameraParameters() {
        if (mState.get() != CAMERA_STATE_PREVIEWING || mCameraDevice == null) {
            return null;
        }
        return mCameraDevice.getParameters();
    }

    /**
     * Set new parameters to current camera.<br/>
     * Should be called on state {@link #CAMERA_STATE_PREVIEWING}.
     *
     * @param parameters Camera parameters to be set.
     * @return true on success, false otherwise.
     */
    synchronized public boolean setCameraParameters(Camera.Parameters parameters) {
        if (mState.get() != CAMERA_STATE_PREVIEWING) {
            return false;
        }
        boolean ret = mCameraDevice.setParametersNoException(parameters);
        mParameters = mCameraDevice.getParameters();
        return ret;
    }

    /**
     * Set new parameters to current camera asynchronous.<br/>
     * Should be called on state {@link #CAMERA_STATE_PREVIEWING}.
     *
     * @param parameters Camera parameters to be set.
     */
    synchronized public void setCameraParametersAsync(Camera.Parameters parameters) {
        if (mState.get() != CAMERA_STATE_PREVIEWING) {
            return;
        }
        mCameraDevice.setParametersAsync(parameters);
    }

    /**
     * Starts camera auto-focus and registers a callback function to run
     * when the camera is focused.<br/>
     * Should be called on state {@link #CAMERA_STATE_PREVIEWING}.
     *
     * @param cb the callback to run
     */
    synchronized public void autoFocus(Camera.AutoFocusCallback cb) {
        if (mState.get() != CAMERA_STATE_PREVIEWING || mCameraDevice == null) {
            Log.e(TAG, "Call autoFocus on invalid state!");
            return;
        }
        mCameraDevice.autoFocus(cb);
    }

    /**
     * Cancels any auto-focus function in progress.Whether or not auto-focus is currently
     * in progress,this function will return the focus position to the default.
     * If the camera does not support auto-focus, this is a no-op.<br/>
     * Should be called on state {@link #CAMERA_STATE_PREVIEWING}.
     */
    synchronized public void cancelAutoFocus() {
        if (mState.get() != CAMERA_STATE_PREVIEWING || mCameraDevice == null) {
            Log.e(TAG, "Call cancelAutoFocus on invalid state!");
            return;
        }
        mCameraDevice.cancelAutoFocus();
    }

    /**
     * Clean resources.<br/>
     * After this call, this object should not be accessed any more.
     */
    synchronized public void release() {
        // stop first
        stop();

        // memory leak occurred without this.
        mPreviewBuf = null;

        // disconnect source pins
        mImgTexSrcPin.disconnect(true);
        mImgBufSrcPin.disconnect(true);

        mGLRender.removeListener(mGLReadyListener);
        mGLRender.removeListener(mGLSizeChangedListener);
        mGLRender.removeListener(mGLDrawFrameListener);
        mGLRender.removeListener(mGLReleasedListener);
        synchronized (mCameraLock) {
            if (mSurfaceTexture != null) {
                mSurfaceTexture.release();
                mSurfaceTexture = null;
            }
        }
        if (mCameraSetUpThread != null) {
            mCameraSetupHandler.sendEmptyMessage(MSG_CAMERA_QUIT);
            try {
                mCameraSetUpThread.join();
            } catch (InterruptedException e) {
                Log.d(TAG, "CameraSetUpThread Interrupted!");
            } finally {
                mCameraSetUpThread = null;
            }
        }
        if (mMainHandler != null) {
            mMainHandler.removeCallbacksAndMessages(null);
        }
    }

    public int getCameraFacing() {
        return mFacing;
    }

    /**
     * Get current mState.
     *
     * @return current mState.
     * @see #CAMERA_STATE_IDLE
     * @see #CAMERA_STATE_INITIALIZING
     * @see #CAMERA_STATE_PREVIEWING
     * @see #CAMERA_STATE_STOPPING
     */
    public int getState() {
        return mState.get();
    }

    /**
     * Get actual preview size currently used
     *
     * @return actual preview size.
     * @see #setPreviewSize(int, int)
     */
    public Size getTargetPreviewSize() {
        return mPreviewSize;
    }

    /**
     * Get actual preview fps set to camera.
     *
     * @return actual preview fps.
     * @see #setPreviewFps(float)
     */
    public float getTargetPreviewFps() {
        return mPreviewFps;
    }

    /**
     * Get actual preview fps in the last second.
     *
     * @return current preview fps.
     */
    public float getCurrentPreviewFps() {
        return mCurrentFps;
    }

    /**
     * start recording
     */
    public void startRecord() {
        mIsRecording = true;
    }

    /**
     * stop recording
     */
    public void stopRecord() {
        mIsRecording = false;
    }

    /**
     * Is recording.
     *
     * @return true if in recording, false if not.
     */
    public boolean isRecording() {
        return mIsRecording;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        //if (VERBOSE) Log.d(TAG, "onFrameAvailable");
        mFrameAvailable = true;
        mGLRender.requestRender();
    }

    /**
     * Notify the event occurred while camera capturing.
     */
    public interface OnCameraCaptureListener {

        /**
         * Notify camera capture started.
         */
        void onStarted();

        /**
         * Notify the first camera video frame rendered.
         */
        void onFirstFrameRendered();

        /**
         * Notify camera facing changed.
         *
         * @param facing new facing
         */
        void onFacingChanged(int facing);

        /**
         * Notify error occurred while camera capturing.
         *
         * @param err err code.
         * @see #CAMERA_ERROR_UNKNOWN
         * @see #CAMERA_ERROR_START_FAILED
         * @see #CAMERA_ERROR_SERVER_DIED
         */
        void onError(int err);
    }

    private void initCameraSetupThread() {
        mCameraSetUpThread = new HandlerThread("camera_setup_thread", Thread.NORM_PRIORITY);
        mCameraSetUpThread.start();
        mCameraSetupHandler = new Handler(mCameraSetUpThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_CAMERA_SETUP: {
                        if (mState.get() == CAMERA_STATE_IDLE) {
                            mState.set(CAMERA_STATE_INITIALIZING);
                            int ret = doCameraSetup();
                            if (ret == 0) {
                                mState.set(CAMERA_STATE_PREVIEWING);
                                mMainHandler.sendEmptyMessage(START_PREVIEW_DONE);
                                mMainHandler.sendEmptyMessage(SWITCH_CAMERA_DONE);
                            } else {
                                mState.set(CAMERA_STATE_IDLE);
                                Message message = mMainHandler.obtainMessage(CAMERA_FAILED, ret, 0);
                                mMainHandler.sendMessage(message);
                            }
                        }
                        break;
                    }
                    case MSG_CAMERA_SWITCH: {
                        if (mState.get() == CAMERA_STATE_PREVIEWING) {
                            mState.set(CAMERA_STATE_INITIALIZING);
                            doCameraRelease();
                            if (mStopping) {
                                mState.set(CAMERA_STATE_IDLE);
                                mMainHandler.sendEmptyMessage(CLOSE_CAMERA_DONE);
                                break;
                            }
                            mFacing = (mFacing == FACING_BACK) ? FACING_FRONT : FACING_BACK;
                            int ret = doCameraSetup();
                            if (ret == 0) {
                                mState.set(CAMERA_STATE_PREVIEWING);
                                mMainHandler.sendEmptyMessage(SWITCH_CAMERA_DONE);
                            } else {
                                mState.set(CAMERA_STATE_IDLE);
                                Message message = mMainHandler.obtainMessage(CAMERA_FAILED, ret, 0);
                                mMainHandler.sendMessage(message);
                            }
                        }
                        break;
                    }
                    case MSG_CAMERA_RELEASE: {
                        if (mState.get() == CAMERA_STATE_PREVIEWING) {
                            mState.set(CAMERA_STATE_STOPPING);
                            doCameraRelease();
                            mState.set(CAMERA_STATE_IDLE);
                            mMainHandler.sendEmptyMessage(CLOSE_CAMERA_DONE);
                        }
                        mSig.open();
                        break;
                    }
                    case MSG_CAMERA_QUIT: {
                        mCameraSetUpThread.quit();
                    }
                }
            }
        };
    }

    private int doCameraSetup() {
        if (mFacing == FACING_FRONT && getCameraId(mFacing) < 0) {
            mFacing = FACING_BACK;
        }
        int cameraId = getCameraId(mFacing);
        try {
            synchronized (mCameraLock) {
                mCameraDevice = CameraUtil.openCamera(mContext, cameraId);
                mCameraDevice.setErrorCallback(cameraErrorCallback);
                mParameters = mCameraDevice.getParameters();
                setCameraParameters();
                mMainHandler.sendEmptyMessage(CAMERA_OPEN_DONE);

                startPreview();
            }
            mTexInited = false;
            mBufInited = false;
        } catch (Exception e) {
            Log.e(TAG, "[setupCamera]-------setup failed");
            return CAMERA_ERROR_START_FAILED;
        }
        return 0;
    }

    private void doCameraRelease() {
        if (!TextUtils.isEmpty(mFocusMode) &&
                mFocusMode.equals(Camera.Parameters.FOCUS_MODE_AUTO)) {
            mCameraDevice.cancelAutoFocus();
        }
        synchronized (mCameraLock) {
            mCameraDevice.stopPreview();
            mCameraDevice.setPreviewCallbackWithBuffer(null);
            //mCameraDevice.setZoomChangeListener(null);
            mCameraDevice.setErrorCallback(null);
            CameraHolder.instance().release();
            mCameraDevice = null;
        }
    }

    private int getCameraId(int facing) {
        return (facing == FACING_BACK) ?
                CameraHolder.instance().getBackCameraId() :
                CameraHolder.instance().getFrontCameraId();
    }

    private void setCameraParameters() {
        if (mParameters == null) {
            return;
        }

        mPreviewSize = CameraUtil.setPreviewResolution(mParameters, mPresetPreviewSize);
        if (!mCameraDevice.setParametersNoException(mParameters)) {
            Log.e(TAG, "setPreviewSize failed");
            mParameters = mCameraDevice.getParameters();
        }

        if (mEnableExposureWorkaround && mFacing == FACING_FRONT && mPresetPreviewFps >= 20) {
            mPreviewFps = CameraUtil.setPreviewFps(mParameters, mPresetPreviewFps, false);
            mCameraDevice.setParametersNoException(mParameters);
        } else {
            mPreviewFps = CameraUtil.setPreviewFps(mParameters, mPresetPreviewFps, true);
            if (!mCameraDevice.setParametersNoException(mParameters)) {
                Log.e(TAG, "setPreviewFps with fixed value failed, retry");
                mPreviewFps = CameraUtil.setPreviewFps(mParameters, mPresetPreviewFps, false);
                mCameraDevice.setParametersNoException(mParameters);
            }
        }

        if (VERBOSE) {
            Log.d(TAG, "try to preview with: " +
                    mPreviewSize.width + "x" +
                    mPreviewSize.height + " " +
                    mPreviewFps + "fps");
        }

        try {
            mFocusMode = CameraUtil.setFocusModeForCamera(mParameters);
            if (!mCameraDevice.setParametersNoException(mParameters)) {
                Log.e(TAG, "setFocuseMode failed");
                mParameters = mCameraDevice.getParameters();
            }
        } catch (Exception e) {
            Log.e(TAG, "setFocuseMode failed");
        }

        try {
            CameraUtil.setVideoStabilization(mParameters);
            if (!mCameraDevice.setParametersNoException(mParameters)) {
                Log.e(TAG, "setVideoStabilization failed");
                mParameters = mCameraDevice.getParameters();
            }
        } catch (Exception e) {
            Log.e(TAG, "setVideoStabilization failed");
        }

        try {
            CameraUtil.setAntibanding(mParameters);
            if (!mCameraDevice.setParametersNoException(mParameters)) {
                Log.e(TAG, "setAntibanding failed");
                mParameters = mCameraDevice.getParameters();
                mCameraDevice.setParametersNoException(mParameters);
            }
        } catch (Exception e) {
            Log.e(TAG, "setAntibanding failed");
        }

        if (VERBOSE) {
            int[] fps = new int[2];
            mParameters.getPreviewFpsRange(fps);
            Log.d(TAG, "Preview with: \n" +
                    mParameters.getPreviewSize().width + "x" +
                    mParameters.getPreviewSize().height + " " +
                    fps[0] / 1000.f + "-" + fps[1] / 1000.f + "fps" + "\n" +
                    "FocusMode: " + mParameters.getFocusMode() + "\n" +
                    "VideoStabilization: " + mParameters.getVideoStabilization() + "\n" +
                    "Antibanding: " + mParameters.getAntibanding()
            );
        }
    }

    private void startPreview() {
        setDisplayOrientation();
        mCameraDevice.setPreviewCallbackWithBuffer(cameraPreviewCallback);
        int bufferSize = mPreviewSize.width * mPreviewSize.height * 3 / 2;
        if (mPreviewBuf == null || mPreviewBuf.length != bufferSize) {
            mPreviewBuf = new byte[bufferSize];
        }
        mCameraDevice.addCallbackBuffer(mPreviewBuf);
        if (mSurfaceTexture != null) {
            mCameraDevice.setPreviewTexture(mSurfaceTexture);
            mCameraDevice.startPreview();
        }

        if (!TextUtils.isEmpty(mFocusMode) &&
                mFocusMode.equals(Camera.Parameters.FOCUS_MODE_AUTO)) {
            mCameraDevice.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    mCameraDevice.cancelAutoFocus();
                }
            });
        }
    }

    private void setDisplayOrientation() {
        int ori = CameraUtil.getDisplayOrientation(mOrientationDegrees, getCameraId(mFacing));
        mCameraDevice.setDisplayOrientation(ori);
    }

    private static class MainHandler extends Handler {
        private final WeakReference<CameraCapture> weakCapture;

        MainHandler(CameraCapture cameraCapture, Looper looper) {
            super(looper);
            this.weakCapture = new WeakReference<>(cameraCapture);
        }

        @Override
        public void handleMessage(Message msg) {
            CameraCapture cameraCapture = weakCapture.get();
            if (cameraCapture == null) {
                return;
            }
            switch (msg.what) {
                case CAMERA_OPEN_DONE: {
                    break;
                }
                case START_PREVIEW_DONE: {
                    if (VERBOSE) Log.d(TAG, "Camera preview started");
                    if (cameraCapture.mOnCameraCaptureListener != null) {
                        cameraCapture.mOnCameraCaptureListener.onStarted();
                    }
                    break;
                }
                case FIRST_FRAME_RENDERED: {
                    if (VERBOSE) Log.d(TAG, "Camera first frame rendered");
                    if (cameraCapture.mOnCameraCaptureListener != null) {
                        cameraCapture.mOnCameraCaptureListener.onFirstFrameRendered();
                    }
                    break;
                }
                case SWITCH_CAMERA_DONE: {
                    if (VERBOSE) Log.d(TAG, "Camera switched");
                    if (cameraCapture.mOnCameraCaptureListener != null) {
                        cameraCapture.mOnCameraCaptureListener.
                                onFacingChanged(cameraCapture.mFacing);
                    }
                    break;
                }
                case CLOSE_CAMERA_DONE: {
                    if (VERBOSE) Log.d(TAG, "Camera closed");
                    break;
                }
                case CAMERA_FAILED: {
                    cameraCapture.stop();
                    cameraCapture.mState.set(CAMERA_STATE_IDLE);
                    if (cameraCapture.mOnCameraCaptureListener != null) {
                        cameraCapture.mOnCameraCaptureListener.onError(msg.arg1);
                    }
                    break;
                }
                default: {
                    break;
                }
            }
        }
    }

    private GLRender.OnReadyListener mGLReadyListener = new GLRender.OnReadyListener() {
        @Override
        public void onReady() {
            if (VERBOSE) Log.d(TAG, "onGLContext ready");
            mTextureId = GlUtil.createOESTextureObject();
            synchronized (mCameraLock) {
                if (mSurfaceTexture != null) {
                    mSurfaceTexture.release();
                }
                mSurfaceTexture = new SurfaceTexture(mTextureId);
                mSurfaceTexture.setOnFrameAvailableListener(CameraCapture.this);
                if (mCameraDevice != null) {
                    mCameraDevice.setPreviewTexture(mSurfaceTexture);
                    mCameraDevice.startPreviewAsync();
                }
            }
            mTexInited = false;
            mFrameAvailable = false;
        }
    };

    private GLRender.OnSizeChangedListener mGLSizeChangedListener =
            new GLRender.OnSizeChangedListener() {
                @Override
                public void onSizeChanged(int width, int height) {
                    if (VERBOSE) Log.d(TAG, "onSizeChanged " + width + "x" + height);
                }
            };

    private GLRender.OnDrawFrameListener mGLDrawFrameListener = new GLRender.OnDrawFrameListener() {
        @Override
        public void onDrawFrame() {
            long pts = System.nanoTime() / 1000 / 1000;

            try {
                mSurfaceTexture.updateTexImage();
            } catch (Exception e) {
                Log.e(TAG, "updateTexImage failed, ignore");
                return;
            }

            if (mState.get() != CAMERA_STATE_PREVIEWING || !mFrameAvailable) {
                return;
            }

            if (!mTexInited) {
                mTexInited = true;
                mFpsLimiter.init(mPresetPreviewFps, pts);
                init();
            }

            if (mFpsLimiter.needDrop(pts) && mEnableFrameDrop) {
                if (TRACE_FPS_LIMIT) {
                    Log.d(TAG, "--- " + pts);
                }
                return;
            }
            if (mEnableFrameDrop && TRACE_FPS_LIMIT) {
                Log.d(TAG, "*** " + pts);
            }

            float[] texMatrix = new float[16];
            mSurfaceTexture.getTransformMatrix(texMatrix);
            ImgTexFrame frame = new ImgTexFrame(mImgTexFormat, mTextureId, texMatrix, pts);
            try {
                mImgTexSrcPin.onFrameAvailable(frame);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Draw frame failed, ignore");
            }

            if (!mFirstFrameRendered) {
                mMainHandler.sendEmptyMessage(FIRST_FRAME_RENDERED);
                mFirstFrameRendered = true;
            }

            // cal preview fps
            mFrameDrawn++;
            long tm = System.currentTimeMillis();
            long tmDiff = tm - mLastTraceTime;
            if (tmDiff >= 1000) {
                mCurrentFps = mFrameDrawn * 1000.f / tmDiff;
                if (TRACE_FPS_LIMIT) {
                    Log.d(TAG, "preview fps: " + String.format(Locale.getDefault(),
                            "%.2f", mCurrentFps));
                }
                mFrameDrawn = 0;
                mLastTraceTime = tm;
            }
        }

        private void init() {
            int ori = CameraUtil.getDisplayOrientation(mOrientationDegrees, getCameraId(mFacing));
            int width = mPreviewSize.width;
            int height = mPreviewSize.height;
            if (ori % 180 != 0) {
                //noinspection SuspiciousNameCombination
                width = mPreviewSize.height;
                //noinspection SuspiciousNameCombination
                height = mPreviewSize.width;
            }
            mImgTexFormat = new ImgTexFormat(ImgTexFormat.COLOR_EXTERNAL_OES, width, height);
            mImgTexSrcPin.onFormatChanged(mImgTexFormat);

            // cal preview fps
            mLastTraceTime = System.currentTimeMillis();
            mFrameDrawn = 0;
            mCurrentFps = 0;

            // every camera open/switch, notify first frame rendered
            mFirstFrameRendered = false;
        }
    };

    private GLRender.OnReleasedListener mGLReleasedListener = new GLRender.OnReleasedListener() {
        @Override
        public void onReleased() {
            if (VERBOSE) Log.d(TAG, "onGLContext released");
            mFrameAvailable = false;
            synchronized (mCameraLock) {
                if (mCameraDevice != null) {
                    mCameraDevice.stopPreview();
                }
                if (mSurfaceTexture != null) {
                    mSurfaceTexture.setOnFrameAvailableListener(null);
                    mSurfaceTexture.release();
                    mSurfaceTexture = null;
                }
            }
        }
    };

    private Camera.ErrorCallback cameraErrorCallback = new Camera.ErrorCallback() {
        @Override
        public void onError(int error, Camera camera) {
            Log.e(TAG, "onCameraError: " + error);
            int code;
            switch (error) {
                case Camera.CAMERA_ERROR_SERVER_DIED:
                    code = CAMERA_ERROR_SERVER_DIED;
                    break;
                case Camera.CAMERA_ERROR_EVICTED:
                    code = CAMERA_ERROR_EVICTED;
                    break;
                default:
                    code = CAMERA_ERROR_UNKNOWN;
            }

            Message msg = mMainHandler.obtainMessage(CAMERA_FAILED, code, 0);
            mMainHandler.sendMessage(msg);
        }
    };

    private Camera.PreviewCallback cameraPreviewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (mImgBufSrcPin.isConnected() && data != null) {
                long recordedTime = System.nanoTime() / 1000 / 1000;

                if (mVideoDirectBuffer == null) {
                    mVideoDirectBuffer = ByteBuffer.allocateDirect(data.length);
                }

                if (mVideoDirectBuffer.capacity() < data.length) {
                    mVideoDirectBuffer = null;
                    mVideoDirectBuffer = ByteBuffer.allocateDirect(data.length);
                }

                mVideoDirectBuffer.clear();
                mVideoDirectBuffer.put(data);

                try {
                    if (!mBufInited) {
                        int ori = CameraUtil.getDisplayOrientation(mOrientationDegrees, mFacing);
                        if (mFacing == FACING_FRONT) {
                            ori = (360 - ori) % 360;
                        }
                        mImgBufFormat = new ImgBufFormat(AVConst.PIX_FMT_NV21,
                                mPreviewSize.width, mPreviewSize.height, ori);
                        mBufInited = true;
                        mImgBufSrcPin.onFormatChanged(mImgBufFormat);
                    }

                    ImgBufFrame frame = new ImgBufFrame(mImgBufFormat,
                            mVideoDirectBuffer, recordedTime);
                    mImgBufSrcPin.onFrameAvailable(frame);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (mCameraDevice != null) {
                mCameraDevice.addCallbackBuffer(data);
            }
        }
    };

    public static class Size {
        public final int width;
        public final int height;

        public Size(int width, int height) {
            this.width = width;
            this.height = height;
        }

        @Override
        public String toString() {
            return "Size{" +
                    "height=" + height +
                    ", width=" + width +
                    "}";
        }
    }
}
