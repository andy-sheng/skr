package com.zq.mediaengine.capture.camera;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.ErrorCallback;
import android.hardware.Camera.OnZoomChangeListener;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;

/**
 * @hide
 */
public class CameraManager {
    private static final String TAG = "CameraManager";
    private static CameraManager sCameraManager = new CameraManager();

    private ConditionVariable mSig = new ConditionVariable();

    private Parameters mParameters;
    private RuntimeException mSetParametersException;
    private IOException mReconnectException;

    private static final int RELEASE = 1;
    private static final int RECONNECT = 2;
    private static final int UNLOCK = 3;
    private static final int LOCK = 4;
    private static final int SET_PREVIEW_TEXTURE_ASYNC = 5;
    private static final int START_PREVIEW_ASYNC = 6;
    private static final int STOP_PREVIEW = 7;
    private static final int SET_PREVIEW_CALLBACK_WITH_BUFFER = 8;
    private static final int ADD_CALLBACK_BUFFER = 9;
    private static final int AUTO_FOCUS = 10;
    private static final int CANCEL_AUTO_FOCUS = 11;
    private static final int SET_DISPLAY_ORIENTATION = 12;
    private static final int SET_ZOOM_CHANGE_LISTENER = 13;
    private static final int SET_ERROR_CALLBACK = 14;
    private static final int SET_PARAMETERS = 15;
    private static final int GET_PARAMETERS = 16;
    private static final int SET_PARAMETERS_ASYNC = 17;
    private static final int WAIT_FOR_IDLE = 18;
    private static final int SET_PREVIEW_DISPLAY_ASYNC = 19;
    private static final int SET_PREVIEW_CALLBACK = 20;
    private static final int SET_PREVIEW_DISPLAY = 21;
    private static final int START_PREVIEW = 22;
    private static final int SET_PREVIEW_TEXTURE = 23;

    private Handler mCameraHandler;
    private CameraProxy mCameraProxy;
    private Camera mCamera;

    public static CameraManager instance() {
        return sCameraManager;
    }

    private CameraManager() {
        HandlerThread ht = new HandlerThread("Camera Handler Thread");
        ht.start();
        mCameraHandler = new CameraHandler(ht.getLooper());
    }

    private class CameraHandler extends Handler {
        CameraHandler(Looper looper) {
            super(looper);
        }

