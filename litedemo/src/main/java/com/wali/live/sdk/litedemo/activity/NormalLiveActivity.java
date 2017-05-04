package com.wali.live.sdk.litedemo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mi.liveassistant.avatar.AvatarUtils;
import com.mi.liveassistant.camera.CameraView;
import com.mi.liveassistant.data.model.User;
import com.mi.liveassistant.room.manager.live.NormalLiveManager;
import com.mi.liveassistant.room.manager.live.callback.ILiveCallback;
import com.mi.liveassistant.room.user.UserInfoManager;
import com.mi.liveassistant.room.user.callback.IUserCallback;
import com.wali.live.sdk.litedemo.R;
import com.wali.live.sdk.litedemo.base.activity.RxActivity;
import com.wali.live.sdk.litedemo.fresco.FrescoWorker;
import com.wali.live.sdk.litedemo.fresco.image.ImageFactory;
import com.wali.live.sdk.litedemo.utils.ToastUtils;

/**
 * Created by chenyong on 2017/4/28.
 */

public class NormalLiveActivity extends RxActivity implements View.OnClickListener {
    private Button mNormalLiveBtn;
    private NormalLiveManager mLiveManager;

    private CameraView mCameraView;

    private boolean mIsBegin;

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
        mNormalLiveBtn = $(R.id.normal_live_btn);
        mNormalLiveBtn.setOnClickListener(this);
        mCameraView = $(R.id.camera_view);
        mAnchorDv = $(R.id.anchor_dv);
        mAnchorTv = $(R.id.anchor_tv);
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

    public static void openActivity(Activity activity) {
        Intent intent = new Intent(activity, NormalLiveActivity.class);
        activity.startActivity(intent);
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
                initAnchor(playerId);
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

    private void initAnchor(long playerId) {
        mUserManager = new UserInfoManager();
        mUserManager.asyncUserByUuid(playerId, new IUserCallback() {
            @Override
            public void notifyFail(int errCode) {
            }

            @Override
            public void notifySuccess(User user) {
                mAnchor = user;
                updateAnchorView();
            }
        });
    }

    private void updateAnchorView() {
        mAnchorTv.setText(mAnchor.getNickname());

        String avatarUrl = AvatarUtils.getAvatarUrlByUid(mAnchor.getUid(), mAnchor.getAvatar());
        Log.d(TAG, "updateAnchorView avatarUrl=" + avatarUrl);
        FrescoWorker.loadImage(mAnchorDv, ImageFactory.newHttpImage(avatarUrl).setIsCircle(true).build());
    }
}
