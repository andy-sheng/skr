package com.mi.liveassistant.unity;

import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.support.annotation.Keep;
import android.support.annotation.RequiresApi;

import com.mi.liveassistant.common.log.MyLog;
import com.unity3d.player.UnityPlayerActivity;

/**
 * Created by yangli on 2017/5/4.
 *
 * @module Unity直播Activity类
 */
public class UnityDemoActivity extends UnityPlayerActivity {
    private static final String TAG = "UnityDemoActivity";

    public static final int REQUEST_MEDIA_PROJECTION = 1000;

    private Intent mScreenRecordIntent;
    private IUnityListener mUnityListener;

    public void setUnityListener(final IUnityListener unityListener) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mUnityListener = unityListener;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mUnityListener != null) {
            mUnityListener.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mUnityListener != null) {
            mUnityListener.onPause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mUnityListener != null) {
            mUnityListener.onStop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUnityListener != null) {
            mUnityListener.onDestroy();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        MyLog.w(TAG, "onActivityResult " + requestCode + " resultCode=" + resultCode + "data =" + data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_MEDIA_PROJECTION:
                    mScreenRecordIntent = data;
                    if (mUnityListener != null) {
                        mUnityListener.onScreenRecordIntent(mScreenRecordIntent);
                    }
                    break;
                default:
                    break;
            }
        } else if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (mUnityListener != null) {
                mUnityListener.onScreenRecordIntent(null);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void queryScreenRecordIntent() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mUnityListener == null) {
                    return;
                }
                MyLog.w(TAG, "queryScreenRecordIntent");
                if (mScreenRecordIntent != null) {
                    mUnityListener.onScreenRecordIntent(mScreenRecordIntent);
                    return;
                }
                startActivityForResult(
                        ((MediaProjectionManager) getApplicationContext().getSystemService(
                                Context.MEDIA_PROJECTION_SERVICE)).createScreenCaptureIntent(),
                        REQUEST_MEDIA_PROJECTION);
            }
        });
    }

    public interface IUnityListener {
        void onResume();

        void onPause();

        void onStop();

        void onDestroy();

        void onScreenRecordIntent(Intent intent);
    }
}
