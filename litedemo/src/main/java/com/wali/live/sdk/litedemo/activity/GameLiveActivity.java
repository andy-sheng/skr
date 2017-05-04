package com.wali.live.sdk.litedemo.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mi.liveassistant.avatar.AvatarUtils;
import com.mi.liveassistant.data.model.User;
import com.mi.liveassistant.room.manager.live.GameLiveManager;
import com.mi.liveassistant.room.manager.live.callback.ILiveCallback;
import com.mi.liveassistant.room.user.UserInfoManager;
import com.mi.liveassistant.room.user.callback.IUserCallback;
import com.wali.live.sdk.litedemo.R;
import com.wali.live.sdk.litedemo.base.activity.RxActivity;
import com.wali.live.sdk.litedemo.fresco.FrescoWorker;
import com.wali.live.sdk.litedemo.fresco.image.ImageFactory;
import com.wali.live.sdk.litedemo.global.GlobalData;
import com.wali.live.sdk.litedemo.utils.ToastUtils;

import static com.wali.live.sdk.litedemo.MainActivity.REQUEST_MEDIA_PROJECTION;

/**
 * Created by chenyong on 2017/4/28.
 */

public class GameLiveActivity extends RxActivity implements View.OnClickListener {
    private Button mGameLiveBtn;

    private SimpleDraweeView mAnchorDv;
    private TextView mAnchorTv;

    private GameLiveManager mLiveManager;
    private UserInfoManager mUserManager;

    private boolean mIsBegin;

    private long mPlayerId;
    private User mAnchor;

    private Intent mIntent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_live);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        initView();
        initManager();
    }

    private void initView() {
        mGameLiveBtn = $(R.id.game_live_btn);
        mGameLiveBtn.setOnClickListener(this);

        mAnchorDv = $(R.id.anchor_dv);
        mAnchorTv = $(R.id.anchor_tv);
    }

    private void initManager() {
        mLiveManager = new GameLiveManager();
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.game_live_btn:
                clickGameBtn();
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void clickGameBtn() {
        if (mIsBegin) {
            ToastUtils.showToast("end game live ...");
            mLiveManager.endLive(new ILiveCallback() {
                @Override
                public void notifyFail(int errCode) {
                    ToastUtils.showToast("end game live fail=" + errCode);
                }

                @Override
                public void notifySuccess(long playerId) {
                    ToastUtils.showToast("end game live success");
                    mIsBegin = false;
                    mGameLiveBtn.setText("begin game live");
                }
            });
        } else {
            if (mIntent == null) {
                ToastUtils.showToast("begin game live intent is null");
                startActivityForResult(
                        ((MediaProjectionManager) GlobalData.app()
                                .getSystemService(Context.MEDIA_PROJECTION_SERVICE)).createScreenCaptureIntent(),
                        REQUEST_MEDIA_PROJECTION);
                return;
            }
            beginLive();
        }
    }

    private void beginLive() {
        mLiveManager.setCaptureIntent(mIntent);
        ToastUtils.showToast("begin game live ...");
        mLiveManager.beginLive(null, "TEST", null, new ILiveCallback() {
            @Override
            public void notifyFail(int errCode) {
                ToastUtils.showToast("begin game live fail=" + errCode);
            }

            @Override
            public void notifySuccess(long playerId) {
                ToastUtils.showToast("begin game live success");
                mIsBegin = true;
                mGameLiveBtn.setText("end game live");

                mPlayerId = playerId;
                initAnchor();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.w(TAG, "onActivityResult " + requestCode + " resultCode=" + resultCode + "data =" + data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_MEDIA_PROJECTION:
                    mIntent = data;
                    beginLive();
                    break;
                default:
                    break;
            }
        } else if (requestCode == REQUEST_MEDIA_PROJECTION) {
            ToastUtils.showToast("media projection forbidden");
        }
    }

    public static void openActivity(Activity activity) {
        Intent intent = new Intent(activity, GameLiveActivity.class);
        activity.startActivity(intent);
    }
}
