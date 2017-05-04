package com.wali.live.sdk.litedemo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.mi.liveassistant.camera.CameraView;
import com.mi.liveassistant.data.model.User;
import com.mi.liveassistant.room.manager.live.NormalLiveManager;
import com.mi.liveassistant.room.manager.live.callback.ILiveCallback;
import com.mi.liveassistant.room.user.UserInfoManager;
import com.mi.liveassistant.room.user.callback.IUserCallback;
import com.wali.live.sdk.litedemo.R;
import com.wali.live.sdk.litedemo.base.activity.RxActivity;
import com.wali.live.sdk.litedemo.topinfo.anchor.TopAnchorView;
import com.wali.live.sdk.litedemo.utils.ToastUtils;

/**
 * Created by chenyong on 2017/4/28.
 */

public class NormalLiveActivity extends RxActivity implements View.OnClickListener {
    /*开播流程*/
    private NormalLiveManager mLiveManager;
    private CameraView mCameraView;
    private Button mNormalLiveBtn;
    private boolean mIsBegin;

    /*主播信息*/
    private UserInfoManager mUserManager;
    private TopAnchorView mAnchorView;
    private long mPlayerId;
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
        mNormalLiveBtn = $(R.id.normal_live_btn);
        mNormalLiveBtn.setOnClickListener(this);

        mAnchorView = $(R.id.anchor_view);
    }

    private void initManager() {
        mLiveManager = new NormalLiveManager(mCameraView);
        mUserManager = new UserInfoManager();
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.normal_live_btn:
                clickNormalBtn();
                break;
        }
    }

    private void clickNormalBtn() {
        if (mIsBegin) {
            endLive();
        } else {
            beginLive();
        }
    }

    private void beginLive() {
        ToastUtils.showToast("begin normal live ...");
        mLiveManager.beginLive(null, "TEST", null, new ILiveCallback() {
            @Override
            public void notifyFail(int errCode) {
                ToastUtils.showToast("begin normal live fail=" + errCode);
            }

            @Override
            public void notifySuccess(long playerId) {
                ToastUtils.showToast("begin normal live success");
                mIsBegin = true;
                mNormalLiveBtn.setText("end normal live");

                mPlayerId = playerId;
                initAnchor();
            }
        });
    }

    private void endLive() {
        ToastUtils.showToast("end normal live ...");
        mLiveManager.endLive(new ILiveCallback() {
            @Override
            public void notifyFail(int errCode) {
                ToastUtils.showToast("end normal live fail=" + errCode);
            }

            @Override
            public void notifySuccess(long playerId) {
                ToastUtils.showToast("end normal live success");
                mIsBegin = false;
                mNormalLiveBtn.setText("begin normal live");
            }
        });
    }

    private void initAnchor() {
        mUserManager = new UserInfoManager();
        mUserManager.asyncUserByUuid(mPlayerId, new IUserCallback() {
            @Override
            public void notifyFail(int errCode) {
            }

            @Override
            public void notifySuccess(User user) {
                mAnchor = user;
                mAnchorView.updateAnchor(mAnchor);
            }
        });
    }

    public static void openActivity(Activity activity) {
        Intent intent = new Intent(activity, NormalLiveActivity.class);
        activity.startActivity(intent);
    }
}
