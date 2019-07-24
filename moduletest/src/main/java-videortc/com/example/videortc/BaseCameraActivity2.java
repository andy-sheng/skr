package com.example.videortc;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
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

import com.wali.live.moduletest.R;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Base streaming activity.
 */
//@Route(path = RouterConstants.ACTIVITY_TEST_VIDEO)
public class BaseCameraActivity2 extends Activity implements
        ActivityCompat.OnRequestPermissionsResultCallback {
    public static final String TAG = "BaseCameraActivity";

    protected static final int PERMISSION_REQUEST_CAMERA_AUDIOREC = 1;
    protected static final String START_STREAM = "开始直播";
    protected static final String STOP_STREAM = "停止直播";

    protected GLSurfaceView mGLSurfaceView;
//    protected TextureView mTextureView;
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

    protected BytedEffectKit1 mBytedEffectKit;
    protected Handler mMainHandler;
    protected Timer mTimer;

    protected String mSdcardPath = Environment.getExternalStorageDirectory().getPath();
    protected String mLogoPath = "file://" + mSdcardPath + "/test.png";
    protected String mBgImagePath = "assets://bg.jpg";

    protected int mVersionCode = 0;
    private UnzipTask mUnzipTask;
    public static final String LICENSE_NAME = "labcv_test_20190523_20190630_com.bytedance.labcv.demo_labcv_test_v2.4.0.licbag";

    public static void startActivity(Context context, Class<?> cls) {
        Intent intent = new Intent(context, cls);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    protected int getLayoutId() {
        return R.layout.base_camera_activity;
    }

    protected void setDisplayPreview() {
        mBytedEffectKit.setDisplayPreview(mGLSurfaceView);
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

        mMainHandler = new Handler();
        mBytedEffectKit = new BytedEffectKit1(getApplicationContext());
        initUI();
        config();
        enableBeautyFilter();

        startDebugInfoTimer();

        ResourceHelper.init(getApplicationContext());
        if (!ResourceHelper.isResourceReady(this, mVersionCode)) {
            mUnzipTask = new UnzipTask(this);
            mUnzipTask.execute(ResourceHelper.ResourceZip);
        } else {
            mMainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setBytedEffect();
                }
            }, 2000);
        }
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
//        mTextureView = findViewById(R.id.texture_view);
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
        // 设置预览View
        setDisplayPreview();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBytedEffectKit.onResume();
        startCameraPreviewWithPermCheck();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBytedEffectKit.onPause();
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
        mBytedEffectKit.release();
    }

    //start streaming
    protected void startStream() {
        mStreamingText.setText(STOP_STREAM);
        mStreamingText.postInvalidate();
        mStreaming = true;
    }

    // stop streaming
    protected void stopStream() {
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
        if (mBytedEffectKit == null) return;
        mDebugInfo = String.format(Locale.getDefault(), " " +
                        "PreviewFps=%.2f \n ",
                0.f);
//                mBytedEffectKit.getCurrentPreviewFps());
    }

    protected void onStartStreamClick() {
        if (mStreaming) {
            stopStream();
        } else {
            startStream();
        }
    }

    protected void saveBitmap(Bitmap bitmap, final String path) {
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(path));
            if (bitmap != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos);
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "保存截图到 " +
                                path, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "保存截图失败",
                            Toast.LENGTH_SHORT).show();
                }
            });
        } finally {
            if (bos != null) try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void onSwitchCamera() {
        // 切换前后摄像头
//        mEngineKit.switchCamera();
    }

    protected void onFlashClick() {
        if (mIsFlashOpened) {
            // 关闭闪光灯
//            mEngineKit.toggleTorch(false);
            mIsFlashOpened = false;

            mBytedEffectKit.stopEffect();
        } else {
            // 开启闪光灯
//            mEngineKit.toggleTorch(true);
            mIsFlashOpened = true;

            mBytedEffectKit.startEffect();
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
//            mBytedEffectKit.startPreview();
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
//                    mBytedEffectKit.startPreview();
                } else {
                    Log.e(TAG, "No CAMERA or AudioRecord permission");
                    Toast.makeText(getApplicationContext(), "No CAMERA or AudioRecord permission",
                            Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    private void setBytedEffect() {
        mBytedEffectKit.start();
    }

    class UnzipTask extends AsyncTask<String, Void, Boolean> {
        private Context mContext;

        public UnzipTask(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            String zipPath = strings[0];
            File dstFile = mContext.getExternalFilesDir("assets");

            return FileUtils.unzipAssetFile(mContext, zipPath, dstFile);
        }

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "onPreExecute");
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            Log.d(TAG, "onPostExecute");
            ResourceHelper.setResourceReady(mContext, aBoolean, mVersionCode);
            setBytedEffect();
        }
    }
}
