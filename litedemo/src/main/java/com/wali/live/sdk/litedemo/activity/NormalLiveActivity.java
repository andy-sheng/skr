package com.wali.live.sdk.litedemo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.WindowManager;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mi.liveassistant.camera.CameraView;
import com.mi.liveassistant.data.model.User;
import com.mi.liveassistant.room.manager.live.NormalLiveManager;
import com.mi.liveassistant.room.user.UserInfoManager;
import com.wali.live.sdk.litedemo.R;
import com.wali.live.sdk.litedemo.base.activity.RxActivity;

/**
 * Created by chenyong on 2017/4/28.
 */

public class NormalLiveActivity extends RxActivity {
    private NormalLiveManager mLiveManager;

    private CameraView mCameraView;

    private SimpleDraweeView mAnchorDv;
    private TextView mAnchorTv;

    private UserInfoManager mUserManager;

    private long mAnchorId;
    private User mAnchor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal_live);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        initView();
        initManager();
    }

    private void initView() {
        mCameraView = $(R.id.camera_view);
    }

    private void initManager() {
        mLiveManager = new NormalLiveManager(mCameraView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLiveManager.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLiveManager.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLiveManager.destroy();
    }

    public static void openActivity(Activity activity) {
        Intent intent = new Intent(activity, NormalLiveActivity.class);
        activity.startActivity(intent);
    }
}
