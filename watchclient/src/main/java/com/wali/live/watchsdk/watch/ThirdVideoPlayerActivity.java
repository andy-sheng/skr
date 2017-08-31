package com.wali.live.watchsdk.watch;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.WindowManager;

import com.base.event.SdkEventClass;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.log.MyLog;
import com.wali.live.component.BaseSdkView;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.base.BaseComponentSdkActivity;
import com.wali.live.watchsdk.videothird.ThirdVideoController;
import com.wali.live.watchsdk.videothird.ThirdVideoView;

import org.greenrobot.eventbus.Subscribe;

import static com.wali.live.component.BaseSdkController.MSG_ON_BACK_PRESSED;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;
import static com.wali.live.component.BaseSdkController.MSG_PLAYER_PAUSE;

/**
 * Created by yangli on 2017/8/28.
 *
 * @module 播放本地视频
 */
public class ThirdVideoPlayerActivity extends BaseComponentSdkActivity {

    private ThirdVideoController mController;
    private BaseSdkView mSdkView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (isMIUIV6()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        } else {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.third_video_player_layout);
        initData();
        initView();
    }

    private String parseVideoPath(@NonNull Context context, Uri uri) {
        Uri videoPathUri = uri;
        if (uri.getScheme().toString().compareTo("content") == 0) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                videoPathUri = Uri.parse(cursor.getString(column_index));
                return videoPathUri.getPath();
            }
        } else if (uri.getScheme().compareTo("file") == 0) {
            return videoPathUri.getPath();
        }
        return videoPathUri.toString();
    }

    private boolean parseVideoInfo() {
        Intent intent = getIntent();
        if (intent == null) {
            MyLog.e(TAG, "parseVideoInfo, but intent not found");
            return false;
        }
        String videoPath = parseVideoPath(this, intent.getData());
        if (TextUtils.isEmpty(videoPath)) {
            MyLog.e(TAG, "parseVideoInfo, but video path not found");
            return false;
        }
        mMyRoomData.setRoomId("0");
        mMyRoomData.setUid(0);
        mMyRoomData.setLiveType(-1);
        mMyRoomData.setVideoUrl(videoPath);
        return true;
    }

    private void initData() {
        if (!parseVideoInfo()) {
            MyLog.e(TAG, "video info not found");
            finish();
        }
    }

    private void initView() {
        mController = new ThirdVideoController(mMyRoomData);
        mController.setupController(this);
        ThirdVideoView compoundView = new ThirdVideoView(this, mController);
        compoundView.setupView();
        compoundView.startView();
        mSdkView = compoundView;
        openOrientation();
    }

    @Override
    protected void trySendDataWithServerOnce() {
    }

    @Override
    protected void tryClearData() {
    }

    @Override
    public void onKickEvent(String msg) {
    }

    @Override
    protected void onPause() {
        super.onPause();
        mController.postEvent(MSG_PLAYER_PAUSE);
    }

    @Override
    protected void onDestroy() {
        MyLog.w(TAG, "onDestroy");
        super.onDestroy();
        if (mController != null) {
            mController.release();
            mController = null;
        }
        if (mSdkView != null) {
            mSdkView.stopView();
            mSdkView.release();
            mSdkView = null;
        }
    }

    @Override
    public boolean isStatusBarDark() {
        return false;
    }

    @Subscribe
    public void onEvent(SdkEventClass.OrientEvent event) {
        if (event.isLandscape()) {
            orientLandscape();
        } else {
            orientPortrait();
        }
    }

    protected void orientLandscape() {
        if (mController != null) {
            mController.postEvent(MSG_ON_ORIENT_LANDSCAPE);
        }
    }

    protected void orientPortrait() {
        if (mController != null) {
            mController.postEvent(MSG_ON_ORIENT_PORTRAIT);
        }
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            // 退出栈弹出
            String fName = fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 1).getName();
            if (!TextUtils.isEmpty(fName)) {
                Fragment fragment = fm.findFragmentByTag(fName);
                MyLog.w(TAG, "fragment name=" + fName + ", fragment=" + fragment);
                if (fragmentBackPressed(fragment)) {
                    return;
                }
                FragmentNaviUtils.popFragmentFromStack(this);
                return;
            }
        } else if (mController != null && mController.postEvent(MSG_ON_BACK_PRESSED)) {
            return;
        }
        super.onBackPressed();
    }
}