        private void setPreviewTexture(Object surfaceTexture) {
            try {
                mCamera.setPreviewTexture((SurfaceTexture) surfaceTexture);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void handleMessage(final Message msg) {
            // Log.v(TAG, "handleMessage:" + msg + "," + mCamera);
            if (msg == null || mCamera == null) {
                mSig.open();
                return;
            }
            try {
                switch (msg.what) {
                    case RELEASE:
                        mCamera.release();
                        mCamera = null;
                        mCameraProxy = null;
                        break;

                    case RECONNECT:
                        mReconnectException = null;
                        try {
                            mCamera.reconnect();
                        } catch (IOException ex) {
                            mReconnectException = ex;
                        }
                        break;

                    case UNLOCK:
                        mCamera.unlock();
                        break;

                    case LOCK:
                        mCamera.lock();
                        break;

                    case SET_PREVIEW_TEXTURE_ASYNC:
                        setPreviewTexture(msg.obj);
                        return; // no need to call mSig.open()

                    case SET_PREVIEW_DISPLAY_ASYNC:
                        try {
                            mCamera.setPreviewDisplay((SurfaceHolder) msg.obj);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        return; // no need to call mSig.open()

                    case SET_PREVIEW_TEXTURE:
                        setPreviewTexture(msg.obj);
                        break;

                    case SET_PREVIEW_DISPLAY:
                        try {
                            mCamera.setPreviewDisplay((SurfaceHolder) msg.obj);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        break;

                    case START_PREVIEW_ASYNC:
                        mCamera.startPreview();
                        return; // no need to call mSig.open()

                    case START_PREVIEW:
                        mCamera.startPreview();
                        break;

                    case STOP_PREVIEW:
                        mCamera.stopPreview();
                        break;

                    case SET_PREVIEW_CALLBACK_WITH_BUFFER:
                        mCamera.setPreviewCallbackWithBuffer((PreviewCallback) msg.obj);
                        break;

                    case ADD_CALLBACK_BUFFER:
                        mCamera.addCallbackBuffer((byte[]) msg.obj);
                        break;

                    case AUTO_FOCUS:
                        mCamera.autoFocus((AutoFocusCallback) msg.obj);
                        break;

                    case CANCEL_AUTO_FOCUS:
                        mCamera.cancelAutoFocus();
                        break;

                    case SET_DISPLAY_ORIENTATION:
                        mCamera.setDisplayOrientation(msg.arg1);
                        break;

                    case SET_ZOOM_CHANGE_LISTENER:
                        mCamera.setZoomChangeListener((OnZoomChangeListener) msg.obj);
                        break;

                    case SET_ERROR_CALLBACK:
                        mCamera.setErrorCallback((ErrorCallback) msg.obj);
                        break;

                    case SET_PARAMETERS:
                        mSetParametersException = null;
                        try {
                            mCamera.setParameters((Parameters) msg.obj);
                        } catch (RuntimeException e) {
                            mSetParametersException = e;
                        }
                        break;

                    case GET_PARAMETERS:
                        mParameters = mCamera.getParameters();
                        break;

                    case SET_PARAMETERS_ASYNC:
                        try {
                            mCamera.setParameters((Parameters) msg.obj);
                        } catch (RuntimeException e) {
                            Log.e(TAG, "Camera set parameters failed");
                        }
                        return; // no need to call mSig.open()

                    case SET_PREVIEW_CALLBACK:
                        mCamera.setPreviewCallback((PreviewCallback) msg.obj);
                        break;

                    case WAIT_FOR_IDLE:
                        // do nothing
                        break;
                    default:
                        throw new RuntimeException("Invalid CameraProxy message="
                                + msg.what);
                }
            } catch (RuntimeException e) {
                if (msg.what != RELEASE && mCamera != null) {
                    try {
                        mCamera.release();
                    } catch (Exception ex) {
                        Log.e(TAG, "Fail to release the camera.");
                    }
                    mCamera = null;
                    mCameraProxy = null;
                }
            }
            mSig.open();
        }
    }

    CameraProxy cameraOpen(int cameraId) {
        mCamera = Camera.open(cameraId);
        if (mCamera != null) {
            mCameraProxy = new CameraProxy();
            return mCameraProxy;
        } else {
            return null;
        }
    }

    public class CameraProxy {

        public void release() {
            mSig.close();
            mCameraHandler.sendEmptyMessage(RELEASE);
            mSig.block();
        }

        public void reconnect() throws IOException {
            mSig.close();
            mCameraHandler.sendEmptyMessage(RECONNECT);
            mSig.block();
            if (mReconnectException != null) {
                throw mReconnectException;
            }
        }

        public void unlock() {
            mSig.close();
            mCameraHandler.sendEmptyMessage(UNLOCK);
            mSig.block();
        }

        public void lock() {
            mSig.close();
            mCameraHandler.sendEmptyMessage(LOCK);
            mSig.block();
        }

        public void setPreviewTextureAsync(final SurfaceTexture surfaceTexture) {
            mCameraHandler.obtainMessage(SET_PREVIEW_TEXTURE_ASYNC, surfaceTexture).sendToTarget();
        }

        public void setPreviewTexture(final SurfaceTexture surfaceTexture) {
            mSig.close();
            mCameraHandler.obtainMessage(SET_PREVIEW_TEXTURE, surfaceTexture).sendToTarget();
            mSig.block();
        }

        public void setPreviewDisplayAsync(final SurfaceHolder surfaceHolder) {
            mCameraHandler.obtainMessage(SET_PREVIEW_DISPLAY_ASYNC, surfaceHolder).sendToTarget();
        }

        public void setPreviewDisplay(final SurfaceHolder surfaceHolder) {
            mSig.close();
            mCameraHandler.obtainMessage(SET_PREVIEW_DISPLAY, surfaceHolder).sendToTarget();
            mSig.block();
        }

        public void startPreviewAsync() {
            mCameraHandler.sendEmptyMessage(START_PREVIEW_ASYNC);
        }

        public void startPreview() {
            mSig.close();
            mCameraHandler.sendEmptyMessage(START_PREVIEW);
            mSig.block();
        }

        public void stopPreview() {
            mSig.close();
            mCameraHandler.sendEmptyMessage(STOP_PREVIEW);
            mSig.block();
        }

        public void setPreviewCallbackWithBuffer(final PreviewCallback cb) {
            mSig.close();
            mCameraHandler.obtainMessage(SET_PREVIEW_CALLBACK_WITH_BUFFER, cb)
                    .sendToTarget();
            mSig.block();
        }

        public void addCallbackBuffer(byte[] callbackBuffer) {
            mSig.close();
            mCameraHandler.obtainMessage(ADD_CALLBACK_BUFFER, callbackBuffer)
                    .sendToTarget();
            mSig.block();
        }

        public void autoFocus(AutoFocusCallback cb) {
            mSig.close();
            mCameraHandler.obtainMessage(AUTO_FOCUS, cb).sendToTarget();
            mSig.block();
        }

        public void cancelAutoFocus() {
            mSig.close();
            mCameraHandler.sendEmptyMessage(CANCEL_AUTO_FOCUS);
            mSig.block();
        }

        public void takePicture(final ShutterCallback shutter,
                                final PictureCallback raw, final PictureCallback postview,
                                final PictureCallback jpeg) {
            mSig.close();
            mCameraHandler.post(new Runnable() {
                @Override
                public void run() {
                    mCamera.takePicture(shutter, raw, postview, jpeg);
                    mSig.open();
                }
            });
            mSig.block();
        }

        public void setDisplayOrientation(int degrees) {
            mSig.close();
            mCameraHandler.obtainMessage(SET_DISPLAY_ORIENTATION, degrees, 0)
                    .sendToTarget();
            mSig.block();
        }

        public void setZoomChangeListener(OnZoomChangeListener listener) {
            mSig.close();
            mCameraHandler.obtainMessage(SET_ZOOM_CHANGE_LISTENER, listener)
                    .sendToTarget();
            mSig.block();
        }

        public void setErrorCallback(ErrorCallback cb) {
            mSig.close();
            mCameraHandler.obtainMessage(SET_ERROR_CALLBACK, cb).sendToTarget();
            mSig.block();
        }

        public void setParameters(Parameters params) throws RuntimeException {
            mSig.close();
            mCameraHandler.obtainMessage(SET_PARAMETERS, params).sendToTarget();
            mSig.block();
            if (mSetParametersException != null) {
                throw mSetParametersException;
            }
        }

        public boolean setParametersNoException(Parameters params) {
            try {
                setParameters(params);
                return true;
            } catch (RuntimeException e) {
                Log.e(TAG, "Camera set parameters failed");
                return false;
            }
        }

        public void setParametersAsync(Parameters params) {
            mCameraHandler.removeMessages(SET_PARAMETERS_ASYNC);
            mCameraHandler.obtainMessage(SET_PARAMETERS_ASYNC, params).sendToTarget();
        }

        public Parameters getParameters() {
            mSig.close();
            mCameraHandler.sendEmptyMessage(GET_PARAMETERS);
            mSig.block();
            Parameters parameters = mParameters;
            mParameters = null;
            return parameters;
        }
    }
}
