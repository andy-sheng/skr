package com.example.videortc;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.engine.EngineEvent;
import com.engine.Params;
import com.module.RouterConstants;
import com.wali.live.moduletest.R;
import com.zq.mediaengine.capture.CameraCapture;
import com.zq.mediaengine.kit.ZqEngineKit;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Base streaming activity.
 */
@Route(path = RouterConstants.ACTIVITY_TEST_VIDEO)
public class BaseCameraActivity extends Activity implements
        ActivityCompat.OnRequestPermissionsResultCallback {
    public static final String TAG = "BaseCameraActivity";

    protected static final int PERMISSION_REQUEST_CAMERA_AUDIOREC = 1;
    protected static final String START_STREAM = "开始直播";
    protected static final String STOP_STREAM = "停止直播";

    protected GLSurfaceView mGLSurfaceView;
    protected Chronometer mChronometer;
    protected ImageView mSwitchCameraView;
    protected ImageView mFlashView;
    protected TextView mUrlTextView;
    protected TextView mStreamingText;
    protected TextView mDebugInfoTextView;

    protected boolean mIsLandscape;
    protected boolean mIsFlashOpened;
    protected boolean mStreaming;
    protected boolean mIsChronometerStarted;
    protected String mDebugInfo = "";

    protected ZqEngineKit mEngineKit;
    protected Handler mMainHandler;
    protected Timer mTimer;

    protected String mSdcardPath = Environment.getExternalStorageDirectory().getPath();
    protected String mLogoPath = "file://" + mSdcardPath + "/test.png";
    protected String mBgImagePath = "assets://bg.jpg";

    public static void startActivity(Context context, Class<?> cls) {
        Intent intent = new Intent(context, cls);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    protected int getLayoutId() {
        return R.layout.base_camera_activity;
    }

    protected void setDisplayPreview() {
        mEngineKit.setDisplayPreview(mGLSurfaceView);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(getLayoutId());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // 4.4以上系统，自动隐藏导航栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

        EventBus.getDefault().register(this);

        mMainHandler = new Handler();
        mEngineKit = ZqEngineKit.getInstance();
        Params params = Params.getFromPref();
        params.setScene(Params.Scene.grab);
        mEngineKit.init("video_test", params);
        initUI();
        config();
        enableBeautyFilter();

        startDebugInfoTimer();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // 4.4以上系统，自动隐藏导航栏
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
        }
    }

    protected void initUI() {
        mGLSurfaceView = findViewById(R.id.gl_surface_view);
        mChronometer = findViewById(R.id.chronometer);
        mSwitchCameraView = findViewById(R.id.switch_cam);
        mFlashView = findViewById(R.id.flash);
        mUrlTextView = findViewById(R.id.url);
        mStreamingText = findViewById(R.id.start_stream_tv);
        mDebugInfoTextView = findViewById(R.id.debug_info);

        mStreamingText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStartStreamClick();
            }
        });

        mSwitchCameraView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSwitchCamera();
            }
        });

        mFlashView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFlashClick();
            }
        });
    }

    protected void config() {
        // 设置推流分辨率
        mEngineKit.setPreviewResolution(ZqEngineKit.VIDEO_RESOLUTION_720P);
        mEngineKit.setTargetResolution(ZqEngineKit.VIDEO_RESOLUTION_360P);

        // 设置推流帧率
        mEngineKit.setPreviewFps(30);
        mEngineKit.setTargetFps(30);

        // 设置视频方向（横屏、竖屏）
        mIsLandscape = false;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mEngineKit.setRotateDegrees(0);

        // 选择前后摄像头
        mEngineKit.setCameraFacing(CameraCapture.FACING_FRONT);

        // 设置预览View
        setDisplayPreview();
    }

    protected void handleOnResume() {
        startCameraPreviewWithPermCheck();
    }

    protected void handleOnPause() {
        mEngineKit.stopCameraPreview();
    }

    @Override
    protected void onResume() {
        super.onResume();
        handleOnResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handleOnPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清理相关资源
        if (mMainHandler != null) {
            mMainHandler.removeCallbacksAndMessages(null);
            mMainHandler = null;
        }
        if (mTimer != null) {
            mTimer.cancel();
        }
        mEngineKit.destroy("video_test");
    }

    //start streaming
    protected void startStream() {
        mEngineKit.joinRoom("bul", 0, true, null);
        mStreamingText.setText(STOP_STREAM);
        mStreamingText.postInvalidate();
        mStreaming = true;
    }

    // stop streaming
    protected void stopStream() {
        mEngineKit.leaveChannel();
        mStreamingText.setText(START_STREAM);
        mStreamingText.postInvalidate();
        mStreaming = false;
        stopChronometer();
    }

    protected void enableBeautyFilter() {
        // TODO:
    }

    private void startDebugInfoTimer() {
        if (mTimer == null) {
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    updateDebugInfo();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mDebugInfoTextView.setText(mDebugInfo);
                        }
                    });
                }
            }, 1000, 1000);
        }
    }

    // update debug info
    private void updateDebugInfo() {
        if (mEngineKit == null) return;
        mDebugInfo = String.format(Locale.getDefault(), " " +
                        "PreviewFps=%.2f \n ",
                mEngineKit.getCurrentPreviewFps());
    }

    protected void onStartStreamClick() {
        if (mStreaming) {
            stopStream();
        } else {
            startStream();
        }
    }

    protected void onSwitchCamera() {
        // 切换前后摄像头
        mEngineKit.switchCamera();
    }

    protected void onFlashClick() {
        if (mIsFlashOpened) {
            // 关闭闪光灯
            mEngineKit.toggleTorch(false);
            mIsFlashOpened = false;
        } else {
            // 开启闪光灯
            mEngineKit.toggleTorch(true);
            mIsFlashOpened = true;
        }
    }

    protected void startChronometer() {
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.start();
        mIsChronometerStarted = true;
    }

    protected void stopChronometer() {
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.stop();
        mIsChronometerStarted = false;
    }

    protected void startCameraPreviewWithPermCheck() {
        int cameraPerm = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int audioPerm = ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        if (cameraPerm != PackageManager.PERMISSION_GRANTED ||
                audioPerm != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                Log.e(TAG, "No CAMERA or AudioRecord permission, please check");
                Toast.makeText(getApplicationContext(), "No CAMERA or AudioRecord permission, please check",
                        Toast.LENGTH_LONG).show();
            } else {
                String[] permissions = {Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_PHONE_STATE};
                ActivityCompat.requestPermissions(this, permissions,
                        PERMISSION_REQUEST_CAMERA_AUDIOREC);
            }
        } else {
            mEngineKit.startCameraPreview();
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(EngineEvent event) {
        Log.e(TAG, "onEvent " + event);
        if (event.getType() == EngineEvent.TYPE_FIRST_VIDEO_DECODED) {
            int userId = event.getUserStatus().getUserId();
            mEngineKit.setLocalVideoRect(0, 0.5f, 1.0f, 0.5f, 1.0f);
            mEngineKit.setRemoteVideoRect(userId,0, 0, 1.0f, 0.5f, 1.0f);
        } else if (event.getType() == EngineEvent.TYPE_USER_LEAVE) {
            int userId = event.getUserStatus().getUserId();
            mEngineKit.setLocalVideoRect(0, 0, 1.0f, 1.0f, 1.0f);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CAMERA_AUDIOREC: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mEngineKit.startCameraPreview();
                } else {
                    Log.e(TAG, "No CAMERA or AudioRecord permission");
                    Toast.makeText(getApplicationContext(), "No CAMERA or AudioRecord permission",
                            Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }
}
