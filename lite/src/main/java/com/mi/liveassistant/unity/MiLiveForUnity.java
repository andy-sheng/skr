package com.mi.liveassistant.unity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.hardware.Camera;
import android.support.annotation.Keep;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.mi.liveassistant.engine.player.widget.VideoPlayerPresenter;
import com.mi.liveassistant.engine.player.widget.VideoPlayerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by yangli on 2017/4/22.
 *
 * @module Unity直播辅助类
 */
@Keep
public class MiLiveForUnity implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private static final String TAG = "MiLiveForUnity";

    private static final int MIN_PREVIEW_PIXELS = 320 * 240; // small screen
    private static final int MAX_PREVIEW_PIXELS = 800 * 480; // large/HD screen

    protected Activity mActivity;

    protected VideoPlayerView mSurfaceView;

    private static final int IDLE = 0;
    private static final int OPENING = 1;
    private static final int OPENED = 2;
    private static final int RELEASING = 3;

    public int mCameraStatus = IDLE;
    private Camera mCamera;
    private ExecutorService mCameraThread = Executors.newSingleThreadExecutor();

    private VideoPlayerPresenter mVideoPlayerPresenter;

    @Keep
    public MiLiveForUnity(Activity activity) {
        Log.e(TAG, "MiLiveForUnity");
        mActivity = activity;
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "MiLiveForUnity addContentView");
//                InitManager.init(mActivity.getApplication());

                LayoutInflater inflater = mActivity.getLayoutInflater();
                Resources resources = mActivity.getResources();
                String pkgName = mActivity.getPackageName();

                int id = resources.getIdentifier("layout_for_unity", "layout", pkgName);
                View view = inflater.inflate(id, null);

                for (int i = 0; i < ((ViewGroup) view).getChildCount(); ++i) {
                    View subView = ((ViewGroup) view).getChildAt(i);
                    if (subView instanceof VideoPlayerView) {
                        mSurfaceView = (VideoPlayerView) subView;
                        break;
                    }
                }
//                SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
//                surfaceHolder.addCallback(MiLiveForUnity.this);
//                surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                mSurfaceView.setZOrderOnTop(true);

                mVideoPlayerPresenter = mSurfaceView.getVideoPlayerPresenter();
                mVideoPlayerPresenter.setRealTime(true);

                FrameLayout.LayoutParams param = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                mActivity.addContentView(view, param);

                listView(mActivity.getWindow().getDecorView(), "");
            }
        });
    }

    private void listView(View view, String depth) {
        Log.e(TAG, depth + "|-" + view);
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); ++i) {
                listView(((ViewGroup) view).getChildAt(i), depth + "| ");
            }
        }
    }

    @Keep
    public void startLive() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String videoUrl = "http://v2.zb.mi.com/live/3243571_1493702092.flv?playui=0";
                mVideoPlayerPresenter.setVideoPath(videoUrl, "");
                mVideoPlayerPresenter.setIpList(new ArrayList<String>(), new ArrayList<String>());
                mVideoPlayerPresenter.setVideoStreamBufferTime(2);
                // openCameraAsync(mSurfaceView.getHolder());
                Log.e(TAG, "startLive done");
            }
        });
    }

    @Keep
    public void stopLive() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mVideoPlayerPresenter.pause();
                // releaseCameraAsync();
            }
        });
    }

    @Keep
    public void destroy() {
        mCameraThread.shutdown();
    }

    private void openCameraAsync(final SurfaceHolder holder) {
        Log.w(TAG, "openCameraAsync");
        if (mCameraStatus == OPENED || mCameraStatus == OPENING) {
            Log.w(TAG, "openCameraAsync, but camera already opened");
            return;
        }
        mCameraStatus = OPENING;
        Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                if (mCamera != null) {
                    mCamera.release();
                    mCamera = null;
                }
                try {
                    mCamera = Camera.open();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                subscriber.onNext(mCamera != null);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.from(mCameraThread))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean state) {
                        if (mCameraStatus != OPENING) {
                            return;
                        }
                        if (state) {
                            mCameraStatus = OPENED;
                            adjustCamera4PortraitDisplay();
                            adjustCameraPreviewSize();
                            startPreview(holder);
                            Log.w(TAG, "openCameraAsync success");
                        } else {
                            mCameraStatus = IDLE;
                            Log.e(TAG, "openCameraAsync failed");
                        }
                    }
                });
    }

    @SuppressLint("NewApi")
    private void adjustCamera4PortraitDisplay() {
        if (android.os.Build.VERSION.SDK_INT >= 8) {
            mCamera.setDisplayOrientation(90);
        } else {
            Camera.Parameters p = mCamera.getParameters();
            p.set("orientation", "portrait");
            mCamera.setParameters(p);
        }
    }

    private void releaseCameraAsync() {
        if (mCameraStatus == IDLE || mCameraStatus == RELEASING) {
            Log.w(TAG, "releaseCameraAsync, but camera is already released");
            return;
        }
        Log.w(TAG, "releaseCameraAsync");
        if (mCameraStatus == OPENED) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
        }
        mCameraStatus = RELEASING;
        Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                if (mCamera != null) {
                    mCamera.release();
                    mCamera = null;
                }
                subscriber.onNext(Boolean.TRUE);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.from(mCameraThread))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        if (mCameraStatus != RELEASING) {
                            return;
                        }
                        mCameraStatus = IDLE;
                        Log.w(TAG, "releaseCameraAsync success");
                    }
                });
    }

    private void startPreview(SurfaceHolder surfaceHolder) {
        try {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(surfaceHolder);
                mCamera.startPreview();
                mCamera.setPreviewCallback(this);
            } else {
                Log.w(TAG, "startPreview, but mCamera is null.");
            }
        } catch (IOException e) {
            Log.e(TAG, "startPreview, but init camera failed. " + e);
        } catch (RuntimeException e) {
            Log.e(TAG, "startPreview, start camera failed." + e);
        }
    }

    private void adjustCameraPreviewSize() {
        Camera.Parameters p = mCamera.getParameters();
        List<Camera.Size> supportedSizes = p.getSupportedPreviewSizes();
        Camera.Size bestSize = supportedSizes.get(0);
        int bestPixels = -1;
        for (Camera.Size size : supportedSizes) {
            int pixels = size.width * size.height;
            if (pixels < MIN_PREVIEW_PIXELS || pixels > MAX_PREVIEW_PIXELS) {
                continue;
            }
            if (pixels > bestPixels) {
                bestPixels = pixels;
                bestSize = size;
            }
        }
        p.setPreviewSize(bestSize.width, bestSize.height);
        mCamera.setParameters(p);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.e(TAG, "surfaceCreated");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.e(TAG, "surfaceChanged width=" + width + ", height=" + height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.e(TAG, "surfaceDestroyed");
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Log.e(TAG, "onPreviewFrame " + data.length);
    }
}
