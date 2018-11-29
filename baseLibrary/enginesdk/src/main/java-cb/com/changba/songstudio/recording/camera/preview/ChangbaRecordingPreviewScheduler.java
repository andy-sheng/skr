package com.changba.songstudio.recording.camera.preview;

import android.content.res.AssetManager;
import android.hardware.Camera.CameraInfo;
import android.util.Log;
import android.view.Surface;

import com.changba.songstudio.recording.camera.preview.ChangbaRecordingPreviewView.ChangbaRecordingPreviewViewCallback;
import com.changba.songstudio.recording.camera.preview.ChangbaVideoCamera.ChangbaVideoCameraCallback;
import com.changba.songstudio.video.encoder.MediaCodecSurfaceEncoder;
import com.common.log.MyLog;

public class ChangbaRecordingPreviewScheduler
        implements ChangbaVideoCameraCallback, ChangbaRecordingPreviewViewCallback {
    private static final String TAG = "ChangbaRecordingPreviewScheduler";
    private ChangbaRecordingPreviewView mPreviewView;
    private ChangbaVideoCamera mCamera;
    private int defaultCameraFacingId = CameraInfo.CAMERA_FACING_FRONT;
    // 是否是首次，确定是否创建 EGLContext
    private boolean hasPrepareEglFlag = false;
    private boolean hasCreateSurfaceWindow = false;

    // encoder
    protected MediaCodecSurfaceEncoder mSurfaceEncoder;
    private Surface mNativeSurface = null;
    // 是否在 stop状态
    private Surface mJavaSurface;
    private int width, height;


    public ChangbaRecordingPreviewScheduler(ChangbaRecordingPreviewView previewView, ChangbaVideoCamera camera) {
        this.mPreviewView = previewView;
        this.mCamera = camera;
        this.mPreviewView.setCallback(this);
        this.mCamera.setCallback(this);
    }

    public void resetStopState() {
    }

    public int getNumberOfCameras() {
        if (null != mCamera) {
            return mCamera.getNumberOfCameras();
        }
        return -1;
    }

    /**
     * 当切换视频滤镜的时候调用这个方法
     **/
    public void switchPreviewFilter(AssetManager assetManager, PreviewFilterType filterType) {
        switch (filterType) {
            case PREVIEW_COOL:
                switchPreviewFilter(filterType.getValue(), assetManager, "filter/cool_1.acv");
                break;
            case PREVIEW_THIN_FACE:
            case PREVIEW_NONE:
            case PREVIEW_ORIGIN:
            case PREVIEW_WHITENING:
            default:
                switchPreviewFilter(filterType.getValue(), assetManager, "");
                break;
        }
    }

    @Override
    public void createSurface(Surface surface, int width, int height) {
        // width height 为 SurfaceView 的宽高
        this.mJavaSurface = surface;
        this.width = width;
        this.height = height;
        startPreview("createSurface");
    }

    public void startPreview(String from) {
        MyLog.d(TAG, "startPreview from:" + from + " mJavaSurface:" + mJavaSurface +" hasPrepareEglFlag:"+hasPrepareEglFlag);
        if (mJavaSurface != null) {
            if (!hasPrepareEglFlag) {
                prepareEGLContext(mJavaSurface, width, height, defaultCameraFacingId);
                hasPrepareEglFlag = true;
            } else {
                createWindowSurface(mJavaSurface);
                hasCreateSurfaceWindow = true;
            }
        }
    }

    @Override
    public void destroySurface() {
        MyLog.d(TAG,"destroySurface" );
        if (hasPrepareEglFlag) {
            this.destroyEGLContext();
            hasPrepareEglFlag = false;
        }
        if (hasCreateSurfaceWindow) {
            this.destroyWindowSurface();
            hasCreateSurfaceWindow = false;
        }
        mJavaSurface = null;
    }

    public void stop() {
        MyLog.d(TAG,"stop" );
        // 会触发 destroySurface 回调
        this.stopEncoding();
    }

    @Override
    public void onPermissionDismiss(String tip) {
        Log.i("problem", "onPermissionDismiss : " + tip);
    }

    private CameraConfigInfo mConfigInfo;

    /**
     * 当底层创建好EGLContext之后，回调回来配置Camera，返回Camera的配置信息，然后在EGLThread线程中回调回来继续做Camera未完的配置以及Preview
     **/
    public CameraConfigInfo configCameraFromNative(int cameraFacingId) {
        defaultCameraFacingId = cameraFacingId;
        mConfigInfo = mCamera.configCameraFromNative(cameraFacingId);
        return mConfigInfo;
    }

    /**
     * 当底层EGLThread创建初纹理之后,设置给Camera
     **/
    public void startPreviewFromNative(int textureId) {
        mCamera.setCameraPreviewTexture(textureId);
    }

    /**
     * 当底层EGLThread更新纹理的时候调用这个方法
     **/
    public void updateTexImageFromNative() {
        mCamera.updateTexImage();
    }

    /**
     * 释放掉当前的Camera
     **/
    public void releaseCameraFromNative() {
        mCamera.releaseCamera();
    }

    public void onMemoryWarning(int queueSize) {
        Log.d("problem", "onMemoryWarning called");
    }

    public void createMediaCodecSurfaceEncoderFromNative(int width, int height, int bitRate, int frameRate) {
        try {
            mSurfaceEncoder = new MediaCodecSurfaceEncoder(width, height, bitRate, frameRate);
            mNativeSurface = mSurfaceEncoder.getInputSurface();
        } catch (Exception e) {
            Log.e("problem", "createMediaCodecSurfaceEncoder failed");
        }
    }

    public void hotConfigEncoderFromNative(int width, int height, int bitRate, int fps) {
        try {
            if (mSurfaceEncoder != null) {
                mSurfaceEncoder.hotConfig(width, height, bitRate, fps);
                mNativeSurface = mSurfaceEncoder.getInputSurface();
            }
        } catch (Exception e) {
            Log.e("problem", "hotConfigMediaCodecSurfaceEncoder failed");
        }
    }

    public long pullH264StreamFromDrainEncoderFromNative(byte[] returnedData) {
        return mSurfaceEncoder.pullH264StreamFromDrainEncoderFromNative(returnedData);
    }

    /**
     * 方法名都不能改，jni会通过反射调这个方法
     *
     * @param textureId
     */
    public void pushToRTCService(int textureId) {
        //DO Something
//        Log.e(TAG, "pushToRTCService " + textureId);
        mSurfaceEncoder.pushToRTCService(textureId);
    }

    public long getLastPresentationTimeUsFromNative() {
        return mSurfaceEncoder.getLastPresentationTimeUs();
    }

    public Surface getEncodeSurfaceFromNative() {
        return mNativeSurface;
    }

    public void reConfigureFromNative(int targetBitrate) {
        if (null != mSurfaceEncoder) {
            mSurfaceEncoder.reConfigureFromNative(targetBitrate);
        }
    }

    public void closeMediaCodecCalledFromNative() {
        if (null != mSurfaceEncoder) {
            mSurfaceEncoder.shutdown();
        }
    }

    @Override
    public native void resetRenderSize(int width, int height);

    @Override
    public native void updateTexMatrix(float texMatrix[]);

    /**
     * 当Camera捕捉到了新的一帧图像的时候会调用这个方法,因为更新纹理必须要在EGLThread中,
     * 所以配合下updateTexImageFromNative使用
     **/
    @Override
    public native void notifyFrameAvailable();

    public native void prepareEGLContext(Surface surface, int width, int height, int cameraFacingId);

    public native void createWindowSurface(Surface surface);

    public native void adaptiveVideoQuality(int maxBitRate, int avgBitRate, int fps);

    public native void hotConfigQuality(int maxBitrate, int avgBitrate, int fps);


    public native void destroyWindowSurface();

    public native void destroyEGLContext();

    public native void startEncoding(int width, int height, int videoBitRate, int frameRate, boolean useHardWareEncoding, int strategy);

    public native void stopEncoding();

    private native void switchPreviewFilter(int value, AssetManager ass, String filename);

    /**
     * 预览状态、录制状态、暂停录制状态
     **/
    public native void switchPauseRecordingPreviewState();

    public native void switchCommonPreviewState();

    /**
     * 切换摄像头, 底层会在返回来调用configCamera, 之后在启动预览
     **/
    public native void switchCameraFacing();

    public native void hotConfig(int bitRate, int fps, int gopSize);

    public native void setBeautifyParam(int key, float value);

}
