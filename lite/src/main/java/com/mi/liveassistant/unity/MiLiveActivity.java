package com.mi.liveassistant.unity;

import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Keep;
import android.support.annotation.RequiresApi;

import com.mi.liveassistant.common.log.MyLog;
import com.unity3d.player.UnityPlayerActivity;

/**
 * Created by yangli on 2017/5/4.
 *
 * @module Unity直播Activity类
 */
public class MiLiveActivity extends UnityPlayerActivity {
    private static final String TAG = "MiLiveActivity";

    public static final int REQUEST_MEDIA_PROJECTION = 1000;

    private Intent mScreenRecordIntent;
    private LiveForUnity mLiveForUnity;

    public void setLiveForUnity(LiveForUnity liveForUnity) {
        mLiveForUnity = liveForUnity;
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    @Keep
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        MyLog.w(TAG, "onActivityResult " + requestCode + " resultCode=" + resultCode + "data =" + data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_MEDIA_PROJECTION:
                    mScreenRecordIntent = data;
                    if (mLiveForUnity != null) {
                        mLiveForUnity.startLive(mScreenRecordIntent);
                    }
                    break;
                default:
                    break;
            }
        } else if (requestCode == REQUEST_MEDIA_PROJECTION) {
            // TODO 处理录屏启动失败
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void queryScreenRecordIntent() {
        MyLog.w(TAG, "queryScreenRecordIntent");
        if (mLiveForUnity == null) {
            return;
        }
        if (mScreenRecordIntent != null) {
            mLiveForUnity.startLive(mScreenRecordIntent);
            return;
        }
        startActivityForResult(
                ((MediaProjectionManager) getApplicationContext().getSystemService(
                        Context.MEDIA_PROJECTION_SERVICE)).createScreenCaptureIntent(),
                REQUEST_MEDIA_PROJECTION);
    }
}
